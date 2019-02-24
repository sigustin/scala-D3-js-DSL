# D3.js DSL in Scala
Scala implementation of a Domain Specific Language to use a Scala wrapper of *D3.js* with more error checking than JavaScript can provide (notably type checking) thanks to the DSL being compiled.

*D3.js* is a very well known JavaScript library to display interactive charts and graphs. However, JavaScript is a very unsafe language: it is weakly and dynamically typed and is interpreted (thus without checking for any error before being executed). Plus, people rarely look at a browser console to check whether the JavaScript engine printed errors or warnings. They are thus often completely ignored (both by users and (bad) developers).

The point of this project is to **create a wrapper around a port of *D3.js* into Scala**, in order to make the Scala compiler **check for errors on compilation** (thus before creating the JavaScript code), notably using type checking.
Moreover, we had to implement this wrapper to **create a Domain Specific Language for chart creation** that was easy to use and hard to misuse thanks to the capabilities of meta-programming of Scala.

This project was made for the course LINGI2132 &ndash; Languages and Translators in 2018.

*Languages used:*
- *Scala (to implement the DSL)*
- *HTML/CSS and a little JavaScript (for the webpage where charts will be displayed)*

## Collaborators
This project had to be done in pairs. I thus made it with [Arnaud Gellens](https://github.com/gellens) (proprietary of the initial repository for this project). We worked together on the same parts of the project.

## What I learned
- The Scala language
- I had a glimpse of how JavaScript libraries can be ported to Scala
- Design an ergonomic DSL

## Files worth checking out
- Our report explaining what we made: [Submission/LINGI2132_Project-DSL_Gellens-Gustin_Report.pdf](https://github.com/sigustin/scala-js-d3-example-app/blob/master/Submission/LINGI2132_Project-DSL_Gellens-Gustin_Report.pdf)
- A slide presentation reviewing the interesting points of our DSL: [Submission/LINGI2132_Project-DSL_Gellens-Gustin_Slides.pdf](https://github.com/sigustin/scala-js-d3-example-app/blob/master/Submission/LINGI2132_Project-DSL_Gellens-Gustin_Slides.pdf)
- The source code defining the DSL: [src/main/scala/lib](https://github.com/sigustin/scala-js-d3-example-app/tree/master/src/main/scala/lib)

## Compilation and execution
You will need [sbt](https://www.scala-sbt.org/) installed to compile and run the project.

Compile and run the project:
```sh
sbt fastOptJS::webpack
```
from the root folder.

Once this is done, you can open the file `index.html` in a browser. It will display the chart that was defined using the DSL in [src/main/scala/example/ScalaJSExample.scala](https://github.com/sigustin/scala-js-d3-example-app/blob/master/src/main/scala/example/ScalaJSExample.scala)
