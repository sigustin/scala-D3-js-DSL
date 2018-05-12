package lib.plot
import d3v4.{Path, Primitive, Projection, d3}
import example.MyRootJson

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

class MigrationPlot extends RelationPlot {
    def draw()={
        val scale = 200 // for this file, this is approximately 0.5*size of America

        val projection: Projection = d3.geoMercator()
        gJS.console.log(projection)

//            .translate((width/2.0+2*scale, height/2.0+2*scale))
//            .scale(scale)

        var path: Path = d3.geoPath(projection)
        //        var path: Path = d3.geoPath().projection(projection.asInstanceOf[TransformType]) // equivalent

        val callback: js.Any => Unit = (d:js.Any) => {
            val geoData = d.asInstanceOf[MyRootJson].features
            println("got:"+d+" => "+geoData)

            val r = scala.util.Random // to generate random id
            val id = "svgID-"+r.nextInt

            val ret = d3.select(localTarget)
                .attr("width", width)
                .attr("height", height)
                .attr("id", id)
                .append("g")
                .attr("id", "map")
                .selectAll("path")
                .data(geoData)
                .enter().append("path")
                .attr("d", path.asInstanceOf[Primitive])
                .attr("fill", "green")

            // move the left corner at the right place and apply the right scale
            val svg_box = gJS.document.querySelector(s"#${id}").getBoundingClientRect()
            val map = gJS.document.querySelector(s"#${id} #map").getBoundingClientRect()

            val scaleX:Double = (svg_box.width/map.width).asInstanceOf[Double]
            val scaleY:Double = (svg_box.height/map.height).asInstanceOf[Double]
            val scaleApplied = scaleX min scaleY

            val dx = (svg_box.x - map.x)*scaleApplied.asInstanceOf[js.Dynamic]
            val dy = (svg_box.y - map.y)*scaleApplied.asInstanceOf[js.Dynamic]

            ret.attr("transform", s"translate(${dx},${dy}) scale(${scaleApplied})")


        }
        d3.json("d3/europe.geo.json", callback)

    }
}
