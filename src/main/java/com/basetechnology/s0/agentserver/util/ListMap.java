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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ListMap<K,V> implements Map<K, V>, Iterable<K> {
  public List<K> list;
  protected Map<K, V> map;

  public ListMap(){
    list = new ArrayList<K>();
    map = new HashMap<K, V>();
  }
  
  public void clear(){
    list.clear();
    map.clear();
  }
  
  public ListMap<K, V> clone(){
    ListMap<K, V> listMap = new ListMap<K, V>();
    for (K key: this)
      listMap.put(key, get(key));
    return listMap;
  }
  
  public boolean containsKey(Object e){
    return map.containsKey(e);
  }
  
  public boolean containsCaseInsensitiveKey(String key){
    for (K key2: map.keySet())
      if (((String)key2).equalsIgnoreCase(key))
        return true;
    return false;
  }
  
  public boolean containsValue(Object e){
    return list.contains(e);
  }

  public boolean equals(ListMap<K, V> otherList){
    int numElements = list.size();
    int numOtherElements = otherList.list.size();
    if (numElements != numOtherElements)
      return false;
    for (int i = 0; i < numElements; i++){
      K key = list.get(i);
      if (! key.equals(otherList.list.get(i)))
        return false;
      if (! map.get(key).equals(otherList.map.get(key)))
        return false;
    }
    return true;
  }
  
  public Set<Map.Entry<K, V>> entrySet(){
    Set<Map.Entry<K, V>> entries = new LinkedHashSet<Map.Entry<K, V>>();
    for (K key: list)
      entries.add(new AbstractMap.SimpleEntry<K, V>(key, map.get(key)));
    return entries;
  }
  
  public boolean isEmpty(){
    return list.isEmpty();
  }
  
  public Iterator<K> iterator(){
    return list.iterator();
  }
  
  public V get(int index){
    return map.get(list.get(index));
  }
  
  public V get(Object key){
    return map.get(key);
  }
  
  public V getCaseInsensitive(String key){
    for (K key2: map.keySet())
      if (((String)key2).equalsIgnoreCase(key))
        return map.get(key2);
    return null;
  }
  
  public Set<K> keySet(){
    Set<K> keys = new LinkedHashSet<K>();
    for (K key: list)
      keys.add(key);
    return keys;
  }
  
  public V put(K key, V value){
    if (! list.contains(key))
      list.add(key);
    return map.put(key, value);
  }

  public void putAll(Map<? extends K,? extends V> m){
    // TODO
  }
  
  public void removeIndex(int index){
    K key = list.get(index);
    list.remove(index);
    map.remove(key);
  }
  
  public V remove(Object key){
    V v = map.get(key);
    list.remove(key);
    map.remove(key);
    return v;
  }
  
  public int size(){
    return list.size();
  }

  public String toString(){
    StringBuilder sb = new StringBuilder("[");
    for (K key: list){
      if (sb.length() > 1)
        sb.append(", ");
      sb.append(key.toString());
      sb.append(": ");
      sb.append(map.get(key));
    }
    sb.append(']');
    return sb.toString();
  }
  
  public Collection<V> values(){
    Set<V> values = new LinkedHashSet<V>();
    for (K key: list)
      values.add(map.get(key));
    return values;
  }
}
