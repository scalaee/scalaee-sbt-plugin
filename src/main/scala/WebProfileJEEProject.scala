/*
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.scalaee.sbtjeeplugin

import sbt._
import Process._


/**
 * Mixin for a JEE web profile project.
 */
trait WebProfileJEEProject extends BasicWebScalaProject {

  /**
   * Deploys an application using the asadmin command delegating to {@link #glassfishAsadminDeployAction}.
   */
  final val glassfishAsadminDeploy = glassfishAsadminDeployAction

  /**
   * Redeploys an application using the asadmin command delegating to {@link #glassfishAsadminRedeployAction}.
   */
  final val glassfishAsadminRedeploy = glassfishAsadminRedeployAction

  /**
   * Undeploys an application using the asadmin command delegating to {@link #glassfishAsadminUndeployAction}.
   */
  final val glassfishAsadminUndeploy = glassfishAsadminUndeployAction

  /**
   * Specifies the location of the asadmin command.
   */
  lazy val glassfishAsadminPath = property[String]

  /**
   * Removes the mainCompilePath and mainResource of all project dependecies and replaces them with jarPath.
   */
  override final def webappClasspath = {
    val mainCompilePaths =
      for {
        p <- dependencies if (p.isInstanceOf[BasicScalaProject])
      } yield p.asInstanceOf[BasicScalaProject].mainCompilePath
    val mainResourcesOutputPaths =
      for {
        p <- dependencies if (p.isInstanceOf[BasicScalaProject])
      } yield p.asInstanceOf[BasicScalaProject].mainResourcesOutputPath
    val remaining = (mainCompilePaths ++ mainResourcesOutputPaths).foldLeft(super.webappClasspath) { _ --- _ }
    val jarPaths =
      for {
        p <- dependencies if (p.isInstanceOf[BasicScalaProject])
      } yield p.asInstanceOf[BasicScalaProject].jarPath
    jarPaths.foldLeft(remaining) { _ +++ _ }
  }

  /**
   * Makes prepareWebapp depend on `package` of all project dependecies.
   */
  override final def prepareWebappAction = {
    val packages = for {
      p <- dependencies if (p.isInstanceOf[BasicScalaProject])
    } yield p.asInstanceOf[BasicScalaProject].`package`
    super.prepareWebappAction dependsOn(packages.toSeq: _*)
  }

  /**
   * Options for the asadmin command for deployment. Attention: Be sure you know what you are doing if you override this!
   */
  protected def glassfishAsadminDeployOptions: List[String] =
    "--force=true" ::
    "--contextroot=" + name ::
    "--name=" + name ::
    Nil

  /**
   * Options for the asadmin command for redeployment. Attention: Be sure you know what you are doing if you override this!
   */
  protected def glassfishAsadminRedeployOptions: List[String] =
    "--properties keepSessions=true" ::
    "--name=" + name ::
    Nil

  /**
   * Options for the asadmin command for undeployment. Attention: Be sure you know what you are doing if you override this!
   */
  protected def glassfishAsadminUndeployOptions: List[String] =
    name ::
    Nil

  /**
   * Deploys an application using the asadmin command. Depends on {@link #prepareWebapp}.
   */
  protected def glassfishAsadminDeployAction =
    task {
      val cmd =
        "%s deploy %s %s".format(
          glassfishAsadminPath.value,
          glassfishAsadminDeployOptions mkString " ",
          temporaryWarPath.absolutePath)
      execute(cmd)
    } dependsOn prepareWebapp describedAs "Deploys an application using the asadmin command."

  /**
   * Redeploys an application using the asadmin command.
   */
  protected def glassfishAsadminRedeployAction =
    task {
      val cmd =
        "%s redeploy %s %s".format(
          glassfishAsadminPath.value,
          glassfishAsadminRedeployOptions mkString " ",
          temporaryWarPath.absolutePath)
      execute(cmd)
    } dependsOn prepareWebapp describedAs "Redeploys an application using the asadmin command."

  /**
   * Undeploys an application using the asadmin command.
   */
  protected def glassfishAsadminUndeployAction =
    task {
      val cmd =
        "%s undeploy %s".format(glassfishAsadminPath.value, glassfishAsadminUndeployOptions mkString " ")
      execute(cmd)
    } describedAs "Undeploys an application using the asadmin command."

  private def execute(cmd: String): Option[String] = {
    log.debug("About to execute the following command: %s" format cmd)
    cmd ! log match {
      case 0 => None
      case n => Some("""Command failed with code "%s".""" format n)
    }
  }

}
