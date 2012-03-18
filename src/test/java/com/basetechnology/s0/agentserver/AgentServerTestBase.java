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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.config.AgentServerProperties;
import com.basetechnology.s0.agentserver.util.JsonListMap;

public class AgentServerTestBase {
  static final Logger log = Logger.getLogger(AgentServerTestBase.class);

  public static AgentAppServer server;

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

  public void restartServer() throws Exception {

    // Shut down the server
    server.shutdown();
    assertEquals("Jetty status", "STOPPED", server.server.getState());

    // Make sure server is gone
    try {
      JSONObject statusJson = doGetJson(AgentAppServer.appServerApiBaseUrl + "/status", 200);
      assertTrue("Should not success with GET /status when server is shutdown", false);
    } catch (Exception e){
      assertEquals("Exception on GET /status of shutdown server", "Connection to http://localhost:" + server.appServerPort + " refused", e.getMessage());
    }
    
    // Restart the server
    server.start();

    // Make sure server is back
    try {
      JSONObject statusJson = doGetJson(AgentAppServer.appServerApiBaseUrl + "/status", 200);
      assertTrue("Server did not return status", statusJson != null);
      assertEquals("Server status: " +statusJson.toString(), "running", statusJson.getString("status"));
    } catch (Exception e){
      assertTrue("Exception on GET /status of shutdown server: " + e.getMessage(), false);
    }
  }
  
  public void cleanupPersistentStore() throws Exception {
    String path = AgentServerProperties.DEFAULT_PERSISTENT_STORE_PATH;
    if (server != null){
      // Make sure server is stopped
      server.stop();
      
      // Get path
      path = server.agentServer.getPersistentStorePath();
    }

    // Get rid of old persistent store
    File file = new File(path);
    if (file.exists()){
      file.delete();
      assertTrue("Persistent store not deleted: " + path, ! file.exists());
    }
  }

  public boolean compareJson(Object object1, Object object2){
    if (object1 == null)
      return object2 == null;
    else if (object2 == null)
      return false;
    
    if (object1 instanceof JSONObject){
      if (! (object2 instanceof JSONObject))
        return false;
      JSONObject json1 = (JSONObject)object1;
      JSONObject json2 = (JSONObject)object2;
      int len1 = json1.length();
      int len2 = json2.length();
      // Find key names that are in one but not the other
      String extra1 = "";
      String extra2 = "";
      for (Iterator<String> it1 = json1.keys(); it1.hasNext(); ){
        String key1 = it1.next();
        boolean match = false;
        for (Iterator<String> it2 = json2.keys(); it2.hasNext(); ){
          String key2 = it2.next();
          if (key1.equals(key2)){
            match = true;
            break;
          }
        }
        if (! match)
          extra1 += (extra1.length() > 0 ? ", " : "") + key1;
      }
      for (Iterator<String> it1 = json2.keys(); it1.hasNext(); ){
        String key1 = it1.next();
        boolean match = false;
        for (Iterator<String> it2 = json1.keys(); it2.hasNext(); ){
          String key2 = it2.next();
          if (key1.equals(key2)){
            match = true;
            break;
          }
        }
        if (! match)
          extra2 += (extra2.length() > 0 ? ", " : "") + key1;
      }
      if (extra1.length() > 0 || extra2.length() > 0){
        fail(( extra1.length() > 0 ? "Expected JSON includes keys not found in actual JSON: " + extra1 : "") + (extra1.length() > 0 && extra2.length() > 0 ? "; " : "") + (extra2.length() > 0 ? "actual JSON includes keys not found in expected JSON: " + extra2 : ""));
        return false;
      }
      for(Iterator<String> it = json1.keys(); it.hasNext(); ){
        String key = it.next();
        if (! json2.has(key))
          return false;
        if (! compareJson(json1.opt(key), json2.opt(key)))
          return false;
      }
      return true;
    } else if (object1 instanceof JSONArray){
      if (! (object2 instanceof JSONArray))
        return false;
      JSONArray json1 = (JSONArray)object1;
      JSONArray json2 = (JSONArray)object2;
      int len1 = json1.length();
      int len2 = json2.length();
      if (len1 != len2)
        return false;
      for (int i = 0; i < len1; i++)
        try {
        if (! compareJson(json1.get(i), json2.get(i)))
          return false;
        } catch (JSONException e){
          return false;
        }
      return true;
    }

    // TODO: Does this work for Long, etc?
    return object1.equals(object2);
  }
  
