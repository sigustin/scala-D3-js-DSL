package d3v4

import scalajs.js
import scalajs.js.{undefined, `|`}
import scala.scalajs.js.annotation._

// https://github.com/d3/d3-axis

@JSImport("d3-format", JSImport.Namespace)
@js.native
object d3format extends js.Object {
  def formatPrefix(specifier: String, value: Double): js.Function1[Double, String] = js.native
}