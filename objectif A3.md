## Objectif
To create a library that plot chords plots (https://bl.ocks.org/mbostock/1046712) and migration plots (http://usmigrationflowmapper.com) in Scala using as base the D3.js library.

//https://www.visualcinnamon.com/2014/12/using-data-storytelling-with-chord

- Create a new plot in a very small number of lines, mostly by giving the underlying matrix and a list of names for each row/column of the matrix. For migration plots, we want to be able to select the part of the world to be displayed. And to dynamically modify it if needed (zoom, ...)

- Be able to merge (sum) two entries, to create a new "group", such that the plot is dynamically updated. Other types of modification of the matrices is welcome!

- Allow to animate most things.

- Change most aspects of the plot, using D3, if needed. The more everything is parametrizable in a statically-typed way, the better.

## Definitions

**Strong typing** :
**Static typing** : for example static method of an object are static(but in pracice ? something else ?)
**Type inference** : by default in Scala (no ?)
**By-name parameters** : allow either to pass value laziyly or to pass function only evaluated when needed
**Implicits** : value set by default
**Currying** : allow for example to have function of this style: fct()()
**Monads** :
**Closures** : allow to have a long suite of call one after an other

## Problem and this to try:
- the D3.js library in js for the plot we are asked
- Scala.js to complie some simple code
