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

package com.basetechnology.s0.agentserver.script.runtime.value;


public class TrueValue extends BooleanValue {

  static public TrueValue one = new TrueValue();
  
  public Object getValue(){
    return true;
  }

  public boolean getBooleanValue(){
    return true;
  }

  public long getLongValue(){
    return 1;
  }

  public double getDoubleValue(){
    return 1.0;
  }

  public String getStringValue(){
    return Boolean.toString(true);
  }

  public String toString(){
    return "true";
  }

}
