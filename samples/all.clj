(def a 10)
(def b "hello")
(def c nil)
(def d a)
(def e (+ 1 2 3 4))
(def f {"a" 1 "b" 2 "c" 3})
(def g [1 2 3])
(def h g[2])

(console.log g[1])

(let a 2 b 5)

(cond 1 (+ 2 2) 3 4 (+ 5 5) 6 7 8)

(fn [x]
  (cond
    (is_array_member form) (do (get_array_member form) (+ 1 2))
    (is_defn form)     (get_defn form)
    (is_def form)      (get_const form)
    (is_if form)       (get_if form)
    (is_do form)       (get_do form)
    (is_vec form)      (get_vec form)
    (is_lambda form)   (get_lambda form)
    (is_map_ds form)   (get_map_ds form)
    (is_literal form)  (get_literal form)
    (is_operator form) (get_operator form)
    (is_fn_call form)  (get_fn_call form)
    true             nil))

(fn [x]
  (console.log x)
  (let a 2 b 5)
  (console.log (+ 5 x a b)))

(defn print_multiple [x y z a b c]
  (if (= 1 1)
    (do (console.log "x :" x)
        (console.log "y :" y)
        z)
    (do (console.log "a :" a)
        (console.log "b :" b)
        (console.log "c :" c))))

(def z (print_multiple (+ 1 2 3 4 5 6) "a" true [1 2 3 {"a" 1 "b" c "c" [1 2 {"x" 10 "y" [20 30]} 3]}] nil {"a" 1 "b" [1 2 3 {"a" 1 "b" c "c" [1 2 {"x" 10 "y" [20 30]} 3]}]}))


(defn factorial [n]
  (if (= n 0)
    1
    (* n (factorial (- n 1)))))

(console.log "factorial of" a "is" (factorial a))

(defn a []
  5)

(console.log (a))
(readline)

(defn a [x]
  (console.log x)
  (console.log 1))

(def b (fn [x]
         (console.log (* x x))
         (console.log 2)))


(def a 5 b 6 c (+ 1 2) d (fun 5 6 (+ 1 2)) e (fn [x]
                                               (console.log (* x x))
                                               (console.log 2)))

(defmacro m-bind [mv mf]
  (conj (list ~mv test) ~mf))

(m-bind mvv mff)
