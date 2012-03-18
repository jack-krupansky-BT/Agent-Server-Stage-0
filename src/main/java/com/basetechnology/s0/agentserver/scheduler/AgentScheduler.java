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

package com.basetechnology.s0.agentserver.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.basetechnology.s0.agentserver.AgentCondition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentInstanceList;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.AgentTimer;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.activities.AgentActivity;
import com.basetechnology.s0.agentserver.activities.AgentActivityCondition;
import com.basetechnology.s0.agentserver.activities.AgentActivityRunScript;
import com.basetechnology.s0.agentserver.activities.AgentActivityThread;
import com.basetechnology.s0.agentserver.activities.AgentActivityTimer;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.NameValue;

public class AgentScheduler implements Runnable {
  static final Logger log = Logger.getLogger(AgentScheduler.class);

  static public AgentScheduler singleton = null;

  public AgentServer agentServer;
  
  int SLEEP_INTERVAL = 10;
  int MAX_THREADS = 8;
  int MAX_COMPLETED_ACTIVITIES = 500;

  public boolean neverStarted;
  public boolean running;
  public boolean paused;
  public boolean shutdown;
  Thread thread;
  
  private boolean requestPause;
  private boolean requestResume;
  protected boolean requestShutdown;
  
  public List<AgentActivity> queue;
  public List<Thread> activeThreads;
  public List<AgentActivity> completedActivities;
  
  public AgentScheduler(AgentServer agentServer) throws RuntimeException, InterruptedException, AgentServerException {
    this(agentServer, true);
  }
  
  public AgentScheduler(AgentServer agentServer, boolean start) throws RuntimeException, InterruptedException, AgentServerException {
    this.agentServer = agentServer;

    // Initialize state of scheduler
    initialize();
    
    // Optionally start the scheduler
    if (start)
      start();
  }

  public void initialize(){
    this.neverStarted = true;
    this.running = false;
    this.paused = false;
    this.shutdown = false;
    this.thread = null;
    
    this.requestPause = false;
    this.requestResume = false;
    this.requestShutdown = false;
    
    this.queue = new ArrayList<AgentActivity>();
    this.activeThreads = new ArrayList<Thread>();
    this.completedActivities = new ArrayList<AgentActivity>();
  }
  
