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

public class StringUtils {
  
  static public String removeCommas(String s){
    if (s == null)
      return null;
    
    int i = s.indexOf(',');
    if (i < 0)
      return s;
    else
      return s.substring(0, i) + removeCommas(s.substring(i + 1));
  }

  static public String parseQuotedString(String quotedString){
    if (quotedString == null)
      return null;
    int len = quotedString.length();
    if (len == 0)
      return quotedString;
    if (quotedString.charAt(0) == '"'){
      StringBuilder sb = new StringBuilder();
      for (int i = 1; i < len; i++){
        char ch = quotedString.charAt(i);
        if (ch == '"')
          break;
        else if (ch == '\\')
          ch = quotedString.charAt(++i);
        sb.append(ch);
      }
      return sb.toString();
    } else
      return quotedString;
  }
}
