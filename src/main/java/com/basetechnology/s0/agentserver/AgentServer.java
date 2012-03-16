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

import java.io.IOException;
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

import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.field.Field;
import com.basetechnology.s0.agentserver.field.FieldList;
import com.basetechnology.s0.agentserver.mailaccessmanager.MailAccessManager;
import com.basetechnology.s0.agentserver.persistence.Persistence;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentFileException;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonUtils;
import com.basetechnology.s0.agentserver.util.ListMap;
import com.basetechnology.s0.agentserver.util.NameValueList;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessConfig;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessManager;
import com.basetechnology.s0.agentserver.webaccessmanager.WebPage;
import com.basetechnology.s0.agentserver.webaccessmanager.WebSiteAccessConfig;

public class AgentServer {
  static final Logger log = Logger.getLogger(AgentServer.class);

  public AgentAppServer agentAppServer;
  public static AgentServer singleton = null;
  public List<String> fieldTypes =
      Arrays.asList("string", "int", "float", "money", "option", "choice", "multi_choice", "date", "location", "text", "help");
  public long startTime = 0;
  public AgentScheduler agentScheduler;
  
  static public String defaultPersistencePath = "C:/cygwin/home/projects/agentserver.pjson";
  //static public String defaultPersistencePath = "agentserver.pjson";
  public String persistencePath = defaultPersistencePath;
  public Persistence persistence;
  public AgentServerConfig config;
  public NameValueList<User> users;
  public NameValueList<AgentDefinitionList> agentDefinitions;
  public NameValueList<AgentInstanceList> agentInstances;
  public AgentServerWebAccessConfig webAccessConfig;
  public WebSiteAccessConfig webSiteAccessConfig;
  public WebAccessManager webAccessManager;
  public MailAccessManager mailAccessManager;

  public AgentServer(AgentAppServer agentAppServer) throws RuntimeException, AgentServerException, IOException, InterruptedException, PersistentFileException, ParseException, TokenizerException, ParserException {
    this(agentAppServer, true);
  }

  public AgentServer(AgentAppServer agentAppServer, boolean start) throws RuntimeException, AgentServerException, IOException, InterruptedException, PersistentFileException, ParseException, TokenizerException, ParserException {
    log.info("Creation of AgentServer object");
    
    // Link back to app server
    this.agentAppServer = agentAppServer;
    
    // Singleton access is now permitted
    singleton = this;
    
    // Start the agent server
    start(start);
  }
  
  public static AgentServer getSingleton() throws AgentServerException, InterruptedException, IOException, PersistentFileException, ParseException, TokenizerException, ParserException {
    if (singleton == null)
      return (singleton = new AgentServer(null, false));
      //throw new AgentServerException("Cannot access singleton of AgentServer until server is instantiated");
    else
      return singleton;
  }
  
  public AgentDefinition addAgentDefinition(AgentDefinition agentDefinition) throws AgentServerException {
    if (agentDefinition != null){
      // Check if the user has any agents yet
      if (! agentDefinitions.containsKey(agentDefinition.user.id)){
        // No, so create an empty agent table for user
        agentDefinitions.put(agentDefinition.user.id, new AgentDefinitionList());
      }

      // Get agent definition table for the user
      AgentDefinitionList usersAgentDefinitions = agentDefinitions.get(agentDefinition.user.id);

      // Store the new agent definition for the user
      usersAgentDefinitions.put(agentDefinition);
      
      // Persist the new agent definition
      persistence.put(agentDefinition);

    }

    // Return the agent definition itself
    return agentDefinition;
  }

  public AgentDefinition addAgentDefinition(String agentJsonString) throws SymbolException, RuntimeException, AgentServerException {
    if (agentJsonString == null || agentJsonString.trim().length() == 0)
      agentJsonString = "{}";
    try {
      return addAgentDefinition(new JSONObject(agentJsonString));
    } catch (JSONException e){
      throw new AgentServerException("JSON parsing exception: " + e.getMessage());
    }
  }

