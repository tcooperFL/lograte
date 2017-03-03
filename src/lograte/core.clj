;; lograte - calculate log message rate per second in a log file
(ns lograte.core
  (:require [clojure.java.io :as io]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.data.json :as json])
  (:gen-class))

;; Time formats
(def timestamp-line-regex #"\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}")
(def custom-formatter (f/formatter "yyyy-MM-dd HH:mm:ss"))

(def NCSA-timestamp-line-regex #"\d{2}/.{3}/\d{4}:\d{2}:\d{2}:\d{2}")
(def NCSA-custom-formatter (f/formatter "dd/MMM/yyyy:HH:mm:ss"))

(defonce logging (memoize (fn []
                            (println "Checking the LOGGING env var")
                            (not (nil? (System/getenv "LOGGING"))))))

(defn log [& args]
  (and (logging)
       (println (format "%s - %s" (f/unparse custom-formatter (t/now)) (apply format args)))))

(defn to-timestamp
  "Match a line for an event timestamp. If not found, return nil, else just the real timestamp.
  Ignore lines starting with a # as comments"
  [line]
  ; First try the more common timestamp, then if no lock, the NCSA format, sometimes used by IIS
  (if (not (.startsWith line "#"))
    (if-let [timestamp (re-find timestamp-line-regex line)]
      (f/parse custom-formatter timestamp)
      (if-let [timestamp (re-find NCSA-timestamp-line-regex line)]
        (f/parse NCSA-custom-formatter timestamp)))))

(defn analyze-event-rate
  "Lazy read the lines from this reader, calculating the events per second, and return the analysis
  including number of events recognized, the timestamp of the first and last, and average per second."
  [rdr]
  (let [result
        (merge {:message-count 0}
               ; Rip through once, never holding it all in memory
               (->> (line-seq rdr)
                    (map to-timestamp)
                    (reduce (fn [{n :message-count start :start end :end :as m} t]
                              (assoc m
                                :message-count (if (nil? t) n (inc n))
                                :start (or start t)
                                :end (or t end)
                                ))
                            {:message-count 0 :start nil :end nil})
                    ))
        duration (or (and (:start result)
                          (t/in-seconds (t/interval (:start result) (:end result))))
                     0)
        rate (float (/ (:message-count result) (max 1 duration)))]
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
                (not (.endsWith (.getName %) "NOTE.txt"))
                (> (.length %) 0))
          (file-seq (io/file folder))))

(defn process-file
  "Open a log file from the given file name and return the event rate analysis"
  [file-name]
  (log "Analyzing %s" (str file-name))
  (with-open [rdr (io/reader file-name)]
    (analyze-event-rate rdr)))

(defn process-multiple-files
  "Analyze each file the collection and reduce to a summary report containing
  the total file count, message count, highest single rate, and average rate.
  We can't know the combined rate profile unless we calibrate time stamps across
  files, which we don't do yet."
  [c]
  (let [analysis
        (reduce (fn [{old-file-count        :file-count
                      old-message-count     :message-count
                      old-combined-duration :combined-duration
                      highest-rate          :highest-rate}
                     {this-message-count :message-count
                      this-duration      :duration-in-seconds
                      this-rate          :rate-per-second}]
                  (log "\tmessages: %d, seconds: %d, rate: %f" this-message-count this-duration this-rate)
                  (hash-map
                    :file-count (inc old-file-count)
                    :message-count (+ old-message-count this-message-count)
                    :combined-duration (+ old-combined-duration this-duration)
                    :highest-rate (max highest-rate this-rate)))
                {:file-count 0 :message-count 0 :combined-duration 0 :highest-rate 0}
                (map process-file c))]
    (assoc analysis
      :average-rate (float (/ (:message-count analysis) (max 1 (:combined-duration analysis)))))))

(defn -main [& args]
  (if (< 0 (count args) 3)
    (let [[file-name pattern] args]
      (println
        (json/write-str
          (if (.isDirectory (io/file file-name))
            (process-multiple-files (get-matching-files file-name (or pattern ".*")))
            (process-file file-name)))))
    (println "Specify a file path or folder and file regex on the complete pathname")))
