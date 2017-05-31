(def a 10)
(def b "hello")
(def c nil)
(def d a)

(def e (+ 1 2 3 4))

(defn factorial [n]
  (if (= n 0)
    1
    (* n (factorial (- n 1)))))

(factorial a)

(def f {:a 1 "b" 2 c 3})
(def g [1 2 3])
{:a 5 :b 6}
[5 6]
