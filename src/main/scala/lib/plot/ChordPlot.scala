package lib.plot

import d3v4.{ChordGroup, _}
import lib.ImplicitConv._
import lib.matrix.{LabelizedRelationMatrix, RelationMatrix}
import org.scalajs.dom.XMLHttpRequest

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => gJS}
import scala.scalajs.js.JSON

@js.native
trait ChordGroupJson extends js.Object {
    val index:Int= js.native
    val startAngle: Double = js.native
    val endAngle: Double= js.native
    val value:Int = js.native
}

@js.native
trait ChordJson extends js.Object {
    val source: MySubChordJson = js.native
    val target: MySubChordJson = js.native
}

@js.native
trait MySubChordJson extends js.Object {
    val index: Int = js.native
    val subindex: Int = js.native
    val startAngle: Double = js.native
    val endAngle: Double = js.native
    val value:Int = js.native
}

@js.native
trait DataFromJsonUrl extends js.Object {
    val label: String = js.native
    val data: js.Array[Double] = js.native
}

class ChordPlot extends RelationPlot {
    //=================== Constructors and related =============================
    private var colorPaletteLocal: Option[List[String]] = None

    def this(data: Seq[(String, Product with Serializable)]) = {
        this()
        val labels: ListBuffer[String] = ListBuffer.empty[String]
        var matrix: ArrayBuffer[Product with Serializable] = ArrayBuffer.empty[Product with Serializable]
        data.foreach(elem => {
            labels += elem._1
            matrix += elem._2
        })
        var arrayOfList: ArrayBuffer[List[Int]] = ArrayBuffer.empty[List[Int]]
        matrix.foreach(tuple => {
            arrayOfList += tuple.productIterator.toList.asInstanceOf[List[Int]]
        })

        setMatrix(LabelizedRelationMatrix(labels.toList, arrayOfList.toList))
    }
    def this(data: List[List[Double]]) = {
        this()
        setMatrix(RelationMatrix(data))
    }
    def this(matrix: RelationMatrix) = {
        this()
        setMatrix(matrix)
    }
    def this(url: String) = {
        this()
        setDataFromUrl(url)
    }

    private var sumData: Option[Double] = None
    private var tickStep: Option[Double] = None
    /** Returns the sum of the data around the whole chord plot */
    private def computeSumDataOverCircle(): Double = {
        // Reset the tick step
        tickStep = None

        data match {
            case None => 0.0
            case Some(d) => {
                var sum = 0.0
                d.foreach(sum += _.sum)
                sum
            }
        }
    }

    //==================== Getters ===========================
    /** Use a color palette in function of the size of the data if none is defined */
    def colorPalette: js.Array[String] = {
        (data, colorPaletteLocal) match {
            case (_, Some(p)) => p
            case (Some(d), None) => if (d.length < 10) d3.schemeCategory10 else d3.schemeCategory20
            case _ => js.Array("") // never used because cannot draw a graph without data
        }
    }

    //================== Setters ===============================
    override def setMatrix(matrix: RelationMatrix): RelationPlot = {
        super.setMatrix(matrix)
        sumData = Some(computeSumDataOverCircle())
        this
    }

    private def setDataFromUrl(url: String): RelationPlot = {
        var xobj = new XMLHttpRequest()
        xobj.open("GET", url, false)
        xobj.send(null)

        if (xobj.readyState == 4 && xobj.status == 200) {
            val r = xobj.responseText
            val d = JSON.parse(r)

            try {
                val dataBuilder = new ListBuffer[List[Double]]
                val labelsBuilder = new ListBuffer[String]
                for (e <- d.asInstanceOf[js.Array[DataFromJsonUrl]]){
                    val row = e.asInstanceOf[DataFromJsonUrl]
                    dataBuilder += row.data.toList
                    labelsBuilder += row.label
                }

                setMatrix(LabelizedRelationMatrix(labelsBuilder.toList, dataBuilder.toList))

            } catch {
                case default:Throwable => {
                    gJS.console.log("error in the json format")
                }
            }


        } else {
            gJS.console.log("error while getting the json")
        }

        this
    }

    def setColorPalette(cp: List[String]): ChordPlot = {colorPaletteLocal = Some(cp); this}
    def colorPalette_=(cp: List[String]): Unit = setColorPalette(cp)

    //=================== Utility function ==============================
    def groupTicks(d:ChordGroup, step: Double): js.Array[js.Dictionary[Double]] = {
        val k: Double = (d.endAngle - d.startAngle) / d.value
        d3.range(0, d.value/(10**scale), step).map(
            (v: Double) => js.Dictionary("value" -> v, "angle" -> (v * k + d.startAngle))
        )
    }

