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

package com.basetechnology.s0.agentserver.activities;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.AgentTimer;
import com.basetechnology.s0.agentserver.AgentTimerStatus;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.scheduler.AgentScheduler;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class AgentActivityTimer extends AgentActivity {
  static final Logger log = Logger.getLogger(AgentActivityTimer.class);
  public AgentTimer timer;
  
  public AgentActivityTimer(AgentInstance agent, AgentTimer timer) throws AgentServerException {
    // TODO: Review whether now plus interval or original schedule plus interval
    super(agent, System.currentTimeMillis() + timer.getInterval(agent), "AgentTimer " + timer.toString());
    this.timer = timer;
    log.info("Starting timer " + timer);
  }
  
  public boolean performActivity() throws SymbolException, RuntimeException, AgentServerException, JSONException {
    startActivity();

    // Get current timer status
    AgentTimerStatus status = agent.timerStatus.get(timer.name);

    // If timer is now disabled, ignore this lingering hit
    if (status.enabled){
      // Remember time of timer trigger
      long now = System.currentTimeMillis();
      status.time = now;

      // Count timer hits
      status.hits++;

      // Run the timer's script
      try {
        // Run the timer's script
        // TODO: Maybe skip this if timer is now disabled
        Value returnValueNode = agent.runScriptString(timer.script);

        // Record the script return value
        status.scriptReturnValue = returnValueNode;
      } catch (RuntimeException e){
        gotException(e);
        return false;
      }

      log.info("Timer script completed - " + timer.script);
      
      // Reschedule the timer for its next interval, unless it is now marked as disabled
      if (status.enabled){
        // Create a new timer activity - time will be now plus timer interval
        AgentActivityTimer timerActivity = new AgentActivityTimer(agent, timer);

        // Schedule the next interval of the timer
        if (AgentScheduler.singleton != null) {
          log.info("Rescheduling timer " + timer.name + " for t plus " + timer.getInterval(agent) + " ms.");
          AgentScheduler.singleton.add(timerActivity);
        } else 
          log.info("Timer " + timer.name + " will not be rescheduled since scheduler has been terminated");
      } else
        log.info("Timer " + timer.name + " will not be rescheduled since it is now disabled");
    }
    
    finishActivity();
    return true;
  }

}
