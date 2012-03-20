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

package com.basetechnology.s0.agentserver.notification;

import org.apache.log4j.Logger;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.config.AgentServerConfig;
import com.basetechnology.s0.agentserver.config.AgentServerProperties;
import com.basetechnology.s0.agentserver.mail.AgentMail;

public class MailNotification {
  static final Logger log = Logger.getLogger(MailNotification.class);

  AgentServer agentServer;
  public String mailServerHostName;
  public String mailServerUserName;
  public String mailServerUserPassword;
  public int mailServerPort;
  public String mailServerFromEmail;
  public String mailServerFromName;
  public String testUserEmail;
  public String testUserName;
  public AgentMail agentMail;
  public int initialMessageId;
  
  public MailNotification(AgentServer agentServer){
    this.agentServer = agentServer;
    AgentServerProperties agentServerProperties = agentServer.config.agentServerProperties;
    mailServerHostName = agentServerProperties.mailServerHostName;
    mailServerUserName = agentServerProperties.mailServerUserName;
    mailServerUserPassword = agentServerProperties.mailServerUserPassword;
    mailServerPort = agentServerProperties.mailServerPort;
    mailServerFromEmail = agentServerProperties.mailServerFromEmail;
    mailServerFromName = agentServerProperties.mailServerFromName;
    initialMessageId = 1000;
    agentMail = new AgentMail(agentServer, mailServerHostName, mailServerUserName,
        mailServerUserPassword, mailServerPort, mailServerFromEmail, mailServerFromName,
        initialMessageId);
  }

  public void notify(NotificationInstance notificationInstance) throws AgentServerException {
    NotificationDefinition notificationDefinition = notificationInstance.definition;
    String notificationName = notificationDefinition.name;
    String notificationDescription = notificationDefinition.description;
    AgentInstance agentInstance = notificationInstance.agentInstance;
    String agentName = agentInstance.name;
    User user = agentInstance.user;
    String userId = user.id;
    AgentServer agentServer = agentInstance.agentServer;
    AgentServerConfig agentServerConfig = agentServer.config;
    
    // Check whether email is being suppressed
    if (agentInstance.suppressEmail)
      log.warn("Email notification suppressed due to suppressEmail flag for instance " + agentInstance.name);
    else if (! agentServerConfig.getMailAccessEnabled())
      log.warn("Email notification suppression due to mail_access_enabled = false for instance " + agentInstance.name);
    else {
      String serverUrl = agentServer.agentAppServer.appServerApiBaseUrl;
      String responseUrl = serverUrl + "/users/" + userId + "/agents/" + agentName +
          "/notifications/" + notificationName +
          "?password=" + user.password + "&response=";
      String toEmail = user.email;
      String toName = user.displayName;
      String subject = notificationInstance.definition.description;
      String message = "Notification of " + notificationName + " (" + notificationDescription + ")" +
          (notificationInstance.details != null ? "\n\nDetails:\n\n" + notificationInstance.details.toString() : "") +
          "\n\n";
      String notificationType = notificationDefinition.type;
      if (notificationType.equals("notify_only"))
        message += "No response necessary";
      else if (notificationType.equals("confirm"))
        message += "Confirm by clicking on this link:\n\n\t" + responseUrl + "confirm";
      else if (notificationType.equals("yes_no"))
        message += "Accept by clicking on this link:\n\n\t" + responseUrl + "accept" +
            "\n\nOr decline by checking on this link:\n\n\t" + responseUrl + "decline";
      String messageTrailer1 = "\n\n----------\nFrom Agent Server - Message Id #";
      String messageTrailer2 = "\n" +
          "For support contact " + agentServerConfig.getContact() + "\n" +
          "Or visit " + agentServerConfig.getWebsite() + "\n";

      int messageId = agentMail.sendMessage(user, toEmail, toName, subject, message,
          messageTrailer1, messageTrailer2);
      log.info("Sent message for notification " + notificationInstance.definition.name +
          " with message Id " + messageId);
    }
  }
}
