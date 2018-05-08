package lib

import d3v4._
import lib.{Graph => GraphBase}

import scala.scalajs.js
import js.Dynamic.{global => gJS}
import lib.ImplicitConv._
import org.scalajs.dom.XMLHttpRequest

import scala.collection.mutable.ArrayBuffer
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
    val source:MySubChordJson = js.native
    val target:MySubChordJson = js.native
}

@js.native
trait MySubChordJson extends js.Object {
    val index:Int= js.native
    val subindex:Int= js.native
    val startAngle: Double = js.native
    val endAngle: Double= js.native
    val value:Int = js.native
}

@js.native
trait DataFromJsonUrl extends js.Object {
    val label:String= js.native
    val data: js.Array[Double] = js.native
}

class ChordGraph extends GraphBase {

    private var colorPaletteLocal: Option[js.Array[String]] = None
    def setColorPalette(cp:js.Array[String])=  {colorPaletteLocal = Some(cp); this}

    def this(data: Seq[(String, Product with Serializable)]) = {
        this()
        val labels: ArrayBuffer[String] = ArrayBuffer.empty[String]
        var matrix: ArrayBuffer[Product with Serializable] = ArrayBuffer.empty[Product with Serializable]
        data.foreach(elem => {
            labels += elem._1
            matrix += elem._2
        })
        var arrayOfList: ArrayBuffer[List[Int]] = ArrayBuffer.empty[List[Int]]
        matrix.foreach(tuple => {
            arrayOfList += tuple.productIterator.toList.asInstanceOf[List[Int]]
        })

        setLabel(labels.toList)
        setData(arrayOfList.toList)
    }

    override def setData(d: js.Array[js.Array[Double]]): GraphBase = {
        super.setData(d)
        sumData = Some(computeSumDataOverCircle())
        this
    }

    override def setData(d: List[List[Double]]): GraphBase = {
        super.setData(d)
        sumData = Some(computeSumDataOverCircle())
        this
    }

    // use a color palette in function of the size of the data if none is defined
    def colorPalette:js.Array[String] = {
        (data, colorPaletteLocal) match {
            case (_, Some(p)) => p
            case (Some(d), None) => if (d.length < 10) d3.schemeCategory10 else d3.schemeCategory20
            case _ => return js.Array("") // never used because cannot draw a graph without data
        }
    }

    private var labelLocal: Option[js.Array[String]] = None
    def setLabel(l:js.Array[String]) = { labelLocal = Some(l); this }


    def setDataFromUrl(url: String): Graph = {
        var xobj = new XMLHttpRequest();
        xobj.open("GET", url, false)
        xobj.send(null);

        if (xobj.readyState == 4 && xobj.status == 200) {
            val r = xobj.responseText
            val d = JSON.parse(r)

            try{
                val tmpData: js.Array[js.Array[Double]] = js.Array()
                val tmpLabel: js.Array[String] = js.Array()
                for (e <- d.asInstanceOf[js.Array[DataFromJsonUrl]]){
                    val row = e.asInstanceOf[DataFromJsonUrl]
                    tmpData.append(row.data)
                    tmpLabel.append(row.label)
                }

                data = Some(tmpData)
                labelLocal = Some(tmpLabel)

            }catch {
                case default:Throwable => {
                    gJS.console.log("error in the json format")
                }
            }


        }else {
            gJS.console.log("error while getting the json")
        }

        this
    }

    def groupTicks(d:ChordGroup, step: Double): js.Array[js.Dictionary[Double]] = {
        val k: Double = (d.endAngle - d.startAngle) / d.value
        d3.range(0, d.value/(10**scale), step).map((v: Double) => js.Dictionary("value" -> v, "angle" -> (v * k + d.startAngle)))
    }

    private var sumData: Option[Double] = None
    /** Returns the sum of the data around the whold chord plot */
    private def computeSumDataOverCircle(): Double = {
        data match {
            case None => 0.0
            case Some(matrix) => {
                var sum = 0.0
                matrix.foreach(sum += _.sum)
                sum
            }
        }
    }

    private val minStepPx = 40 // minimal number of pixels between 2 ticks (not a hard bound)
    /** Computes the number of ticks so that there are not too many nor too few */
    def computeMaxNbTicks(): Int = {
        val diameter = width min height
        val circumference = math.Pi * diameter
        // One tick every 10 pixels
        (circumference / minStepPx).round.toInt
    }

