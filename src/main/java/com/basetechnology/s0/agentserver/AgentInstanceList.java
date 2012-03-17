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

import org.json.JSONException;

import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;

public class AgentInstanceList implements Iterable<AgentInstance> {
  List<AgentInstance> agentInstances = new ArrayList<AgentInstance>();

  public void add(AgentInstance agentInstance){
    agentInstances.add(agentInstance);
  }

  public boolean containsKey(String agentInstanceName){
    // Scan all agent instances of this user for the name
    for(AgentInstance agentInstance: this)
      if (agentInstance.name.equals(agentInstanceName))
        // Found an instance with the specified name
        return true;
    
    // No matching instance for specified name
    return false;
  }

  public AgentInstance get(String agentInstanceName){
    // Scan all agent instances of this user for the name
    for(AgentInstance agentInstance: this)
      if (agentInstance.name.equals(agentInstanceName))
        // Found an instance with the specified name
        return agentInstance;
    
    // No matching instance for specified name
    return null;
  }

  public AgentInstance get(int index){
    return agentInstances.get(index);
  }

  public AgentInstance getByDefinitionName(String agentDefinitionName){
    // Scan all agent instances of this user for an instance of the named definition
    // TODO: Handle multiples
    for(AgentInstance agentInstance: this)
      if (agentInstance.agentDefinition.name.equals(agentDefinitionName))
        // Found an instance with the specified agent definition name
        return agentInstance;
    
    // No matching instance for specified name
    return null;
  }
  
  public AgentInstance getAgentInstance(User user, AgentDefinition agentDefinition, SymbolValues parameters) throws RuntimeException, SymbolException, AgentServerException, JSONException, TokenizerException, ParserException {
    return getAgentInstance(user, agentDefinition, parameters, false);
  }
  
  public AgentInstance getAgentInstance(User user, AgentDefinition agentDefinition, SymbolValues parameters, boolean create) throws RuntimeException, SymbolException, AgentServerException, JSONException, TokenizerException, ParserException {
    // See if we already have an instance to this definition and parameter values on the list
    for(AgentInstance agentInstance: this)
      // Check for match based on agent definition and parameters
      if (agentInstance.equals(agentDefinition, parameters))
        // Found match. Return the existing agent Instance
        return agentInstance;

    // No match
    if (create){
      AgentInstance newAgentInstance = new AgentInstance(user, agentDefinition, parameters);

      // Add the new instance to the list
      agentInstances.add(newAgentInstance);

      // Return the new instance
      return newAgentInstance;
    } else
      // Without the create option, return nothing
      return null;
  }

  public AgentInstance put(User user, AgentDefinition agentDefinition, String agentInstanceName, String agentDescription, SymbolValues parameterValues, String triggerIntervalExpression, String reportingIntervalExpression, boolean enabled, long timeCreated, long timeModified) throws AgentServerException, SymbolException, JSONException, TokenizerException, ParserException {
    // Make sure no existing instance with that name for the user
    for (AgentInstance agentInstance: agentInstances)
      if (agentInstance.name.equals(agentInstanceName))
        throw new AgentServerException("Instance already exists with name '" + agentInstanceName + "' for user '" + user.id + "' - existing instance is for definition named '" + agentInstance.agentDefinition.name + "'; new instance is for definition named '" + agentDefinition.name + "'");

    // Create new instance
    AgentInstance agentInstance = new AgentInstance(user, agentDefinition, agentInstanceName, agentDescription, parameterValues, triggerIntervalExpression, reportingIntervalExpression, enabled, timeCreated, timeModified, null, false);
    put(agentInstance);
    return agentInstance;
  }

  public AgentInstance put(AgentInstance agentInstance) throws AgentServerException {
    // No-op if agent instance is already on the list
    if (! agentInstances.contains(agentInstance)){
      // Add the new instance to the list
      agentInstances.add(agentInstance);
    }
    
    // Return the instance
    return agentInstance;
  }
  
  public Iterator<AgentInstance> iterator(){
    return agentInstances.iterator();
  }
  
  public void remove(String agentInstanceName){
    AgentInstance agentInstance = get(agentInstanceName);
    if (agentInstance != null)
      remove(agentInstance);
  }
  
  public void remove(AgentInstance agentInstance){
    if (agentInstance != null)
      agentInstances.remove(agentInstance);
  }
  
  public int size(){
    return agentInstances.size();
  }
}
