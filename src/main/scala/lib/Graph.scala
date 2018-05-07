package lib

import d3v4._

import scala.scalajs.js
import js.Dynamic.{global => gJS}
import lib.ImplicitConv._
import org.scalajs.dom

trait Graph {
    var scale = 0               // power of ten multiplier of the representation of the data hold in data
    var heightLocal:Option[Double] = None
    var widthLocal:Option[Double] = None



    var target = "svg" // is a html selector used as destination for the graphe
    var svg: Selection[dom.EventTarget] = d3.select(target)

    var data: Option[js.Array[js.Array[Double]]] = None



    def setDimension(h: Int, w:Int): Graph = {
        heightLocal = Some(h)
        widthLocal = Some(w)
        this
    }

    def setTarget(t: String): Graph = {
        target = t
        svg = d3.select(target)
        this
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

    def setData(d: js.Array[js.Array[Double]]): Graph = {
        data = Some(transformData(d))
        this
    }

    def setData(d: List[List[Double]]): Graph = {
        val tmpD:js.Array[js.Array[Double]] = js.Array()
        d.foreach(tmpD.append(_))
        data = Some(transformData(tmpD))
        this
    }


    private def transformData(d: js.Array[js.Array[Double]]): js.Array[js.Array[Double]] = {
        var maxFigureBehideCommaVal = d.maxNbFigureBehindComma
        scale=maxFigureBehideCommaVal
        (d * (10**maxFigureBehideCommaVal)).round
    }
}
