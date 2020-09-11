import mill._
import scalalib._

trait ZioModule extends ScalaModule {

  val zioVersion : String = "1.0.1"
  override def scalaVersion = "2.13.3"

  override def ivyDeps : T[Agg[Dep]] = T { super.ivyDeps() ++ Agg(
    ivy"dev.zio::zio:$zioVersion".withDottyCompat(scalaVersion()),
    ivy"dev.zio::zio-streams:$zioVersion".withDottyCompat(scalaVersion()),
    ivy"org.apache.geronimo.specs:geronimo-jms_1.1_spec:1.1"
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

