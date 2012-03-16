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
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class DivideNode extends BinaryExpressionNode {

  public DivideNode(ExpressionNode leftNode, ExpressionNode rightNode){
    super(leftNode, rightNode);
  }

  // TODO: Maybe strings should be converted to compatible type
  
  public Value evaluateExpression(ScriptState scriptState) throws AgentServerException {
    scriptState.countNodeExecutions();
    Value leftValueNode = leftNode.evaluateExpression(scriptState);
    Value rightValueNode = rightNode.evaluateExpression(scriptState);
    return divide(leftValueNode, rightValueNode);
  }

  public static Value divide(Value leftValueNode, Value rightValueNode){
    if(leftValueNode instanceof BooleanValue){
      if (rightValueNode instanceof BooleanValue){
        boolean leftValue = leftValueNode.getBooleanValue();
        boolean rightValue = rightValueNode.getBooleanValue();
        // TODO: What should this be?
        boolean sumValue = leftValue && rightValue;
        return BooleanValue.create(sumValue);
      } else if (rightValueNode instanceof IntegerValue){
        long leftValue = leftValueNode.getLongValue();
        long rightValue = rightValueNode.getLongValue();
        // TODO: How to handle zero-divide
        long sumValue = leftValue / rightValue;
        return new FloatValue(sumValue);
      } else if (rightValueNode instanceof FloatValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        double sumValue = leftValue / rightValue;
        return new FloatValue(sumValue);
      } else if (rightValueNode instanceof StringValue){
        long leftValue = leftValueNode.getLongValue();
        long rightValue = rightValueNode.getLongValue();
        // TODO: What should this really be: boolean * string??
        long sumValue = leftValue / rightValue;
        return new IntegerValue(sumValue);
      } else
        return leftValueNode;
    } else if(leftValueNode instanceof IntegerValue){
      if (rightValueNode instanceof BooleanValue){
        long leftValue = leftValueNode.getLongValue();
        long rightValue = rightValueNode.getLongValue();
        long sumValue = leftValue / rightValue;
        return new IntegerValue(sumValue);
      } else if (rightValueNode instanceof IntegerValue){
        long leftValue = leftValueNode.getLongValue();
        long rightValue = rightValueNode.getLongValue();
        long sumValue = leftValue / rightValue;
        return new IntegerValue(sumValue);
      } else if (rightValueNode instanceof FloatValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        double sumValue = leftValue / rightValue;
        return new FloatValue(sumValue);
      } else if (rightValueNode instanceof StringValue){
        long leftValue = leftValueNode.getLongValue();
        long rightValue = rightValueNode.getLongValue();
        // TODO: What should this really be: integer * string??
        // Maybe replicate the string n times?
        long sumValue = leftValue / rightValue;
        return new IntegerValue(sumValue);
      } else
        return leftValueNode;
    } else if(leftValueNode instanceof FloatValue){
      if (rightValueNode instanceof BooleanValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        double sumValue = leftValue / rightValue;
        return new FloatValue(sumValue);
      } else if (rightValueNode instanceof IntegerValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        double sumValue = leftValue / rightValue;
        return new FloatValue(sumValue);
      } else if (rightValueNode instanceof FloatValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        double sumValue = leftValue / rightValue;
        return new FloatValue(sumValue);
      } else if (rightValueNode instanceof StringValue){
        double leftValue = leftValueNode.getDoubleValue();
        double rightValue = rightValueNode.getDoubleValue();
        // TODO: What should this really be: integer * string??
        // Maybe replicate the string n times?
        double sumValue = leftValue / rightValue;
        return new FloatValue(sumValue);
      } else
        return leftValueNode;
    } else if(leftValueNode instanceof StringValue){
      if (rightValueNode instanceof BooleanValue){
        String leftValue = leftValueNode.getStringValue();
        boolean rightValue = rightValueNode.getBooleanValue();
        // TODO: What should this really be: integer * string??
        // Maybe replicate the string n times?
        String sumValue = rightValue ? leftValue : "";
        return new StringValue(sumValue);
      } else if (rightValueNode instanceof IntegerValue){
        String leftValue = leftValueNode.getStringValue();
        long rightValue = rightValueNode.getLongValue();
        
        // TODO: What should this really be?
        
        // Replicate the string n times
        StringBuilder sb = new StringBuilder();
        for (long i = 0; i < rightValue; i++)
          sb.append(leftValue);
        String sumValue = sb.toString();
        return new StringValue(sumValue);
      } else if (rightValueNode instanceof FloatValue){
        String leftValue = leftValueNode.getStringValue();
        double rightValue = rightValueNode.getDoubleValue();
        
        // TODO: What should this really be?
        
        // Replicate the string n times
        StringBuilder sb = new StringBuilder();
        for (long i = 0; i < rightValue; i++)
          sb.append(leftValue);
        String sumValue = sb.toString();
        return new StringValue(sumValue);
      } else if (rightValueNode instanceof StringValue){
        String leftValue = leftValueNode.getStringValue();
        String rightValue = rightValueNode.getStringValue();
        // TODO: What should this really be: string * string??
        String sumValue = leftValue + rightValue;
        return new StringValue(sumValue);
      } else
        // TODO: Reconsider whether string plus null is only the string itself
        return leftValueNode;
    } else
      return leftValueNode;
  }
}
