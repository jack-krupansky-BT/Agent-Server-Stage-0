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

import org.json.JSONArray;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.util.JsonUtils;

public class GreaterGoal extends Goal {
  final public static String type = "greater_goal";
  public List<Goal> subgoals;
  public double percentage;
  public double minimumNumber;

  public GreaterGoal(String name, String description, List<Goal> subgoals, double percentage, int minimumNumber){
    super(name, description);
    this.subgoals = new ArrayList<Goal>(subgoals);
    this.percentage = percentage;
    this.minimumNumber = minimumNumber;
  }

  public String getType(){
    return type;
  }

  public static Goal fromJson(JSONObject goalJson, String name, String description) throws AgentServerException {
    List<Goal> subgoals = new ArrayList<Goal>();
    if (goalJson.has("subgoals")){
      Object object = goalJson.opt("subgoals");
      if (! (object instanceof JSONArray))
        throw new AgentServerException("Subgoals must be a JSON array");
      JSONArray subgoalsJson = goalJson.optJSONArray("subgoals");
      int numSubgoals = subgoalsJson.length();
      for (int i = 0; i < numSubgoals; i++){
        JSONObject subgoalJson = subgoalsJson.optJSONObject(i);
        Goal subgoal = Goal.fromJson(subgoalJson);
        subgoals.add(subgoal);
      }
    }
    double percentage = goalJson.optDouble("percentage", 100.0);
    int minimumNumber = goalJson.optInt("minimum_number", -1);
    JsonUtils.validateKeys(goalJson, "Greater goal", new ArrayList<String>(Arrays.asList(
        "type", "name", "description", "subgoals", "percentage", "minimum_number")));
    return new GreaterGoal(name, description, subgoals, percentage, minimumNumber);
  }
  
  public String otherJson(){
    StringBuilder sb = new StringBuilder("\"subgoals\": [");
    for (Goal subgoal: subgoals){
      if (sb.length() > 1)
        sb.append(", ");
      sb.append(subgoal.toJson());
    }
    sb.append(']');
    if (percentage != 100)
      sb.append(", \"percentage\": " + percentage);
    if (minimumNumber != -1)
      sb.append(", \"minimum_number\": " + minimumNumber);
    return sb.toString();
  }

}
