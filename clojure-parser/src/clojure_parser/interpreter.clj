(ns clojure-parser.interpreter
  (:gen-class))

(require '[clojure-parser.core :as parser])

(defn forms [form]
  (cond
    (= :defn form) :defn
    (= :plus form) +
    (= :minus form) -
    (= :multiply form) *
    (= :divide form) /
    (= :equals form) =
    (= :equalequals form) ==
    (= :greater-than form) >
    (= :less-than form) <
    (= :greater-than-or-equal form) >=
    (= :less-than-or-equal form) <=
    (= :println form) println
       :else nil))

(defn definate [exp]
  (do
    (println (first exp))
    (println (read-string (first (rest exp))))
    (println (vec (rest (first (rest (rest exp))))))
    (println (last exp))))

(defn evaluate [expression]
  (if (= :defn (first expression))
    (definate expression)
    ((forms (first expression))
     (first (rest expression))
     (last expression))))

(defn interpret [exp]
  (let [result (first (parser/parse-expression exp))]
    (if (= :expr (first result))
      (let [expression (rest result)]
        (evaluate expression))
      (read-string (rest result)))))

(defn -main [path]
  (let [tree (parser/ast path)]
    (clojure.pprint/pprint 
     (assoc {} :program
            (for [expression tree]
              (parser/mapify (rest expression)))))))
