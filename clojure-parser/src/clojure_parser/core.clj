(ns clojure-parser.core
  (:gen-class))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  [path]
  (seq (slurp path)))