  public AgentDefinition addAgentDefinition(JSONObject agentJson) throws SymbolException, RuntimeException, AgentServerException {
    return addAgentDefinition(null, agentJson);
  }

  public AgentDefinition addAgentDefinition(User user, JSONObject agentJson) throws SymbolException, RuntimeException, AgentServerException {
    // Parse the JSON for the agent definition
    AgentDefinition agentDefinition = AgentDefinition.fromJson(this, user, agentJson);
    
    // Add it to table of agent definitions
    addAgentDefinition(agentDefinition);
    
    // Return the new agent definition
    return agentDefinition;
/*
    // If we have the user, ignore user from JSON
    if (user == null){
      String userId = agentJson.optString("user");
      if (userId == null || userId.trim().length() == 0)
        throw new AgentServerException("Agent definition user id ('user') is missing");
      user = getUser(userId);
      if (user == User.noUser)
        throw new AgentServerException("Agent definition user id does not exist: '" + userId + "'");
    }

    // Parse agent definition name
    String agentDefinitionName = agentJson.optString("name");
    if (agentDefinitionName == null || agentDefinitionName.trim().length() == 0)
      throw new AgentServerException("Agent definition name ('name') is missing");

    // Parse agent definition description
    String agentDescription = agentJson.optString("description");
    if (agentDescription == null || agentDescription.trim().length() == 0)
      agentDescription = "";
    log.info("Adding new agent definition named: " + agentDefinitionName + " for user: " + user.id);
    
    // TODO: Parse comment
    
    AgentDefinitionList agentMap = agentDefinitions.get(user.id);
    if (agentMap == null){
      agentMap = new AgentDefinitionList();
      agentDefinitions.add(user.id, agentMap);
    }

    // Check if named agent definition already exists
    if (agentMap.containsKey(agentDefinitionName))
      throw new AgentServerException("Agent definition name already exists: '" + agentDefinitionName + "'");

    AgentDefinition agent = null;

    SymbolManager symbolManager = new SymbolManager();

    String invalidParameterNames = "";
    String invalidDataSourceNames = "";
    String invalidEventNames = "";

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
    NameValueList<AgentTimer> timers = new NameValueList<AgentTimer>();
    if (agentJson.has("timers")){
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
    NameValueList<AgentCondition> conditions = new NameValueList<AgentCondition>();
    if (agentJson.has("conditions")){
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

    // Parse 'scripts' list
    NameValueList<ScriptDefinition> scripts = new NameValueList<ScriptDefinition>();
    if (agentJson.has("scripts")){
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

    // Parse notifications
    NameValueList<NotificationDefinition> notifications = new NameValueList<NotificationDefinition>();
    if (agentJson.has("notifications")){
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

    // Parse outputs
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

    long reportingInterval = agentJson.optLong("reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL);
    long triggerInterval = agentJson.optLong("trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL);

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
    
    // Validate keys
    JsonUtils.validateKeys(agentJson, "Agent definition", new ArrayList<String>(Arrays.asList(
        "user", "name", "definition", "description", "parameter_values", "trigger_interval", "reporting_interval",
        "enabled", "instantiated", "updated", "state",
        "status", "inputs_changed", "triggered", "outputs_changed")));

    if (invalidParameterNames.length() == 0){
        agent = new AgentDefinition(this, user, agentDefinitionName, agentDescription, parameters, inputs, timers, conditions, scripts, scratchpad, memory, notifications, outputsList, goalsList, triggerInterval, reportingInterval, timeCreated, timeModified);
        addAgentDefinition(agent);
    } else {
      throw new AgentServerException("Invalid field names for agent of class " + agentDefinitionName + ": " + invalidParameterNames);
    }

    // Return the created agent definition
    return agent;
    */
  }

  public AgentDefinition getAgentDefinition (User user, String agentDefinitionName){
    AgentDefinitionList agentMap = agentDefinitions.get(user.id);
    if (agentMap == null)
      return null;
    else
      return agentMap.get(agentDefinitionName);
  }
  
