package lib

import scala.collection.mutable.ListBuffer

/** Square matrix used to store relations between groups for chord plots and migration maps */
class RelationMatrix {
    //============== Constructors and related ===================
    private var data: List[List[Double]] = _
    def getData = data
    private var size: Int = _
    private var zeroDiagonal = false

    // TODO add labels in here?

    def this(rawData: List[List[Double]]) = {
        this()
        data = makeSquare(rawData)
        println("data in matrix "+data)
    }

    /** Pads $data with 0 to make it square */
    private def makeSquare(data: List[List[Double]]): List[List[Double]] = {
        var maxWidth = Int.MinValue
        data.foreach(a => maxWidth = maxWidth max a.length)
        size = maxWidth max data.length

        var invalidEntry = false
        val buffer = new ListBuffer[List[Double]]
        for (i <- 0 until size) {
            val rowBuffer = new ListBuffer[Double]
            for (j <- 0 until size) {
                try {
                    rowBuffer += data(i)(j)
                } catch {
                    case e: IndexOutOfBoundsException => {
                        rowBuffer += 0
                        if (!invalidEntry) {
                            println("[WARNING] the created matrix missed elements => replaced by zeros")
                            invalidEntry = true
                        }
                    }
                }
            }
            buffer += rowBuffer.toList
        }

        buffer.toList
    }

    /**
      * Ensures that the principal diagonal only contains zeros (sets the elements to 0 if needed)
      * After this has been called, the matrix will ensure this is always true
      */
    def ensureZeroDiagonal(): Unit = {
        data = data.zipWithIndex.map{case (l,i) => l.updated(i, 0).asInstanceOf[List[Double]]}
        zeroDiagonal = true
    }

    //===================== Indexing ============================
    /**
      * Returns the element at indices (indexRow, indexCol)
      * or the whole row if $indexCol == * or the whole column if $indexRow == *
      */
    def apply(indexRow: Any)(indexCol: Any): Any = {
        (indexRow, indexCol) match {
            case (indexRowInt: Int, indexColInt: Int) => {
                if (indexRowInt < 0 || indexRowInt >= size || indexColInt < 0 || indexColInt >= size)
                    throw new IndexOutOfBoundsException("Tried to fetch data outside of the matrix")
                data(indexRowInt)(indexColInt)
            }
            case (indexRowInt: Int, _: *.type) => {
                if (indexRowInt < 0 || indexRowInt >= size)
                    throw new IndexOutOfBoundsException("Tried to fetch data outside of the matrix")
                data(indexRowInt)
            }
            case (_: *.type, indexColInt: Int) => {
                if (indexColInt < 0 || indexColInt >= size)
                    throw new IndexOutOfBoundsException("Tried to fetch data outside of the matrix")
                val answer = new ListBuffer[Double]
                data.foreach(answer += _(indexColInt))
                answer.toList
            }
            case _ => throw new IllegalArgumentException("Matrix indices must be Int or *")
        }
    }

    //================== Utility functions =====================
    /** Merge group $indexToIndex._1 and $indexToIndex._2 and puts the resulting elements at index $indexToIndex._1 */
    def merge(indexToIndex: Tuple2[Int, Int]): Unit = {
        // TODO this method might be better off using a ListBuffer
        val index1 = indexToIndex._1
        val index2 = indexToIndex._2
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size)
            throw new IndexOutOfBoundsException
        if (index1 == index2)
            return

        val indexSmall = index1 min index2
        val indexLarge = index1 max index2
        // Merge columns
        data = data.map(row => {
            val (head, val1::tail) = row.splitAt(indexSmall)
            val (mid, val2::end) = tail.splitAt(indexLarge-indexSmall-1)
            if (index1 < index2)
                head ++ List(val1+val2) ++ mid ++ end
            else
                head ++ mid ++ List(val1+val2) ++ end
        })

        // Merge rows
        val (head, row1::tail) = data.splitAt(indexSmall)
        val (mid, row2::end) = tail.splitAt(indexLarge-indexSmall-1)
        var mergedRow = (row1 zip row2).map(t => t._1+t._2)
        if (zeroDiagonal)
            mergedRow = mergedRow.updated(index1, 0).asInstanceOf[List[Double]]
        if (index1 < index2)
            data = head ++ List(mergedRow) ++ mid ++ end
        else
            data = head ++ mid ++ List(mergedRow) ++ end
    }

    override def toString: String = {
        val answer = new StringBuilder()
        answer.append("Matrix(\n")
        data.foreach(row => {
            answer.append("\t")
            row.foreach(elem => {
                answer.append(elem)
                answer.append("\t")
            })
            answer.append("\n")
        })
        answer.append(")")
        answer.toString()
    }
    def prettyPrint(): Unit = {
        println(this)
    }
}

/**
  * This object is used to represent a "whole column" or a "whole row"
  * when indexing a matrix (can't call it ':' as in Python)
  */
object *

object RelationMatrix {
    def apply(data: List[List[Double]]): RelationMatrix = {new RelationMatrix(data)}
    def apply(data: Product with Serializable*): RelationMatrix = {
        val dataList: List[List[Double]] = data.toList.map(_.productIterator.toList.map(_.asInstanceOf[Double]))
        new RelationMatrix(dataList)
    }
}

/** Constructors of matrix representing migration flows */
object FlowsMatrix {
    def apply(data: List[List[Double]]): RelationMatrix = {
        val answer = new RelationMatrix(data)
        answer.ensureZeroDiagonal()
        answer
    }
    def apply(data: Product with Serializable*): RelationMatrix = {
        val dataList: List[List[Double]] = data.toList.map(_.productIterator.toList.map(_.asInstanceOf[Double]))
        val answer = new RelationMatrix(dataList)
        answer.ensureZeroDiagonal()
        answer
    }
}
