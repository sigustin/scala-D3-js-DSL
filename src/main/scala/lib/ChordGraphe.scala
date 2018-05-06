package lib;

import d3v4._
import lib.{Graphe => GrapheBase} // to avoid to hide an other class also called Graphe

import scala.scalajs.js
import js.Dynamic.{ global => gJS }
import lib.ImplicitConv._

class ChordGraphe extends GrapheBase {

    def groupTicks(d:ChordGroup, step: Double): js.Array[js.Dictionary[Double]] = {
        val k: Double = (d.endAngle - d.startAngle) / d.value
        d3.range(0, d.value/(10**scale), step).map((v: Double) => js.Dictionary("value" -> v, "angle" -> (v * k + d.startAngle)))
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

        val formatValue = d3.formatPrefix(",.0", 1e3)

        val chord = d3.chord().padAngle(0.05).sortSubgroups(d3.descending)

        val arc = d3.arc().innerRadius(innerRadius).outerRadius(outerRadius)

        val ribbon = d3.ribbon().radius(innerRadius)

        val color = d3.scaleOrdinal[Int, String]().domain(d3.range(4)).range(js.Array("#000000", "#FFDD89", "#957244", "#F26223"))
        gJS.console.log("hello 2")
        val gtmp = svg.append("g")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")

        gJS.console.log("hello 2'")
        val g: Selection[ChordArray] = gtmp.datum(chord(matrix))
        gJS.console.log("hello 2''")
        val group = g.append("g").attr("class", "groups")
                .selectAll("g")
                .data((c: ChordArray) => c.groups)
            .enter().append("g")

        group.append("path").style("fill", (d: ChordGroup) => color(d.index))
            .style("stroke", (d: ChordGroup) => d3.rgb(color(d.index)).darker())
            .attr("d", (x: ChordGroup) => arc(x))

        var groupTick = group.selectAll(".group-tick").data((d: ChordGroup) => groupTicks(d, 1e3))
            .enter().append("g").attr("class", "group-tick")
                .attr("transform", (d: js.Dictionary[Double]) =>  "rotate(" + (d("angle") * 180 / Math.PI - 90) + ") translate(" + outerRadius + ",0)")

        groupTick.append("line").attr("x2", 6)

        groupTick.filter((d: js.Dictionary[Double]) => d("value") % 5e3 == 0).append("text")
                .attr("x", 8)
                .attr("dy", ".35em")
                .attr("transform", (d: js.Dictionary[Double]) => if(d("angle") > Math.PI) "rotate(180) translate(-16)" else null)
            .style("text-anchor", (d: js.Dictionary[Double]) => if(d("angle") > Math.PI) "end" else null)
            .text((d: js.Dictionary[Double]) => formatValue(d("value")))
        gJS.console.log("hello 3")
        g.append("g").attr("class", "ribbons").selectAll("path").data((c: ChordArray) => c)
            .enter().append("path")
            .attr("d", (d: Chord) => ribbon(d))
            .style("fill", (d: Chord) => color(d.target.index))
            .style("stroke", (d: Chord) => d3.rgb(color(d.target.index)).darker())
    }
}
