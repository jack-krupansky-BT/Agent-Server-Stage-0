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

import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class Output {
  public String name;
  public String description;
  public Map<String, String> attributes;
  public long time;
  
  public Output(String name, String description, Map<String, String> attributes){
    this.name = name;
    this.description = description;
    this.attributes = attributes;
  }
  
  public JSONObject toJson() throws JSONException {
    JSONObject eventJson = new JSONObject();
    eventJson.put("name", name);
    eventJson.put("description", description);
    eventJson.put("attributes", attributes);
    return eventJson;
  }
  
  public Map<String, Object> getState(){
    // TODO: time should be when event occurred
    time = System.currentTimeMillis();
    Map<String, Object> state = new HashMap<String, Object>();
    state.put("time", time);
    Map<String, String> attributesCopy = new HashMap<String, String>();
    for (String attributeName: attributes.keySet())
      attributesCopy.put(attributeName, attributes.get(attributeName));
    state.put("attributes", attributesCopy);
    return state;
  }
}
