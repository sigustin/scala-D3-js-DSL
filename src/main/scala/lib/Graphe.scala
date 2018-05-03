package lib

trait Graphe {
    var height = 480;
    var width = 240;

    def setDimention(h: Int, w:Int): Unit ={
        height = h
        width = w
    }
}
