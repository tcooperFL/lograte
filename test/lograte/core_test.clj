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

(defn create-sample-file!
  "Create and return a temp file with n log messages writtenk"
  [n]
  (let [tmp-file (java.io.File/createTempFile "lograte-test" ".log")]
    (.deleteOnExit tmp-file)
    (with-open [stream (clojure.java.io/writer tmp-file)]
      (binding [*out* stream]
        (doseq [line (take n message-seq)] (println line))))
    tmp-file))

(deftest empty-file-test
  (testing "Be sure an empty file doesn't break things"
    (let [tmp-file (create-sample-file! 0)
          {message-count :message-count rate :rate-per-second :as m} (process-file tmp-file)]
      (is (= 0 message-count))
      (is (= 0 (int rate))))))

(deftest single-event-file-test
  (testing "Be sure a file with just one message doesn't break things"
    (let [tmp-file (create-sample-file! 1)
          {message-count :message-count rate :rate-per-second :as m} (process-file tmp-file)]
      (is (= 1 message-count))
      (is (= 1 (int rate))))))

(deftest log-file-test
  (testing "Test a relatively small quantity from a file"
    (let [tmp-file (create-sample-file! 100)
          {message-count :message-count rate :rate-per-second :as m} (process-file tmp-file)]
      (is (= 100 message-count))
      (is (= 100 (int rate))))))
