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

package com.basetechnology.s0.agentserver.mail;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentSchedulerTest;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.AgentServerTestBase;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.config.AgentServerConfig;
import com.basetechnology.s0.agentserver.config.AgentServerProperties;
import com.basetechnology.s0.agentserver.field.BooleanField;
import com.basetechnology.s0.agentserver.field.FieldList;
import com.basetechnology.s0.agentserver.field.FloatField;
import com.basetechnology.s0.agentserver.field.StringField;
import com.basetechnology.s0.agentserver.mail.AgentMail;
import com.basetechnology.s0.agentserver.notification.MailNotification;
import com.basetechnology.s0.agentserver.notification.NotificationDefinition;
import com.basetechnology.s0.agentserver.notification.NotificationInstance;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolTable;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.util.ListMap;

public class AgentMailTest extends AgentServerTestBase {
  static final Logger log = Logger.getLogger(AgentMailTest.class);

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

  @Test
  public void testBasic() {
    AgentServer agentServer = server.agentServer;
    AgentServerConfig config = agentServer.config;
    AgentServerProperties properties = config.agentServerProperties;

    // Skip test if mail access if disabled
    if(config.getMailAccessEnabled()){
      String serverHostName = properties.mailServerHostName;
      String serverUserName = properties.mailServerUserName;
      String serverUserPwd = properties.mailServerUserPassword;
      int serverPort = properties.mailServerPort;
      String serverFromEmail = properties.mailServerFromEmail;
      String serverFromName = properties.mailServerFromName;
      int nextMessageId = 1000;
      AgentMail agentMail = new AgentMail(agentServer, serverHostName, serverUserName, serverUserPwd,
          serverPort, serverFromEmail, serverFromName, nextMessageId);

      String toEmail = properties.testUserEmail;
      int i = toEmail.indexOf('@');
      String userId = i < 0 ? "" : toEmail.substring(0, i);
      User user = new User(userId);
      String toName = properties.testUserName;
      String subject = "Test subject";
      String message = "Test message.";
      String messageTrailer1 = "\n\n----------\nFrom Agent Server - Message Id #";
      String messageTrailer2 = "\n" +
          "For support contact agent-server-1-admin@basetechnology.com\n" +
          "Or visit http://basetechnology.com\n";

      try {
        int messageId = agentMail.sendMessage(user, toEmail, toName, subject, message,
            messageTrailer1, messageTrailer2);
        assertEquals("Message Id", nextMessageId + 1, messageId);
      } catch (AgentServerException e){
        fail("Exception sending message - " + e.getMessage());
      }
    } else
      log.warn("testBasic skipped since mail_access_enabled is false");
  }


  @Test
  public void testNotification() throws AgentServerException, JSONException, TokenizerException, ParserException {
    // Add user
    AgentServer agentServer = server.agentServer;
    User user = new User("test-user-1", "pwd-1", "Your pwd", "Test User 1", "Test User 1", "", "tu1", "", "", "test-user-1@basetechnology.com", false, "", true, null, null);
    agentServer.addUser(user);
    
    // Add agent definition
    AgentDefinition agentDefinition = new AgentDefinition(server.agentServer);
    agentDefinition.name = "TestAgent";
    agentServer.addAgentDefinition(agentDefinition);
    
    // Add several notifications to the agent definition
    FieldList fieldList1 = new FieldList();
    SymbolTable symbolTable = new SymbolTable("Not1");
    fieldList1.add(new StringField(symbolTable, "Vendor", "", "", "Abc Corp.", 0, 1000, 100, ".*", null));
    fieldList1.add(new FloatField(symbolTable, "Price", "", "", 123.45, 0.0, 1000.0, 100, null));
    fieldList1.add(new BooleanField(symbolTable, "Purchased", "", "", true, null));
    NotificationDefinition notificationDefinition1 =
        new NotificationDefinition("Not1", "FYI, we found your deal", "notify_only", "", true, true, fieldList1, null, "", false);
    agentDefinition.notifications.add(notificationDefinition1.name, notificationDefinition1);

    NotificationDefinition notificationDefinition2 =
        new NotificationDefinition("Not2", "We're ready to continue when you are", "confirm", "", true, true, fieldList1, null, "", true);
    agentDefinition.notifications.add(notificationDefinition2.name, notificationDefinition2);

    NotificationDefinition notificationDefinition3 =
        new NotificationDefinition("Not3", "Do you want this deal?", "yes_no", "", true, true, fieldList1, null, "", true);
    agentDefinition.notifications.add(notificationDefinition3.name, notificationDefinition3);

    // Add agent instance
    AgentInstance agentInstance = agentServer.getAgentInstance(user, agentDefinition);
    
    // Suppress email for now, just check status
    //agentInstance.suppressEmail = true;
    
    // Get the notification instances
    ListMap<String, NotificationInstance> notificationInstances = agentInstance.notifications;
    
    // Perform email notification for notify-only instance notification
    MailNotification mailNotification = new MailNotification(agentServer);
    mailNotification.notify(notificationInstances.get("Not1"));

    // Now email notification for the confirm-only
    mailNotification.notify(notificationInstances.get("Not2"));

    // And now email notification for the yes/no confirmation
    mailNotification.notify(notificationInstances.get("Not3"));
    
    // TODO: Read and execute and check the received emails
    
  }
  
}
