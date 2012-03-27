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

import java.io.BufferedReader;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentInstanceList;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.ScriptDefinition;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.field.Field;
import com.basetechnology.s0.agentserver.field.FieldList;
import com.basetechnology.s0.agentserver.notification.NotificationInstance;
import com.basetechnology.s0.agentserver.scheduler.AgentScheduler;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;

public class HandlePut extends HandleHttp {
  static final Logger log = Logger.getLogger(HandlePut.class);

  static public Thread shutdownThread;
  
  public boolean handlePut(HttpInfo httpInfo) throws Exception {
    // Extract out commonly used info
    this.httpInfo = httpInfo;
    String path = httpInfo.path;
    String[] pathParts = httpInfo.pathParts;
    Request request = httpInfo.request;
    HttpServletResponse response = httpInfo.response;
    AgentServer agentServer = httpInfo.agentServer;
    String lcPath = path.toLowerCase();
    
    if (lcPath.matches("^/agent_definitions/[a-zA-Z0-9_.@\\-]*/[a-zA-Z0-9_.@\\-]$")){
      String userName = pathParts[2];
      String agentDefinitionName = pathParts[3];
      BufferedReader reader = request.getReader();
      JSONObject agentDefinitionJson = getInputJson();

      if (agentDefinitionName == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing agent class name path parameter");
        ((Request)request).setHandled(true);
      } else if (agentDefinitionName.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty agent class name path parameter");
        ((Request)request).setHandled(true);
      } else if (agentDefinitionJson == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Invalid agent class JSON object");
        ((Request)request).setHandled(true);
      } else if (! agentServer.agentDefinitions.containsKey(agentDefinitionName)){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Agent class name does not exist");
        ((Request)request).setHandled(true);
      } else {
        AgentDefinition agentDefinition = agentServer.agentDefinitions.get(userName).get(agentDefinitionName);
        log.info("Updating agent class named: " + agentDefinitionName);

        SymbolManager symbolManager = new SymbolManager();

        // TODO: Process and update changed agent class attributes
        FieldList newParameters = null;
        try {
          if (agentDefinitionJson.has("parameters")){
            newParameters = new FieldList();
            JSONArray parametersJson = agentDefinitionJson.getJSONArray("parameters");
            int numNewParameters = parametersJson.length();
            for (int i = 0; i < numNewParameters; i ++)
              newParameters.add(Field.fromJsonx(symbolManager.getSymbolTable("parameters"), parametersJson.getJSONObject(i)));
          }
        } catch (Exception e){
          e.printStackTrace();
          newParameters = null;
        }
        if (newParameters != null)
          agentDefinition.parameters = newParameters;

        if (agentDefinitionJson.has("description"))
          agentDefinition.description = agentDefinitionJson.optString("description");

        // Update any modified parameter values
        log.info("Updated existing agent class named: " + agentDefinitionName);
        response.setStatus(HttpServletResponse.SC_CREATED);
        // TODO: Set Location header with URL
        ((Request)request).setHandled(true);
      }
    } else if (path.equalsIgnoreCase("/config")){
      checkAdminAccess();
      JSONObject configJson = getInputJson();
      log.info("Updating configuration settings");

      // Update the config settings as requested
      agentServer.config.update(configJson);

      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (path.equalsIgnoreCase("/config/reset")){
      checkAdminAccess();

      log.info("Resetting to original configuration settings");

      // Reset config settings to original defaults
      agentServer.config.restoreDefaults();

      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.equals("/shutdown")){
      checkAdminAccess();

      // Request the agent app server to shutdown
        // TODO: Can we really do this here and still return a response?
        // Or do we need to set a timer, return, and shutdown independent of current request
        
        // Spin up a separate thread to gracefully shutdown the server in a timely manner
        AgentAppServerShutdown agentAppServerShutdown = new AgentAppServerShutdown(agentServer);
        shutdownThread = new Thread(agentAppServerShutdown);
        shutdownThread.start();

        // Done
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return true;
    } else if (lcPath.equals("/status/pause")){
      checkAdminAccess();
      // Request the agent scheduler to pause
      AgentScheduler.singleton.pause();
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.equals("/status/restart")){
      checkAdminAccess();
      // Request the agent scheduler to shutdown
      AgentScheduler.singleton.shutdown();

      // Sleep a little to wait for shutdown to complete
      Thread.sleep(250);

      // Make sure scheduler is no longer running
      if (AgentScheduler.singleton != null)
        // Sleep a little longer to wait for shutdown
        Thread.sleep(250);

      // Force the scheduler to start
      AgentScheduler agentScheduler = new AgentScheduler(agentServer);

      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.equals("/status/resume")){
      checkAdminAccess();
      // Request the agent scheduler to resume
      AgentScheduler.singleton.resume();
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.equals("/status/shutdown")){
      checkAdminAccess();
      // Request the agent scheduler to shutdown
      log.info("Shutting down agent server");
      AgentScheduler.singleton.shutdown();
      log.info("Agent server shut down");
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.equals("/status/start")){
      checkAdminAccess();
      // Make sure scheduler is not already running
      if (AgentScheduler.singleton == null){
        // Force the scheduler to start
        AgentScheduler agentScheduler = new AgentScheduler(agentServer);
      }
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.equals("/status/stop")){
      checkAdminAccess();
      // Request the agent scheduler to shutdown
      log.info("Shutting down agent server");
      AgentScheduler.singleton.shutdown();
      log.info("Agent server shut down");
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*$")){
      User user = checkUserAccess(true);

      // Parse the user info JSON from posted entity
      JSONObject userJson = getInputJson();

      log.info("Updating existing user: " + user.id);

      // Parse the updated user info
      User newUser = User.fromJson(userJson, true);

      // Update the user info
      user.update(agentServer, newUser);

      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/disable$")){
      User user = checkAdminUserAccess();
      String allActivityString = request.getParameter("all_activity");
      boolean disableAllActivity = allActivityString == null || (allActivityString.equalsIgnoreCase("true") ||
          allActivityString.equalsIgnoreCase("yes") ||
          allActivityString.equalsIgnoreCase("on"));
      String newActivityString = request.getParameter("new_activity");
      boolean disableNewActivity = newActivityString == null || (newActivityString.equalsIgnoreCase("true") ||
          newActivityString.equalsIgnoreCase("yes") ||
          newActivityString.equalsIgnoreCase("on"));
      log.info("Disabling user: " + user.id + " diable all activity: " + disableAllActivity +
          " disable new activity: " + disableNewActivity);

      // Disable user as directed
      user.enabled = ! disableAllActivity;
      user.newActivityEnabled = ! disableNewActivity;
      
      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/enable$")){
      User user = checkAdminUserAccess();
      String allActivityString = request.getParameter("all_activity");
      boolean enableAllActivity = allActivityString == null || (allActivityString.equalsIgnoreCase("true") ||
          allActivityString.equalsIgnoreCase("yes") ||
          allActivityString.equalsIgnoreCase("on"));
      String newActivityString = request.getParameter("new_activity");
      boolean enableNewActivity = newActivityString == null || (newActivityString.equalsIgnoreCase("true") ||
          newActivityString.equalsIgnoreCase("yes") ||
          newActivityString.equalsIgnoreCase("on"));
      log.info("Enabling user: " + user.id + " enable new activity: " + enableNewActivity);

      // Enable user as directed
      user.enabled = enableAllActivity;
      user.newActivityEnabled = enableNewActivity;
      
      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agent_definitions/[a-zA-Z0-9_.@\\-]*$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      JSONObject agentJson = getInputJson();

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent definition name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent definition name path parameter");
      if (! agentServer.users.containsKey(user.id))
        throw new AgentAppServerBadRequestException("Unknown user id");

      AgentDefinition agent = agentServer.agentDefinitions.get(user.id).get(agentName);
      log.info("Updating agent definition named: " + agentName + " for user: " + user.id);

      // Parse the updated agent definition info
      AgentDefinition newAgentDefinition = AgentDefinition.fromJson(agentServer, user, agentJson, true);

      // Update the agent definition info
      agent.update(agentServer, newAgentDefinition);

      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      JSONObject agentJson = getInputJson();

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      AgentInstance agent = agentServer.agentInstances.get(user.id).get(agentName);
      String agentDefinitionName = agent.agentDefinition.name;
      log.info("Updating agent instance named: " + agentName + " with definition: " + agentDefinitionName + " for user: " + user.id);

      // Parse the updated agent instance info
      AgentInstance newAgentInstance = AgentInstance.fromJson(agentServer, user, agentJson, agent.agentDefinition, true);

      // Update the agent instance info
      agent.update(agentServer, newAgentInstance);

      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/dismiss_exceptions$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Dismissing exceptions for agent instance " + agentName + " for user: " + user.id);
      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agent = agentMap.get(agentName);

      // Set the time of dismissal for exceptions
      agent.lastDismissedExceptionTime = System.currentTimeMillis();
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/notifications/[a-zA-Z0-9_.@\\-]*$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      String notificationName = pathParts.length >= 7 ? pathParts[6] : null;
      String responseParam = request.getParameter("response");
      String responseChoice = request.getParameter("response_choice");
      String comment = request.getParameter("comment");

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");
      if (notificationName == null)
        throw new AgentAppServerBadRequestException("Missing notification name path parameter");
      if (notificationName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty notification name path parameter");
      if (responseParam == null)
        throw new AgentAppServerBadRequestException("Missing response query parameter");
      if (responseParam.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty response query parameter");
      if (! NotificationInstance.responses.contains(responseParam))
        throw new AgentAppServerBadRequestException("Unknown response keyword query parameter");

      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agent = agentMap.get(agentName);

      NotificationInstance notificationInstance = agent.notifications.get(notificationName);
      if (notificationInstance == null)
        throw new AgentAppServerBadRequestException("Undefined notification name for agent instance '" + agentName + "': " + notificationName);
      if (! notificationInstance.pending)
        throw new AgentAppServerBadRequestException("Cannot respond to notification '" + notificationName + "' for agent instance '" + agentName + "' since it is not pending");
      
      log.info("Respond to a pending notification '" + notificationName + "' for agent instance " + agentName + " for user: " + user.id);

      agent.respondToNotification(notificationInstance, responseParam, responseChoice, comment);
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/(pause|disable)$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Disabling/pausing agent instance " + agentName + " for user: " + user.id);
      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agent = agentMap.get(agentName);

      // Disable/pause the agent
      agent.disable();
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/(resume|enable)$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Enabling/resuming agent instance " + agentName + " for user: " + user.id);
      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agent = agentMap.get(agentName);

      // Enable/resume the agent
      agent.enable();
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/run_script/[a-zA-Z0-9_.@\\-]*$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      String scriptName = pathParts[6];

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");
      if (scriptName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance script name path parameter");
      if (scriptName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance script name path parameter");
      
      ScriptDefinition scriptDefinition =
          agentServer.agentInstances.get(user.id).get(agentName).agentDefinition.scripts.get(scriptName);
      if (scriptDefinition == null)
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "Undefined public agent script for that user: " + scriptName);
      if (! scriptDefinition.publicAccess)
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "Undefined public agent script for that user: " + scriptName);

      log.info("Call a public script for agent instance " + agentName + " for user: " + user.id);
      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agent = agentMap.get(agentName);

      // Call the script
      agent.runScript(scriptName);
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "Path does not address any existing object");
    }
    
    return true;

  }
}
