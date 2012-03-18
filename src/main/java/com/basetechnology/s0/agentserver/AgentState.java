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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.notification.NotificationHistory;
import com.basetechnology.s0.agentserver.notification.NotificationInstance;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.runtime.ExceptionInfo;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonUtils;
import com.basetechnology.s0.agentserver.util.ListMap;

public class AgentState {
  static final Logger log = Logger.getLogger(AgentState.class);
  public long time;
  public SymbolManager symbolManager;
  public SymbolValues parameterValues;
  public SymbolValues inputValues;
  public SymbolValues memoryValues;
  public SymbolValues outputValues;
  public List<ExceptionInfo> exceptionHistory;
  public long lastDismissedExceptionTime;
  public ListMap<String, NotificationInstance> notifications;
  public NotificationHistory notificationHistory;

  // TODO: Need to capture an object value
  // TODO: Keep ref to original object plus saved value(s)

  public AgentState(
      long time,
      SymbolManager symbolManager,
      SymbolValues parameterValues,
      SymbolValues inputValues,
      SymbolValues memoryValues,
      SymbolValues outputValues,
      List<ExceptionInfo> exceptionHistory,
      long lastDismissedExceptionTime,
      ListMap<String, NotificationInstance> notifications,
      NotificationHistory notificationHistory){
    this.time = time;
    this.symbolManager = symbolManager;
    this.parameterValues = parameterValues;
    this.inputValues = inputValues;
    this.memoryValues = memoryValues;
    this.outputValues = outputValues;
    this.exceptionHistory = exceptionHistory;
    this.lastDismissedExceptionTime = lastDismissedExceptionTime;
    this.notifications = notifications;
    this.notificationHistory = notificationHistory;
  }

  public boolean equalValues(AgentState other){
    // TODO: Consider whether or not input values should participate in state change detection
    return parameterValues.equals(other.parameterValues) &&
        //inputValues.equals(other.inputValues) &&
        memoryValues.equals(other.memoryValues) &&
        outputValues.equals(other.outputValues) &&
        notifications.equals(other.notifications) &&
        notificationHistory.equals(other.notificationHistory);    
  }

  static public AgentState fromJson(JSONObject stateJson, SymbolManager symbolManager) throws AgentServerException, SymbolException, ParseException {
    // Parse the timestamp
    long time = 0;
    try {
      time = DateUtils.parseIsoString(stateJson.optString("time"));
    } catch (ParseException e) {
      throw new AgentServerException("Error parsing timestamp for agent state ('" + stateJson.optString("time") + "'): " + e.getMessage());
    }

    // Parse parameter values
    SymbolValues parameters = null;
    if (stateJson.has("parameters"))
      parameters = SymbolValues.fromJson(symbolManager.getSymbolTable("parameters"), stateJson.optJSONObject("parameters"));
    else
      parameters = new SymbolValues();

    // Parse input values
    SymbolValues inputs = null;
    if (stateJson.has("inputs"))
      inputs = SymbolValues.fromJson(symbolManager.getSymbolTable("inputs"), stateJson.optJSONObject("inputs"));
    else
      inputs = new SymbolValues();

/*
    // Parse input values
    SymbolValues events = null;
    if (stateJson.has("events"))
      events = SymbolValues.fromJson(symbolManager.getSymbolTable("events"), stateJson.optJSONObject("events"));
    else
      events = new SymbolValues();
*/
    
    // Parse memory values
    SymbolValues memory = null;
    if (stateJson.has("memory"))
      memory = SymbolValues.fromJson(symbolManager.getSymbolTable("memory"), stateJson.optJSONObject("memory"));
    else
      memory = new SymbolValues();

    // Parse output values
    SymbolValues outputs = null;
    if (stateJson.has("outputs"))
      outputs = SymbolValues.fromJson(symbolManager.getSymbolTable("outputs"), stateJson.optJSONObject("outputs"));
    else
      outputs = new SymbolValues();

    // Parse exception history
    List<ExceptionInfo> exceptions = new ArrayList<ExceptionInfo>();
    if (stateJson.has("exceptions")){
      JSONArray exceptionsJson = stateJson.optJSONArray("exceptions");
      int numExceptions = exceptionsJson.length();
      for (int i = 0; i < numExceptions; i++)
        exceptions.add(ExceptionInfo.fromJson(exceptionsJson.optJSONObject(i)));
    }

    String lde = stateJson.optString("last_dismissed_exception");
    long lastDismissedException = lde == null || lde.length() == 0 ? 0 : DateUtils.parseRfcString(lde);

    // Parse notifications
    ListMap<String, NotificationInstance> notifications = null;
    if (stateJson.has("notifications")){
      notifications = new ListMap<String, NotificationInstance>();
      JSONArray notificationsJson = stateJson.optJSONArray("notifications");
      int numNotifications = notificationsJson.length();
      for (int i = 0; i < numNotifications; i++){
        NotificationInstance notificationInstance = NotificationInstance.fromJson(null, notificationsJson.optJSONObject(i));
        notifications.put(notificationInstance.definition.name, notificationInstance);
      }
    }

    // Parse notification history
    NotificationHistory notificationHistory = null;
    if (stateJson.has("notifications")){
      JSONArray notificationsJson = stateJson.optJSONArray("notifications");
      notificationHistory = NotificationHistory.fromJson(null, notificationsJson);
    }

    // Validate keys
    JsonUtils.validateKeys(stateJson, "Agent state", new ArrayList<String>(Arrays.asList(
        "time", "parameters", "inputs", "memory", "outputs",
        "exceptions", "last_dismissed_exception", "notifications", "notification_history")));

    // Generate the agent state object
    AgentState newState = new AgentState(time, symbolManager, parameters, inputs, memory,
        outputs, exceptions, lastDismissedException, notifications, notificationHistory);

    // Return the agent state object
    return newState;
  }

