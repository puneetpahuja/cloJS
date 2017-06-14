(def a 6)

(defn factorial [n]
  (if (= n 0)
    1
    (* n (factorial (- n 1)))))

(console.log process.argv[0])
(console.log process.argv[1])
(console.log process.argv[2])
(console.log process.argv[3])

(console.log "factorial of" a "is" (factorial a))

