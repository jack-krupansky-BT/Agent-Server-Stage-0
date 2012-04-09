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

package com.basetechnology.s0.agentserver.script.runtime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.script.intermediate.Node;
import com.basetechnology.s0.agentserver.script.intermediate.ScriptNode;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class ScriptState {
  public ScriptRuntime scriptRuntime;
  public AgentServer agentServer;
  public AgentInstance agentInstance;
  public String scriptName;
  public AgentDefinition agentDefinition;
  public SymbolManager symbolManager;
  public Map<String, SymbolValues> categorySymbolValues;
  public Node node;
  public Value returnValue;
  public enum controlFlowChanges {NO_CHANGE, CONTINUE, BREAK, RETURN, THROW};
  public controlFlowChanges controlFlowChange;
  public static final int DEFAULT_EXECUTION_LEVEL = 2;
  public static final int NODE_EXECUTION_LEVEL_1_LIMIT = 10;
  public static final int NODE_EXECUTION_LEVEL_2_LIMIT = 100;
  public static final int NODE_EXECUTION_LEVEL_3_LIMIT = 1000;
  public static final int NODE_EXECUTION_LEVEL_4_LIMIT = 10000;
  public static final List<String> nodeExecutionLevelKeys = Arrays.asList(
      "execution_limit_level_1", "execution_limit_level_2",
      "execution_limit_level_3", "execution_limit_level_4");
  public int nodeExecutionLevel;
  public int nodeExecutionLimit;
  public int nodeExecutionCount;

  public ScriptState(ScriptRuntime scriptRuntime, String scriptName, Node node){
    this(scriptRuntime, scriptName, node, -1);
  }

  public ScriptState(ScriptRuntime scriptRuntime, String scriptName, Node node, int level){
    this.scriptRuntime = scriptRuntime;
    this.agentInstance = scriptRuntime.agentInstance;
    this.agentServer = scriptRuntime.agentInstance.agentServer;
    this.scriptName = scriptName;
    this.node = node;
    this.agentDefinition = null;
    this.symbolManager = agentInstance.symbolManager;
    this.categorySymbolValues = agentInstance.categorySymbolValues;
    this.returnValue = NullValue.one;
    this.controlFlowChange = controlFlowChanges.NO_CHANGE;
    initExecutionLimits(level);
  }

  public void initExecutionLimits(int level){
    if (level < 1)
      level = agentServer == null ? DEFAULT_EXECUTION_LEVEL :
        agentServer.config.getDefaultExecutionLevel();
    nodeExecutionLevel = level;
    nodeExecutionLimit = agentServer == null ? NODE_EXECUTION_LEVEL_3_LIMIT :
        agentServer.config.getInt(nodeExecutionLevelKeys.get(level - 1));
    nodeExecutionCount = 0;
  }
  
  public String getUserId(){
    if (agentInstance != null && agentInstance.user != null)
      return agentInstance.user.id;
    else
      return User.nullUser.id;
  }

  public void countNodeExecutions() throws NodeExecutionLimitException {
    nodeExecutionCount++;
    if (nodeExecutionCount > nodeExecutionLimit)
      throw new NodeExecutionLimitException(((node instanceof ScriptNode) ? "Script " + scriptName : "Non-script") + " has excceded operation execution limit of " + nodeExecutionLimit + " operations");
  }

  public ScriptNode get(String functionName, List<TypeNode> argumentTypes){
    return agentInstance.get(functionName, argumentTypes);
  }
}
