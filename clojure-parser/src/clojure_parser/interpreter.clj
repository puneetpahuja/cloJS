(ns clojure-parser.interpreter
  (:gen-class))

(require '[clojure-parser.core :as parser])

(defn forms [form]
  (form
    {:def :def
    :plus "+"
    :minus "-"
    :multiply "*"
    :divide "/"
    :equals "="}))

(defmacro definate [fn-name args & body]
  (let [name (symbol (name fn-name))]
  `(def ~name (fn ~args ~@body))))

(defn lambdinate [& stuff]
  (println stuff))

(defn vectorate [vector-map]
  (vec (map #(symbol (name %)) (:vector vector-map))))

(defn express [expression-map]
  (concat (list (symbol (name (forms (first (keys expression-map))))))
          (for [arg (first (vals expression-map))] (symbol (name arg)))))

(defn evaluate [expression]
  (let [func (:form expression)
        args (:args expression)]
    (if (= :def func) 
      (list (first args)
        (vectorate (second args))
        (express (last args)))
      (if (= :fn func) 
        (lambdinate args)
        (println expression)))))

(defn interpret-expression [exp]
  (let [result (first (parser/parse-expression exp))]
    (if (= :expr (first result))
      (let [expression (rest result)]
        (evaluate expression))
      (read-string (rest result)))))

(defn -main [path]
  (let [tree (parser/ast (slurp path))]
    (clojure.pprint/pprint
     (assoc {} :program
            (for [expression tree]
              (evaluate expression))))))
