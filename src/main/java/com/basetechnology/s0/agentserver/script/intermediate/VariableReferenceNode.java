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

import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.runtime.NodeExecutionLimitException;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class VariableReferenceNode extends ReferenceNode {
  public Symbol symbol;
  
  public VariableReferenceNode(Symbol symbol){
    this.symbol = symbol;
  }

  public Value getValue(ScriptState scriptState){
    Value valueNode = scriptState.categorySymbolValues.get(symbol.symbolTable.categoryName).get(symbol);
    return valueNode == null ? symbol.type.getDefaultValue() : valueNode;
  }

  public Value evaluateExpression(ScriptState scriptState) throws NodeExecutionLimitException{
    scriptState.countNodeExecutions();
    Value valueNode = getValue(scriptState);
    if (valueNode == null)
      return NullValue.one;
    else
      return valueNode;
  }
  
  public boolean getBooleanValue(ScriptState scriptState){
  Object object = getValue(scriptState);
  if (object == null)
    return false;
  else if (object instanceof Boolean)
    return (Boolean)object;
  else if (object instanceof Integer)
    return (Integer)object != 0;
  else if (object instanceof Long)
    return (Long)object != 0;
  else if (object instanceof Float)
    return (Float)object != 0.0;
  else if (object instanceof Double)
    return (Double)object != 0.0;
  else if (object instanceof String)
    return ((String)object).length() > 0;
  else
    return false;
  }

  public long getLongValue(ScriptState scriptState){
  Object object = getValue(scriptState);
  if (object == null)
    return 0;
  else if (object instanceof Boolean)
    return (Boolean)object ? 1 : 0;
  else if (object instanceof Integer)
    return (Integer)object;
  else if (object instanceof Long)
    return (Long)object;
  else if (object instanceof Float)
    return Float.floatToIntBits((Float)object);
  else if (object instanceof Double)
    return Double.doubleToLongBits((Double)object);
  else if (object instanceof String)
    return Long.parseLong((String)object);
  else
    return 0;
  }

  public double getDoubleValue(ScriptState scriptState){
  Object object = getValue(scriptState);
  if (object == null)
    return 0.0;
  else if (object instanceof Boolean)
    return (Boolean)object ? 1.0 : 0.0;
  else if (object instanceof Integer)
    return (Double)object;
  else if (object instanceof Long)
    return (Double)object;
  else if (object instanceof Float)
    return (Double)object;
  else if (object instanceof Double)
    return (Double)object;
  else if (object instanceof String)
    return Double.parseDouble((String)object);
  else
    return 0;
  }

  public String getStringValue(ScriptState scriptState){
  Object object = getValue(scriptState);
  if (object == null)
    return null;
  else if (object instanceof Boolean)
    return Boolean.toString((Boolean)object);
  else if (object instanceof Integer)
    return Integer.toString((Integer)object);
  else if (object instanceof Long)
    return Long.toString((Long)object);
  else if (object instanceof Float)
    return Float.toString((Float)object);
  else if (object instanceof String)
    return (String)object;
  else
    return null;
  }

  public Value putValue(ScriptState scriptState, Value valueNode) throws RuntimeException {
    // Note: For strings only, value must be copied to avoid sharing between variables
    Value copyNode = valueNode.copyOnAssignment();
    
    // Make sure we have a category symbol value table
    // Get the category symbol value map for this local variable
    String categoryName = symbol.symbolTable.categoryName;
    SymbolValues symbolValues = scriptState.categorySymbolValues.get(categoryName);
    if (symbolValues == null){
      // No values in this category yet, so create it
      symbolValues = new SymbolValues(categoryName);
      scriptState.categorySymbolValues.put(categoryName, symbolValues);
    }

    symbolValues.put(symbol, copyNode);
    return copyNode;
  }
}
