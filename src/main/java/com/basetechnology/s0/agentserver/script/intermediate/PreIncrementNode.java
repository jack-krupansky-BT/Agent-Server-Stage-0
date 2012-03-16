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
import com.basetechnology.s0.agentserver.script.runtime.value.FalseValue;
import com.basetechnology.s0.agentserver.script.runtime.value.FloatValue;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.TrueValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class PreIncrementNode extends UnaryExpressionNode {

  public PreIncrementNode(ExpressionNode node){
    super(node);
  }

  public Value evaluateExpression(ScriptState scriptState) throws AgentServerException {
    scriptState.countNodeExecutions();
    if (node instanceof VariableReferenceNode){
      // Get current value of referenced variable
      VariableReferenceNode varRef = (VariableReferenceNode)node; 
      Value originalValueNode = varRef.getValue(scriptState);

      // Decrement it and create value node with new value
      Value newValueNode = NullValue.one;
      if (originalValueNode instanceof FalseValue)
        newValueNode = TrueValue.one;
      else if (originalValueNode instanceof TrueValue)
        newValueNode = originalValueNode;
      else if (originalValueNode instanceof IntegerValue){
        IntegerValue node = (IntegerValue)originalValueNode;
        newValueNode = new IntegerValue(node.value + 1);
      } else if (originalValueNode instanceof FloatValue){
          FloatValue node = (FloatValue)originalValueNode;
          newValueNode = new FloatValue(node.value + 1.0);
      } else if (originalValueNode instanceof StringValue){
        StringValue node = (StringValue)originalValueNode;
        // TODO: What should we really do here?
        newValueNode = new StringValue(' ' + node.value);
      } else if (originalValueNode instanceof NullValue)
        newValueNode = new IntegerValue(1);

      // Write new value back to variable store
      varRef.putValue(scriptState, newValueNode);
      
      // Return new value node
      return newValueNode;
    } else {
      // For non-variables, just return the expression value
      return node.evaluateExpression(scriptState);
    }
  }

}
