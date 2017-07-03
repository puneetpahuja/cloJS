(ns clojs.ast-generator
  (:require [clojure.algo.monads :refer :all]
            [clojure.string :as string]
            [clojure.tools.trace :as trace]
            [clojure.pprint :as pprint]
            [clojs.utilities :refer :all]
            [clojure.java.io :as io])
  (:gen-class))

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

(defn parse-curly-bracket [code]
  (let [char (first code)]
    (if (= \{ char)
      [char (extract char code)]
      nil)))

(defn parse-close-curly-bracket [code]
  (let [char (first code)]
    (if (= \} char)
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
      (= "if" reserved-keyword) [:if (extract reserved-keyword code)]
      (= "do" reserved-keyword) [:do (extract reserved-keyword code)]
      (= "defmacro" reserved-keyword) [:defmacro (extract reserved-keyword code)]
      ; (= "println" reserved-keyword) [:println (extract reserved-keyword code)]
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
      (= "+ " operator) ['+ (extract operator code)]
      (= "- " operator) ['- (extract operator code)]
      (= "* " operator) ['* (extract operator code)]
      (= "/ " operator) ['/ (extract operator code)]
      (= "= " operator) ['= (extract operator code)]
      (= "> " operator) ['> (extract operator code)]
      (= "< " operator) ['< (extract operator code)]
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
  (defn nested-none-or-more
    [parser]
    (optional (nested-one-or-more parser)))

  (defn skip-one [parser]
    (domonad
      [_ parser]
      :skip))

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

(declare m-argument m-parse-expression)

;; Combined parsers
(with-monad parser-m

  (def m-parse-array-member
    "Parses JavaScript array member accesses like a[3]"
    (domonad
      [array (match-one parse-name)
       elements (one-or-more (match-all (skip-one parse-square-bracket) (match-one parse-operator m-parse-expression m-argument) (skip-one parse-close-square-bracket)))]
      (coll->map (list :array-member (list array (filter #(not (= :skip %)) (flatten elements)))))
      ;;elements
      ))

  (def m-form
    "This matches the possible function names in an s-expression"
    (domonad
      [name (match-one parse-keyword
                       parse-reserved
                       m-parse-array-member
                       parse-name
                       parse-operator)]
      name))

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
       elements (none-or-more (match-one m-argument
                                         (skip-one-or-more parse-space)))
       closing-bracket (match-one parse-close-square-bracket)]
      (coll->map (list :vector (filter #(not (= :skip %)) elements)))))


  (def m-parse-map
    "This matches vectors"
    (domonad
      [opening-bracket (match-one parse-curly-bracket)
       elements (none-or-more (match-all m-argument
                                         (skip-one-or-more parse-space)
                                         m-argument
                                         (skip-none-or-more parse-space)))
       closing-bracket (match-one parse-close-curly-bracket)]
      (coll->map (list :map (remove-last-nil (filter #(not (= :skip %)) (flatten elements)))))))

  (def m-argument
    "Matches the possible arguments of an s-expression"
    (domonad
      [arg (match-one parse-number
                      parse-ampersand
                      parse-keyword
                      parse-reserved
                      m-parse-array-member
                      parse-name
                      parse-string
                      m-parse-vector
                      m-parse-map
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
       args (nested-none-or-more  (match-one (skip-one-or-more parse-newline)
                                             parse-backtick
                                             parse-deref
                                             m-argument
                                             m-parse-expression
                                             (skip-one-or-more parse-space)))
       closing-bracket (match-one parse-close-round-bracket)
       _ (optional (match-one parse-space
                              parse-newline))]
      (concat (list :expr name) (filter #(not (= :skip %)) args)))))

(declare mapify)

(defn mapify-helper [list arguments]
  (let [argument (first list)]
    (if (not-empty list)
      (if (= clojure.lang.LazySeq (type argument))
        (if (= :expr (first argument))
          (mapify-helper (rest list) (conj arguments (mapify (rest argument))))
          (mapify-helper (rest list) (conj arguments (mapify argument))))
        (mapify-helper (rest list) (conj arguments argument)))
      arguments)))

;;; Formatting methods
(defn mapify
  "Takes an s-expression and returns a map of form-name to a vector of form-arguments"
  [lst]
  (assoc {} (first lst)
         (let [list (rest lst)
               arguments []]
           (mapify-helper list arguments))))

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

(defn bind-helper [macro-args args accumalator]
  (if (empty? macro-args)
    (if (empty? args)
      (apply assoc {} accumalator)
      (let [last-key (last (butlast accumalator))
            last-value (last accumalator)]
        (assoc (apply assoc {} accumalator) last-key (conj args last-value))))
    (bind-helper (rest macro-args)
                 (rest args)
                 (conj accumalator (first macro-args) (first args)))))

(defn bind
  "Returns a map of the args in to the bindings in the macro definition"
  [macro-args args]
  (let [macro-args macro-args
        args args
        accumalator []]
    (bind-helper macro-args args accumalator)))

(defn load-macros-helper [expression remainder tree]
  (if (empty? remainder)
    (conj tree (mapify (rest expression)))
    (if (not= \(  (first remainder))
      (load-macros-helper expression
                          (rest remainder)
                          tree)
      (let [exp (m-parse-expression remainder)]
        (load-macros-helper (first exp)
                            (apply str (rest exp))
                            (conj tree (mapify (rest expression))))))))

(defn load-macros
  "Adds macros from a string of code to the macro tree"
  [code]
  (let [exp (m-parse-expression code)
        expression (first exp)
        remainder (apply str (rest exp))
        tree []]
    (load-macros-helper expression remainder tree)))

(defn find-macro [macros exp]
  (let [macro (first macros)
        macro-body (operands macro) 
        exp-args-num (count (operands exp))
        macro-args-num (-> macro-body second operands count)
        second-last-macro-arg (-> macro-body second operands butlast last)]
    (cond
      (empty? macros)
      nil
      (and (= (operator exp) (first macro-body))
           (or (and
                 (= second-last-macro-arg '&)
                 (>= exp-args-num (dec macro-args-num)))
               (and
                 (not (= second-last-macro-arg '&))
                 (= exp-args-num macro-args-num))))
      macro
      :else (find-macro (rest macros) exp))))

(defn de-ref-helper [keys deref-string refs]
  (if (empty? keys)
    deref-string
    (if (some #(= \@ %) (seq (str (first keys))))
      (de-ref-helper (rest keys)
                     (string/replace deref-string
                                     (re-pattern (str ":de-ref " (subs (str (first keys)) 2)))
                                     (subs (str ((first keys) refs))
                                           1
                                           (dec (count (str ((first keys) refs))))))
                     refs)
      (de-ref-helper (rest keys)
                     (string/replace deref-string
                                     (re-pattern (str ":de-ref " (name (first keys))))
                                     (str ((first keys) refs)))
                     refs))))
(defn de-ref
  "Helper function for de-reference"
  [refs body]
  (let [deref-string (str (assoc {} (first (first body)) (last (first body))))
        keys (keys refs)] 
    (de-ref-helper keys deref-string refs)))

(defn evalate
  "Evaluates mapified expression"
  [exp]
  (do
    (clojure.pprint/pprint (apply str "Expand this: " exp))
    (eval exp)))

(declare evaluate)

(defn evaluate-helper [args evaluated-args func]
  (let [arg (first args)]
    (if (empty? args)
      (apply (resolve (symbol (name func))) evaluated-args)
      (if (map? arg)
        (evaluate-helper (rest args) (conj evaluated-args (evaluate arg)) func)
        (evaluate-helper (rest args) (conj evaluated-args arg) func)))))

(defn evaluate [exp]
  (let [func (first (keys exp))
        args (func exp)
        evaluated-args []]
    (evaluate-helper args evaluated-args func)))

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
  (if (map? exp)
    (let [parts (operands exp)
          macro (find-macro macros exp)]
      (if (nil? macro)
        (assoc {} (operator exp) (vec (map #(expand-macro % macros) parts)))
        (let [deref (de-reference macro parts)
              op (operator deref)
              operands (operands deref)]
          (assoc {} op (vec (map #(expand-macro % macros) operands))))))
    exp))

(defn ast-helper
  "Returns AST of the clojure code passed to it."
  [exp expression remainder tree macros]
  (if (empty? remainder)
    (if (= :defmacro (second expression))
      (conj macros (mapify (rest expression)))
      (conj tree (expand-macro (mapify (rest expression)) macros)))
    (if (not= \(  (first remainder))
      (ast-helper exp
                  expression
                  (rest remainder)
                  tree
                  macros)
      (if (= :defmacro (second expression))
        (let [exp (m-parse-expression remainder)]
          (ast-helper exp
                      (first exp)
                      (apply str (rest exp))
                      tree
                      (conj macros (mapify (rest expression)))))
        (let [exp (m-parse-expression remainder)]
          (ast-helper exp
                      (first exp)
                      (apply str (rest exp))
                      (conj tree (expand-macro (mapify (rest expression)) macros))
                      macros))))))

;;; Main methods
(defn ast
  "Returns AST of the clojure code passed to it."
  ([code]
   (let [exp (m-parse-expression code)
         expression (first exp)
         remainder (apply str (rest exp))
         tree []
         macros (load-macros (slurp (io/resource "macros")))]
     (ast-helper exp expression remainder tree macros))))

(defn generate-string
  "Clojure parser that returns an AST of the clojure code passed to it."
  [code-str]
  (assoc {} :program (ast code-str)))

(defn generate
  [file]
  (generate-string (slurp file)))

;;(trace/trace-ns 'clojure-parser.core)
;;(trace/untrace-vars mapify bind-args and-expand bind load-macros find-macro de-ref evalate evaluate de-reference expand-macro)
;; (trace/trace-vars -main expand-macro bind-args and-expand bind find-macro de-reference evalate evaluate de-ref)
;;(trace/trace-vars remove-last-nil)
;;(pprint/pprint (-main "test.clj"))



