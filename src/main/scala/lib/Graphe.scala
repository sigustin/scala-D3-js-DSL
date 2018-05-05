package lib

import d3v4._

trait Graphe {
    var heightLocal:Option[Double] = None;
    var widthLocal:Option[Double] = None;

    var target = "svg" // is a html selector used as destination for the graphe
    var svg = d3.select(target)


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
}
