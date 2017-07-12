(ns clojs.parsers)

;;; Utility methods
(defn extract [element code]
  (if (= java.lang.Character (type element))
    (apply str (rest code))
    (apply str (drop (count element) code))))

;;; Element parsers
(defn parse-newline [code]
  (if (= \newline (first code))
    [\newline (apply str (rest code))]
    nil))

(defn parse-space [code]
  (let [space (re-find #"^\s+" code)]
    (if (nil? space)
      nil
      [space (extract space code)])))

(defn parse-number [code]
  (let [number (re-find #"^[+-]?\d+[.]?[\d+]?" code)]
    (if (nil? number)
      nil
      [(read-string number) (extract number code)])))

(defn parse-string [code]
  (let [string (last (re-find #"^\"([^\"]*)\"" code))]
    (if (nil? string)
      nil
      [string (apply str (drop (+ 2 (count string)) code))])))

(defn parse-square-bracket [code]
  (let [char (first code)]
    (if (= \[ char)
      [char (extract char code)]
      nil)))

(defn parse-close-square-bracket [code]
  (let [char (first code)]
    (if (= \] char)
      [char (extract char code)]
      nil)))

(defn parse-curly-bracket [code]
  (let [char (first code)]
    (if (= \{ char)
      [char (extract char code)]
      nil)))

(defn parse-close-curly-bracket [code]
  (let [char (first code)]
    (if (= \} char)
      [char (extract char code)]
      nil)))

(defn parse-round-bracket [code]
  (let [char (first code)]
    (if (= \( char)
      [char (extract char code)]
      nil)))

(defn parse-close-round-bracket [code]
  (let [char (first code)]
    (if (= \) char)
      [char (extract char code)]
      nil)))

(defn parse-backtick [code]
  (let [char (first code)]
    (if (= \` char)
      [:escape-macro-body (extract char code)]
      nil)))

(defn parse-tilde [code]
  (let [char (first code)]
    (if (= \~ char)
      [:de-ref (extract char code)]
      nil)))

(defn parse-ampersand [code]
  (let [char (first code)]
    (if (= \& char)
      [(symbol (str char)) (extract char code)])))

(defn parse-identifier [code]
  (let [identifier (re-find #"^[\w-.><=@]+[\\?]?" code)]
    (if (nil? identifier)
      nil
      [identifier (extract identifier code)])))

(defn parse-boolean [code]
  (let [boolean (first (parse-identifier code))]
    (cond
      (nil? boolean) nil
      (= "true" boolean) [true (extract boolean code)]
      (= "false" boolean) [false (extract boolean code)]
      :else nil)))

(defn parse-name [code]
  (let [identifier (first (parse-identifier code))]
    (cond
      (nil? identifier) nil
      (= "true" identifier) nil
      (= "false" identifier) nil
      :else [(symbol (name identifier)) (extract identifier code)])))

(defn parse-deref [code]
  (let [for-deref (parse-tilde code)]
    (if (nil? for-deref)
      nil
      (let [deref (parse-name (last for-deref))]
        [(symbol (str ":de-ref " (first deref))) (last deref)]))))

(defn parse-keyword [code]
  (let [keyword (re-find #"^:[\w-.]+" code)]
    (if (nil? keyword)
      nil
      [(read-string keyword) (extract keyword code)])))

(defn parse-reserved [code]
  (let [reserved-keyword (first (parse-identifier code))]
    (cond
      (nil? reserved-keyword) nil
      (= "def" reserved-keyword) [:def (extract reserved-keyword code)]
      (= "defn" reserved-keyword) [:defn (extract reserved-keyword code)]
      (= "if" reserved-keyword) [:if (extract reserved-keyword code)]
      (= "do" reserved-keyword) [:do (extract reserved-keyword code)]
      (= "defmacro" reserved-keyword) [:defmacro (extract reserved-keyword code)]
      ;; (= "println" reserved-keyword) [:println (extract reserved-keyword code)]
      (= "nil" reserved-keyword) [:nil (extract reserved-keyword code)]
      (= "let" reserved-keyword) [:let (extract reserved-keyword code)]
      (= "fn" reserved-keyword) [:fn (extract reserved-keyword code)]
      ;;(= "atom" reserved-keyword) [:atom (extract reserved-keyword code)]
      ;;(= "keyword" reserved-keyword) [:keyword (extract reserved-keyword code)]
      ;;(= "symbol" reserved-keyword) [:symbol (extract reserved-keyword code)]
      ;;(= "intern" reserved-keyword) [:intern (extract reserved-keyword code)]
      ;;(= "namespace" reserved-keyword) [:namespace (extract reserved-keyword code)]
      ;;(= "keyword?" reserved-keyword) [:keyword? (extract reserved-keyword code)]
      ;;(= "for" reserved-keyword) [:for (extract reserved-keyword code)]
      ;; (= "require" reserved-keyword) [:require (extract reserved-keyword code)]
      :else nil)))

(defn parse-operator [code]
  (let [operator (re-find #"^[+-\\*\/=><][+-\\*\/=><]?\s" code)]
    (cond
      (= "+ " operator) ['+ (extract operator code)]
      (= "- " operator) ['- (extract operator code)]
      (= "* " operator) ['* (extract operator code)]
      (= "/ " operator) ['/ (extract operator code)]
      (= "= " operator) ['= (extract operator code)]
      (= "> " operator) ['> (extract operator code)]
      (= "< " operator) ['< (extract operator code)]
      :else nil)))
