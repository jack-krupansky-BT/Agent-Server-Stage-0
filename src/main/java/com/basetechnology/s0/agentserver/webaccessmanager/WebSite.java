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

public class WebSite {
  public WebAccessManager webAccessManager;
  public String url;
  public Robot robot;
  public int numReads = 0;
  public int numWrites = 0;
  public int numWebAccesses = 0;
  public int numWebGets = 0;
  public int numWebPosts = 0;
  public int numWebPuts = 0;
  public int numWebDeletes = 0;
  
  public WebSite(WebAccessManager webAccessManager, String url){
    this.webAccessManager = webAccessManager;
    this.url = url;
    this.robot = null;
    // TODO: Should we process robots.txt immediately, or on first need for it?
  }

  public Robot getRobot(){
    if (robot != null)
      return robot;
    else {
      robot = new Robot(this);
      return robot;
    }
  }
  
  // TODO: Think about whether caller should check cache, or this function?
  public WebPage getWebPage(String url, long refreshInterval, boolean wait){
    return getRobot().getWebPage(url, refreshInterval, wait);
  }

  public WebPage postUrl(String url, String data, long refreshInterval, boolean wait){
    return getRobot().postUrl(url, data, refreshInterval, wait);
  }
  
  public String toString(){
    return "WebSite URL: " + url + " robot: " + robot;
  }
}
