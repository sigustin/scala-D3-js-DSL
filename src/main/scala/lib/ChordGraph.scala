package lib

import d3v4._
import lib.{Graph => GraphBase}

import scala.scalajs.js
import js.Dynamic.{global => gJS}
import lib.ImplicitConv._

import scala.collection.mutable.ArrayBuffer

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

    def groupTicks(d:ChordGroup, step: Double): js.Array[js.Dictionary[Double]] = {
        val k: Double = (d.endAngle - d.startAngle) / d.value
        gJS.console.log(d.value)
        gJS.console.log(d.index)
        gJS.console.log(k)
        d3.range(0, d.value/(10**scale), step).map((v: Double) => js.Dictionary("value" -> v, "angle" -> (v * k + d.startAngle)))
    }

    def sumDataOverCircle(): Double = {
        data match {
            case None => 0.0
            case Some(matrix) => {
                var sum = 0.0
                matrix.foreach(sum += _.sum)
                gJS.console.log("sum data "+sum)
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
        gJS.console.log("nb ticks "+(circumference / minStepPx).round.toInt)
        (circumference / minStepPx).round.toInt
    }

    /** Computes the step of the ticks so that there are at most $computeMaxNbTicks ticks and at least one per data point */
    def computeTickStep(): Double = {
        val sumData = sumDataOverCircle()
        val minTicksStep = closestRoundNb(sumData / computeMaxNbTicks())
        gJS.console.log("minTicksStep "+minTicksStep+" "+(minTicksStep == 0.0))
        if (minTicksStep == 0.0)
            return 1
        return minTicksStep
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

        val differenceTo5X = (nb - 5*math.pow(10, order)).abs
        val differenceTo10X = (nb - math.pow(10, order+1)).abs
        if (differenceTo5X < differenceTo10X)
            return 5*math.pow(10, order).toLong
        math.pow(10, order+1).toLong
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

        val tickStep = computeTickStep()
        gJS.console.log("step ticks "+tickStep)
        var groupTick = group.selectAll(".group-tick").data((d: ChordGroup) => groupTicks(d, tickStep))
            .enter().append("g").attr("class", "group-tick")
            .attr("transform", (d: js.Dictionary[Double]) =>  "rotate(" + (d("angle") * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)")

        groupTick.append("line").attr("x2", 6)

        val formatValue = d3.formatPrefix(",.0", tickStep)
        val bigTickStep = 5*tickStep
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
