;; lograte - calculate log message rate per second in a log file
(ns lograte.core
  (:require [clojure.java.io :as io]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.data.json :as json])
  (:gen-class))

;; Time format
(def timestamp-line-regex #"\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.*")
(def custom-formatter (f/formatter "yyyy-MM-dd HH:mm:ss"))
(defonce logging (memoize (fn []
                            (println "Checking the LOGGING env var")
                            (not (nil? (System/getenv "LOGGING"))))))

(defn log [& args]
  (and (logging)
       (println (format "%s - %s" (f/unparse custom-formatter (t/now)) (apply format args)))))

(defn to-timestamp
  "Match a line for an event timestamp. If not found, return nil, else just the real timestamp."
  [line]
  (if (re-matches timestamp-line-regex line)
    (f/parse custom-formatter (subs line 0 19))))

(defn analyze-event-rate
  "Lazy read the lines from this reader, calculating the events per second, and return the analysis
  including number of events recognized, the timestamp of the first and last, and average per second."
  [rdr]
  (let [result
        (merge {:count 0}
               ; Rip through once, never holding it all in memory
               (->> (line-seq rdr)
                    (map to-timestamp)
                    (reduce (fn [{n :count start :start end :end :as m} t]
                              (assoc m
                                :count (if (nil? t) n (inc n))
                                :start (or start t)
                                :end (or t end)
                                ))
                            {:count 0 :start nil :end nil})
                    ))
        duration (or (and (:start result)
                          (t/in-seconds (t/interval (:start result) (:end result))))
                     0)
        rate (float (/ (:count result) (max 1 duration)))]
    ; Return accumulated statistics and final average calculation
    (assoc result
      :start (.toString (or (:start result) ""))
      :end (.toString (or (:end result) ""))
      :rate-per-second rate
      :duration-in-seconds duration)))

(defn get-matching-files
  "Find all the files with full pathnames matching this pattern anywhere under this folder"
  [folder pattern]
  (filter #(and (re-matches (re-pattern pattern) (.getAbsolutePath %))
                (not (.isDirectory %))
                (not (.startsWith (.getName %) "."))
                (not (.endsWith (.getName %) ".zip"))
                (> (.length %) 0))
          (file-seq (io/file folder))))

(defn process-file
  "Open a log file from the given file name and return the event rate analysis"
  [file-name]
  (log "Analyzing %s" (str file-name))
  (with-open [rdr (io/reader file-name)]
    (analyze-event-rate rdr)))

(defn average-event-rate
  "Analyze each file the collection and reduce to a weighted average
     SUM(num-events)/SUM(duration)
   "
  [c]
  (reduce (fn [{old-count :count files :files total :total-rate highest :highest-rate}
               {new-count :count duration :duration-in-seconds rate :rate-per-second}]
            (log "\tmessages: %d, seconds: %d, rate: %f" new-count duration rate)
            (hash-map
              :files (inc files)
              :count (+ old-count new-count)
              :total-rate (+ total rate)
              :highest-rate (max highest rate)))
          {:files 0 :count 0 :total-rate 0 :highest-rate 0}
          (map process-file c)))

(defn -main [& args]
  (if (< 0 (count args) 3)
    (let [[file-name pattern] args]
      (println
        (json/write-str
          (if (.isDirectory (io/file file-name))
            (average-event-rate (get-matching-files file-name (or pattern ".*")))
            (process-file file-name)))))
    (println "Specify a file path or folder and file regex on the complete pathname")))
