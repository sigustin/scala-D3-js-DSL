package d3v4

import scalajs.js
import scalajs.js.{undefined, `|`}
import scala.scalajs.js.annotation._

// https://github.com/d3/d3-axis

@JSImport("d3-chord", JSImport.Namespace)
@js.native
object d3chord extends js.Object {
  def chord(): ChordLayout = js.native
  def ribbon(): RibbonGenerator = js.native
}

@js.native
trait RibbonGenerator extends js.Object{
  def radius(r: Double): RibbonGenerator = js.native
  def apply(args: Any*): Ribbon = js.native
}

@js.native
trait Ribbon extends js.Object{

}

@js.native
trait ChordLayout extends js.Object{
  def padAngle(angle: Double): ChordLayout = js.native
  def sortSubgroups(comp: Comparator[Int]): ChordLayout = js.native

  def apply(m: js.Array[js.Array[Double]]): ChordArray = js.native
}

@js.native
trait ChordArray extends js.Array[Chord] {
  def groups: js.Array[ChordGroup] = js.native
}

@js.native
trait ChordGroup extends js.Object{
  def startAngle: Double = js.native
  def endAngle: Double = js.native
  def value: Double = js.native
  def index: Int = js.native
}

@js.native
trait Chord extends js.Object{
  def source: ChordDest = js.native
  def target: ChordDest = js.native
}

@js.native
trait ChordDest extends js.Object{
  def startAngle: Double = js.native
  def endAngle: Double = js.native
  def value: Double = js.native
  def index: Int = js.native
  def subindex: Int = js.native
}