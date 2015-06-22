(ns clojure-parser.core
  (:gen-class))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  ([path]
   (-main [] [] (slurp path)))
  ([tree node code]
   (if (empty? code)
     (for [x node] (println x))
     (conj tree (if (= \( (first code))
                  (-main tree [] (rest code))
                  (if (= \) (first code))
                    node
                    (-main tree (conj node (first code)) (rest code)))