  public void assertJsonSourceEquals(String message, String expectedJson, JSONObject json){
    assertJsonSourceEquals(message, expectedJson, json.toString());
  }
  
  public void assertJsonSourceEquals(String message, String expectedJson, JSONArray json){
    assertJsonSourceEquals(message, expectedJson, json.toString());
  }
  
  public void assertJsonSourceEquals(String message, String expectedJson, String json){
    Object json1 = null;
    try {
      if (expectedJson != null && expectedJson.length() > 0){
        if (expectedJson.charAt(0) == '{')
          json1 = new JSONObject(expectedJson);
        else if (json.charAt(0) == '[')
          json1 = new JSONArray(expectedJson);
        else
          json1 = new JSONObject(expectedJson);
      }
    } catch (JSONException e){
      assertTrue("Unable to parse JSON for " + message + " - " + e.getMessage(), false);
    }
    Object json2 = null;
    try {
      if (json != null && json.length() > 0){
        if (json.charAt(0) == '{')
          json2 = new JSONObject(json);
        else if (json.charAt(0) == '[')
          json2 = new JSONArray(json);
        else
          json2 = new JSONObject(json);
      }
    } catch (JSONException e){
      assertTrue("Unable to parse JSON for " + message + " - " + e.getMessage(), false);
    }
    
    // String comparison is still flaky sometimes, so do a direct compare first
    if (! compareJson(json1, json2))
      assertEquals(message, json1 == null ? null : json1.toString(), json2 == null ? null : json2.toString());
    //assertTrue(message + " is not a match - expected: <<" + expectedJson + ">> but got: <<" + json + ">>", compareJson(json1, json2));
  }
  
  public void assertJsonEquals(String message, JSONObject expectedJson, JSONObject json){
    assertTrue(message, compareJson(expectedJson, json));
  }

  public void assertError(JSONObject returnJson, String expectedType, String expectedMessage) throws JSONException {
    assertTrue("Missing return value", returnJson != null);
    assertTrue("Missing errors key", returnJson.has("errors"));
    JSONArray errorsArrayJson = returnJson.getJSONArray("errors");
    assertEquals("Count of errors", 1, errorsArrayJson.length());
    JSONObject errorJson = errorsArrayJson.getJSONObject(0);
    assertTrue("Missing error message", errorJson.has("message"));
    assertTrue("Missing error type", errorJson.has("type"));
    assertEquals("Error type", expectedType, errorJson.getString("type"));
    assertEquals("Error message", expectedMessage, errorJson.getString("message"));
  }

  public void assertNoError(JSONObject returnJson) throws JSONException {
    if (returnJson != null)
      assertTrue("Unexpected error return: " + returnJson.toString(), ! returnJson.has("errors"));
  }

