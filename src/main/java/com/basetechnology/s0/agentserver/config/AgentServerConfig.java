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

package com.basetechnology.s0.agentserver.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.mailaccessmanager.MailAccessManager;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentFileException;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.JsonUtils;
import com.basetechnology.s0.agentserver.util.ListMap;

public class AgentServerConfig {
  static final Logger log = Logger.getLogger(AgentServerConfig.class);
  public AgentServer agentServer;
  public ListMap<String, String> config;
  public boolean batchUpdate;
  public AgentServerProperties agentServerProperties;

  public AgentServerConfig(AgentServer agentServer) throws AgentServerException {
    this.agentServer = agentServer;
    this.config = new ListMap<String, String>();
    this.batchUpdate = false;
    this.agentServerProperties = agentServer.agentServerProperties == null ?
        new AgentServerProperties() : agentServer.agentServerProperties;
  }

  public void load() throws IOException, PersistentFileException, AgentServerException {
    log.info("Loading config table");
    // Load the table of config settings
    if (agentServer.persistence != null)
      config = agentServer.persistence.get("config");
    else
      config = new ListMap<String, String>();
    
    // But if it is not initialized, reset to defaults now
    if (config.size() == 0)
      restoreDefaults();
  }
  
  public String get(String key){
    return config.get(key);
  }
  
  public boolean getBoolean(String key){
    String value = config.get(key);
    if (value == null)
      return false;
    value = value.trim();
    return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") ||
        value.equalsIgnoreCase("on") || value.equalsIgnoreCase("enabled"); 
  }
  
  public int getInt(String key){
    return Integer.parseInt(config.get(key));
  }
  
  public long getLong(String key){
    return Long.parseLong(config.get(key));
  }
  
  public void persist(String key) throws AgentServerException {
    agentServer.persistence.put("config", key, config.get(key));
  }
  
  public void put(String key, Object value) throws AgentServerException{
    config.put(key, value.toString());
    persist(key);
  }
  
  public void update(JSONObject json) throws AgentServerException, JSONException {
    // First validate the keys
    JsonUtils.validateKeys(json, "config", new ArrayList<String>(Arrays.asList(
        "name", "description", "software", "version", "website", "contact",
        "user_agent_name", "default_web_page_refresh_interval",
        "minimum_web_page_refresh_interval", "minimum_web_site_access_interval",
        "minimum_web_access_interval", "max_users", "max_instances",
        "execution_limit_level_1", "execution_limit_level_2",
        "execution_limit_level_3", "execution_limit_level_4",
        "execution_limit_default_level",
        "default_trigger_interval", "default_reporting_interval",
        "minimum_trigger_interval", "minimum_reporting_interval",
        "implicitly_deny_web_access",
        "mail_access_enabled", "minimum_mail_access_interval",
        "minimum_host_mail_access_interval", "minimum_address_mail_access_interval",
        "admin_approve_user_create", "mail_confirm_user_create",
        "default_limit_instance_states_stored", "maximum_limit_instance_states_stored",
        "default_limit_instance_states_returned", "maximum_limit_instance_states_returned")));
    
    // Now simply copy the keys to the config map
    for (Iterator<String> it = json.keys(); it.hasNext(); ){
      String key = it.next();
      // TODO/Note: This will update persistence one key at a time
      put(key, json.getString(key));
    }
  }
  
  public String getContact(){
    return get("contact");
  }
  
  public String getWebsite(){
    return get("website");
  }
 
  public int getDefaultExecutionLevel(){
    return getInt("execution_limit_default_level");
  }

  public int getExecutionLimit(int level){
    return getInt(ScriptState.nodeExecutionLevelKeys.get(level - 1));
  }

  public int getDefaultExecutionLimit(){
    return getExecutionLimit(getDefaultExecutionLevel());
  }
  
  public boolean getMailAccessEnabled(){
    return getBoolean("mail_access_enabled");
  }

  public String getReportingInterval(){
    String reportingInterval = get("reporting_interval");
    if (reportingInterval == null || reportingInterval.trim().length() == 0)
      return AgentDefinition.DEFAULT_REPORTING_INTERVAL_EXPRESSION;
    else
      return reportingInterval;
  }

  public String getTriggerInterval(){
    String triggerInterval = get("trigger_interval");
    if (triggerInterval == null || triggerInterval.trim().length() == 0)
      return AgentDefinition.DEFAULT_TRIGGER_INTERVAL_EXPRESSION;
    else
      return triggerInterval;
  }

  public int getDefaultLimitInstanceStatesStored() {
    String defaultLimitInstanceStatesStoredString = get("default_limit_instance_states_stored");
    if (defaultLimitInstanceStatesStoredString == null || defaultLimitInstanceStatesStoredString.trim().length() == 0)
      return AgentInstance.DEFAULT_LIMIT_INSTANCE_STATES_STORED;
    else
      return Integer.parseInt(defaultLimitInstanceStatesStoredString);
  }

