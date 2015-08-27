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

Any piece of code can be represented as a tree of objects and operators. Here is some simple cloure code and the tree that represents it:

```clojure
(+ 1 2)
```
![alt text][ast]

[ast]: http://condor.depaul.edu/glancast/347class/docs/images/exp11.gif


