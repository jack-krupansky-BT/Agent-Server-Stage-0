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
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;

public class OutputHistory implements Iterable<OutputRecord> {
  public final static int MAX_HISTORY_RECORDS = 100;
  public int maxHistoryRecords;
  public int sequenceNumber;
  public List<OutputRecord> outputHistory;

  public OutputHistory(){
    this(MAX_HISTORY_RECORDS);
  }

  public OutputHistory(int maxHistoryRecords){
    this.maxHistoryRecords = maxHistoryRecords;
    this.sequenceNumber = 0;
    outputHistory = new ArrayList<OutputRecord>();
  }
  
  public OutputRecord add(SymbolValues outputValues){
    return add(outputValues, System.currentTimeMillis());
  }
  
  public OutputRecord add(SymbolValues outputValues, long time){
    // Create a new output record
    OutputRecord outputRecord = new OutputRecord(time, ++sequenceNumber, outputValues.clone());
    
    // Age off older output records in the history
    if (outputHistory.size() >= maxHistoryRecords)
      outputHistory.remove(0);
    
    // Add the new output record
    outputHistory.add(outputRecord);
    
    // Return the new output record
    return outputRecord;
  }
  
  public void clear(){
    outputHistory.clear();
  }
  
  public OutputRecord get(int index){
    return outputHistory.get(index);
  }
  
  public OutputRecord getLatest(){
    int numRecords = outputHistory.size();
    if (numRecords > 0)
      return outputHistory.get(numRecords - 1);
    else
      return null;
  }
  
  public Iterator<OutputRecord> iterator(){
    return outputHistory.iterator();
  }
  
  public int size(){
    return outputHistory.size();
  }
  
  public JSONArray toJson() throws AgentServerException {
    return toJson(outputHistory.size());
  }
  
  public JSONArray toJson(int numRecords) throws AgentServerException {
    JSONArray historyJson = new JSONArray();
    int numHistoryRecords = outputHistory.size();
    int startRecord = numHistoryRecords - numRecords;
    if (startRecord < 0)
      startRecord = 0;
    for (int i = startRecord; i < numHistoryRecords; i++){
      historyJson.put(outputHistory.get(i).toJson());
    }
    return historyJson;
  }
  
  public String toString(){
    try {
      return toJson().toString();
    } catch (AgentServerException e) {
      e.printStackTrace();
      return "Generated exception: " + e.getMessage();
    }
  }
}
