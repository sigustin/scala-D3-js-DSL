package control

object Control {
  /*

  TODO: 
    
  Create Scala constructs to make this new control block work
   
  loop(1 to 5) { i =>
    
  } onException {
    
  }   
   
   
   This code iterates over a Scala range, 
   i is the current value if iteration passed as argument to the closure.
   If an exception is thrown, it is catched and the the onException block is executed
   before continuing on next value of the range
   
   Hint: Don't hesitate to create intermediate classes
  
  */


  class Boucle(r: Range, fun: Int => Unit){
    def onException(funExp: => Unit): Unit ={
      for (e <- r){
        try {
          fun(e)
        }catch{
          case default:Throwable => funExp //add type Throwable to remove a warning
        }
      }
    }
  }

  def loop(r: Range)(fun: Int => Unit): Boucle = {
    new Boucle(r, fun)
  }


  /*
  // test
  lop(1 to 4){println(_)} onException {println("error")}

  lop(0 to 4){
    2 / _
  } onException{
    println("error")
  }
  */

}