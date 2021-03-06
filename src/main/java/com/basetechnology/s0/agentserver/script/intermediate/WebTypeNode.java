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

import java.util.List;

import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.script.runtime.value.WebValue;

public class WebTypeNode extends TypeNode {
  public static WebTypeNode one = new WebTypeNode();

  public WebTypeNode(){
    
  }
  
  public Value create(List<Value> argumentValues){
    return new WebValue();
  }
  
  public Value getDefaultValue(){
    return new WebValue();
  }
  
  public String toString(){
    return "web";
  }

  public boolean isCompatibleType(TypeNode other){
    return other instanceof WebTypeNode || other.getClass() == ObjectTypeNode.class;
  }

}
