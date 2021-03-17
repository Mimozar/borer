/*
 * Copyright (c) 2019-2021 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer

import java.lang.{StringBuilder => JStringBuilder}

import io.bullet.borer.cbor._
import io.bullet.borer.internal.Util
import io.bullet.borer.json._

import scala.annotation.unchecked.uncheckedVariance

case object Cbor extends Target:

  /**
    * Entry point into the CBOR encoding mini-DSL.
    */
  def encode[T: Encoder](value: T): EncodingSetup.Api[EncodingConfig] =
    new EncodingSetup.Impl(value, this, EncodingConfig.default, CborValidation.wrapper, CborRenderer)

  /**
    * Entry point into the CBOR decoding mini-DSL.
    */
  def decode[T](value: T)(implicit p: Input.Provider[T]): DecodingSetup.Api[DecodingConfig] =
    new DecodingSetup.Impl[T, p.Bytes, DecodingConfig](
      value,
      DecodingConfig.default,
      CborValidation.wrapper,
      CborParser.creator[p.Bytes, DecodingConfig],
      this)

  /**
    * Constructs a new [[Writer]] that writes CBOR to the given [[Output]].
    */
  def writer(
      output: Output,
      config: EncodingConfig = EncodingConfig.default,
      receiverWrapper: Receiver.Wrapper[EncodingConfig] = CborValidation.wrapper): Writer =
    new Writer(output, receiverWrapper(CborRenderer(output), config), this, config)

  /**
    * Constructs a new [[Reader]] that reads CBOR from the given [[Input]].
    */
  def reader[T](
      value: T,
      config: DecodingConfig = DecodingConfig.default,
      receiverWrapper: Receiver.Wrapper[DecodingConfig] = CborValidation.wrapper)(
      implicit p: Input.Provider[T]): Reader =
    new InputReader(new CborParser(p(value), config)(p.byteAccess), null, receiverWrapper, config, this)

  /**
    * @param bufferSize                  the buffer size used for configuring the respective [[Output]]
    * @param compressFloatingPointValues set to false in order to always write floats as 32-bit values and doubles
    *                                    as 64-bit values, even if they could safely be represented with fewer bits
    * @param maxArrayLength the maximum array length to accept
    * @param maxMapLength the maximum map length to accept
    * @param maxNestingLevels the maximum number of nesting levels to accept
    */
  final case class EncodingConfig(
      bufferSize: Int = 1024,
      allowBufferCaching: Boolean = true,
      compressFloatingPointValues: Boolean = true,
      maxArrayLength: Long = Int.MaxValue,
      maxMapLength: Long = Int.MaxValue,
      maxNestingLevels: Int = 1000)
      extends Borer.EncodingConfig with CborValidation.Config:

    if bufferSize < 8 then throw new IllegalArgumentException(s"bufferSize must be >= 8, but was $bufferSize")

  object EncodingConfig:
    val default = EncodingConfig()

  /**
    * @param readIntegersAlsoAsFloatingPoint set to false to disable automatic conversion of integer to floating point values
    * @param readDoubleAlsoAsFloat set to false to disable automatic conversion of [[Double]] to [[Float]] values
    * @param maxTextStringLength the maximum text string length to accept
    * @param maxByteStringLength the maximum byte string length to accept
    * @param maxArrayLength the maximum array length to accept
    * @param maxMapLength the maximum map length to accept
    * @param maxNestingLevels the maximum number of nesting levels to accept
    */
  final case class DecodingConfig(
      readIntegersAlsoAsFloatingPoint: Boolean = true,
      readDoubleAlsoAsFloat: Boolean = false,
      maxTextStringLength: Int = 1024 * 1024,
      maxByteStringLength: Int = 10 * 1024 * 1024,
      maxArrayLength: Long = Int.MaxValue,
      maxMapLength: Long = Int.MaxValue,
      maxNestingLevels: Int = 1000)
      extends Borer.DecodingConfig with CborValidation.Config with CborParser.Config:

    Util.requireNonNegative(maxTextStringLength, "maxTextStringLength")
    Util.requireNonNegative(maxByteStringLength, "maxByteStringLength")
    Util.requireNonNegative(maxArrayLength, "maxArrayLength")
    Util.requireNonNegative(maxMapLength, "maxMapLength")
    Util.requireNonNegative(maxNestingLevels, "maxNestingLevels")

    if maxMapLength > Long.MaxValue / 2 then
      throw new IllegalArgumentException(s"maxMapLength must be <= ${Long.MaxValue / 2}, but was $maxMapLength")

  object DecodingConfig:
    val default = DecodingConfig()

