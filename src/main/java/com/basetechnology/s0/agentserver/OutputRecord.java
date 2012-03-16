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

import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonListMap;

public class OutputRecord {
  public long time;
  public int sequenceNumber;
  public SymbolValues output;

  public OutputRecord(long time, int sequenceNumber, SymbolValues output){
    this.time = time;
    this.sequenceNumber = sequenceNumber;
    this.output = output.clone();
  }
  
  public JSONObject toJson() throws AgentServerException{
    try {
    JSONObject outputJson = new JsonListMap();
    outputJson.put("sequence", sequenceNumber);
    outputJson.put("time", DateUtils.toRfcString(time));
    outputJson.put("output", output.toJson());
    return outputJson;
    } catch (JSONException e){
      e.printStackTrace();
      throw new AgentServerException("JSON exception generating output record - " + e.getMessage());
    }
  }
  
  public String toString(){
    return "Output #" + sequenceNumber + " at " + DateUtils.toString(time) + " - " + output.toString();
  }
}
