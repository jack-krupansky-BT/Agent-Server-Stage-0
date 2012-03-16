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
import java.util.ArrayList;
import java.util.List;

public class RobotRecord {
  public String userAgentName;
  public List<String> allowPaths;
  public List<String> disallowPaths;
  public List<String> siteMaps;
  public long crawlDelay;
  
  public RobotRecord(){
    this(null, null, null, null);
  }
  
  public RobotRecord(String userAgentName, List<String> allowPaths, List<String> disallowPaths, List<String> siteMaps){
    this.userAgentName = userAgentName;
    this.allowPaths = allowPaths == null ? new ArrayList<String>() : allowPaths;
    this.disallowPaths = disallowPaths == null ? new ArrayList<String>() : disallowPaths;
    this.siteMaps = siteMaps == null ? new ArrayList<String>() : siteMaps;
    this.crawlDelay = 0;
  }
  
  public boolean isAccessAllowed(String url) throws InvalidUrlException {
    // TODO: SOmehow, eliminate this total hack or ignoring robots.txt if it only has Disallow: /
    // It is needed because the Twitter API robots.txt has "Disallow: /"
    if (disallowPaths.size() >= 1 && disallowPaths.get(0).equals("/"))
      return true;
    
    // Get the path from the URL
    String path = null;
    try {
      URL urlUrl = new URL(url);
      path = urlUrl.getPath();
    } catch (MalformedURLException e) {
      throw new InvalidUrlException("Unable to parse URL: " + url);
    }

    // First check if there is an 'allow' that covers the path
    String lowerPath = path.toLowerCase();
    for (String allowPath: allowPaths)
      if (lowerPath.startsWith(allowPath.toLowerCase()))
        // Yes, no need to check disallows
        // TODO: Verify if that is really true
        return true;

    // Then check if there is a 'disallow' that covers the path
    for (String disallowPath: disallowPaths)
      if (lowerPath.startsWith(disallowPath.toLowerCase()))
        // Yes, access is disallowed
        return false;
    
    // Path survived all the exclusion checks, so access is granted 
    return true;
  }

  public String toString(){
    return "RobotRecord User-agent: " + userAgentName +
        " Allow: " + allowPaths + " Disallow: " + disallowPaths +
        " Crawl-Delay: " + crawlDelay + " Sitemap: " + siteMaps;
  }
}
