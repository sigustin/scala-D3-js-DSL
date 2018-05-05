package d3v4

import scalajs.js
import scalajs.js.{undefined, `|`}
import scala.scalajs.js.annotation._

@JSImport("d3-array", JSImport.Namespace)
@js.native
object d3array extends js.Object {
  def ascending[V](a: V, b: V): Int = js.native
  def descending[V](a: V, b: V): Int = js.native
  def range(stop: Int): js.Array[Int] = js.native
  def range(start: Int, stop: Int): js.Array[Int] = js.native
  def range(start: Int, stop: Int, step: Int): js.Array[Int] = js.native

  def range(stop: Double): js.Array[Double] = js.native
  def range(start: Double, stop: Double): js.Array[Double] = js.native
  def range(start: Double, stop: Double, step: Double): js.Array[Double] = js.native
}