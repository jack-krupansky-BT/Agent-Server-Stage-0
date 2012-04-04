package com.basetechnology.s0.agentserver.field;

import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.script.intermediate.ListTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.MapTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.value.ListValue;
import com.basetechnology.s0.agentserver.script.runtime.value.MapValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class ListField extends Field {
  public TypeNode elementType;
  
  public ListField(SymbolTable symbolTable, String name){
    this.symbol = new Symbol(symbolTable, name, MapTypeNode.one);
    this.label = name;
  }

  public ListField(SymbolTable symbolTable, String name, String label){
    this.symbol = new Symbol(symbolTable, name, MapTypeNode.one);
    this.label = label;
  }

  public ListField(SymbolTable symbolTable, String name, String label, String description,
      TypeNode elementType, String compute){
    this.symbol = new Symbol(symbolTable, name, ListTypeNode.one);
    this.label = label;
    this.description = description;
    this.elementType = elementType;
    this.compute = compute;
  }

  public Field clone(){
    return new ListField(symbol.symbolTable, symbol.name, label, description, elementType, compute);
  }

  public Object getDefaultValue(){
    return null;
  }

  public Value getDefaultValueNode(){
    return new ListValue();
  }

  public TypeNode getType(){
    return ListTypeNode.one;
  }

  public static Field fromJson(SymbolTable symbolTable, JSONObject fieldJson) throws AgentServerException {
    String type = fieldJson.optString("type");
    if (type == null || ! type.equals("list"))
      return null;
    String name = fieldJson.has("name") ? fieldJson.optString("name") : null;
    String label = fieldJson.has("label") ? fieldJson.optString("label") : null;
    String description = fieldJson.has("description") ? fieldJson.optString("description") : null;
    String compute = fieldJson.has("compute") ? fieldJson.optString("compute") : null;
    String elementTypeString = fieldJson.has("element_type") ? fieldJson.optString("element_type") : null;
    TypeNode elementType = Field.getType(elementTypeString);
    return new ListField(symbolTable, name, label, description, elementType, compute);
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
    if (elementType != null)
      json.put("element_type", elementType.toString());
    if (compute != null)
      json.put("compute", compute);
    return json;
  }
  
  public String toString(){
    return "[List field symbol: " + symbol + " label: " + label +
        " description: '" + description + "'" + " compute: (" + compute + ")" +
        "]";
  }

}