  public void clearAgentDefinitions(String userId) throws AgentServerException {
    // Check if the user has any agents yet
    if (! agentDefinitions.containsKey(userId))
      // No, so nothing more to do
      return;

    // Get agent table for the user
    AgentDefinitionList usersAgents = agentDefinitions.get(userId);

    // Clear the user's agent list
    usersAgents.clear();
  }

  public void removeAgentDefinition(AgentDefinition agentDefinition) throws AgentServerException {
    String userId = agentDefinition.user.id;
    String agentName = agentDefinition.name;

    // Check if the user has any agents yet
    if (! agentDefinitions.containsKey(userId))
      throw new AgentServerException("Attempt to delete agent definition ('" + agentName + "') for a user ('" + userId + "') that has no agents");

    // Get agent table for the user
    AgentDefinitionList usersAgents = agentDefinitions.get(userId);

    // Check if that agent exists for user
    if (! usersAgents.containsKey(agentName))
      throw new AgentServerException("Attempt to delete agent definition ('" + agentName + "') that does not exist for user ('" + userId + "')");

    // Delete the named agent definition for the user
    usersAgents.remove(agentName);
  }

  public void removeAgentDefinition(String userId, String agentName) throws AgentServerException {
    // Check if the user has any agents yet
    if (! agentDefinitions.containsKey(userId))
      throw new AgentServerException("Attempt to delete agent definition ('" + agentName + "') for a user ('" + userId + "') that has no agents");

    // Get agent table for the user
    AgentDefinitionList usersAgents = agentDefinitions.get(userId);

    // Check if that agent exists for user
    if (! usersAgents.containsKey(agentName))
      throw new AgentServerException("Attempt to delete agent definition ('" + agentName + "') that does not exist for user ('" + userId + "')");

    // Delete the named agent definition for the user
    usersAgents.remove(agentName);
  }

  public AgentInstance addAgentInstance(AgentInstance agentInstance) throws AgentServerException, SymbolException, JSONException {
    // Get instance list for the user
    AgentInstanceList agentInstanceList = agentInstances.get(agentInstance.user.id);
    if (agentInstanceList == null){
      // Need to do the initial creation of the instance list for this user
      agentInstanceList = new AgentInstanceList();
      agentInstances.put(agentInstance.user.id, agentInstanceList);
    }
    
    // Store the instance in the instance list for the user
    agentInstanceList.put(agentInstance);
    
    // Persist the new agent instance
    persistence.put(agentInstance);
    
    // Return the agent instance
    return agentInstance;
  }

  public AgentInstance addAgentInstance(User user, JSONObject agentInstanceJson) throws AgentServerException, SymbolException, JSONException, TokenizerException, ParserException {
    // Get instance list for the user
    AgentInstance agentInstance = getAgentInstance(user, agentInstanceJson);
    
    // Return the agent instance
    return agentInstance;
  }

  public AgentInstance getAgentInstance(String userId, String agentInstanceName){
    return getAgentInstance(getUser(userId), agentInstanceName);
  }

  public AgentInstance getAgentInstance(User user, String agentInstanceName){
    AgentInstanceList agentMap = agentInstances.get(user.id);
    if (agentMap == null)
      return null;
    else
      return agentMap.get(agentInstanceName);
  }
  
  public AgentInstance getAgentInstance(User user, AgentDefinition agentDefinition) throws RuntimeException, SymbolException, AgentServerException, JSONException, TokenizerException, ParserException {
    return getAgentInstance(user, agentDefinition, null, true);
  }

  public AgentInstance getAgentInstance(User user, AgentDefinition agentDefinition, SymbolValues parameters) throws RuntimeException, SymbolException, AgentServerException, JSONException, TokenizerException, ParserException {
    return getAgentInstance(user, agentDefinition, parameters, true);
  }
  
