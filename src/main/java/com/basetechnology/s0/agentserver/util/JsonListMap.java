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

package com.basetechnology.s0.agentserver.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonListMap extends JSONObject {
  public List<String> keys;
  
  public JsonListMap(){
    super();
    this.keys = new ArrayList<String>();
  }
  
  public JsonListMap(String s) throws JSONException {
    super(s);
    if (keys == null)
      this.keys = new ArrayList<String>();
  }

  public void add(Nameable v) throws JSONException {
    String key = v.getName();
    if (! keys.contains(key))
      keys.add(key);
    put(key, v);
  }

  static public java.lang.String[] getNames(JSONObject jo){
    JSONArray names = jo.names();
    int numNames = names.length();
    String[] stringNames = new String[numNames];
    for (int i = 0; i < numNames; i++){
      String name = names.optString(i);
      stringNames[i] = name;
    }
    return stringNames;
  }

  public Iterator<String> keys(){
    return keys.iterator();
  }

  public JSONArray names(){
    JSONArray names = new JSONArray();
    for (String key: keys)
      names.put(key);
    return names;
  }

  public JSONObject put(String key, Object v) throws JSONException {
    if (keys == null)
      this.keys = new ArrayList<String>();
    if (! keys.contains(key))
      keys.add(key);
    return super.put(key, v);
  }

  public Iterator<String> sortedKeys(){
    return keys.iterator();
  }
}
