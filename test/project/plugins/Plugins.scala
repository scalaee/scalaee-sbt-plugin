/*
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {

  // Module configurations
  def glassfishRepo = "GlassFish Maven Repository" at "http://maven.glassfish.org/content/groups/glassfish"
  val glassfishModuleConfig = ModuleConfiguration("org.glassfish", glassfishRepo)
  val glassfishExtrasModuleConfig = ModuleConfiguration("org.glassfish.extras", glassfishRepo)

  // Dependencies
  val scalaEESBTPlugin = "org.scalaee" % "scalaee-sbt-plugin" % "0.1-SNAPSHOT"
}

