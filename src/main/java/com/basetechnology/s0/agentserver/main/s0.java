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
import com.basetechnology.s0.agentserver.util.ListMap;

public class s0 {
  static final Logger log = Logger.getLogger(s0.class);

  /**
   * Start up the embedded Jetty server for an Agent Server
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {
    // Gather all the "-D" optins to override properties
    ListMap<String, String> commandLineproperties = new ListMap<String, String>();
    for (String arg: args){
      if (arg.startsWith("-D")){
        int i = arg.indexOf('=');
        if (i < 0){
          System.err.println("Missing '=' for -D command line option: " + arg);
          System.exit(1);
        }
        String propertyName = arg.substring(2, i);
        String propertyValue = arg.substring(i + 1);
        commandLineproperties.put(propertyName, propertyValue);
      } else {
        System.err.println("Unknown command line option: " + arg);
        System.exit(1);
      }
    }
    
    // Create a new agent app server
    log.info("Creating new agent app server");
    AgentAppServer server = new AgentAppServer(commandLineproperties);

    // Start it
    log.info("Starting new agent app server");
    server.start();
    log.info("New agent app server started");
  }

}
