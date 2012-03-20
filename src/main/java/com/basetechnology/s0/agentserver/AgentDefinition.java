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
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.field.Field;
import com.basetechnology.s0.agentserver.field.FieldList;
import com.basetechnology.s0.agentserver.goals.Goal;
import com.basetechnology.s0.agentserver.notification.NotificationDefinition;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.JsonUtils;
import com.basetechnology.s0.agentserver.util.NameValue;
import com.basetechnology.s0.agentserver.util.NameValueList;

public class AgentDefinition {
  static final Logger log = Logger.getLogger(AgentDefinition.class);

  public final static long DEFAULT_TRIGGER_INTERVAL = 50;
  public final static String DEFAULT_TRIGGER_INTERVAL_EXPRESSION = Long.toString(DEFAULT_TRIGGER_INTERVAL);
  public final static long DEFAULT_MINIMUM_TRIGGER_INTERVAL = 5;
  public final static String DEFAULT_MINIMUM_TRIGGER_INTERVAL_EXPRESSION = Long.toString(DEFAULT_MINIMUM_TRIGGER_INTERVAL);
  public final static long DEFAULT_REPORTING_INTERVAL = 200;
  public final static String DEFAULT_REPORTING_INTERVAL_EXPRESSION = Long.toString(DEFAULT_REPORTING_INTERVAL);
  public final static long DEFAULT_MINIMUM_REPORTING_INTERVAL = 5;
  public final static String DEFAULT_MINIMUM_REPORTING_INTERVAL_EXPRESSION = Long.toString(DEFAULT_MINIMUM_REPORTING_INTERVAL);
  public long timeCreated;
  public long timeModified;
  public AgentServer agentServer;
  public User user;
  public String name;
  public String description;
  public FieldList parameters;
  public DataSourceReferenceList inputs;
  public NameValueList<AgentTimer> timers;
  public NameValueList<AgentCondition> conditions;
  public NameValueList<ScriptDefinition> scripts;
  public FieldList scratchpad;
  public FieldList memory;
  public FieldList outputs;
  public List<Goal> goals;
  public NameValueList<NotificationDefinition> notifications;
  public String triggerIntervalExpression;
  public String reportingIntervalExpression;
  public SymbolManager symbolManager;
  public Boolean enabled;

  public AgentDefinition(AgentServer agentServer) throws SymbolException, RuntimeException {
    this.timeCreated = System.currentTimeMillis();
    this.timeModified = this.timeCreated;
    this.agentServer = agentServer;
    this.user = User.noUser;
    this.parameters = new FieldList();
    this.inputs = new DataSourceReferenceList();
    this.timers = new NameValueList<AgentTimer>();
    this.conditions = new NameValueList<AgentCondition>();
    this.scripts = new NameValueList<ScriptDefinition>();
    this.scratchpad = new FieldList();
    this.memory = new FieldList();
    this.notifications = new NameValueList<NotificationDefinition>();
    this.outputs = new FieldList();
    this.goals = new ArrayList<Goal>();
    this.triggerIntervalExpression = agentServer.getDefaultTriggerInterval();
    this.reportingIntervalExpression = agentServer.getDefaultReportingInterval();
    this.enabled = true;
  }

