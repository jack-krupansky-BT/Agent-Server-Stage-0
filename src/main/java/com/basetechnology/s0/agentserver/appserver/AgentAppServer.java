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
import java.util.Iterator;

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
import com.basetechnology.s0.agentserver.util.XmlUtils;
import com.basetechnology.s0.agentserver.webaccessmanager.WebAccessException;

public class AgentAppServer {
  static final Logger log = Logger.getLogger(AgentAppServer.class);
  public static String apiPathPrefix;
  public static String appServerBaseUrl;
  public static String appServerApiBaseUrl;
  public AgentServer agentServer;

  public Server server;

  public int appServerPort;
  
  public static void setOutput(HttpInfo httpInfo, JSONObject outputJson){
    HttpServletResponse response = httpInfo.response;
    String format = httpInfo.format;
    response.setStatus(HttpServletResponse.SC_OK);
    if (format.equals("json")){
      // Output response in JSON format
      response.setContentType("application/json; charset=utf-8");
      try {
        response.getWriter().println(outputJson.toString(4));
      } catch (IOException e){
        // Not much we can do without causing recursion
        //throw new AgentAppServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to format output for response - " + e.getMessage());
        log.info("Unable to format output for response - " + e.getMessage());
      } catch (JSONException e){
        // Not much we can do without causing recursion
        //throw new AgentAppServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to format output for response - " + e.getMessage());
        log.info("Unable to format output for response - " + e.getMessage());
      }
    } else if (format.equals("xml") || format.equals("rss")){
      // Output response in XML format
      response.setContentType("application/xml; charset=utf-8");
      try {
        // TODO: Handle entities
        response.getWriter().println(XmlUtils.formatJsonAsXml(outputJson));
      } catch (IOException e){
        // Not much we can do without causing recursion
        //throw new AgentAppServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to format output for response - " + e.getMessage());
        log.info("Unable to format output for response - " + e.getMessage());
      }
    } else if (format.equals("html")){
      // Output response in plain text format
      // TODO: Leave input in native format so that order of field can be preserved
      response.setContentType("text/html; charset=utf-8");
      String title = "Agent Output";
      StringBuilder sb = new StringBuilder("<html><head><title>" + title + "</title></head><body><h1>Output Field Values</h1><table border=\"1\">");
      if (outputJson != null){
        // TODO: If JSON is an array, write multiple rows
        for (Iterator<String> it = outputJson.keys(); it.hasNext(); ){
          String key = it.next();
          sb.append("<tr><td>" + key + "</td><td>" + outputJson.opt(key).getClass().getSimpleName() + "</td><td>");
          // TODO Handle list and map types
          String value = outputJson.optString(key);
          sb.append(value);
          sb.append("</td></tr>");
        }
      }
      sb.append("</table></body></html>");
      try {
        response.getWriter().println(sb.toString());
      } catch (IOException e){
        // Not much we can do without causing recursion
        //throw new AgentAppServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to format output for response - " + e.getMessage());
        log.info("Unable to format output for response - " + e.getMessage());
      }
    } else if (format.equals("text")){
      // Output response in plain text format
      // TODO: Leave input in native format so that order of field can be preserved
      response.setContentType("text/plain; charset=utf-8");
      StringBuilder sb = new StringBuilder();
      if (outputJson != null){
        // TODO: If JSON is an array, write multiple rows
        for (Iterator<String> it = outputJson.keys(); it.hasNext(); ){
          String key = it.next();
          // TODO Handle list and map types
          String value = outputJson.optString(key);
          if (sb.length() > 0)
            sb.append(' ');
          sb.append(value);
        }
      }
      try {
        response.getWriter().println(sb.toString());
      } catch (IOException e){
        // Not much we can do without causing recursion
        //throw new AgentAppServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to format output for response - " + e.getMessage());
        log.info("Unable to format output for response - " + e.getMessage());
      }
    } else if (format.equals("tab")){
      // Output response in plain text format
      // TODO: Leave input in native format so that order of field can be preserved
      response.setContentType("text/plain; charset=utf-8");
      StringBuilder sb = new StringBuilder();
      if (outputJson != null){
        // TODO: If JSON is an array, write multiple rows
        for (Iterator<String> it = outputJson.keys(); it.hasNext(); ){
          String key = it.next();
          // TODO Handle list and map types
          String value = outputJson.optString(key);
          if (sb.length() > 0)
            sb.append('\t');
          sb.append(value);
        }
      }
      try {
        response.getWriter().println(sb.toString());
      } catch (IOException e){
        // Not much we can do without causing recursion
        //throw new AgentAppServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to format output for response - " + e.getMessage());
        log.info("Unable to format output for response - " + e.getMessage());
      }
    } else if (format.equals("csv")){
      // Output response in plain text format
      // TODO: Leave input in native format so that order of field can be preserved
      response.setContentType("text/plain; charset=utf-8");
      StringBuilder sb = new StringBuilder();
      if (outputJson != null){
        // TODO: If JSON is an array, write multiple rows
        for (Iterator<String> it = outputJson.keys(); it.hasNext(); ){
          String key = it.next();
          // TODO Handle list and map types
          String value = outputJson.optString(key);
          if (sb.length() > 0)
            sb.append(',');
          if (value.contains(","))
            sb.append("\"" + value + "\"");
          // TODO: Verify Add quotes if comma or other quotable characters
          else
            sb.append(value);
        }
      }
      try {
        response.getWriter().println(sb.toString());
      } catch (IOException e){
        // Not much we can do without causing recursion
        //throw new AgentAppServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to format output for response - " + e.getMessage());
        log.info("Unable to format output for response - " + e.getMessage());
      }
    } else {
      // Just treat as JSON
      //throw new AgentAppServerBadRequestException("Unsupported output format: " + format);
      response.setContentType("application/json; charset=utf-8");
      try {
        response.getWriter().println(outputJson.toString(4));
      } catch (IOException e){
        // Not much we can do without causing recursion
        //throw new AgentAppServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to format output for response - " + e.getMessage());
        log.info("Unable to format output for response - " + e.getMessage());
      } catch (JSONException e){
        // Not much we can do without causing recursion
        //throw new AgentAppServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to format output for response - " + e.getMessage());
        log.info("Unable to format output for response - " + e.getMessage());
      }
    }
  }

  public static void handleException(HttpInfo httpInfo, int statusCode, Exception e) throws IOException {
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
      setOutput(httpInfo, errorsObjectJson);
    } catch (JSONException e1){
      // Not sure what we can do, but fall back to raw JSON
      response.getWriter().println("{\"errors\": [{\"type\": \"" + type + "\", \"message\": \"" + message + "\"}]}");
    }
    response.setStatus(statusCode);
    response.setContentType("application/" + type + "; charset=utf-8");
    ((Request)request).setHandled(true);
  }

  public AgentAppServer() throws RuntimeException, AgentServerException, Exception {
    this(true);
  }

  public AgentAppServer(boolean start) throws RuntimeException, AgentServerException,  Exception {

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
            handled = new HandleGet().handleGet(httpInfo);
          } else if (method.equalsIgnoreCase("POST")){
            handled = new HandlePost().handlePost(httpInfo);
          } else if (method.equalsIgnoreCase("PUT")){
            handled = new HandlePut().handlePut(httpInfo);
          } else if (method.equalsIgnoreCase("DELETE")){
            handled = new HandleDelete().handleDelete(httpInfo);
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
