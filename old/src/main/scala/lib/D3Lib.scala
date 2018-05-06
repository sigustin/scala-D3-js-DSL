package lib

import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3

import scala.annotation.meta.field
import scala.scalajs.js
import scala.scalajs.js.annotation._

/*@JSExport("H")
@JSExportAll
class D3lib(var o: Int) { // it is a class not an object because we do not want a singleton here
  @JSExport
  def this() = this(450)
  def p = o
  def p_= (value:Int):Unit = o = value
}*/

object D3Lib extends Graph { // it is a class not an object because we do not want a singleton here
    var data = js.Array(1)

    def setData(d: js.Array[Int]): Unit ={
        data = d
    }

    def draw(): Unit = {
        /**
          * Adapted from http://thecodingtutorials.blogspot.ch/2012/07/introduction-to-d3.html
          */

        val graphHeight = height


        val nbBar = data.length

        //The width of each bar.
        val barWidth = width/nbBar - width/nbBar/5

        //The distance between each bar.
        val barSeparation = width/nbBar/5

        //The maximum value of the data.
        val maxData = 50

        //The actual horizontal distance from drawing one bar rectangle to drawing the next.
        val horizontalBarDistance = barWidth + barSeparation

        //The value to multiply each bar's value by to get its height.
        val barHeightMultiplier = graphHeight / maxData;

        //Color for start
        val c = d3.rgb("DarkSlateBlue")

        val rectXFun = (d: Int, i: Int) => i * horizontalBarDistance
        val rectYFun = (d: Int) => graphHeight - d * barHeightMultiplier
        val rectHeightFun = (d: Int) => d * barHeightMultiplier
        val rectColorFun = (d: Int, i: Int) => c.brighter(i * 0.5).toString

        val svg = d3.select("body").append("svg").attr("width", width+"px").attr("height", height+"px")
        val sel = svg.selectAll("rect").data(data)
        sel.enter()
            .append("rect")
            .attr("x", rectXFun)
            .attr("y", rectYFun)
            .attr("width", barWidth)
            .attr("height", rectHeightFun)
            .style("fill", rectColorFun)


    }

}