  public AgentDefinition(
      AgentServer agentServer,
      User user,
      String name,
      String description,
      FieldList parameters,
      DataSourceReferenceList inputs,
      NameValueList<AgentTimer> timers,
      NameValueList<AgentCondition> conditions,
      NameValueList<ScriptDefinition> scripts,
      FieldList scratchpad,
      FieldList memory,
      NameValueList<NotificationDefinition> notifications,
      FieldList outputs,
      List<Goal> goals,
      String triggerIntervalExpression,
      String reportingIntervalExpression,
      long timeCreated,
      long timeModified,
      Boolean enabled,
      boolean update) throws SymbolException, RuntimeException  {
    this.timeCreated = update ? timeCreated : (timeCreated > 0 ? timeCreated : System.currentTimeMillis());
    this.timeModified = update? timeModified : (timeModified > 0 ? timeModified : this.timeCreated);
    this.agentServer = agentServer;
    this.user = user != null ? user : update ? null : User.noUser;
    this.name = name;
    this.description = description;
    this.parameters = parameters != null ? parameters : update ? null : new FieldList();
    this.inputs = inputs != null ? inputs : update ? null : new DataSourceReferenceList();
    this.timers = timers != null ? timers : update ? null : new NameValueList<AgentTimer>();
    this.conditions = conditions != null ? conditions : update ? null : new NameValueList<AgentCondition>();
    this.scripts = scripts != null ? scripts : update ? null : new NameValueList<ScriptDefinition>();
    this.scratchpad = scratchpad != null ? scratchpad : update ? null : new FieldList();
    this.memory = memory != null ? memory : update ? null : new FieldList();
    this.notifications = notifications != null ? notifications : update ? null : new NameValueList<NotificationDefinition>();
    this.outputs = outputs != null ? outputs : update ? null : new FieldList();
    this.goals = goals != null ? goals : update ? null : new ArrayList<Goal>();
    this.triggerIntervalExpression = triggerIntervalExpression;
    this.reportingIntervalExpression = reportingIntervalExpression;
    this.enabled = enabled;
  }

  static public AgentDefinition fromJson(AgentServer agentServer, String agentJsonSource) throws AgentServerException, SymbolException, JSONException {
    return fromJson(agentServer, null, new JSONObject(agentJsonSource), false);
  }

  static public AgentDefinition fromJson(AgentServer agentServer, JSONObject agentJson) throws AgentServerException, SymbolException {
    return fromJson(agentServer, null, agentJson, false);
  }

  static public AgentDefinition fromJson(AgentServer agentServer, User user, JSONObject agentJson) throws AgentServerException, SymbolException {
    return fromJson(agentServer, user, agentJson, false);
  }
  