case object Json extends Target:

  /**
    * Entry point into the JSON encoding mini-DSL.
    */
  def encode[T: Encoder](value: T): EncodingSetup.JsonApi[T, EncodingConfig] =
    new EncodingSetup.Impl(value, Json, EncodingConfig.default, Receiver.nopWrapper, JsonRenderer)

  /**
    * Entry point into the JSON decoding mini-DSL.
    */
  def decode[T](value: T)(implicit p: Input.Provider[T]): DecodingSetup.Api[DecodingConfig] =
    new DecodingSetup.Impl[T, p.Bytes, DecodingConfig](
      value,
      DecodingConfig.default,
      Receiver.nopWrapper,
      JsonParser.creator[p.Bytes, DecodingConfig],
      this)

  /**
    * Constructs a new [[Writer]] that writes JSON to the given [[Output]].
    */
  def writer(
      output: Output,
      config: EncodingConfig = EncodingConfig.default,
      receiverWrapper: Receiver.Wrapper[EncodingConfig] = Receiver.nopWrapper): Writer =
    new Writer(output, receiverWrapper(JsonRenderer(output), config), null, config)

  /**
    * Constructs a new [[Reader]] that reads JSON from the given [[Input]].
    */
  def reader[T](
      value: T,
      config: DecodingConfig = DecodingConfig.default,
      receiverWrapper: Receiver.Wrapper[DecodingConfig] = Receiver.nopWrapper)(
      implicit p: Input.Provider[T]): Reader =
    val directParser = io.bullet.borer.json.DirectParser(value, config)
    val parser       = if directParser ne null then null else new JsonParser(p(value), config)(p.byteAccess)
    new InputReader(parser, directParser, receiverWrapper, config, Json)

  final case class EncodingConfig(
      bufferSize: Int = 1024,
      allowBufferCaching: Boolean = true
  ) extends Borer.EncodingConfig:

    def compressFloatingPointValues = false

    if bufferSize < 8 then throw new IllegalArgumentException(s"bufferSize must be >= 8, but was $bufferSize")

  object EncodingConfig:
    val default = EncodingConfig()

  /**
    * @param readIntegersAlsoAsFloatingPoint set to false to disable automatic conversion of integer to floating point values
    * @param readDecimalNumbersOnlyAsNumberStrings set to true to disable the fast, immediate conversion of
    *                                              JSON numbers to [[Double]] values where easily possible.
    *                                              In rare cases this might be necessary to allow for maximum
    *                                              possible precision when reading 32-bit [[Float]] values from JSON.
    *                                              (see https://github.com/sirthias/borer/issues/20 for more info on this)
    * @param maxNumberAbsExponent the maximum absolute exponent value to accept in JSON numbers
    * @param maxStringLength the maximum string length to accept
    *                        Note: For performance reasons this is a soft limit, that the parser will sometimes overstep.
    *                        The only guarantee is that it will never accept Strings that are more than twice as long as
    *                        the this limit.
    * @param maxNumberMantissaDigits the maximum number of digits to accept before the exponent in JSON numbers
    * @param initialCharbufferSize the initial size of the parser's Char buffer. Will grow to the max string length in
    *                              the document rounded up to the next power of two
    */
  final case class DecodingConfig(
      readIntegersAlsoAsFloatingPoint: Boolean = true,
      readDecimalNumbersOnlyAsNumberStrings: Boolean = false,
      maxNumberAbsExponent: Int = 64,
      maxStringLength: Int = 1024 * 1024,
      maxNumberMantissaDigits: Int = 34,
      initialCharbufferSize: Int = 2048,
      allowBufferCaching: Boolean = true,
      allowDirectParsing: Boolean = true)
      extends Borer.DecodingConfig with JsonParser.Config:

    Util.requireRange(maxNumberAbsExponent, 1, 999, "maxNumberAbsExponent")
    Util.requirePositive(maxStringLength, "maxStringLength")
    Util.requireRange(maxNumberMantissaDigits, 1, 200, "maxNumberMantissaDigits")
    Util.requirePositive(initialCharbufferSize, "initialCharbufferSize")
    if !Util.isPowerOf2(initialCharbufferSize) then
      throw new IllegalArgumentException(
        s"initialCharbufferSize must be a power of two, but was $initialCharbufferSize")

    // the JsonParser never produces Float values directly (only doubles), so this is necessary
    def readDoubleAlsoAsFloat = true

  object DecodingConfig:
    val default = DecodingConfig()