    private var tickStep: Option[Double] = None
    private def getTickStep: Double = {
        if (tickStep isDefined)
            return tickStep.get
        else {
            computeTickStep()
            return tickStep.get
        }
    }
    /**
      * Computes the step of the ticks so that
      * there are at most $computeMaxNbTicks ticks and at least one per data point
      * (not in pixels)
      * and puts it in $tickStep
      */
    private def computeTickStep(): Unit = {
        val sumDataValue: Double = sumData.getOrElse(0)
        val minTicksStep = closestRoundNb(sumDataValue / computeMaxNbTicks())
        if (minTicksStep == 0.0)
            tickStep = Some(1)
        tickStep = Some(minTicksStep)
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

    def groupLabelData(d:ChordGroup): js.Array[js.Dictionary[Double]] = {
        val angleMean = (d.endAngle - d.startAngle) / 2
        js.Array(js.Dictionary("index" -> d.index.toDouble, "angle" -> (d.startAngle + angleMean)))
    }

    def draw(): Unit = {
        var matrix:js.Array[js.Array[Double]] = js.Array()
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
                        }catch{
                            case default:Throwable => {
                                try {
                                    val e = dJs.asInstanceOf[ChordJson]
                                    e.source.index != i && e.target.index != i
                                }catch{
                                    case default:Throwable => {
                                        true
                                    }
                                }
                            }

                        }})
                    .style("stroke-opacity", opacity.toString)
                    .style("fill-opacity", opacity.toString);
            }
        }


        group
            .on("mouseover", fade(0.2))
            .on("mouseout", fade(0.8))


        var groupTick = group.selectAll(".group-tick").data((d: ChordGroup) => groupTicks(d, getTickStep))
            .enter().append("g").attr("class", "group-tick")
            .attr("transform", (d: js.Dictionary[Double]) =>  "rotate(" + (d("angle") * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)")

        groupTick.append("line").attr("x2", 6)

        val formatValue = d3.formatPrefix(",.0", getTickStep)
        val bigTickStep = closestRoundNb(nbTicksBetweenBigTicks() * getTickStep)
        groupTick.filter((d: js.Dictionary[Double]) => d("value") % bigTickStep == 0).append("text")
                .attr("x", 8)
                .attr("dy", ".35em")
                .attr("transform", (d: js.Dictionary[Double]) => if(d("angle") > Math.PI) "rotate(180) translate(-16)" else null)
            .style("text-anchor", (d: js.Dictionary[Double]) => if(d("angle") > Math.PI) "end" else null)
            .text((d: js.Dictionary[Double]) => formatValue(d("value")))

        if (labelLocal.isDefined){
            val label = d3.scaleOrdinal[Int, String]().domain(d3.range(4)).range(labelLocal.get)
            val groupLabel = group.selectAll(".group-label").data((d: ChordGroup) => groupLabelData(d))
                .enter().append("g").attr("class", "group-label")
                .attr("transform", (d: js.Dictionary[Double]) =>  "rotate(" + (d("angle") * 180 / Math.PI - 90) + ") translate(" + (outerRadius+24) + ",0)")
            //            .attr("transform", (d: js.Dictionary[Double]) =>  "rotate(" + (d("angle") * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)")

            groupLabel.append("text")
                .attr("x", 8)
                .attr("dy", ".35em")
                //            .attr("transform", (d: js.Dictionary[Double]) => "rotate(" + ((d("angle") * 180 / Math.PI - 90) + ")" + "translate(" + (innerRadius + 26) + ")" +  (if (d("angle") > Math.PI ) "rotate(180)" else "")))
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


        g.append("g").attr("class", "ribbons").selectAll("path").data((c: ChordArray) => c)
            .enter().append("path")
            .attr("d", (d: Chord) => ribbon(d))
            .style("fill", (d: Chord) => color(d.target.index))
            .style("stroke", (d: Chord) => d3.rgb(color(d.target.index)).darker())
    }
}
object ChordGraph {
//    def apply(d:List[List[Double]]): ChordGraph =  new ChordGraph().setData(d)
    def apply(d:js.Array[js.Array[Double]]): ChordGraph =  new ChordGraph().setData(d)
    def apply(d: (String, Product with Serializable)*): ChordGraph = new ChordGraph(d)
}
