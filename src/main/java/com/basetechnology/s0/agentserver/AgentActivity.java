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

public class AgentActivity {
  static final Logger log = Logger.getLogger(AgentActivity.class);

  public AgentInstance agent;
  public enum StatusTypes {NOT_STARTED, STARTING, RUNNING, EXCEPTION, STOPPING, COMPLETED, ABORTING, ABORTED};
  public StatusTypes status;
  public long when;
  public String description;
  public boolean abortRequested;
  public AgentActivityThread activityThread;
  public Exception exception;
  public long startTime;
  public long endTime;
  
  public AgentActivity(){
    this(null, 0, null);
  }
  
  public AgentActivity(AgentInstance agent, long when, String description){
    this.agent = agent;
    this.when = when;
    this.description = description;
    status = StatusTypes.NOT_STARTED;
    abortRequested = false;
  }
  
  public boolean performActivity()  throws SymbolException, RuntimeException, AgentServerException, JSONException {
    startActivity();
    finishActivity();
    return true;
  }
  
  public void startingActivity(){
    status = StatusTypes.STARTING;
    startTime = System.currentTimeMillis();
  }
  
  public void startActivity(){
    status = StatusTypes.RUNNING;
    log.info("Starting activity - " + description);
  }
  
  public void gotException(Exception e){
    endTime = System.currentTimeMillis();
    exception = e;
    e.printStackTrace();
    log.error("Exception in activity - " + description + " - " + e);
    status = StatusTypes.EXCEPTION;
  }

  public void finishActivity(){
    endTime = System.currentTimeMillis();
    status = StatusTypes.COMPLETED;
    log.info("Finished activity - " + description);
    // TODO - Should state capture be done at this point?
  }
  
  public String toString(){
    long delta = when - System.currentTimeMillis();
    return "Activity " + description + " - scheduled for " +
        (delta > 0 ? "+" : "") + delta + " ms. from now";
  }
}
