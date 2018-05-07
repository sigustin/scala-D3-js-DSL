package intro.test

import java.util.Collections
import java.util.LinkedList

object Demo {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(125); 

	val a = Array.tabulate(10)(_%3);System.out.println("""a  : Array[Int] = """ + $show(a ));$skip(27); 
	val b = Array.fill(10)(3);System.out.println("""b  : Array[Int] = """ + $show(b ));$skip(22); 
	val c = Set(1,3,1,2);System.out.println("""c  : scala.collection.immutable.Set[Int] = """ + $show(c ));$skip(16); 
	val c2 = c + 2
	import scala.collection.mutable.Set;System.out.println("""c2  : scala.collection.immutable.Set[Int] = """ + $show(c2 ));$skip(60); 
	val c3 = Set(1,3,1,2);System.out.println("""c3  : scala.collection.mutable.Set[Int] = """ + $show(c3 ));$skip(11); val res$0 = 
	c3.add(5);System.out.println("""res0: Boolean = """ + $show(res$0));$skip(13); 
	println(c3)}
	
}
