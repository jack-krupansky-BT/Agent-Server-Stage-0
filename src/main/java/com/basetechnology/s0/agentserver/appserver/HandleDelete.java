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

package com.basetechnology.s0.agentserver.appserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;

import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.User;

public class HandleDelete extends HandleHttp {
  static final Logger log = Logger.getLogger(HandleDelete.class);

  public boolean handleDelete(HttpInfo httpInfo) throws IOException, ServletException, AgentAppServerException, AgentServerException {
    // Extract out commonly used info
    String path = httpInfo.path;
    String[] pathParts = httpInfo.pathParts;
    Request request = httpInfo.request;
    HttpServletResponse response = httpInfo.response;
    String format = httpInfo.format;
    AgentServer agentServer = httpInfo.agentServer;
    String lcPath = path.toLowerCase();
   
    if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*$")){
      String userId = pathParts[2];
      String password = request.getParameter("password");
      if (userId == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing user Id path parameter");
        return true;
      } else if (userId.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty user Id path parameter");
        return true;
      } else if (password == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing password query parameter");
        return true;
      } else if (password.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty password query parameter");
        return true;
      } else if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password)){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Unknown user Id or incorrect password");
        return true;
      } else {
        agentServer.users.remove(userId);
        log.info("Deleted user: " + userId);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        ((Request)request).setHandled(true);
        return true;
      }
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*$")){
      String userId = pathParts[2];
      String agentInstanceName = pathParts[4];

      if (userId == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing user name path parameter");
        ((Request)request).setHandled(true);
      } else if (userId.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty user name path parameter");
        ((Request)request).setHandled(true);
      } else if (agentInstanceName == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing agent name path parameter");
        ((Request)request).setHandled(true);
      } else if (agentInstanceName.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty agent name path parameter");
      }
      User user = agentServer.getUser(userId);
      if (user == null)
        throw new AgentAppServerBadRequestException("No user with id '" + userId + "'");
      AgentInstance agentInstance = agentServer.getAgentInstance(user, agentInstanceName);
      if (agentInstance == null)
        throw new AgentAppServerBadRequestException("No agent instance named '" + agentInstanceName + " for user '" + userId + "'");
      log.info("Deleting agent instance named: " + agentInstanceName + " of agent definition named " + agentInstance.agentDefinition.name + " for user: " + userId);

      // Delete the instance
      agentServer.removeAgentInstance(agentInstance);

      // Delete was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "Path does not address any existing object");
    }

    return true;
  }
}