  public AgentInstance getAgentInstance(User user, AgentDefinition agentDefinition, SymbolValues parameters, boolean create) throws RuntimeException, SymbolException, AgentServerException, JSONException, TokenizerException, ParserException {
    // Get instance list for the user
    AgentInstanceList agentInstanceList = agentInstances.get(user.id);
    if (agentInstanceList == null){
      // Need to do the initial creation of the instance list for this user
      agentInstanceList = new AgentInstanceList();
      agentInstances.put(user.id, agentInstanceList);
    }
    
    // Get the instance from the user's instance list (or add if it doesn't exist yet)
    return agentInstanceList.getAgentInstance(user, agentDefinition, parameters, create);
  }

  public AgentInstance getAgentInstance(String agentJsonString) throws SymbolException, RuntimeException, AgentServerException, TokenizerException, ParserException {
    if (agentJsonString == null || agentJsonString.trim().length() == 0)
      agentJsonString = "{}";
    try {
      return getAgentInstance(new JSONObject(agentJsonString));
    } catch (JSONException e){
      throw new AgentServerException("JSON parsing exception: " + e.getMessage());
    }
  }

  public AgentInstance getAgentInstance(JSONObject agentJson) throws SymbolException, RuntimeException, AgentServerException, JSONException, TokenizerException, ParserException {
    return getAgentInstance(null, agentJson);
  }

