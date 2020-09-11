import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`

import mill._
import scalalib._

trait ZioModule extends ScalaModule {
  def scalaVersion = "2.13.3"

  override def ivyDeps : T[Agg[Dep]] = T { super.ivyDeps() ++ Agg(
    ivy"dev.zio::zio:1.0.1".withDottyCompat(scalaVersion())
  )}

  override def scalacOptions = Seq(
    "--deprecation",
    "--target:8",
    "-Werror",
    "--feature",
    Seq(
      "adapted-args",
      "constant",
      "deprecation",
      "doc-detached",
      "inaccessible",
      "infer-any",
      "missing-interpolator",
      "nullary-unit",
      "option-implicit",
      "poly-implicit-overload",
      "stars-align",
      // Compiler doesn't know it but suggests it: "Recompile with -Xlint:unchecked for details."
      // "unchecked",
      "unused",
    ).mkString("-Xlint:", ",", ""),
    //    "--unchecked"
  )}

object zioSamples extends Module {

  object zioBase extends ZioModule {
    override def finalMainClass : T[String] = T { "de.woq.zio.base.FirstZioApp" }
  }

}

