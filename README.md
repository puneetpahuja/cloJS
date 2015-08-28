# seqing clojure
Learning Clojure by writing a Clojure parser. In Clojure.

### Introduction ###

So you want to learn Clojure? Parse it. In Clojure. Oh and do it with *Monads*. At least that's how we do things in [GeekSkool][]. 

[GeekSkool]: http://geekskool.com

GeekSkool is an intensive 3-month programme where programmers come to improve their skills. The preferred way that [Santosh Rajan][], the founder of GeekSkool, likes to do this is by throwing daunting challenges at the unsuspecting student. 

[Santosh Rajan]: https://twitter.com/santoshrajan

So we're going to learn Clojure by 'parsing' Clojure, using Clojure. But what exactly do we mean by 'parsing'? 

In this project, parsing means converting valid Clojure code into something called an *[Abstract Syntax Tree][]*, or AST for short.

[Abstract Syntax Tree]: https://en.wikipedia.org/wiki/Abstract_syntax_tree

Any piece of code can be represented as a tree of objects and operators and functions. Here is some simple Clojure code and the tree that represents it:

```clojure
(* 3 (+ 5 2))
```
![alt text][ast]

[ast]: http://www.codeproject.com/KB/recipes/sota_expression_evaluator/simplified_ast.png

Now we can hardly expect the parser to spit out an image like this - what we want is a data structure that represents this tree. In Clojure, that would be a map - of parents and children. If we look at the tree again, 
we see that:

\+ has two children: 5 and 2

\* has two children: 3, and \+ with two children 5 and 2

This implies \+ could be mapped to a vector containing 5 and 2 (vector because there is an implicit order to the children, and also the Clojure convention is to put arguments in vectors): 

```clojure
{+ [5 2]}
```

\* can then be mapped to a vector containing 3 and the afore-mentioned map: 

```clojure
{* [3 {+ [5 2]}]}
```

So 

![alt text][ast]

[ast]: http://www.codeproject.com/KB/recipes/sota_expression_evaluator/simplified_ast.png

is

```clojure
{* [3 {+ [5 2]}]}
```

OK. So we are going to write a Clojure program that takes such simple Clojure code as an input, and returns a map like the one above as the output, except with some more helpful annotations. 

We'll start by breaking up the problem into small, solvable pieces. We are only going to parse a subset of Clojure, with the following language objects:

* Numbers
* Strings
* Symbols (Literals which generally name other objects)
* Keywords (Symbols prefaced by ':' used especially in maps)
* Identifiers (Symbols which are the names of functions defined in the program, and built-in functions)
* Booleans (The symbols true and false)
* Back-ticks, Tildes and Ampersands (so we can implement simple macros)
* Parentheses
* Square brackets
* Operators
* Vectors ([arg1 arg2..] We will ignore other types of collections for now)

These objects and characters composed together, form S-expressions in the following format:

```clojure
(func-name arg1 arg2 arg3...)
```
(Optionally, macro body expressions can start with back-ticks before the opening parens - but we'll get to that later)

Function names can only be composed by the following in our subset of Clojure:

* Operators
* Keywords (when applied to maps, for example)
* Identifiers

Arguments can be composed by any of the following: 

* Numbers
* Strings
* Symbols
* Identifiers (functions can be passed as arguments)
* Keywords
* Booleans
* Vectors 
* Tildes (Prefaced to dereferencable symbols in the Macro-body - more later...)
* S-expressions themselves (nested expression as in our exaple above)

We will obviously have to parse the parentheses as well to understand where an expression begins, and where it ends.

So let's start by just parsing these individual components and then move on to *composing* these parsers together to parse function names and arguments - using the Parser Monad.

What exactly does a parser look like?

A parser is a function that takes a string as an input and returns two things:

* A 'consumed' part
* The rest of the string

or 

* nil

if it doesn't find what it's looking for. 


```haskell
Parser :: String -> [Anything, String] | nil
```
So a space parser could take 

" the rest of the string" 

and return 

[" ", "the rest of the string"]

"string without starting space" would just return `nil`.

This is what it looks like in poorly written, non-idiomatic Clojure:

```clojure
(defn parse-space [code]
  (let [space (re-find #"^\s+" code)]
    (if (nil? space)
      nil
      [space (extract space code)])))
```
'Extract' here is a helper function that does exactly what it says on the tin.

Similarly,

```clojure
(defn parse-newline [code]                                                                 
    (if (= \newline (first code))                                                                           
      [\newline (apply str (rest code))]
      nil))

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
      [:macro-body (extract char code)]
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

(defn parse-keyword [code]
  (let [keyword (re-find #"^:[\w-.]+" code)]
    (if (nil? keyword)
      nil
      [(read-string keyword) (extract keyword code)])))
      
(defn parse-operator [code]
  (let [operator (re-find #"^[+-\\*\/=><][+-\\*\/=><]?\s" code)]
    (cond
      (= "+ " operator) [:plus (extract operator code)]
      (= "- " operator) [:minus (extract operator code)]
      (= "* " operator) [:multiply (extract operator code)]
      (= "/ " operator) [:divide (extract operator code)]
      (= "= " operator) [:equals (extract operator code)]
      (= "> " operator) [:greater-than (extract operator code)]
      (= "< " operator) [:less-than (extract operator code)]
      :else nil)))
```
So a whole host of simple parsers like this can be given the responsibility of parsing little pieces of the code such as numbers, strings, symbols and identifiers.

We are going to combine little parsers such as these to parse more complex structures - and this is where things get interesting.

Cue *Monads*.

Combining or *composing* functions is something we are very familiar with when it comes to, say, math.

We can easily chain a series of mathematical functions such as these without wondering if the whole, [rube-goldberg][] structure will work or not.

[rube-goldberg]: https://en.wikipedia.org/wiki/Rube_Goldberg_machine

[txt][math]

[math]: https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcSzkS0qcrsBtSgZH_tlPadeRveBd_lm3UvMTD1Uxps7AppMt71Z
