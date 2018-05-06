package lib

import org.singlespaced.d3js.d3

import scala.scalajs.js
import js.Dynamic.{ global => g }
// source : https://github.com/spaced/scala-js-d3-example-app/tree/json_example


@js.native
trait MyRootJson extends js.Object {
    val root:js.Array[MyJson]= js.native
}

@js.native
trait MyJson extends js.Object {
    val name:String= js.native
    val index:Int= js.native
    val from:js.Array[FromJson] = js.native
}

@js.native
trait FromJson extends js.Object {
    val name:String= js.native
}

@js.native
trait SimpleRootJson extends js.Object {
    val root:js.Array[js.Array[Double]]= js.native
}

@js.native
trait SimpleJson extends js.Object {
    val from:js.Array[Double]= js.native
    var ind: Int = js.native
    var color: String = js.native
}


class Chords extends Graph {

    //var data = js.Array(js.Array(1.0))

    def draw(): Unit ={
        val outerRadius = 960 / 2
        val innerRadius = outerRadius - 130;

        val fill = d3.scale.category20c();

        val chord = d3.layout.chord()
            .padding(.04)
//            .sortSubgroups()
        //chord.sortChords()
        //chord.sortSubgroups(d3.descending)
        //chord.sortChords(d3.descending)

        val arc = d3.svg.arc()
            .innerRadius(innerRadius)
            .outerRadius(innerRadius + 20);

        val svg = d3.select("body").append("svg")
            .attr("width", outerRadius * 2)
            .attr("height", outerRadius * 2)
            .append("g")
            .attr("transform", "translate(" + outerRadius + "," + outerRadius + ")");


        d3.json("d3/simple_chord.json", (error:js.Any, json:js.Any) => {
            //if (error != None) throw new Exception
            val data = json.asInstanceOf[SimpleRootJson].root

            val indexByName = d3.map()
            val nameByIndex = d3.map()
            val matrix = js.Array(js.Array(1.0))
            var n = 0

            g.console.log(data)
            g.console.log(data(1))

            for (i <- 0 to 3){
                g.console.log(data(i))
                matrix.append(data(i))
            }

            chord.matrix(matrix);

            val gElem = svg.selectAll(".group")
                .data(chord.groups)
                .enter().append("g")
                .attr("class", "group");


//            val getIndex:Int => String = (d:SimpleJson) => "green"

            gElem.append("path")
                .style("fill", "green")
                .style("stroke", "green")
                .attr("d");
            //            g.style("stroke", function(d) { return fill(d.index); })

            var tmp = 2 // TODO change that, statment that does not return anything (error otherwise)
        })
        /*data = js.Array(
            js.Array(1, 2, 0, 0),
            js.Array(1, 1, 0, 0),
            js.Array(0, 0, 1, 0),
            js.Array(0, 0, 0, 1))

        chord.matrix(data);

        val g = svg.selectAll(".group")
            .data(chord.groups)
            .enter().append("g")
            .attr("class", "group");

        var i = 0
        val getIndex = (d:MyJson) => fill(d.index)
        g.append("path")
        g.style("fill", getIndex)
            .style("stroke", function(d) { return fill(d.index); })
            .attr("d", arc);

        g.append("text")
            .each(function(d) { d.angle = (d.startAngle + d.endAngle) / 2; })
            .attr("dy", ".35em")
            .attr("transform", function(d) {
                return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
                + "translate(" + (innerRadius + 26) + ")"
                + (d.angle > Math.PI ? "rotate(180)" : "");
            })
            .style("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
            .text(function(d) { return nameByIndex.get(d.index); });

        svg.selectAll(".chord")
            .data(chord.chords)
            .enter().append("path")
            .attr("class", "chord")
            .style("stroke", function(d) { return d3.rgb(fill(d.source.index)).darker(); })
            .style("fill", function(d) { return fill(d.source.index); })
            .attr("d", d3.svg.chord().radius(innerRadius));*/

//        d3.json("chords_data2.json", (error: String, imports: Boolean) => Unit {
//            if (error) throw error;
//
//            val indexByName = d3.map()
//            val nameByIndex = d3.map()
//            val matrix = Array()
//            val n = 0
//
//            // Compute a unique index for each package name.
//            imports.forEach(function(d) {
//                if (!indexByName.has(d = d.name)) {
//                    nameByIndex.set(n, d);
//                    indexByName.set(d, n++);
//                }
//            });
//
//            // Construct a square matrix counting package imports.
//            imports.forEach(function(d) {
//                var source = indexByName.get(d.name),
//                row = matrix[source];
//                if (!row) {
//                    row = matrix[source] = [];
//                    for (var i = -1; ++i < n;) row[i] = 0;
//                }
//                d.imports.forEach(function(d) { row[indexByName.get(d)]++; });
//            });
//            console.log(matrix)
//            chord.matrix(matrix);
//
//            var g = svg.selectAll(".group")
//                .data(chord.groups)
//                .enter().append("g")
//                .attr("class", "group");
//
//            g.append("path")
//                .style("fill", function(d) { return fill(d.index); })
//                .style("stroke", function(d) { return fill(d.index); })
//                .attr("d", arc);
//
//            g.append("text")
//                .each(function(d) { d.angle = (d.startAngle + d.endAngle) / 2; })
//                .attr("dy", ".35em")
//                .attr("transform", function(d) {
//                    return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
//                    + "translate(" + (innerRadius + 26) + ")"
//                    + (d.angle > Math.PI ? "rotate(180)" : "");
//                })
//                .style("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
//                .text(function(d) { return nameByIndex.get(d.index); });
//
//            svg.selectAll(".chord")
//                .data(chord.chords)
//                .enter().append("path")
//                .attr("class", "chord")
//                .style("stroke", function(d) { return d3.rgb(fill(d.source.index)).darker(); })
//                .style("fill", function(d) { return fill(d.source.index); })
//                .attr("d", d3.svg.chord().radius(innerRadius));
//
//        })

    }
}
