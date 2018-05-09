package lib

import scala.collection.mutable.ListBuffer

/** Square matrix used in flow maps */
class FlowsMatrix {
    private var data: List[List[Double]] = _
    private var size: Int = _

    def this(rawData: List[List[Double]]) = {
        this()
        data = makeSquare(rawData)
        println("data in matrix "+data)
    }

    /** Pads $data with 0 to make it square */
    private def makeSquare(data: List[List[Double]]): List[List[Double]] = {
        // TODO print warning if 0 need to be added
        var maxWidth = Int.MinValue
        data.foreach(a => maxWidth = maxWidth max a.length)
        size = maxWidth max data.length


        val buffer = new ListBuffer[List[Double]]
        for (i <- 0 until size) {
            val rowBuffer = new ListBuffer[Double]
            for (j <- 0 until size) {
                try {
                    rowBuffer += data(i)(j)
                } catch {
                    case e: IndexOutOfBoundsException => rowBuffer += 0
                }
            }
            buffer += rowBuffer.toList
        }

        buffer.toList
    }

    def apply(index: Int): List[Double] = {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException
        data(index)
    }
    def apply(indexRow: Int, indexCol: Int): Double = {
        if (indexRow < 0 || indexRow >= size || indexCol < 0 || indexCol >= size)
            throw new IndexOutOfBoundsException
        data(indexRow)(indexCol)
    }
}

object FlowsMatrix {
    def apply(data: List[List[Double]]): FlowsMatrix = {new FlowsMatrix(data)}
    def apply(data: Product with Serializable*): FlowsMatrix = {
        val dataList: List[List[Double]] = data.toList.map(_.productIterator.toList.map(_.asInstanceOf[Double]))
        new FlowsMatrix(dataList)
    }
}
