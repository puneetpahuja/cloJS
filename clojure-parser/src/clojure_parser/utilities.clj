(ns clojure-parser.utilities)

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

(defn defn? [form]
  (and (exp? form)
       (= :def (operator form))
       (exp? (second (operands form)))
       (= :fn (operator (second (operands form))))))

(def literal? (complement exp?))
(def if? (partial exp-type? 'if))
(def def? (partial exp-type? :def))
(def vec? (partial exp-type? :vector))
(def map-ds? (partial exp-type? :map))

(def operator-symbols #{'= '* '+ '- '/ '<= '>= '< '>})
(def operators (into operator-symbols (map keyword operator-symbols)))

(defn fn-call? [form]
  (and (exp? form) (symbol? (operator form))))

(defn operator? [form]
  (and (exp? form) (contains? operators (operator form))))

(defn quotify [value]
  (str \" value \"))

