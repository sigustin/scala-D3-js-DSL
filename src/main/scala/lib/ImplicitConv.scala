package lib

object ImplicitConv {
    implicit def intListToDoubleList(d: List[List[Int]]): List[List[Double]] = d.map(_.map(_.toDouble))
}
