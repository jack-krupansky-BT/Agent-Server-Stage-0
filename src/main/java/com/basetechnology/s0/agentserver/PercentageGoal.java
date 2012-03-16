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

import org.json.JSONObject;

import com.basetechnology.s0.agentserver.util.JsonUtils;

public class PercentageGoal extends Goal {
  final public static String type = "greater_goal";
  public String percentageExpression;
  public int threshold;

  public PercentageGoal(String name, String description, String percentageExpression, int threshold){
    super(name, description);
    this.percentageExpression = percentageExpression;
    this.threshold = threshold;
  }

  public String getType(){
    return type;
  }

  public static Goal fromJson(JSONObject goalJson, String name, String description) throws AgentServerException {
    String percentageExpression = goalJson.optString("percentage_expression");
    int threshold = goalJson.optInt("threshold", 100);
    JsonUtils.validateKeys(goalJson, "Percentage goal", new ArrayList<String>(Arrays.asList(
        "type", "name", "description", "percentage_expression", "threshold")));
    return new PercentageGoal(name, description, percentageExpression, threshold);
  }
  
  public String otherJson(){
    return "\"percentage_expression\": \"" + percentageExpression + "\", \"threshold\": " + threshold;
  }
}
