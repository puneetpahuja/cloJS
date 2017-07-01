(def a 10)
(def b "hello")
(def c nil)
(def d a)
(def e (+ 1 2 3 4))
(def f {"a" 1 "b" 2 "c" 3})
(def g [1 2 3])

(defn print_multiple [x y z a b c]
  (if (= 1 1)
    (do (console.log "x :" x)
        (console.log "y :" y)
        z)
    (do (console.log "a :" a)
        (console.log "b :" b)
        (console.log "c :" c))))

(def z (print_multiple (+ 1 2 3 4 5 6) "a" true [1 2 3 {"a" 1 "b" c "c" [1 2 {"x" 10 "y" [20 30]} 3]}] nil {"a" 1 "b" [1 2 3 {"a" 1 "b" c "c" [1 2 {"x" 10 "y" [20 30]} 3]}]}))

(console.log "z :" z)
