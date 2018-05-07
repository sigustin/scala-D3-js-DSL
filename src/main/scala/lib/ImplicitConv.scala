package lib

import scala.scalajs.js

object ImplicitConv {
    implicit def intListToDoubleList(d: List[List[Int]]): List[List[Double]] = d.map(_.map(_.toDouble))

    implicit def GraphToChordGraph(g: Graph): ChordGraph = g.asInstanceOf[ChordGraph]

    implicit def ListToArray[T](d:List[T]): js.Array[T] = {
        val newT:js.Array[T]= js.Array()
        d.foreach(newT.append(_))
        newT
    }

    // syntactic sugar for power of a value
    implicit class PowerInt(val i:Double) extends AnyVal {
        def ** (exp:Double):Double = Math.pow(i,exp)
    }

    // add functionality to the js.Array[js.Array[Double]]
    implicit class SuperArray(val t:js.Array[js.Array[Double]]) {

        /**
          * @return a table with the value of d multiplied by m
          */
        def * (m:Double):js.Array[js.Array[Double]] = {
            val newT:js.Array[js.Array[Double]] = js.Array()
            for(r <- 0 until t.length){
                val tmpNewSubT:js.Array[Double] = js.Array()
                for (e <- 0 until t(r).length){
                    tmpNewSubT.append(t(r)(e) * m)
                }
                newT.append(tmpNewSubT)
            }
            newT
        }

        /**
          * @return the table d with only integer value (but still of type Double) [needed by the library]
          */
        def round: js.Array[js.Array[Double]]={
            for(r <- 0 until t.length; e <- 0 until t(r).length){
                t(r)(e) = t(r)(e).toInt.toDouble
            }
            t
        }

        /**
          * @return the maximum number of figure behind the comma of the value inside d
          */
        def maxNbFigureBehindComma: Int = {
            val maxPossible = 20 // TODO see if it have a sense to have a max value and if this value is ok
            var maxFigureBehideComma = 0
            for(r <- 0 until t.length; e <- 0 until t(r).length){
                val nbFigureBehideCommaVal = nbFigureBehindComma(t(r)(e))
                if (nbFigureBehideCommaVal > maxFigureBehideComma)
                    maxFigureBehideComma = nbFigureBehideCommaVal
            }
            if (maxFigureBehideComma > maxPossible) maxPossible else maxFigureBehideComma
        }
    }

    /**
      * @return the number of figure behind the comma of the value d
      */
    private def nbFigureBehindComma(d: Double) = {
        // TODO handle the case when the string of d is like '1.525E-6'
        if (d%1 == 0)
            0
        else{
            val figureString = String.valueOf(d)
            val sizeString = figureString.length
            val positionComma = figureString.indexOf(".") + 1
            sizeString - positionComma
        }

    }
}
