(ns clojure-parser.core
  (:use [clojure.algo.monads :only [domonad with-monad state-t maybe-m
  fetch-state set-state m-seq m-plus m-result]])
  (:gen-class))

(require '[clojure.string :as string])

(declare concrete-to-abstract parse-expression ast cst m-argument m-parse-vector)

;;; Utility methods 

(defn not-nil? [element]
  (not (nil? element)))

(defn not-empty? [list]
  (not (empty? list)))

(defn extract [element code]
  (if (= java.lang.Character (type element))
    (apply str (rest code))
    (apply str (drop (count element) code))))

;;; Element parsers

(defn parse-identifier [code]
  (let [identifier (re-find #"^[\w-.><=@]+[\\?]?" code)]
    (if (nil? identifier)
      nil
      [identifier (extract identifier code)])))

(defn parse-name [code]
  (let [identifier (first (parse-identifier code))]
    (cond 
      (nil? identifier) nil
      (= "true" identifier) nil
      (= "false" identifier) nil
      :else [(symbol (name identifier)) (extract identifier code)])))

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

(defn parse-close-square-bracket [code]
  (let [char (first code)]
    (if (= \] char)
      [char (extract char code)]
      nil)))

(defn parse-round-bracket [code]
  (let [char (first code)]
    (if (= \( char)
      [char (extract char code)]
      nil)))

(defn parse-close-round-bracket [code]
  (let [char (first code)]
    (if (= \) char)
      [char (extract char code)]
      nil)))

(defn parse-quote [code]
  (let [char (first code)]
    (if (= \` char)
      [:macro-body (extract char code)]
      nil)))

(defn parse-tilde [code]
  (let [char (first code)]
    (if (= \~ char)
      [:de-ref (extract char code)]
      nil)))

(defn parse-ampersand [code]
  (let [char (first code)]
    (if (= \& char)
      [(symbol (str char)) (extract char code)])))

(defn parse-deref [code]
  (let [for-deref (parse-tilde code)]
    (if (nil? for-deref)
      nil
      (let [deref (parse-name (last for-deref))]
        [(symbol (str ":de-ref " (first deref))) (last deref)]))))

(defn parse-reserved [code]
  (let [reserved-keyword (first (parse-identifier code))]
    (cond
      (nil? boolean) nil
      (= "def" reserved-keyword) [:def (extract reserved-keyword code)]
      (= "defn" reserved-keyword) [:defn (extract reserved-keyword code)]
      (= "defmacro" reserved-keyword) [:defmacro (extract reserved-keyword code)]
      (= "println" reserved-keyword) [:println (extract reserved-keyword code)]
      (= "nil" reserved-keyword) [:nil (extract reserved-keyword code)]
      (= "let" reserved-keyword) [:let (extract reserved-keyword code)]
      (= "fn" reserved-keyword) [:fn (extract reserved-keyword code)]
      (= "atom" reserved-keyword) [:atom (extract reserved-keyword code)] 
      (= "keyword" reserved-keyword) [:keyword (extract reserved-keyword code)]
      (= "symbol" reserved-keyword) [:symbol (extract reserved-keyword code)]
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
      (= "> " operator) [:greater-than (apply str (rest (rest code)))]
      (= "< " operator) [:less-than (apply str (rest (rest code)))]
      :else nil)))

;;; Parser monad

(def parser-m (state-t maybe-m))

(with-monad parser-m
  ;; Parser combinators

  (defn optional
    "Take a parser and return an optional version of it."
    [parser]
    (m-plus parser (m-result nil)))

  (defn one-or-more
    "Matches the same parser repeatedly until it fails - the first time has
  to succeed for the parser to progress"
    [parser]
    (domonad [value parser
              values (optional (one-or-more parser))]
             (if values
               (into [value] (flatten values))
               [value])))

  (defn none-or-more
    "Matches the same parser repeatedly until it fails - first can fail and
  second will continue"
    [parser]
    (optional (one-or-more parser)))

  (defn nested-one-or-more
    "Matches the same parser repeatedly until it fails - the first time has
  to succeed for the parser to progress"
    [parser]
    (domonad [value parser
              values (optional (nested-one-or-more parser))]
             (if values
               (into [value] values)
               [value])))

  (defn skip-one-or-more
    "Matches the parser on or more times until it fails, but doesn't return
  the values for binding"
    [parser]
    (domonad
     [_ parser
      _ (optional (skip-one-or-more parser))]
     :skip))

  (defn skip-none-or-more
     "Matches the same parser zero or more times until it fails,
     then returns true."
     [parser]
     (optional (skip-one-or-more parser)))

  (defn match-one
    "Match at least one of the parsers in the given order, or fail"
    [& parsers]
    (reduce m-plus parsers))

  (defn match-all
    "Match all the given parsers, or fail"
    [& parsers]
    (m-bind (m-seq parsers)
            (comp m-result flatten))))

;; Combined parsers

(with-monad parser-m
  (def m-form
    (domonad
     [name (match-one parse-keyword
                      parse-reserved
                      parse-name
                      parse-operator)]
     name))

  (def m-parse-vector
    (domonad
     [opening-bracket (match-one parse-square-bracket)
      elements (one-or-more (match-one m-argument
                                       (skip-one-or-more parse-space)))
      closing-bracket (match-one parse-close-square-bracket)]
     (flatten (list :vector (filter #(not (= :skip %)) elements)))))

  (def m-argument
    (domonad
     [arg (match-one parse-number
                     parse-ampersand
                     parse-keyword
                     parse-reserved
                     parse-name
                     parse-string
                     m-parse-vector
                     parse-boolean)]
     arg))

  (def m-parse-expression
    (domonad
     [_ (optional (match-one parse-space
                             parse-newline))
      opening-bracket (match-one parse-round-bracket)
      name (match-one m-form)
      _ (optional (match-one parse-space))
      args (nested-one-or-more  (match-one (skip-one-or-more parse-newline)
                                           parse-quote
                                           parse-deref
                                           m-argument
                                           m-parse-expression
                                           (skip-one-or-more parse-space)))
      closing-bracket (match-one parse-close-round-bracket)
      _ (optional (match-one parse-space
                             parse-newline))]
     (concat (list :expr name) (filter #(not (= :skip %)) args)))))

;;; Formatting methods

(defn mapify [lst]
  (assoc {} (first lst)
         (loop [list (rest lst)
                arguments []]
           (let [argument (first list)]
             (if (not-empty list)
               (if (= clojure.lang.LazySeq (type argument))
                 (if (= :expr (first argument))
                   (recur (rest list) (conj arguments (mapify (rest argument))))
                   (recur (rest list) (conj arguments (mapify argument))))
                 (recur (rest list) (conj arguments argument)))
               arguments)))))

;;; Macro expansion

(defn bind-args [macro-args args]
  (apply assoc {} (interleave macro-args args)))

(defn and-expand [macro-args]
  (if (= (symbol "&") (last (butlast macro-args)))
    (let [and-term (symbol (apply str (conj (seq (str (last macro-args))) \@)))]
      (conj (vec (butlast (butlast macro-args))) and-term))
    macro-args))

(defn bind [macro-args args]
  (loop [macro-args macro-args
         args args
         accumalator []]
    (if (empty? macro-args)
      (if (empty? args)
        (apply assoc {} accumalator)
        (let [last-key (last (butlast accumalator))
              last-value (last accumalator)]
          (assoc (apply assoc {} accumalator) last-key (conj args last-value))))
      (recur (rest macro-args)
             (rest args)
             (conj accumalator (first macro-args) (first args))))))

(defn load-macros [code]
  (loop [expression (first (m-parse-expression code))
         remainder (apply str (rest (m-parse-expression code)))
         tree []]
    (if (empty? remainder)
      (conj tree (mapify (rest expression)))
      (if (not= \(  (first remainder))
        (recur expression
               (rest remainder)
               tree)
        (recur (first (m-parse-expression remainder))
               (apply str (rest (m-parse-expression remainder)))
               (conj tree (mapify (rest expression))))))))

(defn find-macro [macros name]
  (loop [macros macros
         name name]
    (let [macro (first macros)]
      (if (empty? macros)
        nil
        (if (= name (first (:defmacro macro)))
          macro
          (recur (rest macros) name))))))

(defn de-ref [refs body]
  (let [deref-string (str (assoc {} (first (first body)) (last (first body))))]
    (loop [keys (keys refs)
           deref-string deref-string]
      (if (empty? keys)
        deref-string
        (if (some #(= \@ %) (seq (str (first keys))))
          (recur (rest keys)
               (string/replace deref-string
                               (re-pattern (str ":de-ref " (name (first keys))))
                               (subs (str ((first keys) refs))
                                     1
                                     (dec (count (str ((first keys) refs)))))))
          (recur (rest keys)
                 (string/replace deref-string
                                 (re-pattern (str ":de-ref " (name (first keys))))
                                 (str ((first keys) refs)))))))))

(defn de-reference [macro parts]
  (let [macro-args (map keyword (map str (and-expand (:vector (second (:defmacro macro))))))
        macro-body (last (:defmacro macro))
        reference-map (bind macro-args parts)
        expanded-form (de-ref reference-map macro-body)]
      (read-string expanded-form)))

(defn expand-macro [exp macros]
  (let [parts (first (vals exp))
        macro (find-macro macros (first (keys exp)))]
    (if (nil? macro)
      exp
      (de-reference macro parts))))

;;; Main methods

(defn ast
  "Returns AST of the clojure code passed to it."
  ([code]
     (loop [expression (first (m-parse-expression code))
            remainder (apply str (rest (m-parse-expression code)))
            tree []
            macros (load-macros (slurp "/home/ramshreyas/Dev/clojure/seqingclojure/clojure-parser/src/clojure_parser/macros"))]
       (if (empty? remainder)
         (if (= :defmacro (second expression))
           (conj macros (mapify (rest expression)))
           (conj tree (expand-macro (mapify (rest expression)) macros)))
         (if (not= \(  (first remainder))
           (recur expression
                  (rest remainder)
                  tree
                  macros)
           (if (= :defmacro (second expression))
             (recur (first (m-parse-expression remainder))
                    (apply str (rest (m-parse-expression remainder)))
                    tree
                    (conj macros (mapify (rest expression))))
             (recur (first (m-parse-expression remainder))
                    (apply str (rest (m-parse-expression remainder)))
                    (conj tree (expand-macro (mapify (rest expression)) macros))
                    macros)))))))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  [path]
  (let [tree (ast (slurp path))]
    (clojure.pprint/pprint (assoc {} :program
           (for [expression tree]
             expression)))))

(-main "/home/ramshreyas/Dev/clojure/seqingclojure/clojure-parser/src/clojure_parser/square.clj")
