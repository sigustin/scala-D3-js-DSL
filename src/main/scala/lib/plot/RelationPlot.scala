package lib.plot

import d3v4._
import lib.ImplicitConv._
import lib.matrix.{LabelizedRelationMatrix, RelationMatrix}
import org.scalajs.dom

import scala.collection.mutable.Stack
import scala.scalajs.js

/**
  * Enumerates the different behaviors for focusing sections (and then merge them when several are focused)
  * When $click is the focus event of a plot, sections should be focused on click (and pushed in $focusedSections)
  * When 2 sections are focused, they should be merged
  * Resp. when $hover is the focus event
  * When $none is the focus event, nothing should happen
  */
object FocusEvent extends Enumeration {
    val none, click, hover = Value
}

trait RelationPlot {
    var scale = 0 // power of ten multiplier of the representation of the data
    private var heightLocal:Option[Double] = None
    private var widthLocal:Option[Double] = None

    protected var localTarget = "svg" // is a html selector used as destination for the graphe
    var svg: Selection[dom.EventTarget] = d3.select(localTarget)

    // Both matrices may contain labels or not
    protected var basisMatrix: Option[RelationMatrix] = None // When resetting the display, get back to this data
    protected val historyMatrices: Stack[RelationMatrix] = Stack[RelationMatrix]()
    protected var _displayedMatrix: Option[RelationMatrix] = None

    protected var sumData: Option[Double] = None

    var focusEvent = FocusEvent.click
    var focusedSection: Option[Int] = None // Stores the index of the currently focused section (not yet merged)

    //================= Setters and getters ===========================
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
    def dimension: (Double, Double) = (widthLocal.get, heightLocal.get)

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
    def target: String = localTarget

    /** Sets the basis matrix of the plot and let it display itself */
    protected def setMatrix(matrix: RelationMatrix): RelationPlot = {
        basisMatrix = Some(matrix)
        saveDisplayedMatrix()
        _displayedMatrix = Some(matrix)
        computeSumData()
        this
    }
    /** Sets the displayed matrix of the plot */
    def displayedMatrix_=(matrix: RelationMatrix): Unit = {
        println("SETTING DISPLAYED MATRIX")
        saveDisplayedMatrix()
        _displayedMatrix = Some(matrix)
    }
    def displayedMatrix: RelationMatrix = _displayedMatrix.getOrElse(RelationMatrix(List(List())))

    def setLabels(l: List[String]): ChordPlot = {
        _displayedMatrix match {
            case Some(matrix) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        labelizedMat.setLabels (l)
                        this
                    case _: RelationMatrix =>
                        data match {
                            case Some(d) =>
                                val labelizedMatrix = LabelizedRelationMatrix(l, d)
                                displayedMatrix = labelizedMatrix
                                this
                            case None => throw new UnsupportedOperationException ("Can't set labels for an empty matrix")
                        }
                    case _ => throw new IllegalStateException("Matrix is of invalid type")
                }
            case None => throw new UnsupportedOperationException ("Can't set labels for no matrix")
        }
    }
    def labels_=(l: List[String]): Unit = setLabels(l)
    def labels: List[String] = {
        _displayedMatrix match {
            case Some(matrix) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix => labelizedMat.getLabels
                    case _ => List()
                }
            case None => List()
        }
    }

    def updateLabel(labelToLabel: (String, String)): ChordPlot = {
        _displayedMatrix match {
            case Some(matrix) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = labelizedMat.updateLabel(labelToLabel)
                        this
                    case _ => throw new UnsupportedOperationException("Can't update a label on a plot without labels")
                }
            case None => throw new UnsupportedOperationException("Can't update labels on a plot without data")
        }
    }
    def updateLabel(oldLabel: String, newLabel: String): ChordPlot = updateLabel(oldLabel -> newLabel)

    /**
      * Returns the sum of the data around the whole chord plot
      * @post the result is >= 0.0
      */
    protected def computeSumData(): Unit = {
        data match {
            case None => sumData = None
            case Some(d) =>
                var sum = 0.0
                d.foreach(sum += _.sum.abs)
                sumData = Some(sum)
        }
    }

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
        _displayedMatrix match {
            case Some(matrix) => Some(matrix.getData)
            case None => None
        }
    }

    def getLabels: Option[List[String]] = {
        _displayedMatrix match {
            case Some(matrix) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix => Some(labelizedMat.getLabels)
                    case _ => None
                }
            case _ => None
        }
    }

    //================= History =======================
    /** Saves the current $_displayedMatrix into the history (if it is not the last one in) */
    def saveDisplayedMatrix(): Unit = {
        if (_displayedMatrix.isDefined
            && (historyMatrices.isEmpty || historyMatrices.top != _displayedMatrix.get))
            historyMatrices.push(_displayedMatrix.get)
    }
    /** Resets the displayed matrix to the basis one (if there is one) */
    def revertToInitial(): Unit = {
        println(s"revert init: $historyMatrices")
        if (historyMatrices.nonEmpty) {
            while (historyMatrices.length > 1)
                historyMatrices.pop()
            _displayedMatrix = Some(historyMatrices.pop())
        }
        else
            println("[WARNING] Can't revert to initial state without history")
    }
    /** Goes back one state in the history */
    def revert(): Unit = {
        println(s"revert: $historyMatrices")
        if (historyMatrices.nonEmpty)
            _displayedMatrix = Some(historyMatrices.pop())
        else
            println("[WARNING] Can't revert to previous state without history")
    }

    //=================== Utility methods ==========================
    /** Transform all Double so that they are castable to js.Int (by Javascript) */
    private def transformData(d: js.Array[js.Array[Double]]): js.Array[js.Array[Double]] = {
        val maxFigureBehindCommaVal = d.maxNbFigureBehindComma
        scale=maxFigureBehindCommaVal
        (d * (10**maxFigureBehindCommaVal)).round
    }

    /**
      * Merges all elements of section $indexToIndex._1 into section $indexToIndex._2
      * or $indexToIndex._1._1 and $indexToIndex._1._2 and name it as $indexToIndex._2
      * and makes the plot ready to display those changes
      */
    def merge(indexToIndex: (Any, Any)): RelationPlot = {
        println("merge")
        val matrix = _displayedMatrix.getOrElse(
            throw new UnsupportedOperationException("Can't merge two sections when there is no data in the plot"))
        indexToIndex match {
            case (_: Int, _: Int) =>
                matrix match {
                    case labelizedMatrix: LabelizedRelationMatrix =>
                        displayedMatrix = labelizedMatrix.mergeAndKeepLabels(indexToIndex.asInstanceOf[(Int, Int)])
                    case _ => displayedMatrix = matrix.merge(indexToIndex.asInstanceOf[(Int, Int)])
                }
            case (_: String, _: String) | (_: Int, _: String) | (_: String, _: Int) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = labelizedMat.merge(indexToIndex)
                    case _ => throw new UnsupportedOperationException("Can't use labels to index matrix without labels")
                }
            case (indices: (Any, Any), label: String) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = labelizedMat.merge(indices).updateLabel(indices._2 -> label)
                    case _ => throw new UnsupportedOperationException("Can't add labels to matrix without labels (set the labels for all the matrix before merging)")
                }
            case _ => throw new UnsupportedOperationException("Can index matrices only using Int or String")
        }
        this
    }
    def merge(index1: Any, index2: Any): RelationPlot = merge(index1 -> index2)

    //===================== Listeners =========================
    /** Calls the function $f when the plot is clicked on */
    def onClick(f: => Unit): RelationPlot = {svg.on("click", () => f); this}
    // TODO does not work with 1 or 2 arguments (as in JS) because "same type after erasure"
    def onClick(f: => js.Any => Unit): Unit = svg.on("click", f)
