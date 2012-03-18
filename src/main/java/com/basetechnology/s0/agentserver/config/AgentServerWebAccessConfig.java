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

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessConfig;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessManager;

public class AgentServerWebAccessConfig extends WebAccessConfig {
  public AgentServerConfig config;

  public AgentServerWebAccessConfig(AgentServerConfig config){
    super(config.getLong("minimum_web_access_interval"),
        config.getLong("minimum_web_site_access_interval"),
        config.getLong("default_web_page_refresh_interval"),
        config.getLong("minimum_web_page_refresh_interval"),
        config.get("user_agent_name"),
        config.getBoolean("implicitly_deny_web_access"));
    this.config = config;
  }
  
  public boolean getImplicitlyDenyWebAccess(){
    return config.getBoolean("implicitly_deny_web_access");
  }
  
  public long getDefaultWebPageRefreshInterval(){
    return config.getLong("default_web_page_refresh_interval");
  }
  
  public long getMinimumWebPageRefreshInterval(){
    return config.getLong("minimum_web_page_refresh_interval");
  }
  
  public long getMinimumWebAccessInterval(){
    return config.getLong("minimum_web_access_interval");
  }
  
  public long getMinimumWebSiteAccessInterval(){
    return config.getLong("minimum_web_site_access_interval");
  }

  public String getUserAgentName(){
    return config.get("user_agent_name");
  }
  
  public void setImplicitlyDenyWebAccess(boolean implicitlyDenyWebAccess) throws Exception {
    config.put("implicitly_deny_web_access", implicitlyDenyWebAccess ? "true" : "false");
  }
  
  public void setDefaultWebPageRefreshInterval(long defaultWebPageRefreshInterval) throws Exception {
    config.put("default_web_page_refresh_interval",
        defaultWebPageRefreshInterval >= 0 ? defaultWebPageRefreshInterval :
          WebAccessManager.DEFAULT_DEFAULT_WEB_PAGE_REFRESH_INTERVAL);
  }
  
  public void setMinimumWebPageRefreshInterval(long minimumWebPageRefreshInterval) throws Exception {
    config.put("minimum_web_page_refresh_interval",
        minimumWebPageRefreshInterval >= 0 ? minimumWebPageRefreshInterval :
          WebAccessManager.DEFAULT_MINIMUM_WEB_PAGE_REFRESH_INTERVAL);
  }
  
  public void setMinimumWebAccessInterval(long minimumWebAccessInterval) throws Exception {
    config.put("minimum_web__access_interval",
        minimumWebAccessInterval >= 0 ? minimumWebAccessInterval :
          WebAccessManager.DEFAULT_MINIMUM_WEB_ACCESS_INTERVAL);
  }
  
  public void setMinimumWebSiteAccessInterval(long minimumWebSiteAccessInterval) throws Exception {
    config.put("minimum_web_site_access_interval",
        minimumWebSiteAccessInterval >= 0 ? minimumWebSiteAccessInterval :
          WebAccessManager.DEFAULT_MINIMUM_WEB_SITE_ACCESS_INTERVAL);
  }

  public void setUserAgentName(String userAgentName) throws AgentServerException {
    config.put("user_agent_name",
        userAgentName != null ? userAgentName : WebAccessManager.DEFAULT_USER_AGENT_NAME);
  }

}
