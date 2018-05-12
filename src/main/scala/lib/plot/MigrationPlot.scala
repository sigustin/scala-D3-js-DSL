package lib.plot
import d3v4.{Path, Primitive, Projection, d3}
import example.MyRootJson
import org.scalajs.dom.raw.MouseEvent

import scala.scalajs.js.annotation.{JSExport, JSExportNamed, JSExportTopLevel}
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => gJS}

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
    val geometry:CountryData= js.native
    val properties:js.Any= js.native
}

@js.native
trait CountryData extends js.Object {
    val admin:String= js.native
    val pop_est:Int = js.native
}




class MigrationPlot extends RelationPlot {

    var countrySelected: Option[String] = None

    val r = scala.util.Random // to generate random id
    val idDivInfo = "divID-"+r.nextInt
    val idSvg = "svgID-"+r.nextInt

    val div = gJS.document.createElement("div")
    div.innerHTML = "Hello"
    div.setAttribute("id", idDivInfo)
    div.style.position = "absolute"
    div.style.background = "white"



    def draw()={
        val projection: Projection = d3.geoMercator()

        val handleClick_inside: js.Any => Unit = (d:js.Any) => {
            d3.event.stopPropagation()
            println("inside")
            gJS.console.log(d)
        }

        val handleMouseOver_inside: js.Any => Unit = (d:js.Any) => {
            if (d3.event != null){
                d3.event.stopPropagation()
                val x = d3.event.asInstanceOf[MouseEvent].clientX
                val y = d3.event.asInstanceOf[MouseEvent].clientY

                val div = gJS.document.getElementById(idDivInfo)
                div.style.display = "block"
                val dataCountry = d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData]
                val country = dataCountry.admin
                val pop = dataCountry.pop_est
                div.innerHTML = buildDivContent(country, pop)
                gJS.console.log()
                div.style.left = (x+20)+"px"
                div.style.top = (y+10)+"px"
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
                .on("click",handleClick_inside)
                .on("mousemove", handleMouseOver_inside)


            // move the left corner at the right place and apply the right scale
            val svg_box = gJS.document.querySelector(s"#${idSvg}").getBoundingClientRect()
            val map = gJS.document.querySelector(s"#${idSvg} #map").getBoundingClientRect()

            val scaleX:Double = (svg_box.width/map.width).asInstanceOf[Double]
            val scaleY:Double = (svg_box.height/map.height).asInstanceOf[Double]
            val scaleApplied = scaleX min scaleY

            val dx = (svg_box.x - map.x)*scaleApplied.asInstanceOf[js.Dynamic]
            val dy = (svg_box.y - map.y)*scaleApplied.asInstanceOf[js.Dynamic]

            ret.attr("transform", s"translate(${dx},${dy}) scale(${scaleApplied})")




            // add div for info on the country under the mouse
            gJS.document.getElementById(idSvg).parentElement.appendChild(div)


        }
        d3.json("d3/europe.geo.json", callback)

    }

    def buildDivContent(name:String, population:Int):String = {
        val f1 = d3.formatPrefix(".3s", population)
        val pop = f1(population)

        return s"""
            <div style="margin:10px;">
              <div style="font-size: 20px;"> ${name} </div>
              <div style="font-size: 15px;"> Population: ${pop} </div>
            </div>
            """
    }

}

