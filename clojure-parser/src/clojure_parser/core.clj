(ns clojure-parser.core
  (:gen-class))

(defn extract [element code]
  (apply str (drop (count element) code)))

(defn parse-identifier [code]
  (let [identifier (re-find #"^\w+" code)]
    (if (nil? identifier)
      nil
      [identifier (extract identifier code)])))

(defn parse-keyword [code]
  (let [keyword (re-find #"^:\w+" code)]
    (if (nil? keyword)
      nil
      [keyword (extract keyword code)])))

(defn parse-space [code]
  (let [space (re-find #"^\s+" code)]
    (if (nil? space)
      nil
      [space (extract space code)])))

(defn parse-number [code]
  (let [number (re-find #"^[+-]?\d+[.]?[\d+]?\s" code)]
    (if (nil? number)
      nil
      [(read-string number) (extract number code)])))

(defn parse-boolean [code]
  (let [boolean (first (parse-identifier code))]
    (cond
      (nil? boolean) nil
      (= "true" boolean) [true (extract boolean code)]
      (= "false" boolean) [false (extract boolean code)]
      :else nil)))

(defn parse-reserved [code]
  (let [reserved-keyword (first (parse-identifier code))]
    (cond
      (nil? boolean) nil
      :else nil)))

(defn parse-operator [code]
  (let [operator (str (first code) (first (next code)))]
    (cond
      (= "+ " operator) ["+" (apply str (rest (rest code)))]
      (= "- " operator) ["-" (apply str (rest (rest code)))]
      (= "* " operator) ["*" (apply str (rest (rest code)))]
      (= "/ " operator) ["/" (apply str (rest (rest code)))]
      (= "= " operator) ["=" (apply str (rest (rest code)))]
      (= "> " operator) ["=" (apply str (rest (rest code)))]
      (= "< " operator) ["=" (apply str (rest (rest code)))]
      (= ">= " operator) ["=" (apply str (rest (rest code)))]
      (= "<= " operator) ["=" (apply str (rest (rest code)))]
      :else nil )))

(defn parse-string [code]
  (let [string (last (re-find #"^\"([^\"]*)\"" code))]
    (if (nil? string) 
      nil
      [string (apply str (drop (+ 2 (count string)) code))])))

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
