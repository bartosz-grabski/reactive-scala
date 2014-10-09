import scala.util.{ Try, Success, Failure }

object tutorial3 {

  class Rational(val numer: Int, val denom: Int) {

    require(denom != 0) // precondition for the main constructor

    // another constructor can be added like this:
    def this(x: Int) = this(x, 1)

    // adds two Rational numbers and returns a new Rational which represents the sum
    def +(that: Rational) =
      new Rational(
        this.numer * that.denom + that.denom * this.denom,
        this.denom * that.denom)

    override def toString = numer + "/" + denom
  }

  //////////////////////////////////////
  // 1. Companion objects for classes //
  //////////////////////////////////////

  // let's define a companion object for the class Rational
  object Rational {
    def apply(x: Int, y: Int) = new Rational(x, y)
    def apply(x: Int) = new Rational(x)
  }

  // *** Note ***
  // object with method apply is automatically a function
  // F(x) actually means F.apply(x)
  // In fact, every function is translated to an object of class Function1, Function2, ...
  // depending on the number of arguments.

  // now we can write:
  val r1 = Rational(1, 2)                         //> r1  : tutorial3.Rational = 1/2

  // *** Note ***
  // Companion objects can be used where static classes were used in Java
  // Typical use is an object factory.

}