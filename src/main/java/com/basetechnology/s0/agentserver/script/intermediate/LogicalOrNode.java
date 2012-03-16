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
import com.basetechnology.s0.agentserver.script.runtime.value.BooleanValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class LogicalOrNode extends BinaryLogicalOperatorNode {

  public LogicalOrNode(ExpressionNode leftNode, ExpressionNode rightNode){
    super(leftNode, rightNode);
  }

  public Value evaluateExpression(ScriptState scriptState) throws AgentServerException {
    scriptState.countNodeExecutions();
    Value leftValueNode = leftNode.evaluateExpression(scriptState);
    boolean leftValue = leftValueNode.getBooleanValue();

    // Only evaluate the right node if the left value is false
    if (! leftValue){
      Value rightValueNode = rightNode.evaluateExpression(scriptState);
      boolean rightValue = rightValueNode.getBooleanValue();
      return BooleanValue.create(rightValue);
    } else
      return BooleanValue.create(true);
  }

}
