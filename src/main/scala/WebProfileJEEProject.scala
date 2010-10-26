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
 * JEE web profile project.
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
  protected def glassfishAsadmin: String

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
          glassfishAsadmin,
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
          glassfishAsadmin,
          glassfishAsadminRedeployOptions mkString " ",
          temporaryWarPath.absolutePath)
      execute(cmd)
    } describedAs "Redeploys an application using the asadmin command."

  /**
   * Undeploys an application using the asadmin command.
   */
  protected def glassfishAsadminUndeployAction =
    task {
      val cmd =
        "%s undeploy %s".format(glassfishAsadmin, glassfishAsadminUndeployOptions mkString " ")
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
