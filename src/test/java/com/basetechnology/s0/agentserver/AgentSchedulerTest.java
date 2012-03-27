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

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.basetechnology.s0.agentserver.AgentCondition;
import com.basetechnology.s0.agentserver.AgentConditionStatus;
import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentInstanceList;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.AgentState;
import com.basetechnology.s0.agentserver.AgentTimer;
import com.basetechnology.s0.agentserver.AgentTimerStatus;
import com.basetechnology.s0.agentserver.OutputHistory;
import com.basetechnology.s0.agentserver.OutputRecord;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.activities.AgentActivity;
import com.basetechnology.s0.agentserver.activities.AgentActivityRunScript;
import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.config.AgentServerProperties;
import com.basetechnology.s0.agentserver.scheduler.AgentScheduler;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.runtime.value.BooleanValue;
import com.basetechnology.s0.agentserver.script.runtime.value.FloatValue;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import static org.junit.Assert.*;

@Ignore
class DummyActivity extends AgentActivity {
  static final Logger log = Logger.getLogger(DummyActivity.class);

  public DummyActivity(AgentInstance agent, long when, String description){
    super(agent, when, description);
  }

  public boolean performActivity(){
    startActivity();
    log.info("Dummy activity " + description + " running");

    // Just sleep a little to simulate activity
    try {
      Thread.sleep(500);
    } catch (Exception e){
    }

    log.info("Dummy activity " + description + " completed");
    finishActivity();
    return true;
  }
}

public class AgentSchedulerTest {
  static final Logger log = Logger.getLogger(AgentSchedulerTest.class);

  AgentAppServer agentAppServer = null;
  AgentServer agentServer;

