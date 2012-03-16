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

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;

public class ForStatementNode extends StatementNode {
  public StatementNode initialExpression;
  public ExpressionNode conditionExpression;
  public ExpressionNode incrementExpression;
  public StatementNode bodyStatement;
  
  public ForStatementNode(StatementNode initialExpression,
      ExpressionNode conditionExpression,
      ExpressionNode incrementExpression,
      StatementNode bodyStatement){
    this.initialExpression = initialExpression;
    this.conditionExpression = conditionExpression;
    this.incrementExpression = incrementExpression;
    this.bodyStatement = bodyStatement;
  }
  
  public void run(ScriptState scriptState) throws AgentServerException {
    scriptState.countNodeExecutions();
    // Evaluate the initial expression
    initialExpression.run(scriptState);
    
    // Repeatedly run the body until the condition expression is false
    while ((conditionExpression instanceof NullExpressionNode) || conditionExpression.evaluateBooleanExpression(scriptState)) {
      // Run the loop body once
      bodyStatement.run(scriptState);
      
      // Code may want to prematurely end the loop
      if (scriptState.controlFlowChange == ScriptState.controlFlowChanges.RETURN)
        break;
      else if (scriptState.controlFlowChange == ScriptState.controlFlowChanges.BREAK){
        scriptState.controlFlowChange = ScriptState.controlFlowChanges.NO_CHANGE;
        break;
      }

      // Reset control flow state if code merely wanted to immediately continue the loop
      if (scriptState.controlFlowChange == ScriptState.controlFlowChanges.CONTINUE)
        scriptState.controlFlowChange = ScriptState.controlFlowChanges.NO_CHANGE;
      
      // Evaluate the increment expression
      incrementExpression.evaluateExpression(scriptState);
    }
  }

}
