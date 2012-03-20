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

import com.basetechnology.s0.agentserver.script.intermediate.BooleanTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.IntegerTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.ObjectTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.value.BooleanValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class BooleanField extends Field {
  public boolean defaultValue;

  public BooleanField(SymbolTable symbolTable, String name){
    this.symbol = new Symbol(symbolTable, name, BooleanTypeNode.one);
    this.label = name;
  }

  public BooleanField(SymbolTable symbolTable, String name, String label){
    this.symbol = new Symbol(symbolTable, name, BooleanTypeNode.one);
    this.label = label;
  }

  public BooleanField(SymbolTable symbolTable, String name, String label, String description, boolean defaultValue, String compute){
    this.symbol = new Symbol(symbolTable, name, BooleanTypeNode.one);
    this.label = label;
    this.description = description;
    this.defaultValue = defaultValue;
    this.compute = compute;
  }

  public Field clone(){
    return new BooleanField(symbol.symbolTable, symbol.name, label, description, defaultValue, compute);
  }

  public Object getDefaultValue(){
    return defaultValue;
  }

  public Value getDefaultValueNode(){
    return BooleanValue.create(defaultValue);
  }

  public TypeNode getType(){
    return BooleanTypeNode.one;
  }

  public static Field fromJson(SymbolTable symbolTable, JSONObject fieldJson){
    String type = fieldJson.optString("type");
    if (type == null || ! (type.equals("option") || type.equals("boolean")))
      return null;
    String name = fieldJson.has("name") ? fieldJson.optString("name") : null;
    String label = fieldJson.has("label") ? fieldJson.optString("label") : null;
    String description = fieldJson.has("description") ? fieldJson.optString("description") : null;
    boolean defaultValue = fieldJson.has("default_value") ? fieldJson.optBoolean("default_value") : true;
    String compute = fieldJson.has("compute") ? fieldJson.optString("compute") : null;
    return new BooleanField(symbolTable, name, label, description, defaultValue, compute);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("type", "boolean");
    if (symbol.name != null)
      json.put("name", symbol.name);
    if (label != null)
      json.put("label", label);
    if (description != null)
      json.put("description", description);
    if (defaultValue != false)
      json.put("default_value", defaultValue);
    if (compute != null)
      json.put("compute", compute);
    return json;
  }
  
  public String toString(){
    return "[Boolean field symbol: " + symbol + " label: " + label +
        " description: '" + description + "'" + " default value: " + defaultValue +
        " compute: (" + compute + ")]";
  }
}
