package lib

import d3v4._
import scala.scalajs.js



trait Graphe {
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
        data = Some(d)
    }

    def setData(d: List[List[Double]]) ={
        val tmpD:js.Array[js.Array[Double]] = js.Array()
        d.foreach(e => {
            val tmpSubD: js.Array[Double] = js.Array()
            e.foreach(tmpSubD.append(_))
            tmpD.append(tmpSubD)
        })
        data = Some(tmpD)
    }

    /*def setData(d: List[List[Int]]) ={
        setData(d.map(_.map(_.toDouble)))
    }*/
//    def intToDouble[T: Int](x: T) = implicitly[Int[T]].toDouble(x)
//    def intToDouble[T](x: T)(implicit n: Numeric[T]) = n.toDouble(x)
//    implicit def intListToDoubleList(d: List[List[Int]]): List[List[Double]] = d.map(_.map(_.toDouble))
}
