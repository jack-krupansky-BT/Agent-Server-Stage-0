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

package com.basetechnology.s0.agentserver.field;

import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.script.intermediate.IntegerTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.ObjectTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class IntField extends Field {
  public long defaultValue;
  public long minValue;
  public long maxValue;
  public int nominalWidth;

  public IntField(SymbolTable symbolTable, String name){
    this.symbol = new Symbol(symbolTable, name, IntegerTypeNode.one);
    this.label = name;
    minValue = Long.MIN_VALUE;
    maxValue = Long.MAX_VALUE;
  }

  public IntField(SymbolTable symbolTable, String name, String label){
    this.symbol = new Symbol(symbolTable, name, IntegerTypeNode.one);
    this.label = label;
    minValue = Long.MIN_VALUE;
    maxValue = Long.MAX_VALUE;
  }

  public IntField(SymbolTable symbolTable, String name, String label, String description, long defaultValue, long minValue, long maxValue, int nominalWidth){
    this.symbol = new Symbol(symbolTable, name, IntegerTypeNode.one);
    this.label = label;
    this.description = description;
    this.defaultValue = defaultValue;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.nominalWidth = nominalWidth;
  }

  public Field clone(){
    return new IntField(symbol.symbolTable, symbol.name, label, description, defaultValue, minValue, maxValue, nominalWidth);
  }

  public Object getDefaultValue(){
    return defaultValue;
  }

  public Value getDefaultValueNode(){
    return new IntegerValue(defaultValue);
  }

  public TypeNode getType(){
    return IntegerTypeNode.one;
  }

  public static Field fromJson(SymbolTable symbolTable, JSONObject fieldJson){
    String type = fieldJson.optString("type");
    if (type == null || ! (type.equals("int") || type.equals("integer")))
      return null;
    String name = fieldJson.has("name") ? fieldJson.optString("name") : null;
    String label = fieldJson.has("label") ? fieldJson.optString("label") : null;
    String description = fieldJson.has("description") ? fieldJson.optString("description") : null;
    long defaultValue = fieldJson.has("default_value") ? fieldJson.optLong("default_value") : 0;
    long minValue = fieldJson.has("min_value") ? fieldJson.optLong("min_value") : Long.MIN_VALUE;
    long maxValue = fieldJson.has("max_value") ? fieldJson.optLong("max_value") : Long.MAX_VALUE;
    int nominalWidth = fieldJson.has("nominal_width") ? fieldJson.optInt("nominal_width") : 0;
    return new IntField(symbolTable, name, label, description, defaultValue, minValue, maxValue, nominalWidth);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("type", "int");
    if (symbol.name != null)
      json.put("name", symbol.name);
    if (label != null)
      json.put("label", label);
    if (description != null)
      json.put("description", description);
    if (defaultValue != 0)
      json.put("default_value", defaultValue);
    if (minValue != Long.MIN_VALUE)
      json.put("min_value", minValue);
    if (maxValue != Long.MAX_VALUE)
      json.put("max_value", maxValue);
    if (nominalWidth != 0)
      json.put("nominal_width", nominalWidth);
    return json;
  }
  
  public String toString(){
    return "[Int field symbol: " + symbol + " label: " + label +
        " description: '" + description + "'" + " default value: " + defaultValue +
        " min value: " + minValue + " max value: " + maxValue +
        " nominal width: " + nominalWidth +
        "]";
  }
}