  static public AgentDefinition fromJson(AgentServer agentServer, User user, JSONObject agentJson, boolean update) throws AgentServerException, SymbolException {
    // Parse the JSON for the agent definition

    // If we have the user, ignore user from JSON
    if (user == null){
      String userId = agentJson.optString("user");
      if (userId == null || userId.trim().length() == 0)
        throw new AgentServerException("Agent definition user id ('user') is missing");
      user = agentServer.getUser(userId);
      if (user == User.noUser)
        throw new AgentServerException("Agent definition user id does not exist: '" + userId + "'");
    }

    // Parse agent definition name
    String agentDefinitionName = agentJson.optString("name");
    if (agentDefinitionName == null || agentDefinitionName.trim().length() == 0 && ! update)
      throw new AgentServerException("Agent definition name ('name') is missing");

    // Parse agent definition description
    String agentDescription = agentJson.optString("description");
    if (agentDescription == null || agentDescription.trim().length() == 0)
      agentDescription = "";
    //log.info("Adding new agent definition named: " + agentDefinitionName + " for user: " + user.id);
    
    // TODO: Parse comment
    
    AgentDefinitionList agentMap = agentServer.agentDefinitions.get(user.id);
    if (agentMap == null){
      agentMap = new AgentDefinitionList();
      agentServer.agentDefinitions.add(user.id, agentMap);
    }

    // Check if named agent definition already exists
    if (agentMap.containsKey(agentDefinitionName))
      throw new AgentServerException("Agent definition name already exists: '" + agentDefinitionName + "'");

    AgentDefinition agent = null;

    SymbolManager symbolManager = new SymbolManager();

    String invalidParameterNames = "";

    // Parse 'parameter' fields
    FieldList parameters = null;
    if (agentJson.has("parameters")){
      JSONArray parameterJson = agentJson.optJSONArray("parameters");
      parameters = new FieldList();
      int numparameter = parameterJson.length();
      for (int i = 0; i < numparameter; i++){
        JSONObject outputJson = parameterJson.optJSONObject(i);
        Field field = Field.fromJsonx(symbolManager.getSymbolTable("parameter"), outputJson);
        // TODO: give error for dup names
        parameters.add(field);
      }
    }

    // Parse inputs - data source references
    DataSourceReferenceList inputs = null;
    if (agentJson.has("inputs")){
      SymbolTable symbolTable = symbolManager.getSymbolTable("parameter_values");
      JSONArray inputsJson = agentJson.optJSONArray("inputs");
      inputs = new DataSourceReferenceList();
      int numInputs = inputsJson.length();
      for (int i = 0; i < numInputs; i++){
        Object element = inputsJson.opt(i);
        String dataSourceName = null;
        DataSourceReference dataSourceReference = null;
        if (element instanceof JSONObject){
          // Ignore empty data sources
          JSONObject elementJson = (JSONObject)element;
          if (elementJson.length() == 0)
            continue;

          // Parse the 'name' for data source
          if (! elementJson.has("name"))
            throw new AgentServerException("Inputs JSON object is missing 'name' key");
          dataSourceName = elementJson.optString("name");
          
          // Parse the 'data_source'
          if (! elementJson.has("data_source"))
            throw new AgentServerException("Inputs JSON object is missing 'data_source' key");
          String dataSourceDataSource = elementJson.optString("data_source");
          
          // Validate the data source reference
          AgentDefinition dataSourceDefinition = agentMap.get(dataSourceDataSource);
          if (dataSourceDefinition == null)
            throw new AgentServerException("Inputs JSON for data source name '" + dataSourceName + "' references data source '" + dataSourceDataSource + "' which is not a defined data source");
          
          // Parse the optional 'parameter_values'
          SymbolValues parameterValues = null;
          if (elementJson.has("parameter_values")){
            Object parametersObject = elementJson.opt("parameter_values");
            if (! (parametersObject instanceof JSONObject))
              throw new AgentServerException("Inputs parameters_values value expected JSON object but found " + parametersObject.getClass().getSimpleName());
            JSONObject parameterValuesJson = elementJson.optJSONObject("parameter_values");
            parameterValues = SymbolValues.fromJson(symbolTable, parameterValuesJson);
          } else
            parameterValues = new SymbolValues("parameters");

          // Validate that parameter value keys are all valid data source definition parameters
          Map<String, Value> treeMap = new TreeMap<String, Value>();
          for (Symbol symbol: parameterValues)
            treeMap.put(symbol.name, null);
          invalidParameterNames = "";
          for (String parameterName: treeMap.keySet())
            if (! dataSourceDefinition.parameters.containsKey(parameterName))
              invalidParameterNames += (invalidParameterNames.length() > 0 ? ", " : "") + parameterName;
          if (invalidParameterNames.length() > 0)
            throw new AgentServerException("Invalid parameter field names for inputs name '" + dataSourceName + "' for data source '" + agentDefinitionName + "': " + invalidParameterNames);

          // Construct the data source reference, including parameter values
          dataSourceReference = new DataSourceReference(dataSourceName, dataSourceDefinition, parameterValues);
        } else if (element instanceof String){
          String dataSourceDataSource = (String)element;
          dataSourceName = dataSourceDataSource;
          
          // Validate the data source reference
          AgentDefinition dataSourceDefinition = agentMap.get(dataSourceDataSource);
          if (dataSourceDefinition == null)
            throw new AgentServerException("Inputs JSON references data source '" + dataSourceDataSource + "' which is not a defined data source");

          // Construct the data source reference
          dataSourceReference = new DataSourceReference(dataSourceName, dataSourceDefinition);
        } else
          throw new AgentServerException("Expected JSON object or string for input data source, but encountered " + element.getClass().getSimpleName());
        
        // Add this data source reference to the list
        inputs.add(dataSourceReference);
      }
    }

    // Parse 'timers' list
    NameValueList<AgentTimer> timers = null;
    if (agentJson.has("timers")){
      timers = new NameValueList<AgentTimer>();
      JSONArray timersJson = agentJson.optJSONArray("timers");
      if (timersJson != null){
        int numScripts = timersJson.length();
        for (int i = 0; i < numScripts; i++){
          // Get JSON for next timer
          JSONObject timerJson = timersJson.optJSONObject(i);
          
          // Ignore empty timers
          if (! timerJson.keys().hasNext())
            continue;
          
          AgentTimer timerDefinition = AgentTimer.fromJson(timerJson);
          timers.put(timerDefinition.name, timerDefinition);
        }
      }
    }

    // Parse 'conditions' list
    NameValueList<AgentCondition> conditions = null;
    if (agentJson.has("conditions")){
      conditions = new NameValueList<AgentCondition>();
      JSONArray conditionsJson = agentJson.optJSONArray("conditions");
      if (conditionsJson != null){
        int numScripts = conditionsJson.length();
        for (int i = 0; i < numScripts; i++){
          // Get JSON for next condition
          JSONObject conditionJson = conditionsJson.optJSONObject(i);
          
          // Ignore empty conditions
          if (! conditionJson.keys().hasNext())
            continue;
          
          AgentCondition conditionDefinition = AgentCondition.fromJson(conditionJson);
          conditions.put(conditionDefinition.name, conditionDefinition);
        }
      }
    }

    // Parse 'notifications' list
    NameValueList<NotificationDefinition> notifications = null;
    if (agentJson.has("notifications")){
      notifications = new NameValueList<NotificationDefinition>();
      JSONArray notificationsJson = agentJson.optJSONArray("notifications");
      if (notificationsJson != null){
        int numScripts = notificationsJson.length();
        for (int i = 0; i < numScripts; i++){
          // Get JSON for next notification
          JSONObject notificationJson = notificationsJson.optJSONObject(i);
          
          // Ignore empty notifications
          if (! notificationJson.keys().hasNext())
            continue;
          
          NotificationDefinition notificationDefinition = NotificationDefinition.fromJson(notificationJson);
          notifications.put(notificationDefinition.name, notificationDefinition);
        }
      }
    }

    // Parse 'scripts' list
    NameValueList<ScriptDefinition> scripts = null;
    if (agentJson.has("scripts")){
      scripts = new NameValueList<ScriptDefinition>();
      JSONArray scriptsJson = agentJson.optJSONArray("scripts");
      if (scriptsJson != null){
        int numScripts = scriptsJson.length();
        for (int i = 0; i < numScripts; i++){
          // Get JSON for next script
          JSONObject scriptJson = scriptsJson.optJSONObject(i);
          
          // Ignore empty scripts
          if (! scriptJson.keys().hasNext())
            continue;
          
          ScriptDefinition scriptDefinition = ScriptDefinition.fromJson(scriptJson);
          scripts.put(scriptDefinition.name, scriptDefinition);
        }
      }
    }

    FieldList outputsList = null;
    if (agentJson.has("outputs")){
      JSONArray outputsJson = agentJson.optJSONArray("outputs");
      outputsList = new FieldList();
      int numOutputs = outputsJson.length();
      for (int i = 0; i < numOutputs; i++){
        // Get next output field definition
        JSONObject outputJson = outputsJson.optJSONObject(i);
        
        // Ignore empty field definitions
        if (outputJson.length() == 0)
          continue;
        
        // Parse the output field definition
        Field field = Field.fromJsonx(symbolManager.getSymbolTable("outputs"), outputJson);

        // TODO: give error for dup names
        
        // Add parsed field to the outputs field list
        outputsList.add(field);
      }
    }

    // Parse 'scratchpad' fields
    FieldList scratchpad = null;
    if (agentJson.has("scratchpad")){
      JSONArray scratchpadJson = agentJson.optJSONArray("scratchpad");
      scratchpad = new FieldList();
      int numMemory = scratchpadJson.length();
      for (int i = 0; i < numMemory; i++){
        JSONObject fieldJson = scratchpadJson.optJSONObject(i);
        Field field = Field.fromJsonx(symbolManager.getSymbolTable("scratchpad"), fieldJson);
        // TODO: give error for dup names
        scratchpad.add(field);
      }
    }

    // Parse 'memory' fields
    FieldList memory = null;
    if (agentJson.has("memory")){
      JSONArray memoryJson = agentJson.optJSONArray("memory");
      memory = new FieldList();
      int numMemory = memoryJson.length();
      for (int i = 0; i < numMemory; i++){
        JSONObject fieldJson = memoryJson.optJSONObject(i);
        Field field = Field.fromJsonx(symbolManager.getSymbolTable("memory"), fieldJson);
        // TODO: give error for dup names
        memory.add(field);
      }
    }

    // Parse 'goals'

    List<Goal> goalsList = null;
    if (agentJson.has("goals")){
      JSONArray goalsJson = agentJson.optJSONArray("goals");
      goalsList = new ArrayList<Goal>();
      int numGoals = goalsJson.length();
      for (int i = 0; i < numGoals; i++){
        // Get next goal definition
        JSONObject goalJson = goalsJson.optJSONObject(i);
        
        // Ignore empty goal definitions
        if (goalJson.length() == 0)
          continue;
        
        // Parse the goal definition
        Goal goal = Goal.fromJson(goalJson);

        // Add parsed goal to the goals list
        goalsList.add(goal);
      }
    }

    String reportingIntervalExpression = agentJson.has("reporting_interval") ? agentJson.optString("reporting_interval") :
      update ? null : agentServer.getDefaultReportingInterval();
    String triggerIntervalExpression = agentJson.has("trigger_interval") ? agentJson.optString("trigger_interval") :
      update ? null : agentServer.getDefaultTriggerInterval();

    String created = agentJson.optString("created", null);
    long timeCreated = -1;
    try {
      timeCreated = created != null ? DateUtils.parseRfcString(created): -1;
    } catch (ParseException e){
      throw new AgentServerException("Unable to parse created date ('" + created + "') - " + e.getMessage());
    }
    String modified = agentJson.optString("modified", null);
    long timeModified = -1;
    try {
      timeModified = modified != null ? DateUtils.parseRfcString(modified): -1;
    } catch (ParseException e){
      throw new AgentServerException("Unable to parse modified date ('" + modified + "') - " + e.getMessage());
    }

    Boolean enabled = update ? null : true;
    if (agentJson.has("enabled"))
      enabled = agentJson.optBoolean("enabled");
    
    // Validate keys
    JsonUtils.validateKeys(agentJson, "Agent definition", new ArrayList<String>(Arrays.asList(
        "user", "name", "description", "parameters", "inputs", "timers", "conditions",
        "notifications", "scripts", "outputs", "scratchpad", "memory", "goals", "created",
        "modified", "reporting_interval", "trigger_interval", "enabled")));

    // TODO: Differentiate create vs. recreate vs. update - handling of SHA
    agent = new AgentDefinition(agentServer, user, agentDefinitionName, agentDescription, parameters,
        inputs, timers, conditions, scripts, scratchpad, memory, notifications, outputsList,
        goalsList, triggerIntervalExpression, reportingIntervalExpression, timeCreated, timeModified, enabled, update);

    // Return the created agent definition
    return agent;
  }

