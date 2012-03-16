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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.ListMap;

public class Robot {
  static final Logger log = Logger.getLogger(Robot.class);
  public WebSite webSite;
  public long crawlDelayMs;
  public long lastAccess;
  public ListMap<String, RobotRecord> robotRecords;
  
  public Robot(WebSite webSite){
    this.webSite = webSite;
    this.robotRecords = new ListMap<String, RobotRecord>();
    
    //Now parse the robots.txt file, if any, for the site
    parseRobotsFile();
  }
  
  // TODO: Where to store/get access time for frequency checking
  
  // TODO: Also check that frequency doesn't exceed admin limit for agent server
  // Although, that should merely delay the response

  public void parseRobotsFile(){
    // Read robots.txt
    String text = getWebPageText(webSite.url + "robots.txt");
    if (text == null)
      // Okay to have a missing or emoty file
      return;
    
    int len = text.length();
    boolean sawRecordEnd = false;
    RobotRecord tempRecord = new RobotRecord();
    for (int i = 0, recordNo = 0; i < len; i++){
      // Blank line indicates end of recrod and possibly start of another
      char ch = text.charAt(i);
      if (ch == '\r'){
        // Check for CRNL as opposed to simply CR
        if (i < len - 1 && text.charAt(i + 1) == '\n')
          i++;

        // Remember that we saw end of record
        sawRecordEnd = true;
        continue;
      } else if (ch == '\n'){
        // Remember that we saw end of record
        sawRecordEnd = true;
        continue;
      } else if (ch == ' '){
        // Trim leading whitespace
        for (i++; i < len && text.charAt(i) == ' '; i++){}
        
        // Check for comment line
        if (i < len && text.charAt(i) == '#'){
          // Skip the comment - up through end of line
          i--;
          continue;
        }
      } else if (ch == '#'){
        // Skip comment line
        for (i++; i < len && (ch = text.charAt(i)) != '\r' && ch != '\n'; i++){}
        if (ch == '\r'){
          // Check for CRNL as opposed to simply CR
          if (i < len - 1 && text.charAt(i + 1) == '\n')
            i++;
        }
        continue;
      }

      // Scan the field name
      int j = i;
      for (i++; i < len && (ch = text.charAt(i)) != '\r' && ch != '\n' && ch != ':'; i++){}
      if (ch != ':'){
        // If no field name delimiter, simply ignore the rest of the line
        if (ch == '\r'){
          // Check for CRNL as opposed to simply CR
          if (i < len - 1 && text.charAt(i + 1) == '\n')
            i++;
        }
        continue;
      }
      String fieldName = text.substring(j, i).trim();
      
      // Scan the field value
      j = ++i;
      for (; i < len && (ch = text.charAt(i)) != '\r' && ch != '\n'; i++){}
      if (ch == '\r'){
        // Check for CRNL as opposed to simply CR
        if (i < len - 1 && text.charAt(i + 1) == '\n')
          i++;
      }
      String fieldValue = text.substring(j, i).trim();
      
      // If either field name or field value is empty, skip the line
      if (fieldName.length() == 0 || fieldValue.length() == 0)
        continue;
      
      // If this is the first field after we saw a record-ending blank line, start a new record
      if (sawRecordEnd){
        // First store the previous record
        // Ignore if no user agent name
        if (tempRecord.userAgentName != null)
          robotRecords.put(tempRecord.userAgentName, tempRecord);
        
        // Start a new record
        tempRecord = new RobotRecord();
        sawRecordEnd = false;
      }
      
      // Check and store the field
      if (fieldName.equalsIgnoreCase("User-agent"))
        tempRecord.userAgentName = fieldValue;
      else if (fieldName.equalsIgnoreCase("Allow"))
        tempRecord.allowPaths.add(fieldValue);
      else if (fieldName.equalsIgnoreCase("Disallow"))
        tempRecord.disallowPaths.add(fieldValue);
      else if (fieldName.equalsIgnoreCase("Sitemap"))
        tempRecord.siteMaps.add(fieldValue);
      else if (fieldName.equalsIgnoreCase("Crawl-delay")){
        try {
          tempRecord.crawlDelay = Long.parseLong(fieldValue);
        } catch (Exception e){
          // Ignore the field if not parseable
        }
      } else {
        // Ignore any unrecognized field name
      }
    }
    
    // Store current record if user-agent name set
    if (tempRecord.userAgentName != null)
      robotRecords.put(tempRecord.userAgentName, tempRecord);
  }
  
  public boolean isAccessAllowed(String url) throws InvalidUrlException {
    RobotRecord robotRecord = getRobotRecord();
    if (robotRecord != null) {
      // Save the Crawl-delay, if any, for this record
      crawlDelayMs = robotRecord.crawlDelay * 1000;
      return robotRecord.isAccessAllowed(url);
    } else {
      // No robot record, so crawl is implicitly allowed and with no delay
      crawlDelayMs = 0;
      return true;
    }
  }

