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

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.User;

public class HandleHttp {
  public HttpInfo httpInfo;
  
  public JSONObject getInputJson() throws JSONException, IOException {
    BufferedReader reader = httpInfo.request.getReader();
    JSONObject json = new JSONObject(new JSONTokener(reader));
    return json;
  }

  public static JSONObject getJsonRequest(HttpInfo httpInfo) throws AgentAppServerBadRequestException {
    Request request = httpInfo.request;
    BufferedReader reader = null;
    int contentLength = -1;
    try {
      contentLength = request.getContentLength();
      reader = request.getReader();
    } catch (Exception e){
      throw new AgentAppServerBadRequestException("Unable to read JSON from request - " + e.getMessage());
    }
    JSONObject requestJson = null;
    try {
      if (contentLength <= 0)
        requestJson = new JSONObject();
      else
        requestJson = new JSONObject(new JSONTokener(reader));
    } catch (Exception e){
      throw new AgentAppServerBadRequestException("Unable to parse JSON from request - " + e.getMessage());
    } finally {
      try {
        reader.close();
      } catch (Exception e){
        // Ignore
      }
    }
    return requestJson;
  }

  public void checkAdminAccess() throws AgentAppServerBadRequestException {
    // Make sure we have the admin password
    Request request = httpInfo.request;
    String adminPassword = request.getParameter("admin_password");
    String password = request.getParameter("password");
    if (adminPassword == null)
      adminPassword = password;
    if (adminPassword == null)
      throw new AgentAppServerBadRequestException("Missing admin password query parameter");
    if (adminPassword.trim().length() == 0)
      throw new AgentAppServerBadRequestException("Empty admin password query parameter");
    adminPassword = adminPassword.trim();
    AgentServer agentServer = httpInfo.agentServer;
    if (! adminPassword.equals(agentServer.getAdminPassword()))
      throw new AgentAppServerBadRequestException("Invalid admin password");
  }

  public User checkAdminUserAccess() throws AgentAppServerBadRequestException {
    // Make sure we have the admin password
    Request request = httpInfo.request;
    String adminPassword = request.getParameter("admin_password");
    String password = request.getParameter("password");
    if (adminPassword == null)
      adminPassword = password;
    if (adminPassword == null)
      throw new AgentAppServerBadRequestException("Missing admin password query parameter");
    if (adminPassword.trim().length() == 0)
      throw new AgentAppServerBadRequestException("Empty admin password query parameter");
    adminPassword = adminPassword.trim();
    AgentServer agentServer = httpInfo.agentServer;
    if (! adminPassword.equals(agentServer.getAdminPassword()))
      throw new AgentAppServerBadRequestException("Invalid admin password");

    // Get user id from path
    String[] pathParts = httpInfo.pathParts;
    String userId = pathParts[2];

    // Return the user info
    if (userId.equals("*"))
      return User.allUser;
    else
      return agentServer.users.get(userId);
}

  public User checkUserAccess(boolean newActivity) throws AgentAppServerBadRequestException {
    // Get user id from path
    String[] pathParts = httpInfo.pathParts;
    String userId = pathParts[2];
    
    // Get password from query parameters
    Request request = httpInfo.request;
    String password = request.getParameter("password");

    // Get optional admin password
    String adminPassword = request.getParameter("admin_password");

    // Do the validation
    return checkUserAccess(userId, password, adminPassword, newActivity);
  }

  public User checkUserAccess(String userId, String password, String adminPassword, boolean newActivity) throws AgentAppServerBadRequestException {
    // Validate user id and password
    AgentServer agentServer = httpInfo.agentServer;
    if (userId == null)
      throw new AgentAppServerBadRequestException("Missing user Id path parameter");
    if (userId.trim().length() == 0)
      throw new AgentAppServerBadRequestException("Empty user Id path parameter");
    if (! agentServer.users.containsKey(userId))
      throw new AgentAppServerBadRequestException("Unknown user Id or invalid password");
    User user = agentServer.users.get(userId);

    // Admin override password may have been specified
    if (adminPassword != null){
      if (adminPassword.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty admin password query parameter");
      adminPassword = adminPassword.trim();
      if (! adminPassword.equals(agentServer.getAdminPassword()))
        throw new AgentAppServerBadRequestException("Invalid admin password");
    } else {
      // Validate user password
      if (password == null)
        throw new AgentAppServerBadRequestException("Missing password query parameter");
      if (password.trim().length() == 0)
        throw new AgentAppServerBadRequestException("Empty password query parameter");
      if (! agentServer.users.get(userId).password.equals(password))
        throw new AgentAppServerBadRequestException("Unknown user Id or invalid password");
    }

    // Check whether activity is permitted - activity is always permitted for admin
    if (adminPassword == null){
      if (! user.enabled)
        throw new AgentAppServerBadRequestException("All activity is disabled for this user");
      if (newActivity && ! user.newActivityEnabled)
        throw new AgentAppServerBadRequestException("New activity is disabled for this user");
    }
    
    return user;
  }
}
