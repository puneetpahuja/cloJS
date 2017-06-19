(ns clojure-parser.ast-converter
  (:require [clojure-parser.core :as ast-gen]
            [clojure.tools.trace :as trace]
            [clojure.pprint :as pprint]
            [clojure-parser.utilities :refer :all]
            [clojure.string :as str]
            [me.raynes.conch :refer [programs with-programs let-programs] :as sh]
            [clojure.tools.cli :refer [cli]])
  (:gen-class))

(comment TODO
         * support cond
         * convert a script rather than run it (give an option for running)
         * write basic test cases so that regression testing is easy
         * macro support - check ast generator if it expands the macro (built-in and domonad)
         * provide an option in the linux command whether he wants to run the generated js file or not
         * provide support for giving multiple js files at once in command line input
         
         BUGS
         * running the linux command takes a lot of time
         * "(console.log process.argv[3])" is not generated correctly. its generated as
         "{console.log [process.argv {:vector [3]}]}" instead of "{console.log [process.argv[3]]}"
         fix - make "identifier[identifier]" as a separate entity
         * empty map gen gives error
         
         DOC
         * you cant use "-" in function/variable names because running the converted js code will give error. follow js naming conventions.

         COMMIT
         * use arrow functions)

(def js-generator-script-string "let fs = require (\"fs\"); let gen = require (\"escodegen\"); fs.writeFileSync(process.argv[2], gen.generate(JSON.parse(fs.readFileSync(process.argv[1], \"utf8\"))));")

(declare get-form get-return-form get-forms)

(defn get-identifier [identifier]
  {"type" "Identifier"
   "name" (str identifier)})

(defn get-block [body]
  {"type" "BlockStatement"
   "body" body})

(defn get-do-common [form func]
  (let [body (operands form)]
    (get-block (conj (get-forms (butlast body) :do)
                     (func (last body) :do)))))

(defn get-do [form]
  (get-do-common form get-form))

(defn get-return-do [form]
  (get-do-common form get-return-form))

(defn get-literal-raw-value [value type]
  (condp = type
    'number (str value)
    'string (quotify value)
    'boolean (str value)
    'nil "null"))

(defn get-literal
  ([value]
   (cond
     (number? value)           (get-literal value 'number)
     (string? value)           (get-literal value 'string)
     (instance? Boolean value) (get-literal value 'boolean)
     (= :nil value)            (get-literal nil 'nil)
     :else                     (get-identifier value)))

  ([value type]
   (assoc {"type" "Literal" "value" value} "raw" (get-literal-raw-value value type))))

(defn get-operator [form]
  (let [operator (operator form)
        operands (operands form)
        jst {"type" "BinaryExpression"
             "operator" (str (clojure->js operator))
             "right" (get-form (last operands) :op)}]
    (if (= (count operands) 2)
      (assoc jst "left" (get-form (first operands) :op))
      (assoc jst "left" (get-operator {operator (butlast operands)})))))

(defn get-return [argument]
  {"type" "ReturnStatement"
   "argument" argument})


(defn get-if-common [form func]
  (let [body (operands form)]
    {"type" "IfStatement"
     "test" (get-form (first body) :if-test)
     "consequent" (func (second body) :if)
     "alternate" (if (= (count body) 2)
                   nil
                   (func (last body) :if))}))

(defn get-if [form]
  (get-if-common form get-form))

(defn get-return-if [form]
  (get-if-common form get-return-form)) 

(defn get-return-form [form parent]
  (cond (if? form) (get-return-if form)
        (do? form) (get-return-do form)
        :else (get-return (get-form form :return))))

(defn get-fn-body [forms]
  (get-block (conj (get-forms (butlast forms) :defn)
                   (get-return-form (last forms) :defn))))

(def get-fn-param get-identifier)

(defn get-fn-params [params]
                                        ; TODO generalize
  (into [] (map get-fn-param params)))

(defn get-arrow [fn-forms]
  {"type" "ArrowFunctionExpression"
   "id" nil
   "params" (get-fn-params (operands (first fn-forms)))
   "body" (get-fn-body (rest fn-forms))
   "generator" false
   "expression" false
   "async" false})

(defn get-const-helper [identifier init]
  {"type" "VariableDeclaration"
   "declarations" [{"type" "VariableDeclarator"
                    "id" (get-identifier identifier)
                    "init" init}]
   "kind" "const"})

(defn get-defn [form]
  (let [defn-body (operands form)
        fn-name (first defn-body)
        fn-forms (rest defn-body)]
    (get-const-helper fn-name (get-arrow fn-forms))))

(defn get-lambda [form]
  (get-arrow (operands form)))

(defn get-const [form]
  (let [operands (operands form)
        identifier (first operands)
        value-form (second operands)]
    (get-const-helper identifier (get-form value-form :const))))

(defn get-fn-call [form]
  {"type" "CallExpression"
   "callee" (get-identifier (operator form))
   "arguments" (get-forms (operands form) :fn-call)})


(defn get-exp [form]
  {"type" "ExpressionStatement"
   "expression" (get-form form :exp)})

(defn get-map-property [property]
  {"type" "Property"
   "key" (get-form (first property) :map)
   "computed" false
   "value" (get-form (second property) :map)
   "kind" "init"
   "method" false
   "shorthand" false})

(defn get-map-properties [properties]
  (into [] (map get-map-property (partition 2 properties)))) 

(defn get-map-ds [form]
  {"type" "ObjectExpression"
   "properties" (get-map-properties (operands form))})

(defn get-vec [form]
  {"type" "ArrayExpression"
   "elements" (get-forms (operands form) :vec)})

(defn get-form [form parent]
  (cond
    (and (form-is? form [vec? literal? operator? fn-call? lambda?]) (contains? #{:defn :if :do :program} parent)) (get-exp form)
    (defn? form)     (get-defn form)
    (def? form)      (get-const form)
    (if? form)       (get-if form)
    (do? form)       (get-do form)
    (vec? form)      (get-vec form)
    (lambda? form)   (get-lambda form)
    (map-ds? form)   (get-map-ds form)
    (literal? form)  (get-literal form)
    (operator? form) (get-operator form)
    (fn-call? form)  (get-fn-call form)
    :else            {:not-a-form form}))

(defn get-forms [forms parent]
  (into [] (map #(get-form % parent) forms)))

(defn get-program [body]
  {"type" "Program"
   "body" body
   "sourceType" "script"})

(defn get-ast [ast]
  (get-program (get-forms (:program ast) :program)))

(defn -main [& args]
  ;; (trace/trace-ns 'clojure-parser.ast-converter)
  ;; (trace/trace-ns 'clojure-parser.utilities)
  ;; (trace/trace-vars )
  (let [[opts args banner] (cli args
                                ["-h" "--help" "Run as \"clojs <input_clojure_file.clj>\""
                                 :default false :flag true])
        input-file (first args)
        ast (ast-gen/-main input-file)
        js-ast-map (get-ast ast)
        js-ast-json (jsonify js-ast-map)
        input-filename-parts (str/split input-file #"\.")
        output-filename-parts (if (> (count input-filename-parts) 1) (vec (butlast input-filename-parts)) input-filename-parts)
        output-file (str/join "." output-filename-parts)
        json-name (str output-file ".json")
        js-name (str output-file ".js")]
    (print-json ast)
    ;; (print-json js-ast-json)
    (spit json-name js-ast-json)
    (programs node) 
    (node "-e" js-generator-script-string json-name js-name)
    (println (slurp js-name))
    ;;(println (node js-name))
    ))

(-main "test.clj")
;;(-main "dummy.clj")
;;(-main "fact.clj")
