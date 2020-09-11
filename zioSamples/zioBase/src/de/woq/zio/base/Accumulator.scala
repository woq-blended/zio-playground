package de.woq.zio.base

import zio._

trait Counter[T <: Numeric[_]] {
  def increment : ZIO[Any, Nothing, T]
  def decrement : ZIO[Any, Nothing, T]
}

object Counter {
}