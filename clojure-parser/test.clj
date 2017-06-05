(def a 1)
(def a (func 5))
(func 5 4)
(def a (+ 1 2))
(+ 12 13)


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
