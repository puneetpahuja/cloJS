(ns clojure-parser.core
  (:gen-class))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  [path]
  (loop [node [[]] 
         code (slurp path)
         previous-character ""] 
    ;;; If code is empty, return node
    (if (empty? code)
      (for [x node] (println x))
      ;;;Else if open bracket,
      (if (= \( (first code))
        (recur (conj node []) (rest code) (first code)))
        (recur (assoc node
                      (- (count node) 1) 
                      (str (last node) (first code)))
               (rest code)
               (first code)))))
