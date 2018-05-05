package example

import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3

import scala.scalajs.js
import lib.{Chords, D3Lib}

object ScalaJSExample extends js.JSApp {
    def main(): Unit = {
        /*val data = js.Array(8, 22, 31, 36, 48, 17, 25)
        val g = D3Lib
        g.setDimention(480, 480)
        g.setData(data)
        g.draw()*/
        val g2 = new Chords()
        g2.draw()
    }
}
