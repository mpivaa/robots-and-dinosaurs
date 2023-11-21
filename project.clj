(defproject robots-vs-dinosaurs "0.2.0"
  :description "Robots vs Dinosaurs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [clojure-lanterna "0.9.7"]
                 [clj-fuzzy "0.4.1"]
                 [failjure "1.3.0"]
                 [clansi "1.0.0"]]
  :main ^:skip-aot robots-vs-dinosaurs.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
