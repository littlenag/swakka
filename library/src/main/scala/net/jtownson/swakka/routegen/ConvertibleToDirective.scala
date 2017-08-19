package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.FormFieldDirectives.FieldMagnet._
import akka.http.scaladsl.server.directives.FutureDirectives.onComplete
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshal, _}
import akka.stream.ActorMaterializer
import net.jtownson.swakka.jsonschema.ApiModelDictionary._
import net.jtownson.swakka.model.Parameters.BodyParameter.OpenBodyParameter
import net.jtownson.swakka.model.Parameters.FormParameter1.OpenFormParameter1
import net.jtownson.swakka.model.Parameters.FormParameter2.OpenFormParameter2
import net.jtownson.swakka.model.Parameters.HeaderParameter.OpenHeaderParameter
import net.jtownson.swakka.model.Parameters.PathParameter.OpenPathParameter
import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter
import net.jtownson.swakka.model.Parameters._
import net.jtownson.swakka.routegen.PathHandling.{containsParamToken, pathWithParamMatcher}
import shapeless.{::, HList, HNil}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe.TypeTag
import scala.util.{Failure, Success, Try}

trait ConvertibleToDirective[T] {
  def convertToDirective(modelPath: String, t: T): Directive1[T]
}

object ConvertibleToDirective {

  val BooleanSegment: PathMatcher1[Boolean] =
    PathMatcher("""^(?i)(true|false)$""".r) flatMap (s => Some(s.toBoolean))

  val FloatNumber: PathMatcher1[Float] =
    PathMatcher("""[+-]?\d*\.?\d*""".r) flatMap { string =>
      try Some(java.lang.Float.parseFloat(string))
      catch {
        case _: NumberFormatException ⇒ None
      }
    }

  def converter[T](t: T)(implicit ev: ConvertibleToDirective[T]): ConvertibleToDirective[T] = ev

