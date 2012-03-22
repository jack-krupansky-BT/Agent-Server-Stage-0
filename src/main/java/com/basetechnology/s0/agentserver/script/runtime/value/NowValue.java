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

import java.util.List;

import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;

public class NowValue extends Value {

  static public NowValue one = new NowValue();

  public Value clone(){
    return this;
  }

  public Value getDefaultValue(){
    return new IntegerValue(System.currentTimeMillis());
  }

  public Object getValue(){
    return new IntegerValue(System.currentTimeMillis());
  }

  public boolean getBooleanValue(){
    return true;
  }

  public long getLongValue(){
    return System.currentTimeMillis();
  }

  public double getDoubleValue(){
    return getLongValue();
  }

  public String getStringValue(){
    return Long.toString(getLongValue());
  }

  public Value evaluateExpression(ScriptState scriptState){
    return new IntegerValue(getLongValue());
  }

  public Value getNamedValue(ScriptState scriptState, String name) throws RuntimeException {
    return super.getNamedValue(scriptState, name);
  }

  public Value getMethodValue(ScriptState scriptState, String name, List<Value> arguments) throws RuntimeException {
      return super.getMethodValue(scriptState, name, arguments);
  }
  
  public String toString(){
    // TODO: Or should this be Iso/Rfc format for date/time
    return getStringValue();
  }

}