  public String doGet(String url) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpget = new HttpGet(url);
    HttpResponse response = httpclient.execute(httpget);
    int statusCode = response.getStatusLine().getStatusCode();
    HttpEntity entity = response.getEntity();
    String s = EntityUtils.toString(entity);
    if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    if (s == null || s.length() == 0)
      return null;
    return s;
  }

  public String doGet(String url, int expectedStatusCode) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpget = new HttpGet(url);
    HttpResponse response = httpclient.execute(httpget);
    int statusCode = response.getStatusLine().getStatusCode();
    HttpEntity entity = response.getEntity();
    String s = EntityUtils.toString(entity);
    if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    assertEquals("HTTP result code", expectedStatusCode, statusCode);
    if (s == null || s.length() == 0)
      return null;
    return s;
  }

  public JSONObject doGetJson(String url, int expectedStatusCode) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpGet = new HttpGet(url);
    HttpResponse response = httpclient.execute(httpGet);
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    String s = EntityUtils.toString(entity);
    //if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    assertEquals("HTTP result code", expectedStatusCode, statusCode);
    //if (statusCode / 100 != 2)
      //return null;
    //else {
      if (s == null || s.length() == 0)
        return null;
      JSONObject json = new JsonListMap(s);
      return json;
    //}
  }

  public String doGetXml(String url, int expectedStatusCode) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpGet = new HttpGet(url);
    HttpResponse response = httpclient.execute(httpGet);
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    String s = EntityUtils.toString(entity);
    //if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    assertEquals("HTTP result code", expectedStatusCode, statusCode);
    //if (statusCode / 100 != 2)
      //return null;
    //else {
      if (s == null || s.length() == 0)
        return null;
      return s;
    //}
  }

  public JSONObject doPostJson(String url, JSONObject contentJson, int expectedStatusCode) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    String contentString = "";
    if (contentJson != null)
      contentString = contentJson.toString();
    StringEntity contentEntity = new StringEntity(contentString, "UTF-8");
    HttpPost httpPost = new HttpPost(url);
    if (contentJson != null)
      httpPost.setEntity(contentEntity);
    HttpResponse response = httpclient.execute(httpPost);
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    String s = EntityUtils.toString(entity);
    //if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    assertEquals("HTTP result code", expectedStatusCode, statusCode);
    //if (statusCode / 100 != 2)
      //return null;
    //else {
      if (s == null || s.length() == 0)
        return null;
      JSONObject json = new JSONObject(s);
      return json;
    //}
  }

  public JSONObject doPostJson(String url, String contentString, int expectedStatusCode) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    StringEntity contentEntity = new StringEntity(contentString, "UTF-8");
    HttpPost httpPost = new HttpPost(url);
    if (contentString != null)
      httpPost.setEntity(contentEntity);
    HttpResponse response = httpclient.execute(httpPost);
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    String s = entity == null ? null : EntityUtils.toString(entity);
    //if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    assertEquals("HTTP result code", expectedStatusCode, statusCode);
    //if (statusCode / 100 != 2)
      //return null;
    //else {
      if (s == null || s.length() == 0)
        return null;
      JSONObject json = new JSONObject(s);
      return json;
    //}
  }

  public String doPostText(String url, String contentString, int expectedStatusCode) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    StringEntity contentEntity = new StringEntity(contentString, "UTF-8");
    HttpPost httpPost = new HttpPost(url);
    if (contentString != null)
      httpPost.setEntity(contentEntity);
    HttpResponse response = httpclient.execute(httpPost);
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    String s = EntityUtils.toString(entity);
    if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    assertEquals("HTTP result code", expectedStatusCode, statusCode);
    return s;
  }

  public JSONObject doPutJson(String url, int expectedStatusCode) throws Exception {
    return doPutJson(url, "", expectedStatusCode);
  }

  public JSONObject doPutJson(String url, JSONObject contentJson, int expectedStatusCode) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    String contentString = "";
    if (contentJson != null)
      contentString = contentJson.toString();
    StringEntity contentEntity = new StringEntity(contentString, "UTF-8");
    HttpPut httpPut = new HttpPut(url);
    if (contentJson != null)
      httpPut.setEntity(contentEntity);
    HttpResponse response = httpclient.execute(httpPut);
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    String s = EntityUtils.toString(entity);
    //if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    assertEquals("HTTP result code", expectedStatusCode, statusCode);
    if (s == null || s.length() == 0)
      return null;
    JSONObject json = new JSONObject(s);
    return json;
  }

  public JSONObject doPutJson(String url, String contentString, int expectedStatusCode) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    StringEntity contentEntity = new StringEntity(contentString, "UTF-8");
    HttpPut httpPut = new HttpPut(url);
    if (contentString != null)
      httpPut.setEntity(contentEntity);
    HttpResponse response = httpclient.execute(httpPut);
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    String s = entity == null ? null : EntityUtils.toString(entity);
    //if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    assertEquals("HTTP result code", expectedStatusCode, statusCode);
    if (s == null || s.length() == 0)
      return null;
    JSONObject json = new JSONObject(s);
    return json;
  }

  public JSONObject doDeleteJson(String url, int expectedStatusCode) throws Exception {
    HttpClient httpclient = new DefaultHttpClient();
    HttpDelete httpDelete = new HttpDelete(url);
    HttpResponse response = httpclient.execute(httpDelete);
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    String s = entity == null ? null : EntityUtils.toString(entity);
    //if (statusCode / 100 != 2)
      log.info("HTTP response entity for result code " + statusCode + ": " + s);
    assertEquals("HTTP result code", expectedStatusCode, statusCode);
    //if (statusCode / 100 != 2)
      //return null;
    //else {
      if (s == null || s.length() == 0)
        return null;
      JSONObject json = new JSONObject(s);
      return json;
    //}
  }

}
