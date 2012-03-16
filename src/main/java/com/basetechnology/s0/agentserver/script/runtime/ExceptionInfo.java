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

package com.basetechnology.s0.agentserver.script.runtime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.JsonUtils;

public class ExceptionInfo {
  public Exception exception;
  public String type;
  public String message;
  public long time;
  public String scriptName;
  
  public ExceptionInfo(Exception exception, String scriptName){
    this(exception, null, null, -1, scriptName);
  }

  public ExceptionInfo clone(){
    return new ExceptionInfo(exception, type, message, time, scriptName);
  }
  
  public ExceptionInfo(Exception exception, String type, String message, long time, String  scriptName){
    this.exception = exception;
    this.type = type != null ? type : exception != null ? exception.getClass().getName() : null;
    this.message = message != null ? message : exception != null ? exception.getMessage() : null;
    this.time = time > 0 ? time : System.currentTimeMillis();
    this.scriptName = scriptName;
  }

  public static ExceptionInfo fromJson(JSONObject exceptionJson) throws ParseException, AgentServerException {
    String type = exceptionJson.optString("type");
    long time = DateUtils.parseRfcString(exceptionJson.optString("time"));
    String message = exceptionJson.optString("message");
    String scriptName = exceptionJson.optString("script");
    
    // Validate keys
    JsonUtils.validateKeys(exceptionJson, "ExceptionInfo", new ArrayList<String>(Arrays.asList(
        "type", "time", "message", "script")));

    // Generate and return an ExceptionInfo object
    return new ExceptionInfo(null, type, message, time, scriptName);
  }
  
  public JSONObject toJson() throws JSONException{
    JSONObject exceptionJson = new JsonListMap();
    exceptionJson.put("type", type);
    exceptionJson.put("time", DateUtils.toRfcString(time));
    exceptionJson.put("message", message);
    exceptionJson.put("script", scriptName);
    return exceptionJson;
  }
  
  public String toString(){
    return "Exception type: " + type + " at " + DateUtils.toRfcString(time) + " message: " + message;
  }
}
