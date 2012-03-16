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

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentFileException;
import com.basetechnology.s0.agentserver.util.JsonListMap;
import com.basetechnology.s0.agentserver.util.ListMap;

public class WebSiteAccessConfig {
  public AgentServer agentServer;
  public ListMap<String, ListMap<String, String>> webSiteAccess;
  public boolean localImplicitDeny;
  
  public WebSiteAccessConfig(AgentServer agentServer){
    this.agentServer = agentServer;
    this.webSiteAccess = new ListMap<String, ListMap<String, String>>();
    this.localImplicitDeny = agentServer == null ? false :
      agentServer.webAccessConfig.getImplicitlyDenyWebAccess();
  }

  public void load() throws IOException, PersistentFileException, AgentServerException {
    // Load list of all access lists
    if (agentServer == null)
      // No-op if no agent server
      return;
    
    // Get the full list of keys and their values from the webaccess table in the persistent store
    ListMap<String, String> siteAccess = agentServer.persistence.get("webaccess");
    
    // Empty out any existing table
    webSiteAccess.clear();
    
    // Iterate site by site
    for (String url: siteAccess){
      // Get the text of the access list for this site
      String accessListString = siteAccess.get(url);
      
      // Parse the access list as JSON source text - a map of user Id to access verb
      try {
        // Parse the map
        JSONObject accessListJson = new JSONObject(accessListString);
    
        // Create a new access list for this site
        ListMap<String, String> accessList = new ListMap<String, String>();
        
        // Now process the entries on the list, user by user
        for (Iterator<String> it = accessListJson.keys(); it.hasNext(); ){
          // Get the user Id and access verb for next entry in the list
          String userId = it.next();
          String verb = accessListJson.getString(userId);
          // TODO: Consider validating user Id - and whether to simply ignore or warn of bad ones

          // Add the user to the access list for the site
          accessList.put(userId, verb);
        }

        // Now add the access list for the site to full table
        webSiteAccess.put(url, accessList);
      } catch (JSONException e){
        throw new AgentServerException("Unable to parse JSON access list for website " + url + " - " + e.getMessage());
      }
    }

  }

  public void addAccess(WebSite webSite, String userId, String verb) throws JSONException, AgentServerException{
    // Validate parameters
    if (webSite == null)
      throw new WebAccessException("Null WebSite parameter for addAccess");
    if (userId == null)
      throw new WebAccessException("Null user Id parameter for addAccess");
    if (userId.trim().length() == 0)
      throw new WebAccessException("Empty user Id parameter for addAccess");
    if (verb == null)
      throw new WebAccessException("Null verb parameter for addAccess");
    if (verb.trim().length() == 0)
      throw new WebAccessException("Empty verb parameter for addAccess");
    if (! verb.equalsIgnoreCase("grant") && ! verb.equalsIgnoreCase("deny"))
      throw new WebAccessException("Verb parameter for addAccess is not 'grant' or 'deny': " + verb);
    
    // Get the URL for the site
    String url = webSite.url;
    
    // Do we have any entries yet for the site?
    if (! webSiteAccess.containsKey(url))
      // No, create an empty access list for the site
      webSiteAccess.put(url, new ListMap<String, String>());
    
    // Get the access list for the site (may be empty)
    ListMap<String, String> accessList = webSiteAccess.get(url);
    
    // Add/replace access entry for the user
    accessList.put(userId, verb);
    
    // Persist any changes to access control for the web site
    if (agentServer != null){
      JSONObject accessListJson = new JsonListMap();
      for (String userId1: accessList)
        accessListJson.put(userId1, accessList.get(userId1));
      agentServer.persistence.put("webaccess", url, accessListJson.toString());
    }
  }

  
  public void addWebSiteAccessControls(String userId, JSONObject accessControlsJson) throws JSONException, AgentServerException{
    // Process the list one URL at a time
    for (Iterator<String> it = accessControlsJson.keys(); it.hasNext(); ){
      String url = it.next();
      Object verbObject = accessControlsJson.opt(url);
      String verb = null;
      if (verbObject instanceof String)
        verb = (String)verbObject;
      if (verbObject instanceof Long)
        verb = ((Long)verbObject).toString();
      else if (verbObject instanceof Double)
        verb = ((Double)verbObject).toString();
      WebSite webSite = agentServer.webAccessManager.getWebSite(url);
      addAccess(webSite, userId, verb);
    }
    
  }
  
