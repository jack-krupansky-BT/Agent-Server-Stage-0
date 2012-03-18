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

import java.text.ParseException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonListMap;

public class NotificationRecord {
  static final Logger log = Logger.getLogger(NotificationRecord.class);

  public long time;
  public int sequenceNumber;
  public NotificationInstance notificationInstance;

  public NotificationRecord(long time, int sequenceNumber, NotificationInstance notificationInstance){
    this.time = time;
    this.sequenceNumber = sequenceNumber;
    this.notificationInstance = notificationInstance;
  }

  static public NotificationRecord fromJson(AgentInstance agentInstance, JSONObject notificationRecordJson) throws AgentServerException, SymbolException{
    int sequenceNumber = notificationRecordJson.optInt("sequence");

    String timeString = notificationRecordJson.optString("time", null);
    long time = -1;
    try {
      time = timeString != null ? DateUtils.parseRfcString(timeString): -1;
    } catch (ParseException e){
      throw new AgentServerException("Unable to parse time for notification record: " + timeString + " - " + e.getMessage());
    }

    NotificationInstance notificationInstance = NotificationInstance.fromJson(agentInstance, notificationRecordJson.optJSONObject("notification"));
    
    return new NotificationRecord(time, sequenceNumber, notificationInstance);
  }
  
  public JSONObject toJson(){
    try {
    JSONObject outputJson = new JsonListMap();
    outputJson.put("sequence", sequenceNumber);
    outputJson.put("time", DateUtils.toRfcString(time));
    outputJson.put("notification", notificationInstance.toJson());
    return outputJson;
    } catch (JSONException e){
      log.info("Exception generating JSON for output record - " + e.getMessage());
      e.printStackTrace();
      return new JSONObject();
    }
  }
  
  public String toString(){
    return "Notification #" + sequenceNumber + " at " + DateUtils.toString(time) + " - " + notificationInstance.toString();
  }

}
