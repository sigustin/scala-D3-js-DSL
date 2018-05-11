package lib.plot

import d3v4._
import lib.ImplicitConv._
import lib.matrix.{LabelizedRelationMatrix, RelationMatrix}
import org.scalajs.dom

import scala.scalajs.js

trait RelationPlot {
    var scale = 0 // power of ten multiplier of the representation of the data hold in data
    private var heightLocal:Option[Double] = None
    private var widthLocal:Option[Double] = None

    var target = "svg" // is a html selector used as destination for the graphe
    var svg: Selection[dom.EventTarget] = d3.select(target)

    // Both matrices may contain labels or not
    protected var basisMatrix: Option[RelationMatrix] = None // When resetting the display, get back to this data
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

    def setLabels(l: List[String]): ChordPlot = {
        displayedMatrix match {
            case Some(matrix) =>
                matrix match {
                    case m: LabelizedRelationMatrix =>
                        matrix.asInstanceOf[LabelizedRelationMatrix].setLabels (l)
                        this
                    case m: RelationMatrix =>
                        data match {
                            case Some(d) =>
                                val labelizedMatrix = LabelizedRelationMatrix(l, d)
                                displayedMatrix = Some(labelizedMatrix)
                                this
                            case None => throw new IllegalStateException ("Tried to set labels for an empty matrix")
                        }
                    case _ => throw new IllegalStateException("Matrix is of invalid type")
                }
            case None => throw new IllegalStateException ("Tried to set labels for no matrix")
        }
    }
    def labels_=(l: List[String]): Unit = setLabels(l)

    def updateLabel(labelToLabel: (String, String)): ChordPlot = {
        displayedMatrix match {
            case Some(matrix) => {
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = Some(labelizedMat.updateLabel(labelToLabel))
                        this
                    case _ => throw new IllegalArgumentException("Can't update a label on a plot without labels")
                }
            }
            case None => throw new IllegalArgumentException("Can't update labels on a plot without data")
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

    def getLabels: Option[List[String]] = {
        displayedMatrix match {
            case Some(matrix) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix => Some(labelizedMat.getLabels)
                    case _ => None
                }
            case _ => None
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
