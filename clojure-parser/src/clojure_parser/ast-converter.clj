(ns clojure-parser.ast-converter
  (:require [clojure-parser.core :as ast-gen]
            [clojure.tools.trace :as trace]
            [clojure.pprint :as pprint]
            [clojure-parser.utilities :refer :all])
  (:gen-class))

(declare get-form get-return-form get-forms)

(defn jsonify []
  ; replace nil with null
  ; replace = with === 
)

(defn get-identifier [identifier]
   {"type" "Identifier"
    "name" (str identifier)})

(defn get-block [body]
  {"type" "BlockStatement"
   "body" body})

(defn get-literal-raw-value [value type]
  (condp = type
    'number (str value)
    'string (quotify value)
    'boolean (str value)
    'nil "null"))

(defn get-literal
  ([value]
   (cond
    (number? value) (get-literal value 'number)
    (string? value) (get-literal value 'string)
    (instance? Boolean value) (get-literal value 'boolean)
    (= :nil value) (get-literal nil 'nil)
    true (get-identifier value)))

  ([value type]
   (assoc {"type" "Literal" "value" value} "raw" (get-literal-raw-value value type))))

(defn get-operator [form]
  (let [operator (operator form)
        operands (operands form)
        jst {"type" "BinaryExpression"
             "operator" (str operator)
             "right" (get-form (last operands))}]
    (if (= (count operands) 2)
      (assoc jst "left" (get-form (first operands)))
      (assoc jst "left" (get-operator {operator (butlast operands)})))))

(defn get-return [argument]
  {"type" "ReturnStatement"
   "argument" argument})

(defn get-return-operator [form]
  (get-return (get-operator form)))

(defn get-return-literal [form]
  (get-return (get-literal form)))

(defn get-if-common [form func]
  (let [body (operands form)]
    {"type" "IfStatement"
     "test" (get-form (first body))
     "consequent" (get-block [(func (second body))])
     "alternate" (get-block [(func (last body))])}))

(defn get-if [form]
  (get-if-common form get-form))

(defn get-return-if [form]
  (get-if-common form get-return-form))

(defn get-return-form [form]
  (cond (if? form) (get-return-if form)
        (operator? form) (get-return-operator form)
        (literal? form) (get-return-literal form)))

(defn get-fn-body [forms]
  (get-block (conj (into [] (map get-form (butlast forms)))
                   (get-return-form (last forms)))))

(def get-fn-param get-identifier)

(defn get-fn-params [params]
  ; TODO generalize
  (into [] (map get-fn-param params)))

(defn get-fn [form]
  (let [operands (operands form)
        identifier (first operands)
        value (second operands)]
    {"type" "FunctionDeclaration"
     "id" (get-identifier identifier)
     "params" (get-fn-params (:vector (first (:fn value))))
     "body" (get-fn-body (rest (:fn value)))
     "generator" false
     "expression" false}))

(defn get-var [form]
  (let [operands (operands form)
        identifier (first operands)
        value (second operands)]
    {"type" "VariableDeclaration"
     "declarations" [{"type" "VariableDeclarator"
                      "id" (get-identifier identifier)
                      "init" (get-form value)}]
     "kind" "var"}))

(defn get-def [form]
  (if (defn? form)
    (get-fn form)
    (get-var form)))

(defn get-fn-call [form]
  {"type" "CallExpression"
   "callee" (get-identifier (operator form))
   "arguments" (get-forms (operands form))})

(defn get-exp [form]
  {"type" "ExpressionStatement"
   "expression" (get-form form)})

(defn get-form [form & {:keys [top-level-form] :or {top-level-form false}}]
  (cond
   (literal? form) (get-literal form)
   (def? form) (get-def form)
   (if? form) (get-if form)
   top-level-form (get-exp form)
   (operator? form) (get-operator form)
   (fn-call? form) (get-fn-call form)))

(defn get-forms [forms & {:keys [top-level-forms] :or {top-level-forms false}}]
  (into [] (map #(get-form % :top-level-form top-level-forms) forms)))

(defn get-program [body]
  {"type" "Program"
   "body" body
   "sourceType" "script"})

(defn get-ast [ast]
  (get-program (get-forms (:program ast) :top-level-forms true)))

(defn -main []
  ; (trace/trace-ns 'clojure-parser.ast-converter)
  ; (trace/trace-ns 'clojure-parser.utilities)
  (def ast (ast-gen/-main "fact.clj"))
  ; (def ast (ast-gen/-main "test.clj"))
  (pprint/pprint ast)
  (println "\n\n\n\n")
  (pprint/pprint (get-ast ast)))

(-main)
