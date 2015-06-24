(ns clojure-parser.core
  (:gen-class))

(defn parse-string [code]
  (let [string (re-find #"^\.*\\\"(.*)\\\".*" code)]
    (if (nil? string)
      [nil code]
      [string (apply str (drop (count string) code))])))

(defn parse-word [code]
  (let [word (re-find #"^\w+" code)]
    (if (nil? word)
      [nil code]
      [word (apply str (drop (count word) code))])))

(defn parse-keyword [code]
  (let [keyword (re-find #"^:\w+" code)]
    (if (nil? keyword)
      [nil code]
      [keyword (apply str (drop (count keyword) code))])))

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