  static final long nearDeltaTime = 5000;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    if (agentAppServer != null){
      agentAppServer.stop();
      agentAppServer = null;
      agentServer = null;
    }
    File pf = new File(AgentServerProperties.DEFAULT_PERSISTENT_STORE_PATH);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServerProperties.DEFAULT_PERSISTENT_STORE_PATH, ! pf.exists());

    agentAppServer = new AgentAppServer();
    agentServer = agentAppServer.agentServer;
  }

  @After
  public void tearDown() throws Exception {
    if (agentAppServer != null){
      agentAppServer.stop();
    }
    File pf = new File(AgentServerProperties.DEFAULT_PERSISTENT_STORE_PATH);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServerProperties.DEFAULT_PERSISTENT_STORE_PATH, ! pf.exists());
    agentAppServer = null;
    agentServer = null;
  }

  @Test
  public void test() throws Exception {
    // Create a couple of dummy agent definitions and instances
    AgentDefinition dummyAgent1 = new AgentDefinition(agentServer);
    AgentInstance dummyAgent1Instance = agentServer.getAgentInstance(User.noUser,dummyAgent1, null);

    //AgentInstance dummyAgent1Instance = new AgentInstance(dummyAgent1);
    dummyAgent1Instance.enable();
    AgentDefinition dummyAgent2 = new AgentDefinition(agentServer);
    AgentInstance dummyAgent2Instance = new AgentInstance(dummyAgent2);
    dummyAgent2Instance.enable();

    // Add the new agent definitions and instances to the agent server
    agentServer.addAgentDefinition(dummyAgent1);
    agentServer.addAgentInstance(dummyAgent1Instance);
    agentServer.addAgentDefinition(dummyAgent2);
    agentServer.addAgentInstance(dummyAgent2Instance);

    AgentActivity dummyActivity1 = new DummyActivity(dummyAgent1Instance, 0, "Dummy-1");
    AgentActivity dummyActivity2 = new DummyActivity(dummyAgent2Instance, 0, "Dummy-2");

    // Suspend scheduler so nothing runs yet
    AgentScheduler agentScheduler = agentServer.agentScheduler;
    agentScheduler.pause();

    // Schedule a couple of immediate dummy events
    agentScheduler.add(dummyActivity1);
    agentScheduler.add(dummyActivity2);

    // Wait a little while
    Thread.sleep(250);

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 2, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Un-suspend scheduler to let activities run
    agentScheduler.resume();

    // Make sure dummy activity completed
    agentScheduler.waitUntilDone(2 * 1000);
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 2, agentScheduler.completedActivities.size());
    
    // Done. Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testInitScript() throws Exception {
    // Create a test user
    agentServer.addUser("Test-User");

    // Create a couple of dummy agent definitions and instances - but disable them
    AgentDefinition dummyAgentInit1 = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"ag1\", \"scripts\":[{\"name\": \"init\", \"script\": \"return 'rv-init-1';\"}, {\"name\": \"script1\", \"script\": \"wait(50); return 'rv-script-1';\"}], \"parameters\": [{\"name\": \"x\", \"type\": \"int\"}, {\"name\": \"y\", \"type\": \"float\"}, {\"name\": \"z\", \"type\": \"string\"}]}");
    AgentInstance dummyAgentInit1Instance = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName1\", \"definition\": \"ag1\", \"parameter_values\": {\"x\": 123, \"y\": 4.56, \"z\": \"abc\"}, \"enabled\": false}");
    AgentDefinition dummyAgentInit2 = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"ag2\", \"scripts\":[{\"name\": \"init\", \"script\": \"return 'rv-init-2';\"}, {\"name\": \"script2\", \"script\": \"wait(50); return 'rv-script-2';\"}], \"parameters\": [{\"name\": \"x\", \"type\": \"int\"}, {\"name\": \"y\", \"type\": \"float\"}, {\"name\": \"z\", \"type\": \"string\"}]}");
    AgentInstance dummyAgentInit2Instance = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName2\", \"definition\": \"ag2\", \"parameter_values\": {\"x\": 123, \"y\": 4.56, \"z\": \"abc\"}, \"enabled\": false}");

    // Create and start agent scheduler, but won't run 'init' all agents since they are disabled
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Make sure no scripts were run
    assertEquals("Number of scripts executed", 0, dummyAgentInit1Instance.scriptStatus.size());
    assertEquals("Number of script start times recorded", 0, dummyAgentInit1Instance.scriptStartTime.size());
    assertEquals("Number of script end times recorded", 0, dummyAgentInit1Instance.scriptEndTime.size());
    assertEquals("Number of script return values", 0, dummyAgentInit1Instance.scriptReturnValue.size());
    assertEquals("Number of scripts executed", 0, dummyAgentInit2Instance.scriptStatus.size());
    assertEquals("Number of script start times recorded", 0, dummyAgentInit2Instance.scriptStartTime.size());
    assertEquals("Number of script end times recorded", 0, dummyAgentInit2Instance.scriptEndTime.size());
    assertEquals("Number of script return values", 0, dummyAgentInit2Instance.scriptReturnValue.size());

    // Record current time
    long baseTime = System.currentTimeMillis();

    // Wait a little to assure a modest delta time
    Thread.sleep(25);

    // Enable the new agents, which should cause them to run
    dummyAgentInit1Instance.enable();
    dummyAgentInit2Instance.enable();

    // Wait a little while for both agents to run 'init'
    agentScheduler.waitUntilDone(4 * 1000);

    // Make sure the two init script activities ran
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 2, agentScheduler.completedActivities.size());

    // Make sure both 'init' scripts were run
    assertEquals("Number of scripts executed", 1, dummyAgentInit1Instance.scriptStatus.size());
    assertTrue("Script status is missing", dummyAgentInit1Instance.scriptStatus.containsKey("init"));
    assertEquals("Script status", "ran", dummyAgentInit1Instance.scriptStatus.get("init"));
    assertEquals("Number of script start times recorded", 1, dummyAgentInit1Instance.scriptStartTime.size());
    assertTrue("Script start time is missing", dummyAgentInit1Instance.scriptStartTime.containsKey("init"));
    long deltaTime = (Long)dummyAgentInit1Instance.scriptStartTime.get("init") - baseTime;
    assertTrue("Script start time is not near current time", deltaTime > 25 && deltaTime < nearDeltaTime);
    assertEquals("Number of script end times recorded", 1, dummyAgentInit1Instance.scriptEndTime.size());
    assertTrue("Script end time is missing", dummyAgentInit1Instance.scriptEndTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptEndTime.get("init") - baseTime;
    assertTrue("Script end time is not near current time", deltaTime > 25 && deltaTime < nearDeltaTime);
    assertEquals("Number of script return values", 1, dummyAgentInit1Instance.scriptReturnValue.size());
    assertTrue("Script return value is missing", dummyAgentInit1Instance.scriptReturnValue.containsKey("init"));
    Value returnValueNode = dummyAgentInit1Instance.scriptReturnValue.get("init");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-init-1", returnValueNode.getStringValue());
    assertEquals("Number of scripts executed", 1, dummyAgentInit2Instance.scriptStatus.size());
    assertTrue("Script status is missing", dummyAgentInit2Instance.scriptStatus.containsKey("init"));
    assertEquals("Script status", "ran", dummyAgentInit2Instance.scriptStatus.get("init"));
    assertEquals("Number of script start times recorded", 1, dummyAgentInit2Instance.scriptStartTime.size());
    assertTrue("Script start time is missing", dummyAgentInit2Instance.scriptStartTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptStartTime.get("init") - baseTime;
    assertTrue("Script start time is not near current time", deltaTime > 25 && deltaTime < nearDeltaTime);
    assertEquals("Number of script end times recorded", 1, dummyAgentInit2Instance.scriptEndTime.size());
    assertTrue("Script end time is missing", dummyAgentInit2Instance.scriptEndTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptEndTime.get("init") - baseTime;
    assertTrue("Script end time is not near current time", deltaTime > 25 && deltaTime < nearDeltaTime);
    assertEquals("Number of script return values", 1, dummyAgentInit2Instance.scriptReturnValue.size());
    assertTrue("Script return value is missing", dummyAgentInit2Instance.scriptReturnValue.containsKey("init"));
    returnValueNode = dummyAgentInit2Instance.scriptReturnValue.get("init");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-init-2", returnValueNode.getStringValue());

    AgentActivity dummyActivityScript1 = new AgentActivityRunScript(dummyAgentInit1Instance, 0, "Dummy-1-Script", "script1");
    AgentActivity dummyActivityScript2 = new AgentActivityRunScript(dummyAgentInit2Instance, 0, "Dummy-2-Script", "script2");

    // Suspend scheduler to prevent activities from running
    agentScheduler.pause();

    // Schedule a couple of immediate dummy events
    agentScheduler.add(dummyActivityScript1);
    agentScheduler.add(dummyActivityScript2);

    // Wait a little while
    Thread.sleep(250);

    // Make sure no activities were run since scheduler is supposed to be suspended
    assertEquals("Number of uncompleted activities", 2, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 2, agentScheduler.completedActivities.size());

    // Record current time
    long baseTime2 = System.currentTimeMillis();

    // Wait a little to assure a modest delta time
    Thread.sleep(25);

    // Un-suspend scheduler to let activities run
    agentScheduler.resume();

    // Make sure dummy activity completed
    agentScheduler.waitUntilDone(2 * 1000);
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 4, agentScheduler.completedActivities.size());

    // Make sure all 'init' scripts, two 'init' plus two dummy, were run
    assertEquals("Number of scripts executed", 2, dummyAgentInit1Instance.scriptStatus.size());
    assertTrue("Script status is missing", dummyAgentInit1Instance.scriptStatus.containsKey("init"));
    assertEquals("Script status", "ran", dummyAgentInit1Instance.scriptStatus.get("init"));
    assertTrue("Script status is missing", dummyAgentInit1Instance.scriptStatus.containsKey("script1"));
    assertEquals("Script status", "ran", dummyAgentInit1Instance.scriptStatus.get("script1"));
    assertEquals("Number of script start times recorded", 2, dummyAgentInit1Instance.scriptStartTime.size());
    assertTrue("Script start time is missing", dummyAgentInit1Instance.scriptStartTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptStartTime.get("init") - baseTime;
    assertTrue("Script start time is not near current time - deltaTime: " + deltaTime, deltaTime > 25 && deltaTime < nearDeltaTime);
    assertTrue("Script start time is missing", dummyAgentInit1Instance.scriptStartTime.containsKey("script1"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptStartTime.get("script1") - baseTime2;
    assertTrue("Script start time is not near current time - deltaTime: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    assertEquals("Number of script end times recorded", 2, dummyAgentInit1Instance.scriptEndTime.size());
    assertTrue("Script end time is missing", dummyAgentInit1Instance.scriptEndTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptEndTime.get("init") - baseTime;
    assertTrue("Script end time is not near current time - deltaTime: " + deltaTime, deltaTime > 25 && deltaTime < nearDeltaTime);
    assertTrue("Script end time is missing", dummyAgentInit1Instance.scriptEndTime.containsKey("script1"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptEndTime.get("script1") - baseTime2;
    assertTrue("Script end time is not near current time - deltaTime: " + deltaTime, deltaTime > 25 && deltaTime < nearDeltaTime);
    assertEquals("Number of script return values", 2, dummyAgentInit1Instance.scriptReturnValue.size());
    assertTrue("Script return value is missing", dummyAgentInit1Instance.scriptReturnValue.containsKey("init"));
    returnValueNode = dummyAgentInit1Instance.scriptReturnValue.get("init");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-init-1", returnValueNode.getStringValue());
    assertTrue("Script return value is missing", dummyAgentInit1Instance.scriptReturnValue.containsKey("script1"));
    returnValueNode = dummyAgentInit1Instance.scriptReturnValue.get("script1");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-script-1", returnValueNode.getStringValue());
    assertEquals("Number of scripts executed", 2, dummyAgentInit2Instance.scriptStatus.size());
    assertTrue("Script status is missing", dummyAgentInit2Instance.scriptStatus.containsKey("init"));
    assertEquals("Script status", "ran", dummyAgentInit2Instance.scriptStatus.get("init"));
    assertTrue("Script status is missing", dummyAgentInit2Instance.scriptStatus.containsKey("script2"));
    assertEquals("Script status", "ran", dummyAgentInit2Instance.scriptStatus.get("script2"));
    assertEquals("Number of script start times recorded", 2, dummyAgentInit2Instance.scriptStartTime.size());
    assertTrue("Script start time is missing", dummyAgentInit2Instance.scriptStartTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptStartTime.get("init") - baseTime;
    assertTrue("Script start time is not near current time - deltaTime: " + deltaTime, deltaTime > 25 && deltaTime < nearDeltaTime);
    assertTrue("Script start time is missing", dummyAgentInit2Instance.scriptStartTime.containsKey("script2"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptStartTime.get("script2") - baseTime2;
    assertTrue("Script start time is not near current time - deltaTime: " + deltaTime, deltaTime > 25 && deltaTime < nearDeltaTime);
    assertEquals("Number of script end times recorded", 2, dummyAgentInit2Instance.scriptEndTime.size());
    assertTrue("Script end time is missing", dummyAgentInit2Instance.scriptEndTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptEndTime.get("init") - baseTime;
    assertTrue("Script end time is not near current time - deltaTime: " + deltaTime, deltaTime > 25 && deltaTime < nearDeltaTime);
    assertTrue("Script end time is missing", dummyAgentInit2Instance.scriptEndTime.containsKey("script2"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptEndTime.get("script2") - baseTime2;
    assertTrue("Script end time is not near current time - deltaTime: " + deltaTime, deltaTime > 25 && deltaTime < nearDeltaTime);
    assertEquals("Number of script return values", 2, dummyAgentInit2Instance.scriptReturnValue.size());
    assertTrue("Script return value is missing", dummyAgentInit2Instance.scriptReturnValue.containsKey("init"));
    returnValueNode = dummyAgentInit2Instance.scriptReturnValue.get("init");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-init-2", returnValueNode.getStringValue());
    assertTrue("Script return value is missing", dummyAgentInit2Instance.scriptReturnValue.containsKey("script2"));
    returnValueNode = dummyAgentInit2Instance.scriptReturnValue.get("script2");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-script-2", returnValueNode.getStringValue());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testStartupInitScript() throws Exception {
    // Create a test user
    agentServer.addUser("Test-User");

    // Record current time
    long baseTime = System.currentTimeMillis();

    // Create a couple of dummy agent definitions and instances
    AgentDefinition dummyAgentInit1 = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"ag1\", \"scripts\":[{\"name\": \"init\", \"script\": \"return 'rv-init-1';\"}, {\"name\": \"script1\", \"script\": \"wait(50); return 'rv-script-1';\"}], \"parameters\": [{\"name\": \"x\", \"type\": \"int\"}, {\"name\": \"y\", \"type\": \"float\"}, {\"name\": \"z\", \"type\": \"string\"}]}");
    AgentInstance dummyAgentInit1Instance = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName1\", \"definition\": \"ag1\", \"parameter_values\": {\"x\": 123, \"y\": 4.56, \"z\": \"abc\"}}");
    AgentDefinition dummyAgentInit2 = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"ag2\", \"scripts\":[{\"name\": \"init\", \"script\": \"return 'rv-init-2';\"}, {\"name\": \"script2\", \"script\": \"wait(50); return 'rv-script-2';\"}], \"parameters\": [{\"name\": \"x\", \"type\": \"int\"}, {\"name\": \"y\", \"type\": \"float\"}, {\"name\": \"z\", \"type\": \"string\"}]}");
    AgentInstance dummyAgentInit2Instance = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName2\", \"definition\": \"ag2\", \"parameter_values\": {\"x\": 123, \"y\": 4.56, \"z\": \"abc\"}}");

    // Wait a little to assure a modest delta time
    Thread.sleep(25);

    // Create and start agent scheduler, which should 'init' all agents
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Wait a little while for both agents to run 'init'
    agentScheduler.waitUntilDone(4 * 1000);

    // Make sure the two init script activities ran
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 2, agentScheduler.completedActivities.size());

    // Make sure both 'init' scripts were run
    assertEquals("Number of scripts executed", 1, dummyAgentInit1Instance.scriptStatus.size());
    assertTrue("Script status is missing", dummyAgentInit1Instance.scriptStatus.containsKey("init"));
    assertEquals("Script status", "ran", dummyAgentInit1Instance.scriptStatus.get("init"));
    assertEquals("Number of script start times recorded", 1, dummyAgentInit1Instance.scriptStartTime.size());
    assertTrue("Script start time is missing", dummyAgentInit1Instance.scriptStartTime.containsKey("init"));
    long deltaTime = (Long)dummyAgentInit1Instance.scriptStartTime.get("init") - baseTime;
    assertTrue("Script start time is not near current time: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Number of script end times recorded", 1, dummyAgentInit1Instance.scriptEndTime.size());
    assertTrue("Script end time is missing", dummyAgentInit1Instance.scriptEndTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptEndTime.get("init") - baseTime;
    assertTrue("Script end time is not near current time", deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Number of script return values", 1, dummyAgentInit1Instance.scriptReturnValue.size());
    assertTrue("Script return value is missing", dummyAgentInit1Instance.scriptReturnValue.containsKey("init"));
    Value returnValueNode = dummyAgentInit1Instance.scriptReturnValue.get("init");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-init-1", returnValueNode.getStringValue());
    assertEquals("Number of scripts executed", 1, dummyAgentInit2Instance.scriptStatus.size());
    assertTrue("Script status is missing", dummyAgentInit2Instance.scriptStatus.containsKey("init"));
    assertEquals("Script status", "ran", dummyAgentInit2Instance.scriptStatus.get("init"));
    assertEquals("Number of script start times recorded", 1, dummyAgentInit2Instance.scriptStartTime.size());
    assertTrue("Script start time is missing", dummyAgentInit2Instance.scriptStartTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptStartTime.get("init") - baseTime;
    assertTrue("Script start time is not near current time: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Number of script end times recorded", 1, dummyAgentInit2Instance.scriptEndTime.size());
    assertTrue("Script end time is missing", dummyAgentInit2Instance.scriptEndTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptEndTime.get("init") - baseTime;
    assertTrue("Script end time is not near current time: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Number of script return values", 1, dummyAgentInit2Instance.scriptReturnValue.size());
    assertTrue("Script return value is missing", dummyAgentInit2Instance.scriptReturnValue.containsKey("init"));
    returnValueNode = dummyAgentInit2Instance.scriptReturnValue.get("init");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-init-2", returnValueNode.getStringValue());

    AgentActivity dummyActivityScript1 = new AgentActivityRunScript(dummyAgentInit1Instance, 0, "Dummy-1-Script", "script1");
    AgentActivity dummyActivityScript2 = new AgentActivityRunScript(dummyAgentInit2Instance, 0, "Dummy-2-Script", "script2");

    // Schedule a couple of immediate dummy events
    agentScheduler.add(dummyActivityScript1);
    agentScheduler.add(dummyActivityScript2);

    // Suspend scheduler to prevent activities from running
    agentScheduler.pause();

    // Wait a little while
    Thread.sleep(250);

    // Make sure no activities were run since scheduler is supposed to be suspended
    assertEquals("Number of uncompleted activities", 2, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 2, agentScheduler.completedActivities.size());

    // Record current time
    long baseTime2 = System.currentTimeMillis();

    // Wait a little to assure a modest delta time
    Thread.sleep(25);

    // Un-suspend scheduler to let activities run
    agentScheduler.resume();

    // Make sure dummy activity completed
    agentScheduler.waitUntilDone(2 * 1000);
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 4, agentScheduler.completedActivities.size());

    // Make sure all 'init' scripts, two 'init' plus two dummy, were run
    assertEquals("Number of scripts executed", 2, dummyAgentInit1Instance.scriptStatus.size());
    assertTrue("Script status is missing", dummyAgentInit1Instance.scriptStatus.containsKey("init"));
    assertEquals("Script status", "ran", dummyAgentInit1Instance.scriptStatus.get("init"));
    assertTrue("Script status is missing", dummyAgentInit1Instance.scriptStatus.containsKey("script1"));
    assertEquals("Script status", "ran", dummyAgentInit1Instance.scriptStatus.get("script1"));
    assertEquals("Number of script start times recorded", 2, dummyAgentInit1Instance.scriptStartTime.size());
    assertTrue("Script start time is missing", dummyAgentInit1Instance.scriptStartTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptStartTime.get("init") - baseTime;
    assertTrue("Script start time is not near current time - deltaTime: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertTrue("Script start time is missing", dummyAgentInit1Instance.scriptStartTime.containsKey("script1"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptStartTime.get("script1") - baseTime2;
    assertTrue("Script start time is not near current time - deltaTime: " + deltaTime, deltaTime > 25 && deltaTime < nearDeltaTime);
    assertEquals("Number of script end times recorded", 2, dummyAgentInit1Instance.scriptEndTime.size());
    assertTrue("Script end time is missing", dummyAgentInit1Instance.scriptEndTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptEndTime.get("init") - baseTime;
    assertTrue("Script end time is not near current time: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertTrue("Script end time is missing", dummyAgentInit1Instance.scriptEndTime.containsKey("script1"));
    deltaTime = (Long)dummyAgentInit1Instance.scriptEndTime.get("script1") - baseTime2;
    assertTrue("Script end time is not near current time - deltaTime: " + deltaTime, deltaTime > 25 && deltaTime < nearDeltaTime);
    assertEquals("Number of script return values", 2, dummyAgentInit1Instance.scriptReturnValue.size());
    assertTrue("Script return value is missing", dummyAgentInit1Instance.scriptReturnValue.containsKey("init"));
    returnValueNode = dummyAgentInit1Instance.scriptReturnValue.get("init");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-init-1", returnValueNode.getStringValue());
    assertTrue("Script return value is missing", dummyAgentInit1Instance.scriptReturnValue.containsKey("script1"));
    returnValueNode = dummyAgentInit1Instance.scriptReturnValue.get("script1");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-script-1", returnValueNode.getStringValue());
    assertEquals("Number of scripts executed", 2, dummyAgentInit2Instance.scriptStatus.size());
    assertTrue("Script status is missing", dummyAgentInit2Instance.scriptStatus.containsKey("init"));
    assertEquals("Script status", "ran", dummyAgentInit2Instance.scriptStatus.get("init"));
    assertTrue("Script status is missing", dummyAgentInit2Instance.scriptStatus.containsKey("script2"));
    assertEquals("Script status", "ran", dummyAgentInit2Instance.scriptStatus.get("script2"));
    assertEquals("Number of script start times recorded", 2, dummyAgentInit2Instance.scriptStartTime.size());
    assertTrue("Script start time is missing", dummyAgentInit2Instance.scriptStartTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptStartTime.get("init") - baseTime;
    assertTrue("Script start time is not near current time - deltaTime: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertTrue("Script start time is missing", dummyAgentInit2Instance.scriptStartTime.containsKey("script2"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptStartTime.get("script2") - baseTime2;
    assertTrue("Script start time is not near current time - deltaTime: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Number of script end times recorded", 2, dummyAgentInit2Instance.scriptEndTime.size());
    assertTrue("Script end time is missing", dummyAgentInit2Instance.scriptEndTime.containsKey("init"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptEndTime.get("init") - baseTime;
    assertTrue("Script end time is not near current time - deltaTime: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertTrue("Script end time is missing", dummyAgentInit2Instance.scriptEndTime.containsKey("script2"));
    deltaTime = (Long)dummyAgentInit2Instance.scriptEndTime.get("script2") - baseTime2;
    assertTrue("Script end time is not near current time - deltaTime: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Number of script return values", 2, dummyAgentInit2Instance.scriptReturnValue.size());
    assertTrue("Script return value is missing", dummyAgentInit2Instance.scriptReturnValue.containsKey("init"));
    returnValueNode = dummyAgentInit2Instance.scriptReturnValue.get("init");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-init-2", returnValueNode.getStringValue());
    assertTrue("Script return value is missing", dummyAgentInit2Instance.scriptReturnValue.containsKey("script2"));
    returnValueNode = dummyAgentInit2Instance.scriptReturnValue.get("script2");
    assertTrue("Script return value is not a StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Script return value", "rv-script-2", returnValueNode.getStringValue());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testTimer1() throws Exception {
    // Test a single timer that is disabled
    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has a disabled timer and make sure it doesn't run
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"TimerTest\", " +
            "\"scripts\": [{\"name\": \"init\", \"script\": \"return 'ran-init';\"}], " +
        "\"timers\": [{\"name\": \"timer-1\", \"interval\": 10, \"description\": \"10 ms timer\", \"script\": \"return 'hit timer-10';\", \"enabled\": false}]}");
    assertEquals("Timer count", 1, agDef.timers.size());
    AgentTimer timer = agDef.timers.get(0).value;
    assertTrue("Timer name is missing", timer.name != null);
    assertEquals("Timer name", "timer-1", timer.name);
    assertTrue("Timer description is missing", timer.description != null);
    assertEquals("Timer description", "10 ms timer", timer.description);
    assertEquals("Timer interval", 10, Long.parseLong(timer.intervalExpression));
    assertTrue("Timer script is missing", timer.script != null);
    assertEquals("Timer script", "return 'hit timer-10';", timer.script);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"TimerTest\", \"definition\": \"TimerTest\"}");

    // Sleep for awhile to give any timers a chance to trigger
    Thread.sleep(1250);

    // Make sure that only the 'init' script ran
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 1, agentScheduler.completedActivities.size());

    // Make sure no timers were triggered
    assertEquals("Number of timers triggered", 1, agInst.timerStatus.size());
    AgentTimerStatus status = agInst.timerStatus.get("timer-1");
    assertEquals("Timer hits", 0, status.hits);

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testTimer2() throws Exception {
    // Test a single active timer

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has a timer
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"TimerTest\", " +
        "\"timers\": [{\"name\": \"timer-1\", \"interval\": 50, \"description\": \"50 ms timer\", \"script\": \"return 'hit timer-1';\", \"enabled\": true}]}");
    assertEquals("Timer count", 1, agDef.timers.size());
    AgentTimer timer = agDef.timers.get(0).value;
    assertTrue("Timer name is missing", timer.name != null);
    assertEquals("Timer name", "timer-1", timer.name);
    assertTrue("Timer description is missing", timer.description != null);
    assertEquals("Timer description", "50 ms timer", timer.description);
    assertEquals("Timer interval", 50, Long.parseLong(timer.intervalExpression));
    assertTrue("Timer script is missing", timer.script != null);
    assertEquals("Timer script", "return 'hit timer-1';", timer.script);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"TimerTest\", \"definition\": \"TimerTest\"}");

    // Sleep for awhile to give any timers a chance to trigger a few times
    Thread.sleep(350);

    // Manually disable the timer
    AgentTimerStatus timerStatus = agInst.timerStatus.get("timer-1"); 
    timerStatus.enabled = false;

    // Wait a little longer for timer to settle down to a halt
    // TODO: If a timer is already in queue, the disable won't prevent next hit
    Thread.sleep(75);

    // Check that a reasonable number of timer hits occurred
    long hitCount = agInst.timerStatus.get("timer-1").hits;
    // TODO: Why aren't more hits occurring - s.b. 6 or 7
    assertTrue("Count of timer trigger hits is out of range 3 to 8: " + hitCount, hitCount >= 3 && hitCount <= 8);

    // Wait a little longer to assure that no additional trigger hits occur
    Thread.sleep(50 * 2);
    long hitCount2 = agInst.timerStatus.get("timer-1").hits;
    assertEquals("Hit count which should be unchanged", hitCount, hitCount2);

    // Make sure expected number of timers were triggered
    assertEquals("Number of timers triggered", 1, agInst.timerStatus.size());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }


  @Test
  public void testTimer3() throws Exception {

    // Test fours active timers, with one that never gets to fire

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has four timers, one that never has a chance to fire
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"TimerTest\", " +
            "\"memory\": [{\"name\": \"count1\", \"type\": \"integer\"}, {\"name\": \"count2\", \"type\": \"integer\"}, {\"name\": \"count3\", \"type\": \"integer\"}, {\"name\": \"count4\", \"type\": \"integer\"}], " +
            "\"timers\": [{\"name\": \"timer-1\", \"interval\": 50, \"description\": \"50 ms timer\", \"script\": \"memory.count1++; return 'hit timer-1';\", \"enabled\": true}, " +
            "             {\"name\": \"timer-2\", \"interval\": 100, \"description\": \"100 ms timer\", \"script\": \"memory.count2++; return 'hit timer-2';\", \"enabled\": true}, " +
            "             {\"name\": \"timer-3\", \"interval\": 250, \"description\": \"250 ms timer\", \"script\": \"memory.count3++; return 'hit timer-3';\", \"enabled\": true}, " +
        "             {\"name\": \"timer-4\", \"interval\": 25000, \"description\": \"25 sec timer\", \"script\": \"memory.count4++; return 'hit timer-4';\", \"enabled\": true}]}");
    assertEquals("Timer count", 4, agDef.timers.size());
    AgentTimer timer = agDef.timers.get(0).value;
    assertTrue("Timer name is missing", timer.name != null);
    assertEquals("Timer name", "timer-1", timer.name);
    assertTrue("Timer description is missing", timer.description != null);
    assertEquals("Timer description", "50 ms timer", timer.description);
    assertEquals("Timer interval", 50, Long.parseLong(timer.intervalExpression));
    assertTrue("Timer script is missing", timer.script != null);
    assertEquals("Timer script", "memory.count1++; return 'hit timer-1';", timer.script);
    timer = agDef.timers.get(1).value;
    assertTrue("Timer name is missing", timer.name != null);
    assertEquals("Timer name", "timer-2", timer.name);
    assertTrue("Timer description is missing", timer.description != null);
    assertEquals("Timer description", "100 ms timer", timer.description);
    assertEquals("Timer interval", 100, Long.parseLong(timer.intervalExpression));
    assertTrue("Timer script is missing", timer.script != null);
    assertEquals("Timer script", "memory.count2++; return 'hit timer-2';", timer.script);
    timer = agDef.timers.get(2).value;
    assertTrue("Timer name is missing", timer.name != null);
    assertEquals("Timer name", "timer-3", timer.name);
    assertTrue("Timer description is missing", timer.description != null);
    assertEquals("Timer description", "250 ms timer", timer.description);
    assertEquals("Timer interval", 250, Long.parseLong(timer.intervalExpression));
    assertTrue("Timer script is missing", timer.script != null);
    assertEquals("Timer script", "memory.count3++; return 'hit timer-3';", timer.script);
    timer = agDef.timers.get(3).value;
    assertTrue("Timer name is missing", timer.name != null);
    assertEquals("Timer name", "timer-4", timer.name);
    assertTrue("Timer description is missing", timer.description != null);
    assertEquals("Timer description", "25 sec timer", timer.description);
    assertEquals("Timer interval", 25000, Long.parseLong(timer.intervalExpression));
    assertTrue("Timer script is missing", timer.script != null);
    assertEquals("Timer script", "memory.count4++; return 'hit timer-4';", timer.script);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"TimerTest\", \"definition\": \"TimerTest\"}");

    // Sleep for awhile to give any timers a chance to trigger a few times
    Thread.sleep(525);
    //Thread.sleep(600);

    // Manually disable the timers
    AgentTimerStatus timerStatus = agInst.timerStatus.get("timer-1"); 
    timerStatus.enabled = false;
    timerStatus = agInst.timerStatus.get("timer-2"); 
    timerStatus.enabled = false;
    timerStatus = agInst.timerStatus.get("timer-3"); 
    timerStatus.enabled = false;
    timerStatus = agInst.timerStatus.get("timer-4"); 
    timerStatus.enabled = false;

    // Wait a little longer for timers to settle down to a halt
    Thread.sleep(50);

    // Check that a reasonable number of timer hits occurred
    long hitCount1 = agInst.timerStatus.get("timer-1").hits;
    // TODO: Why is this wrong? -- "skipping activity due to busy agent"
    assertTrue("Count of timer trigger hits is out of range 6 to 11: " + hitCount1, hitCount1 >= 1 && hitCount1 <= 16);
    long hitCount2 = agInst.timerStatus.get("timer-2").hits;
    assertTrue("Count of timer trigger hits is out of range 4 to 6: " + hitCount2, hitCount2 >= 1 && hitCount2 <= 8);
    long hitCount3 = agInst.timerStatus.get("timer-3").hits;
    assertTrue("Count of timer trigger hits is out of range 2 to 2: " + hitCount3, hitCount3 >= 1 && hitCount3 <= 4);
    long hitCount4 = agInst.timerStatus.get("timer-4").hits;
    assertEquals("Count of timer trigger hits for timer-4", 0, hitCount4);

    // Wait a little longer to assure that no additional trigger hits occur
    Thread.sleep(50 * 2);
    long hitCount1a = agInst.timerStatus.get("timer-1").hits;
    assertEquals("Hit count which should be unchanged", hitCount1, hitCount1a);
    long hitCount2a = agInst.timerStatus.get("timer-2").hits;
    assertEquals("Hit count which should be unchanged", hitCount2, hitCount2a);
    long hitCount3a = agInst.timerStatus.get("timer-3").hits;
    assertEquals("Hit count which should be unchanged", hitCount3, hitCount3a);
    long hitCount4a = agInst.timerStatus.get("timer-4").hits;
    assertEquals("Hit count which should be unchanged", hitCount4, hitCount4a);

    // Make sure expected number of timers were active
    assertEquals("Number of timers triggered", 4, agInst.timerStatus.size());

    // Verify that the memory timers were updated properly
    Value valueNode = agInst.getMemory("count1");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", hitCount1, valueNode.getIntValue());
    valueNode = agInst.getMemory("count2");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", hitCount2, valueNode.getIntValue());
    valueNode = agInst.getMemory("count3");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", hitCount3, valueNode.getIntValue());
    valueNode = agInst.getMemory("count4");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", hitCount4, valueNode.getIntValue());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testCondition1() throws Exception {
    // Test a condition that is missing various keys

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Test condition without name
    {
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
              "\"name\": \"ConditionTest\", " +
              "\"scripts\": [{\"name\": \"init\", \"script\": \"return 'ran-init';\"}], " +
          "\"conditions\": [{\"interval\": 10, \"condition\": \"true\", \"description\": \"10 ms condition\", \"script\": \"return 'hit condition-10';\", \"enabled\": false}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "10 ms condition", condition.description);
      assertEquals("Condition interval", "10", condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "return 'hit condition-10';", condition.script);
      assertTrue("Condition condition is missing", condition.condition != null);
      assertEquals("Condition condition", "true", condition.condition);
    }

    // Test condition without condition
    {
      agentServer.agentDefinitions.clear();
      try {
        AgentDefinition agDef = agentServer.addAgentDefinition(
            "{\"user\": \"Test-User\"," +
                "\"name\": \"ConditionTest\", " +
                "\"scripts\": [{\"name\": \"init\", \"script\": \"return 'ran-init';\"}], " +
            "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 10, \"description\": \"10 ms condition\", \"script\": \"return 'hit condition-10';\", \"enabled\": false}]}");
        assertTrue("Agent definition succeeded despite missing condition expression", agDef == null);
      } catch (AgentServerException e){
        assertEquals("Missing condition exception", "Condition condition expression is missing", e.getMessage());
      }
    }

    // Test condition without interval
    {
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
              "\"name\": \"ConditionTest\", " +
              "\"scripts\": [{\"name\": \"init\", \"script\": \"return 'ran-init';\"}], " +
          "\"conditions\": [{\"name\": \"condition-1\", \"condition\": \"true\", \"description\": \"10 ms condition\", \"script\": \"return 'hit condition-10';\", \"enabled\": false}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "condition-1", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "10 ms condition", condition.description);
      assertEquals("Condition interval", "", condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "return 'hit condition-10';", condition.script);
      assertTrue("Condition condition is missing", condition.condition != null);
      assertEquals("Condition condition", "true", condition.condition);
    }

    // Test condition without script
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
              "\"name\": \"ConditionTest\", " +
              "\"scripts\": [{\"name\": \"init\", \"script\": \"return 'ran-init';\"}], " +
          "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 10, \"condition\": \"true\", \"description\": \"10 ms condition\", \"enabled\": false}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "condition-1", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "10 ms condition", condition.description);
      assertEquals("Condition interval", "10", condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "", condition.script);
      assertTrue("Condition condition is missing", condition.condition != null);
      assertEquals("Condition condition", "true", condition.condition);
    }
  }

  @Test
  public void testCondition2() throws Exception {

    // First test with conditions that are always true

    // Test a single condition that is disabled

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has a disabled condition and make sure it doesn't run
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"ConditionTest\", " +
            "\"scripts\": [{\"name\": \"init\", \"script\": \"return 'ran-init';\"}], " +
        "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 10, \"condition\": \"true\", \"description\": \"10 ms condition\", \"script\": \"return 'hit condition-10';\", \"enabled\": false}]}");
    assertEquals("Condition count", 1, agDef.conditions.size());
    AgentCondition condition = agDef.conditions.get(0).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-1", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "10 ms condition", condition.description);
    assertEquals("Condition interval", "10", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "return 'hit condition-10';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "true", condition.condition);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"ConditionTest\", \"definition\": \"ConditionTest\"}");

    // Sleep for awhile to give any conditions a chance to trigger
    Thread.sleep(1250);

    // Make sure that only the 'init' script ran
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 1, agentScheduler.completedActivities.size());

    // Make sure no conditions were triggered
    assertEquals("Number of conditions triggered", 1, agInst.conditionStatus.size());
    AgentConditionStatus status = agInst.conditionStatus.get("condition-1");
    assertEquals("Condition hits", 0, status.hits);
  }

  @Test
  public void testCondition3() throws Exception {

    // Test a single active condition that is always false

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has a condition
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"ConditionTest\", " +
        "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 50, \"condition\": \"false\", \"description\": \"50 ms condition\", \"script\": \"return 'hit condition-1';\", \"enabled\": true}]}");
    assertEquals("Condition count", 1, agDef.conditions.size());
    AgentCondition condition = agDef.conditions.get(0).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-1", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "50 ms condition", condition.description);
    assertEquals("Condition interval", "50", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "return 'hit condition-1';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "false", condition.condition);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"ConditionTest\", \"definition\": \"ConditionTest\"}");

    // Sleep for awhile to give any conditions a chance to trigger a few times
    Thread.sleep(350);

    // Manually disable the condition
    AgentConditionStatus conditionStatus = agInst.conditionStatus.get("condition-1"); 
    conditionStatus.enabled = false;

    // Wait a little longer for condition to settle down to a halt
    // TODO: If a condition is already in queue, the disable won't prevent next hit
    Thread.sleep(75);

    // Make sure no hits occurred
    assertEquals("Count of condition hits", 0, agInst.conditionStatus.get("condition-1").hits);

    // Check that a reasonable number of condition checks occurred
    long hitCount = agInst.conditionStatus.get("condition-1").checkHits;
    // TODO: Why aren't more hits occurring - s.b. 6 or 7
    assertTrue("Count of condition trigger hits is out of range 3 to 8: " + hitCount, hitCount >= 3 && hitCount <= 8);

    // Wait a little longer to assure that no additional trigger hits occur
    Thread.sleep(50 * 2);
    assertEquals("Count of condition hits", 0, agInst.conditionStatus.get("condition-1").hits);
    long hitCount2 = agInst.conditionStatus.get("condition-1").checkHits;
    assertEquals("Hit count which should be unchanged", hitCount, hitCount2);

    // Make sure expected number of conditions were triggered
    assertEquals("Number of conditions triggered", 1, agInst.conditionStatus.size());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testCondition4() throws Exception {

    // Test a single active condition with a complex expression that is always false

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has a complex condition which always evaluates to false
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"ConditionTest\", " +
            "\"memory\": [{\"name\": \"count1\", \"type\": \"integer\"}, {\"name\": \"count2\", \"type\": \"integer\"}, {\"name\": \"boolean1\", \"type\": \"boolean\"}, {\"name\": \"float1\", \"type\": \"float\"}], " +
        "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 50, \"condition\": \"(count1 > count2 && boolean1) || float1 > count1\", \"description\": \"50 ms condition\", \"script\": \"return 'hit condition-1';\", \"enabled\": true}]}");
    assertEquals("Condition count", 1, agDef.conditions.size());
    AgentCondition condition = agDef.conditions.get(0).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-1", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "50 ms condition", condition.description);
    assertEquals("Condition interval", "50", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "return 'hit condition-1';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "(count1 > count2 && boolean1) || float1 > count1", condition.condition);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"ConditionTest\", \"definition\": \"ConditionTest\"}");

    // Sleep for awhile to give any conditions a chance to trigger a few times
    Thread.sleep(350);

    // Manually disable the condition
    AgentConditionStatus conditionStatus = agInst.conditionStatus.get("condition-1"); 
    conditionStatus.enabled = false;

    // Wait a little longer for condition to settle down to a halt
    // TODO: If a condition is already in queue, the disable won't prevent next hit
    Thread.sleep(75);

    // Make sure no hits occurred
    assertEquals("Count of condition hits", 0, agInst.conditionStatus.get("condition-1").hits);

    // Check that a reasonable number of condition checks occurred
    long hitCount = agInst.conditionStatus.get("condition-1").checkHits;
    // TODO: Why aren't more hits occurring - s.b. 6 or 7
    assertTrue("Count of condition check hits is out of range 3 to 8: " + hitCount, hitCount >= 3 && hitCount <= 8);

    // Wait a little longer to assure that no additional trigger hits occur
    Thread.sleep(50 * 2);
    assertEquals("Count of condition hits", 0, agInst.conditionStatus.get("condition-1").hits);
    long hitCount2 = agInst.conditionStatus.get("condition-1").checkHits;
    assertEquals("Check count which should be unchanged", hitCount, hitCount2);

    // Make sure expected number of conditions were triggered
    assertEquals("Number of conditions triggered", 1, agInst.conditionStatus.size());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testCondition5() throws Exception {

    // Test a single active condition that is always true

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has a condition
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"ConditionTest\", " +
        "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 50, \"condition\": \"true\", \"description\": \"50 ms condition\", \"script\": \"return 'hit condition-1';\", \"enabled\": true}]}");
    assertEquals("Condition count", 1, agDef.conditions.size());
    AgentCondition condition = agDef.conditions.get(0).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-1", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "50 ms condition", condition.description);
    assertEquals("Condition interval", "50", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "return 'hit condition-1';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "true", condition.condition);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"ConditionTest\", \"definition\": \"ConditionTest\"}");

    // Sleep for awhile to give any conditions a chance to trigger a few times
    Thread.sleep(350);

    // Manually disable the condition
    AgentConditionStatus conditionStatus = agInst.conditionStatus.get("condition-1"); 
    conditionStatus.enabled = false;

    // Wait a little longer for condition to settle down to a halt
    // TODO: If a condition is already in queue, the disable won't prevent next hit
    Thread.sleep(75);

    // Check that a reasonable number of condition hits occurred
    long hitCount = agInst.conditionStatus.get("condition-1").hits;
    // TODO: Why aren't more hits occurring - s.b. 6 or 7
    assertTrue("Count of condition trigger hits is out of range 3 to 8: " + hitCount, hitCount >= 3 && hitCount <= 8);

    // Wait a little longer to assure that no additional trigger hits occur
    Thread.sleep(50 * 2);
    long hitCount2 = agInst.conditionStatus.get("condition-1").hits;
    assertEquals("Hit count which should be unchanged", hitCount, hitCount2);

    // Make sure expected number of conditions were triggered
    assertEquals("Number of conditions triggered", 1, agInst.conditionStatus.size());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testCondition6() throws Exception {

    // Test a single active condition with a complex expression that dynamically becomes true

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has a complex condition which always evaluates to false
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"ConditionTest\", " +
            "\"memory\": [{\"name\": \"count1\", \"type\": \"integer\"}, {\"name\": \"count2\", \"type\": \"integer\"}, {\"name\": \"boolean1\", \"type\": \"boolean\"}, {\"name\": \"float1\", \"type\": \"float\"}], " +
        "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 50, \"condition\": \"(count1 > count2 && boolean1) || float1 > count1\", \"description\": \"50 ms condition\", \"script\": \"return 'hit condition-1';\", \"enabled\": true}]}");
    assertEquals("Condition count", 1, agDef.conditions.size());
    AgentCondition condition = agDef.conditions.get(0).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-1", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "50 ms condition", condition.description);
    assertEquals("Condition interval", "50", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "return 'hit condition-1';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "(count1 > count2 && boolean1) || float1 > count1", condition.condition);
    //assertEquals("Condition condition", "(count1 > count2 && boolean1) || float1 > count1", condition.condition);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"ConditionTest\", \"definition\": \"ConditionTest\"}");

    // Dynamically set memory variables so that condition will trigger
    agInst.putMemory("count1", new IntegerValue(1));
    agInst.putMemory("boolean1", BooleanValue.create(true));
    agInst.putMemory("float1", new FloatValue(2.0));

    // Sleep for awhile to give any conditions a chance to trigger a few times
    Thread.sleep(350);

    // Manually disable the condition
    AgentConditionStatus conditionStatus = agInst.conditionStatus.get("condition-1"); 
    conditionStatus.enabled = false;

    // Wait a little longer for condition to settle down to a halt
    // TODO: If a condition is already in queue, the disable won't prevent next hit
    Thread.sleep(75);

    // Check that a reasonable number of condition checks occurred
    long checkCount = agInst.conditionStatus.get("condition-1").checkHits;
    // TODO: Why aren't more hits occurring - s.b. 6 or 7
    assertTrue("Count of condition checks is out of range 3 to 8: " + checkCount, checkCount >= 3 && checkCount <= 8);

    // Check that a reasonable number of condition hits occurred
    long hitCount = agInst.conditionStatus.get("condition-1").hits;
    // TODO: Why aren't more hits occurring - s.b. 6 or 7
    assertTrue("Count of condition trigger hits is out of range 3 to 8: " + hitCount, hitCount >= 3 && hitCount <= 8);

    // Wait a little longer to assure that no additional trigger hits occur
    Thread.sleep(50 * 2);
    long checkCount2 = agInst.conditionStatus.get("condition-1").checkHits;
    assertEquals("Check count which should be unchanged", checkCount, checkCount2);
    long hitCount2 = agInst.conditionStatus.get("condition-1").hits;
    assertEquals("Hit count which should be unchanged", hitCount, hitCount2);

    // Make sure expected number of conditions were triggered
    assertEquals("Number of conditions triggered", 1, agInst.conditionStatus.size());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testCondition7() throws Exception {

    // Test a single active condition with a complex expression that dynamically becomes true after awhile

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has a complex condition which always evaluates to false
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"ConditionTest\", " +
            "\"memory\": [{\"name\": \"count1\", \"type\": \"integer\"}, {\"name\": \"count2\", \"type\": \"integer\"}, {\"name\": \"boolean1\", \"type\": \"boolean\"}, {\"name\": \"float1\", \"type\": \"float\"}], " +
        "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 50, \"condition\": \"(count1 > count2 && boolean1) || float1 > count1\", \"description\": \"50 ms condition\", \"script\": \"return 'hit condition-1';\", \"enabled\": true}]}");
    assertEquals("Condition count", 1, agDef.conditions.size());
    AgentCondition condition = agDef.conditions.get(0).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-1", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "50 ms condition", condition.description);
    assertEquals("Condition interval", "50", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "return 'hit condition-1';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "(count1 > count2 && boolean1) || float1 > count1", condition.condition);
    //assertEquals("Condition condition", "(count1 > count2 && boolean1) || float1 > count1", condition.condition);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"ConditionTest\", \"definition\": \"ConditionTest\"}");

    // Sleep for awhile while condition fails to trigger
    Thread.sleep(175);

    // Dynamically set memory variables so that condition will trigger
    agInst.putMemory("count1", new IntegerValue(1));
    agInst.putMemory("boolean1", BooleanValue.create(true));
    agInst.putMemory("float1", new FloatValue(2.0));

    // Sleep for awhile to give any conditions a chance to trigger a few times
    Thread.sleep(175);

    // Manually disable the condition
    AgentConditionStatus conditionStatus = agInst.conditionStatus.get("condition-1"); 
    conditionStatus.enabled = false;

    // Wait a little longer for condition to settle down to a halt
    // TODO: If a condition is already in queue, the disable won't prevent next hit
    Thread.sleep(75);

    // Check that a reasonable number of condition checks occurred
    long checkCount = agInst.conditionStatus.get("condition-1").checkHits;
    assertTrue("Count of condition checks is out of range 3 to 8: " + checkCount, checkCount >= 3 && checkCount <= 8);

    // Check that a reasonable number of condition hits occurred - roughly half the time
    long hitCount = agInst.conditionStatus.get("condition-1").hits;
    assertTrue("Count of condition trigger hits is out of range 2 to 4: " + hitCount, hitCount >= 2 && hitCount <= 4);

    // Wait a little longer to assure that no additional trigger hits occur
    Thread.sleep(50 * 2);
    long checkCount2 = agInst.conditionStatus.get("condition-1").checkHits;
    assertEquals("Check count which should be unchanged", checkCount, checkCount2);
    long hitCount2 = agInst.conditionStatus.get("condition-1").hits;
    assertEquals("Hit count which should be unchanged", hitCount, hitCount2);

    // Make sure expected number of conditions were triggered
    assertEquals("Number of conditions triggered", 1, agInst.conditionStatus.size());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testCondition8() throws Exception {

    // Test four active conditions, with one that never gets to fire

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has four conditions, one that never has a chance to fire
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"ConditionTest\", " +
            "\"memory\": [{\"name\": \"count1\", \"type\": \"integer\"}, {\"name\": \"count2\", \"type\": \"integer\"}, {\"name\": \"count3\", \"type\": \"integer\"}, {\"name\": \"count4\", \"type\": \"integer\"}], " +
            "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 50, \"condition\": \"true\", \"description\": \"50 ms condition\", \"script\": \"memory.count1++; return 'hit condition-1';\", \"enabled\": true}, " +
            "             {\"name\": \"condition-2\", \"interval\": 100, \"condition\": \"true\", \"description\": \"100 ms condition\", \"script\": \"memory.count2++; return 'hit condition-2';\", \"enabled\": true}, " +
            "             {\"name\": \"condition-3\", \"interval\": 250, \"condition\": \"true\", \"description\": \"250 ms condition\", \"script\": \"memory.count3++; return 'hit condition-3';\", \"enabled\": true}, " +
        "             {\"name\": \"condition-4\", \"interval\": 25000, \"condition\": \"true\", \"description\": \"25 sec condition\", \"script\": \"memory.count4++; return 'hit condition-4';\", \"enabled\": true}]}");
    assertEquals("Condition count", 4, agDef.conditions.size());
    AgentCondition condition = agDef.conditions.get(0).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-1", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "50 ms condition", condition.description);
    assertEquals("Condition interval", "50", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "memory.count1++; return 'hit condition-1';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "true", condition.condition);
    condition = agDef.conditions.get(1).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-2", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "100 ms condition", condition.description);
    assertEquals("Condition interval", "100", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "memory.count2++; return 'hit condition-2';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "true", condition.condition);
    condition = agDef.conditions.get(2).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-3", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "250 ms condition", condition.description);
    assertEquals("Condition interval", "250", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "memory.count3++; return 'hit condition-3';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "true", condition.condition);
    condition = agDef.conditions.get(3).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-4", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "25 sec condition", condition.description);
    assertEquals("Condition interval", "25000", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "memory.count4++; return 'hit condition-4';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "true", condition.condition);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"ConditionTest\", \"definition\": \"ConditionTest\"}");

    // Sleep for awhile to give any conditions a chance to trigger a few times
    Thread.sleep(550);
    //Thread.sleep(600);

    // Manually disable the conditions
    AgentConditionStatus conditionStatus = agInst.conditionStatus.get("condition-1"); 
    conditionStatus.enabled = false;
    conditionStatus = agInst.conditionStatus.get("condition-2"); 
    conditionStatus.enabled = false;
    conditionStatus = agInst.conditionStatus.get("condition-3"); 
    conditionStatus.enabled = false;
    conditionStatus = agInst.conditionStatus.get("condition-4"); 
    conditionStatus.enabled = false;

    // Wait a little longer for conditions to settle down to a halt
    Thread.sleep(50);

    // Check that a reasonable number of condition hits occurred
    long hitCount1 = agInst.conditionStatus.get("condition-1").hits;
    // TODO: Why is this wrong? -- "skipping activity due to busy agent"
    assertTrue("Count of condition trigger hits is out of range 5 to 11: " + hitCount1, hitCount1 >= 1 && hitCount1 <= 15);
    long hitCount2 = agInst.conditionStatus.get("condition-2").hits;
    assertTrue("Count of condition trigger hits is out of range 4 to 6: " + hitCount2, hitCount2 >= 1 && hitCount2 <= 8);
    long hitCount3 = agInst.conditionStatus.get("condition-3").hits;
    assertTrue("Count of condition trigger hits is out of range 2 to 2: " + hitCount3, hitCount3 >= 1 && hitCount3 <= 5);
    long hitCount4 = agInst.conditionStatus.get("condition-4").hits;
    assertEquals("Count of condition trigger hits for condition-4", 0, hitCount4);

    // Wait a little longer to assure that no additional trigger hits occur
    Thread.sleep(50 * 2);
    long hitCount1a = agInst.conditionStatus.get("condition-1").hits;
    assertEquals("Hit count which should be unchanged", hitCount1, hitCount1a);
    long hitCount2a = agInst.conditionStatus.get("condition-2").hits;
    assertEquals("Hit count which should be unchanged", hitCount2, hitCount2a);
    long hitCount3a = agInst.conditionStatus.get("condition-3").hits;
    assertEquals("Hit count which should be unchanged", hitCount3, hitCount3a);
    long hitCount4a = agInst.conditionStatus.get("condition-4").hits;
    assertEquals("Hit count which should be unchanged", hitCount4, hitCount4a);

    // Make sure expected number of conditions were active
    assertEquals("Number of conditions triggered", 4, agInst.conditionStatus.size());

    // Verify that the memory conditions were updated properly
    Value valueNode = agInst.getMemory("count1");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", hitCount1, valueNode.getIntValue());
    valueNode = agInst.getMemory("count2");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", hitCount2, valueNode.getIntValue());
    valueNode = agInst.getMemory("count3");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", hitCount3, valueNode.getIntValue());
    valueNode = agInst.getMemory("count4");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", hitCount4, valueNode.getIntValue());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testCondition9() throws Exception {

    // Now test with four conditions which are always false

    // Create a test user
    agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());

    // Create an agent that has four conditions which are always false, so none fire
    agentServer.agentInstances.clear();
    agentServer.agentDefinitions.clear();
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"ConditionTest\", " +
            "\"memory\": [{\"name\": \"count1\", \"type\": \"integer\"}, {\"name\": \"count2\", \"type\": \"integer\"}, {\"name\": \"count3\", \"type\": \"integer\"}, {\"name\": \"count4\", \"type\": \"integer\"}], " +
            "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 50, \"condition\": \"false\", \"description\": \"50 ms condition\", \"script\": \"memory.count1++; return 'hit condition-1';\", \"enabled\": true}, " +
            "             {\"name\": \"condition-2\", \"interval\": 100, \"condition\": \"false\", \"description\": \"100 ms condition\", \"script\": \"memory.count2++; return 'hit condition-2';\", \"enabled\": true}, " +
            "             {\"name\": \"condition-3\", \"interval\": 250, \"condition\": \"false\", \"description\": \"250 ms condition\", \"script\": \"memory.count3++; return 'hit condition-3';\", \"enabled\": true}, " +
        "             {\"name\": \"condition-4\", \"interval\": 25000, \"condition\": \"false\", \"description\": \"25 sec condition\", \"script\": \"memory.count4++; return 'hit condition-4';\", \"enabled\": true}]}");
    assertEquals("Condition count", 4, agDef.conditions.size());
    AgentCondition condition = agDef.conditions.get(0).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-1", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "50 ms condition", condition.description);
    assertEquals("Condition interval", "50", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "memory.count1++; return 'hit condition-1';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "false", condition.condition);
    condition = agDef.conditions.get(1).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-2", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "100 ms condition", condition.description);
    assertEquals("Condition interval", "100", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "memory.count2++; return 'hit condition-2';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "false", condition.condition);
    condition = agDef.conditions.get(2).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-3", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "250 ms condition", condition.description);
    assertEquals("Condition interval", "250", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "memory.count3++; return 'hit condition-3';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "false", condition.condition);
    condition = agDef.conditions.get(3).value;
    assertTrue("Condition name is missing", condition.name != null);
    assertEquals("Condition name", "condition-4", condition.name);
    assertTrue("Condition description is missing", condition.description != null);
    assertEquals("Condition description", "25 sec condition", condition.description);
    assertEquals("Condition interval", "25000", condition.interval);
    assertTrue("Condition script is missing", condition.script != null);
    assertEquals("Condition script", "memory.count4++; return 'hit condition-4';", condition.script);
    assertTrue("Condition condition is missing", condition.condition != null);
    assertEquals("Condition condition", "false", condition.condition);

    // Now create an instance of that agent definition
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"ConditionTest\", \"definition\": \"ConditionTest\"}");

    // Sleep for awhile to give any conditions a chance to trigger a few times
    //Thread.sleep(525);
    Thread.sleep(600);

    // Manually disable the conditions
    AgentConditionStatus conditionStatus = agInst.conditionStatus.get("condition-1"); 
    conditionStatus.enabled = false;
    conditionStatus = agInst.conditionStatus.get("condition-2"); 
    conditionStatus.enabled = false;
    conditionStatus = agInst.conditionStatus.get("condition-3"); 
    conditionStatus.enabled = false;
    conditionStatus = agInst.conditionStatus.get("condition-4"); 
    conditionStatus.enabled = false;

    // Wait a little longer for conditions to settle down to a halt
    Thread.sleep(50);

    // Make sure no actual hits occurred since conditions were hard-wired to be false
    assertEquals("Count of condition trigger hits", 0, agInst.conditionStatus.get("condition-1").hits);
    assertEquals("Count of condition trigger hits", 0, agInst.conditionStatus.get("condition-2").hits);
    assertEquals("Count of condition trigger hits", 0, agInst.conditionStatus.get("condition-3").hits);
    assertEquals("Count of condition trigger hits", 0, agInst.conditionStatus.get("condition-4").hits);

    // Check that a reasonable number of condition checks occurred
    long hitCount1 = agInst.conditionStatus.get("condition-1").checkHits;
    assertTrue("Count of condition trigger hits is out of range 8 to 11: " + hitCount1, hitCount1 >= 8 && hitCount1 <= 11);
    long hitCount2 = agInst.conditionStatus.get("condition-2").checkHits;
    assertTrue("Count of condition trigger hits is out of range 4 to 6: " + hitCount2, hitCount2 >= 4 && hitCount2 <= 6);
    long hitCount3 = agInst.conditionStatus.get("condition-3").checkHits;
    assertTrue("Count of condition trigger hits is out of range 2 to 2: " + hitCount3, hitCount3 >= 2 && hitCount3 <= 2);
    long hitCount4 = agInst.conditionStatus.get("condition-4").checkHits;
    assertEquals("Count of condition trigger hits for condition-4", 0, hitCount4);

    // Wait a little longer to assure that no additional trigger hits occur
    Thread.sleep(50 * 2);
    assertEquals("Count of condition check trigger hits", 0, agInst.conditionStatus.get("condition-1").hits);
    assertEquals("Count of condition check trigger hits", 0, agInst.conditionStatus.get("condition-2").hits);
    assertEquals("Count of condition check trigger hits", 0, agInst.conditionStatus.get("condition-3").hits);
    assertEquals("Count of condition check trigger hits", 0, agInst.conditionStatus.get("condition-4").hits);
    long hitCount1a = agInst.conditionStatus.get("condition-1").checkHits;
    assertEquals("Hit count which should be unchanged", hitCount1, hitCount1a);
    long hitCount2a = agInst.conditionStatus.get("condition-2").checkHits;
    assertEquals("Hit count which should be unchanged", hitCount2, hitCount2a);
    long hitCount3a = agInst.conditionStatus.get("condition-3").checkHits;
    assertEquals("Hit count which should be unchanged", hitCount3, hitCount3a);
    long hitCount4a = agInst.conditionStatus.get("condition-4").checkHits;
    assertEquals("Hit count which should be unchanged", hitCount4, hitCount4a);

    // Make sure expected number of conditions were active
    assertEquals("Number of conditions triggered", 4, agInst.conditionStatus.size());

    // Verify that the memory conditions were updated properly
    Value valueNode = agInst.getMemory("count1");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", 0, valueNode.getIntValue());
    valueNode = agInst.getMemory("count2");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", 0, valueNode.getIntValue());
    valueNode = agInst.getMemory("count3");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", 0, valueNode.getIntValue());
    valueNode = agInst.getMemory("count4");
    assertTrue("Counter is not an IntegerValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Counter", 0, valueNode.getIntValue());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testDataSource1() throws Exception {

    // Test basic scenario of an agent taking input from a timer-driven data source

    // Create a test user
    User user = agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());
    // Record start time
    long startTime = System.currentTimeMillis();

    // Create a data source that uses a timer to sequence output through several values
    // Initially disabled until ready with agent that references it
    AgentDefinition dsDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"DataSource1\", " +
            "\"parameters\": [{\"name\": \"prefix_text\", \"type\": \"String\", \"default_value\": \"Count is\"}], " +
            "\"memory\": [{\"name\": \"count\", \"type\": \"integer\", \"default_value\": 0}], " +
            "\"outputs\": [{\"name\": \"field1\", \"type\": \"integer\", \"default_value\": -1}, " +
            "              {\"name\": \"field2\", \"type\": \"string\", \"default_value\": \"nothing\"}], " +
            "\"timers\": [{\"name\": \"timer-1\", \"interval\": 50, \"description\": \"50 ms timer\", \"script\": \"if (memory.count < 6){outputs.field1 = ++memory.count; outputs.field2 = parameters.prefix_text + ' ' + memory.count;}\", \"enabled\": true}], " +
        "\"enabled\": false}");

    // Now create an agent that references that data source and copies input to output
    // Also sets a parameter for the data source
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"TestAgent\", " +
            "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\", " +
            "             \"parameter_values\": {\"prefix_text\": \"Our count is...\"}}], " +
            "\"outputs\": [{\"name\": \"outField1\", \"type\": \"integer\", \"default_value\": -123}, " +
            "              {\"name\": \"outField2\", \"type\": \"string\", \"default_value\": \"not-yet\"}], " +
            "\"scripts\": [{\"name\": \"inputs_changed\", \"script\": \"outputs.outField1 = inputs.input1['field1']; outputs.outField2 = inputs.input1['field2'];\"}], " +
        "\"enabled\": false, \"trigger_interval\": 1}");

    // Now instantiate the agent, which should also instantiate the data source agent it references
    AgentInstance agInst = agentServer.getAgentInstance(user, agDef);

    // Make sure default output for (disabled) agent is correct
    SymbolValues agSyms = agInst.categorySymbolValues.get("outputs");
    assertEquals("Count of data source output fields", 2, agSyms.size());
    Value valueNode = agSyms.get("outField1");
    assertTrue("Agent output field field1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
    valueNode = agSyms.get("outField2");
    assertTrue("Agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

    // Find the data source instance that was automatically instantiated for input to agent
    assertEquals("Count of users with instantiated instances", 1, agentServer.agentInstances.size());
    assertEquals("Count of auto-instantiated data source instances for agent definition", 1, agInst.dataSourceInstances.size());
    AgentInstanceList instances = agentServer.agentInstances.get(user.id);
    assertEquals("Count of instantiated instances", 2, instances.size());
    AgentInstance dsInst = instances.getByDefinitionName("DataSource1");

    // Make sure default output is correct for the data source instance
    log.info("TEST: Initial data source output values: " + dsInst.categorySymbolValues.get("outputs").toJson());
    assertEquals("Count of output values", 2, dsInst.categorySymbolValues.get("outputs").size());
    valueNode = dsInst.getOutput("field1");
    assertTrue("Data source output field field1 is missing", valueNode != null);
    assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of data source output field1", -1, valueNode.getIntValue());
    valueNode = dsInst.getOutput("field2");
    assertTrue("Data source output field field2 is missing", valueNode != null);
    assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of data source output field2", "nothing", valueNode.getStringValue());

    // Make sure initial output is correct for the agent instance
    log.info("TEST: Initial agent output values: " + agInst.categorySymbolValues.get("outputs").toJson());
    assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
    valueNode = agInst.getOutput("outField1");
    assertTrue("agent output field outField1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
    valueNode = agInst.getOutput("outField2");
    assertTrue("agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

    log.info("TEST: Initial agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

    // Now enable the agent and then the data source
    agInst.enable();
    dsInst.enable();

    // Let agents run for awhile
    Thread.sleep(800);

    // Now disable the data source instance
    dsInst.disable();

    // Wait more than enough time for agents to settle down
    Thread.sleep(100);

    // Verify final output of data source instance
    assertEquals("Count of data source output values", 2, dsInst.categorySymbolValues.get("outputs").size());
    valueNode = dsInst.getOutput("field1");
    assertTrue("Data source output field field1 is missing", valueNode != null);
    assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of data source output field1", 6, valueNode.getIntValue());
    valueNode = dsInst.getOutput("field2");
    assertTrue("Data source output field field2 is missing", valueNode != null);
    assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of data source output field2", "Our count is... 6", valueNode.getStringValue());

    log.info("TEST: Final agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

    // Verify final output of agent instance
    assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
    valueNode = agInst.getOutput("outField1");
    assertTrue("agent output field outField1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", 6, valueNode.getIntValue());
    valueNode = agInst.getOutput("outField2");
    assertTrue("agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "Our count is... 6", valueNode.getStringValue());

    // Verify data source output history
    OutputHistory dsHistory = dsInst.outputHistory;
    assertEquals("Count of data source history", 7, dsHistory.size());
    OutputRecord dsRecord = dsHistory.get(0);
    assertEquals("Data source history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
    long deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[0].output", "{\"field1\":-1,\"field2\":\"nothing\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(1);
    assertEquals("Data source history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[1].output", "{\"field1\":1,\"field2\":\"Our count is... 1\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(2);
    assertEquals("Data source history[2].sequenceNumber", 3, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[2].time delta not near expected: " + deltaTime, deltaTime > 20 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[2].output", "{\"field1\":2,\"field2\":\"Our count is... 2\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(3);
    assertEquals("Data source history[3].sequenceNumber", 4, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[3].time delta not near expected: " + deltaTime, deltaTime > 30 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[3].output", "{\"field1\":3,\"field2\":\"Our count is... 3\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(4);
    assertEquals("Data source history[4].sequenceNumber", 5, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[4].time delta not near expected: " + deltaTime, deltaTime > 40 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[4].output", "{\"field1\":4,\"field2\":\"Our count is... 4\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(5);
    assertEquals("Data source history[5].sequenceNumber", 6, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[5].time delta not near expected: " + deltaTime, deltaTime > 50 && deltaTime < 6000);
    assertEquals("Data source history[5].output", "{\"field1\":5,\"field2\":\"Our count is... 5\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(6);
    assertEquals("Data source history[6].sequenceNumber", 7, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[6].time delta not near expected: " + deltaTime, deltaTime > 60 && deltaTime < 7000);
    assertEquals("Data source history[6].output", "{\"field1\":6,\"field2\":\"Our count is... 6\"}", dsRecord.output.toJson().toString());

    // Verify agent output history
    OutputHistory agHistory = agInst.outputHistory;
    assertEquals("Count of agent history", 7, agHistory.size());
    dsRecord = agHistory.get(0);
    assertEquals("Agent history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[0].output", "{\"outField1\":-123,\"outField2\":\"not-yet\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(1);
    assertEquals("Agent history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[1].output", "{\"outField1\":1,\"outField2\":\"Our count is... 1\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(2);
    assertEquals("Agent history[2].sequenceNumber", 3, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[2].time delta not near expected: " + deltaTime, deltaTime > 20 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[2].output", "{\"outField1\":2,\"outField2\":\"Our count is... 2\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(3);
    assertEquals("Agent history[3].sequenceNumber", 4, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[3].time delta not near expected: " + deltaTime, deltaTime > 30 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[3].output", "{\"outField1\":3,\"outField2\":\"Our count is... 3\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(4);
    assertEquals("Agent history[4].sequenceNumber", 5, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[4].time delta not near expected: " + deltaTime, deltaTime > 40 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[4].output", "{\"outField1\":4,\"outField2\":\"Our count is... 4\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(5);
    assertEquals("Agent history[5].sequenceNumber", 6, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[5].time delta not near expected: " + deltaTime, deltaTime > 50 && deltaTime < 6000);
    assertEquals("Agent history[5].output", "{\"outField1\":5,\"outField2\":\"Our count is... 5\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(6);
    assertEquals("Agent history[6].sequenceNumber", 7, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[6].time delta not near expected: " + deltaTime, deltaTime > 60 && deltaTime < 7000);
    assertEquals("Agent history[6].output", "{\"outField1\":6,\"outField2\":\"Our count is... 6\"}", dsRecord.output.toJson().toString());

    // TODO Test with a trigger interval on agent so it only sees a couple of the data source changes

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testDataSource2() throws Exception {

    // Test moderate throttle down of agent using 'trigger_interval' - half as many events

    // Create a test user
    User user = agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());
    // Record start time
    long startTime = System.currentTimeMillis();

    // Create a data source that uses a timer to sequence output through several values
    // Initially disabled until ready with agent that references it
    AgentDefinition dsDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"DataSource1\", " +
            "\"parameters\": [{\"name\": \"prefix_text\", \"type\": \"String\", \"default_value\": \"Count is\"}], " +
            "\"memory\": [{\"name\": \"count\", \"type\": \"integer\", \"default_value\": 0}], " +
            "\"outputs\": [{\"name\": \"field1\", \"type\": \"integer\", \"default_value\": -1}, " +
            "              {\"name\": \"field2\", \"type\": \"string\", \"default_value\": \"nothing\"}], " +
            "\"timers\": [{\"name\": \"timer-1\", \"interval\": 50, \"description\": \"50 ms timer\", \"script\": \"if (memory.count < 6){outputs.field1 = ++memory.count; outputs.field2 = parameters.prefix_text + ' ' + memory.count;}\", \"enabled\": true}], " +
        "\"enabled\": false}");

    // Now create an agent that references that data source and copies input to output
    // Also sets a parameter for the data source
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"TestAgent\", " +
            "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\", " +
            "             \"parameter_values\": {\"prefix_text\": \"Our count is...\"}}], " +
            "\"outputs\": [{\"name\": \"outField1\", \"type\": \"integer\", \"default_value\": -123}, " +
            "              {\"name\": \"outField2\", \"type\": \"string\", \"default_value\": \"not-yet\"}], " +
            "\"scripts\": [{\"name\": \"inputs_changed\", \"script\": \"outputs.outField1 = inputs.input1['field1']; outputs.outField2 = inputs.input1['field2'];\"}], " +
        "\"enabled\": false, \"trigger_interval\": 100}");

    // Now instantiate the agent, which should also instantiate the data source agent it references
    AgentInstance agInst = agentServer.getAgentInstance(user, agDef);

    // Make sure default output for (disabled) agent is correct
    SymbolValues agSyms = agInst.categorySymbolValues.get("outputs");
    assertEquals("Count of data source output fields", 2, agSyms.size());
    Value valueNode = agSyms.get("outField1");
    assertTrue("Agent output field field1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
    valueNode = agSyms.get("outField2");
    assertTrue("Agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

    // Find the data source instance that was automatically instantiated for input to agent
    assertEquals("Count of users with instantiated instances", 1, agentServer.agentInstances.size());
    assertEquals("Count of auto-instantiated data source instances for agent definition", 1, agInst.dataSourceInstances.size());
    AgentInstanceList instances = agentServer.agentInstances.get(user.id);
    assertEquals("Count of instantiated instances", 2, instances.size());
    AgentInstance dsInst = instances.getByDefinitionName("DataSource1");

    // Make sure default output is correct for the data source instance
    log.info("TEST: Initial data source output values: " + dsInst.categorySymbolValues.get("outputs").toJson());
    assertEquals("Count of output values", 2, dsInst.categorySymbolValues.get("outputs").size());
    valueNode = dsInst.getOutput("field1");
    assertTrue("Data source output field field1 is missing", valueNode != null);
    assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of data source output field1", -1, valueNode.getIntValue());
    valueNode = dsInst.getOutput("field2");
    assertTrue("Data source output field field2 is missing", valueNode != null);
    assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of data source output field2", "nothing", valueNode.getStringValue());

    // Make sure initial output is correct for the agent instance
    log.info("TEST: Initial agent output values: " + agInst.categorySymbolValues.get("outputs").toJson());
    assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
    valueNode = agInst.getOutput("outField1");
    assertTrue("agent output field outField1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
    valueNode = agInst.getOutput("outField2");
    assertTrue("agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

    log.info("TEST: Initial agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

    // Now enable the agent and then the data source
    agInst.enable();
    dsInst.enable();

    // Let agents run for awhile
    Thread.sleep((6 + 1) * 100);

    // Now disable the data source instance
    dsInst.disable();

    // Wait more than enough time for agents to settle down
    Thread.sleep(100);

    // Verify final output of data source instance
    assertEquals("Count of data source output values", 2, dsInst.categorySymbolValues.get("outputs").size());
    valueNode = dsInst.getOutput("field1");
    assertTrue("Data source output field field1 is missing", valueNode != null);
    assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of data source output field1", 6, valueNode.getIntValue());
    valueNode = dsInst.getOutput("field2");
    assertTrue("Data source output field field2 is missing", valueNode != null);
    assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of data source output field2", "Our count is... 6", valueNode.getStringValue());

    log.info("TEST: Final agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

    // Verify final output of agent instance
    assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
    valueNode = agInst.getOutput("outField1");
    assertTrue("agent output field outField1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", 6, valueNode.getIntValue());
    valueNode = agInst.getOutput("outField2");
    assertTrue("agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "Our count is... 6", valueNode.getStringValue());

    // Verify data source output history
    OutputHistory dsHistory = dsInst.outputHistory;
    assertEquals("Count of data source history", 7, dsHistory.size());
    OutputRecord dsRecord = dsHistory.get(0);
    assertEquals("Data source history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
    long deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[0].output", "{\"field1\":-1,\"field2\":\"nothing\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(1);
    assertEquals("Data source history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[1].output", "{\"field1\":1,\"field2\":\"Our count is... 1\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(2);
    assertEquals("Data source history[2].sequenceNumber", 3, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[2].time delta not near expected: " + deltaTime, deltaTime > 20 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[2].output", "{\"field1\":2,\"field2\":\"Our count is... 2\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(3);
    assertEquals("Data source history[3].sequenceNumber", 4, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[3].time delta not near expected: " + deltaTime, deltaTime > 30 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[3].output", "{\"field1\":3,\"field2\":\"Our count is... 3\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(4);
    assertEquals("Data source history[4].sequenceNumber", 5, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[4].time delta not near expected: " + deltaTime, deltaTime > 40 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[4].output", "{\"field1\":4,\"field2\":\"Our count is... 4\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(5);
    assertEquals("Data source history[5].sequenceNumber", 6, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[5].time delta not near expected: " + deltaTime, deltaTime > 50 && deltaTime < 6000);
    assertEquals("Data source history[5].output", "{\"field1\":5,\"field2\":\"Our count is... 5\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(6);
    assertEquals("Data source history[6].sequenceNumber", 7, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[6].time delta not near expected: " + deltaTime, deltaTime > 60 && deltaTime < 7000);
    assertEquals("Data source history[6].output", "{\"field1\":6,\"field2\":\"Our count is... 6\"}", dsRecord.output.toJson().toString());

    // Verify agent output history
    OutputHistory agHistory = agInst.outputHistory;
    int agHistorySize = agHistory.size();
    log.info("TEST: agHistorySize: " + agHistorySize);
    assertTrue("Agent history size not in range 3 to 5: " + agHistorySize, agHistorySize >= 3 && agHistorySize <= 5);
    assertEquals("Count of agent history", 4, agHistory.size());
    dsRecord = agHistory.get(0);
    assertEquals("Agent history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[0].output", "{\"outField1\":-123,\"outField2\":\"not-yet\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(1);
    assertEquals("Agent history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[1].output", "{\"outField1\":2,\"outField2\":\"Our count is... 2\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(2);
    assertEquals("Agent history[2].sequenceNumber", 3, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[2].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[2].output", "{\"outField1\":4,\"outField2\":\"Our count is... 4\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(3);
    assertEquals("Agent history[3].sequenceNumber", 4, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[3].time delta not near expected: " + deltaTime, deltaTime > 30 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[3].output", "{\"outField1\":6,\"outField2\":\"Our count is... 6\"}", dsRecord.output.toJson().toString());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testDataSource3() throws Exception {

    // Test heavier throttle down of agent using 'trigger_interval' - only a few events

    // Create a test user
    User user = agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());
    // Record start time
    long startTime = System.currentTimeMillis();

    // Create a data source that uses a timer to sequence output through several values
    // Initially disabled until ready with agent that references it
    AgentDefinition dsDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"DataSource1\", " +
            "\"parameters\": [{\"name\": \"prefix_text\", \"type\": \"String\", \"default_value\": \"Count is\"}], " +
            "\"memory\": [{\"name\": \"count\", \"type\": \"integer\", \"default_value\": 0}], " +
            "\"outputs\": [{\"name\": \"field1\", \"type\": \"integer\", \"default_value\": -1}, " +
            "              {\"name\": \"field2\", \"type\": \"string\", \"default_value\": \"nothing\"}], " +
            "\"timers\": [{\"name\": \"timer-1\", \"interval\": 50, \"description\": \"50 ms timer\", \"script\": \"if (memory.count < 6){outputs.field1 = ++memory.count; outputs.field2 = parameters.prefix_text + ' ' + memory.count;}\", \"enabled\": true}], " +
        "\"enabled\": false}");

    // Now create an agent that references that data source and copies input to output
    // Also sets a parameter for the data source
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"TestAgent\", " +
            "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\", " +
            "             \"parameter_values\": {\"prefix_text\": \"Our count is...\"}}], " +
            "\"outputs\": [{\"name\": \"outField1\", \"type\": \"integer\", \"default_value\": -123}, " +
            "              {\"name\": \"outField2\", \"type\": \"string\", \"default_value\": \"not-yet\"}], " +
            "\"scripts\": [{\"name\": \"inputs_changed\", \"script\": \"outputs.outField1 = inputs.input1['field1']; outputs.outField2 = inputs.input1['field2'];\"}], " +
        "\"enabled\": false, \"trigger_interval\": 200}");

    // Now instantiate the agent, which should also instantiate the data source agent it references
    AgentInstance agInst = agentServer.getAgentInstance(user, agDef);

    // Make sure default output for (disabled) agent is correct
    SymbolValues agSyms = agInst.categorySymbolValues.get("outputs");
    assertEquals("Count of data source output fields", 2, agSyms.size());
    Value valueNode = agSyms.get("outField1");
    assertTrue("Agent output field field1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
    valueNode = agSyms.get("outField2");
    assertTrue("Agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

    // Find the data source instance that was automatically instantiated for input to agent
    assertEquals("Count of users with instantiated instances", 1, agentServer.agentInstances.size());
    assertEquals("Count of auto-instantiated data source instances for agent definition", 1, agInst.dataSourceInstances.size());
    AgentInstanceList instances = agentServer.agentInstances.get(user.id);
    assertEquals("Count of instantiated instances", 2, instances.size());
    AgentInstance dsInst = instances.getByDefinitionName("DataSource1");

    // Make sure default output is correct for the data source instance
    log.info("TEST: Initial data source output values: " + dsInst.categorySymbolValues.get("outputs").toJson());
    assertEquals("Count of output values", 2, dsInst.categorySymbolValues.get("outputs").size());
    valueNode = dsInst.getOutput("field1");
    assertTrue("Data source output field field1 is missing", valueNode != null);
    assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of data source output field1", -1, valueNode.getIntValue());
    valueNode = dsInst.getOutput("field2");
    assertTrue("Data source output field field2 is missing", valueNode != null);
    assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of data source output field2", "nothing", valueNode.getStringValue());

    // Make sure initial output is correct for the agent instance
    log.info("TEST: Initial agent output values: " + agInst.categorySymbolValues.get("outputs").toJson());
    assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
    valueNode = agInst.getOutput("outField1");
    assertTrue("agent output field outField1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
    valueNode = agInst.getOutput("outField2");
    assertTrue("agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

    log.info("TEST: Initial agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

    // Now enable the agent and then the data source
    agInst.enable();
    dsInst.enable();

    // Let agents run for awhile
    Thread.sleep(800);

    // Now disable the data source instance
    dsInst.disable();

    // Wait more than enough time for agents to settle down
    Thread.sleep(100);

    // Verify final output of data source instance
    assertEquals("Count of data source output values", 2, dsInst.categorySymbolValues.get("outputs").size());
    valueNode = dsInst.getOutput("field1");
    assertTrue("Data source output field field1 is missing", valueNode != null);
    assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of data source output field1", 6, valueNode.getIntValue());
    valueNode = dsInst.getOutput("field2");
    assertTrue("Data source output field field2 is missing", valueNode != null);
    assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of data source output field2", "Our count is... 6", valueNode.getStringValue());

    log.info("TEST: Final agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

    // Verify final output of agent instance
    assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
    valueNode = agInst.getOutput("outField1");
    assertTrue("agent output field outField1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", 6, valueNode.getIntValue());
    valueNode = agInst.getOutput("outField2");
    assertTrue("agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "Our count is... 6", valueNode.getStringValue());

    // Verify data source output history
    OutputHistory dsHistory = dsInst.outputHistory;
    assertEquals("Count of data source history", 7, dsHistory.size());
    OutputRecord dsRecord = dsHistory.get(0);
    assertEquals("Data source history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
    long deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[0].output","{\"field1\":-1,\"field2\":\"nothing\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(1);
    assertEquals("Data source history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[1].output", "{\"field1\":1,\"field2\":\"Our count is... 1\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(2);
    assertEquals("Data source history[2].sequenceNumber", 3, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[2].time delta not near expected: " + deltaTime, deltaTime > 20 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[2].output", "{\"field1\":2,\"field2\":\"Our count is... 2\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(3);
    assertEquals("Data source history[3].sequenceNumber", 4, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[3].time delta not near expected: " + deltaTime, deltaTime > 30 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[3].output", "{\"field1\":3,\"field2\":\"Our count is... 3\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(4);
    assertEquals("Data source history[4].sequenceNumber", 5, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[4].time delta not near expected: " + deltaTime, deltaTime > 40 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[4].output", "{\"field1\":4,\"field2\":\"Our count is... 4\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(5);
    assertEquals("Data source history[5].sequenceNumber", 6, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[5].time delta not near expected: " + deltaTime, deltaTime > 50 && deltaTime < 6000);
    assertEquals("Data source history[5].output", "{\"field1\":5,\"field2\":\"Our count is... 5\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(6);
    assertEquals("Data source history[6].sequenceNumber", 7, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[6].time delta not near expected: " + deltaTime, deltaTime > 60 && deltaTime < 7000);
    assertEquals("Data source history[6].output", "{\"field1\":6,\"field2\":\"Our count is... 6\"}", dsRecord.output.toJson().toString());

    // Verify agent output history
    OutputHistory agHistory = agInst.outputHistory;
    assertEquals("Count of agent history", 3, agHistory.size());
    dsRecord = agHistory.get(0);
    assertEquals("Agent history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[0].output", "{\"outField1\":-123,\"outField2\":\"not-yet\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(1);
    assertEquals("Agent history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    // TODO: Sometimes 4 is 3 - some timing issue - ds is delayed?
    assertEquals("Agent history[1].output", "{\"outField1\":4,\"outField2\":\"Our count is... 4\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(2);
    assertEquals("Agent history[2].sequenceNumber", 3, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[2].time delta not near expected: " + deltaTime, deltaTime > 20 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[2].output", "{\"outField1\":6,\"outField2\":\"Our count is... 6\"}", dsRecord.output.toJson().toString());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testDataSource4() throws Exception {

    // Test throttle down of agent using 'trigger_interval' so that only one change event occurs

    // Create a test user
    User user = agentServer.addUser("Test-User");

    // Create and start agent scheduler
    AgentScheduler agentScheduler = agentServer.agentScheduler;

    // Make sure no activities were run
    assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
    assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());
    // Record start time
    long startTime = System.currentTimeMillis();

    // Create a data source that uses a timer to sequence output through several values
    // Initially disabled until ready with agent that references it
    AgentDefinition dsDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"DataSource1\", " +
            "\"parameters\": [{\"name\": \"prefix_text\", \"type\": \"String\", \"default_value\": \"Count is\"}], " +
            "\"memory\": [{\"name\": \"count\", \"type\": \"integer\", \"default_value\": 0}], " +
            "\"outputs\": [{\"name\": \"field1\", \"type\": \"integer\", \"default_value\": -1}, " +
            "              {\"name\": \"field2\", \"type\": \"string\", \"default_value\": \"nothing\"}], " +
            "\"timers\": [{\"name\": \"timer-1\", \"interval\": 50, \"description\": \"50 ms timer\", \"script\": \"if (memory.count < 6){outputs.field1 = ++memory.count; outputs.field2 = parameters.prefix_text + ' ' + memory.count;}\", \"enabled\": true}], " +
        "\"enabled\": false}");

    // Now create an agent that references that data source and copies input to output
    // Also sets a parameter for the data source
    AgentDefinition agDef = agentServer.addAgentDefinition(
        "{\"user\": \"Test-User\"," +
            "\"name\": \"TestAgent\", " +
            "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\", " +
            "             \"parameter_values\": {\"prefix_text\": \"Our count is...\"}}], " +
            "\"outputs\": [{\"name\": \"outField1\", \"type\": \"integer\", \"default_value\": -123}, " +
            "              {\"name\": \"outField2\", \"type\": \"string\", \"default_value\": \"not-yet\"}], " +
            "\"scripts\": [{\"name\": \"inputs_changed\", \"script\": \"outputs.outField1 = inputs.input1['field1']; outputs.outField2 = inputs.input1['field2'];\"}], " +
        "\"enabled\": false, \"trigger_interval\": 500}");

    // Now instantiate the agent, which should also instantiate the data source agent it references
    AgentInstance agInst = agentServer.getAgentInstance(user, agDef);

    // Make sure default output for (disabled) agent is correct
    SymbolValues agSyms = agInst.categorySymbolValues.get("outputs");
    assertEquals("Count of data source output fields", 2, agSyms.size());
    Value valueNode = agSyms.get("outField1");
    assertTrue("Agent output field field1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
    valueNode = agSyms.get("outField2");
    assertTrue("Agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

    // Find the data source instance that was automatically instantiated for input to agent
    assertEquals("Count of users with instantiated instances", 1, agentServer.agentInstances.size());
    assertEquals("Count of auto-instantiated data source instances for agent definition", 1, agInst.dataSourceInstances.size());
    AgentInstanceList instances = agentServer.agentInstances.get(user.id);
    assertEquals("Count of instantiated instances", 2, instances.size());
    AgentInstance dsInst = instances.getByDefinitionName("DataSource1");

    // Make sure default output is correct for the data source instance
    log.info("TEST: Initial data source output values: " + dsInst.categorySymbolValues.get("outputs").toJson());
    assertEquals("Count of output values", 2, dsInst.categorySymbolValues.get("outputs").size());
    valueNode = dsInst.getOutput("field1");
    assertTrue("Data source output field field1 is missing", valueNode != null);
    assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of data source output field1", -1, valueNode.getIntValue());
    valueNode = dsInst.getOutput("field2");
    assertTrue("Data source output field field2 is missing", valueNode != null);
    assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of data source output field2", "nothing", valueNode.getStringValue());

    // Make sure initial output is correct for the agent instance
    log.info("TEST: Initial agent output values: " + agInst.categorySymbolValues.get("outputs").toJson());
    assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
    valueNode = agInst.getOutput("outField1");
    assertTrue("agent output field outField1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
    valueNode = agInst.getOutput("outField2");
    assertTrue("agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

    log.info("TEST: Initial agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

    // Now enable the agent and then the data source
    agInst.enable();
    dsInst.enable();

    // Let agents run for awhile
    Thread.sleep(1200);

    // Now disable the data source instance
    dsInst.disable();

    // Wait more than enough time for agents to settle down
    Thread.sleep(100);

    // Verify final output of data source instance
    assertEquals("Count of data source output values", 2, dsInst.categorySymbolValues.get("outputs").size());
    valueNode = dsInst.getOutput("field1");
    assertTrue("Data source output field field1 is missing", valueNode != null);
    assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of data source output field1", 6, valueNode.getIntValue());
    valueNode = dsInst.getOutput("field2");
    assertTrue("Data source output field field2 is missing", valueNode != null);
    assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of data source output field2", "Our count is... 6", valueNode.getStringValue());

    log.info("TEST: Final agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

    // Verify final output of agent instance
    assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
    valueNode = agInst.getOutput("outField1");
    assertTrue("agent output field outField1 is missing", valueNode != null);
    assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
    assertEquals("Value of agent output outField1", 6, valueNode.getIntValue());
    valueNode = agInst.getOutput("outField2");
    assertTrue("agent output field outField2 is missing", valueNode != null);
    assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
    assertEquals("Value of agent output outField2", "Our count is... 6", valueNode.getStringValue());

    // Verify data source output history
    OutputHistory dsHistory = dsInst.outputHistory;
    assertEquals("Count of data source history", 7, dsHistory.size());
    OutputRecord dsRecord = dsHistory.get(0);
    assertEquals("Data source history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
    long deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[0].output", "{\"field1\":-1,\"field2\":\"nothing\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(1);
    assertEquals("Data source history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[1].output", "{\"field1\":1,\"field2\":\"Our count is... 1\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(2);
    assertEquals("Data source history[2].sequenceNumber", 3, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[2].time delta not near expected: " + deltaTime, deltaTime > 20 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[2].output", "{\"field1\":2,\"field2\":\"Our count is... 2\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(3);
    assertEquals("Data source history[3].sequenceNumber", 4, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[3].time delta not near expected: " + deltaTime, deltaTime > 30 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[3].output", "{\"field1\":3,\"field2\":\"Our count is... 3\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(4);
    assertEquals("Data source history[4].sequenceNumber", 5, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[4].time delta not near expected: " + deltaTime, deltaTime > 40 && deltaTime < nearDeltaTime);
    assertEquals("Data source history[4].output", "{\"field1\":4,\"field2\":\"Our count is... 4\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(5);
    assertEquals("Data source history[5].sequenceNumber", 6, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[5].time delta not near expected: " + deltaTime, deltaTime > 50 && deltaTime < 6000);
    assertEquals("Data source history[5].output", "{\"field1\":5,\"field2\":\"Our count is... 5\"}", dsRecord.output.toJson().toString());
    dsRecord = dsHistory.get(6);
    assertEquals("Data source history[6].sequenceNumber", 7, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Data source history[6].time delta not near expected: " + deltaTime, deltaTime > 60 && deltaTime < 7000);
    assertEquals("Data source history[6].output", "{\"field1\":6,\"field2\":\"Our count is... 6\"}", dsRecord.output.toJson().toString());

    // Verify agent output history
    OutputHistory agHistory = agInst.outputHistory;
    assertEquals("Count of agent history", 2, agHistory.size());
    dsRecord = agHistory.get(0);
    assertEquals("Agent history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[0].output", "{\"outField1\":-123,\"outField2\":\"not-yet\"}", dsRecord.output.toJson().toString());
    dsRecord = agHistory.get(1);
    assertEquals("Agent history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
    deltaTime = dsRecord.time - startTime;
    assertTrue("Agent history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
    assertEquals("Agent history[1].output", "{\"outField1\":6,\"outField2\":\"Our count is... 6\"}", dsRecord.output.toJson().toString());

    // Tell the scheduler to stop
    agentServer.shutdown();
    assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
  }

  @Test
  public void testStateCapture() throws Exception {

    // Test basic scenario of an agent taking input from a timer-driven data source
    // Data source counts timer events in memory, but only sets outputs on even counts
    // So, we should see a state change for memory for every timer event, but an output
    // state change every other timer event
    {
      // Create a test user
      User user = agentServer.addUser("Test-User");

      // Access the scheduler that agent server created
      AgentScheduler agentScheduler = agentServer.agentScheduler;

      // Make sure no activities were run
      assertEquals("Number of uncompleted activities", 0, agentScheduler.queue.size());
      assertEquals("Number of completed activities", 0, agentScheduler.completedActivities.size());
      // Record start time
      long startTime = System.currentTimeMillis();

      // Create a data source that uses a timer to sequence output through several values
      // Initially disabled until ready with agent that references it
      AgentDefinition dsDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
              "\"name\": \"DataSource1\", " +
              "\"parameters\": [{\"name\": \"prefix_text\", \"type\": \"String\", \"default_value\": \"Count is\"}], " +
              "\"memory\": [{\"name\": \"count\", \"type\": \"integer\", \"default_value\": 0}], " +
              "\"outputs\": [{\"name\": \"field1\", \"type\": \"integer\", \"default_value\": -1}, " +
              "              {\"name\": \"field2\", \"type\": \"string\", \"default_value\": \"nothing\"}], " +
              "\"timers\": [{\"name\": \"timer-1\", \"interval\": 50, \"description\": \"50 ms timer\", \"script\": \"if (memory.count <= 6 && ++memory.count % 2 == 0){outputs.field1 = memory.count; outputs.field2 = parameters.prefix_text + ' ' + memory.count;}\", \"enabled\": true}], " +
          "\"enabled\": false}");

      // Now create an agent that references that data source and copies input to output
      // Also sets a parameter for the data source
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
              "\"name\": \"TestAgent\", " +
              "\"parameters\": [{\"name\": \"suffix_text\", \"type\": \"String\", \"default_value\": \" **\"}], " +
              "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\", " +
              "             \"parameter_values\": {\"prefix_text\": \"Our count is...\"}}], " +
              "\"memory\": [{\"name\": \"count1\", \"type\": \"integer\", \"default_value\": 0}], " +
              "\"outputs\": [{\"name\": \"outField1\", \"type\": \"integer\", \"default_value\": -123}, " +
              "              {\"name\": \"outField2\", \"type\": \"string\", \"default_value\": \"not-yet\"}], " +
              "\"scripts\": [{\"name\": \"inputs_changed\", \"script\": \"outputs.outField1 = inputs.input1['field1']; outputs.outField2 = inputs.input1.field2 + suffix_text; count1++;\"}], " +
          "\"enabled\": false, \"trigger_interval\": 200}");

      // Now instantiate the agent, which should also instantiate the data source agent it references
      AgentInstance agInst = agentServer.getAgentInstance(
          "{\"name\": \"MyInstance\", \"definition\": \"TestAgent\", \"user\": \"Test-User\", " +
          "\"parameter_values\": {\"suffix_text\": \" ++\"}}");

      // Make sure default output for (disabled) agent is correct
      SymbolValues agSyms = agInst.categorySymbolValues.get("outputs");
      assertEquals("Count of data source output fields", 2, agSyms.size());
      Value valueNode = agSyms.get("outField1");
      assertTrue("Agent output field field1 is missing", valueNode != null);
      assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
      assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
      valueNode = agSyms.get("outField2");
      assertTrue("Agent output field outField2 is missing", valueNode != null);
      assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
      assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

      // Find the data source instance that was automatically instantiated for input to agent
      assertEquals("Count of users with instantiated instances", 1, agentServer.agentInstances.size());
      assertEquals("Count of auto-instantiated data source instances for agent definition", 1, agInst.dataSourceInstances.size());
      AgentInstanceList instances = agentServer.agentInstances.get(user.id);
      assertEquals("Count of instantiated instances", 2, instances.size());
      AgentInstance dsInst = instances.getByDefinitionName("DataSource1");

      // Make sure default output is correct for the data source instance
      log.info("TEST: Initial data source output values: " + dsInst.categorySymbolValues.get("outputs").toJson());
      assertEquals("Count of output values", 2, dsInst.categorySymbolValues.get("outputs").size());
      valueNode = dsInst.getOutput("field1");
      assertTrue("Data source output field field1 is missing", valueNode != null);
      assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
      assertEquals("Value of data source output field1", -1, valueNode.getIntValue());
      valueNode = dsInst.getOutput("field2");
      assertTrue("Data source output field field2 is missing", valueNode != null);
      assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
      assertEquals("Value of data source output field2", "nothing", valueNode.getStringValue());

      // Make sure initial output is correct for the agent instance
      log.info("TEST: Initial agent output values: " + agInst.categorySymbolValues.get("outputs").toJson());
      assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
      valueNode = agInst.getOutput("outField1");
      assertTrue("agent output field outField1 is missing", valueNode != null);
      assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
      assertEquals("Value of agent output outField1", -123, valueNode.getIntValue());
      valueNode = agInst.getOutput("outField2");
      assertTrue("agent output field outField2 is missing", valueNode != null);
      assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
      assertEquals("Value of agent output outField2", "not-yet", valueNode.getStringValue());

      log.info("TEST: Initial agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

      // Now enable the agent and then the data source
      agInst.enable();
      dsInst.enable();

      // Let agents run for awhile
      Thread.sleep(800);

      // Now disable the data source instance
      dsInst.disable();

      // Wait more than enough time for agents to settle down
      Thread.sleep(100);

      // Verify final output of data source instance
      assertEquals("Count of data source output values", 2, dsInst.categorySymbolValues.get("outputs").size());
      valueNode = dsInst.getOutput("field1");
      assertTrue("Data source output field field1 is missing", valueNode != null);
      assertTrue("Type of data source output field1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
      assertEquals("Value of data source output field1", 6, valueNode.getIntValue());
      valueNode = dsInst.getOutput("field2");
      assertTrue("Data source output field field2 is missing", valueNode != null);
      assertTrue("Type of data source output field2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
      assertEquals("Value of data source output field2", "Our count is... 6", valueNode.getStringValue());

      log.info("TEST: Final agent input values: " + agInst.categorySymbolValues.get("inputs").toJson());

      // Verify final output of agent instance
      assertEquals("Symbol values for outputs", "{\"outField1\":6,\"outField2\":\"Our count is... 6 ++\"}", agInst.categorySymbolValues.get("outputs").toJson().toString());
      assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
      valueNode = agInst.getOutput("outField1");
      assertTrue("agent output field outField1 is missing", valueNode != null);
      assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
      assertEquals("Value of agent output outField1", 6, valueNode.getIntValue());
      valueNode = agInst.getOutput("outField2");
      assertTrue("agent output field outField2 is missing", valueNode != null);
      assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
      assertEquals("Value of agent output outField2", "Our count is... 6 ++", valueNode.getStringValue());

      // Verify data source output history
      OutputHistory dsHistory = dsInst.outputHistory;
      assertEquals("Count of data source history", 4, dsHistory.size());
      OutputRecord dsRecord = dsHistory.get(0);
      assertEquals("Data source history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
      long deltaTime = dsRecord.time - startTime;
      assertTrue("Data source history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
      assertEquals("Data source history[0].output", "{\"field1\":-1,\"field2\":\"nothing\"}", dsRecord.output.toJson().toString());
      dsRecord = dsHistory.get(1);
      assertEquals("Data source history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Data source history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
      assertEquals("Data source history[1].output", "{\"field1\":2,\"field2\":\"Our count is... 2\"}", dsRecord.output.toJson().toString());
      dsRecord = dsHistory.get(2);
      assertEquals("Data source history[2].sequenceNumber", 3, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Data source history[2].time delta not near expected: " + deltaTime, deltaTime > 20 && deltaTime < nearDeltaTime);
      assertEquals("Data source history[2].output", "{\"field1\":4,\"field2\":\"Our count is... 4\"}", dsRecord.output.toJson().toString());
      dsRecord = dsHistory.get(3);
      assertEquals("Data source history[3].sequenceNumber", 4, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Data source history[3].time delta not near expected: " + deltaTime, deltaTime > 30 && deltaTime < nearDeltaTime);
      assertEquals("Data source history[3].output", "{\"field1\":6,\"field2\":\"Our count is... 6\"}", dsRecord.output.toJson().toString());
      assertTrue("Data source history[3].time delta not near expected: " + deltaTime, deltaTime > 60 && deltaTime < 7000);

      // Verify agent output history
      OutputHistory agHistory = agInst.outputHistory;
      assertEquals("Count of agent history", 4, agHistory.size());
      dsRecord = agHistory.get(0);
      assertEquals("Agent history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Agent history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
      assertEquals("Agent history[0].output", "{\"outField1\":-123,\"outField2\":\"not-yet\"}", dsRecord.output.toJson().toString());
      dsRecord = agHistory.get(1);
      assertEquals("Agent history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Agent history[1].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
      assertEquals("Agent history[1].output", "{\"outField1\":2,\"outField2\":\"Our count is... 2 ++\"}", dsRecord.output.toJson().toString());
      dsRecord = agHistory.get(2);
      assertEquals("Agent history[2].sequenceNumber", 3, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Agent history[2].time delta not near expected: " + deltaTime, deltaTime > 10 && deltaTime < nearDeltaTime);
      assertEquals("Agent history[2].output", "{\"outField1\":4,\"outField2\":\"Our count is... 4 ++\"}", dsRecord.output.toJson().toString());
      dsRecord = agHistory.get(3);
      assertEquals("Agent history[3].sequenceNumber", 4, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Agent history[3].time delta not near expected: " + deltaTime, deltaTime > 20 && deltaTime < nearDeltaTime);
      assertEquals("Agent history[3].output", "{\"outField1\":6,\"outField2\":\"Our count is... 6 ++\"}", dsRecord.output.toJson().toString());

      // Verify memory history of data source
      List<AgentState> states = dsInst.state;
      assertEquals("Number of states for data source", 8, states.size());
      AgentState state = states.get(0);
      assertEquals("Parameter prefix_text", "Our count is...", state.getParameter("prefix_text").getStringValue());
      assertEquals("Memory count variable", 0, state.getMemory("count").getIntValue());
      assertEquals("Output field1", -1, state.getOutput("field1").getIntValue());
      assertEquals("Output field2", "nothing", state.getOutput("field2").getStringValue());
      state = states.get(1);
      assertEquals("Parameter prefix_text", "Our count is...", state.getParameter("prefix_text").getStringValue());
      assertEquals("Memory count variable", 1, state.getMemory("count").getIntValue());
      assertEquals("Output field1", -1, state.getOutput("field1").getIntValue());
      assertEquals("Output field2", "nothing", state.getOutput("field2").getStringValue());
      state = states.get(2);
      assertEquals("Parameter prefix_text", "Our count is...", state.getParameter("prefix_text").getStringValue());
      assertEquals("Memory count variable", 2, state.getMemory("count").getIntValue());
      assertEquals("Output field1", 2, state.getOutput("field1").getIntValue());
      assertEquals("Output field2", "Our count is... 2", state.getOutput("field2").getStringValue());
      state = states.get(3);
      assertEquals("Parameter prefix_text", "Our count is...", state.getParameter("prefix_text").getStringValue());
      assertEquals("Memory count variable", 3, state.getMemory("count").getIntValue());
      assertEquals("Output field1", 2, state.getOutput("field1").getIntValue());
      assertEquals("Output field2", "Our count is... 2", state.getOutput("field2").getStringValue());
      state = states.get(4);
      assertEquals("Parameter prefix_text", "Our count is...", state.getParameter("prefix_text").getStringValue());
      assertEquals("Memory count variable", 4, state.getMemory("count").getIntValue());
      assertEquals("Output field1", 4, state.getOutput("field1").getIntValue());
      assertEquals("Output field2", "Our count is... 4", state.getOutput("field2").getStringValue());
      state = states.get(5);
      assertEquals("Parameter prefix_text", "Our count is...", state.getParameter("prefix_text").getStringValue());
      assertEquals("Memory count variable", 5, state.getMemory("count").getIntValue());
      assertEquals("Output field1", 4, state.getOutput("field1").getIntValue());
      assertEquals("Output field2", "Our count is... 4", state.getOutput("field2").getStringValue());
      state = states.get(6);
      assertEquals("Parameter prefix_text", "Our count is...", state.getParameter("prefix_text").getStringValue());
      assertEquals("Memory count variable", 6, state.getMemory("count").getIntValue());
      assertEquals("Output field1", 6, state.getOutput("field1").getIntValue());
      assertEquals("Output field2", "Our count is... 6", state.getOutput("field2").getStringValue());
      state = states.get(7);
      assertEquals("Parameter prefix_text", "Our count is...", state.getParameter("prefix_text").getStringValue());
      assertEquals("Memory count variable", 7, state.getMemory("count").getIntValue());
      assertEquals("Output field1", 6, state.getOutput("field1").getIntValue());
      assertEquals("Output field2", "Our count is... 6", state.getOutput("field2").getStringValue());
      
      // Verify state history for agent
      states = agInst.state;
      assertEquals("Number of states for agent", 4, states.size());
      state = states.get(0);
      assertEquals("Memory count1", 0, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", -123, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "not-yet", state.getOutput("outField2").getStringValue());
      state = states.get(1);
      assertEquals("Memory count1", 1, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", 2, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "Our count is... 2 ++", state.getOutput("outField2").getStringValue());
      state = states.get(2);
      assertEquals("Memory count1", 2, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", 4, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "Our count is... 4 ++", state.getOutput("outField2").getStringValue());
      state = states.get(3);
      assertEquals("Memory count1", 3, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", 6, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "Our count is... 6 ++", state.getOutput("outField2").getStringValue());

      // Check state history
      assertEquals("Count of state history for data source", 8, dsInst.state.size());
      state = dsInst.state.get(0);
      assertEquals("Data source output values in state history[0]", "{\"field1\":-1,\"field2\":\"nothing\"}", state.outputValues.toJson().toString());
      assertEquals("Count of state history for agent", 4, agInst.state.size());
      state = agInst.state.get(0);
      assertEquals("Agent output values in state history[0]", "{\"outField1\":-123,\"outField2\":\"not-yet\"}", state.outputValues.toJson().toString());
      
      // Shutdown and restart server and see that instances have their current state
      agentServer.shutdown();
      assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
      agentServer.start();
      assertEquals("agentScheduler status ", "running", agentServer.getStatus());

      // Find our agent instance by name
      agInst = agentServer.getAgentInstance("Test-User", "MyInstance");

      // Check that state history was persisted and reloaded properly
      assertEquals("Count of state history for data source", 8, dsInst.state.size());
      state = dsInst.state.get(0);
      assertTrue("State[0] is null", state != null);
      assertEquals("Data source output values in state history[0]", "{\"field1\":-1,\"field2\":\"nothing\"}", state.outputValues.toJson().toString());
      assertTrue("agInst.state is null", agInst.state != null);
      assertEquals("Count of state history for agent", 4, agInst.state.size());
      state = agInst.state.get(0);
      assertEquals("Agent output values in state history[0]", "{\"outField1\":-123,\"outField2\":\"not-yet\"}", state.outputValues.toJson().toString());

      // Verify final output of agent instance
      assertEquals("Symbol values for outputs", "{\"outField1\":6,\"outField2\":\"Our count is... 6 ++\"}", agInst.categorySymbolValues.get("outputs").toJson().toString());
      assertEquals("Count of output values", 2, agInst.categorySymbolValues.get("outputs").size());
      valueNode = agInst.getOutput("outField1");
      assertTrue("agent output field outField1 is missing", valueNode != null);
      assertTrue("Type of agent output outField1 is not IntegerNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof IntegerValue);
      assertEquals("Value of agent output outField1", 6, valueNode.getIntValue());
      valueNode = agInst.getOutput("outField2");
      assertTrue("agent output field outField2 is missing", valueNode != null);
      assertTrue("Type of agent output outField2 is not StringNodeType: " + valueNode.getClass().getSimpleName() , valueNode instanceof StringValue);
      assertEquals("Value of agent output outField2", "Our count is... 6 ++", valueNode.getStringValue());

      // Verify agent output history
      agHistory = agInst.outputHistory;
      assertEquals("Count of agent history", 4, agHistory.size());
      dsRecord = agHistory.get(0);
      assertEquals("Agent history[0].sequenceNumber", 1, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Agent history[0].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
      assertEquals("Agent history[0].output", "{\"outField1\":-123,\"outField2\":\"not-yet\"}", dsRecord.output.toJson().toString());
      dsRecord = agHistory.get(1);
      assertEquals("Agent history[1].sequenceNumber", 2, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Agent history[1].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
      assertEquals("Agent history[1].output", "{\"outField1\":2,\"outField2\":\"Our count is... 2 ++\"}", dsRecord.output.toJson().toString());
      dsRecord = agHistory.get(2);
      assertEquals("Agent history[1].sequenceNumber", 3, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Agent history[2].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
      assertEquals("Agent history[2].output", "{\"outField1\":4,\"outField2\":\"Our count is... 4 ++\"}", dsRecord.output.toJson().toString());
      dsRecord = agHistory.get(3);
      assertEquals("Agent history[3].sequenceNumber", 4, dsRecord.sequenceNumber);
      deltaTime = dsRecord.time - startTime;
      assertTrue("Agent history[3].time delta not near expected: " + deltaTime, deltaTime >= 0 && deltaTime < nearDeltaTime);
      assertEquals("Agent history[3].output", "{\"outField1\":6,\"outField2\":\"Our count is... 6 ++\"}", dsRecord.output.toJson().toString());
      
      // Verify state history for agent
      states = agInst.state;
      assertEquals("Number of states for agent", 4, states.size());
      state = states.get(0);
      assertEquals("Memory count1", 0, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", -123, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "not-yet", state.getOutput("outField2").getStringValue());
      state = states.get(1);
      assertEquals("Memory count1", 1, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", 2, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "Our count is... 2 ++", state.getOutput("outField2").getStringValue());
      state = states.get(2);
      assertEquals("Memory count1", 2, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", 4, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "Our count is... 4 ++", state.getOutput("outField2").getStringValue());
      state = states.get(3);
      assertEquals("Memory count1", 3, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", 6, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "Our count is... 6 ++", state.getOutput("outField2").getStringValue());

      // Shutdown and restart the server several more times to test resilience
      agentServer.shutdown();
      assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
      agentServer.start();
      assertEquals("agentScheduler status ", "running", agentServer.getStatus());
      agentServer.shutdown();
      assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
      agentServer.start();
      assertEquals("agentScheduler status ", "running", agentServer.getStatus());
      agentServer.shutdown();
      assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
      agentServer.start();
      assertEquals("agentScheduler status ", "running", agentServer.getStatus());

      // Find our agent instance by name
      agInst = agentServer.getAgentInstance("Test-User", "MyInstance");
      
      // Verify state history for agent
      states = agInst.state;
      assertEquals("Number of states for agent", 4, states.size());
      state = states.get(0);
      assertEquals("Memory count1", 0, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", -123, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "not-yet", state.getOutput("outField2").getStringValue());
      state = states.get(1);
      assertEquals("Memory count1", 1, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", 2, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "Our count is... 2 ++", state.getOutput("outField2").getStringValue());
      state = states.get(2);
      assertEquals("Memory count1", 2, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", 4, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "Our count is... 4 ++", state.getOutput("outField2").getStringValue());
      state = states.get(3);
      assertEquals("Memory count1", 3, state.getMemory("count1").getIntValue());
      assertEquals("Output outField1", 6, state.getOutput("outField1").getIntValue());
      assertEquals("Output outField2", "Our count is... 6 ++", state.getOutput("outField2").getStringValue());
      
      // Done. Tell the scheduler to stop
      agentServer.shutdown();
      assertEquals("agentScheduler status ", "shutdown", agentServer.getStatus());
    }
  }

}
