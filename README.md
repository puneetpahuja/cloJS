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
With Leiningen ![](https://clojars.org/clojs/latest-version.svg)

With Maven:
``` xml
<dependency>
  <groupId>clojs</groupId>
  <artifactId>clojs</artifactId>
  <version>0.1.4</version>
</dependency>
```
## Dependencies
### `node.js`
* [Install via package manager](https://nodejs.org/en/download/package-manager/) **or**
* [Download](https://nodejs.org/en/download/)

### `escodegen npm package`
`npm`(node.js package manager) is installed with `node.js`. `escodegen` can be installed by typing:

```bash
npm install escodegen
```

 in your terminal/command line.

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
``` clojure
(require '[clojs.clojs :refer [convert convert-string] :as cj])
```
Convert a code snippet as string with `convert-string`. It takes a string(Clojure code) and returns a string(JavaScript code):

```clojure
(convert-string "(def a 1)")
=> const a = 1;
```
Convert one or more files containing code with `convert`. It takes the path of each input file as a string and creates the equivalent JavaScript file(.js) in the same folder as the input file:

```clojure
(convert "a.clj" "resources/b.clj")
=> nil
```
This will create `a.js`in the folder where `a.clj` is present and `b.js` in the `resources` folder where `b.clj` is present.
**Note:** If `a.js` or `b.js` are already present in the respective folders, they will be overwritten.

Here's a [sample Clojure app that uses clojs](https://github.com/puneetpahuja/using-clojs)

## Syntax
Here is all the syntax that `clojs` supports:
**Legend:** `a -> b` means `a` in Clojure is converted to `b` in JavaScript

### Literals
| Input literal type | Input            | Output           | Output literal type |
| ------------------ | ---------------- | ---------------- | ------------------- |
| String             | `"abc"`          | `'abc'`          | String              |
| Number             | `123`, `123.456` | `123`, `123.456` | Number              |
| Nil                | `nil`            | `null`           | Null                |
| Boolean            | `true`, `false`  | `true`, `false`  | Boolean             |

**Note**
* `null` in JavaScript is `nil` in Clojure. 
* Strings in Clojure use only `""` as delimiters.

### Identifiers
Identifiers like variable names, function names are converted as it is.
**Note to Clojure programmers:** Do not use `-` in variable/function names like `string-length` because running the resulting JavaScript code will error out as JavaScript use infix notation and `-` is not allowed in identifiers. Use `_` instead like `string_length`.

### Vectors -> Arrays
```clojure
["abc" 123 nil true false [1 2 3]]
```
->
```javascript
['abc', 123, null, true, false, [1, 2, 3]]
```
No commas in Clojure. Nesting is supported.

### Maps -> Objects
```clojure
{"a" 1 "b" {10 "t" 20 "f"} c 3 "d" [1 2 3]}
```
->
```javascript
{'a': 1, 'b': { 10: 't', 20: 'f'}, c: 3, 'd': [1, 2, 3]}
```
No semicolons or commas in Clojure. Nesting is supported.

**Note:** Literals, identifiers, vectors, objects are not supported at the top-level. They have to be inside parentheses.

### `def` -> `const`
```clojure
(def a 1 b "xyz" c nil d true e [1 2 3] f a g {a 1})
```
->
```javascript
const a = 1, b = 'xyz', c = null, d = true, e = [1, 2, 3], f = a, g = {a: 1}
```

### Array element access and Object property access
```clojure
(def h e[0] i g.a j g["a"])
```
->
```javascript
const h = e[0], i = g.a, j = g['a'];
```
**Note:** Currently only 1D arrays are supported i.e. `a[1][2]` wont work.

### Operators
#### Prefix Operators -> Binary Operators
As Clojure uses prefix notation you can give any number of arguments to the operators. `clojs` requires them to be greater than or equal to two.

| Input                      | Output                                   |
| -------------------------- | ---------------------------------------- |
| `(+ a b 1 1.2 c)`          | `a + b + 1 + 1.2 + c`                    |
| `(- a b 1 1.2 c)`          | `a - b - 1 - 1.2 - c`                    |
| `(* a b 1 1.2 c)`          | `a * b * 1 * 1.2 * c`                    |
| `(/ a b 1 1.2 c)`          | `a / b / 1 / 1.2 / c`                    |
| `(mod a b 1 1.2 c)`        | `a % b % 1 % 1.2 % c`                    |
| `(< a b 1 1.2 c)`          | `a < b < 1 < 1.2 < c`                    |
| `(<= a b 1 1.2 c)`         | `a <= b <= 1 <= 1.2 <= c`                |
| `(> a b 1 1.2 c)`          | `a > b > 1 > 1.2 > c`                    |
| `(>= a b 1 1.2 c)`         | `a >= b >= 1 >= 1.2 >= c`                |
| `(= a b 1 1.2 c)`          | `a === b === 1 === 1.2 === c`            |
| `(== a b 1 1.2 c)`         | `a == b == 1 == 1.2 == c`                |
| `(!== a b 1 1.2 c)`        | `a !== b !== 1 !== 1.2 !== c`            |
| `(!= a b 1 1.2 c)`         | `a != b != 1 != 1.2 != c`                |
| `(in a b 1 1.2 c)`         | `a in b in 1 in 1.2 in c`                |
| `(instanceof a b 1 1.2 c)` | `a instanceof b instanceof 1 instanceof 1.2 instanceof c` |
| `(and a b 1 1.2 c)`        | `a && b && 1 && 1.2 && c`                |
| `(or a b 1 1.2 c)`         | `a` &#124;&#124; `b` &#124;&#124; `1` &#124;&#124; `1.2` &#124;&#124; `c`                |
| `(assign a b)`             | `a = b`       |

#### Unary Operators

| Input        | Output     |
| ------------ | ---------- |
| `(not a)`    | `!a`       |
| `(typeof a)` | `typeof a` |

### `if` statement
```clojure
(if (= n 0)
  true
  false)
```
```javascript
if (n === 0)
  true;
else
  false;
```
### Function Call
```clojure
(console.log 1 2 "abc" a b)
```
->

```javascript
console.log(1, 2, "abc", a, b);
```
No commas in Clojure.
### Block Statement - `do`

```clojure
  (if (= 1 1)
    (do (console.log "x :" x)
        (console.log "y :" y)
        (console.log "z :" z))
    (do (console.log "a :" a)
        (console.log "b :" b)
        (console.log "c :" c))
```

->

```javascript
if (1 === 1) {
        console.log('x :', x);
        console.log('y :', y);
        console.log('z :', z);;
    } else {
        console.log('a :', a);
        console.log('b :', b);
        console.log('c :', c);
    }
```

### Function Definition - `defn`

```clojure
(defn factorial [n]
  (if (= n 0)
    1
    (* n (factorial (- n 1)))))
```
->

```javascript
const factorial = n => {
    if (n === 0)
        return 1;
    else
        return n * factorial(n - 1);
};
```

Return is implicit. The last statement is the return statement.

### Lambda -> Anonymous functions

```clojure
(fn [x]
  (console.log x)
  (console.log (+ 5 x)))
```

->

```javascript
x => {
    console.log(x);
    return console.log(5 + x);
};
```

### `let`

```clojure
(fn [x]
  (console.log x)
  (let a 2 b 5)
  (console.log (+ 5 x a b)))
```

->

```javascript
x => {
    console.log(x);
    let a = 2, b = 5;
    return console.log(5 + x + a + b);
};
```

### `return` statement

```clojure
(return a)
```
->
```javascript
return a;
```

### `chaining`

```clojure
(.attr (.parent ($ this)) "id")
```
->
```javascript
$(this).parent().attr('id');
```

### `cond` -> `if-else` chain

```clojure
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
```

->

```javascript
x => {
    if (is_array_member(form)) {
        get_array_member(form);
        return 1 + 2;
    } else if (is_defn(form))
        return get_defn(form);
    else if (is_def(form))
        return get_const(form);
    else if (is_if(form))
        return get_if(form);
    else if (is_do(form))
        return get_do(form);
    else if (is_vec(form))
        return get_vec(form);
    else if (is_lambda(form))
        return get_lambda(form);
    else if (is_map_ds(form))
        return get_map_ds(form);
    else if (is_literal(form))
        return get_literal(form);
    else if (is_operator(form))
        return get_operator(form);
    else if (is_fn_call(form))
        return get_fn_call(form);
    else if (true)
        return null;
};
```

### Macros

```clojure
(defmacro m-bind [mv mf]
  (conj (list ~mv test) ~mf))

(m-bind mvv mff)
```

->

```javascript
mff(mvv, test);
```

## Samples

You can see a converted sample containing all the syntax: [`all.clj`](https://github.com/puneetpahuja/cloJS/blob/master/samples/all.clj) -> [`all.js`](https://github.com/puneetpahuja/cloJS/blob/master/samples/all.js) 

## Examples 

[A todo app written using cloJS.](https://github.com/puneetpahuja/todo-clojs)

## Components

1. Ramshreyas's [seqingclojure](https://github.com/Ramshreyas/seqingclojure) - it makes the AST for the input code.
2. `clojs` uses this AST to convert it into an equivalent JavaScript AST in JSON format.
3. estool's npm package [escodegen](https://github.com/estools/escodegen) - it converts the JavaScript AST into JavaScript code.

## License

Released under the Eclipse Public License: <https://github.com/puneetpahuja/cloJS/blob/master/LICENSE>
