package com.basetechnology.s0.agentserver;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.util.DateUtils;

public class AgentServerBugTest extends AgentServerTestBase {

  @Test
  public void testAgAgentBug() throws Exception {
    // Setup common info
    String baseUrl = AgentAppServer.appServerApiBaseUrl;

    // Create one user
    String url = baseUrl + "/users?id=test-user-1&password=test-pwd-1";
    JSONObject returnJson = doPostJson(url, "{}", 201);
    
    // Create agent #1
    url = baseUrl + "/users/test-user-1/agent_definitions?password=test-pwd-1";
    returnJson = doPostJson(url, "{" +
        "\"name\": \"Agent1\"," +
        "\"memory\": [{\"name\": \"counter\", \"type\": \"int\"}]," +
        "\"timers\": [{\"name\": \"count\", \"interval\": \"seconds(3/4.0)\", \"script\": \"counter++;\"}]," +
        "\"outputs\": [{\"name\": \"outCounter\", \"type\": \"int\", \"compute\": \"counter\"}]" +
        "}", 201);
    
    // Create agent #2
    returnJson = doPostJson(url, "{" +
        " \"name\": \"Agent2\"," +
        " \"memory\": [{\"name\": \"str\", \"type\": \"string\"}]," +
        " \"timers\": [{\"name\": \"count\", \"interval\": \"seconds(2/4.0)\", \"script\": \"str += 'a';\"}]," +
        " \"outputs\": [{\"name\": \"outStr\", \"type\": \"string\", \"compute\": \"str\"}]" +
        "}", 201);

    // Create agent #3
    returnJson = doPostJson(url, "{" +
        " \"name\": \"Agent3\"," +
        " \"timers\": [{\"name\": \"count\", \"interval\": \"seconds(1.5/4.0)\"}]," +
        " \"outputs\": [{\"name\": \"time\", \"type\": \"string\", \"compute\": \"now.toDate\"}]" +
        "}", 201);

    // Create the aggregating agent that combines the output of all three
    returnJson = doPostJson(url, "{" +
        " \"name\": \"Agent\"," +
        " \"inputs\":[" +
        "   {\"name\": \"input1\", \"data_source\": \"Agent1\"}," +
        "   {\"name\": \"input2\", \"data_source\": \"Agent2\"}," +
        "   {\"name\": \"input3\", \"data_source\": \"Agent3\"}]," +
        " \"outputs\": [" +
        "   {\"name\": \"outCounter\", \"type\": \"int\", \"compute\": \"input1.outCounter\"}," +
        "   {\"name\": \"outStr\", \"type\": \"string\", \"compute\": \"input2.outStr\"}," +
        "   {\"name\": \"time\", \"type\": \"string\", \"compute\": \"input3.time\"}," +
        "   {\"name\": \"all3\", \"type\": \"string\"," +
        "     \"compute\": \"'Time is ' + input3.time + ', count is ' + input1.outCounter + ' string is ' + input2.outStr\"}]," +
        "   \"trigger_interval\": \"100\"," +
        "   }", 201);

    // Now instantiate the aggregating agent
    // Note: Originally, this would result in an NPE.
    url = baseUrl + "/users/test-user-1/agents?password=test-pwd-1";
    returnJson = doPostJson(url, "{\"name\": \"MyAgent-1\", \"definition\": \"Agent\", \"public_output\": \"true\"}", 201);
    
    // Give agent a little time to run and generate at least several output values
    Thread.sleep(2000);
    
    // Check output values of aggregating agent
    long now = System.currentTimeMillis();
    JSONObject outputJson = doGetJson(baseUrl + "/users/test-user-1/agents/MyAgent-1/output", 200);
    assertTrue("Output return JSON is missing", outputJson != null);
    assertEquals("Count of fields in output JSON", 4, outputJson.length());
    assertTrue("outCounter is missing from output JSON", outputJson.has("outCounter"));
    assertTrue("outStr is missing from output JSON", outputJson.has("outStr"));
    assertTrue("time is missing from output JSON", outputJson.has("time"));
    assertTrue("all3 is missing from output JSON", outputJson.has("all3"));
    Object outCounterObject = outputJson.get("outCounter");
    assertTrue("outCounter is not an Integer: " + outCounterObject.getClass().getSimpleName(), outCounterObject instanceof Integer);
    assertTrue("outCounter is zero", ((Integer)outCounterObject) != 0);
    Object outStrObject = outputJson.get("outStr");
    assertTrue("outStr is not a String: " + outStrObject.getClass().getSimpleName(), outStrObject instanceof String);
    String outStr = (String)outStrObject;
    int outStrLength = outStr.length();
    assertTrue("outStr length is not between 2 and 20: " + outStrLength, outStrLength >= 2 && outStrLength <= 20);
    String prefix = outStr.substring(0, 2);
    assertEquals("OutStr prefix", "aa", prefix);
    Object timeObject = outputJson.get("time");
    assertTrue("time is not a String: " + timeObject.getClass().getSimpleName(), timeObject instanceof String);
    String timeString = (String)timeObject;
    int timeLength = timeString.length();
    assertTrue("time length is not between 18 and 40: " + timeLength, timeLength >= 18 && timeLength <= 40);
    long time = DateUtils.parseDate(timeString);
    long delta = now - time;
    assertTrue("time delta is not near now: " + delta, delta >= 0 && delta <= 2000);
    
    // Check status of all agent instances
    JSONObject statusJson = doGetJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1", 200);
    assertTrue("Status return JSON is missing", statusJson != null);
    assertEquals("Count of fields in status JSON", 1, statusJson.length());
    assertTrue("agent_instances is missing", statusJson.has("agent_instances"));
    JSONArray agentInstanceArrayJson = statusJson.getJSONArray("agent_instances");
    assertEquals("Count of fields in agent_instances JSON array", 4, agentInstanceArrayJson.length());
    
    // Now delete our agent - all of its auto-created input agent instances should be auto-deleted
    doDeleteJson(baseUrl + "/users/test-user-1/agents/MyAgent-1?password=test-pwd-1", 204);
    
    // Check to see that no instances are left running
    statusJson = doGetJson(baseUrl + "/users/test-user-1/agents?password=test-pwd-1", 200);
    assertTrue("Status return JSON is missing", statusJson != null);
    assertEquals("Count of fields in status JSON", 1, statusJson.length());
    assertTrue("agent_instances is missing", statusJson.has("agent_instances"));
    agentInstanceArrayJson = statusJson.getJSONArray("agent_instances");
    assertEquals("Count of fields in agent_instances JSON array", 0, agentInstanceArrayJson.length());
    
  }

}
