(defn square [x]
  (* x x))

(def square (fn [x] (* x x)))

(square 5)

(null? 3)

(defmacro defmonad [name mbind mresult]
  (def ~name (list ~mbind ~mresult)))

(defmonad identity
  (fn [mv mf] (mf mv))
  (fn [x] x))
