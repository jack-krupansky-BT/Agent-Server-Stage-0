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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionNode;
import com.basetechnology.s0.agentserver.script.intermediate.ObjectTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;

public class Value extends ExpressionNode {

  public Value evaluateExpression(ScriptState scriptState){
    return this;
  }
  
  public Object getValue(){
    return null;
  }

  public boolean getBooleanValue(){
    return false;
  }

  public int getIntValue(){
    return (int)getLongValue();
  }

  public long getLongValue(){
    return 0;
  }

  public double getDoubleValue(){
    return 0.0;
  }

  public float getFloatValue(){
    return (float)getDoubleValue();
  }

  public String getStringValue(){
    return null;
  }

  public boolean getBooleanValue(ScriptState scriptState){
    return getBooleanValue();
  }

  public long getLongValue(ScriptState scriptState){
    return getLongValue();
  }

  public double getDoubleValue(ScriptState scriptState){
    return getDoubleValue();
  }

  public String getStringValue(ScriptState scriptState){
    return getStringValue();
  }

  public Value getNamedValue(ScriptState scriptState, String name) throws RuntimeException {
    if (name.equals("type"))
      return new StringValue(getTypeString());
    else if (name.equals("toJson") || name.equals("json"))
      return new StringValue(toJson());
    else if (name.equals("toString"))
      return new StringValue(toString());
    else if (name.equals("toText"))
      return new StringValue(toText());
    else if (name.equals("toXml"))
      return new StringValue(toXml());
    else if (name.equals("boolean"))
      return BooleanValue.create(getBooleanValue());
    else if (name.equals("int") || name.equals("integer"))
      return new IntegerValue(getIntValue());
    else if (name.equals("float"))
      return new FloatValue(getFloatValue());
    else if (name.equals("string"))
      return new StringValue(toString());
    else if (name.equals("text"))
      return new StringValue(toText());
    else
      throw new RuntimeException("No item named '" + name + "' for value of type " + getTypeString());
  }

  public Value getMethodValue(ScriptState scriptState, String name, List<Value> arguments) throws RuntimeException {
    int numArguments = arguments.size();
    if (name.equals("type") && numArguments == 0)
      return new StringValue(getTypeString());
    else if ((name.equals("toJson") || name.equals("json")) && numArguments == 0)
      return new StringValue(toJson());
    else if (name.equals("toString") && numArguments == 0)
      return new StringValue(toString());
    else
      throw new RuntimeException("No method named '" + name + "' with " + numArguments + " arguments for value of type " + getTypeString());
  }

  public Value getSubscriptedValue(ScriptState scriptState, List<Value> subscriptValues) throws RuntimeException {
    throw new RuntimeException("Subscripting is not supported for value of type " + getTypeString());
  }

  public Value putSubscriptedValue(ScriptState scriptState, List<Value> subscriptValues, Value value) throws RuntimeException {
    throw new RuntimeException("Subscripting assignment is not supported for value of type " + getTypeString());
  }

  public Value add(Value otherValue){
    return otherValue;
  }

  public Value divide(int divisor){
    return NullValue.one;
  }
  
  public int compareValue(Value otherValue){
    // Shouldn't get here
    // TODO: Maybe this should throw an exception
    return -1;
  }
  
  public Value copyOnAssignment(){
    // For most values, no need to make a copy of actual value on assignment, only strings 
    return this;
  }

  public Value clone(){
    return this;
  }
  
  public String toJson(){
    return toString();
  }

  public Object toJsonObject() throws AgentServerException {
    return getValue();
  }
  
  public String toString(){
    return "<value-node-" + getTypeString() + ">";
  }

  public String toText(){
    return "";
  }

  public TypeNode getType(){
    return ObjectTypeNode.one;
  }
  
  public String getTypeString(){
    return "<value-node>";
  }

  static public Value toValueNode(Object object){
    if (object == null)
      return NullValue.one;
    else if (object == (Boolean)false)
      return FalseValue.one;
    else if (object == (Boolean)true)
      return TrueValue.one;
    else if (object instanceof Integer)
      return new IntegerValue((Integer)object);
    else if (object instanceof Long)
      return new IntegerValue((Long)object);
    else if (object instanceof Float)
      return new FloatValue((Float)object);
    else if (object instanceof Double)
      return new FloatValue((Double)object);
    else if (object instanceof String)
      return new StringValue((String)object);
    else if (object instanceof List){
      List<Object> list = (List<Object>)object;
      List<Value> valueNodeList = new ArrayList<Value>();
      for (Object element: list)
        valueNodeList.add(toValueNode(element));
      return new ListValue(ObjectTypeNode.one, valueNodeList);
    } else if (object instanceof Map){
      Map<String, Object> list = (Map<String, Object>)object;
      List<FieldValue> valueNodeList = new ArrayList<FieldValue>();
      for (String key: list.keySet())
        valueNodeList.add(new FieldValue(key, toValueNode(list.get(key))));
      return new MapValue(ObjectTypeNode.one, (List<Value>)(Object)valueNodeList);
    } else
      return NullValue.one;
  }

  public boolean equals(Value valueNode){
    return valueNode instanceof NullValue;
  }

  public String toXml(){
    return "";
  }
  
  public Value negateValue(){
    return null;
  }
}
