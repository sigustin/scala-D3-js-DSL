package example


import lib.plot.{ChordPlot, MigrationPlot}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@js.native
trait MyRootJson extends js.Object {
    val features:js.Array[MyRootJson]= js.native
}

@js.native
trait MyFeatureJson extends js.Object {
    val properties:MyCountryJson= js.native
}
@js.native
trait MyCountryJson extends js.Object {
    val name: String = js.native
}

object ScalaJSExample {

    @JSExportTopLevel("myproject")
    protected def getInstance(): this.type = this

    @JSExport
    def main(args: Array[String]): Unit = {
//        //========= Chord Graph =====================
//        val plot = ChordGraph(
//            "LabelA" -> (11975, 5871, 8916, 2868),
//            "LabelB" -> (1951, 10048, 2060, 6171),
//            "LabelC" -> (8010, 16145, 8090, 8045),
//            "LabelD" -> (1013, 990, 940, 6907)
//        ).draw()
//        val dataJs = js.Array[js.Array[Double]](
//            js.Array(11.975,  5.871, 8.916, 2.868),
//            js.Array(1.951, 10.048, 2.060, 6.171),
//            js.Array(8.010, 16.145, 8.090, 8.045),
//            js.Array(1.013, 0.990,  0.940, 6.907)
//        )
//        val dataJs = js.Array[js.Array[Double]](
//            js.Array(11975,  5871, 8916, 2868),
//            js.Array(1951, 10048, 2060, 6171),
//            js.Array(8010, 16145, 8090, 8045),
//            js.Array(1013, 990,  940, 6907)
//        )

//        val dataJs = js.Array[js.Array[Double]](
//            js.Array(12, 6, 9, 3),
//            js.Array(2, 10, 2, 6),
//            js.Array(8, 16, 8, 8),
//            js.Array(1, 1,  1, 7)
//        )

//        val g = new ChordPlot()
//        g.setDataFromUrl("data.json")
//        g.setTarget("#playground2 svg")
//        g.setDimension(480, 480)
//        g.setColorPalette(List("#000000", "#FFDD89", "#957244", "#F26223"))
//        g.draw()

//        val g = new ChordPlot()
//            .setData(dataJs)
//            .setTarget("#playground2 svg")
//            .setDimension(600, 600)
//            .setColorPalette(List("#000000", "#FFDD89", "#957244", "#F26223"))
//            .setLabel(List("A", "B", "C", "D"))
//            .draw()

//        val plot = ChordGraph(
//            "LabelA" -> (1,2,3),
//            "LabelB" -> (4,5,4),
//            "LabelC" -> (3,2,1)
//        )
//
//        plot
//            .setTarget("#playground2 svg")
//            .setDimension(600, 600)
//            .setColorPalette(List("#000000", "#FFDD89", "#957244", "#F26223"))
//            .draw()

//        // ========== Flow map =====================
//        val g= new MigrationPlot()
////        g.setDimension(240, 300)
//        g.setDimension(600, 700)
//        g.setTarget("#playground2 svg")
//        g.draw()


        //========== Test matrices =================
//        val testMatrix = FlowsMatrix((1,2,3), (4,5,6), (7,8,9))
//        println(testMatrix(0)(*)) // Actually no error
//        println(testMatrix(0 -> *))
//        println(testMatrix(*)(2))
//        println(testMatrix(* -> 2))
//        println(testMatrix(0)(2)) // Id.
//        println(testMatrix(0 -> 2))
////        println(testMatrix(-1)(-1)) // exception as intended
//
//        println(testMatrix)
//        testMatrix.merge(1 -> 2)
//        println(testMatrix)
//
//        //MOCKUP
//        val mat = LabelizedFlowsMatrix(
//            "LabelA" -> (1,2,5),
//            "LabelB" -> (3,4,5),
//            "LabelC" -> (7,8,9)
//        )
//        println(mat(0 -> *))
//        println(mat("LabelA")("LabelB"))
//        println(mat("LabelA" -> "LabelB"))
//        println(mat("LabelA" -> *))
//        println(mat(1)("LabelB"))
//        println(mat)
//        mat.merge("LabelB" -> 2)
//        println(mat)

//        val plot = ChordPlot(
//            "LabelA" -> (1,2,3),
//            "LabelB" -> (4,5,4),
//            "LabelC" -> (3,2,1)
//        )
        val plot = ChordPlot("data.json") // TODO Invalid syntax exception?

        plot
            .setTarget("#playground2 svg")
            .setDimension(600, 600)
            .updateLabel("LabelA" -> "LabelOne")

        plot.colorPalette = List("#000000", "#FFDD89", "#957244", "#F26223")
        plot.draw()

//        plot onClick {d: js.Any => println(s"got $d")}
//        plot onClick {println("test")}

//        var clickCount = 0
//        plot onClickDown {
//            if (clickCount % 2 == 0)
////                plot.merge(("LabelB", "LabelC") -> "Label B and C")
//                plot.merge(("A-", "B-") -> "A- and B-")
//            else
//                plot.revertDisplay()
//            plot.draw()
//            clickCount += 1
//        } onClickUp {
//            println("clicked :D")
//        }
    }

    /*
    @JSExportTopLevel("myproject")
    protected def getInstance(): this.type = this

    def groupTicks(d: ChordGroup, step: Double): js.Array[js.Dictionary[Double]] = {
        val k: Double = (d.endAngle - d.startAngle) / d.value
        d3.range(0, d.value, step).map((v: Double) => js.Dictionary("value" -> v, "angle" -> (v * k + d.startAngle)))
    }

    @JSExport
    def main(args: Array[String]): Unit = {
        val matrix = js.Array[js.Array[Double]](
            js.Array(11975,  5871, 8916, 2868),
            js.Array(1951, 10048, 2060, 6171),
            js.Array(8010, 16145, 8090, 8045),
            js.Array(1013,   990,  940, 6907)
        )

        import d3v4.d3
        val svg = d3.select("svg")
        val width = svg.attr("width").toDouble
        val height = svg.attr("height").toDouble
        val outerRadius = Math.min(width, height) * 0.5 - 40
        val innerRadius = outerRadius - 30

        val formatValue = d3.formatPrefix(",.0", 1e3)

        val chord = d3.chord().padAngle(0.05).sortSubgroups(d3.descending)

        val arc = d3.arc().innerRadius(innerRadius).outerRadius(outerRadius)

        val ribbon = d3.ribbon().radius(innerRadius)

        val color = d3.scaleOrdinal[Int, String]().domain(d3.range(4)).range(js.Array("#000000", "#FFDD89", "#957244", "#F26223"))

        val g: Selection[ChordArray] = svg.append("g").attr("transform", "translate(" + width / 2 + "," + height / 2 + ")").datum(chord(matrix))

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

        g.append("g").attr("class", "ribbons").selectAll("path").data((c: ChordArray) => c)
            .enter().append("path")
            .attr("d", (d: Chord) => ribbon(d))
            .style("fill", (d: Chord) => color(d.target.index))
            .style("stroke", (d: Chord) => d3.rgb(color(d.target.index)).darker())
    }*/
}
