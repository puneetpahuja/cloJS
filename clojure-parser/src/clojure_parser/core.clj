(ns clojure-parser.core
  (:gen-class))

(defn parse-arguments [cd]
  (if (= \[ (first cd))
    (loop [code cd
           arguments [""]]
      (if (= \] (first code))
        [arguments (rest code)]
        (do
          (println "args: " arguments)
          (recur (rest code) [(str (get arguments 0) (first code))]))))
    [nil code]))

(defn parse-space [code]
  (if (= \space (first code))
    [\space (apply str (rest code))]
    [nil code]))

(defn parse [exp cd]
  (loop [expression exp code cd]
    (if (empty? code) 
      expression
      (recur {:node (str (:node expression) (first code))} 
             (apply str (rest code))))))


(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  ([path]
   (-main [] {:node ""} (slurp path)))
  ([tree node code]
   (if (empty? code)
     tree
     (conj tree (parse node code)))))
