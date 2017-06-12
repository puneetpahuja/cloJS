(ns clojure-parser.utilities
  (:require [indent.indent :refer [indent-dispatch]]
            [clojure.pprint :as pprint]
            [clojure.data.json :as json]))

(defn map->vector
  "Converts {key value} to [key value]. Map should have only one key-value pair."
  [map]
  (into [] (first (seq map))))

(defn operator [form]
  (first (map->vector form)))

(defn operands [form]
  (second (map->vector form)))

(def exp? map?)

(defn exp-type? [type form]
  (and (exp? form) (= (operator form) type)))

(def literal? (complement exp?))
(def if? (partial exp-type? :if))
(def def? (partial exp-type? :def))
(def defn? (partial exp-type? :defn))
(def vec? (partial exp-type? :vector))
(def map-ds? (partial exp-type? :map))
(def do? (partial exp-type? :do))

(def operators #{'= '* '+ '- '/ '<= '>= '< '>})
;(def operator-symbols #{'= '* '+ '- '/ '<= '>= '< '>})
;(def operators (into operator-symbols (map keyword operator-symbols)))

(defn fn-call? [form]
  (and (exp? form) (symbol? (operator form))))

(defn operator? [form]
  (and (exp? form) (contains? operators (operator form))))

(defn quotify [value]
  (str \" value \"))

(defn print-json [json]
  (pprint/with-pprint-dispatch indent-dispatch (pprint/pprint json)))

(defn form-is? [form forms]
  (reduce #(or %1 (%2 form)) false forms))

(def clojure->js-map {'= '===})

(defn clojure->js [symbol]
  (get clojure->js-map symbol symbol))

(defn jsonify [json-map]
  (json/write-str json-map))

(def lambda? (partial exp-type? :fn))
