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

package com.basetechnology.s0.agentserver.persistence.persistentfile;

import java.util.Iterator;

public class PersistentTableIterator implements Iterator<String> {
  public PersistentFile file;
  public int nextTableIndex;
  public int numTables;

  public PersistentTableIterator(PersistentFile file){
    this.file = file;
    this.nextTableIndex = 0;
    this.numTables = file.persistentTables.size();
  }
  
  public boolean hasNext(){
    return nextTableIndex <= numTables - 1;
  }
  
  public String next(){
    if (nextTableIndex <= numTables - 1){
      nextTableIndex++;
      return file.persistentTables.get(nextTableIndex - 1).name;
    } else
      return null;
  }
  
  public void remove(){
    // TODO: What should this do? -- We don't have delete yet.
  }

}
