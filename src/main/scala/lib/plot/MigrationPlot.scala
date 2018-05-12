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


    /*def handleMouseOver_inside: js.Any => Unit = (d:js.Any) => {
        var x:Double = 0
        var y:Double = 0

        if (d3.event != null){
            println("here")
            d3.event.stopPropagation()
            x = d3.event.asInstanceOf[MouseEvent].clientX
            y = d3.event.asInstanceOf[MouseEvent].clientY
        }else {
            x = d.asInstanceOf[MouseEvent].clientX
            y = d.asInstanceOf[MouseEvent].clientY
        }
        println("mouseover inside")
        val div = gJS.document.getElementById(idDivInfo)
        div.innerHTML = "bonjour"

        gJS.console.log(x, y)
        div.style.left = x+"px"
        div.style.top = y+"px"
    }*/

    def draw()={
        val scale = 200 // for this file, this is approximately 0.5*size of America

        val projection: Projection = d3.geoMercator()

        val handleClick_inside: js.Any => Unit = (d:js.Any) => {
            d3.event.stopPropagation()
            println("inside")
            gJS.console.log(d)
        }

        val handleMouseOver_inside: js.Any => Unit = (d:js.Any) => {
            var x:Double = 0
            var y:Double = 0

            if (d3.event != null){
                println("here")
                d3.event.stopPropagation()
                x = d3.event.asInstanceOf[MouseEvent].clientX
                y = d3.event.asInstanceOf[MouseEvent].clientY

                println("mouseover inside")
                val div = gJS.document.getElementById(idDivInfo)

                val country = d.asInstanceOf[MigrationData].properties.asInstanceOf[CountryData].admin
                div.innerHTML = country
                gJS.console.log()
                div.style.left = (x+20)+"px"
                div.style.top = (y+10)+"px"
            }

        }

        val handleClick_outside: js.Any => Unit = (d:js.Any) => {
            println("outside")
            gJS.console.log(d)
        }



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

            svg.on("click", handleClick_outside)
//                .on("mouseover", handleMouseOver_inside)


            // add div for info
            gJS.document.getElementById(idSvg).parentElement.appendChild(div)
//            gJS.document.getElementById(idSvg).addEventListener("mousemove", handleMouseOver_inside)
//            gJS.document.getElementById(idSvg).setAttribute("onmousemove", "handleMouseOver_inside(event)")

        }
        d3.json("d3/europe.geo.json", callback)

    }

}

