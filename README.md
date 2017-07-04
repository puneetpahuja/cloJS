# `cloJS` [![Clojars Project](https://img.shields.io/clojars/v/clojs.svg)](https://clojars.org/clojs)
A library for Clojure to convert Clojure code to JavaScript.
Combining the simplicity of Clojure syntax with the power of JavaScript libraries.

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

Please open issues against the [clojs repo on Github](https://github.com/puneetpahuja/cloJS/issues).

## Mailing List

Please ask questions on the [clojs mailing list](https://groups.google.com/forum/#!forum/clojs).



## Who is it for?
This is for:
  - JavaScript programmers who want to use simple lisp-like functional syntax of clojure. You can call all the javascript functions.
  - Clojure programmers who want to use JavaScript functions and libraries.

## Introduction
You write Clojure syntax code using JavaScript functions and it is converted into a JS code.

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




