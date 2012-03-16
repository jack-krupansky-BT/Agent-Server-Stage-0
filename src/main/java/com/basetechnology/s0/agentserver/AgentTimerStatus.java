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

import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class AgentTimerStatus {
  public AgentTimer agentTimer;
  public long time;
  public Value scriptReturnValue;
  public boolean enabled;
  public long hits;
  
  public AgentTimerStatus(AgentTimer agentTimer){
    this.agentTimer = agentTimer;
    this.time = 0;
    this.scriptReturnValue = NullValue.one;
    this.enabled = agentTimer.enabled;
    this.hits = 0;
  }

}
