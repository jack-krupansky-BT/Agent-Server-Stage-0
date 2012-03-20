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
import com.basetechnology.s0.agentserver.script.intermediate.StringTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class TextField extends Field {
  public String defaultValue;
  public int minLength;
  public int maxLength;
  public int nominalWidth;
  public int nominalHeight;

  public TextField(SymbolTable symbolTable, String name){
    this.symbol = new Symbol(symbolTable, name, IntegerTypeNode.one);
    this.label = name;
  }

  public TextField(SymbolTable symbolTable, String name, String text){
    this.symbol = new Symbol(symbolTable, name, IntegerTypeNode.one);
    this.label = name;
  }

  public TextField(SymbolTable symbolTable, String name, String label, String description,
      String defaultValue, int minLength, int maxLength, int nominalWidth, int nominalHeight,
      String compute){
    this.symbol = new Symbol(symbolTable, name, IntegerTypeNode.one);
    this.label = label;
    this.description = description;
    this.defaultValue = defaultValue;
    this.minLength = minLength;
    this.maxLength = maxLength;
    this.nominalWidth = nominalWidth;
    this.nominalHeight = nominalHeight;
    this.compute = compute;
  }

  public Field clone(){
    return new TextField(symbol.symbolTable, symbol.name, label, description, defaultValue, minLength,
        maxLength, nominalWidth, nominalHeight, compute);
  }

  public Object getDefaultValue(){
    return defaultValue;
  }

  public Value getDefaultValueNode(){
    return new StringValue(defaultValue);
  }

  public TypeNode getType(){
    return StringTypeNode.one;
  }

  public static Field fromJson(SymbolTable symbolTable, JSONObject fieldJson){
    String type = fieldJson.optString("type");
    if (type == null || ! type.equals("string"))
      return null;
    String name = fieldJson.has("name") ? fieldJson.optString("name") : null;
    String label = fieldJson.has("label") ? fieldJson.optString("label") : null;
    String description = fieldJson.has("description") ? fieldJson.optString("description") : null;
    String defaultValue = fieldJson.has("default_value") ? fieldJson.optString("default_value") : null;
    int minLength = fieldJson.has("min_length") ? fieldJson.optInt("min_length") : 0;
    int maxLength = fieldJson.has("max_length") ? fieldJson.optInt("max_length") : 0;
    int nominalWidth = fieldJson.has("nominal_width") ? fieldJson.optInt("nominal_width") : 0;
    int nominalHeight = fieldJson.has("nominal_height") ? fieldJson.optInt("nominal_height") : 0;
    String compute = fieldJson.has("compute") ? fieldJson.optString("compute") : null;
    return new TextField(symbolTable, name, label, description, defaultValue, minLength, maxLength,
        nominalWidth, nominalHeight, compute);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("type", "text");
    if (symbol.name != null)
      json.put("name", symbol.name);
    if (label != null)
      json.put("label", label);
    if (description != null)
      json.put("description", description);
    if (defaultValue != null)
      json.put("default_value", defaultValue);
    if (minLength != 0)
      json.put("min_length", minLength);
    if (maxLength != 0)
      json.put("max_length", maxLength);
    if (nominalWidth != 0)
      json.put("nominal_width", nominalWidth);
    if (nominalHeight != 0)
      json.put("nominal_height", nominalHeight);
    if (compute != null)
      json.put("compute", compute);
    return json;
  }
  
  public String toString(){
    return "[Text field symbol: " + symbol + " label: " + label +
        " description: '" + description + "'" + " default value: " + defaultValue +
        " min length: " + minLength + " max length: " + maxLength +
        " nominal width: " + nominalWidth + " nominal height: " + nominalHeight +
        " compute: (" + compute + ")" +
        "]";
  }
}
