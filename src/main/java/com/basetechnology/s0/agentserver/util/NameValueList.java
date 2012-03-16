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

package com.basetechnology.s0.agentserver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.basetechnology.s0.agentserver.AgentServerException;

public class NameValueList<T> implements Iterable<NameValue<T>> {
  public List<NameValue<T>> nameValueList = new ArrayList<NameValue<T>>();
  public Map<String, NameValue<T>> nameValueMap = new HashMap<String, NameValue<T>>();

  public NameValueList(){
    // Nothing needed
  }
  
  public NameValueList(List<NameValue<T>> nameValueList){
    // Make a copy of the string list and build a map for lookup by name
    for (NameValue<T> nameValue: nameValueList){
      // Copy this string
      NameValue<T> nameValueCopy = nameValue.clone();
      
      // Add this string to the new string list
      nameValueList.add(nameValueCopy);
      
      // Add this string to map for lookup by its name
      // TODO: Symbol has name and SymbolTable (and hence SymbolValues)
      nameValueMap.put(nameValueCopy.name, nameValueCopy);
    }
  }

  public boolean containsKey(String stringName){
    return nameValueMap.containsKey(stringName);
  }

  public NameValue<T> add(String name, T value) throws AgentServerException {
    // Check if name is already in list
    if (containsKey(name))
      // Treat as update rather than add
      return put(name, value);
    else {
      // Add new name to the list
      NameValue<T> newNameValue = new NameValue<T>(name, value);
      nameValueList.add(newNameValue);

      // Update the map
      nameValueMap.put(name, newNameValue);
      
      // Return the new name-value
      return newNameValue;
    }
  }

  public void clear(){
    // Clear the list
    nameValueList.clear();
    
    // Clear the map
    nameValueMap.clear();
  }
  
  public NameValue<T> get(int i){
    return nameValueList.get(i);
  }

  public T get(String stringName){
    NameValue<T> nameValue = nameValueMap.get(stringName);
    if (nameValue == null)
      return null;
    else
      return nameValue.value;
  }

  public NameValue<T> put(String stringName, NameValue<T> value) throws AgentServerException {
    // Check if name is alread in list
    if (containsKey(stringName)){
      // Need to update list
      // Get current name/value pair
      NameValue<T> currentValue = nameValueMap.get(stringName);
      
      // Find current name/value pair in list
      int i = nameValueList.indexOf(currentValue);
      if (i < 0)
        throw new AgentServerException("Corrupted nameValueList for key: '" + stringName + "'");
      
      // Update the existing list value
      nameValueList.set(i, value);
    } else {
      // Need to add to list
      nameValueList.add(value);
    }
    return nameValueMap.put(stringName, value);
  }

  public NameValue<T> put(String stringName, T value) throws AgentServerException {
    return put(stringName, new NameValue<T>(stringName, value));
  }

  public NameValue<T> put(String stringName, String value) throws AgentServerException {
    return put(stringName, new NameValue<T>(stringName, (T)value));
  }

  public Iterator<NameValue<T>> iterator(){
    return nameValueList.iterator();
  }

  public NameValue<T> remove(String stringName){
    // Get the current name/value from the map
    NameValue<T> nameValue = nameValueMap.get(stringName);
    
    // It may not exist
    if (nameValue != null){
      // Remove the name/value from the list
      nameValueList.remove(nameValue);
      
      // Remove the value from the map
      return nameValueMap.remove(stringName);
    } else
      // Nothing to do for nonexistent value
      return null;
  }
  
  public int size(){
    return nameValueList.size();
  }

}
