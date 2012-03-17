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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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
import com.basetechnology.s0.agentserver.AgentState;
import com.basetechnology.s0.agentserver.AgentTimer;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.field.Field;
import com.basetechnology.s0.agentserver.field.FieldList;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionNode;
import com.basetechnology.s0.agentserver.script.intermediate.ScriptNode;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.ScriptParser;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.script.runtime.ExceptionInfo;
import com.basetechnology.s0.agentserver.script.runtime.ScriptRuntime;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.DateUtils;

public class HandlePost extends HandleHttp {
  static final Logger log = Logger.getLogger(HandlePut.class);

  public boolean handlePost(HttpInfo httpInfo) throws IOException, ServletException, AgentAppServerException, SymbolException, RuntimeException, AgentServerException, JSONException, TokenizerException, ParserException {
    // Extract out commonly used info
    String path = httpInfo.path;
    String[] pathParts = httpInfo.pathParts;
    Request request = httpInfo.request;
    HttpServletResponse response = httpInfo.response;
    String format = httpInfo.format;
    AgentServer agentServer = httpInfo.agentServer;
    String lcPath = path.toLowerCase();
    
    if (path.equalsIgnoreCase("/users")){
      // User can specify parameters in JSON or as query parameters
      // Query overrides JSON if query parameter is non-null
      JSONObject userJson = getJsonRequest(httpInfo);

      String id = request.getParameter("id");
      if (id == null)
        id = userJson.optString("id");
      else
        userJson.put("id", id);
      String password = request.getParameter("password");
      if (password == null)
        password = userJson.optString("password");
      else
        userJson.put("password", password);
      String passwordHint = request.getParameter("password_hint");
      if (passwordHint == null)
        passwordHint = userJson.optString("password_hint");
      else
        userJson.put("password_hint", passwordHint);
      String fullName = request.getParameter("full_name");
      if (fullName == null)
        fullName = userJson.optString("full_name");
      else
        userJson.put("full_name", fullName);
      String displayName = request.getParameter("display_name");
      if (displayName == null)
        displayName = userJson.optString("display_name");
      else
        userJson.put("display_name", displayName);
      String nickName = request.getParameter("nick_name");
      if (nickName == null)
        nickName = userJson.optString("nick_name");
      else
        userJson.put("nick_name", nickName);
      String bio = request.getParameter("bio");
      if (bio == null)
        bio = userJson.optString("bio");
      else
        userJson.put("bio", bio);
      String interests = request.getParameter("interests");
      if (interests == null)
        interests = userJson.optString("interests");
      else
        userJson.put("interests", interests);
      String incognitoString = request.getParameter("incognito");
      boolean incognito = incognitoString != null &&
          (incognitoString.equalsIgnoreCase("true") || incognitoString.equalsIgnoreCase("yes") ||
              incognitoString.equalsIgnoreCase("on"));
      if (incognitoString == null)
        incognito = userJson.optBoolean("incognito");
      else
        userJson.put("incognito", incognito ? "true" : false);
      String email = request.getParameter("email");
      if (email == null)
        email = userJson.optString("email");
      else
        userJson.put("email", email);
      String comment = request.getParameter("comment");
      if (comment == null)
        comment = userJson.optString("comment");
      else
        userJson.put("comment", comment);

      if (id == null){
        throw new AgentAppServerBadRequestException("Missing id query parameter");
      } else if (id.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty id query parameter");
      } else if (id.trim().length() < User.MIN_ID_LENGTH){
        throw new AgentAppServerBadRequestException("Id must be at least 4 characters");
      } else if (password == null){
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      } else if (password.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      } else if (password.trim().length() < User.MIN_ID_LENGTH){
        throw new AgentAppServerBadRequestException("Password must be at least 4 characters");
      } else if (agentServer.users.containsKey(id.trim())){
        throw new AgentAppServerBadRequestException("User with that id already exists");
      } else {
        id = id.trim();
        password = password.trim();
        log.info("Adding new user: " + id);
        Boolean approved = ! agentServer.config.getBoolean("admin_approve_user_create");
        User newUser = new User(id, password, passwordHint, fullName, displayName, nickName, bio, interests, email, incognito, comment, approved, null, null);
        newUser.generateSha();
        agentServer.addUser(newUser);
        response.setStatus(HttpServletResponse.SC_CREATED);
        // TODO: Set Location header with URL
        return true;
      }
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agent_definitions$")){
      String userId = pathParts[2];
      String password = request.getParameter("password");
      JSONObject agentDefinitionJson = getJsonRequest(httpInfo);

      if (userId == null){
        throw new AgentAppServerBadRequestException("Missing user name path parameter");
      } else if (userId.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty user name path parameter");
      } else if (password == null){
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      } else if (password.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      } else if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password)){
        throw new AgentAppServerBadRequestException("Unknown user Id or incorrect password");
      } else if (agentDefinitionJson == null){
        throw new AgentAppServerBadRequestException("Invalid agent definition JSON object");
      } else {
        log.info("Adding new agent definition for user: " + userId);
        User user = agentServer.getUser(userId);
        
        // Parse and add the agent definition
        AgentDefinition agentDefinition = agentServer.addAgentDefinition(user, agentDefinitionJson);
        
        // Done
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      }
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents$")){
      String userId = pathParts[2];
      String password = request.getParameter("password");
      JSONObject agentInstanceJson = getJsonRequest(httpInfo);

      if (userId == null){
        throw new AgentAppServerBadRequestException("Missing user name path parameter");
      } else if (userId.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty user name path parameter");
      } else if (password == null){
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      } else if (password.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      } else if (! agentServer.users.containsKey(userId) ||
          ! agentServer.users.get(userId).password.equals(password)){
        throw new AgentAppServerBadRequestException("Unknown user Id or incorrect password");
      } else if (agentInstanceJson == null){
        throw new AgentAppServerBadRequestException("Invalid agent instance JSON object");
      } else {
        log.info("Adding new agent instance for user: " + userId);
        User user = agentServer.getUser(userId);
        
        // Parse and add the agent instance
        AgentInstance agentInstance = agentServer.addAgentInstance(user, agentInstanceJson);
        
        // Done
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      }
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-*]*/website_access$")){
      String userId = pathParts[2];
      String password = request.getParameter("password");
      JSONObject accessControlsJson = getJsonRequest(httpInfo);

      if (userId == null){
        throw new AgentAppServerBadRequestException("Missing user name path parameter");
      } else if (userId.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty user name path parameter");
      } else if (password == null){
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      } else if (password.trim().length() == 0){
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      } else if ((! userId.equals("*") && ! agentServer.users.containsKey(userId)) ||
          ! password.equals(agentServer.getAdminPassword())){
        throw new AgentAppServerBadRequestException("Unknown user Id or incorrect admin password");
      } else if (accessControlsJson == null){
        throw new AgentAppServerBadRequestException("Invalid website access JSON object");
      } else {
        log.info("Adding web site access controls for user: " + userId);
        User user = agentServer.getUser(userId);

        // Add the web site access controls for the user
        agentServer.addWebSiteAccessControls(user, accessControlsJson);
        
        // Done
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      }
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
    } else {
      throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "Path does not address any existing object");
    }
    return true;

  }
}
