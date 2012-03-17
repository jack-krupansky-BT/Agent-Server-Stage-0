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

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class AgentActivityCondition extends AgentActivity {
  static final Logger log = Logger.getLogger(AgentActivityCondition.class);

  public AgentCondition condition;
  
  public AgentActivityCondition(AgentInstance agent, AgentCondition condition) throws RuntimeException {
    super(agent, System.currentTimeMillis() + condition.interval, "AgentCondition " + condition.toString());
    this.condition = condition;
  }
  
  public boolean performActivity() throws SymbolException, RuntimeException, AgentServerException, JSONException {
    startActivity();

    // Get current condition status
    AgentConditionStatus status = agent.conditionStatus.get(condition.name);

    // If condition is now disabled, ignore this lingering hit
    if (status.enabled){
      // Remember time of that we checked the trigger condition
      long now = System.currentTimeMillis();
      status.checkTime = now;
      
      // Count check interval hits
      status.checkHits++;

      // Evaluate the condition's condition expression
      try {
        // Run the condition's script
        Value conditionValue = agent.evaluateExpression(condition.condition);
        log.info("Condition \"" + condition.condition + "\" = " + conditionValue.toString());

        // Record the expression value
        status.conditionValue = conditionValue.getBooleanValue();
      } catch (AgentServerException e){
        // TODO: Record an exception status for the condition misfire
        gotException(e);
        return false;
      }

      // This is only a hit if condition expression evaluates to true
      if (status.conditionValue){
        // Count condition hits
        status.hits++;

        // Record time of condition hit
        status.time = now;

        // Run the condition's script
        try {
          // Run the condition's script - no need to re-capture input values
          Value returnValueNode = agent.runScriptString(condition.script, false);

          // Record the script return value
          status.scriptReturnValue = returnValueNode;
        } catch (TokenizerException e){
          // TODO: Record an exception status for the condition misfire
          gotException(e);
          return false;
        } catch (ParserException e){
          gotException(e);
          return false;
        } catch (RuntimeException e){
          gotException(e);
          return false;
        }
      }

      // Reschedule the condition for its next interval, unless it is now marked as disabled
      if (status.enabled){
        // Create a new condition activity - time will be now plus condition interval
        AgentActivityCondition conditionActivity = new AgentActivityCondition(agent, condition);

        // Schedule the next interval of the condition
        log.info("Rescheduling condition " + condition.name + " for t plus " + condition.interval + " ms.");
        AgentScheduler.singleton.add(conditionActivity);
      } else
        log.info("Condition " + condition.name + " will not be rescheduled since it is now disabled");
    }
    
    finishActivity();
    return true;
  }

}