//    def onClick(f: => Selection[dom.EventTarget]#ListenerFunction2): Unit = svg.on("click", f)
    /** Calls the function $f when the plot is double-clicked */
    def onDoubleClick(f: => Unit): RelationPlot = {svg.on("dblclick", () => f); this}
    /** Calls the function $f when the plot is clicked on (mouse down) */
    def onMouseDown(f: => Unit): RelationPlot = onClickDown(f)
    def onClickDown(f: => Unit): RelationPlot = {svg.on("mousedown", () => f); this}
    /** Calls the function $f when the plot stops being clicked on (mouse up) */
    def onMouseUp(f: => Unit): RelationPlot = onClickUp(f)
    def onClickUp(f: => Unit): RelationPlot = {svg.on("mouseup", () => f); this}
    /** Calls the function $f when the mouse goes over the plot */
    def onMouseOver(f: => Unit): RelationPlot = {svg.on("mouseover", () => f); this}
    /** Calls the function $f when the plot is hovered by the mouse (mouse enter) */
    def onMouseEnter(f: => Unit): RelationPlot = onHover(f)
    def onHover(f: => Unit): RelationPlot = {svg.on("mouseenter", () => f); this}
    /** Calls the function $f when the plot stops being hovered by the mouse (mouse leave) */
    def onMouseLeave(f: => Unit): RelationPlot = onHoverOff(f)
    def onHoverOff(f: => Unit): RelationPlot = {svg.on("mouseleave", () => f); this}
    /** Calls the function $f when the plot is focused */
    def whenFocused(f: => Unit): RelationPlot = {svg.on("focus", () => f); this} // Not useful as the svg in the html file is not focusable
    /** Calls the function $f when a key is pressed */
    def onKeyPressed(f: => Unit): RelationPlot = {svg.on("keydown", () => f); this} // TODO doesn't seem to work
    /** Calls the function $f when scrolled */
    def onScroll(f: => Unit): RelationPlot = {svg.on("SCGScroll", () => f); this} // TODO doesn't seem to work
    /** Calls the function $f when the plot is resized (never happens) */
    def onResize(f: => Unit): RelationPlot = {svg.on("resize", () => f); this}
    /** Calls the function $f when the page changes visibility */
    def onPageVisibilityChange(f: => Unit): RelationPlot = {svg.on("visibilitychange", () => f); this} // TODO doesn't seem to work

    //------------------ Focusing behavior -------------------------
    /** Set the focusing behavior */
    def focusSectionsOnClick: Unit = focusEvent = FocusEvent.click
    def focusSectionsOnHover: Unit = focusEvent = FocusEvent.hover
    def dontFocusSections: Unit = focusEvent = FocusEvent.none

    //=================== =======================
    /** Merges sections when two of them are selected */
    val focusAndMergeSections: js.Any => Unit =
        (d: js.Any) => {
            if (d3.event != null)
                d3.event.stopPropagation()
            val i = d.asInstanceOf[ChordGroupJson].index // TODO ChordGroupJson only works for Chord at the moment
            println(s"focus and merge section called: $i and focused is $focusedSection")
            if (focusedSection.isDefined) {
                if (focusedSection.get != i) {
                    merge(i -> focusedSection.get)
                    draw()
                    focusedSection = None
                }
                else if (focusEvent == FocusEvent.click)
                    focusedSection = None
            }
            else
                focusedSection = Some(i)
        }

    //=============== Abstract methods =====================
    def draw(): Unit
}
