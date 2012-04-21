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

package com.basetechnology.s0.agentserver.appserver;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServerTestBase;
import com.basetechnology.s0.agentserver.OutputHistory;
import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.mailaccessmanager.MailAccessManager;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.webaccessmanager.WebSiteAccessConfig;

public class AgentAppServerTest extends AgentServerTestBase {
  static final Logger log = Logger.getLogger(AgentAppServerTest.class);

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }
  
  @Before
  public void setUp() throws Exception {
    cleanupPersistentStore();
    server = new AgentAppServer();
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
    cleanupPersistentStore();
  }
  
  @Test
  public void testGeneral() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Verify proper error response to invalid path
    String url = baseUrl + "/junk/path";
    JSONObject returnJson = doGetJson(url, 404);
    assertTrue("No JSON object returned", returnJson != null);
    //assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Path does not address any existing object\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerException\"}]}", returnJson.toString());
    assertTrue("errors is not present", returnJson.has("errors"));
    assertEquals("Count of Error JSON keys", 1, returnJson.length());
    Object object = returnJson.get("errors");
    assertTrue("errors is not a JSON array", object instanceof JSONArray);
    JSONArray errorsJson = returnJson.getJSONArray("errors");
    assertEquals("Count of errors", 1, errorsJson.length());
    object = errorsJson.get(0);
    assertTrue("errors[0] is not a JSON object", object instanceof JSONObject);
    JSONObject errorJson = errorsJson.getJSONObject(0);
    assertEquals("Count of error keys", 2, errorJson.length());
    assertEquals("Error type", "com.basetechnology.s0.agentserver.appserver.AgentAppServerException", errorJson.getString("type"));
    assertEquals("Error message", "Path does not address any existing object", errorJson.getString("message"));

    url = baseUrl + "/junk/path";
    returnJson = doPutJson(url, "{}", 404);
    assertTrue("No JSON object returned", returnJson != null);
    assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Path does not address any existing object\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerException\"}]}", returnJson.toString());
    assertTrue("errors is not present", returnJson.has("errors"));
    assertEquals("Count of Error JSON keys", 1, returnJson.length());
    object = returnJson.get("errors");
    assertTrue("errors is not a JSON array", object instanceof JSONArray);
    errorsJson = returnJson.getJSONArray("errors");
    assertEquals("Count of errors", 1, errorsJson.length());
    object = errorsJson.get(0);
    assertTrue("errors[0] is not a JSON object", object instanceof JSONObject);
    errorJson = errorsJson.getJSONObject(0);
    assertEquals("Count of error keys", 2, errorJson.length());
    assertEquals("Error type", "com.basetechnology.s0.agentserver.appserver.AgentAppServerException", errorJson.getString("type"));
    assertEquals("Error message", "Path does not address any existing object", errorJson.getString("message"));

    url = baseUrl + "/junk/path";
    returnJson = doPostJson(url, new JSONObject(), 404);
    assertTrue("No JSON object returned", returnJson != null);
    assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Path does not address any existing object\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerException\"}]}", returnJson.toString());
    assertTrue("errors is not present", returnJson.has("errors"));
    assertEquals("Count of Error JSON keys", 1, returnJson.length());
    object = returnJson.get("errors");
    assertTrue("errors is not a JSON array", object instanceof JSONArray);
    errorsJson = returnJson.getJSONArray("errors");
    assertEquals("Count of errors", 1, errorsJson.length());
    object = errorsJson.get(0);
    assertTrue("errors[0] is not a JSON object", object instanceof JSONObject);
    errorJson = errorsJson.getJSONObject(0);
    assertEquals("Count of error keys", 2, errorJson.length());
    assertEquals("Error type", "com.basetechnology.s0.agentserver.appserver.AgentAppServerException", errorJson.getString("type"));
    assertEquals("Error message", "Path does not address any existing object", errorJson.getString("message"));

    url = baseUrl + "/junk/path";
    returnJson = doDeleteJson(url, 404);
    assertTrue("No JSON object returned", returnJson != null);
    assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Path does not address any existing object\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerException\"}]}", returnJson.toString());
    assertTrue("errors is not present", returnJson.has("errors"));
    assertEquals("Count of Error JSON keys", 1, returnJson.length());
    object = returnJson.get("errors");
    assertTrue("errors is not a JSON array", object instanceof JSONArray);
    errorsJson = returnJson.getJSONArray("errors");
    assertEquals("Count of errors", 1, errorsJson.length());
    object = errorsJson.get(0);
    assertTrue("errors[0] is not a JSON object", object instanceof JSONObject);
    errorJson = errorsJson.getJSONObject(0);
    assertEquals("Count of error keys", 2, errorJson.length());
    assertEquals("Error type", "com.basetechnology.s0.agentserver.appserver.AgentAppServerException", errorJson.getString("type"));
    assertEquals("Error message", "Path does not address any existing object", errorJson.getString("message"));

    url = baseUrl + "/";
    returnJson = doGetJson(url, 404);
    assertTrue("No JSON object returned", returnJson != null);
    assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Path does not address any existing object\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerException\"}]}", returnJson.toString());
    assertTrue("errors is not present", returnJson.has("errors"));
    assertEquals("Count of Error JSON keys", 1, returnJson.length());
    object = returnJson.get("errors");
    assertTrue("errors is not a JSON array", object instanceof JSONArray);
    errorsJson = returnJson.getJSONArray("errors");
    assertEquals("Count of errors", 1, errorsJson.length());
    object = errorsJson.get(0);
    assertTrue("errors[0] is not a JSON object", object instanceof JSONObject);
    errorJson = errorsJson.getJSONObject(0);
    assertEquals("Count of error keys", 2, errorJson.length());
    assertEquals("Error type", "com.basetechnology.s0.agentserver.appserver.AgentAppServerException", errorJson.getString("type"));
    assertEquals("Error message", "Path does not address any existing object", errorJson.getString("message"));

    url = baseUrl + "/users/user-id/agent_definitions/agent-definition-name?password=user-password";
    returnJson = doGetJson(url, 400);
    assertTrue("No JSON object returned", returnJson != null);
    assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Unknown user Id or invalid password\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException\"}]}", returnJson.toString());
    assertTrue("errors is not present", returnJson.has("errors"));
    assertEquals("Count of Error JSON keys", 1, returnJson.length());
    object = returnJson.get("errors");
    assertTrue("errors is not a JSON array", object instanceof JSONArray);
    errorsJson = returnJson.getJSONArray("errors");
    assertEquals("Count of errors", 1, errorsJson.length());
    object = errorsJson.get(0);
    assertTrue("errors[0] is not a JSON object", object instanceof JSONObject);
    errorJson = errorsJson.getJSONObject(0);
    assertEquals("Count of error keys", 2, errorJson.length());
    assertEquals("Error type", "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException", errorJson.getString("type"));
    assertEquals("Error message", "Unknown user Id or invalid password", errorJson.getString("message"));

    url = AgentAppServer.appServerBaseUrl + "/users/user-id/agent_definitions/agent-definition-name?password=user-password";
    returnJson = doGetJson(url, 400);
    assertTrue("No JSON object returned", returnJson != null);
    assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Unsupported API path prefix (needs to be '/API/v0.1')\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException\"}]}", returnJson.toString());
    assertTrue("errors is not present", returnJson.has("errors"));
    assertEquals("Count of Error JSON keys", 1, returnJson.length());
    object = returnJson.get("errors");
    assertTrue("errors is not a JSON array", object instanceof JSONArray);
    errorsJson = returnJson.getJSONArray("errors");
    assertEquals("Count of errors", 1, errorsJson.length());
    object = errorsJson.get(0);
    assertTrue("errors[0] is not a JSON object", object instanceof JSONObject);
    errorJson = errorsJson.getJSONObject(0);
    assertEquals("Count of error keys", 2, errorJson.length());
    assertEquals("Error type", "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException", errorJson.getString("type"));
    assertEquals("Error message", "Unsupported API path prefix (needs to be '/API/v0.1')", errorJson.getString("message"));

    // Test that XML response format works for errors
    url = AgentAppServer.appServerBaseUrl + "/users/user-id/agent_definitions/agent-definition-name?password=user-password&format=xml";
    String returnXml = doGetXml(url, 400);
    assertTrue("No JSON object returned", returnXml != null);
    assertEquals("Error JSON", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><errors><message>Unsupported API path prefix (needs to be '/API/v0.1')</message><type>com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException</type></errors>", returnXml.trim());
    
  }
  
  @Ignore
  public void test() throws Exception {
    String baseUrl = AgentAppServer.appServerApiBaseUrl;
    String url = baseUrl + "/users";
    JSONObject userJson = doGetJson(url, 200);
    JSONArray usersArrayJson = userJson.getJSONArray("users");
    int numUsers = usersArrayJson.length();
    assertEquals("Number of users", 0, numUsers);
    log.info("Users: " + userJson + " Users array: " + usersArrayJson + " numUsers: " + numUsers);
    
    String expectedUserName = "joe";
    url = baseUrl + "/users?user_name=" + expectedUserName;
    userJson = doPostJson(url, (JSONObject)null, 201);
    
    url = baseUrl + "/users";
    userJson = doGetJson(url, 200);
    usersArrayJson = userJson.getJSONArray("users");
    numUsers = usersArrayJson.length();
    assertEquals("Number of users", 1, numUsers);
    String userName = usersArrayJson.getString(0);
    assertEquals("User[0]", expectedUserName, userName);
    log.info("Users: " + userJson + " Users array: " + usersArrayJson + " numUsers: " + numUsers);
    
    String expectedUserName2 = "bill@att.com";
    url = baseUrl + "/users?user_name=" + expectedUserName2;
    userJson = doPostJson(url, (JSONObject)null, 201);
    
    url = baseUrl + "/users";
    userJson = doGetJson(url, 200);
    assertTrue("\"users\" field is not present", userJson.has("users"));
    usersArrayJson = userJson.getJSONArray("users");
    numUsers = usersArrayJson.length();
    assertEquals("Number of users", 2, numUsers);
    userName = usersArrayJson.getString(0);
    assertEquals("User[0]", expectedUserName, userName);
    userName = usersArrayJson.getString(1);
    assertEquals("User[1]", expectedUserName2, userName);
    log.info("Users: " + userJson + " Users array: " + usersArrayJson + " numUsers: " + numUsers);

    // Test query of field types
    
    url = baseUrl + "/field_types";
    JSONObject fieldTypesJson = doGetJson(url, 200);
    JSONArray fieldTypesArrayJson = fieldTypesJson.getJSONArray("field_types");
    int numFieldTypes = fieldTypesArrayJson.length();
    List<String> fieldTypeList = new ArrayList<String>();
    for (int i = 0; i < numFieldTypes; i++)
      fieldTypeList.add(fieldTypesArrayJson.getString(i));
    String[] fieldTypes = {"string", "int", "float", "money", "date", "location",
        "text", "help", "boolean", "choice", "multi_choice"};
    assertEquals("Number of field types", fieldTypes.length, numFieldTypes);
    for (String fieldType: fieldTypes)
      assertTrue("Missing field type: " + fieldType, fieldTypeList.contains(fieldType));
    log.info("Field types: " + fieldTypesJson + " Field types array: " + fieldTypesArrayJson + " numFieldTypes: " + numFieldTypes);

    url = baseUrl + "/users/" + expectedUserName + "/agents";
    JSONObject agentsJson = doGetJson(url, 200);
    assertTrue("\"agents\" field is not present", agentsJson.has("agents"));
    JSONArray agentsArrayJson = agentsJson.getJSONArray("agents");
    int numAgents = agentsArrayJson.length();
    assertEquals("Number of agents", 0, numAgents);
    log.info("Agents: " + agentsJson + " agents array: " + agentsArrayJson + " numAgents: " + numAgents);
    
    url = baseUrl + "/agent_classes";
    JSONObject agentClassesJson = doGetJson(url, 200);
    assertTrue("\"agent_classes\" field is not present", agentClassesJson.has("agent_classes"));
    JSONArray agentClassesArrayJson = agentClassesJson.getJSONArray("agent_classes");
    int numAgentClasses = agentClassesArrayJson.length();
    assertEquals("Number of agents", 2, numAgentClasses);
    JSONObject agentClassJson = agentClassesArrayJson.getJSONObject(0);
    String expectedAgentClassName = "alpha";
    String agentClassName = agentClassJson.getString("name");
    assertEquals("Agent class[0]", expectedAgentClassName, agentClassName);
    String agentClassDescription = agentClassJson.getString("description");
    assertEquals("Agent class[0]", "The first sample agent class", agentClassDescription);
    agentClassJson = agentClassesArrayJson.getJSONObject(1);
    agentClassName = agentClassJson.getString("name");
    assertEquals("Agent class[1]", "beta", agentClassName);
    agentClassDescription = agentClassJson.getString("description");
    assertEquals("Agent class[1]", "The second sample agent class", agentClassDescription);
    log.info("Agent classes: " + agentClassesJson + " agent classes array: " + agentClassesArrayJson + " numAgentClasses: " + numAgentClasses);
    
    url = baseUrl + "/agent_classes/" + expectedAgentClassName;
    agentClassJson = doGetJson(url, 200);
    assertTrue("\"name\" field is not present", agentClassJson.has("name"));
    agentClassName = agentClassJson.getString("name");
    assertEquals("Agent class name", expectedAgentClassName, agentClassName);
    assertTrue("\"parameters\" field is not present", agentClassJson.has("parameters"));
    JSONArray agentParametersArrayJson = agentClassJson.getJSONArray("parameters");
    int numagentParameters = agentParametersArrayJson.length();
    assertEquals("Number of agent fields", 2, numagentParameters);
    JSONObject agentparameterJson = agentParametersArrayJson.getJSONObject(0);
    assertTrue("\"name\" field is not present for field[0]", agentparameterJson.has("name"));
    String agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [0]", "a1f1", agentFieldName);
    assertTrue("\"label\" field is not present for field[0]", agentparameterJson.has("label"));
    String agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [0]", "Label a1f1", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[0]", agentparameterJson.has("type"));
    String agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [0]", "string", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(1);
    assertTrue("\"name\" field is not present for field[1]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [1]", "a1f2", agentFieldName);
    assertTrue("\"label\" field is not present for field[1]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [1]", "Label a1f2", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[1]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [1]", "int", agentFieldType);
    log.info("Agent class alpha: " + agentClassJson + " numagentParameters: " + numagentParameters);

    expectedAgentClassName = "beta";
    url = baseUrl + "/agent_classes/" + expectedAgentClassName;
    agentClassJson = doGetJson(url, 200);
    assertTrue("\"name\" field is not present", agentClassJson.has("name"));
    agentClassName = agentClassJson.getString("name");
    assertEquals("Agent class name", expectedAgentClassName, agentClassName);
    assertTrue("\"parameters\" field is not present", agentClassJson.has("parameters"));
    agentParametersArrayJson = agentClassJson.getJSONArray("parameters");
    numagentParameters = agentParametersArrayJson.length();
    assertEquals("Number of agent fields", 10, numagentParameters);
    agentparameterJson = agentParametersArrayJson.getJSONObject(0);
    assertTrue("\"name\" field is not present for field[0]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [0]", "a2f1", agentFieldName);
    assertTrue("\"label\" field is not present for field[0]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [0]", "a2f1", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[0]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [0]", "text", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(1);
    assertTrue("\"name\" field is not present for field[1]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [1]", "a2f2", agentFieldName);
    assertTrue("\"label\" field is not present for field[1]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [1]", "Label a2f2", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[1]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [1]", "string", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(2);
    assertTrue("\"name\" field is not present for field[2]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [2]", "a2f3", agentFieldName);
    assertTrue("\"label\" field is not present for field[2]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [2]", "Label a2f3", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[2]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [2]", "int", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(3);
    assertTrue("\"name\" field is not present for field[3]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [3]", "a2f4", agentFieldName);
    assertTrue("\"label\" field is not present for field[3]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [3]", "Label a2f4", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[3]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [3]", "string", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(4);
    assertTrue("\"name\" field is not present for field[4]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [4]", "a2f5", agentFieldName);
    assertTrue("\"label\" field is not present for field[4]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [4]", "Label a2f5", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[4]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [4]", "boolean", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(5);
    assertTrue("\"name\" field is not present for field[5]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [5]", "a2f6", agentFieldName);
    assertTrue("\"label\" field is not present for field[5]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [5]", "Label a2f6", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[5]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [5]", "date", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(6);
    assertTrue("\"name\" field is not present for field[6]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [6]", "a2f7", agentFieldName);
    assertTrue("\"label\" field is not present for field[6]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [6]", "Label a2f7", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[6]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [6]", "money", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(7);
    assertTrue("\"name\" field is not present for field[7]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [7]", "a2f8", agentFieldName);
    assertTrue("\"label\" field is not present for field[7]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [7]", "Label a2f8", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[7]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [7]", "location", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(8);
    assertTrue("\"name\" field is not present for field[8]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [8]", "a2f9", agentFieldName);
    assertTrue("\"label\" field is not present for field[8]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [8]", "Label a2f9", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[8]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [8]", "choice", agentFieldType);
    agentparameterJson = agentParametersArrayJson.getJSONObject(9);
    assertTrue("\"name\" field is not present for field[9]", agentparameterJson.has("name"));
    agentFieldName = agentparameterJson.getString("name");
    assertEquals("Agent field name [9]", "a2f10", agentFieldName);
    assertTrue("\"label\" field is not present for field[9]", agentparameterJson.has("label"));
    agentFieldLabel = agentparameterJson.getString("label");
    assertEquals("Agent field label [9]", "Label a2f10", agentFieldLabel);
    assertTrue("\"type\" field is not present for field[9]", agentparameterJson.has("type"));
    agentFieldType = agentparameterJson.getString("type");
    assertEquals("Agent field type [9]", "multi_choice", agentFieldType);
    log.info("Agent class alpha: " + agentClassJson + " numagentParameters: " + numagentParameters);

    // Now create an actual agent
    String expectedAgentName = "My_Alpha";
    expectedAgentClassName = "alpha";
    String expectedAgentDescription = "My alpha agent";
    String encodedDescription = expectedAgentDescription.replace(' ', '+');
    url = baseUrl + "/users/" + expectedUserName + "/agents?name=" + expectedAgentName +
        "&class=" + expectedAgentClassName + "&description=" + encodedDescription;
    JSONObject agentJson = new JSONObject();
    JSONArray parameterValuesJson = new JSONArray();
    JSONObject parameterJson = new JSONObject();
    parameterJson.put("name", "a1f1");
    String initialValue1 = "Initial-value";
    parameterJson.put("value", initialValue1);
    parameterValuesJson.put(parameterJson);
    parameterJson = new JSONObject();
    parameterJson.put("name", "a1f2");
    int initialValue2 = 123;
    parameterJson.put("value", initialValue2);
    parameterValuesJson.put(parameterJson);
    agentJson.put("parameter_values", parameterValuesJson);
    JSONArray eventsArrayJson = new JSONArray();
    eventsArrayJson.put("event1");
    eventsArrayJson.put("event3");
    agentJson.put("events", eventsArrayJson);
    JSONArray dataSourcesArrayJson = new JSONArray();
    dataSourcesArrayJson.put("data_source2");
    dataSourcesArrayJson.put("data_source1");
    agentJson.put("inputs", dataSourcesArrayJson);
    agentJson.put("processing_script", "Do a little something, then do that, rinse, repeat");
    JSONArray outputsArrayJson = new JSONArray();
    JSONObject outputJson = new JSONObject();
    outputJson.put("name", "output1");
    outputJson.put("description", "The first output");
    outputJson.put("label", "First Output");
    outputJson.put("type", "string");
    outputJson.put("default_value", "abc");
    outputsArrayJson.put(outputJson);
    outputJson = new JSONObject();
    outputJson.put("name", "output2");
    outputJson.put("description", "The second output");
    outputJson.put("label", "Second Output");
    outputJson.put("type", "int");
    outputJson.put("default_value", 123);
    outputsArrayJson.put(outputJson);
    outputJson = new JSONObject();
    outputJson.put("name", "output3");
    outputJson.put("description", "The third output");
    outputJson.put("label", "Third Output");
    outputJson.put("type", "money");
    outputJson.put("default_value", 1000.23);
    outputsArrayJson.put(outputJson);
    outputJson = new JSONObject();
    outputJson.put("name", "output4");
    outputJson.put("label", "Fourth Output");
    outputJson.put("description", "The fourth output");
    outputJson.put("type", "float");
    outputJson.put("default_value", 3.14159);
    outputsArrayJson.put(outputJson);
    agentJson.put("outputs", outputsArrayJson);
    long expectedReportingInterval = 20000;
    agentJson.put("default_reporting_interval", expectedReportingInterval);
    JSONObject agentReturnJson = doPostJson(url, agentJson, 201);
    assertTrue("Unexpected JSON returned", agentReturnJson == null);

    url = baseUrl + "/users/" + expectedUserName + "/agents";
    agentsJson = doGetJson(url, 200);
    assertTrue("\"agents\" field is not present", agentsJson.has("agents"));
    agentsArrayJson = agentsJson.getJSONArray("agents");
    numAgents = agentsArrayJson.length();
    assertEquals("Number of agents", 1, numAgents);
    agentJson = agentsArrayJson.getJSONObject(0);
    assertTrue("Missing agent name", agentJson.has("name"));
    String agentName = agentJson.getString("name");
    assertEquals("Agent name", expectedAgentName, agentName);
    assertTrue("Missing agent class name", agentJson.has("class_name"));
    agentClassName = agentJson.getString("class_name");
    assertEquals("Agent class name", expectedAgentClassName, agentClassName);
    agentClassName = agentJson.getString("class_name");
    log.info("Agents for user " + expectedUserName + ": " + agentsJson + " agents array: " + agentsArrayJson + " numAgents: " + numAgents);

    // Ping the agent
    url = baseUrl + "/users/" + expectedUserName + "/agents/" + expectedAgentName;
    agentJson = doGetJson(url, 200);
    assertTrue("Agent JSON not returned", agentJson != null);
    assertTrue("Missing agent name", agentJson.has("name"));
    agentName = agentJson.getString("name");
    assertEquals("Agent name", expectedAgentName, agentName);
    assertTrue("Missing agent description", agentJson.has("description"));
    String agentDescription = agentJson.getString("description");
    assertEquals("Agent description", expectedAgentDescription, agentDescription);
    assertTrue("Missing agent time", agentJson.has("time"));
    String initialTime = agentJson.getString("time");
    assertTrue("Agent parameters not present", agentJson.has("parameters"));
    JSONArray agentParametersJson = agentJson.getJSONArray("parameters");
    int numParameters = agentParametersJson.length();
    assertEquals("Number of agent parameters", 2, numParameters);
    parameterJson = agentParametersJson.getJSONObject(0);
    assertTrue("Missing agent parameter name", parameterJson.has("name"));
    String parameterName = parameterJson.getString("name");
    assertEquals("Agent parameter name", "a1f1", parameterName);
    assertTrue("Missing agent parameter label", parameterJson.has("label"));
    String parameterLabel = parameterJson.getString("label");
    assertEquals("Agent parameter label", "Label a1f1", parameterLabel);
    assertTrue("Missing agent parameter type", parameterJson.has("type"));
    String parameterType = parameterJson.getString("type");
    assertEquals("Agent parameter type", "string", parameterType);
    parameterJson = agentParametersJson.getJSONObject(1);
    assertTrue("Missing agent parameter name", parameterJson.has("name"));
    parameterName = parameterJson.getString("name");
    assertEquals("Agent parameter name", "a1f2", parameterName);
    assertTrue("Missing agent parameter label", parameterJson.has("label"));
    parameterLabel = parameterJson.getString("label");
    assertEquals("Agent parameter label", "Label a1f2", parameterLabel);
    assertTrue("Missing agent parameter type", parameterJson.has("type"));
    parameterType = parameterJson.getString("type");
    assertEquals("Agent parameter type", "int", parameterType);
    assertTrue("Agent parameter_values not present", agentJson.has("parameter_values"));
    JSONObject agentParameterValuesJson = agentJson.getJSONObject("parameter_values");
    int numParameterValues = agentParameterValuesJson.length();
    assertEquals("Number of agent parameter_values", 2, numParameterValues);
    assertTrue("Agent parameter_values not present for a1f1", agentParameterValuesJson.has("a1f1"));
    assertEquals("Agent parameter_values for a1f1", initialValue1, agentParameterValuesJson.getString("a1f1"));
    assertTrue("Agent parameter_values not present for a1f1", agentParameterValuesJson.has("a1f2"));
    assertEquals("Agent parameter_values for a1f2", initialValue2, agentParameterValuesJson.get("a1f2"));
    assertTrue("Agent inputs not present", agentJson.has("inputs"));
    JSONArray inputsJson = agentJson.getJSONArray("inputs");
    int numInputs = inputsJson.length();
    assertEquals("Number of agent inputs", 2, numInputs);
    assertEquals("Agent input[0]", "data_source2", inputsJson.getString(0));
    assertEquals("Agent input[1]", "data_source1", inputsJson.getString(1));
    JSONArray agentEventsJson = agentJson.getJSONArray("events");
    int numEvents = agentEventsJson.length();
    assertEquals("Number of agent events", 2, numEvents);
    assertEquals("Agent event[0]", "event1", agentEventsJson.getString(0));
    assertEquals("Agent event[1]", "event3", agentEventsJson.getString(1));
    JSONArray outputsJson = agentJson.getJSONArray("outputs");
    int numOutputs = outputsJson.length();
    assertEquals("Number of agent outputs", 4, numOutputs);
    assertTrue("Agent output[0] is not a JSON object", outputsJson.get(0) instanceof JSONObject);
    outputJson = outputsJson.getJSONObject(0);
    assertTrue("\"name\" is not present for agent output[0]", outputJson.has("name"));
    assertTrue("\"type\" is not present for agent output[0]", outputJson.has("type"));
    assertTrue("\"description\" is not present for agent output[0]", outputJson.has("description"));
    assertTrue("\"label\" is not present for agent output[0]", outputJson.has("label"));
    assertTrue("\"default_value\" is not present for agent output[0]", outputJson.has("default_value"));
    assertEquals("Agent output[0] name", "output1", outputJson.getString("name"));
    assertEquals("Agent output[0] type", "string", outputJson.getString("type"));
    assertEquals("Agent output[0] label", "First Output", outputJson.getString("label"));
    assertEquals("Agent output[0] description", "The first output", outputJson.getString("description"));
    assertEquals("Agent output[0] default_value", "abc", outputJson.getString("default_value"));
    outputJson = outputsJson.getJSONObject(1);
    assertTrue("\"name\" is not present for agent output[1]", outputJson.has("name"));
    assertTrue("\"type\" is not present for agent output[1]", outputJson.has("type"));
    assertTrue("\"description\" is not present for agent output[1]", outputJson.has("description"));
    assertTrue("\"label\" is not present for agent output[1]", outputJson.has("label"));
    assertTrue("\"default_value\" is not present for agent output[1]", outputJson.has("default_value"));
    assertEquals("Agent output[1] name", "output2", outputJson.getString("name"));
    assertEquals("Agent output[1] type", "int", outputJson.getString("type"));
    assertEquals("Agent output[1] label", "Second Output", outputJson.getString("label"));
    assertEquals("Agent output[1] description", "The second output", outputJson.getString("description"));
    assertEquals("Agent output[1] default_value", "123", outputJson.getString("default_value"));
    outputJson = outputsJson.getJSONObject(2);
    assertTrue("\"name\" is not present for agent output[2]", outputJson.has("name"));
    assertTrue("\"type\" is not present for agent output[2]", outputJson.has("type"));
    assertTrue("\"description\" is not present for agent output[2]", outputJson.has("description"));
    assertTrue("\"label\" is not present for agent output[2]", outputJson.has("label"));
    assertTrue("\"default_value\" is not present for agent output[2]", outputJson.has("default_value"));
    assertEquals("Agent output[2] name", "output3", outputJson.getString("name"));
    assertEquals("Agent output[2] type", "money", outputJson.getString("type"));
    assertEquals("Agent output[2] label", "Third Output", outputJson.getString("label"));
    assertEquals("Agent output[2] description", "The third output", outputJson.getString("description"));
    assertEquals("Agent output[2] default_value", "1000.23", outputJson.getString("default_value"));
    outputJson = outputsJson.getJSONObject(3);
    assertTrue("\"name\" is not present for agent output[3]", outputJson.has("name"));
    assertTrue("\"type\" is not present for agent output[3]", outputJson.has("type"));
    assertTrue("\"description\" is not present for agent output[3]", outputJson.has("description"));
    assertTrue("\"label\" is not present for agent output[3]", outputJson.has("label"));
    assertTrue("\"default_value\" is not present for agent output[3]", outputJson.has("default_value"));
    assertEquals("Agent output[3] name", "output4", outputJson.getString("name"));
    assertEquals("Agent output[3] type", "float", outputJson.getString("type"));
    assertEquals("Agent output[3] label", "Fourth Output", outputJson.getString("label"));
    assertEquals("Agent output[3] description", "The fourth output", outputJson.getString("description"));
    assertEquals("Agent output[3] default_value", "3.14159", outputJson.getString("default_value"));
    assertTrue("Agent input_values missing", agentJson.has("input_values"));
    JSONObject inputValuesJson = agentJson.getJSONObject("input_values");
    assertEquals("Number of agent input_values", 2, inputValuesJson.length());
    assertTrue("Agent input_values missing data_source2", inputValuesJson.has("data_source2"));
    JSONObject dataSourceValuesJson = inputValuesJson.getJSONObject("data_source2");
    assertEquals("Agent input_values data_source2 attribute count", 3, dataSourceValuesJson.length());
    assertTrue("Agent input_values data_source2 missing attribute ds-attr2-1", dataSourceValuesJson.has("ds-attr2-1"));
    assertEquals("Agent input_values data_source2 attribute ds-attr2-1", "ds-value2-1", dataSourceValuesJson.getString("ds-attr2-1"));
    assertTrue("Agent input_values data_source2 missing attribute ds-attr2-2", dataSourceValuesJson.has("ds-attr2-2"));
    assertEquals("Agent input_values data_source2 attribute ds-attr2-2", "ds-value2-2", dataSourceValuesJson.getString("ds-attr2-2"));
    assertTrue("Agent input_values data_source2 missing attribute ds-attr2-3", dataSourceValuesJson.has("ds-attr2-3"));
    assertEquals("Agent input_values data_source2 attribute ds-attr2-3", "ds-value2-3", dataSourceValuesJson.getString("ds-attr2-3"));
    assertTrue("Agent input_values missing data_source1", inputValuesJson.has("data_source1"));
    dataSourceValuesJson = inputValuesJson.getJSONObject("data_source1");
    assertEquals("Agent input_values data_source1 attribute count", 2, dataSourceValuesJson.length());
    assertTrue("Agent input_values data_source1 missing attribute ds-attr1-1", dataSourceValuesJson.has("ds-attr1-1"));
    assertEquals("Agent input_values data_source1 attribute ds-attr1-1", "ds-value1-1", dataSourceValuesJson.getString("ds-attr1-1"));
    assertTrue("Agent input_values data_source1 missing attribute ds-attr1-2", dataSourceValuesJson.has("ds-attr1-2"));
    assertEquals("Agent input_values data_source1 attribute ds-attr1-2", "ds-value1-2", dataSourceValuesJson.getString("ds-attr1-2"));
    JSONObject eventsValuesJson = agentJson.getJSONObject("event_values");
    assertEquals("Number of agent event_values", 2, eventsValuesJson.length());
    assertTrue("Agent event_values missing event1", eventsValuesJson.has("event1"));
    JSONObject eventValuesJson = eventsValuesJson.getJSONObject("event1");
    assertEquals("Agent event_values event1 attribute count", 2, eventValuesJson.length());
    assertTrue("Agent event_values event1 missing attribute attr1-1", eventValuesJson.has("attr1-1"));
    assertEquals("Agent event_values event1 attribute attr1-1", "value1-1", eventValuesJson.getString("attr1-1"));
    assertTrue("Agent event_values event1 missing attribute attr1-2", eventValuesJson.has("attr1-2"));
    assertEquals("Agent event_values event1 attribute attr1-2", "value1-2", eventValuesJson.getString("attr1-2"));
    assertTrue("Agent event_values missing event3", eventsValuesJson.has("event3"));
    eventValuesJson = eventsValuesJson.getJSONObject("event3");
    assertEquals("Agent event_values event3 attribute count", 4, eventValuesJson.length());
    assertTrue("Agent event_values event3 missing attribute attr3-1", eventValuesJson.has("attr3-1"));
    assertEquals("Agent event_values event3 attribute attr3-1", "value3-1", eventValuesJson.getString("attr3-1"));
    assertTrue("Agent event_values event3 missing attribute attr3-2", eventValuesJson.has("attr3-2"));
    assertEquals("Agent event_values event3 attribute attr3-2", "value3-2", eventValuesJson.getString("attr3-2"));
    assertTrue("Agent event_values event3 missing attribute attr3-3", eventValuesJson.has("attr3-3"));
    assertEquals("Agent event_values event3 attribute attr3-3", "value3-3", eventValuesJson.getString("attr3-3"));
    assertTrue("Agent event_values event3 missing attribute attr3-4", eventValuesJson.has("attr3-4"));
    assertEquals("Agent event_values event3 attribute attr3-4", "value3-4", eventValuesJson.getString("attr3-4"));
    JSONObject outputValuesJson = agentJson.getJSONObject("output_values");
    assertEquals("Number of agent output_values", 4, outputValuesJson.length());
    assertTrue("Agent output_values missing attribute output1", outputValuesJson.has("output1"));
    assertEquals("Agent output_values attribute output1", "abc", outputValuesJson.get("output1"));
    assertTrue("Agent output_values missing attribute output2", outputValuesJson.has("output2"));
    assertEquals("Agent output_values attribute output2", 123, outputValuesJson.get("output2"));
    assertTrue("Agent output_values missing attribute output3", outputValuesJson.has("output3"));
    assertEquals("Agent output_values attribute output3", 1000.23, outputValuesJson.get("output3"));
    assertTrue("Agent output_values missing attribute output4", outputValuesJson.has("output4"));
    assertEquals("Agent output_values attribute output4", 3.14159, outputValuesJson.get("output4"));
    log.info("Agent " + expectedAgentName + " for user " + expectedUserName + ": " + agentJson + " agent fields array: " + agentParametersArrayJson + " numParameters: " + numParameters + " numInputs: " + numInputs + " numEvents: " + numEvents + " numOutputs: " + numOutputs);

    // Sleep a little so date/time will change
    Thread.sleep(10);
    
    // Update parameter values for agent
    agentJson = new JSONObject();
    parameterValuesJson = new JSONArray();
    parameterJson = new JSONObject();
    parameterJson.put("name", "a1f1");
    String expectedUpdatedValue1 = "updated-value";
    parameterJson.put("value", expectedUpdatedValue1);
    parameterValuesJson.put(parameterJson);
    parameterJson = new JSONObject();
    parameterJson.put("name", "a1f2");
    int expectedUpdatedValue2 = 4567;
    parameterJson.put("value", expectedUpdatedValue2);
    parameterValuesJson.put(parameterJson);
    agentJson.put("parameter_values", parameterValuesJson);
    JSONObject returnJson = doPutJson(url, agentJson, 201);
    assertTrue("PUT agent update returned JSON", returnJson == null);
    
    // Re-ping agent to see updated parameters
    agentJson = doGetJson(url, 200);
    assertTrue("Agent JSON not returned", agentJson != null);
    assertTrue("Missing agent name", agentJson.has("name"));
    agentName = agentJson.getString("name");
    assertEquals("Agent name", expectedAgentName, agentName);
    assertTrue("Missing agent time", agentJson.has("time"));
    String updatedDate = agentJson.getString("time");
    assertTrue("Agent time did not change on update: " + updatedDate, ! updatedDate.equals(initialTime));
    assertTrue("Agent fields not present", agentJson.has("parameters"));
    agentParametersJson = agentJson.getJSONArray("parameters");
    numParameters = agentParametersJson.length();
    assertEquals("Number of agent fields", 2, numParameters);
    parameterJson = agentParametersJson.getJSONObject(0);
    assertTrue("Missing agent field name", parameterJson.has("name"));
    parameterName = parameterJson.getString("name");
    assertEquals("Agent parameter name", "a1f1", parameterName);
    assertTrue("Missing agent parameter label", parameterJson.has("label"));
    parameterLabel = parameterJson.getString("label");
    assertEquals("Agent parameter label", "Label a1f1", parameterLabel);
    assertTrue("Missing agent parameter type", parameterJson.has("type"));
    parameterType = parameterJson.getString("type");
    assertEquals("Agent parameter type", "string", parameterType);
    parameterJson = agentParametersJson.getJSONObject(1);
    assertTrue("Missing agent parameter name", parameterJson.has("name"));
    parameterName = parameterJson.getString("name");
    assertEquals("Agent parameter name", "a1f2", parameterName);
    assertTrue("Missing agent parameter label", parameterJson.has("label"));
    parameterLabel = parameterJson.getString("label");
    assertEquals("Agent parameter label", "Label a1f2", parameterLabel);
    assertTrue("Missing agent parameter type", parameterJson.has("type"));
    parameterType = parameterJson.getString("type");
    assertEquals("Agent parameter type", "int", parameterType);
    agentParameterValuesJson = agentJson.getJSONObject("parameter_values");
    numParameterValues = agentParameterValuesJson.length();
    assertEquals("Number of agent parameter_values", 2, numParameterValues);
    assertTrue("Agent parameter_values not present for a1f1", agentParameterValuesJson.has("a1f1"));
    assertEquals("Agent parameter_values for a1f1", expectedUpdatedValue1, agentParameterValuesJson.getString("a1f1"));
    assertTrue("Agent parameter_values not present for a1f1", agentParameterValuesJson.has("a1f2"));
    assertEquals("Agent parameter_values for a1f2", expectedUpdatedValue2, agentParameterValuesJson.get("a1f2"));
    log.info("Updated agent " + expectedAgentName + " for user " + expectedUserName + ": " + agentJson + " agent parameters array: " + agentParametersArrayJson + " numParameters: " + numParameters);

    // Ping for agent definition only, no state
    String urlNoState = url + "?exclude_state=true";
    agentJson = doGetJson(urlNoState, 200);
    assertTrue("Agent JSON not returned", agentJson != null);
    assertTrue("Missing agent name", agentJson.has("name"));
    agentName = agentJson.getString("name");
    assertEquals("Agent name", expectedAgentName, agentName);
    assertTrue("Missing agent description", agentJson.has("description"));
    assertTrue("Missing agent class_name", agentJson.has("class_name"));
    assertTrue("Missing agent parameters", agentJson.has("parameters"));
    assertTrue("Missing agent inputs", agentJson.has("inputs"));
    assertTrue("Missing agent events", agentJson.has("events"));
    assertTrue("Missing agent outputs", agentJson.has("outputs"));
    assertFalse("Unexpected agent parameter_values", agentJson.has("parameter_values"));
    assertFalse("Unexpected agent input_values", agentJson.has("input_values"));
    assertFalse("Unexpected agent event_values", agentJson.has("event_values"));
    assertFalse("Unexpected agent output_values", agentJson.has("output_values"));

    // Ping for agent state only, no definition 
    String urlNoDefinition = url + "?exclude_definition=true";
    agentJson = doGetJson(urlNoDefinition, 200);
    assertTrue("Agent JSON not returned", agentJson != null);
    assertTrue("Missing agent name", agentJson.has("name"));
    agentName = agentJson.getString("name");
    assertEquals("Agent name", expectedAgentName, agentName);
    assertTrue("Missing agent description", agentJson.has("description"));
    assertTrue("Missing agent class_name", agentJson.has("class_name"));
    assertFalse("Unexpected agent parameters", agentJson.has("parameters"));
    assertFalse("Unexpected agent inputs", agentJson.has("inputs"));
    assertFalse("Unexpected agent events", agentJson.has("events"));
    assertFalse("Unexpected agent outputs", agentJson.has("outputs"));
    assertTrue("Missing agent parameter_values", agentJson.has("parameter_values"));
    assertTrue("Missing agent input_values", agentJson.has("input_values"));
    assertTrue("Missing agent event_values", agentJson.has("event_values"));
    assertTrue("Missing agent output_values", agentJson.has("output_values"));

    // Ping for agent header only, no definition or state 
    String urlNoDefinitionOrState = url + "?exclude_definition=true&exclude_state=true";
    agentJson = doGetJson(urlNoDefinitionOrState, 200);
    assertTrue("Agent JSON not returned", agentJson != null);
    assertTrue("Missing agent name", agentJson.has("name"));
    agentName = agentJson.getString("name");
    assertEquals("Agent name", expectedAgentName, agentName);
    assertTrue("Missing agent description", agentJson.has("description"));
    assertTrue("Missing agent class_name", agentJson.has("class_name"));
    assertFalse("Unexpected agent parameters", agentJson.has("parameters"));
    assertFalse("Unexpected agent inputs", agentJson.has("inputs"));
    assertFalse("Unexpected agent events", agentJson.has("events"));
    assertFalse("Unexpected agent outputs", agentJson.has("outputs"));
    assertFalse("Unexpected agent parameter_values", agentJson.has("parameter_values"));
    assertFalse("Unexpected agent input_values", agentJson.has("input_values"));
    assertFalse("Unexpected agent event_values", agentJson.has("event_values"));
    assertFalse("Unexpected agent output_values", agentJson.has("output_values"));

    // Test query of event list
    url = baseUrl + "/events";
    JSONObject eventsJson = doGetJson(url, 200);
    assertTrue("\"events\" field is not present", eventsJson.has("events"));
    eventsArrayJson = eventsJson.getJSONArray("events");
    numEvents = eventsArrayJson.length();
    assertEquals("Number of events", 3, numEvents);
    JSONObject eventJson = eventsArrayJson.getJSONObject(0);
    assertTrue("\"name\" field is not present", eventJson.has("name"));
    String eventName = eventJson.getString("name");
    assertEquals("Events[0] name", "event1", eventName);
    assertTrue("\"description\" field is not present", eventJson.has("description"));
    String eventDescription = eventJson.getString("description");
    assertEquals("Events[0] description", "First dummy event", eventDescription);
    assertTrue("\"enabled\" field is not present", eventJson.has("enabled"));
    Object enabled = eventJson.get("enabled");
    assertEquals("Events[0] enabled", true, enabled);
    assertTrue("\"attributes\" field is not present", eventJson.has("attributes"));
    JSONArray attributesJson = eventJson.getJSONArray("attributes");
    assertEquals("Event[0] \"attributes\" size", 2, attributesJson.length());
    JSONObject attributeJson = attributesJson.getJSONObject(0);
    assertTrue("Event[0] name attribute is not present", attributeJson.has("name"));
    assertEquals("Event[0] name attribute", "attr1-1", attributeJson.getString("name"));
    assertTrue("Event[0] type attribute is not present", attributeJson.has("type"));
    assertEquals("Event[0] type attribute", "string", attributeJson.getString("type"));
    assertTrue("Event[0] label attribute is not present", attributeJson.has("label"));
    assertEquals("Event[0] label attribute", "Attr #1 for Event #1", attributeJson.getString("label"));
    assertTrue("Event[0] description attribute is not present", attributeJson.has("description"));
    assertEquals("Event[0] description attribute", "First Attribute for event #1", attributeJson.getString("description"));
    assertTrue("Event[0] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("Event[0] default_value attribute", "value1-1", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(1);
    assertTrue("Event[0] name attribute is not present", attributeJson.has("name"));
    assertEquals("Event[0] name attribute", "attr1-2", attributeJson.getString("name"));
    assertTrue("Event[0] type attribute is not present", attributeJson.has("type"));
    assertEquals("Event[0] type attribute", "string", attributeJson.getString("type"));
    assertTrue("Event[0] label attribute is not present", attributeJson.has("label"));
    assertEquals("Event[0] label attribute", "Attr #2 for Event #1", attributeJson.getString("label"));
    assertTrue("Event[0] description attribute is not present", attributeJson.has("description"));
    assertEquals("Event[0] description attribute", "Second Attribute for event #1", attributeJson.getString("description"));
    assertTrue("Event[0] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("Event[0] default_value attribute", "value1-2", attributeJson.getString("default_value"));
    assertTrue("\"attribute_values\" field is not present", eventJson.has("attribute_values"));
    JSONObject attributeValuesJson = eventJson.getJSONObject("attribute_values");
    assertEquals("Event[0] \"attribute_values\" size", 2, attributeValuesJson.length());
    assertTrue("Event[0] attribute value for attr1-1 is not present", attributeValuesJson.has("attr1-1"));
    assertEquals("Event[0] attribute value for attr1-1", "value1-1", attributeValuesJson.getString("attr1-1"));
    assertTrue("Event[0] attribute value for attr1-2 is not present", attributeValuesJson.has("attr1-2"));
    assertEquals("Event[0] attribute value for attr1-2", "value1-2", attributeValuesJson.getString("attr1-2"));
    eventJson = eventsArrayJson.getJSONObject(1);
    assertTrue("\"name\" field is not present", eventJson.has("name"));
    eventName = eventJson.getString("name");
    assertEquals("Events[1] name", "event2", eventName);
    assertTrue("\"description\" field is not present", eventJson.has("description"));
    eventDescription = eventJson.getString("description");
    assertEquals("Events[1] description", "Second dummy event", eventDescription);
    assertTrue("\"enabled\" field is not present", eventJson.has("enabled"));
    enabled = eventJson.get("enabled");
    assertEquals("Events[1] enabled", true, enabled);
    assertTrue("\"attributes\" field is not present", eventJson.has("attributes"));
    attributesJson = eventJson.getJSONArray("attributes");
    assertEquals("Event[1] \"attributes\" size", 3, attributesJson.length());
    attributeJson = attributesJson.getJSONObject(0);
    assertTrue("Event[1] name attribute is not present", attributeJson.has("name"));
    assertEquals("Event[1] name attribute", "attr2-1", attributeJson.getString("name"));
    assertTrue("Event[1] type attribute is not present", attributeJson.has("type"));
    assertEquals("Event[1] type attribute", "string", attributeJson.getString("type"));
    assertTrue("Event[1] label attribute is not present", attributeJson.has("label"));
    assertEquals("Event[1] label attribute", "Attr #1 for Event #2", attributeJson.getString("label"));
    assertTrue("Event[1] description attribute is not present", attributeJson.has("description"));
    assertEquals("Event[1] description attribute", "First Attribute for Event #2", attributeJson.getString("description"));
    assertTrue("Event[1] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("Event[1] default_value attribute", "value2-1", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(1);
    assertTrue("Event[1] name attribute is not present", attributeJson.has("name"));
    assertEquals("Event[1] name attribute", "attr2-2", attributeJson.getString("name"));
    assertTrue("Event[1] type attribute is not present", attributeJson.has("type"));
    assertEquals("Event[1] type attribute", "string", attributeJson.getString("type"));
    assertTrue("Event[1] label attribute is not present", attributeJson.has("label"));
    assertEquals("Event[1] label attribute", "Attr #2 for Event #2", attributeJson.getString("label"));
    assertTrue("Event[1] description attribute is not present", attributeJson.has("description"));
    assertEquals("Event[1] description attribute", "Second Attribute for Event #2", attributeJson.getString("description"));
    assertTrue("Event[1] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("Event[1] default_value attribute", "value2-2", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(2);
    assertTrue("Event[1] name attribute is not present", attributeJson.has("name"));
    assertEquals("Event[1] name attribute", "attr2-3", attributeJson.getString("name"));
    assertTrue("Event[1] type attribute is not present", attributeJson.has("type"));
    assertEquals("Event[1] type attribute", "string", attributeJson.getString("type"));
    assertTrue("Event[1] label attribute is not present", attributeJson.has("label"));
    assertEquals("Event[1] label attribute", "Attr #3 for Event #2", attributeJson.getString("label"));
    assertTrue("Event[1] description attribute is not present", attributeJson.has("description"));
    assertEquals("Event[1] description attribute", "Third Attribute for Event #2", attributeJson.getString("description"));
    assertTrue("Event[1] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("Event[1] default_value attribute", "value2-3", attributeJson.getString("default_value"));
    attributeValuesJson = eventJson.getJSONObject("attribute_values");
    assertEquals("Event[1] \"attribute_values\" size", 3, attributeValuesJson.length());
    assertTrue("Event[1] attribute value for attr2-1 is not present", attributeValuesJson.has("attr2-1"));
    assertEquals("Event[1] attribute value for attr2-1", "value2-1", attributeValuesJson.getString("attr2-1"));
    assertTrue("Event[1] attribute value for attr2-2 is not present", attributeValuesJson.has("attr2-2"));
    assertEquals("Event[1] attribute value for attr2-2", "value2-2", attributeValuesJson.getString("attr2-2"));
    assertTrue("Event[1] attribute value for attr2-3 is not present", attributeValuesJson.has("attr2-3"));
    assertEquals("Event[1] attribute value for attr2-3", "value2-3", attributeValuesJson.getString("attr2-3"));
    eventJson = eventsArrayJson.getJSONObject(2);
    assertTrue("\"name\" field is not present", eventJson.has("name"));
    eventName = eventJson.getString("name");
    assertEquals("Events[2] name", "event3", eventName);
    eventDescription = eventJson.getString("description");
    assertEquals("Events[2] description", "Third dummy event", eventDescription);
    assertTrue("\"enabled\" field is not present", eventJson.has("enabled"));
    enabled = eventJson.get("enabled");
    assertEquals("Events[2] enabled", true, enabled);
    assertTrue("\"attributes\" field is not present", eventJson.has("attributes"));
    attributesJson = eventJson.getJSONArray("attributes");
    assertEquals("Event[2] \"attributes\" size", 4, attributesJson.length());
    attributeJson = attributesJson.getJSONObject(0);
    assertTrue("Event[2] name attribute is not present", attributeJson.has("name"));
    assertEquals("Event[2] name attribute", "attr3-1", attributeJson.getString("name"));
    assertTrue("Event[2] type attribute is not present", attributeJson.has("type"));
    assertEquals("Event[2] type attribute", "string", attributeJson.getString("type"));
    assertTrue("Event[2] label attribute is not present", attributeJson.has("label"));
    assertEquals("Event[2] label attribute", "Attr #1 for Event #3", attributeJson.getString("label"));
    assertTrue("Event[2] description attribute is not present", attributeJson.has("description"));
    assertEquals("Event[2] description attribute", "First Attribute for Event #3", attributeJson.getString("description"));
    assertTrue("Event[2] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("Event[2] default_value attribute", "value3-1", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(1);
    assertTrue("Event[2] name attribute is not present", attributeJson.has("name"));
    assertEquals("Event[2] name attribute", "attr3-2", attributeJson.getString("name"));
    assertTrue("Event[2] type attribute is not present", attributeJson.has("type"));
    assertEquals("Event[2] type attribute", "string", attributeJson.getString("type"));
    assertTrue("Event[2] label attribute is not present", attributeJson.has("label"));
    assertEquals("Event[2] label attribute", "Attr #2 for Event #3", attributeJson.getString("label"));
    assertTrue("Event[2] description attribute is not present", attributeJson.has("description"));
    assertEquals("Event[2] description attribute", "Second Attribute for Event #3", attributeJson.getString("description"));
    assertTrue("Event[2] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("Event[2] default_value attribute", "value3-2", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(2);
    assertTrue("Event[2] name attribute is not present", attributeJson.has("name"));
    assertEquals("Event[2] name attribute", "attr3-3", attributeJson.getString("name"));
    assertTrue("Event[2] type attribute is not present", attributeJson.has("type"));
    assertEquals("Event[2] type attribute", "string", attributeJson.getString("type"));
    assertTrue("Event[2] label attribute is not present", attributeJson.has("label"));
    assertEquals("Event[2] label attribute", "Attr #3 for Event #3", attributeJson.getString("label"));
    assertTrue("Event[2] description attribute is not present", attributeJson.has("description"));
    assertEquals("Event[2] description attribute", "Third Attribute for Event #3", attributeJson.getString("description"));
    assertTrue("Event[2] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("Event[2] default_value attribute", "value3-3", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(3);
    assertTrue("Event[2] name attribute is not present", attributeJson.has("name"));
    assertEquals("Event[2] name attribute", "attr3-4", attributeJson.getString("name"));
    assertTrue("Event[2] type attribute is not present", attributeJson.has("type"));
    assertEquals("Event[2] type attribute", "string", attributeJson.getString("type"));
    assertTrue("Event[2] label attribute is not present", attributeJson.has("label"));
    assertEquals("Event[2] label attribute", "Attr #4 for Event #3", attributeJson.getString("label"));
    assertTrue("Event[2] description attribute is not present", attributeJson.has("description"));
    assertEquals("Event[2] description attribute", "Fourth Attribute for Event #3", attributeJson.getString("description"));
    assertTrue("Event[2] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("Event[2] default_value attribute", "value3-4", attributeJson.getString("default_value"));
    attributeValuesJson = eventJson.getJSONObject("attribute_values");
    assertEquals("Event[2] \"attribute_values\" size", 4, attributeValuesJson.length());
    assertTrue("Event[2] attribute value for attr3-1 is not present", attributeValuesJson.has("attr3-1"));
    assertEquals("Event[2] attribute value for attr3-1", "value3-1", attributeValuesJson.getString("attr3-1"));
    assertTrue("Event[2] attribute value for attr3-2 is not present", attributeValuesJson.has("attr3-2"));
    assertEquals("Event[2] attribute value for attr3-2", "value3-2", attributeValuesJson.getString("attr3-2"));
    assertTrue("Event[2] attribute value for attr3-3 is not present", attributeValuesJson.has("attr3-3"));
    assertEquals("Event[2] attribute value for attr3-3", "value3-3", attributeValuesJson.getString("attr3-3"));
    assertTrue("Event[2] attribute value for attr3-4 is not present", attributeValuesJson.has("attr3-4"));
    assertEquals("Event[2] attribute value for attr3-4", "value3-4", attributeValuesJson.getString("attr3-4"));
    log.info("Events: " + eventsJson + " Users array: " + usersArrayJson + " numEvents: " + numEvents);
    
    // Test query of data source list
    url = baseUrl + "/data_sources";
    JSONObject dataSourcesJson = doGetJson(url, 200);
    assertTrue("\"data_sources\" field is not present", dataSourcesJson.has("data_sources"));
    dataSourcesArrayJson = dataSourcesJson.getJSONArray("data_sources");
    int numDataSources = dataSourcesArrayJson.length();
    assertEquals("Number of data sources", 3, numDataSources);
    JSONObject dataSourceJson = dataSourcesArrayJson.getJSONObject(0);
    assertTrue("\"name\" field is not present", dataSourceJson.has("name"));
    String dataSourceName = dataSourceJson.getString("name");
    assertEquals("DataSources[0] name", "data_source1", dataSourceName);
    assertTrue("\"description\" field is not present", dataSourceJson.has("description"));
    String dataSourceDescription = dataSourceJson.getString("description");
    assertEquals("DataSources[0] description", "First dummy data source", dataSourceDescription);
    assertTrue("\"enabled\" field is not present", dataSourceJson.has("enabled"));
    assertEquals("DataSources[0] enabled", true, dataSourceJson.get("enabled"));
    assertTrue("\"attributes\" field is not present", eventJson.has("attributes"));
    attributesJson = dataSourceJson.getJSONArray("attributes");
    assertEquals("DataSource[0] \"attributes\" size", 2, attributesJson.length());
    attributeJson = attributesJson.getJSONObject(0);
    assertTrue("DataSource[0] name attribute is not present", attributeJson.has("name"));
    assertEquals("DataSource[0] name attribute", "ds-attr1-1", attributeJson.getString("name"));
    assertTrue("DataSource[0] type attribute is not present", attributeJson.has("type"));
    assertEquals("DataSource[0] type attribute", "string", attributeJson.getString("type"));
    assertTrue("DataSource[0] label attribute is not present", attributeJson.has("label"));
    assertEquals("DataSource[0] label attribute", "Attr #1 for DataSource #1", attributeJson.getString("label"));
    assertTrue("DataSource[0] description attribute is not present", attributeJson.has("description"));
    assertEquals("DataSource[0] description attribute", "First Attribute for dataSource #1", attributeJson.getString("description"));
    assertTrue("DataSource[0] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("DataSource[0] default_value attribute", "ds-value1-1", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(1);
    assertTrue("DataSource[0] name attribute is not present", attributeJson.has("name"));
    assertEquals("DataSource[0] name attribute", "ds-attr1-2", attributeJson.getString("name"));
    assertTrue("DataSource[0] type attribute is not present", attributeJson.has("type"));
    assertEquals("DataSource[0] type attribute", "string", attributeJson.getString("type"));
    assertTrue("DataSource[0] label attribute is not present", attributeJson.has("label"));
    assertEquals("DataSource[0] label attribute", "Attr #2 for DataSource #1", attributeJson.getString("label"));
    assertTrue("DataSource[0] description attribute is not present", attributeJson.has("description"));
    assertEquals("DataSource[0] description attribute", "Second Attribute for DataSource #1", attributeJson.getString("description"));
    assertTrue("DataSource[0] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("DataSource[0] default_value attribute", "ds-value1-2", attributeJson.getString("default_value"));
    assertTrue("\"attribute_values\" field is not present", dataSourceJson.has("attribute_values"));
    attributeValuesJson = dataSourceJson.getJSONObject("attribute_values");
    assertEquals("DataSource[0] \"ds-attribute_values\" size", 2, attributeValuesJson.length());
    assertTrue("DataSource[0] attribute value for ds-attr1-1 is not present", attributeValuesJson.has("ds-attr1-1"));
    assertEquals("DataSource[0] attribute value for ds-attr1-1", "ds-value1-1", attributeValuesJson.getString("ds-attr1-1"));
    assertTrue("DataSource[0] attribute value for ds-attr1-2 is not present", attributeValuesJson.has("ds-attr1-2"));
    assertEquals("DataSource[0] attribute value for ds-attr1-2", "ds-value1-2", attributeValuesJson.getString("ds-attr1-2"));
    dataSourceJson = dataSourcesArrayJson.getJSONObject(1);
    assertTrue("\"name\" field is not present", dataSourceJson.has("name"));
    dataSourceName = dataSourceJson.getString("name");
    assertEquals("DataSources[1] name", "data_source2", dataSourceName);
    assertTrue("\"description\" field is not present", dataSourceJson.has("description"));
    dataSourceDescription = dataSourceJson.getString("description");
    assertEquals("DataSources[1] description", "Second dummy data source", dataSourceDescription);
    assertTrue("\"enabled\" field is not present", dataSourceJson.has("enabled"));
    assertEquals("DataSources[1] enabled", true, dataSourceJson.get("enabled"));
    assertTrue("\"attributes\" field is not present", eventJson.has("attributes"));
    attributesJson = dataSourceJson.getJSONArray("attributes");
    assertEquals("DataSource[1] \"attributes\" size", 3, attributesJson.length());
    attributeJson = attributesJson.getJSONObject(0);
    assertTrue("DataSource[1] name attribute is not present", attributeJson.has("name"));
    assertEquals("DataSource[1] name attribute", "ds-attr2-1", attributeJson.getString("name"));
    assertTrue("DataSource[1] type attribute is not present", attributeJson.has("type"));
    assertEquals("DataSource[1] type attribute", "string", attributeJson.getString("type"));
    assertTrue("DataSource[1] label attribute is not present", attributeJson.has("label"));
    assertEquals("DataSource[1] label attribute", "Attr #1 for DataSource #2", attributeJson.getString("label"));
    assertTrue("DataSource[1] description attribute is not present", attributeJson.has("description"));
    assertEquals("DataSource[1] description attribute", "First Attribute for DataSource #2", attributeJson.getString("description"));
    assertTrue("DataSource[1] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("DataSource[1] default_value attribute", "ds-value2-1", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(1);
    assertTrue("DataSource[1] name attribute is not present", attributeJson.has("name"));
    assertEquals("DataSource[1] name attribute", "ds-attr2-2", attributeJson.getString("name"));
    assertTrue("DataSource[1] type attribute is not present", attributeJson.has("type"));
    assertEquals("DataSource[1] type attribute", "string", attributeJson.getString("type"));
    assertTrue("DataSource[1] label attribute is not present", attributeJson.has("label"));
    assertEquals("DataSource[1] label attribute", "Attr #2 for DataSource #2", attributeJson.getString("label"));
    assertTrue("DataSource[1] description attribute is not present", attributeJson.has("description"));
    assertEquals("DataSource[1] description attribute", "Second Attribute for DataSource #2", attributeJson.getString("description"));
    assertTrue("DataSource[1] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("DataSource[1] default_value attribute", "ds-value2-2", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(2);
    assertTrue("DataSource[1] name attribute is not present", attributeJson.has("name"));
    assertEquals("DataSource[1] name attribute", "ds-attr2-3", attributeJson.getString("name"));
    assertTrue("DataSource[1] type attribute is not present", attributeJson.has("type"));
    assertEquals("DataSource[1] type attribute", "string", attributeJson.getString("type"));
    assertTrue("DataSource[1] label attribute is not present", attributeJson.has("label"));
    assertEquals("DataSource[1] label attribute", "Attr #3 for DataSource #2", attributeJson.getString("label"));
    assertTrue("DataSource[1] description attribute is not present", attributeJson.has("description"));
    assertEquals("DataSource[1] description attribute", "Third Attribute for DataSource #2", attributeJson.getString("description"));
    assertTrue("DataSource[1] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("DataSource[1] default_value attribute", "ds-value2-3", attributeJson.getString("default_value"));
    attributeValuesJson = dataSourceJson.getJSONObject("attribute_values");
    assertEquals("DataSource[1] \"attribute_values\" size", 3, attributeValuesJson.length());
    assertTrue("DataSource[1] attribute value for ds-attr2-1 is not present", attributeValuesJson.has("ds-attr2-1"));
    assertEquals("DataSource[1] attribute value for ds-attr2-1", "ds-value2-1", attributeValuesJson.getString("ds-attr2-1"));
    assertTrue("DataSource[1] attribute value for ds-attr2-2 is not present", attributeValuesJson.has("ds-attr2-2"));
    assertEquals("DataSource[1] attribute value for ds-attr2-2", "ds-value2-2", attributeValuesJson.getString("ds-attr2-2"));
    assertTrue("DataSource[1] attribute value for ds-attr2-3 is not present", attributeValuesJson.has("ds-attr2-3"));
    assertEquals("DataSource[1] attribute value for ds-attr2-3", "ds-value2-3", attributeValuesJson.getString("ds-attr2-3"));
    dataSourceJson = dataSourcesArrayJson.getJSONObject(2);
    assertTrue("\"name\" field is not present", dataSourceJson.has("name"));
    dataSourceName = dataSourceJson.getString("name");
    assertEquals("DataSources[2] name", "data_source3", dataSourceName);
    dataSourceDescription = dataSourceJson.getString("description");
    assertEquals("DataSources[2] description", "Third dummy data source", dataSourceDescription);
    assertTrue("\"enabled\" field is not present", dataSourceJson.has("enabled"));
    assertEquals("DataSources[2] enabled", true, dataSourceJson.get("enabled"));
    assertTrue("\"attributes\" field is not present", eventJson.has("attributes"));
    attributesJson = dataSourceJson.getJSONArray("attributes");
    assertEquals("DataSource[2] \"ds-attributes\" size", 4, attributesJson.length());
    attributeJson = attributesJson.getJSONObject(0);
    assertTrue("DataSource[2] name attribute is not present", attributeJson.has("name"));
    assertEquals("DataSource[2] name attribute", "ds-attr3-1", attributeJson.getString("name"));
    assertTrue("DataSource[2] type attribute is not present", attributeJson.has("type"));
    assertEquals("DataSource[2] type attribute", "string", attributeJson.getString("type"));
    assertTrue("DataSource[2] label attribute is not present", attributeJson.has("label"));
    assertEquals("DataSource[2] label attribute", "Attr #1 for DataSource #3", attributeJson.getString("label"));
    assertTrue("DataSource[2] description attribute is not present", attributeJson.has("description"));
    assertEquals("DataSource[2] description attribute", "First Attribute for DataSource #3", attributeJson.getString("description"));
    assertTrue("DataSource[2] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("DataSource[2] default_value attribute", "ds-value3-1", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(1);
    assertTrue("DataSource[2] name attribute is not present", attributeJson.has("name"));
    assertEquals("DataSource[2] name attribute", "ds-attr3-2", attributeJson.getString("name"));
    assertTrue("DataSource[2] type attribute is not present", attributeJson.has("type"));
    assertEquals("DataSource[2] type attribute", "string", attributeJson.getString("type"));
    assertTrue("DataSource[2] label attribute is not present", attributeJson.has("label"));
    assertEquals("DataSource[2] label attribute", "Attr #2 for DataSource #3", attributeJson.getString("label"));
    assertTrue("DataSource[2] description attribute is not present", attributeJson.has("description"));
    assertEquals("DataSource[2] description attribute", "Second Attribute for DataSource #3", attributeJson.getString("description"));
    assertTrue("DataSource[2] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("DataSource[2] default_value attribute", "ds-value3-2", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(2);
    assertTrue("DataSource[2] name attribute is not present", attributeJson.has("name"));
    assertEquals("DataSource[2] name attribute", "ds-attr3-3", attributeJson.getString("name"));
    assertTrue("DataSource[2] type attribute is not present", attributeJson.has("type"));
    assertEquals("DataSource[2] type attribute", "string", attributeJson.getString("type"));
    assertTrue("DataSource[2] label attribute is not present", attributeJson.has("label"));
    assertEquals("DataSource[2] label attribute", "Attr #3 for DataSource #3", attributeJson.getString("label"));
    assertTrue("DataSource[2] description attribute is not present", attributeJson.has("description"));
    assertEquals("DataSource[2] description attribute", "Third Attribute for DataSource #3", attributeJson.getString("description"));
    assertTrue("DataSource[2] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("DataSource[2] default_value attribute", "ds-value3-3", attributeJson.getString("default_value"));
    attributeJson = attributesJson.getJSONObject(3);
    assertTrue("DataSource[2] name attribute is not present", attributeJson.has("name"));
    assertEquals("DataSource[2] name attribute", "ds-attr3-4", attributeJson.getString("name"));
    assertTrue("DataSource[2] type attribute is not present", attributeJson.has("type"));
    assertEquals("DataSource[2] type attribute", "string", attributeJson.getString("type"));
    assertTrue("DataSource[2] label attribute is not present", attributeJson.has("label"));
    assertEquals("DataSource[2] label attribute", "Attr #4 for DataSource #3", attributeJson.getString("label"));
    assertTrue("DataSource[2] description attribute is not present", attributeJson.has("description"));
    assertEquals("DataSource[2] description attribute", "Fourth Attribute for DataSource #3", attributeJson.getString("description"));
    assertTrue("DataSource[2] default_value attribute is not present", attributeJson.has("default_value"));
    assertEquals("DataSource[2] default_value attribute", "ds-value3-4", attributeJson.getString("default_value"));
    attributeValuesJson = dataSourceJson.getJSONObject("attribute_values");
    assertEquals("DataSource[2] \"attribute_values\" size", 4, attributeValuesJson.length());
    assertTrue("DataSource[2] attribute value for ds-attr3-1 is not present", attributeValuesJson.has("ds-attr3-1"));
    assertEquals("DataSource[2] attribute value for ds-attr3-1", "ds-value3-1", attributeValuesJson.getString("ds-attr3-1"));
    assertTrue("DataSource[2] attribute value for ds-attr3-2 is not present", attributeValuesJson.has("ds-attr3-2"));
    assertEquals("DataSource[2] attribute value for ds-attr3-2", "ds-value3-2", attributeValuesJson.getString("ds-attr3-2"));
    assertTrue("DataSource[2] attribute value for ds-attr3-3 is not present", attributeValuesJson.has("ds-attr3-3"));
    assertEquals("DataSource[2] attribute value for ds-attr3-3", "ds-value3-3", attributeValuesJson.getString("ds-attr3-3"));
    assertTrue("DataSource[2] attribute value for ds-attr3-4 is not present", attributeValuesJson.has("ds-attr3-4"));
    assertEquals("DataSource[2] attribute value for ds-attr3-4", "ds-value3-4", attributeValuesJson.getString("ds-attr3-4"));
    log.info("Data sources: " + dataSourcesJson + " data sources array: " + usersArrayJson + " numDataSources: " + numDataSources);
    
    // Add a custom agent class
    url = baseUrl + "/agent_classes?name=custom-agent-class";
    agentClassJson = new JSONObject();
    agentClassJson.put("description", "First custom agent class");
    JSONArray agentClassParametersArrayJson = new JSONArray();
    JSONObject agentParameter = new JSONObject();
    agentParameter.put("name", "custom-param1-1");
    agentParameter.put("description", "First custom parameter");
    agentParameter.put("label", "First");
    agentParameter.put("type", "string");
    agentParameter.put("default_value", "abc");
    agentClassParametersArrayJson.put(agentParameter);
    agentParameter = new JSONObject();
    agentParameter.put("name", "custom-param1-2");
    agentParameter.put("description", "Second custom parameter");
    agentParameter.put("label", "Second");
    agentParameter.put("type", "int");
    agentParameter.put("default_value", 123);
    agentClassParametersArrayJson.put(agentParameter);
    agentClassJson.put("parameters", agentClassParametersArrayJson);
    JSONObject agentClassReturnJson = doPostJson(url, agentClassJson, 201);
    assertTrue("Unexpected JSON returned", agentClassReturnJson == null);
    
    // Verify that the new agent class was created
    url = baseUrl + "/agent_classes";
    agentClassesJson = doGetJson(url, 200);
    agentClassesArrayJson = agentClassesJson.getJSONArray("agent_classes");
    assertEquals("Number of agent classes", 3, agentClassesArrayJson.length());
    agentClassJson = agentClassesArrayJson.getJSONObject(2);
    assertEquals("Custom agent class name", "custom-agent-class", agentClassJson.getString("name"));
    assertEquals("Custom agent class description", "First custom agent class", agentClassJson.getString("description"));
    url = baseUrl + "/agent_classes/custom-agent-class";
    agentClassJson = doGetJson(url, 200);
    assertEquals("Custom agent class name", "custom-agent-class", agentClassJson.getString("name"));
    assertEquals("Custom agent class description", "First custom agent class", agentClassJson.getString("description"));
    assertTrue("Missing custom class parameters field", agentClassJson.has("parameters"));
    JSONArray agentClassParametersJson = agentClassJson.getJSONArray("parameters");
    assertEquals("Number of parameters for custom agent class", 2, agentClassParametersJson.length());
    JSONObject agentClassParameter = agentClassParametersJson.getJSONObject(0);
    assertTrue("Missing custom class parameter name field", agentClassParameter.has("name"));
    assertEquals("Custom class parameter name", "custom-param1-1", agentClassParameter.getString("name"));
    assertTrue("Missing custom class parameter description field", agentClassParameter.has("description"));
    assertEquals("Custom class parameter description", "First custom parameter", agentClassParameter.getString("description"));
    assertTrue("Missing custom class parameter label field", agentClassParameter.has("label"));
    assertEquals("Custom class parameter label", "First", agentClassParameter.getString("label"));
    assertTrue("Missing custom class parameter type field", agentClassParameter.has("type"));
    assertEquals("Custom class parameter type", "string", agentClassParameter.getString("type"));
    assertTrue("Missing custom class parameter default_value field", agentClassParameter.has("default_value"));
    assertEquals("Custom class parameter default_value", "abc", agentClassParameter.get("default_value"));
    agentClassParameter = agentClassParametersJson.getJSONObject(1);
    assertTrue("Missing custom class parameter name field", agentClassParameter.has("name"));
    assertEquals("Custom class parameter name", "custom-param1-2", agentClassParameter.getString("name"));
    assertTrue("Missing custom class parameter description field", agentClassParameter.has("description"));
    assertEquals("Custom class parameter description", "Second custom parameter", agentClassParameter.getString("description"));
    assertTrue("Missing custom class parameter label field", agentClassParameter.has("label"));
    assertEquals("Custom class parameter label", "Second", agentClassParameter.getString("label"));
    assertTrue("Missing custom class parameter type field", agentClassParameter.has("type"));
    assertEquals("Custom class parameter type", "int", agentClassParameter.getString("type"));
    assertTrue("Missing custom class parameter default_value field", agentClassParameter.has("default_value"));
    assertEquals("Custom class parameter default_value", 123, agentClassParameter.get("default_value"));

    // Create an agent for the new custom class
    expectedAgentName = "My-custom-agent";
    expectedAgentClassName = "custom-agent-class";
    expectedAgentDescription = "My custom agent";
    encodedDescription = expectedAgentDescription.replace(' ', '+');
    url = baseUrl + "/users/" + expectedUserName + "/agents?name=" + expectedAgentName +
        "&class=" + expectedAgentClassName + "&description=" + encodedDescription;
    agentJson = new JSONObject();
    parameterValuesJson = new JSONArray();
    parameterJson = new JSONObject();
    parameterJson.put("name", "custom-param1-1");
    initialValue1 = "Initial-value";
    parameterJson.put("value", initialValue1);
    parameterValuesJson.put(parameterJson);
    parameterJson = new JSONObject();
    parameterJson.put("name", "custom-param1-2");
    initialValue2 = 456;
    parameterJson.put("value", initialValue2);
    parameterValuesJson.put(parameterJson);
    agentJson.put("parameter_values", parameterValuesJson);
    agentJson.put("processing_script", "custom script...");
    outputsArrayJson = new JSONArray();
    outputJson = new JSONObject();
    outputJson.put("name", "output1");
    outputJson.put("description", "The first output");
    outputJson.put("label", "First Output");
    outputJson.put("type", "string");
    outputJson.put("default_value", "abc");
    outputsArrayJson.put(outputJson);
    agentJson.put("outputs", outputsArrayJson);
    agentReturnJson = doPostJson(url, agentJson, 201);
    assertTrue("Unexpected JSON returned", agentReturnJson == null);

    // Verify the new custom agent
    url = baseUrl + "/users/" + expectedUserName + "/agents/" + expectedAgentName;
    agentJson = doGetJson(url, 200);
    assertTrue("Agent JSON not returned", agentJson != null);
    assertTrue("Missing agent name", agentJson.has("name"));
    agentName = agentJson.getString("name");
    assertEquals("Agent name", expectedAgentName, agentName);
    assertTrue("Missing agent description", agentJson.has("description"));
    assertEquals("Agent description", expectedAgentDescription, agentJson.getString("description"));
    assertTrue("Missing agent time", agentJson.has("time"));
    assertTrue("Agent parameters not present", agentJson.has("parameters"));
    agentParametersJson = agentJson.getJSONArray("parameters");
    numParameters = agentParametersJson.length();
    assertEquals("Number of agent parameters", 2, numParameters);
    parameterJson = agentParametersJson.getJSONObject(0);
    assertTrue("Missing agent parameter name", parameterJson.has("name"));
    assertEquals("Agent parameter name", "custom-param1-1", parameterJson.getString("name"));
    assertTrue("Missing agent parameter label", parameterJson.has("label"));
    assertEquals("Agent parameter label", "First", parameterJson.getString("label"));
    assertTrue("Missing agent parameter type", parameterJson.has("type"));
    assertEquals("Agent parameter type", "string", parameterJson.getString("type"));
    parameterJson = agentParametersJson.getJSONObject(1);
    assertTrue("Missing agent parameter name", parameterJson.has("name"));
    parameterName = parameterJson.getString("name");
    assertEquals("Agent parameter name", "custom-param1-2", parameterName);
    assertTrue("Missing agent parameter label", parameterJson.has("label"));
    parameterLabel = parameterJson.getString("label");
    assertEquals("Agent parameter label", "Second", parameterLabel);
    assertTrue("Missing agent parameter type", parameterJson.has("type"));
    parameterType = parameterJson.getString("type");
    assertEquals("Agent parameter type", "int", parameterType);
    assertTrue("Agent parameter_values not present", agentJson.has("parameter_values"));
    agentParameterValuesJson = agentJson.getJSONObject("parameter_values");
    numParameterValues = agentParameterValuesJson.length();
    assertEquals("Number of agent parameter_values", 2, numParameterValues);
    assertTrue("Agent parameter_values not present for custom-param1-1", agentParameterValuesJson.has("custom-param1-1"));
    assertEquals("Agent parameter_values for custom-param1-1", initialValue1, agentParameterValuesJson.getString("custom-param1-1"));
    assertTrue("Agent parameter_values not present for custom-param1-2", agentParameterValuesJson.has("custom-param1-2"));
    assertEquals("Agent parameter_values for custom-param1-2", initialValue2, agentParameterValuesJson.get("custom-param1-2"));
    //assertFalse("Unexpected inputs for custom agent", agentJson.has("inputs"));
    if (agentJson.has("inputs"))
      assertEquals("Number of inputs for custom agent", 0, agentJson.getJSONArray("inputs").length());
    //assertFalse("Unexpected events for custom agent", agentJson.has("events"));
    if (agentJson.has("events"))
      assertEquals("Number of events for custom agent", 0, agentJson.getJSONArray("events").length());
    outputsJson = agentJson.getJSONArray("outputs");
    numOutputs = outputsJson.length();
    assertEquals("Number of agent outputs", 1, numOutputs);
    assertTrue("Agent output[0] is not a JSON object", outputsJson.get(0) instanceof JSONObject);
    outputJson = outputsJson.getJSONObject(0);
    assertTrue("\"name\" is not present for agent output[0]", outputJson.has("name"));
    assertTrue("\"type\" is not present for agent output[0]", outputJson.has("type"));
    assertTrue("\"description\" is not present for agent output[0]", outputJson.has("description"));
    assertTrue("\"label\" is not present for agent output[0]", outputJson.has("label"));
    assertTrue("\"default_value\" is not present for agent output[0]", outputJson.has("default_value"));
    assertEquals("Agent output[0] name", "output1", outputJson.getString("name"));
    assertEquals("Agent output[0] type", "string", outputJson.getString("type"));
    assertEquals("Agent output[0] label", "First Output", outputJson.getString("label"));
    assertEquals("Agent output[0] description", "The first output", outputJson.getString("description"));
    assertEquals("Agent output[0] default_value", "abc", outputJson.getString("default_value"));
    assertEquals("Number of input_values for custom agent", 0, agentJson.getJSONObject("input_values").length());
    assertEquals("Number of event_values for custom agent", 0, agentJson.getJSONObject("event_values").length());
    assertTrue("Agent output_values missing", agentJson.has("output_values"));
    outputValuesJson = agentJson.getJSONObject("output_values");
    assertEquals("Number of agent output_values", 1, outputValuesJson.length());
    assertTrue("Agent output_values missing attribute output1", outputValuesJson.has("output1"));
    assertEquals("Agent output_values attribute output1", "abc", outputValuesJson.get("output1"));
    log.info("Agent " + expectedAgentName + " for user " + expectedUserName + ": " + agentJson + " agent fields array: " + agentParametersArrayJson + " numParameters: " + numParameters + " numInputs: " + numInputs + " numEvents: " + numEvents + " numOutputs: " + numOutputs);
    
    // Verify that a non-existent agent class is detected
    url = baseUrl + "/agent_classes/custom-agent-class-xxx";
    agentClassJson = doGetJson(url, 404);
    
    // Add a custom agent class that has no parameters
    url = baseUrl + "/agent_classes?name=custom-agent-class-no-parameters";
    agentClassParametersArrayJson = new JSONArray();
    agentClassJson = new JSONObject();
    agentClassJson.put("parameters", agentClassParametersArrayJson);
    agentClassReturnJson = doPostJson(url, agentClassJson, 201);
    assertTrue("Unexpected JSON returned", agentClassReturnJson == null);

    // Test add a custom agent class with missing name
    url = baseUrl + "/agent_classes";
    doPostJson(url, agentClassJson, 400);

    // Test add a custom agent class with missing parameters
    url = baseUrl + "/agent_classes?name=custom-agent-class2";
    agentClassParametersArrayJson = new JSONArray();
    agentClassJson = new JSONObject();
    agentClassReturnJson = doPostJson(url, agentClassJson, 400);
    assertTrue("Unexpected JSON returned", agentClassReturnJson == null);
    
    // Add a custom data source
    url = baseUrl + "/data_sources?name=custom-data-source";
    dataSourceJson = new JSONObject();
    dataSourceJson.put("description", "First custom data source");
    dataSourceJson.put("default_reporting_interval", 1000);
    dataSourceJson.put("enabled", false);
    JSONArray dataSourceParametersArrayJson = new JSONArray();
    JSONObject dataSourceAttribute = new JSONObject();
    dataSourceAttribute.put("name", "custom-ds-attr1-1");
    dataSourceAttribute.put("description", "First custom attribute");
    dataSourceAttribute.put("label", "First");
    dataSourceAttribute.put("type", "string");
    dataSourceAttribute.put("default_value", "abc");
    dataSourceParametersArrayJson.put(dataSourceAttribute);
    dataSourceAttribute = new JSONObject();
    dataSourceAttribute.put("name", "custom-ds-attr1-2");
    dataSourceAttribute.put("description", "Second custom attribute");
    dataSourceAttribute.put("label", "Second");
    dataSourceAttribute.put("type", "int");
    dataSourceAttribute.put("default_value", 123);
    dataSourceParametersArrayJson.put(dataSourceAttribute);
    dataSourceJson.put("attributes", dataSourceParametersArrayJson);
    JSONObject dataSourceReturnJson = doPostJson(url, dataSourceJson, 201);
    assertTrue("Unexpected JSON returned", dataSourceReturnJson == null);
    
    // Verify that the new data source was created
    url = baseUrl + "/data_sources";
    dataSourcesJson = doGetJson(url, 200);
    dataSourcesArrayJson = dataSourcesJson.getJSONArray("data_sources");
    assertEquals("Number of data sources", 4, dataSourcesArrayJson.length());
    dataSourceJson = dataSourcesArrayJson.getJSONObject(3);
    assertEquals("Custom data source name", "custom-data-source", dataSourceJson.getString("name"));
    assertEquals("Custom data source description", "First custom data source", dataSourceJson.getString("description"));
    assertEquals("Custom data source default_reporting_interval", 1000, dataSourceJson.get("default_reporting_interval"));
    assertEquals("Custom data source enabled", false, dataSourceJson.get("enabled"));
    url = baseUrl + "/data_sources/custom-data-source";
    dataSourceJson = doGetJson(url, 200);
    assertTrue("No JSON returned for data source", dataSourceJson != null);
    assertEquals("Custom data source name", "custom-data-source", dataSourceJson.getString("name"));
    assertEquals("Custom data source description", "First custom data source", dataSourceJson.getString("description"));
    assertTrue("Missing custom class attributes field", dataSourceJson.has("attributes"));
    JSONArray dataSourceParametersJson = dataSourceJson.getJSONArray("attributes");
    assertEquals("Number of attributes for custom data source", 2, dataSourceParametersJson.length());
    JSONObject dataSourceParameter = dataSourceParametersJson.getJSONObject(0);
    assertTrue("Missing custom class attribute name field", dataSourceParameter.has("name"));
    assertEquals("Custom data source attribute name", "custom-ds-attr1-1", dataSourceParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", dataSourceParameter.has("description"));
    assertEquals("Custom data source attribute description", "First custom attribute", dataSourceParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", dataSourceParameter.has("label"));
    assertEquals("Custom data source attribute label", "First", dataSourceParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", dataSourceParameter.has("type"));
    assertEquals("Custom data source attribute type", "string", dataSourceParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", dataSourceParameter.has("default_value"));
    assertEquals("Custom data source attribute default_value", "abc", dataSourceParameter.get("default_value"));
    dataSourceParameter = dataSourceParametersJson.getJSONObject(1);
    assertTrue("Missing custom class attribute name field", dataSourceParameter.has("name"));
    assertEquals("Custom data source attribute name", "custom-ds-attr1-2", dataSourceParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", dataSourceParameter.has("description"));
    assertEquals("Custom data source attribute description", "Second custom attribute", dataSourceParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", dataSourceParameter.has("label"));
    assertEquals("Custom data source attribute label", "Second", dataSourceParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", dataSourceParameter.has("type"));
    assertEquals("Custom data source attribute type", "int", dataSourceParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", dataSourceParameter.has("default_value"));
    assertEquals("Custom data source attribute default_value", 123, dataSourceParameter.get("default_value"));

    // Verify empty update of data source - should be no-op
    url = baseUrl + "/data_sources/custom-data-source";
    dataSourceJson = new JSONObject();
    JSONObject returnedDataSourceJson = doPutJson(url, dataSourceJson, 201);
    assertTrue("Unexpected JSON on return from PUT to updata data source", returnedDataSourceJson == null);
    dataSourceJson = doGetJson(url, 200);
    assertTrue("No JSON returned for data source", dataSourceJson != null);
    assertEquals("Custom data source name", "custom-data-source", dataSourceJson.getString("name"));
    assertEquals("Custom data source description", "First custom data source", dataSourceJson.getString("description"));
    assertEquals("Custom data source default_reporting_interval", 1000, dataSourceJson.get("default_reporting_interval"));
    assertEquals("Custom data source enabled", false, dataSourceJson.get("enabled"));
    assertTrue("Missing custom class attributes field", dataSourceJson.has("attributes"));
    dataSourceParametersJson = dataSourceJson.getJSONArray("attributes");
    assertEquals("Number of attributes for custom data source", 2, dataSourceParametersJson.length());
    dataSourceParameter = dataSourceParametersJson.getJSONObject(0);
    assertTrue("Missing custom class attribute name field", dataSourceParameter.has("name"));
    assertEquals("Custom data source attribute name", "custom-ds-attr1-1", dataSourceParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", dataSourceParameter.has("description"));
    assertEquals("Custom data source attribute description", "First custom attribute", dataSourceParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", dataSourceParameter.has("label"));
    assertEquals("Custom data source attribute label", "First", dataSourceParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", dataSourceParameter.has("type"));
    assertEquals("Custom data source attribute type", "string", dataSourceParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", dataSourceParameter.has("default_value"));
    assertEquals("Custom data source attribute default_value", "abc", dataSourceParameter.get("default_value"));
    dataSourceParameter = dataSourceParametersJson.getJSONObject(1);
    assertTrue("Missing custom class attribute name field", dataSourceParameter.has("name"));
    assertEquals("Custom data source attribute name", "custom-ds-attr1-2", dataSourceParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", dataSourceParameter.has("description"));
    assertEquals("Custom data source attribute description", "Second custom attribute", dataSourceParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", dataSourceParameter.has("label"));
    assertEquals("Custom data source attribute label", "Second", dataSourceParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", dataSourceParameter.has("type"));
    assertEquals("Custom data source attribute type", "int", dataSourceParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", dataSourceParameter.has("default_value"));
    assertEquals("Custom data source attribute default_value", 123, dataSourceParameter.get("default_value"));

    // Test update of entire data source
    dataSourceJson = new JSONObject();
    dataSourceJson.put("description", "First custom data source - revised");
    dataSourceJson.put("default_reporting_interval", 2000);
    dataSourceJson.put("enabled", true);
    dataSourceParametersArrayJson = new JSONArray();
    dataSourceAttribute = new JSONObject();
    dataSourceAttribute.put("name", "custom-ds-attr1-1a");
    dataSourceAttribute.put("description", "First custom attribute - revised");
    dataSourceAttribute.put("label", "First - revised");
    dataSourceAttribute.put("type", "string");
    dataSourceAttribute.put("default_value", "abc-revised");
    dataSourceParametersArrayJson.put(dataSourceAttribute);
    dataSourceJson.put("attributes", dataSourceParametersArrayJson);
    dataSourceReturnJson = doPutJson(url, dataSourceJson, 201);
    assertTrue("Unexpected JSON returned", dataSourceReturnJson == null);

    // Verify the updated data source
    url = baseUrl + "/data_sources";
    dataSourcesJson = doGetJson(url, 200);
    dataSourcesArrayJson = dataSourcesJson.getJSONArray("data_sources");
    assertEquals("Number of data sources", 4, dataSourcesArrayJson.length());
    dataSourceJson = dataSourcesArrayJson.getJSONObject(3);
    assertEquals("Custom data source name", "custom-data-source", dataSourceJson.getString("name"));
    assertEquals("Custom data source description", "First custom data source - revised", dataSourceJson.getString("description"));
    assertEquals("Custom data source default_reporting_interval", 2000, dataSourceJson.get("default_reporting_interval"));
    assertEquals("Custom data source enabled", true, dataSourceJson.get("enabled"));
    url = baseUrl + "/data_sources/custom-data-source";
    dataSourceJson = doGetJson(url, 200);
    assertTrue("No JSON returned for data source", dataSourceJson != null);
    assertEquals("Custom data source name", "custom-data-source", dataSourceJson.getString("name"));
    assertEquals("Custom data source description", "First custom data source - revised", dataSourceJson.getString("description"));
    assertEquals("Custom data source default_reporting_interval", 2000, dataSourceJson.get("default_reporting_interval"));
    assertEquals("Custom data source enabled", true, dataSourceJson.get("enabled"));
    assertTrue("Missing custom class attributes field", dataSourceJson.has("attributes"));
    dataSourceParametersJson = dataSourceJson.getJSONArray("attributes");
    assertEquals("Number of attributes for custom data source", 1, dataSourceParametersJson.length());
    dataSourceParameter = dataSourceParametersJson.getJSONObject(0);
    assertTrue("Missing custom class attribute name field", dataSourceParameter.has("name"));
    assertEquals("Custom data source attribute name", "custom-ds-attr1-1a", dataSourceParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", dataSourceParameter.has("description"));
    assertEquals("Custom data source attribute description", "First custom attribute - revised", dataSourceParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", dataSourceParameter.has("label"));
    assertEquals("Custom data source attribute label", "First - revised", dataSourceParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", dataSourceParameter.has("type"));
    assertEquals("Custom data source attribute type", "string", dataSourceParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", dataSourceParameter.has("default_value"));
    assertEquals("Custom data source attribute default_value", "abc-revised", dataSourceParameter.get("default_value"));

    // Verify that a non-existent data source is detected
    url = baseUrl + "/data_sources/custom-data-source-xxx";
    dataSourceJson = doGetJson(url, 404);
    
    // Add a custom data source that has no attributes
    url = baseUrl + "/data_sources?name=custom-data-source-no-attributes";
    dataSourceParametersArrayJson = new JSONArray();
    dataSourceJson = new JSONObject();
    dataSourceJson.put("attributes", dataSourceParametersArrayJson);
    dataSourceReturnJson = doPostJson(url, dataSourceJson, 201);
    assertTrue("Unexpected JSON returned", dataSourceReturnJson == null);

    // Test add a custom data source with missing name
    url = baseUrl + "/data_sources";
    doPostJson(url, dataSourceJson, 400);

    // Test add a custom data source with missing attributes
    url = baseUrl + "/data_sources?name=custom-data-source2";
    dataSourceParametersArrayJson = new JSONArray();
    dataSourceJson = new JSONObject();
    dataSourceReturnJson = doPostJson(url, dataSourceJson, 400);
    assertTrue("Unexpected JSON returned", dataSourceReturnJson == null);
    
    // Add a custom event
    url = baseUrl + "/events?name=custom-event";
    eventJson = new JSONObject();
    eventJson.put("description", "First custom event");
    eventJson.put("default_reporting_interval", 100);
    eventJson.put("enabled", true);
    JSONArray eventParametersArrayJson = new JSONArray();
    JSONObject eventAttribute = new JSONObject();
    eventAttribute.put("name", "custom-ev-attr1-1");
    eventAttribute.put("description", "First custom attribute");
    eventAttribute.put("label", "First");
    eventAttribute.put("type", "string");
    eventAttribute.put("default_value", "abc");
    eventParametersArrayJson.put(eventAttribute);
    eventAttribute = new JSONObject();
    eventAttribute.put("name", "custom-ev-attr1-2");
    eventAttribute.put("description", "Second custom attribute");
    eventAttribute.put("label", "Second");
    eventAttribute.put("type", "int");
    eventAttribute.put("default_value", 123);
    eventParametersArrayJson.put(eventAttribute);
    eventJson.put("attributes", eventParametersArrayJson);
    JSONObject eventReturnJson = doPostJson(url, eventJson, 201);
    assertTrue("Unexpected JSON returned", eventReturnJson == null);
    
    // Verify that the new custom event was created
    url = baseUrl + "/events";
    eventsJson = doGetJson(url, 200);
    eventsArrayJson = eventsJson.getJSONArray("events");
    assertEquals("Number of events", 4, eventsArrayJson.length());
    eventJson = eventsArrayJson.getJSONObject(3);
    assertEquals("Custom event name", "custom-event", eventJson.getString("name"));
    assertEquals("Custom event description", "First custom event", eventJson.getString("description"));
    assertEquals("Custom event default_reporting_interval", 100, eventJson.get("default_reporting_interval"));
    assertEquals("Custom event enabled", true, eventJson.get("enabled"));
    url = baseUrl + "/events/custom-event";
    eventJson = doGetJson(url, 200);
    assertTrue("No JSON returned for event", eventJson != null);
    assertEquals("Custom event name", "custom-event", eventJson.getString("name"));
    assertEquals("Custom event description", "First custom event", eventJson.getString("description"));
    assertTrue("Missing custom class attributes field", eventJson.has("attributes"));
    JSONArray eventParametersJson = eventJson.getJSONArray("attributes");
    assertEquals("Number of attributes for custom event", 2, eventParametersJson.length());
    JSONObject eventParameter = eventParametersJson.getJSONObject(0);
    assertTrue("Missing custom class attribute name field", eventParameter.has("name"));
    assertEquals("Custom event attribute name", "custom-ev-attr1-1", eventParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", eventParameter.has("description"));
    assertEquals("Custom event attribute description", "First custom attribute", eventParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", eventParameter.has("label"));
    assertEquals("Custom event attribute label", "First", eventParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", eventParameter.has("type"));
    assertEquals("Custom event attribute type", "string", eventParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", eventParameter.has("default_value"));
    assertEquals("Custom event attribute default_value", "abc", eventParameter.get("default_value"));
    eventParameter = eventParametersJson.getJSONObject(1);
    assertTrue("Missing custom class attribute name field", eventParameter.has("name"));
    assertEquals("Custom event attribute name", "custom-ev-attr1-2", eventParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", eventParameter.has("description"));
    assertEquals("Custom event attribute description", "Second custom attribute", eventParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", eventParameter.has("label"));
    assertEquals("Custom event attribute label", "Second", eventParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", eventParameter.has("type"));
    assertEquals("Custom event attribute type", "int", eventParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", eventParameter.has("default_value"));
    assertEquals("Custom event attribute default_value", 123, eventParameter.get("default_value"));

    // Verify empty update of event - should be no-op
    url = baseUrl + "/events/custom-event";
    eventJson = new JSONObject();
    JSONObject returnedEventJson = doPutJson(url, eventJson, 201);
    assertTrue("Unexpected JSON on return from PUT to updata event", returnedEventJson == null);
    eventJson = doGetJson(url, 200);
    assertTrue("No JSON returned for event", eventJson != null);
    assertEquals("Custom event name", "custom-event", eventJson.getString("name"));
    assertEquals("Custom event description", "First custom event", eventJson.getString("description"));
    assertEquals("Custom event default_reporting_interval", 100, eventJson.get("default_reporting_interval"));
    assertEquals("Custom event enabled", true, eventJson.get("enabled"));
    assertTrue("Missing custom class attributes field", eventJson.has("attributes"));
    eventParametersJson = eventJson.getJSONArray("attributes");
    assertEquals("Number of attributes for custom event", 2, eventParametersJson.length());
    eventParameter = eventParametersJson.getJSONObject(0);
    assertTrue("Missing custom class attribute name field", eventParameter.has("name"));
    assertEquals("Custom event attribute name", "custom-ev-attr1-1", eventParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", eventParameter.has("description"));
    assertEquals("Custom event attribute description", "First custom attribute", eventParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", eventParameter.has("label"));
    assertEquals("Custom event attribute label", "First", eventParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", eventParameter.has("type"));
    assertEquals("Custom event attribute type", "string", eventParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", eventParameter.has("default_value"));
    assertEquals("Custom event attribute default_value", "abc", eventParameter.get("default_value"));
    eventParameter = eventParametersJson.getJSONObject(1);
    assertTrue("Missing custom class attribute name field", eventParameter.has("name"));
    assertEquals("Custom event attribute name", "custom-ev-attr1-2", eventParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", eventParameter.has("description"));
    assertEquals("Custom event attribute description", "Second custom attribute", eventParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", eventParameter.has("label"));
    assertEquals("Custom event attribute label", "Second", eventParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", eventParameter.has("type"));
    assertEquals("Custom event attribute type", "int", eventParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", eventParameter.has("default_value"));
    assertEquals("Custom event attribute default_value", 123, eventParameter.get("default_value"));

    // Test update of entire event
    eventJson = new JSONObject();
    eventJson.put("description", "First custom event - revised");
    eventJson.put("default_reporting_interval", 2000);
    eventJson.put("enabled", true);
    eventParametersArrayJson = new JSONArray();
    eventAttribute = new JSONObject();
    eventAttribute.put("name", "custom-ev-attr1-1a");
    eventAttribute.put("description", "First custom attribute - revised");
    eventAttribute.put("label", "First - revised");
    eventAttribute.put("type", "string");
    eventAttribute.put("default_value", "abc-revised");
    eventParametersArrayJson.put(eventAttribute);
    eventJson.put("attributes", eventParametersArrayJson);
    eventReturnJson = doPutJson(url, eventJson, 201);
    assertTrue("Unexpected JSON returned", eventReturnJson == null);

    // Verify the updated event
    url = baseUrl + "/events";
    eventsJson = doGetJson(url, 200);
    eventsArrayJson = eventsJson.getJSONArray("events");
    assertEquals("Number of events", 4, eventsArrayJson.length());
    eventJson = eventsArrayJson.getJSONObject(3);
    assertEquals("Custom event name", "custom-event", eventJson.getString("name"));
    assertEquals("Custom event description", "First custom event - revised", eventJson.getString("description"));
    assertEquals("Custom event default_reporting_interval", 2000, eventJson.get("default_reporting_interval"));
    assertEquals("Custom event enabled", true, eventJson.get("enabled"));
    url = baseUrl + "/events/custom-event";
    eventJson = doGetJson(url, 200);
    assertTrue("No JSON returned for event", eventJson != null);
    assertEquals("Custom event name", "custom-event", eventJson.getString("name"));
    assertEquals("Custom event description", "First custom event - revised", eventJson.getString("description"));
    assertEquals("Custom event default_reporting_interval", 2000, eventJson.get("default_reporting_interval"));
    assertEquals("Custom event enabled", true, eventJson.get("enabled"));
    assertTrue("Missing custom class attributes field", eventJson.has("attributes"));
    eventParametersJson = eventJson.getJSONArray("attributes");
    assertEquals("Number of attributes for custom event", 1, eventParametersJson.length());
    eventParameter = eventParametersJson.getJSONObject(0);
    assertTrue("Missing custom class attribute name field", eventParameter.has("name"));
    assertEquals("Custom event attribute name", "custom-ev-attr1-1a", eventParameter.getString("name"));
    assertTrue("Missing custom class attribute description field", eventParameter.has("description"));
    assertEquals("Custom event attribute description", "First custom attribute - revised", eventParameter.getString("description"));
    assertTrue("Missing custom class attribute label field", eventParameter.has("label"));
    assertEquals("Custom event attribute label", "First - revised", eventParameter.getString("label"));
    assertTrue("Missing custom class attribute type field", eventParameter.has("type"));
    assertEquals("Custom event attribute type", "string", eventParameter.getString("type"));
    assertTrue("Missing custom class attribute default_value field", eventParameter.has("default_value"));
    assertEquals("Custom event attribute default_value", "abc-revised", eventParameter.get("default_value"));
    
    // Verify that a non-existent event is detected
    url = baseUrl + "/events/custom-event-xxx";
    eventJson = doGetJson(url, 404);
    
    // Add a custom event that has no attributes
    url = baseUrl + "/events?name=custom-event-no-attributes";
    eventParametersArrayJson = new JSONArray();
    eventJson = new JSONObject();
    eventJson.put("attributes", eventParametersArrayJson);
    eventReturnJson = doPostJson(url, eventJson, 201);
    assertTrue("Unexpected JSON returned", eventReturnJson == null);

    // Test add a custom event with missing name
    url = baseUrl + "/events";
    doPostJson(url, eventJson, 400);

    // Test add a custom event with missing attributes
    url = baseUrl + "/events?name=custom-event2";
    eventParametersArrayJson = new JSONArray();
    eventJson = new JSONObject();
    eventReturnJson = doPostJson(url, eventJson, 400);
    assertTrue("Unexpected JSON returned", eventReturnJson == null);

    // Test DELETE of a data source
    url = baseUrl + "/data_sources/custom-data-source";
    returnedDataSourceJson = doDeleteJson(url, 204);
    assertTrue("Unexpected JSON on return from DELETE of data source", returnedDataSourceJson == null);
    returnedDataSourceJson = doGetJson(url, 404);
    assertTrue("Unexpected JSON on return from GET of deleted data source", returnedDataSourceJson == null);
    url = baseUrl + "/data_sources/custom-data-source-no-attributes";
    returnedDataSourceJson = doDeleteJson(url, 204);
    assertTrue("Unexpected JSON on return from DELETE of data source", returnedDataSourceJson == null);
    returnedDataSourceJson = doGetJson(url, 404);
    assertTrue("Unexpected JSON on return from GET of deleted data source", returnedDataSourceJson == null);
    url = baseUrl + "/data_sources";
    dataSourcesJson = doGetJson(url, 200);
    assertTrue("\"data_sources\" field is not present", dataSourcesJson.has("data_sources"));
    dataSourcesArrayJson = dataSourcesJson.getJSONArray("data_sources");
    numDataSources = dataSourcesArrayJson.length();
    assertEquals("Number of data sources", 3, numDataSources);
    dataSourceJson = dataSourcesArrayJson.getJSONObject(0);
    assertTrue("\"name\" field is not present", dataSourceJson.has("name"));
    assertEquals("DataSources[0] name", "data_source1", dataSourceJson.getString("name"));
    dataSourceJson = dataSourcesArrayJson.getJSONObject(1);
    assertTrue("\"name\" field is not present", dataSourceJson.has("name"));
    assertEquals("DataSources[1] name", "data_source2", dataSourceJson.getString("name"));
    dataSourceJson = dataSourcesArrayJson.getJSONObject(2);
    assertTrue("\"name\" field is not present", dataSourceJson.has("name"));
    assertEquals("DataSources[2] name", "data_source3", dataSourceJson.getString("name"));

    // Test DELETE of an event
    url = baseUrl + "/events/custom-event";
    eventJson = new JSONObject();
    returnedEventJson = doDeleteJson(url, 204);
    assertTrue("Unexpected JSON on return from DELETE of event", returnedEventJson == null);
    returnedEventJson = doGetJson(url, 404);
    assertTrue("Unexpected JSON on return from GET of deleted event", returnedEventJson == null);
    url = baseUrl + "/events/custom-event-no-attributes";
    returnedEventJson = doDeleteJson(url, 204);
    assertTrue("Unexpected JSON on return from DELETE of event", returnedEventJson == null);
    returnedEventJson = doGetJson(url, 404);
    assertTrue("Unexpected JSON on return from GET of deleted event", returnedEventJson == null);
    url = baseUrl + "/events";
    eventsJson = doGetJson(url, 200);
    assertTrue("\"events\" field is not present", eventsJson.has("events"));
    eventsArrayJson = eventsJson.getJSONArray("events");
    numEvents = eventsArrayJson.length();
    assertEquals("Number of events", 3, numEvents);
    eventJson = eventsArrayJson.getJSONObject(0);
    assertTrue("\"name\" field is not present", eventJson.has("name"));
    assertEquals("Events[0] name", "event1", eventJson.getString("name"));
    eventJson = eventsArrayJson.getJSONObject(1);
    assertTrue("\"name\" field is not present", eventJson.has("name"));
    assertEquals("Events[1] name", "event2", eventJson.getString("name"));
    eventJson = eventsArrayJson.getJSONObject(2);
    assertTrue("\"name\" field is not present", eventJson.has("name"));
    assertEquals("Events[2] name", "event3", eventJson.getString("name"));

    // Test DELETE of a user
    // TODO
    
    // Test DELETE of an agent
    // TODO
    
    // Test DELETE of an agent class
    // TODO

    // Test evaluation of an expression
    url = baseUrl + "/evaluate";
    String expressonString = "'Hello' + ' Wo' + 'rld - 2 + 2 = ' + (2 + 2)";
    String resultString = doPostText(url, expressonString, 200);
    assertTrue("resultString is null", resultString != null);
    resultString = resultString.trim();
    assertEquals("Result string - expression value", "Hello World - 2 + 2 = 4", resultString);

    expressonString = "7 + 3.14 * 5";
    resultString = doPostText(url, expressonString, 200);
    assertTrue("resultString is null", resultString != null);
    resultString = resultString.trim();
    assertEquals("Result string - expression value", "22.700000000000003", resultString);
    
    // Test running of a script
    url = baseUrl + "/run";
    String scriptString = " if (2 + 2 != 5) return 'Yes'; else return 'No';";
    resultString = doPostText(url, scriptString, 200);
    assertTrue("resultString is null", resultString != null);
    resultString = resultString.trim();
    assertEquals("Result string - expression value", "Yes", resultString);

    url = baseUrl + "/run";
    scriptString = " if (2 + 2 == 5) return 'Yes'; else return 'No';";
    resultString = doPostText(url, scriptString, 200);
    assertTrue("resultString is null", resultString != null);
    resultString = resultString.trim();
    assertEquals("Result string - expression value", "No", resultString);
}
  
  @Test
  public void testUsers() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;
    
    // Test query of /users before any users exist
    // TODO: Should admin be predefined? (Or, "agmin"?)
    String url = baseUrl + "/users?password=" + server.agentServer.getAdminPassword();
    JSONObject userJson = doGetJson(url, 200);
    JSONArray usersArrayJson = userJson.getJSONArray("users");
    int numUsers = usersArrayJson.length();
    assertEquals("Number of users", 0, numUsers);
    log.info("Users: " + userJson + " Users array: " + usersArrayJson + " numUsers: " + numUsers);

    // Test addition of a single user
    String expectedId = "Test.User";
    String expectedPassword = "my-pwd";
    url = baseUrl + "/users?id=" + expectedId + "&password=" + expectedPassword;
    userJson = doPostJson(url, (JSONObject)null, 201);
    assertTrue("Return JSON should be null", userJson == null);

    // Check full user list
    url = baseUrl + "/users?password=" + server.agentServer.getAdminPassword();
    userJson = doGetJson(url, 200);
    usersArrayJson = userJson.getJSONArray("users");
    numUsers = usersArrayJson.length();
    assertEquals("Number of users", 1, numUsers);
    userJson  = usersArrayJson.getJSONObject(0);
    assertEquals("User[0] id", expectedId, userJson.getString("id"));
    assertEquals("User[0] display_name", "", userJson.getString("display_name"));
    log.info("Users: " + userJson + " Users array: " + usersArrayJson + " numUsers: " + numUsers);

    // Make sure password is required
    url = baseUrl + "/users/" + expectedId + "?password=junk";
    userJson = doGetJson(url, 400);
    assertException(userJson,
        "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Unknown user Id or invalid password");

    url = baseUrl + "/users/" + expectedId + "?password=";
    userJson = doGetJson(url, 400);
    assertException(userJson,
        "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Empty password query parameter");

    url = baseUrl + "/users/" + expectedId;
    userJson = doGetJson(url, 400);
    assertException(userJson,
        "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Missing password query parameter");

    url = baseUrl + "/users/" + expectedId + "?admin_password=junk";
    userJson = doGetJson(url, 400);
    assertException(userJson,
        "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Invalid admin password");

    // Check the individual user details
    url = baseUrl + "/users/" + expectedId + "?password=" + expectedPassword;
    userJson = doGetJson(url, 200);
    assertEquals("User id", expectedId, userJson.getString("id"));
    assertEquals("User password", expectedPassword, userJson.getString("password"));
    assertEquals("User password hint", "", userJson.getString("password_hint"));
    assertEquals("User full name", "", userJson.getString("full_name"));
    assertEquals("User display name", "", userJson.getString("display_name"));
    assertEquals("User nick name", "", userJson.getString("nick_name"));
    assertEquals("User bio", "", userJson.getString("bio"));
    assertEquals("User interests", "", userJson.getString("interests"));
    assertEquals("User email", "", userJson.getString("email"));
    assertEquals("User incognito", false, userJson.getBoolean("incognito"));
    assertEquals("User approved", true, userJson.getBoolean("approved"));
    assertEquals("User id SHA", "68e7bea5149cf79298f3a6369bd9c459", userJson.getString("sha_id"));
    assertEquals("User password SHA", "87d435059558808d06f480d9c0aaf35c", userJson.getString("sha_password"));
    assertJsonSourceEquals("User JSON", "{\"id\":\"Test.User\",\"password\":\"my-pwd\",\"password_hint\":\"\",\"full_name\":\"\",\"display_name\":\"\",\"nick_name\":\"\",\"bio\":\"\",\"interests\":\"\",\"incognito\":false,\"email\":\"\",\"comment\":\"\", \"approved\": true,\"sha_id\":\"68e7bea5149cf79298f3a6369bd9c459\",\"sha_password\":\"87d435059558808d06f480d9c0aaf35c\", \"organization\":\"\"}", userJson);

    // Test addition of a user with an email address for user Id
    String expectedId2 = "mary.jones@att.com";
    String expectedPassword2 = "mary-pwd";
    String expectedDisplayName2 = "Mary Jones";
    String expectedDisplayName2Encoded = "Mary+Jones";
    url = baseUrl + "/users?id=" + expectedId2 + "&password=" + expectedPassword2 + "&display_name=" + expectedDisplayName2Encoded;
    userJson = doPostJson(url, (JSONObject)null, 201);
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doGetJson(url, 200);
    assertJsonSourceEquals("User", "{\"interests\":\"\",\"incognito\":false,\"sha_id\":\"6bd89f4ce19fdf7b973ca0a2b65f511a\",\"password\":\"mary-pwd\",\"id\":\"mary.jones@att.com\",\"nick_name\":\"\",\"display_name\":\"Mary Jones\",\"approved\":true,\"organization\":\"\",\"bio\":\"\",\"email\":\"\",\"password_hint\":\"\",\"comment\":\"\",\"sha_password\":\"bf311f089c10794f1cb6c2c4ecaf9bdb\",\"full_name\":\"\"}", userJson);
    
    // Access that user's info using admin password
    url = baseUrl + "/users/" + expectedId2 + "?admin_password=" + server.agentServer.getAdminPassword();
    userJson = doGetJson(url, 200);
    assertJsonSourceEquals("User", "{\"interests\":\"\",\"incognito\":false,\"sha_id\":\"6bd89f4ce19fdf7b973ca0a2b65f511a\",\"password\":\"mary-pwd\",\"id\":\"mary.jones@att.com\",\"nick_name\":\"\",\"display_name\":\"Mary Jones\",\"approved\":true,\"organization\":\"\",\"bio\":\"\",\"email\":\"\",\"password_hint\":\"\",\"comment\":\"\",\"sha_password\":\"bf311f089c10794f1cb6c2c4ecaf9bdb\",\"full_name\":\"\"}", userJson);

    // Make sure user can be disabled and no longer accessed
    url = baseUrl + "/users/" + expectedId2 + "/disable?password=" + server.agentServer.getAdminPassword();
    userJson = doPutJson(url, 204);
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doGetJson(url, 400);
    assertException(userJson,
        "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "All activity is disabled for this user");

    // Make sure admin can still access disabled user
    url = baseUrl + "/users/" + expectedId2 + "?admin_password=" + server.agentServer.getAdminPassword();
    userJson = doGetJson(url, 200);
    assertJsonSourceEquals("User", "{\"interests\":\"\",\"incognito\":false,\"sha_id\":\"6bd89f4ce19fdf7b973ca0a2b65f511a\",\"password\":\"mary-pwd\",\"id\":\"mary.jones@att.com\",\"nick_name\":\"\",\"display_name\":\"Mary Jones\",\"approved\":true,\"organization\":\"\",\"bio\":\"\",\"email\":\"\",\"password_hint\":\"\",\"comment\":\"\",\"sha_password\":\"bf311f089c10794f1cb6c2c4ecaf9bdb\",\"full_name\":\"\"}", userJson);

    // Now disable only new activity for that user
    url = baseUrl + "/users/" + expectedId2 + "/disable?all_activity=no&new_activity=yes&password=" + server.agentServer.getAdminPassword();
    userJson = doPutJson(url, 204);
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doGetJson(url, 200);
    assertJsonSourceEquals("User", "{\"interests\":\"\",\"incognito\":false,\"sha_id\":\"6bd89f4ce19fdf7b973ca0a2b65f511a\",\"password\":\"mary-pwd\",\"id\":\"mary.jones@att.com\",\"nick_name\":\"\",\"display_name\":\"Mary Jones\",\"approved\":true,\"organization\":\"\",\"bio\":\"\",\"email\":\"\",\"password_hint\":\"\",\"comment\":\"\",\"sha_password\":\"bf311f089c10794f1cb6c2c4ecaf9bdb\",\"full_name\":\"\"}", userJson);
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doPutJson(url, "{}", 400);
    assertException(userJson,
        "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "New activity is disabled for this user");
    
    // Now re-enable user for all activity
    url = baseUrl + "/users/" + expectedId2 + "/enable?password=" + server.agentServer.getAdminPassword();
    userJson = doPutJson(url, 204);
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doGetJson(url, 200);
    assertJsonSourceEquals("User", "{\"interests\":\"\",\"incognito\":false,\"sha_id\":\"6bd89f4ce19fdf7b973ca0a2b65f511a\",\"password\":\"mary-pwd\",\"id\":\"mary.jones@att.com\",\"nick_name\":\"\",\"display_name\":\"Mary Jones\",\"approved\":true,\"organization\":\"\",\"bio\":\"\",\"email\":\"\",\"password_hint\":\"\",\"comment\":\"\",\"sha_password\":\"bf311f089c10794f1cb6c2c4ecaf9bdb\",\"full_name\":\"\"}", userJson);
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doPutJson(url, "{}", 204);

    // Use negative options to disable user using /enable
    url = baseUrl + "/users/" + expectedId2 + "/enable?all_activity=no&new_activity=no&password=" + server.agentServer.getAdminPassword();
    userJson = doPutJson(url, 204);
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doGetJson(url, 400);
    assertException(userJson,
        "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "All activity is disabled for this user");
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doPutJson(url, "{}", 400);
    assertException(userJson,
        "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "All activity is disabled for this user");

    // Now re-enable using negative options of /disable
    url = baseUrl + "/users/" + expectedId2 + "/disable?all_activity=no&new_activity=no&password=" + server.agentServer.getAdminPassword();
    userJson = doPutJson(url, 204);
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doGetJson(url, 200);
    assertJsonSourceEquals("User", "{\"interests\":\"\",\"incognito\":false,\"sha_id\":\"6bd89f4ce19fdf7b973ca0a2b65f511a\",\"password\":\"mary-pwd\",\"id\":\"mary.jones@att.com\",\"nick_name\":\"\",\"display_name\":\"Mary Jones\",\"approved\":true,\"organization\":\"\",\"bio\":\"\",\"email\":\"\",\"password_hint\":\"\",\"comment\":\"\",\"sha_password\":\"bf311f089c10794f1cb6c2c4ecaf9bdb\",\"full_name\":\"\"}", userJson);
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doPutJson(url, "{}", 204);
    
    // Check all users
    url = baseUrl + "/users?password=" + server.agentServer.getAdminPassword();
    userJson = doGetJson(url, 200);
    assertTrue("\"users\" field is not present", userJson.has("users"));
    usersArrayJson = userJson.getJSONArray("users");
    numUsers = usersArrayJson.length();
    assertEquals("Number of users", 2, numUsers);
    userJson  = usersArrayJson.getJSONObject(0);
    assertEquals("User[0] id", expectedId, userJson.getString("id"));
    assertEquals("User[0] display_name", "", userJson.getString("display_name"));
    userJson  = usersArrayJson.getJSONObject(1);
    assertEquals("User[1] id", expectedId2, userJson.getString("id"));
    assertEquals("User[2] display_name", expectedDisplayName2, userJson.getString("display_name"));
    log.info("Users: " + userJson + " Users array: " + usersArrayJson + " numUsers: " + numUsers);
    
    // Test attempt to re-add an existing user
    url = baseUrl + "/users?id=" + expectedId2 + "&password=" + expectedPassword2;
    userJson = doPostJson(url, (JSONObject)null, 400);
    assertException(userJson,
        "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "User with that id already exists");
    
    url = baseUrl + "/users?password=" + server.agentServer.getAdminPassword();
    userJson = doGetJson(url, 200);
    assertTrue("\"users\" field is not present", userJson.has("users"));
    usersArrayJson = userJson.getJSONArray("users");
    numUsers = usersArrayJson.length();
    assertEquals("Number of users", 2, numUsers);
    userJson  = usersArrayJson.getJSONObject(0);
    assertEquals("User[0] id", expectedId, userJson.getString("id"));
    assertEquals("User[0] display_name", "", userJson.getString("display_name"));
    userJson  = usersArrayJson.getJSONObject(1);
    assertEquals("User[1] id", expectedId2, userJson.getString("id"));
    assertEquals("User[2] display_name", expectedDisplayName2, userJson.getString("display_name"));
    log.info("Users: " + userJson + " Users array: " + usersArrayJson + " numUsers: " + numUsers);
    
    // Test delete of a user
    url = baseUrl + "/users/" + expectedId2 + "?password=" + expectedPassword2;
    userJson = doDeleteJson(url, 204);
    
    url = baseUrl + "/users?password=" + server.agentServer.getAdminPassword();
    userJson = doGetJson(url, 200);
    assertTrue("\"users\" field is not present", userJson.has("users"));
    usersArrayJson = userJson.getJSONArray("users");
    numUsers = usersArrayJson.length();
    assertEquals("Number of users", 1, numUsers);
    userJson  = usersArrayJson.getJSONObject(0);
    assertEquals("User[0] id", expectedId, userJson.getString("id"));
    assertEquals("User[0] display_name", "", userJson.getString("display_name"));
    
    // Test a second delete of a user
    url = baseUrl + "/users/" + expectedId + "?password=" + expectedPassword;
    userJson = doDeleteJson(url, 204);
    
    url = baseUrl + "/users?password=" + server.agentServer.getAdminPassword();
    userJson = doGetJson(url, 200);
    assertTrue("\"users\" field is not present", userJson.has("users"));
    usersArrayJson = userJson.getJSONArray("users");
    numUsers = usersArrayJson.length();
    assertEquals("Number of users", 0, numUsers);
    
    // Now add back one of the users we deleted
    String expectedPassword2a = "other-Mary-pwd";
    url = baseUrl + "/users?id=" + expectedId2 + "&password=" + expectedPassword2a;
    userJson = doPostJson(url, (JSONObject)null, 201);
    
    url = baseUrl + "/users?password=" + server.agentServer.getAdminPassword();
    userJson = doGetJson(url, 200);
    assertTrue("\"users\" field is not present", userJson.has("users"));
    usersArrayJson = userJson.getJSONArray("users");
    numUsers = usersArrayJson.length();
    assertEquals("Number of users", 1, numUsers);
    userJson  = usersArrayJson.getJSONObject(0);
    assertEquals("User[0] id", expectedId2, userJson.getString("id"));
    assertEquals("User[0] display_name", "", userJson.getString("display_name"));
    log.info("Users: " + userJson + " Users array: " + usersArrayJson + " numUsers: " + numUsers);

    // Now test all the rest of the user parameters
    url = baseUrl + "/users?id=jsmith&password=catylack&password_hint=What+is+my+pet%3F&full_name=John+W.+Smith,+Jr.&display_name=John+Smith&nick_name=John&bio=Hacker+and+Slacker&interests=hacking,+slacking&email=jsmith@example.com&incognito=yes&comment=just+a+test&organization=MyCo,+Inc.";
    userJson = doPostJson(url, (JSONObject)null, 201);
    
    url = baseUrl + "/users?password=" + server.agentServer.getAdminPassword();
    userJson = doGetJson(url, 200);
    assertTrue("\"users\" field is not present", userJson.has("users"));
    usersArrayJson = userJson.getJSONArray("users");
    numUsers = usersArrayJson.length();
    assertEquals("Number of users", 2, numUsers);
    userJson  = usersArrayJson.getJSONObject(0);
    assertEquals("User[0] id", expectedId2, userJson.getString("id"));
    assertEquals("User[0] display_name", "", userJson.getString("display_name"));
    userJson  = usersArrayJson.getJSONObject(1);
    assertEquals("User[1] id", "jsmith", userJson.getString("id"));
    assertEquals("User[1] display_name", "(Incognito)", userJson.getString("display_name"));
    log.info("Users: " + userJson + " Users array: " + usersArrayJson + " numUsers: " + numUsers);

    url = baseUrl + "/users/jsmith?password=catylack";
    userJson = doGetJson(url, 200);
    assertTrue("id field is not present", userJson.has("id"));
    assertEquals("id field", "jsmith", userJson.getString("id"));
    assertTrue("password field is not present", userJson.has("password"));
    assertEquals("password field", "catylack", userJson.getString("password"));
    assertTrue("password_hint field is not present", userJson.has("password_hint"));
    assertEquals("password_hint field", "What is my pet?", userJson.getString("password_hint"));
    assertTrue("full_name field is not present", userJson.has("full_name"));
    assertEquals("full_name field", "John W. Smith, Jr.", userJson.getString("full_name"));
    assertTrue("display_name field is not present", userJson.has("display_name"));
    assertEquals("display_name field", "John Smith", userJson.getString("display_name"));
    assertTrue("nick_name field is not present", userJson.has("nick_name"));
    assertEquals("nick_name field", "John", userJson.getString("nick_name"));
    assertTrue("organization field is not present", userJson.has("organization"));
    assertEquals("organization field", "MyCo, Inc.", userJson.getString("organization"));
    assertTrue("bio field is not present", userJson.has("bio"));
    assertEquals("bio field", "Hacker and Slacker", userJson.getString("bio"));
    assertTrue("interests field is not present", userJson.has("interests"));
    assertEquals("interests field", "hacking, slacking", userJson.getString("interests"));
    assertTrue("email field is not present", userJson.has("email"));
    assertEquals("email field", "jsmith@example.com", userJson.getString("email"));
    assertTrue("incognito field is not present", userJson.has("incognito"));
    assertEquals("incognito field", true, userJson.getBoolean("incognito"));
    assertTrue("comment field is not present", userJson.has("comment"));
    assertEquals("comment field", "just a test", userJson.getString("comment"));
    assertTrue("approved field is not present", userJson.has("approved"));
    assertEquals("approved field", true, userJson.getBoolean("approved"));
    assertTrue("sha id field is not present", userJson.has("sha_id"));
    assertEquals("sha id field", "39ce7e2a8573b41ce73b5ba41617f8f7", userJson.getString("sha_id"));
    assertTrue("sha password field is not present", userJson.has("sha_password"));
    assertEquals("sha password field", "48948d0a35f82adb133e57cc62a81e5a", userJson.getString("sha_password"));
    assertJsonSourceEquals("User JSON", "{\"id\":\"jsmith\",\"password\":\"catylack\",\"password_hint\":\"What is my pet?\",\"full_name\":\"John W. Smith, Jr.\",\"display_name\":\"John Smith\",\"nick_name\":\"John\",\"bio\":\"Hacker and Slacker\",\"interests\":\"hacking, slacking\",\"incognito\":true,\"email\":\"jsmith@example.com\",\"comment\":\"just a test\",\"sha_id\":\"39ce7e2a8573b41ce73b5ba41617f8f7\",\"sha_password\":\"48948d0a35f82adb133e57cc62a81e5a\", \"approved\": true, \"organization\": \"MyCo, Inc.\"}", userJson.toString());
    assertEquals("Number of fields in user JSON", 15, userJson.length());

    // Test no-op update - no field changes
    url = baseUrl + "/users/jsmith?password=catylack";
    userJson = doPutJson(url, "{}", 204);

    url = baseUrl + "/users/jsmith?password=catylack";
    userJson = doGetJson(url, 200);
    assertTrue("id field is not present", userJson.has("id"));
    assertEquals("id field", "jsmith", userJson.getString("id"));
    assertTrue("password field is not present", userJson.has("password"));
    assertEquals("password field", "catylack", userJson.getString("password"));
    assertTrue("password_hint field is not present", userJson.has("password_hint"));
    assertEquals("password_hint field", "What is my pet?", userJson.getString("password_hint"));
    assertTrue("full_name field is not present", userJson.has("full_name"));
    assertEquals("full_name field", "John W. Smith, Jr.", userJson.getString("full_name"));
    assertTrue("display_name field is not present", userJson.has("display_name"));
    assertEquals("display_name field", "John Smith", userJson.getString("display_name"));
    assertTrue("nick_name field is not present", userJson.has("nick_name"));
    assertEquals("nick_name field", "John", userJson.getString("nick_name"));
    assertTrue("organization field is not present", userJson.has("organization"));
    assertEquals("organization field", "MyCo, Inc.", userJson.getString("organization"));
    assertTrue("bio field is not present", userJson.has("bio"));
    assertEquals("bio field", "Hacker and Slacker", userJson.getString("bio"));
    assertTrue("interests field is not present", userJson.has("interests"));
    assertEquals("interests field", "hacking, slacking", userJson.getString("interests"));
    assertTrue("email field is not present", userJson.has("email"));
    assertEquals("email field", "jsmith@example.com", userJson.getString("email"));
    assertTrue("incognito field is not present", userJson.has("incognito"));
    assertEquals("incognito field", true, userJson.getBoolean("incognito"));
    assertTrue("comment field is not present", userJson.has("comment"));
    assertEquals("comment field", "just a test", userJson.getString("comment"));
    assertTrue("approved field is not present", userJson.has("approved"));
    assertEquals("approved field", true, userJson.getBoolean("approved"));
    assertTrue("sha id field is not present", userJson.has("sha_id"));
    assertEquals("sha id field", "39ce7e2a8573b41ce73b5ba41617f8f7", userJson.getString("sha_id"));
    assertTrue("sha password field is not present", userJson.has("sha_password"));
    assertEquals("sha password field", "48948d0a35f82adb133e57cc62a81e5a", userJson.getString("sha_password"));
    assertJsonSourceEquals("User JSON", "{\"id\":\"jsmith\",\"password\":\"catylack\",\"password_hint\":\"What is my pet?\",\"full_name\":\"John W. Smith, Jr.\",\"display_name\":\"John Smith\",\"nick_name\":\"John\",\"bio\":\"Hacker and Slacker\",\"interests\":\"hacking, slacking\",\"incognito\":true,\"email\":\"jsmith@example.com\",\"comment\":\"just a test\", \"approved\": true,\"sha_id\":\"39ce7e2a8573b41ce73b5ba41617f8f7\",\"sha_password\":\"48948d0a35f82adb133e57cc62a81e5a\", \"organization\": \"MyCo, Inc.\"}", userJson.toString());
    assertEquals("Number of fields in user JSON", 15, userJson.length());

    // Test change of password update - no other field changes
    url = baseUrl + "/users/jsmith?password=catylack";
    userJson = doPutJson(url, "{\"password\": \"new-caty\"}", 204);

    url = baseUrl + "/users/jsmith?password=new-caty";
    userJson = doGetJson(url, 200);
    assertTrue("id field is not present", userJson.has("id"));
    assertEquals("id field", "jsmith", userJson.getString("id"));
    assertTrue("password field is not present", userJson.has("password"));
    assertEquals("password field", "new-caty", userJson.getString("password"));
    assertTrue("password_hint field is not present", userJson.has("password_hint"));
    assertEquals("password_hint field", "What is my pet?", userJson.getString("password_hint"));
    assertTrue("full_name field is not present", userJson.has("full_name"));
    assertEquals("full_name field", "John W. Smith, Jr.", userJson.getString("full_name"));
    assertTrue("display_name field is not present", userJson.has("display_name"));
    assertEquals("display_name field", "John Smith", userJson.getString("display_name"));
    assertTrue("nick_name field is not present", userJson.has("nick_name"));
    assertEquals("nick_name field", "John", userJson.getString("nick_name"));
    assertTrue("organization field is not present", userJson.has("organization"));
    assertEquals("organization field", "MyCo, Inc.", userJson.getString("organization"));
    assertTrue("bio field is not present", userJson.has("bio"));
    assertEquals("bio field", "Hacker and Slacker", userJson.getString("bio"));
    assertTrue("interests field is not present", userJson.has("interests"));
    assertEquals("interests field", "hacking, slacking", userJson.getString("interests"));
    assertTrue("email field is not present", userJson.has("email"));
    assertEquals("email field", "jsmith@example.com", userJson.getString("email"));
    assertTrue("comment field is not present", userJson.has("comment"));
    assertEquals("comment field", "just a test", userJson.getString("comment"));
    assertTrue("incognito field is not present", userJson.has("incognito"));
    assertEquals("incognito field", true, userJson.getBoolean("incognito"));
    assertTrue("approved field is not present", userJson.has("approved"));
    assertEquals("approved field", true, userJson.getBoolean("approved"));
    assertTrue("sha id field is not present", userJson.has("sha_id"));
    assertEquals("sha id field", "39ce7e2a8573b41ce73b5ba41617f8f7", userJson.getString("sha_id"));
    assertTrue("sha password field is not present", userJson.has("sha_password"));
    assertEquals("sha password field", "80493da6699540fe8d5077bb2f2c5aad", userJson.getString("sha_password"));
    assertJsonSourceEquals("User JSON", "{\"id\":\"jsmith\",\"password\":\"new-caty\",\"password_hint\":\"What is my pet?\",\"full_name\":\"John W. Smith, Jr.\",\"display_name\":\"John Smith\",\"nick_name\":\"John\",\"bio\":\"Hacker and Slacker\",\"interests\":\"hacking, slacking\",\"incognito\":true,\"email\":\"jsmith@example.com\",\"comment\":\"just a test\",\"approved\":true,\"sha_id\":\"39ce7e2a8573b41ce73b5ba41617f8f7\",\"sha_password\":\"80493da6699540fe8d5077bb2f2c5aad\", \"organization\": \"MyCo, Inc.\"}", userJson.toString());
    assertEquals("Number of fields in user JSON", 15, userJson.length());

    // Test change of all fields update
    url = baseUrl + "/users/jsmith?password=new-caty";
    userJson = doPutJson(url, "{\"nick_name\":\"Johnxx\",\"id\":\"jsmithxx\",\"display_name\":\"John Smithxx\",\"bio\":\"Hacker and Slackerxx\",\"email\":\"jsmith@example.comxx\",\"interests\":\"hacking, slackingxx\",\"incognito\":\"no\",\"sha_id\":\"39ce7e2a8573b41ce73b5ba41617f8f7\",\"password_hint\":\"What is my pet?xx\",\"password\":\"new-catyxx\",\"sha_password\":\"48948d0a35f82adb133e57cc62a81e5a\",\"full_name\":\"John W. Smith, Jr.xx\", \"comment\": \"just a testxx\"}", 204);

    url = baseUrl + "/users/jsmith?password=new-catyxx";
    userJson = doGetJson(url, 200);
    assertTrue("id field is not present", userJson.has("id"));
    assertEquals("id field", "jsmith", userJson.getString("id"));
    assertTrue("password field is not present", userJson.has("password"));
    assertEquals("password field", "new-catyxx", userJson.getString("password"));
    assertTrue("password_hint field is not present", userJson.has("password_hint"));
    assertEquals("password_hint field", "What is my pet?xx", userJson.getString("password_hint"));
    assertTrue("full_name field is not present", userJson.has("full_name"));
    assertEquals("full_name field", "John W. Smith, Jr.xx", userJson.getString("full_name"));
    assertTrue("display_name field is not present", userJson.has("display_name"));
    assertEquals("display_name field", "John Smithxx", userJson.getString("display_name"));
    assertTrue("nick_name field is not present", userJson.has("nick_name"));
    assertEquals("nick_name field", "Johnxx", userJson.getString("nick_name"));
    assertTrue("bio field is not present", userJson.has("bio"));
    assertEquals("bio field", "Hacker and Slackerxx", userJson.getString("bio"));
    assertTrue("interests field is not present", userJson.has("interests"));
    assertEquals("interests field", "hacking, slackingxx", userJson.getString("interests"));
    assertTrue("email field is not present", userJson.has("email"));
    assertEquals("email field", "jsmith@example.comxx", userJson.getString("email"));
    assertTrue("incognito field is not present", userJson.has("incognito"));
    assertEquals("incognito field", false, userJson.getBoolean("incognito"));
    assertTrue("comment field is not present", userJson.has("comment"));
    assertEquals("comment field", "just a testxx", userJson.getString("comment"));
    assertTrue("approved field is not present", userJson.has("approved"));
    assertEquals("approved field", true, userJson.getBoolean("approved"));
    assertTrue("sha id field is not present", userJson.has("sha_id"));
    assertEquals("sha id field", "39ce7e2a8573b41ce73b5ba41617f8f7", userJson.getString("sha_id"));
    assertTrue("sha password field is not present", userJson.has("sha_password"));
    assertEquals("sha password field", "66ae87a1f83faf23ee6d66291b3d61b5", userJson.getString("sha_password"));
    assertJsonSourceEquals("User JSON", "{\"id\":\"jsmith\",\"password\":\"new-catyxx\",\"password_hint\":\"What is my pet?xx\",\"full_name\":\"John W. Smith, Jr.xx\",\"display_name\":\"John Smithxx\",\"nick_name\":\"Johnxx\",\"bio\":\"Hacker and Slackerxx\",\"interests\":\"hacking, slackingxx\",\"incognito\":false,\"email\":\"jsmith@example.comxx\",\"comment\":\"just a testxx\",\"approved\":true,\"sha_id\":\"39ce7e2a8573b41ce73b5ba41617f8f7\",\"sha_password\":\"66ae87a1f83faf23ee6d66291b3d61b5\", \"organization\": \"MyCo, Inc.\"}", userJson.toString());
    assertEquals("Number of fields in user JSON", 15, userJson.length());

    // Test update of only the comment and interests fields
    url = baseUrl + "/users/jsmith?password=new-catyxx";
    userJson = doPutJson(url, "{\"interests\":\"work, leisure, play\", \"comment\": \"Just updated comment and interests\"}", 204);

    url = baseUrl + "/users/jsmith?password=new-catyxx";
    userJson = doGetJson(url, 200);
    assertTrue("id field is not present", userJson.has("id"));
    assertEquals("id field", "jsmith", userJson.getString("id"));
    assertTrue("password field is not present", userJson.has("password"));
    assertEquals("password field", "new-catyxx", userJson.getString("password"));
    assertTrue("password_hint field is not present", userJson.has("password_hint"));
    assertEquals("password_hint field", "What is my pet?xx", userJson.getString("password_hint"));
    assertTrue("full_name field is not present", userJson.has("full_name"));
    assertEquals("full_name field", "John W. Smith, Jr.xx", userJson.getString("full_name"));
    assertTrue("display_name field is not present", userJson.has("display_name"));
    assertEquals("display_name field", "John Smithxx", userJson.getString("display_name"));
    assertTrue("nick_name field is not present", userJson.has("nick_name"));
    assertEquals("nick_name field", "Johnxx", userJson.getString("nick_name"));
    assertTrue("organization field is not present", userJson.has("organization"));
    assertEquals("organization field", "MyCo, Inc.", userJson.getString("organization"));
    assertTrue("bio field is not present", userJson.has("bio"));
    assertEquals("bio field", "Hacker and Slackerxx", userJson.getString("bio"));
    assertTrue("interests field is not present", userJson.has("interests"));
    assertEquals("interests field", "work, leisure, play", userJson.getString("interests"));
    assertTrue("email field is not present", userJson.has("email"));
    assertEquals("email field", "jsmith@example.comxx", userJson.getString("email"));
    assertTrue("incognito field is not present", userJson.has("incognito"));
    assertEquals("incognito field", false, userJson.getBoolean("incognito"));
    assertTrue("comment field is not present", userJson.has("comment"));
    assertEquals("comment field", "Just updated comment and interests", userJson.getString("comment"));
    assertTrue("approved field is not present", userJson.has("approved"));
    assertEquals("approved field", true, userJson.getBoolean("approved"));
    assertTrue("sha id field is not present", userJson.has("sha_id"));
    assertEquals("sha id field", "39ce7e2a8573b41ce73b5ba41617f8f7", userJson.getString("sha_id"));
    assertTrue("sha password field is not present", userJson.has("sha_password"));
    assertEquals("sha password field", "66ae87a1f83faf23ee6d66291b3d61b5", userJson.getString("sha_password"));
    assertJsonSourceEquals("User JSON", "{\"id\":\"jsmith\",\"password\":\"new-catyxx\",\"password_hint\":\"What is my pet?xx\",\"full_name\":\"John W. Smith, Jr.xx\",\"display_name\":\"John Smithxx\",\"nick_name\":\"Johnxx\",\"bio\":\"Hacker and Slackerxx\",\"interests\":\"work, leisure, play\",\"incognito\":false,\"email\":\"jsmith@example.comxx\",\"comment\":\"Just updated comment and interests\",\"approved\":true,\"sha_id\":\"39ce7e2a8573b41ce73b5ba41617f8f7\",\"sha_password\":\"66ae87a1f83faf23ee6d66291b3d61b5\", \"organization\": \"MyCo, Inc.\"}", userJson.toString());
    assertEquals("Number of fields in user JSON", 15, userJson.length());

    // Test if users reflected in /status
    url = baseUrl + "/status";
    JSONObject statusJson = doGetJson(url, 200);
    assertTrue("num_registered_users is not present", statusJson.has("num_registered_users"));
    assertEquals("num_registered_users", 2, statusJson.getInt("num_registered_users"));
    assertTrue("num_active_users is not present", statusJson.has("num_active_users"));
    assertEquals("num_active_users", 0, statusJson.getInt("num_active_users"));

  }
  
  @Test
  public void testNoOp() throws Exception {
    
  }
  
  @Test
  public void testAbout() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    String url = baseUrl + "/about";
    JSONObject aboutJson = doGetJson(url, 200);
    assertTrue("About JSON not returned", aboutJson != null);
    assertEquals("Number of about keys", 6, aboutJson.length());
    assertTrue("name is not present", aboutJson.has("name"));
    assertEquals("name", "MyTestAgentServer-0001", aboutJson.getString("name"));
    assertTrue("version is not present", aboutJson.has("version"));
    assertEquals("version", "0.1.0", aboutJson.getString("version"));
    assertTrue("software is not present", aboutJson.has("software"));
    assertEquals("software", "s0", aboutJson.getString("software"));
    assertTrue("description is not present", aboutJson.has("description"));
    assertEquals("description", "Test server for Agent Server - Stage 0", aboutJson.getString("description"));
    assertTrue("contact is not present", aboutJson.has("contact"));
    assertEquals("contact", "agent-server-1-admin@basetechnology.com", aboutJson.getString("contact"));
    assertTrue("website is not present", aboutJson.has("website"));
    assertEquals("website", "http://basetechnology.com/agentserver", aboutJson.getString("website"));
    assertJsonSourceEquals("About JSON", "{\"name\":\"MyTestAgentServer-0001\",\"software\":\"s0\",\"version\":\"0.1.0\",\"description\":\"Test server for Agent Server - Stage 0\",\"website\":\"http://basetechnology.com/agentserver\",\"contact\":\"agent-server-1-admin@basetechnology.com\"}", aboutJson.toString());
  }
  
  @Test
  public void testConfig() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Determine if mail access is enabled
    boolean mailAccessEnabled =server.agentServer.config.getMailAccessEnabled();
    
    int numConfigKeys = 34;
    
    // Test reading of config settings
    String url = baseUrl + "/config";
    JSONObject configJson = doGetJson(url, 200);
    assertTrue("Config JSON not returned", configJson != null);
    assertEquals("Number of config keys", numConfigKeys, configJson.length());
    assertTrue("name is not present", configJson.has("name"));
    assertEquals("name", "MyTestAgentServer-0001", configJson.getString("name"));
    assertTrue("version is not present", configJson.has("version"));
    assertEquals("version", "0.1.0", configJson.getString("version"));
    assertTrue("software is not present", configJson.has("software"));
    assertEquals("software", "s0", configJson.getString("software"));
    assertTrue("description is not present", configJson.has("description"));
    assertEquals("description", "Test server for Agent Server - Stage 0", configJson.getString("description"));
    assertTrue("contact is not present", configJson.has("contact"));
    assertEquals("contact", "agent-server-1-admin@basetechnology.com", configJson.getString("contact"));
    assertTrue("website is not present", configJson.has("website"));
    assertEquals("website", "http://basetechnology.com/agentserver", configJson.getString("website"));
    assertTrue("admin_approve_user_create is not present", configJson.has("admin_approve_user_create"));
    assertEquals("admin_approve_user_create", false, configJson.getBoolean("admin_approve_user_create"));
    assertTrue("mail_confirm_user_create is not present", configJson.has("mail_confirm_user_create"));
    assertEquals("mail_confirm_user_create", false, configJson.getBoolean("mail_confirm_user_create"));
    assertTrue("user_agent_name is not present", configJson.has("user_agent_name"));
    assertEquals("user_agent_name", "AgentServer", configJson.getString("user_agent_name"));
    assertTrue("default_web_page_refresh_interval is not present", configJson.has("default_web_page_refresh_interval"));
    assertEquals("default_web_page_refresh_interval", "60000", configJson.getString("default_web_page_refresh_interval"));
    assertTrue("minimum_web_page_refresh_interval is not present", configJson.has("minimum_web_page_refresh_interval"));
    assertEquals("minimum_web_page_refresh_interval", "60000", configJson.getString("minimum_web_page_refresh_interval"));
    assertTrue("minimum_web_site_access_interval is not present", configJson.has("minimum_web_site_access_interval"));
    assertEquals("minimum_web_site_access_interval", "60000", configJson.getString("minimum_web_site_access_interval"));
    assertTrue("minimum_web_access_interval is not present", configJson.has("minimum_web_access_interval"));
    assertEquals("minimum_web_access_interval", "100", configJson.getString("minimum_web_access_interval"));
    assertTrue("implicitly_deny_web_access is not present", configJson.has("implicitly_deny_web_access"));
    assertEquals("implicitly_deny_web_access", "false", configJson.getString("implicitly_deny_web_access"));
    assertTrue("implicitly_deny_web_write_access is not present", configJson.has("implicitly_deny_web_write_access"));
    assertEquals("implicitly_deny_web_write_access", "true", configJson.getString("implicitly_deny_web_write_access"));
    assertTrue("mail_access_enabled not present", configJson.has("mail_access_enabled"));
    assertEquals("mail_access_enabled", Boolean.toString(server.agentServer.config.getMailAccessEnabled()), configJson.getString("mail_access_enabled"));
    assertTrue("minimum_mail_access_interval is not present", configJson.has("minimum_mail_access_interval"));
    assertEquals("minimum_mail_access_interval", Long.toString(MailAccessManager.DEFAULT_MINIMUM_MAIL_ACCESS_INTERVAL), configJson.getString("minimum_mail_access_interval"));
    assertTrue("minimum_host_mail_access_interval is not present", configJson.has("minimum_host_mail_access_interval"));
    assertEquals("minimum_host_mail_access_interval", Long.toString(MailAccessManager.DEFAULT_MINIMUM_HOST_MAIL_ACCESS_INTERVAL), configJson.getString("minimum_host_mail_access_interval"));
    assertTrue("minimum_address_mail_access_interval is not present", configJson.has("minimum_address_mail_access_interval"));
    assertEquals("minimum_address_mail_access_interval", Long.toString(MailAccessManager.DEFAULT_MINIMUM_ADDRESS_MAIL_ACCESS_INTERVAL), configJson.getString("minimum_address_mail_access_interval"));
    assertTrue("max_users is not present", configJson.has("max_users"));
    assertEquals("max_users", "100", configJson.getString("max_users"));
    assertTrue("max_instances is not present", configJson.has("max_instances"));
    assertEquals("max_instances", "1000", configJson.getString("max_instances"));
    assertTrue("default_trigger_interval is not present", configJson.has("default_trigger_interval"));
    assertEquals("default_trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION, configJson.getString("default_trigger_interval"));
    assertTrue("default_reporting_interval is not present", configJson.has("default_reporting_interval"));
    assertEquals("default_reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION, configJson.getString("default_reporting_interval"));
    assertTrue("minimum_trigger_interval is not present", configJson.has("minimum_trigger_interval"));
    assertEquals("minimum_trigger_interval", AgentDefinition.DEFAULT_MINIMUM_TRIGGER_INTERVAL_EXPRESSION, configJson.getString("minimum_trigger_interval"));
    assertTrue("minimum_reporting_interval is not present", configJson.has("minimum_reporting_interval"));
    assertEquals("minimum_reporting_interval", AgentDefinition.DEFAULT_MINIMUM_REPORTING_INTERVAL_EXPRESSION, configJson.getString("minimum_reporting_interval"));
    assertTrue("default_limit_instance_states_stored is not present", configJson.has("default_limit_instance_states_stored"));
    assertEquals("default_limit_instance_states_stored", AgentInstance.DEFAULT_LIMIT_INSTANCE_STATES_STORED, configJson.getInt("default_limit_instance_states_stored"));
    assertTrue("maximum_limit_instance_states_stored is not present", configJson.has("maximum_limit_instance_states_stored"));
    assertEquals("maximum_limit_instance_states_stored", AgentInstance.DEFAULT_MAXIMUM_LIMIT_INSTANCE_STATES_STORED, configJson.getInt("maximum_limit_instance_states_stored"));
    assertTrue("default_limit_instance_states_returned is not present", configJson.has("default_limit_instance_states_returned"));
    assertEquals("default_limit_instance_states_returned", AgentInstance.DEFAULT_LIMIT_INSTANCE_STATES_RETURNED, configJson.getInt("default_limit_instance_states_returned"));
    assertTrue("maximum_limit_instance_states_returned is not present", configJson.has("maximum_limit_instance_states_returned"));
    assertEquals("maximum_limit_instance_states_returned", AgentInstance.DEFAULT_MAXIMUM_LIMIT_INSTANCE_STATES_RETURNED, configJson.getInt("maximum_limit_instance_states_returned"));
    String initialConfigJsonExpected =
        "{\"name\":\"MyTestAgentServer-0001\"," +
            "\"software\":\"s0\",\"version\":\"0.1.0\"," +
            "\"description\":\"Test server for Agent Server - Stage 0\"," +
            "\"contact\":\"agent-server-1-admin@basetechnology.com\"," +
            "\"default_trigger_interval\":\"50\"," +
            "\"minimum_trigger_interval\":\"5\"," +
            "\"default_limit_instance_states_stored\":\"25\"," +
            "\"maximum_limit_instance_states_stored\":\"1000\"," +
            "\"default_limit_instance_states_returned\":\"10\"," +
            "\"maximum_limit_instance_states_returned\":\"1000\"," +
            "\"max_users\":\"100\"," +
            "\"max_instances\":\"1000\"," +
            "\"website\":\"http://basetechnology.com/agentserver\"," +
            "\"default_web_page_refresh_interval\":\"60000\"," +
            "\"minimum_web_page_refresh_interval\":\"60000\"," +
            "\"minimum_web_site_access_interval\":\"60000\"," +
            "\"minimum_web_access_interval\":\"100\"," +
            "\"user_agent_name\":\"AgentServer\"," +
            "\"default_reporting_interval\":\"200\"," +
            "\"minimum_reporting_interval\":\"5\"," +
            "\"implicitly_deny_web_access\":\"false\"," +
            "\"implicitly_deny_web_write_access\":\"true\"," +
            "\"execution_limit_default_level\": \"2\"," +
            "\"execution_limit_level_1\": \"10\"," +
            "\"execution_limit_level_2\": \"100\"," +
            "\"execution_limit_level_3\": \"1000\"," +
            "\"execution_limit_level_4\": \"10000\"," +
            "\"mail_access_enabled\": \"" + mailAccessEnabled + "\"," +
            "\"minimum_mail_access_interval\": \"2000\"," +
            "\"minimum_host_mail_access_interval\": \"2000\"," +
            "\"minimum_address_mail_access_interval\": \"10000\"," +
            "\"admin_approve_user_create\": \"false\"," +
            "\"mail_confirm_user_create\": \"false\"}";
    assertJsonSourceEquals("config JSON", initialConfigJsonExpected, configJson.toString());

    // Try update without password - should fail
    configJson = doPutJson(url, "{}", 400);
    assertTrue("Exception JSON not returned", configJson != null);
    assertJsonSourceEquals("Exception", "{\"errors\":[{\"message\":\"Missing admin password query parameter\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException\"}]}", configJson.toString());

    // Try update with bad password - should fail
    configJson = doPutJson(url + "?password=junk", "{}", 400);
    assertTrue("Exception JSON not returned", configJson != null);
    assertJsonSourceEquals("Exception", "{\"errors\":[{\"message\":\"Invalid admin password\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException\"}]}", configJson.toString());

    configJson = doPutJson(url + "?password=", "{}", 400);
    assertTrue("Exception JSON not returned", configJson != null);
    assertJsonSourceEquals("Exception", "{\"errors\":[{\"message\":\"Empty admin password query parameter\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException\"}]}", configJson.toString());

    // Do an update with no changes and verify that nothing changed
    configJson = doPutJson(url + "?password=abracadabra", "{}", 204);
    assertEquals("JSON returned", null, configJson);
    configJson = doGetJson(url, 200);
    assertTrue("Config JSON not returned", configJson != null);
    assertEquals("Number of config keys", numConfigKeys, configJson.length());
    assertJsonSourceEquals("config JSON", initialConfigJsonExpected, configJson.toString());

    // Update with a junk key - should fail
    configJson = doPutJson(url + "?password=", "{\"junk\": 3, \"abc\": \"def\", \"zebra\": \"donkey\"}", 400);
    assertTrue("Exception JSON not returned", configJson != null);
    assertJsonSourceEquals("Exception", "{\"errors\":[{\"message\":\"Empty admin password query parameter\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException\"}]}", configJson.toString());

    // Update a key with the identical value - should be a no-op
    configJson = doPutJson(url + "?password=abracadabra", "{\"name\": \"MyTestAgentServer-0001\"}", 204);
    assertEquals("JSON returned", null, configJson);
    configJson = doGetJson(url, 200);
    assertTrue("Config JSON not returned", configJson != null);
    assertEquals("Number of config keys", numConfigKeys, configJson.length());
    assertJsonSourceEquals("config JSON", initialConfigJsonExpected, configJson.toString());

    // Update with the same JSON returned by GET - should also be a no-op
    configJson = doPutJson(url + "?password=abracadabra", configJson.toString(), 204);
    assertEquals("JSON returned", null, configJson);
    configJson = doGetJson(url, 200);
    assertTrue("Config JSON not returned", configJson != null);
    assertEquals("Number of config keys", numConfigKeys, configJson.length());
    assertJsonSourceEquals("config JSON", initialConfigJsonExpected, configJson.toString());

    // Update a few values
    configJson = doPutJson(url + "?password=abracadabra", "{\"version\":\"2.0\",\"description\":\"My Test server for Agent Server - Stage 0\",\"max_users\":\"125\",\"user_agent_name\":\"MyAgentServer\"}", 204);
    assertEquals("JSON returned", null, configJson);
    configJson = doGetJson(url, 200);
    assertTrue("Config JSON not returned", configJson != null);
    assertEquals("Number of config keys", numConfigKeys, configJson.length());
    String someUpdatedConfigJsonExpected =
        "{\"name\":\"MyTestAgentServer-0001\"," +
            "\"software\":\"s0\",\"version\":\"2.0\"," +
            "\"description\":\"My Test server for Agent Server - Stage 0\"," +
            "\"contact\":\"agent-server-1-admin@basetechnology.com\"," +
            "\"default_trigger_interval\":\"50\"," +
            "\"minimum_trigger_interval\":\"5\"," +
            "\"default_limit_instance_states_stored\":\"25\"," +
            "\"maximum_limit_instance_states_stored\":\"1000\"," +
            "\"default_limit_instance_states_returned\":\"10\"," +
            "\"maximum_limit_instance_states_returned\":\"1000\"," +
            "\"max_users\":\"125\"," +
            "\"max_instances\":\"1000\"," +
            "\"website\":\"http://basetechnology.com/agentserver\"," +
            "\"default_web_page_refresh_interval\":\"60000\"," +
            "\"minimum_web_page_refresh_interval\":\"60000\"," +
            "\"minimum_web_site_access_interval\":\"60000\"," +
            "\"minimum_web_access_interval\":\"100\"," +
            "\"user_agent_name\":\"MyAgentServer\"," +
            "\"default_reporting_interval\":\"200\"," +
            "\"minimum_reporting_interval\":\"5\"," +
            "\"implicitly_deny_web_access\":\"false\"," +
            "\"implicitly_deny_web_write_access\":\"true\"," +
            "\"execution_limit_default_level\": \"2\"," +
            "\"execution_limit_level_1\": \"10\"," +
            "\"execution_limit_level_2\": \"100\"," +
            "\"execution_limit_level_3\": \"1000\"," +
            "\"execution_limit_level_4\": \"10000\"," +
            "\"mail_access_enabled\": \"" + mailAccessEnabled + "\"," +
            "\"minimum_mail_access_interval\": \"2000\"," +
            "\"minimum_host_mail_access_interval\": \"2000\"," +
            "\"minimum_address_mail_access_interval\": \"10000\"," +
            "\"admin_approve_user_create\": \"false\"," +
            "\"mail_confirm_user_create\": \"false\"}";
    assertJsonSourceEquals("config JSON", someUpdatedConfigJsonExpected, configJson.toString());
    
    // Update all values
    configJson = doPutJson(url + "?password=abracadabra", "{\"name\":\"ZZ-MyTestAgentServer-0001\",\"software\":\"zz-s0\",\"version\":\"zz-0.1.0\",\"description\":\"zz-Test server for Agent Server - Stage 0\",\"contact\":\"zz-jack@basetechnology.com\",\"default_trigger_interval\":\"1200\",\"max_users\":\"1100\",\"max_instances\":\"11000\",\"website\":\"zzhttp://basetechnology.com/agentserver\",\"default_web_page_refresh_interval\":\"160000\",\"minimum_web_page_refresh_interval\":\"100000\",\"minimum_web_site_access_interval\":\"90000\",\"minimum_web_access_interval\":\"75\",\"user_agent_name\":\"zz-AgentServer\",\"default_reporting_interval\":\"150\",\"implicitly_deny_web_access\":\"false\",\"implicitly_deny_web_write_access\":\"true\", \"execution_limit_default_level\": \"2\", \"execution_limit_level_1\": \"11\", \"execution_limit_level_2\": \"220\", \"execution_limit_level_3\": \"3300\", \"execution_limit_level_4\": \"44000\", \"mail_access_enabled\": \"false\", \"minimum_mail_access_interval\": \"3300\", \"minimum_host_mail_access_interval\": \"4400\", \"minimum_address_mail_access_interval\": \"44000\", \"admin_approve_user_create\": \"true\", \"mail_confirm_user_create\": \"true\", \"minimum_trigger_interval\": \"55\", \"minimum_reporting_interval\": \"56\", \"default_limit_instance_states_stored\":\"257\",\"maximum_limit_instance_states_stored\":\"10007\",\"default_limit_instance_states_returned\":\"107\",\"maximum_limit_instance_states_returned\":\"10007\"}", 204);
    assertEquals("JSON returned", null, configJson);
    configJson = doGetJson(url, 200);
    assertTrue("Config JSON not returned", configJson != null);
    assertEquals("Number of config keys", numConfigKeys, configJson.length());
    String allUpdatedConfigJsonExpected =
        "{\"name\":\"ZZ-MyTestAgentServer-0001\"," +
        "\"software\":\"zz-s0\"," +
        "\"version\":\"zz-0.1.0\"," +
        "\"description\":\"zz-Test server for Agent Server - Stage 0\"," +
        "\"contact\":\"zz-jack@basetechnology.com\"," +
        "\"default_trigger_interval\":\"1200\"," +
        "\"minimum_trigger_interval\":\"55\"," +
        "\"default_limit_instance_states_stored\":\"257\"," +
        "\"maximum_limit_instance_states_stored\":\"10007\"," +
        "\"default_limit_instance_states_returned\":\"107\"," +
        "\"maximum_limit_instance_states_returned\":\"10007\"," +
        "\"max_users\":\"1100\"," +
        "\"max_instances\":\"11000\"," +
        "\"website\":\"zzhttp://basetechnology.com/agentserver\"," +
        "\"default_web_page_refresh_interval\":\"160000\"," +
        "\"minimum_web_page_refresh_interval\":\"100000\"," +
        "\"minimum_web_site_access_interval\":\"90000\"," +
        "\"minimum_web_access_interval\":\"75\"," +
        "\"user_agent_name\":\"zz-AgentServer\"," +
        "\"default_reporting_interval\":\"150\"," +
        "\"minimum_reporting_interval\":\"56\"," +
        "\"implicitly_deny_web_access\":\"false\"," +
        "\"implicitly_deny_web_write_access\":\"true\"," +
        "\"execution_limit_default_level\": \"2\"," +
        "\"execution_limit_level_1\": \"11\"," +
        "\"execution_limit_level_2\": \"220\"," +
        "\"execution_limit_level_3\": \"3300\"," +
        "\"execution_limit_level_4\": \"44000\"," +
        "\"mail_access_enabled\": \"false\"," +
    		"\"minimum_mail_access_interval\": \"3300\"," +
    		"\"minimum_host_mail_access_interval\": \"4400\"," +
    		"\"minimum_address_mail_access_interval\": \"44000\"," +
    		"\"admin_approve_user_create\": \"true\"," +
    		"\"mail_confirm_user_create\": \"true\"}";
    assertJsonSourceEquals("config JSON", allUpdatedConfigJsonExpected, configJson.toString());
    
    // Restart server and verify that config settings were persisted
    server.restart();
    configJson = doGetJson(url, 200);
    assertTrue("Config JSON not returned", configJson != null);
    assertEquals("Number of config keys", numConfigKeys, configJson.length());
    assertJsonSourceEquals("config JSON", allUpdatedConfigJsonExpected, configJson.toString());
    
    // Restore settings to original values
    configJson = doPutJson(url + "/reset?password=abracadabra", 204);
    configJson = doGetJson(url, 200);
    assertTrue("Config JSON not returned", configJson != null);
    assertEquals("Number of config keys", numConfigKeys, configJson.length());
    assertJsonSourceEquals("config JSON", initialConfigJsonExpected, configJson.toString());
  }
  
  @Test
  public void testStatus() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Test status for running server
    String url = baseUrl + "/status";
    JSONObject statusJson = doGetJson(url, 200);
    assertTrue("status is not present", statusJson.has("status"));
    assertEquals("status", "running", statusJson.getString("status"));
    assertTrue("since is not present", statusJson.has("since"));
    String since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("since", since, statusJson.getString("since"));
    assertTrue("num_registered_users is not present", statusJson.has("num_registered_users"));
    assertEquals("num_registered_users", 0, statusJson.getInt("num_registered_users"));
    assertTrue("num_active_users is not present", statusJson.has("num_active_users"));
    assertEquals("num_active_users", 0, statusJson.getInt("num_active_users"));
    assertTrue("num_registered_agents is not present", statusJson.has("num_registered_agents"));
    assertEquals("num_registered_agents", 0, statusJson.getInt("num_registered_agents"));
    assertTrue("num_active_agents is not present", statusJson.has("num_active_agents"));
    assertEquals("num_active_agents", 0, statusJson.getInt("num_active_agents"));
    assertEquals("Number of elements in status", 6, statusJson.length());
    assertEquals("Status JSON", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":0,\"num_active_users\":0,\"num_registered_agents\":0,\"num_active_agents\":0}", statusJson.toString());

    // Test status for paused server
    url = baseUrl + "/status/pause?password=" + server.agentServer.getAdminPassword();
    statusJson = doPutJson(url, 204);

    url = baseUrl + "/status";
    statusJson = doGetJson(url, 200);
    assertTrue("status is not present", statusJson.has("status"));
    assertEquals("status", "paused", statusJson.getString("status"));
    assertTrue("since is not present", statusJson.has("since"));
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("since", since, statusJson.getString("since"));
    assertTrue("num_registered_users is not present", statusJson.has("num_registered_users"));
    assertEquals("num_registered_users", 0, statusJson.getInt("num_registered_users"));
    assertTrue("num_active_users is not present", statusJson.has("num_active_users"));
    assertEquals("num_active_users", 0, statusJson.getInt("num_active_users"));
    assertTrue("num_registered_agents is not present", statusJson.has("num_registered_agents"));
    assertEquals("num_registered_agents", 0, statusJson.getInt("num_registered_agents"));
    assertTrue("num_active_agents is not present", statusJson.has("num_active_agents"));
    assertEquals("num_active_agents", 0, statusJson.getInt("num_active_agents"));
    assertEquals("Status JSON", "{\"status\":\"paused\",\"since\":\"" + since + "\",\"num_registered_users\":0,\"num_active_users\":0,\"num_registered_agents\":0,\"num_active_agents\":0}", statusJson.toString());

    // Test status for resume of paused server
    url = baseUrl + "/status/resume?password=" + server.agentServer.getAdminPassword();
    statusJson = doPutJson(url, 204);

    url = baseUrl + "/status";
    statusJson = doGetJson(url, 200);
    assertTrue("status is not present", statusJson.has("status"));
    assertEquals("status", "running", statusJson.getString("status"));
    assertTrue("since is not present", statusJson.has("since"));
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("since", since, statusJson.getString("since"));
    assertTrue("num_registered_users is not present", statusJson.has("num_registered_users"));
    assertEquals("num_registered_users", 0, statusJson.getInt("num_registered_users"));
    assertTrue("num_active_users is not present", statusJson.has("num_active_users"));
    assertEquals("num_active_users", 0, statusJson.getInt("num_active_users"));
    assertTrue("num_registered_agents is not present", statusJson.has("num_registered_agents"));
    assertEquals("num_registered_agents", 0, statusJson.getInt("num_registered_agents"));
    assertTrue("num_active_agents is not present", statusJson.has("num_active_agents"));
    assertEquals("num_active_agents", 0, statusJson.getInt("num_active_agents"));
    assertEquals("Status JSON", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":0,\"num_active_users\":0,\"num_registered_agents\":0,\"num_active_agents\":0}", statusJson.toString());
    
    // Test status for shutdown server
    url = baseUrl + "/status/shutdown?password=" + server.agentServer.getAdminPassword();
    statusJson = doPutJson(url, 204);

    url = baseUrl + "/status";
    statusJson = doGetJson(url, 200);
    assertTrue("status is not present", statusJson.has("status"));
    assertEquals("status", "shutdown", statusJson.getString("status"));
    assertTrue("since is not present", statusJson.has("since"));
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("since", since, statusJson.getString("since"));
    assertTrue("num_registered_users is not present", statusJson.has("num_registered_users"));
    assertEquals("num_registered_users", 0, statusJson.getInt("num_registered_users"));
    assertTrue("num_active_users is not present", statusJson.has("num_active_users"));
    assertEquals("num_active_users", 0, statusJson.getInt("num_active_users"));
    assertTrue("num_registered_agents is not present", statusJson.has("num_registered_agents"));
    assertEquals("num_registered_agents", 0, statusJson.getInt("num_registered_agents"));
    assertTrue("num_active_agents is not present", statusJson.has("num_active_agents"));
    assertEquals("num_active_agents", 0, statusJson.getInt("num_active_agents"));
    assertEquals("Status JSON", "{\"status\":\"shutdown\",\"since\":\"" + since + "\",\"num_registered_users\":0,\"num_active_users\":0,\"num_registered_agents\":0,\"num_active_agents\":0}", statusJson.toString());

    // Test status after starting server
    url = baseUrl + "/status/start?password=" + server.agentServer.getAdminPassword();
    statusJson = doPutJson(url, 204);

    url = baseUrl + "/status";
    statusJson = doGetJson(url, 200);
    assertTrue("status is not present", statusJson.has("status"));
    assertEquals("status", "running", statusJson.getString("status"));
    assertTrue("since is not present", statusJson.has("since"));
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("since", since, statusJson.getString("since"));
    assertTrue("num_registered_users is not present", statusJson.has("num_registered_users"));
    assertEquals("num_registered_users", 0, statusJson.getInt("num_registered_users"));
    assertTrue("num_active_users is not present", statusJson.has("num_active_users"));
    assertEquals("num_active_users", 0, statusJson.getInt("num_active_users"));
    assertTrue("num_registered_agents is not present", statusJson.has("num_registered_agents"));
    assertEquals("num_registered_agents", 0, statusJson.getInt("num_registered_agents"));
    assertTrue("num_active_agents is not present", statusJson.has("num_active_agents"));
    assertEquals("num_active_agents", 0, statusJson.getInt("num_active_agents"));
    assertEquals("Status JSON", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":0,\"num_active_users\":0,\"num_registered_agents\":0,\"num_active_agents\":0}", statusJson.toString());

    // Test status after restarting server
    url = baseUrl + "/status/restart?password=" + server.agentServer.getAdminPassword();
    statusJson = doPutJson(url, 204);

    url = baseUrl + "/status";
    statusJson = doGetJson(url, 200);
    assertTrue("status is not present", statusJson.has("status"));
    assertEquals("status", "running", statusJson.getString("status"));
    assertTrue("since is not present", statusJson.has("since"));
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("since", since, statusJson.getString("since"));
    assertTrue("num_registered_users is not present", statusJson.has("num_registered_users"));
    assertEquals("num_registered_users", 0, statusJson.getInt("num_registered_users"));
    assertTrue("num_active_users is not present", statusJson.has("num_active_users"));
    assertEquals("num_active_users", 0, statusJson.getInt("num_active_users"));
    assertTrue("num_registered_agents is not present", statusJson.has("num_registered_agents"));
    assertEquals("num_registered_agents", 0, statusJson.getInt("num_registered_agents"));
    assertTrue("num_active_agents is not present", statusJson.has("num_active_agents"));
    assertEquals("num_active_agents", 0, statusJson.getInt("num_active_agents"));
    assertEquals("Status JSON", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":0,\"num_active_users\":0,\"num_registered_agents\":0,\"num_active_agents\":0}", statusJson.toString());

  }
  
  @Test
  public void testAgentDefinition() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Need a test user
    String url = baseUrl + "/users?id=test-user&password=test-pwd";
    JSONObject userJson = doPostJson(url, "{}", 201);

    // Test error on agent definition create without user id or password
    url = baseUrl + "/users/test-user/agent_definitions";
    JSONObject agDefJson = doPostJson(url, "{}", 400);
    assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Missing password query parameter\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException\"}]}", agDefJson.toString());

    // Test for missing name
    url = baseUrl + "/users/test-user/agent_definitions?password=test-pwd";
    agDefJson = doPostJson(url, "{}", 400);
    assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Agent definition name ('name') is missing\",\"type\":\"com.basetechnology.s0.agentserver.AgentServerException\"}]}", agDefJson.toString());

    // Test for creation of empty agent definition with only the required name
    url = baseUrl + "/users/test-user/agent_definitions?password=test-pwd";
    agDefJson = doPostJson(url, "{\"name\": \"test-definition\"}", 201);
    assertTrue("Unexpected response entity", agDefJson == null);

    // Test for error on creation of agent definition with name that is already defined
    url = baseUrl + "/users/test-user/agent_definitions?password=test-pwd";
    agDefJson = doPostJson(url, "{\"name\": \"test-definition\"}", 400);
    assertTrue("Response entity is missing", agDefJson != null);
    assertEquals("Error JSON", "{\"errors\":[{\"message\":\"Agent definition name already exists: 'test-definition'\",\"type\":\"com.basetechnology.s0.agentserver.AgentServerException\"}]}", agDefJson.toString());

    // Test creation of a second agent and check status
    url = baseUrl + "/users/test-user/agent_definitions?password=test-pwd";
    agDefJson = doPostJson(url, "{\"name\": \"test-definition-2\"}", 201);
    assertTrue("Unexpected response entity", agDefJson == null);

    // Check query of agent definitions
    url = baseUrl + "/users/test-user/agent_definitions/test-definition?password=test-pwd";
    agDefJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", agDefJson != null);

    url = baseUrl + "/users/test-user/agent_definitions/test-definition-2?password=test-pwd";
    agDefJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", agDefJson != null);

    url = baseUrl + "/users/test-user/agent_definitions/test-definition-3?password=test-pwd";
    agDefJson = doGetJson(url, 404);
    assertTrue("Response entity is missing", agDefJson != null);
    assertTrue("Errors missing", agDefJson.has("errors"));
    JSONArray errorsJson = agDefJson.getJSONArray("errors");
    assertEquals("Count of errors", 1, errorsJson.length());
    JSONObject errorJson = agDefJson.getJSONArray("errors").getJSONObject(0);
    assertEquals("Error[0] type", "com.basetechnology.s0.agentserver.appserver.AgentAppServerException", errorJson.getString("type"));
    assertEquals("Error[0] message", "No agent definition with that name for that user", errorJson.getString("message"));

    url = baseUrl + "/users/test-user/agent_definitions?password=test-pwd";
    agDefJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", agDefJson != null);
    JSONArray agentsJson = agDefJson.getJSONArray("agent_definitions");
    assertEquals("Count of agent definitions", 2, agentsJson.length());
    agDefJson = agentsJson.getJSONObject(0);
    assertEquals("Agent definition[0]", "{\"description\":\"\",\"name\":\"test-definition\",\"user\":\"test-user\"}", agDefJson.toString());
    agDefJson = agentsJson.getJSONObject(1);
    assertEquals("Agent definition[1]", "{\"description\":\"\",\"name\":\"test-definition-2\",\"user\":\"test-user\"}", agDefJson.toString());

    url = baseUrl + "/agent_definitions?password=abracadabra";
    agDefJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", agDefJson != null);
    agentsJson = agDefJson.getJSONArray("agent_definitions");
    assertEquals("Count of agent definitions", 2, agentsJson.length());
    agDefJson = agentsJson.getJSONObject(0);
    assertEquals("Agent definition[0]", "{\"description\":\"\",\"name\":\"test-definition\",\"user\":\"test-user\"}", agDefJson.toString());
    agDefJson = agentsJson.getJSONObject(1);
    assertEquals("Agent definition[1]", "{\"description\":\"\",\"name\":\"test-definition-2\",\"user\":\"test-user\"}", agDefJson.toString());

    // Test status for running server
    url = baseUrl + "/status";
    JSONObject statusJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", statusJson != null);
    String since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("Status JSON", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":1,\"num_active_users\":0,\"num_registered_agents\":2,\"num_active_agents\":0}", statusJson.toString());

    // Check query of agent definitions
    url = baseUrl + "/users/test-user/agent_definitions/test-definition/status?password=test-pwd";
    statusJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", statusJson != null);
    assertEquals("Number of status keys", 5, statusJson.length());
    long createdTime = server.agentServer.agentDefinitions.get("test-user").get("test-definition").timeCreated;
    long now = System.currentTimeMillis();
    assertTrue("Creation time is not set - zero", createdTime != 0);
    assertTrue("Creation time is in the future", createdTime <= now);
    assertTrue("Creation time too far in the past: " + (now - createdTime), now - createdTime < 5000);
    String created = DateUtils.toRfcString(createdTime);
    long modifiedTime = server.agentServer.agentDefinitions.get("test-user").get("test-definition").timeModified;
    assertTrue("Edit time is not set - zero", modifiedTime != 0);
    assertTrue("Edit time is in the future", modifiedTime <= now);
    assertTrue("Edit time too far in the past: " + (now - modifiedTime), now - modifiedTime < 5000);
    String modified = DateUtils.toRfcString(modifiedTime);
    assertEquals("Agent definition status", "{\"created\":\"" + created + "\",\"modified\":\"" + modified + "\",\"name\":\"test-definition\",\"num_active_instances\":0,\"user_id\":\"test-user\"}", statusJson.toString());

    // Update an agent definition
    url = baseUrl + "/users/test-user/agent_definitions?password=test-pwd";
    agDefJson = doPostJson(url, "{\"name\": \"test-definition-3\", \"outputs\": [{\"name\": \"output1\", \"type\": \"string\", \"default_value\": \"abc\"}]}", 201);
    assertTrue("Unexpected response entity", agDefJson == null);
    url = baseUrl + "/users/test-user/agent_definitions/test-definition-3?password=test-pwd";
    agDefJson = doGetJson(url, 200);
    AgentDefinition agDef = server.agentServer.agentDefinitions.get("test-user").get("test-definition-3");
    createdTime = agDef.timeCreated;
    now = System.currentTimeMillis();
    assertTrue("Creation time is not set - zero", createdTime != 0);
    assertTrue("Creation time is in the future", createdTime <= now);
    assertTrue("Creation time too far in the past: " + (now - createdTime), now - createdTime < 5000);
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agDef.timeModified;
    assertTrue("Modification time is not set - zero", modifiedTime != 0);
    assertTrue("Modification time is in the future", modifiedTime <= now);
    assertTrue("Modification time too far in the past: " + (now - modifiedTime), now - modifiedTime < 5000);
    modified = DateUtils.toRfcString(modifiedTime);
    assertJsonSourceEquals("Agent definition", "{\"user\": \"test-user\", \"name\": \"test-definition-3\", \"description\": \"\",\"created\": \"" + created + "\", \"modified\": \"" + modified + "\", \"parameters\": [], \"inputs\": [], \"timers\": [], \"conditions\": [], \"notifications\": [], \"scripts\": [], \"scratchpad\": [], \"memory\": [], \"outputs\": [{\"default_value\": \"abc\", \"name\": \"output1\", \"type\": \"string\"}], \"goals\": [], \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"enabled\": true}", agDefJson);

    // Now update the description
    agDefJson = doPutJson(url, "{\"description\": \"Our test definition\"}", 204);
    assertTrue("Unexpected response entity", agDefJson == null);

    url = baseUrl + "/users/test-user/agent_definitions/test-definition-3?password=test-pwd";
    agDefJson = doGetJson(url, 200);
    agDef = server.agentServer.agentDefinitions.get("test-user").get("test-definition-3");
    createdTime = agDef.timeCreated;
    now = System.currentTimeMillis();
    assertTrue("Creation time is not set - zero", createdTime != 0);
    assertTrue("Creation time is in the future", createdTime <= now);
    assertTrue("Creation time too far in the past: " + (now - createdTime), now - createdTime < 5000);
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agDef.timeModified;
    assertTrue("Modification time is not set - zero", modifiedTime != 0);
    assertTrue("Modification time is in the future", modifiedTime <= now);
    assertTrue("Modification time too far in the past: " + (now - modifiedTime), now - modifiedTime < 5000);
    modified = DateUtils.toRfcString(modifiedTime);
    assertJsonSourceEquals("Agent definition", "{\"user\": \"test-user\", \"name\": \"test-definition-3\", \"description\": \"Our test definition\",\"created\": \"" + created + "\", \"modified\": \"" + modified + "\", \"parameters\": [], \"inputs\": [], \"timers\": [], \"conditions\": [], \"notifications\": [], \"scripts\": [], \"scratchpad\": [], \"memory\": [], \"outputs\": [{\"default_value\": \"abc\", \"name\": \"output1\", \"type\": \"string\"}], \"goals\": [], \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"enabled\": true}", agDefJson);

  }
  
  @Test
  public void testAgentInstance() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create three test users
    String url = baseUrl + "/users?id=test-user-1&password=test-pwd-1";
    JSONObject userJson = doPostJson(url, "{}", 201);
    url = baseUrl + "/users?id=test-user-2&password=test-pwd-2";
    userJson = doPostJson(url, "{}", 201);
    url = baseUrl + "/users?id=test-user-3&password=test-pwd-3";
    userJson = doPostJson(url, "{}", 201);

    // Create two agent definitions for each user
    url = baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1";
    JSONObject agDefJson = doPostJson(url, "{\"name\": \"test-definition-1\", \"description\": \"Test definition #1\", \"parameters\": [{\"name\": \"p1\", \"type\": \"int\", \"default_value\": 123}]}", 201);
    url = baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1";
    agDefJson = doPostJson(url, "{\"name\": \"test-definition-2\", \"description\": \"Test definition #2\", \"parameters\": [{\"name\": \"p1\", \"type\": \"string\", \"default_value\": \"abc\"}]}", 201);

    url = baseUrl + "/users/test-user-2/agent_definitions?password=test-pwd-2";
    agDefJson = doPostJson(url, "{\"name\": \"test-definition-1\", \"description\": \"Test definition #3\", \"parameters\": [{\"name\": \"p1\", \"type\": \"int\", \"default_value\": 456}]}", 201);
    url = baseUrl + "/users/test-user-2/agent_definitions?password=test-pwd-2";
    agDefJson = doPostJson(url, "{\"name\": \"test-definition-2\", \"description\": \"Test definition #4\", \"parameters\": [{\"name\": \"p1\", \"type\": \"string\", \"default_value\": \"def\"}]}", 201);

    url = baseUrl + "/users/test-user-3/agent_definitions?password=test-pwd-3";
    agDefJson = doPostJson(url, "{\"name\": \"test-definition-1\", \"description\": \"Test definition #5\", \"parameters\": [{\"name\": \"p1\", \"type\": \"int\", \"default_value\": 789}]}", 201);
    url = baseUrl + "/users/test-user-3/agent_definitions?password=test-pwd-3";
    agDefJson = doPostJson(url, "{\"name\": \"test-definition-2\", \"description\": \"Test definition #6\", \"parameters\": [{\"name\": \"p1\", \"type\": \"string\", \"default_value\": \"ghi\"}]}", 201);

    // Instantiate each agent definition twice, once with default params, once with overrides
    url = baseUrl + "/users/test-user-1/agents?password=test-pwd-1";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-1\", \"description\": \"Test instance #1\", \"definition\": \"test-definition-1\",}", 201);
    url = baseUrl + "/users/test-user-1/agents?password=test-pwd-1";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-2\", \"description\": \"Test instance #2\", \"definition\": \"test-definition-1\", \"parameter_values\": {\"p1\": 12399}}", 201);

    url = baseUrl + "/users/test-user-1/agents?password=test-pwd-1";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-3\", \"description\": \"Test instance #3\", \"definition\": \"test-definition-2\",}", 201);
    url = baseUrl + "/users/test-user-1/agents?password=test-pwd-1";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-4\", \"description\": \"Test instance #4\", \"definition\": \"test-definition-2\", \"parameter_values\": {\"p1\": \"abc99\"}}", 201);

    url = baseUrl + "/users/test-user-2/agents?password=test-pwd-2";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-5\", \"description\": \"Test instance #5\", \"definition\": \"test-definition-1\",}", 201);
    url = baseUrl + "/users/test-user-2/agents?password=test-pwd-2";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-6\", \"description\": \"Test instance #6\", \"definition\": \"test-definition-1\", \"parameter_values\": {\"p1\": 45699}}", 201);

    url = baseUrl + "/users/test-user-2/agents?password=test-pwd-2";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-7\", \"description\": \"Test instance #7\", \"definition\": \"test-definition-2\",}", 201);
    url = baseUrl + "/users/test-user-2/agents?password=test-pwd-2";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-8\", \"description\": \"Test instance #8\", \"definition\": \"test-definition-2\", \"parameter_values\": {\"p1\": \"def99\"}}", 201);

    url = baseUrl + "/users/test-user-3/agents?password=test-pwd-3";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-9\", \"description\": \"Test instance #9\", \"definition\": \"test-definition-1\",}", 201);
    url = baseUrl + "/users/test-user-3/agents?password=test-pwd-3";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-10\", \"description\": \"Test instance #10\", \"definition\": \"test-definition-1\", \"parameter_values\": {\"p1\": 78999}}", 201);

    url = baseUrl + "/users/test-user-3/agents?password=test-pwd-3";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-11\", \"description\": \"Test instance #11\", \"definition\": \"test-definition-2\",}", 201);
    url = baseUrl + "/users/test-user-3/agents?password=test-pwd-3";
    agDefJson = doPostJson(url, "{\"name\": \"test-instance-12\", \"description\": \"Test instance #12\", \"definition\": \"test-definition-2\", \"parameter_values\": {\"p1\": \"ghi99\"}}", 201);

    // Check total user, definition, and instance counts
    url = baseUrl + "/status";
    JSONObject statusJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", statusJson != null);
    String since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("Status JSON", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":3,\"num_active_users\":3,\"num_registered_agents\":6,\"num_active_agents\":12}", statusJson.toString());

    // Check all agent definitions for all users
    url = baseUrl + "/agent_definitions?password=abracadabra";
    JSONObject definitionsJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", definitionsJson != null);
    assertEquals("Size of agent definitions JSON object", 1, definitionsJson.length());
    assertTrue("agent_definitions key is missing", definitionsJson.has("agent_definitions"));
    JSONArray definitionsArrayJson = definitionsJson.getJSONArray("agent_definitions");
    assertEquals("Count of agent definitions", 6, definitionsArrayJson.length());
    JSONObject definitionJson = definitionsArrayJson.getJSONObject(0);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[0] key is missing", definitionJson.has("user"));
    assertTrue("name[0] key is missing", definitionJson.has("name"));
    assertTrue("description[0] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[0] JSON", "{\"description\":\"Test definition #1\",\"name\":\"test-definition-1\",\"user\":\"test-user-1\"}", definitionJson.toString());
    assertEquals("user[0]", "test-user-1", definitionJson.getString("user"));
    assertEquals("name[0]", "test-definition-1", definitionJson.getString("name"));
    assertEquals("description[0]", "Test definition #1", definitionJson.getString("description"));
    definitionJson = definitionsArrayJson.getJSONObject(1);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[1] key is missing", definitionJson.has("user"));
    assertTrue("name[1] key is missing", definitionJson.has("name"));
    assertTrue("description[1] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #2\",\"name\":\"test-definition-2\",\"user\":\"test-user-1\"}", definitionJson.toString());
    assertEquals("user[1]", "test-user-1", definitionJson.getString("user"));
    assertEquals("name[1]", "test-definition-2", definitionJson.getString("name"));
    assertEquals("description[1]", "Test definition #2", definitionJson.getString("description"));
    definitionJson = definitionsArrayJson.getJSONObject(2);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[2] key is missing", definitionJson.has("user"));
    assertTrue("name[2] key is missing", definitionJson.has("name"));
    assertTrue("description[2] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #3\",\"name\":\"test-definition-1\",\"user\":\"test-user-2\"}", definitionJson.toString());
    assertEquals("user[2]", "test-user-2", definitionJson.getString("user"));
    assertEquals("name[2]", "test-definition-1", definitionJson.getString("name"));
    assertEquals("description[2]", "Test definition #3", definitionJson.getString("description"));
    definitionJson = definitionsArrayJson.getJSONObject(3);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[3] key is missing", definitionJson.has("user"));
    assertTrue("name[3] key is missing", definitionJson.has("name"));
    assertTrue("description[3] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #4\",\"name\":\"test-definition-2\",\"user\":\"test-user-2\"}", definitionJson.toString());
    assertEquals("user[3]", "test-user-2", definitionJson.getString("user"));
    assertEquals("name[3]", "test-definition-2", definitionJson.getString("name"));
    assertEquals("description[3]", "Test definition #4", definitionJson.getString("description"));
    definitionJson = definitionsArrayJson.getJSONObject(4);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[4] key is missing", definitionJson.has("user"));
    assertTrue("name[4] key is missing", definitionJson.has("name"));
    assertTrue("description[4] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #5\",\"name\":\"test-definition-1\",\"user\":\"test-user-3\"}", definitionJson.toString());
    assertEquals("user[4]", "test-user-3", definitionJson.getString("user"));
    assertEquals("name[4]", "test-definition-1", definitionJson.getString("name"));
    assertEquals("description[4]", "Test definition #5", definitionJson.getString("description"));
    definitionJson = definitionsArrayJson.getJSONObject(5);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[5] key is missing", definitionJson.has("user"));
    assertTrue("name[5] key is missing", definitionJson.has("name"));
    assertTrue("description[5] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #6\",\"name\":\"test-definition-2\",\"user\":\"test-user-3\"}", definitionJson.toString());
    assertEquals("user[5]", "test-user-3", definitionJson.getString("user"));
    assertEquals("name[5]", "test-definition-2", definitionJson.getString("name"));
    assertEquals("description[5]", "Test definition #6", definitionJson.getString("description"));

    // Check agent definitions for one user at a time
    url = baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1";
    definitionsJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", definitionsJson != null);
    assertEquals("Size of agent definitions JSON object", 1, definitionsJson.length());
    assertTrue("agent_definitions key is missing", definitionsJson.has("agent_definitions"));
    definitionsArrayJson = definitionsJson.getJSONArray("agent_definitions");
    assertEquals("Count of agent definitions", 2, definitionsArrayJson.length());
    definitionJson = definitionsArrayJson.getJSONObject(0);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[0] key is missing", definitionJson.has("user"));
    assertTrue("name[0] key is missing", definitionJson.has("name"));
    assertTrue("description[0] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[0] JSON", "{\"description\":\"Test definition #1\",\"name\":\"test-definition-1\",\"user\":\"test-user-1\"}", definitionJson.toString());
    assertEquals("user[0]", "test-user-1", definitionJson.getString("user"));
    assertEquals("name[0]", "test-definition-1", definitionJson.getString("name"));
    assertEquals("description[0]", "Test definition #1", definitionJson.getString("description"));
    definitionJson = definitionsArrayJson.getJSONObject(1);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[1] key is missing", definitionJson.has("user"));
    assertTrue("name[1] key is missing", definitionJson.has("name"));
    assertTrue("description[1] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #2\",\"name\":\"test-definition-2\",\"user\":\"test-user-1\"}", definitionJson.toString());
    assertEquals("user[1]", "test-user-1", definitionJson.getString("user"));
    assertEquals("name[1]", "test-definition-2", definitionJson.getString("name"));
    assertEquals("description[1]", "Test definition #2", definitionJson.getString("description"));

    url = baseUrl + "/users/test-user-2/agent_definitions?password=test-pwd-2";
    definitionsJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", definitionsJson != null);
    assertEquals("Size of agent definitions JSON object", 1, definitionsJson.length());
    assertTrue("agent_definitions key is missing", definitionsJson.has("agent_definitions"));
    definitionsArrayJson = definitionsJson.getJSONArray("agent_definitions");
    assertEquals("Count of agent definitions", 2, definitionsArrayJson.length());
    definitionJson = definitionsArrayJson.getJSONObject(0);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[0] key is missing", definitionJson.has("user"));
    assertTrue("name[0] key is missing", definitionJson.has("name"));
    assertTrue("description[0] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #3\",\"name\":\"test-definition-1\",\"user\":\"test-user-2\"}", definitionJson.toString());
    assertEquals("user[0]", "test-user-2", definitionJson.getString("user"));
    assertEquals("name[0]", "test-definition-1", definitionJson.getString("name"));
    assertEquals("description[0]", "Test definition #3", definitionJson.getString("description"));
    definitionJson = definitionsArrayJson.getJSONObject(1);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[1] key is missing", definitionJson.has("user"));
    assertTrue("name[1] key is missing", definitionJson.has("name"));
    assertTrue("description[1] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #4\",\"name\":\"test-definition-2\",\"user\":\"test-user-2\"}", definitionJson.toString());
    assertEquals("user[1]", "test-user-2", definitionJson.getString("user"));
    assertEquals("name[1]", "test-definition-2", definitionJson.getString("name"));
    assertEquals("description[1]", "Test definition #4", definitionJson.getString("description"));

    url = baseUrl + "/users/test-user-3/agent_definitions?password=test-pwd-3";
    definitionsJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", definitionsJson != null);
    assertEquals("Size of agent definitions JSON object", 1, definitionsJson.length());
    assertTrue("agent_definitions key is missing", definitionsJson.has("agent_definitions"));
    definitionsArrayJson = definitionsJson.getJSONArray("agent_definitions");
    assertEquals("Count of agent definitions", 2, definitionsArrayJson.length());
    definitionJson = definitionsArrayJson.getJSONObject(0);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[0] key is missing", definitionJson.has("user"));
    assertTrue("name[0] key is missing", definitionJson.has("name"));
    assertTrue("description[0] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #5\",\"name\":\"test-definition-1\",\"user\":\"test-user-3\"}", definitionJson.toString());
    assertEquals("user[0]", "test-user-3", definitionJson.getString("user"));
    assertEquals("name[0]", "test-definition-1", definitionJson.getString("name"));
    assertEquals("description[0]", "Test definition #5", definitionJson.getString("description"));
    definitionJson = definitionsArrayJson.getJSONObject(1);
    assertEquals("Count of keys for agent definition", 3, definitionJson.length());
    assertTrue("user[1] key is missing", definitionJson.has("user"));
    assertTrue("name[1] key is missing", definitionJson.has("name"));
    assertTrue("description[1] key is missing", definitionJson.has("description"));
    assertEquals("Agent definition[1] JSON", "{\"description\":\"Test definition #6\",\"name\":\"test-definition-2\",\"user\":\"test-user-3\"}", definitionJson.toString());
    assertEquals("user[1]", "test-user-3", definitionJson.getString("user"));
    assertEquals("name[1]", "test-definition-2", definitionJson.getString("name"));
    assertEquals("description[1]", "Test definition #6", definitionJson.getString("description"));

    // Check all agent instances for all user
    url = baseUrl + "/agents?password=abracadabra";
    JSONObject instancesJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", instancesJson != null);
    assertEquals("Size of agent instances JSON object", 1, instancesJson.length());
    assertTrue("agent_instances key is missing", instancesJson.has("agent_instances"));
    JSONArray instancesArrayJson = instancesJson.getJSONArray("agent_instances");
    assertEquals("Count of agent instances", 12, instancesArrayJson.length());
    JSONObject instanceJson = instancesArrayJson.getJSONObject(0);
    assertEquals("Count of keys for agent instance", 4, instanceJson.length());
    assertTrue("user[0] key is missing", instanceJson.has("user"));
    assertTrue("name[0] key is missing", instanceJson.has("name"));
    assertTrue("definition[0] key is missing", instanceJson.has("name"));
    assertTrue("description[0] key is missing", instanceJson.has("definition"));
    assertEquals("Agent instance[0] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #1\",\"name\":\"test-instance-1\",\"user\":\"test-user-1\"}", instanceJson.toString());
    assertEquals("user[0]", "test-user-1", instanceJson.getString("user"));
    assertEquals("name[0]", "test-instance-1", instanceJson.getString("name"));
    assertEquals("definition[0]", "test-definition-1", instanceJson.getString("definition"));
    assertEquals("description[0]", "Test instance #1", instanceJson.getString("description"));
    assertEquals("Agent instance[1] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #2\",\"name\":\"test-instance-2\",\"user\":\"test-user-1\"}", instancesArrayJson.getJSONObject(1).toString());
    assertEquals("Agent instance[2] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #3\",\"name\":\"test-instance-3\",\"user\":\"test-user-1\"}", instancesArrayJson.getJSONObject(2).toString());
    assertEquals("Agent instance[3] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #4\",\"name\":\"test-instance-4\",\"user\":\"test-user-1\"}", instancesArrayJson.getJSONObject(3).toString());
    assertEquals("Agent instance[4] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #5\",\"name\":\"test-instance-5\",\"user\":\"test-user-2\"}", instancesArrayJson.getJSONObject(4).toString());
    assertEquals("Agent instance[5] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #6\",\"name\":\"test-instance-6\",\"user\":\"test-user-2\"}", instancesArrayJson.getJSONObject(5).toString());
    assertEquals("Agent instance[6] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #7\",\"name\":\"test-instance-7\",\"user\":\"test-user-2\"}", instancesArrayJson.getJSONObject(6).toString());
    assertEquals("Agent instance[7] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #8\",\"name\":\"test-instance-8\",\"user\":\"test-user-2\"}", instancesArrayJson.getJSONObject(7).toString());
    assertEquals("Agent instance[8] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #9\",\"name\":\"test-instance-9\",\"user\":\"test-user-3\"}", instancesArrayJson.getJSONObject(8).toString());
    assertEquals("Agent instance[9] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #10\",\"name\":\"test-instance-10\",\"user\":\"test-user-3\"}", instancesArrayJson.getJSONObject(9).toString());
    assertEquals("Agent instance[10] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #11\",\"name\":\"test-instance-11\",\"user\":\"test-user-3\"}", instancesArrayJson.getJSONObject(10).toString());
    assertEquals("Agent instance[11] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #12\",\"name\":\"test-instance-12\",\"user\":\"test-user-3\"}", instancesArrayJson.getJSONObject(11).toString());
    
    // Check agent instances one user at a time
    url = baseUrl + "/users/test-user-1/agents?password=test-pwd-1";
    instancesJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", instancesJson != null);
    assertEquals("Size of agent instances JSON object", 1, instancesJson.length());
    assertTrue("agent_instances key is missing", instancesJson.has("agent_instances"));
    instancesArrayJson = instancesJson.getJSONArray("agent_instances");
    assertEquals("Count of agent instances", 4, instancesArrayJson.length());
    instanceJson = instancesArrayJson.getJSONObject(0);
    assertEquals("Count of keys for agent instance", 4, instanceJson.length());
    assertTrue("user[0] key is missing", instanceJson.has("user"));
    assertTrue("name[0] key is missing", instanceJson.has("name"));
    assertTrue("definition[0] key is missing", instanceJson.has("name"));
    assertTrue("description[0] key is missing", instanceJson.has("definition"));
    assertEquals("Agent instance[0] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #1\",\"name\":\"test-instance-1\",\"user\":\"test-user-1\"}", instanceJson.toString());
    assertEquals("user[0]", "test-user-1", instanceJson.getString("user"));
    assertEquals("name[0]", "test-instance-1", instanceJson.getString("name"));
    assertEquals("definition[0]", "test-definition-1", instanceJson.getString("definition"));
    assertEquals("description[0]", "Test instance #1", instanceJson.getString("description"));
    assertEquals("Agent instance[1] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #2\",\"name\":\"test-instance-2\",\"user\":\"test-user-1\"}", instancesArrayJson.getJSONObject(1).toString());
    assertEquals("Agent instance[2] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #3\",\"name\":\"test-instance-3\",\"user\":\"test-user-1\"}", instancesArrayJson.getJSONObject(2).toString());
    assertEquals("Agent instance[3] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #4\",\"name\":\"test-instance-4\",\"user\":\"test-user-1\"}", instancesArrayJson.getJSONObject(3).toString());

    url = baseUrl + "/users/test-user-2/agents?password=test-pwd-2";
    instancesJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", instancesJson != null);
    assertEquals("Size of agent instances JSON object", 1, instancesJson.length());
    assertTrue("agent_instances key is missing", instancesJson.has("agent_instances"));
    instancesArrayJson = instancesJson.getJSONArray("agent_instances");
    assertEquals("Count of agent instances", 4, instancesArrayJson.length());
    instanceJson = instancesArrayJson.getJSONObject(0);
    assertEquals("Count of keys for agent instance", 4, instanceJson.length());
    assertEquals("Agent instance[4] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #5\",\"name\":\"test-instance-5\",\"user\":\"test-user-2\"}", instancesArrayJson.getJSONObject(0).toString());
    assertEquals("Agent instance[5] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #6\",\"name\":\"test-instance-6\",\"user\":\"test-user-2\"}", instancesArrayJson.getJSONObject(1).toString());
    assertEquals("Agent instance[6] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #7\",\"name\":\"test-instance-7\",\"user\":\"test-user-2\"}", instancesArrayJson.getJSONObject(2).toString());
    assertEquals("Agent instance[7] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #8\",\"name\":\"test-instance-8\",\"user\":\"test-user-2\"}", instancesArrayJson.getJSONObject(3).toString());

    url = baseUrl + "/users/test-user-3/agents?password=test-pwd-3";
    instancesJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", instancesJson != null);
    assertEquals("Size of agent instances JSON object", 1, instancesJson.length());
    assertTrue("agent_instances key is missing", instancesJson.has("agent_instances"));
    instancesArrayJson = instancesJson.getJSONArray("agent_instances");
    assertEquals("Count of agent instances", 4, instancesArrayJson.length());
    instanceJson = instancesArrayJson.getJSONObject(0);
    assertEquals("Count of keys for agent instance", 4, instanceJson.length());
    assertEquals("Agent instance[8] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #9\",\"name\":\"test-instance-9\",\"user\":\"test-user-3\"}", instancesArrayJson.getJSONObject(0).toString());
    assertEquals("Agent instance[9] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #10\",\"name\":\"test-instance-10\",\"user\":\"test-user-3\"}", instancesArrayJson.getJSONObject(1).toString());
    assertEquals("Agent instance[10] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #11\",\"name\":\"test-instance-11\",\"user\":\"test-user-3\"}", instancesArrayJson.getJSONObject(2).toString());
    assertEquals("Agent instance[11] JSON", "{\"definition\":\"test-definition-2\",\"description\":\"Test instance #12\",\"name\":\"test-instance-12\",\"user\":\"test-user-3\"}", instancesArrayJson.getJSONObject(3).toString());

    // Check detail for all instances
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/test-instance-1?password=test-pwd-1", 200);
    assertTrue("Response entity is missing", statusJson != null);
    int countExpectedInstanceKeys = 16;
    assertEquals("Count of keys for agent instance status", countExpectedInstanceKeys, statusJson.length());
    assertTrue("user key is missing", statusJson.has("user"));
    assertTrue("name key is missing", statusJson.has("name"));
    assertTrue("instantiated key is missing", statusJson.has("instantiated"));
    assertTrue("updated key is missing", statusJson.has("updated"));
    assertTrue("definition key is missing", statusJson.has("definition"));
    assertTrue("description key is missing", statusJson.has("description"));
    assertTrue("trigger_interval key is missing", statusJson.has("trigger_interval"));
    assertTrue("reporting_interval key is missing", statusJson.has("reporting_interval"));
    assertTrue("public_output key is missing", statusJson.has("public_output"));
    assertTrue("limit_instance_states_stored key is missing", statusJson.has("limit_instance_states_stored"));
    assertTrue("inputs_changed key is missing", statusJson.has("inputs_changed"));
    assertTrue("triggered key is missing", statusJson.has("triggered"));
    assertTrue("outputs_changed key is missing", statusJson.has("outputs_changed"));
    assertTrue("status key is missing", statusJson.has("status"));
    assertTrue("enabled key is missing", statusJson.has("enabled"));
    assertTrue("parameter_values key is missing", statusJson.has("parameter_values"));

    assertEquals("user", "test-user-1", statusJson.getString("user"));
    assertEquals("name", "test-instance-1", statusJson.getString("name"));
    AgentInstance agInst = server.agentServer.agentInstances.get("test-user-1").get("test-instance-1");
    long createdTime = agInst.timeInstantiated;
    String created = DateUtils.toRfcString(createdTime);
    assertEquals("instantiated", created, statusJson.getString("instantiated"));
    long modifiedTime = agInst.timeUpdated;
    String modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    assertEquals("updated", modified, statusJson.getString("updated"));
    assertEquals("definition", "test-definition-1", statusJson.getString("definition"));
    assertEquals("description", "Test instance #1", statusJson.getString("description"));
    int outputHistorySize = agInst.outputHistory.size();
    long outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    String outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #1", "{\"user\":\"test-user-1\",\"name\":\"test-instance-1\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #1\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":123}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/test-instance-2?password=test-pwd-1", 200);
    agInst = server.agentServer.agentInstances.get("test-user-1").get("test-instance-2");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #2", "{\"user\":\"test-user-1\",\"name\":\"test-instance-2\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #2\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":12399}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/test-instance-3?password=test-pwd-1", 200);
    agInst = server.agentServer.agentInstances.get("test-user-1").get("test-instance-3");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #3", "{\"user\":\"test-user-1\",\"name\":\"test-instance-3\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #3\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":\"abc\"}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/test-instance-4?password=test-pwd-1", 200);
    agInst = server.agentServer.agentInstances.get("test-user-1").get("test-instance-4");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #4", "{\"user\":\"test-user-1\",\"name\":\"test-instance-4\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #4\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":\"abc99\"}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-2/agents/test-instance-5?password=test-pwd-2", 200);
    agInst = server.agentServer.agentInstances.get("test-user-2").get("test-instance-5");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #5", "{\"user\":\"test-user-2\",\"name\":\"test-instance-5\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #5\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":456}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-2/agents/test-instance-6?password=test-pwd-2", 200);
    agInst = server.agentServer.agentInstances.get("test-user-2").get("test-instance-6");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #6", "{\"user\":\"test-user-2\",\"name\":\"test-instance-6\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #6\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":45699}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-2/agents/test-instance-7?password=test-pwd-2", 200);
    agInst = server.agentServer.agentInstances.get("test-user-2").get("test-instance-7");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #7", "{\"user\":\"test-user-2\",\"name\":\"test-instance-7\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #7\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":\"def\"}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-2/agents/test-instance-8?password=test-pwd-2", 200);
    agInst = server.agentServer.agentInstances.get("test-user-2").get("test-instance-8");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #8", "{\"user\":\"test-user-2\",\"name\":\"test-instance-8\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #8\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":\"def99\"}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-9?password=test-pwd-3", 200);
    agInst = server.agentServer.agentInstances.get("test-user-3").get("test-instance-9");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #9", "{\"user\":\"test-user-3\",\"name\":\"test-instance-9\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #9\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":789}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-10?password=test-pwd-3", 200);
    agInst = server.agentServer.agentInstances.get("test-user-3").get("test-instance-10");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #10", "{\"user\":\"test-user-3\",\"name\":\"test-instance-10\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #10\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":78999}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-11?password=test-pwd-3", 200);
    agInst = server.agentServer.agentInstances.get("test-user-3").get("test-instance-11");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #11", "{\"user\":\"test-user-3\",\"name\":\"test-instance-11\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #11\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":\"ghi\"}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-12?password=test-pwd-3", 200);
    agInst = server.agentServer.agentInstances.get("test-user-3").get("test-instance-12");
    createdTime = agInst.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = agInst.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = agInst.outputHistory.size();
    outputsChangedTime = agInst.outputHistory.size() > 0 ? agInst.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #12", "{\"user\":\"test-user-3\",\"name\":\"test-instance-12\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #12\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\",\"trigger_interval\":\"50\",\"reporting_interval\":\"200\",\"public_output\":false,\"limit_instance_states_stored\":25,\"enabled\":true,\"parameter_values\":{\"p1\":\"ghi99\"}, \"status\": \"active\", \"inputs_changed\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"triggered\": \"\"}", statusJson.toString());

    // Check status of all instances
    url = baseUrl + "/users/test-user-1/agents/test-instance-1/status?password=test-pwd-1";
    statusJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", statusJson != null);
    int countExpectedInstanceStatusKeys = 16;
    assertEquals("Count of keys for agent instance status", countExpectedInstanceStatusKeys, statusJson.length());
    assertTrue("user key is missing", statusJson.has("user"));
    assertTrue("name key is missing", statusJson.has("name"));
    assertTrue("instantiated key is missing", statusJson.has("instantiated"));
    assertTrue("inputs_changed key is missing", statusJson.has("inputs_changed"));
    assertTrue("triggered key is missing", statusJson.has("triggered"));
    assertTrue("outputs_changed key is missing", statusJson.has("outputs_changed"));
    assertTrue("status key is missing", statusJson.has("status"));
    assertTrue("updated key is missing", statusJson.has("updated"));
    assertTrue("definition key is missing", statusJson.has("definition"));
    assertTrue("description key is missing", statusJson.has("description"));
    assertTrue("trigger_interval key is missing", statusJson.has("trigger_interval"));
    assertTrue("reporting_interval key is missing", statusJson.has("reporting_interval"));
    assertTrue("public_output key is missing", statusJson.has("public_output"));
    assertTrue("limit_instance_states_stored key is missing", statusJson.has("limit_instance_states_stored"));
    assertTrue("enabled key is missing", statusJson.has("enabled"));
    assertTrue("parameter_values key is missing", statusJson.has("parameter_values"));

    assertEquals("user", "test-user-1", statusJson.getString("user"));
    assertEquals("name", "test-instance-1", statusJson.getString("name"));
    assertEquals("status", "active", statusJson.getString("status"));
    AgentInstance instance = server.agentServer.agentInstances.get("test-user-1").get("test-instance-1");
    long instantiatedTime = instance.timeInstantiated;
    String instantiated = DateUtils.toRfcString(instantiatedTime);
    assertEquals("instantiated", instantiated, statusJson.getString("instantiated"));
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    long updatedTime = instance.timeUpdated;
    String updated = updatedTime > 0 ? DateUtils.toRfcString(updatedTime) : "";
    assertEquals("updated", updated, statusJson.getString("updated"));
    long inputsChangedTime = instance.lastInputsChanged;
    String inputsChanged = inputsChangedTime > 0 ? DateUtils.toRfcString(inputsChangedTime) : "";
    assertEquals("inputs_changed", inputsChanged, statusJson.getString("inputs_changed"));
    long triggeredTime = instance.lastTriggered;
    String triggered = triggeredTime > 0 ? DateUtils.toRfcString(triggeredTime) : "";
    assertEquals("triggered", triggered, statusJson.getString("triggered"));
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertEquals("outputs_changed", outputsChanged, statusJson.getString("outputs_changed"));
    assertEquals("definition", "test-definition-1", statusJson.getString("definition"));
    assertEquals("description", "Test instance #1", statusJson.getString("description"));
    assertJsonSourceEquals("Agent instance JSON #1", "{\"user\":\"test-user-1\",\"name\":\"test-instance-1\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #1\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": 123}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/test-instance-2/status?password=test-pwd-1", 200);
    instance = server.agentServer.agentInstances.get("test-user-1").get("test-instance-2");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-1").get("test-instance-2").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #2", "{\"user\":\"test-user-1\",\"name\":\"test-instance-2\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #2\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": 12399}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/test-instance-3/status?password=test-pwd-1", 200);
    instance = server.agentServer.agentInstances.get("test-user-1").get("test-instance-3");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-1").get("test-instance-3").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #3", "{\"user\":\"test-user-1\",\"name\":\"test-instance-3\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #3\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"abc\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/test-instance-4/status?password=test-pwd-1", 200);
    instance = server.agentServer.agentInstances.get("test-user-1").get("test-instance-4");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-1").get("test-instance-4").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #4", "{\"user\":\"test-user-1\",\"name\":\"test-instance-4\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #4\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"abc99\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-2/agents/test-instance-5/status?password=test-pwd-2", 200);
    instance = server.agentServer.agentInstances.get("test-user-2").get("test-instance-5");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-2").get("test-instance-5").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #5", "{\"user\":\"test-user-2\",\"name\":\"test-instance-5\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #5\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": 456}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-2/agents/test-instance-6/status?password=test-pwd-2", 200);
    instance = server.agentServer.agentInstances.get("test-user-2").get("test-instance-6");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-2").get("test-instance-6").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #6", "{\"user\":\"test-user-2\",\"name\":\"test-instance-6\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #6\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": 45699}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-2/agents/test-instance-7/status?password=test-pwd-2", 200);
    instance = server.agentServer.agentInstances.get("test-user-2").get("test-instance-7");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-2").get("test-instance-7").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #7", "{\"user\":\"test-user-2\",\"name\":\"test-instance-7\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #7\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"def\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-2/agents/test-instance-8/status?password=test-pwd-2", 200);
    instance = server.agentServer.agentInstances.get("test-user-2").get("test-instance-8");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-2").get("test-instance-8").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #8", "{\"user\":\"test-user-2\",\"name\":\"test-instance-8\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #8\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"def99\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-9/status?password=test-pwd-3", 200);
    instance = server.agentServer.agentInstances.get("test-user-3").get("test-instance-9");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-3").get("test-instance-9").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #9", "{\"user\":\"test-user-3\",\"name\":\"test-instance-9\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #9\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": 789}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-10/status?password=test-pwd-3", 200);
    instance = server.agentServer.agentInstances.get("test-user-3").get("test-instance-10");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-3").get("test-instance-10").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #10", "{\"user\":\"test-user-3\",\"name\":\"test-instance-10\",\"definition\":\"test-definition-1\",\"description\":\"Test instance #10\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": 78999}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-11/status?password=test-pwd-3", 200);
    instance = server.agentServer.agentInstances.get("test-user-3").get("test-instance-11");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-3").get("test-instance-11").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #11", "{\"user\":\"test-user-3\",\"name\":\"test-instance-11\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #11\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"ghi\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-12/status?password=test-pwd-3", 200);
    instance = server.agentServer.agentInstances.get("test-user-3").get("test-instance-12");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-3").get("test-instance-12").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON #12", "{\"user\":\"test-user-3\",\"name\":\"test-instance-12\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #12\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"ghi99\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    // Test update of instance info
    // First an empty change that changes nothing - should not change modified time stamp
    // Sleep a little so that modification time change (in ms)
    long prevModifiedTime = modifiedTime;
    long prevCreatedTime = createdTime;
    Thread.sleep(1);
    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-12?password=test-pwd-3", 200);
    instance = server.agentServer.agentInstances.get("test-user-3").get("test-instance-12");
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = instance.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    assertEquals("Creation time", prevCreatedTime, createdTime);
    assertEquals("Modified time", prevModifiedTime, modifiedTime);
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON", "{\"user\":\"test-user-3\",\"name\":\"test-instance-12\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #12\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\",\"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"ghi99\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());
    statusJson = doPutJson(baseUrl + "/users/test-user-3/agents/test-instance-12?password=test-pwd-3",
        "{}", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-12/status?password=test-pwd-3", 200);
    createdTime = instance.timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = instance.timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    assertEquals("Creation time", prevCreatedTime, createdTime);
    assertTrue("Modified time is unchanged", prevModifiedTime != modifiedTime);
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON", "{\"user\":\"test-user-3\",\"name\":\"test-instance-12\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #12\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"ghi99\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());
    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-12?password=test-pwd-3", 200);
    assertJsonSourceEquals("Agent instance JSON", "{\"user\":\"test-user-3\",\"name\":\"test-instance-12\",\"definition\":\"test-definition-2\",\"description\":\"Test instance #12\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"ghi99\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    prevModifiedTime = modifiedTime;
    prevCreatedTime = createdTime;
    Thread.sleep(1);
    statusJson = doPutJson(baseUrl + "/users/test-user-3/agents/test-instance-12?password=test-pwd-3",
        "{\"description\": \"Revised description\"}", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-12/status?password=test-pwd-3", 200);
    createdTime = server.agentServer.agentInstances.get("test-user-3").get("test-instance-12").timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-3").get("test-instance-12").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    assertEquals("Creation time", prevCreatedTime, createdTime);
    assertTrue("Modified time is unchanged", prevModifiedTime != modifiedTime);
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON", "{\"user\":\"test-user-3\",\"name\":\"test-instance-12\",\"definition\":\"test-definition-2\",\"description\":\"Revised description\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"ghi99\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());
    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-12?password=test-pwd-3", 200);
    assertJsonSourceEquals("Agent instance JSON", "{\"user\":\"test-user-3\",\"name\":\"test-instance-12\",\"definition\":\"test-definition-2\",\"description\":\"Revised description\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"ghi99\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"200\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    Thread.sleep(1);
    statusJson = doPutJson(baseUrl + "/users/test-user-3/agents/test-instance-12?password=test-pwd-3",
        "{\"reporting_interval\": 350}", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doGetJson(baseUrl + "/users/test-user-3/agents/test-instance-12?password=test-pwd-3", 200);
    createdTime = server.agentServer.agentInstances.get("test-user-3").get("test-instance-12").timeInstantiated;
    created = DateUtils.toRfcString(createdTime);
    modifiedTime = server.agentServer.agentInstances.get("test-user-3").get("test-instance-12").timeUpdated;
    modified = modifiedTime > 0 ? DateUtils.toRfcString(modifiedTime) : "";
    assertEquals("Creation time", prevCreatedTime, createdTime);
    assertTrue("Modified time is unchanged", prevModifiedTime != modifiedTime);
    outputHistorySize = instance.outputHistory.size();
    outputsChangedTime = outputHistorySize > 0 ? instance.outputHistory.get(outputHistorySize - 1).time : 0;
    outputsChanged = outputsChangedTime > 0 ? DateUtils.toRfcString(outputsChangedTime) : "";
    assertJsonSourceEquals("Agent instance JSON", "{\"user\":\"test-user-3\",\"name\":\"test-instance-12\",\"definition\":\"test-definition-2\",\"description\":\"Revised description\",\"instantiated\":\"" + created + "\",\"updated\":\"" + modified + "\", \"status\": \"active\", \"inputs_changed\": \"\", \"triggered\": \"\", \"outputs_changed\": \"" + outputsChanged + "\", \"public_output\": false, \"enabled\": true, \"parameter_values\": {\"p1\": \"ghi99\"}, \"trigger_interval\": \"50\", \"reporting_interval\": \"350\", \"limit_instance_states_stored\": 25}", statusJson.toString());

    // Test remove of instance - one from each user
    statusJson = doDeleteJson(baseUrl + "/users/test-user-1/agents/test-instance-2?password=test-pwd-1", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doDeleteJson(baseUrl + "/users/test-user-2/agents/test-instance-6?password=test-pwd-2", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doDeleteJson(baseUrl + "/users/test-user-3/agents/test-instance-10?password=test-pwd-3", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    url = baseUrl + "/agents?password=abracadabra";
    instancesJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", instancesJson != null);
    instancesArrayJson = instancesJson.getJSONArray("agent_instances");
    assertEquals("Count of agent instances", 9, instancesArrayJson.length());


    // Check total user, definition, and instance counts
    url = baseUrl + "/status";
    statusJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", statusJson != null);
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("Status JSON", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":3,\"num_active_users\":3,\"num_registered_agents\":6,\"num_active_agents\":9}", statusJson.toString());

    // Test remove of two more instances from each user
    statusJson = doDeleteJson(baseUrl + "/users/test-user-1/agents/test-instance-3?password=test-pwd-1", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doDeleteJson(baseUrl + "/users/test-user-1/agents/test-instance-4?password=test-pwd-1", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doDeleteJson(baseUrl + "/users/test-user-2/agents/test-instance-7?password=test-pwd-2", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doDeleteJson(baseUrl + "/users/test-user-2/agents/test-instance-8?password=test-pwd-2", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doDeleteJson(baseUrl + "/users/test-user-3/agents/test-instance-11?password=test-pwd-3", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doDeleteJson(baseUrl + "/users/test-user-3/agents/test-instance-12?password=test-pwd-3", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    url = baseUrl + "/agents?password=abracadabra";
    instancesJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", instancesJson != null);
    instancesArrayJson = instancesJson.getJSONArray("agent_instances");
    assertEquals("Count of agent instances", 3, instancesArrayJson.length());

    // Check total user, definition, and instance counts
    url = baseUrl + "/status";
    statusJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", statusJson != null);
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("Status JSON", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":3,\"num_active_users\":3,\"num_registered_agents\":6,\"num_active_agents\":3}", statusJson.toString());

    // Check actual remaining instance names
    url = baseUrl + "/agents?password=abracadabra";
    instancesJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", instancesJson != null);
    assertEquals("Size of agent instances JSON object", 1, instancesJson.length());
    assertTrue("agent_instances key is missing", instancesJson.has("agent_instances"));
    instancesArrayJson = instancesJson.getJSONArray("agent_instances");
    assertEquals("Count of agent instances", 3, instancesArrayJson.length());
    instanceJson = instancesArrayJson.getJSONObject(0);
    assertEquals("Count of keys for agent instance", 4, instanceJson.length());
    assertTrue("user[0] key is missing", instanceJson.has("user"));
    assertTrue("name[0] key is missing", instanceJson.has("name"));
    assertTrue("definition[0] key is missing", instanceJson.has("name"));
    assertTrue("description[0] key is missing", instanceJson.has("definition"));
    assertEquals("Agent instance[0] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #1\",\"name\":\"test-instance-1\",\"user\":\"test-user-1\"}", instanceJson.toString());
    assertEquals("user[0]", "test-user-1", instanceJson.getString("user"));
    assertEquals("name[0]", "test-instance-1", instanceJson.getString("name"));
    assertEquals("definition[0]", "test-definition-1", instanceJson.getString("definition"));
    assertEquals("description[0]", "Test instance #1", instanceJson.getString("description"));
    assertEquals("Agent instance[1] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #5\",\"name\":\"test-instance-5\",\"user\":\"test-user-2\"}", instancesArrayJson.getJSONObject(1).toString());
    assertEquals("Agent instance[2] JSON", "{\"definition\":\"test-definition-1\",\"description\":\"Test instance #9\",\"name\":\"test-instance-9\",\"user\":\"test-user-3\"}", instancesArrayJson.getJSONObject(2).toString());

    // Delete remaining instances
    statusJson = doDeleteJson(baseUrl + "/users/test-user-1/agents/test-instance-1?password=test-pwd-1", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doDeleteJson(baseUrl + "/users/test-user-2/agents/test-instance-5?password=test-pwd-2", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    statusJson = doDeleteJson(baseUrl + "/users/test-user-3/agents/test-instance-9?password=test-pwd-3", 204);
    assertTrue("Unexpected return JSON", statusJson == null);
    url = baseUrl + "/agents?password=abracadabra";
    instancesJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", instancesJson != null);
    instancesArrayJson = instancesJson.getJSONArray("agent_instances");
    assertEquals("Count of agent instances", 0, instancesArrayJson.length());
    
    // Check total user, definition, and instance counts
    url = baseUrl + "/status";
    statusJson = doGetJson(url, 200);
    assertTrue("Response entity is missing", statusJson != null);
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("Status JSON", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":3,\"num_active_users\":0,\"num_registered_agents\":6,\"num_active_agents\":0}", statusJson.toString());

  }

  @Test
  public void testHelloWorld() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create a test user
    doPostJson(baseUrl + "/users?id=test-user-1&password=test-pwd-1", "{}", 201);

    // Create one agent definition that says "Hello World" in its output
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}]}", 201);

    // Instantiate the agent definition once
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"definition\": \"HelloWorld\"}", 201);

    // Get the agent instance's output as JSON
    JSONObject outputJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1", 200);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 1, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertEquals("Output JSON", "{\"field1\":\"Hello World\"}", outputJson.toString());

    // Get the agent instance's output as XML
    String outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.xml?password=test-pwd-1", 200);
    assertTrue("Output XML is missing", outputString != null);
    assertEquals("Output XML", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><field1>Hello World</field1>", outputString.trim());

    // Get the agent instance's output as plain text
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.text?password=test-pwd-1", 200);
    assertTrue("Output XML is missing", outputString != null);
    assertEquals("Output plain text", "Hello World", outputString.trim());

    // Get the agent instance's output as CSV
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.csv?password=test-pwd-1", 200);
    assertTrue("Output XML is missing", outputString != null);
    assertEquals("Output plain text", "Hello World", outputString.trim());

    // Get the agent instance's output as tab-delimited
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.csv?password=test-pwd-1", 200);
    assertTrue("Output XML is missing", outputString != null);
    assertEquals("Output plain text", "Hello World", outputString.trim());

  }

  @Test
  public void testOutputFormats() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create a test user
    doPostJson(baseUrl + "/users?id=test-user-1&password=test-pwd-1", "{}", 201);

    // Create one agent definition that says "Hello World" in its output
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}, {\"name\": \"field2\", \"type\": \"int\", \"default_value\": 123}, {\"name\": \"field3\", \"type\": \"float\", \"default_value\": 4.56}, {\"name\": \"field4\", \"type\": \"boolean\", \"default_value\": true}]}", 201);

    // Instantiate the agent definition once
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"definition\": \"HelloWorld\"}", 201);

    // Get the agent instance's output as JSON
    JSONObject outputJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1", 200);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 4, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertTrue("Output field1 is not of type String: " + outputJson.get("field1").getClass().getSimpleName(), outputJson.get("field1") instanceof String);
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertTrue("Output field2 is not of type Long: " + outputJson.get("field2").getClass().getSimpleName(), outputJson.get("field2") instanceof Integer);
    assertEquals("Output field2", 123, outputJson.getInt("field2"));
    assertTrue("Output field3 is missing", outputJson.has("field3"));
    assertTrue("Output field1 is not of type Double: " + outputJson.get("field3").getClass().getSimpleName(), outputJson.get("field3") instanceof Double);
    assertEquals("Output field3", 4.56, outputJson.getDouble("field3"), 0.0003);
    assertTrue("Output field4 is missing", outputJson.has("field4"));
    assertTrue("Output field4 is not of type Boolean: " + outputJson.get("field4").getClass().getSimpleName(), outputJson.get("field4") instanceof Boolean);
    assertEquals("Output field4", true, outputJson.getBoolean("field4"));
    assertEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":123,\"field3\":4.56,\"field4\":true}", outputJson.toString());

    // Same with explicit .json
    outputJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld/output.json?password=test-pwd-1", 200);
    assertEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":123,\"field3\":4.56,\"field4\":true}", outputJson.toString());

    // Same with explicit &format=json
    outputJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1&format=json", 200);
    assertEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":123,\"field3\":4.56,\"field4\":true}", outputJson.toString());

    // Get the agent instance's output as XML
    String outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.xml?password=test-pwd-1", 200);
    assertTrue("Output XML is missing", outputString != null);
    assertEquals("Output XML", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><field1>Hello World</field1><field2>123</field2><field3>4.56</field3><field4>true</field4>", outputString.trim());

    // Same with &format=xml
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1&format=xml", 200);
    assertTrue("Output XML is missing", outputString != null);
    assertEquals("Output XML", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><field1>Hello World</field1><field2>123</field2><field3>4.56</field3><field4>true</field4>", outputString.trim());
    
    // Get the agent instance's output as plain text
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.text?password=test-pwd-1", 200);
    assertTrue("Output text is missing", outputString != null);
    assertEquals("Output plain text", "Hello World 123 4.56 true", outputString.trim());

    // Same with &format=text
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1&format=text", 200);
    assertTrue("Output text is missing", outputString != null);
    assertEquals("Output plain text", "Hello World 123 4.56 true", outputString.trim());

    // Same with &format=txt
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1&format=txt", 200);
    assertTrue("Output text is missing", outputString != null);
    // TODO: Not okay for output fields to be shuffled like this!
    assertEquals("Output plain text", "Hello World 123 4.56 true", outputString.trim());
    
    // Get the agent instance's output as CSV
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.csv?password=test-pwd-1", 200);
    assertTrue("Output text is missing", outputString != null);
    assertEquals("Output plain text", "Hello World,123,4.56,true", outputString.trim());

    // Same with &format=csv
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1&format=csv", 200);
    assertTrue("Output text is missing", outputString != null);
    assertEquals("Output plain text", "Hello World,123,4.56,true", outputString.trim());
    
    // Get the agent instance's output as tab-delimited
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.tab?password=test-pwd-1", 200);
    assertTrue("Output text is missing", outputString != null);
    assertEquals("Output plain text", "Hello World\t123\t4.56\ttrue", outputString.trim());

    // Same with &format=tab
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1&format=tab", 200);
    assertTrue("Output text is missing", outputString != null);
    assertEquals("Output plain text", "Hello World\t123\t4.56\ttrue", outputString.trim());

    // Get the agent instance's output as RSS
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.rss?password=test-pwd-1", 200);
    assertTrue("Output test is missing", outputString != null);
    assertEquals("Output RSS", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><field1>Hello World</field1><field2>123</field2><field3>4.56</field3><field4>true</field4>", outputString.trim());

    // Same with &format=rss
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1&format=rss", 200);
    assertTrue("Output text is missing", outputString != null);
    assertEquals("Output RSS", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><field1>Hello World</field1><field2>123</field2><field3>4.56</field3><field4>true</field4>", outputString.trim());

    // Get the agent instance's output as HTML
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.html?password=test-pwd-1", 200);
    assertTrue("Output test is missing", outputString != null);
    assertEquals("Output HTML", "<html><head><title>Agent Output</title></head><body><h1>Output Field Values</h1><table border=\"1\"><tr><td>field1</td><td>String</td><td>Hello World</td></tr><tr><td>field2</td><td>Long</td><td>123</td></tr><tr><td>field3</td><td>Double</td><td>4.56</td></tr><tr><td>field4</td><td>Boolean</td><td>true</td></tr></table></body></html>", outputString.trim());

    // Same with &format=html
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1&format=html", 200);
    assertTrue("Output text is missing", outputString != null);
    assertEquals("Output HTML", "<html><head><title>Agent Output</title></head><body><h1>Output Field Values</h1><table border=\"1\"><tr><td>field1</td><td>String</td><td>Hello World</td></tr><tr><td>field2</td><td>Long</td><td>123</td></tr><tr><td>field3</td><td>Double</td><td>4.56</td></tr><tr><td>field4</td><td>Boolean</td><td>true</td></tr></table></body></html>", outputString.trim());

    // Test HTML output format for /about
    outputString = doGet(baseUrl + "/about?format=html", 200);
    assertTrue("Output text is missing", outputString != null);
    assertEquals("Output HTML", "<html><head><title>Agent Output</title></head><body><h1>Output Field Values</h1><table border=\"1\"><tr><td>name</td><td>String</td><td>MyTestAgentServer-0001</td></tr><tr><td>software</td><td>String</td><td>s0</td></tr><tr><td>version</td><td>String</td><td>0.1.0</td></tr><tr><td>description</td><td>String</td><td>Test server for Agent Server - Stage 0</td></tr><tr><td>website</td><td>String</td><td>http://basetechnology.com/agentserver</td></tr><tr><td>contact</td><td>String</td><td>agent-server-1-admin@basetechnology.com</td></tr></table></body></html>", outputString.trim());

  }

  @Test
  public void testBasicPersistence() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create a test user
    doPostJson(baseUrl + "/users?id=test-user-1&password=test-pwd-1", "{\"display_name\": \"Test User\"}", 201);

    // Create one agent definition that says "Hello World" in its output
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"test-user-1\", \"name\": \"HelloWorld\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}]}", 201);

    // Instantiate the agent definition once
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"test-user-1\", \"name\": \"MyHelloWorld\", \"definition\": \"HelloWorld\"}", 201);

    // Get agent server status
    JSONObject serverStatusJson = doGetJson(baseUrl + "/status", 200);
    String since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("Server status", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":1,\"num_active_users\":1,\"num_registered_agents\":1,\"num_active_agents\":1}", serverStatusJson.toString());

    // Shut down the server
    server.shutdown();
    assertEquals("Jetty status", "STOPPED", server.server.getState());

    // Make sure server is gone
    try {
      JSONObject statusJson = doGetJson(baseUrl + "/status", 200);
      assertTrue("Should not success with GET /status when server is shutdown", false);
    } catch (Exception e){
      assertEquals("Exception on GET /status of shutdown server", "Connection to http://localhost:" + server.appServerPort + " refused", e.getMessage());
    }
    
    // Restart the server
    server.start();

    // Make sure server is back
    try {
      JSONObject statusJson = doGetJson(baseUrl + "/status", 200);
      assertTrue("Server did not return status", statusJson != null);
      assertEquals("Server status: " +statusJson.toString(), "running", statusJson.getString("status"));
    } catch (Exception e){
      assertTrue("Exception on GET /status of shutdown server: " + e.getMessage(), false);
    }

    // Make sure agent server status is unchanged
    serverStatusJson = doGetJson(baseUrl + "/status", 200);
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("Server status", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":1,\"num_active_users\":1,\"num_registered_agents\":1,\"num_active_agents\":1}", serverStatusJson.toString());
    
    // Make sure user still exists
    JSONObject userJson = doGetJson(baseUrl + "/users/test-user-1?password=test-pwd-1", 200);
    assertTrue("User JSON not returned", userJson != null);
    assertTrue("User JSON does not have display_name", userJson.has("display_name"));
    assertEquals("User display_name", "Test User", userJson.getString("display_name"));

    // Make sure agent definition still exists
    JSONObject agDefJson = doGetJson(baseUrl + "/users/test-user-1/agent_definitions/HelloWorld?password=test-pwd-1", 200);
    assertTrue("Agent definition JSON not returned", agDefJson != null);
    assertTrue("Agent definition JSON does not have name", agDefJson.has("name"));
    assertEquals("Agent definition name", "HelloWorld", agDefJson.getString("name"));
    assertTrue("Agent definition JSON does not have user", agDefJson.has("user"));
    assertEquals("Agent definition user", "test-user-1", agDefJson.getString("user"));
    assertTrue("Agent definition JSON does not have outputs", agDefJson.has("outputs"));
    assertEquals("Agent definition outputs", "[{\"default_value\":\"Hello World\",\"name\":\"field1\",\"type\":\"string\"}]", agDefJson.getString("outputs"));
    
    // Make sure agent instance still exists
    JSONObject agInstJson = doGetJson(baseUrl + "/users/test-user-1/agents/MyHelloWorld?password=test-pwd-1", 200);
    assertTrue("Agent instance JSON not returned", agInstJson != null);
    assertTrue("Agent instance JSON does not have name", agInstJson.has("name"));
    assertEquals("Agent instance name", "MyHelloWorld", agInstJson.getString("name"));
    assertTrue("Agent instance JSON does not have user", agInstJson.has("user"));
    assertEquals("Agent instance user", "test-user-1", agInstJson.getString("user"));
    assertTrue("Agent instance JSON does not have definition", agInstJson.has("definition"));
    assertEquals("Agent instance definition", "HelloWorld", agInstJson.getString("definition"));
    
    // Add two users, two agent definitions, and three instances
    doPostJson(baseUrl + "/users", "{\"id\": \"test-user-2\", \"password\": \"test-pwd-2\", \"display_name\": \"Test User #2\"}", 201);
    doPostJson(baseUrl + "/users", "{\"id\": \"test-user-3\", \"password\": \"test-pwd-3\", \"display_name\": \"Test User #3\"}", 201);

    doPostJson(baseUrl + "/users/test-user-2/agent_definitions?password=test-pwd-2",
        "{\"user\": \"test-user-2\", \"name\": \"HelloWorld2\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World-2\"}]}", 201);
    doPostJson(baseUrl + "/users/test-user-2/agent_definitions?password=test-pwd-2",
        "{\"user\": \"test-user-2\", \"name\": \"HelloWorld3\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World-3\"}]}", 201);

    doPostJson(baseUrl + "/users/test-user-2/agents?password=test-pwd-2",
        "{\"user\": \"test-user-2\", \"name\": \"MyHelloWorld-2\", \"definition\": \"HelloWorld2\"}", 201);
    doPostJson(baseUrl + "/users/test-user-2/agents?password=test-pwd-2",
        "{\"user\": \"test-user-2\", \"name\": \"MyHelloWorld-3\", \"definition\": \"HelloWorld3\"}", 201);
    doPostJson(baseUrl + "/users/test-user-2/agents?password=test-pwd-2",
        "{\"user\": \"test-user-2\", \"name\": \"MyHelloWorld-4\", \"definition\": \"HelloWorld3\"}", 201);

    // Check to see that server status is updated for new object
    serverStatusJson = doGetJson(baseUrl + "/status", 200);
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("Server status", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":3,\"num_active_users\":2,\"num_registered_agents\":3,\"num_active_agents\":4}", serverStatusJson.toString());

    // Shut down the server
    server.shutdown();
    assertEquals("Jetty status", "STOPPED", server.server.getState());

    // Make sure server is gone
    try {
      JSONObject statusJson = doGetJson(baseUrl + "/status", 200);
      assertTrue("Should not success with GET /status when server is shutdown", false);
    } catch (Exception e){
      assertEquals("Exception on GET /status of shutdown server", "Connection to http://localhost:" + server.appServerPort + " refused", e.getMessage());
    }
    
    // Restart the server
    server.start();

    // Make sure server is back
    try {
      JSONObject statusJson = doGetJson(baseUrl + "/status", 200);
      assertTrue("Server did not return status", statusJson != null);
      assertEquals("Server status: " +statusJson.toString(), "running", statusJson.getString("status"));
    } catch (Exception e){
      assertTrue("Exception on GET /status of shutdown server: " + e.getMessage(), false);
    }

    // Make sure agent server status is unchanged
    serverStatusJson = doGetJson(baseUrl + "/status", 200);
    since = DateUtils.toRfcString(server.agentServer.startTime);
    assertEquals("Server status", "{\"status\":\"running\",\"since\":\"" + since + "\",\"num_registered_users\":3,\"num_active_users\":2,\"num_registered_agents\":3,\"num_active_agents\":4}", serverStatusJson.toString());

    //TODO: Need state in agent instance JSON
  }


  @Test
  public void testOutputLimits() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create a test user
    doPostJson(baseUrl + "/users?id=test-user-1&password=test-pwd-1", "{}", 201);

    // Allow for longer script execution
    server.agentServer.config.putDefaultExecutionLevel(3);
    
    // Create an agent definition that produces 100 rows of output
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"DataSource\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}, {\"name\": \"field2\", \"type\": \"int\", \"default_value\": 123}, {\"name\": \"field3\", \"type\": \"float\", \"default_value\": 4.56}, {\"name\": \"field4\", \"type\": \"boolean\", \"default_value\": true}], " +
            "\"scratchpad\": [{\"name\": \"count\", \"type\": \"int\"}], " +
            "\"memory\": [{\"name\": \"countm\", \"type\": \"int\"}], " +
            "\"timers\": [{\"name\": \"t1\", \"interval\": 20, \"script\": \"if (++count < 10){countm = count / 3; field2++; field3=3.14;}\"}]" +
            "}", 201);

    // Create an agent that simply generates a single line of output but depends on the data source
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}, {\"name\": \"field2\", \"type\": \"int\", \"default_value\": 123}, {\"name\": \"field3\", \"type\": \"float\", \"default_value\": 4.56}, {\"name\": \"field4\", \"type\": \"boolean\", \"default_value\": true}], " +
            "\"inputs\": [{\"name\": \"ds1\", \"data_source\": \"DataSource\"}], " +
            "\"scripts\": [{\"name\": \"init\", \"script\": \"for(int i = 0; i < 100; i++){outputs.field2++;; outputs.field3=3.14; /*outputs.commit();*/}\"}]" +
            "}", 201);

    // Instantiate the agent definition once
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"definition\": \"HelloWorld\"}", 201);

    // Give agent some time to run
    log.info("Before waitUntilDone");
    server.agentServer.agentScheduler.waitUntilDone();
    log.info("After waitUntilDone");
    
    //Thread.sleep(400);

    server.agentServer.agentScheduler.pause();

    // Get the agent instance's output as JSON
    JSONObject outputJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1", 200);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 4, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertTrue("Output field1 is not of type String: " + outputJson.get("field1").getClass().getSimpleName(), outputJson.get("field1") instanceof String);
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertTrue("Output field2 is not of type Long: " + outputJson.get("field2").getClass().getSimpleName(), outputJson.get("field2") instanceof Integer);
    assertEquals("Output field2", 223, outputJson.getInt("field2"));
    assertTrue("Output field3 is missing", outputJson.has("field3"));
    assertTrue("Output field1 is not of type Double: " + outputJson.get("field3").getClass().getSimpleName(), outputJson.get("field3") instanceof Double);
    assertEquals("Output field3", 3.14, outputJson.getDouble("field3"), 0.0003);
    assertTrue("Output field4 is missing", outputJson.has("field4"));
    assertTrue("Output field4 is not of type Boolean: " + outputJson.get("field4").getClass().getSimpleName(), outputJson.get("field4") instanceof Boolean);
    assertEquals("Output field4", true, outputJson.getBoolean("field4"));
    assertEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":223,\"field3\":3.14,\"field4\":true}", outputJson.toString());
    
    // Now check the agent output history
    JSONObject historyJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld/output_history?password=test-pwd-1", 200);
    assertTrue("Output history JSON is missing", historyJson != null);
    assertEquals("Count of fields in output history", 1, historyJson.length());
    JSONArray historyArrayJson = historyJson.getJSONArray("output_history");
    assertTrue("Output history JSON array is missing", historyArrayJson != null);
    assertEquals("Count of rows in output history", 2, historyArrayJson.length());
    outputJson = historyArrayJson.getJSONObject(0);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 4, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertTrue("Output field1 is not of type String: " + outputJson.get("field1").getClass().getSimpleName(), outputJson.get("field1") instanceof String);
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertTrue("Output field2 is not of type Long: " + outputJson.get("field2").getClass().getSimpleName(), outputJson.get("field2") instanceof Integer);
    assertEquals("Output field2", 123, outputJson.getInt("field2"));
    assertTrue("Output field3 is missing", outputJson.has("field3"));
    assertTrue("Output field1 is not of type Double: " + outputJson.get("field3").getClass().getSimpleName(), outputJson.get("field3") instanceof Double);
    assertEquals("Output field3", 4.56, outputJson.getDouble("field3"), 0.0003);
    assertTrue("Output field4 is missing", outputJson.has("field4"));
    assertTrue("Output field4 is not of type Boolean: " + outputJson.get("field4").getClass().getSimpleName(), outputJson.get("field4") instanceof Boolean);
    assertEquals("Output field4", true, outputJson.getBoolean("field4"));
    assertJsonSourceEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":123,\"field3\":4.56,\"field4\":true}", outputJson.toString());
    outputJson = historyArrayJson.getJSONObject(1);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 4, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertTrue("Output field1 is not of type String: " + outputJson.get("field1").getClass().getSimpleName(), outputJson.get("field1") instanceof String);
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertTrue("Output field2 is not of type Long: " + outputJson.get("field2").getClass().getSimpleName(), outputJson.get("field2") instanceof Integer);
    assertEquals("Output field2", 223, outputJson.getInt("field2"));
    assertTrue("Output field3 is missing", outputJson.has("field3"));
    assertTrue("Output field1 is not of type Double: " + outputJson.get("field3").getClass().getSimpleName(), outputJson.get("field3") instanceof Double);
    assertEquals("Output field3", 3.14, outputJson.getDouble("field3"), 0.0003);
    assertTrue("Output field4 is missing", outputJson.has("field4"));
    assertTrue("Output field4 is not of type Boolean: " + outputJson.get("field4").getClass().getSimpleName(), outputJson.get("field4") instanceof Boolean);
    assertEquals("Output field4", true, outputJson.getBoolean("field4"));
    assertJsonSourceEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":223,\"field3\":3.14,\"field4\":true}", outputJson.toString());
   
    // Get the generated name for the data source instance
    String dsInstanceName = server.agentServer.getAgentInstanceName("test-user-1", "HelloWorld", "ds1");
    assertTrue("Unable to find name of data source instance", dsInstanceName != null);
    AgentInstance dsInstance = server.agentServer.getAgentInstance("test-user-1", "HelloWorld", "ds1");
    assertTrue("Unable to find agent instance of data source instance", dsInstance != null);

    // Now check output history for data source
    historyJson = doGetJson(baseUrl + "/users/test-user-1/agents/" + dsInstanceName + "/output_history?password=test-pwd-1&count=20", 200);
    assertTrue("Output history JSON is missing", historyJson != null);
    assertEquals("Count of fields in output history", 1, historyJson.length());
    historyArrayJson = historyJson.getJSONArray("output_history");
    assertTrue("Output history JSON array is missing", historyArrayJson != null);
    assertEquals("Count of rows in output history", 10, historyArrayJson.length());
    outputJson = historyArrayJson.getJSONObject(0);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 4, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertTrue("Output field1 is not of type String: " + outputJson.get("field1").getClass().getSimpleName(), outputJson.get("field1") instanceof String);
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertTrue("Output field2 is not of type Long: " + outputJson.get("field2").getClass().getSimpleName(), outputJson.get("field2") instanceof Integer);
    assertEquals("Output field2", 123, outputJson.getInt("field2"));
    assertTrue("Output field3 is missing", outputJson.has("field3"));
    assertTrue("Output field1 is not of type Double: " + outputJson.get("field3").getClass().getSimpleName(), outputJson.get("field3") instanceof Double);
    assertEquals("Output field3", 4.56, outputJson.getDouble("field3"), 0.0003);
    assertTrue("Output field4 is missing", outputJson.has("field4"));
    assertTrue("Output field4 is not of type Boolean: " + outputJson.get("field4").getClass().getSimpleName(), outputJson.get("field4") instanceof Boolean);
    assertEquals("Output field4", true, outputJson.getBoolean("field4"));
    assertJsonSourceEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":123,\"field3\":4.56,\"field4\":true}", outputJson.toString());

    outputJson = historyArrayJson.getJSONObject(1);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 4, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertTrue("Output field1 is not of type String: " + outputJson.get("field1").getClass().getSimpleName(), outputJson.get("field1") instanceof String);
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertTrue("Output field2 is not of type Long: " + outputJson.get("field2").getClass().getSimpleName(), outputJson.get("field2") instanceof Integer);
    assertEquals("Output field2", 124, outputJson.getInt("field2"));
    assertTrue("Output field3 is missing", outputJson.has("field3"));
    assertTrue("Output field1 is not of type Double: " + outputJson.get("field3").getClass().getSimpleName(), outputJson.get("field3") instanceof Double);
    assertEquals("Output field3", 3.14, outputJson.getDouble("field3"), 0.0003);
    assertTrue("Output field4 is missing", outputJson.has("field4"));
    assertTrue("Output field4 is not of type Boolean: " + outputJson.get("field4").getClass().getSimpleName(), outputJson.get("field4") instanceof Boolean);
    assertEquals("Output field4", true, outputJson.getBoolean("field4"));
    assertJsonSourceEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":124,\"field3\":3.14,\"field4\":true}", outputJson.toString());

    outputJson = historyArrayJson.getJSONObject(2);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 4, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertTrue("Output field1 is not of type String: " + outputJson.get("field1").getClass().getSimpleName(), outputJson.get("field1") instanceof String);
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertTrue("Output field2 is not of type Long: " + outputJson.get("field2").getClass().getSimpleName(), outputJson.get("field2") instanceof Integer);
    assertEquals("Output field2", 125, outputJson.getInt("field2"));
    assertTrue("Output field3 is missing", outputJson.has("field3"));
    assertTrue("Output field1 is not of type Double: " + outputJson.get("field3").getClass().getSimpleName(), outputJson.get("field3") instanceof Double);
    assertEquals("Output field3", 3.14, outputJson.getDouble("field3"), 0.0003);
    assertTrue("Output field4 is missing", outputJson.has("field4"));
    assertTrue("Output field4 is not of type Boolean: " + outputJson.get("field4").getClass().getSimpleName(), outputJson.get("field4") instanceof Boolean);
    assertEquals("Output field4", true, outputJson.getBoolean("field4"));
    assertJsonSourceEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":125,\"field3\":3.14,\"field4\":true}", outputJson.toString());

    outputJson = historyArrayJson.getJSONObject(8);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 4, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertTrue("Output field1 is not of type String: " + outputJson.get("field1").getClass().getSimpleName(), outputJson.get("field1") instanceof String);
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertTrue("Output field2 is not of type Long: " + outputJson.get("field2").getClass().getSimpleName(), outputJson.get("field2") instanceof Integer);
    assertEquals("Output field2", 131, outputJson.getInt("field2"));
    assertTrue("Output field3 is missing", outputJson.has("field3"));
    assertTrue("Output field1 is not of type Double: " + outputJson.get("field3").getClass().getSimpleName(), outputJson.get("field3") instanceof Double);
    assertEquals("Output field3", 3.14, outputJson.getDouble("field3"), 0.0003);
    assertTrue("Output field4 is missing", outputJson.has("field4"));
    assertTrue("Output field4 is not of type Boolean: " + outputJson.get("field4").getClass().getSimpleName(), outputJson.get("field4") instanceof Boolean);
    assertEquals("Output field4", true, outputJson.getBoolean("field4"));
    assertJsonSourceEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":131,\"field3\":3.14,\"field4\":true}", outputJson.toString());

    outputJson = historyArrayJson.getJSONObject(9);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 4, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertTrue("Output field1 is not of type String: " + outputJson.get("field1").getClass().getSimpleName(), outputJson.get("field1") instanceof String);
    assertEquals("Output field1", "Hello World", outputJson.getString("field1"));
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertTrue("Output field2 is not of type Long: " + outputJson.get("field2").getClass().getSimpleName(), outputJson.get("field2") instanceof Integer);
    assertEquals("Output field2", 132, outputJson.getInt("field2"));
    assertTrue("Output field3 is missing", outputJson.has("field3"));
    assertTrue("Output field1 is not of type Double: " + outputJson.get("field3").getClass().getSimpleName(), outputJson.get("field3") instanceof Double);
    assertEquals("Output field3", 3.14, outputJson.getDouble("field3"), 0.0003);
    assertTrue("Output field4 is missing", outputJson.has("field4"));
    assertTrue("Output field4 is not of type Boolean: " + outputJson.get("field4").getClass().getSimpleName(), outputJson.get("field4") instanceof Boolean);
    assertEquals("Output field4", true, outputJson.getBoolean("field4"));
    assertJsonSourceEquals("Output JSON", "{\"field1\":\"Hello World\",\"field2\":132,\"field3\":3.14,\"field4\":true}", outputJson.toString());

    // Make sure state is only present if requested for an instance
    JSONObject instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/" + dsInstanceName + "?password=test-pwd-1", 200);
    assertTrue("Agent instance info is missing", instanceJson != null);
    int countExpectedInstanceKeys = 16;
    assertEquals("Count of fields in agent instance info", countExpectedInstanceKeys, instanceJson.length());
    assertTrue("State is unexpectedly present", ! instanceJson.has("state"));
    assertTrue("Agent instance name is missing", instanceJson.has("name"));
    assertEquals("Agent instance name", "DataSource_1", instanceJson.getString("name"));
    assertTrue("Agent instance user id is missing", instanceJson.has("user"));
    assertEquals("Agent instance user id", "test-user-1", instanceJson.getString("user"));
    assertTrue("Agent instance definition is missing", instanceJson.has("definition"));
    assertEquals("Agent instance definition", "DataSource", instanceJson.getString("definition"));
    assertTrue("Agent instance description is missing", instanceJson.has("description"));
    assertEquals("Agent instance description", "", instanceJson.getString("description"));
    assertTrue("Agent instance created is missing", instanceJson.has("instantiated"));
    assertEquals("Agent instance created", DateUtils.toRfcString(dsInstance.timeInstantiated), instanceJson.getString("instantiated"));
    assertTrue("Agent instance modified is missing", instanceJson.has("updated"));
    assertEquals("Agent instance modified", dsInstance.timeUpdated > 0 ? DateUtils.toRfcString(dsInstance.timeUpdated) : "", instanceJson.getString("updated"));
    assertTrue("Agent instance trigger_interval is missing", instanceJson.has("trigger_interval"));
    assertEquals("Agent instance trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION, instanceJson.getString("trigger_interval"));
    assertTrue("Agent instance reporting_interval is missing", instanceJson.has("reporting_interval"));
    assertEquals("Agent instance reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION, instanceJson.getString("reporting_interval"));
    assertTrue("Agent instance enabled is missing", instanceJson.has("enabled"));
    assertEquals("Agent instance name", true, instanceJson.getBoolean("enabled"));
    assertTrue("Agent instance parameter_values is missing", instanceJson.has("parameter_values"));
    assertEquals("Agent instance parameter_values", "{}", instanceJson.get("parameter_values").toString());

    // Check the full data source instance state history
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/" + dsInstanceName + "?password=test-pwd-1&state=yes", 200);
    assertTrue("Agent instance info is missing", instanceJson != null);
    assertEquals("Count of fields in agent instance info", 17, instanceJson.length());
    assertTrue("State is missing", instanceJson.has("state"));
    assertTrue("Agent instance name is missing", instanceJson.has("name"));
    assertEquals("Agent instance name", "DataSource_1", instanceJson.getString("name"));
    assertTrue("Agent instance user id is missing", instanceJson.has("user"));
    assertEquals("Agent instance user id", "test-user-1", instanceJson.getString("user"));
    assertTrue("Agent instance definition is missing", instanceJson.has("definition"));
    assertEquals("Agent instance definition", "DataSource", instanceJson.getString("definition"));
    assertTrue("Agent instance description is missing", instanceJson.has("description"));
    assertEquals("Agent instance description", "", instanceJson.getString("description"));
    assertTrue("Agent instance created is missing", instanceJson.has("instantiated"));
    assertEquals("Agent instance created", DateUtils.toRfcString(dsInstance.timeInstantiated), instanceJson.getString("instantiated"));
    assertTrue("Agent instance modified is missing", instanceJson.has("updated"));
    assertEquals("Agent instance modified", dsInstance.timeUpdated > 0 ? DateUtils.toRfcString(dsInstance.timeUpdated) : "", instanceJson.getString("updated"));
    assertTrue("Agent instance status is missing", instanceJson.has("status"));
    assertEquals("Agent instance status", "active", instanceJson.getString("status"));
    assertTrue("Agent instance inputs_changed is missing", instanceJson.has("inputs_changed"));
    assertEquals("Agent instance inputs_changed", dsInstance.lastInputsChanged > 0 ? DateUtils.toRfcString(dsInstance.lastInputsChanged) : "", instanceJson.getString("inputs_changed"));
    assertTrue("Agent instance triggered is missing", instanceJson.has("triggered"));
    assertEquals("Agent instance triggered", dsInstance.lastTriggered > 0 ? DateUtils.toRfcString(dsInstance.lastTriggered) : "", instanceJson.getString("triggered"));
    assertTrue("Agent instance outputs_changed is missing", instanceJson.has("outputs_changed"));
    OutputHistory outputHistory = dsInstance.outputHistory;
    int numOutputs = outputHistory.size();
    long timeOutputsChanged = outputHistory.get(numOutputs - 1).time;
    assertEquals("Agent instance outputs_changed", timeOutputsChanged > 0 ? DateUtils.toRfcString(timeOutputsChanged) : "", instanceJson.getString("outputs_changed"));
    assertTrue("Agent instance trigger_interval is missing", instanceJson.has("trigger_interval"));
    assertEquals("Agent instance trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION, instanceJson.getString("trigger_interval"));
    assertTrue("Agent instance reporting_interval is missing", instanceJson.has("reporting_interval"));
    assertEquals("Agent instance reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION, instanceJson.getString("reporting_interval"));
    assertTrue("Agent instance enabled is missing", instanceJson.has("enabled"));
    assertTrue("Agent instance limit_instance_states_stored is missing", instanceJson.has("limit_instance_states_stored"));
    assertEquals("Agent instance limit_instance_states_stored", AgentInstance.DEFAULT_LIMIT_INSTANCE_STATES_STORED, instanceJson.getInt("limit_instance_states_stored"));
    assertEquals("Agent instance name", true, instanceJson.getBoolean("enabled"));
    assertTrue("Agent instance parameter_values is missing", instanceJson.has("parameter_values"));
    assertEquals("Agent instance parameter_values", "{}", instanceJson.get("parameter_values").toString());
    assertTrue("Agent instance state is missing", instanceJson.has("state"));
    Object stateObject = instanceJson.get("state");
    assertTrue("Agent instance state is not a JSONArray: " + stateObject.getClass().getSimpleName(), stateObject instanceof JSONArray);
    JSONArray stateArrayJson = (JSONArray)stateObject;
    assertEquals("Count of data source instance states", 10, stateArrayJson.length());
    JSONObject stateJson = stateArrayJson.getJSONObject(0);
    assertTrue("Data source state[0] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[0]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":4.56,\"field2\":123}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[0] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[0]", "{\"countm\":0}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(1);
    assertTrue("Data source state[1] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[1]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":124}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[1] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[1]", "{\"countm\":0}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(2);
    assertTrue("Data source state[2] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[2]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":125}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[2] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[2]", "{\"countm\":0}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(3);
    assertTrue("Data source state[3] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[3]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":126}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[3] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[3]", "{\"countm\":1}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(4);
    assertTrue("Data source state[4] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[4]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":127}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[4] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[4]", "{\"countm\":1}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(5);
    assertTrue("Data source state[5] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[5]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":128}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[5] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[5]", "{\"countm\":1}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(6);
    assertTrue("Data source state[6] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[6]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":129}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[6] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[6]", "{\"countm\":2}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(7);
    assertTrue("Data source state[7] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[7]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":130}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[7] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[7]", "{\"countm\":2}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(8);
    assertTrue("Data source state[8] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[8]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":131}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[8] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[8]", "{\"countm\":2}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(9);
    assertTrue("Data source state[9] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[9]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":132}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[9] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[9]", "{\"countm\":3}", stateJson.getJSONObject("memory").toString());
    //assertJsonSourceEquals("Agent instance state", "[{\"time\":\"" + DateUtils.toIsoString(dsInstance.state.get(0).time) + "\",\"inputs\":{},\"events\":{},\"parameters\":{},\"outputs\":{\"field4\":true,\"field3\":3.14,\"field2\":132,\"field1\":\"Hello World\"},\"memory\":{\"countm\":1}}]", instanceJson.get("state").toString());

    // Now check the agent instance without state
    AgentInstance agInstance = server.agentServer.getAgentInstance("test-user-1", "HelloWorld");
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld?password=test-pwd-1", 200);
    assertTrue("Agent instance info is missing", instanceJson != null);
    assertEquals("Count of fields in agent instance info", countExpectedInstanceKeys, instanceJson.length());
    assertTrue("State is unexpectedly present", ! instanceJson.has("state"));
    assertTrue("Agent instance name is missing", instanceJson.has("name"));
    assertEquals("Agent instance name", "HelloWorld", instanceJson.getString("name"));
    assertTrue("Agent instance user id is missing", instanceJson.has("user"));
    assertEquals("Agent instance user id", "test-user-1", instanceJson.getString("user"));
    assertTrue("Agent instance definition is missing", instanceJson.has("definition"));
    assertEquals("Agent instance definition", "HelloWorld", instanceJson.getString("definition"));
    assertTrue("Agent instance description is missing", instanceJson.has("description"));
    assertEquals("Agent instance description", "", instanceJson.getString("description"));
    assertTrue("Agent instance created is missing", instanceJson.has("instantiated"));
    assertEquals("Agent instance created", DateUtils.toRfcString(agInstance.timeInstantiated), instanceJson.getString("instantiated"));
    assertTrue("Agent instance modified is missing", instanceJson.has("updated"));
    assertEquals("Agent instance modified", dsInstance.timeUpdated > 0 ? DateUtils.toRfcString(agInstance.timeUpdated) : "", instanceJson.getString("updated"));
    assertTrue("Agent instance trigger_interval is missing", instanceJson.has("trigger_interval"));
    assertEquals("Agent instance trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION, instanceJson.getString("trigger_interval"));
    assertTrue("Agent instance reporting_interval is missing", instanceJson.has("reporting_interval"));
    assertEquals("Agent instance reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION, instanceJson.getString("reporting_interval"));
    assertTrue("Agent instance enabled is missing", instanceJson.has("enabled"));
    assertEquals("Agent instance name", true, instanceJson.getBoolean("enabled"));
    assertTrue("Agent instance parameter_values is missing", instanceJson.has("parameter_values"));
    assertEquals("Agent instance parameter_values", "{}", instanceJson.get("parameter_values").toString());

    // Check instance state with an explicit count, &count=1 to get only latest state
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/" + dsInstanceName + "?password=test-pwd-1&state=yes&count=1", 200);
    assertTrue("Agent instance info is missing", instanceJson != null);
    assertEquals("Count of fields in agent instance info", 17, instanceJson.length());
    assertTrue("State is missing", instanceJson.has("state"));
    assertTrue("Agent instance name is missing", instanceJson.has("name"));
    assertEquals("Agent instance name", "DataSource_1", instanceJson.getString("name"));
    assertTrue("Agent instance user id is missing", instanceJson.has("user"));
    assertEquals("Agent instance user id", "test-user-1", instanceJson.getString("user"));
    assertTrue("Agent instance definition is missing", instanceJson.has("definition"));
    assertEquals("Agent instance definition", "DataSource", instanceJson.getString("definition"));
    assertTrue("Agent instance description is missing", instanceJson.has("description"));
    assertEquals("Agent instance description", "", instanceJson.getString("description"));
    assertTrue("Agent instance created is missing", instanceJson.has("instantiated"));
    assertEquals("Agent instance created", DateUtils.toRfcString(dsInstance.timeInstantiated), instanceJson.getString("instantiated"));
    assertTrue("Agent instance modified is missing", instanceJson.has("updated"));
    assertEquals("Agent instance modified", dsInstance.timeUpdated > 0 ? DateUtils.toRfcString(dsInstance.timeUpdated) : "", instanceJson.getString("updated"));
    assertTrue("Agent instance status is missing", instanceJson.has("status"));
    assertEquals("Agent instance status", "active", instanceJson.getString("status"));
    assertTrue("Agent instance inputs_changed is missing", instanceJson.has("inputs_changed"));
    assertEquals("Agent instance inputs_changed", dsInstance.lastInputsChanged > 0 ? DateUtils.toRfcString(dsInstance.lastInputsChanged) : "", instanceJson.getString("inputs_changed"));
    assertTrue("Agent instance triggered is missing", instanceJson.has("triggered"));
    assertEquals("Agent instance triggered", dsInstance.lastTriggered > 0 ? DateUtils.toRfcString(dsInstance.lastTriggered) : "", instanceJson.getString("triggered"));
    assertTrue("Agent instance outputs_changed is missing", instanceJson.has("outputs_changed"));
    outputHistory = dsInstance.outputHistory;
    numOutputs = outputHistory.size();
    timeOutputsChanged = outputHistory.get(numOutputs - 1).time;
    assertEquals("Agent instance outputs_changed", timeOutputsChanged > 0 ? DateUtils.toRfcString(timeOutputsChanged) : "", instanceJson.getString("outputs_changed"));
    assertTrue("Agent instance trigger_interval is missing", instanceJson.has("trigger_interval"));
    assertEquals("Agent instance trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION, instanceJson.getString("trigger_interval"));
    assertTrue("Agent instance reporting_interval is missing", instanceJson.has("reporting_interval"));
    assertEquals("Agent instance reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION, instanceJson.getString("reporting_interval"));
    assertTrue("Agent instance enabled is missing", instanceJson.has("enabled"));
    assertTrue("Agent instance limit_instance_states_stored is missing", instanceJson.has("limit_instance_states_stored"));
    assertEquals("Agent instance limit_instance_states_stored", AgentInstance.DEFAULT_LIMIT_INSTANCE_STATES_STORED, instanceJson.getInt("limit_instance_states_stored"));
    assertEquals("Agent instance name", true, instanceJson.getBoolean("enabled"));
    assertTrue("Agent instance parameter_values is missing", instanceJson.has("parameter_values"));
    assertEquals("Agent instance parameter_values", "{}", instanceJson.get("parameter_values").toString());
    assertTrue("Agent instance state is missing", instanceJson.has("state"));
    stateObject = instanceJson.get("state");
    assertTrue("Agent instance state is not a JSONArray: " + stateObject.getClass().getSimpleName(), stateObject instanceof JSONArray);
    stateArrayJson = (JSONArray)stateObject;
    assertEquals("Count of data source instance states", 1, stateArrayJson.length());
    stateJson = stateArrayJson.getJSONObject(0);
    int stateHistorySize = dsInstance.state.size();
    long stateTime = dsInstance.state.get(stateHistorySize - 1).time;
    String stateTimeString = DateUtils.toIsoString(stateTime); 
    assertEquals("State", "{\"exceptions\":[],\"time\":\"" + stateTimeString + "\",\"notification_history\":[],\"inputs\":{},\"notifications\":[],\"parameters\":{},\"outputs\":{\"field4\":true,\"field3\":3.14,\"field2\":132,\"field1\":\"Hello World\"},\"memory\":{\"countm\":3},\"last_dismissed_exception\":\"\"}", stateJson.toString());

    // Check instance state with an explicit count, &count=3, to get latest 3

    // Check the full data source instance state history
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/" + dsInstanceName + "?password=test-pwd-1&state=yes&count=3", 200);
    assertTrue("Agent instance info is missing", instanceJson != null);
    assertEquals("Count of fields in agent instance info", 17, instanceJson.length());
    assertTrue("State is missing", instanceJson.has("state"));
    assertTrue("Agent instance name is missing", instanceJson.has("name"));
    assertEquals("Agent instance name", "DataSource_1", instanceJson.getString("name"));
    assertTrue("Agent instance user id is missing", instanceJson.has("user"));
    assertEquals("Agent instance user id", "test-user-1", instanceJson.getString("user"));
    assertTrue("Agent instance definition is missing", instanceJson.has("definition"));
    assertEquals("Agent instance definition", "DataSource", instanceJson.getString("definition"));
    assertTrue("Agent instance description is missing", instanceJson.has("description"));
    assertEquals("Agent instance description", "", instanceJson.getString("description"));
    assertTrue("Agent instance created is missing", instanceJson.has("instantiated"));
    assertEquals("Agent instance created", DateUtils.toRfcString(dsInstance.timeInstantiated), instanceJson.getString("instantiated"));
    assertTrue("Agent instance modified is missing", instanceJson.has("updated"));
    assertEquals("Agent instance modified", dsInstance.timeUpdated > 0 ? DateUtils.toRfcString(dsInstance.timeUpdated) : "", instanceJson.getString("updated"));
    assertTrue("Agent instance status is missing", instanceJson.has("status"));
    assertEquals("Agent instance status", "active", instanceJson.getString("status"));
    assertTrue("Agent instance inputs_changed is missing", instanceJson.has("inputs_changed"));
    assertEquals("Agent instance inputs_changed", dsInstance.lastInputsChanged > 0 ? DateUtils.toRfcString(dsInstance.lastInputsChanged) : "", instanceJson.getString("inputs_changed"));
    assertTrue("Agent instance triggered is missing", instanceJson.has("triggered"));
    assertEquals("Agent instance triggered", dsInstance.lastTriggered > 0 ? DateUtils.toRfcString(dsInstance.lastTriggered) : "", instanceJson.getString("triggered"));
    assertTrue("Agent instance outputs_changed is missing", instanceJson.has("outputs_changed"));
    outputHistory = dsInstance.outputHistory;
    numOutputs = outputHistory.size();
    timeOutputsChanged = outputHistory.get(numOutputs - 1).time;
    assertEquals("Agent instance outputs_changed", timeOutputsChanged > 0 ? DateUtils.toRfcString(timeOutputsChanged) : "", instanceJson.getString("outputs_changed"));
    assertTrue("Agent instance trigger_interval is missing", instanceJson.has("trigger_interval"));
    assertEquals("Agent instance trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION, instanceJson.getString("trigger_interval"));
    assertTrue("Agent instance reporting_interval is missing", instanceJson.has("reporting_interval"));
    assertEquals("Agent instance reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION, instanceJson.getString("reporting_interval"));
    assertTrue("Agent instance enabled is missing", instanceJson.has("enabled"));
    assertEquals("Agent instance name", true, instanceJson.getBoolean("enabled"));
    assertTrue("Agent instance parameter_values is missing", instanceJson.has("parameter_values"));
    assertEquals("Agent instance parameter_values", "{}", instanceJson.get("parameter_values").toString());
    assertTrue("Agent instance state is missing", instanceJson.has("state"));
    stateObject = instanceJson.get("state");
    assertTrue("Agent instance state is not a JSONArray: " + stateObject.getClass().getSimpleName(), stateObject instanceof JSONArray);
    stateArrayJson = (JSONArray)stateObject;
    assertEquals("Count of data source instance states", 3, stateArrayJson.length());
    stateJson = stateArrayJson.getJSONObject(0);
    assertTrue("Data source state[0] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[0]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":130}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[0] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[0]", "{\"countm\":2}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(1);
    assertTrue("Data source state[1] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[1]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":131}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[1] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[1]", "{\"countm\":2}", stateJson.getJSONObject("memory").toString());
    stateJson = stateArrayJson.getJSONObject(2);
    assertTrue("Data source state[2] is missing outputs", stateJson.has("outputs"));
    assertJsonSourceEquals("Output state[2]", "{\"field1\":\"Hello World\", \"field4\":true,\"field3\":3.14,\"field2\":132}", stateJson.getJSONObject("outputs").toString());
    assertTrue("Data source state[2] is missing memory", stateJson.has("memory"));
    assertJsonSourceEquals("Memory state[2]", "{\"countm\":3}", stateJson.getJSONObject("memory").toString());

    // Check the agent instance state history
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld?password=test-pwd-1&state=yes", 200);
    assertTrue("Agent instance info is missing", instanceJson != null);
    assertEquals("Count of fields in agent instance info", 17, instanceJson.length());
    assertTrue("State is missing", instanceJson.has("state"));
    assertTrue("Agent instance name is missing", instanceJson.has("name"));
    assertEquals("Agent instance name", "HelloWorld", instanceJson.getString("name"));
    assertTrue("Agent instance user id is missing", instanceJson.has("user"));
    assertEquals("Agent instance user id", "test-user-1", instanceJson.getString("user"));
    assertTrue("Agent instance definition is missing", instanceJson.has("definition"));
    assertEquals("Agent instance definition", "HelloWorld", instanceJson.getString("definition"));
    assertTrue("Agent instance description is missing", instanceJson.has("description"));
    assertEquals("Agent instance description", "", instanceJson.getString("description"));
    assertTrue("Agent instance created is missing", instanceJson.has("instantiated"));
    assertEquals("Agent instance created", DateUtils.toRfcString(agInstance.timeInstantiated), instanceJson.getString("instantiated"));
    assertTrue("Agent instance modified is missing", instanceJson.has("updated"));
    assertEquals("Agent instance modified", agInstance.timeUpdated > 0 ? DateUtils.toRfcString(agInstance.timeUpdated) : "", instanceJson.getString("updated"));
    assertTrue("Agent instance status is missing", instanceJson.has("status"));
    assertEquals("Agent instance status", "active", instanceJson.getString("status"));
    assertTrue("Agent instance inputs_changed is missing", instanceJson.has("inputs_changed"));
    assertEquals("Agent instance inputs_changed", agInstance.lastInputsChanged > 0 ? DateUtils.toRfcString(agInstance.lastInputsChanged) : "", instanceJson.getString("inputs_changed"));
    assertTrue("Agent instance triggered is missing", instanceJson.has("triggered"));
    assertEquals("Agent instance triggered", agInstance.lastTriggered > 0 ? DateUtils.toRfcString(agInstance.lastTriggered) : "", instanceJson.getString("triggered"));
    assertTrue("Agent instance outputs_changed is missing", instanceJson.has("outputs_changed"));
    outputHistory = agInstance.outputHistory;
    numOutputs = outputHistory.size();
    timeOutputsChanged = outputHistory.get(numOutputs - 1).time;
    assertEquals("Agent instance outputs_changed", timeOutputsChanged > 0 ? DateUtils.toRfcString(timeOutputsChanged) : "", instanceJson.getString("outputs_changed"));
    assertTrue("Agent instance trigger_interval is missing", instanceJson.has("trigger_interval"));
    assertEquals("Agent instance trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION, instanceJson.getString("trigger_interval"));
    assertTrue("Agent instance reporting_interval is missing", instanceJson.has("reporting_interval"));
    assertEquals("Agent instance reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION, instanceJson.getString("reporting_interval"));
    assertTrue("Agent instance public_output is missing", instanceJson.has("public_output"));
    assertEquals("Agent instance public_output", AgentInstance.DEFAULT_PUBLIC_OUTPUT, instanceJson.getBoolean("public_output"));
    assertTrue("Agent instance limit_instance_states_stored is missing", instanceJson.has("limit_instance_states_stored"));
    assertEquals("Agent instance limit_instance_states_stored", AgentInstance.DEFAULT_LIMIT_INSTANCE_STATES_STORED, instanceJson.getInt("limit_instance_states_stored"));
    assertTrue("Agent instance enabled is missing", instanceJson.has("enabled"));
    assertEquals("Agent instance name", true, instanceJson.getBoolean("enabled"));
    assertTrue("Agent instance parameter_values is missing", instanceJson.has("parameter_values"));
    assertEquals("Agent instance parameter_values", "{}", instanceJson.get("parameter_values").toString());
    JSONArray stateHistoryJson = instanceJson.getJSONArray("state");
    assertEquals("Count of agent instance history states", 2, stateHistoryJson.length());
    stateJson = stateHistoryJson.getJSONObject(0);
    assertJsonSourceEquals("Agent instance state", "{\"time\":\"" + DateUtils.toIsoString(agInstance.state.get(0).time) + "\",\"inputs\":{\"ds1\":{\"field4\":true,\"field3\":4.56,\"field2\":123,\"field1\":\"Hello World\"}},\"parameters\":{},\"outputs\":{\"field4\":true,\"field3\":4.56,\"field2\":123,\"field1\":\"Hello World\"},\"memory\":{},\"exceptions\":[],\"last_dismissed_exception\":\"\",\"notifications\":[],\"notification_history\":[]}", stateJson.toString());
    
    // Now restart the server and see that state is restored
    restartServer();

    agInstance = server.agentServer.getAgentInstance("test-user-1", "HelloWorld");

    // Check the agent instance state history
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld?password=test-pwd-1&state=yes", 200);
    assertTrue("Agent instance info is missing", instanceJson != null);
    assertEquals("Count of fields in agent instance info", 17, instanceJson.length());
    assertTrue("State is missing", instanceJson.has("state"));
    assertTrue("Agent instance name is missing", instanceJson.has("name"));
    assertEquals("Agent instance name", "HelloWorld", instanceJson.getString("name"));
    assertTrue("Agent instance user id is missing", instanceJson.has("user"));
    assertEquals("Agent instance user id", "test-user-1", instanceJson.getString("user"));
    assertTrue("Agent instance definition is missing", instanceJson.has("definition"));
    assertEquals("Agent instance definition", "HelloWorld", instanceJson.getString("definition"));
    assertTrue("Agent instance description is missing", instanceJson.has("description"));
    assertEquals("Agent instance description", "", instanceJson.getString("description"));
    assertTrue("Agent instance created is missing", instanceJson.has("instantiated"));
    assertEquals("Agent instance created", DateUtils.toRfcString(agInstance.timeInstantiated), instanceJson.getString("instantiated"));
    assertTrue("Agent instance modified is missing", instanceJson.has("updated"));
    assertEquals("Agent instance modified", agInstance.timeUpdated > 0 ? DateUtils.toRfcString(agInstance.timeUpdated) : "", instanceJson.getString("updated"));
    assertTrue("Agent instance status is missing", instanceJson.has("status"));
    assertEquals("Agent instance status", "active", instanceJson.getString("status"));
    assertTrue("Agent instance inputs_changed is missing", instanceJson.has("inputs_changed"));
    assertEquals("Agent instance inputs_changed", agInstance.lastInputsChanged > 0 ? DateUtils.toRfcString(agInstance.lastInputsChanged) : "", instanceJson.getString("inputs_changed"));
    assertTrue("Agent instance triggered is missing", instanceJson.has("triggered"));
    assertEquals("Agent instance triggered", agInstance.lastTriggered > 0 ? DateUtils.toRfcString(agInstance.lastTriggered) : "", instanceJson.getString("triggered"));
    assertTrue("Agent instance outputs_changed is missing", instanceJson.has("outputs_changed"));
    outputHistory = agInstance.outputHistory;
    numOutputs = outputHistory.size();
    timeOutputsChanged = outputHistory.get(numOutputs - 1).time;
    assertEquals("Agent instance outputs_changed", timeOutputsChanged > 0 ? DateUtils.toRfcString(timeOutputsChanged) : "", instanceJson.getString("outputs_changed"));
    assertTrue("Agent instance trigger_interval is missing", instanceJson.has("trigger_interval"));
    assertEquals("Agent instance trigger_interval", AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION, instanceJson.getString("trigger_interval"));
    assertTrue("Agent instance reporting_interval is missing", instanceJson.has("reporting_interval"));
    assertEquals("Agent instance reporting_interval", AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION, instanceJson.getString("reporting_interval"));
    assertTrue("Agent instance public_output is missing", instanceJson.has("public_output"));
    assertEquals("Agent instance public_output", AgentInstance.DEFAULT_PUBLIC_OUTPUT, instanceJson.getBoolean("public_output"));
    assertTrue("Agent instance limit_instance_states_stored is missing", instanceJson.has("limit_instance_states_stored"));
    assertEquals("Agent instance limit_instance_states_stored", AgentInstance.DEFAULT_LIMIT_INSTANCE_STATES_STORED, instanceJson.getInt("limit_instance_states_stored"));
    assertTrue("Agent instance enabled is missing", instanceJson.has("enabled"));
    assertEquals("Agent instance name", true, instanceJson.getBoolean("enabled"));
    assertTrue("Agent instance parameter_values is missing", instanceJson.has("parameter_values"));
    assertEquals("Agent instance parameter_values", "{}", instanceJson.get("parameter_values").toString());
    stateHistoryJson = instanceJson.getJSONArray("state");
    assertEquals("Count of agent instance history states", 2 + 1, stateHistoryJson.length());
    stateJson = stateHistoryJson.getJSONObject(0);
    assertJsonSourceEquals("Agent instance state", "{\"time\":\"" + DateUtils.toIsoString(agInstance.state.get(0).time) + "\",\"inputs\":{\"ds1\":{\"field4\":true,\"field3\":4.56,\"field2\":123,\"field1\":\"Hello World\"}},\"parameters\":{},\"outputs\":{\"field4\":true,\"field3\":4.56,\"field2\":123,\"field1\":\"Hello World\"},\"memory\":{},\"exceptions\":[],\"last_dismissed_exception\":\"\",\"notifications\":[],\"notification_history\":[]}", stateJson.toString());
    stateJson = stateHistoryJson.getJSONObject(1);
    // TODO: Review this and figure out which state is correct
    // Tentative conclusion: exact execution order of the two agents is indeterminate
    //assertJsonSourceEquals("Agent instance state", "{\"time\":\"" + DateUtils.toIsoString(agInstance.state.get(1).time) + "\",\"inputs\":{\"ds1\":{\"field4\":true,\"field3\":4.56,\"field2\":123,\"field1\":\"Hello World\"}},\"parameters\":{},\"outputs\":{\"field4\":true,\"field3\":3.14,\"field2\":223,\"field1\":\"Hello World\"},\"memory\":{},\"exceptions\":[],\"last_dismissed_exception\":\"\",\"notifications\":[],\"notification_history\":[]}", stateJson.toString());
    //assertJsonSourceEquals("Agent instance state", "{\"time\":\"" + DateUtils.toIsoString(agInstance.state.get(1).time) + "\",\"inputs\":{\"ds1\":{\"field4\":true,\"field3\":3.14,\"field2\":124,\"field1\":\"Hello World\"}},\"parameters\":{},\"outputs\":{\"field4\":true,\"field3\":3.14,\"field2\":223,\"field1\":\"Hello World\"},\"memory\":{},\"exceptions\":[],\"last_dismissed_exception\":\"\",\"notifications\":[],\"notification_history\":[]}", stateJson.toString());
    stateJson = stateHistoryJson.getJSONObject(2);
    assertJsonSourceEquals("Agent instance state", "{\"time\":\"" + DateUtils.toIsoString(agInstance.state.get(2).time) + "\",\"inputs\":{\"ds1\":{\"field4\":true,\"field3\":3.14,\"field2\":132,\"field1\":\"Hello World\"}},\"parameters\":{},\"outputs\":{\"field4\":true,\"field3\":3.14,\"field2\":323,\"field1\":\"Hello World\"},\"memory\":{},\"exceptions\":[],\"last_dismissed_exception\":\"\",\"notifications\":[],\"notification_history\":[]}", stateJson.toString());

    // TODO: Determine what state history should be on restart
    
  }

  @Test
  public void testWebSiteAccess() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create four test users
    doPostJson(baseUrl + "/users?id=user1&password=pwd1", "{}", 201);
    doPostJson(baseUrl + "/users?id=user2&password=pwd2", "{}", 201);
    doPostJson(baseUrl + "/users?id=user3&password=pwd3", "{}", 201);
    doPostJson(baseUrl + "/users?id=user4&password=pwd4", "{}", 201);


    // Now manually add some access entries
    
    // Four sites, site1, site2, site3, site4
    String site1 = "http://www.site1.com/";
    String site2 = "http://www.site2.com/";
    String site3 = "http://www.site3.com/";
    String site4 = "http://www.site4.com/";
    String site5 = "http://www.site5.com/";
    
    // For users, user1, user2, user3, user4
    
    // Scenario
    //
    // site1: Full access to all
    // site2: No access to anybody
    // site3: Access to all but user1 and user2
    // site4: Access to nobody but user1 and user2
    // site5: no entry

    // Verify that getting web site access controls requires admin password
    JSONObject accessJson = doGetJson(baseUrl + "/users/user1/website_access?password=pwd1", 400);
    assertTrue("JSON", accessJson != null);
    assertJsonSourceEquals("JSON error", "{\"errors\":[{\"message\":\"Invalid admin password\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException\"}]}", accessJson.toString());

    // Verify that setting web site access controls requires admin password
    accessJson = doPostJson(baseUrl + "/users/user1/website_access?password=pwd1", "{}", 400);
    assertTrue("JSON", accessJson != null);
    assertJsonSourceEquals("JSON error", "{\"errors\":[{\"message\":\"Invalid admin password\",\"type\":\"com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException\"}]}", accessJson.toString());

    // Verify that getting web site access controls is a no-op if none set
    accessJson = doGetJson(baseUrl + "/users/user1/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertEquals("Access JSON map size", 0, accessJson.length());

    // Verify that add of an empty list is a no-op
    accessJson = doPostJson(baseUrl + "/users/user1/website_access?password=" + server.agentServer.getAdminPassword(),
        "{}",
        204);
    assertTrue("Access JSON map should not be returned", accessJson == null);

    // Verify that bad access keyword is detected
    accessJson = doPostJson(baseUrl + "/users/user1/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site1.com/\": \"grantx\"}",
        400);
    assertTrue("JSON", accessJson != null);
    assertJsonSourceEquals("JSON error", "{\"errors\":[{\"message\":\"Verb parameter for addAccess is not 'grant' or 'deny': grantx\",\"type\":\"com.basetechnology.s0.agentserver.webaccessmanager.WebAccessException\"}]}", accessJson.toString());

    accessJson = doPostJson(baseUrl + "/users/user1/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site1.com/\": \"\"}",
        400);
    assertTrue("JSON", accessJson != null);
    assertJsonSourceEquals("JSON error", "{\"errors\":[{\"message\":\"Empty verb parameter for addAccess\",\"type\":\"com.basetechnology.s0.agentserver.webaccessmanager.WebAccessException\"}]}", accessJson.toString());

    accessJson = doPostJson(baseUrl + "/users/user1/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site1.com/\": 4.5}",
        400);
    assertTrue("JSON", accessJson != null);
    assertJsonSourceEquals("JSON error", "{\"errors\":[{\"message\":\"Verb parameter for addAccess is not 'grant' or 'deny': 4.5\",\"type\":\"com.basetechnology.s0.agentserver.webaccessmanager.WebAccessException\"}]}", accessJson.toString());

    // Add the web site access controls for the scenario to be tested
    accessJson = doPostJson(baseUrl + "/users/*/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site1.com/\": \"grant\"}", 204);
    assertTrue("Access JSON map should not be returned", accessJson == null);

    accessJson = doPostJson(baseUrl + "/users/*/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site2.com\": \"deny\"}", 204);
    assertTrue("Access JSON map should not be returned", accessJson == null);

    accessJson = doPostJson(baseUrl + "/users/*/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site3.com/\": \"grant\"}", 204);
    assertTrue("Access JSON map should not be returned", accessJson == null);
    accessJson = doPostJson(baseUrl + "/users/user1/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site3.com/any/path\": \"deny\"}", 204);
    assertTrue("Access JSON map should not be returned", accessJson == null);
    accessJson = doPostJson(baseUrl + "/users/user2/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site3.com\": \"deny\"}", 204);
    assertTrue("Access JSON map should not be returned", accessJson == null);

    accessJson = doPostJson(baseUrl + "/users/*/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site4.com\": \"deny\"}", 204);
    assertTrue("Access JSON map should not be returned", accessJson == null);
    accessJson = doPostJson(baseUrl + "/users/user1/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site4.com\": \"grant\"}", 204);
    assertTrue("Access JSON map should not be returned", accessJson == null);
    accessJson = doPostJson(baseUrl + "/users/user2/website_access?password=" + server.agentServer.getAdminPassword(),
        "{\"http://www.site4.com\": \"grant\"}", 204);
    assertTrue("Access JSON map should not be returned", accessJson == null);

    // Validate that entries created as expected
    accessJson = doGetJson(baseUrl + "/users/*/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON",
        "{\"http://www.site1.com/\": \"grant\", \"http://www.site2.com/\": \"deny\", \"http://www.site3.com/\": \"grant\", \"http://www.site4.com/\": \"deny\"}", accessJson);

    accessJson = doGetJson(baseUrl + "/users/user1/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON",
        "{\"http://www.site3.com/\": \"deny\", \"http://www.site4.com/\": \"grant\"}", accessJson);

    accessJson = doGetJson(baseUrl + "/users/user2/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON",
        "{\"http://www.site3.com/\": \"deny\", \"http://www.site4.com/\": \"grant\"}", accessJson);

    accessJson = doGetJson(baseUrl + "/users/user3/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON", "{}", accessJson);

    accessJson = doGetJson(baseUrl + "/users/user4/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON", "{}", accessJson);

    // Now do full access check
    WebSiteAccessConfig wsac = server.agentServer.webSiteAccessConfig;
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user4"));
    
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user1"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user2"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user3"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user4"));
    
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site3, "user1"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site3, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site3, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site3, "user4"));
    
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site4, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site4, "user2"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site4, "user3"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site4, "user4"));

    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user4"));

    // Now turn on "implicit deny" option
    server.agentServer.webAccessConfig.setImplicitlyDenyWebAccess(true);

    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user4"));
    
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user1"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user2"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user3"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user4"));
    
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site3, "user1"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site3, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site3, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site3, "user4"));
    
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site4, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site4, "user2"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site4, "user3"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site4, "user4"));

    assertEquals("Access allowed", false, wsac.isAccessAllowed(site5, "user1"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site5, "user2"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site5, "user3"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site5, "user4"));

    // Now turn "implicit deny" back off
    server.agentServer.webAccessConfig.setImplicitlyDenyWebAccess(false);

    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user4"));
    
    // Check that web access control persists over shutdown and restart
    restartServer();

    accessJson = doGetJson(baseUrl + "/users/*/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON",
        "{\"http://www.site1.com/\": \"grant\", \"http://www.site2.com/\": \"deny\", \"http://www.site3.com/\": \"grant\", \"http://www.site4.com/\": \"deny\"}", accessJson);

    accessJson = doGetJson(baseUrl + "/users/user1/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON",
        "{\"http://www.site3.com/\": \"deny\", \"http://www.site4.com/\": \"grant\"}", accessJson);

    accessJson = doGetJson(baseUrl + "/users/user2/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON",
        "{\"http://www.site3.com/\": \"deny\", \"http://www.site4.com/\": \"grant\"}", accessJson);

    accessJson = doGetJson(baseUrl + "/users/user3/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON", "{}", accessJson);

    accessJson = doGetJson(baseUrl + "/users/user4/website_access?password=" + server.agentServer.getAdminPassword(), 200);
    assertTrue("Access JSON map not returned", accessJson != null);
    assertJsonSourceEquals("Access list JSON", "{}", accessJson);

    // Now do full access check
    wsac = server.agentServer.webSiteAccessConfig;
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user4"));
    
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user1"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user2"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user3"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user4"));
    
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site3, "user1"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site3, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site3, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site3, "user4"));
    
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site4, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site4, "user2"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site4, "user3"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site4, "user4"));

    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user4"));

  }

  @Test
  public void testAgentInstanceStatus() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;
    
    // Kick off an agent

    // Sleep a little to give system a chance to settle down so timing works out better
    // TODO: This still doesn't work 100% of the time - some delays elsewhere
    Thread.sleep(250);
    System.gc();
    
    long now = System.currentTimeMillis();
    
    // Create a test user
    doPostJson(baseUrl + "/users?id=test-user-1&password=test-pwd-1", "{}", 201);

    // Create one agent definition that says "Hello World" in its output
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"description\": \"Test agent definition\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}, {\"name\": \"field2\", \"type\": \"int\", \"default_value\": 123}, {\"name\": \"field3\", \"type\": \"float\", \"default_value\": 4.56}, {\"name\": \"field4\", \"type\": \"boolean\", \"default_value\": true}]}", 201);

    // Instantiate the agent definition once
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld.mine\", \"description\": \"Test agent\", \"definition\": \"HelloWorld\"}", 201);

    // Check the agent's status
    JSONObject statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld.mine/status?password=test-pwd-1", 200);
    assertTrue("Status not returned", statusJson != null);
    int countExpectedInstanceKeys = 16;
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "HelloWorld.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "HelloWorld", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "active", statusJson.get("status"));
    
    // TODO: Validate times in range
    
    // Now define an agent that gets an exception
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"Bad1\", \"description\": \"Test agent definition\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}, {\"name\": \"field2\", \"type\": \"int\", \"default_value\": 123}, {\"name\": \"field3\", \"type\": \"float\", \"default_value\": 4.56}, {\"name\": \"field4\", \"type\": \"boolean\", \"default_value\": true}], \"scripts\": [{\"name\": \"init\", \"script\": \"int i = 1, j = i / 0;\"}]}", 201);
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"Bad1.mine\", \"description\": \"Test agent\", \"definition\": \"Bad1\"}", 201);
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/Bad1.mine/status?password=test-pwd-1", 200);
/*    assertTrue("Status not returned", statusJson != null);
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "Bad1.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "Bad1", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "starting", statusJson.get("status"));
    */
    
    // Give agent a little time to start up and hit the exception
    // TODO: Figure out why it takes so long
    Thread.sleep(200);
    
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/Bad1.mine/status?password=test-pwd-1", 200);
    assertTrue("Status not returned", statusJson != null);
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "Bad1.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "Bad1", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "exception: / by zero", statusJson.get("status"));
    
    // Now dismiss the exceptions and see that status indicates this
    statusJson = doPutJson(baseUrl + "/users/test-user-1/agents/Bad1.mine/dismiss_exceptions?password=test-pwd-1", 204);
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/Bad1.mine/status?password=test-pwd-1", 200);
    assertTrue("Status not returned", statusJson != null);
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "Bad1.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "Bad1", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "active", statusJson.get("status"));
    
  }

  @Test
  public void testNotification() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;
    
    long now = System.currentTimeMillis();
    
    // Create a test user
    doPostJson(baseUrl + "/users?id=test-user-1&password=test-pwd-1", "{}", 201);

    // First test agent with no notifications
    
    // Create one agent definition that says "Hello World" in its output
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"test-user-1\", \"name\": \"HelloWorld\", \"description\": \"Test agent definition\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}, {\"name\": \"field2\", \"type\": \"int\", \"default_value\": 123}, {\"name\": \"field3\", \"type\": \"float\", \"default_value\": 4.56}, {\"name\": \"field4\", \"type\": \"boolean\", \"default_value\": true}]}", 201);

    // Instantiate the agent definition once
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"test-user-1\", \"name\": \"HelloWorld.mine\", \"description\": \"Test agent\", \"definition\": \"HelloWorld\"}", 201);

    // Check the agent's status
    JSONObject statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld.mine/status?password=test-pwd-1", 200);
    assertTrue("Status not returned", statusJson != null);
    int countExpectedInstanceKeys = 16;
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "HelloWorld.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "HelloWorld", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "active", statusJson.get("status"));
    
    // Check that agent definition has no notifications
    JSONObject definitionJson = doGetJson(baseUrl + "/users/test-user-1/agent_definitions/HelloWorld?password=test-pwd-1", 200);
    assertTrue("Notifications missing", definitionJson.has("notifications"));
    JSONArray notificationDefinitionsJson = definitionJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 0, notificationDefinitionsJson.length());
    
    // Check that notifications list and notification history are empty
    JSONObject instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld.mine?password=test-pwd-1&state=yes", 200);
    assertTrue("state missing", instanceJson.has("state"));
    JSONArray statesJson = instanceJson.getJSONArray("state");
    assertEquals("State history size", 1, statesJson.length());
    JSONObject stateJson = statesJson.getJSONObject(0);
    assertTrue("notifications missing", stateJson.has("notifications"));
    JSONArray notificationsJson = stateJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 0, notificationsJson.length());
    JSONArray notificationHistoryJson = stateJson.getJSONArray("notification_history");
    assertEquals("Notification history size", 0, notificationHistoryJson.length());

    now = System.currentTimeMillis();
    String nowString = DateUtils.toRfcString(now);
    
    // Now define an agent with two notifications
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld2\", \"description\": \"Test agent definition\", \"outputs\": [{\"name\": \"field1\", \"type\": \"string\", \"default_value\": \"Hello World\"}, {\"name\": \"field2\", \"type\": \"int\", \"default_value\": 123}, {\"name\": \"field3\", \"type\": \"float\", \"default_value\": 4.56}, {\"name\": \"field4\", \"type\": \"boolean\", \"default_value\": true}], " +
            "\"scripts\": [" +
            "  {\"name\": \"do_notify1\", \"script\": \"notify('Not1');\", \"public\": true}," +
            "  {\"name\": \"do_notify2\", \"script\": \"notifications.Not2.vendor = 'Abc Corp.'; Not2.price = 123.45; Not2.purchased = true; notify('Not2');\", \"public\": true}," +
            "  {\"name\": \"do_notify2a\", \"script\": \"notifications.Not2.vendor = 'Delta Ltd.'; Not2.price = 250.75; Not2.purchased = false; notify('Not2');\", \"public\": true}], " +
            "\"notifications\": [{\"name\": \"Not1\", \"type\": \"notify_only\", \"manual\": true}," +
            "{\"name\": \"Not2\", \"description\": \"Second notification\", \"type\": \"yes_no\", \"manual\": true, \"timeout\": \"minutes(90)\", \"scripts\": [{\"name\": \"accept\", \"script\": \"return 2+2;\"}], \"details\": [{\"name\": \"vendor\", \"type\": \"string\", \"default_value\": \"N/A\"}, {\"name\": \"price\", \"type\": \"float\", \"default_value\": 0.00}, {\"name\": \"purchased\", \"type\": \"boolean\", \"default_value\": false}]}]}", 201);

    // Instantiate the agent definition once
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld2.mine\", \"description\": \"Test agent\", \"definition\": \"HelloWorld2\"}", 201);

    // Check the agent's status
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/status?password=test-pwd-1", 200);
    assertTrue("Status not returned", statusJson != null);
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "HelloWorld2.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "HelloWorld2", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "active", statusJson.get("status"));
    
    // Check that agent definition has two notifications
    definitionJson = doGetJson(baseUrl + "/users/test-user-1/agent_definitions/HelloWorld2?password=test-pwd-1", 200);
    assertTrue("Notifications missing", definitionJson.has("notifications"));
    notificationDefinitionsJson = definitionJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 2, notificationDefinitionsJson.length());

    JSONObject notificationDefinitionJson = notificationDefinitionsJson.getJSONObject(0);
    assertEquals("Count of fields in notification definition[0]", 10, notificationDefinitionJson.length());
    assertTrue("Notification definition[0] name is missing", notificationDefinitionJson.has("name"));
    assertEquals("Notification definition[0] name", "Not1", notificationDefinitionJson.getString("name"));
    assertTrue("Notification definition[0] description is missing", notificationDefinitionJson.has("description"));
    assertEquals("Notification definition[0] description", "", notificationDefinitionJson.getString("description"));
    assertTrue("Notification definition[0] type is missing", notificationDefinitionJson.has("type"));
    assertEquals("Notification definition[0] type", "notify_only", notificationDefinitionJson.getString("type"));
    assertTrue("Notification definition[0] condition is missing", notificationDefinitionJson.has("condition"));
    assertEquals("Notification definition[0] condition", "", notificationDefinitionJson.getString("condition"));
    assertTrue("Notification definition[0] timeout is missing", notificationDefinitionJson.has("timeout"));
    assertEquals("Notification definition[0] timeout", "", notificationDefinitionJson.getString("timeout"));
    assertTrue("Notification definition[0] suspend is missing", notificationDefinitionJson.has("suspend"));
    assertEquals("Notification definition[0] suspend", true, notificationDefinitionJson.getBoolean("suspend"));
    assertTrue("Notification definition[0] manual is missing", notificationDefinitionJson.has("manual"));
    assertEquals("Notification definition[0] manual", true, notificationDefinitionJson.getBoolean("manual"));
    assertTrue("Notification definition[0] enabled is missing", notificationDefinitionJson.has("enabled"));
    assertEquals("Notification definition[0] enabled", true, notificationDefinitionJson.getBoolean("enabled"));
    assertTrue("Notification definition[0] details is missing", notificationDefinitionJson.has("details"));
    assertJsonSourceEquals("Notification definition[0] details", "[]", notificationDefinitionJson.getJSONArray("details"));
    assertTrue("Notification definition[0] scripts is missing", notificationDefinitionJson.has("scripts"));
    assertJsonSourceEquals("Notification definition[0] scripts", "[]", notificationDefinitionJson.getJSONArray("scripts"));
    assertJsonSourceEquals("JSON for Notification definition[0]", "{\"name\": \"Not1\", \"description\": \"\", \"timeout\": \"\", \"suspend\": true, \"type\": \"notify_only\", \"condition\": \"\", \"manual\": true, \"enabled\": true, \"details\": [], \"scripts\": []}", notificationDefinitionJson);

    notificationDefinitionJson = notificationDefinitionsJson.getJSONObject(1);
    assertEquals("Count of fields in notification definition[0]", 10, notificationDefinitionJson.length());
    assertTrue("Notification definition[0] name is missing", notificationDefinitionJson.has("name"));
    assertEquals("Notification definition[0] name", "Not2", notificationDefinitionJson.getString("name"));
    assertTrue("Notification definition[0] description is missing", notificationDefinitionJson.has("description"));
    assertEquals("Notification definition[0] description", "Second notification", notificationDefinitionJson.getString("description"));
    assertTrue("Notification definition[0] type is missing", notificationDefinitionJson.has("type"));
    assertEquals("Notification definition[0] type", "yes_no", notificationDefinitionJson.getString("type"));
    assertTrue("Notification definition[0] condition is missing", notificationDefinitionJson.has("condition"));
    assertEquals("Notification definition[0] condition", "", notificationDefinitionJson.getString("condition"));
    assertTrue("Notification definition[0] timeout is missing", notificationDefinitionJson.has("timeout"));
    assertEquals("Notification definition[0] timeout", "minutes(90)", notificationDefinitionJson.getString("timeout"));
    assertTrue("Notification definition[0] suspend is missing", notificationDefinitionJson.has("suspend"));
    assertEquals("Notification definition[0] suspend", true, notificationDefinitionJson.getBoolean("suspend"));
    assertTrue("Notification definition[0] manual is missing", notificationDefinitionJson.has("manual"));
    assertEquals("Notification definition[0] manual", true, notificationDefinitionJson.getBoolean("manual"));
    assertTrue("Notification definition[0] enabled is missing", notificationDefinitionJson.has("enabled"));
    assertEquals("Notification definition[0] enabled", true, notificationDefinitionJson.getBoolean("enabled"));
    assertTrue("Notification definition[0] details is missing", notificationDefinitionJson.has("details"));
    assertJsonSourceEquals("Notification definition[0] details", "[{\"default_value\":\"N/A\",\"name\":\"vendor\",\"type\":\"string\"},{\"name\":\"price\",\"max_value\":1.7976931348623157E308,\"type\":\"float\"},{\"name\":\"purchased\",\"type\":\"boolean\"}]", notificationDefinitionJson.getJSONArray("details"));
    assertTrue("Notification definition[0] scripts is missing", notificationDefinitionJson.has("scripts"));
    assertJsonSourceEquals("Notification definition[0] scripts", "[{\"execution_level\":0,\"description\":\"\",\"name\":\"accept\",\"parameters\":\"\",\"script\":\"return 2+2;\",\"public\":false}]", notificationDefinitionJson.getJSONArray("scripts"));
    assertJsonSourceEquals("JSON for Notification definition[0]", "{\"name\": \"Not2\", \"description\": \"Second notification\", \"timeout\": \"minutes(90)\", \"suspend\": true, \"type\": \"yes_no\", \"condition\": \"\", \"manual\": true, \"enabled\": true, \"details\": [{\"default_value\":\"N/A\",\"name\":\"vendor\",\"type\":\"string\"},{\"name\":\"price\",\"max_value\":1.7976931348623157E308,\"type\":\"float\"},{\"name\":\"purchased\",\"type\":\"boolean\"}], \"scripts\": [{\"execution_level\":0,\"description\":\"\",\"name\":\"accept\",\"parameters\":\"\",\"script\":\"return 2+2;\",\"public\":false}]}", notificationDefinitionJson);

    // Check that notifications list and notification history are empty
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine?password=test-pwd-1&state=yes", 200);
    assertTrue("state missing", instanceJson.has("state"));
    statesJson = instanceJson.getJSONArray("state");
    assertEquals("State history size", 1, statesJson.length());
    stateJson = statesJson.getJSONObject(0);
    assertTrue("notifications missing", stateJson.has("notifications"));
    notificationsJson = stateJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 2, notificationsJson.length());
    notificationHistoryJson = stateJson.getJSONArray("notification_history");
    assertEquals("Notification history size", 0, notificationHistoryJson.length());

    JSONObject notificationInstanceJson = notificationsJson.getJSONObject(0);
    assertEquals("Count of notification instance fields", 9, notificationInstanceJson.length());
    assertTrue("Notification instance[0] name is missing", notificationInstanceJson.has("name"));
    assertEquals("Notification instance[0] name", "Not1", notificationInstanceJson.getString("name"));
    assertTrue("Notification instance[0] details is missing", notificationInstanceJson.has("details"));
    assertJsonSourceEquals("Notification instance[0] details", "{}", notificationInstanceJson.get("details").toString());
    assertTrue("Notification instance[0] pending is missing", notificationInstanceJson.has("pending"));
    assertEquals("Notification instance[0] pending", false, notificationInstanceJson.getBoolean("pending"));
    assertTrue("Notification instance[0] timeout is missing", notificationInstanceJson.has("timeout"));
    assertEquals("Notification instance[0] timeout", 0, notificationInstanceJson.getLong("timeout"));
    assertTrue("Notification instance[0] response is missing", notificationInstanceJson.has("response"));
    assertEquals("Notification instance[0] response", "", notificationInstanceJson.getString("response"));
    assertTrue("Notification instance[0] response_choice is missing", notificationInstanceJson.has("response_choice"));
    assertEquals("Notification instance[0] response_choice", "", notificationInstanceJson.getString("response_choice"));
    assertTrue("Notification instance[0] comment is missing", notificationInstanceJson.has("comment"));
    assertEquals("Notification instance[0] comment", "", notificationInstanceJson.getString("comment"));
    assertTrue("Notification instance[0] time_notified is missing", notificationInstanceJson.has("time_notified"));
    assertEquals("Notification instance[0] time_notified", "", notificationInstanceJson.getString("time_notified"));
    assertTrue("Notification instance[0] time_response is missing", notificationInstanceJson.has("time_response"));
    assertEquals("Notification instance[0] time_response", "", notificationInstanceJson.getString("time_response"));

    notificationInstanceJson = notificationsJson.getJSONObject(1);
    assertEquals("Count of notification instance fields", 9, notificationInstanceJson.length());
    assertTrue("Notification instance[1] name is missing", notificationInstanceJson.has("name"));
    assertEquals("Notification instance[1] name", "Not2", notificationInstanceJson.getString("name"));
    assertTrue("Notification instance[1] details is missing", notificationInstanceJson.has("details"));
    assertJsonSourceEquals("Notification instance[1] details", "{\"vendor\": \"N/A\", \"price\": 0.00, \"purchased\": false}", notificationInstanceJson.get("details").toString());
    assertTrue("Notification instance[1] pending is missing", notificationInstanceJson.has("pending"));
    assertEquals("Notification instance[1] pending", false, notificationInstanceJson.getBoolean("pending"));
    assertTrue("Notification instance[1] timeout is missing", notificationInstanceJson.has("timeout"));
    assertEquals("Notification instance[1] timeout", 90 * 60 * 1000, notificationInstanceJson.getLong("timeout"));
    assertTrue("Notification instance[1] response is missing", notificationInstanceJson.has("response"));
    assertEquals("Notification instance[1] response", "", notificationInstanceJson.getString("response"));
    assertTrue("Notification instance[1] response_choice is missing", notificationInstanceJson.has("response_choice"));
    assertEquals("Notification instance[1] response_choice", "", notificationInstanceJson.getString("response_choice"));
    assertTrue("Notification instance[0] comment is missing", notificationInstanceJson.has("comment"));
    assertEquals("Notification instance[0] comment", "", notificationInstanceJson.getString("comment"));
    assertTrue("Notification instance[1] time_notified is missing", notificationInstanceJson.has("time_notified"));
    assertEquals("Notification instance[1] time_notified", "", notificationInstanceJson.getString("time_notified"));
    assertTrue("Notification instance[1] time_response is missing", notificationInstanceJson.has("time_response"));
    assertEquals("Notification instance[1] time_response", "", notificationInstanceJson.getString("time_response"));

    // Check pending notifications via REST API
    JSONObject pendingNotificationsObjectJson =
        doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/notifications?password=test-pwd-1", 200);
    assertTrue("Object not returned", pendingNotificationsObjectJson != null);
    assertTrue("pending_notifications is missing", pendingNotificationsObjectJson.has("pending_notifications"));
    JSONArray pendingNotificationsJson = pendingNotificationsObjectJson.getJSONArray("pending_notifications");
    assertEquals("Count of pending notifications", 0, pendingNotificationsJson.length());

    // Trigger a notification
    instanceJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/run_script/do_notify2?password=test-pwd-1&state=yes", 200);

    // Sleep a little since notification processing gets queued
    Thread.sleep(50);

    // Check to see that agent is now suspended waiting for notification
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/status?password=test-pwd-1", 200);
    assertTrue("Status not returned", statusJson != null);
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "HelloWorld2.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "HelloWorld2", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "notification_pending_suspended: Not2", statusJson.get("status"));

    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine?password=test-pwd-1&state=yes", 200);
    assertTrue("state missing", instanceJson.has("state"));
    statesJson = instanceJson.getJSONArray("state");
    assertEquals("State history size", 2, statesJson.length());

    stateJson = statesJson.getJSONObject(0);
    assertTrue("notifications missing", stateJson.has("notifications"));
    notificationsJson = stateJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 2, notificationsJson.length());
    notificationHistoryJson = stateJson.getJSONArray("notification_history");
    assertEquals("Notification history size", 0, notificationHistoryJson.length());

    stateJson = statesJson.getJSONObject(1);
    assertTrue("notifications missing", stateJson.has("notifications"));
    notificationsJson = stateJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 2, notificationsJson.length());
    notificationHistoryJson = stateJson.getJSONArray("notification_history");
    assertEquals("Notification history size", 1, notificationHistoryJson.length());
    
    // Check that detail info is as expected for the notification

    // First in the actual agent instance state
    JSONObject notificationJson = notificationsJson.getJSONObject(0);
    assertEquals("Count of fields in notification[0]", 9, notificationJson.length());
    assertTrue("name is missing from notification[0]", notificationJson.has("name"));
    assertEquals("name for notification[0]", "Not1", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[0]", notificationJson.has("time_notified"));
    assertEquals("time_notified for notification[0]", "", notificationJson.getString("time_notified"));
    assertTrue("time_response is missing from notification[0]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[0]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[0]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[0]", "{}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[0]", notificationJson.has("pending"));
    assertEquals("pending for notification[0]", false, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[0]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[0]", 0, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[0]", notificationJson.has("response"));
    assertEquals("response for notification[0]", "", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[0]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[0]", "", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[0]", notificationJson.has("comment"));
    assertEquals("comment for notification[0]", "", notificationJson.getString("comment"));

    notificationJson = notificationsJson.getJSONObject(1);
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    String timeString = notificationJson.getString("time_notified");
    long time = DateUtils.parseRfcString(timeString);
    long delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[1] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[1]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[1]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[1]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[1]", "{\"vendor\": \"Abc Corp.\", \"price\": 123.45, \"purchased\": true}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[1]", notificationJson.has("pending"));
    assertEquals("pending for notification[1]", true, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[1]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[1]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[1]", notificationJson.has("response"));
    assertEquals("response for notification[1]", "no_response", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[1]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[1]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[1]", notificationJson.has("comment"));
    assertEquals("comment for notification[1]", "", notificationJson.getString("comment"));

    // Now check it in the latest notification history
    JSONObject notificationRecordJson = notificationHistoryJson.getJSONObject(0);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 1, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[1] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[1]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[1]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[1]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[1]", "{\"vendor\": \"Abc Corp.\", \"price\": 123.45, \"purchased\": true}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[1]", notificationJson.has("pending"));
    assertEquals("pending for notification[1]", true, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[1]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[1]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[1]", notificationJson.has("response"));
    assertEquals("response for notification[1]", "no_response", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[1]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[1]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[1]", notificationJson.has("comment"));
    assertEquals("comment for notification[1]", "", notificationJson.getString("comment"));

    // Now query the current pending notification via the REST API
    pendingNotificationsObjectJson =
        doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/notifications?password=test-pwd-1", 200);
    assertTrue("Object not returned", pendingNotificationsObjectJson != null);
    assertEquals("Count of field in wrapped array", 1, pendingNotificationsObjectJson.length());
    assertTrue("pending_notifications is missing", pendingNotificationsObjectJson.has("pending_notifications"));
    pendingNotificationsJson = pendingNotificationsObjectJson.getJSONArray("pending_notifications");
    assertEquals("Count of pending notifications", 1, pendingNotificationsJson.length());
    JSONObject pendingNotificationJson = pendingNotificationsJson.getJSONObject(0);
    assertEquals("Count of fields in pending notification", 7, pendingNotificationJson.length());
    assertTrue("agent is missing from pending notification", pendingNotificationJson.has("agent"));
    assertEquals("agent of pending notification", "HelloWorld2.mine", pendingNotificationJson.getString("agent"));
    assertTrue("name is missing from pending notification", pendingNotificationJson.has("name"));
    assertEquals("name of pending notification", "Not2", pendingNotificationJson.getString("name"));
    assertTrue("description is missing from pending notification", pendingNotificationJson.has("description"));
    assertEquals("description of pending notification", "Second notification", pendingNotificationJson.getString("description"));
    assertTrue("type is missing from pending notification", pendingNotificationJson.has("type"));
    assertEquals("type of pending notification", "yes_no", pendingNotificationJson.getString("type"));
    assertTrue("time is missing from pending notification", pendingNotificationJson.has("time"));
    timeString = pendingNotificationJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for pending notification is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("timeout is missing from pending notification", pendingNotificationJson.has("timeout"));
    assertEquals("timeout of pending notification", 90 * 60 * 1000, pendingNotificationJson.getLong("timeout"));
    assertTrue("details is missing from pending notification", pendingNotificationJson.has("details"));
    assertJsonSourceEquals("details of pending notification", "{\"vendor\": \"Abc Corp.\", \"price\": 123.45, \"purchased\": true}", pendingNotificationJson.getJSONObject("details"));

    // Test error detection for invalid notification responses
    JSONObject returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not1", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Missing password query parameter");
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Missing password query parameter");
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not1?password=test-pwd-2", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Unknown user Id or invalid password");
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not1?password=test-pwd-1", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Missing response query parameter");
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not1?password=test-pwd-1&response=", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Empty response query parameter");
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not1?password=test-pwd-1&response=junk", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Unknown response keyword query parameter");
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not1?password=test-pwd-1&response=accept", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Cannot respond to notification 'Not1' for agent instance 'HelloWorld2.mine' since it is not pending");

    // Now manually respond to the notification via the REST API
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not2?password=test-pwd-1&response=accept", 204);
    assertNoError(returnJson);

    // Make sure no notifications are pending
    pendingNotificationsObjectJson =
        doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/notifications?password=test-pwd-1", 200);
    assertTrue("Object not returned", pendingNotificationsObjectJson != null);
    assertEquals("Count of field in wrapped array", 1, pendingNotificationsObjectJson.length());
    assertTrue("pending_notifications is missing", pendingNotificationsObjectJson.has("pending_notifications"));
    pendingNotificationsJson = pendingNotificationsObjectJson.getJSONArray("pending_notifications");
    assertEquals("Count of pending notifications", 0, pendingNotificationsJson.length());
    
    // Check to see that agent is now active again
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/status?password=test-pwd-1", 200);
    assertTrue("Status not returned", statusJson != null);
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "HelloWorld2.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "HelloWorld2", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "active", statusJson.get("status"));

    // Check latest notification history to see that response was stored properly
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine?password=test-pwd-1&state=yes", 200);
    assertTrue("state missing", instanceJson.has("state"));
    statesJson = instanceJson.getJSONArray("state");
    assertEquals("State history size", 4, statesJson.length());

    stateJson = statesJson.getJSONObject(3);
    assertTrue("notifications missing", stateJson.has("notifications"));
    notificationsJson = stateJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 2, notificationsJson.length());
    notificationHistoryJson = stateJson.getJSONArray("notification_history");
    assertEquals("Notification history size", 2, notificationHistoryJson.length());

    notificationRecordJson = notificationHistoryJson.getJSONObject(0);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 1, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[1] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[1]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[1]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[1]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[1]", "{\"vendor\": \"Abc Corp.\", \"price\": 123.45, \"purchased\": true}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[1]", notificationJson.has("pending"));
    assertEquals("pending for notification[1]", true, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[1]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[1]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[1]", notificationJson.has("response"));
    assertEquals("response for notification[1]", "no_response", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[1]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[1]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[1]", notificationJson.has("comment"));
    assertEquals("comment for notification[1]", "", notificationJson.getString("comment"));

    notificationRecordJson = notificationHistoryJson.getJSONObject(1);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 2, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[1] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[1]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[1]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[1]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[1]", "{\"vendor\": \"Abc Corp.\", \"price\": 123.45, \"purchased\": true}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[1]", notificationJson.has("pending"));
    assertEquals("pending for notification[1]", false, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[1]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[1]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[1]", notificationJson.has("response"));
    assertEquals("response for notification[1]", "accept", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[1]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[1]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[1]", notificationJson.has("comment"));
    assertEquals("comment for notification[1]", "", notificationJson.getString("comment"));

    // Make sure notification is really no longer pending
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not2?password=test-pwd-1&response=accept", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Cannot respond to notification 'Not2' for agent instance 'HelloWorld2.mine' since it is not pending");

    // Ditto, but using GET rather than PUT (ala email link response)
    returnJson = doGetJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not2?password=test-pwd-1&response=accept", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Cannot respond to notification 'Not2' for agent instance 'HelloWorld2.mine' since it is not pending");

    // Repeat notification, but with a "decline" response and a comment added
    instanceJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/run_script/do_notify2a?password=test-pwd-1", 200);
    
    // Wait for queued notification to be performed
    server.agentServer.agentScheduler.waitUntilDone(5 * 1000);
    
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/status?password=test-pwd-1", 200);
    assertEquals("Status", "notification_pending_suspended: Not2", statusJson.get("status"));

    pendingNotificationsObjectJson =
        doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/notifications?password=test-pwd-1", 200);
    assertTrue("Object not returned", pendingNotificationsObjectJson != null);
    assertEquals("Count of field in wrapped array", 1, pendingNotificationsObjectJson.length());
    assertTrue("pending_notifications is missing", pendingNotificationsObjectJson.has("pending_notifications"));
    pendingNotificationsJson = pendingNotificationsObjectJson.getJSONArray("pending_notifications");
    assertEquals("Count of pending notifications", 1, pendingNotificationsJson.length());
    pendingNotificationJson = pendingNotificationsJson.getJSONObject(0);
    assertEquals("Count of fields in pending notification", 7, pendingNotificationJson.length());
    assertTrue("agent is missing from pending notification", pendingNotificationJson.has("agent"));
    assertEquals("agent of pending notification", "HelloWorld2.mine", pendingNotificationJson.getString("agent"));
    assertTrue("name is missing from pending notification", pendingNotificationJson.has("name"));
    assertEquals("name of pending notification", "Not2", pendingNotificationJson.getString("name"));
    assertTrue("description is missing from pending notification", pendingNotificationJson.has("description"));
    assertEquals("description of pending notification", "Second notification", pendingNotificationJson.getString("description"));
    assertTrue("type is missing from pending notification", pendingNotificationJson.has("type"));
    assertEquals("type of pending notification", "yes_no", pendingNotificationJson.getString("type"));
    assertTrue("time is missing from pending notification", pendingNotificationJson.has("time"));
    timeString = pendingNotificationJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for pending notification is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("timeout is missing from pending notification", pendingNotificationJson.has("timeout"));
    assertEquals("timeout of pending notification", 90 * 60 * 1000, pendingNotificationJson.getLong("timeout"));
    assertTrue("details is missing from pending notification", pendingNotificationJson.has("details"));
    assertJsonSourceEquals("details of pending notification", "{\"vendor\": \"Delta Ltd.\", \"price\": 250.75, \"purchased\": false}", pendingNotificationJson.getJSONObject("details"));

    // Issue the response, including a user comment
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not2?password=test-pwd-1&response=decline&response_choice=Alpha&comment=This+is+a+simple+comment.", 204);
    assertNoError(returnJson);

    // Make sure no notifications are pending
    pendingNotificationsObjectJson =
        doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/notifications?password=test-pwd-1", 200);
    assertTrue("Object not returned", pendingNotificationsObjectJson != null);
    assertEquals("Count of field in wrapped array", 1, pendingNotificationsObjectJson.length());
    assertTrue("pending_notifications is missing", pendingNotificationsObjectJson.has("pending_notifications"));
    pendingNotificationsJson = pendingNotificationsObjectJson.getJSONArray("pending_notifications");
    assertEquals("Count of pending notifications", 0, pendingNotificationsJson.length());
    
    // Check to see that agent is now active again
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/status?password=test-pwd-1", 200);
    assertTrue("Status not returned", statusJson != null);
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "HelloWorld2.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "HelloWorld2", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "active", statusJson.get("status"));

    // Check latest notification history to see that response was stored properly
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine?password=test-pwd-1&state=yes", 200);
    assertTrue("state missing", instanceJson.has("state"));
    statesJson = instanceJson.getJSONArray("state");
    assertEquals("State history size", 7, statesJson.length());

    stateJson = statesJson.getJSONObject(6);
    assertTrue("notifications missing", stateJson.has("notifications"));
    notificationsJson = stateJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 2, notificationsJson.length());
    notificationHistoryJson = stateJson.getJSONArray("notification_history");
    assertEquals("Notification history size", 4, notificationHistoryJson.length());

    notificationRecordJson = notificationHistoryJson.getJSONObject(0);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 1, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[1] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[1]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[1]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[1]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[1]", "{\"vendor\": \"Abc Corp.\", \"price\": 123.45, \"purchased\": true}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[1]", notificationJson.has("pending"));
    assertEquals("pending for notification[1]", true, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[1]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[1]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[1]", notificationJson.has("response"));
    assertEquals("response for notification[1]", "no_response", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[1]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[1]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[1]", notificationJson.has("comment"));
    assertEquals("comment for notification[1]", "", notificationJson.getString("comment"));

    notificationRecordJson = notificationHistoryJson.getJSONObject(1);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 2, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[1] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[1]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[1]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[1]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[1]", "{\"vendor\": \"Abc Corp.\", \"price\": 123.45, \"purchased\": true}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[1]", notificationJson.has("pending"));
    assertEquals("pending for notification[1]", false, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[1]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[1]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[1]", notificationJson.has("response"));
    assertEquals("response for notification[1]", "accept", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[1]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[1]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[1]", notificationJson.has("comment"));
    assertEquals("comment for notification[1]", "", notificationJson.getString("comment"));

    notificationRecordJson = notificationHistoryJson.getJSONObject(2);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 3, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[2] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[2]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[2]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[2]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[2]", "{\"vendor\": \"Delta Ltd.\", \"price\": 250.75, \"purchased\": false}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[2]", notificationJson.has("pending"));
    assertEquals("pending for notification[2]", true, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[2]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[2]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[2]", notificationJson.has("response"));
    assertEquals("response for notification[2]", "no_response", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[2]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[2]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[2]", notificationJson.has("comment"));
    assertEquals("comment for notification[2]", "", notificationJson.getString("comment"));

    notificationRecordJson = notificationHistoryJson.getJSONObject(3);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 4, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[3] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[3]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[3]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[3]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[3]", "{\"vendor\": \"Delta Ltd.\", \"price\": 250.75, \"purchased\": false}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[3]", notificationJson.has("pending"));
    assertEquals("pending for notification[3]", false, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[3]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[3]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[3]", notificationJson.has("response"));
    assertEquals("response for notification[3]", "decline", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[3]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[3]", "Alpha", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[3]", notificationJson.has("comment"));
    assertEquals("comment for notification[3]", "This is a simple comment.", notificationJson.getString("comment"));

    // Make sure notification is really no longer pending
    returnJson = doPutJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not2?password=test-pwd-1&response=accept", 400);
    assertError(returnJson, "com.basetechnology.s0.agentserver.appserver.AgentAppServerBadRequestException",
        "Cannot respond to notification 'Not2' for agent instance 'HelloWorld2.mine' since it is not pending");

    // Now test the other notification which is a simple notify-only
    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/run_script/do_notify1?password=test-pwd-1", 200);
    assertNoError(returnJson);
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/status?password=test-pwd-1", 200);
    assertEquals("Status", "active", statusJson.get("status"));

    pendingNotificationsObjectJson =
        doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/notifications?password=test-pwd-1", 200);
    assertNoError(pendingNotificationsObjectJson);
    assertTrue("Object not returned", pendingNotificationsObjectJson != null);
    assertEquals("Count of field in wrapped array", 1, pendingNotificationsObjectJson.length());
    assertTrue("pending_notifications is missing", pendingNotificationsObjectJson.has("pending_notifications"));
    pendingNotificationsJson = pendingNotificationsObjectJson.getJSONArray("pending_notifications");
    assertEquals("Count of pending notifications", 0, pendingNotificationsJson.length());

    // Check latest notification history to see that notify-only notification was stored properly
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine?password=test-pwd-1&state=yes", 200);
    assertTrue("state missing", instanceJson.has("state"));
    statesJson = instanceJson.getJSONArray("state");
    assertEquals("State history size", 9, statesJson.length());

    stateJson = statesJson.getJSONObject(8);
    assertTrue("notifications missing", stateJson.has("notifications"));
    notificationsJson = stateJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 2, notificationsJson.length());
    notificationHistoryJson = stateJson.getJSONArray("notification_history");
    assertEquals("Notification history size", 5, notificationHistoryJson.length());

    notificationRecordJson = notificationHistoryJson.getJSONObject(0);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 1, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[1] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[1]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[1]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[1]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[1]", "{\"vendor\": \"Abc Corp.\", \"price\": 123.45, \"purchased\": true}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[1]", notificationJson.has("pending"));
    assertEquals("pending for notification[1]", true, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[1]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[1]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[1]", notificationJson.has("response"));
    assertEquals("response for notification[1]", "no_response", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[1]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[1]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[1]", notificationJson.has("comment"));
    assertEquals("comment for notification[1]", "", notificationJson.getString("comment"));

    notificationRecordJson = notificationHistoryJson.getJSONObject(1);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 2, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[1] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("time_response is missing from notification[1]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[1]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[1]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[1]", "{\"vendor\": \"Abc Corp.\", \"price\": 123.45, \"purchased\": true}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[1]", notificationJson.has("pending"));
    assertEquals("pending for notification[1]", false, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[1]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[1]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[1]", notificationJson.has("response"));
    assertEquals("response for notification[1]", "accept", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[1]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[1]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[1]", notificationJson.has("comment"));
    assertEquals("comment for notification[1]", "", notificationJson.getString("comment"));

    notificationRecordJson = notificationHistoryJson.getJSONObject(2);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 3, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[2] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("time_response is missing from notification[2]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[2]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[2]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[2]", "{\"vendor\": \"Delta Ltd.\", \"price\": 250.75, \"purchased\": false}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[2]", notificationJson.has("pending"));
    assertEquals("pending for notification[2]", true, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[2]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[2]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[2]", notificationJson.has("response"));
    assertEquals("response for notification[2]", "no_response", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[2]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[2]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[2]", notificationJson.has("comment"));
    assertEquals("comment for notification[2]", "", notificationJson.getString("comment"));

    notificationRecordJson = notificationHistoryJson.getJSONObject(3);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 4, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[3] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("time_response is missing from notification[3]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[3]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[3]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[3]", "{\"vendor\": \"Delta Ltd.\", \"price\": 250.75, \"purchased\": false}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[3]", notificationJson.has("pending"));
    assertEquals("pending for notification[3]", false, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[3]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[3]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[3]", notificationJson.has("response"));
    assertEquals("response for notification[3]", "decline", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[3]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[3]", "Alpha", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[3]", notificationJson.has("comment"));
    assertEquals("comment for notification[3]", "This is a simple comment.", notificationJson.getString("comment"));

    notificationRecordJson = notificationHistoryJson.getJSONObject(4);
    assertEquals("Count of fields in notification record [0]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [0]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 5, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [0]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[0] is not in range: " + delta, delta >= 0 && delta <= 5000);
    assertTrue("notification is missing from notification record [0]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[1]", 9, notificationJson.length());
    assertTrue("name is missing from notification[1]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not1", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[1]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[3] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("time_response is missing from notification[3]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[3]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[3]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[3]", "{}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[3]", notificationJson.has("pending"));
    assertEquals("pending for notification[3]", false, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[3]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[3]", 0, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[3]", notificationJson.has("response"));
    assertEquals("response for notification[3]", "no_response", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[3]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[3]", "no_choice", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[3]", notificationJson.has("comment"));
    assertEquals("comment for notification[3]", "", notificationJson.getString("comment"));

    // Now test responding using GET
    // Repeat notification, but with a "pass" response and a different comment added
    instanceJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/run_script/do_notify2a?password=test-pwd-1", 200);
    
    // Wait for queued notification to be performed
    server.agentServer.agentScheduler.waitUntilDone(5 * 1000);
    
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/status?password=test-pwd-1", 200);
    assertEquals("Status", "notification_pending_suspended: Not2", statusJson.get("status"));

    pendingNotificationsObjectJson =
        doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/notifications?password=test-pwd-1", 200);
    assertTrue("Object not returned", pendingNotificationsObjectJson != null);
    assertEquals("Count of field in wrapped array", 1, pendingNotificationsObjectJson.length());
    assertTrue("pending_notifications is missing", pendingNotificationsObjectJson.has("pending_notifications"));
    pendingNotificationsJson = pendingNotificationsObjectJson.getJSONArray("pending_notifications");
    assertEquals("Count of pending notifications", 1, pendingNotificationsJson.length());
    pendingNotificationJson = pendingNotificationsJson.getJSONObject(0);
    assertEquals("Count of fields in pending notification", 7, pendingNotificationJson.length());
    assertTrue("agent is missing from pending notification", pendingNotificationJson.has("agent"));
    assertEquals("agent of pending notification", "HelloWorld2.mine", pendingNotificationJson.getString("agent"));
    assertTrue("name is missing from pending notification", pendingNotificationJson.has("name"));
    assertEquals("name of pending notification", "Not2", pendingNotificationJson.getString("name"));
    assertTrue("description is missing from pending notification", pendingNotificationJson.has("description"));
    assertEquals("description of pending notification", "Second notification", pendingNotificationJson.getString("description"));
    assertTrue("type is missing from pending notification", pendingNotificationJson.has("type"));
    assertEquals("type of pending notification", "yes_no", pendingNotificationJson.getString("type"));
    assertTrue("time is missing from pending notification", pendingNotificationJson.has("time"));
    timeString = pendingNotificationJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for pending notification is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("timeout is missing from pending notification", pendingNotificationJson.has("timeout"));
    assertEquals("timeout of pending notification", 90 * 60 * 1000, pendingNotificationJson.getLong("timeout"));
    assertTrue("details is missing from pending notification", pendingNotificationJson.has("details"));
    assertJsonSourceEquals("details of pending notification", "{\"vendor\": \"Delta Ltd.\", \"price\": 250.75, \"purchased\": false}", pendingNotificationJson.getJSONObject("details"));

    // Check GET for specific notification
    pendingNotificationJson =
        doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not2?password=test-pwd-1", 200);
    assertTrue("Object not returned", pendingNotificationJson != null);
    assertEquals("Count of field in wrapped array", 7, pendingNotificationJson.length());
    assertTrue("pending_notifications is missing", pendingNotificationsObjectJson.has("pending_notifications"));
    pendingNotificationsJson = pendingNotificationsObjectJson.getJSONArray("pending_notifications");
    assertEquals("Count of pending notifications", 1, pendingNotificationsJson.length());
    pendingNotificationJson = pendingNotificationsJson.getJSONObject(0);
    assertEquals("Count of fields in pending notification", 7, pendingNotificationJson.length());
    assertTrue("agent is missing from pending notification", pendingNotificationJson.has("agent"));
    assertEquals("agent of pending notification", "HelloWorld2.mine", pendingNotificationJson.getString("agent"));
    assertTrue("name is missing from pending notification", pendingNotificationJson.has("name"));
    assertEquals("name of pending notification", "Not2", pendingNotificationJson.getString("name"));
    assertTrue("description is missing from pending notification", pendingNotificationJson.has("description"));
    assertEquals("description of pending notification", "Second notification", pendingNotificationJson.getString("description"));
    assertTrue("type is missing from pending notification", pendingNotificationJson.has("type"));
    assertEquals("type of pending notification", "yes_no", pendingNotificationJson.getString("type"));
    assertTrue("time is missing from pending notification", pendingNotificationJson.has("time"));
    timeString = pendingNotificationJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for pending notification is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("timeout is missing from pending notification", pendingNotificationJson.has("timeout"));
    assertEquals("timeout of pending notification", 90 * 60 * 1000, pendingNotificationJson.getLong("timeout"));
    assertTrue("details is missing from pending notification", pendingNotificationJson.has("details"));
    assertJsonSourceEquals("details of pending notification", "{\"vendor\": \"Delta Ltd.\", \"price\": 250.75, \"purchased\": false}", pendingNotificationJson.getJSONObject("details"));

    // Issue the response, including a user comment
    returnJson = doGetJson(baseUrl +
        "/users/test-user-1/agents/HelloWorld2.mine/notifications/Not2?password=test-pwd-1&response=pass&response_choice=Beta&comment=This+is+another+comment.", 204);
    assertNoError(returnJson);

    // Make sure no notifications are pending
    pendingNotificationsObjectJson =
        doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/notifications?password=test-pwd-1", 200);
    assertTrue("Object not returned", pendingNotificationsObjectJson != null);
    assertEquals("Count of field in wrapped array", 1, pendingNotificationsObjectJson.length());
    assertTrue("pending_notifications is missing", pendingNotificationsObjectJson.has("pending_notifications"));
    pendingNotificationsJson = pendingNotificationsObjectJson.getJSONArray("pending_notifications");
    assertEquals("Count of pending notifications", 0, pendingNotificationsJson.length());
    
    // Check to see that agent is now active again
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine/status?password=test-pwd-1", 200);
    assertTrue("Status not returned", statusJson != null);
    assertEquals("Count of status fields", countExpectedInstanceKeys, statusJson.length());
    assertTrue("Name field is missing", statusJson.has("name"));
    assertEquals("Name", "HelloWorld2.mine", statusJson.get("name"));
    assertTrue("Definition field is missing", statusJson.has("definition"));
    assertEquals("Definition", "HelloWorld2", statusJson.get("definition"));
    assertTrue("Description field is missing", statusJson.has("description"));
    assertEquals("Description", "Test agent", statusJson.get("description"));
    assertTrue("Status field is missing", statusJson.has("status"));
    assertEquals("Status", "active", statusJson.get("status"));

    // Check latest notification history to see that response was stored properly
    instanceJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld2.mine?password=test-pwd-1&state=yes", 200);
    assertTrue("state missing", instanceJson.has("state"));
    statesJson = instanceJson.getJSONArray("state");
    assertEquals("State history size", 10, statesJson.length());

    stateJson = statesJson.getJSONObject(9);
    assertTrue("notifications missing", stateJson.has("notifications"));
    notificationsJson = stateJson.getJSONArray("notifications");
    assertEquals("Count of notifications", 2, notificationsJson.length());
    notificationHistoryJson = stateJson.getJSONArray("notification_history");
    assertEquals("Notification history size", 7, notificationHistoryJson.length());


    notificationRecordJson = notificationHistoryJson.getJSONObject(6);
    assertEquals("Count of fields in notification record [6]", 3, notificationRecordJson.length());
    assertTrue("sequence is missing from notification record [6]", notificationRecordJson.has("sequence"));
    assertEquals("sequence", 7, notificationRecordJson.getInt("sequence"));
    assertTrue("time is missing from notification record [6]", notificationRecordJson.has("time"));
    timeString = notificationRecordJson.getString("time");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification record[6] is not in range: " + delta, delta >= 0 && delta < 5000);
    assertTrue("notification is missing from notification record [6]", notificationRecordJson.has("notification"));
    notificationJson = notificationRecordJson.getJSONObject("notification");
    assertEquals("Count of field in notification[6]", 9, notificationJson.length());
    assertTrue("name is missing from notification[6]", notificationJson.has("name"));
    assertEquals("name for notification[1]", "Not2", notificationJson.getString("name"));
    assertTrue("time_notified is missing from notification[6]", notificationJson.has("time_notified"));
    timeString = notificationJson.getString("time_notified");
    time = DateUtils.parseRfcString(timeString);
    delta = time - now/1000*1000;
    assertTrue("Delta time for time_notified for notification[6] is not in range: " + delta, delta >= 0 && delta < 4000);
    assertTrue("time_response is missing from notification[6]", notificationJson.has("time_response"));
    assertEquals("time_response for notification[3]", "", notificationJson.getString("time_response"));
    assertTrue("details is missing from notification[6]", notificationJson.has("details"));
    assertJsonSourceEquals("details for notification[6]", "{\"vendor\": \"Delta Ltd.\", \"price\": 250.75, \"purchased\": false}", notificationJson.getString("details"));
    assertTrue("pending is missing from notification[6]", notificationJson.has("pending"));
    assertEquals("pending for notification[6]", false, notificationJson.getBoolean("pending"));
    assertTrue("timeout is missing from notification[6]", notificationJson.has("timeout"));
    assertEquals("timeout for notification[6]", 90 * 60 * 1000, notificationJson.getLong("timeout"));
    assertTrue("response is missing from notification[6]", notificationJson.has("response"));
    assertEquals("response for notification[6]", "pass", notificationJson.getString("response"));
    assertTrue("response_choice is missing from notification[6]", notificationJson.has("response_choice"));
    assertEquals("response_choice for notification[6]", "Beta", notificationJson.getString("response_choice"));
    assertTrue("comment is missing from notification[6]", notificationJson.has("comment"));
    assertEquals("comment for notification[3]", "This is another comment.", notificationJson.getString("comment"));

    // TODO: Test suspend=false
    
    // TODO: Test timeout
    
    // TODO: Test via email notification
    
    // TODO: Test agent to agent notification
    
    // TODO: Test notify_only
    
    // TODO: Test confirm only - no suspend
    
    // TODO: Test 2 notify-only plus one suspend
  }


  @Test
  public void testAgentDefinitionParseErrors() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create a test user
    doPostJson(baseUrl + "/users?id=test-user-1&password=test-pwd-1", "{}", 201);

    // Test definition with timer expression syntax error
    JSONObject returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"timers\": [{\"name\": \"t1\", \"interval\": \"5*\", \"script\": \";\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    Object errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    JSONArray errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    JSONObject errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing timer 't1' interval expression \"5*\" - Expected expression primary, but found: EndToken", errorJson.getString("message"));

    // Test definition with timer expression semantic error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"timers\": [{\"name\": \"t1\", \"interval\": \"x*\", \"script\": \";\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing timer 't1' interval expression \"x*\" - No definition for symbol 'x' for any category", errorJson.getString("message"));

    // Test definition with timer script syntax error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"timers\": [{\"name\": \"t1\", \"interval\": \"100\", \"script\": \"5*;\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing timer 't1' script \"5*;\" - Expected expression primary, but found: SemicolonOperatorToken", errorJson.getString("message"));

    // Test definition with timer script semantic error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"timers\": [{\"name\": \"t1\", \"interval\": \"100\", \"script\": \"x*;\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing timer 't1' script \"x*;\" - No definition for symbol 'x' for any category", errorJson.getString("message"));

    // Test definition with script syntax error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"scripts\": [{\"name\": \"init\", \"script\": \"5*;\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing 'init' script \"5*;\" - Expected expression primary, but found: SemicolonOperatorToken", errorJson.getString("message"));

    // Test definition with script semantic error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"scripts\": [{\"name\": \"init\", \"script\": \"x*;\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing 'init' script \"x*;\" - No definition for symbol 'x' for any category", errorJson.getString("message"));

    // Test definition with notification script syntax error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"notifications\": [{\"name\": \"test\", \"scripts\": [{\"name\": \"yes\", \"script\": \"5*;\"}]}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing notification 'test' 'yes' script \"5*;\" - Expected expression primary, but found: SemicolonOperatorToken", errorJson.getString("message"));

    // Test definition with notification script semantic error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"notifications\": [{\"name\": \"test\", \"scripts\": [{\"name\": \"yes\", \"script\": \"x*;\"}]}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing notification 'test' 'yes' script \"x*;\" - No definition for symbol 'x' for any category", errorJson.getString("message"));

    // Test definition with notification timeout syntax error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"notifications\": [{\"name\": \"test\", \"timeout\": \"5*\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing notification 'test' timeout expression \"5*\" - Expected expression primary, but found: EndToken", errorJson.getString("message"));

    // Test definition with notification timeout semantic error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"notifications\": [{\"name\": \"test\", \"timeout\": \"x*\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing notification 'test' timeout expression \"x*\" - No definition for symbol 'x' for any category", errorJson.getString("message"));

    // Test definition with notification condition syntax error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"notifications\": [{\"name\": \"test\", \"condition\": \"5*\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing notification 'test' condition expression \"5*\" - Expected expression primary, but found: EndToken", errorJson.getString("message"));

    // Test definition with notification timeout semantic error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"notifications\": [{\"name\": \"test\", \"condition\": \"x*\"}]}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing notification 'test' condition expression \"x*\" - No definition for symbol 'x' for any category", errorJson.getString("message"));
    
    // Test definition with trigger interval syntax error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"trigger_interval\": \"5*\"}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing trigger_interval expression \"5*\" - Expected expression primary, but found: EndToken", errorJson.getString("message"));

    // Test definition with trigger interval semantic error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"trigger_interval\": \"x*\"}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing trigger_interval expression \"x*\" - No definition for symbol 'x' for any category", errorJson.getString("message"));

    // Test definition with reporting interval syntax error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"reporting_interval\": \"5*\"}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing reporting_interval expression \"5*\" - Expected expression primary, but found: EndToken", errorJson.getString("message"));

    // Test definition with reporting interval semantic error
    returnJson = doPostJson(
        baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"reporting_interval\": \"x*\"}", 400);
    assertTrue("Return JSON is empty", returnJson != null);
    assertTrue("Return JSON is missing 'errors'", returnJson.has("errors"));
    errorsObject = returnJson.get("errors");
    assertTrue("Errors value is not a JSONArray: " + errorsObject.getClass().getSimpleName(), errorsObject instanceof JSONArray);
    errorsArrayJson = (JSONArray)errorsObject;
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Error JSON is missing 'type'", errorJson.has("type"));
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.AgentServerException", errorJson.getString("type"));
    assertTrue("Error JSON is missing 'message'", errorJson.has("message"));
    assertEquals("Exception message", "ParserException parsing reporting_interval expression \"x*\" - No definition for symbol 'x' for any category", errorJson.getString("message"));

  }
  
  @Test
  public void testApiUsage() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Test reading of API usage summary text
    String url = baseUrl + "/usage";
    String usageText = doGet(url, 200);
    assertTrue("Usage text is missing", usageText != null);
    int usageTextLen = usageText.length();
    assertTrue("Usage text length is too small: " + usageTextLen, usageTextLen > 1000);
    String prefix = usageText.substring(0, 312);
    assertEquals("Usage text prefix (first 312 chars)", " * Copyright 2012 John W. Krupansky d/b/a Base Technology\r\n * Licensed under the Apache License, Version 2.0\r\n\r\nREST API Usage Summary for Base Technology Agent Server\r\n\r\nAPI Version 0.1\r\n\r\nGET http://localhost:8980/API/v0.1/about\r\n\r\n - Summarize the agent server\r\n \r\nGET http://localhost:8980/API/v0.1/config?pa", prefix);
  }

  @Test
  public void testMemoryListInit() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create a test user
    doPostJson(baseUrl + "/users?id=test-user-1&password=test-pwd-1", "{}", 201);

    // Create one agent definition that says "Hello World" in its output
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\"," +
            "\"memory\": [" +
            " {\"name\": \"m1\", \"type\": \"list\", \"compute\": \"['abc', 'def', 123, true]\"}, " +
            " {\"name\": \"m2\", \"type\": \"map\", \"compute\": \"{name: 'John Doe', address: 'Here', phone: 123}\"}], " +
            "\"outputs\": [" +
            "  {\"name\": \"field1\", \"type\": \"list\", \"compute\": \"m1\"}, " +
            "  {\"name\": \"field2\", \"type\": \"map\", \"compute\": \"m2\"}]}", 201);

    // Instantiate the agent definition once
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"definition\": \"HelloWorld\"}", 201);

    // Get the agent instance's output as JSON
    JSONObject outputJson = doGetJson(baseUrl + "/users/test-user-1/agents/HelloWorld/output?password=test-pwd-1", 200);
    assertTrue("Output JSON is missing", outputJson != null);
    assertEquals("Count of fields in output", 2, outputJson.length());
    assertTrue("Output field1 is missing", outputJson.has("field1"));
    assertJsonSourceEquals("Output field1", "[\"abc\",\"def\",\"123\",\"true\"]", outputJson.get("field1").toString());
    assertTrue("Output field2 is missing", outputJson.has("field2"));
    assertJsonSourceEquals("Output field2", "{\"name\": \"John Doe\", \"address\": \"Here\", \"phone\": 123}", outputJson.get("field2").toString());
    // TODO: 123 and true should not be strings!
    assertJsonSourceEquals("Output JSON", "{\"field1\":[\"abc\",\"def\",\"123\",\"true\"], \"field2\": {\"name\": \"John Doe\", \"address\": \"Here\", \"phone\": 123}}", outputJson.toString());

    // Get the agent instance's output as XML
    String outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.xml?password=test-pwd-1", 200);
    assertTrue("Output XML is missing", outputString != null);
    assertEquals("Output XML", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><field1>[abc, def, 123, true]</field1><field2><address>Here</address><name>John Doe</name><phone>123</phone></field2>", outputString.trim());

    // Get the agent instance's output as plain text
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.text?password=test-pwd-1", 200);
    assertTrue("Output XML is missing", outputString != null);
    assertEquals("Output plain text", "[abc, def, 123, true] {\"name\":\"John Doe\",\"address\":\"Here\",\"phone\":123}", outputString.trim());

    // Get the agent instance's output as CSV
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.csv?password=test-pwd-1", 200);
    assertTrue("Output CSV is missing", outputString != null);
    assertEquals("Output CSV", "\"[abc, def, 123, true]\",\"{\"name\":\"John Doe\",\"address\":\"Here\",\"phone\":123}\"", outputString.trim());

    // Get the agent instance's output as tab-delimited
    outputString = doGet(baseUrl + "/users/test-user-1/agents/HelloWorld/output.tab?password=test-pwd-1", 200);
    assertTrue("Output tab-delimited text is missing", outputString != null);
    assertEquals("Output tab-delimited text", "[abc, def, 123, true]\t{\"name\":\"John Doe\",\"address\":\"Here\",\"phone\":123}", outputString.trim());

  }

  @Test
  public void testRunScript() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create a test user
    doPostJson(baseUrl + "/users?id=test-user-1&password=test-pwd-1", "{}", 201);

    // Create one agent definition with some scripts (functions)
    doPostJson(baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\"," +
            "\"memory\": [" +
            " {\"name\": \"p\", \"type\": \"int\", \"default_value\": 123}, " +
            " {\"name\": \"q\", \"type\": \"float\", \"default_value\": 456.25}, " +
            " {\"name\": \"r\", \"type\": \"string\", \"default_value\": \"abc\"}], " +
            "\"scripts\": [" +
            "  {\"name\": \"f\", \"script\": \"f(int x, int y){return x + y;}\", \"public\": true}, " +
            "  {\"name\": \"g\", \"script\": \"g(object x, object y){return min(x, y);}\", \"public\": true}, " +
            "  {\"name\": \"h\", \"script\": \"string h(object x, object y){return f(x, y) + ' ' + g(x, y);}\", \"public\": true}, " +
            "  {\"name\": \"get_p\", \"script\": \"int get_p(){return p;}\", \"public\": true}, " +
            "  {\"name\": \"set_p\", \"script\": \"int get_p(object new_p){return p = new_p;}\", \"public\": true}, " +
            "  {\"name\": \"get_q\", \"script\": \"float get_q(){return q;}\", \"public\": true}, " +
            "  {\"name\": \"get_r\", \"script\": \"string get_r(){return r;}\", \"public\": true} " +
            "  ]}", 201);

    // Instantiate the agent definition once
    doPostJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1",
        "{\"user\": \"Test-User\", \"name\": \"HelloWorld\", \"definition\": \"HelloWorld\"}", 201);
    
    // Call function and check return value
    JSONObject returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/get_p?password=test-pwd-1", 200);
    assertJsonSourceEquals("Return value JSON", "{\"return_value\": 123}", returnJson);

    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/f?password=test-pwd-1&arg=98&arg=76", 200);
    assertJsonSourceEquals("Return value JSON", "{\"return_value\": 174}", returnJson);

    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/f?password=test-pwd-1&arg=98.125&arg=76.25", 200);
    assertJsonSourceEquals("Return value JSON", "{\"return_value\": 174.375}", returnJson);

    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/f?password=test-pwd-1&arg=false&arg=false", 200);
    assertJsonSourceEquals("Return value JSON", "{\"return_value\": false}", returnJson);
    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/f?password=test-pwd-1&arg=false&arg=true", 200);
    assertJsonSourceEquals("Return value JSON", "{\"return_value\": true}", returnJson);
    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/f?password=test-pwd-1&arg=true&arg=false", 200);
    assertJsonSourceEquals("Return value JSON", "{\"return_value\": true}", returnJson);
    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/f?password=test-pwd-1&arg=true&arg=true", 200);
    assertJsonSourceEquals("Return value JSON", "{\"return_value\": true}", returnJson);

    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/f?password=test-pwd-1&arg=%22abc%22&arg=%22def%22", 200);
    assertJsonSourceEquals("Return value JSON", "{\"return_value\": \"abcdef\"}", returnJson);

    // TODO: Test list and map arguments
    
    // Test wrong number of arguments
    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/f?password=test-pwd-1&arg=98", 400);
    assertJsonSourceEquals("Return value JSON", "{\"errors\": [{\"type\": \"com.basetechnology.s0.agentserver.AgentServerException\", \"message\": \"Incorrect number of arguments for function 'f' - expected 2 but got 1\"}]}", returnJson);

    // Test call of user function that calls two other user functions
    returnJson = doPutJson(baseUrl + "/users/test-user-1/agents/HelloWorld/run_script/h?password=test-pwd-1&arg=123&arg=456", 200);
    assertJsonSourceEquals("Return value JSON", "{\"return_value\": \"579 123\"}", returnJson);

  }
  
}
