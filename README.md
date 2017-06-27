# clojs
Combining the simplicity of Clojure syntax with the power of JavaScript libraries.

## Who is it for?
This is for:
  - JavaScript programmers who want to use simple lisp-like functional syntax of clojure. You can call all the javascript functions.
  - Clojure programmers who want to use JavaScript functions and libraries.

## Introduction
You write Clojure syntax code using JavaScript functions and it is converted into a JS code.

## Components
Ramshreyas's [seqingclojure](https://github.com/Ramshreyas/seqingclojure) - it makes the AST for the input code.

We use this AST to convert it into an equivalent JS AST in JSON format.

estool's npm package [escodegen](https://github.com/estools/escodegen) - it converts the JS AST into JS code.

## Syntax
Our syntax | Converted JS code
---------- | -----------------
```clojure
(def a 5 b 6)
``` | ```javascript
const a = 5, b = 6
```
