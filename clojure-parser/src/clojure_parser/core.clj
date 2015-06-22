(ns clojure-parser.core
  (:gen-class))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  ([path]
   (-main [] [] (slurp path)))
  ([tr nd cd]
   (loop [tree tr
          node nd
          code cd]
     (if (empty? code)
       (for [x tree] (println x))
       (if (= \( (first code))
         (recur [] (conj node tree) (rest code))
         (if (= \) (first code))
           (recur (conj tree node) [] (rest code))
           (recur tree (conj node (first code)) (rest code))))))))
