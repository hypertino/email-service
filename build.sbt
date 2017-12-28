crossScalaVersions := Seq("2.12.4", "2.11.12")

scalaVersion := crossScalaVersions.value.head

lazy val `email-service` = project in file(".") enablePlugins Raml2Hyperbus settings (
    name := "email-service",
    version := "0.4.1-SNAPSHOT",
    organization := "com.hypertino",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("public")
    ),
    libraryDependencies ++= Seq(
      "com.hypertino" %% "hyperbus" % "0.6-SNAPSHOT",
      "com.hypertino" %% "hyperbus-t-inproc" % "0.6-SNAPSHOT" % "test",
      "com.hypertino" %% "service-control" % "0.4.1",
      "com.sun.mail" % "javax.mail" % "1.6.0",
      "com.lihaoyi" %% "scalatags" % "0.6.7",
      "com.google.guava" % "guava" % "19.0",
      "com.hypertino" %% "inflector" % "1.0.6",
      "com.hypertino" %% "service-config" % "0.2.0" % "test",
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test",
      "org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    ),
    ramlHyperbusSources := Seq(
      ramlSource(
        path = "api/email-service-api/email.raml",
        packageName = "com.hypertino.services.email.api",
        isResource = false
      )
    )
)


