(ns clojure-parser.core
  (:require [clojure.algo.monads :refer :all]
            [clojure.string :as string]
            [clojure.tools.trace :as trace]
            [clojure.pprint :as pprint]))

;;; Utility methods
(defn extract [element code]
  (if (= java.lang.Character (type element))
    (apply str (rest code))
    (apply str (drop (count element) code))))

;;; Element parsers
(defn parse-newline [code]
    (if (= \newline (first code))
      [\newline (apply str (rest code))]
      nil))

(defn parse-space [code]
  (let [space (re-find #"^\s+" code)]
    (if (nil? space)
      nil
      [space (extract space code)])))

(defn parse-number [code]
  (let [number (re-find #"^[+-]?\d+[.]?[\d+]?" code)]
    (if (nil? number)
      nil
      [(read-string number) (extract number code)])))

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

(defn parse-backtick [code]
  (let [char (first code)]
    (if (= \` char)
      [:escape-macro-body (extract char code)]
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

(defn parse-identifier [code]
  (let [identifier (re-find #"^[\w-.><=@]+[\\?]?" code)]
    (if (nil? identifier)
      nil
      [identifier (extract identifier code)])))

(defn parse-boolean [code]
  (let [boolean (first (parse-identifier code))]
    (cond
      (nil? boolean) nil
      (= "true" boolean) [true (extract boolean code)]
      (= "false" boolean) [false (extract boolean code)]
      :else nil)))

(defn parse-name [code]
  (let [identifier (first (parse-identifier code))]
    (cond
      (nil? identifier) nil
      (= "true" identifier) nil
      (= "false" identifier) nil
      :else [(symbol (name identifier)) (extract identifier code)])))

(defn parse-deref [code]
  (let [for-deref (parse-tilde code)]
    (if (nil? for-deref)
      nil
      (let [deref (parse-name (last for-deref))]
        [(symbol (str ":de-ref " (first deref))) (last deref)]))))

(defn parse-keyword [code]
  (let [keyword (re-find #"^:[\w-.]+" code)]
    (if (nil? keyword)
      nil
      [(read-string keyword) (extract keyword code)])))

(defn parse-reserved [code]
  (let [reserved-keyword (first (parse-identifier code))]
    (cond
     (nil? reserved-keyword) nil
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
      (= "+ " operator) [:+ (extract operator code)]
      (= "- " operator) [:- (extract operator code)]
      (= "* " operator) [:* (extract operator code)]
      (= "/ " operator) [:/ (extract operator code)]
      (= "= " operator) [:= (extract operator code)]
      (= "> " operator) [:> (extract operator code)]
      (= "< " operator) [:< (extract operator code)]
      :else nil)))

;;; Parser monad
(def parser-m (state-t maybe-m))

;;; Parser combinators
(with-monad parser-m
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
    "This matches the possible function names in an s-expression"
    (domonad
     [name (match-one parse-keyword
                      parse-reserved
                      parse-name
                      parse-operator)]
     name))

  (declare m-argument)

  (def m-parse-literal
    (domonad
     [literal (match-one parse-string
                         parse-number
                         parse-boolean)]
     (list :literal literal)))

  (def m-parse-vector
    "This matches vectors"
    (domonad
     [opening-bracket (match-one parse-square-bracket)
      elements (one-or-more (match-one m-argument
                                       (skip-one-or-more parse-space)))
      closing-bracket (match-one parse-close-square-bracket)]
     (flatten (list :vector (filter #(not (= :skip %)) elements)))))

  (def m-argument
    "Matches the possible arguments of an s-expression"
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
    "Matches s-expressions"
    (domonad
     [_ (optional (match-one parse-space
                             parse-newline))
      opening-bracket (match-one parse-round-bracket)
      name (match-one m-form)
      _ (optional (match-one parse-space))
      args (nested-one-or-more  (match-one (skip-one-or-more parse-newline)
                                           parse-backtick
                                           parse-deref
                                           m-argument
                                           m-parse-expression
                                           (skip-one-or-more parse-space)))
      closing-bracket (match-one parse-close-round-bracket)
      _ (optional (match-one parse-space
                             parse-newline))]
     (concat (list :expr name) (filter #(not (= :skip %)) args)))))

  (def m-parse-expression-my
    (domonad
     [_ (skip-one-or-more (match-one parse-space
                                     parse-newline))
      exp (match-one m-parse-literal
                          m-parse-expression)
      - (skip-one-or-more (match-one parse-space
                                     parse-newline))]
     exp))

;;; Formatting methods
(defn mapify
  "Takes an s-expression and returns a map of form-name to a vector of form-arguments"
  [lst]
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
(defn bind-args
  "Helper function to bind"
  [macro-args args]
  (apply assoc {} (interleave macro-args args)))

(defn and-expand
  "Expands and splices in variable number of expressions in the macro-args"
  [macro-args]
  (if (= (symbol "&") (last (butlast macro-args)))
    (let [and-term (symbol (apply str (conj (seq (str (last macro-args))) \@)))]
      (conj (vec (butlast (butlast macro-args))) and-term))
    macro-args))

(defn bind
  "Returns a map of the args in to the bindings in the macro definition"
  [macro-args args]
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

(defn load-macros
  "Adds macros from a string of code to the macro tree"
  [code]
  (let [exp (m-parse-expression code)]
    (loop [expression (first exp)
           remainder (apply str (rest exp))
           tree []]
      (if (empty? remainder)
        (conj tree (mapify (rest expression)))
        (if (not= \(  (first remainder))
          (recur expression
                 (rest remainder)
                 tree)
          (let [exp (m-parse-expression remainder)]
            (recur (first exp)
                   (apply str (rest exp))
                   (conj tree (mapify (rest expression))))))))))

(defn find-macro
  "Checks if the given name is a macro, returns the macro tree"
  [macros name]
  (loop [macros macros
         name name]
    (let [macro (first macros)]
      (if (empty? macros)
        nil
        (if (= name (first (:defmacro macro)))
          macro
          (recur (rest macros) name))))))

(defn de-ref
  "Helper function for de-reference"
  [refs body]
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

(defn evalate
  "Evaluates mapified expression"
  [exp]
  (do
    (clojure.pprint/pprint (apply str "Expand this: " exp))
    (eval exp)))

(defn evaluate [exp]
  (let [func (first (keys exp))]
    (loop [args (func exp)
           evaluated-args []]
      (let [arg (first args)]
        (if (empty? args)
          (apply (resolve (symbol (name func))) evaluated-args)
          (if (map? arg)
            (recur (rest args) (conj evaluated-args (evaluate arg)))
            (recur (rest args) (conj evaluated-args arg))))))))

(declare ast)

(defn de-reference
  "Returns the macro body with the place-holders replaced by the final values"
  [macro parts]
  (let [macro-args (map keyword (map str (and-expand (:vector (second (:defmacro macro))))))
        escape (last (butlast (:defmacro macro)))
        macro-body (last (:defmacro macro))
        reference-map (bind macro-args parts)
        expanded-form (de-ref reference-map macro-body)]
      (if (= :escape-macro-body escape)
        (read-string expanded-form)
        (first (ast (str (evaluate (read-string expanded-form))))))))

(defn expand-macro
  "If the supplied expression is a macro, returns the expanded form"
  [exp macros]
  (let [parts (first (vals exp))
        macro (find-macro macros (first (keys exp)))]
    (if (nil? macro)
      exp
      (de-reference macro parts))))

;;; Main methods
(defn ast
  "Returns AST of the clojure code passed to it."
  ([code]
     (loop [exp (m-parse-expression code)
            expression (first exp)
            remainder (apply str (rest exp))
            tree []
            macros (load-macros (slurp "macros"))]
       (if (empty? remainder)
         (if (= :defmacro (second expression))
           (conj macros (mapify (rest expression)))
           (conj tree (expand-macro (mapify (rest expression)) macros)))
         (if (not= \(  (first remainder))
           (recur exp
                  expression
                  (rest remainder)
                  tree
                  macros)
           (if (= :defmacro (second expression))
             (let [exp (m-parse-expression remainder)]
               (recur exp
                      (first exp)
                      (apply str (rest exp))
                      tree
                      (conj macros (mapify (rest expression)))))
             (let [exp (m-parse-expression remainder)]
               (recur exp
                      (first exp)
                      (apply str (rest exp))
                      (conj tree (expand-macro (mapify (rest expression)) macros))
                      macros))))))))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  [path]
  (let [tree (ast (slurp path))]
    ;; (clojure.pprint/pprint (assoc {} :program
    ;;                               (for [expression tree]
    ;;                                 expression)))
    (assoc {} :program tree)))

; (trace/trace-ns 'clojure-parser.core)
; (trace/trace-vars -main expand-macro bind-args and-expand bind find-macro de-reference evalate evaluate de-ref)

(pprint/pprint (-main "fact.clj"))



