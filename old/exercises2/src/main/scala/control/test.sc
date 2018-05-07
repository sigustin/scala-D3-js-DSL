val b:Int  = 3

val a:Range = 0 to 5

class Boucle(r: Range, fun: Int => Unit){
  def onException(funExp: => Unit): Unit ={
    try {
      for (e <- r){
        fun(e)
      }
    }catch{
      case default:Throwable => funExp
    }
  }
}

def loop(r: Range)(fun: Int => Unit): Boucle = {
  new Boucle(r, fun)
}

loop(1 to 4){println(_)} onException {println("error")}

loop(0 to 4){
  2 / _
} onException{
  println("error")
}