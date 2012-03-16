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

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;

import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.User;

public class AgentMail {
  static final Logger log = Logger.getLogger(AgentMail.class);

  AgentServer agentServer;
  public String mailServerHostName;
  public String mailServerUserName;
  public String mailServerUserPassword;
  public int mailServerPort;
  public String mailServerFromEmail;
  public String mailServerFromName;
  public String testUserEmail;
  public String testUserName;
  public int nextMessageId;
  public boolean debug = true;

  public AgentMail(AgentServer agentServer, String mailServerHostName, String mailServerUserName,
      String mailServerUserPassword, int mailServerPort,
      String mailServerFromEmail, String mailServerFromName, int nextMessageId){
    this.agentServer = agentServer;
    this.mailServerHostName = mailServerHostName;
    this.mailServerUserName = mailServerUserName;
    this.mailServerUserPassword = mailServerUserPassword;
    this.mailServerPort = mailServerPort;
    this.mailServerFromEmail = mailServerFromEmail;
    this.mailServerFromName = mailServerFromName;
    this.nextMessageId = nextMessageId;
  }

  public int sendMessage(User user, String toEmail, String toName, String subject,
      String message, String messageTrailer1, String messageTrailer2) throws AgentServerException {
    try {
      // Wait until we have mail access
      agentServer.mailAccessManager.wait(user, toEmail);
      
      int messageId = ++nextMessageId;
      log.info("Sending mail on behalf of user " + user.id + " with message Id " + messageId +
          " To: '<" + toName + ">" + toEmail + "' Subject: '" + subject + "'");

      Email email = new SimpleEmail();
      email.setDebug(debug);
      email.setHostName(mailServerHostName);
      email.setSmtpPort(mailServerPort);
      email.setAuthenticator(new DefaultAuthenticator(mailServerUserName, mailServerUserPassword));
      email.setTLS(true);
      email.setFrom(mailServerFromEmail, mailServerFromName);
      // TODO: Reconsider whether we want to always mess with subject line
      email.setSubject(subject + " (#" + messageId + ")");
      email.setMsg(message + messageTrailer1 +
          (messageTrailer2 != null ? messageId + messageTrailer2 : ""));
      email.addTo(toEmail, toName);
      email.send();
      log.info("Message sent");

      // Return the message Id
      return messageId;
    } catch (EmailException e){
      e.printStackTrace();
      throw new AgentServerException("EmailException sending email - " + e.getMessage());
    }
  }
}
