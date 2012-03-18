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

package com.basetechnology.s0.agentserver.goals;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.util.JsonUtils;

public class Goal {
  public String name;
  public String description;
  
  public Goal(String name, String description){
    this.name = name;
    this.description = description;
  }

  public String getType(){
    return "goal";
  }
  
  public static Goal fromJson(JSONObject goalJson) throws AgentServerException {
    if (! goalJson.has("type"))
      throw new AgentServerException("Missing type in goal JSON");
    String type = goalJson.optString("type");
    if (type.equalsIgnoreCase(BooleanGoal.type))
      return BooleanGoal.fromJson(goalJson);
    else if (type.equalsIgnoreCase(GreaterGoal.type))
      return GreaterGoal.fromJson(goalJson);
    else if (type.equalsIgnoreCase(PercentageGoal.type))
      return PercentageGoal.fromJson(goalJson);
    else {
      String name = goalJson.optString("name");
      String description = goalJson.optString("description");
      JsonUtils.validateKeys(goalJson, "Goal", new ArrayList<String>(Arrays.asList(
          "type", "name", "description")));
      return new Goal(name, description);
    }
  }
  
  public String toJson(){
    String otherJson = otherJson();
    return "{\"name\": \"" + name + "\", \"type\": \"" + getType() +
        "\", \"description\": \"" + description + "\"" +
        (otherJson == null ? "" : ", " + otherJson) + "}";
  }
  
  public String otherJson(){
    return null;
  }
  
  public String toString(){
    return toJson(); 
  }
}
