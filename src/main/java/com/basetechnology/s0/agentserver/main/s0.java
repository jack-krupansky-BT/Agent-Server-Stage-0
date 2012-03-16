/**
 * Copyright 2012 John W. Krupansky d/b/a Base Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.basetechnology.s0.agentserver.main;

import org.apache.log4j.Logger;

import com.basetechnology.s0.agentserver.appserver.AgentAppServer;

public class s0 {
  static final Logger log = Logger.getLogger(s0.class);

  /**
   * A little test embedded Jetty server for Agent Server
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {
    AgentAppServer server = new AgentAppServer();

    String userName = "joe";
    log.info("Adding dummy user: " + userName);
    server.agentServer.addUser(userName);

    server.start();
    
    log.info("End of Main");
  }

}