/**
  * Super-type of the [[Cbor]] and [[Json]] objects.
  *
  * Used, for example, as the type of the `target` member of [[Reader]] and [[Writer]] instances,
  * which allows custom logic to pick different (de)serialization approaches
  * depending on whether the target is CBOR or JSON.
  */
sealed abstract class Target:

  def encode[T: Encoder](value: T): EncodingSetup.Api[_]

  def decode[T](input: T)(implicit w: Input.Provider[T]): DecodingSetup.Api[_]

/**
  * Main entry point into the CBOR API.
  */
object Borer:

  sealed abstract class EncodingConfig extends Writer.Config:
    def bufferSize: Int
    def allowBufferCaching: Boolean

  sealed abstract class DecodingConfig extends Reader.Config

  abstract private[borer] class AbstractSetup[Config](defaultConfig: Config, defaultWrapper: Receiver.Wrapper[Config]):
    protected var config: Config                            = defaultConfig
    protected var receiverWrapper: Receiver.Wrapper[Config] = defaultWrapper

    final def withConfig(config: Config): this.type =
      this.config = config
      this

    final def withPrintLogging(
        maxShownByteArrayPrefixLen: Int,
        maxShownStringPrefixLen: Int,
        maxShownArrayElems: Int,
        maxShownMapEntries: Int): this.type =
      withStackedWrapper(
        Logging(
          Logging
            .PrintLogger(maxShownByteArrayPrefixLen, maxShownStringPrefixLen, maxShownArrayElems, maxShownMapEntries)))

    final def withStringLogging(
        stringBuilder: JStringBuilder,
        maxShownByteArrayPrefixLen: Int,
        maxShownStringPrefixLen: Int,
        maxShownArrayElems: Int,
        maxShownMapEntries: Int,
        lineSeparator: String): this.type =
      withStackedWrapper {
        Logging {
          Logging.ToStringLogger(
            stringBuilder,
            maxShownByteArrayPrefixLen,
            maxShownStringPrefixLen,
            maxShownArrayElems,
            maxShownMapEntries,
            lineSeparator)
        }
      }

    final def withWrapper(wrapper: Receiver.Wrapper[Config]): this.type =
      receiverWrapper = wrapper
      this

    final def withStackedWrapper(wrapper: Receiver.Wrapper[Config]): this.type =
      val prevWrapper = receiverWrapper
      receiverWrapper =
        if prevWrapper eq Receiver.nopWrapper[Config] then wrapper
        else (r: Receiver, conf: Config) => wrapper(prevWrapper(r, conf), conf)
      this

  sealed abstract class Error[+IO](private var _io: IO @uncheckedVariance, msg: String, cause: Throwable = null)
      extends RuntimeException(msg, cause):

    final override def getMessage = s"$msg (${_io})"

    final def io: IO = _io

    private[borer] def withPosOf(reader: Reader): Error[Input.Position] =
      val thiz = this.asInstanceOf[Error[Input.Position]]
      if thiz._io.asInstanceOf[AnyRef] eq null then thiz._io = reader.position
      thiz

    private[borer] def withOut(out: Output): Error[Output] =
      val thiz = this.asInstanceOf[Error[Output]]
      if thiz._io eq null then thiz._io = out
      thiz

  object Error:

    final class UnexpectedEndOfInput[IO](io: IO, expected: String)
        extends Error[IO](io, s"Expected $expected but got end of input")

    final class InvalidInputData[IO](io: IO, msg: String) extends Error[IO](io, msg):
      def this(io: IO, expected: String, actual: String) = this(io, s"Expected $expected but got $actual")

    final class ValidationFailure[IO](io: IO, msg: String) extends Error[IO](io, msg)

    final class Unsupported[IO](io: IO, msg: String) extends Error[IO](io, msg)

    final class Overflow[IO](io: IO, msg: String) extends Error[IO](io, msg)

    final class General[IO](io: IO, cause: Throwable) extends Error[IO](io, cause.toString, cause)
