/*
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.scalaee.sbtjeeplugin

import org.glassfish.api.deployment.DeployCommandParameters
import sbt._
import java.io.File
import org.glassfish.api.embedded. {EmbeddedFileSystem, ContainerBuilder, Server}


case class DeployedApplication(server: Server,
                               name: String,
                               location: File,
                               params: DeployCommandParameters)


class WebProfileJEEProject(info: ProjectInfo) extends DefaultWebProject(info) {

  private val GlassFishAlreadyStarted = "GlassFish already started!"

  private val GlassFishNotStarted = "GlassFish not started!"

  private var deployedApplicationOption: Option[DeployedApplication] = None

  protected def glassfishContextRoot = name

  protected def glassfishPort = 8080

  protected def glassfishServerID = organization

  protected def glassfishInstallRoot = "glassfishInstance"

  final val glassfishRun = glassfishRunAction

  final val glassfishStop = glassfishStopAction

  final val glassfishRedeploy = glassfishRedeployAction

  System.setProperty("glassfish.embedded.tmpdir", this.outputPath.absolutePath)

  protected def glassfishRunAction =
    task {
      try { 
        log.debug("Trying to start GlassFish ....")

        if (deployedApplicationOption.isDefined) {
          Some(GlassFishAlreadyStarted)
        }
        else {
          val builder = new Server.Builder(glassfishServerID)

          val installDir = new File(outputDirectoryName, glassfishInstallRoot)
          installDir.mkdir()

          val modulesDir = new File(installDir, "modules")
          modulesDir.mkdir() // Required to avoid NPE during startup

          val domainDir = new File(installDir, "domains/domain1")

          val efsb = new EmbeddedFileSystem.Builder()
          efsb.installRoot(installDir)
          efsb.instanceRoot(domainDir)
          val efs = efsb.build()
          builder.embeddedFileSystem(efs)

          val server = builder.build()
          val port = server.createPort(glassfishPort)
          server.addContainer(server.createConfig(ContainerBuilder.Type.web)).bind(port, "http")
          server.addContainer(server.createConfig(ContainerBuilder.Type.ejb))
          server.start()
          log.info("Successfully started GlassFish.")

          log.debug("Deploying project ....")
          val war = new File(temporaryWarPath.absolutePath)
          val params = new DeployCommandParameters
          params.contextroot = glassfishContextRoot
          params.name = glassfishContextRoot
          val name = server.getDeployer.deploy(war, params)
          server.getDeployer.setAutoDeploy(true)
          deployedApplicationOption = Some(DeployedApplication(server, name, war, params))
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
        log.debug("Stopping GlassFish ....")

        if (deployedApplicationOption.isEmpty) {
          Some(GlassFishNotStarted)
        }
        else {
          deployedApplicationOption.get.server.stop()
          deployedApplicationOption = None

          log.info("Successfully stopped GlassFish.")
          None
        }
      } catch {
        case e =>
          Some("Error when trying to stop GlassFish: %s." format e.getMessage)
      }
    } describedAs "Stops GlassFish."

  protected def glassfishRedeployAction =
    task {
      try {
        log.debug("Redeploying application in GlassFish ....")

        if (deployedApplicationOption.isEmpty) {
          Some(GlassFishNotStarted)
        }
        else {
          val s = deployedApplicationOption.get.server
          val n = deployedApplicationOption.get.name
          val l = deployedApplicationOption.get.location
          val p = deployedApplicationOption.get.params
          s.getDeployer.undeploy(n, null)
          s.getDeployer.deploy(l, p)
          log.info("Successfully redeployed application in GlassFish.")
          None
        }
      } catch {
        case e =>
          Some("Error when trying to redeploy application in GlassFish: %s." format e.getMessage)
      }
    } dependsOn prepareWebapp describedAs "Redeploy application in Glassfish."

}
