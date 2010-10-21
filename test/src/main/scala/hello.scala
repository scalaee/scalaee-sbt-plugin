/*
 * Copyright (c) 2010 WeigleWilczek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.scalaee.example.hello

import javax.faces.bean.{ ManagedBean, SessionScoped }
import javax.ejb. {LocalBean, EJB, Stateless}

@ManagedBean(name = "helloBean")
@SessionScoped
class HelloBean {

  def getHello = helloEJB.hello

  @EJB
  private var helloEJB: HelloEJB = _
}

@Stateless
@LocalBean // IMPORTANT: Scala introduces a ScalaObject "interface"!
class HelloEJB {

  def hello = "Hello from an EJB!"

}




