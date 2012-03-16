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
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.ScriptDefinition;
import com.basetechnology.s0.agentserver.field.Field;
import com.basetechnology.s0.agentserver.field.FieldList;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.JsonUtils;
import com.basetechnology.s0.agentserver.util.NameValue;
import com.basetechnology.s0.agentserver.util.NameValueList;


public class NotificationDefinition {
  static final Logger log = Logger.getLogger(ScriptDefinition.class);
  public String name;
  public String description;
  static public final List<String> types = Arrays.asList(
      "notify_only", "confirm", "yes_no", "choice");
  public String type;
  public String condition;
  public boolean manual;
  public boolean enabled;
  public FieldList detail;
  public NameValueList<ScriptDefinition> scripts; // See: NotificationInstance.responses
  public String timeoutExpression;
  public boolean suspend;

  public NotificationDefinition(String name, String description,
      String type, String condition, boolean manual, boolean enabled, FieldList detail, NameValueList<ScriptDefinition> scripts,
      String timeoutExpression, boolean suspend){
    this.name = name;
    this.description = description;
    this.type = type;
    this.condition = condition;
    this.manual = manual;
    this.enabled = enabled;
    this.detail = detail;
    this.scripts = scripts;
    this.timeoutExpression = timeoutExpression;
    this.suspend = suspend;
  }

  static public NotificationDefinition fromJson(String notificationDefinitionString) throws JSONException, AgentServerException, SymbolException {
    return fromJson(new JSONObject(notificationDefinitionString));
  }

  static public NotificationDefinition fromJson(JSONObject notificationDefinitionJson) throws AgentServerException, SymbolException{
    // TODO: Whether empty fields should be null of empty strings
    String name = notificationDefinitionJson.optString("name", "");
    String type = notificationDefinitionJson.optString("type", "");
    String condition = notificationDefinitionJson.optString("condition", "");
    boolean manual = notificationDefinitionJson.optBoolean("manual", false);
    boolean enabled = notificationDefinitionJson.optBoolean("enabled", true);
    String description = notificationDefinitionJson.optString("description", "");
    String timeoutExpression = notificationDefinitionJson.optString("timeout", "");
    boolean suspend = notificationDefinitionJson.optBoolean("suspend", true);
    
    // Parse 'detail' fields
    FieldList details = null;
    if (notificationDefinitionJson.has("details")){
      SymbolManager symbolManager = new SymbolManager();
      JSONArray detailJson = notificationDefinitionJson.optJSONArray("details");
      details = new FieldList();
      int numdetail = detailJson.length();
      for (int i = 0; i < numdetail; i++){
        JSONObject outputJson = detailJson.optJSONObject(i);
        Field field = Field.fromJsonx(symbolManager.getSymbolTable("details"), outputJson);
        // TODO: give error for dup names
        details.add(field);
      }
    }

    // Parse 'scripts' list
    NameValueList<ScriptDefinition> scripts = new NameValueList<ScriptDefinition>();
    if (notificationDefinitionJson.has("scripts")){
      JSONArray scriptsJson = notificationDefinitionJson.optJSONArray("scripts");
      if (scriptsJson != null){
        int numScripts = scriptsJson.length();
        for (int i = 0; i < numScripts; i++){
          // Get JSON for next script
          JSONObject scriptJson = scriptsJson.optJSONObject(i);
          
          // Ignore empty scripts
          if (! scriptJson.keys().hasNext())
            continue;
          
          ScriptDefinition scriptDefinition = ScriptDefinition.fromJson(scriptJson);
          scripts.put(scriptDefinition.name, scriptDefinition);
        }
      }
    }
    
    // Validate keys
    JsonUtils.validateKeys(notificationDefinitionJson, "Notification definition", new ArrayList<String>(Arrays.asList(
        "name", "description", "type", "condition", "manual", "enabled", "details", "scripts", "timeout")));

    return new NotificationDefinition(name, description, type, condition, manual, enabled, details, scripts,
        timeoutExpression, suspend);
  }

  public JSONObject toJson(){
    JSONObject notificationJson = new JsonListMap();
    try {
      notificationJson.put("name", name);
      notificationJson.put("description", description);
      notificationJson.put("type", type);
      notificationJson.put("condition", condition);
      notificationJson.put("manual", manual);
      notificationJson.put("enabled", enabled);
      notificationJson.put("timeout", timeoutExpression);
      notificationJson.put("suspend", suspend);
      JSONArray detailArrayJson = new JSONArray();
      if (detail != null)
        for (Field field: detail)
          detailArrayJson.put(field.toJson());
      notificationJson.put("details", detailArrayJson);
      JSONArray scriptsArrayJson = new JSONArray();
      if (scripts != null)
        for (NameValue<ScriptDefinition> nameValue: scripts)
          scriptsArrayJson.put(nameValue.value.toJson());
      notificationJson.put("scripts", scriptsArrayJson);
    } catch (JSONException e){
      log.error("Unable to output NotificationInstance as JSONObject - " + e.getMessage());
      e.printStackTrace();
    }
    return notificationJson;
  }
  
  public String toString(){
    return toJson().toString();
  }
}
