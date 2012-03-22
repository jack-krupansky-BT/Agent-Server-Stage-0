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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

  public static SimpleDateFormat rfcFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
  public static SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  public static long parseDate(String time) throws ParseException {
    try {
      return rfcFormat.parse(time).getTime();
    } catch (ParseException e){
      return isoFormat.parse(time).getTime();
    }
  }

  public static long parseIsoString(String time) throws ParseException {
    return isoFormat.parse(time).getTime();
  }

  public static long parseRfcString(String time) throws ParseException {
    return rfcFormat.parse(time).getTime();
  }
  
  public static String toString(long time){
    return toIsoString(time);
  }
  
  public static String toRfcString(long time){
    // Format time in RFC date/time format: EEE, dd MMM yyyy HH:mm:ss Z
    return rfcFormat.format(new Date(time));
  }

  public static String toIsoString(long time){
    // Format time in ISO date/time format: yyyy-MM-dd'T'HH:mm:ss.SSSZ
    return isoFormat.format(new Date(time));
  }
}
