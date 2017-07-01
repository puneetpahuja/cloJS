(defn remove-from-start
  [str substr]
  (str.substring substr.length))

(defn parse-space
  [string]
  (let [space (re-find #"^\s+" string)]
    (if space
      [space (remove-from-start string space)]
      [nil string])))

(defn parse-number
  [string]
  (let [string (second (parse-space string))
        numstr (re-find #"^\-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?" string)]
    (when numstr
      [(read-string numstr) (remove-from-start string numstr)])))

(defn parse-char
  [string char]
  (let [string (second (parse-space string))
        first-char (first string)
        parseable? (= char first-char)]
    (when parseable?
      [char (subs string 1)])))

(defn parse-word
  [string word parsed-object]
  (let [string (second (parse-space string))
        token (re-find (re-pattern (str "^" word  "($|[,\\s\\]\\}])")) string)]
    (when token
      [parsed-object (remove-from-start string word)])))

(defn parse-string
  [string]
  (let [string (second (parse-space string))
        parsed-string (re-find #"^\".*?(?<!\\)\"" string)]
    (when parsed-string
      [(subs parsed-string 1 (dec (count parsed-string))) (remove-from-start string parsed-string)])))

(defn parse-bool
  [string]
  (or (parse-word string "true" true) (parse-word string "false" false)))

(defn parse-null
  [string]
  (parse-word string "null" nil))

(def parse-key parse-string)

(defn parse-key-and-value
  [string]
  (let [key (parse-key string)]
    (when key
      (let [colon (parse-char (second key) \:)]
        (when colon
          (declare parse-value)
          (let [value (parse-value (second colon))]
            (when value
              [{(first key) (first value)} (second value)])))))))

(defn parse-object
  "Returns a hash-map equivalent to JSON-STR if JSON-STR is properly formed, NIL otherwise."
  [string]
  (let [object (parse-char string \{)]
    (when object
      (loop [json-map {}
             string (second object)
             object-end (parse-char string \})]
        (if object-end
          [json-map (second object-end)]
          (let [key-and-value (parse-key-and-value string)]
            (when key-and-value
              (let [comma (parse-char (second key-and-value) \,)
                       end (parse-char (second key-and-value) \})]
                (when (or comma end)
                  (if comma
                    (recur (into json-map (first key-and-value)) (second comma) end)
                    (recur (into json-map (first key-and-value)) (second end) end)))))))))))

(defn parse-array
  [string]
  (let [array (parse-char string \[)]
    (when array
      (loop [vec []
             string (second array)
             array-end (parse-char string \])]
        (if array-end
          [vec (second array-end)]
          (let [value (parse-value string)]
            (when value
              (let [comma (parse-char (second value) \,)
                    end (parse-char (second value) \])]
                (when (or comma end)
                  (if comma
                    (recur (into vec [(first value)]) (second comma) end)
                    (recur (into vec [(first value)]) (second end) end))))))))))) 

(defn parse-value
  [string]
  (or (parse-null string) (parse-bool string) (parse-string string) (parse-number string) (parse-object string) (parse-array string)))

(defn -main
  "Pretty prints result map if input string is ok, prints error message otherwise."
  [& args]
  (let [input-filename (first args)
        json-str (slurp input-filename)
        error-msg "ERROR : Invalid input JSON"
        json (parse-value json-str)]
    (if (and json (blank? (second json)))
      (with-pprint-dispatch indent.indent/indent-dispatch (pprint (first json)))
      (println error-msg))))
