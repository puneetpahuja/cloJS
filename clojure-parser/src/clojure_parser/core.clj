(ns clojure-parser.core
  (:gen-class))

(defn not-nil? [element]
  (not (nil? element)))

(defn parse-sequence [string functions]
  (filter (fn [x] (not-nil? (first x)))
          (for [function functions]
            (function string))))

(defn extract [element code]
  (apply str (drop (count element) code)))

(defn parse-identifier [code]
  (let [identifier (re-find #"^\w+[\\?]?" code)]
    (if (nil? identifier)
      [nil code]
      (if (= "true" identifier)
        [nil code]
        (if (= "false" identifier)
          [nil code]
          [identifier (extract identifier code)])))))

(defn parse-keyword [code]
  (let [keyword (re-find #"^:\w+" code)]
    (if (nil? keyword)
      [nil code]
      [keyword (extract keyword code)])))

(defn parse-space [code]
  (let [space (re-find #"^\s+" code)]
    (if (nil? space)
      [nil code]
      [space (extract space code)])))

(defn parse-number [code]
  (let [number (re-find #"^[+-]?\d+[.]?[\d+]?\s" code)]
    (if (nil? number)
      [nil code]
      [(read-string number) (extract number code)])))

(defn parse-boolean [code]
  (let [boolean (first (parse-identifier code))]
    (cond
      (nil? boolean) [nil code]
      (= "true" boolean) [true (extract boolean code)]
      (= "false" boolean) [false (extract boolean code)]
      :else [nil code])))

(defn parse-reserved [code]
  (let [reserved-keyword (first (parse-identifier code))]
    (cond
      (nil? boolean) [nil code]
      (= "def" reserved-keyword) ["def" (extract reserved-keyword code)]
      (= "defn" reserved-keyword) ["defn" (extract reserved-keyword code)]
      (= "nil" reserved-keyword) ["nil" (extract reserved-keyword code)]
      (= "fn" reserved-keyword) ["fn" (extract reserved-keyword code)]
      (= "atom" reserved-keyword) ["atom" (extract reserved-keyword code)] 
      (= "keyword" reserved-keyword) ["keyword" (extract reserved-keyword code)]
      (= "symbol" reserved-keyword) ["symbol" (extract reserved-keyword code)]
      (= "name" reserved-keyword) ["name" (extract reserved-keyword code)]
      (= "intern" reserved-keyword) ["intern" (extract reserved-keyword code)]
      (= "namespace" reserved-keyword) ["namespace" (extract reserved-keyword code)]
      (= "keyword?" reserved-keyword) ["keyword?" (extract reserved-keyword code)]
      (= "for" reserved-keyword) ["for" (extract reserved-keyword code)]
      (= "require" reserved-keyword) ["require" (extract reserved-keyword code)]
      :else [nil code])))

(defn parse-operator [code]
  (let [operator (re-find #"^[+-\\*\/=><][+-\\*\/=><]?\s" code)]
    (cond
      (= "+ " operator) ["+" (apply str (rest (rest code)))]
      (= "- " operator) ["-" (apply str (rest (rest code)))]
      (= "* " operator) ["*" (apply str (rest (rest code)))]
      (= "/ " operator) ["/" (apply str (rest (rest code)))]
      (= "= " operator) ["=" (apply str (rest (rest code)))]
      (= "== " operator) ["==" (apply str (rest (rest code)))]
      (= "> " operator) [">" (apply str (rest (rest code)))]
      (= "< " operator) ["<" (apply str (rest (rest code)))]
      (= ">= " operator) [">=" (apply str (rest (rest code)))]
      (= "<= " operator) ["<=" (apply str (rest (rest code)))]
      :else [nil code] )))

(defn parse-string [code]
  (let [string (last (re-find #"^\"([^\"]*)\"" code))]
    (if (nil? string) 
      [nil code]
      [string (apply str (drop (+ 2 (count string)) code))])))

(defn parse-function [code]
  (let [function (first (parse-sequence code [parse-space
                                              parse-keyword
                                              parse-reserved
                                              parse-operator
                                              parse-identifier]))]
    (if (= \space function) 
      (recur (rest code))
      function)))

(defn -main
  "Clojure parser that returns an AST of the clojure code passed to it."
  ([path]
   (println (slurp path))))
