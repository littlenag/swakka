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
package net.jtownson.swakka.coreroutegen

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import net.jtownson.swakka.openapimodel.Tuple0
import shapeless.{::, Generic, HList, HNil}

/**
  * RouteGen is a type class that supports the conversion of an OpenApi model into a Akka-Http Route.
  * This allows the processing of an HTTP request according to a Swagger definition.
  * See also ConvertibleToDirective.
  *
  * @tparam T
  */
trait RouteGen[T] {
  def toRoute(t: T): Route
}

object RouteGen {

  implicit def hconsRouteGen[H, T <: HList](
                                             implicit ev1: RouteGen[H],
                                             ev2: RouteGen[T]): RouteGen[H :: T] =
    (l: H :: T) => ev1.toRoute(l.head) ~ ev2.toRoute(l.tail)

  implicit val hNilRouteGen: RouteGen[HNil] =
    (_: HNil) => RouteDirectives.reject

  implicit def genericRouteGen[P, L]
  (implicit gen: Generic.Aux[P, L],
   ev: RouteGen[L]): RouteGen[P] =
    (p: P) => ev.toRoute(gen.to(p))

  implicit val tuple0RouteGen: RouteGen[Tuple0] =
    (_: Tuple0) => RouteDirectives.reject
}