# `cloJS` [![Clojars Project](https://img.shields.io/clojars/v/clojs.svg)](https://clojars.org/clojs)
A library for Clojure to convert Clojure code to JavaScript. It combines the simplicity of Clojure syntax with the power of JavaScript libraries. So all the function calls are of JavaScript.

## Artifacts
`clojs` artifacts are [released to Clojars](https://clojars.org/clojs/clojs).

If you are using Maven, add the following repository definition to your `pom.xml`:
``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Release
With Leiningen:
![](https://clojars.org/clojs/latest-version.svg)

With Maven:
``` xml
<dependency>
  <groupId>clojs</groupId>
  <artifactId>clojs</artifactId>
  <version>0.1.4</version>
</dependency>
```

## Bugs and Enhancements
Please open issues against the [cloJS repo on Github](https://github.com/puneetpahuja/cloJS/issues).

## Mailing List
Please ask questions on the [cloJS mailing list](https://groups.google.com/forum/#!forum/clojs).

## Who is it for?
This is for:
  - JavaScript programmers who want to use simple LISP like syntax of clojure. All the JavaScript functions can be called.
  - Clojure programmers who want to use JavaScript functions and libraries without learning JavaScript syntax.

## Introduction
You write Clojure syntax code using JavaScript functions and it is converted into JavaScript code.

## Usage

### clojs.clojs
The namespace for code conversion in the `clojs` library is `clojs.clojs`.
``` clj
(require '[clojs.clojs :refer [convert convert-string] :as cj])
```
Convert a code snippet as string with `convert-string`. It takes a string(Clojure code) and returns a string(JavaScript code):

```clj
(convert-string "(def a 1)")
=> const a = 1;
```
Convert one or more files containing code with `convert`. It takes the path of each input file as a string and creates the equivalent JavaScript file(.js) in the same folder as the input file:

```clj
(convert "a.clj" "resources/b.clj")
=> nil
```
This will create `a.js`in the folder where `a.clj` is present and `b.js` in the `resources` folder where `b.clj` is present.
**Note:** If `a.js` or `b.js` are already present in the respective folders, they will be overwritten.

Here's a [sample Clojure app that uses clojs](https://github.com/puneetpahuja/using-clojs)

## Syntax
Here is all the syntax that `clojs` supports:
**Legend** : `a -> b` means `a` in Clojure is converted to `b` in JavaScript

### Literals
Input literal type | Input | Output | Output literal type
-------------------|-------|--------|--------------------
String | `"abc"` | `'abc'` | String
Number | `123`, `123.456`| `123`, `123.456` | Number
Nil | `nil` | `null` | Null
Boolean | `true`, `false` | `true`, `false` | Boolean
**Note**
* `null` in JavaScript is `nil` in Clojure. 
* Strings in Clojure use only `""` as delimiters.

### Identifiers
Identifiers like variable names, function names are converted as it is.
**Note to Clojure programmers:** Do not use `-` in variable/function names like `string-length` because running the resulting JavaScript code will error out as JavaScript use infix notation and `-` is not allowed in identifers. Use `_` instead like `string_length`.

### Vectors -> Arrays
```clj
["abc" 123 nil true false [1 2 3]]
```
->
```javascript
['abc', 123, null, true, false, [1, 2, 3]]
```
No commas in Clojure. Nesting is supported.

### Maps -> Objects
```clj
{"a" 1 "b" {10 "t" 20 "f"} "c" 3 "d" [1 2 3]}
```
->
```javascript
{'a': 1, 'b': { 10: 't', 20: 'f'}, 'c': 3, 'd': [1, 2, 3]}
```
No semicolons or commas in Clojure. Nesting is supported.

### Operators


## Components
1. Ramshreyas's [seqingclojure](https://github.com/Ramshreyas/seqingclojure) - it makes the AST for the input code.
2. We use this AST to convert it into an equivalent JS AST in JSON format.
3. estool's npm package [escodegen](https://github.com/estools/escodegen) - it converts the JS AST into JS code.

## Syntax
Our syntax | Converted JS code
---------- | -----------------
`(def a 5 b 6)` | `const a = 5, b = 6;`
`(let y 1 z 2)` | `let y = 1, z = 2;`
`(if true x y)` | `if (true) {x;} else {y;}`
`(cond t1 e1 t2 e2 t3 e3)` | `if (t1) {e1;} else if (t2) {e2;} else if (t3) {e3;}`

## License

Released under the Eclipse Public License: <https://github.com/puneetpahuja/cloJS/blob/master/LICENSE>
