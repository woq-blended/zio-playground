package de.woq.zio.base

import zio._
import zio.console._

object FirstZioApp extends App {

  def times2(i : Int) : UIO[Int] = IO.succeed(i).map(_ * 2)

  val program : ZIO[Console, Nothing, Unit] =
    for {
      _ <- putStrLn("Hallo Andreas")
      v <- times2(21)
      _ <- putStrLn(s"Dies ist ein Test -- $v")
    } yield()

  def run(args: List[String]) =
    program.exitCode

}
