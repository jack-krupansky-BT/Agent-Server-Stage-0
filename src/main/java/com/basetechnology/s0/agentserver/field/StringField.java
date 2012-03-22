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

import com.basetechnology.s0.agentserver.script.intermediate.StringTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class StringField extends Field {
  public String defaultValue;
  public int minLength;
  public int maxLength;
  public int nominalWidth;
  public String validRegex;

  public StringField(SymbolTable symbolTable, String name){
    this.symbol = new Symbol(symbolTable, name, StringTypeNode.one);
    this.label = name;
    this.defaultValue = "";
  }

  public StringField(SymbolTable symbolTable, String name, String label){
    this.symbol = new Symbol(symbolTable, name, StringTypeNode.one);
    this.label = label;
    this.defaultValue = "";
  }

  public StringField(SymbolTable symbolTable, String name, String label, String description, String defaultValue, int minLength, int maxLength, int nominalWidth, String validRegex, String compute){
    this.symbol = new Symbol(symbolTable, name, StringTypeNode.one);
    this.label = label;
    this.description = description;
    this.defaultValue = defaultValue == null ? "" : defaultValue;
    this.minLength = minLength;
    this.maxLength = maxLength;
    this.nominalWidth = nominalWidth;
    this.validRegex = validRegex;
    this.compute = compute;
  }

  public Field clone(){
    return new StringField(symbol.symbolTable, symbol.name, label, description, defaultValue, minLength, maxLength, nominalWidth, validRegex, compute);
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
    String type = fieldJson.optString("type").toLowerCase();
    if (type == null || ! type.equals("string"))
      return null;
    String name = fieldJson.has("name") ? fieldJson.optString("name") : null;
    String label = fieldJson.has("label") ? fieldJson.optString("label") : null;
    String description = fieldJson.has("description") ? fieldJson.optString("description") : null;
    String defaultValue = fieldJson.has("default_value") ? fieldJson.optString("default_value") : null;
    int minLength = fieldJson.has("min_length") ? fieldJson.optInt("min_length") : 0;
    int maxLength = fieldJson.has("max_length") ? fieldJson.optInt("max_length") : 0;
    int nominalWidth = fieldJson.has("nominal_width") ? fieldJson.optInt("nominal_width") : 0;
    String validRegex = fieldJson.has("valid_regex") ? fieldJson.optString("valid_regex") : null;
    String compute = fieldJson.has("compute") ? fieldJson.optString("compute") : null;
    return new StringField(symbolTable, name, label, description, defaultValue, minLength, maxLength, nominalWidth, validRegex, compute);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("type", "string");
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
      json.put("nominal_length", nominalWidth);
    if (validRegex != null)
      json.put("valid_regex", validRegex);
    if (compute != null)
      json.put("compute", compute);
    return json;
  }
  
  public String toString(){
    return "[String field symbol: " + symbol + " label: " + label +
        " description: '" + description + "'" + " default value: " + defaultValue +
        " min length: " + minLength + " max length: " + maxLength +
        " nominal width: " + nominalWidth + " regex: '" + validRegex + "'" +
        " compute: (" + compute + ")" +
        "]";
  }
}
