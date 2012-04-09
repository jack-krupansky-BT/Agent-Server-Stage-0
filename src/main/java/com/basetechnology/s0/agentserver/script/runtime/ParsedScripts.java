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

package com.basetechnology.s0.agentserver.script.runtime;

import java.util.ArrayList;
import java.util.List;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.script.intermediate.ScriptNode;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;

public class ParsedScripts {
  public List<ScriptNode> parsedScripts;

  public ParsedScripts(){
    this.parsedScripts = new ArrayList<ScriptNode>();
  }
  
  public void add(ScriptNode scriptNode){
    parsedScripts.add(scriptNode);
  }
  
  public void clear(){
    parsedScripts.clear();
  }
  
  public ScriptNode get(String functionName, List<TypeNode> argumentTypes){
    for (ScriptNode scriptNode: parsedScripts){
      if (scriptNode.functionName.equals(functionName)){
        // Found matching function name. Now check for parameter types
        if (scriptNode.checkArgumentTypes(argumentTypes))
          // Found matching function name and parameters types
          return scriptNode;
      }
    }
    
    // Named script not found
    return null;
    //throw new AgentServerException("No script named '" + functionName + "' with paramter types " + argumentTypes.toString());
  }
  
  public int size(){
    return parsedScripts.size();
  }
}
