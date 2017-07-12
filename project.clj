(defproject clojs "0.1.6-SNAPSHOT"
  :description "Converts Clojure code to JavaScript"
  :url "https://github.com/puneetpahuja/cloJS"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/algo.monads "0.1.6"]
                 [org.clojure/data.json "0.2.6"]
                 [me.raynes/conch "0.8.0"]
                 [org.clojure/tools.cli "0.2.4"]
                 [org.clojure/tools.trace "0.7.9"]]
  :resource-paths ["resources"]
  :main ^:skip-aot clojs.clojs
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
