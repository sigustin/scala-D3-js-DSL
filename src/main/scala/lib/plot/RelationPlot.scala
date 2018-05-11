package lib.plot

import d3v4._
import lib.ImplicitConv._
import lib.matrix.RelationMatrix
import org.scalajs.dom

import scala.scalajs.js

trait RelationPlot {
    var scale = 0 // power of ten multiplier of the representation of the data hold in data
    private var heightLocal:Option[Double] = None
    private var widthLocal:Option[Double] = None

    var target = "svg" // is a html selector used as destination for the graphe
    var svg: Selection[dom.EventTarget] = d3.select(target)

    protected var basisMatrix: Option[RelationMatrix] = None
    protected var displayedMatrix: Option[RelationMatrix] = None

    //================= Setters ===========================
    /** Sets the visible dimension of the plot in the svg image */
    def setDimension(w: Int, h:Int): RelationPlot = {
        heightLocal = Some(h)
        widthLocal = Some(w)
        d3.select(target)
            .attr("width", w)
            .attr("height", h)
        this
    }

    /** Sets the target html tag */
    def setTarget(t: String): RelationPlot = {
        target = t
        svg = d3.select(target)
        this
    }
    def target_(t: String): Unit = {
        target = t
        svg = d3.select(target)
    }

    /** Sets the basis matrix of the plot and let it display itself */
    protected def setMatrix(matrix: RelationMatrix): RelationPlot = {
        basisMatrix = Some(matrix)
        displayedMatrix = Some(matrix)
        this
    }
    /** Sets the displayed matrix of the plot */
    def displayedMatrix_=(matrix: RelationMatrix): Unit = {
        displayedMatrix = Some(matrix)
    }
    /** Resets the displayed matrix to the basis one (if there is one) */
    def resetDisplay(): Unit = {
        basisMatrix match {
            case Some(matrix) => displayedMatrix = Some(matrix)
            case None => displayedMatrix = None
        }
    }

    //=================== Getters ===========================
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

    def data: Option[List[List[Double]]] = {
        displayedMatrix match {
            case Some(matrix) => Some(matrix.getData)
            case None => None
        }
    }

    //=================== Utility method ==========================
    /** Transform all Double so that they are castable to js.Int (by Javascript) */
    private def transformData(d: js.Array[js.Array[Double]]): js.Array[js.Array[Double]] = {
        var maxFigureBehindCommaVal = d.maxNbFigureBehindComma
        scale=maxFigureBehindCommaVal
        (d * (10**maxFigureBehindCommaVal)).round
    }
}
