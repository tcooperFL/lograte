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
             )
        duration (t/in-seconds (t/interval (:start result) (:end result)))
        rate (float (/ (:count result) (max 1 duration)))]
    ; Return accumulated statistics and final average calculation
    (assoc result
      :start (.toString (:start result))
      :end (.toString (:end result))
      :rate-per-second rate
      :duration-in-seconds duration)))

(defn process-file
  "Open a log file from the given file name and return the event rate analysis"
  [file-name]
  (with-open [rdr (io/reader file-name)]
    (analyze-event-rate rdr)))

(defn -main [& args]
  (println
    (if (or (empty? args) (not-empty (rest args)))
      "Specify a log file path or \"-e\" for reading from stdin"
      (json/write-str
        (if (= "-e" (first args))
          (analyze-event-rate *in*)
          (process-file (first args)))))))
