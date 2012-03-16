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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FieldList implements Iterable<Field> {
  public List<Field> fieldList = new ArrayList<Field>();
  public Map<String, Field> fieldMap = new HashMap<String, Field>();

  public FieldList(){
    // Nothing needed
  }
  
  public FieldList(List<Field> fieldList){
    // Make a copy of the field list and build a map for lookup by name
    for (Field field: fieldList){
      // Copy this field
      Field fieldCopy = field.clone();
      
      // Add this field to the new field list
      fieldList.add(fieldCopy);
      
      // Add this field to map for lookup by its name
      // TODO: Symbol has name and SymbolTable (and hence SymbolValues)
      fieldMap.put(fieldCopy.symbol.name, fieldCopy);
    }
  }
  
  public void add(Field field){
    fieldList.add(field);
    fieldMap.put(field.symbol.name, field);
  }
  
  public boolean containsKey(String fieldName){
    return fieldMap.containsKey(fieldName);
  }
  
  public Field get(int i){
    return fieldList.get(i);
  }

  public Field get(String fieldName){
    return fieldMap.get(fieldName);
  }

  public Iterator<Field> iterator(){
    return fieldList.iterator();
  }
  
  public int size(){
    return fieldList.size();
  }
}
