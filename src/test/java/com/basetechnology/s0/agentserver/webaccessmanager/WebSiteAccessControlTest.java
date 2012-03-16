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

package com.basetechnology.s0.agentserver.webaccessmanager;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentFileException;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessException;
import com.basetechnology.s0.agentserver.webaccessmanager.WebSite;
import com.basetechnology.s0.agentserver.webaccessmanager.WebSiteAccessConfig;

public class WebSiteAccessControlTest {

  AgentAppServer agentAppServer = null;
  AgentServer agentServer = null;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    File pf = new File(AgentServer.defaultPersistencePath);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServer.defaultPersistencePath, ! pf.exists());
  }

  @After
  public void tearDown() throws Exception {
    if (agentAppServer != null){
      agentAppServer.stop();
      agentAppServer = null;
      agentServer = null;
    }
    File pf = new File(AgentServer.defaultPersistencePath);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServer.defaultPersistencePath, ! pf.exists());
  }

  @Test
  public void testWithoutAgentServer() throws JSONException, AgentServerException {
    // Without AgentServer we can't test everything, but some basic tests are good enough

    // Make sure we can construct a WSAC without an AgentServer
    WebSiteAccessConfig wsac = new WebSiteAccessConfig(null);
    
    // Make sure methods respond gracefully when nothing is set up
    try {
      wsac.load();
    } catch (IOException e){
      e.printStackTrace();
      fail("IOException - " + e);
    } catch (PersistentFileException e){
      e.printStackTrace();
      fail("PersistentFileException - " + e);
    } catch (AgentServerException e){
      e.printStackTrace();
      fail("AgentServerException - " + e);
    }
    
    try {
      boolean canAccess = wsac.isAccessAllowed((WebSite)null, null);
      assertTrue("Can access when access should not have been granted", ! canAccess);
    } catch (WebAccessException e){
      assertEquals("Exception", "Null WebSite parameter for isAccessAllowed", e.getMessage());
    }
    
    try {
      boolean canAccess = wsac.isAccessAllowed(new WebSite(null, null), null);
      assertTrue("Can't access when access should have been granted", canAccess);
    } catch (WebAccessException e){
      assertEquals("Exception", "Null user Id parameter for isAccessAllowed", e.getMessage());
    }
    
    try {
      boolean canAccess = wsac.isAccessAllowed(new WebSite(null, null), "");
      assertTrue("Can't access when access should have been granted", canAccess);
    } catch (WebAccessException e){
      assertEquals("Exception", "Empty user Id parameter for isAccessAllowed", e.getMessage());
    }
    
    try {
      boolean canAccess = wsac.isAccessAllowed(new WebSite(null, "http://www.cnn.com"), "testuser");
      assertTrue("Can't access when access should have been granted", canAccess);
    } catch (Exception e){
      e.printStackTrace();
      fail("IOException - " + e);
    }

    // Now manually add some access entries
    
    // Four sites, site1, site2, site3, site4
    WebSite site1 = new WebSite(null, "http://site1.com/");
    WebSite site2 = new WebSite(null, "http://site2.com/");
    WebSite site3 = new WebSite(null, "http://site3.com/");
    WebSite site4 = new WebSite(null, "http://site4.com/");
    WebSite site5 = new WebSite(null, "http://site5.com/");
    
    // For users, user1, user2, user3, user4
    
    // Scenario
    //
    // site1: Full access to all
    // site2: No access to anybody
    // site3: Access to all but user1 and user2
    // site4: Access to nobody but user1 and user2
    // site5: no entry
    
    wsac.addAccess(site1, "*", "grant");

    wsac.addAccess(site2, "*", "deny");
    
    wsac.addAccess(site3, "*", "grant");
    wsac.addAccess(site3, "user1", "deny");
    wsac.addAccess(site3, "user2", "deny");
    
    wsac.addAccess(site4, "*", "deny");
    wsac.addAccess(site4, "user1", "grant");
    wsac.addAccess(site4, "user2", "grant");

    // Validate that entries created as expected
    assertEquals("Entry", "grant", wsac.getAccess(site1, "*"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user1"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user4"));

    assertEquals("Entry", "deny", wsac.getAccess(site2, "*"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user1"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user4"));

    assertEquals("Entry", "grant", wsac.getAccess(site3, "*"));
    assertEquals("Entry", "deny", wsac.getAccess(site3, "user1"));
    assertEquals("Entry", "deny", wsac.getAccess(site3, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site3, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site3, "user4"));

    assertEquals("Entry", "deny", wsac.getAccess(site4, "*"));
    assertEquals("Entry", "grant", wsac.getAccess(site4, "user1"));
    assertEquals("Entry", "grant", wsac.getAccess(site4, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site4, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site4, "user4"));

    // Now do full access check
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
    wsac.localImplicitDeny = true;

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
    wsac.localImplicitDeny = false;

    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user4"));
  }

  @Test
  public void testWithAgentServer() throws AgentServerException, PersistentFileException, IOException, InterruptedException, Exception {
    // Create an agent server
    agentAppServer = new AgentAppServer();
    agentServer = agentAppServer.agentServer;

    // Access the WSAC created by the agent server
    WebSiteAccessConfig wsac = agentServer.webSiteAccessConfig;
    
    // Make sure methods respond gracefully when nothing is set up
    try {
      wsac.load();
    } catch (IOException e){
      e.printStackTrace();
      fail("IOException - " + e);
    } catch (PersistentFileException e){
      e.printStackTrace();
      fail("PersistentFileException - " + e);
    } catch (AgentServerException e){
      e.printStackTrace();
      fail("AgentServerException - " + e);
    }
    
    try {
      boolean canAccess = wsac.isAccessAllowed((WebSite)null, null);
      assertTrue("Can access when access should not have been granted", ! canAccess);
    } catch (WebAccessException e){
      assertEquals("Exception", "Null WebSite parameter for isAccessAllowed", e.getMessage());
    }
    
    try {
      boolean canAccess = wsac.isAccessAllowed(new WebSite(null, null), null);
      assertTrue("Can't access when access should have been granted", canAccess);
    } catch (WebAccessException e){
      assertEquals("Exception", "Null user Id parameter for isAccessAllowed", e.getMessage());
    }
    
    try {
      boolean canAccess = wsac.isAccessAllowed(new WebSite(null, null), "");
      assertTrue("Can't access when access should have been granted", canAccess);
    } catch (WebAccessException e){
      assertEquals("Exception", "Empty user Id parameter for isAccessAllowed", e.getMessage());
    }
    
    try {
      boolean canAccess = wsac.isAccessAllowed(new WebSite(null, "http://www.cnn.com"), "testuser");
      assertTrue("Can't access when access should have been granted", canAccess);
    } catch (Exception e){
      e.printStackTrace();
      fail("IOException - " + e);
    }

    // Now manually add some access entries
    
    // Four sites, site1, site2, site3, site4
    WebSite site1 = new WebSite(null, "http://site1.com/");
    WebSite site2 = new WebSite(null, "http://site2.com/");
    WebSite site3 = new WebSite(null, "http://site3.com/");
    WebSite site4 = new WebSite(null, "http://site4.com/");
    WebSite site5 = new WebSite(null, "http://site5.com/");
    
    // For users, user1, user2, user3, user4
    
    // Scenario
    //
    // site1: Full access to all
    // site2: No access to anybody
    // site3: Access to all but user1 and user2
    // site4: Access to nobody but user1 and user2
    // site5: no entry
    
    wsac.addAccess(site1, "*", "grant");

    wsac.addAccess(site2, "*", "deny");
    
    wsac.addAccess(site3, "*", "grant");
    wsac.addAccess(site3, "user1", "deny");
    wsac.addAccess(site3, "user2", "deny");
    
    wsac.addAccess(site4, "*", "deny");
    wsac.addAccess(site4, "user1", "grant");
    wsac.addAccess(site4, "user2", "grant");

    // Validate that entries created as expected
    assertEquals("Entry", "grant", wsac.getAccess(site1, "*"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user1"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user4"));

    assertEquals("Entry", "deny", wsac.getAccess(site2, "*"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user1"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user4"));

    assertEquals("Entry", "grant", wsac.getAccess(site3, "*"));
    assertEquals("Entry", "deny", wsac.getAccess(site3, "user1"));
    assertEquals("Entry", "deny", wsac.getAccess(site3, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site3, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site3, "user4"));

    assertEquals("Entry", "deny", wsac.getAccess(site4, "*"));
    assertEquals("Entry", "grant", wsac.getAccess(site4, "user1"));
    assertEquals("Entry", "grant", wsac.getAccess(site4, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site4, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site4, "user4"));

    // Now do full access check
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
    agentServer.webAccessConfig.setImplicitlyDenyWebAccess(true);

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
    agentServer.webAccessConfig.setImplicitlyDenyWebAccess(false);

    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user4"));
    
    // Check that web access control persists over shutdown and restart
    agentServer.shutdown();
    agentServer.start();

    assertEquals("Entry", "grant", wsac.getAccess(site1, "*"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user1"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user4"));

    assertEquals("Entry", "deny", wsac.getAccess(site2, "*"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user1"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user4"));

    assertEquals("Entry", "grant", wsac.getAccess(site3, "*"));
    assertEquals("Entry", "deny", wsac.getAccess(site3, "user1"));
    assertEquals("Entry", "deny", wsac.getAccess(site3, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site3, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site3, "user4"));

    assertEquals("Entry", "deny", wsac.getAccess(site4, "*"));
    assertEquals("Entry", "grant", wsac.getAccess(site4, "user1"));
    assertEquals("Entry", "grant", wsac.getAccess(site4, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site4, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site4, "user4"));

    // Now do full access check
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
  public void testStarUrl() throws Exception {
    // Test with URL of "*" to control full web access on/off
    // Note: This is really the same as implicit deny flag
    
    // Create an agent server
    agentAppServer = new AgentAppServer();
    agentServer = agentAppServer.agentServer;

    // Access the WSAC created by the agent server
    WebSiteAccessConfig wsac = agentServer.webSiteAccessConfig;
    
    // Now manually add some access entries
    
    // Four sites, site1, site2, site3, site4
    WebSite site1 = new WebSite(null, "http://site1.com/");
    WebSite site2 = new WebSite(null, "http://site2.com/");
    WebSite site3 = new WebSite(null, "http://site3.com/");
    WebSite site4 = new WebSite(null, "http://site4.com/");
    WebSite site5 = new WebSite(null, "http://site5.com/");
    WebSite site6 = new WebSite(null, "http://site6.com/");
    
    // For users, user1, user2, user3, user4
    
    // Scenario
    //
    // site1: Full access to all
    // site2: No access to anybody
    // site3: Access to all but user1 and user2
    // site4: Access to nobody but user1 and user2
    // site5: no entry
    
    wsac.addAccess(site1, "*", "grant");

    wsac.addAccess(site2, "*", "deny");
    
    wsac.addAccess(site3, "*", "grant");
    wsac.addAccess(site3, "user1", "deny");
    wsac.addAccess(site3, "user2", "deny");
    
    wsac.addAccess(site4, "*", "deny");
    wsac.addAccess(site4, "user1", "grant");
    wsac.addAccess(site4, "user2", "grant");

    // Validate that entries created as expected
    assertEquals("Entry", "grant", wsac.getAccess(site1, "*"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user1"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site1, "user4"));

    assertEquals("Entry", "deny", wsac.getAccess(site2, "*"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user1"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site2, "user4"));

    assertEquals("Entry", "grant", wsac.getAccess(site3, "*"));
    assertEquals("Entry", "deny", wsac.getAccess(site3, "user1"));
    assertEquals("Entry", "deny", wsac.getAccess(site3, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site3, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site3, "user4"));

    assertEquals("Entry", "deny", wsac.getAccess(site4, "*"));
    assertEquals("Entry", "grant", wsac.getAccess(site4, "user1"));
    assertEquals("Entry", "grant", wsac.getAccess(site4, "user2"));
    assertEquals("Entry", null, wsac.getAccess(site4, "user3"));
    assertEquals("Entry", null, wsac.getAccess(site4, "user4"));

    // Now do full access check
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
    
    // Now add an entry for URL of "*" to "grant" for all users - should be a no-op
    WebSite starSite = new WebSite(null, "*");
    wsac.addAccess(starSite, "*", "grant");
    assertEquals("Entry", "grant", wsac.getAccess(starSite, "*"));
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

    // Now replace the "*" URL entry with "deny" and detect change
    wsac.addAccess(starSite, "*", "deny");
    assertEquals("Entry", "deny", wsac.getAccess(starSite, "*"));
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

    // Now add a new user and see that they automatically get denied
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site6, "user5"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site1, "user5"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site2, "user5"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site3, "user5"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site4, "user5"));
    assertEquals("Access allowed", false, wsac.isAccessAllowed(site5, "user5"));

    // Change the "*" URL back to "grant" and see if new user gets grant
    wsac.addAccess(starSite, "*", "grant");
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site6, "user5"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user1"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user2"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user3"));
    assertEquals("Access allowed", true, wsac.isAccessAllowed(site5, "user4"));

    // TODO: Review rules as to what happens if we have a "*" user for a site and a "*" site rule
    // Appears to be that the "*" user rule prevails
    // I guess that makes sense

  }
  
}
