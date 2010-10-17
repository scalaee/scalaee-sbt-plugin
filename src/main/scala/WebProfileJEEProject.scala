/*
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.scalaee.sbtjeeplugin

import org.glassfish.api.deployment.DeployCommandParameters
import org.glassfish.api.embedded.{ ContainerBuilder, Server }
import sbt._

object WebProfileJEEProject {

  private val GlassFishAlreadyStarted = "GlassFish already started!"

  private val GlassFishNotStarted = "GlassFish not started!"
}

class WebProfileJEEProject(info: ProjectInfo) extends DefaultWebProject(info) {
 import WebProfileJEEProject._

  System.setProperty("glassfish.embedded.tmpdir", this.outputPath.absolutePath)

  final val glassfishRun = glassfishRunAction

  final val glassfishStop = glassfishStopAction

  protected def glassfishRunAction =
    task {
      try {
        log.debug("Trying to start GlassFish ....")

        if (serverOption.isDefined) {
          Some(GlassFishAlreadyStarted)
        }
        else {
          val server = new Server.Builder(glassfishServerID).build
          serverOption = Some(server)
          server.createPort(glassfishPort)
          server.addContainer(server createConfig ContainerBuilder.Type.web)
          server.addContainer(server createConfig ContainerBuilder.Type.ejb)
          server.start()
          log.info("Successfully started GlassFish.")

          log.debug("Trying to deploy project ....")
          val war = new java.io.File(temporaryWarPath.absolutePath)
          val params = new DeployCommandParameters
          params.contextroot = glassfishContextRoot
          server.getDeployer.deploy(war, params)
          log.info("Successfully deployed project under context root: %s" format params.contextroot)

          None
        }
      } catch {
        case e =>
          Some("Error when trying to start GlassFish: %s." format e.getMessage)
      }
    } dependsOn prepareWebapp describedAs "Starts GlassFish."

  protected def glassfishStopAction =
    task {
      try {
        log.debug("Trying to stop GlassFish ....")

        if (serverOption.isEmpty) {
          Some(GlassFishNotStarted)
        }
        else {
          serverOption.get.stop
          serverOption = None
          log.info("Successfully stopped GlassFish.")
          None
        }
      } catch {
        case e =>
          Some("Error when trying to stop GlassFish: %s." format e.getMessage)
      }
    } describedAs "Stops GlassFish."

  protected def glassfishContextRoot = name

  protected def glassfishPort = 8080

  protected def glassfishServerID = organization

  private var serverOption: Option[Server] = None
}
