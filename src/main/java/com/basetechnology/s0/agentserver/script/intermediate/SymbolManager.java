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

public class SymbolManager {
  public Map<String, SymbolTable> symbolTables = new HashMap<String, SymbolTable>();

  public void clear(){
    symbolTables.clear();
  }
  
  public Symbol get(String name) throws SymbolException {
    return get(null, name);
  }
  
  public Symbol get(String categoryName, String name) throws SymbolException {
    return get(categoryName, name, true);
  }
  
  public Symbol get(String categoryName, String name, boolean optional) throws SymbolException {
    if (categoryName == null){
      // Lookup symbol in all categories
      // TODO: Categories need to be ordered for nested blocks to work
      String catNames = "";
      int numCatNames = 0;
      Symbol symbol = null;
      for (String catName: symbolTables.keySet()){
        SymbolTable symbolTable = symbolTables.get(catName);
        if (symbolTable != null && symbolTable.contains(name)){
          // Keep track of where we found the symbol
          symbol = symbolTable.get(name);
          
          // Keep track of all categories in which we found the symbol
          catNames += (catNames.length() > 0 ? ", " : "") + catName;
          numCatNames++;
        }
      }
      if (symbol != null){
        if (numCatNames == 1)
          return symbol;
        else 
          throw new SymbolException("Symbol '" + name + "' is defined in multiple categories: " + catNames);
      } else if (! optional)
        throw new SymbolException("No definition for symbol '" + name + "' for any category");
      else
        return null;
    } else {
      // Lookup the symbol in the specified category
      SymbolTable symbolTable = symbolTables.get(categoryName);
      if (symbolTable == null)
        throw new SymbolException("No symbol map for category name '" + categoryName + "'");
      else if (symbolTable.contains(name))
        return symbolTable.get(name);
      else if (! optional)
        throw new SymbolException("No definition for symbol '" + name + "' for category name '" + categoryName + "'");
      else
        return null;
    }
  }

  public Symbol put(String name) throws SymbolException {
    return put("default", name, new ObjectTypeNode());
  }
  
  public Symbol put(String categoryName, String name) throws SymbolException {
    return put(categoryName, name, new ObjectTypeNode());
  }
  
  public Symbol put(String categoryName, String name, TypeNode type) throws SymbolException {
    // Get the symbol map for specified category
    SymbolTable symbolTable = symbolTables.get(categoryName);
    if (symbolTable == null){
      // No symbol map for this category, so create one
      symbolTable = new SymbolTable(categoryName);
      symbolTables.put(categoryName, symbolTable);
    }
    
    // See if symbol already exists
    Symbol symbol = symbolTable.get(name);
    if (symbol == null){
      // Symbol does not yet exist, so create it
      symbol = symbolTable.put(name, type);
    } else if (symbol.type != type)
      // If symbol already exists, may need to update the type
      symbol.type = type;
    
    return symbol;
  }

  public void removeCategory(String categoryName){
    if (symbolTables.containsKey(categoryName))
      symbolTables.remove(categoryName);
  }
  
  public boolean isCategory(String categoryName){
    return categoryName == null ? false : symbolTables.containsKey(categoryName);
  }

  public SymbolTable getSymbolTable(String categoryName){
    if (! symbolTables.containsKey(categoryName))
      symbolTables.put(categoryName, new SymbolTable(categoryName));
    return symbolTables.get(categoryName);
  }

  public String toString(){
    StringBuilder sb = new StringBuilder("[SymbolManager ");
    boolean first = true;
    for (String symbolTableName: symbolTables.keySet()){
      if (! first){
        sb.append(", ");
        first = false;
      }
      sb.append(symbolTables.get(symbolTableName).toString());
    }
    sb.append(']');
    return sb.toString();
  }
}
