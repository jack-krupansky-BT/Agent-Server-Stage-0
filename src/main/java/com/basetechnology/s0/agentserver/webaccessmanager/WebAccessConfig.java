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

public class WebAccessConfig {
  protected long minimumWebAccessInterval;
  protected long minimumWebSiteAccessInterval;
  protected long defaultWebPageRefreshInterval;
  protected long minimumWebPageRefreshInterval;
  protected String userAgentName;
  protected boolean implicitlyDenyWebAccess;
  protected boolean implicitlyDenyWebWriteAccess;

  public WebAccessConfig(
      long minimumWebAccessInterval,
      long minimumWebSiteAccessInterval,
      long defaultWebPageRefreshInterval,
      long minimumWebPageRefreshInterval,
      String userAgentName,
      boolean implicitlyDenyWebAccess,
      boolean implicitlyDenyWebWriteAccess){
    this.minimumWebAccessInterval = minimumWebAccessInterval;
    this.minimumWebSiteAccessInterval = minimumWebSiteAccessInterval;
    this.defaultWebPageRefreshInterval = defaultWebPageRefreshInterval;
    this.minimumWebPageRefreshInterval = minimumWebPageRefreshInterval;
    this.userAgentName = userAgentName;
    this.implicitlyDenyWebAccess = implicitlyDenyWebAccess;
    this.implicitlyDenyWebWriteAccess = implicitlyDenyWebWriteAccess;
  }
  
  public boolean getImplicitlyDenyWebAccess(){
    return implicitlyDenyWebAccess;
  }
  
  public boolean getImplicitlyDenyWebWriteAccess(){
    return implicitlyDenyWebWriteAccess;
  }
  
  public long getDefaultWebPageRefreshInterval(){
    return defaultWebPageRefreshInterval;
  }
  
  public long getMinimumWebPageRefreshInterval(){
    return minimumWebPageRefreshInterval;
  }
  
  public long getMinimumWebAccessInterval(){
    return minimumWebAccessInterval;
  }
  
  public long getMinimumWebSiteAccessInterval(){
    return minimumWebSiteAccessInterval;
  }

  public String getUserAgentName(){
    return userAgentName;
  }
  
  public void setImplicitlyDenyWebAccess(boolean implicitlyDenyWebAccess) throws Exception {
    this.implicitlyDenyWebAccess = implicitlyDenyWebAccess;
  }
  
  public void setImplicitlyDenyWebWriteAccess(boolean implicitlyDenyWebWriteAccess) throws Exception {
    this.implicitlyDenyWebAccess = implicitlyDenyWebWriteAccess;
  }
  
  public void setDefaultWebPageRefreshInterval(long defaultWebPageRefreshInterval) throws Exception {
    this.defaultWebPageRefreshInterval =
        defaultWebPageRefreshInterval >= 0 ? defaultWebPageRefreshInterval :
          WebAccessManager.DEFAULT_DEFAULT_WEB_PAGE_REFRESH_INTERVAL;
  }
  
  public void setMinimumWebPageRefreshInterval(long minimumWebPageRefreshInterval) throws Exception {
    this.minimumWebPageRefreshInterval =
        minimumWebPageRefreshInterval >= 0 ? minimumWebPageRefreshInterval :
          WebAccessManager.DEFAULT_MINIMUM_WEB_PAGE_REFRESH_INTERVAL;
  }
  
  public void setMinimumWebAccessInterval(long minimumWebAccessInterval) throws Exception {
    this.minimumWebAccessInterval =
        minimumWebAccessInterval >= 0 ? minimumWebAccessInterval :
          WebAccessManager.DEFAULT_MINIMUM_WEB_SITE_ACCESS_INTERVAL;
  }
  
  public void setMinimumWebSiteAccessInterval(long minimumWebSiteAccessInterval) throws Exception {
    this.minimumWebSiteAccessInterval =
        minimumWebSiteAccessInterval >= 0 ? minimumWebSiteAccessInterval :
          WebAccessManager.DEFAULT_MINIMUM_WEB_SITE_ACCESS_INTERVAL;
  }

  public void setUserAgentName(String userAgentName) throws Exception {
    this.userAgentName =
        userAgentName != null ? userAgentName : WebAccessManager.DEFAULT_USER_AGENT_NAME;
  }

}
