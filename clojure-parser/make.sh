#!/usr/bin/env bash

cd /home/puneet/code/geekskool/seqingclojure/clojure-parser
lein bin
cp target/base+system+user+dev/clojure-parser-0.1.0-SNAPSHOT clojs
cp clojs run/clojs
cp macros run/macros
cp clojs ~/test/clojs
cp macros ~/test/macros