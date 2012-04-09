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

package com.basetechnology.s0.agentserver.script.runtime.value;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.intermediate.StringTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;
import com.basetechnology.s0.agentserver.util.JsonUtils;
import com.basetechnology.s0.agentserver.util.StringUtils;
import com.basetechnology.s0.agentserver.util.TextAnalyzer;
import com.basetechnology.s0.agentserver.util.Word;
import com.basetechnology.s0.agentserver.util.XmlUtils;

public class StringValue extends Value {
  static final Logger log = Logger.getLogger(StringValue.class);

  public String value;

  static public StringValue empty = new StringValue("");
  
  public StringValue(String value){
    this.value = value;
  }

  public TypeNode getType(){
    return StringTypeNode.one;
  }

  public Value getDefaultValue(){
    return StringValue.empty;
  }

  public Object getValue(){
    return value;
  }

  // TODO: Reconsider whether string.boolean is a parse or simply a check for non-null and non-empty
  public boolean getBooleanValue(){
    return value != null && (value.trim().equalsIgnoreCase("true") || value.trim().equalsIgnoreCase("on"));
  }

  public long getLongValue(){
    // Remove commas and fraction
    // TODO: Should round
    return (long)Double.parseDouble(StringUtils.removeCommas(value));
  }

  public double getDoubleValue(){
    // Remove commas
    return Double.parseDouble(StringUtils.removeCommas(value));
  }

  public String getStringValue(){
    return value;
  }

  public Value getNamedValue(ScriptState scriptState, String name) throws RuntimeException {
    if (name.equals("copy"))
      return new StringValue(value);
    else if (name.equals("html")){
      XmlUtils xml = new XmlUtils();
      return xml.parseHtml(scriptState, value);
    } else if (name.equals("length"))
      return new IntegerValue(value.length());
    else if (name.equals("json"))
      return JsonUtils.parseJson(value);
    else if (name.equals("lower")){
      // Lower case the existing value
      value = value.toLowerCase();

      // Return this string object as the result
      return this;
    } else if (name.equals("upper")){
      // Upper case the existing value
      value = value.toUpperCase();

      // Return this string object as the result
      return this;
    } else if (name.equals("words")){
      // Parse the raw string as a sequence of words
      TextAnalyzer ta = new TextAnalyzer();
      List<Word> words = ta.analyze(value);
      
      // Copy the raw word list into a list of values
      List<Value> wordValues = new ArrayList<Value>();
      for (Word word: words)
        wordValues.add(new StringValue(word.proper));
      
      // Generate and return a new list of the words
      return new ListValue(StringTypeNode.one, wordValues);
    } else if (name.equals("xml")){
      XmlUtils xml = new XmlUtils();
      return xml.parseXml(scriptState, value);
    } else if (name.equals("urlDecode")){
      try {
        return new StringValue(URLDecoder.decode(value, "UTF-8"));
      } catch (UnsupportedEncodingException e){
        throw new RuntimeException("Unable to encode URL - UTF-* is not a supported encoding");
      }
    } else if (name.equals("urlEncode")){
      try {
        return new StringValue(URLEncoder.encode(value, "UTF-8"));
      } catch (UnsupportedEncodingException e){
        throw new RuntimeException("Unable to encode URL - UTF-* is not a supported encoding");
      }
    } else
      return super.getNamedValue(scriptState, name);
  }

