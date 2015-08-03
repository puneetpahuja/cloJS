(defn square [x]
  (* x x))

(def square (fn [x] (* x x)))

(square "5")

(test "1" 2 3 4)
