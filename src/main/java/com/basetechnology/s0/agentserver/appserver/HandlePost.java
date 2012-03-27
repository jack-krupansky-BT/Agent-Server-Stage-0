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
import org.json.JSONException;
import org.json.JSONObject;
import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;

public class HandlePost extends HandleHttp {
  static final Logger log = Logger.getLogger(HandlePut.class);

  public boolean handlePost(HttpInfo httpInfo) throws IOException, ServletException, AgentAppServerException, SymbolException, RuntimeException, AgentServerException, JSONException, TokenizerException, ParserException {
    // Extract out commonly used info
    this.httpInfo = httpInfo;
    String path = httpInfo.path;
    String[] pathParts = httpInfo.pathParts;
    Request request = httpInfo.request;
    HttpServletResponse response = httpInfo.response;
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
      String organization = request.getParameter("organization");
      if (organization == null)
        organization = userJson.optString("organization");
      else
        userJson.put("organization", organization);
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
        User newUser = new User(id, password, passwordHint, fullName, displayName, nickName,
            organization, bio, interests, email, incognito, comment, approved, true, true, null, null);
        newUser.generateSha();
        agentServer.addUser(newUser);
        response.setStatus(HttpServletResponse.SC_CREATED);
        // TODO: Set Location header with URL
        return true;
      }
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agent_definitions$")){
      User user = checkUserAccess(true);
      JSONObject agentDefinitionJson = getJsonRequest(httpInfo);

      if (agentDefinitionJson == null)
        throw new AgentAppServerBadRequestException("Invalid agent definition JSON object");

      log.info("Adding new agent definition for user: " + user.id);

      // Parse and add the agent definition
      AgentDefinition agentDefinition = agentServer.addAgentDefinition(user, agentDefinitionJson);

      // Done
      response.setStatus(HttpServletResponse.SC_CREATED);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-]*/agents$")){
      User user = checkUserAccess(true);
      JSONObject agentInstanceJson = getJsonRequest(httpInfo);

      if (agentInstanceJson == null)
        throw new AgentAppServerBadRequestException("Invalid agent instance JSON object");

      log.info("Adding new agent instance for user: " + user.id);

      // Parse and add the agent instance
      AgentInstance agentInstance = agentServer.addAgentInstance(user, agentInstanceJson);

      // Done
      response.setStatus(HttpServletResponse.SC_CREATED);
    } else if (lcPath.matches("^/users/[a-zA-Z0-9_.@\\-*]*/website_access$")){
      User user = checkAdminUserAccess();
      JSONObject accessControlsJson = getJsonRequest(httpInfo);

      if (accessControlsJson == null)
        throw new AgentAppServerBadRequestException("Invalid website access JSON object");

      log.info("Adding web site access controls for user: " + user.id);

      // Add the web site access controls for the user
      agentServer.addWebSiteAccessControls(user, accessControlsJson);

      // Done
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      throw new AgentAppServerException(HttpServletResponse.SC_NOT_FOUND, "Path does not address any existing object");
    }
    return true;

  }
}
