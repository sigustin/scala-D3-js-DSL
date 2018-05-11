package lib.matrix

/**
  * Square matrix indexable by labels used to store relations between sections in chord graphs and migration maps
  */
class LabelizedRelationMatrix extends RelationMatrix {
    //=================== Constructors and related ==========================
    protected var labels: List[String] = List() // To each section correspond one and only one label (different labels)
    protected var labelsIndices: Map[String, Int] = Map() // Each label corresponds to an index

    def this(labels: List[String], data:List[List[Double]]) = {
        this()
        this.data = makeSquare(data) // Same as $RelationMatrix
        checkAndInitLabels(labels)
    }

    /** Checks that all labels are different and initializes $this.labels and $this.labelIndices */
    def checkAndInitLabels(labels: List[String]): Unit = {
        var fixedLabels = labels.padTo(size, "")
        if (labels.length < size)
            println("[WARNING] Provided labels are missing some values => replaced by empty strings")
        else if (labels.length > size) {
            println("[WARNING] Number of provided labels is too large => some labels will be ignored")
            fixedLabels = fixedLabels.slice(0, size)
        }

        this.labels = fixedLabels

        val labelsIndicesBuilder = scala.collection.mutable.Map[String, Int]()
        labels.zipWithIndex.foreach{case (label, i) => labelsIndicesBuilder += (label -> i)}
        labelsIndices = labelsIndicesBuilder.toMap
    }

    //=================== Indexing ================================
    /** Returns the index of $label or throws an exception if the label didn't exist */
    private def getIndex(label: String): Int =
        labelsIndices.getOrElse(label, throw new IllegalArgumentException("Invalid label: "+label))

    /**
      * Returns the element at indices labelled (indexRow, indexCol)
      * or the whole row if $indexCol == * or the whole column if $indexRow == *
      */
    override def apply(indexRow: Any)(indexCol: Any): Any = {
        (indexRow, indexCol) match {
            case (_: Int, _: Int) | (_: Int, _: *.type) | (_: *.type, _: Int) => super.apply(indexRow)(indexCol)
            case (labelRow: String, labelCol: String) => super.apply(getIndex(labelRow))(getIndex(labelCol))
            case (labelRow: String, _: Int) => super.apply(getIndex(labelRow))(indexCol)
            case (_: Int, labelCol: String) => super.apply(indexRow)(getIndex(labelCol))
            case (labelRow: String, _: *.type) => super.apply(getIndex(labelRow))(indexCol)
            case (_: *.type, labelCol: String) => super.apply(indexRow)(getIndex(labelCol))
            case _ => throw new IllegalArgumentException("Matrix indices must be Int, * or a label")
        }
    }
    /** Allows for syntax matrix(x -> y) for indexing relations */
    override def apply(indices: (Any, Any)): Any = {
        indices match {
            case (indexRow: Int, indexCol: Int) => super.apply(indexRow)(indexCol)
            case (indexRow: Int, _: *.type) => super.apply(indexRow)(*)
            case (_: *.type, indexCol: Int) => super.apply(*)(indexCol)
            case (labelRow: String, labelCol: String) => apply(labelRow)(labelCol)
            case (labelRow: String, _: *.type ) => apply(labelRow)(*)
            case (_: *.type, labelCol: String) => apply(*)(labelCol)
            case (labelRow: String, indexCol: Int) => apply(labelRow)(indexCol)
            case (indexRow: Int, labelCol: String) => apply(indexRow)(labelCol)
            case _ => throw new IllegalArgumentException("Matrix indices must be Int, * or a label")
        }
    }

    //====================== Utility functions ===========================
    /** Merge section $labelToLabel._1 and $labelToLabel._2 and puts the resulting elements at index of $labelToLabel._2 */
    def merge(labelToLabel: (String, String))(implicit d: DummyImplicit): Unit = { // DummyImplicit to avoid "same type after erasure error'
        super.merge(getIndex(labelToLabel._1) -> getIndex(labelToLabel._2))
    }
    def merge(labelToIndex: (String, Int))(implicit d1: DummyImplicit, d2: DummyImplicit): Unit = {
        super.merge(getIndex(labelToIndex._1) -> labelToIndex._2)
    }
    def merge(indexToLabel: (Int, String))(implicit d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit): Unit = {
        super.merge(indexToLabel._1 -> getIndex(indexToLabel._2))
    }

    override def toString: String = {
        val answer = new StringBuilder()
        answer.append("Matrix(\n")
        data.zipWithIndex.foreach{
            case (row, i) => {
                answer.append("\t")
                answer.append(labels(i))
                answer.append(":\t")
                row.foreach(elem => {
                    answer.append(elem)
                    answer.append("\t")
                })
                answer.append("\n")
            }
        }
        answer.append(")")
        answer.toString()
    }
}

object LabelizedRelationMatrix {
    def apply(labels: List[String], data: List[List[Double]]): LabelizedRelationMatrix =
        new LabelizedRelationMatrix(labels, data)
    def apply(dataAndLabels: (String, Product with Serializable)*): LabelizedRelationMatrix = {
        val labels = dataAndLabels.flatMap(t => List(t._1)).toList
        val data = dataAndLabels.flatMap(t => List(t._2.productIterator.toList.asInstanceOf[List[Double]])).toList
        LabelizedRelationMatrix(labels, data)
    }
}

/** Constructors of matrix representing migration flows */
object LabelizedFlowsMatrix {
    def apply(labels: List[String], data: List[List[Double]]): LabelizedRelationMatrix = {
        val answer = new LabelizedRelationMatrix(labels, data)
        answer.ensureZeroDiagonal()
        answer
    }
    def apply(dataAndLabels: (String, Product with Serializable)*): LabelizedRelationMatrix = {
        val labels = dataAndLabels.flatMap(t => List(t._1)).toList
        val data = dataAndLabels.flatMap(t => List(t._2.productIterator.toList.asInstanceOf[List[Double]])).toList
        val answer = LabelizedRelationMatrix(labels, data)
        answer.ensureZeroDiagonal()
        answer
    }
}
