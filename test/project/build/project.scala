/*
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
import org.scalaee.sbtjeeplugin.WebProfileJEEProject
import sbt._

class ScalaEESBTPluginTestProject(info: ProjectInfo) extends DefaultWebProject(info) with WebProfileJEEProject {

  val x = "javax" % "javaee-api" % "6.0" % "provided"

}

