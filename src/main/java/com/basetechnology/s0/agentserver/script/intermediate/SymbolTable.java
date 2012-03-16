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

import java.util.HashMap;
import java.util.Map;

import com.basetechnology.s0.agentserver.util.ListMap;

public class SymbolTable {

  public String categoryName;
  public ListMap<String, Symbol> symbolNames = new ListMap<String, Symbol>();

  public SymbolTable(String categoryName){
    this.categoryName = categoryName;
  }

  public SymbolTable(String categoryName, SymbolValues symbolValues){
    this.categoryName = categoryName;
  }

  public void clear(){
    clearNames();
    clearValues();
  }

  public void clearNames(){
    symbolNames.clear();
  }

  public void clearValues(){
    symbolNames.clear();
  }
  
  public boolean contains(String name){
    return symbolNames.containsKey(name);
  }
  
  public Symbol get(String name) throws SymbolException {
    return get(name, true);
  }

  public Symbol get(String name, boolean optional) throws SymbolException {
    if (symbolNames == null)
      throw new SymbolException("No symbol map for category name '" + categoryName + "'");
    else if (symbolNames.containsKey(name))
      return symbolNames.get(name);
    else if (! optional)
      throw new SymbolException("No definition for symbol '" + name + "' for category name '" + categoryName + "'");
    else
      return null;
  }

  public Symbol put(String name){
    return put(name, new ObjectTypeNode());
  }
  
  public Symbol put(String name, TypeNode type){
    // See if symbol already exists
    Symbol symbol = symbolNames.get(name);
    if (symbol == null){
      // Symbol does not yet exist, so create it
      symbol = new Symbol(this, name, type);
      symbolNames.put(name, symbol);
    } else if (symbol.type != type)
      // If symbol already exists, may need to update the type
      symbol.type = type;
    
    return symbol;
  }
  

  public String toString(){
    return "SymbolTable " + categoryName + ": " + symbolNames.toString();
  }
}
