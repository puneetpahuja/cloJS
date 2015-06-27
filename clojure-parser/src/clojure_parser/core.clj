(ns clojure-parser.core
  (:gen-class))

(defn not-nil? [element]
  (not (nil? element)))

(defn batch-parse [string functions]
  (let [result ((first functions) string)]
    (if (nil? (first result))
      (recur string (rest functions))
      result)))


(defn extract [element code]
  (if (= java.lang.Character (type element))
    (apply str (rest code))
    (apply str (drop (count element) code))))

(defn parse-identifier [code]
  (let [identifier (re-find #"^\w+[\\?]?" code)]
    (if (nil? identifier)
      nil
      [identifier (extract identifier code)])))

(defn parse-name [code]
  (let [identifier (first (parse-identifier code))]
    (if (nil? identifier)
      nil
      (if (= "true" identifier)
        nil
        (if (= "false" identifier)
          nil
          [identifier (extract identifier code)])))))

(defn parse-keyword [code]
  (let [keyword (re-find #"^:\w+" code)]
    (if (nil? keyword)
      nil
      [(read-string keyword) (extract keyword code)])))

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
      (nil? boolean) [nil code]
      (= "def" reserved-keyword) ["def" (extract reserved-keyword code)]
      (= "defn" reserved-keyword) ["defn" (extract reserved-keyword code)]
      (= "nil" reserved-keyword) ["nil" (extract reserved-keyword code)]
      (= "fn" reserved-keyword) ["fn" (extract reserved-keyword code)]
      (= "atom" reserved-keyword) ["atom" (extract reserved-keyword code)] 
      (= "keyword" reserved-keyword) ["keyword" (extract reserved-keyword code)]
      (= "symbol" reserved-keyword) ["symbol" (extract reserved-keyword code)]
      (= "name" reserved-keyword) ["name" (extract reserved-keyword code)]
      (= "intern" reserved-keyword) ["intern" (extract reserved-keyword code)]
      (= "namespace" reserved-keyword) ["namespace" (extract reserved-keyword code)]
      (= "keyword?" reserved-keyword) ["keyword?" (extract reserved-keyword code)]
      (= "for" reserved-keyword) ["for" (extract reserved-keyword code)]
      (= "require" reserved-keyword) ["require" (extract reserved-keyword code)]
      :else [nil code])))

(defn parse-operator [code]
  (let [operator (re-find #"^[+-\\*\/=><][+-\\*\/=><]?\s" code)]
    (cond
      (= "+ " operator) [:plus (apply str (rest (rest code)))]
      (= "- " operator) [:minus (apply str (rest (rest code)))]
      (= "* " operator) [:mulitply (apply str (rest (rest code)))]
      (= "/ " operator) [:divide (apply str (rest (rest code)))]
      (= "= " operator) [:equals (apply str (rest (rest code)))]
      (= "== " operator) [:equalequals (apply str (rest (rest code)))]
      (= "> " operator) [:greater-than (apply str (rest (rest code)))]
      (= "< " operator) [:less-than (apply str (rest (rest code)))]
      (= ">= " operator) [:greater-than-or-equal (apply str (rest (rest code)))]
      (= "<= " operator) [:less-than-or-equal (apply str (rest (rest code)))]
      :else [nil code] )))

(defn parse-string [code]
  (let [string (last (re-find #"^\"([^\"]*)\"" code))]
    (if (nil? string) 
      [nil code]
      [string (apply str (drop (+ 2 (count string)) code))])))

(defn parse-square-bracket [code]
  (let [char (first code)]
    (if (= \[ char)
      [char (extract char code)]
      nil)))

(defn parse-form [code]
  (batch-parse code [parse-keyword
                     parse-reserved
                     parse-operator
                     parse-name]))

(defn parse-argument [code]
  (batch-parse code [parse-keyword                              
                     parse-operator
                     parse-name
                     parse-number
                     parse-string
                     parse-boolean]))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  ([path]
   (println (slurp path))))
