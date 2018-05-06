## Objectif
To create a library that plot chords plots (https://bl.ocks.org/mbostock/1046712) and migration plots (http://usmigrationflowmapper.com) in Scala using as base the D3.js library.

Tutorial to explain chords plots: (https://www.visualcinnamon.com/2014/12/using-data-storytelling-with-chord)

- Create a new plot in a very small number of lines, mostly by giving the underlying matrix and a list of names for each row/column of the matrix. For migration plots, we want to be able to select the part of the world to be displayed. And to dynamically modify it if needed (zoom, ...)

- Be able to merge (sum) two entries, to create a new "group", such that the plot is dynamically updated. Other types of modification of the matrices is welcome!

- Allow to animate most things.

- Change most aspects of the plot, using D3, if needed. The more everything is parametrizable in a statically-typed way, the better.

## Definitions

**Strong typing** : proprety that ensure that call of a function is made with parameter of the right type
**Static typing** : the type of a variable is known at compile time
**Type inference** : by default in Scala (-> more readable code)
**By-name parameters** : allow either to pass value laziyly or to pass function only evaluated when needed, and create custome structure
**Implicits** : value set by default or add function to an existing type
**Currying** : allow for example to have function of this style: fct()()
**Monads** : ? (in what this property could be usefull ?)
**Closures** : function whose return value depends on the value of one or more variables declared outside this function.

## Preprety used
- Monads used to have a default value for the size of the graphe.
- add multiplication by a constant to the type `js.Array[js.Array[Double]]`
- add implicit convertion between `List[T]` and `js.Array[T]`

##TODO
- GRAPHE:
    - DONE: set dimension
    - DONE: set data (as 2D matrix)
    - DONE: set a target to attache the graphe

- CHORD
    - DONE: set color
    - TODO: add percentage scale mode
    - TODO: adaptative scall for the numeric mode
    - TODO: set text label
    - TODO: add interaction when the mouse is over something

- MIGRATION
    - TODO: find a example for the migration graphe
    - TODO: translate it into a scala file
    - TODO: handle zoom
    - TODO: add interaction when the mouse is over something
