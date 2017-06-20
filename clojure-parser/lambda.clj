(defn a [x]
  (console.log x)
  (console.log 1))

(def b (fn [x]
         (console.log (* x x))
         (console.log 2)))

(a 5)

(b 6)
