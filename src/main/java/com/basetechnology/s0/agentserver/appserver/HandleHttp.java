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

import org.eclipse.jetty.server.Request;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
}
