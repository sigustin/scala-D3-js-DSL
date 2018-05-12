package lib.plot

import d3v4._
import lib.ImplicitConv._
import lib.matrix.{LabelizedRelationMatrix, RelationMatrix}
import org.scalajs.dom

import scala.scalajs.js

trait RelationPlot {
    var scale = 0 // power of ten multiplier of the representation of the data
    private var heightLocal:Option[Double] = None
    private var widthLocal:Option[Double] = None

    protected var localTarget = "svg" // is a html selector used as destination for the graphe
    var svg: Selection[dom.EventTarget] = d3.select(localTarget)

    // Both matrices may contain labels or not
    protected var basisMatrix: Option[RelationMatrix] = None // When resetting the display, get back to this data
    protected var displayedMatrix: Option[RelationMatrix] = None

    //================= Setters ===========================
    /** Sets the visible dimension of the plot in the svg image */
    def setDimension(w: Int, h:Int): RelationPlot = {
        heightLocal = Some(h)
        widthLocal = Some(w)
        d3.select(localTarget)
            .attr("width", w)
            .attr("height", h)
        this
    }
    def dimension_=(w: Int, h: Int): Unit = setDimension(w, h)
    def dimension_=(dim: (Int, Int)): Unit = setDimension(dim._1, dim._2)

    /** Sets the target html tag */
    def setTarget(t: String): RelationPlot = {
        localTarget = t
        svg = d3.select(localTarget)
        this
    }
    def target_=(t: String): Unit = {
        localTarget = t
        svg = d3.select(localTarget)
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
                            case None => throw new UnsupportedOperationException ("Can't set labels for an empty matrix")
                        }
                    case _ => throw new IllegalStateException("Matrix is of invalid type")
                }
            case None => throw new UnsupportedOperationException ("Can't set labels for no matrix")
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
                    case _ => throw new UnsupportedOperationException("Can't update a label on a plot without labels")
                }
            }
            case None => throw new UnsupportedOperationException("Can't update labels on a plot without data")
        }
    }
    def updateLabel(oldLabel: String, newLabel: String): ChordPlot = updateLabel(oldLabel -> newLabel)

    //=================== Getters ===========================
    def height: Double = {
        heightLocal match {
            case Some(h) => h
            case None => d3.select(localTarget).attr("height").toDouble
        }
    }
    def width: Double = {
        widthLocal match {
            case Some(w) => w
            case None => d3.select(localTarget).attr("width").toDouble
        }
    }

    def getTarget: String = localTarget

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

    //=================== Utility methods ==========================
    /** Transform all Double so that they are castable to js.Int (by Javascript) */
    private def transformData(d: js.Array[js.Array[Double]]): js.Array[js.Array[Double]] = {
        var maxFigureBehindCommaVal = d.maxNbFigureBehindComma
        scale=maxFigureBehindCommaVal
        (d * (10**maxFigureBehindCommaVal)).round
    }

    /**
      * Merges all elements of section $indexToIndex._1 into section $indexToIndex._2
      * and makes the plot ready to display it
      */
    def merge(indexToIndex: (Int, Int)): RelationPlot = {
        displayedMatrix match {
            case Some(matrix) => displayedMatrix = Some(matrix.merge(indexToIndex._1 -> indexToIndex._2))
            case None => throw new UnsupportedOperationException("Can't merge two sections when there is no data in the plot")
        }
        this
    }
    def merge(index1: Int, index2: Int): RelationPlot = merge(index1 -> index2)
    def merge(labelToLabel: (String, String))(implicit d: DummyImplicit): RelationPlot = {
        displayedMatrix match {
            case Some(matrix) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = Some(labelizedMat.merge(labelToLabel))
                        this
                    case _ => throw new UnsupportedOperationException("Can't use labels to index matrix without labels")
                }
            case None => throw new UnsupportedOperationException("Can't merge sections when no plot has been initialized")
        }
    }
    def merge(label1: String, label2: String): RelationPlot = merge(label1 -> label2)
    def merge(labelToIndex: (String, Int))(implicit d1: DummyImplicit, d2: DummyImplicit): RelationPlot = {
        displayedMatrix match {
            case Some(matrix) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = Some(labelizedMat.merge(labelToIndex._1 -> labelToIndex._2)) // Giving $labelToIndex directly restuls in a compiler error
                        this
                    case _ => throw new UnsupportedOperationException("Can't use labels to index matrix without labels")
                }
            case None => throw new UnsupportedOperationException("Can't merge sections when no plot has been initialized")
        }
    }
    def merge(label: String, index: Int): RelationPlot = merge(label -> index)
    def merge(indexToLabel: (Int, String))(implicit d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit): RelationPlot = {
        displayedMatrix match {
            case Some(matrix) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = Some(labelizedMat.merge(indexToLabel))
                        this
                    case _ => throw new UnsupportedOperationException("Can't use labels to index matrix without labels")
                }
            case None => throw new UnsupportedOperationException("Can't merge sections when no plot has been initialized")
        }
    }
    def merge(index: Int, label: String): RelationPlot = merge(index -> label)
}
