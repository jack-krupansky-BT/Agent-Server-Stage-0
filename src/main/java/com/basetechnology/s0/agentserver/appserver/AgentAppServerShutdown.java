package com.basetechnology.s0.agentserver.appserver;

import org.apache.log4j.Logger;
import com.basetechnology.s0.agentserver.AgentServer;

public class AgentAppServerShutdown implements Runnable {
  static final Logger log = Logger.getLogger(AgentAppServerShutdown.class);

  public AgentServer agentServer;
  
  public AgentAppServerShutdown(AgentServer agentServer){
    this.agentServer = agentServer;
  }
  
  public void run(){
    try {
      // Sleep a little to give Jetty time to pass response back to application
      Thread.sleep(250);
      
      // Stop the agentServer now
      log.info("Stopping agent server");
      agentServer.stop();
      log.info("Agent server stopped");
      
      // Ask Jetty to shut down gracefully in a little bit to give time for response to go out
      log.info("Requesting graceful shutdown of agent app server");
      agentServer.agentAppServer.server.stop();
      log.info("Shutdown in progress");
    } catch (Exception e){
      log.info("Exception in AgentAppServerShutdown: " + e);
      e.printStackTrace();
    }
  }

}
