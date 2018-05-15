package lib.plot
//import d3v4.{Path, Primitive, Projection, d3}
import d3v4._
import example.MyRootJson
import lib.ImplicitConv._
import lib.matrix.{LabelizedFlowsMatrix, LabelizedRelationMatrix}
import org.scalajs.dom.raw.MouseEvent

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => gJS}


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

@js.native
trait FlowMigration extends js.Object{
    val from:String = js.native
    val to:String = js.native
    val qt:Double = js.native
}




class MigrationPlot extends RelationPlot {
    //================= Constructors and related =====================
    var countrySelected: Option[String] = None
    var mapUrl:Option[String] = None
    var colorRegion = "green"
    var colorArrow = "black"

    val r = scala.util.Random // to generate random id
    val idDivInfo = "divID-"+r.nextInt
    val idSvg = "svgID-"+r.nextInt

    val div = gJS.document.createElement("div")
    div.style.display = "None"
    div.setAttribute("id", idDivInfo)
    div.style.position = "absolute"
    div.style.background = "white"

    def this(mUrl:String, data: Seq[(String, Product with Serializable)]) = {
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
        mapUrl = Some(mUrl)
        setMatrixAndWipeHistory(LabelizedFlowsMatrix(labels.toList, arrayOfList.toList))
    }

    def this(mUrl:String, dUrl:String){
        this()
        mapUrl = Some(mUrl)
        setDataFromUrl(dUrl)
    }

    //====================== Display methods ===========================

    def draw()={
        val projection: Projection = d3.geoMercator()
        
        svg.on("click", showData)
            .on("mousemove", hidePopup)

        var path: Path = d3.geoPath(projection)

        val callback: js.Any => Unit = (d:js.Any) => {
            val geoData = d.asInstanceOf[MyRootJson].features


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
                .attr("fill", colorRegion)
                .attr("id", (d:js.Any) => d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData].adm0_a3)
                .on("mousemove", buildPopupRegion)
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
                s"""
                  |<marker id="arrowhead" markerWidth="10" markerHeight="7"
                  |    refX="7" refY="3.5" orient="auto" markerUnits ="userSpaceOnUse", fill="${colorArrow}">
                  |    <polygon points="0 0, 10 3.5, 0 7" />
                  |</marker>
                """.stripMargin)



            val buf:js.Array[FlowMigration] = js.Array()
            val labelsOption = getLabels
            if (labelsOption.isDefined){
                val labels = labelsOption.get
                for (i <- 0 until labels.length){
                    val from = labels(i)
                    for (j <- 0 until labels.length){
                        val toward = labels(j)
                        if (i < j && data.get(i)(j) - data.get(j)(i) != 0){
                            var fromLoc = from
                            var towardLoc = toward
                            if (data.get(i)(j) < data.get(j)(i)){
                                val tmp = from
                                fromLoc = toward
                                towardLoc = tmp
                            }

                            val d = js.Dynamic.literal("from" -> fromLoc, "to" -> towardLoc, "qt" -> Math.abs(data.get(j)(i) - data.get(i)(j))).asInstanceOf[FlowMigration]
                            buf.append(d)

                            val country = d3.select("#"+towardLoc).datum()
                            val country2 = d3.select("#"+fromLoc).datum()
                            val coord = path.centroid(country)
                            val coord2 = path.centroid(country2)

                            val x1 = coord._1*scaleApplied + dx.asInstanceOf[Double]
                            val y1 = coord._2*scaleApplied + dy.asInstanceOf[Double]
                            val x2 = coord2._1*scaleApplied + dx.asInstanceOf[Double]
                            val y2 = coord2._2*scaleApplied + dy.asInstanceOf[Double]

                            val (x1b, y1b, x2b, y2b) = buildLine(x1, y1, x2, y2)


                            def makeFlowListener(on:Boolean):js.Any => Unit = {
                               d => {
                                   val f = d.asInstanceOf[FlowMigration]
                                   d3.select(localTarget).select(s"#${f.from}-${f.to}")
                                       .attr("stroke", if (on) "red" else colorArrow)

                               }
                            }


                            val strokeWidth:Double = Math.max(Math.abs(data.get(i)(j) - data.get(j)(i)) / sumData.get*30, 1)
                            val p = "M"+x1b+","+y1b+"L"+x2b+","+y2b+"Z"
                            val ret2 = d3.select(localTarget)
                                .append("g")
                                .append("path")
                                .attr("id", s"${fromLoc}-${towardLoc}")
                                .attr("class", "flowMigration")
                                .attr("d", p)
                                .attr("stroke", colorArrow)
                                .attr("stroke-linecap", "butt")
                                .attr("stroke-width", strokeWidth)
                                .attr("marker-end", "url(#arrowhead)")
                                .on("mouseover", makeFlowListener(true))
                                .on("mouseout", makeFlowListener(false))
                                .on("mousemove", buildPopupFlow)
                        }
                    }
                }

                d3.select(localTarget).selectAll(".flowMigration")
                    .data(buf).enter()
            }


