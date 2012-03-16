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

package com.basetechnology.s0.agentserver.script.intermediate;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class ExpressionNode  extends Node {

  public Value evaluateExpression(ScriptState scriptState) throws AgentServerException {
    scriptState.countNodeExecutions();
    Value valueNode = getValue(scriptState);
    if (valueNode == null)
      return NullValue.one;
    else
      return valueNode;
  }

  public boolean evaluateBooleanExpression(ScriptState scriptState) throws AgentServerException {
    Value valueNode = evaluateExpression(scriptState);
    Object object = valueNode.getValue();
    if (object == null)
      return false;
    else if (object instanceof Boolean)
      return (Boolean)object;
    else if (object instanceof Integer)
      return (Integer)object != 0;
    else if (object instanceof Long)
      return (Long)object != 0;
    else if (object instanceof Float)
      return (Float)object != 0.0;
    else if (object instanceof Double)
      return (Double)object != 0.0;
    else if (object instanceof String)
      return ((String)object).length() > 0;
    else
      return false;
  }

  public Value getValue(ScriptState scriptState)  throws AgentServerException {
    return NullValue.one;
  }

  public boolean getBooleanValue(ScriptState scriptState){
    return false;
  }

  public long getLongValue(ScriptState scriptState){
    return 0;
  }

  public double getDoubleValue(ScriptState scriptState){
    return 0.0;
  }

  public String getStringValue(ScriptState scriptState){
    return null;
  }

  public Value putValue(ScriptState scriptState, Value valueNode)  throws AgentServerException {
    return valueNode;
  }
}
