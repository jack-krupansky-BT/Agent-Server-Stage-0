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

package com.basetechnology.s0.agentserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataSourceReferenceList implements Iterable<DataSourceReference> {
  public List<DataSourceReference> dataSourceReferenceList = new ArrayList<DataSourceReference>();

  public void add(DataSourceReference dataSourceReference){
    dataSourceReferenceList.add(dataSourceReference);
  }
  
  public DataSourceReference get(int index){
    return dataSourceReferenceList.get(index);
  }
  
  public Iterator<DataSourceReference> iterator(){
    return dataSourceReferenceList.iterator();
  }
  
  public int size(){
    return dataSourceReferenceList.size();
  }
}