  public JSONObject toJson() throws AgentServerException {
    try {
      JSONObject agentJson = new JsonListMap();
      agentJson.put("user", user.id);
      agentJson.put("name", name);
      agentJson.put("description", description);
      agentJson.put("created", DateUtils.toRfcString(timeCreated));
      agentJson.put("modified", DateUtils.toRfcString(timeModified));

      // Output parameter field definitions
      JSONArray parametersArrayJson = new JSONArray();
      for (Field parameter: parameters)
        parametersArrayJson.put(parameter.toJson());
      agentJson.put("parameters", parametersArrayJson);

      // Output data source definitions
      JSONArray inputsArrayJson = new JSONArray();
      for (DataSourceReference input: inputs)
        inputsArrayJson.put(input.toJson());
      agentJson.put("inputs", inputsArrayJson);

      // Output timers
      JSONArray timersArrayJson = new JSONArray();
      for (NameValue<AgentTimer> timerNameValue: timers)
        timersArrayJson.put(timerNameValue.value.toJson());
      agentJson.put("timers", timersArrayJson);

      // Output conditions
      JSONArray conditionsArrayJson = new JSONArray();
      for (NameValue<AgentTimer> timerNameValue: timers)
        timersArrayJson.put(timerNameValue.value.toJson());
      agentJson.put("conditions", conditionsArrayJson);

      // Output notifications
      JSONArray notificationsArrayJson = new JSONArray();
      for (NameValue<NotificationDefinition> notificationNameValue: notifications)
        notificationsArrayJson.put(notificationNameValue.value.toJson());
      agentJson.put("notifications", notificationsArrayJson);

      // Output scripts
      JSONArray scriptsArrayJson = new JSONArray();
      for (NameValue<ScriptDefinition> scriptNameValue: scripts)
        scriptsArrayJson.put(scriptNameValue.value.toJson());
      agentJson.put("scripts", scriptsArrayJson);

      // Output scratchpad field definitions
      JSONArray scratchpadFieldsArrayJson = new JSONArray();
      for (Field scratchpadField: scratchpad)
        scratchpadFieldsArrayJson.put(scratchpadField.toJson());
      agentJson.put("scratchpad", scratchpadFieldsArrayJson);

      // Output memory field definitions
      JSONArray memoryFieldsArrayJson = new JSONArray();
      for (Field memoryField: memory)
        memoryFieldsArrayJson.put(memoryField.toJson());
      agentJson.put("memory", memoryFieldsArrayJson);

      // Output output field definitions
      JSONArray outputFieldsArrayJson = new JSONArray();
      for (Field outputField: outputs)
        outputFieldsArrayJson.put(outputField.toJson());
      agentJson.put("outputs", outputFieldsArrayJson);

      // Output goal definitions
      JSONArray goalsArrayJson = new JSONArray();
      for (Goal goal: goals)
        goalsArrayJson.put(goal.toJson());
      agentJson.put("goals", goalsArrayJson);

      agentJson.put("trigger_interval", triggerIntervalExpression);
      agentJson.put("reporting_interval", reportingIntervalExpression);

      agentJson.put("enabled", enabled);

      return agentJson;
    } catch (JSONException e){
      throw new AgentServerException("Unexpected JSON error generating JSON for agent definition");
    }
  }
  
