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

public class BooleanGoal extends Goal {
  final public static String type = "greater_goal";
  public String booleanExpression;

  public BooleanGoal(String name, String description, String booleanExpression){
    super(name, description);
    this.booleanExpression = booleanExpression;
  }

  public String getType(){
    return type;
  }

  public static Goal fromJson(JSONObject goalJson, String name, String description) throws AgentServerException {
    String booleanExpression = goalJson.optString("boolean_expression");
    JsonUtils.validateKeys(goalJson, "Boolean goal", new ArrayList<String>(Arrays.asList(
        "type", "name", "description", "boolean_expression")));
    return new BooleanGoal(name, description, booleanExpression);
  }
  
  public String otherJson(){
    return "\"boolean_expression\": \"" + booleanExpression + "\"";
  }

}
