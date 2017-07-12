(ns clojs.monads
  (:require [clojure.algo.monads :refer :all]))

;;; Parser monad
(def parser-m (state-t maybe-m))

;;; Parser combinators
(with-monad parser-m
  (defn optional
    "Take a parser and return an optional version of it."
    [parser]
    (m-plus parser (m-result nil)))

  (defn one-or-more
    "Matches the same parser repeatedly until it fails - the first time has
  to succeed for the parser to progress"
    [parser]
    (domonad [value parser
              values (optional (one-or-more parser))]
             (if values
               (into [value] (flatten values))
               [value])))

  (defn none-or-more
    "Matches the same parser repeatedly until it fails - first can fail and
  second will continue"
    [parser]
    (optional (one-or-more parser)))

  (defn nested-one-or-more
    "Matches the same parser repeatedly until it fails - the first time has
  to succeed for the parser to progress"
    [parser]
    (domonad [value parser
              values (optional (nested-one-or-more parser))]
             (if values
               (into [value] values)
               [value])))
  (defn nested-none-or-more
    [parser]
    (optional (nested-one-or-more parser)))

  (defn skip-one [parser]
    (domonad
      [_ parser]
      :skip))

  (defn skip-one-or-more
    "Matches the parser on or more times until it fails, but doesn't return
     the values for binding"
    [parser]
    (domonad
      [_ parser
       _ (optional (skip-one-or-more parser))]
      :skip))

  (defn skip-none-or-more
    "Matches the same parser zero or more times until it fails,
     then returns true."
    [parser]
    (optional (skip-one-or-more parser)))

  (defn match-one
    "Match at least one of the parsers in the given order, or fail"
    [& parsers]
    (reduce m-plus parsers))

  (defn match-all
    "Match all the given parsers, or fail"
    [& parsers]
    (m-bind (m-seq parsers)
            (comp m-result flatten))))
