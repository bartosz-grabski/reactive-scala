object tutorial4 {
  //////////////////
  // 1. Implicits //
  //////////////////

  // a) conversion
  implicit def double2Int(d: Double) = d.toInt    //> double2Int: (d: Double)Int
  val x: Int = 44.0                               //> x  : Int = 44

  implicit def defaultVal: Double = 42.0          //> defaultVal: => Double
  //implicit def otherVal: Double = 40.0

  // b) implicit parameter
  def foo(implicit x: Double) = println(x)        //> foo: (implicit x: Double)Unit
  foo                                             //> 42.0

  //////////////////////////
  // 2. Partial functions //
  //////////////////////////

  val root: PartialFunction[Double, Double] = {
    case d if (d >= 0) => math.sqrt(d)
  }                                               //> root  : PartialFunction[Double,Double] = <function1>

  root isDefinedAt -1                             //> res0: Boolean = false

  // collect applies a partial function to all list elements for which it is defined, and creates a new list
  List(-2.0, 0, 2, 4) collect root                //> res1: List[Double] = List(0.0, 1.4142135623730951, 2.0)

  // Documentation: http://www.scala-lang.org/api/current/index.html#scala.PartialFunction
  
}