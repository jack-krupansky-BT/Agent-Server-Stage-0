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

package com.basetechnology.s0.agentserver.persistence;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.User;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentFile;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentFileException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.util.ListMap;


public class Persistence {
  public AgentServer agentServer;
  public String path;
  public PersistentFile file;

  public Persistence(AgentServer agentServer, String path) throws AgentServerException, ParseException, TokenizerException, ParserException {
    this.agentServer = agentServer;
    this.path = path;
    initialize();
  }

  public void initialize() throws AgentServerException, ParseException, TokenizerException, ParserException {
    try {
      // Get a new persistent file object
      if (file == null)
        file = new PersistentFile();

      // Does our persistent file exists yet?
      if (checkForPersistenceFile()){
        // Yes, open it
        file.open(path);
        
        // And load all the tables and create all the agent server objects
        loadAllTables();
      } else {
        // No, create a new persistent file for this agent server
        List<String> tableNames = Arrays.asList("config", "users", "agentDefinitions", "agentInstances", "webaccess");
        file.create(path, "Agent Server Stage 0", "0.1", tableNames);
        
        // And open it
        file.open(path);
      }
    } catch (PersistentFileException e){
      e.printStackTrace();
      throw new AgentServerException("PersistentFileException: " + e.getMessage());
    } catch (IOException e){
      e.printStackTrace();
      throw new AgentServerException("IOException reading persistent store: " + e.getMessage());
    }
  }

  public void close() throws IOException {
    file.close();
  }
  
  public boolean checkForPersistenceFile(){
    File file = new File(path);
    return file.exists();
  }

  public JSONObject getJson(String tableName, String key) throws AgentServerException {
    String value = get(tableName, key);
    if (value == null)
      return null;
    else
      try {
        return new JSONObject(value);
      } catch (JSONException e){
        e.printStackTrace();
        throw new AgentServerException("JSONException reading persistent store: " + e.getMessage());
      }
  }

  public String get(String tableName, String key) throws AgentServerException {
    try {
      return file.get(tableName, key);
    } catch (PersistentFileException e){
      e.printStackTrace();
      throw new AgentServerException("PersistentFileException: " + e.getMessage());
    } catch (IOException e){
      e.printStackTrace();
      throw new AgentServerException("IOException reading persistent store: " + e.getMessage());
    }
  }

  public ListMap<String, String> get(String tableName) throws IOException, PersistentFileException {
    return file.get(tableName);
  }
  
  public Iterator<String> iterator(String tableName) throws PersistentFileException {
    return file.iterator(tableName);
  }

  public void put(User user) throws AgentServerException {
    put("users", user.id, user.toJson());
  }

  public void put(AgentDefinition agentDefinition) throws AgentServerException {
    put("agentDefinitions", agentDefinition.user.id + "|" + agentDefinition.name, agentDefinition.toJson().toString());
  }

  public void put(AgentInstance agentInstance) throws AgentServerException {
    put("agentInstances", agentInstance.user.id + "|" + agentInstance.name, agentInstance.toJson().toString());
  }

  public void put(String tableName, String key, String value) throws AgentServerException {
    try {
      file.put(tableName, key, value);
    } catch (PersistentFileException e){
      e.printStackTrace();
      throw new AgentServerException("PersistentFileException: " + e.getMessage());
    } catch (IOException e){
      e.printStackTrace();
      throw new AgentServerException("IOException reading persistent store: " + e.getMessage());
    }
  }

  public void loadAllTables() throws AgentServerException, ParseException, TokenizerException, ParserException {
    loadUsers();
    loadAgentDefinitions();
    loadAgentInstances();
    // TODO: Status of the scheduler - is it suspended, when is it started?
    // TODO: What to do about pending activities - store/load them? Or, can they be ignored?
  }

  public void loadUsers() throws AgentServerException {
    try {
      // Load all users
      for (String userId: file.iterable("users")){
        String userJsonSource = file.get("users", userId);
        agentServer.recreateUser(userJsonSource);
      }
    } catch (PersistentFileException e){
      e.printStackTrace();
      throw new AgentServerException("PersistentFileException loading users: " + e.getMessage());
    } catch (IOException e){
      e.printStackTrace();
      throw new AgentServerException("IOException loading users from persistent store: " + e.getMessage());
    } catch (JSONException e){
      e.printStackTrace();
      throw new AgentServerException("JSONException loading users from persistent store: " + e.getMessage());
    }
  }

  public void loadAgentDefinitions() throws AgentServerException {
    try {
      // Load all agent definitions
      for (String agentDefinitionId: file.iterable("agentDefinitions")){
        String agentDefinitionJsonSource = file.get("agentDefinitions", agentDefinitionId);
        agentServer.recreateAgentDefinition(agentDefinitionJsonSource);
      }
    } catch (PersistentFileException e){
      e.printStackTrace();
      throw new AgentServerException("PersistentFileException loading agent definitions: " + e.getMessage());
    } catch (IOException e){
      e.printStackTrace();
      throw new AgentServerException("IOException loading agent definitions from persistent store: " + e.getMessage());
    } catch (JSONException e){
      e.printStackTrace();
      throw new AgentServerException("JSONException loading agent definitions from persistent store: " + e.getMessage());
    } catch (SymbolException e){
      e.printStackTrace();
      throw new AgentServerException("SymbolException loading agent definitions from persistent store: " + e.getMessage());
    }
  }

  public void loadAgentInstances() throws AgentServerException, ParseException, TokenizerException, ParserException {
    try {
      // Load all agent instances
      for (String agentInstanceId: file.iterable("agentInstances")){
        String agentInstanceJsonSource = file.get("agentInstances", agentInstanceId);
        agentServer.recreateAgentInstance(agentInstanceJsonSource);
      }
    } catch (PersistentFileException e){
      e.printStackTrace();
      throw new AgentServerException("PersistentFileException loading agent instances: " + e.getMessage());
    } catch (IOException e){
      e.printStackTrace();
      throw new AgentServerException("IOException loading agent instances from persistent store: " + e.getMessage());
    } catch (JSONException e){
      e.printStackTrace();
      throw new AgentServerException("JSONException loading agent instances from persistent store: " + e.getMessage());
    } catch (SymbolException e){
      e.printStackTrace();
      throw new AgentServerException("SymbolException loading agent instances from persistent store: " + e.getMessage());
    }
  }
}
