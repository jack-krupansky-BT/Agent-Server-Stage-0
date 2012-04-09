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

package com.basetechnology.s0.agentserver.script.runtime.value;

import java.util.List;

import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.intermediate.FloatTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;

public class FloatValue extends NumberValue {
  public static final double EPSILON = 0.0000001;
  public double value;

  static public FloatValue zero = new FloatValue(0.0);
  
  public FloatValue(double value){
    this.value = value;
  }

  public TypeNode getType(){
    return FloatTypeNode.one;
  }

  public Value getDefaultValue(){
    return FloatValue.zero;
  }

  public Object getValue(){
    return value;
  }

  public boolean getBooleanValue(){
    return value != 0;
  }

  public long getLongValue(){
    return (long)value;
  }

  public double getDoubleValue(){
    return value;
  }

  public String getStringValue(){
    return Double.toString(value);
  }

  public Value getNamedValue(ScriptState scriptState, String name) throws RuntimeException {
    if (name.equals("sqrt"))
      return new FloatValue(Math.sqrt(this.getFloatValue()));
    return super.getNamedValue(scriptState, name);
  }

  public Value getMethodValue(ScriptState scriptState, String name, List<Value> arguments) throws RuntimeException {
    int numArguments = arguments.size();
    if (name.equals("sqrt") && numArguments == 0)
      return new FloatValue(Math.sqrt(this.getFloatValue()));
    else
      return super.getMethodValue(scriptState, name, arguments);
  }

  public Value add(Value otherValue){
    return new FloatValue(value + otherValue.getFloatValue());
  }

  public Value divide(int divisor){
    return new FloatValue(value / divisor);
  }

  public int compareValue(Value otherValue){
    if (otherValue instanceof BooleanValue){
      double leftValue = getDoubleValue();
      double rightValue = otherValue.getDoubleValue();
      if (leftValue == rightValue)
        return 0;
      else if (leftValue < rightValue)
        return -1;
      else
        return 1;
    } else if (otherValue instanceof IntegerValue){
      double leftValue = getDoubleValue();
      double rightValue = otherValue.getDoubleValue();
      if (leftValue == rightValue)
        return 0;
      else if (leftValue < rightValue)
        return -1;
      else
        return 1;
    } else if (otherValue instanceof FloatValue){
      double leftValue = getDoubleValue();
      double rightValue = otherValue.getDoubleValue();
      if (leftValue == rightValue)
        return 0;
      else if (leftValue < rightValue)
        return -1;
      else
        return 1;
    } else if (otherValue instanceof StringValue){
      String leftValue = getStringValue();
      String rightValue = otherValue.getStringValue();
      return leftValue.compareTo(rightValue);
    } else {
      // TODO: What should this be?
      double leftValue = getDoubleValue();
      double rightValue = otherValue.getDoubleValue();
      if (leftValue == rightValue)
        return 0;
      else if (leftValue < rightValue)
        return -1;
      else
        return 1;
    }
  }
  
  public String toString(){
    return Double.toString(value);
  }

  public String getTypeString(){
    return "float";
  }

  public boolean equals(Value valueNode){
    return valueNode instanceof FloatValue && (Math.abs(value - valueNode.getDoubleValue()) < EPSILON);
  }

  public Value negateValue(){
    return new FloatValue(- value);
  }

}
