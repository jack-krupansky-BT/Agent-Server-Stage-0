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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.intermediate.StringTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.ListMap;

public class MapValue extends Value {
  public TypeNode type;
  public ListMap<String, Value> value;

  public MapValue(TypeNode type, List<Value> value){
    this.type = type;
    ListMap<String, Value> newValueMap = new ListMap<String, Value>();
    if (value != null){
      List<FieldValue> fieldValueList = (List<FieldValue>)(Object)value;
      for (FieldValue fieldValueNode: fieldValueList)
        newValueMap.put(fieldValueNode.fieldName, fieldValueNode.valueNode);
    }
    this.value = newValueMap;
  }

  public Object getValue(){
    return value;
  }

  public boolean getBooleanValue(){
    return value != null && value.size() > 0;
  }

  public long getLongValue(){
    return value.size();
  }

  public double getDoubleValue(){
    return value.size();
  }

  public String getStringValue(){
    return Integer.toString(value.size());
  }

  public Value getNamedValue(ScriptState scriptState, String name) throws RuntimeException {
    // First check if there is a key with this name
    if (value.containsKey(name)){
      // If so, simply return the value associated with that key
      return value.get(name);
    }
    
    // If no such key, treat as a zero-argument function call
    if (name.equals("length") || name.equals("size"))
      return new IntegerValue(value.size());
    else if (name.equals("clear")){
      // Clear the list
      value.clear();

      // No return value
      return NullValue.one;
    } else if (name.equals("concat")){
      // Combine all elements into a single string with comma as a delimiter
      // key:value,...
      StringBuilder sb = new StringBuilder();
      for (String key: value){
        if (sb.length() > 0)
          sb.append(',');
        sb.append(key);
        sb.append(':');
        sb.append(value.get(key));
      }
      
      // Return the combined string
      return new StringValue(sb.toString());
    } else if (name.equals("keys")){
      // Build list of key string values
      List<Value> keysList = new ArrayList<Value>();
      for (String key: value.keySet())
        keysList.add(new StringValue(key));
      
      // Generate and return the new value node for list of the key strings for map
      return new ListValue(StringTypeNode.one, keysList);
    } else if (name.equals("max")){
      // See if any elements in list
      int numElements = value.size();
      if (numElements == 0)
        // No, then it has no maximum value
        return NullValue.one;
      
      // Iterate over list looking for maximum value
      Value maxValueNode = value.get(0);
      for (String key: value.keySet()){
        Value valueNode = value.get(key);
        if (valueNode.compareValue(maxValueNode) > 0)
          maxValueNode = valueNode;
      }
      
      // Return the maximum value
      return maxValueNode;
    } else if (name.equals("min")){
      // See if any elements in list
      int numElements = value.size();
      if (numElements == 0)
        // No, then it has no minimum value
        return NullValue.one;
      
      // Iterate over list looking for minimum value
      Value minValueNode = value.get(0);
      for (String key: value.keySet()){
        Value valueNode = value.get(key);
        if (valueNode.compareValue(minValueNode) < 0)
          minValueNode = valueNode;
      }
      
      // Return the minimum value
      return minValueNode;
    } else
      return super.getNamedValue(scriptState, name);
  }

  public Value getMethodValue(ScriptState scriptState, String name, List<Value> arguments) throws RuntimeException {
    int numArguments = arguments.size();
    if ((name.equals("length") || name.equals("size")) && numArguments == 0)
      return new IntegerValue(value.size());
    else if ((name.equals("add") || name.equals("put") || name.equals("set")) && numArguments == 2){
      // Append the new value
      value.put(arguments.get(0).getStringValue(), arguments.get(1));
      // TODO: Find out what this Java return value is really all about
      return TrueValue.one;
    } else if (name.equals("clear") && numArguments == 0){
      // Clear the list
      value.clear();
      
      // No return value
      return NullValue.one;
      // TODO: Add "contains"
    } else if (name.equals("concat") && numArguments <= 1){
      // Combine all elements into a single string specified delimiter (or comma) between them
      // Combine all elements into a single string with comma as a delimiter
      // key:value,...
      String delimiter = ",";
      if (numArguments == 1)
        delimiter = arguments.get(0).getStringValue();
      StringBuilder sb = new StringBuilder();
      for (String key: value){
        if (sb.length() > 0)
          sb.append(delimiter);
        sb.append(key);
        sb.append(':');
        sb.append(value.get(key));
      }
      
      // Return the combined string
      return new StringValue(sb.toString());
    } else if (name.equals("keys") && numArguments == 0){
      // Build list of key string values
      List<Value> keysList = new ArrayList<Value>();
      for (String key: value.keySet())
        keysList.add(new StringValue(key));
      
      // Generate and return the new value node for list of the key strings for map
      return new ListValue(StringTypeNode.one, keysList);
    } else if (name.equals("remove") && numArguments == 1){
      // Get the key of element to remove
      String key = arguments.get(0).getStringValue();

      // Remove the value
      Value removedValue = value.remove(key);
      
      // Return the removed value
      return removedValue;

    } else
      return super.getMethodValue(scriptState, name, arguments);
  }

