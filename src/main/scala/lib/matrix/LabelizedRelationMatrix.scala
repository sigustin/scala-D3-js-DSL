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

    //====================== Utility functions ===========================
    override def toString: String = {
        // TODO this could be better
        labels.toString +"\n"+ super.toString
    }
}

object LabelizedRelationMatrix {
    def apply(labels: List[String], data: List[List[Double]]): LabelizedRelationMatrix =
        new LabelizedRelationMatrix(labels, data)
}

/** Constructors of matrix representing migration flows */
object LabelizedFlowsMatrix {
    def apply(labels: List[String], data: List[List[Double]]): LabelizedRelationMatrix = {
        val answer = new LabelizedRelationMatrix(labels, data)
        answer.ensureZeroDiagonal()
        answer
    }
}
