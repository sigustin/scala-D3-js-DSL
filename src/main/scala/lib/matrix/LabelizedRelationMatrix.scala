package lib.matrix

/**
  * Immutable square matrix indexable by labels used to store relations between sections in chord graphs and migration maps
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
        if (labels.distinct.length != labels.length)
            println("[WARNING] Provided duplicated labels => removing the duplicates")

        var fixedLabels = labels.distinct.padTo(size, "")
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
    def setLabels(labels: List[String]): Unit = checkAndInitLabels(labels)

    def updateLabel(labelToLabel: (Any, String)): LabelizedRelationMatrix = {
        val newLabel = labelToLabel._2
        var oldLabel: String = "uninitialized"
        labelToLabel._1 match {
            case index: Int => oldLabel = labels(index)
            case label: String => oldLabel = label
        }
        if (!existsLabel(oldLabel))
            println(s"[WARNING] Label '$oldLabel' is not present in the plot => ignoring the update")

        val updatedLabels = labels.map(label => if (label == oldLabel) newLabel else label)
        new LabelizedRelationMatrix(updatedLabels, data)
    }

    //================== Getters ======================
    def getLabels: List[String] = labels

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
    /** Returns true iff a section is labelled $label */
    def existsLabel(label: String): Boolean = labelsIndices.keySet.contains(label)

    /** Merge section $indexToIndex._1 and $indexToIndex._2 and puts the resulting elements at index of $indexToIndex._2 */
    def mergeAndKeepLabels(indexToIndex: (Any, Any)): LabelizedRelationMatrix = { // DummyImplicit to avoid "same type after erasure error'
        indexToIndex match {
            case (index1: Int, index2: Int) =>
                val updatedLabels = labels.slice(0, index1) ++ labels.slice(index1+1, labels.length)
                new LabelizedRelationMatrix(updatedLabels, super.mergeData(index1 -> index2))
            case (index: Int, label: String) =>
                val updatedLabels = labels.slice(0, index) ++ labels.slice(index+1, labels.length)
                new LabelizedRelationMatrix(updatedLabels, super.mergeData(index -> getIndex(label)))
            case (label: String, index: Int) =>
                val indexLabel = getIndex(label)
                val updatedLabels = labels.slice(0, indexLabel) ++ labels.slice(indexLabel+1, labels.length)
                new LabelizedRelationMatrix(updatedLabels, super.mergeData(indexLabel -> index))
            case (label1: String, label2: String) =>
                val indexLabel1 = getIndex(label1)
                val updatedLabels = labels.slice(0, indexLabel1) ++ labels.slice(indexLabel1+1, labels.length)
                new LabelizedRelationMatrix(updatedLabels, super.mergeData(getIndex(label1) -> getIndex(label2)))
            case _ => throw new IllegalArgumentException("Can only index matrices using Int and labels")
        }
    }
    /** Id. defined to avoid calling super.merge with those specific arguments */
    def merge(indexToIndex: (Any, Any)): LabelizedRelationMatrix = mergeAndKeepLabels(indexToIndex)

    override def toString: String = {
        val answer = new StringBuilder()
        answer.append("Matrix(\n")
        data.zipWithIndex.foreach{
            case (row, i) =>
                answer.append("\t")
                answer.append(labels(i))
                answer.append(":\t")
                row.foreach(elem => {
                    answer.append(elem)
                    answer.append("\t")
                })
                answer.append("\n")
        }
        answer.append(")")
        answer.toString()
    }

    override def equals(other: Any): Boolean = {
        other match {
            case otherMatrix: LabelizedRelationMatrix =>
                if (labels == otherMatrix.labels)
                    super.equals(other)
                else
                    false
            case _ => false
        }
    }
    override def hashCode(): Int = labels.hashCode() + super.hashCode()
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
