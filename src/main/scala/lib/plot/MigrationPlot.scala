package lib.plot
//import d3v4.{Path, Primitive, Projection, d3}
import d3v4._
import example.MyRootJson
import lib.ImplicitConv._
import lib.matrix.LabelizedRelationMatrix
import org.scalajs.dom.raw.MouseEvent

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => gJS}
//import lib.ImplicitConv._

//trait TopoJson extends js.Object {
//    val `type`: String = js.native
//    val transform: js.Any = js.native
//    val arcs: js.Array[js.Array[js.Array[Int]]] = js.native
//    val objects: ???
//}
//trait TopoObject extends js.Object {
//    val `type`: String = js.native
//    val geometries: js.Array[???]
//}
//trait TopoPolygon extends js.Object {
//    val `type`: String = js.native
//    val arcs: js.Array[js.Array[Int]]
//    val properties:
//}
//trait TopoMultipolygon extends js.Object {
//    val `type`: String = js.native
//    ???
//}
//trait TopoProperties extends js.Object {
//    val STATEFP: String = js.native
//    val STUSPS: String = js.native
//    val
//}

@js.native
trait MigrationData extends js.Object {
    val geometry:CountryData = js.native
    val properties:js.Any= js.native
}

@js.native
trait CountryData extends js.Object {
    val admin:String= js.native
    val adm0_a3:String = js.native
    val pop_est:Int = js.native
}




class MigrationPlot extends RelationPlot {
    //================= Constructors and related =====================
    var countrySelected: Option[String] = None

    val r = scala.util.Random // to generate random id
    val idDivInfo = "divID-"+r.nextInt
    val idSvg = "svgID-"+r.nextInt

    val div = gJS.document.createElement("div")
    div.style.display = "None"
    div.setAttribute("id", idDivInfo)
    div.style.position = "absolute"
    div.style.background = "white"

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



    def draw()={
        val projection: Projection = d3.geoMercator()

        val handleClick_inside: js.Any => Unit = (d:js.Any) => {
            d3.event.stopPropagation()
            println("inside")
            gJS.console.log(d)
            val name = d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData].admin
            gJS.console.log()
            println(basisMatrix)
//            println(basisMatrix(name -> *))
        }

        val handleMouseOver_inside: js.Any => Unit = (d:js.Any) => {
            if (d3.event != null){
                d3.event.stopPropagation()

                // popup
                val x = d3.event.asInstanceOf[MouseEvent].clientX
                val y = d3.event.asInstanceOf[MouseEvent].clientY

                val div = gJS.document.getElementById(idDivInfo)
                div.style.display = "block"
                val dataCountry = d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData]
                val country = dataCountry.admin
                val pop = dataCountry.pop_est
                div.innerHTML = buildDivContent(country, pop)

                div.style.left = (x+10)+"px"
                div.style.top = (y+10)+"px"
            }

        }


        def color(on:Boolean): js.Any => Unit = {
            d => {
                val country = d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData].admin
                svg.selectAll("path")
                    .filter((d:js.Any) => {
                        try {
                            val e = d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData]
                            country == e.admin
                        } catch {
                            case _: Throwable => false
                        }
                    })
                    .style("fill", (if (on) "yellow" else "green"))
//                    .style("stroke-opacity", opacity.toString)
//                    .style("fill-opacity", opacity.toString)
            }
        }



        val handleMouseOver_outside: js.Any => Unit = (d:js.Any) => {
            var div = gJS.document.getElementById(idDivInfo)
            if (div != null)
                div.style.display = "None"
        }

        val handleClick_outside: js.Any => Unit = (d:js.Any) => {
            println("outside")
            gJS.console.log(d)
        }

        svg.on("click", handleClick_outside)
            .on("mousemove", handleMouseOver_outside)

        var path: Path = d3.geoPath(projection)
        //        var path: Path = d3.geoPath().projection(projection.asInstanceOf[TransformType]) // equivalent

        val callback: js.Any => Unit = (d:js.Any) => {
            val geoData = d.asInstanceOf[MyRootJson].features
            println("got:"+d+" => "+geoData)




            val ret = d3.select(localTarget)
                .attr("width", width)
                .attr("height", height)
                .attr("id", idSvg)
                .append("g")
                .attr("id", "map")
                .selectAll("path")
                .data(geoData)
                .enter().append("path")
                .attr("d", path.asInstanceOf[Primitive])
                .attr("fill", "green")
                .attr("id", (d:js.Any) => d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData].adm0_a3)
                .on("click",handleClick_inside)
                .on("mousemove", handleMouseOver_inside)
                .on("mouseover", color(true))
                .on("mouseout", color(false))


            // move the left corner at the right place and apply the right scale
            val svg_box = gJS.document.querySelector(s"#${idSvg}").getBoundingClientRect()
            val map = gJS.document.querySelector(s"#${idSvg} #map").getBoundingClientRect()

            val scaleX:Double = (svg_box.width/map.width).asInstanceOf[Double]
            val scaleY:Double = (svg_box.height/map.height).asInstanceOf[Double]
            val scaleApplied = scaleX min scaleY

            val dx = (svg_box.x - map.x)*scaleApplied.asInstanceOf[js.Dynamic]
            val dy = (svg_box.y - map.y)*scaleApplied.asInstanceOf[js.Dynamic]

            ret.attr("transform", s"translate(${dx},${dy}) scale(${scaleApplied})")

            // add arrow
            d3.select(localTarget).append("defs").html(
                """
                  |<marker id="arrowhead" markerWidth="10" markerHeight="7"
                  |    refX="7" refY="3.5" orient="auto" markerUnits ="userSpaceOnUse">
                  |    <polygon points="0 0, 10 3.5, 0 7" />
                  |</marker>
                """.stripMargin)

            // does not work // TODO remove
