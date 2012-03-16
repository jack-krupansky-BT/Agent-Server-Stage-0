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

import java.util.ArrayList;
import java.util.List;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class SubscriptedReferenceNode extends ReferenceNode {
  public ExpressionNode node;
  public List<ExpressionNode> arguments;

  public SubscriptedReferenceNode(ExpressionNode node, List<ExpressionNode> arguments){
    this.node = node;
    this.arguments = arguments;
  }

  public Value getValue(ScriptState scriptState) throws AgentServerException {
    Value valueNode = node.evaluateExpression(scriptState);
    scriptState.countNodeExecutions();
    List<Value> argumentValues = new ArrayList<Value>();
    for (ExpressionNode argumentExpressionNode: arguments)
      argumentValues.add(argumentExpressionNode.evaluateExpression(scriptState));
    return valueNode.getSubscriptedValue(scriptState, argumentValues);
  }

  public Value evaluateExpression(ScriptState scriptState) throws AgentServerException {
    Value valueNode = getValue(scriptState);
    if (valueNode == null)
      return NullValue.one;
    else
      return valueNode;
  }

  public Value putValue(ScriptState scriptState, Value newValueNode) throws AgentServerException {
    Value valueNode = node.evaluateExpression(scriptState);
    List<Value> argumentValues = new ArrayList<Value>();
    for (ExpressionNode argumentExpressionNode: arguments)
      argumentValues.add(argumentExpressionNode.evaluateExpression(scriptState));
    return valueNode.putSubscriptedValue(scriptState, argumentValues, newValueNode);
  }

}