  public RobotRecord getRobotRecord(){
    String userAgentName = webSite.webAccessManager.config.getUserAgentName();
    if (userAgentName != null && userAgentName.length() > 0 && robotRecords.containsCaseInsensitiveKey(userAgentName))
      return robotRecords.getCaseInsensitive(userAgentName);
    else if (robotRecords.containsKey("*"))
      return robotRecords.get("*");
    else
      return null;
  }
  
  public boolean isAccessAllowedNow(){
    // Perform throttling and optional wait, if necessary
    long now = System.currentTimeMillis();
    long delta = now - lastAccess;
    long sleepIntervalCrawl = crawlDelayMs - delta;

    // Need to factor in overall throttling
    long minOverallDelta = webSite.webAccessManager.config.getMinimumWebAccessInterval();
    long deltaOverall = now - webSite.webAccessManager.lastAccess;
    long sleepIntervalOverall = minOverallDelta - deltaOverall;

    // Need to factor in site-specific throttling as well
    long minSiteDelta = webSite.webAccessManager.config.minimumWebSiteAccessInterval;
    long sleepIntervalSite = minSiteDelta - delta;

    log.info("isAccessAllowedNow - sleepIntervalCrawl: " + sleepIntervalCrawl + " sleepIntervalOverall: " + sleepIntervalOverall + " sleepIntervalSite: " + sleepIntervalSite);

    // Which requires a longer wait, site, robots.txt, or overall system throttling?
    if (sleepIntervalOverall >= sleepIntervalSite){
      if (sleepIntervalOverall >= sleepIntervalCrawl){
        // Check if too frequent for overall throttling
        if (sleepIntervalOverall > 0){
          // This is too-frequent access
          return false;
        }
      } else {
        // Check if too frequent for robots.txt crawl throttling
        if (sleepIntervalCrawl > 0){
          // This is too-frequent access
          return false;
        }
      }
    } else {
      if (sleepIntervalSite >= sleepIntervalCrawl){
        // Check if too frequent for site throttling
        if (sleepIntervalSite > 0){
          // This is too-frequent access
          return false;
        }
      } else {
        // Check if too frequent for robots.txt crawl throttling
        if (sleepIntervalCrawl > 0){
          // Check if too frequent for robots.txt crawl throttling
          if (sleepIntervalCrawl > 0){
            // This is too-frequent access
            return false;
          }
        }
      }
    }
    
    // Okay to access page page
    return true;
  }
  
  // TODO: Who checks the cache?
  // TODO: The other verbs - POST, PUT, DELETE

