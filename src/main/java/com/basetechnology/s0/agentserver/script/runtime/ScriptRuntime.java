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

import java.util.List;
import java.util.Map;

import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionNode;
import com.basetechnology.s0.agentserver.script.intermediate.ScriptNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class ScriptRuntime {
  public AgentInstance agentInstance;
  
  public ScriptRuntime(AgentInstance agentInstance){
    this.agentInstance = agentInstance;
  }

  public Value evaluateExpression(String scriptName, ExpressionNode expressionNode) throws AgentServerException {
    // Create new state for the script execution
    ScriptState scriptState = new ScriptState(this, scriptName, expressionNode);

    // Evaluate the expression
    Value returnValueNode = expressionNode == null ? NullValue.one : expressionNode.evaluateExpression(scriptState);
    return returnValueNode;
  }

  public Value runScript(String scriptName, ScriptNode scriptNode) throws AgentServerException {
    // Create new state for the script execution
    ScriptState scriptState = new ScriptState(this, scriptName, scriptNode);
    
    // Start execution of the script
    // TODO: Do this in a separate thread
    try {
    if (scriptNode != null)
      scriptNode.run(scriptState);
    } catch (Exception e){
      e.printStackTrace();
      agentInstance.exceptionHistory.add(new ExceptionInfo(e, scriptName));
    }
    
    // Return the return value, if any
    return scriptState.returnValue;
  }
  
  public void logException(Exception e){
    
  }
}