  public AgentInstance getAgentInstance(User user, JSONObject agentJson) throws SymbolException, RuntimeException, AgentServerException, JSONException, TokenizerException, ParserException {
    // Parse the JSON for the agent instance

    // If we have the user, ignore user from JSON
    if (user == null){
      String userId = agentJson.optString("user");
      if (userId == null || userId.trim().length() == 0)
        throw new AgentServerException("Agent instance user id ('user') is missing");
      user = getUser(userId);
      if (user == User.noUser)
        throw new AgentServerException("Agent instance user id does not exist: '" + userId + "'");
    }

    // Parse the agent instance name
    String agentInstanceName = agentJson.optString("name");
    if (agentInstanceName == null || agentInstanceName.trim().length() == 0)
      throw new AgentServerException("Agent instance name ('name') is missing");
    
    // Parse the agent definition name
    String agentDefinitionName = agentJson.optString("definition");
    if (agentDefinitionName == null || agentDefinitionName.trim().length() == 0)
      throw new AgentServerException("Agent instance definition name ('definition') is missing for user '" + user.id + "'");
    String agentDescription = agentJson.optString("description");
    if (agentDescription == null || agentDescription.trim().length() == 0)
      agentDescription = "";
    log.info("Adding new agent instance named: " + agentInstanceName + " for agent definition '" + agentDefinitionName + "' for user: " + user.id);
    AgentInstanceList agentMap = agentInstances.get(user.id);
    if (agentMap == null){
      agentMap = new AgentInstanceList();
      agentInstances.add(user.id, agentMap);
    }
    
    // Check if referenced agent definition exists
    AgentDefinitionList userAgentDefinitions = agentDefinitions.get(user.id);
    if (userAgentDefinitions == null)
      throw new AgentServerException("Agent instance '" + agentInstanceName + "' references agent definition '" + agentDefinitionName + "' which does not exist for user '" + user.id + "'");
    AgentDefinition agentDefinition = agentDefinitions.nameValueMap.get(user.id).value.get(agentDefinitionName);
    if (agentDefinition == null)
      throw new AgentServerException("Agent instance '" + agentInstanceName + "' references agent definition '" + agentDefinitionName + "' which does not exist for user '" + user.id + "'");

    // Parse the agent instance parameter values
    String invalidParameterNames = "";
    SymbolManager symbolManager = new SymbolManager();
    SymbolTable symbolTable = symbolManager.getSymbolTable("parameter_values");
    JSONObject parameterValuesJson = null;
    SymbolValues parameterValues = null;
    if (agentJson.has("parameter_values")){
      // Parse the parameter values
      parameterValuesJson = agentJson.optJSONObject("parameter_values");
      parameterValues = SymbolValues.fromJson(symbolTable, parameterValuesJson);
      
      // Validate that they are all valid agent definition parameters
      Map<String, Value> treeMap = new TreeMap<String, Value>();
      for (Symbol symbol: parameterValues)
        treeMap.put(symbol.name, null);
      for (String parameterName: treeMap.keySet())
        if (! agentDefinition.parameters.containsKey(parameterName))
          invalidParameterNames += (invalidParameterNames.length() > 0 ? ", " : "") + parameterName;
      if (invalidParameterNames.length() > 0)
        throw new AgentServerException("Parameter names for agent instance " + agentInstanceName + " are not defined for referenced agent definition " + agentDefinitionName + ": " + invalidParameterNames);
    }

    // Check if instance of named agent definition with specified parameters exists yet
    if (agentMap.getAgentInstance(user, agentDefinition, parameterValues) != null)
      throw new AgentServerException("Agent instance name already exists: '" + agentInstanceName + "' with paramters " + parameterValues.toString());

    long triggerInterval = agentJson.optLong("trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL);
    long reportingInterval = agentJson.optLong("reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL);

    boolean enabled = agentJson.optBoolean("enabled", true);

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

    AgentInstance agentInstance = agentMap.put(user, agentDefinition, agentInstanceName, agentDescription, parameterValues, triggerInterval, reportingInterval, enabled, timeCreated, timeModified);
    
    // Persist the new agent instance
    persistence.put(agentInstance);

    // Return the created/shared agent instance
    return agentInstance;
  }

  public AgentInstance getAgentInstance(String userId, String agentInstanceName, String dataSourceName){
    AgentInstanceList agentMap = agentInstances.get(userId);
    if (agentMap == null)
      return null;
    
    AgentInstance agentInstance = agentMap.get(agentInstanceName);
    if (agentInstance == null)
      return null;
    return agentInstance.getDataSourceInstance(dataSourceName);
  }

  public String getAgentInstanceName(String userId, String agentInstanceName, String dataSourceName){
    AgentInstance dataSourceInstance = getAgentInstance(userId, agentInstanceName, dataSourceName);
    if (dataSourceInstance != null)
      return dataSourceInstance.name;
    else
      return null;
  }

  public AgentInstance getAgentInstance(User user, String agentInstanceName, String dataSourceName){
    AgentInstanceList agentMap = agentInstances.get(user.id);
    if (agentMap == null)
      return null;
    
    AgentInstance agentInstance = agentMap.get(agentInstanceName);
    if (agentInstance == null)
      return null;
    return agentInstance.getDataSourceInstance(dataSourceName);
  }

  public void removeAgentInstance(AgentInstance agentInstance) throws AgentServerException {
    String userId = agentInstance.user.id;
    String agentName = agentInstance.name;

    // Check if the user has any agents yet
    if (! agentInstances.containsKey(userId))
      throw new AgentServerException("Attempt to delete agent instance ('" + agentName + "') for a user ('" + userId + "') that has no agents");

    // Get agent table for the user
    AgentInstanceList usersAgents = agentInstances.get(userId);

    // Check if that agent exists for user
    if (! usersAgents.containsKey(agentName))
      throw new AgentServerException("Attempt to delete agent instance ('" + agentName + "') that does not exist for user ('" + userId + "')");

    // Delete the named agent definition for the user
    usersAgents.remove(agentName);
  }

  public User addUser(String userId) throws AgentServerException {
    return addUser(new User(userId));
  }

  public User addUser(User user) throws AgentServerException {
    // Add the new user
    users.add(user.id, user);
    
    // Persist the new user
    persistence.put(user);
    
    // Return the new user
    return user;
  }

  public User getUser(String userId){
    if (userId == null || userId.trim().length() == 0)
      return null;
    else if (userId.equals("*"))
      return User.allUser;
    else {
      User user = users.get(userId);
      return user == null ? User.noUser : user;
    }
  }
  
  public void recreateUser(String userJsonSource) throws AgentServerException, JSONException {
    User newUser = User.fromJson(new JSONObject(userJsonSource));
    addUser(newUser);
  }
  
  public void recreateAgentDefinition(String agentDefinitionJsonSource) throws AgentServerException, JSONException, SymbolException {
    AgentDefinition newAgentDefinition = AgentDefinition.fromJson(this, agentDefinitionJsonSource);
    addAgentDefinition(newAgentDefinition);
  }
  
  public void recreateAgentInstance(String agentInstanceJsonSource) throws AgentServerException, JSONException, SymbolException, ParseException, TokenizerException, ParserException {
    AgentInstance newAgentInstance = AgentInstance.fromJson(this, agentInstanceJsonSource);
    addAgentInstance(newAgentInstance);
  }

  public void shutdown() throws Exception {
    stop();
    // TODO: Should this do something else in addition to stop?
  }
  
  public void start() throws AgentServerException, InterruptedException, IOException, PersistentFileException, ParseException, TokenizerException, ParserException {
    start(true);
  }
  
  public void start(boolean start) throws AgentServerException, InterruptedException, IOException, PersistentFileException, ParseException, TokenizerException, ParserException {
    // No-op if already started
    if (startTime > 0)
      return;
    
    // Record start time for server
    startTime = System.currentTimeMillis();
    
    // Initialize members
    this.users = new NameValueList<User>();
    this.agentDefinitions = new NameValueList<AgentDefinitionList>();
    this.agentInstances = new NameValueList<AgentInstanceList>();

    // Initialize the agent scheduler, but keep it suspended for now
    if (agentScheduler == null)
      agentScheduler = new AgentScheduler(this, false);
    else
      agentScheduler.initialize();
    
    // Initialize persistence
    if (persistence == null)
      persistence = new Persistence(this, persistencePath);
    else
      persistence.initialize();
    
    // Force a reload of config
    if (config == null)
      config = new AgentServerConfig(this);
    config.load();

    // Initialize the web access configuration parameters
    if (webAccessConfig == null)
      webAccessConfig = new AgentServerWebAccessConfig(config);

    // Initialize the web site access control lists
    if (webSiteAccessConfig == null)
      webSiteAccessConfig = new WebSiteAccessConfig(this);
    webSiteAccessConfig.load();
    
    // Initialize the web access manager
    this.webAccessManager = new WebAccessManager(webAccessConfig, webSiteAccessConfig);

    // Initialize the web access manager
    this.mailAccessManager = new MailAccessManager(this);
    mailAccessManager.readConfig();
    
    // Optionally start scheduler
    if (start)
      agentScheduler.start();
  }
  
  public void stop() throws AgentServerException, InterruptedException, IOException {
    // First shut down the agent scheduler and wait for it to fully stop
    agentScheduler.shutdown();
    
    // Now close the persistent store
    persistence.close();
    
    // Indicate that the agent server is no longer running
    startTime = 0;
  }
  
  public String getStatus(){
    return agentScheduler.getStatus();
  }

  public long getDefaultReportingInterval(){
    return config.getLong("reporting_interval");
  }

  public long getDefaultTriggerInterval(){
    return config.getLong("trigger_interval");
  }
  
  public void addWebSiteAccessControls(User user, JSONObject accessControlsJson) throws JSONException, AgentServerException{
    webAccessManager.addWebSiteAccessControls(user.id, accessControlsJson);
    
  }
  
  public WebPage getWebPage(String userId, String url, long refreshInterval, boolean wait) throws InterruptedException {
    return webAccessManager.getWebPage(userId, url, refreshInterval, wait);
  }

  public ListMap<String, String> getWebSiteAccessControls(User user) throws JSONException, AgentServerException{
    return webAccessManager.getWebSiteAccessControls(user);
  }
}
