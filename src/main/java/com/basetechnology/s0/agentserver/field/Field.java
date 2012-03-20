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

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public abstract class Field {
  public Symbol symbol;
  public String label;
  public String description;
  public static String[] types = {"string", "int", "float", "money", "date", "location",
    "text", "help", "option", "choice", "multi_choice"};
  public String compute;

  public abstract Field clone();

  public abstract Object getDefaultValue();

  public abstract Value getDefaultValueNode();

  public abstract TypeNode getType();
  
  public abstract JSONObject toJson() throws JSONException;

  public static Field fromJsonx(SymbolTable symbolTable, JSONObject fieldJson) throws AgentServerException {
    String type = fieldJson.optString("type").toLowerCase();
    if (type == null)
      throw new AgentServerException("'type' is missing from field definition");
    else if (type.trim().length() == 0)
      throw new AgentServerException("'type' is empty in field definition");
    else if (type.equals("string"))
      return StringField.fromJson(symbolTable, fieldJson);
    else if (type.equals("int") || type.equals("integer"))
      return IntField.fromJson(symbolTable, fieldJson);
    else if (type.equals("float"))
      return FloatField.fromJson(symbolTable, fieldJson);
    else if (type.equals("money"))
      return MoneyField.fromJson(symbolTable, fieldJson);
    else if (type.equals("date"))
      return DateField.fromJson(symbolTable, fieldJson);
    else if (type.equals("location"))
      return LocationField.fromJson(symbolTable, fieldJson);
    else if (type.equals("text"))
      return TextField.fromJson(symbolTable, fieldJson);
    else if (type.equals("help"))
      return HelpField.fromJson(symbolTable, fieldJson);
    else if (type.equals("option") || type.equals("boolean"))
      return BooleanField.fromJson(symbolTable, fieldJson);
    else if (type.equals("choice"))
      return ChoiceField.fromJson(symbolTable, fieldJson);
    else if (type.equals("multi_choice"))
      return MultiChoiceField.fromJson(symbolTable, fieldJson);
    else
      throw new AgentServerException("Invalid type ('" + type + "') in field definition");
  }
  
  public String toJsonString() throws JSONException {
    JSONObject json = toJson();
    return json.toString(4);
  }
  
  public String toString(){
    return "[" + this.getClass().getSimpleName() + " field symbol: " + symbol + " label: " + label + " description: '" + description + "' compute: (" + compute + ")]";
  }
}
