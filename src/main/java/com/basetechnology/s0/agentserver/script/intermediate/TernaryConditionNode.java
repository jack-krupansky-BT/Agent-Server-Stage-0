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
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class TernaryConditionNode extends ExpressionNode {
  public ExpressionNode conditionNode;
  public ExpressionNode leftNode;
  public ExpressionNode rightNode;

  public TernaryConditionNode(ExpressionNode conditionNode, ExpressionNode leftNode, ExpressionNode rightNode){
    this.conditionNode = conditionNode;
    this.leftNode = leftNode;
    this.rightNode = rightNode;
  }

  public Value evaluateExpression(ScriptState scriptState) throws AgentServerException {
    // Keep track of executions
    scriptState.countNodeExecutions();

    // Get boolean value of the condition node
    Value conditionValueNode = conditionNode.evaluateExpression(scriptState);
    boolean conditionValue = conditionValueNode.getBooleanValue();

    // Evaluate and return the left node if condition is true, otherwise evaluate and return the right node
    if (conditionValue)
      return leftNode.evaluateExpression(scriptState);
    else
      return rightNode.evaluateExpression(scriptState);
  }

}
