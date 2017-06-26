(defn a [x]
  (console.log x)
  (console.log 1))

(def b (fn [x]
         (console.log (* x x))
         (console.log 2)))

(def a 5 b 6 c (+ 1 2) d (fun 5 6 (+ 1 2)) e (fn [x]
                                               (console.log (* x x))
                                               (console.log 2)))

(let a 2 b 5)

(a 5)

(b 6)
