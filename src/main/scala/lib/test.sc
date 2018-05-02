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

val b = Array(Array(0, 0, 0, 0)).transpose

for (i <- b; j <- i){
  print(j)
}