  public ListMap<String, String> getWebSiteAccessControls(String userId){
    // Build list of all web site URLs which this user has an access entry for
    ListMap<String, String> accessList = new ListMap<String, String>();
    for(String url: webSiteAccess){
      ListMap<String, String> urlAccessList = webSiteAccess.get(url);
      for (String userId2: urlAccessList)
        if (userId2.equals(userId))
          accessList.put(url, urlAccessList.get(userId));
    }
    return accessList;
  }

  public String getAccess(WebSite webSite, String userId){
    // Validate parameters
    if (webSite == null)
      throw new WebAccessException("Null WebSite parameter for getAccess");
    if (userId == null)
      throw new WebAccessException("Null user Id parameter for getAccess");
    if (userId.trim().length() == 0)
      throw new WebAccessException("Empty user Id parameter for getAccess");

    // Do we even have an access table yet?
    if (webSiteAccess == null)
      // No, so we don't know the access
      return null;

    // Get the access list for the web site
    ListMap<String, String> access = webSiteAccess.get(webSite.url);

    // Get and return the access entry for the user Id
    if (access == null)
      // No entry for the user, so we don't know
      return null;
    String entry = access.get(userId);
    return entry;
  }

  public void clear(){
    // Clear all site entries
    webSiteAccess.clear();
  }

  public boolean isAccessAllowed(String url, String userId){
    return isAccessAllowed(agentServer.webAccessManager.getWebSite(url), userId);
  }
  
  public boolean isAccessAllowed(WebSite webSite, String userId){
    // Validate parameters
    if (webSite == null)
      throw new WebAccessException("Null WebSite parameter for isAccessAllowed");
    if (userId == null)
      throw new WebAccessException("Null user Id parameter for isAccessAllowed");
    if (userId.trim().length() == 0)
      throw new WebAccessException("Empty user Id parameter for isAccessAllowed");
    
    // Get the access list for this site, if none, use global implicit deny option
    if (webSiteAccess != null){
      ListMap<String, String> access = webSiteAccess.get(webSite.url);

      // If no entry for this site, access is granted unless global implicit deny option is enabled
      if (access != null){
        // Check if this user has an entry
        if (access.containsKey(userId)){
          // Yes, get it
          String entry = access.get(userId);
          if (entry.equalsIgnoreCase("grant"))
            // This user is explicitly granted access to this site
            return true;
          else if (entry.equalsIgnoreCase("deny"))
            // This user is explicitly denied access to this site
            return false;
          // TODO: Can't get here?
        }

        // Check if "*" has an entry
        if (access.containsKey("*")){
          // Yes, get it
          String entry = access.get("*");
          if (entry.equalsIgnoreCase("grant"))
            // All users are explicitly granted access to this site (unless a user was explicitly denied)
            return true;
          else if (entry.equalsIgnoreCase("deny"))
            // This user is explicitly denied access to this site
            return false;
          // TODO: Can't get here?
        }
      }

      // If no explicit entry, check for the "*" URL as the explicit implicit default
      access = webSiteAccess.get("*");

      // If no entry for URL "*", access is granted unless global implicit deny option is enabled
      if (access != null){
        // Check if this user has an entry
        if (access.containsKey(userId)){
          // Yes, get it
          String entry = access.get(userId);
          if (entry.equalsIgnoreCase("grant"))
            // This user is explicitly granted access to this site
            return true;
          else if (entry.equalsIgnoreCase("deny"))
            // This user is explicitly denied access to this site
            return false;
          // TODO: Can't get here?
        }

        // Check if "*" has an entry
        if (access.containsKey("*")){
          // Yes, get it
          String entry = access.get("*");
          if (entry.equalsIgnoreCase("grant"))
            // All users are explicitly granted access to this site (unless a user was explicitly denied)
            return true;
          else if (entry.equalsIgnoreCase("deny"))
            // This user is explicitly denied access to this site
            return false;
          // TODO: Can't get here?
        }
      }

    }
    
    // If none of the above, treat as implicit "grant", unless the implicit "deny" config option is set
    if (agentServer == null)
      // If no agent server, use local version of the implicit deny option
      return ! localImplicitDeny;
    else
      return ! agentServer.webAccessConfig.getImplicitlyDenyWebAccess();
  }
}
