(ns clojure-parser.core
  (:gen-class))

(defn parse-operator [code]
  (let [operator (str (first code) (first (next code)))]
    (cond
      (= "+ " operator) ["+" (apply str (rest (rest code)))]
      (= "- " operator) ["-" (apply str (rest (rest code)))]
      (= "* " operator) ["*" (apply str (rest (rest code)))]
      (= "/ " operator) ["/" (apply str (rest (rest code)))]
      :else nil )))

(defn parse-string [code]
  (let [string (last (re-find #"^\"([^\"]*)\"" code))]
    (if (nil? string) 
      nil
      [string (apply str (drop (+ 2 (count string)) code))])))

(defn parse-word [code]
  (let [word (re-find #"^\w+" code)]
    (if (nil? word)
      nil
      [word (apply str (drop (count word) code))])))

(defn parse-keyword [code]
  (let [keyword (re-find #"^:\w+" code)]
    (if (nil? keyword)
      nil
      [keyword (apply str (drop (count keyword) code))])))

(defn parse-space [code]
  (let [space (re-find #"^\s+" code)]
    (if (nil? space)
      nil
      [space  (apply str (drop (count space) code))])))

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
