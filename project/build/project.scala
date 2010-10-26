/*
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
import sbt._
import Path._

class ScalaEESBTPluginProject(info: ProjectInfo) extends PluginProject(info) {

  // Module configurations
//  def glassfishRepo = "GlassFish Maven Repository" at "http://maven.glassfish.org/content/groups/glassfish"
//  val glassfishModuleConfig = ModuleConfiguration("org.glassfish", glassfishRepo)
//  val glassfishExtrasModuleConfig = ModuleConfiguration("org.glassfish.extras", glassfishRepo)

  // Dependencies (compile)
//  val glassfishEmbeddedAll = "org.glassfish.extras" % "glassfish-embedded-all" % "3.0.1"

  // Dependencies (test)
  val specs = "org.scala-tools.testing" % "specs" % "1.6.2.1" % "test" withSources
  val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test"

  // Publishing
  override def managedStyle = ManagedStyle.Maven
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
  lazy val publishTo = Resolver.file("Local Test Repository", fileProperty("java.io.tmpdir").asFile)
}
