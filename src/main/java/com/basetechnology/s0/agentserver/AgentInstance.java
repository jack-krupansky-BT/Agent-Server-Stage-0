/**
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.activities.AgentActivityNotification;
import com.basetechnology.s0.agentserver.activities.AgentActivityTriggerInputChanged;
import com.basetechnology.s0.agentserver.field.Field;
import com.basetechnology.s0.agentserver.goals.Goal;
import com.basetechnology.s0.agentserver.notification.MailNotification;
import com.basetechnology.s0.agentserver.notification.NotificationDefinition;
import com.basetechnology.s0.agentserver.notification.NotificationHistory;
import com.basetechnology.s0.agentserver.notification.NotificationInstance;
import com.basetechnology.s0.agentserver.notification.NotificationRecord;
import com.basetechnology.s0.agentserver.scheduler.AgentScheduler;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionNode;
import com.basetechnology.s0.agentserver.script.intermediate.MapTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.ObjectTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.ScriptNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.ScriptParser;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.script.runtime.ExceptionInfo;
import com.basetechnology.s0.agentserver.script.runtime.ScriptRuntime;
import com.basetechnology.s0.agentserver.script.runtime.value.FieldValue;
import com.basetechnology.s0.agentserver.script.runtime.value.MapValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.JsonUtils;
import com.basetechnology.s0.agentserver.util.ListMap;
import com.basetechnology.s0.agentserver.util.NameValue;

public class AgentInstance {
  static final Logger log = Logger.getLogger(AgentInstance.class);
  public static final int DEFAULT_MAX_INSTANCES = 1000;
  public static final int DEFAULT_LIMIT_INSTANCE_STATES_STORED = 25;
  public static final int DEFAULT_MAXIMUM_LIMIT_INSTANCE_STATES_STORED = 1000;
  public static final int DEFAULT_LIMIT_INSTANCE_STATES_RETURNED = 10;
  public static final int DEFAULT_MAXIMUM_LIMIT_INSTANCE_STATES_RETURNED = 1000;
  public static final boolean DEFAULT_PUBLIC_OUTPUT= false;
  public AgentServer agentServer;
  public long timeInstantiated;
  public long timeUpdated;
  public User user;
  public String name;
  public String description;
  public AgentDefinition agentDefinition;
  public SymbolValues parameterValues;
  public Map<String, String> scriptStatus;
  public Map<String, Long> scriptStartTime;
  public Map<String, Value> scriptReturnValue;
  public Map<String, Long> scriptEndTime;
  public Map<String, AgentTimerStatus> timerStatus;
  public Map<String, AgentConditionStatus> conditionStatus;
  public String triggerIntervalExpression;
  public String reportingIntervalExpression;
  public Boolean publicOutput;
  public int limitInstanceStatesStored;
  public Boolean enabled;
  public boolean pendingSuspended;
  public boolean busy;
  public long lastInputsChanged;
  public long lastTriggerReady;
  public long lastTriggered;
  public List<AgentState> state;
  public ScriptRuntime scriptRuntime;
  public SymbolManager symbolManager;
  public Map<String, SymbolValues> categorySymbolValues;
  public boolean scheduledInit;
  public boolean ranInit;
  public List<AgentInstance> dependentInstances;
  public Map<DataSourceReference, AgentInstance> dataSourceInstances;
  public OutputHistory outputHistory;
  public static final int DEFAULT_OUTPUT_COUNT = 10;
  public int defaultOutputCount = DEFAULT_OUTPUT_COUNT;
  public static final int DEFAULT_OUTPUT_LIMIT = 20;
  public int outputLimit = DEFAULT_OUTPUT_LIMIT;
  public boolean update;
  public List<ExceptionInfo> exceptionHistory;
  public ListMap<String, NotificationInstance> notifications;
  public NotificationHistory notificationHistory;
  public long lastDismissedExceptionTime;
  public boolean suppressEmail;
  public boolean autoCreated = false;
  public boolean check;

  public static int autoNameCounter = 0;
  
  public AgentInstance (){
    // Nothing needed
  }
  
  public AgentInstance (AgentDefinition agentDefinition) throws AgentServerException {
    this(User.noUser, agentDefinition, null, false);
  }
  
  public AgentInstance (AgentDefinition agentDefinition, boolean check) throws AgentServerException {
    this(User.noUser, agentDefinition, null, check);
  }
  
  public AgentInstance (User user, AgentDefinition agentDefinition) throws AgentServerException {
    this(user, agentDefinition, null, false);
  }
  
  public AgentInstance (User user, AgentDefinition agentDefinition, boolean check) throws AgentServerException {
    this(user, agentDefinition, null, check);
  }

  public AgentInstance(User user, AgentDefinition agentDefinition, SymbolValues parameterValues) throws AgentServerException  {
    this(user, agentDefinition, null, null, parameterValues, null, null, false, -1, agentDefinition.enabled, -1, -1, null, false, false);
  }

  public AgentInstance(User user, AgentDefinition agentDefinition, SymbolValues parameterValues, boolean check) throws AgentServerException  {
    this(user, agentDefinition, null, null, parameterValues, null, null, false, -1, agentDefinition.enabled, -1, -1, null, false, check);
  }
  
  public AgentInstance(
      User user,
      AgentDefinition agentDefinition,
      String name,
      String description,
      SymbolValues parameterValues,
      String triggerIntervalExpression,
      String reportingIntervalExpression,
      Boolean publicOutput,
      int limitInstanceStatesStored,
      Boolean enabled,
      long timeInstantiated,
      long timeUpdated,
      List<AgentState> state,
      boolean update,
      boolean check) throws AgentServerException {
    this.check = check;
    this.update = update;
    this.timeInstantiated = timeInstantiated > 0 ? timeInstantiated : System.currentTimeMillis();
    this.timeUpdated = timeUpdated > 0 ? timeUpdated : 0;
    this.user = user == null ? User.noUser : user;
    this.agentDefinition = agentDefinition;
    this.agentServer = agentDefinition.agentServer;
    this.name = name == null ? (check ? "check__" + agentDefinition.name :  agentDefinition.name + "_" + ++autoNameCounter) : name;
    this.description = description;
    this.parameterValues = parameterValues == null && ! update ? new SymbolValues("parameters") : parameterValues;
    this.dependentInstances = new ArrayList<AgentInstance>();
    this.dataSourceInstances = new HashMap<DataSourceReference, AgentInstance>();
    if (! update || check)
      initCategorySymbolValues(parameterValues);
    this.scriptStatus = new HashMap<String, String>();
    this.scriptStartTime = new HashMap<String, Long>();
    this.scriptReturnValue = new HashMap<String, Value>();
    this.scriptEndTime = new HashMap<String, Long>();
    this.timerStatus = new HashMap<String, AgentTimerStatus>();
    this.conditionStatus = new HashMap<String, AgentConditionStatus>();
    this.triggerIntervalExpression =
        triggerIntervalExpression == null || triggerIntervalExpression.trim().length() == 0 ?
            agentDefinition.triggerIntervalExpression :
              triggerIntervalExpression;
    this.reportingIntervalExpression =
        reportingIntervalExpression == null || reportingIntervalExpression.trim().length() == 0 ?
            agentDefinition.reportingIntervalExpression :
              reportingIntervalExpression;
    this.publicOutput = publicOutput;
    this.limitInstanceStatesStored = limitInstanceStatesStored >= 0 ? limitInstanceStatesStored :
      update ? limitInstanceStatesStored : agentServer.config.getDefaultLimitInstanceStatesStored();
    this.lastInputsChanged = 0;
    this.lastTriggerReady = 0;
    this.lastTriggered = 0;
    this.scriptRuntime = new ScriptRuntime(this);
    this.outputHistory = new OutputHistory();
    this.exceptionHistory = new ArrayList<ExceptionInfo>();
    // TODO: How to default this:
    this.lastDismissedExceptionTime = 0;
    this.notifications = new ListMap<String, NotificationInstance>();
    this.notificationHistory = new NotificationHistory();
    this.pendingSuspended = false;
    this.suppressEmail = false;
    
    if (! update && ! check)
      this.enabled = false;
    
    if (! update || check)
      buildSymbols();

    // Set initial state for instance.

    // If state was specified, restore it
    // TODO Whether to do this before or after setting up data sources?
    if (! update && ! check)
    setState(state, update);

    // Initialize status for conditions and timers
    if (! update && ! check){
      initializeConditionStatus();
      initializeTimerStatus();
    }

    this.busy = false;
    this.ranInit = false;

    if (! update && ! check && enabled != null && enabled)
      enable();
    log.info("Enabled for " + this.name + ": " + enabled);
  }

  public boolean equals(AgentDefinition otherAgentDefinition, SymbolValues otherParameterValues){
    return (agentDefinition == otherAgentDefinition && parameterValues.equals(otherParameterValues));
  }

  protected void initCategorySymbolValues(SymbolValues parameterValues){
    categorySymbolValues = new HashMap<String, SymbolValues>();
    categorySymbolValues.put("parameters", parameterValues == null ? new SymbolValues("parameters") : parameterValues);
    categorySymbolValues.put("inputs", new SymbolValues("inputs"));
    categorySymbolValues.put("events", new SymbolValues("events"));
    categorySymbolValues.put("scratchpad", new SymbolValues("scratchpad"));
    categorySymbolValues.put("memory", new SymbolValues("memory"));
    categorySymbolValues.put("goals", new SymbolValues("goals"));
    categorySymbolValues.put("notifications", new SymbolValues("notifications"));
    categorySymbolValues.put("outputs", new SymbolValues("outputs"));
  }

  public void captureDataSourceOutputValues() throws AgentServerException {
    if (agentDefinition.inputs != null){
      for (DataSourceReference dataSourceReference: agentDefinition.inputs){
        // Get the instance for that data source reference
        AgentInstance dataSourceInstance = dataSourceInstances.get(dataSourceReference);

        // Create a map object with a key for each output field of the data source
        MapValue map = new MapValue(ObjectTypeNode.one, null);

        for (Field field: dataSourceReference.dataSource.outputs){
          // Get the raw field name for the data source output field
          String fieldName = field.symbol.name;

          // Get the field value from the data source instance
          Value fieldValue = dataSourceInstance.getOutput(fieldName);

          // Make a deep copy of it
          Value fieldValueCopy = fieldValue.clone();

          // Add the value copy to the map
          map.put(fieldName, fieldValueCopy);
        }

        // Store the value as value of input data source name
        putInput(dataSourceReference.name, map);
      }
      log.info("Captured input values for instance " + name + ": " + categorySymbolValues.get("inputs").toJson());
    }
  }
  
  public void enable() throws AgentServerException {
    enabled = true;
    captureState();

    // Queue the 'init' script to run if it hasn't already
    if (! scheduledInit){
      scheduledInit = true;
      log.info("Scheduling 'init' for instance '" + name + "'");
      AgentScheduler.scheduleInit(this);

      // If no init script, we'e done "starting"
      if (! agentDefinition.scripts.containsKey("init"))
        ranInit = true;

    } else {
      log.info("Skipped scheduling 'init' for instance '" + name + "' since it has already been performed before agent instance was enabled");
      
      // Now schedule all timers and conditions for this agent
      AgentScheduler.scheduleTimersAndConditions(this);
    }
  }

  public void disable() throws AgentServerException {
    if (enabled){
      captureState();
      enabled = false;
    }
  }

  public void buildSymbols() throws SymbolException {
    // Re-build symbol manager tables
    symbolManager = new SymbolManager();

    // But definition may not be complete yet
    if (agentDefinition != null){
      // Add parameters
      if (agentDefinition != null && agentDefinition.parameters != null)
        for (Field field: agentDefinition.parameters)
          symbolManager.put("parameters", field.symbol.name, field.symbol.type);

      // Add inputs
      if (agentDefinition.inputs != null)
        for (DataSourceReference input: agentDefinition.inputs)
          symbolManager.put("inputs", input.name, MapTypeNode.one);

      // Add scratchpad
      if (agentDefinition.scratchpad != null)
        for (Field field: agentDefinition.scratchpad)
          symbolManager.put("scratchpad", field.symbol.name, field.symbol.type);

      // Add memory
      if (agentDefinition.memory != null)
        for (Field field: agentDefinition.memory)
          symbolManager.put("memory", field.symbol.name, field.symbol.type);

      // Add goals
      if (agentDefinition.goals != null)
        for (Goal goal: agentDefinition.goals)
          symbolManager.put("goals", goal.name, ObjectTypeNode.one);

      // Add notifications
      if (agentDefinition.notifications != null)
        for (NameValue<NotificationDefinition> nameValue: agentDefinition.notifications)
          symbolManager.put("notifications", nameValue.name, MapTypeNode.one);

      // Add outputs
      if (agentDefinition.outputs != null)
        for (Field field: agentDefinition.outputs)
          symbolManager.put("outputs", field.symbol.name, field.symbol.type);
    }
  }

  public AgentState captureState() throws AgentServerException {
    // Capture parameter values
    SymbolValues parameterStates = new SymbolValues("parameters");
    SymbolValues parameterValues = categorySymbolValues.get("parameters");
    if (parameterValues != null && agentDefinition != null){
      for (Field parameter: agentDefinition.parameters){
        Symbol symbol = symbolManager.get("parameters", parameter.symbol.name);
        Value parameterValue = parameterValues.get(symbol);
        // TODO: Make sure this is a copy of the value
        Value copy = parameterValue.clone();
        parameterStates.put(symbol, parameterValue.clone());
      }
    }

    // Capture data source values
    // TODO: Rework this
    SymbolValues inputStates = new SymbolValues("inputs");
    SymbolValues inputValues = categorySymbolValues.get("inputs");
    if (agentDefinition.inputs != null)
      for (DataSourceReference dataSource: agentDefinition.inputs){
        // TODO: What to do?? - Should be map of all captured input values for this data source
        //eventStates.put(event, event.getState().clone());
        Symbol symbol = symbolManager.get("inputs", dataSource.name);
        inputStates.put(symbol, inputValues.get(symbol).clone());
      }
    /*
    if (agentDefinition.inputs != null)
      for (DataSourceReference dataSource: agentDefinition.inputs)
        inputStates.put(dataSource, dataSource.getState());
*/

    // Capture memory values
    SymbolValues memoryStates = new SymbolValues("memory");
    SymbolValues memoryValues = categorySymbolValues.get("memory");
    if (memoryValues != null)
      if (memoryValues != null && agentDefinition.memory != null){
        for (Field memory: agentDefinition.memory){
          Symbol symbol = symbolManager.get("memory", memory.symbol.name);
          memoryStates.put(symbol, memoryValues.get(symbol).clone());
        }
      }
    log.info("Memory states in capture: " + memoryStates.toString() + " memory values: " + memoryValues + " scratchpad values: " + categorySymbolValues.get("scratchpad"));

    // TODO: Capture goal values
    
    // Capture output values
    SymbolValues outputStates = new SymbolValues("outputs");
    SymbolValues outputValues = categorySymbolValues.get("outputs");
    if (outputValues != null)
      if (outputValues != null && agentDefinition.outputs != null){
        for (Field output: agentDefinition.outputs){
          Symbol symbol = symbolManager.get("outputs", output.symbol.name);
          outputStates.put(symbol, outputValues.get(symbol).clone());
        }
      }

    // Capture exception history
    List<ExceptionInfo> exceptionStates = new ArrayList<ExceptionInfo>();
    for (ExceptionInfo exceptionInfo: exceptionHistory)
      exceptionStates.add(exceptionInfo.clone());

    // Capture notifications
    ListMap<String, NotificationInstance> notificationStates = notifications.clone();
    
    // Capture the notification history
    NotificationHistory notificationHistoryStates = notificationHistory.clone();
    
    // Save the captured state, if it changed
    AgentState newState = new AgentState(
        System.currentTimeMillis(),
        symbolManager,
        parameterStates,
        inputStates,
        memoryStates,
        outputStates, exceptionStates, lastDismissedExceptionTime,
        notificationStates, notificationHistoryStates);
    int stateSize = state.size(); 
    if (stateSize == 0 || ! state.get(stateSize - 1).equalValues(newState)){
      // Limit number of states recorded - roll off the oldest
      if (stateSize >= limitInstanceStatesStored || stateSize >= agentServer.config.getMaximumLimitInstanceStatesStored())
        state.remove(0);
      
      // Store the new state
      state.add(newState);
      
      // And persist it
      agentServer.persistence.put(this);
    }

    // Return the captured state
    return newState;
  }

  public void instantiateInputDataSources() throws AgentServerException {
    // Instantiate referenced data sources
    for (DataSourceReference dataSourceReference: agentDefinition.inputs){
      AgentInstance dataSourceInstance = dataSourceReference.instantiate(this, user, agentDefinition.agentServer);
      dataSourceInstances.put(dataSourceReference, dataSourceInstance);
    }
  }
  
  public void initializeVariables() throws AgentServerException {
    // Set value for each parameter, as specified or default if not specified
    SymbolValues parameterValues = categorySymbolValues.get("parameters");
    for (Field field: agentDefinition.parameters){
      // TODO: Should parameters support 'compute' as well?
      // See if user specified an explicit parameter value
      Value valueNode = this.parameterValues.get(field.symbol.name);
      if (valueNode instanceof NullValue){
        // No explicit value, so use default value from agent definition
        valueNode = field.getDefaultValueNode();
      }
      parameterValues.put(symbolManager.get("parameters", field.symbol.name), valueNode);
    }
    log.info("Initial parameter values for instance " + name + ": " + categorySymbolValues.get("parameters").toJson());

    // Capture current output values of all data sources specified as inputs
    captureDataSourceOutputValues();

    // Set default value for each scratchpad field
    SymbolValues scratchpadValues = categorySymbolValues.get("scratchpad");
    for (Field field: agentDefinition.scratchpad){
      if (field.compute != null){
        Value newValue = evaluateExpression(field.compute);
        scratchpadValues.put(symbolManager.get("scratchpad", field.symbol.name), newValue);
        log.info("Computed initial scratchpad value for " + field.symbol.name + ": " + newValue.toJson());
      } else
        scratchpadValues.put(symbolManager.get("scratchpad", field.symbol.name), field.getDefaultValueNode());
    }

    // Set default value for each memory field
    log.info("Initialize memory variables for " + name);
    SymbolValues memoryValues = categorySymbolValues.get("memory");
    for (Field field: agentDefinition.memory){
      if (field.compute != null){
        Value newValue = evaluateExpression(field.compute);
        memoryValues.put(symbolManager.get("memory", field.symbol.name), newValue);
        log.info("Computed initial memory value for " + field.symbol.name + ": " + newValue.toJson());
      } else
        memoryValues.put(symbolManager.get("memory", field.symbol.name), field.getDefaultValueNode());
    }

    // Set default value for each output field
    SymbolValues outputValues = categorySymbolValues.get("outputs");
    for (Field field: agentDefinition.outputs)
      // TODO: Should this support 'compute' at this stage or is it okay to come later?
      outputValues.put(symbolManager.get("outputs", field.symbol.name), field.getDefaultValueNode());
    log.info("Initial output values for instance " + name + ": " + categorySymbolValues.get("outputs").toJson());
    
    // Initialize output history
    this.outputHistory.clear();
    
    // Initialize notifications
    for (NameValue<NotificationDefinition> nameValue: agentDefinition.notifications){
      NotificationDefinition notificationDefinition = agentDefinition.notifications.get(nameValue.name);
      SymbolValues notificationDetailValues = categorySymbolValues.get("notifications");
      
      // Initialize default values for detail fields
      MapValue mapValue = null;
      if (notificationDefinition.detail != null){
        // Construct a map with details values for detail fields
        List<FieldValue> mapFields = new ArrayList<FieldValue>();
        for (Field field: notificationDefinition.detail)
          mapFields.add(new FieldValue(field.symbol.name, field.getDefaultValueNode()));
        mapValue = new MapValue(ObjectTypeNode.one, (List<Value>)(Object)mapFields);
        
        // Initialize the notification value with the map of detail fields
        notificationDetailValues.put(symbolManager.get("notifications", notificationDefinition.name), mapValue);
      }

      NotificationInstance notificationInstance = new NotificationInstance(this, notificationDefinition, mapValue);
      notifications.put(nameValue.name, notificationInstance);
      
      // Compute the timeout value
      notificationInstance.timeout = evaluateExpression(notificationDefinition.timeoutExpression).getLongValue();
    }
      
    // Checkpoint the initial output values and trigger dependent agents
    checkpointOutput();
  }
  
  public void initializeConditionStatus(){
    // Initialize status for conditions
    for (NameValue<AgentCondition> conditionNameValue: agentDefinition.conditions){
      AgentCondition condition = conditionNameValue.value;
      AgentConditionStatus status = new AgentConditionStatus(condition);
      conditionStatus.put(condition.name, status);
    }
  }
  
  public void initializeTimerStatus(){
    // Initialize status for timers
    for (NameValue<AgentTimer> timerNameValue: agentDefinition.timers){
      AgentTimer timer = timerNameValue.value;
      AgentTimerStatus status = new AgentTimerStatus(timer);
      timerStatus.put(timer.name, status);
    }
  }

  public void checkpointOutput() throws AgentServerException {
    // Recompute any computed fields
    SymbolValues outputValues = categorySymbolValues.get("outputs");
    for (Field field: agentDefinition.outputs){
      if (field.compute != null){
        Value newValue = evaluateExpression(field.compute);
        outputValues.put(symbolManager.get("outputs", field.symbol.name), newValue);
        log.info("Computed new output value for " + field.symbol.name + ": " + newValue.toJson());
      }
    }
    //log.info("Initial output values for instance " + name + ": " + categorySymbolValues.get("outputs").toJson());
    
    // Trigger dependent instances if output values of this instance changed
    // TODO: Where else do we need to do this?
    // - Init of instance for initial output values
    SymbolValues currentOutputValues = categorySymbolValues.get("outputs");
    OutputRecord outputRecord = outputHistory.getLatest(); 
    SymbolValues savedOutputValues = outputRecord == null ? null :  outputRecord.output;
    if (savedOutputValues == null || ! savedOutputValues.equals(currentOutputValues)){
      if (savedOutputValues == null)
        log.info("Initial output for " + name + ": " + currentOutputValues);
      else
        log.info("Output changed for " + name + " - #" + outputRecord.sequenceNumber + " old output: " + savedOutputValues + " new output: " + currentOutputValues);

      // Trigger all dependent instances that output has changed
      triggerInputChanged();

      //if (savedOutputValues != null)
        //log.info("equals: " + savedOutputValues.equals(currentOutputValues));
      
      // Save deep copy of changed output
      outputHistory.add(currentOutputValues.clone());
      
      // Trigger notifications
      triggerNotifications();
    } else
      log.info("Output unchanged - no triggering");
  }

  public void triggerNotifications() throws AgentServerException {
    // Note: Only called when outputs have changed
    for (String notificationName: notifications){
      // Get the next notification
      NotificationInstance notificationInstance = notifications.get(notificationName);
      NotificationDefinition notificationDefinition = notificationInstance.definition;
      
      // Skip "manual" notifications
      if (notificationDefinition.manual)
        continue;
      
      // Notification may be conditional on some expression
      String condition = notificationDefinition.condition;
      if (condition != null && condition.trim().length() > 0)
        if (! evaluateExpression(condition).getBooleanValue())
          continue;
      
      // Queue up the notification
      queueNotify(notificationInstance);
    }
  }
  
  public long evaluateExpressionLong(String expression) throws AgentServerException {
    return evaluateExpression(expression, false).getLongValue();
  }
  
  public Value evaluateExpression(String expression) throws AgentServerException {
    return evaluateExpression(expression, false);
  }
  
  public Value evaluateExpression(String expression, boolean captureInputs) throws AgentServerException {
    try {
    // Compile the script
    // TODO: Cache and reuse compiled scripts
    ScriptParser parser = new ScriptParser(this);
    ExpressionNode expressionNode = parser.parseExpressionString(expression);

    // Optionally capture output field values for data source inputs
    if (captureInputs)
      captureDataSourceOutputValues();

    // Run the compiled expression
    Value valueNode = scriptRuntime.evaluateExpression(expression, expressionNode);
    
    // Return the return value of the evaluated expression
    return valueNode;
    } catch (TokenizerException e){
      throw new AgentServerException("TokenizerException parsing expression \"" + expression + "\" - " + e.getMessage());
    } catch (ParserException e){
      throw new AgentServerException("ParserException parsing expression \"" + expression + "\" - " + e.getMessage());
    }
  }

  public Value runScript(String scriptName) throws TokenizerException, ParserException, SymbolException, RuntimeException, JSONException, AgentServerException {
    return runScript(scriptName, true);
  }
  
  public Value runScript(String scriptName, boolean captureInputs) throws TokenizerException, ParserException, SymbolException, RuntimeException, JSONException, AgentServerException {
    // Reset script status
    scriptStartTime.put(scriptName, null);
    scriptEndTime.put(scriptName, null);
    scriptReturnValue.put(scriptName, null);

    // TODO: Consider scripts with parameters
    
    // Make sure script name is defined
    if (! agentDefinition.scripts.containsKey(scriptName)){
      // TODO: What to do? For now no-op
      scriptStatus.put(scriptName, "undefined");
      return NullValue.one;
      //throw new RuntimeException("Undefined script name, '" + scriptName + "' for agent " + name);
    }

    // Record start time for script
    scriptStartTime.put(scriptName, System.currentTimeMillis());
    
    // TODO: Record script status: never ran, compile errors, exceptions, aborted, timed-out
    
    // Compile the script
    // TODO: Cache and reuse compiled scripts
    scriptStatus.put(scriptName, "compiling");
    ScriptParser parser = new ScriptParser(this);
    String script = agentDefinition.scripts.get(scriptName).script;
    // TODO: Do something with script definition
    ScriptNode scriptNode = parser.parseScriptString(script);

    // Optionally capture output field values for data source inputs
    if (captureInputs)
      captureDataSourceOutputValues();

    // Run the compiled script
    scriptStatus.put(scriptName, "running");
    Value valueNode = scriptRuntime.runScript(scriptName, scriptNode);
    scriptStatus.put(scriptName, "ran");

    // Record the script return value, if any
    scriptReturnValue.put(scriptName, valueNode);
    
    // Record end time for script
    scriptEndTime.put(scriptName, System.currentTimeMillis());

    // Capture state, if changed
    captureState();
    
    log.info("Finished running script '" + scriptName + "' for instance '" + name + "'");
    
    // Trigger dependent instances if output values of this instance changed
    checkpointOutput();

    // Return the return value node for the script
    return valueNode;
  }
  
  public Value runScriptString(String script) throws AgentServerException {
    return runScriptString(script, true);
  }
  
  public Value runScriptString(String script, boolean captureInputs) throws AgentServerException {
    try {
      // Compile the script
      // TODO: Cache and reuse compiled scripts
      ScriptParser parser = new ScriptParser(this);
      ScriptNode scriptNode = parser.parseScriptString(script);

      // Optionally capture output field values for data source inputs
      if (captureInputs)
        captureDataSourceOutputValues();

      // Run the compiled script
      Value valueNode = scriptRuntime.runScript(script, scriptNode);

      // Capture state
      captureState();

      // Trigger dependent instances if output values of this instance changed
      checkpointOutput();

      // Return the return value node for the script
      return valueNode;
    } catch (TokenizerException e){
      throw new AgentServerException("TokenizerException parsing script string \"" + script + "\" - " + e.getMessage());
    } catch (ParserException e){
      throw new AgentServerException("ParserException parsing expression \"" + script + "\" - " + e.getMessage());
    }
  }

  public AgentState getCurrentState(){
    int numStates = state.size();
    if (numStates == 0)
      return null;
    else
      return state.get(numStates - 1); 
  }

  public JSONObject toJson() throws AgentServerException {
    return toJson(true);
  }

  public JSONObject toJson(boolean includeState) throws AgentServerException {
    return toJson(includeState, -1);
  }

  public JSONObject toJson(boolean includeState, int stateCount) throws AgentServerException {
    try {
      JSONObject agentJson = new JsonListMap();
      agentJson.put("user", user.id);
      agentJson.put("name", name);
      agentJson.put("definition", agentDefinition.name);
      agentJson.put("description", description == null ? "" : description);
      agentJson.put("instantiated", DateUtils.toRfcString(timeInstantiated));
      agentJson.put("updated", timeUpdated > 0 ? DateUtils.toRfcString(timeUpdated) : "");

      agentJson.put("trigger_interval", triggerIntervalExpression);
      agentJson.put("reporting_interval", reportingIntervalExpression);
      agentJson.put("public_output", publicOutput);
      agentJson.put("limit_instance_states_stored", limitInstanceStatesStored);
      agentJson.put("enabled", enabled);

      // Return most recent parameter values
      JSONObject currentParameterValuesJson = new JsonListMap();
      for (Field parameter: agentDefinition.parameters)
        currentParameterValuesJson.put(parameter.symbol.name, getParameter(parameter.symbol.name).getValue());
      agentJson.put("parameter_values", currentParameterValuesJson);

      // Summarize activity of instance
      agentJson.put("inputs_changed", lastInputsChanged > 0 ? DateUtils.toRfcString(lastInputsChanged) : "");
      agentJson.put("triggered", lastTriggered > 0 ? DateUtils.toRfcString(lastTriggered) : "");
      agentJson.put("outputs_changed", outputHistory.size() > 0 ? DateUtils.toRfcString(outputHistory.get(outputHistory.size() - 1).time) : "");
      agentJson.put("status", getStatus());

      if (includeState){
        // Generate array of state history
        JSONArray stateHistoryJson = new JSONArray();

        // Default and limit count of states to return
        int historySize = state.size();
        if (stateCount <= 0)
          stateCount = agentServer.config.getDefaultLimitInstanceStatesReturned();
        if (stateCount <= 0)
          stateCount = historySize;
        if (stateCount > historySize)
          stateCount = historySize;
        int limitCount = agentServer.config.getMaximumLimitInstanceStatesReturned();
        if (stateCount > limitCount)
          stateCount = limitCount;
        int startIndex = historySize - stateCount;
        for (int i = startIndex; i < historySize; i++)
          stateHistoryJson.put(state.get(i).toJson());
        
        agentJson.put("state", stateHistoryJson);
      }

      return agentJson;
    } catch (JSONException e) {
      e.printStackTrace();
      throw new AgentServerException("JSON exception in AgentInstance.toJson - " + e.getMessage());
    }
  }

  public Value getEvent(String fieldName) throws SymbolException {
    return categorySymbolValues.get("events").get(symbolManager.get("events", fieldName));
  }
  
  public Value getInput(String fieldName) throws SymbolException {
    return categorySymbolValues.get("inputs").get(symbolManager.get("inputs", fieldName));
  }
  
  public Value getMemory(String fieldName) throws SymbolException {
    return categorySymbolValues.get("memory").get(symbolManager.get("memory", fieldName));
  }
  
  public Value getOutput(String fieldName) throws SymbolException {
    return categorySymbolValues.get("outputs").get(symbolManager.get("outputs", fieldName));
  }
  
  public Value getParameter(String fieldName) throws SymbolException {
    return categorySymbolValues.get("parameters").get(symbolManager.get("parameters", fieldName));
  }
  
  public void putInput(String fieldName, Value value) throws SymbolException {
    categorySymbolValues.get("inputs").put(symbolManager.get("inputs", fieldName), value);
  }
  
  public void putMemory(String fieldName, Value value) throws SymbolException {
    categorySymbolValues.get("memory").put(symbolManager.get("memory", fieldName), value);
  }
  
  public void addReference(AgentInstance agentInstance){
    dependentInstances.add(agentInstance);
  }
  
  public void removeReference(AgentInstance agentInstance){
    dependentInstances.remove(agentInstance);
  }
  
  public void release() throws AgentServerException {
    if (dependentInstances.size() > 0)
      // TODO: Should we maybe mark for auto-release when dependents do go away
      throw new AgentServerException("Can't release an instance that has dependents");
    
    // Remove references for all data sources
    for (DataSourceReference dataSourceReference: dataSourceInstances.keySet())
      dataSourceInstances.get(dataSourceReference).removeReference(this);
    dataSourceInstances.clear();
  }

  public void triggerInputChanged() throws AgentServerException {
    // Trigger each instance that is dependent on this instance as an input
    for (AgentInstance agentInstance: dependentInstances)
      triggerInputChanged(agentInstance);
  }

  public void triggerInputChanged(AgentInstance dataSourceInstance) throws AgentServerException {
    // Create a new trigger activity for data source change
    AgentActivityTriggerInputChanged triggerActivity = new AgentActivityTriggerInputChanged(dataSourceInstance, this);

    // Schedule the trigger activity
    if (AgentScheduler.singleton != null)
      AgentScheduler.singleton.add(triggerActivity);
  }
  
  public void update(AgentServer agentServer, AgentInstance updated) throws SymbolException, JSONException, AgentServerException {
    // TODO: Only update time if there are any actual changes
    this.timeUpdated = System.currentTimeMillis();
    
    if (updated.description != null)
      this.description = updated.description;
    
    if (updated.parameterValues != null)
      this.parameterValues = updated.parameterValues;
    // TODO: Do we need to update Symbol Manager?
    
    if (updated.enabled != null)
      this.enabled = updated.enabled;
    
    if (updated.triggerIntervalExpression != null)
      this.triggerIntervalExpression = updated.triggerIntervalExpression;
    
    if (updated.reportingIntervalExpression != null)
      this.reportingIntervalExpression = updated.reportingIntervalExpression;
    
    // Persist the changes
    agentServer.persistence.put(this);
  }

  static public AgentInstance fromJson(AgentServer agentServer, String agentJsonSource) throws AgentServerException, SymbolException, JSONException, ParseException, TokenizerException, ParserException {
    return fromJson(agentServer, null, new JSONObject(agentJsonSource), null, false);
  }

  static public AgentInstance fromJson(AgentServer agentServer, JSONObject agentJson) throws AgentServerException, SymbolException, JSONException, ParseException, TokenizerException, ParserException {
    return fromJson(agentServer, null, agentJson, null, false);
  }
  
  static public AgentInstance fromJson(AgentServer agentServer, User user, JSONObject agentJson, AgentDefinition agentDefinition, boolean update) throws AgentServerException, SymbolException, JSONException, ParseException, TokenizerException, ParserException {
    // Parse the JSON for the agent instance

    // If we have the user, ignore user from JSON
    if (user == null){
      String userId = agentJson.optString("user");
      if (userId == null || userId.trim().length() == 0)
        throw new AgentServerException("Agent instance user id ('user') is missing");
      user = agentServer.getUser(userId);
      if (user == User.noUser)
        throw new AgentServerException("Agent instance user id does not exist: '" + userId + "'");
    }

    // Parse the agent instance name
    String agentInstanceName = agentJson.optString("name");
    if (! update && (agentInstanceName == null || agentInstanceName.trim().length() == 0))
      throw new AgentServerException("Agent instance name ('name') is missing");
    
    // Parse the agent definition name - but ignore for update since it can't be changed
    if (! update){
      String agentDefinitionName = agentJson.optString("definition");
      if (agentDefinitionName == null || agentDefinitionName.trim().length() == 0)
        throw new AgentServerException("Agent instance definition name ('definition') is missing for user '" + user.id + "'");

      // Check if referenced agent definition exists
      agentDefinition = agentServer.getAgentDefinition(user, agentDefinitionName);
      if (agentDefinition == null)
        throw new AgentServerException("Agent instance '" + agentInstanceName + "' references agent definition '" + agentDefinitionName + "' which does not exist for user '" + user.id + "'");
    }

    // Parse the agent instance description
    String agentDescription = agentJson.optString("description", null);
    if (! update && (agentDescription == null || agentDescription.trim().length() == 0))
      agentDescription = "";

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
        throw new AgentServerException("Parameter names for agent instance " + agentInstanceName + " are not defined for referenced agent definition " + agentDefinition.name + ": " + invalidParameterNames);
    }

    String triggerInterval = JsonUtils.getString(agentJson, "trigger_interval", update ? null : AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION);
    String reportingInterval = JsonUtils.getString(agentJson, "reporting_interval", update ? null : AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION);

    Boolean publicOutput = null;
    if (agentJson.has("public_output"))
      publicOutput = agentJson.optBoolean("public_output");
    else if (update)
      publicOutput = null;
    else
      publicOutput = false;

    int limitInstanceStatesStored = agentJson.optInt("limit_instance_states_stored", -1);

    Boolean enabled = null;
    if (agentJson.has("enabled"))
      enabled = agentJson.optBoolean("enabled");
    else if (update)
      enabled = null;
    else
      enabled = true;

    // Parse creation and modification timestamps
    String created = agentJson.optString("instantiated", null);
    long timeInstantiated = -1;
    try {
      timeInstantiated = created != null ? DateUtils.parseRfcString(created): -1;
    } catch (ParseException e){
      throw new AgentServerException("Unable to parse created date ('" + created + "') - " + e.getMessage());
    }
    String modified = agentJson.optString("updated", null);
    long timeUpdated = -1;
    try {
      timeUpdated = modified != null ? (modified.length() > 0 ? DateUtils.parseRfcString(modified) : 0): -1;
    } catch (ParseException e){
      throw new AgentServerException("Unable to parse modified date ('" + modified + "') - " + e.getMessage());
    }

    // Parse state history
    List<AgentState> state = null;
    if (agentJson.has("state")){
      JSONArray stateHistoryJson = agentJson.optJSONArray("state");
      int numStates = stateHistoryJson.length();
      state = new ArrayList<AgentState>();
      for (int i = 0; i < numStates; i++){
        JSONObject stateJson = stateHistoryJson.optJSONObject(i);
        AgentState newState = AgentState.fromJson(stateJson, symbolManager);
        state.add(newState);
        
      }
    }
    
    // Validate keys
    JsonUtils.validateKeys(agentJson, "Agent instance", new ArrayList<String>(Arrays.asList(
        "user", "name", "definition", "description", "parameter_values", "trigger_interval", "reporting_interval",
        "enabled", "instantiated", "updated", "state",
        "status", "inputs_changed", "triggered", "outputs_changed",
        "public_output", "limit_instance_states_stored")));

    AgentInstance agentInstance = new AgentInstance(user, agentDefinition, agentInstanceName, agentDescription, parameterValues, triggerInterval, reportingInterval, publicOutput, limitInstanceStatesStored, enabled, timeInstantiated, timeUpdated, state, update, false);

    // Return the new agent instance
    return agentInstance;
  }
  
  public long getReportingInterval() throws AgentServerException {
    // Get the desired reporting interval
    long reportingInterval = evaluateExpressionLong(reportingIntervalExpression);
    
    // May need to throttle it down
    long minimumReportingInterval = agentServer.getMinimumReportingInterval();
    if (reportingInterval < minimumReportingInterval){
      log.info("Throttling reporting_interval " + reportingInterval + " for " + name +
          " down to minimum of " + minimumReportingInterval);
      reportingInterval = minimumReportingInterval;
    }
    return reportingInterval;
  }
  
  public long getTriggerInterval() throws AgentServerException {
    // Get the desired trigger interval
    long triggerInterval = evaluateExpressionLong(triggerIntervalExpression);
    
    // May need to throttle it down
    long minimumTriggerInterval = agentServer.getMinimumTriggerInterval();
    if (triggerInterval < minimumTriggerInterval){
      log.info("Throttling trigger_interval " + triggerInterval + " for " + name +
          " down to minimum of " + minimumTriggerInterval);
      triggerInterval = minimumTriggerInterval;
    }
    return triggerInterval;
  }
  
  public long getTriggerTime() throws AgentServerException {
    // If we have never triggered, we can accept input immediately
    if (lastTriggered == 0)
      return System.currentTimeMillis();
    else
      // Otherwise we can't take input until our trigger interval expires
      // Note: That may be a time in the past, but that is okay and means immediately
      return lastTriggered + getTriggerInterval();
  }
  
  public void setState(List<AgentState> state, boolean update) throws AgentServerException {
    int stateSize = state == null ? 0 : state.size();
    if (stateSize > 0){
    // Restore saved state history
    this.state = state;
    
    // Now initialize all variables as per saved state
      AgentState currentState = state.get(stateSize - 1);

      // Restore parameter values
      SymbolValues parameterValues = categorySymbolValues.get("parameters");
      for (Symbol symbol: currentState.parameterValues)
        parameterValues.put(symbolManager.get("parameters", symbol.name),
            currentState.parameterValues.get(symbol.name).clone());
        
      // Restore captured inputs
      SymbolValues inputValues = categorySymbolValues.get("inputs");
      for (Symbol symbol: currentState.inputValues)
        inputValues.put(symbolManager.get("inputs", symbol.name),
            currentState.inputValues.get(symbol.name).clone());
      
      // Instantiate data sources used as inputs
      if (! update)
        instantiateInputDataSources();

      // Restore memory
      SymbolValues memoryValues = categorySymbolValues.get("memory");
      for (Symbol symbol: currentState.memoryValues)
        memoryValues.put(symbolManager.get("memory", symbol.name),
            currentState.memoryValues.get(symbol.name).clone());
      
      // Restore outputs
      SymbolValues outputValues = categorySymbolValues.get("outputs");
      for (Symbol symbol: currentState.outputValues)
        outputValues.put(symbolManager.get("outputs", symbol.name),
            currentState.outputValues.get(symbol.name).clone());
      
      // Restore output history
      outputHistory = new OutputHistory();
      SymbolValues prevOutputValues = null;
      for (AgentState agentState: this.state){
        SymbolValues outputValues2 = agentState.outputValues;
        if (prevOutputValues == null || ! outputValues2.equals(prevOutputValues))
          outputHistory.add(outputValues2, agentState.time);
        prevOutputValues = outputValues2;
      }
      
      // Restore exception history
      exceptionHistory = new ArrayList<ExceptionInfo>();
      for (ExceptionInfo exceptionInfo: currentState.exceptionHistory)
        exceptionHistory.add(exceptionInfo.clone());
      lastDismissedExceptionTime = currentState.lastDismissedExceptionTime;
      
      // Restore notification history
      notificationHistory = new NotificationHistory();
      for (NotificationRecord notificationRecord: currentState.notificationHistory){
        notificationRecord.notificationInstance.agentInstance = this;
        notificationHistory.add(notificationRecord);
      }
    } else {
      // Simply initialize all variables to default values
      this.state = new ArrayList<AgentState>();
      
      // Instantiate data sources used as inputs
      if (! update)
        instantiateInputDataSources();

      // TODO: Somewhere, we need detection of loops in input dependencies
      
      // Initialize parameters, capture inputs, and set memory and outputs to default values
      if (! update && state == null)
        // TODO: Maybe pass incoming state here to be incorporated with default behavior
        initializeVariables();
    }
  }

  public AgentInstance getDataSourceInstance(String dataSourceName){
    for (DataSourceReference dsr: dataSourceInstances.keySet())
      if (dsr.name.equals(dataSourceName))
        return dataSourceInstances.get(dsr);
    return null;
  }

  public String getDataSourceInstanceName(String dataSourceName){
    for (DataSourceReference dsr: dataSourceInstances.keySet())
      if (dsr.name.equals(dataSourceName))
        return dataSourceInstances.get(dsr).name;
    return null;
  }

  public NotificationInstance getPendingNotification(){
    for (String name: notifications){
      NotificationInstance notification = notifications.get(name);
      if (notification.pending)
        return notification;
    }
    return null;
  }
  
  public String getStatus(){
    NotificationInstance pendingNotification = getPendingNotification();
    if (exceptionHistory.size() > 0 && exceptionHistory.get(exceptionHistory.size() - 1).time > lastDismissedExceptionTime)
      return "exception: " + exceptionHistory.get(exceptionHistory.size() - 1).message;
    else if (! ranInit)
      return "starting";
    else if (pendingNotification != null){
      if (pendingSuspended)
        return "notification_pending_suspended: " + pendingNotification.definition.name;
      else
        return "notification_pending_active: " + pendingNotification.definition.name;
    } else if (enabled)
      return "active";
    else
      return "disabled";
  }

  public void queueNotify(String notificationName) throws AgentServerException {
    NotificationInstance notificationInstance = notifications.get(notificationName);
    if (notificationInstance == null)
      throw new AgentServerException("Undefined notification name: " + notificationName);
    
    queueNotify(notificationInstance);
  }

  public void queueNotify(NotificationInstance notificationInstance) throws AgentServerException {
    // Create a new activity for the notification
    AgentActivityNotification agentActivityNotification=
        new AgentActivityNotification(this, 0, notificationInstance);

    // Queue up the new activity
    // TODO: This needs to synchronized
    AgentScheduler.singleton.add(agentActivityNotification);

  }

  public void notify(NotificationInstance notificationInstance) throws AgentServerException {
    // Store info for the notification
    notificationInstance.pending = ! notificationInstance.definition.type.equals("notify_only");
    notificationInstance.timeNotified = System.currentTimeMillis();
    notificationInstance.timeResponse = 0;
    notificationInstance.response = "no_response";
    notificationInstance.responseChoice = "no_choice";
    notificationInstance.comment = "";
    
    // May need to suspend instance for this notification
    if (notificationInstance.definition.suspend)
      pendingSuspended = true;
    
    //Save notification state history
    notificationHistory.add(notificationInstance);
    
    // Perform the notification - email-only, for now
    if (user.email != null && user.email.trim().length() > 0){
      if (suppressEmail)
        log.warn("Email notification suppressed by suppressEmail flag for instance " + name);
      else if (! agentServer.config.getMailAccessEnabled())
        log.warn("Email notification suppressed by mail_access_enabled = false for instance " + name);
      else {
        MailNotification mailNotification = new MailNotification(agentServer);
        mailNotification.notify(notificationInstance);
      }
    } else
      log.info("No email notification since user '" + user.id + "' has no email address");
    
    // And capture full agent state and persist it
    captureState();
  }
  
  public void respondToNotification(NotificationInstance notificationInstance,
      String response, String responseChoice, String comment) throws AgentServerException {
    // Validate the response
    if (! NotificationInstance.responses.contains(response))
      throw new AgentServerException("Invalid response for notification '" +
          notificationInstance.definition.name + "' of agent instance '" +
          name + "': " + response);
    
    // Store the response and choice
    notificationInstance.response = response;
    if (responseChoice != null)
      notificationInstance.responseChoice = responseChoice;
    if (comment != null)
      notificationInstance.comment = comment;
    
    // Clear pending status
    notificationInstance.pending = false;
    pendingSuspended = false;
    
    //Save notification state history
    notificationHistory.add(notificationInstance);

    // And capture full agent state and persist it
    captureState();

    // Now run an optional script based on the response
    ScriptDefinition scriptDefinition = notificationInstance.definition.scripts.get(response);
    if (scriptDefinition != null){
        runScriptString(scriptDefinition.script);
        // TODO: Pass script name/description to runScriptString
    } else
      log.info("No script named '" + response + "' to run in response to notification '" +
          notificationInstance.definition.name + "' for agent instance '" + name + "'");
  }
  
  public void deReference(AgentInstance agentInstance) throws AgentServerException {
    // Remove an agent instance from the dependents list of this agent instance
    dependentInstances.remove(agentInstance);
    
    // If no more dependents, auto-delete this instance if it was auto-created
    if (dependentInstances.size() == 0 && autoCreated){
      log.info("Auto-deleting agent instance " + name + " since all dependents have gone away");
      agentServer.removeAgentInstance(this);
    } else {
      log.info("Agent instance " + name + " still has " + dependentInstances.size() + " dependents after dependent " + agentInstance.name + " de-references it");
    }
  }
  
  public void deReferenceInputs() throws AgentServerException{
    // De-reference agents associated with each input
    for (DataSourceReference dsr: dataSourceInstances.keySet()){
      AgentInstance agentInstance = dataSourceInstances.get(dsr);
      agentInstance.deReference(this);
    }
  }
}
