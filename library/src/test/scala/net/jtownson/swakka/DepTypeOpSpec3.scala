package net.jtownson.swakka

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.ops.hlist.Tupler
import shapeless.{::, HList, HNil}

// Given an hlist such as Container[String] :: Container[Int], pass function, f: (String, Int) => some return type.

class DepTypeOpSpec3 extends FlatSpec {

  case class Container[T](value: T)

  trait ParameterValue[P] {
    type Out
    def get(p: P): Out
  }

  object ParameterValue {
    type Aux[P, O] = ParameterValue[P] { type Out = O }

    def apply[P](implicit inst: ParameterValue[P]): Aux[P, inst.Out] = inst

    def instance[P, O](f: P => O): Aux[P, O] = new ParameterValue[P] {
      type Out = O

      override def get(p: P) = f(p)
    }

    implicit def parameterValue[T]: Aux[Container[T], T] =
      instance(p => p.value)

    implicit val hNilParameterValue: Aux[HNil, HNil] =
      instance(_ => HNil)

    implicit def hListParameterValue[H, T <: HList, HO, TO <: HList](
     implicit
     ph: Aux[H, HO],
     pt: Aux[T, TO]): Aux[H :: T, HO :: TO] =
      instance[H :: T, HO :: TO] {
        case (h :: t) => ph.get(h) :: pt.get(t)
      }
  }

  trait ResponseFunction[Tuple] {
    type Function
    type Return
    def apply(f: Function, t: Tuple): Return
  }

  object ResponseFunction {
    type Aux[T, F, R] = ResponseFunction[T] { type Function = F; type Return = R }

    def apply[T](implicit inst: ResponseFunction[T]): Aux[T, inst.Function, inst.Return] = inst

    implicit def tuple1[A,R]: Aux[A, (A) => R, R] = new ResponseFunction[A] {
      type Function = A => R
      type Return = R

      override def apply(f: Function, t: A): Return = f(t)
    }

    implicit def tuple2[A,B,R]: Aux[(A,B), (A, B) => R, R] = new ResponseFunction[(A, B)] {
      type Function = (A,B) => R
      type Return = R

      override def apply(f: (A, B) => R, t: (A, B)): Return = f.tupled(t)
    }
  }

  def munge[L, O <: HList, T, F, R](l: L, f: F)(implicit pv: ParameterValue.Aux[L, O], tv: Tupler.Aux[O, T], rfv: ResponseFunction.Aux[T, F, R]): R =
    rfv.apply(f, tv(pv.get(l)))


  "ParameterValue" should "Work" in {
    val f: (String, Int) => String = (s, i) => s"I got $s and $i"

    val l = Container[String]("p1") :: Container[Int](1) :: HNil

    val r = munge(l, f)

    println(r)
  }
}


