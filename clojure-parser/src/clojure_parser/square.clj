(defn square [x]
  (* x x))

(def square (fn [x] (* x x)))

(square 5)

(defmacro test [arg & body]
  `(test ~arg (inner ~@body)))

(test 1 2 3 4)
