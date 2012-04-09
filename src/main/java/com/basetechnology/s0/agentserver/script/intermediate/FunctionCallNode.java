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
import com.basetechnology.s0.agentserver.script.runtime.value.FloatValue;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class FunctionCallNode extends ExpressionNode {
  public String functionName;
  public List<ExpressionNode> argumentList;
  
  public FunctionCallNode(String functionName, List<ExpressionNode> argumentList){
    this.functionName = functionName;
    this.argumentList = argumentList;
  }

  public Value evaluateExpression(ScriptState scriptState) throws AgentServerException {
    scriptState.countNodeExecutions();
    int numArgs = argumentList.size();

    if (functionName.equals("avg") && numArgs >= 1){
      Value sumValue = argumentList.get(0).evaluateExpression(scriptState);
      for (int i = 1; i < numArgs; i++){
        Value nextValue = argumentList.get(i).evaluateExpression(scriptState);
        sumValue = sumValue.add(nextValue);
      }
      return sumValue.divide(numArgs);
    } if (functionName.equals("centuries") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = arg1.getDoubleValue(scriptState);
      return new IntegerValue((long)(value * 100 * 365 * 24 * 60 * 60 * 1000));
    } if (functionName.equals("days") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = arg1.getDoubleValue(scriptState);
      return new IntegerValue((long)(value * 24 * 60 * 60 * 1000));
    } if (functionName.equals("decades") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = arg1.getDoubleValue(scriptState);
      return new IntegerValue((long)(value * 10 * 365 * 24 * 60 * 60 * 1000));
    } if (functionName.equals("eval") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      String expression = arg1.getStringValue();
      Value returnValue = scriptState.agentInstance.evaluateExpression(expression);
      return returnValue;
    } if (functionName.equals("hours") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = arg1.getDoubleValue(scriptState);
      return new IntegerValue((long)(value * 60 * 60 * 1000));
    } if (functionName.equals("max") && numArgs >= 1){
      Value maxValue = argumentList.get(0).evaluateExpression(scriptState);
      for (int i = 1; i < numArgs; i++){
        Value nextValue = argumentList.get(i).evaluateExpression(scriptState);
        if (nextValue.compareValue(maxValue) > 0)
          maxValue = nextValue;
      }
      return maxValue;
    } if (functionName.equals("minutes") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = arg1.getDoubleValue(scriptState);
      return new IntegerValue((long)(value * 60 * 1000));
    } if (functionName.equals("min") && numArgs >= 1){
      Value minValue = argumentList.get(0).evaluateExpression(scriptState);
      for (int i = 1; i < numArgs; i++){
        Value nextValue = argumentList.get(i).evaluateExpression(scriptState);
        if (nextValue.compareValue(minValue) < 0)
          minValue = nextValue;
      }
      return minValue;
    } if (functionName.equals("months") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = arg1.getDoubleValue(scriptState);
      return new IntegerValue((long)(value * 30 * 24 * 60 * 60 * 1000));
    } if (functionName.equals("ms") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = arg1.getDoubleValue(scriptState);
      return new IntegerValue((long)value);
    } if (functionName.equals("notify") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      String notificationName = arg1.getStringValue(scriptState);
      scriptState.agentInstance.queueNotify(notificationName);
      return NullValue.one;
    } if (functionName.equals("now") && numArgs == 0){
      return new IntegerValue(System.currentTimeMillis());
    } if (functionName.equals("runScript") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      String scriptString = arg1.getStringValue();
      Value returnValue = scriptState.agentInstance.runScriptString(scriptString);
      return returnValue;
    } if (functionName.equals("seconds") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = arg1.getDoubleValue(scriptState);
      return new IntegerValue((long)(value * 1000));
    } else if (functionName.equals("sqrt") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = Math.sqrt(arg1.getDoubleValue(scriptState));
      return new FloatValue(value);
    } if (functionName.equals("sum") && numArgs >= 1){
      Value sumValue = argumentList.get(0).evaluateExpression(scriptState);
      for (int i = 1; i < numArgs; i++){
        Value nextValue = argumentList.get(i).evaluateExpression(scriptState);
        sumValue = sumValue.add(nextValue);
      }
      return sumValue;
    } if (functionName.equals("weeks") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = arg1.getDoubleValue(scriptState);
      return new IntegerValue((long)(value * 7 * 24 * 60 * 60 * 1000));
    } else if ((functionName.equals("wait") || functionName.equals("sleep")) && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      try {
        Thread.sleep(arg1.getIntValue());
      } catch (InterruptedException e){
        // Ignore the exception
      }
      return NullValue.one;
    } if (functionName.equals("years") && numArgs == 1){
      Value arg1 = argumentList.get(0).evaluateExpression(scriptState);
      double value = Math.sqrt(arg1.getDoubleValue(scriptState));
      return new IntegerValue((long)(value * 365 * 24 * 60 * 60 * 1000));
    } else {
      // Check for user-defined functions
      List<Value> argumentValues = new ArrayList<Value>();
      List<TypeNode> argumentTypes = new ArrayList<TypeNode>();
      for (ExpressionNode argumentNode: argumentList){
        Value argumentValue = argumentNode.evaluateExpression(scriptState);
        argumentValues.add(argumentValue);
        TypeNode argumentType = argumentValue.getType();
        argumentTypes.add(argumentType);
      }
      ScriptNode scriptNode = scriptState.get(functionName, argumentTypes);
      if (scriptNode != null){
        Value valueNode = scriptState.scriptRuntime.runScript(functionName, scriptNode, argumentValues);
        return valueNode;
      } else
        throw new RuntimeException("Unknown function: " + functionName + " with " + numArgs + " arguments");
    }
  }
}
