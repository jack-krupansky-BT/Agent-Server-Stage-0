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

package com.basetechnology.s0.agentserver.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.intermediate.ObjectTypeNode;
import com.basetechnology.s0.agentserver.script.runtime.value.BooleanValue;
import com.basetechnology.s0.agentserver.script.runtime.value.FieldValue;
import com.basetechnology.s0.agentserver.script.runtime.value.FloatValue;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.ListValue;
import com.basetechnology.s0.agentserver.script.runtime.value.MapValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class JsonUtils {

  static public Value convertJsonValue(Object jsonObject) throws RuntimeException {
    if (jsonObject == null || jsonObject == JSONObject.NULL)
      return NullValue.one;
    else if (jsonObject instanceof JSONObject)
      return convertJsonObject((JSONObject)jsonObject);
    else if (jsonObject instanceof JSONArray)
      return convertJsonArray((JSONArray)jsonObject);
    else if (jsonObject instanceof Boolean)
      return BooleanValue.create((Boolean)jsonObject);
    else if (jsonObject instanceof Integer)
      return new IntegerValue((Integer)jsonObject);
    else if (jsonObject instanceof Long)
      return new IntegerValue((Long)jsonObject);
    else if (jsonObject instanceof Double)
      return new FloatValue((Double)jsonObject);
    else if (jsonObject instanceof String)
      return new StringValue((String)jsonObject);
    else
      throw new RuntimeException("Internal error - unable to convert JSON value of type " + jsonObject.getClass().getSimpleName());
  }

  static public Value convertJsonArray(JSONArray arrayJson) throws RuntimeException {
    List<Value> list = new ArrayList<Value>();
    int numElements = arrayJson.length();
    try {
      for (int i = 0; i < numElements; i++)
        list.add(convertJsonValue(arrayJson.get(i)));
    } catch (Exception e){
      throw new RuntimeException("parseJson exception converting value: " + e.getMessage());
    }
    return new ListValue(ObjectTypeNode.one, list);
  }

  static public Value convertJsonObject(JSONObject objectJson) throws RuntimeException {
    List<FieldValue> list = new ArrayList<FieldValue>();
    try {
      Iterator<String> iterator = objectJson.keys();
      while (iterator.hasNext()){
        String key = iterator.next();
        list.add(new FieldValue(key, convertJsonValue(objectJson.get(key))));
      }
    } catch (Exception e){
      throw new RuntimeException("parseJson exception converting value: " + e.getMessage());
    }
    return new MapValue(ObjectTypeNode.one, (List<Value>)(Object)list);
  }
  
  static public Value parseJson(String s) throws RuntimeException {
    if (s == null)
      return NullValue.one;
    String sTrim = s.trim();
    int len = s.length();
    if (len == 0)
      return NullValue.one;
    char ch = len < 1 ? 0 : sTrim.charAt(0);
    try {
    if (ch == '['){
      JSONArray arrayJson = new JSONArray(s);
      return convertJsonArray(arrayJson);
    } else if (ch == '{'){
      JSONObject objectJson = new JSONObject(s);
      return convertJsonObject(objectJson);
    } else {
      // Must be a simple Java object
      // Check for boolean
      if (s.equalsIgnoreCase("true"))
        return BooleanValue.create(true);
      else if (s.equalsIgnoreCase("false"))
        return BooleanValue.create(false);
      
      // Check for string
      if (len > 1 && s.charAt(0) == '"')
        return new StringValue(StringUtils.parseQuotedString(s));

      // Try for an integer
      try {
        long longInteger = Long.parseLong(s);
        return new IntegerValue(longInteger);
      } catch (NumberFormatException e){
        // Try for a real number
        try {
          double doubleFloat = Double.parseDouble(s);
          return new FloatValue(doubleFloat);
        } catch (NumberFormatException e1){
          throw new RuntimeException("parseJson exception - string is not a valid JSON value: '" + s + "'");
        }
      }
    }
    } catch (Exception e){
      throw new RuntimeException("parseJson exception: " + e.getMessage());
    }
  }

  public static void validateKeys(JSONObject objectJson, String objectName, List<String> validKeys) throws AgentServerException {
    String badKeys = "";
    Map<String, Value> treeMap = new TreeMap<String, Value>();
    for (Iterator<String> it = objectJson.keys(); it.hasNext(); )
      treeMap.put(it.next(), null);
    for (String key: treeMap.keySet())
      if (! validKeys.contains(key))
        badKeys += (badKeys.length() > 0 ? ", " : "") + key;
    if (badKeys.length() > 0)
      throw new AgentServerException(objectName + " JSON has invalid keys: " + badKeys);
  }
  
  public static String getString(JSONObject objectJson, String key, String defaultString){
    if (objectJson.has(key)){
      Object object = objectJson.opt(key);
      if (object instanceof Boolean)
        return Boolean.toString((Boolean)object);
      else if (object instanceof Integer)
        return Integer.toString((Integer)object);
      else if (object instanceof Long)
        return Long.toString((Long)object);
      else if (object instanceof Float)
        return Float.toString((Float)object);
      else if (object instanceof Double)
        return Double.toString((Double)object);
      else
        return (String)object;
    } else
      return defaultString;
  }
}
