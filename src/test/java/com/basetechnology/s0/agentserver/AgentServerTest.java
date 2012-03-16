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

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.basetechnology.s0.agentserver.AgentCondition;
import com.basetechnology.s0.agentserver.AgentConditionStatus;
import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.AgentTimer;
import com.basetechnology.s0.agentserver.AgentTimerStatus;
import com.basetechnology.s0.agentserver.DataSourceReference;
import com.basetechnology.s0.agentserver.ScriptDefinition;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.field.Field;
import com.basetechnology.s0.agentserver.script.intermediate.FloatTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.IntegerTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.StringTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.runtime.value.FloatValue;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.MapValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;
import com.basetechnology.s0.agentserver.util.NameValue;
import com.basetechnology.s0.agentserver.util.NameValueList;

import static org.junit.Assert.*;

public class AgentServerTest {
  static final Logger log = Logger.getLogger(AgentServerTest.class);

  AgentAppServer agentAppServer = null;
  AgentServer agentServer;

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
    File pf = new File(AgentServer.defaultPersistencePath);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServer.defaultPersistencePath, ! pf.exists());

    agentAppServer = new AgentAppServer();
    agentServer = agentAppServer.agentServer;
  }

  @After
  public void tearDown() throws Exception {
    if (agentAppServer != null){
      agentAppServer.stop();
    }
    File pf = new File(AgentServer.defaultPersistencePath);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServer.defaultPersistencePath, ! pf.exists());
    agentAppServer = null;
    agentServer = null;
  }

  @Test
  public void testAgentDefinition() throws Exception {
    // Test creation of an empty agent definition
    try {
      AgentDefinition agent = agentServer.addAgentDefinition((String)null);
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent definition user id ('user') is missing", message);
    }

    try {
      AgentDefinition agent = agentServer.addAgentDefinition("");
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent definition user id ('user') is missing", message);
    }

    try {
      AgentDefinition agent = agentServer.addAgentDefinition("    ");
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent definition user id ('user') is missing", message);
    }

    // Test bad syntax agent definition
    try {
      AgentDefinition agent = agentServer.addAgentDefinition("  {junk  ");
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "JSON parsing exception: Expected a ':' after a key at character 8", message);
    }

    // Test junk keys in agent definition
    try {
      AgentDefinition agent = agentServer.addAgentDefinition("  {\"junk\": \"junk\"}  ");
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent definition user id ('user') is missing", message);
    }

    // Test non-existent user name
    try {
      AgentDefinition agent = agentServer.addAgentDefinition("{\"user\": \"test-user\", \"name\": \"testAgentName\"}");
      assertTrue("Nonexistent user name not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent definition user id does not exist: 'test-user'", message);
    }

    // Now add a test user
    agentServer.addUser("Test-User");
    NameValueList<User> userNameValueList = agentServer.users;
    assertTrue("User name table is missing", userNameValueList != null);
    assertEquals("Number of users", 1, userNameValueList.size());
    NameValue<User> userNameValue = userNameValueList.get(0);
    assertTrue("User NameValue is missing", userNameValue != null);
    String userName = userNameValue.name;
    assertTrue("User name is missing", userName != null);
    assertEquals("User name", "Test-User", userName);
    User user = userNameValue.value;
    assertTrue("User object is missing", user != null);
    userName = user.id;
    assertTrue("User name is missing", userName != null);
    assertEquals("User name", "Test-User", userName);

    // Make sure same user can be added more than once without any harm or effect
    agentServer.addUser("Test-User");
    userNameValueList = agentServer.users;
    assertTrue("User name table is missing", userNameValueList != null);
    assertEquals("Number of users", 1, userNameValueList.size());
    userNameValue = userNameValueList.get(0);
    assertTrue("User NameValue is missing", userNameValue != null);
    userName = userNameValue.name;
    assertTrue("User name is missing", userName != null);
    assertEquals("User name", "Test-User", userName);
    user = userNameValue.value;
    assertTrue("User object is missing", user != null);
    userName = user.id;
    assertTrue("User name is missing", userName != null);
    assertEquals("User name", "Test-User", userName);

    // Test for an existing user name
    AgentDefinition agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName\"}");
    assertTrue("Agent definition not created", agent != null);
    assertEquals("Number of agent definitions for user", 1, agentServer.agentDefinitions.get("Test-User").size());

    // Test for an existing agent definition name
    try {
      agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName\"}");
      assertTrue("Agent definition should not have been created", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent definition name already exists: 'testAgentName'", message);
    }

    // Test delete for an agent definition
    agentServer.removeAgentDefinition("Test-User", "testAgentName");
    assertEquals("Number of agent definitions for user", 0, agentServer.agentDefinitions.get("Test-User").size());

    // Test for using empty scripts list
    agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName\", \"scripts\":[]}");
    assertTrue("Agent definition not created", agent != null);
    assertEquals("Number of agent definitions for user", 1, agentServer.agentDefinitions.get("Test-User").size());
    assertEquals("Number of scripts for agent", 0, agent.scripts.size());
    assertEquals("Agent definition name", "testAgentName", agent.name);

    agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName2\", \"scripts\":[{}]}");
    assertTrue("Agent definition not created", agent != null);
    assertEquals("Number of agent definitions for user", 2, agentServer.agentDefinitions.get("Test-User").size());
    assertEquals("Agent definition name", "testAgentName2", agent.name);
    assertEquals("Number of scripts for agent", 0, agent.scripts.size());

    // Test for creating 'init' script
    agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName3\", \"scripts\":[{\"name\": \"init\"}]}");
    assertTrue("Agent definition not created", agent != null);
    assertEquals("Number of agent definitions for user", 3, agentServer.agentDefinitions.get("Test-User").size());
    assertEquals("Agent definition name", "testAgentName3", agent.name);
    assertEquals("Number of scripts for agent", 1, agent.scripts.size());
    ScriptDefinition script = agent.scripts.get(0).value;
    String scriptName = script.name;
    assertTrue("Script name is missing", scriptName != null);
    assertEquals("Script name", "init", scriptName);
    String scriptDescription = script.description;
    assertTrue("Script description is missing", scriptDescription != null);
    assertEquals("Script description", "", scriptDescription);
    String scriptScript = script.script;
    assertTrue("Script body is missing", scriptScript != null);
    assertEquals("Script body", "", scriptScript);

    agentServer.clearAgentDefinitions("Test-User");
    assertEquals("Number of agent definitions for user", 0, agentServer.agentDefinitions.get("Test-User").size());
    agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName3\", \"scripts\":[{\"name\": \"init\", \"description\": \"A test script\", \"script\": \"return 2+2;\"}]}");
    assertTrue("Agent definition not created", agent != null);
    assertEquals("Number of agent definitions for user", 1, agentServer.agentDefinitions.get("Test-User").size());
    assertEquals("Agent definition name", "testAgentName3", agent.name);
    assertEquals("Number of scripts for agent", 1, agent.scripts.size());
    script = agent.scripts.get(0).value;
    scriptName = script.name;
    assertTrue("Script name is missing", scriptName != null);
    assertEquals("Script name", "init", scriptName);
    scriptDescription = script.description;
    assertTrue("Script description is missing", scriptDescription != null);
    assertEquals("Script description", "A test script", scriptDescription);
    scriptScript = script.script;
    assertTrue("Script body is missing", scriptScript != null);
    assertEquals("Script body", "return 2+2;", scriptScript);

    // Check default values
    assertEquals("Agent definition description", "", agent.description);
    assertEquals("Default reporting interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL, agent.reportingInterval);
    assertEquals("Number of parameters", 0, agent.parameters.size());
    assertEquals("Number of inputs", 0, agent.inputs.size());
    assertEquals("Number of memory fields", 0, agent.memory.size());
    assertEquals("Number of output fields", 0, agent.outputs.size());
    
    // Check description and reporting interval
    agentServer.clearAgentDefinitions("Test-User");
    assertEquals("Number of agent definitions for user", 0, agentServer.agentDefinitions.get("Test-User").size());
    agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName3\", \"scripts\":[{\"name\": \"init\", \"description\": \"A test script\", \"script\": \"return 2+2;\"}], \"reporting_interval\": 3000, \"description\": \"Test agent definition\"}");
    assertTrue("Agent definition not created", agent != null);
    assertEquals("Number of agent definitions for user", 1, agentServer.agentDefinitions.get("Test-User").size());
    assertEquals("Agent definition name", "testAgentName3", agent.name);
    assertEquals("Number of scripts for agent", 1, agent.scripts.size());
    script = agent.scripts.get(0).value;
    scriptName = script.name;
    assertTrue("Script name is missing", scriptName != null);
    assertEquals("Script name", "init", scriptName);
    scriptDescription = script.description;
    assertTrue("Script description is missing", scriptDescription != null);
    assertEquals("Script description", "A test script", scriptDescription);
    scriptScript = script.script;
    assertTrue("Script body is missing", scriptScript != null);
    assertEquals("Script body", "return 2+2;", scriptScript);
    assertEquals("Agent definition description", "Test agent definition", agent.description);
    assertEquals("Default reporting interval", 3000, agent.reportingInterval);
    assertEquals("Number of parameters", 0, agent.parameters.size());
    assertEquals("Number of inputs", 0, agent.inputs.size());
    assertEquals("Number of scratchpad fields", 0, agent.scratchpad.size());
    assertEquals("Number of memory fields", 0, agent.memory.size());
    assertEquals("Number of output fields", 0, agent.outputs.size());
  }

  @Test
  public void testAgentInstance() throws Exception {
    // Test creation of an empty agent instance
    try {
      AgentInstance agent = agentServer.getAgentInstance((String)null);
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent instance user id ('user') is missing", message);
    }

    try {
      AgentInstance agent = agentServer.getAgentInstance("");
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent instance user id ('user') is missing", message);
    }

    try {
      AgentInstance agent = agentServer.getAgentInstance("    ");
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent instance user id ('user') is missing", message);
    }

    // Test bad syntax agent definition
    try {
      AgentInstance agent = agentServer.getAgentInstance("  {junk  ");
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "JSON parsing exception: Expected a ':' after a key at character 8", message);
    }

    // Test junk keys in agent definition
    try {
      AgentInstance agent = agentServer.getAgentInstance("  {\"junk\": \"junk\"}  ");
      assertTrue("Creation of empty agent not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent instance user id ('user') is missing", message);
    }

    // Test non-existent user name
    try {
      AgentInstance agent = agentServer.getAgentInstance("{\"user\": \"test-user\", \"name\": \"testAgentName\"}");
      assertTrue("Nonexistent user name not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent instance user id does not exist: 'test-user'", message);
    }

    // Now add a test user
    agentServer.addUser("Test-User");

    // Test for nonexistent agent definition name
    try {
      AgentInstance agent = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName\"}");
      assertTrue("Nonexistent agent definition name not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent instance definition name ('definition') is missing for user 'Test-User'", message);
    }
    
    // Test for an existing user name but 'definition' is missing
    try {
      AgentInstance agent = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName\"}");
      assertTrue("Nonexistent agent definition name not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent instance definition name ('definition') is missing for user 'Test-User'", message);
    }

    // Test for nonexistent agent definition reference
    try {
      AgentInstance agent = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName\", \"definition\": \"ag1\"}");
      assertTrue("Nonexistent agent definition name not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent instance 'testAgentName' references agent definition 'ag1' which does not exist for user 'Test-User'", message);
    }

    // Same test but with a non-referenced agent definition
    try {
      AgentDefinition agentDefinition = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName\", \"scripts\":[]}");
      AgentInstance agent = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName\", \"definition\": \"ag1\"}");
      assertTrue("Nonexistent agent definition name not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent instance 'testAgentName' references agent definition 'ag1' which does not exist for user 'Test-User'", message);
    }

    // Now created the referenced agent definition before referencing it
    {
      AgentDefinition agentDefinition = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"ag1\", \"scripts\":[]}");
      AgentInstance agent = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName\", \"definition\": \"ag1\"}");
      assertTrue("Agent instance not created", agent != null);
      assertEquals("Number of agent definitions for user", 2, agentServer.agentDefinitions.get("Test-User").size());
      assertEquals("Number of agent instances for user", 1, agentServer.agentInstances.get("Test-User").size());
    }

    // Test referencing parameter names in instance that are not defined in definition
    try {
      AgentDefinition agentDefinition = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"ag2\", \"scripts\":[]}");
      AgentInstance agent = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName2\", \"definition\": \"ag2\", \"parameter_values\": {\"x\": 123, \"y\": 4.56, \"z\": \"abc\"}}");
      assertTrue("Nonexistent parameter names of agent definition name not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Parameter names for agent instance testAgentName2 are not defined for referenced agent definition ag2: x, y, z", message);
    }

    try {
      AgentDefinition agentDefinition = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"ag3\", \"scripts\":[], \"parameters\": [{\"name\": \"y\", \"type\": \"string\"}]}");
      AgentInstance agent = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName3\", \"definition\": \"ag3\", \"parameter_values\": {\"x\": 123, \"y\": 4.56, \"z\": \"abc\"}}");
      assertTrue("Nonexistent parameter names of agent definition name not detected", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Parameter names for agent instance testAgentName3 are not defined for referenced agent definition ag3: x, z", message);
    }
    
    // Test for an existing agent definition name
    try {
      AgentDefinition agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName\"}");
      assertTrue("Agent definition should not have been created", agent == null);
    } catch (AgentServerException e){
      String message = e.getMessage();
      assertEquals("Thrown exception message", "Agent definition name already exists: 'testAgentName'", message);
    }

    // Test delete for an agent definition
    {
      agentServer.removeAgentDefinition("Test-User", "testAgentName");
      assertEquals("Number of agent definitions for user", 3, agentServer.agentDefinitions.get("Test-User").size());
    }

    // Test for using empty scripts list
    {
      AgentDefinition agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName\", \"scripts\":[]}");
      assertTrue("Agent definition not created", agent != null);
      assertEquals("Number of agent definitions for user", 4, agentServer.agentDefinitions.get("Test-User").size());
      assertEquals("Number of scripts for agent", 0, agent.scripts.size());
      assertEquals("Agent definition name", "testAgentName", agent.name);

      agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName2\", \"scripts\":[{}]}");
      assertTrue("Agent definition not created", agent != null);
      assertEquals("Number of agent definitions for user", 5, agentServer.agentDefinitions.get("Test-User").size());
      assertEquals("Agent definition name", "testAgentName2", agent.name);
      assertEquals("Number of scripts for agent", 0, agent.scripts.size());

      // Test for creating 'init' script
      agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName3\", \"scripts\":[{\"name\": \"init\"}]}");
      assertTrue("Agent definition not created", agent != null);
      assertEquals("Number of agent definitions for user", 6, agentServer.agentDefinitions.get("Test-User").size());
      assertEquals("Agent definition name", "testAgentName3", agent.name);
      assertEquals("Number of scripts for agent", 1, agent.scripts.size());
      ScriptDefinition script = agent.scripts.get(0).value;
      String scriptName = script.name;
      assertTrue("Script name is missing", scriptName != null);
      assertEquals("Script name", "init", scriptName);
      String scriptDescription = script.description;
      assertTrue("Script description is missing", scriptDescription != null);
      assertEquals("Script description", "", scriptDescription);
      String scriptScript = script.script;
      assertTrue("Script body is missing", scriptScript != null);
      assertEquals("Script body", "", scriptScript);

      agentServer.clearAgentDefinitions("Test-User");
      assertEquals("Number of agent definitions for user", 0, agentServer.agentDefinitions.get("Test-User").size());
      agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName3\", \"scripts\":[{\"name\": \"init\", \"description\": \"A test script\", \"script\": \"return 2+2;\"}]}");
      assertTrue("Agent definition not created", agent != null);
      assertEquals("Number of agent definitions for user", 1, agentServer.agentDefinitions.get("Test-User").size());
      assertEquals("Agent definition name", "testAgentName3", agent.name);
      assertEquals("Number of scripts for agent", 1, agent.scripts.size());
      script = agent.scripts.get(0).value;
      scriptName = script.name;
      assertTrue("Script name is missing", scriptName != null);
      assertEquals("Script name", "init", scriptName);
      scriptDescription = script.description;
      assertTrue("Script description is missing", scriptDescription != null);
      assertEquals("Script description", "A test script", scriptDescription);
      scriptScript = script.script;
      assertTrue("Script body is missing", scriptScript != null);
      assertEquals("Script body", "return 2+2;", scriptScript);

      // Check default values
      assertEquals("Agent definition description", "", agent.description);
      assertEquals("Default reporting interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL, agent.reportingInterval);
      assertEquals("Number of parameters", 0, agent.parameters.size());
      assertEquals("Number of inputs", 0, agent.inputs.size());
      assertEquals("Number of memory fields", 0, agent.memory.size());
      assertEquals("Number of output fields", 0, agent.outputs.size());

      // Check description and reporting interval
      agentServer.clearAgentDefinitions("Test-User");
      assertEquals("Number of agent definitions for user", 0, agentServer.agentDefinitions.get("Test-User").size());
      agent = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"testAgentName3\", \"scripts\":[{\"name\": \"init\", \"description\": \"A test script\", \"script\": \"return 2+2;\"}], \"reporting_interval\": 3000, \"description\": \"Test agent definition\"}");
      assertTrue("Agent definition not created", agent != null);
      assertEquals("Number of agent definitions for user", 1, agentServer.agentDefinitions.get("Test-User").size());
      assertEquals("Agent definition name", "testAgentName3", agent.name);
      assertEquals("Number of scripts for agent", 1, agent.scripts.size());
      script = agent.scripts.get(0).value;
      scriptName = script.name;
      assertTrue("Script name is missing", scriptName != null);
      assertEquals("Script name", "init", scriptName);
      scriptDescription = script.description;
      assertTrue("Script description is missing", scriptDescription != null);
      assertEquals("Script description", "A test script", scriptDescription);
      scriptScript = script.script;
      assertTrue("Script body is missing", scriptScript != null);
      assertEquals("Script body", "return 2+2;", scriptScript);
      assertEquals("Agent definition description", "Test agent definition", agent.description);
      assertEquals("Default reporting interval", 3000, agent.reportingInterval);
      assertEquals("Number of parameters", 0, agent.parameters.size());
      assertEquals("Number of inputs", 0, agent.inputs.size());
      assertEquals("Number of memory fields", 0, agent.memory.size());
      assertEquals("Number of output fields", 0, agent.outputs.size());
    }

    }

  @Test
  public void testMemoryAccess() throws Exception {
    // Create a test user
    agentServer.addUser("Test-User");
    
    // Create an agent definition and instance with two memory fields
    AgentDefinition agDef = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"ag1\", \"scripts\":[{\"name\": \"init\", \"script\": \"return 'rv-init-1';\"}, {\"name\": \"return_memory_aa\", \"script\": \"return memory.aa;\"}, {\"name\": \"increment_memory_aa\", \"script\": \"memory.aa++; return memory.aa;\"}, {\"name\": \"increment_memory_bb\", \"script\": \"memory.bb++; return memory.bb;\"}, {\"name\": \"modify_memory_both\", \"script\": \"memory.aa = memory.aa * 4; memory.bb = memory.bb * 4; return map(aa: memory.aa, bb: memory.bb);\"}], \"parameters\": [{\"name\": \"x\", \"type\": \"int\"}, {\"name\": \"y\", \"type\": \"float\"}, {\"name\": \"z\", \"type\": \"string\"}], \"memory\": [{\"name\": \"aa\", \"type\": \"float\", \"default_value\": 2.125}, {\"name\": \"bb\", \"type\": \"string\", \"default_value\": \"cat\"}]}");
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"testAgentName1\", \"definition\": \"ag1\", \"parameter_values\": {\"x\": 123, \"y\": 4.56, \"z\": \"abc\"}}");

    // Check that the two memory fields have the specified default values
    Value aaValueNode = agInst.getMemory("aa");
    assertTrue("aaValueNode is missing", aaValueNode != null);
    assertTrue("aaValueNode is not a FloatTypeNode: " + aaValueNode.getClass().getSimpleName(), aaValueNode instanceof FloatValue);
    assertEquals("memory.aa", 2.125, aaValueNode.getFloatValue(), 0.0001);
    Value bbValueNode = agInst.getMemory("bb");
    assertTrue("bbValueNode is missing", bbValueNode != null);
    assertTrue("bbValueNode is not a StringTypeNode: " + bbValueNode.getClass().getSimpleName(), bbValueNode instanceof StringValue);
    assertEquals("memory.bb", "cat", bbValueNode.getStringValue());

    // Run script that returns the 'aa' memory field
    Value returnValueNode = agInst.runScript("return_memory_aa");
    assertTrue("Return value is missing", returnValueNode != null);
    assertTrue("Return value is not of type FloatValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof FloatValue);
    assertEquals("Return value", 2.125, returnValueNode.getFloatValue(), 0.0001);
    aaValueNode = agInst.getMemory("aa");
    assertTrue("aaValueNode is missing", aaValueNode != null);
    assertTrue("aaValueNode is not a FloatTypeNode: " + aaValueNode.getClass().getSimpleName(), aaValueNode instanceof FloatValue);
    assertEquals("memory.aa", 2.125, aaValueNode.getFloatValue(), 0.0001);
    bbValueNode = agInst.getMemory("bb");
    assertTrue("bbValueNode is missing", bbValueNode != null);
    assertTrue("bbValueNode is not a StringTypeNode: " + bbValueNode.getClass().getSimpleName(), bbValueNode instanceof StringValue);
    assertEquals("memory.bb", "cat", bbValueNode.getStringValue());

    // Run Script that modifies only the 'aa' field
    returnValueNode = agInst.runScript("increment_memory_aa");
    assertTrue("Return value is missing", returnValueNode != null);
    assertTrue("Return value is not of type FloatValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof FloatValue);
    assertEquals("Return value", 3.125, returnValueNode.getFloatValue(), 0.0001);
    aaValueNode = agInst.getMemory("aa");
    assertTrue("aaValueNode is missing", aaValueNode != null);
    assertTrue("aaValueNode is not a FloatTypeNode: " + aaValueNode.getClass().getSimpleName(), aaValueNode instanceof FloatValue);
    assertEquals("memory.aa", 3.125, aaValueNode.getFloatValue(), 0.0001);
    bbValueNode = agInst.getMemory("bb");
    assertTrue("bbValueNode is missing", bbValueNode != null);
    assertTrue("bbValueNode is not a StringTypeNode: " + bbValueNode.getClass().getSimpleName(), bbValueNode instanceof StringValue);
    assertEquals("memory.bb", "cat", bbValueNode.getStringValue());

    // Run Script that modifies only the 'bb' field
    returnValueNode = agInst.runScript("increment_memory_bb");
    assertTrue("Return value is missing", returnValueNode != null);
    assertTrue("Return value is not of type StringValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof StringValue);
    assertEquals("Return value", "cat ", returnValueNode.getStringValue());
    aaValueNode = agInst.getMemory("aa");
    assertTrue("aaValueNode is missing", aaValueNode != null);
    assertTrue("aaValueNode is not a FloatTypeNode: " + aaValueNode.getClass().getSimpleName(), aaValueNode instanceof FloatValue);
    assertEquals("memory.aa", 3.125, aaValueNode.getFloatValue(), 0.0001);
    bbValueNode = agInst.getMemory("bb");
    assertTrue("bbValueNode is missing", bbValueNode != null);
    assertTrue("bbValueNode is not a StringTypeNode: " + bbValueNode.getClass().getSimpleName(), bbValueNode instanceof StringValue);
    assertEquals("memory.bb", "cat ", bbValueNode.getStringValue());

    // Run Script that modifies both the 'aa' and 'bb' memory fields and returns as a map
    returnValueNode = agInst.runScript("modify_memory_both");
    assertTrue("Return value is missing", returnValueNode != null);
    assertTrue("Return value is not of type MapValueNode: " + returnValueNode.getClass().getSimpleName(), returnValueNode instanceof MapValue);
    MapValue map = (MapValue)returnValueNode;
    assertEquals("Number of fields in return value map", 2, map.value.size());
    assertTrue("Return value map is missing field 'aa'", map.value.containsKey("aa"));
    Value valueNode = map.value.get("aa"); 
    assertTrue("Map field 'aa' value is missing", valueNode != null);
    assertTrue("Map field 'aa' value is not a FloatTypeNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Map field 'aa' value", 12.5, valueNode.getFloatValue(), 0.0001);
    assertTrue("Return value map is missing field 'bb'", map.value.containsKey("bb"));
    valueNode = map.value.get("bb"); 
    assertTrue("Map field 'bb' value is missing", valueNode != null);
    assertTrue("Map field 'bb' value is not a StringTypeNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Map field 'bb' value", "cat cat cat cat ", valueNode.getStringValue());
    aaValueNode = agInst.getMemory("aa");
    assertTrue("aaValueNode is missing", aaValueNode != null);
    assertTrue("aaValueNode is not a FloatTypeNode: " + aaValueNode.getClass().getSimpleName(), aaValueNode instanceof FloatValue);
    assertEquals("memory.aa", 12.5, aaValueNode.getFloatValue(), 0.0001);
    bbValueNode = agInst.getMemory("bb");
    assertTrue("bbValueNode is missing", bbValueNode != null);
    assertTrue("bbValueNode is not a StringTypeNode: " + bbValueNode.getClass().getSimpleName(), bbValueNode instanceof StringValue);
    assertEquals("memory.bb", "cat cat cat cat ", bbValueNode.getStringValue());
    
  }

  @Test
  public void testHelloWorld() throws Exception {
    // Create a test user
    agentServer.addUser("Test-User");
    
    // Create an agent definition and instance with two memory fields
    AgentDefinition agDef = agentServer.addAgentDefinition("{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}]}");
    AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"definition\": \"HelloWorld\"}");
    
    // Check that the output field is set to "Hello World"
    Value field1ValueNode = agInst.getOutput("field1");
    assertTrue("field1 is missing", field1ValueNode != null);
    assertTrue("field1 is not a StringTypeNode: " + field1ValueNode.getClass().getSimpleName(), field1ValueNode instanceof StringValue);
    assertEquals("output.field1", "Hello World", field1ValueNode.getStringValue());
    
  }

  @Test
  public void testTimerParsing() throws Exception {
    // Create a test user
    agentServer.addUser("Test-User");

    // Test agent with no timers
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\"}");
      assertEquals("Timer count", 0, agDef.timers.size());
    }

    // Test agent with empty timers list
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": []}");
      assertEquals("Timer count", 0, agDef.timers.size());
    }

    // Test agent with timers list with only empty timers
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{}, {}, {}]}");
      assertEquals("Timer count", 0, agDef.timers.size());
    }

    // Test agent with a single timer
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"name\": \"timer-1\", \"interval\": 10, \"description\": \"10 ms timer\", \"script\": \"return 'hit timer-10';\", \"enabled\": true}]}");
      assertEquals("Timer count", 1, agDef.timers.size());
      AgentTimer timer = agDef.timers.get(0).value;
      assertTrue("Timer name is missing", timer.name != null);
      assertEquals("Timer name", "timer-1", timer.name);
      assertTrue("Timer description is missing", timer.description != null);
      assertEquals("Timer description", "10 ms timer", timer.description);
      assertEquals("Timer interval", 10, timer.interval);
      assertTrue("Timer script is missing", timer.script != null);
      assertEquals("Timer script", "return 'hit timer-10';", timer.script);
      assertEquals("Timer enabled", true, timer.enabled);
    }

    // Test agent with a single timer with missing interval
    {
      agentServer.agentDefinitions.clear();
      try {
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"name\": \"timer-1\", \"description\": \"10 ms timer\", \"script\": \"return 'hit timer-10';\", \"enabled\": true}]}");
      assertTrue("Missing timer interval not detected", agDef == null);
      } catch (AgentServerException e){
        assertEquals("addAgentDefinition exception", "Timer interval is missing", e.getMessage());
      }
    }

    // Test agent with a single timer with zero interval
    {
      agentServer.agentDefinitions.clear();
      try {
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"name\": \"timer-1\", \"interval\": 0, \"description\": \"10 ms timer\", \"script\": \"return 'hit timer-10';\", \"enabled\": true}]}");
      assertTrue("Missing timer interval not detected", agDef == null);
      } catch (AgentServerException e){
        assertEquals("addAgentDefinition exception", "Timer interval may not be zero or negative", e.getMessage());
      }
    }

    // Test agent with a single timer with negative interval
    {
      agentServer.agentDefinitions.clear();
      try {
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"name\": \"timer-1\", \"interval\": -10, \"description\": \"10 ms timer\", \"script\": \"return 'hit timer-10';\", \"enabled\": true}]}");
      assertTrue("Missing timer interval not detected", agDef == null);
      } catch (AgentServerException e){
        assertEquals("addAgentDefinition exception", "Timer interval may not be zero or negative", e.getMessage());
      }
    }

    // Test agent with a single timer with missing description
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"name\": \"timer-1\", \"interval\": 10, \"script\": \"return 'hit timer-10';\", \"enabled\": true}]}");
      assertEquals("Timer count", 1, agDef.timers.size());
      AgentTimer timer = agDef.timers.get(0).value;
      assertTrue("Timer name is missing", timer.name != null);
      assertEquals("Timer name", "timer-1", timer.name);
      assertTrue("Timer description is missing", timer.description != null);
      assertEquals("Timer description", "", timer.description);
      assertEquals("Timer interval", 10, timer.interval);
      assertTrue("Timer script is missing", timer.script != null);
      assertEquals("Timer script", "return 'hit timer-10';", timer.script);
      assertEquals("Timer enabled", true, timer.enabled);
    }

    // Test agent with a timer missing name
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"interval\": 10, \"description\": \"10 ms timer\", \"script\": \"return 'hit timer-10';\", \"enabled\": true}]}");
      assertEquals("Timer count", 1, agDef.timers.size());
      AgentTimer timer = agDef.timers.get(0).value;
      assertTrue("Timer name is missing", timer.name != null);
      assertEquals("Timer name", "", timer.name);
      assertTrue("Timer description is missing", timer.description != null);
      assertEquals("Timer description", "10 ms timer", timer.description);
      assertEquals("Timer interval", 10, timer.interval);
      assertTrue("Timer script is missing", timer.script != null);
      assertEquals("Timer script", "return 'hit timer-10';", timer.script);
      assertEquals("Timer enabled", true, timer.enabled);
    }

    // Test agent with a timer missing script
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"interval\": 10, \"enabled\": true}]}");
      assertEquals("Timer count", 1, agDef.timers.size());
      AgentTimer timer = agDef.timers.get(0).value;
      assertTrue("Timer name is missing", timer.name != null);
      assertEquals("Timer name", "", timer.name);
      assertTrue("Timer description is missing", timer.description != null);
      assertEquals("Timer description", "", timer.description);
      assertEquals("Timer interval", 10, timer.interval);
      assertTrue("Timer script is missing", timer.script != null);
      assertEquals("Timer script", "", timer.script);
      assertEquals("Timer enabled", true, timer.enabled);
    }

    // Test agent with a timer disabled
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"interval\": 10, \"enabled\": false}]}");
      assertEquals("Timer count", 1, agDef.timers.size());
      AgentTimer timer = agDef.timers.get(0).value;
      assertTrue("Timer name is missing", timer.name != null);
      assertEquals("Timer name", "", timer.name);
      assertTrue("Timer description is missing", timer.description != null);
      assertEquals("Timer description", "", timer.description);
      assertEquals("Timer interval", 10, timer.interval);
      assertTrue("Timer script is missing", timer.script != null);
      assertEquals("Timer script", "", timer.script);
      assertEquals("Timer enabled", false, timer.enabled);
    }

    // Test agent with a timer missing enabled flag
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"interval\": 10}]}");
      assertEquals("Timer count", 1, agDef.timers.size());
      AgentTimer timer = agDef.timers.get(0).value;
      assertTrue("Timer name is missing", timer.name != null);
      assertEquals("Timer name", "", timer.name);
      assertTrue("Timer description is missing", timer.description != null);
      assertEquals("Timer description", "", timer.description);
      assertEquals("Timer interval", 10, timer.interval);
      assertTrue("Timer script is missing", timer.script != null);
      assertEquals("Timer script", "", timer.script);
      assertEquals("Timer enabled", true, timer.enabled);
    }

    // Test agent with a multiple timers
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"TimerTest\", " +
          "\"timers\": [{\"name\": \"timer-1\", \"interval\": 10, \"description\": \"10 ms timer\", \"script\": \"return 'hit timer-1';\", \"enabled\": true}, " +
          "             {\"name\": \"timer-2\", \"interval\": 2500, \"description\": \"2500 ms timer\", \"script\": \"return 'hit timer-2';\", \"enabled\": false}, " +
          "             {\"name\": \"timer-3\", \"interval\": 1234567890, \"description\": \"long timer\", \"script\": \"return 'hit timer-3';\", \"enabled\": true}]}");
      assertEquals("Timer count", 3, agDef.timers.size());
      AgentTimer timer = agDef.timers.get(0).value;
      assertTrue("Timer name is missing", timer.name != null);
      assertEquals("Timer name", "timer-1", timer.name);
      assertTrue("Timer description is missing", timer.description != null);
      assertEquals("Timer description", "10 ms timer", timer.description);
      assertEquals("Timer interval", 10, timer.interval);
      assertTrue("Timer script is missing", timer.script != null);
      assertEquals("Timer script", "return 'hit timer-1';", timer.script);
      assertEquals("Timer enabled", true, timer.enabled);
      timer = agDef.timers.get(1).value;
      assertTrue("Timer name is missing", timer.name != null);
      assertEquals("Timer name", "timer-2", timer.name);
      assertTrue("Timer description is missing", timer.description != null);
      assertEquals("Timer description", "2500 ms timer", timer.description);
      assertEquals("Timer interval", 2500, timer.interval);
      assertTrue("Timer script is missing", timer.script != null);
      assertEquals("Timer script", "return 'hit timer-2';", timer.script);
      assertEquals("Timer enabled", false, timer.enabled);
      timer = agDef.timers.get(2).value;
      assertTrue("Timer name is missing", timer.name != null);
      assertEquals("Timer name", "timer-3", timer.name);
      assertTrue("Timer description is missing", timer.description != null);
      assertEquals("Timer description", "long timer", timer.description);
      assertEquals("Timer interval", 1234567890, timer.interval);
      assertTrue("Timer script is missing", timer.script != null);
      assertEquals("Timer script", "return 'hit timer-3';", timer.script);
      assertEquals("Timer enabled", true, timer.enabled);

      // Now create an instance of that agent definition
      AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"TimerTest\", \"definition\": \"TimerTest\"}");
      
      // Make sure no timer status yet since no timers have run
      assertEquals("Count of timer statuses", 3, agInst.timerStatus.size());
      AgentTimerStatus status = agInst.timerStatus.get("timer-1");
      assertEquals("Timer hits", 0, status.hits);
      status = agInst.timerStatus.get("timer-2");
      assertEquals("Timer hits", 0, status.hits);
      status = agInst.timerStatus.get("timer-3");
      assertEquals("Timer hits", 0, status.hits);
    }
    
  }

  @Test
  public void testConditionParsing() throws Exception {
    // Create a test user
    agentServer.addUser("Test-User");

    // Test agent with no conditions
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\"}");
      assertEquals("Condition count", 0, agDef.conditions.size());
    }

    // Test agent with empty conditions list
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": []}");
      assertEquals("Condition count", 0, agDef.conditions.size());
    }

    // Test agent with conditions list with only empty conditions
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{}, {}, {}]}");
      assertEquals("Condition count", 0, agDef.conditions.size());
    }

    // Test agent with a single condition
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 10, \"condition\": \"i.i1.f1 - i.i2.f1 < i.i3.f3\", \"description\": \"10 ms condition\", \"script\": \"m.hit['condition-1'] = true;\", \"enabled\": true}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "condition-1", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "10 ms condition", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f1 - i.i2.f1 < i.i3.f3", condition.condition);
      assertEquals("Condition interval", 10, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "m.hit['condition-1'] = true;", condition.script);
      assertEquals("Condition enabled", true, condition.enabled);
    }

    // Test agent with a single condition with junk keywords
    {
      try {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"namex\": \"condition-1\", \"intervalx\": 10, \"condition\": \"i.i1.f1 - i.i2.f1 < i.i3.f3\", \"descriptionx\": \"10 ms condition\", \"scriptx\": \"m.hit['condition-1'] = true;\", \"enabledx\": true}]}");
      assertTrue("Should not have created agent definition when condition had bad keys", agDef == null);
      } catch (AgentServerException e){
        assertEquals("Exception thrown", "Agent condition JSON has invalid keys: descriptionx, enabledx, intervalx, namex, scriptx", e.getMessage());
      }
    }

    // Test agent with a single condition missing interval
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"name\": \"condition-1\", \"condition\": \"i.i1.f1 - i.i2.f1 < i.i3.f3\", \"description\": \"10 ms condition\", \"script\": \"m.hit['condition-1'] = true;\", \"enabled\": true}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "condition-1", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "10 ms condition", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f1 - i.i2.f1 < i.i3.f3", condition.condition);
      assertEquals("Condition interval", 0, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "m.hit['condition-1'] = true;", condition.script);
      assertEquals("Condition enabled", true, condition.enabled);
    }

    // Test agent with a single condition missing script
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"name\": \"condition-1\", \"condition\": \"i.i1.f1 - i.i2.f1 < i.i3.f3\", \"description\": \"10 ms condition\", \"enabled\": true}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "condition-1", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "10 ms condition", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f1 - i.i2.f1 < i.i3.f3", condition.condition);
      assertEquals("Condition interval", 0, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "", condition.script);
      assertEquals("Condition enabled", true, condition.enabled);
    }

    // Test agent with a single condition missing name
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"condition\": \"i.i1.f1 - i.i2.f1 < i.i3.f3\", \"description\": \"10 ms condition\", \"enabled\": true}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "10 ms condition", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f1 - i.i2.f1 < i.i3.f3", condition.condition);
      assertEquals("Condition interval", 0, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "", condition.script);
      assertEquals("Condition enabled", true, condition.enabled);
    }

    // Test agent with a single condition missing description
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"condition\": \"i.i1.f1 - i.i2.f1 < i.i3.f3\", \"enabled\": true}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f1 - i.i2.f1 < i.i3.f3", condition.condition);
      assertEquals("Condition interval", 0, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "", condition.script);
      assertEquals("Condition enabled", true, condition.enabled);
    }

    // Test agent with condition disabled
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"condition\": \"i.i1.f1 - i.i2.f1 < i.i3.f3\", \"enabled\": false}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f1 - i.i2.f1 < i.i3.f3", condition.condition);
      assertEquals("Condition interval", 0, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "", condition.script);
      assertEquals("Condition enabled", false, condition.enabled);
    }

    // Test agent with a single condition missing enabled flag
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"condition\": \"i.i1.f1 - i.i2.f1 < i.i3.f3\"}]}");
      assertEquals("Condition count", 1, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f1 - i.i2.f1 < i.i3.f3", condition.condition);
      assertEquals("Condition interval", 0, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "", condition.script);
      assertEquals("Condition enabled", true, condition.enabled);
    }

    // Test agent with a single condition with missing condition expression
    {
      agentServer.agentDefinitions.clear();
      try {
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 10, \"description\": \"10 ms condition\", \"script\": \"m.hit['condition-1'] = true;\", \"enabled\": true}]}");
      assertTrue("Missing condition interval not detected", agDef == null);
      } catch (AgentServerException e){
        assertEquals("addAgentDefinition exception", "Condition condition expression is missing", e.getMessage());
      }
    }

    // Test agent with multiple conditions
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"ConditionTest\", " +
          "\"conditions\": [{\"name\": \"condition-1\", \"interval\": 10, \"condition\": \"i.i1.f1 - i.i2.f1 < i.i3.f3\", \"description\": \"10 ms condition\", \"script\": \"m.hit['condition-1'] = true;\", \"enabled\": true}, " +
          "                 {\"name\": \"condition-2\", \"interval\": 2500, \"condition\": \"i.i1.f2 - i.i2.f2 < i.i3.f4\", \"description\": \"2.5 sec condition\", \"script\": \"m.hit['condition-2'] = true;\", \"enabled\": false}, " +
          "                 {\"name\": \"condition-3\", \"interval\": 10000, \"condition\": \"i.i1.f3 - i.i2.f3 < i.i3.f5\", \"description\": \"10 sec condition\", \"script\": \"m.hit['condition-3'] = true;\"}]}, \"enabled\": true");
      assertEquals("Condition count", 3, agDef.conditions.size());
      AgentCondition condition = agDef.conditions.get(0).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "condition-1", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "10 ms condition", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f1 - i.i2.f1 < i.i3.f3", condition.condition);
      assertEquals("Condition interval", 10, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "m.hit['condition-1'] = true;", condition.script);
      assertEquals("Condition enabled", true, condition.enabled);
      condition = agDef.conditions.get(1).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "condition-2", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "2.5 sec condition", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f2 - i.i2.f2 < i.i3.f4", condition.condition);
      assertEquals("Condition interval", 2500, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "m.hit['condition-2'] = true;", condition.script);
      assertEquals("Condition enabled", false, condition.enabled);
      condition = agDef.conditions.get(2).value;
      assertTrue("Condition name is missing", condition.name != null);
      assertEquals("Condition name", "condition-3", condition.name);
      assertTrue("Condition description is missing", condition.description != null);
      assertEquals("Condition description", "10 sec condition", condition.description);
      assertTrue("Condition condition expression is missing", condition.condition != null);
      assertEquals("Condition description", "i.i1.f3 - i.i2.f3 < i.i3.f5", condition.condition);
      assertEquals("Condition interval", 10000, condition.interval);
      assertTrue("Condition script is missing", condition.script != null);
      assertEquals("Condition script", "m.hit['condition-3'] = true;", condition.script);
      assertEquals("Condition enabled", true, condition.enabled);

      // Now create an instance of that agent definition
      AgentInstance agInst = agentServer.getAgentInstance("{\"user\": \"Test-User\", \"name\": \"ConditionTest\", \"definition\": \"ConditionTest\"}");
      
      // Make sure no condition status yet since no conditions have run
      assertEquals("Count of condition statuses", 3, agInst.conditionStatus.size());
      AgentConditionStatus status = agInst.conditionStatus.get("condition-1");
      assertEquals("Condition hits", 0, status.hits);
      status = agInst.conditionStatus.get("condition-2");
      assertEquals("Condition hits", 0, status.hits);
      status = agInst.conditionStatus.get("condition-3");
      assertEquals("Condition hits", 0, status.hits);
    }
  }

  @Test
  public void testDataSourceParsing() throws Exception {
    // Create a test user
    User user = agentServer.addUser("Test-User");

    // Test agent with no data sources
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\"}");
      assertEquals("Data source count", 0, agDef.inputs.size());
    }

    // Test agent with empty data sources list
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\", " +
          "\"inputs\": []}");
      assertEquals("Data source count", 0, agDef.inputs.size());
    }

    // Test agent with data source list with only empty data sources
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\", " +
          "\"inputs\": [{}, {}, {}]}");
      assertEquals("Condition count", 0, agDef.inputs.size());
    }

    // Test agent with a single data source with missing name
    {
      agentServer.agentDefinitions.clear();
      try {
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\", " +
          "\"inputs\": [{\"data_source\": \"xx\"}]}");
      assertTrue("Missing condition interval not detected", agDef == null);
      } catch (AgentServerException e){
        assertEquals("addAgentDefinition exception", "Inputs JSON object is missing 'name' key", e.getMessage());
      }
    }

    // Test agent with a single data source missing data_source
    {
      agentServer.agentDefinitions.clear();
      try {
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\", " +
          "\"inputs\": [{\"name\": \"xx\"}]}");
      assertTrue("Missing condition interval not detected", agDef == null);
      } catch (AgentServerException e){
        assertEquals("addAgentDefinition exception", "Inputs JSON object is missing 'data_source' key", e.getMessage());
      }
    }

    // Test agent with a single data source referencing a nonexistent data source
    {
      agentServer.agentDefinitions.clear();
      try {
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\", " +
          "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\"}]}");
      assertTrue("Missing condition interval not detected", agDef == null);
      } catch (AgentServerException e){
        assertEquals("addAgentDefinition exception", "Inputs JSON for data source name 'input1' references data source 'DataSource1' which is not a defined data source", e.getMessage());
      }
    }

    // Test agent with a single data source that does exist
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition dsDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSource1\", " +
          "\"outputs\": [{\"name\": \"outField1\", \"description\": \"First test output field\", \"type\": \"string\", \"default_value\": \"Hello World\"}]}");
      assertEquals("Data source count", 0, dsDef.inputs.size());
      Field f = dsDef.outputs.get(0);
      assertTrue("Output field name not present", f.symbol.name != null);
      assertEquals("Output field name", "outField1", f.symbol.name);
      assertTrue("Output field description not present", f.description != null);
      assertEquals("Output field description", "First test output field", f.description);
      assertTrue("Output field type is not StringNodeType: " + f.getType().getClass().getSimpleName() , f.getType() instanceof StringTypeNode);
      assertTrue("Output field default value is not present", f.getDefaultValue() != null);
      assertEquals("Output field default value", "Hello World", f.getDefaultValue());
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\", " +
          "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\"}]}");
      assertEquals("Data source count", 1, agDef.inputs.size());
      DataSourceReference dsr = agDef.inputs.get(0);
      assertTrue("Data source name not present", dsr.name != null);
      assertEquals("Data source name", "input1", dsr.name);
      assertTrue("Data source data_source not present", dsr.dataSource != null);
      assertEquals("Data source data_source", "DataSource1", dsr.dataSource.name);
      assertTrue("Data source parameter values not present", dsr.parameterValues != null);
      assertEquals("Data source paramter values count", 0, dsr.parameterValues.size());
    }

    // Test agent with a single data source that does exist, and with parameters
    {
      agentServer.agentDefinitions.clear();
      AgentDefinition dsDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSource1\", " +
          "\"parameters\": [{\"name\": \"p1\", \"description\": \"First parameter\", \"type\": \"string\", \"default_value\": \"abc\"}, " +
          "                 {\"name\": \"p2\", \"description\": \"Second parameter\", \"type\": \"int\", \"default_value\": 123}, " +
          "                 {\"name\": \"p3\", \"description\": \"Third parameter\", \"type\": \"float\", \"default_value\": 4.5}], " +
          "\"outputs\": [{\"name\": \"outField1\", \"description\": \"First test output field\", \"type\": \"string\", \"default_value\": \"Hello World\"}]}");
      assertEquals("Data source count", 0, dsDef.inputs.size());
      assertEquals("Parameter field count", 3, dsDef.parameters.size());
      Field f = dsDef.parameters.get(0);
      assertTrue("Output field name not present", f.symbol.name != null);
      assertEquals("Output field name", "p1", f.symbol.name);
      assertTrue("Output field description not present", f.description != null);
      assertEquals("Output field description", "First parameter", f.description);
      assertTrue("Output field type is not StringNodeType: " + f.getType().getClass().getSimpleName() , f.getType() instanceof StringTypeNode);
      assertTrue("Output field default value is not present", f.getDefaultValue() != null);
      assertEquals("Output field default value", "abc", f.getDefaultValue());
      f = dsDef.parameters.get(1);
      assertTrue("Output field name not present", f.symbol.name != null);
      assertEquals("Output field name", "p2", f.symbol.name);
      assertTrue("Output field description not present", f.description != null);
      assertEquals("Output field description", "Second parameter", f.description);
      assertTrue("Output field type is not IntegerNodeType: " + f.getType().getClass().getSimpleName() , f.getType() instanceof IntegerTypeNode);
      assertTrue("Output field default value is not present", f.getDefaultValue() != null);
      assertEquals("Output field default value", Long.valueOf(123), f.getDefaultValue());
      f = dsDef.parameters.get(2);
      assertTrue("Output field name not present", f.symbol.name != null);
      assertEquals("Output field name", "p3", f.symbol.name);
      assertTrue("Output field description not present", f.description != null);
      assertEquals("Output field description", "Third parameter", f.description);
      assertTrue("Output field type is not FloatNodeType: " + f.getType().getClass().getSimpleName() , f.getType() instanceof FloatTypeNode);
      assertTrue("Output field default value is not present", f.getDefaultValue() != null);
      assertEquals("Output field default value", 4.5, (Double)f.getDefaultValue(), 0.0001);
      assertEquals("Output field count", 1, dsDef.outputs.size());
      f = dsDef.outputs.get(0);
      assertTrue("Output field name not present", f.symbol.name != null);
      assertEquals("Output field name", "outField1", f.symbol.name);
      assertTrue("Output field description not present", f.description != null);
      assertEquals("Output field description", "First test output field", f.description);
      assertTrue("Output field type is not StringNodeType: " + f.getType().getClass().getSimpleName() , f.getType() instanceof StringTypeNode);
      assertTrue("Output field default value is not present", f.getDefaultValue() != null);
      assertEquals("Output field default value", "Hello World", f.getDefaultValue());

      // Reference the data source without any parameters
      AgentDefinition agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\", " +
          "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\"}]}");
      assertEquals("Data source count", 1, agDef.inputs.size());
      DataSourceReference dsr = agDef.inputs.get(0);
      assertTrue("Data source name not present", dsr.name != null);
      assertEquals("Data source name", "input1", dsr.name);
      assertTrue("Data source data_source not present", dsr.dataSource != null);
      assertEquals("Data source data_source", "DataSource1", dsr.dataSource.name);
      assertTrue("Data source parameter values not present", dsr.parameterValues != null);
      assertEquals("Data source paramter values count", 0, dsr.parameterValues.size());

      // Reference the data source with empty parameter list
      agentServer.removeAgentDefinition(agDef);
      agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\", " +
          "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\", " +
          "             \"parameter_values\": {}}]}");
      assertEquals("Data source count", 1, agDef.inputs.size());
      dsr = agDef.inputs.get(0);
      assertTrue("Data source name not present", dsr.name != null);
      assertEquals("Data source name", "input1", dsr.name);
      assertTrue("Data source data_source not present", dsr.dataSource != null);
      assertEquals("Data source data_source", "DataSource1", dsr.dataSource.name);
      assertTrue("Data source parameter values not present", dsr.parameterValues != null);
      assertEquals("Data source paramter values count", 0, dsr.parameterValues.size());

      // Now instantiate the agent and verify that it instantiates the data source with
      // default parameter values
      
      AgentInstance agInst = agentServer.getAgentInstance(user, agDef);
      assertEquals("Number of data source instance references", 1, agInst.dataSourceInstances.size());
      AgentInstance dsInst = agInst.dataSourceInstances.get(dsr);
      assertEquals("Data source instance definition", dsInst.agentDefinition, dsDef);
      assertEquals("Number of data source instance dependents", 1, dsInst.dependentInstances.size());
      assertEquals("Data source dependent", agInst, dsInst.dependentInstances.get(0));

      // Make sure we can't release the data source instance since it has dependents
      try {
        dsInst.release();
      } catch (AgentServerException e){
        assertEquals("Exception for release of instance with dependents", "Can't release an instance that has dependents", e.getMessage());
      }
      
      // Now release the instance we explicitly created
      agInst.release();
      
      // Now we should be able to release the data source instance
      dsInst.release();
      
      // Reference the data source with parameter list containing bogus keys
      agentServer.removeAgentDefinition(agDef);
      try {
        agDef = agentServer.addAgentDefinition(
            "{\"user\": \"Test-User\"," +
            "\"name\": \"DataSourceTest\", " +
            "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\", " +
            "             \"parameter_values\": {\"p4\": 123, \"p5\": 4.5, \"p6\": \"abc\"}}]}");
        assertTrue("Created agent definition when it should have failed due to bogus keys", agDef == null);
      } catch (AgentServerException e){
        assertEquals("addAgentDefinition exception", "Invalid parameter field names for inputs name 'input1' for data source 'DataSourceTest': p4, p5, p6", e.getMessage());
      }

      // Reference the data source with full parameter list
      agDef = agentServer.addAgentDefinition(
          "{\"user\": \"Test-User\"," +
          "\"name\": \"DataSourceTest\", " +
          "\"outputs\": [{}, {}], " +
          "\"inputs\": [{\"name\": \"input1\", \"data_source\": \"DataSource1\", " +
          "             \"parameter_values\": {\"p1\": \"def\", \"p2\": 456, \"p3\": 3.25}}]}");
      assertEquals("Data source count", 1, agDef.inputs.size());
      dsr = agDef.inputs.get(0);
      assertTrue("Data source name not present", dsr.name != null);
      assertEquals("Data source name", "input1", dsr.name);
      assertTrue("Data source data_source not present", dsr.dataSource != null);
      assertEquals("Data source data_source", "DataSource1", dsr.dataSource.name);
      assertTrue("Data source parameter values not present", dsr.parameterValues != null);
      assertEquals("Data source parameter values count", 3, dsr.parameterValues.size());
      Value v = dsr.parameterValues.get("p1");
      assertTrue("Value of parameter p1 is missing", v != null);
      assertTrue("Type of value for parameter p1 is not StringNodeType: " + v.getClass().getSimpleName() , v instanceof StringValue);
      assertEquals("Value of parameter p1", "def", v.getStringValue());
      v = dsr.parameterValues.get("p2");
      assertTrue("Value of parameter p2 is missing", v != null);
      assertTrue("Type of value for parameter p2 is not IntegerNodeType: " + v.getClass().getSimpleName() , v instanceof IntegerValue);
      assertEquals("Value of parameter p2", 456, v.getIntValue());
      v = dsr.parameterValues.get("p3");
      assertTrue("Value of parameter p3 is missing", v != null);
      assertTrue("Type of value for parameter p3 is not FloatNodeType: " + v.getClass().getSimpleName() , v instanceof FloatValue);
      assertEquals("Value of parameter p3", 3.25, v.getFloatValue(), 0.0001);
      
      // Instantiate agent and see that it captures output values from data source
      agInst = agentServer.getAgentInstance(user, agDef);
      SymbolValues inputValues = agInst.categorySymbolValues.get("inputs");
      assertEquals("Number of inputs values", 1, inputValues.size());
      Value valueNode = inputValues.get("input1");
      assertTrue("input1 value is missing", valueNode != null);
      assertTrue("input1 value is not a MapValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue map = (MapValue)valueNode;
      valueNode = map.get("outField1");
      assertTrue("outField1 value is missing", valueNode != null);
      assertTrue("outField1 value is not a StringValueNode: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("outField1 string value", "Hello World", valueNode.getStringValue());
      
    }

  }

}
