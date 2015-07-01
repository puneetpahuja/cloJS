(ns clojure-parser.core
  (:gen-class))

(defn not-nil? [element]
  (not (nil? element)))

(defn not-empty? [list]
  (not (empty? list)))

(defn extract [element code]
  (if (= java.lang.Character (type element))
    (apply str (rest code))
    (apply str (drop (count element) code))))

(defn batch-parse [string functions]
  (if (empty? functions)
    nil
    (let [result ((first functions) string)]
      (if (nil? (first result))
        (recur string (rest functions))
        result))))

(defn batch-parse [string functions]
  (if (empty? functions)
    nil
    (let [result ((first functions) string)]
      (if (nil? (first result))
        (recur string (rest functions))
        result))))

(defn nested-parse [code-string 
                    type
                    opening-character 
                    closing-character
                    functions]
  (if (= opening-character (first code-string))
    (loop [code (apply str (rest code-string)) array [type]]
      (if (= closing-character (first code))
        [(filter #(not= \newline %) 
                 (filter #(nil? (re-find #"^\s+" (str %))) array)) 
         (apply str (rest code))]
        (let [result (batch-parse code functions)]
          (if (nil? result)
            [(filter #(not= \newline %)
                     (filter #(nil? (re-find #"^\s+" (str %))) array))
             (apply str (rest code))]
            (recur (last result) (conj array (first result)))))))
    nil))

(defn parse-identifier [code]
  (let [identifier (re-find #"^[\w-.]+[\\?]?" code)]
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
  (let [keyword (re-find #"^:[\w-.]+" code)]
    (if (nil? keyword)
      nil
      [(read-string keyword) (extract keyword code)])))

(defn parse-space [code]
  (let [space (re-find #"^\s+" code)]
    (if (nil? space)
      nil
      [space (extract space code)])))

(defn parse-newline [code]
    (if (= \newline (first code)) 
      [\newline (apply str (rest code))]
      nil))

(defn parse-number [code]
  (let [number (re-find #"^[+-]?\d+[.]?[\d+]?" code)]
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
      (= "def" reserved-keyword) [:def (extract reserved-keyword code)]
      (= "defn" reserved-keyword) [:defn (extract reserved-keyword code)]
      (= "println" reserved-keyword) [:println (extract reserved-keyword code)]
      (= "nil" reserved-keyword) [:nil (extract reserved-keyword code)]
      (= "fn" reserved-keyword) [:fn (extract reserved-keyword code)]
      (= "atom" reserved-keyword) [:atom (extract reserved-keyword code)] 
      (= "keyword" reserved-keyword) [:keyword (extract reserved-keyword code)]
      (= "symbol" reserved-keyword) [:symbol (extract reserved-keyword code)]
      (= "name" reserved-keyword) [:name (extract reserved-keyword code)]
      (= "intern" reserved-keyword) [:intern (extract reserved-keyword code)]
      (= "namespace" reserved-keyword) [:namespace (extract reserved-keyword code)]
      (= "keyword?" reserved-keyword) [:keyword? (extract reserved-keyword code)]
      (= "for" reserved-keyword) [:for (extract reserved-keyword code)]
      (= "require" reserved-keyword) [:require (extract reserved-keyword code)]
      :else nil)))

(defn parse-operator [code]
  (let [operator (re-find #"^[+-\\*\/=><][+-\\*\/=><]?\s" code)]
    (cond
      (= "+ " operator) [:plus (apply str (rest (rest code)))]
      (= "- " operator) [:minus (apply str (rest (rest code)))]
      (= "* " operator) [:multiply (apply str (rest (rest code)))]
      (= "/ " operator) [:divide (apply str (rest (rest code)))]
      (= "= " operator) [:equals (apply str (rest (rest code)))]
      (= "== " operator) [:equalequals (apply str (rest (rest code)))]
      (= "> " operator) [:greater-than (apply str (rest (rest code)))]
      (= "< " operator) [:less-than (apply str (rest (rest code)))]
      (= ">= " operator) [:greater-than-or-equal (apply str (rest (rest code)))]
      (= "<= " operator) [:less-than-or-equal (apply str (rest (rest code)))]
      :else nil)))

(defn parse-string [code]
  (let [string (last (re-find #"^\"([^\"]*)\"" code))]
    (if (nil? string) 
      nil
      [string (apply str (drop (+ 2 (count string)) code))])))

(defn parse-square-bracket [code]
  (let [char (first code)]
    (if (= \[ char)
      [char (extract char code)]
      nil)))

(defn parse-vector [code]
  (nested-parse code  :vector \[ \] [parse-space
                                     parse-keyword
                                     parse-operator
                                     parse-name
                                     parse-number
                                     parse-string
                                     parse-boolean
                                     parse-reserved
                                     parse-vector]))

(defn parse-form [code]
  (batch-parse code [parse-keyword
                     parse-reserved
                     parse-operator
                     parse-name]))

(defn parse-argument [code]
  (batch-parse code [parse-number
                     parse-keyword                              
                     parse-operator
                     parse-reserved
                     parse-name
                     parse-string
                     parse-vector
                     parse-boolean]))

(defn parse-expression [code]
  (nested-parse code :expr \( \) [parse-newline
                                  parse-space
                                  parse-argument
                                  parse-form
                                  parse-expression]))

(defn mapify [lst]
  (assoc {} (first lst) 
         (loop [list (rest lst)
                arguments []]
           (let [argument (first list)]
             (if (not-empty list)
               (if (= clojure.lang.LazySeq (type argument))
                 (if (= :expr (first argument))
                   (recur (rest (rest list)) (conj arguments (mapify (rest argument)))) 
                   (recur (rest list) (conj arguments (mapify argument))))
                 (recur (rest list) (conj arguments argument)))
               arguments)))))

(defn format-special-forms [exp]
  exp)

(defn forms [form]
  (cond
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

(defn interpret [exp]
  (let [result (first (parse-expression exp))]
    (if (= :expr (first result))
      (let [expression (rest result)]
        ((forms (first expression)) (first (rest expression)) (last expression))))))

(defn ast
  "Clojure parser that returns an AST of the clojure code passed to it."
  ([path]
   (loop [expression (first (parse-expression (slurp path)))
         remainder (apply str (rest (parse-expression (slurp path))))
         tree []]
     (if (empty? remainder)
       (conj tree expression)
       (if (not= \(  (first remainder))
         (recur expression
                (rest remainder)
                tree)
         (recur (first (parse-expression remainder))
                (apply str (rest (parse-expression remainder)))
                (conj tree expression)))))))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  [path]
  (let [tree (ast path)]
    (clojure.pprint/pprint (assoc {} :program
           (for [expression tree]
             (mapify (rest expression)))))))

(-main "/home/ramshreyas/Dev/clojure/seqingclojure/clojure-parser/src/clojure_parser/square.clj")