  public void update(AgentServer agentServer, AgentDefinition updated) throws AgentServerException {
    boolean modified = false;

    if (updated.description != null && ! this.description.equals(updated.description)){
      modified = true;
      this.description = updated.description;
    }

    if (updated.inputs != null){
      this.inputs = updated.inputs;
    }

    if (updated.timers != null){
      modified = true;
      this.timers = updated.timers;
    }

    if (updated.conditions != null){
      modified = true;
      this.conditions = updated.conditions;
    }

    if (updated.scripts != null){
      modified = true;
      this.scripts = updated.scripts;
    }

    if (updated.memory != null){
      modified = true;
      this.memory = updated.memory;
    }
    
    if (updated.outputs != null){
      modified = true;
      this.outputs = updated.outputs;
    }

    if (updated.goals != null){
      modified = true;
      this.goals = updated.goals;
    }
    
    if (updated.triggerIntervalExpression != null && ! this.triggerIntervalExpression.equals(updated.triggerIntervalExpression)){
      modified = true;
      this.triggerIntervalExpression = updated.triggerIntervalExpression;
    }
    
    if (updated.reportingIntervalExpression != null && ! this.reportingIntervalExpression.equals(updated.reportingIntervalExpression)){
      modified = true;
      this.reportingIntervalExpression = updated.reportingIntervalExpression;
    }

    // Did anything actually change?
    if (modified){
      // Yes, record time of modification
      this.timeModified = System.currentTimeMillis();

      // Persist the changes
      agentServer.persistence.put(this);
    }
  }
  
  public String toString(){
    try {
      return toJson().toString();
    } catch (AgentServerException e){
      log.info("Unable to output AgentState as string - " + e.getMessage());
      e.printStackTrace();
      return "[AgentState: Unable to output AgentState as string - " + e.getMessage();
    }
  }
}
