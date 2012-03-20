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
import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentInstanceList;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.ScriptDefinition;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.activities.AgentActivityThread;
import com.basetechnology.s0.agentserver.field.Field;
import com.basetechnology.s0.agentserver.field.FieldList;
import com.basetechnology.s0.agentserver.notification.NotificationInstance;
import com.basetechnology.s0.agentserver.scheduler.AgentScheduler;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonListMap;

public class HandlePut extends HandleHttp {
  static final Logger log = Logger.getLogger(HandlePut.class);

  static public Thread shutdownThread;
  
  public boolean handlePut(HttpInfo httpInfo) throws Exception {
    this.httpInfo = httpInfo;
    
    // Extract out commonly used info
    String path = httpInfo.path;
    String[] pathParts = httpInfo.pathParts;
    Request request = httpInfo.request;
    HttpServletResponse response = httpInfo.response;
    String format = httpInfo.format;
    AgentServer agentServer = httpInfo.agentServer;
    String lcPath = path.toLowerCase();
    
    if (lcPath.matches("^/agent_definitions/[a-zA-Z0-9_.@\\-]*/[a-zA-Z0-9_.@\\-]$")){
      String userName = pathParts[2];
      String agentDefinitionName = pathParts[3];
      BufferedReader reader = request.getReader();
      JSONObject agentDefinitionJson = null;
      try {
        agentDefinitionJson = new JSONObject(new JSONTokener(reader));
      } catch (Exception e){}

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
      // Get password from query parameters
      String password = request.getParameter("password");
      if (password == null){
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      } else if (password.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      } else if (! password.equals(agentServer.getAdminPassword())){
        throw new AgentAppServerBadRequestException("Incorrect password");
      }

      JSONObject configJson = getInputJson();
      log.info("Updating configuration settings");

      // Update the config settings as requested
      agentServer.config.update(configJson);

      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (path.equalsIgnoreCase("/config/reset")){
      // Get password from query parameters
      String password = request.getParameter("password");
      if (password == null){
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      } else if (password.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      } else if (! password.equals(agentServer.getAdminPassword())){
        throw new AgentAppServerBadRequestException("Incorrect password");
      }

      log.info("Resetting to original configuration settings");

      // Reset config settings to original defaults
      agentServer.config.restoreDefaults();

      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.equals("/shutdown")){
      // Get password from query parameters
      String password = request.getParameter("password");
      if (password == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing password query parameter");
        ((Request)request).setHandled(true);
      } else if (password.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty password query parameter");
        ((Request)request).setHandled(true);
      } else if (! password.equals(agentServer.getAdminPassword())){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Incorrect password");
        return true;
      } else {
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
      }
    } else if (lcPath.equals("/status/pause")){
      // Get password from query parameters
      String password = request.getParameter("password");
      if (password == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing password query parameter");
        ((Request)request).setHandled(true);
      } else if (password.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty password query parameter");
        ((Request)request).setHandled(true);
      } else if (! password.equals(agentServer.getAdminPassword())){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Incorrect password");
        return true;
      } else {
        // Request the agent scheduler to pause
        AgentScheduler.singleton.pause();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return true;
      }
    } else if (lcPath.equals("/status/restart")){
      // Get password from query parameters
      String password = request.getParameter("password");
      if (password == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing password query parameter");
        ((Request)request).setHandled(true);
      } else if (password.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty password query parameter");
        ((Request)request).setHandled(true);
      } else if (! password.equals(agentServer.getAdminPassword())){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Incorrect password");
        return true;
      } else {
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
        return true;
      }
    } else if (lcPath.equals("/status/resume")){
      // Get password from query parameters
      String password = request.getParameter("password");
      if (password == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing password query parameter");
        ((Request)request).setHandled(true);
      } else if (password.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty password query parameter");
        ((Request)request).setHandled(true);
      } else if (! password.equals(agentServer.getAdminPassword())){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Incorrect password");
        return true;
      } else {
        // Request the agent scheduler to resume
        AgentScheduler.singleton.resume();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return true;
      }
    } else if (lcPath.equals("/status/shutdown")){
      // Get password from query parameters
      String password = request.getParameter("password");
      if (password == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing password query parameter");
        ((Request)request).setHandled(true);
      } else if (password.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty password query parameter");
        ((Request)request).setHandled(true);
      } else if (! password.equals(agentServer.getAdminPassword())){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Incorrect password");
        return true;
      } else {
        // Request the agent scheduler to shutdown
        log.info("Shutting down agent server");
        AgentScheduler.singleton.shutdown();
        log.info("Agent server shut down");
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return true;
      }
    } else if (lcPath.equals("/status/start")){
      // Get password from query parameters
      String password = request.getParameter("password");
      if (password == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing password query parameter");
        ((Request)request).setHandled(true);
      } else if (password.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty password query parameter");
        ((Request)request).setHandled(true);
      } else if (! password.equals(agentServer.getAdminPassword())){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Incorrect password");
        return true;
      } else {
        // Make sure scheduler is not already running
        if (AgentScheduler.singleton == null){
          // Force the scheduler to start
          AgentScheduler agentScheduler = new AgentScheduler(agentServer);
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return true;
      }
    } else if (lcPath.equals("/status/stop")){
      // Get password from query parameters
      String password = request.getParameter("password");
      if (password == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing password query parameter");
        ((Request)request).setHandled(true);
      } else if (password.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty password query parameter");
        ((Request)request).setHandled(true);
      } else if (! password.equals(agentServer.getAdminPassword())){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Incorrect password");
        return true;
      } else {
        // Request the agent scheduler to shutdown
        log.info("Shutting down agent server");
        AgentScheduler.singleton.shutdown();
        log.info("Agent server shut down");
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return true;
      }
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*$")){
      // Get user id from path
      String id = pathParts[2];
      
      // Get password from query parameters
      String password = request.getParameter("password");

      // Parse the user info JSON from posted entity
      BufferedReader reader = request.getReader();
      if (reader == null)
        throw new AgentAppServerBadRequestException("Missing JSON in request");
      JSONObject userJson = null;
      try {
        userJson = new JSONObject(new JSONTokener(reader));
      } catch (Exception e){
        throw new AgentAppServerBadRequestException("Exception parsing JSON - " + e.getMessage());
      }

      if (id == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing id query parameter");
        ((Request)request).setHandled(true);
      } else if (id.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty id query parameter");
        ((Request)request).setHandled(true);
      } else if (id.trim().length() < User.MIN_ID_LENGTH){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Id must be at least 4 characters");
        ((Request)request).setHandled(true);
      } else if (password == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing password query parameter");
        ((Request)request).setHandled(true);
      } else if (password.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty password query parameter");
        ((Request)request).setHandled(true);
      } else if (password.trim().length() < User.MIN_ID_LENGTH){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Password must be at least 4 characters");
        ((Request)request).setHandled(true);
      } else if (! agentServer.users.containsKey(id) ||
          ! agentServer.users.get(id).password.equals(password)){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Unknown user Id or incorrect password");
        return true;
      }
      if (userJson == null)
        throw new AgentAppServerBadRequestException("Missing JSON");

        id = id.trim();
        password = password.trim();
        log.info("Updating existing user: " + id);
        
        // Get the existing user info
        User user = agentServer.users.get(id);
        
        // Parse the updated user info
        User newUser = User.fromJson(userJson, true);
        
        // Update the user info
        user.update(agentServer, newUser);
        
        // Update was successful
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agent_definitions/[a-zA-Z0-9_.@\\-]*$")){
      String userId = pathParts[2];
      String agentName = pathParts[4];
      BufferedReader reader = request.getReader();
      JSONObject agentJson = new JSONObject(new JSONTokener(reader));

      if (userId == null)
        throw new AgentAppServerBadRequestException("Missing user Id path parameter");
      else if (userId.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty user Id path parameter");
      else if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent definition name path parameter");
      else if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent definition name path parameter");
      else if (! agentServer.users.containsKey(userId))
        throw new AgentAppServerBadRequestException("Unknown user id");

      User user = agentServer.getUser(userId);
      AgentDefinition agent = agentServer.agentDefinitions.get(userId).get(agentName);
      log.info("Updating agent definition named: " + agentName + " for user: " + userId);

      // Parse the updated agent definition info
      AgentDefinition newAgentDefinition = AgentDefinition.fromJson(agentServer, user, agentJson, true);

      // Update the agent definition info
      agent.update(agentServer, newAgentDefinition);

      // Update was successful
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*$")){
      String userId = pathParts[2];
      String agentName = pathParts[4];
      BufferedReader reader = request.getReader();
      JSONObject agentJson = null;
      try {
        agentJson = new JSONObject(new JSONTokener(reader));
      } catch (Exception e){}

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
      } else if (agentName == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Missing agent name path parameter");
        ((Request)request).setHandled(true);
      } else if (agentName.trim().length() == 0){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Empty agent name path parameter");
        ((Request)request).setHandled(true);
      } else if (! agentServer.users.containsKey(userId)){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Unknown user name");
        ((Request)request).setHandled(true);
      } else if (agentJson == null){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().println("<title>Agent Server</title>");
        response.getWriter().println("<h1>Bad Request</h1>");
        response.getWriter().println("Invalid agent JSON object");
        ((Request)request).setHandled(true);
      } else {
        User user = agentServer.getUser(userId);
        AgentInstance agent = agentServer.agentInstances.get(userId).get(agentName);
        String agentDefinitionName = agent.agentDefinition.name;
        log.info("Updating agent instance named: " + agentName + " with class: " + agentDefinitionName + " for user: " + userId);

        // Parse the updated agent instance info
        AgentInstance newAgentInstance = AgentInstance.fromJson(agentServer, user, agentJson, agent.agentDefinition, true);
        
        // Update the agent instance info
        agent.update(agentServer, newAgentInstance);
        
        // Update was successful
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      }
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/dismiss_exceptions$")){
      String userId = pathParts[2];
      String agentName = pathParts[4];
      String password = request.getParameter("password");
      if (userId == null)
        throw new AgentAppServerBadRequestException("Missing user name path parameter");
      else if (userId.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty user name path parameter");
      else if (password == null)
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      else if (password.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      else if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password))
        throw new AgentAppServerBadRequestException("Unknown user name or invalid password");
      else if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      else if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      else if (! agentServer.agentInstances.get(userId).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Dismissing exceptions for agent instance " + agentName + " for user: " + userId);
      AgentInstanceList agentMap = agentServer.agentInstances.get(userId);
      AgentInstance agent = agentMap.get(agentName);

      // Set the time of dismissal for exceptions
      agent.lastDismissedExceptionTime = System.currentTimeMillis();
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/notifications/[a-zA-Z0-9_.@\\-]*$")){
      String userId = pathParts[2];
      String agentName = pathParts[4];
      String notificationName = pathParts.length >= 7 ? pathParts[6] : null;
      String password = request.getParameter("password");
      String responseParam = request.getParameter("response");
      String responseChoice = request.getParameter("response_choice");
      String comment = request.getParameter("comment");
      if (userId == null)
        throw new AgentAppServerBadRequestException("Missing user name path parameter");
      if (userId.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty user name path parameter");
      if (password == null)
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      if (password.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password))
        throw new AgentAppServerBadRequestException("Unknown user name or invalid password");
      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(userId).containsKey(agentName))
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

      AgentInstanceList agentMap = agentServer.agentInstances.get(userId);
      AgentInstance agent = agentMap.get(agentName);

      NotificationInstance notificationInstance = agent.notifications.get(notificationName);
      if (notificationInstance == null)
        throw new AgentAppServerBadRequestException("Undefined notification name for agent instance '" + agentName + "': " + notificationName);
      if (! notificationInstance.pending)
        throw new AgentAppServerBadRequestException("Cannot respond to notification '" + notificationName + "' for agent instance '" + agentName + "' since it is not pending");
      
      log.info("Respond to a pending notification '" + notificationName + "' for agent instance " + agentName + " for user: " + userId);

      agent.respondToNotification(notificationInstance, responseParam, responseChoice, comment);
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/(pause|disable)$")){
      String userId = pathParts[2];
      String agentName = pathParts[4];
      String password = request.getParameter("password");
      if (userId == null)
        throw new AgentAppServerBadRequestException("Missing user Id path parameter");
      else if (userId.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty user Id path parameter");
      else if (password == null)
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      else if (password.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      else if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password))
        throw new AgentAppServerBadRequestException("Unknown user name or invalid password");
      else if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      else if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      else if (! agentServer.agentInstances.get(userId).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Disabling/pausing agent instance " + agentName + " for user: " + userId);
      AgentInstanceList agentMap = agentServer.agentInstances.get(userId);
      AgentInstance agent = agentMap.get(agentName);

      // Disable/pause the agent
      agent.disable();
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/(resume|enable)$")){
      String userId = pathParts[2];
      String agentName = pathParts[4];
      String password = request.getParameter("password");
      if (userId == null)
        throw new AgentAppServerBadRequestException("Missing user Id path parameter");
      else if (userId.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty user Id path parameter");
      else if (password == null)
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      else if (password.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      else if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password))
        throw new AgentAppServerBadRequestException("Unknown user name or invalid password");
      else if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      else if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      else if (! agentServer.agentInstances.get(userId).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Enabling/resuming agent instance " + agentName + " for user: " + userId);
      AgentInstanceList agentMap = agentServer.agentInstances.get(userId);
      AgentInstance agent = agentMap.get(agentName);

      // Enable/resume the agent
      agent.enable();
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/run_script/[a-zA-Z0-9_.@\\-]*$")){
      String userId = pathParts[2];
      String agentName = pathParts[4];
      String scriptName = pathParts[6];
      String password = request.getParameter("password");
      if (userId == null)
        throw new AgentAppServerBadRequestException("Missing user name path parameter");
      else if (userId.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty user name path parameter");
      else if (password == null)
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      else if (password.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      else if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password))
        throw new AgentAppServerBadRequestException("Unknown user name or invalid password");
      else if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      else if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      else if (! agentServer.agentInstances.get(userId).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");
      else if (scriptName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance script name path parameter");
      else if (scriptName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance script name path parameter");
      
      ScriptDefinition scriptDefinition =
          agentServer.agentInstances.get(userId).get(agentName).agentDefinition.scripts.get(scriptName);
      if (scriptDefinition == null)
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "Undefined public agent script for that user: " + scriptName);
      if (! scriptDefinition.publicAccess)
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "Undefined public agent script for that user: " + scriptName);

      log.info("Call a public script for agent instance " + agentName + " for user: " + userId);
      AgentInstanceList agentMap = agentServer.agentInstances.get(userId);
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