//            svg.call(d3.zoom().on("zoom",  () => {val e = d3.event.transform; gJS.console.log(e.toString); gJS.console.log(s"translate(${e.x*scaleApplied}, ${e.y*scaleApplied}) scaleApplied(${e.k*scaleApplied})"); svg.attr("transform", s"translate(${e.x*scaleApplied}, ${e.y*scaleApplied}) scale(${e.k*scaleApplied})")}))
//            svg.call(d3.zoom().on("zoom",  () => {svg.attr("transform", d3.event.transform.toString)}))

            // trace the migration flow
            val labelsOption = getLabels
            if (labelsOption.isDefined){
                val labels = labelsOption.get
                for (i <- 0 until labels.length){
                    val from = labels(i)
                    for (j <- 0 until labels.length){
                        val toward = labels(j)
                        if (i < j && data.get(i)(j) - data.get(j)(i) != 0){
//                            gJS.console.log(from, " -> ", toward)
                            // the sum of the flow is from 1 to 2, (i,j) -> (j,i)
                            var fromLoc = from
                            var towardLoc = toward
                            if (data.get(i)(j) < data.get(j)(i)){
                                val tmp = from
                                fromLoc = toward
                                towardLoc = tmp
                            }

                            val country = d3.select("#"+fromLoc).datum()
                            val country2 = d3.select("#"+towardLoc).datum()
                            val coord = path.centroid(country)
                            val coord2 = path.centroid(country2)

                            val x1 = coord._1*scaleApplied + dx.asInstanceOf[Double]
                            val y1 = coord._2*scaleApplied + dy.asInstanceOf[Double]
                            val x2 = coord2._1*scaleApplied + dx.asInstanceOf[Double]
                            val y2 = coord2._2*scaleApplied + dy.asInstanceOf[Double]

                            val (x1b, y1b, x2b, y2b) = buildLine(x1, y1, x2, y2)

                            println(x1,","+ y1," "+ x2,","+ y2)
                            println(x1b,","+ y1b," "+ x2b,","+ y2b)


                            val strokeWidth:Double = Math.max(Math.abs(data.get(i)(j) - data.get(j)(i)) / sumData.get*20, 1)
                            val p = "M"+x1b+","+y1b+"L"+x2b+","+y2b+"Z"
                            val ret2 = d3.select(localTarget)
                                .append("g")
                                .append("path")
                                .attr("d", p)
                                .attr("stroke", "black")
                                .attr("stroke-width", strokeWidth)
                                .attr("marker-end", "url(#arrowhead)")
                        }
                    }
                }
            }

//            val country = d3.select("#Canada").datum()
//            val country2 = d3.select("#Mexico").datum()
//            val coord = path.centroid(country)
//            val coord2 = path.centroid(country2)
//
//            val x1 = coord._1*scaleApplied + dx.asInstanceOf[Double]
//            val y1 = coord._2*scaleApplied + dy.asInstanceOf[Double]
//            val x2 = coord2._1*scaleApplied + dx.asInstanceOf[Double]
//            val y2 = coord2._2*scaleApplied + dy.asInstanceOf[Double]
//
//            val p = "M"+x1+","+y1+"L"+x2+","+y2+"Z"
//            val ret2 = d3.select(localTarget)
//                .attr("width", width)
//                .attr("height", height)
//                .append("g")
//                .append("path")
//                .attr("d", p)
//                .attr("stroke", "black")
//                .attr("stoke-width", "1")




            /*val select:js.Any => Unit = (d:js.Any) => {
                gJS.console.log(d)
                val country = d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData].admin

                svg.selectAll("path")
//                    .filter((d:js.Any) => {
////                        gJS.console.log(d)
//                        false
//                    })
                    .style("fill", "yellow")

            }*/


//            ret.on("mouseover", select)



            // add div for info on the country under the mouse
            gJS.document.getElementById(idSvg).parentElement.appendChild(div)


        }
        d3.json("Maps/europe.geo.json", callback)

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

    def buildLine(x1:Double, y1:Double, x2:Double, y2:Double):(Double, Double, Double, Double) = {
        val dx = Math.abs(x1 - x2)
        val dy = Math.abs(y1 - y2)
        val hyp = Math.sqrt(dx*dx + dy*dy)
        val alpha = (Math.acos(dy/hyp))
        val d = 0.02*Math.min(height, width)

        var x1b, y1b, x2b, y2b = 0.0
        val dxb = d*Math.sin(alpha)
        val dyb = d*Math.cos(alpha)
        if (x2 > x1){
            x1b = x1 + dxb
            x2b = x2 - dxb

        }else{
            x1b = x1 - dxb
            x2b = x2 + dxb
        }
        if (y2 > y1){
            y1b = y1 + dyb
            y2b = y2 - dyb
        }else {
            y1b = y1 - dyb
            y2b = y2 + dyb
        }

        (x1b, y1b, x2b, y2b)

    }
}

object MigrationPlot {
    def apply(d: (String, Product with Serializable)*): MigrationPlot = new MigrationPlot(d)
}
