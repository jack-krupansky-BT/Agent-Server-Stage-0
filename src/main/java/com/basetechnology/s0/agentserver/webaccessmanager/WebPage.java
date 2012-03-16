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

import org.apache.http.HttpResponse;

import com.basetechnology.s0.agentserver.util.DateUtils;

public class WebPage {
  public WebSite webSite;
  public String url;
  public long refreshInterval = WebAccessManager.DEFAULT_DEFAULT_WEB_PAGE_REFRESH_INTERVAL;
  public long time;
  public HttpResponse response;
  public int statusCode;
  public String reasonPhrase;
  public String text;

  // TODO: Should include content type and HTTP header info
  
  public WebPage(WebSite webSite, String url, long refreshInterval, long time, HttpResponse response, int statusCode,
      String reasonPhrase, String text){
    this.webSite = webSite;
    this.url = url;
    this.refreshInterval = refreshInterval;
    this.time = time;
    this.response = response;
    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;
    this.text = text;
  }
  
  public String toString(){
    return "WebPage URL: " + url + " refreshInterval: " + refreshInterval +
        " time: " + DateUtils.toRfcString(time) + "length: " + text.length() + " response: " + response;
  }
}
