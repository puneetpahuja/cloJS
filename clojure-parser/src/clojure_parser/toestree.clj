(ns clojure-parser.toestree
  (:gen-class))

(require '[clojure-parser.core :as parser])

(defn tree [path]
  (assoc {} :program (parser/ast (slurp path))))
