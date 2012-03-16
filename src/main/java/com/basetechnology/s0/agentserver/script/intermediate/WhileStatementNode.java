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

public class WhileStatementNode extends StatementNode {
  public ExpressionNode condition;
  public StatementNode bodyStatement;
  
  public WhileStatementNode(ExpressionNode condition, StatementNode bodyStatement){
    this.condition = condition;
    this.bodyStatement = bodyStatement;
  }
  
  public void run(ScriptState scriptState) throws AgentServerException {
    scriptState.countNodeExecutions();
    while (condition.evaluateBooleanExpression(scriptState)){
      bodyStatement.run(scriptState);
      if (scriptState.controlFlowChange == ScriptState.controlFlowChanges.BREAK){
        scriptState.controlFlowChange = ScriptState.controlFlowChanges.NO_CHANGE;
        break;
      } else if (scriptState.controlFlowChange == ScriptState.controlFlowChanges.CONTINUE)
        scriptState.controlFlowChange = ScriptState.controlFlowChanges.NO_CHANGE;
      else if (scriptState.controlFlowChange == ScriptState.controlFlowChanges.RETURN)
        return;
    }
  }
}
