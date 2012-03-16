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
import com.basetechnology.s0.agentserver.script.runtime.value.FloatValue;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class EqualsNode extends RelationalOperatorNode {

  public EqualsNode(ExpressionNode leftNode, ExpressionNode rightNode){
    super(leftNode, rightNode);
  }

  // TODO: Reconsider whether types should ever be converted for equality check
  
  public Value evaluateExpression(ScriptState scriptState) throws AgentServerException {
    scriptState.countNodeExecutions();
    Value leftValueNode = leftNode.evaluateExpression(scriptState);
    Value rightValueNode = rightNode.evaluateExpression(scriptState);
    if(leftValueNode instanceof NullValue){
      return BooleanValue.create(rightValueNode instanceof NullValue);
    } else if(leftValueNode instanceof BooleanValue){
      if (rightValueNode instanceof BooleanValue){
        boolean leftValue = leftValueNode.getBooleanValue();
        boolean rightValue = rightValueNode.getBooleanValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof IntegerValue){
        long leftValue = leftValueNode.getLongValue();
        long rightValue = rightValueNode.getLongValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof FloatValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof StringValue){
        String leftValue = leftValueNode.getStringValue();
        String rightValue = rightValueNode.getStringValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else
        return BooleanValue.create(false);
    } else if(leftValueNode instanceof IntegerValue){
      if (rightValueNode instanceof BooleanValue){
        long leftValue = leftValueNode.getLongValue();
        long rightValue = rightValueNode.getLongValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof IntegerValue){
        long leftValue = leftValueNode.getLongValue();
        long rightValue = rightValueNode.getLongValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof FloatValue){
        double leftValue = leftValueNode.getLongValue();
        double rightValue = rightValueNode.getLongValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof StringValue){
        String leftValue = leftValueNode.getStringValue();
        String rightValue = rightValueNode.getStringValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else
        return BooleanValue.create(false);
    } else if(leftValueNode instanceof FloatValue){
      if (rightValueNode instanceof BooleanValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof IntegerValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof FloatValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof StringValue){
        String leftValue = leftValueNode.getStringValue();
        String rightValue = rightValueNode.getStringValue();
        boolean sumValue = leftValue == rightValue;
        return BooleanValue.create(sumValue);
      } else
        return BooleanValue.create(false);
    } else if(leftValueNode instanceof StringValue){
      if (rightValueNode instanceof BooleanValue){
        String leftValue = leftValueNode.getStringValue();
        String rightValue = rightValueNode.getStringValue();
        boolean sumValue = leftValue.equals(rightValue);
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof IntegerValue){
        String leftValue = leftValueNode.getStringValue();
        String rightValue = rightValueNode.getStringValue();
        boolean sumValue = leftValue.equals(rightValue);
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof FloatValue){
        String leftValue = leftValueNode.getStringValue();
        String rightValue = rightValueNode.getStringValue();
        boolean sumValue = leftValue.equals(rightValue);
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof StringValue){
        String leftValue = leftValueNode.getStringValue();
        String rightValue = rightValueNode.getStringValue();
        boolean sumValue = leftValue.equals(rightValue);
        return BooleanValue.create(sumValue);
      } else
        return BooleanValue.create(false);
    } else
      return BooleanValue.create(false);
  }

}
