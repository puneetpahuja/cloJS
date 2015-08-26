(defn square [x]
  (* x x))

(def square (fn [x] (* x x)))

(square 5)

(null? 3 2)

(defmacro domonad [bindings & body]
  `(m-bind ~bindings (m-result ~@body)))

(domonad bindings body1 body2)
