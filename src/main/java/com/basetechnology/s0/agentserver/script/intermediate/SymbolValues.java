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

package com.basetechnology.s0.agentserver.script.intermediate;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.JsonUtils;
import com.basetechnology.s0.agentserver.util.ListMap;

public class SymbolValues implements Iterable<Symbol> {
  static final Logger log = Logger.getLogger(SymbolValues.class);
  public String categoryName;
  public ListMap<Symbol, Value> symbolValues = new ListMap<Symbol, Value>();

  public SymbolValues(){
    this(null);
  }

  public SymbolValues(String categoryName){
    this.categoryName = categoryName == null ? "" : categoryName;
  }
  
  public void clear(){
    symbolValues.clear();
  }

  public SymbolValues clone(){
    // Create an empty copy of this object
    SymbolValues copyValues = new SymbolValues(categoryName);
    
    // Copy all the key/values from current object to the new one, with deep copy of values
    for (Symbol symbol: symbolValues.keySet())
      copyValues.put(symbol, symbolValues.get(symbol).clone());
    
    // Return the new copy of this object
    return copyValues;
  }
  
  public boolean contains(Symbol symbol){
    return symbolValues.containsKey(symbol);
  }
  
  public boolean contains(String name){
    // Must linearly search for matching symbol name
    for (Symbol symbol: symbolValues.keySet())
      if (symbol.name.equals(name))
        // Found matching symbol name
        return true;
    
    // No match
    return false;
  }
  
  public Value get(Symbol symbol){
    if (symbolValues.containsKey(symbol))
      return symbolValues.get(symbol);
    else
      return NullValue.one;
  }
  
  public Value get(String name){
    // Must linearly search for matching symbol name
    for (Symbol symbol: symbolValues.keySet())
      if (symbol.name.equals(name))
        // Found matching symbol name. Return its value
        return symbolValues.get(symbol);
    
    // No match
    return NullValue.one;
  }
  
  public Iterator<Symbol> iterator(){
    return symbolValues.keySet().iterator();
  }
  
  public void put(Symbol symbol, Value valueNode){
    symbolValues.put(symbol, valueNode);
  }
  
  public Set<Symbol> keySet(){
    return symbolValues.keySet();
  }
  
  public int size(){
    return symbolValues.size();
  }

  public static SymbolValues fromJson(SymbolTable symbolTable, JSONObject valuesJson) throws RuntimeException, SymbolException {
    SymbolValues symbolValues = new SymbolValues(symbolTable == null ? null : symbolTable.categoryName);
    
    if (valuesJson != null)
      for (Iterator<String> it = valuesJson.keys(); it.hasNext(); ){
        String key = it.next();
        Value valueNode = JsonUtils.convertJsonValue(valuesJson.opt(key));
        symbolValues.put(symbolTable.put(key), valueNode);
      }

    return symbolValues;
  }

  public JSONObject toJson() throws AgentServerException {
    JSONObject valueJson = new JsonListMap();
    for (Symbol symbol: symbolValues.keySet()){
      try {
        valueJson.put(symbol.name, get(symbol).toJsonObject());
      } catch (JSONException e){
        log.info("Unable to output SymbolValues as JSON - " + e.getMessage());
        e.printStackTrace();
      }
    }
    return valueJson;
  }
  
  public String toString(){
    return categoryName + ": " + symbolValues.toString();
  }

  public boolean equals(SymbolValues otherSymbolValues){
    if (otherSymbolValues == null)
      return false;

    // Category name must agree
    if (! categoryName.equals(otherSymbolValues.categoryName))
      return false;

    // Sizes must agree
    int len1 = symbolValues.size();
    int len2 = otherSymbolValues.symbolValues.size();
    if (len1 != len2)
      return false;

    // And values for each key must match recursively
    // Note: Can't do symbol match or lookup since they may use different symbol tables
    for (Symbol symbol: symbolValues.keySet()){
      String symbolName = symbol.name;
      boolean match = false;
      for (Symbol otherSymbol: otherSymbolValues.symbolValues.keySet())
        if (symbolName.equals(otherSymbol.name)){
          // Found match. compare the two values
          Value valueNode = symbolValues.get(symbol);
          Value otherValueNode = otherSymbolValues.get(otherSymbol); 
          if (valueNode.equals(otherValueNode)){
            match = true;
            break;
          } else {
            //log.info(valueNode.toString() + "  NOT EQUALS  " + otherValueNode.toString());
            return false;
          }
        }
      if (! match)
        return false;
    }

    // Everything matches
    return true;
  }
}
