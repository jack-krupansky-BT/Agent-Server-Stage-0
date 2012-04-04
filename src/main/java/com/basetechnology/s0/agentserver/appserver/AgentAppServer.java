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

package com.basetechnology.s0.agentserver.appserver;

import java.io.IOException;
import java.text.ParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.util.ListMap;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessException;

public class AgentAppServer {
  static final Logger log = Logger.getLogger(AgentAppServer.class);
  public static String apiPathPrefix;
  public static String appServerBaseUrl;
  public static String appServerApiBaseUrl;
  public ListMap<String, String> commandLineproperties;
  public AgentServer agentServer;

  public Server server;

  public int appServerPort;

  public AgentAppServer() throws RuntimeException, AgentServerException, Exception {
    this(true);
  }

  public AgentAppServer(ListMap<String, String> commandLineproperties) throws RuntimeException, AgentServerException, Exception {
    this(true, commandLineproperties);
  }

  public AgentAppServer(boolean start) throws RuntimeException, AgentServerException,  Exception {
    this(start, null);
  }

  public AgentAppServer(boolean start, ListMap<String, String> commandLineproperties) throws RuntimeException, AgentServerException,  Exception {
    // Save command line properties
    this.commandLineproperties = commandLineproperties;
    
    // Start the agent server.
    agentServer = new AgentServer(this);

    // Get the desired port number
    appServerPort = agentServer.config.agentServerProperties.appServerPort;

    // Build and save the bas API URLs
    apiPathPrefix = "/API/v0.1";
    appServerBaseUrl = "http://localhost:" + appServerPort;
    appServerApiBaseUrl = appServerBaseUrl + apiPathPrefix;

    // Start the Jetty server
    startJetty();

    // Optionally start the agent app server
    if (start)
      start();
  }

  public void handleException(HttpInfo httpInfo, int statusCode, Exception e) throws IOException {
    Request request = httpInfo.request;
    HttpServletResponse response = httpInfo.response;
    String type = e.getClass().getName();
    String message = e.getMessage();
    log.error("Logger: " + message, e);
    log.info(type + ": " + e);
    e.printStackTrace();
    JSONObject errorsObjectJson = new JSONObject();
    JSONArray errorsArrayJson = new JSONArray();
    JSONObject errorJson = new JSONObject();
    try {
      errorJson.put("type", type);
      errorJson.put("message", message);
      errorsArrayJson.put(errorJson);
      errorsObjectJson.put("errors", errorsArrayJson);
      (httpInfo.handleHttp == null ? new HandleGet(httpInfo) : httpInfo.handleHttp).setOutput(errorsObjectJson);
    } catch (JSONException e1){
      // Not sure what we can do, but fall back to raw JSON
      response.getWriter().println("{\"errors\": [{\"type\": \"" + type + "\", \"message\": \"" + message + "\"}]}");
    }
    response.setStatus(statusCode);
    response.setContentType("application/" + type + "; charset=utf-8");
    ((Request)request).setHandled(true);
  }

