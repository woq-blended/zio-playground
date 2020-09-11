package de.woq.zio.base

import zio._
import zio.console.Console

object FirstZioApp extends App {

  val program : ZIO[Console, Nothing, Unit] =
    for {
      _ <- console.putStrLn("Hello Andreas")
      _ <- console.putStrLn("Dies ist ein Test")
    } yield()

  def run(args: List[String]) =
    program.exitCode

}
