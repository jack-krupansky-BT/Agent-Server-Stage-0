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
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentDefinitionList;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentInstanceList;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.OutputRecord;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.field.Field;
import com.basetechnology.s0.agentserver.notification.NotificationInstance;
import com.basetechnology.s0.agentserver.scheduler.AgentScheduler;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionNode;
import com.basetechnology.s0.agentserver.script.intermediate.ScriptNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.parser.ScriptParser;
import com.basetechnology.s0.agentserver.script.runtime.ScriptRuntime;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.ListMap;
import com.basetechnology.s0.agentserver.util.NameValue;

public class HandleGet extends HandleHttp {
  static final Logger log = Logger.getLogger(HandleGet.class);

  public boolean handleGet(HttpInfo httpInfo) throws IOException, ServletException, AgentAppServerException, AgentServerException, InterruptedException, SymbolException, JSONException {
    // Extract out commonly used info
    this.httpInfo = httpInfo;
    String path = httpInfo.path;
    String[] pathParts = httpInfo.pathParts;
    Request request = httpInfo.request;
    HttpServletResponse response = httpInfo.response;
    AgentServer agentServer = httpInfo.agentServer;
    String lcPath = path.toLowerCase();
    
    if (path.equalsIgnoreCase("/about")){
      log.info("Getting about info");
      response.setContentType("application/json; charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);
      JSONObject aboutJson = new JsonListMap();
      aboutJson.put("name", agentServer.config.get("name"));
      aboutJson.put("software", agentServer.config.get("software"));
      aboutJson.put("version", agentServer.config.get("version"));
      aboutJson.put("description", agentServer.config.get("description"));
      aboutJson.put("website", agentServer.config.get("website"));
      aboutJson.put("contact", agentServer.config.get("contact"));
      AgentAppServer.setOutput(httpInfo, aboutJson);
    } else if (path.equalsIgnoreCase("/evaluate")){
      try {
        BufferedReader reader = request.getReader();
        String expressionString = null;
        try {
          StringBuilder builder = new StringBuilder();
          char[] buffer = new char[8192];
          int read;
          while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
            builder.append(buffer, 0, read);
          }
          expressionString = builder.toString();
        } catch (Exception e){
          log.info("Exception reading expression text : " + e);
        }

        log.info("Evaluating expression: " + expressionString);
        AgentDefinition dummyAgentDefinition = new AgentDefinition(agentServer);
        AgentInstance dummyAgentInstance = new AgentInstance(dummyAgentDefinition);
        ScriptParser parser = new ScriptParser(dummyAgentInstance);
        ScriptRuntime scriptRuntime = new ScriptRuntime(dummyAgentInstance);
        ExpressionNode expressionNode = parser.parseExpressionString(expressionString);
        Value valueNode = scriptRuntime.evaluateExpression(expressionString, expressionNode);
        String resultString = valueNode.getStringValue();

        response.setContentType("text/plain; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(resultString);
      } catch (Exception e){
        log.info("Evaluate Exception: " + e);
      }
      ((Request)request).setHandled(true);
    } else if (path.equalsIgnoreCase("/run")){
      try {
        BufferedReader reader = request.getReader();
        String scriptString = null;
        try {
          StringBuilder builder = new StringBuilder();
          char[] buffer = new char[8192];
          int read;
          while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
            builder.append(buffer, 0, read);
          }
          scriptString = builder.toString();
        } catch (Exception e){
          log.info("Exception reading script text : " + e);
        }

        log.info("Running script: " + scriptString);
        AgentDefinition dummyAgentDefinition = new AgentDefinition(agentServer);
        AgentInstance dummyAgentInstance = new AgentInstance(dummyAgentDefinition);
        ScriptParser parser = new ScriptParser(dummyAgentInstance);
        ScriptRuntime scriptRuntime = new ScriptRuntime(dummyAgentInstance);
        ScriptNode scriptNode = parser.parseScriptString(scriptString);
        Value valueNode = scriptRuntime.runScript(scriptString, scriptNode);
        String resultString = valueNode.getStringValue();
        log.info("Script result: " + resultString);

        response.setContentType("text/plain; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(resultString);
      } catch (Exception e){
        log.info("Run Exception: " + e);
      }
      ((Request)request).setHandled(true);
    } else if (path.equalsIgnoreCase("/status")){
      log.info("Getting status info");
      response.setContentType("application/json; charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);

      // Sleep a little to assure status reflects any recent operation
      Thread.sleep(100);

      // Get the status info
      JSONObject aboutJson = new JsonListMap();
      AgentScheduler agentScheduler = AgentScheduler.singleton;
      aboutJson.put("status", agentScheduler == null ? "shutdown" : agentScheduler.getStatus());
      aboutJson.put("since", DateUtils.toRfcString(agentServer.startTime));
      aboutJson.put("num_registered_users", agentServer.users.size());
      int numActiveUsers = 0;
      for (NameValue<AgentInstanceList> agentInstanceListNameValue: agentServer.agentInstances)
        if (agentInstanceListNameValue.value.size() > 0)
          numActiveUsers++;
      aboutJson.put("num_active_users", numActiveUsers);
      int num_registered_agents = 0;
      for (NameValue<AgentDefinitionList> agentDefinitionListNameValue: agentServer.agentDefinitions)
        num_registered_agents += agentDefinitionListNameValue.value.size();
      aboutJson.put("num_registered_agents", num_registered_agents);
      int num_active_agents = 0;
      for (NameValue<AgentInstanceList> agentInstanceListNameValue: agentServer.agentInstances)
        num_active_agents += agentInstanceListNameValue.value.size();
      aboutJson.put("num_active_agents", num_active_agents);
      response.getWriter().println(aboutJson.toString(4));
    } else if (path.equalsIgnoreCase("/config")){
      log.info("Getting configuration settings");
      response.setContentType("application/json; charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);

      // Get the config info
      JSONObject configJson = agentServer.config.toJson();

      response.getWriter().println(configJson.toString(4));
    } else if (path.equalsIgnoreCase("/agent_definitions")){
      checkAdminAccess();
      log.info("Getting list of agent definitions");
      JSONArray agentDefinitionsArrayJson = new JSONArray();
      // Get all agents for all users
      for (NameValue<AgentDefinitionList> userAgentDefinitions: agentServer.agentDefinitions){
        // Get all agents for this user
        for (AgentDefinition agentDefinition: agentServer.agentDefinitions.get(userAgentDefinitions.name)){
          // Generate JSON for short summary of agent definition
          JSONObject agentDefinitionJson = new JsonListMap();
          agentDefinitionJson.put("user", agentDefinition.user.id);
          agentDefinitionJson.put("name", agentDefinition.name);
          agentDefinitionJson.put("description", agentDefinition.description);
          agentDefinitionsArrayJson.put(agentDefinitionJson);
        }
      }
      JSONObject agentDefinitionsJson = new JSONObject();
      agentDefinitionsJson.put("agent_definitions", agentDefinitionsArrayJson);
      AgentAppServer.setOutput(httpInfo, agentDefinitionsJson);
    } else if (path.equalsIgnoreCase("/agents")){
      checkAdminAccess();
      log.info("Getting list of agent instances for all users");
      JSONArray agentInstancesArrayJson = new JSONArray();
      // Get all agents for all users
      for (NameValue<AgentInstanceList> userAgentInstances: agentServer.agentInstances){
        // Get all agents for this user
        for (AgentInstance agentInstance: agentServer.agentInstances.get(userAgentInstances.name)){
          // Generate JSON for short summary of agent instance
          JSONObject agentInstanceJson = new JsonListMap();
          agentInstanceJson.put("user", agentInstance.user.id);
          agentInstanceJson.put("name", agentInstance.name);
          agentInstanceJson.put("definition", agentInstance.agentDefinition.name);
          agentInstanceJson.put("description", agentInstance.description);
          agentInstancesArrayJson.put(agentInstanceJson);
        }
      }
      JSONObject agentInstancesJson = new JSONObject();
      agentInstancesJson.put("agent_instances", agentInstancesArrayJson);
      AgentAppServer.setOutput(httpInfo, agentInstancesJson);
    } else if (path.equalsIgnoreCase("/field_types")){
      try {
        log.info("Getting list of field types");
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        JSONArray fieldTypesArrayJson = new JSONArray();
        for (String fieldType: Field.types)
          fieldTypesArrayJson.put(fieldType);
        JSONObject fieldTypesJson = new JSONObject();
        fieldTypesJson.put("field_types", fieldTypesArrayJson);
        response.getWriter().println(fieldTypesJson.toString(4));
      } catch (JSONException e){
        throw new AgentServerException("JSON error generating JSON for agent definition status - " + e);
      }
      return true;
    } else if (path.equalsIgnoreCase("/usage")){
      log.info("Getting API usage summary text");

      // Get text of API usage summary, api-usage.txt
      // TODO: Where to read the file from... ./doc or???
      String apiUsageText = FileUtils.readFileToString(new File("./doc/api-usage.txt"));

      // Return the text
      response.setContentType("application/json; charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().println(apiUsageText);
    } else if (path.equalsIgnoreCase("/users")){
      checkAdminAccess();
      log.info("Getting list of all user ids");
      JSONArray usersArrayJson = new JSONArray();
      for (NameValue<User> userIdValue: agentServer.users){
        User user = userIdValue.value;
        JSONObject userJson = new JSONObject();
        userJson.put("id", user.id);
        userJson.put("display_name", user.incognito ? "(Incognito)" :
          (user.displayName == null ? "" : user.displayName));
        usersArrayJson.put(userJson);
      }
      JSONObject usersJson = new JSONObject();
      usersJson.put("users", usersArrayJson);
      AgentAppServer.setOutput(httpInfo, usersJson);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*$")){
      User user = checkUserAccess(false);
      log.info("Getting detailed info for a specified user Id: " + user.id);
      AgentAppServer.setOutput(httpInfo, user.toJson());
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agent_definitions$")){
      User user = checkUserAccess(false);
      log.info("Getting list of all agent definitions for user Id: " + user.id);

      // Get all agents for this user
      JSONArray agentDefinitionsArrayJson = new JSONArray();
      for (AgentDefinition agentDefinition: agentServer.agentDefinitions.get(user.id)){
        // Generate JSON for short summary of agent definition
        JSONObject agentDefinitionJson = new JsonListMap();
        agentDefinitionJson.put("user", agentDefinition.user.id);
        agentDefinitionJson.put("name", agentDefinition.name);
        agentDefinitionJson.put("description", agentDefinition.description);
        agentDefinitionsArrayJson.put(agentDefinitionJson);
      }
      JSONObject agentDefinitionsJson = new JSONObject();
      agentDefinitionsJson.put("agent_definitions", agentDefinitionsArrayJson);
      AgentAppServer.setOutput(httpInfo, agentDefinitionsJson);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agent_definitions/[a-zA-Z0-9_.@\\-]*$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent definition name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent definition name path parameter");
      if (! agentServer.agentDefinitions.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent definition with that name for that user");

      log.info("Getting definition for agent definition " + agentName + " for user: " + user.id);
      AgentDefinitionList agentMap = agentServer.agentDefinitions.get(user.id);
      AgentDefinition agentDefinition = agentMap.get(agentName);
      AgentAppServer.setOutput(httpInfo, agentDefinition.toJson());
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agent_definitions/[a-zA-Z0-9_.@\\-]*/status$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      
      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent definition name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent definition name path parameter");
      if (! agentServer.agentDefinitions.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent definition with that name for that user");

      log.info("Getting status for agent definition " + agentName + " for user: " + user.id);
      AgentDefinitionList agentMap = agentServer.agentDefinitions.get(user.id);
      AgentDefinition agent = agentMap.get(agentName);
      JSONObject statusJson = new JSONObject();
      statusJson.put("user_id", user.id);
      statusJson.put("name", agent.name);
      statusJson.put("created", DateUtils.toRfcString(agent.timeCreated));
      statusJson.put("modified", DateUtils.toRfcString(agent.timeModified));
      int numActiveInstances = 0;
      for (NameValue<AgentInstanceList> agentInstanceListNameValue: agentServer.agentInstances)
        for (AgentInstance agentInstance: agentInstanceListNameValue.value)
          if (agentInstance.agentDefinition == agent)
            numActiveInstances++;
      statusJson.put("num_active_instances", numActiveInstances);
      AgentAppServer.setOutput(httpInfo, statusJson);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents$")){
      User user = checkUserAccess(false);
      log.info("Getting list of all agent instances for a user");

      // Get all agents for this user
      JSONArray agentInstancesArrayJson = new JSONArray();
      for (AgentInstance agentInstance: agentServer.agentInstances.get(user.id)){
        // Generate JSON for short summary of agent instance
        JSONObject agentInstanceJson = new JsonListMap();
        agentInstanceJson.put("user", agentInstance.user.id);
        agentInstanceJson.put("name", agentInstance.name);
        agentInstanceJson.put("definition", agentInstance.agentDefinition.name);
        // TODO: Add the SHA for this instance
        agentInstanceJson.put("description", agentInstance.description);
        agentInstancesArrayJson.put(agentInstanceJson);
      }
      JSONObject agentInstancesJson = new JSONObject();
      agentInstancesJson.put("agent_instances", agentInstancesArrayJson);
      AgentAppServer.setOutput(httpInfo, agentInstancesJson);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      String stateString = request.getParameter("state");
      boolean includeState = stateString != null && (stateString.equalsIgnoreCase("true") ||
          stateString.equalsIgnoreCase("yes") ||
          stateString.equalsIgnoreCase("on"));
      String countString = request.getParameter("count");
      int count = -1;
      if (countString != null && countString.trim().length() > 0)
        count = Integer.parseInt(countString);

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Getting detail info for agent instance " + agentName + " for user: " + user.id);
      AgentInstance agentInstance = agentServer.agentInstances.get(user.id).get(agentName);

      AgentAppServer.setOutput(httpInfo, agentInstance.toJson(includeState, count));
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/notifications$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Getting current pending notification for agent instance " + agentName + " for user: " + user.id);
      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agent = agentMap.get(agentName);

      // Build a JSON array of all pending notifications for agent
      JSONArray pendingNotificationsJson = new JSONArray();
      for (String notificationName: agent.notifications){
        NotificationInstance notificationInstance = agent.notifications.get(notificationName);
        if (notificationInstance.pending){
          // Generate and return a summary of the notification
          JSONObject notificationSummaryJson = new JsonListMap();
          notificationSummaryJson.put("agent", agent.name);
          notificationSummaryJson.put("name", notificationInstance.definition.name);
          notificationSummaryJson.put("description", notificationInstance.definition.description);
          notificationSummaryJson.put("details", notificationInstance.details.toJsonObject());
          notificationSummaryJson.put("type", notificationInstance.definition.type);
          notificationSummaryJson.put("time", DateUtils.toRfcString(notificationInstance.timeNotified));
          notificationSummaryJson.put("timeout", notificationInstance.timeout);
          pendingNotificationsJson.put(notificationSummaryJson);
        }
      }

      // Build a wrapper object for the array
      JSONObject wrapperJson = new JSONObject();
      wrapperJson.put("pending_notifications", pendingNotificationsJson);

      // Return the wrapped list
      AgentAppServer.setOutput(httpInfo, wrapperJson);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/notifications/[a-zA-Z0-9_.@\\-]*$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      // TODO: Maybe if path ends with "/", should be treated as GET of list of pending notifications
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

      // TODO: If no response, maybe it should return current notification info and details
      log.info("Respond to a pending notification '" + notificationName + "' for agent instance " + agentName + " for user: " + user.id);

      agent.respondToNotification(notificationInstance, responseParam, responseChoice, comment);
      
      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/output$")){
      String userId = pathParts[2];
      String agentName = pathParts[4];

      if (userId == null)
        throw new AgentAppServerBadRequestException("Missing user Id path parameter");
      if (userId.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty user Id path parameter");
      if (! agentServer.users.containsKey(userId))
        throw new AgentAppServerBadRequestException("Unknown user name or invalid password");
      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(userId).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      // Password not required for "public output" instances
      AgentInstance agent = agentServer.agentInstances.get(userId).get(agentName);
      User user = null;
      if (! agent.publicOutput)
        user = checkUserAccess(false);

      log.info("Getting output for agent instance " + agentName + " for user: " + userId);

      // Build a JSON object equivalent to map of output fields
      JSONObject outputJson = new JsonListMap();
      SymbolValues outputValues = agent.categorySymbolValues.get("outputs");
      for (Symbol outputSymbol: outputValues){
        String fieldName = outputSymbol.name;
        outputJson.put(fieldName, agent.getOutput(fieldName).toJsonObject());
      }

      AgentAppServer.setOutput(httpInfo, outputJson);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/output_history$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      String countString = request.getParameter("count");
      int count = countString == null ? -1 : Integer.parseInt(countString);

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Getting output history for agent instance " + agentName + " for user: " + user.id);
      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agent = agentMap.get(agentName);

      // Limit or default the user's specified count
      if (count <= 0)
        count = agent.defaultOutputCount;
      if (count > agent.outputLimit)
        count = agent.outputLimit;
      int outputSize = agent.outputHistory.size();
      if (count > outputSize)
        count = outputSize;

      // Compute starting history index
      int start = outputSize - count;

      int n = agent.outputHistory.size();
      if (n > 4){
        SymbolValues s1 = agent.outputHistory.get(n-2).output;
        SymbolValues s2 = agent.outputHistory.get(n-1).output;
        boolean eq = s1.equals(s2);
      }

      // Build a JSON array of output rows
      JSONArray outputJson = new JSONArray();
      for (int i = start; i < outputSize; i++){
        OutputRecord outputs = agent.outputHistory.get(i);
        outputJson.put(outputs.output.toJson());
      }

      // Wrap the array in an object since that is what output code expects
      JSONObject outputHistory = new JsonListMap();
      outputHistory.put("output_history", outputJson);
      AgentAppServer.setOutput(httpInfo, outputHistory);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/state$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      String countString = request.getParameter("count");

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      int count = -1;
      if (countString != null && countString.trim().length() > 0)
        count = Integer.parseInt(countString);

      log.info("Getting full state and detail info for agent instance " + agentName + " for user: " + user.id);
      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agentInstance = agentMap.get(agentName);
      AgentAppServer.setOutput(httpInfo, agentInstance.toJson(true, count));
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents/[a-zA-Z0-9_.@\\-]*/status$")){
      User user = checkUserAccess(false);
      String agentName = pathParts[4];
      String stateString = request.getParameter("state");
      boolean includeState = stateString != null && (stateString.equalsIgnoreCase("true") ||
          stateString.equalsIgnoreCase("yes") ||
          stateString.equalsIgnoreCase("on"));
      String countString = request.getParameter("count");
      int count = -1;
      if (countString != null && countString.trim().length() > 0)
        count = Integer.parseInt(countString);

      if (agentName == null)
        throw new AgentAppServerBadRequestException("Missing agent instance name path parameter");
      if (agentName.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty agent instance name path parameter");
      if (! agentServer.agentInstances.get(user.id).containsKey(agentName))
        throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "No agent instance with that name for that user");

      log.info("Getting status for agent instance " + agentName + " for user: " + user.id);
/*      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agent = agentMap.get(agentName);
      JSONObject statusJson = new JsonListMap();
      statusJson.put("user", user.id);
      statusJson.put("name", agent.name);
      statusJson.put("definition", agent.agentDefinition.name);
      statusJson.put("description", agent.description);
      statusJson.put("status", agent.getStatus());
      statusJson.put("instantiated", DateUtils.toRfcString(agent.timeInstantiated));
      long lastUpdated = agent.timeUpdated;
      statusJson.put("updated", lastUpdated > 0 ? DateUtils.toRfcString(lastUpdated) : "");
      long lastInputsChanged = agent.lastInputsChanged;
      statusJson.put("inputs_changed", lastInputsChanged > 0 ? DateUtils.toRfcString(lastInputsChanged) : "");
      long lastTriggered = agent.lastTriggered;
      statusJson.put("triggered", lastTriggered > 0 ? DateUtils.toRfcString(lastTriggered) : "");
      int outputsSize = agent.outputHistory.size();
      long lastOutput = outputsSize > 0 ? agent.outputHistory.get(outputsSize - 1).time : 0;
      statusJson.put("outputs_changed", lastOutput > 0 ? DateUtils.toRfcString(lastOutput) : "");
      
      // Done
      AgentAppServer.setOutput(httpInfo, statusJson);
      */
      AgentInstanceList agentMap = agentServer.agentInstances.get(user.id);
      AgentInstance agentInstance = agentMap.get(agentName);
      AgentAppServer.setOutput(httpInfo, agentInstance.toJson(includeState, count));
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-*]*/website_access$")){
      User user = checkAdminUserAccess();

      log.info("Getting web site access controls for user: " + user.id);

      // Get the access control list for the user
      ListMap<String, String> accessList =  agentServer.getWebSiteAccessControls(user);

      // Put the list in JSON format
      JSONObject accessListJson = new JSONObject();
      for (String url: accessList)
        accessListJson.put(url, accessList.get(url));

      // Done
      AgentAppServer.setOutput(httpInfo, accessListJson);
    } else {
      throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "Path does not address any existing object");
    }
    return true;
  }

}
