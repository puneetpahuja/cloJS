(ns clojs.ast-generator
  (:require [clojure.string :as string]
            [clojure.algo.monads :refer :all]
            [clojure.tools.trace :as trace]
            [clojure.pprint :as pprint]
            [clojs.utilities :refer :all]
            [clojure.java.io :as io]
            [clojs.parsers :refer :all]
            [clojs.monads :refer :all]
            :reload)
  (:gen-class))

(declare m-argument m-parse-expression)

(defn filter-skip [ast]
  (read-string (string/replace (str ast) ":skip" "")))

(defn process-map-properties [properties]
  (reduce concat () properties))

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
       elements (nested-none-or-more (match-one m-parse-expression
                                                m-argument
                                                (skip-one-or-more parse-space)
                                                (skip-one-or-more parse-newline)))
       closing-bracket (match-one parse-close-square-bracket)]
      (filter-skip (apply list :vector elements))))

  (def m-parse-map-property
    (domonad
      [_ (skip-none-or-more parse-space)
       key (match-one m-argument)
       _ (skip-one-or-more parse-space)
       value (match-one m-argument m-parse-expression)
       _ (skip-none-or-more parse-space)]
      [key value]))


  (def m-parse-map
    "This matches vectors"
    (domonad
      [opening-bracket (match-one parse-curly-bracket)
       elements (nested-none-or-more m-parse-map-property)
       closing-bracket (match-one parse-close-curly-bracket)]
      (filter-skip (apply list :map (process-map-properties elements)))))

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
      (if (contains? #{clojure.lang.LazySeq clojure.lang.PersistentList} (type argument))
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
