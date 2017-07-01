(def a b[3])
(a b[4])
(def a 1)
(def a (func 5))
(func 5 4)
(def a (+ 1 2))
(+ 12 13)

(cond 1 (+ 2 2) 3 4 (+ 5 5) 6 7 8)

(do
  (def a 1)
  (def b 2))

(defn print-multiple [x y z a b c]
  (if true
    (do (func x)
        (func y)
        (func z))
    (do (func a)
        (func b)
        (func c))))