  public void run(){
    neverStarted = false;
    running = true;
    log.info("AgentScheduler.run started");
    boolean noneAvailable = false;
    while (true){
      if (requestShutdown){
        log.info("AgentScheduler.run shutdown requested");
        break;
      }
      
      // Sleep a little if queue is empty or maximum threads are running or we are suspended
      while (queue.size() == 0 || activeThreads.size() >= MAX_THREADS || requestPause || paused || noneAvailable){
        // Check if pause or resume requested
        if (requestResume){
          paused = false;
          requestPause = false;
          running = true;
          requestResume = false;
          log.info("Scheduler is resuming at external request");
        }
        else if (requestPause){
          paused = true;
          requestPause = false;
          running = false;
          log.info("Scheduler is pausing at external request");
        }

        try {
          //log.info("AgentScheduler.run sleep queue.size: " + queue.size() + " requestShutdown: " + requestShutdown + " requestPause: " + requestPause);
          // Sleep a little
          // TODO: Do a wait if nothing is in queue
          Thread.sleep(SLEEP_INTERVAL);

          // Need to re-check whether anything is available to run
          noneAvailable = false;
        } catch (InterruptedException e ){
          // Nothing to do
        }
        
        // May be a request to shut down
        if (requestShutdown){
          log.info("AgentScheduler.run sleep requestShutdown: " + requestShutdown + " requestPause: " + requestPause);
          break;
        }
      }

      // Need to re-check whether anything is available to run
      noneAvailable = true;
      
      // Now process the queue
      int queueLen = queue.size();
      for (int i = 0; i < queueLen;){
        long now = System.currentTimeMillis();

        // Skip queue processing if trying to pause or shut down
        if (requestShutdown || requestPause)
          break;

        // Process next activity if its scheduled time has come or is overdue
        AgentActivity activity = queue.get(i);
        if (activity.status == AgentActivity.StatusTypes.NOT_STARTED &&
            now >= activity.when && activity.agent.enabled){
          // Skip activity for now if target agent is already busy
          if (! activity.agent.busy){
            // Mark agent as busy
            activity.agent.busy = true;

            // Mark the activity as 'starting'
            activity.startingActivity();
            
            // Start the agent activity in a new thread
            AgentActivityThread activityThread = new AgentActivityThread(activity);
            Thread thread = new Thread(activityThread);
            thread.start();
            activityThread.thread = thread;
            activity.activityThread = activityThread;

            // Note that some activity occurred
            noneAvailable = false;
            
            // TODO: Log start of activity
            //log.info("Starting activity - " + activity.description);
          } else
            log.info("Skipping activity due to busy agent - " + activity.description);
            
          // Process next activity in queue
          i++;
        } else if (activity.status == AgentActivity.StatusTypes.COMPLETED ||
            activity.status == AgentActivity.StatusTypes.ABORTED ||
            activity.status == AgentActivity.StatusTypes.EXCEPTION){
          // Add the activity to the completed list
          completedActivities.add(activity);
          if (completedActivities.size() > MAX_COMPLETED_ACTIVITIES)
            completedActivities.remove(0);
          
          // Agent is no longer busy with this activity
          activity.agent.busy = false;
          
          // Note that other activities may now be able to run
          noneAvailable = false;
          
          // Done with this activity, remove it
          // TODO: Log end of this activity
          //log.info("Finished activity - " + activity.description + " status: " + activity.status + " in " + (activity.endTime - activity.startTime) + " ms.");
          queue.remove(i);
          queueLen = queue.size();
          
          // May be done with the thread in which the activity was running
          AgentActivityThread activityThread = activity.activityThread;
          if (activity.status == AgentActivity.StatusTypes.COMPLETED ||
              activity.status == AgentActivity.StatusTypes.ABORTED ||
              activity.status == AgentActivity.StatusTypes.EXCEPTION){
            // Yes, we really done with that activity thread
            // TODO: If not, when would thread be released??
            activeThreads.remove(activityThread.thread);
            activity.activityThread.thread = null;
            activity.activityThread = null;
          }
        } else {
          // Process next activity in queue
          i++;
        }

        // TODO: Flush activities that never completed in maximum permissible time - and log this
      }
    }

    running = false;
    paused = false;
    shutdown = true;
    requestShutdown = false;
    singleton = null;
    // TODO: Anything else to do to release this thread?
    thread = null;
    log.info("AgentScheduler.run finished");
  }

  public String getStatus(){
    if (neverStarted)
      return "never started";
    else if (running)
      return "running";
    else if (paused)
      return "paused";
    else if (shutdown)
      return "shutdown";
    else
      return "indeterminate";
  }
  
  // TODO: Insert activity based on time relative to existing activities
  
  // TODO: Need to synchronize this, and with reading from queue
  public void add(AgentActivity activity){
    queue.add(activity);
    // TODO: Notify scheduler's thread if it is waiting due to previously empty queue
  }

  static public void scheduleInit(AgentInstance agent) throws AgentServerException {
    // TODO: Should 'init' be called if agent is not yet enabled?
    // No-op if no scheduler created yet
    if (singleton != null){
      // Check if agent even has an 'init' script
      if (agent.agentDefinition.scripts.containsKey("init")){
        // Make sure we have a singleton for this class

        // Create a new activity for the 'init'
        AgentActivityRunScript initActivity = new AgentActivityRunScript(agent, 0, "Initialize agent", "init");

        // Queue up the new activity
        // TODO: This needs to synchronized
        singleton.add(initActivity);
      }
   
      // Now schedule all timers and conditions for this agent
      log.info("scheduleTimersAndConditions for " + agent.name);
      scheduleTimersAndConditions(agent);
    }
  }

