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

package net.jtownson.swakka.openapijson

import net.jtownson.swakka.jsonschema.SchemaWriters
import net.jtownson.swakka.openapimodel._
import spray.json._

trait OpenApiJsonProtocol extends
  PathParametersJsonProtocol with
  QueryParametersJsonProtocol with
  HeaderParametersJsonProtocol with
  BodyParametersJsonProtocol with
  HListParametersJsonProtocol with

  ResponsesJsonProtocol with
  PathsJsonProtocol with

  PathParametersConstrainedJsonProtocol with
  HeaderParametersConstrainedJsonProtocol with
  QueryParametersConstrainedJsonProtocol with
  FormFieldParametersConstrainedJsonProtocol with

  HeadersJsonProtocol with
  SecurityDefinitionsJsonProtocol with
  DateTimeJsonProtocol with
  MultiValuedJsonProtocol with
  FormFieldParametersJsonProtocol with
  SchemaWriters with
  DefaultJsonProtocol {

  val contactWriter: JsonWriter[Contact] = (contact: Contact) => {
    val fields: List[(String, JsValue)] = List(
      contact.name.map("name" -> JsString(_)),
      contact.url.map("url" -> JsString(_)),
      contact.email.map("email" -> JsString(_))
    ).flatten

    JsObject(fields: _*)
  }

  val licenceWriter: JsonWriter[License] = (licence: License) => {
    val fields: List[(String, JsValue)] = List(
      Some("name" -> JsString(licence.name)),
      licence.url.map("url" -> JsString(_))).flatten

    JsObject(fields: _*)
  }

  val infoWriter: JsonWriter[Info] = (info: Info) => {

    val fields: List[(String, JsValue)] = List(
      Some("title" -> JsString(info.title)),
      Some("version" -> JsString(info.version)),
      info.description.map("description" -> JsString(_)),
      info.termsOfService.map("termsOfService" -> JsString(_)),
      info.contact.map("contact" -> contactWriter.write(_)),
      info.licence.map("license" -> licenceWriter.write(_))).
      flatten

    JsObject(fields: _*)
  }

  implicit def apiWriterNoSecurityDefs[Paths]
  (implicit ev: PathsJsonFormat[Paths]): RootJsonWriter[OpenApi[Paths, Nothing]] = apiWriter[Paths, Nothing]

  implicit def apiWriter[Paths, SecurityDefinitions]
  (implicit ev: PathsJsonFormat[Paths], ev2: SecurityDefinitionsJsonFormat[SecurityDefinitions]): RootJsonWriter[OpenApi[Paths, SecurityDefinitions]] =
    new RootJsonWriter[OpenApi[Paths, SecurityDefinitions]] {
      override def write(api: OpenApi[Paths, SecurityDefinitions]): JsValue = {

        val paths = ev.write(api.paths)

        val fields = List(
          Some("swagger" -> JsString("2.0")),
          Some("info" -> infoWriter.write(api.info)),
          api.host.map("host" -> JsString(_)),
          api.basePath.map("basePath" -> JsString(_)),
          api.securityDefinitions.map(sd => "securityDefinitions" -> ev2.write(sd)),
          asJsArray("schemes", api.schemes),
          asJsArray("consumes", api.consumes),
          asJsArray("produces", api.produces),
          Some("paths" -> paths)
        ).flatten

        JsObject(fields: _*)
      }
    }

  implicit def apiFormatNoSecurityDefs[Paths]
  (implicit ev: PathsJsonFormat[Paths]): RootJsonFormat[OpenApi[Paths, Nothing]] = apiFormat[Paths, Nothing]

  implicit def apiFormat[Paths, SecurityDefinitions]
  (implicit ev: PathsJsonFormat[Paths], ev2: SecurityDefinitionsJsonFormat[SecurityDefinitions]): RootJsonFormat[OpenApi[Paths, SecurityDefinitions]] =
    lift(apiWriter[Paths, SecurityDefinitions])

  private def asJsArray(key: String, entries: Option[Seq[_]]): Option[(String, JsValue)] = {
    entries.map(s => key -> JsArray(s.map(ss => JsString(ss.toString)): _*))
  }

}

object OpenApiJsonProtocol extends OpenApiJsonProtocol
