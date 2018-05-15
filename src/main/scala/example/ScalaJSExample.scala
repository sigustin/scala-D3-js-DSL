package example


import lib.matrix.FlowsMatrix
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
        //========= Chord Graph =====================
        val plot = ChordPlot("data.json")
        plot
            .setTarget("#playground2 svg")
            .setDimension(600, 600)
            .updateLabel("FRA" -> "France")

        plot.colorPalette = List("#000000", "#FFDD89", "#957244", "#F26223", "#902383")
        plot.draw()

        val plot2 = ChordPlot(
            "LabelA" -> (11975, 5871, 8916, 2868),
            "LabelB" -> (1951, 10048, 2060, 6171),
            "LabelC" -> (8010, 16145, 8090, 8045),
            "LabelD" -> (1013, 990, 940, 6907)
        )
        plot2
            .setTarget("#plot1 svg")
            .draw()

//        // ========== Migration map =====================
//        val g = MigrationPlot(
//            "Maps/europe.geo.json",
//            "FIN" -> (0, 4, 5, 4, 6),
//            "FRA" -> (1, 0, 2, 3, 8),
//            "ITA" -> (3, 2, 0, 1, 6),
//            "ESP" -> (10, 20, 30, 0, 1),
//            "GBR" -> (2, 6, 8, 9, 0)
//        )
//        g.draw()

//        val g= MigrationPlot(
//            "Maps/europe.geo.json",
//            "data.json"
//        )
//        g.colorRegion = "#123456"
//        g.colorArrow = "orange"
//        g.showPopup = true
//        g.setDimension(600, 700)
//        g.setTarget("#playground2 svg")
//        g.draw()


        //========== Test matrices =================
//        val testMatrix = FlowsMatrix(
//            (1,2,3),
//            (4,5,6),
//            (7,8,9)
//        )
//        println(testMatrix(0)(*))
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
    }
}
