/*
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.scalaee.sbtjeeplugin

import java.io.File
import org.glassfish.api.deployment.DeployCommandParameters
import org.glassfish.api.embedded.{ EmbeddedFileSystem, ContainerBuilder, Server }
import sbt._
import Process._

case class DeployedApplication(
  server: Server,
  name: String,
  location: File,
  params: DeployCommandParameters)

class WebProfileJEEProject(info: ProjectInfo) extends DefaultWebProject(info) {

  final val glassfishRun = glassfishRunAction

  final val glassfishStop = glassfishStopAction

  final val glassfishRedeploy = glassfishRedeployAction

  final val glassfishAsadminDeploy = glassfishAsadminDeployAction

  protected def glassfishContextRoot = name

  protected def glassfishPort = 8080

  protected def glassfishServerID = organization

  protected def glassfishInstallDir = outputDirectoryName / "glassfishInstance"

  protected def glassfishDomainDir = glassfishInstallDir / "domains" / "domain1"

  protected def glassfishAsadmin: Option[String] = None

  protected def glassfishAsadminOptions = List(
    "--force=true",
    "--contextroot=" + glassfishContextRoot,
    "--name=" + name)

  protected def glassfishRunAction =
    task {
      try {
        log.debug("Trying to start GlassFish ....")

        if (deployedApplicationOption.isDefined) {
          Some(GlassFishAlreadyStarted)
        }
        else {
          val builder = new Server.Builder(glassfishServerID)
          val efsb = new EmbeddedFileSystem.Builder()

          val installDir = glassfishInstallDir.asFile
          installDir.mkdir()
          efsb.installRoot(installDir)

          val modulesDir = new File(installDir, "modules")
          modulesDir.mkdir() // Required to avoid NPE during startup

          val domainDir = glassfishDomainDir.asFile
          efsb.instanceRoot(domainDir)

          val efs = efsb.build()
          builder.embeddedFileSystem(efs)

          val server = builder.build()
          val port = server createPort glassfishPort
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
        case e => {
          e.printStackTrace()
          Some("Error when trying to start GlassFish: %s." format e.getMessage)
        }
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

  protected def glassfishAsadminDeployAction =
    task {
      glassfishAsadmin match {
        case Some(asadmin) => {
          val cmd =
            glassfishAsadmin.get +
            " deploy " +
            glassfishAsadminOptions.mkString(" ") +
            " " +
            temporaryWarPath.absolutePath
          cmd ! log
          log.info("Deployed application with: '" + cmd + "'" )
          None
        }
        case None => {
          Some("Specify the asadmin command location with the 'glassfishAsadmin' setting.")
        }
      }
    } dependsOn prepareWebapp describedAs "Deploy application using the asadmin command."

  private val GlassFishAlreadyStarted = "GlassFish already started!"

  private val GlassFishNotStarted = "GlassFish not started!"

  private var deployedApplicationOption: Option[DeployedApplication] = None
}
