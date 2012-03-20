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

import com.basetechnology.s0.agentserver.AgentDefinition;
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
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agent_definitions/[a-zA-Z0-9_.@\\-]*$")){
      String userId = pathParts[2];
      String password = request.getParameter("password");
      String agentDefinitionName = pathParts[4];

      if (userId == null)
        throw new AgentAppServerBadRequestException("Missing user Id path parameter");
      if (userId.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty user Id path parameter");
      if (password == null)
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      if (password.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password))
        throw new AgentAppServerBadRequestException("Unknown user name or invalid password");
      if (agentDefinitionName == null)
        throw new AgentAppServerBadRequestException("Missing agent definition name path parameter");
      if (agentDefinitionName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent definition name path parameter");
      if (! agentServer.agentDefinitions.get(userId).containsKey(agentDefinitionName))
        throw new AgentAppServerBadRequestException("No agent definition with that name for that user");

      // Delete the agent definition
      log.info("Deleting agent definition named: " + agentDefinitionName + " for user: " + userId);
      User user = agentServer.getUser(userId);
      AgentDefinition agentDefinition = agentServer.getAgentDefinition(user, agentDefinitionName);
      agentServer.removeAgentDefinition(agentDefinition);

      // Delete was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*$")){
      String userId = pathParts[2];
      String password = request.getParameter("password");
      String agentInstanceName = pathParts[4];

      if (userId == null)
        throw new AgentAppServerBadRequestException("Missing user Id path parameter");
      if (userId.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty user Id path parameter");
      if (password == null)
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      if (password.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password))
        throw new AgentAppServerBadRequestException("Unknown user name or invalid password");
      if (agentInstanceName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentInstanceName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(userId).containsKey(agentInstanceName))
        throw new AgentAppServerBadRequestException("No agent definition with that name for that user");

      User user = agentServer.getUser(userId);

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
