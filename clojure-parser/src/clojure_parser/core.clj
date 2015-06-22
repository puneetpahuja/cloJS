(ns clojure-parser.core
  (:gen-class))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  ([path]
   (-main [] [] (slurp path)))
  ([tree node code]
     (conj tree (-main node code)))
  ([node code]
   (if (empty? code)
     (for [x node] (println x))
     (if (= \( (first code))
       (-main node [] (rest code))
       (if (= \) (first code))
         node
         (if (= \newline (first code))
           (-main node (rest code))
           (-main (conj node (first code)) (rest code))))))))