  public void startJetty() throws Exception {
    if (server != null){
      String jettyState = server.getState();
      if (jettyState.equalsIgnoreCase("STARTED"))
        return;
    }
    
    Handler handler = new AbstractHandler()
    {
      public void handle(String target, Request request, HttpServletRequest servletRequest, HttpServletResponse response)
          throws IOException, ServletException {
        // Gather all the request info in one place
        HttpInfo httpInfo = new HttpInfo(target, request, servletRequest, response, null, null, "json", agentServer);

        // Handle major exceptions in a common manner at the outer level
        try {
          // Get path from URL
          String path = request.getPathInfo();

          // Determine output format
          String format = "json";
          if (path.endsWith(".json")){
            format = "json";
            path = path.substring(0, path.length() - 5);
          } else if (path.endsWith(".csv")){
            format = "csv";
            path = path.substring(0, path.length() - 4);
          } else if (path.endsWith(".tab")){
            format = "tab";
            path = path.substring(0, path.length() - 4);
          } else if (path.endsWith(".xml")){
            format = "xml";
            path = path.substring(0, path.length() - 4);
          } else if (path.endsWith(".rss")){
            format = "rss";
            path = path.substring(0, path.length() - 4);
          } else if (path.endsWith(".html")){
            format = "html";
            path = path.substring(0, path.length() - 5);
          } else if (path.endsWith(".txt")){
            format = "text";
            path = path.substring(0, path.length() - 4);
          } else if (path.endsWith(".text")){
            format = "text";
            path = path.substring(0, path.length() - 5);
          } else {
            String formatParameter = request.getParameter("format");
            if (formatParameter != null){
              if (formatParameter.equalsIgnoreCase("json"))
                format = "json";
              else if (formatParameter.equalsIgnoreCase("xml"))
                format = "xml";
              else if (formatParameter.equalsIgnoreCase("rss"))
                format = "rss";
              else if (formatParameter.equalsIgnoreCase("html"))
                format = "html";
              else if (formatParameter.equalsIgnoreCase("txt") || formatParameter.equalsIgnoreCase("text"))
                format = "text";
              else if (formatParameter.equalsIgnoreCase("csv"))
                format = "csv";
              else if (formatParameter.equalsIgnoreCase("tab"))
                format = "tab";
              else
                throw new AgentAppServerBadRequestException("Unsupported format type query parameter (only json, xml, html, rss, text, txt, csv, and tab supported): " + formatParameter);
            }
          }
          httpInfo.format = format;

          // Make sure path is an API path that we handle
          if (! path.startsWith(apiPathPrefix))
            throw new AgentAppServerBadRequestException("Unsupported API path prefix (needs to be '" + apiPathPrefix + "')");

          // Strip the API version from the path
          path = path.substring(apiPathPrefix.length());
          httpInfo.path = path;

          // Get common info
          String method = request.getMethod();
          String[] pathParts = path.split("/");
          httpInfo.pathParts = pathParts;
          int numPathParts = pathParts.length;
          log.info("Request method: " + method + " path: " + path + " numPathParts: " + numPathParts);

          // Determine which HTTP method is being invoked
          boolean handled = false;
          if (method.equalsIgnoreCase("GET")){
            handled = new HandleGet(httpInfo).handleGet();
          } else if (method.equalsIgnoreCase("POST")){
            handled = new HandlePost(httpInfo).handlePost();
          } else if (method.equalsIgnoreCase("PUT")){
            handled = new HandlePut(httpInfo).handlePut();
          } else if (method.equalsIgnoreCase("DELETE")){
            handled = new HandleDelete(httpInfo).handleDelete();
          } else {
            throw new AgentAppServerException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "The " + method + " is not supported by the agent server");
          }

          // I'm not really sure when/why this might be false
          // But maybe because this handler expects to process all paths and give an error page
          // if the path is not supported by the agent server
          ((Request)request).setHandled(handled);
        } catch (AgentAppServerBadRequestException e){
          handleException(httpInfo, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (AgentAppServerException e){
          handleException(httpInfo, e.statusCode, e);
        } catch (AgentServerException e){
          handleException(httpInfo, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (ParserException e){
          handleException(httpInfo, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (TokenizerException e){
          handleException(httpInfo, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (JSONException e){
          handleException(httpInfo, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (InterruptedException e){
          handleException(httpInfo, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        } catch (WebAccessException e){
          handleException(httpInfo, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (ParseException e) {
          handleException(httpInfo, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        } catch (Exception e) {
          handleException(httpInfo, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
      }

    };

    server = new Server(appServerPort);
    server.setHandler(handler);
    
    String jettyState = server.getState();
    server.start();
    jettyState = server.getState();
  }

  public void restart() throws Exception {
    // Shutdown first
    shutdown();
    
    // And then start up again
    start();
  }
  
  public void shutdown() throws Exception {
    stop();
    // TODO: Should this do something else in addition to stop?
  }

  public void start() throws Exception {
    log.info("Starting agent server");
    try {
      // Start Jetty
      startJetty();
      
      // Start the agent server
      agentServer.start();
    } catch (Exception e){
      log.info("Agent server start exception: " + e);
      e.printStackTrace();
      throw e;
    }
    log.info("Agent server started");
  }

  public void stop() throws Exception {
    log.info("Stopping agent server");
    try {
      // Stop the embedded Jetty server
      server.stop();

      // Stop the agent server
      agentServer.stop();
    } catch (Exception e){
      log.info("Agent server stop exception: " + e);
      throw e;
    }
    log.info("Agent server stopped");
  }
}
