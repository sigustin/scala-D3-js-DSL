package lib

import d3v4._
import scala.scalajs.js
import js.Dynamic.{ global => gJS }
import lib.ImplicitConv._

trait Graphe {
    var scale = 0               // power of ten multiplier of the representation of the data hold in data
    var heightLocal:Option[Double] = None;
    var widthLocal:Option[Double] = None;



    var target = "svg" // is a html selector used as destination for the graphe
    var svg = d3.select(target)

    var data: Option[js.Array[js.Array[Double]]] = None



    def setDimention(h: Int, w:Int): Unit ={
        heightLocal = Some(h)
        widthLocal = Some(w)
    }

    def setTarget(t: String): Unit ={
        target = t
        svg = d3.select(target)
    }

    def height: Double = {
        heightLocal match {
            case Some(h) => h
            case None => d3.select(target).attr("height").toDouble
        }
    }

    def width: Double = {
        widthLocal match {
            case Some(w) => w
            case None => d3.select(target).attr("width").toDouble
        }
    }

    def setData(d: js.Array[js.Array[Double]]) ={
        data = Some(trasformeData(d))
    }

    def setData(d: List[List[Double]]) ={
        val tmpD:js.Array[js.Array[Double]] = js.Array()
        d.foreach(e => {
            val tmpSubD: js.Array[Double] = js.Array()
            e.foreach(tmpSubD.append(_))
            tmpD.append(tmpSubD)
        })
        data = Some(trasformeData(tmpD))
    }



    private def trasformeData(d: js.Array[js.Array[Double]]): js.Array[js.Array[Double]]={
        var maxFigureBehideCommaVal = d.maxNbFigureBehindComma
        scale=maxFigureBehideCommaVal
        (d * (10**maxFigureBehideCommaVal)).round
    }
}
