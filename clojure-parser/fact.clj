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
