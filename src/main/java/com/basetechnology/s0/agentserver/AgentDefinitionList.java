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

package com.basetechnology.s0.agentserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AgentDefinitionList implements Iterable<AgentDefinition> {
  List<AgentDefinition> agentDefinitions = new ArrayList<AgentDefinition>();

  public void add(AgentDefinition agentDefinition){
    agentDefinitions.add(agentDefinition);
  }

  public void clear(){
    agentDefinitions.clear();
  }
  
  public boolean containsKey(String agentDefinitionName){
    // Scan all agent definitions of this user for the name
    for(AgentDefinition agentDefinition: this)
      if (agentDefinition.name.equals(agentDefinitionName))
        // Found agent definition with the specified name
        return true;
    
    // No matching agent with specified name for this user
    return false;
  }

  public AgentDefinition get(String agentDefinitionName){
    // Scan all agent definitions of this user for the name
    for(AgentDefinition agentDefinition: this)
      if (agentDefinition.name.equals(agentDefinitionName))
        // Found an instance with the specified name
        return agentDefinition;
    
    // No matching agent with specified name for this user
    return null;
  }

  public AgentDefinition get(int index){
    return agentDefinitions.get(index);
  }

  public AgentDefinition put(AgentDefinition agentDefinition) throws AgentServerException {
    // No-op if agent definition is already on the list
    if (! agentDefinitions.contains(agentDefinition)){
      // Add the new instance to the list
      agentDefinitions.add(agentDefinition);
    }
    
    // Return the agent definition
    return agentDefinition;
  }
  
  public void remove(String agentDefinitionName){
    AgentDefinition agentDefinition = get(agentDefinitionName);
    if (agentDefinition != null)
      remove(agentDefinition);
  }
  
  public void remove(AgentDefinition agentDefinition){
    if (agentDefinition != null)
      agentDefinitions.remove(agentDefinition);
  }
  
  public Iterator<AgentDefinition> iterator(){
    return agentDefinitions.iterator();
  }
  
  public int size(){
    return agentDefinitions.size();
  }
}
