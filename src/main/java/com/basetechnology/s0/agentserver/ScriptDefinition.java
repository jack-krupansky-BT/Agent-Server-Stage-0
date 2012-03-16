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

package com.basetechnology.s0.agentserver;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.JsonUtils;

public class ScriptDefinition {
  static final Logger log = Logger.getLogger(ScriptDefinition.class);
  public String name;
  public String parameters;
  public String description;
  public String script;
  public int executionLevel;
  public boolean publicAccess;

  public ScriptDefinition (String name, String parameters, String description, String script,
      int executionLevel, boolean publicAccess){
    this.name = name;
    this.parameters = parameters;
    this.description = description;
    this.script = script;
    this.executionLevel = executionLevel;
    this.publicAccess = publicAccess;
  }

  static public ScriptDefinition fromJson(String scriptString) throws JSONException, AgentServerException {
    return fromJson(new JSONObject(scriptString));
  }

  static public ScriptDefinition fromJson(JSONObject scriptJson) throws AgentServerException{
    // TODO: Whether empty fields should be null of empty strings
    String name = scriptJson.optString("name", "");
    String parameters = scriptJson.optString("parameters", "");
    String description = scriptJson.optString("description", "");
    String script = scriptJson.optString("script","");
    int executionLevel = scriptJson.optInt("execution_level");
    boolean publicAccess = scriptJson.optBoolean("public");
    
    // Validate keys
    JsonUtils.validateKeys(scriptJson, "Script Definition", new ArrayList<String>(Arrays.asList(
        "name", "parameters", "description", "script", "execution_level", "public")));

    return new ScriptDefinition(name, parameters, description, script, executionLevel, publicAccess);
  }
  
  public JSONObject toJson(){
    JSONObject scriptJson = new JsonListMap();
    try {
      scriptJson.put("name", name);
      scriptJson.put("parameters", parameters);
      scriptJson.put("description", description);
      scriptJson.put("script", script);
      scriptJson.put("execution_level", executionLevel);
      scriptJson.put("public", publicAccess);
    } catch (JSONException e){
      log.error("Unable to output ScriptDefinition as JSONObject - " + e.getMessage());
      e.printStackTrace();
    }
    return scriptJson;
  }

  public String toString(){
    return name + ": \"" + description + "\" - \"" + script + "\"";
  }
}
