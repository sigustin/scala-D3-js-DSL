package stream


abstract class MyStream {
    import MyStream._

    def isEmpty: Boolean
    def head  : Int
    def tail: MyStream

    def apply(i: Int): Int = {
      if (i < 0) throw new NoSuchElementException("")
      else if (i == 0) head
      else tail(i-1)
    }
    def filter(p: Int => Boolean): MyStream = {
      p(head) match {
        case true => cons(head, tail.filter(p))
        case false => tail.filter(p)
      }
    }
    
    /**
     * take the n first entry of this stream to create a finite stream of at most n entry
     */
    def take(n: Int): MyStream = {
    	isEmpty || n==0 match{
        case true => empty
        case false => cons(head, tail.take(n-1))
      }
    }
    
    /**
     * apply the f function on each entry of the stream
     * this allow us to use iterate on Mystream object using for loops
     */
    def foreach[U](f: Int => U): Unit = {
    	if (!isEmpty){
            f(head) // the functiion f would have something like a accumulator to keep a state
            tail.foreach(f)
      }
    }
}

object MyStream {
    def cons(hd: Int, tl: => MyStream) = new MyStream {
      def isEmpty = false
      def head: Int = hd
      def tail: MyStream = tl

    }
    val empty = new MyStream {
      def isEmpty = true
      def head = throw new NoSuchElementException("empty.head")
      def tail = throw new NoSuchElementException("empty.tail")
    }
}

object StreamUtils {
  
  import MyStream._
  
  def streamFrom(from: Int): MyStream = cons(from,streamFrom(from+1))
  
  /**
   * Return an infinite fibonacci stream: a, b, a+b, b+a+b, a+b+b+a+b, b+a+b+a+b+b+a+b, ...
   */
  def fibonacci(a: Int, b: Int): MyStream = cons(a, fibonacci(b, a+b))
  
}

  
  