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

import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString
import net.jtownson.swakka.openapijson.ParameterTemplates.{
  constrainedParam,
  defaultOf
}
import net.jtownson.swakka.openapimodel._
import spray.json.{JsArray, JsBoolean, JsNumber, JsString}

trait FormFieldParametersConstrainedJsonProtocol {

  implicit val requiredStringFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[String, String]] =
    (fdp: FormFieldParameterConstrained[String, String]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = true,
        `type` = "string",
        format = None,
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsString(_)).toVector)),
        minLength = fdp.constraints.minLength.map(JsNumber(_)),
        maxLength = fdp.constraints.maxLength.map(JsNumber(_)),
        pattern = fdp.constraints.pattern.map(JsString(_))
    )

  implicit val requiredBooleanFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[Boolean, Boolean]] =
    (fdp: FormFieldParameterConstrained[Boolean, Boolean]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = true,
        `type` = "boolean",
        format = None,
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsBoolean(_)).toVector)),
    )

  implicit val requiredIntFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[Int, Int]] =
    (fdp: FormFieldParameterConstrained[Int, Int]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = true,
        `type` = "integer",
        format = Some("int32"),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = fdp.constraints.multipleOf.map(JsNumber(_)),
        maximum = fdp.constraints.maximum.map(JsNumber(_)),
        minimum = fdp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = fdp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = fdp.constraints.exclusiveMinimum.map(JsNumber(_))
    )

  implicit val requiredLongFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[Long, Long]] =
    (fdp: FormFieldParameterConstrained[Long, Long]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = true,
        `type` = "integer",
        format = Some("int64"),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = fdp.constraints.multipleOf.map(JsNumber(_)),
        maximum = fdp.constraints.maximum.map(JsNumber(_)),
        minimum = fdp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = fdp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = fdp.constraints.exclusiveMinimum.map(JsNumber(_))
    )

  implicit val requiredFloatFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[Float, Float]] =
    (fdp: FormFieldParameterConstrained[Float, Float]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = true,
        `type` = "number",
        format = Some("float"),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsNumber(_)).toVector)),
        maximum = fdp.constraints.maximum.map(JsNumber(_)),
        minimum = fdp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = fdp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = fdp.constraints.exclusiveMinimum.map(JsNumber(_))
    )

  implicit val requiredDoubleFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[Double, Double]] =
    (fdp: FormFieldParameterConstrained[Double, Double]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = true,
        `type` = "number",
        format = Some("double"),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsNumber(_)).toVector)),
        maximum = fdp.constraints.maximum.map(JsNumber(_)),
        minimum = fdp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = fdp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = fdp.constraints.exclusiveMinimum.map(JsNumber(_))
    )

  type FileData = (FileInfo, Source[ByteString, Any])

  implicit val requiredFileFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[FileData, FileData]] =
    (_: FormFieldParameterConstrained[FileData, FileData]) => ???

  implicit val optionalStringFormDataParamFormatConstrained
    : ParameterJsonFormat[
      FormFieldParameterConstrained[Option[String], String]] =
    (fdp: FormFieldParameterConstrained[Option[String], String]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = false,
        `type` = "string",
        format = None,
        default = defaultOf(fdp),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsString(_)).toVector)),
        minLength = fdp.constraints.minLength.map(JsNumber(_)),
        maxLength = fdp.constraints.maxLength.map(JsNumber(_)),
        pattern = fdp.constraints.pattern.map(JsString(_))
    )

  implicit val optionalBooleanFormDataParamFormatConstrained
    : ParameterJsonFormat[
      FormFieldParameterConstrained[Option[Boolean], Boolean]] =
    (fdp: FormFieldParameterConstrained[Option[Boolean], Boolean]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = false,
        `type` = "boolean",
        format = None,
        default = defaultOf(fdp),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsBoolean(_)).toVector)),
    )

  implicit val optionalIntFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[Option[Int], Int]] =
    (fdp: FormFieldParameterConstrained[Option[Int], Int]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = false,
        `type` = "integer",
        format = Some("int32"),
        default = defaultOf(fdp),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = fdp.constraints.multipleOf.map(JsNumber(_)),
        maximum = fdp.constraints.maximum.map(JsNumber(_)),
        minimum = fdp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = fdp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = fdp.constraints.exclusiveMinimum.map(JsNumber(_))
    )

  implicit val optionalLongFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[Option[Long], Long]] =
    (fdp: FormFieldParameterConstrained[Option[Long], Long]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = false,
        `type` = "integer",
        format = Some("int64"),
        default = defaultOf(fdp),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = fdp.constraints.multipleOf.map(JsNumber(_)),
        maximum = fdp.constraints.maximum.map(JsNumber(_)),
        minimum = fdp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = fdp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = fdp.constraints.exclusiveMinimum.map(JsNumber(_))
    )

  implicit val optionalFloatFormDataParamFormatConstrained
    : ParameterJsonFormat[FormFieldParameterConstrained[Option[Float], Float]] =
    (fdp: FormFieldParameterConstrained[Option[Float], Float]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = false,
        `type` = "number",
        format = Some("float"),
        default = defaultOf(fdp),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsNumber(_)).toVector)),
        maximum = fdp.constraints.maximum.map(JsNumber(_)),
        minimum = fdp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = fdp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = fdp.constraints.exclusiveMinimum.map(JsNumber(_))
    )

  implicit val optionalDoubleFormDataParamFormatConstrained
    : ParameterJsonFormat[
      FormFieldParameterConstrained[Option[Double], Double]] =
    (fdp: FormFieldParameterConstrained[Option[Double], Double]) =>
      constrainedParam(
        in = "formData",
        name = fdp.name,
        description = fdp.description,
        required = false,
        `type` = "number",
        format = Some("double"),
        default = defaultOf(fdp),
        enum = fdp.constraints.enum.map(set =>
          JsArray(set.map(JsNumber(_)).toVector)),
        maximum = fdp.constraints.maximum.map(JsNumber(_)),
        minimum = fdp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = fdp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = fdp.constraints.exclusiveMinimum.map(JsNumber(_))
    )

  implicit val optionalFileFormDataParamFormatConstrained: ParameterJsonFormat[
    FormFieldParameterConstrained[Option[FileData], FileData]] =
    (_: FormFieldParameterConstrained[Option[FileData], FileData]) => ???

}

object FormFieldParametersConstrainedJsonProtocol
    extends FormFieldParametersConstrainedJsonProtocol
