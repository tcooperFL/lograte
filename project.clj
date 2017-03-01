(defproject lograte "0.1.0-SNAPSHOT"
  :description "Given a log, calculate the event generation rate/second over intervals."
  :plugins [[lein-ancient "0.6.10"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-time "0.13.0"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot lograte.core
  :target-path "target/%s"
  :global-vars {*assert* true *print-level* 5 *print-length* 20}
  :profiles {:uberjar {:aot :all}})
