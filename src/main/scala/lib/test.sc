import scala.scalajs.js

object obj {
    var a = 0
    def inc ={
        a += 1
        this
    }
    def inc2 ={
        a += 2
        this
    }
}

obj.inc.inc2
obj.a


///------------
//val jsArr = js.Array(List(1,3)) // strange error ??
var	plusOne	= (x:Int) =>	x	+	1

//val a = (b: Int, c:Int) => Unit ={
//    print(b, c)
//}

def call2(f: (Int, Int) => Int): Int ={
    f(1, 2)
}
def call1(f: Int => Int): Int ={
    f(2)
}
call2((x:Int, y:Int)=> {x + y})
val scalaFun: Int => Int = (x: Int) => x * x
val scalaFu: Int => Int = (x: Int) => {
    x*x
}
val scalaF = (x: Int) => {
    x*x
}

val scalaFu2: (Int, Int) => Int = (x: Int, y: Int) => {
    x*y
}

def plus1(x:Int):Int = {
    x + 1
}

call1(scalaFun)
call1(plus1)
call2(scalaFu2)