(ns lograte.core-test
  (:require [clojure.test :refer :all]
            [lograte.core :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.data.json :as json]))

;; An infinte sequence of time stamped and numbered messages
(def message-seq
  (map-indexed #(str (f/unparse custom-formatter %2) " message " %1)
               (repeatedly t/now)))

(defn create-sample!
  "Open this file and write n log messages"
  [n file]
  (with-open [stream (clojure.java.io/writer file)]
    (binding [*out* stream]
      (doseq [line (take n message-seq)] (println line)))))

(deftest log-file-test
  (testing "Test a relatively small quantity from a file"
    (let [sample-size 100
          tmp-file (java.io.File/createTempFile "lograte-test" ".log")]
      (.deleteOnExit tmp-file)
      (create-sample! sample-size tmp-file)
      (let [{message-count :count rate :rate-per-second :as m} (process-file tmp-file)]
        (is (= sample-size message-count))
        (is (= sample-size (int rate)))))))