  public int getMaximumLimitInstanceStatesStored() {
    String maximumLimitInstanceStatesStoredString = get("maximum_limit_instance_states_stored");
    if (maximumLimitInstanceStatesStoredString == null || maximumLimitInstanceStatesStoredString.trim().length() == 0)
      return AgentInstance.DEFAULT_MAXIMUM_LIMIT_INSTANCE_STATES_STORED;
    else
      return Integer.parseInt(maximumLimitInstanceStatesStoredString);
  }

  public int getDefaultLimitInstanceStatesReturned() {
    String defaultLimitInstanceStatesReturnedString = get("default_limit_instance_states_returned");
    if (defaultLimitInstanceStatesReturnedString == null || defaultLimitInstanceStatesReturnedString.trim().length() == 0)
      return AgentInstance.DEFAULT_LIMIT_INSTANCE_STATES_RETURNED;
    else
      return Integer.parseInt(defaultLimitInstanceStatesReturnedString);
  }

  public int getMaximumLimitInstanceStatesReturned() {
    String maximumLimitInstanceStatesReturnedString = get("maximum_limit_instance_states_returned");
    if (maximumLimitInstanceStatesReturnedString == null || maximumLimitInstanceStatesReturnedString.trim().length() == 0)
      return AgentInstance.DEFAULT_MAXIMUM_LIMIT_INSTANCE_STATES_RETURNED;
    else
      return Integer.parseInt(maximumLimitInstanceStatesReturnedString);
  }
  
  public void putDefaultExecutionLevel(int level) throws AgentServerException{
    put("execution_limit_default_level", level);
  }
  
  public void restoreDefaults() throws AgentServerException {
    log.info("Setting defaults for config properties");
    this.config = new ListMap<String, String>();
    
    put("name", agentServerProperties.agentServerName);
    put("description", agentServerProperties.agentServerDescription);
    put("software", "s0");
    put("version", "0.1.0");
    put("website", agentServerProperties.website);
    put("admin_approve_user_create", agentServerProperties.adminApproveUserCreate);
    put("mail_confirm_user_create", agentServerProperties.mailConfirmUserCreate);
    put("contact", agentServerProperties.supportContactEmail);
    put("user_agent_name", agentServerProperties.userAgentName);
    put("default_web_page_refresh_interval", agentServerProperties.defaultWebPageRefreshInterval);
    put("minimum_web_page_refresh_interval", agentServerProperties.minimumWebPageRefreshInterval);
    put("minimum_web_site_access_interval", agentServerProperties.minimumWebSiteAccess_interval);
    put("minimum_web_access_interval", agentServerProperties.minimumWebAccessInterval);
    put("execution_limit_level_1", agentServerProperties.execution_limit_level_1);
    put("execution_limit_level_2", agentServerProperties.executionLimitLevel2);
    put("execution_limit_level_3", agentServerProperties.executionLimitLevel3);
    put("execution_limit_level_4", agentServerProperties.executionLimitLevel4);
    put("execution_limit_default_level", agentServerProperties.executionLimitDefaultLevel);
    put("max_users", agentServerProperties.maxUsers);
    put("max_instances", agentServerProperties.maxInstances);
    put("implicitly_deny_web_access", agentServerProperties.implicitlyDenyWebAccess);
    put("default_trigger_interval", agentServerProperties.defaultTriggerInterval);
    put("default_reporting_interval", agentServerProperties.defaultReportingInterval);
    put("minimum_trigger_interval", agentServerProperties.minimumTriggerInterval);
    put("minimum_reporting_interval", agentServerProperties.minimumReportingInterval);
    put("default_limit_instance_states_stored", agentServerProperties.defaultLimitInstanceStatesStored);
    put("maximum_limit_instance_states_stored", agentServerProperties.maximumLimitInstanceStatesStored);
    put("default_limit_instance_states_returned", agentServerProperties.defaultLimitInstanceStatesReturned);
    put("maximum_limit_instance_states_returned", agentServerProperties.maximumLimitInstanceStatesReturned);
    // TODO: How to handle directory since we can't read the config file until we know the directory
    // Probably needs to be a command line or environment variable, maybe both
    
    // Set defaults for mail access manager
    MailAccessManager.setConfigDefaults(this);
  }
  
  public JSONObject toJson() throws JSONException {
    JSONObject configJson = new JsonListMap();
    for(String key: config)
      configJson.put(key, config.get(key));
    return configJson;
  }
  
  public String toString(){
    return "AgentServerConfig " + config;
  }
}