  public Value getMethodValue(ScriptState scriptState, String name, List<Value> arguments) throws RuntimeException {
    int numArguments = arguments.size();
    if (name.equals("length") && numArguments == 0){
      return new IntegerValue(value.length());
    } else if (name.equals("after") && (numArguments == 1 || numArguments == 2)){
      // Extract and return text after the first occurrence of a substring
      String s = arguments.get(0).getStringValue();
      
      // Optional 'from' index as second argument
      int len = value.length();
      int fromIndex = numArguments == 1 ? 0 : arguments.get(1).getIntValue();
      if (fromIndex < 0)
        throw new RuntimeException("String index for before is less than zero: " + fromIndex);
      else if (fromIndex > len)
        throw new RuntimeException("String index of " + fromIndex + " for before is greater than string length of " + len);
      
      // Watch out for empty substrings
      if (s == null)
        return StringValue.empty;
      if (len == 0)
        return StringValue.empty;
      
      // Find the substring
      int index = value.indexOf(s, fromIndex);
      if (index < 0)
        // Substring not found
        return StringValue.empty;
      else
        // Extract and return the text after the substring
        return new StringValue(value.substring(index + s.length()));
    } else if (name.equals("afterRegex") && (numArguments == 1 || numArguments == 2)){
      // Extract and return text after the first occurrence of a regex
      String pat = arguments.get(0).getStringValue();
      
      // Optional 'from' index as second argument
      int len = value.length();
      int fromIndex = numArguments == 1 ? 0 : arguments.get(1).getIntValue();
      if (fromIndex < 0)
        throw new RuntimeException("String index for before is less than zero: " + fromIndex);
      else if (fromIndex > len)
        throw new RuntimeException("String index of " + fromIndex + " for before is greater than string length of " + len);
      
      // Watch out for empty regex patterns
      if (pat == null)
        return StringValue.empty;
      if (len == 0)
        return StringValue.empty;
      
      // Find the regex substring
      Pattern p = Pattern.compile(pat);
      Matcher m = p.matcher(value);
      if (m.find(fromIndex))
        // Extract and return the text after the substring
        return new StringValue(value.substring(m.end()));
      else
        // Pattern not found
        return StringValue.empty;
    } else if (name.equals("before") && (numArguments == 1 || numArguments == 2)){
      // Extract and return text  before the first occurrence of a substring
      String s = arguments.get(0).getStringValue();
      
      // Optional 'from' index as second argument
      int len = value.length();
      int fromIndex = numArguments == 1 ? 0 : arguments.get(1).getIntValue();
      if (fromIndex < 0)
        throw new RuntimeException("String index for before is less than zero: " + fromIndex);
      else if (fromIndex > len)
        throw new RuntimeException("String index of " + fromIndex + " for before is greater than string length of " + len);
      
      // Watch out for empty substrings
      if (s == null)
        return StringValue.empty;
      if (len == 0)
        return StringValue.empty;
      
      // Find the substring
      int index = value.indexOf(s, fromIndex);
      if (index < 0)
        // Substring not found
        return StringValue.empty;
      else
        // Extract and return the text before the substring
        return new StringValue(value.substring(0, index));
    } else if (name.equals("beforeRegex") && (numArguments == 1 || numArguments == 2)){
      // Extract and return text before the first occurrence of a regex
      String pat = arguments.get(0).getStringValue();
      
      // Optional 'from' index as second argument
      int len = value.length();
      int fromIndex = numArguments == 1 ? 0 : arguments.get(1).getIntValue();
      if (fromIndex < 0)
        throw new RuntimeException("String index for before is less than zero: " + fromIndex);
      else if (fromIndex > len)
        throw new RuntimeException("String index of " + fromIndex + " for before is greater than string length of " + len);
      
      // Watch out for empty regex patterns
      if (pat == null)
        return StringValue.empty;
      if (len == 0)
        return StringValue.empty;
      
      // Find the regex substring
      Pattern p = Pattern.compile(pat);
      Matcher m = p.matcher(value);
      if (m.find(fromIndex))
        // Extract and return the text before the substring
        return new StringValue(value.substring(0, m.start()));
      else
        // Pattern not found
        return StringValue.empty;
    } else if (name.equals("between") && (numArguments == 2 || numArguments == 3)){
      // Extract and return text between the first occurrence of two substrings
      String s1 = arguments.get(0).getStringValue();
      String s2 = arguments.get(1).getStringValue();
      
      // Optional 'from' index as third argument
      int len = value.length();
      int fromIndex = numArguments == 2 ? 0 : arguments.get(2).getIntValue();
      if (fromIndex < 0)
        throw new RuntimeException("String index for between is less than zero: " + fromIndex);
      else if (fromIndex > len)
        throw new RuntimeException("String index of " + fromIndex + " for between is greater than string length of " + len);
      
      // Watch out for empty substrings
      if (s1 == null)
        return StringValue.empty;
      if (len == 0)
        return StringValue.empty;
      
      // Find the first substring
      int index = value.indexOf(s1, fromIndex);
      if (index < 0)
        // Substring not found
        return StringValue.empty;
      
      // Now find the second substring starting with end of the first
      int index2 = value.indexOf(s2, index + s1.length());
      if (index2 < 0)
        // Second substring not found
        return StringValue.empty;

      // Extract and return the text between the two substrings
      return new StringValue(value.substring(index + s1.length(), index2));
    } else if (name.equals("betweenRegex") && (numArguments == 2 || numArguments == 3)){
      // Extract and return text between the first occurrence of two regexes
      String pat1 = arguments.get(0).getStringValue();
      String pat2 = arguments.get(1).getStringValue();
      
      // Optional 'from' index as third argument
      int len = value.length();
      int fromIndex = numArguments == 2 ? 0 : arguments.get(2).getIntValue();
      if (fromIndex < 0)
        throw new RuntimeException("String index for between is less than zero: " + fromIndex);
      else if (fromIndex > len)
        throw new RuntimeException("String index of " + fromIndex + " for between is greater than string length of " + len);
      
      // Watch out for empty regex patterns
      if (pat1 == null)
        return StringValue.empty;
      if (pat2 == null)
        return StringValue.empty;
      if (len == 0)
        return StringValue.empty;

      // Find the first pattern
      Pattern p1 = Pattern.compile(pat1);
      Matcher m1 = p1.matcher(value);
      if (m1.find(fromIndex)){
        // Find the second pattern
        Pattern p2 = Pattern.compile(pat2);
        Matcher m2 = p2.matcher(value);
        if (m2.find(m1.end())){
          // Extract and return the text between the two regex matches
          return new StringValue(value.substring(m1.end(), m2.start()));
        } else
          // Pattern not found
          return StringValue.empty;
      } else
        // Pattern not found
        return StringValue.empty;
    } else if (name.equals("copy") && numArguments == 0){
      return new StringValue(value);
    } else if (name.equals("endIndexOfRegex") && (numArguments == 1 || numArguments == 2)){
      // Returns the index of the end of the first occurrence of a regex, or -1 if not found
      String pat = arguments.get(0).getStringValue();
      
      // Optional 'from' index as second argument
      int len = value.length();
      int fromIndex = numArguments == 1 ? 0 : arguments.get(1).getIntValue();
      if (fromIndex < 0)
        throw new RuntimeException("String index for indexOf is less than zero: " + fromIndex);
      else if (fromIndex > len)
        throw new RuntimeException("String index of " + fromIndex + " for indexOf is greater than string length of " + len);
      
      // Watch out for empty regex
      if (pat == null || pat.length() == 0)
        return new IntegerValue(-1);
      if (len == 0)
        return new IntegerValue(-1);
      
      
      // Find the regex substring
      Pattern p = Pattern.compile(pat);
      Matcher m = p.matcher(value);
      if (m.find(fromIndex))
        // Return index of the end of the first occurrence of the regex pattern
        return new IntegerValue(m.end());
      else
        // Pattern not found
        return new IntegerValue(-1);
    } else if (name.equals("equals") && numArguments == 1){
      String otherString = arguments.get(0).getStringValue();
      return BooleanValue.create(value.equals(otherString));
    } else if (name.equals("equalsIgnoreCase") && numArguments == 1){
      String otherString = arguments.get(0).getStringValue();
      return BooleanValue.create(value.equalsIgnoreCase(otherString));
    } else if ((name.equals("get") || name.equals("charAt")) && numArguments == 1){
      int len = value.length();
      int index = arguments.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("String index is less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("String index of " + index + " is greater than string length of " + len + " minus 1");
      
      return new StringValue(value.substring(index, index + 1));
    } else if (name.equals("get") && numArguments == 2){
      int len = value.length();
      int beginIndex = arguments.get(0).getIntValue();
      if (beginIndex < 0)
        throw new RuntimeException("String index is less than zero: " + beginIndex);
      else if (beginIndex >= len)
        throw new RuntimeException("String index of " + beginIndex + " is greater than string length of " + len + " minus 1");
      int endIndex = arguments.get(1).getIntValue();
      if (endIndex < 0)
        throw new RuntimeException("String index is less than zero: " + endIndex);
      else if (endIndex > len)
        throw new RuntimeException("String index of " + endIndex + " is greater than string length of " + len);
      
      return new StringValue(value.substring(beginIndex, endIndex));
    } else if (name.equals("indexOf") && (numArguments == 1 || numArguments == 2)){
      // Returns the index of the first occurrence of a substring, or -1 if not found
      String s = arguments.get(0).getStringValue();
      
      // Optional 'from' index as second argument
      int len = value.length();
      int fromIndex = numArguments == 1 ? 0 : arguments.get(1).getIntValue();
      if (fromIndex < 0)
        throw new RuntimeException("String index for indexOf is less than zero: " + fromIndex);
      else if (fromIndex > len)
        throw new RuntimeException("String index of " + fromIndex + " for indexOf is greater than string length of " + len);
      
      // Watch out for empty substrings
      if (s == null)
        return new IntegerValue(-1);
      if (len == 0)
        return new IntegerValue(-1);
      
      // Find and return the index of the substring
      int index = value.indexOf(s, fromIndex);
      return new IntegerValue(index);
    } else if (name.equals("indexOfRegex") && (numArguments == 1 || numArguments == 2)){
      // Returns the index of the first occurrence of a regex, or -1 if not found
      String pat = arguments.get(0).getStringValue();
      
      // Optional 'from' index as second argument
      int len = value.length();
      int fromIndex = numArguments == 1 ? 0 : arguments.get(1).getIntValue();
      if (fromIndex < 0)
        throw new RuntimeException("String index for indexOf is less than zero: " + fromIndex);
      else if (fromIndex > len)
        throw new RuntimeException("String index of " + fromIndex + " for indexOf is greater than string length of " + len);
      
      // Watch out for empty regex
      if (pat == null || pat.length() == 0)
        return new IntegerValue(-1);
      if (len == 0)
        return new IntegerValue(-1);
      
      
      // Find the regex substring
      Pattern p = Pattern.compile(pat);
      Matcher m = p.matcher(value);
      if (m.find(fromIndex))
        // Return index of first occurrence of the regex pattern
        return new IntegerValue(m.start());
      else
        // Pattern not found
        return new IntegerValue(-1);
    } else if (name.equals("insert") && numArguments == 2){
      int index = arguments.get(0).getIntValue();
      String s = arguments.get(1).getStringValue();
      int len = value.length();
      if (index < 0)
        throw new RuntimeException("String index for insert is less than zero: " + index);
      else if (index > len)
        throw new RuntimeException("String index of " + index + " for insert is greater than string length of " + len);
      else {
        // Insert new string into existing string at specified index
        value = value.substring(0, index) + s + value.substring(index);
        
        // Return this string object as the result
        return this;
      }
    } else if (name.equals("lower")){
      // Lower case the existing value
      value = value.toLowerCase();

      // Return this string object as the result
      return this;
    } else if (name.equals("matches") && numArguments == 1){
      // Returns true if regex pattern argument matches the entire string
      String pat = arguments.get(0).getStringValue();
      
      // Watch out for missing pattern
      if (pat == null)
        return FalseValue.one;

      // Try the match and return the result
      return BooleanValue.create(value.matches(pat));
    } else if ((name.equals("put") || name.equals("set")) && numArguments == 2){
      int len = value.length();
      int index = arguments.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("String index is less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("String index of " + index + " is greater than string length of " + len + " minus 1");
      String newSubstring = arguments.get(1).getStringValue();

      // Update string value in-place
      value = value.substring(0, index) + newSubstring + value.substring(index + 1); 
      
      // Return the current string with its revised value
      return this;
    } else if ((name.equals("put") || name.equals("set")) && numArguments == 3){
      int len = value.length();
      int beginIndex = arguments.get(0).getIntValue();
      if (beginIndex < 0)
        throw new RuntimeException("String index is less than zero: " + beginIndex);
      else if (beginIndex >= len)
        throw new RuntimeException("String index of " + beginIndex + " is greater than string length of " + len + " minus 1");
      int endIndex = arguments.get(1).getIntValue();
      if (endIndex < 0)
        throw new RuntimeException("String index is less than zero: " + endIndex);
      else if (endIndex > len)
        throw new RuntimeException("String index of " + endIndex + " is greater than string length of " + len);
      String newSubstring = arguments.get(2).getStringValue();

      // Update string value in-place
      value = value.substring(0, beginIndex) + newSubstring + value.substring(endIndex); 
      
      // Return the current string with its revised value
      return this;
    } else if (name.equals("remove") && (numArguments == 1 || numArguments == 2)){
      int index = arguments.get(0).getIntValue();
      int endIndex = numArguments == 2 ? arguments.get(1).getIntValue() : index + 1;
      int len = value.length();
      if (index < 0)
        throw new RuntimeException("String index for remove is less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("String index of " + index + " for remove is greater than string length of " + len + " (minus one)");
      if (endIndex < 0)
        throw new RuntimeException("String index for remove less than zero: " + endIndex);
      else if (endIndex > len)
        throw new RuntimeException("String index of " + endIndex + " for remove is greater than string length of " + len);
      else {
        // Remove the specified substring range from existing string
        value = value.substring(0, index) + value.substring(endIndex);

        // Return this string object as the result
        return this;
      }
    } else if (name.equals("split") && numArguments == 1){
      // Splits a string into a list of substrings based on a delimiter regex pattern
      String pat = arguments.get(0).getStringValue();

      // Treat missing or empty pattern as no split - return a list with the original string as single element
      if (pat == null || pat.length() == 0){
        List<Value> stringList = new ArrayList<Value>();
        stringList.add(new StringValue(value));
        return new ListValue(StringTypeNode.one, stringList);
      }

      // Do the split
      String[] strings = value.split(pat);
      
      // Return the strings as a list - but don't return a single empty string if input was empty
      List<Value> stringList = new ArrayList<Value>();
      if (strings.length > 1 || strings[0].length() > 0)
        for (String s: strings)
          stringList.add(new StringValue(s));
      return new ListValue(StringTypeNode.one, stringList);
    } else if ((name.equals("substr") || name.equals("substring")) && (numArguments == 1 || numArguments == 2)){
        int index = arguments.get(0).getIntValue();
        int len = value.length();
        int endIndex = numArguments == 1 ? len : arguments.get(1).getIntValue();
        if (index < 0)
          throw new RuntimeException("String index for " + name + " is less than zero: " + index);
        else if (index >= len)
          throw new RuntimeException("String index of " + index + " for " + name + " is greater than string length of " + len + " (minus one)");
        if (endIndex < 0)
          throw new RuntimeException("String index for " + name + " less than zero: " + endIndex);
        else if (endIndex > len)
          throw new RuntimeException("String index of " + endIndex + " for " + name + " is greater than string length of " + len);
        else {
          // Extract substring of existing string
          String subString = value.substring(index, endIndex);
          
          // Generate a new string value node and return the substring in it
          return new StringValue(subString);
        }
    } else if (name.equals("upper")){
      // Upper case the existing value
      value = value.toUpperCase();

      // Return this string object as the result
      return this;
    } else
      return super.getMethodValue(scriptState, name, arguments);
  }

  public Value getSubscriptedValue(ScriptState scriptState, List<Value> subscriptValues) throws RuntimeException {
    int numSubscripts = subscriptValues.size();
    if (numSubscripts == 1){
      // Fetch character at that index
      int len = value.length();
      int index = subscriptValues.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("String index less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("String index of " + index + " is greater than string length of " + len + " (minus one)");
      else
        return new StringValue(Character.toString(value.charAt(index)));
    } else if (numSubscripts == 2){
      // Fetch substring starting at first index and ending before the second index
      int len = value.length();
      int index = subscriptValues.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("String index less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("String index of " + index + " is greater than string length of " + len + " (minus one)");
      int endIndex = subscriptValues.get(1).getIntValue();
      if (endIndex < 0)
        throw new RuntimeException("String index less than zero: " + endIndex);
      else if (endIndex > len)
        throw new RuntimeException("String index of " + endIndex + " is greater than string length of " + len);
      return new StringValue(value.substring(index, endIndex));
    } else
      throw new RuntimeException("Strings do not support " + numSubscripts + " subscripts");
  }

  public Value putSubscriptedValue(ScriptState scriptState, List<Value> subscriptValues, Value newValue) throws RuntimeException {
    int numSubscripts = subscriptValues.size();
    if (numSubscripts == 1){
      // Replace character at that index
      int len = value.length();
      int index = subscriptValues.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("String index less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("String index of " + index + " is greater than string length of " + len + " (minus one)");
      else {
        // Get the replacement string value
        String newSubstring = newValue.getStringValue();
        
        // Replace the indexed character
        value = value.substring(0, index) + newSubstring + value.substring(index + 1); 
        
        // Return the current string with its revised value
        return this;
      }
    } else if (numSubscripts == 2){
      // Fetch substring starting at first index and ending before the second index
      int len = value.length();
      int index = subscriptValues.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("String index less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("String index of " + index + " is greater than string length of " + len + " (minus one)");
      int endIndex = subscriptValues.get(1).getIntValue();
      if (endIndex < 0)
        throw new RuntimeException("String index less than zero: " + endIndex);
      else if (endIndex > len)
        throw new RuntimeException("String index of " + endIndex + " is greater than string length of " + len);
      else {
        // Get the replacement string value
        String newSubstring = newValue.getStringValue();
        
        // Replace the indexed character
        value = value.substring(0, index) + newSubstring + value.substring(endIndex); 
        
        // Return the current string with its revised value
        return this;
      }
    } else
      throw new RuntimeException("Strings do not support " + numSubscripts + " subscripts");
  }

  public int compareValue(Value otherValue){
    String leftValue = getStringValue();
    String rightValue = otherValue.getStringValue();
    return leftValue.compareTo(rightValue);
  }

  public Value copyOnAssignment(){
    // For most values, no need to make a copy of actual value on assignment, only strings 
    return new StringValue(value);
  }

  public Value clone(){
    return new StringValue(value);
  }

  public boolean equals(Value valueNode){
    if (valueNode instanceof StringValue){
      String otherValue = valueNode.getStringValue();
      if (value == null)
        return otherValue == null;
      else if (otherValue == null)
        return false;
      else
        return value.equals(otherValue);
    } else
      return false;
  }
  
  public String toJson(){
    StringBuilder sb = new StringBuilder("\"");
    if (value == null ){
      log.error("value of StingValue is null");
      return "";
    }
    int len = value.length();
    for (int i = 0; i < len; i++){
      char ch = value.charAt(i);
      if (ch == '"' || ch == '\\')
        sb.append('\\');
      sb.append(ch);
    }
    sb.append('"');
    return sb.toString();
  }

  public String toString(){
    return value;
  }

  public String toText(){
    return toString();
  }

  public String toXml(){
    return XmlUtils.escapeEntities(value);
  }

  public String getTypeString(){
    return "string";
  }

}
