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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.util.ListMap;

public class WebAccessManager {
  static final Logger log = Logger.getLogger(WebAccessManager.class);
  public WebAccessConfig config;
  public WebSiteAccessConfig siteConfig;
  public static final String DEFAULT_USER_AGENT_NAME = "AgentServer";
  public ListMap<String, WebSite> webSites;
  public ListMap<String, WebPageCache> webPageCache;
  public static final long DEFAULT_MINIMUM_WEB_ACCESS_INTERVAL = 100;
  public static final long DEFAULT_DEFAULT_WEB_PAGE_REFRESH_INTERVAL = 60 * 1000;
  public static final long DEFAULT_MINIMUM_WEB_PAGE_REFRESH_INTERVAL = 60 * 1000;
  public static final long DEFAULT_MINIMUM_WEB_SITE_ACCESS_INTERVAL = 60 * 1000;
  public static final boolean DEFAULT_IMPLICITLY_DENY_WEB_ACCESS = false;
  public long lastAccess;

  public WebAccessManager(WebAccessConfig config, WebSiteAccessConfig siteConfig){
    this.config = config;
    this.siteConfig = siteConfig;
    clear();
  }

  public void clear(){
    this.webSites = new ListMap<String, WebSite>();
    this.webPageCache = new ListMap<String, WebPageCache>();
    this.lastAccess = 0;
  }
  
  public WebSite getWebSite(String url){
    String webSiteUrl = null;
    try {
      URL tempUrl = new URL(url);

      String protocol = tempUrl.getProtocol();
      String host = tempUrl.getHost();
      int port = tempUrl.getPort();
      webSiteUrl = protocol + "://" + host + (port > 0 ? ":" + port : "") + "/";
    } catch (MalformedURLException e) {
      throw new InvalidUrlException(e.getMessage());
    }
    
    // Check if we already know about this site
    if (! webSites.containsKey(webSiteUrl)){
      // No, create a new WebSite entry
      WebSite webSite = new WebSite(this, webSiteUrl);
      webSites.put(webSiteUrl, webSite);
    }
    
    // Return the WebSite for this URL
    return webSites.get(webSiteUrl);
  }

  public WebPage getWebPage(String userId, String url){
    return getWebPage(userId, url, true, -1, true);
  }
  
  public WebPage getWebPage(String userId, String url, boolean useCache, long refreshInterval, boolean wait){
    // Check if already in the web page cache
    WebSite webSite = null;
    if (useCache){
      WebPageCache pageCache = webPageCache.get(url);
      if (pageCache != null){
        // It's in the cache, but is it still fresh enough?
        WebPage webPage = pageCache.webPage;
        webSite = webPage.webSite;

        if (refreshInterval < 0)
          refreshInterval = webPage.refreshInterval;
        long now = System.currentTimeMillis();
        long delta = now - webPage.time;
        if (delta < refreshInterval || ! webSite.getRobot().isAccessAllowedNow()){
          // Fresh enough to keep using the page from the cache, or robots.txt or admin say no
          // to another read of the web site right now
          webSite.numReads++;
          return webPage;
        }
      }
    }

    // Not in cache; need to fetch the page
    if (webSite == null)
      webSite = getWebSite(url);

    // Check if this user is even allowed to access the web site
    if (siteConfig != null && ! siteConfig.isAccessAllowed(webSite, userId))
      throw new WebSiteAccessDeniedException("User " + userId + " is not permitted to access web site " + webSite.url);

    // Access granted, count the read accesses for this site
    webSite.numReads++;

    // Read the web page
    WebPage webPage = webSite.getWebPage(url, refreshInterval, wait);
    
    // Add it to the cache
    webPageCache.put(url, new WebPageCache(webPage, refreshInterval));
    
    // Return the new page
    return webPage;
  }

  public WebPage postUrl(String userId, String url, String data){
    return postUrl(userId, url, data, -1, true);
  }
  
  public WebPage postUrl(String userId, String url, String data, long refreshInterval, boolean wait){
    // Get info for the web site
    WebSite webSite = getWebSite(url);

    // Check if this user is even allowed to access the web site
    if (siteConfig != null && ! siteConfig.isAccessAllowed(webSite, userId))
      throw new WebSiteAccessDeniedException("User " + userId + " is not permitted to access web site " + webSite.url);

    // Access granted, count the read accesses for this site
    webSite.numReads++;

    // Post to the URL
    WebPage webPage = webSite.postUrl(url, data, refreshInterval, wait);
    
    // Add it to the cache
    webPageCache.put(url, new WebPageCache(webPage, refreshInterval));
    
    // Return the new page
    return webPage;
  }

  public void addWebSiteAccessControls(String userId, JSONObject accessControlsJson) throws JSONException, AgentServerException{
    siteConfig.addWebSiteAccessControls(userId, accessControlsJson);
  }
  
  public ListMap<String, String> getWebSiteAccessControls(User user) throws JSONException, AgentServerException{
    return siteConfig.getWebSiteAccessControls(user.id);
  }

}