            // add div for info on the country under the mouse
            gJS.document.getElementById(idSvg).parentElement.appendChild(div)


        }
        d3.json(mapUrl.get, callback)

    }

    //--------------- Draw utilities -------------------------

    val showData: js.Any => Unit = (d:js.Any) => {
        d3.event.stopPropagation()
        println("data of the flows:")
        println(basisMatrix)
    }

    val buildPopupRegion: js.Any => Unit = (d:js.Any) => {
        if (d3.event != null){
            d3.event.stopPropagation()

            // popup
            val x = d3.event.asInstanceOf[MouseEvent].clientX
            val y = d3.event.asInstanceOf[MouseEvent].clientY

            val div = gJS.document.getElementById(idDivInfo)
            if (showPopup)
                div.style.display = "block"
            val dataCountry = d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData]
            val country = dataCountry.admin
            val pop = dataCountry.pop_est
            div.innerHTML = buildDivContentCountry(country, pop)

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
                .style("fill", (if (on) "yellow" else colorRegion))
        }
    }

    val buildPopupFlow: js.Any => Unit = (d:js.Any) => { // TODO
        if (d3.event != null){
            d3.event.stopPropagation()

            // popup
            val x = d3.event.asInstanceOf[MouseEvent].clientX
            val y = d3.event.asInstanceOf[MouseEvent].clientY

            val div = gJS.document.getElementById(idDivInfo)
            if (showPopup)
                div.style.display = "block"
            val dFlow = d.asInstanceOf[FlowMigration]
            div.innerHTML = buildDivContentFlow(dFlow.from, dFlow.to, dFlow.qt)

            div.style.left = (x+10)+"px"
            div.style.top = (y+10)+"px"
        }

    }

    val hidePopup: js.Any => Unit = (d:js.Any) => {
        var div = gJS.document.getElementById(idDivInfo)
        if (div != null)
            div.style.display = "None"
    }

    def buildDivContentCountry(name:String, population:Int):String = {
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

    def buildDivContentFlow(from:String, to:String, qt:Double):String = {
        val f1 = d3.formatPrefix(".0s", qt)
        val qtFormat = f1(qt)

        val f = d3.select(localTarget).select("#"+from).data().asInstanceOf[js.Array[MigrationData]](0).properties.asInstanceOf[CountryData].admin
        val t = d3.select(localTarget).select("#"+to).data().asInstanceOf[js.Array[MigrationData]](0).properties.asInstanceOf[CountryData].admin

        // the mouseover listener prevent that the popup is blocked when the mouse arrive on it, cause blink
        return s"""
            <div style="margin:10px; color: black"; onmouseover="this.style.display = 'None'">
              <div><span style="font-size: 20px;"> ${qtFormat} </span>
              <span>NET MOVER</span>
              </div>
              <div style="font-size: 15px;"> from: ${f} </div>
              <div style="font-size: 15px;"> to: ${t} </div>
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
    def apply(mapUrl: String, d: (String, Product with Serializable)*): MigrationPlot = new MigrationPlot(mapUrl, d)
    def apply(mapUrl:String, dataUrl:String): MigrationPlot = new MigrationPlot(mapUrl, dataUrl)
}
