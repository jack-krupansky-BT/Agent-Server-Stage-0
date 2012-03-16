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
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

class HelpField extends Field {
  public String help;

  HelpField(SymbolTable symbolTable, String name, String help){
    this.symbol = new Symbol(symbolTable, name, IntegerTypeNode.one);
    this.help = help;
  }

  public Field clone(){
    return new HelpField(symbol.symbolTable, symbol.name, help);
  }

  public Object getDefaultValue(){
    return null;
  }

  public Value getDefaultValueNode(){
    return NullValue.one;
  }

  public TypeNode getType(){
    return ObjectTypeNode.one;
  }

  public static Field fromJson(SymbolTable symbolTable, JSONObject fieldJson){
    String type = fieldJson.optString("type");
    if (type == null || ! type.equals("string"))
      return null;
    String name = fieldJson.has("name") ? fieldJson.optString("name") : null;
    String help = fieldJson.has("help") ? fieldJson.optString("help") : null;
    return new HelpField(symbolTable, name, help);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("type", "help");
    if (symbol.name != null)
      json.put("name", symbol.name);
    if (help != null)
      json.put("help", help);
    return json;
  }
  
  public String toString(){
    return "[Text field symbol: " + symbol + " label: " + label +
        " description: '" + description + "'" + " help text: '" + help + "'" +
        "]";
  }
}
