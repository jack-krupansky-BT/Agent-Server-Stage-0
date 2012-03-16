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

import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.field.Field;

public class FieldState {
  public Field field;
  public Object value;

  public FieldState(Field field, Object value){
    this.field = field;
    this.value = value;
  }

  public FieldState clone(){
    return new FieldState(field, value);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject fieldState = new JSONObject();
    fieldState.put("value", value);
    return fieldState;
  }
}
