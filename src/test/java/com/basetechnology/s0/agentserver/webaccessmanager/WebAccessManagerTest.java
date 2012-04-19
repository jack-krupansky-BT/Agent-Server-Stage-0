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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.webaccessmanager.InvalidUrlException;
import com.basetechnology.s0.agentserver.webaccessmanager.Robot;
import com.basetechnology.s0.agentserver.webaccessmanager.RobotExclusionException;
import com.basetechnology.s0.agentserver.webaccessmanager.RobotRecord;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessConfig;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessFrequencyException;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessManager;
import com.basetechnology.s0.agentserver.webaccessmanager.WebPage;
import com.basetechnology.s0.agentserver.webaccessmanager.WebSite;
import com.basetechnology.s0.agentserver.webaccessmanager.WebSiteAccessConfig;

public class WebAccessManagerTest {

  WebAccessConfig wac = null;
  WebSiteAccessConfig wsac = null;
  WebAccessManager wam = null;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    if (wam != null)
      wam.clear();
    wac = new WebAccessConfig(
        WebAccessManager.DEFAULT_MINIMUM_WEB_ACCESS_INTERVAL,
        WebAccessManager.DEFAULT_MINIMUM_WEB_SITE_ACCESS_INTERVAL,
        WebAccessManager.DEFAULT_DEFAULT_WEB_PAGE_REFRESH_INTERVAL,
        WebAccessManager.DEFAULT_MINIMUM_WEB_PAGE_REFRESH_INTERVAL,
        WebAccessManager.DEFAULT_USER_AGENT_NAME,
        WebAccessManager.DEFAULT_IMPLICITLY_DENY_WEB_ACCESS);
    wsac = new WebSiteAccessConfig(null);
    wam = new WebAccessManager(wac, wsac);
  }

  @After
  public void tearDown() throws Exception {
    if (wam != null){
      wam.clear();
      wam = null;
      wac = null;
      wsac = null;
    }
  }

  @Test
  public void testGetWebSite() {

    // Try a null URL
    try {
      String testUrl = null;
      WebSite webSite = wam.getWebSite(testUrl);
      fail("getWebSite succeeded unexpectedly");
    } catch (InvalidUrlException e){
      // Expected
    }

    // Try an empty URL
    try {
      String testUrl = "";
      WebSite webSite = wam.getWebSite(testUrl);
      fail("getWebSite succeeded unexpectedly");
    } catch (InvalidUrlException e){
      // Expected
    }
    try {
      String testUrl = "   ";
      WebSite webSite = wam.getWebSite(testUrl);
      fail("getWebSite succeeded unexpectedly");
    } catch (InvalidUrlException e){
      // Expected
    }

    // Try some malformed URLs
    try {
      String testUrl = "http//";
      WebSite webSite = wam.getWebSite(testUrl);
      fail("getWebSite succeeded unexpectedly");
    } catch (InvalidUrlException e){
      // Expected
    }
    
    // Try a bogus URL
    {
      String testUrl = "http://bogus.cnn.bogus/star";
      WebSite webSite = wam.getWebSite(testUrl);
      assertEquals("WebSite URL", "http://bogus.cnn.bogus/", webSite.url);
    }
    
    // Try a valid URL
    {
      String testUrl = "http://www.cnn.com/US/";
      WebSite webSite = wam.getWebSite(testUrl);
      assertEquals("WebSite URL", "http://www.cnn.com/", webSite.url);
    }
  }

  @Test
  public void testGetWebPage() throws InterruptedException, AgentServerException, Exception {

    // Dummy user Id
    String userId = "testuser";
    
    // Throttle down throttling
    int minOverallInterval = 250;
    wac.setMinimumWebAccessInterval(minOverallInterval);
    int minSiteInterval = 300;
    wac.setMinimumWebSiteAccessInterval(minSiteInterval);
    
    // Try a null URL
    try {
      String testUrl = null;
      WebPage webPage = wam.getWebPage(null, testUrl);
      fail("getWebPage succeeded unexpectedly");
    } catch (InvalidUrlException e){
      // Expected
    }

    // Try an empty URL
    try {
      String testUrl = "  ";
      WebPage webPage = wam.getWebPage(null, testUrl);
      fail("getWebPage succeeded unexpectedly");
    } catch (InvalidUrlException e){
      // Expected
    }

    // Try a valid URL
    {
      String testUrl = "http://www.cnn.com/US/";
      WebPage webPage = wam.getWebPage(userId, testUrl);
      assertTrue("webPage is null", webPage != null);
      assertTrue("webPage.webSite is null", webPage.webSite != null);
      assertEquals("WebPage URL", "http://www.cnn.com/US/", webPage.url);
      assertEquals("WebSite URL", "http://www.cnn.com/", webPage.webSite.url);
      assertEquals("Status code", 200, webPage.statusCode);
      assertEquals("Reason phrase", "OK", webPage.reasonPhrase);
      String text = webPage.text;
      assertTrue("Web page text is null", text != null);
      int len = text.length();
      assertTrue("Web page text size is out of range: " + len, len > 500 && len < 250000);
      String searchText = "<title>U.S. News - Headlines, Stories and Video from CNN.com</title>";
      assertTrue("Web page text does not contain '" + "'", text.contains(searchText));
      assertEquals("Number of reads", 1, webPage.webSite.numReads);
      assertEquals("Number of web reads", 1, webPage.webSite.numWebAccesses);

      // Validate parse of robots.txt
      Robot robot = webPage.webSite.robot;
      assertEquals("Number of robot records", 1, robot.robotRecords.size());
      RobotRecord record = robot.robotRecords.get(0);
      assertEquals("User-agent name", "*", record.userAgentName);
      assertEquals("Crawl-delay", 0, record.crawlDelay);
      assertEquals("Count of allow paths", 0, record.allowPaths.size());
      assertEquals("Count of disallow paths", 28, record.disallowPaths.size());
      assertEquals("Disallow path[0]", "/.element", record.disallowPaths.get(0));
      assertEquals("Disallow path[1]", "/editionssi", record.disallowPaths.get(1));
      assertEquals("Disallow path[2]", "/ads", record.disallowPaths.get(2));
      assertEquals("Disallow path[26]", "/virtual", record.disallowPaths.get(26));
      assertEquals("Disallow path[27]", "/WEB-INF", record.disallowPaths.get(27));
      assertEquals("Count of sitemaps", 3, record.siteMaps.size());
      assertEquals("Sitemap[0]", "http://www.cnn.com/sitemaps/sitemap-index.xml", record.siteMaps.get(0));
      assertEquals("Sitemap[1]", "http://www.cnn.com/sitemaps/sitemap-news.xml", record.siteMaps.get(1));
      assertEquals("Sitemap[2]", "http://www.cnn.com/sitemaps/sitemap-video-index.xml", record.siteMaps.get(2));

      // Re-fetch page immediately and verify that it came from the cache
      WebPage webPage2 = wam.getWebPage(userId, testUrl);
      assertTrue("Failed to return cached page", webPage == webPage2);
      WebPage webPage3 = wam.getWebPage(userId, testUrl);
      assertTrue("Failed to return cached page", webPage == webPage3);
      assertEquals("Number of reads", 3, webPage.webSite.numReads);
      assertEquals("Number of web reads", 1, webPage.webSite.numWebAccesses);
      
      // Try to use zero refresh interval so cache is bypassed
      // But, this should simply return cache due to admin limit of 60 sec.
      Thread.sleep(1);
      WebPage webPage4 = wam.getWebPage(userId, testUrl, true, 0, false);
      assertTrue("Returned fresh page", webPage == webPage4);
      long delta = webPage4.time - webPage.time;
      assertTrue("Time changed", delta == 0);
      assertEquals("Number of reads", 4, webPage.webSite.numReads);
      assertEquals("Number of web reads", 1, webPage.webSite.numWebAccesses);

      // Now verify that default still returns the cached page
      WebPage webPage5 = wam.getWebPage(userId, testUrl);
      assertTrue("Failed to return cached page", webPage4 == webPage5);
      WebPage webPage6 = wam.getWebPage(userId, testUrl);
      assertTrue("Failed to return cached page", webPage5 == webPage6);
      assertEquals("Number of reads", 6, webPage.webSite.numReads);
      assertEquals("Number of web reads", 1, webPage.webSite.numWebAccesses);

      // Now turn off administrative throttling
      // Using refresh interval of zero should now return a fresh page since this
      // web site has no Crawl-delay specified
      wac.setMinimumWebSiteAccessInterval(0);
      wac.setMinimumWebAccessInterval(0);
      Thread.sleep(1);
      webPage4 = wam.getWebPage(userId, testUrl, true, 0, false);
      assertTrue("Returned cached page", webPage != webPage4);
      delta = webPage4.time - webPage.time;
      assertTrue("Time unchanged", delta != 0);
      assertEquals("Number of reads", 7, webPage.webSite.numReads);
      assertEquals("Number of web reads", 2, webPage.webSite.numWebAccesses);

      // Restore admin throttling
      wac.setMinimumWebSiteAccessInterval(minSiteInterval);
      wac.setMinimumWebAccessInterval(minOverallInterval);
      
      // Verify that robots.txt exclusion is working
      try {
        testUrl = "http://www.cnn.com/.element";
        webPage = wam.getWebPage(userId, testUrl);
        fail("getWebPage succeeded unexpectedly, despite robots.txt exclusion");
      } catch (RobotExclusionException e){
        // Expected
      }

      try {
        testUrl = "http://www.cnn.com/WEB-INF";
        webPage = wam.getWebPage(userId, testUrl);
        fail("getWebPage succeeded unexpectedly, despite robots.txt exclusion");
      } catch (RobotExclusionException e){
        // Expected
      }

      try {
        testUrl = "http://www.cnn.com/cnews";
        webPage = wam.getWebPage(userId, testUrl);
        fail("getWebPage succeeded unexpectedly, despite robots.txt exclusion");
      } catch (RobotExclusionException e){
        // Expected
      }
      assertEquals("Number of reads", 10, webPage.webSite.numReads);
      assertEquals("Number of web reads", 2, webPage.webSite.numWebAccesses);

      // Access a page on the same site but without a wait - should fail
      try {
        testUrl = "http://www.cnn.com/WORLD/";
        webPage = wam.getWebPage(userId, testUrl, true, -1, false);
        fail("getWebPage succeeded unexpectedly, despite lack of wait option");
      } catch (WebAccessFrequencyException e){
        assertTrue ("Improper exception message: " + e.getMessage(), e.getMessage().contains("minimum site access interval of 300 "));
        // Expected
      }
      assertEquals("Number of reads", 11, webPage.webSite.numReads);
      assertEquals("Number of web reads", 2, webPage.webSite.numWebAccesses);

      // Turn off sit throttling and re-read and see that it fails on the overall throttling
      wac.setMinimumWebSiteAccessInterval(0);
      try {
        testUrl = "http://www.cnn.com/WORLD/";
        webPage = wam.getWebPage(userId, testUrl, true, -1, false);
        fail("getWebPage succeeded unexpectedly, despite lack of wait option");
      } catch (WebAccessFrequencyException e){
        assertTrue ("Improper exception message: " + e.getMessage(), e.getMessage().contains("minimum overall web access interval of 250 "));
        // Expected
      }
      assertEquals("Number of reads", 12, webPage.webSite.numReads);
      assertEquals("Number of web reads", 2, webPage.webSite.numWebAccesses);

      // Now access same page with wait option - should succeed
      testUrl = "http://www.cnn.com/WORLD/";
      webPage = wam.getWebPage(userId, testUrl, true, -1, true);
      assertEquals("Number of reads", 13, webPage.webSite.numReads);
      assertEquals("Number of web reads", 3, webPage.webSite.numWebAccesses);

      // Re-read the same page and see that it comes from cache
      testUrl = "http://www.cnn.com/WORLD/";
      webPage = wam.getWebPage(userId, testUrl, true, -1, true);
      assertEquals("Number of reads", 14, webPage.webSite.numReads);
      assertEquals("Number of web reads", 3, webPage.webSite.numWebAccesses);

      // TODO: Need to test with a site that has Crawl-delay
      // Fake a robots.txt crawl delay and test without wait option to see it fail
      wac.setMinimumWebSiteAccessInterval(0);
      wac.setMinimumWebAccessInterval(0);
      wam.webPageCache.clear();
      WebSite webSite = wam.getWebSite("http://www.cnn.com/WORLD/");
      webSite.robot.getRobotRecord().crawlDelay = 2;
      try {
        testUrl = "http://www.cnn.com/WORLD/";
        webPage = wam.getWebPage(userId, testUrl, true, -1, false);
        fail("getWebPage succeeded unexpectedly, despite lack of wait option");
      } catch (WebAccessFrequencyException e){
        assertTrue ("Improper exception message: " + e.getMessage(), e.getMessage().contains("Crawl-delay of 2 "));
        // Expected
      }
      assertEquals("Number of reads", 15, webPage.webSite.numReads);
      assertEquals("Number of web reads", 3, webPage.webSite.numWebAccesses);

      // Now do same operation with wait - it should succeed
      testUrl = "http://www.cnn.com/WORLD/";
      webPage = wam.getWebPage(userId, testUrl, true, -1, true);
      assertEquals("Number of reads", 16, webPage.webSite.numReads);
      assertEquals("Number of web reads", 4, webPage.webSite.numWebAccesses);

    }

  }

}
