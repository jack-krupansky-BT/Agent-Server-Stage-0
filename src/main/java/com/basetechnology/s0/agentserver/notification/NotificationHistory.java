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

package com.basetechnology.s0.agentserver.notification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.util.ListMap;

public class NotificationHistory implements Iterable<NotificationRecord> {
  public final static int MAX_HISTORY_RECORDS = 100;
  public int maxHistoryRecords;
  public int sequenceNumber;
  public List<NotificationRecord> notificationHistory;

  public NotificationHistory(){
    this(MAX_HISTORY_RECORDS);
  }

  public NotificationHistory(int maxHistoryRecords){
    this.maxHistoryRecords = maxHistoryRecords;
    this.sequenceNumber = 0;
    notificationHistory = new ArrayList<NotificationRecord>();
  }

  public NotificationHistory clone(){
    NotificationHistory notificationHistory = new NotificationHistory();
    for (NotificationRecord notificationRecord: this)
      notificationHistory.add(notificationRecord);
    return notificationHistory;
  }

  public boolean equals(NotificationHistory other){
    int numElements = size();
    int numOtherElements = other.size();
    if (numElements != numOtherElements)
      return false;
    for (int i = 0; i < numElements; i++)
      if (get(i).equals(other.get(i)))
        return false;
    return true;
  }
  
  public NotificationRecord add(NotificationInstance notificationInstance){
    return add(notificationInstance, System.currentTimeMillis());
  }
  
  public NotificationRecord add(NotificationInstance notificationInstance, long time){
    // Create a new output record
    NotificationRecord outputRecord = new NotificationRecord(time, ++sequenceNumber, notificationInstance.clone());
    
    // Add and return the new output record
    return add(outputRecord);
  }
  
  public NotificationRecord add(NotificationRecord notificationRecord){
    // Age off older output records in the history
    if (notificationHistory.size() >= maxHistoryRecords)
      notificationHistory.remove(0);

    // Add the new output record
    notificationHistory.add(notificationRecord);
    
    // Return the new output record
    return notificationRecord;
  }
  
  public void clear(){
    notificationHistory.clear();
  }
  
  public NotificationRecord get(int index){
    return notificationHistory.get(index);
  }
  
  public NotificationRecord getLatest(){
    int numRecords = notificationHistory.size();
    if (numRecords > 0)
      return notificationHistory.get(numRecords - 1);
    else
      return null;
  }
  
  public Iterator<NotificationRecord> iterator(){
    return notificationHistory.iterator();
  }
  
  public int size(){
    return notificationHistory.size();
  }
  
  public JSONArray toJson(){
    return toJson(notificationHistory.size());
  }

  public static NotificationHistory fromJson(AgentInstance agentInstance, JSONArray notificationHistoryJson) throws AgentServerException, SymbolException{
    int numHistoryRecords = notificationHistoryJson.length();
    NotificationHistory notificationHistory = new NotificationHistory();
    for (int i = 0; i < numHistoryRecords; i++){
      notificationHistory.add(
          NotificationRecord.fromJson(agentInstance, notificationHistoryJson.optJSONObject(i)));
    }
    return notificationHistory;
  }
  
  public JSONArray toJson(int numRecords){
    JSONArray historyJson = new JSONArray();
    int numHistoryRecords = notificationHistory.size();
    int startRecord = numHistoryRecords - numRecords;
    if (startRecord < 0)
      startRecord = 0;
    for (int i = startRecord; i < numHistoryRecords; i++){
      historyJson.put(notificationHistory.get(i).toJson());
    }
    return historyJson;
  }
  
  public String toString(){
    return toJson().toString();
  }
}
