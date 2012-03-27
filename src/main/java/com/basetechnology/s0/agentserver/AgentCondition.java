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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.JsonUtils;

public class AgentCondition {
  public String name;
  public String description;
  public String interval;
  public String condition;
  public String script;
  public boolean enabled;
  
  public AgentCondition(String name, String description, String interval, String condition, String script, boolean enabled){
    this.name = name;
    this.description = description;
    this.interval = interval;
    this.condition = condition;
    this.script = script;
    this.enabled = enabled;
  }

  static public AgentCondition fromJson(String timerString) throws JSONException, AgentServerException {
    return fromJson(new JSONObject(timerString));
  }

  static public AgentCondition fromJson(JSONObject conditionJson) throws AgentServerException {
    // TODO: Whether empty fields should be null or empty strings
    String name = conditionJson.optString("name", "");
    String description = conditionJson.optString("description", "");
    String interval = conditionJson.optString("interval");
    if (! conditionJson.has("condition"))
      throw new AgentServerException("Condition condition expression is missing");
    String condition = conditionJson.optString("condition");
    String script = conditionJson.optString("script","");
    boolean enabled = conditionJson.optBoolean("enabled",true);
    JsonUtils.validateKeys(conditionJson, "Agent condition", new ArrayList<String>(Arrays.asList(
        "name", "description", "interval", "condition", "script", "enabled")));
    return new AgentCondition(name, description, interval, condition, script, enabled);
  }
  
  public long getInterval(AgentInstance agentInstance) throws AgentServerException {
    return agentInstance.evaluateExpressionLong(interval);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject timerJson = new JSONObject();
    timerJson.put("name", name);
    timerJson.put("description", description);
    timerJson.put("interval", interval);
    timerJson.put("condition", condition);
    timerJson.put("script", script);
    timerJson.put("enabled", enabled);
    return timerJson;
  }

  public String toString(){
    return name + ": " + interval + " ms. - \"" + condition + "\" - \"" + description + "\" - \"" + script + "\" " + (enabled ? "(enabled)" : "(disabled)");
  }

}
