(def a 10)
(def b "hello")
(def c nil)
(def d a)
(def e (+ 1 2 3 4))
(def f {"a" 1 "b" 2 "c" 3})
(def g [1 2 3])


(defn factorial [n]
  (if (= n 0)
    1
    (* n (factorial (- n 1)))))

(factorial a)

(defn print_multiple [x y z a b c]
  (if true
    (do (func x)
        (func y)
        (func z))
    (do (func a)
        (func b)
        (func c))))

(def z (print_multiple (+ 1 2 3 4 5 6) "a" true [1 2 3 {"a" 1 "b" c "c" [1 2 {"x" 10 "y" [20 30]} 3]}] nil {"a" 1 "b" [1 2 3 {"a" 1 "b" c "c" [1 2 {"x" 10 "y" [20 30]} 3]}]}))

