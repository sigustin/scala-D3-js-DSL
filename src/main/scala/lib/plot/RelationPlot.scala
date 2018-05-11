package lib.plot

import d3v4._
import lib.ImplicitConv._
import org.scalajs.dom

import scala.scalajs.js

trait RelationPlot {
    var scale = 0               // power of ten multiplier of the representation of the data hold in data
    private var heightLocal:Option[Double] = None
    private var widthLocal:Option[Double] = None



    var target = "svg" // is a html selector used as destination for the graphe
    var svg: Selection[dom.EventTarget] = d3.select(target)

    var data: Option[js.Array[js.Array[Double]]] = None

    def setDimension(w: Int, h:Int): RelationPlot = {
        heightLocal = Some(h)
        widthLocal = Some(w)
        d3.select(target)
            .attr("width", w)
            .attr("height", h)
        this
    }

    def setTarget(t: String): RelationPlot = {
        target = t
        svg = d3.select(target)
        this
    }
    def target_(t: String): Unit = {
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

    def setData(d: js.Array[js.Array[Double]]): RelationPlot = {
        data = Some(transformData(d))
        this
    }
    def setData(d: List[List[Double]]): RelationPlot = {
        val tmpD:js.Array[js.Array[Double]] = js.Array()
        d.foreach(tmpD.append(_))
        data = Some(transformData(tmpD))
        this
    }


    private def transformData(d: js.Array[js.Array[Double]]): js.Array[js.Array[Double]] = {
        var maxFigureBehindCommaVal = d.maxNbFigureBehindComma
        scale=maxFigureBehindCommaVal
        (d * (10**maxFigureBehindCommaVal)).round
    }
}
