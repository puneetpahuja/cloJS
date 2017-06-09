(def a 6)

(defn factorial [n]
  (if (= n 0)
    1
    (* n (factorial (- n 1)))))

(console.log "factorial of" a "is" (factorial a))

