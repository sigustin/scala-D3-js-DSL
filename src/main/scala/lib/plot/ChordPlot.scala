package lib.plot

import d3v4.{ChordGroup, _}
import lib.ImplicitConv._
import lib.matrix.{LabelizedRelationMatrix, RelationMatrix}
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.raw.MouseEvent

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
    private var indexSelected:Option[Int] = None

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

        setMatrixAndWipeHistory(LabelizedRelationMatrix(labels.toList, arrayOfList.toList))
    }
    def this(data: List[List[Double]]) = {
        this()
        setMatrixAndWipeHistory(RelationMatrix(data))
    }
    def this(matrix: RelationMatrix) = {
        this()
        setMatrixAndWipeHistory(matrix)
    }
    def this(url: String) = {
        this()
        setDataFromUrl(url)
    }

    private var tickStep: Option[Double] = None

    val idDivInfo = "divID-"+scala.util.Random.nextInt
    val div = gJS.document.createElement("div")
    div.style.display = "None"
    div.setAttribute("id", idDivInfo)
    div.style.position = "absolute"
    div.style.background = "white"

    //==================== Getters ===========================
    /** Use a color palette in function of the size of the data if none is defined */
    private def colorPaletteJS: js.Array[String] = {
        (data, colorPaletteLocal) match {
            case (_, Some(p)) => p
            case (Some(d), None) => if (d.length < 10) d3.schemeCategory10 else d3.schemeCategory20
            case _ => js.Array("") // never used because cannot draw a graph without data
        }
    }

    //================== Setters ===============================
    override def setMatrixAndWipeHistory(matrix: RelationMatrix): RelationPlot = {
        super.setMatrixAndWipeHistory(matrix)
        tickStep = None // Reset the tick step
        this
    }

    private def setDataFromUrl(url: String): RelationPlot = {
        val xobj = new XMLHttpRequest()
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

                setMatrixAndWipeHistory(LabelizedRelationMatrix(labelsBuilder.toList, dataBuilder.toList))

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
    def colorPalette: List[String] = colorPaletteLocal.getOrElse(List())

    //=================== Utility function ==============================
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
            if (minTicksStep == 0.0) {
                tickStep = Some(1)
                return
            }
            tickStep = Some(minTicksStep)
        }

        if (tickStep.isDefined)
            tickStep.get
        else {
            computeTickStep()
            tickStep.get
        }
    }

    /** Computes the closest "round" number to $nb */
    private def closestRoundNb(nb: Double): Long = {
        /** Computes the order of $nb */
        def getOrder(nb: Double): Int = {
            if (nb < 0) println(s"[WARNING] Tried to compute the order of $nb => actually computed the order of ${nb.abs}")
            var fixedNb = nb.abs

            var stop = false
            var pow = 0
            while (!stop) {
                if (fixedNb >= math.pow(10, pow))
                    pow += 1
                else
                    stop = true
            }
            pow-1
        }

        val order = getOrder(nb)
        if (nb.abs == math.pow(10, order))
            return nb.toLong

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
                return preferredBigTickStep
            (totalNbTicks / minNbBigTicks).round.toInt
        }
        else 0
    }

    //====================== Display methods ===========================
    def groupLabelData(d:ChordGroup): js.Array[js.Dictionary[Double]] = {
        val angleMean = (d.endAngle - d.startAngle) / 2
        js.Array(js.Dictionary("index" -> d.index.toDouble, "angle" -> (d.startAngle + angleMean)))
    }

    override def draw(): Unit = {
        var matrix: js.Array[js.Array[Double]] = js.Array()
        data match {
            case Some(d) => matrix = d
            case None => return
        }

        import d3v4.d3

        // Refresh display
        d3.select(localTarget+" g").remove()

        gJS.document.querySelector(localTarget).style.fontSize = "15px"
//        gJS.document.querySelector(localTarget).style.stroke = "black" needed to override the style of reveal.js

        val outerRadius = Math.min(width, height) * 0.5 - 40
        val innerRadius = outerRadius - 30

        // Make sections ('groups' in JS)
        val chord = d3.chord().padAngle(0.05).sortSubgroups(d3.descending)
        val g = svg.append("g")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
            .datum(chord(matrix))

        val section = g.append("g").attr("class", "groups")
                .selectAll("g")
                .data((c: ChordArray) => c.groups)
                .enter().append("g")

        val color = d3.scaleOrdinal[Int, String]().domain(d3.range(4)).range(colorPaletteJS)
        val arc = d3.arc().innerRadius(innerRadius).outerRadius(outerRadius)
        section.append("path").style("fill", (d: ChordGroup) => color(d.index))
            .style("stroke", (d: ChordGroup) => d3.rgb(color(d.index)).darker())
            .attr("d", (x: ChordGroup) => arc(x))

        section
            .on("mouseover", fadeSections(0.2))
            .on("mouseout", fadeSections(0.8))
            //.on("click", merger)

        svg.call(d3.zoom().on("zoom",  () => g.attr("transform", d3.event.transform.toString)))
        onClick {
            revert()
            draw()
        }

        // Set focusing behavior
        focusEvent match {
            case FocusEvent.click => section.on("click", focusAndMergeSections)
            case FocusEvent.hover => section.on("mouseenter", focusAndMergeSections)
            case _ =>
        }

        // Place ticks around the plot
        def groupTicks(d:ChordGroup, step: Double): js.Array[js.Dictionary[Double]] = {
            val k: Double = (d.endAngle - d.startAngle) / d.value
            d3.range(0, (d.value+1)/(10**scale), step).map(
                (v: Double) => js.Dictionary("value" -> v, "angle" -> (v * k + d.startAngle))
            )
        }

        var groupTick = section.selectAll(".group-tick").data((d: ChordGroup) => groupTicks(d, getTickStep))
            .enter().append("g").attr("class", "group-tick")
            .attr("transform", (d: js.Dictionary[Double]) =>
                "rotate(" + (d("angle") * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)")

        groupTick.append("line").attr("x2", 6)

        val formatValue = d3.formatPrefix(",.0", getTickStep) // Should print K for thousands, M for millions...
        val computedBigTickStep = closestRoundNb(nbTicksBetweenBigTicks() * getTickStep)
        val bigTickStep = if (computedBigTickStep == 0.0) getTickStep else computedBigTickStep
        groupTick.filter((d: js.Dictionary[Double]) => d("value") % bigTickStep == 0).append("text")
                .attr("x", 8)
                .attr("dy", ".35em")
                .attr("transform", (d: js.Dictionary[Double]) =>
                    if(d("angle") > Math.PI) "rotate(180) translate(-16)"
                    else null)
            .style("text-anchor", (d: js.Dictionary[Double]) => if(d("angle") > Math.PI) "end" else null)
            .text((d: js.Dictionary[Double]) => formatValue(d("value")))

        // Place labels
        val labels = getLabels
        if (labels.isDefined){
            val label = d3.scaleOrdinal[Int, String]().domain(d3.range(labels.get.size)).range(labels.get)
            val groupLabel = section.selectAll(".group-label").data((d: ChordGroup) => groupLabelData(d))
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

        // Make ribbons
        val ribbon = d3.ribbon().radius(innerRadius)
        val path = g.append("g").attr("class", "ribbons")
            .selectAll("path").data((c: ChordArray) => c)
        path
            .enter().append("path")
            .attr("d", (d: Chord) => ribbon(d))
            .style("fill", (d: Chord) => color(d.target.index))
            .style("stroke", (d: Chord) => d3.rgb(color(d.target.index)).darker())

//        g.selectAll(".ribbons").on("mouseover", () => {println("rib")})
        g.selectAll(".ribbons").on("mouseover", handleMouseOver_inside)
        svg.on("mousemove", handleMouseOver_outside)

        // Place element that will be used as hovering text
        d3.select("body")
            .append("div")
            .attr("id", idDivInfo)
            .style("display", "None")
            .style("background", "white")
            .style("position", "absolute")

        path.exit().remove()
    }

    //--------------- Draw utilities -------------------------
    /** Makes all sections fade but the one hovered */
    def fadeSections(opacity:Double): js.Any => Unit = {
        d => {
            val i = d.asInstanceOf[ChordGroupJson].index
            svg.selectAll("path")
                .filter((d:js.Any) => {
                    val dJs = d.asInstanceOf[js.Any]
                    try {
                        val e = dJs.asInstanceOf[ChordGroupJson]
                        i != e.index
                    } catch {
                        case _: Throwable =>
                            try {
                                val e = dJs.asInstanceOf[ChordJson]
                                e.source.index != i && e.target.index != i
                            } catch {
                                case _: Throwable => true
                            }
                    }
                })
                .style("stroke-opacity", opacity.toString)
                .style("fill-opacity", opacity.toString)
        }
    }

    val handleMouseOver_inside: js.Any => Unit =
        (d:js.Any) => {
            if (d3.event != null){
                d3.event.stopPropagation()

                // popup
                val x = d3.event.asInstanceOf[MouseEvent].clientX
                val y = d3.event.asInstanceOf[MouseEvent].clientY

                println(s"move $x $y")

//                val div = gJS.document.getElementById(idDivInfo)
//                println(s"div is $div")
//                div.style.display = "block"
                val selectedDiv = d3.select("#"+idDivInfo)
//                println(s"selectedDiv is ${selectedDiv.attr("style")}")
                selectedDiv.style("display", "block")
//                println(s"selectedDiv is ${selectedDiv.attr("style")}")
//                val dataCountry = d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData]
//                val country = dataCountry.admin
//                val pop = dataCountry.pop_est
                div.innerHTML = buildDivContent("test", 10)

//                div.style.left = (x+10)+"px"
//                div.style.top = (y+10)+"px"
                selectedDiv.style("left", (x+10)+"px")
                selectedDiv.style("top", (y+10)+"px")
            }
        }

    val handleMouseOver_outside: js.Any => Unit =
        (d:js.Any) => {
            var div = gJS.document.getElementById(idDivInfo)
            if (div != null)
                div.style.display = "None"
        }

    def buildDivContent(name:String, population:Int):String = {
        val f1 = d3.formatPrefix(".3s", population)
        val pop = f1(population)

        // the mouseover listener prevent that the popup is blocked when the mouse arrive on it, cause blink
        return s"""
            <div style="margin:10px; color: black"; onmouseover="this.style.display = 'None'">
              <div style="font-size: 20px;"> ${name} </div>
              <div style="font-size: 15px;"> Population: ${pop} </div>
            </div>
            """
    }
}

object ChordPlot {
    def apply(d:List[List[Double]]): ChordPlot =  new ChordPlot(d)
    def apply(d: (String, Product with Serializable)*): ChordPlot = new ChordPlot(d)
    def apply(matrix: RelationMatrix): ChordPlot = new ChordPlot(matrix)
    def apply(url: String) = new ChordPlot(url)
}
