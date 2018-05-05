## Get started

To get started, open `sbt` in this example project, and execute the task
`fastOptJS::webpack`. This creates the file `target/scala-2.12/scalajs-bundler/example-fastopt-bundle.js`.
You can now open `index.html` in your favorite Web browser!

During development, it is useful to use `~fastOptJS::webpack` in sbt, so that each
time you save a source file, a compilation of the project is triggered.
Hence only a refresh of your Web page is needed to see the effects of your
changes.

## Adding new function from D3.js

You will probably need to access functions from D3.js that are not yet in the scala.js "facade".
First, check in which d3.js package is your function. Let's say its in `d3-color`, and that you want
to add the function `rgb(r, g, b)`. 

Read first the D3.js documentation to see what are the arg types and the return value of the function.
In this example, it is of type `rgb(r: Double, g: Double, b: Double): Rgb` where `Rgb` is a javascript object,
having its own functions.

Now, simply go to the file `d3v4/Color.scala` , and add the new function, telling scala it already exists in JS:

```scala
@JSImport("d3-color", JSImport.Namespace) //tells to import d3-color, and that this object is a facade of this package
@js.native //tell scala it's a native js object
object d3color extends js.Object {
  def rgb(r:Double, g:Double, b:Double):Rgb = js.native //tell scala that this function exists
}
```

of course, you will also have to define the type `Rgb`. The doc tells us it has 3 values:

```scala
@js.native
trait Rgb extends {
  def r:Double = js.native
  def g:Double = js.native
  def b:Double = js.native
}
```

There are of course many more functions in the JS versions of the objects `d3color` and `Rgb`. But as you
do not need them in your scala code, you can simply ignore them. For now, at least.

## Need an external JS library?

You are free to add whatever you want as external library. Find your JS library package on NPM, then add it to `build.sbt`: 

```scala
npmDependencies in Compile ++= (
  "d3" -> "4.12.2" ::
    Nil
  )
```