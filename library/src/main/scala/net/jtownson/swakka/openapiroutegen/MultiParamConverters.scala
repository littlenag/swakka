/*
 * Copyright 2017 Jeremy Townson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jtownson.swakka.openapiroutegen

import akka.http.scaladsl.server.{
  Directive1,
  Rejection,
  ValidationRejection
}
import akka.http.scaladsl.server.Directives.{onComplete, provide, reject}
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshal}
import akka.stream.Materializer
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapiroutegen.ParamValidator._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait MultiParamConverters {

  type MultiParamConverter[T, P <: Parameter[T]] =
    ConvertibleToDirective.Aux[MultiValued[T, P], Seq[T]]

  trait ConstraintShim[T, U, P <: Parameter[T]] {
    def constraints(param: P): Option[Constraints[U]]
  }

  object ConstraintShim {
    def instance[T, U, P <: Parameter[T]](
        f: Parameter[T] => Option[Constraints[U]]) =
      new ConstraintShim[T, U, P] {
        override def constraints(param: P) = f(param)
      }

    implicit def qpch[T]: ConstraintShim[T, T, QueryParameter[T]] = qp => None
    implicit def qpcch[T]: ConstraintShim[T, T, QueryParameterConstrained[T, T]] = qp => Some(qp.constraints)

  }

  implicit val sv: ParamValidator[String, String] = stringValidator
  implicit val osv: ParamValidator[Option[String], String] = optionValidator(sv)
  implicit val ssv: ParamValidator[Seq[String], String] = sequenceValidator(sv)

  implicit def multiValuedConverter[T, P <: Parameter[T], U](
      implicit um: FromStringUnmarshaller[T],
      mat: Materializer,
      ec: ExecutionContext,
      validator: ParamValidator[Seq[T], U],
      ch: ConstraintShim[T, U, P]): MultiParamConverter[T, P] =
    instance((_: String, mp: MultiValued[T, P]) => {

      val marshalledParams: Directive1[Try[Seq[T]]] =
        queryParamsWithName(mp.name.name)
          .map(params =>
            Future.sequence(params.map(param => Unmarshal(param).to[T])))
          .flatMap(marshalledParams => onComplete(marshalledParams))

      marshalledParams.flatMap({
        case Success(Nil) =>
          mp.singleParam.default match {
            case Some(default) =>
              provideWithCheck(Seq(default), mp, validator, ch)
            case _ =>
              mp.default match {
                case Some(defaultSeq) =>
                  provideWithCheck(defaultSeq, mp, validator, ch)
                case _ => provideWithCheck(Nil, mp, validator, ch)
              }
          }
        case Success(seq) => provideWithCheck(seq, mp, validator, ch)
        case Failure(t) =>
          reject(ValidationRejection(
            s"Failed to marshal multivalued parameter ${mp.name.name}. The following error occurred: $t",
            Some(t)))
      })
    })

  private def provideWithCheck[T, P <: Parameter[T], U](
      values: Seq[T],
      p: MultiValued[T, P],
      validator: ParamValidator[Seq[T], U],
      constraintHack: ConstraintShim[T, U, P]): Directive1[Seq[T]] = {

    val maybeConstraints: Option[Constraints[U]] =
      constraintHack.constraints(p.singleParam)
    val successDefault = Right(values)

    val validationResult: Either[String, Seq[T]] = maybeConstraints
      .map(c => validator.validate(c, values))
      .getOrElse(successDefault)

    validationResult.fold(
      errors => reject(validationRejection(values, p, errors)),
      validatedValues => provide(validatedValues))
  }

  private def validationRejection[T, P <: Parameter[T]](
      s: Seq[T],
      p: MultiValued[T, P],
      errMsg: String): Rejection =
    ValidationRejection(
      s"The value $s is not allowed by this request for parameter ${p.name.name}. $errMsg.")

  private def queryParamsWithName(name: String): Directive1[Seq[String]] =
    extract(_.request.uri.query().toSeq)
      .map(_.filter({ case (key, _) => name == key }).map(_._2))
      .flatMap(params => provide(params))

}
