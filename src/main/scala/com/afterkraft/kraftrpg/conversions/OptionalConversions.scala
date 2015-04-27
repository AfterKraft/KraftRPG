/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Gabriel Harris-Rouquette
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.afterkraft.kraftrpg.conversions

import scala.language.implicitConversions
import com.google.common.base.{Optional => GuavaOptional}

/**
 * A simplified utility for implicit conversions of [[GuavaOptional]] and [[Option]].
 * This util enables simplifying code and not having to convert between options.
 */
object OptionalConversions {

  /**
   * Converts a [[GuavaOptional]] into an [[Option]] by implicit definition.
   *
   * @param option The guava optional to convert
   * @tparam T The type of optional
   * @return A scala option
   */
  implicit def asOptionalConverter[T](option: GuavaOptional[T]): Option[T]= {
    // Traditionally, a match statement is required, but for the sake of
    // simpler bytecode, use a traditional if else statement.
    if (option.isPresent) Some(option.get()) else None
  }

  /**
   * Converts an [[Option]] into a [[GuavaOptional]] by implicit definition.
   *
   * @param option The option to convert
   * @tparam T The type of option
   * @return The guava optional
   */
  implicit def asOptionConverter[T](option: Option[T]): GuavaOptional[T] = {
    option match {
      case Some(v) => GuavaOptional.of(v)
      case _ => GuavaOptional.absent()
    }
  }

  implicit def jbool2sbool(bool: JBool): scala.Boolean = bool.booleanValue

  implicit def sbool2jbool(bool: Boolean): JBool = bool.asInstanceOf[java.lang.Boolean]

  // Because we can't be bothered to write these generics.
  type ExOpt[A] = GuavaOptional[_ <: A]

  // I just hate importing Optional.
  type Opt[A] = GuavaOptional[A]

  type JBool = java.lang.Boolean

}