  public WebPage getWebPage(String url, long refreshInterval, boolean wait) throws InterruptedException {
    // Check if access is permitted to the page at any time
    if (! isAccessAllowed(url))
      throw new RobotExclusionException("robots.txt denies access to this page: " + url);

    // Perform throttling and optional wait, if necessary
    long now = System.currentTimeMillis();
    long delta = now - lastAccess;
    long sleepIntervalCrawl = crawlDelayMs - delta;

    // Need to factor in overall throttling
    long minOverallDelta = webSite.webAccessManager.config.getMinimumWebAccessInterval();
    long deltaOverall = now - webSite.webAccessManager.lastAccess;
    long sleepIntervalOverall = minOverallDelta - deltaOverall;

    // Need to factor in site-specific throttling as well
    long minSiteDelta = webSite.webAccessManager.config.getMinimumWebSiteAccessInterval();
    long sleepIntervalSite = minSiteDelta - delta;

    log.info("crawlDelayMs: " + crawlDelayMs + " sleepIntervalCrawl: " + sleepIntervalCrawl + " sleepIntervalOverall: " + sleepIntervalOverall + " sleepIntervalSite: " + sleepIntervalSite);
    
    // Which requires a longer wait, site, robots.txt, or overall system throttling?
    if (sleepIntervalOverall >= sleepIntervalSite){
      if (sleepIntervalOverall >= sleepIntervalCrawl){
        // Check if too frequent for overall throttling
        if (sleepIntervalOverall > 0){
          // This is too-frequent access
          if (wait){
            // Sleep until we reach the minimum interval for overall web access
            log.info("Sleeping for " + sleepIntervalOverall + " ms. due to overall minimum web access interval of " + minOverallDelta + " ms. - web page URL: " + url);
            Thread.sleep(sleepIntervalOverall);
          } else
            throw new WebAccessFrequencyException("Web access is too frequent (" + deltaOverall + " ms. since last access) relative to minimum overall web access interval of " + minOverallDelta + " sec. and 'wait' option was not specified");
        }
      } else {
        // Check if too frequent for robots.txt crawl throttling
        if (sleepIntervalCrawl > 0){
          // This is too-frequent access
          if (wait){
            // Sleep until we reach the minimum interval for access to this site
            log.info("Sleeping for " + sleepIntervalCrawl + " ms. due to robots.txt Crawl-delay interval of " + crawlDelayMs / 1000 + " sec. - web page URL: " + url);
            Thread.sleep(sleepIntervalCrawl);
          } else
            throw new WebAccessFrequencyException("Web access is too frequent (" + delta + " ms. since last access) relative to robots.txt Crawl-delay of " + crawlDelayMs / 1000 + " sec. and 'wait' option was not specified");
        }
      }
    } else {
      if (sleepIntervalSite >= sleepIntervalCrawl){
        // Check if too frequent for site throttling
        if (sleepIntervalSite > 0){
          // This is too-frequent access
          if (wait){
            // Sleep until we reach the minimum interval for access to this site
            log.info("Sleeping for " + sleepIntervalSite + " ms. due to minimum site access interval of " + crawlDelayMs + " ms. - web page URL: " + url);
            Thread.sleep(sleepIntervalSite);
          } else
            throw new WebAccessFrequencyException("Web access is too frequent (" + delta + " ms. since last access) relative to minimum site access interval of " + minSiteDelta + " ms. and 'wait' option was not specified");
        }
      } else {
        // Check if too frequent for robots.txt crawl throttling
        if (sleepIntervalCrawl > 0){
          // This is too-frequent access
          if (wait){
            // Sleep until we reach the minimum interval for access to this site
            log.info("Sleeping for " + sleepIntervalCrawl + " ms. due to robots.txt Crawl-delay interval of " + crawlDelayMs / 1000 + " sec. - web page URL: " + url);
            Thread.sleep(sleepIntervalCrawl);
          } else
            throw new WebAccessFrequencyException("Web access is too frequent (" + delta + " ms. since last access) relative to robots.txt Crawl-delay of " + crawlDelayMs / 1000 + " sec. and 'wait' option was not specified");
        }
      }
    }

    // Get updated time after the wait
    now = System.currentTimeMillis();

    // Get the web page
    try {
      // Keep track of web accesses
      webSite.numWebReads++;

      // Do the GET for the specified web page
      log.info("Fetching web page: " + url);
      HttpClient httpclient = new DefaultHttpClient();
      HttpGet httpget = new HttpGet(url);
      HttpResponse response = httpclient.execute(httpget);

      // Save the status for the response
      int statusCode = response.getStatusLine().getStatusCode();
      String reasonPhrase = response.getStatusLine().getReasonPhrase();

      // Save the text of the response entity
      HttpEntity entity = response.getEntity();
      String text = EntityUtils.toString(entity);
      if (statusCode / 100 != 2)
        log.warn("HTTP response entity for status code " + statusCode + ": " + text);
      else
        log.info("HTTP response entity for status code " + statusCode + ": " + text);

      // Record time of most recent access
      now = System.currentTimeMillis();
      lastAccess = now;
      webSite.webAccessManager.lastAccess = now;

      // Create a WebPage for this page
      WebPage webPage = new WebPage(webSite, url, refreshInterval, System.currentTimeMillis(),
          response, statusCode, reasonPhrase, text);

      // Get the expiration, if any, from the page (header)
      Header[] expiresHeaders = response.getHeaders("Expires");
      String expiresString = null;
      long expires = 0;
      if (expiresHeaders.length >= 1){
        expiresString = expiresHeaders[0].getValue();
        expires = DateUtils.parseRfcString(expiresString);
      }

      long expiresDelta = expires - now;
      if (expires > 0)
        log.info("Web page returned an expiration in " + expiresDelta + " ms.");

      // TODO: Should we always override the user refresh if expires is preset?
      if (expires > 0)
        webPage.refreshInterval = expiresDelta;

      // Return the WebPage for this page
      return webPage;
    } catch (Exception e) {
      throw new WebAccessException("Web GET exception: " + e.getMessage());
    }

    // TODO Who adds this to the cache
    // Return the new web page
  }

  public String getWebPageText(String url){
    // Get the web page
    try {
      // Do the GET for the specified web page
      log.info("Fetching web page: " + url);
      HttpClient httpclient = new DefaultHttpClient();
      HttpGet httpget = new HttpGet(url);
      HttpResponse response = httpclient.execute(httpget);

      // Save the status for the response
      int statusCode = response.getStatusLine().getStatusCode();
      String reasonPhrase = response.getStatusLine().getReasonPhrase();

      // Save the text of the response entity
      HttpEntity entity = response.getEntity();
      String text = EntityUtils.toString(entity);
      if (statusCode / 100 != 2)
        log.warn("HTTP response entity for status code " + statusCode + ": " + text);
      else
        log.info("HTTP response entity for status code " + statusCode + ": " + text);

      // Return the text contents of the file
      return text;
    } catch (Exception e) {
      throw new WebAccessException("Web GET exception: " + e.getMessage());
    }
  }
  
  public String toString(){
    return "Robot URL: " + webSite.url + " User-agent name: " + webSite.webAccessManager.config.getUserAgentName() +
        " lastAccess: " + DateUtils.toRfcString(lastAccess) + " robotRecords: " + robotRecords;
  }
}
