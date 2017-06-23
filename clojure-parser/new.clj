(def a [1 2 3 4])
(def b a[2])
(console.log a[3])

(cond
    (array-member? form) (get-array-member form)
    (defn? form)     (get-defn form)
    (def? form)      (get-const form)
    (if? form)       (get-if form)
    (do? form)       (get-do form)
    (vec? form)      (get-vec form)
    (lambda? form)   (get-lambda form)
    (map-ds? form)   (get-map-ds form)
    (literal? form)  (get-literal form)
    (operator? form) (get-operator form)
    (fn-call? form)  (get-fn-call form)
    true             nil)
