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

public class Symbol implements Comparable<Symbol> {
  public SymbolTable symbolTable;
  public String name;
  public TypeNode type;

  public Symbol(SymbolTable symbolTable, String name, TypeNode type){
    this.symbolTable = symbolTable;
    this.name = name;
    this.type = type;
  }
  
  public int compareTo (Symbol otherSymbol){
    int c1 = symbolTable.categoryName.compareTo(otherSymbol.symbolTable.categoryName);
    if (c1 != 0)
      return c1;
    return name.compareTo(otherSymbol.name);
  }
  
  public String toString(){
    return type.toString() + " " + symbolTable.categoryName + "." + name;
  }
  
  public String toStringNoCategory(){
    return type.toString() + " " + name;
  }
}