  public JSONObject toJson() throws AgentServerException {
    try {
      JSONObject stateJson = new JSONObject();

      // Timestamp for agent state
      stateJson.put("time", DateUtils.toIsoString(time));

      // Parameter states
      JSONObject parameterValuesJson = new JSONObject();
      for (Symbol parameter: parameterValues.keySet()){
        String parameterName = parameter.name;
        parameterValuesJson.put(parameterName, parameterValues.get(parameter).toJsonObject());
      }
      stateJson.put("parameters", parameterValuesJson);

      // Input states
      JSONObject inputValuesJson = new JSONObject();
      for (Symbol inputs: inputValues.keySet()){
        String inputsName = inputs.name;
        inputValuesJson.put(inputsName, inputValues.get(inputs).toJsonObject());
      }
      stateJson.put("inputs", inputValuesJson);

      // Memory states
      JSONObject memoryValuesJson = new JSONObject();
      for (Symbol memory: memoryValues.keySet()){
        String memoryName = memory.name;
        memoryValuesJson.put(memoryName, memoryValues.get(memory).toJsonObject());
      }
      stateJson.put("memory", memoryValuesJson);

      // Output states
      JSONObject outputValuesJson = new JSONObject();
      for (Symbol output: outputValues.keySet()){
        String outputName = output.name;
        outputValuesJson.put(outputName, outputValues.get(output).toJsonObject());
        //log.info("AgentState.toJson: outputName: " + outputName + " value: " + outputValues.get(output) + " value.toJson: " + outputValues.get(output).toJsonObject());
      }
      stateJson.put("outputs", outputValuesJson);
      //log.info("AgentState.toJson: outputs " + outputValuesJson.toString());
      //log.info("AgentState.toJson: " + stateJson.toString());

      // Exception history
      JSONArray exceptionsJson = new JSONArray();
      for (ExceptionInfo exceptionInfo: exceptionHistory)
        exceptionsJson.put(exceptionInfo.toJson());
      stateJson.put("exceptions", exceptionsJson);
      stateJson.put("last_dismissed_exception",
          lastDismissedExceptionTime == 0 ? "" : DateUtils.toRfcString(lastDismissedExceptionTime));

      // Notifications
      JSONArray notificationsJson = new JSONArray();
      for (String notificationName: notifications)
        notificationsJson.put(notifications.get(notificationName).toJson());
      stateJson.put("notifications", notificationsJson);

      // Notification history
      stateJson.put("notification_history", notificationHistory.toJson());

      return stateJson;
    } catch (JSONException e){
      e.printStackTrace();
      throw new AgentServerException("JSON Exception in AgentState.toJson - " + e.getMessage());
    }
  }

  public Value getParameter(String name) throws SymbolException {
    return parameterValues.get(symbolManager.get("parameters", name));
  }

  public Value getInput(String name) throws SymbolException {
    return inputValues.get(symbolManager.get("inputs", name));
  }

  public Value getMemory(String name) throws SymbolException {
    return memoryValues.get(symbolManager.get("memory", name));
  }

  public Value getOutput(String name) throws SymbolException {
    return outputValues.get(symbolManager.get("outputs", name));
  }

  public String toString(){
    try {
      return toJson().toString();
    } catch (AgentServerException e){
      log.error("Unable to output AgentState as string - " + e.getMessage());
      e.printStackTrace();
      return "[AgentState: Unable to output AgentState as string - " + e.getMessage() + "]";
    }
  }
}