  static public void scheduleTimersAndConditions(AgentInstance agent) throws AgentServerException {
    // Now schedule all timers for this agent, if agent is enabled
    // TODO: Should this be done only after 'init' finishes?
    if (agent.enabled)
      for (NameValue<AgentTimer> agentTimerNameValue: agent.agentDefinition.timers)
        // Ignore disabled timers
        if (agentTimerNameValue.value.enabled){
          // Create a new timer init activity
          AgentActivityTimer timerActivity = new AgentActivityTimer(agent, agentTimerNameValue.value);

          // Schedule it
          singleton.add(timerActivity);
        }

    // Now schedule all conditions for this agent, if agent is enabled
    // TODO: Should this be done only after 'init' finishes?
    if (agent.enabled)
      for (NameValue<AgentCondition> agentConditionNameValue: agent.agentDefinition.conditions)
        // Ignore disabled conditions
        if (agentConditionNameValue.value.enabled){
          // Create a new condition init activity
          AgentActivityCondition conditionActivity = new AgentActivityCondition(agent, agentConditionNameValue.value);

          // Schedule it
          singleton.add(conditionActivity);
        }
  }

  public void scheduleInitAll() throws AgentServerException {
    log.info("Scheduling all enabled agents for 'init'");
    // Start all agents for all users
    for (NameValue<AgentInstanceList> userAgentInstancesNameValue: agentServer.agentInstances){
      // Start all agents for this user
      for (AgentInstance agentInstance: agentServer.agentInstances.get(userAgentInstancesNameValue.name)){
        // Only schedule init of this agent if enabled
        if (agentInstance.enabled)
          AgentScheduler.scheduleInit(agentInstance);
      }
    }
  }
  
  public void shutDown(){
    this.requestShutdown = true;
  }
  
  public void waitUntilDone() throws InterruptedException {
    waitUntilDone(2 * 1000);
  }
  
  public void waitUntilDone(long howLong) throws InterruptedException {
    long now = System.currentTimeMillis();
    long max = now + howLong;
    while (queue.size() > 0 && System.currentTimeMillis() < max){
      // Sleep a little waiting for queue to empty
      Thread.sleep(SLEEP_INTERVAL);
    }
  }
  
  public void pause(){
    this.requestPause = true;
  }
  
  public void resume(){
    this.requestResume = true;
  }

  public void start() throws RuntimeException, InterruptedException, AgentServerException {
    start(true);
  }

  public void start(boolean wait) throws RuntimeException, InterruptedException, AgentServerException {
    // No-op if we are already running
    if (running)
      return;

    log.info("Starting AgentScheduler at " + DateUtils.toRfcString(System.currentTimeMillis()));
    // Reset state
    shutdown = false;
    requestShutdown = false;
    requestPause = false;
    
    // Create a new thread for this scheduler to run in
    thread = new Thread(this);
  
    // Remember this scheduler instance as the singleton instance
    singleton = this;

    thread.start();

    // Queue up call to 'init' for all enabled agents
    scheduleInitAll();

    // Queue up scheduling of all timers for all agents
    // TODO

    // Optionally wait until scheduler is actually running
    if (wait){
      int waitLimit = 2000;
      int sleepTime = 5;
      for (int i = 0; i < waitLimit && ! running; i += sleepTime)
        Thread.sleep(sleepTime);
      if (! running)
        throw new AgentServerException("Agent scheduler has not started within " + waitLimit + " ms.");
    }
  }

  public void shutdown() throws InterruptedException, AgentServerException {
    shutdown(true);
  }

  public void shutdown(boolean wait) throws InterruptedException, AgentServerException {
    // May never have been started
    if (this.neverStarted)
      return;

    this.requestShutdown = true;
    if (wait){
      int waitLimit = 2000;
      int sleepTime = 5;
      for (int i = 0; i < waitLimit && ! shutdown; i += sleepTime)
        Thread.sleep(sleepTime);
      if (! shutdown)
        throw new AgentServerException("Agent scheduler has not shut down within " + waitLimit + " ms.");
    }
  }

  public void stop() throws Exception {
    // For now, stop is merely a synonym for shutdown
    shutdown();
  }

}