  implicit val stringReqQueryConverter: ConvertibleToDirective[QueryParameter[String]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.?(default)).map(close(qp))
        case None => parameter(qp.name).map(close(qp))
      }
    })

  implicit val stringOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[String]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.?(default)).map(os => close(qp)(Some(os)))
        case _ => parameter(qp.name.?).map(close(qp))
      }
    })

  implicit val floatReqQueryConverter: ConvertibleToDirective[QueryParameter[Float]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Float].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Float]).map(close(qp))
      }
    })

  implicit val floatOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Float]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Float].?(default)).map(of => close(qp)(Some(of)))
        case _ => parameter(qp.name.as[Float].?).map(close(qp))
      }
    })

  implicit val doubleReqQueryConverter: ConvertibleToDirective[QueryParameter[Double]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Double].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Double]).map(close(qp))
      }
    })

  implicit val doubleOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Double]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Double].?(default)).map(dp => close(qp)(Some(dp)))
        case _ => parameter(qp.name.as[Double].?).map(close(qp))
      }
    })

  implicit val booleanReqQueryConverter: ConvertibleToDirective[QueryParameter[Boolean]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Boolean].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Boolean]).map(close(qp))
      }
    })

  implicit val booleanOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Boolean]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Boolean].?(default)).map(bp => close(qp)(Some(bp)))
        case _ => parameter(qp.name.as[Boolean].?).map(close(qp))
      }
    })

  implicit val intReqQueryConverter: ConvertibleToDirective[QueryParameter[Int]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Int].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Int]).map(close(qp))
      }
    })

  implicit val intOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Int]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Int].?(default)).map(ip => close(qp)(Some(ip)))
        case _ => parameter(qp.name.as[Int].?).map(close(qp))
      }
    })

  implicit val longReqQueryConverter: ConvertibleToDirective[QueryParameter[Long]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Long].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Long]).map(close(qp))
      }
    })

  implicit val longOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Long]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Long].?(default)).map(lp => close(qp)(Some(lp)))
        case _ => parameter(qp.name.as[Long].?).map(close(qp))
      }
    })


  implicit val stringReqPathConverter: ConvertibleToDirective[PathParameter[String]] =
    pathParamDirective(Segment)

  implicit val floatPathConverter: ConvertibleToDirective[PathParameter[Float]] =
    pathParamDirective(FloatNumber)

  implicit val doublePathConverter: ConvertibleToDirective[PathParameter[Double]] =
    pathParamDirective(DoubleNumber)

  implicit val booleanPathConverter: ConvertibleToDirective[PathParameter[Boolean]] =
    pathParamDirective(BooleanSegment)

  implicit val intPathConverter: ConvertibleToDirective[PathParameter[Int]] =
    pathParamDirective(IntNumber)

  implicit val longPathConverter: ConvertibleToDirective[PathParameter[Long]] =
    pathParamDirective(LongNumber)


  implicit val stringReqHeaderConverter: ConvertibleToDirective[HeaderParameter[String]] =
    requiredHeaderParamDirective(s => s)

  implicit val stringOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[String]]] =
    optionalHeaderParamDirective(s => s)

  implicit val floatReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Float]] =
    requiredHeaderParamDirective(_.toFloat)

  implicit val floatOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Float]]] =
    optionalHeaderParamDirective(_.toFloat)

  implicit val doubleReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Double]] =
    requiredHeaderParamDirective(_.toDouble)

  implicit val doubleOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Double]]] =
    optionalHeaderParamDirective(_.toDouble)

  implicit val booleanReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Boolean]] =
    requiredHeaderParamDirective(_.toBoolean)

  implicit val booleanOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Boolean]]] =
    optionalHeaderParamDirective(_.toBoolean)

  implicit val intReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Int]] =
    requiredHeaderParamDirective(_.toInt)

  implicit val intOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Int]]] =
    optionalHeaderParamDirective(_.toInt)

  implicit val longReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Long]] =
    requiredHeaderParamDirective(_.toLong)

  implicit val longOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Long]]] =
    optionalHeaderParamDirective(_.toLong)

  implicit def bodyParamConverter[T](implicit ev: FromRequestUnmarshaller[T]): ConvertibleToDirective[BodyParameter[T]] =
    (_: String, bp: BodyParameter[T]) => {
      bp.default match {
        case None => entity(as[T]).map(close(bp))
        case Some(default) => optionalEntity[T](as[T]).map {
          case Some(value) => close(bp)(value)
          case None => close(bp)(default)
        }
      }
    }

  implicit def bodyOptParamConverter[T](implicit ev: FromRequestUnmarshaller[T]): ConvertibleToDirective[BodyParameter[Option[T]]] =
    (_: String, bp: BodyParameter[Option[T]]) => {
      bp.default match {
        case None => optionalEntity[T](as[T]).map(close(bp))
        case Some(default) => optionalEntity[T](as[T]).map {
          case v@Some(_) => close(bp)(v)
          case None => close(bp)(default)
        }
      }
    }

  def optionalEntity[T](unmarshaller: FromRequestUnmarshaller[T]): Directive1[Option[T]] =
    entity(as[String]).flatMap { stringEntity =>
      if (stringEntity == null || stringEntity.isEmpty) {
        provide(Option.empty[T])
      } else {
        entity(unmarshaller).flatMap(e => provide(Some(e)))
      }
    }

  def formParamConverter1[P1 : FromStringUnmarshaller, T : TypeTag](constructor: (P1) => T): ConvertibleToDirective[FormParameter1[P1, T]] =
    (_: String, fp: FormParameter1[P1, T]) => {
      val fields: Seq[String] = apiModelKeys[T]

      formFields(Symbol(fields(0)).as[P1]).map(f => close(fp)(constructor.apply(f)))
    }

  sealed trait FormFieldDefaults[T] {
    def getOrDefault(fieldName: String, t: Option[T]): T
  }

  implicit def mandatoryFormFieldDefaults[T]: FormFieldDefaults[T] = new FormFieldDefaults[T] {
    override def getOrDefault(fieldName: String, t: Option[T]): T =
      t.getOrElse { throw RejectionError(MissingFormFieldRejection(fieldName)) }
  }

  implicit def optionalFormFieldDefaults[U]: FormFieldDefaults[Option[U]] = new FormFieldDefaults[Option[U]] {
    override def getOrDefault(fieldName: String, t: Option[Option[U]]): Option[U] = t.flatten
  }

  def formParamConverter2[P1: FromStringUnmarshaller, P2: FromStringUnmarshaller, T : TypeTag]
  (constructor: (P1, P2) => T)
  (implicit p1Default: FormFieldDefaults[P1], p2Default: FormFieldDefaults[P2], materializer: ActorMaterializer, ec: ExecutionContext):
  ConvertibleToDirective[FormParameter2[P1, P2, T]] =
    (_: String, fp: FormParameter2[P1, P2, T]) => {

      val fields: Seq[String] = apiModelKeys[T]

      entity(as[FormData]).flatMap((formData: FormData) => {
        val of1: Option[String] = formData.fields.get(fields(0))
        val of2: Option[String] = formData.fields.get(fields(1))

        val xf: Future[Option[P1]] = swap(of1.map(f1 => Unmarshal(f1).to[P1]))
        val yf: Future[Option[P2]] = swap(of2.map(f2 => Unmarshal(f2).to[P2]))

        val marshalling: Future[(Option[P1], Option[P2])] = for {
          x <- xf
          y <- yf
        } yield (x, y)

        onComplete(marshalling).flatMap({
          case Success((op1, op2)) =>
            try {
              provide(close(fp)(constructor.apply(p1Default.getOrDefault(fields(0), op1), p2Default.getOrDefault(fields(1), op2))))
            } catch {
              case RejectionError(rejection) =>
                reject(rejection)
            }
          case Failure(x) =>
            reject(MalformedRequestContentRejection("Error unmarshalling form fields to the types declared.", x))
        })
      })
    }

  private def swap[T](x: Option[Future[T]])(implicit ec: ExecutionContext): Future[Option[T]] =
    x match {
      case Some(f) => f.map(Some(_))
      case None    => Future.successful(None)
    }

  implicit val hNilConverter: ConvertibleToDirective[HNil] =
    (modelPath: String, _: HNil) => {
      if (containsParamToken(modelPath))
        pass.tmap[HNil](_ => HNil)
      else
        path(PathHandling.splittingPathMatcher(modelPath)).tmap[HNil](_ => HNil)
    }

  implicit def hConsConverter[H, T <: HList](implicit head: ConvertibleToDirective[H],
                                             tail: ConvertibleToDirective[T]): ConvertibleToDirective[H :: T] =
    (modelPath: String, l: H :: T) => {
      val headDirective: Directive1[H] = head.convertToDirective(modelPath, l.head)
      val tailDirective: Directive1[T] = tail.convertToDirective(modelPath, l.tail)

      (headDirective & tailDirective).tmap((t: (H, T)) => t._1 :: t._2)
    }

  private def requiredHeaderParamDirective[T](valueParser: String => T):
  ConvertibleToDirective[HeaderParameter[T]] = (_: String, hp: HeaderParameter[T]) => {
    hp.default match {
      case Some(default) => optionalHeaderValueByName(hp.name).map {
        case Some(header) => close(hp)(valueParser(header))
        case None => close(hp)(default)
      }
      case None => headerValueByName(hp.name).map(value => close(hp)(valueParser(value)))
    }
  }

  private def optionalHeaderParamDirective[T](valueParser: String => T):
  ConvertibleToDirective[HeaderParameter[Option[T]]] = (_: String, hp: HeaderParameter[Option[T]]) => {

    hp.default match {
      case Some(default) =>
        optionalHeaderValueByName(hp.name).map {
          case Some(header) => close(hp)(Some(valueParser(header)))
          case None => close(hp)(default)
        }
      case None =>
        optionalHeaderValueByName(hp.name).map {
          case Some(value) => close(hp)(Some(valueParser(value)))
          case None => close(hp)(None)
        }
    }
  }


  private def instance[T](f: T => Directive1[T]): ConvertibleToDirective[T] =
    (_: String, t: T) => f(t)

  private def close[T](qp: QueryParameter[T]): T => QueryParameter[T] =
    t => qp.asInstanceOf[OpenQueryParameter[T]].closeWith(t)

  private def close[T](pp: PathParameter[T]): T => PathParameter[T] =
    t => pp.asInstanceOf[OpenPathParameter[T]].closeWith(t)

  private def close[T](bp: BodyParameter[T]): T => BodyParameter[T] =
    t =>
      bp.asInstanceOf[OpenBodyParameter[T]].closeWith(t)

  private def close[T](hp: HeaderParameter[T]): T => HeaderParameter[T] =
    t => hp.asInstanceOf[OpenHeaderParameter[T]].closeWith(t)

  private def close[P1, T](fp: FormParameter1[P1, T]): T => FormParameter1[P1, T] =
    t => fp.asInstanceOf[OpenFormParameter1[P1, T]].closeWith(t)

  private def close[P1, P2, T](fp: FormParameter2[P1, P2, T]): T => FormParameter2[P1, P2, T] =
    t => fp.asInstanceOf[OpenFormParameter2[P1, P2, T]].closeWith(t)

  private def pathParamDirective[T](pm: PathMatcher1[T]): ConvertibleToDirective[PathParameter[T]] = {
    (modelPath: String, pp: PathParameter[T]) =>
      rawPathPrefixTest(pathWithParamMatcher(modelPath, pp.name.name, pm)).map(close(pp))
  }
}
