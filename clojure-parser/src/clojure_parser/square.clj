(defn square [x]
  (* x x))

(def square (fn [x] (* x x)))

(defmacro defn [name args body]
  `(def ~name (fn ~args ~body)))

(square 5)
