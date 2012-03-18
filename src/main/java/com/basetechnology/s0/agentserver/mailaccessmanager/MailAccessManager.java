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

package com.basetechnology.s0.agentserver.mailaccessmanager;

import org.apache.log4j.Logger;

import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.config.AgentServerConfig;
import com.basetechnology.s0.agentserver.util.ListMap;

public class MailAccessManager {
  static final Logger log = Logger.getLogger(MailAccessManager.class);
  public AgentServer agentServer;
  public static final int DEFAULT_MINIMUM_MAIL_ACCESS_INTERVAL = 2000; 
  public static final int DEFAULT_MINIMUM_HOST_MAIL_ACCESS_INTERVAL = 2000; 
  public static final int DEFAULT_MINIMUM_ADDRESS_MAIL_ACCESS_INTERVAL = 10000;
  public static final boolean DEFAULT_MAIL_ACCESS_ENABLED = true;
  public boolean mailAccessEnabled = DEFAULT_MAIL_ACCESS_ENABLED;
  public long minimumMailAccessInterval = DEFAULT_MINIMUM_MAIL_ACCESS_INTERVAL;
  public long minimumHostMailAccessInterval = DEFAULT_MINIMUM_HOST_MAIL_ACCESS_INTERVAL;
  public long minimumAddressMailAccessInterval = DEFAULT_MINIMUM_ADDRESS_MAIL_ACCESS_INTERVAL;
  long lastAccessTime;
  ListMap<String, Long> lastHostAccessTimes;
  ListMap<String, Long> lastAddressAccessTimes;

  public MailAccessManager(AgentServer agentServer){
    this.agentServer = agentServer;
    this.lastHostAccessTimes = new ListMap<String, Long>();
    this.lastAddressAccessTimes = new ListMap<String, Long>();
    this.lastAccessTime = 0;
    readConfig();
  }

  public String getHostName(String emailAddress){
    int i = emailAddress.indexOf('@');
    if (i >= 0)
      return emailAddress.substring(i + 1);
    else
      return "";
  }

  public long getDelayedAccessTime(String emailAddress){
    // Get current time
    long now = System.currentTimeMillis();
    
    // Get time of last access to this email address
    long lastAddressAccess = 0;
    if (lastAddressAccessTimes.containsKey(emailAddress))
      lastAddressAccess = lastAddressAccessTimes.get(emailAddress);
    
    // Compute elapsed time since that last email address access
    long deltaAddress = now - lastAddressAccess;
    
    // Get the host of this email address
    // TODO: Figure out how to do this properly
    String hostName = getHostName(emailAddress);

    // Get time of last access to this email host
    long lastHostAccess = 0;
    if (lastHostAccessTimes.containsKey(hostName))
      lastHostAccess = lastHostAccessTimes.get(hostName);
    
    // Compute elapsed time since that last host access
    long deltaHost = now - lastHostAccess;
    
    // Compute elapsed time since last email access overall
    long deltaAccess = now - this.lastAccessTime;
    
    // Compute wait time for overall, host, and specific email address
    long overallWait = deltaAccess > minimumMailAccessInterval ? 0 :
      minimumMailAccessInterval - deltaAccess;
    long hostWait = deltaHost > minimumHostMailAccessInterval ? 0 :
      minimumHostMailAccessInterval - deltaHost;
    long addressWait = deltaAddress > minimumAddressMailAccessInterval ? 0 :
      minimumAddressMailAccessInterval - deltaAddress;
    
    // Return whichever wait is longest
    if (overallWait > hostWait){
      if (overallWait > addressWait)
        return overallWait;
      else
        return addressWait;
    } else if (hostWait > addressWait)
      return hostWait;
    else
      return addressWait;
  }

  public long recordEmailSend(String emailAddress){
    // See if we need to delay
    long delay = getDelayedAccessTime(emailAddress);
    if (delay > 0)
      // Yes, don't record the send at this time
      return delay;
    
    // No need for a delay, so record now as time of last access overall, for host and for specific email address
    long now = System.currentTimeMillis();
    String hostName = getHostName(emailAddress);
    lastAccessTime = now;
    lastHostAccessTimes.put(hostName, now);
    lastAddressAccessTimes.put(emailAddress, now);
    
    // Indicate no need to delay
    return 0;
  }
  
  public void readConfig(){
    AgentServerConfig config = agentServer.config;

    mailAccessEnabled = config.getBoolean("mail_access_enabled");
    minimumMailAccessInterval = config.getLong("minimum_mail_access_interval");
    minimumHostMailAccessInterval = config.getLong("minimum_host_mail_access_interval");
    minimumAddressMailAccessInterval = config.getLong("minimum_address_mail_access_interval");
  }
  
  static public void setConfigDefaults(AgentServerConfig config) throws AgentServerException {
    config.put("mail_access_enabled", DEFAULT_MAIL_ACCESS_ENABLED);
    config.put("minimum_mail_access_interval", DEFAULT_MINIMUM_MAIL_ACCESS_INTERVAL);
    config.put("minimum_host_mail_access_interval", DEFAULT_MINIMUM_HOST_MAIL_ACCESS_INTERVAL);
    config.put("minimum_address_mail_access_interval", DEFAULT_MINIMUM_ADDRESS_MAIL_ACCESS_INTERVAL);
  }
  
  public void wait(User user, String emailAddress){
    // TODO: There have to be some thread safety synchronization issues here
    // Wait as long as indicated
    // This could require multiple waits if lots of traffic
    while (true){
      long delay = recordEmailSend(emailAddress);
      if (delay == 0)
        return;
      log.info("Waiting " + delay + " ms. for mail access for user " + user.id + " for email address " + emailAddress);
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e){
        // Ignore
        e.printStackTrace();
        log.info("InterruptedException in MailAccessManager.wait - " + e);
      }
    }
  }
}
