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

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;

public class BlockStatementNode extends StatementNode {
  public List<Symbol> localVariables;
  public List<StatementNode> statementSequence;
  
  public BlockStatementNode(List<Symbol> localVariables, List<StatementNode> statementSequence){
    this.localVariables = localVariables;
    this.statementSequence = statementSequence;
  }
  
  public void run(ScriptState scriptState) throws AgentServerException {
    scriptState.countNodeExecutions();
    // Initialize all local variables
    for (Symbol localVariable: localVariables){
      // Get the category symbol value map for this local variable
      String categoryName = localVariable.symbolTable.categoryName;
      SymbolValues symbolValues = scriptState.categorySymbolValues.get(categoryName);
      if (symbolValues == null){
        // No values in this category yet, so create it
        symbolValues = new SymbolValues(categoryName);
        scriptState.categorySymbolValues.put(categoryName, symbolValues);
      }
      
      // Initialize the variable
      symbolValues.put(localVariable, localVariable.type.getDefaultValue());
    }
    
    for (StatementNode statementNode: statementSequence){
      // Execute the next statement
      statementNode.run(scriptState);
      
      // Time to return or break or continue?
      switch (scriptState.controlFlowChange){
      case RETURN:
      case BREAK:
      case CONTINUE:
        // TODO: Should cleanup local variables on exit from block
        return;
      }
    }
  }

}