    private val minStepPx = 40 // minimal number of pixels between 2 ticks (not a hard bound)
    /** Computes the number of ticks so that there are not too many nor too few */
    def computeMaxNbTicks(): Int = {
        val diameter = width min height
        val circumference = math.Pi * diameter
        // One tick every 10 pixels
        (circumference / minStepPx).round.toInt
    }

    private def getTickStep: Double = {
        /**
          * Computes the step of the ticks so that
          * there are at most $computeMaxNbTicks ticks and at least one per data point
          * (not in pixels) and puts it in $tickStep
          */
        def computeTickStep(): Unit = {
            val sumDataValue: Double = sumData.getOrElse(0)
            val minTicksStep = closestRoundNb(sumDataValue / computeMaxNbTicks())
            if (minTicksStep == 0.0)
                tickStep = Some(1)
            tickStep = Some(minTicksStep)
        }

        if (tickStep.isDefined)
            tickStep.get
        else {
            computeTickStep()
            tickStep.get
        }
    }

    /** Computes the order of $nb */
    private def getOrder(nb: Double): Int = {
        var stop = false
        var pow = 0
        while (!stop) {
            if (nb >= math.pow(10, pow))
                pow += 1
            else
                stop = true
        }
        pow-1
    }
    /** Computes the closest "round" number to $nb */
    private def closestRoundNb(nb: Double): Long = {
        val order = getOrder(nb)
        if (order == 0)
            return 0
        if (nb == math.pow(10, order))
            return math.pow(10, order).toLong

        val differenceTo1X = (nb - math.pow(10, order)).abs
        val differenceTo2dot5X = (nb - 2.5*math.pow(10, order)).abs
        val differenceTo5X = (nb - 5*math.pow(10, order)).abs
        val differenceTo10X = (nb - math.pow(10, order+1)).abs
        if (differenceTo1X < differenceTo2dot5X)
            return math.pow(10, order).toLong
        if (differenceTo2dot5X < differenceTo5X)
            return (2.5*math.pow(10, order)).toLong
        if (differenceTo5X < differenceTo10X)
            return 5*math.pow(10, order).toLong
        math.pow(10, order+1).toLong
    }

    /** Computes the number of ticks between each big tick (included) */
    private def nbTicksBetweenBigTicks(): Int = {
        val preferredBigTickStep = 4
        val minNbBigTicks = 8
        val maxNbBigTicks = 16
        if (sumData.getOrElse(0) != 0) {
            val totalNbTicks = sumData.get / getTickStep
            val nbBigTicks: Int = (totalNbTicks / preferredBigTickStep).round.toInt
            if (nbBigTicks >= minNbBigTicks && nbBigTicks < maxNbBigTicks)
                return (totalNbTicks / nbBigTicks).round.toInt
            (totalNbTicks / minNbBigTicks).round.toInt
        }
        else 0
    }

    //====================== Display methods ===========================
    def groupLabelData(d:ChordGroup): js.Array[js.Dictionary[Double]] = {
        val angleMean = (d.endAngle - d.startAngle) / 2
        js.Array(js.Dictionary("index" -> d.index.toDouble, "angle" -> (d.startAngle + angleMean)))
    }

