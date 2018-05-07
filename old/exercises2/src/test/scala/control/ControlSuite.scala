package control

import org.scalatest._
import control.Control._

class ControlSuite extends FlatSpec with Matchers {

  "Control" should "work" in {
    

    //TODO: unfomment and make it work
    var nbErr = 0
    loop(1 to 6) { i =>
      if (i % 2  == 0) println(i)
      else throw new Exception("odd number")
      //else throw error("odd number") // HACK was there but not functional, change it by the line above
    } onException {
      nbErr += 1
    }
    
    nbErr should be(3)  
    

    
  }
  


}