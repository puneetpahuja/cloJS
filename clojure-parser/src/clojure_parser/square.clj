(defn square [x]
  (* x x))

(def square (fn [x] (* x x)))

(square 5)

(>= 3 4)

(>= 5 6)

(>> 4 inc square str)