    def draw(): Unit = {
        var matrix: js.Array[js.Array[Double]] = js.Array()
        data match {
            case Some(d) => matrix = d
            case None => return
        }

        import d3v4.d3

        val outerRadius = Math.min(width, height) * 0.5 - 40
        val innerRadius = outerRadius - 30

        val chord = d3.chord().padAngle(0.05).sortSubgroups(d3.descending)
        val arc = d3.arc().innerRadius(innerRadius).outerRadius(outerRadius)
        val ribbon = d3.ribbon().radius(innerRadius)
        val color = d3.scaleOrdinal[Int, String]().domain(d3.range(4)).range(colorPalette)

        val g = svg.append("g")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
            .datum(chord(matrix))

        val group = g.append("g").attr("class", "groups")
                .selectAll("g")
                .data((c: ChordArray) => c.groups)
                .enter().append("g")

        group.append("path").style("fill", (d: ChordGroup) => color(d.index))
            .style("stroke", (d: ChordGroup) => d3.rgb(color(d.index)).darker())
            .attr("d", (x: ChordGroup) => arc(x))


        def fade(opacity:Double): js.Any => Unit = {
            d => {
                val i = d.asInstanceOf[ChordGroupJson].index

                svg.selectAll("path")
                    .filter((d:js.Any) => {
                        val dJs = d.asInstanceOf[js.Any]
                        try {
                            val e = dJs.asInstanceOf[ChordGroupJson]
                            i != e.index
                        } catch {
                            case default:Throwable => {
                                try {
                                    val e = dJs.asInstanceOf[ChordJson]
                                    e.source.index != i && e.target.index != i
                                } catch {
                                    case default:Throwable => true
                                }
                            }
                        }
                    })
                    .style("stroke-opacity", opacity.toString)
                    .style("fill-opacity", opacity.toString)
            }
        }

        group
            .on("mouseover", fade(0.2))
            .on("mouseout", fade(0.8))


        var groupTick = group.selectAll(".group-tick").data((d: ChordGroup) => groupTicks(d, getTickStep))
            .enter().append("g").attr("class", "group-tick")
            .attr("transform", (d: js.Dictionary[Double]) =>
                "rotate(" + (d("angle") * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)")

        groupTick.append("line").attr("x2", 6)

        val formatValue = d3.formatPrefix(",.0", getTickStep)
        val bigTickStep = closestRoundNb(nbTicksBetweenBigTicks() * getTickStep)
        groupTick.filter((d: js.Dictionary[Double]) => d("value") % bigTickStep == 0).append("text")
                .attr("x", 8)
                .attr("dy", ".35em")
                .attr("transform", (d: js.Dictionary[Double]) =>
                    if(d("angle") > Math.PI) "rotate(180) translate(-16)"
                    else null)
            .style("text-anchor", (d: js.Dictionary[Double]) => if(d("angle") > Math.PI) "end" else null)
            .text((d: js.Dictionary[Double]) => formatValue(d("value")))

        val labels = getLabels
        if (labels.isDefined){
            val label = d3.scaleOrdinal[Int, String]().domain(d3.range(labels.get.size)).range(labels.get)
            val groupLabel = group.selectAll(".group-label").data((d: ChordGroup) => groupLabelData(d))
                .enter().append("g").attr("class", "group-label")
                .attr("transform", (d: js.Dictionary[Double]) =>
                    "rotate(" + (d("angle") * 180 / Math.PI - 90) + ") translate(" + (outerRadius+24) + ",0)")
//                .attr("transform", (d: js.Dictionary[Double]) =>  "rotate(" + (d("angle") * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)")

            groupLabel.append("text")
                .attr("x", 8)
                .attr("dy", ".35em")
//                .attr("transform", (d: js.Dictionary[Double]) => "rotate(" + ((d("angle") * 180 / Math.PI - 90) + ")" + "translate(" + (innerRadius + 26) + ")" +  (if (d("angle") > Math.PI ) "rotate(180)" else "")))
//                .attr("transform", (d: js.Dictionary[Double]) => (if (d("angle") > Math.PI ) "rotate(180) translate(-16)" else null))
                .attr("transform", (d: js.Dictionary[Double]) => "translate(8) rotate(90)")
//                .style("text-anchor", (d: js.Dictionary[Double]) => if(d("angle") > Math.PI) "end" else null)
                .attr("text-anchor", "middle")
                .text((d: js.Dictionary[Double]) => label(d("index").toInt) )

//        groupTick.append("text")
//            .attr("dy", ".35em")
//            .attr("transform", (d: js.Dictionary[Double]) => "rotate(" + (d("angle") * 180 / Math.PI - 90) + ")" + "translate(" + (innerRadius + 26) + ")" +  (if (d("angle") > Math.PI ) "rotate(180)" else ""))
//            .style("text-anchor", (d: js.Dictionary[Double]) => { if (d("angle") > Math.PI ) "end" else null })
//            .text((d: js.Dictionary[Double]) => label(d("value").toInt) )
        }


        val path = g.append("g").attr("class", "ribbons")
            .selectAll("path").data((c: ChordArray) => c)
        path
            .enter().append("path")
            .attr("d", (d: Chord) => ribbon(d))
            .style("fill", (d: Chord) => color(d.target.index))
            .style("stroke", (d: Chord) => d3.rgb(color(d.target.index)).darker())

        path.exit().remove()
    }
}
object ChordPlot {
    def apply(d:List[List[Double]]): ChordPlot =  new ChordPlot(d)
    def apply(d: (String, Product with Serializable)*): ChordPlot = new ChordPlot(d)
    def apply(matrix: RelationMatrix): ChordPlot = new ChordPlot(matrix)
    def apply(url: String) = new ChordPlot(url)
}
