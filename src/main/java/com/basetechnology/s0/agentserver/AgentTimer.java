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

import org.json.JSONException;
import org.json.JSONObject;

public class AgentTimer {
  public String name;
  public String description;
  public long interval;
  public String script;
  public boolean enabled;
  
  public AgentTimer(String name, String description, long interval, String script, boolean enabled){
    this.name = name;
    this.description = description;
    this.interval = interval;
    this.script = script;
    this.enabled = enabled;
  }

  static public AgentTimer fromJson(String timerString) throws JSONException, AgentServerException {
    return fromJson(new JSONObject(timerString));
  }

  static public AgentTimer fromJson(JSONObject timerJson) throws AgentServerException {
    // TODO: Whether empty fields should be null or empty strings
    String name = timerJson.optString("name", "");
    String description = timerJson.optString("description", "");
    if (! timerJson.has("interval"))
      throw new AgentServerException("Timer interval is missing");
    long interval = timerJson.optLong("interval");
    if (interval <= 0)
      throw new AgentServerException("Timer interval may not be zero or negative");
    String script = timerJson.optString("script","");
    boolean enabled = timerJson.optBoolean("enabled",true);
    return new AgentTimer(name, description, interval, script, enabled);
  }
  
  public JSONObject toJson() throws JSONException {
    JSONObject timerJson = new JSONObject();
    timerJson.put("name", name);
    timerJson.put("description", description);
    timerJson.put("interval", interval);
    timerJson.put("script", script);
    timerJson.put("enabled", enabled);
    return timerJson;
  }

  public String toString(){
    return name + ": " + interval + " ms. - \"" + description + "\" - \"" + script + "\" " + (enabled ? "(enabled)" : "(disabled)");
  }

}
