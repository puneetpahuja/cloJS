# seqing clojure
Learning Clojure by writing a Clojure parser. In Clojure.

### Introduction ###

So you want to learn Clojure? Parse it. In Clojure. At least that's how we do things in [GeekSkool][]. 

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

This implies \+ could be mapped to a vector containing 5 and 2 : 

```
{+ [5 2]}
```

\* can then be mapped to 3 and the afore-mentioned map: 

```
{* [3 {+ [5 2]}]}
```

So 

![alt text][ast]

[ast]: http://www.codeproject.com/KB/recipes/sota_expression_evaluator/simplified_ast.png

is

```
{* [3 {+ [5 2] } ] }
```

OK. So we are going to write a Clojure program that takes such simple Clojure code as an input, and returns a map like the one above as the output. 