  public Value getSubscriptedValue(ScriptState scriptState, List<Value> subscriptValues) throws RuntimeException {
    int numSubscripts = subscriptValues.size();
    if (numSubscripts == 1){
      // Fetch element with that key
      String key = subscriptValues.get(0).getStringValue();
      if (value.containsKey(key))
        return (Value)value.get(key);
      else
        return NullValue.one;
    } else
      throw new RuntimeException("Maps do not support " + numSubscripts + " subscripts");
  }

  public Value get(String key) throws RuntimeException {
    if (value.containsKey(key))
      return (Value)value.get(key);
    else
      return NullValue.one;
  }

  public Value putSubscriptedValue(ScriptState scriptState, List<Value> subscriptValues, Value newValue) throws RuntimeException {
    int numSubscripts = subscriptValues.size();
    if (numSubscripts == 1){
      // Modify element with that key
      String key = subscriptValues.get(0).getStringValue();
      value.put(key, newValue);
      return newValue;
    } else
      throw new RuntimeException("Maps do not support " + numSubscripts + " subscripts for assignment");
  }

  public Value put(String key, Value newValue) throws RuntimeException {
    value.put(key, newValue);
    return newValue;
  }

  public MapValue clone(){
    List<FieldValue> newList = new ArrayList<FieldValue>();
    for(String key: value.keySet())
      newList.add(new FieldValue(key, value.get(key).clone()));
    return new MapValue(type, (List<Value>)(Object)newList);
  }
  
  public String toJson(){
    // Return JSON-format comma-separated list of element key/value pairs within braces
    StringBuilder sb = new StringBuilder("{");
    for (String parameterName: value.keySet()){
      Value valueNode = value.get(parameterName);
      if (sb.length() > 1)
        sb.append(", ");
      sb.append('"');
      // TODO: Should escape this
      sb.append(parameterName);
      sb.append("\": ");
      sb.append(valueNode.toJson());
    }
    sb.append('}');
    return sb.toString();
  }
  
  public Object toJsonObject() throws AgentServerException {
    try {
    JSONObject json = new JsonListMap();
    for (String parameterName: value.keySet())
      json.put(parameterName, value.get(parameterName).toJsonObject());
    return json;
    } catch (JSONException e){
      e.printStackTrace();
      throw new AgentServerException("JSON exception formatting map object - " + e.getMessage());
    }
  }
  
  public String toString(){
    // Return comma-separated list of element key/value pairs within braces
    StringBuilder sb = new StringBuilder("{");
    for (String key: value.keySet()){
      Value valueNode = value.get(key);
      if (sb.length() > 1)
        sb.append(", ");
      sb.append(key);
      sb.append(": ");
      sb.append(valueNode.toString());
    }
    sb.append('}');
    return sb.toString();
  }

  public String toText(){
    // Return space-delimited list of element values
    StringBuilder sb = new StringBuilder();
    for (String key: value.keySet()){
      // Get the text of the next element
      Value valueNode = value.get(key);
      String text = valueNode.toText();
      
      // Ignore empty text
      if (text == null || text.length() == 0)
        continue;
      
      // Separate text with single space
      if (sb.length() > 1)
        sb.append(" ");
      
      // Tack on the text for this element
      sb.append(text);
    }
    
    // Return the acculated text
    return sb.toString();
  }

  public String toXml(){
    StringBuilder sb = new StringBuilder();
    for (String key: value){
      // XML formatting depends a little on value type
      Value valueNode = value.get(key);
      if (valueNode instanceof ListValue)
        sb.append(((ListValue)valueNode).toXml(key));
      else {
        // Generate the element start tag
        sb.append("<" + key + ">");

        // Get the XML text of the element
        String xmlText = valueNode.toXml();
        sb.append(xmlText);

        // Generate the element end tag
        sb.append("</" + key + ">");
      }
    }

    // Return the accumulated text
    return sb.toString();
  }

  public String getTypeString(){
    return "map<string, " + type.toString() + ">";
  }

  public boolean equals(Value valueNode){
    // Other value must also be a map
    if (valueNode instanceof MapValue){
      // Sizes must agree
      int len1 = value.size();
      MapValue value2 = (MapValue)valueNode;
      int len2 = value2.value.size();
      if (len1 != len2)
        return false;
      
      // And values for each key must match recursively
      for (String key: value.keySet())
        if (! value2.value.containsKey(key))
          return false;
        else if (! value.get(key).equals(value2.value.get(key)))
          return false;
      
      // Everything matches
      return true;
    } else
      // No match
      return false;
  }

}
