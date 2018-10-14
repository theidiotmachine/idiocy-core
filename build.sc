// build.sc
import mill._, scalalib._

object audio_core extends ScalaModule {
  def scalaVersion = "2.12.6"
  def scalacOptions = Seq("-feature", "-deprecation")
  def ivyDeps = Agg(
    ivy"com.lihaoyi::upickle:0.6.6"
  )

  object test extends Tests{
    def ivyDeps = Agg(ivy"com.lihaoyi::utest:0.6.0")
    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object gui extends ScalaModule {
  def moduleDeps = Seq(audio_core)
  def scalaVersion = "2.12.6"
  def scalacOptions = Seq("-feature", "-deprecation")
  def ivyDeps = Agg(
    ivy"org.scala-lang.modules::scala-xml:1.1.0",
    ivy"com.lihaoyi::upickle:0.6.6"
  )
}
