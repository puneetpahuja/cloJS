(ns clojure-parser.core
  (:gen-class))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  [path]
  (loop [node [""] 
         code (slurp path)
         previous-character ""] 
    ;;; If code is empty, return node
    (if (empty? code) 
      node
      ;;;Else if open bracket, 
      (recur (assoc node 0 (str (first node) (first code)))
             (rest code)
             (first code)))))
