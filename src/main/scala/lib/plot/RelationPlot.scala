package lib.plot

import d3v4._
import lib.ImplicitConv._
import lib.matrix.{LabelizedRelationMatrix, RelationMatrix}
import org.scalajs.dom

import scala.scalajs.js

/**
  * Enumerates the different behaviors for focusing sections (and then merge them when several are focused)
  * When $click is the focus event of a plot, sections should be focused on click (and pushed in $focusedSections)
  * When 2 sections are focused, they should be merged
  * Resp. when $hover is the focus event
  * When $drag is the focus event, a section should be focused when it is dragged from or to
  * When $none is the focus event, nothing should happen
  */
object FocusEvent extends Enumeration {
    val none, click, hover, drag = Value
}

trait RelationPlot {
    var scale = 0 // power of ten multiplier of the representation of the data
    private var heightLocal:Option[Double] = None
    private var widthLocal:Option[Double] = None

    protected var localTarget = "svg" // is a html selector used as destination for the graphe
    var svg: Selection[dom.EventTarget] = d3.select(localTarget)

    // Both matrices may contain labels or not
    protected var basisMatrix: Option[RelationMatrix] = None // When resetting the display, get back to this data
    protected var displayedMatrix: Option[RelationMatrix] = None

    var focusEvent = FocusEvent.click
    var focusedSection: Option[Int] = None // Stores the index of the currently focused section (not yet merged)

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
    def revertDisplay(): Unit = {
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
                        m.setLabels (l)
                        this
                    case _: RelationMatrix =>
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
            case Some(matrix) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = Some(labelizedMat.updateLabel(labelToLabel))
                        this
                    case _ => throw new UnsupportedOperationException("Can't update a label on a plot without labels")
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
        val matrix = displayedMatrix.getOrElse(
            throw new UnsupportedOperationException("Can't merge two sections when there is no data in the plot"))
        indexToIndex match {
            case (_: Int, _: Int) =>
                matrix match {
                    case labelizedMatrix: LabelizedRelationMatrix =>
                        displayedMatrix = Some(labelizedMatrix.mergeAndKeepLabels(indexToIndex.asInstanceOf[(Int, Int)]))
                    case _ => displayedMatrix = Some(matrix.merge(indexToIndex.asInstanceOf[(Int, Int)]))
                }
            case (_: String, _: String) | (_: Int, _: String) | (_: String, _: Int) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = Some(labelizedMat.merge(indexToIndex))
                    case _ => throw new UnsupportedOperationException("Can't use labels to index matrix without labels")
                }
            case (indices: (Any, Any), label: String) =>
                matrix match {
                    case labelizedMat: LabelizedRelationMatrix =>
                        displayedMatrix = Some(labelizedMat.merge(indices).updateLabel(indices._2 -> label))
                    case _ => throw new UnsupportedOperationException("Can't add labels to matrix without labels (set the labels for all the matrix before merging)")
                }
            case _ => throw new UnsupportedOperationException("Can index matrices only using Int or String")
        }
        this
    }
    def merge(index1: Any, index2: Any): RelationPlot = merge(index1 -> index2)

    /**
      * Returns the sum of the data around the whole chord plot
      * @post the result is >= 0.0
      */
    protected def computeSumData(): Double = {

        data match {
            case None => 0.0
            case Some(d) =>
                var sum = 0.0
                d.foreach(sum += _.sum.abs)
                print(sum)
                sum
        }
    }

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

    // TODO listeners lists for events on groups?

    //------------------ Focusing behavior -------------------------
    /** Set the focusing behavior */
    def focusSectionsOnClick: Unit = focusEvent = FocusEvent.click
    def focusSectionsOnHover: Unit = focusEvent = FocusEvent.hover
    def focusSectionsOnDrag: Unit = focusEvent = FocusEvent.drag

    //=================== =======================
    /** Merges sections when two of them are selected */
    val focusAndMergeSections: js.Any => Unit =
        (d: js.Any) => {
            if (d3.event != null)
                d3.event.stopPropagation()
            val i = d.asInstanceOf[ChordGroupJson].index // TODO ChordGroupJson only works for Chord at the moment
            println(s"focus and merge section called: $i and focused is $focusedSection")
            if (focusedSection.isDefined) {
                if (focusedSection.get != i)
                    merge(i -> focusedSection.get)
                focusedSection = None
                draw()
            }
            else
                focusedSection = Some(i)
        }

    /** Reverts the display and redraws the plot */
    val revertAndRedraw: js.Any => Unit =
        (d:js.Any) => {
            revertDisplay()
            draw()
        }

    //=============== Abstract methods =====================
    def draw(): Unit
}
