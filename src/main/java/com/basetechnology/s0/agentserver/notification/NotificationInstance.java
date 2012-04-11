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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.script.intermediate.ObjectTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.runtime.value.FieldValue;
import com.basetechnology.s0.agentserver.script.runtime.value.MapValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.JsonUtils;

public class NotificationInstance {
  static final Logger log = Logger.getLogger(NotificationInstance.class);
  public AgentInstance agentInstance;
  public NotificationDefinition definition;
  public MapValue details;
  public boolean pending;
  static public final List<String> responses = Arrays.asList(
      "ok", "yes", "no", "accept", "decline", "confirm", "cancel", "abort", "fail", "retry",
      "ignore", "skip", "pass", "choice");
  public String response;
  public String responseChoice;
  public String comment;
  public long timeout;
  public long timeNotified;
  public long timeResponse;

  public NotificationInstance(AgentInstance agentInstance, NotificationDefinition definition, MapValue details){
    this(agentInstance, definition, details, false, "", "", "", 0, 0, 0);
  }

  public NotificationInstance(AgentInstance agentInstance, NotificationDefinition definition,
      MapValue details, boolean pending, String response, String responseChoice, String comment, long timeout,
      long timeNotified, long timeResponse){
    this.agentInstance = agentInstance;
    this.definition = definition;
    this.details = details;
    this.pending = pending;
    this.response = response;
    this.responseChoice = responseChoice;
    this.comment = comment;
    this.timeout = timeout;
    this.timeNotified = timeNotified;
    this.timeResponse = timeResponse;
  }

  static public NotificationInstance fromJson(AgentInstance agentInstance, String notificationInstanceString) throws JSONException, AgentServerException, SymbolException {
    return fromJson(agentInstance, new JSONObject(notificationInstanceString));
  }

  public NotificationInstance clone(){
    return new NotificationInstance(agentInstance, definition, details == null ? null : details.clone(), pending, response, responseChoice, comment, timeout, timeNotified, timeResponse);
  }
  
  static public NotificationInstance fromJson(AgentInstance agentInstance, JSONObject notificationInstanceJson) throws AgentServerException, SymbolException{
    // TODO: Whether empty fields should be null of empty strings
    String definitionName = notificationInstanceJson.optString("definition", "");
    if (definitionName == null)
      throw new AgentServerException("Missing notification 'definition' name");
    if (definitionName.trim().length() == 0)
      throw new AgentServerException("Empty notification 'definition' name");
    NotificationDefinition definition =
        agentInstance.agentDefinition.notifications.get(definitionName);
    if (definition == null)
      throw new AgentServerException("Undefined notification 'definition' name: " + definitionName);
    
    // Parse the detail values
    MapValue details = null;
    String invalidDetailNames = "";
    SymbolManager symbolManager = new SymbolManager();
    SymbolTable symbolTable = symbolManager.getSymbolTable("details");
    JSONObject detailsJson = null;
    if (notificationInstanceJson.has("details")){
      // Parse the detail values
      detailsJson = notificationInstanceJson.optJSONObject("details");
      List<FieldValue> detailFieldValues = new ArrayList<FieldValue>();
      for(Iterator<String> it = detailsJson.keys(); it.hasNext(); ){
        String key = it.next();
        if (! definition.detail.containsKey(key))
          invalidDetailNames += (invalidDetailNames.length() > 0 ? ", " : "") + key;
        detailFieldValues.add(new FieldValue(key, Value.toValueNode(detailsJson.opt(key))));
      }
      details = new MapValue(ObjectTypeNode.one, (List<Value>)(Object)detailFieldValues);
      
      // Validate that they are all valid agent definition notification detail fields
      if (invalidDetailNames.length() > 0)
        throw new AgentServerException("Detail names for notification '" + definitionName + "' instance for agent instance " + agentInstance.name + " are not defined for the notification definition: " + invalidDetailNames);
    }

    boolean pending = notificationInstanceJson.optBoolean("pending");
    
    String response = notificationInstanceJson.optString("response", "");

    String responseChoice = notificationInstanceJson.optString("response_choice", "");

    long timeout = notificationInstanceJson.optLong("timeout", -1);

    String comment = notificationInstanceJson.optString("comment", "");

    String timeNotifiedString = notificationInstanceJson.optString("time_notified", null);
    long timeNotified = -1;
    try {
      timeNotified = timeNotifiedString != null ? DateUtils.parseRfcString(timeNotifiedString): -1;
    } catch (ParseException e){
      throw new AgentServerException("Unable to parse notification time ('" + timeNotifiedString + "') - " + e.getMessage());
    }
    
    String timeResponseString = notificationInstanceJson.optString("time_response", null);
    long timeResponse = -1;
    try {
      timeResponse = timeResponseString != null ? DateUtils.parseRfcString(timeResponseString): -1;
    } catch (ParseException e){
      throw new AgentServerException("Unable to parse notification response time ('" + timeResponseString + "') - " + e.getMessage());
    }
    
    // Validate keys
    JsonUtils.validateKeys(notificationInstanceJson, "Notification Instance", new ArrayList<String>(Arrays.asList(
        "name", "details", "response", "response_choice", "comment", "timeout", "time_notified", "time_response")));

    return new NotificationInstance(agentInstance, definition, details, pending, response, responseChoice, comment, timeout, timeNotified, timeResponse);
  }

  public JSONObject toJson(){
    JSONObject notificationJson = new JsonListMap();
    try {
      notificationJson.put("name", definition.name);
      notificationJson.put("details", details == null ? new JSONObject() : details.toJson());
      notificationJson.put("pending", pending);
      notificationJson.put("timeout", timeout);
      notificationJson.put("response", response);
      notificationJson.put("response_choice", responseChoice);
      notificationJson.put("comment", comment);
      notificationJson.put("time_notified", timeNotified > 0 ? DateUtils.toRfcString(timeNotified) : "");
      notificationJson.put("time_response", timeResponse > 0 ? DateUtils.toRfcString(timeResponse) : "");
    } catch (JSONException e){
      log.error("Unable to output NotificationInstance as JSONObject - " + e.getMessage());
      e.printStackTrace();
    }
    return notificationJson;
  }
  
  public String toString(){
    return toJson().toString();
  }
}
