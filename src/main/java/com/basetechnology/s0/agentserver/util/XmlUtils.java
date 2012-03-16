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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.intermediate.ObjectTypeNode;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;
import com.basetechnology.s0.agentserver.script.runtime.value.FieldValue;
import com.basetechnology.s0.agentserver.script.runtime.value.ListValue;
import com.basetechnology.s0.agentserver.script.runtime.value.MapValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

// TODO: Add option for whether to throw errors or silently ignore/fix them

public class XmlUtils {
  
  String xmlString;
  int len;
  List<String> elementNames;
  List<Map<String, Integer>> elementNameCounters;
  List<Value> elementValues;
  int nextCharIndex;
  StringBuilder nextItem;
  boolean unqiuelyNameRepeatedElements;
  boolean ignoreAttributes;
  
  public char getChar(){
    if (nextCharIndex < len)
      return xmlString.charAt(nextCharIndex);
    else
      return 0; 
  }
  
  public char getNonBlankChar(){
    char ch = getChar();
    while (Character.isWhitespace(ch))
      ch = getNextChar();
    return ch;
  }
  
  public char peekChar(int i){
    if (nextCharIndex + i < len)
      return xmlString.charAt(nextCharIndex + i);
    else
      return 0; 
  }
  
  public char getNextChar(){
    if (nextCharIndex < len)
      nextCharIndex++;
    return getChar();
  }
  
  public char getNextNonBlankChar(){
    if (nextCharIndex < len)
      nextCharIndex++;
    return getNonBlankChar();
  }

  protected void processEndElement(ScriptState scriptState, String elementName) throws RuntimeException {
    // Pop the stack
    String poppedElementName = elementNames.remove(elementNames.size() - 1);
    Map<String, Integer> poppedElementNameCounters = elementNameCounters.remove(elementNameCounters.size() - 1);
    Value poppedElementValueNode = elementValues.remove(elementValues.size() - 1);
    
    // Make sure start and end element names match
    if (! elementName.equals(poppedElementName))
      throw new XmlParserException("End element tag name of </" + elementName + "> does not match start element tag of <" + poppedElementName + ">");

    // Get the accumulated text for this element
    String text = unescapeEntities(nextItem.toString());
    
    // Reset the element text for any enclosing element
    nextItem = new StringBuilder();

    // Get current structure
    Value valueNode = elementValues.get(elementValues.size() - 1);
    
    // Build structure
    Value newValueNode = null;
    if (poppedElementValueNode instanceof NullValue)
      newValueNode = new StringValue(text);
    else if (poppedElementValueNode instanceof MapValue){
      // Add element text as the 'text_n' attribute for this element - if non-white space
      MapValue mapValueNode = (MapValue)poppedElementValueNode;
      List<Value> subscriptValues = new ArrayList<Value>();
      if (text.trim().length() > 0){
        // Generate a name for this implied element
        Integer elementNameCounter = poppedElementNameCounters.get("text");
        if (elementNameCounter == null)
          elementNameCounter = 0;
        String textName = "text_" + ++elementNameCounter;
        poppedElementNameCounters.put("text", elementNameCounter);
        subscriptValues.add(new StringValue(textName));
        poppedElementValueNode.putSubscriptedValue(scriptState, subscriptValues, new StringValue(text));
      }

      newValueNode = poppedElementValueNode;
    }
    if (valueNode instanceof NullValue){
      // Start structure with a map
      List<FieldValue> fieldValues = new ArrayList<FieldValue>();
      FieldValue fieldValueNode = new FieldValue(elementName, newValueNode);
      fieldValues.add(fieldValueNode);
      newValueNode = new MapValue(ObjectTypeNode.one, (List<Value>)(Object)fieldValues);
      elementValues.set(elementValues.size() - 1, newValueNode);
    } else if (valueNode instanceof MapValue){
      // Check if element already exists
      MapValue mapValueNode = (MapValue)valueNode;
      List<Value> subscriptValues = new ArrayList<Value>();
      subscriptValues.add(new StringValue(elementName));
      Value existingValueNode = null;
      existingValueNode = mapValueNode.getSubscriptedValue(scriptState, subscriptValues);
      if (existingValueNode instanceof NullValue){
        // Element name does not yet exist, simply add it
        existingValueNode = mapValueNode.putSubscriptedValue(scriptState, subscriptValues, newValueNode);
      } else if (existingValueNode instanceof ListValue) {
        // Add to existing list for this element name
        ListValue listValueNode = (ListValue)existingValueNode;
        listValueNode.appendValue(newValueNode);
      } else {
        // If uniqueness required, append "_n" to name
        if (unqiuelyNameRepeatedElements){
          Integer elementNameCounter = poppedElementNameCounters.get(elementName);
          if (elementNameCounter == null)
            elementNameCounter = 0;
          String elementNameSuffixed = elementName + '_' + ++elementNameCounter;
          poppedElementNameCounters.put(elementName, elementNameCounter);
          subscriptValues = new ArrayList<Value>();
          subscriptValues.add(new StringValue(elementNameSuffixed));
          existingValueNode = mapValueNode.putSubscriptedValue(scriptState, subscriptValues, newValueNode);
        } else {
          // Create a list since we now have two items
          List<Value> valueList = new ArrayList<Value>();
          valueList.add(existingValueNode);
          valueList.add(newValueNode);
          newValueNode = new ListValue(ObjectTypeNode.one, (List<Value>)(Object)valueList);
          mapValueNode.putSubscriptedValue(scriptState, subscriptValues, newValueNode);
        }
      }
    } else if (valueNode instanceof ListValue){
      // Append to list
      ListValue listValueNode = (ListValue)valueNode;
      listValueNode.appendValue(newValueNode);
    } else {
      // ?? What else??
      // TODO: 
    }
  }

  public void processUnassociatedText(ScriptState scriptState) throws RuntimeException{
    if (nextItem.toString().trim().length() > 0){
      
      // Get the accumulated text for this element
      String text = unescapeEntities(nextItem.toString().trim());
      
      // Generate a name for this implied element
      int stackSize = elementNameCounters.size();
      int topIndex = stackSize - 2;
      if (topIndex < 0)
        topIndex = 0;
      Map<String, Integer> elementNameCounterMap = elementNameCounters.get(topIndex);
      int elementTextCounter = 0;
      if (elementNameCounterMap.containsKey("text"))
        elementTextCounter = elementNameCounterMap.get("text");
      elementTextCounter++;
      elementNameCounterMap.put("text", elementTextCounter);
      String textName = "text_" + elementTextCounter;
      
      // Reset the element text for any enclosing element
      nextItem = new StringBuilder();

      // Get current structure
      Value valueNode = elementValues.get(topIndex);
      
      // Build structure
      Value newValueNode = null;
      String poppedElementName = elementNames.get(topIndex);
      Value poppedElementValueNode = elementValues.get(topIndex);
      boolean done = false;
      if (poppedElementValueNode instanceof NullValue)
        newValueNode = new StringValue(text);
      else if (poppedElementValueNode instanceof MapValue){
        // Add element text as the '_text' attribute for this element - if non-white space
        MapValue mapValueNode = (MapValue)poppedElementValueNode;
        List<Value> subscriptValues = new ArrayList<Value>();
        if (text.trim().length() > 0){
          subscriptValues.add(new StringValue(textName));
          poppedElementValueNode.putSubscriptedValue(scriptState, subscriptValues, new StringValue(text));
        }

        newValueNode = poppedElementValueNode;
        
        done = true;
      }

      if (! done){
        if (valueNode instanceof NullValue){
          // Start structure with a map
          List<FieldValue> fieldValues = new ArrayList<FieldValue>();
          FieldValue fieldValueNode = new FieldValue(textName, newValueNode);
          fieldValues.add(fieldValueNode);
          newValueNode = new MapValue(ObjectTypeNode.one, (List<Value>)(Object)fieldValues);
          elementValues.set(topIndex, newValueNode);
        } else if (valueNode instanceof MapValue){
          // Check if element already exists
          MapValue mapValueNode = (MapValue)valueNode;
          List<Value> subscriptValues = new ArrayList<Value>();
          subscriptValues.add(new StringValue(textName));
          Value existingValueNode = null;
          existingValueNode = mapValueNode.getSubscriptedValue(scriptState, subscriptValues);
          if (existingValueNode instanceof NullValue){
            // Element name does not yet exist, simply add it
            existingValueNode = mapValueNode.putSubscriptedValue(scriptState, subscriptValues, newValueNode);
          } else if (existingValueNode instanceof ListValue) {
            // Add to existing list for this element name
            ListValue listValueNode = (ListValue)existingValueNode;
            listValueNode.appendValue(newValueNode);
          } else {
            // Create a list since we now have two items
            List<Value> valueList = new ArrayList<Value>();
            valueList.add(existingValueNode);
            valueList.add(newValueNode);
            newValueNode = new ListValue(ObjectTypeNode.one, (List<Value>)(Object)valueList);
            mapValueNode.putSubscriptedValue(scriptState, subscriptValues, newValueNode);
          }
        } else if (valueNode instanceof ListValue){
          // Append to list
          ListValue listValueNode = (ListValue)valueNode;
          listValueNode.appendValue(newValueNode);
        } else {
          // ?? What else??
          // TODO: 
        }
      }

    }

  }

  public Value parseHtml(ScriptState scriptState, String xmlString) throws RuntimeException {
    return parseXml(scriptState, xmlString, true, true);
  }

  public Value parseXml(ScriptState scriptState, String xmlString) throws RuntimeException {
    return parseXml(scriptState, xmlString, false, false);
  }

  public Value parseXml(ScriptState scriptState, String xmlString,
      boolean unqiuelyNameRepeatedElements, boolean ignoreAttributes) throws RuntimeException {
    this.xmlString = xmlString;
    this.len = xmlString.length();
    this.nextCharIndex = 0;
    this.elementNames = new ArrayList<String>();
    this.elementValues = new ArrayList<Value>();
    this.elementNameCounters = new ArrayList<Map<String, Integer>>();
    this.unqiuelyNameRepeatedElements = unqiuelyNameRepeatedElements;
    this.ignoreAttributes = ignoreAttributes;

    // Start with empty stack
    elementNames.add("<top>");
    elementNameCounters.add(new HashMap<String, Integer>());
    elementValues.add(NullValue.one);

    char ch = getChar();
    nextItem = new StringBuilder();
    while (ch != 0){
      ch = getChar();
      if (ch == '<'){
        ch = getNextChar();
        if (ch == '?'){
          // Parse <? ... /> directive
          ch = getNextChar();
          while (ch != 0 && ! (ch == '?' && peekChar(1) == '>'))
            ch = getNextChar();
          
          // Skip over end of the directive
          ch = getNextChar();
          ch = getNextChar();
        } else if (ch == '/'){
          // End of an element

          // Skip over the '/'
          ch = getNextChar();
          
          // Parse the element name
          String elementName = "";
          while (ch != 0 && ch != '>'){
            elementName += ch;
            ch = getNextChar();
          }
          
          // Skip the '> ending the end of the element
          if (ch == '>')
            ch = getNextChar();

          // Process the whole element now
          processEndElement(scriptState, elementName);
        } else {
          // start of a new element
          
          // Parse the element name
          String elementName = "";
          while (ch != 0 && ch != ' ' && ch != '>'){
            elementName += ch;
            ch = getNextChar();
          }

          // Parse the element attributes
          List<FieldValue> attributeValues = new ArrayList<FieldValue>();
          char lastCh = 0;
          ch = getNonBlankChar();
          while (ch != 0 && ch != '>'){
            // Parse the attribute name
            if (! Character.isLetter(ch)){
              // Skip junk
              lastCh = ch;
              ch = getNextNonBlankChar();
              continue;
            }
            StringBuilder attributeNameBuilder = new StringBuilder();
            while (ch != 0 && ch != '>' && ch != '=' && ch != ' '){
              attributeNameBuilder.append(ch);
              ch = getNextChar();
            }
            String attributeName = attributeNameBuilder.toString();

            // Skip white space
            ch = getNonBlankChar();

            // Parse the '=' and skip any white space
            if (ch != '=') {
              // TODO: What to do here for error recovery?
            }
            ch = getNextNonBlankChar();

            // Parse the attribute value
            StringBuilder attributeValueBuilder = new StringBuilder();
            if (ch == '"'){
              // Parse the quoted string attribute value
              ch = getNextChar();
              while (ch != 0 && ch != '>' && ch != '"'){
                if (ch == '\\')
                  ch = getNextChar();
                attributeValueBuilder.append(ch);
                ch = getNextChar();
              }
              
              // Skip over the closing '"'
              if (ch == '"')
                ch = getNextChar();
            } else {
              // Parse the non-quoted attribute value
              attributeValueBuilder.append(ch);
              while (ch != 0 && ch != '>' && ch != '=' && ch != ' '){
                attributeValueBuilder.append(ch);
                ch = getNextChar();
              }
            }
            String attributeValue = unescapeEntities(attributeValueBuilder.toString());

            // Store the attribute value
            FieldValue attributeFieldValueNode = new FieldValue(attributeName, new StringValue(attributeValue));
            attributeValues.add(attributeFieldValueNode);

            // Peek at the next non-white space character after this attribute
            lastCh = ch;
            ch = getNonBlankChar();
          }

          // Skip the '> ending the start of the element
          if (ch == '>')
            ch = getNextChar();

          // Push the new element name on the stack
          elementNames.add(elementName);

          // Initialize its text counter
          elementNameCounters.add(new HashMap<String, Integer>());
          
          // Push the attribute map, or null if no attributes
          if (attributeValues.size() > 0 && ! ignoreAttributes)
            elementValues.add(new MapValue(ObjectTypeNode.one, (List<Value>)(Object)attributeValues));
          else
            elementValues.add(NullValue.one);

          // If there is unassociated text floating around, place it into a text element
          if (nextItem.toString().trim().length() > 0)
            processUnassociatedText(scriptState);
          
          // Start accumulating text for the new element
          nextItem = new StringBuilder();
          
          // For attribute-only element (ends with "/>"), need to store its value now
          if (lastCh == '/'){
            // Process the whole element now
            processEndElement(scriptState, elementName);
          }
        }
      } else {
        // Save this character of element text and move on to the next character
        nextItem.append(ch);
        ch = getNextChar();
      }
    }

    // If there is unassociated text floating around, place it into a text element
    if (nextItem.toString().trim().length() > 0)
      processUnassociatedText(scriptState);

    // Return the value on the top of the stack
    return elementValues.remove(elementValues.size() - 1);
  }

  static public String escapeEntities (String string){
    return StringEscapeUtils.escapeHtml4(string);
  }

  static public String unescapeEntities (String xmlString){
    return StringEscapeUtils.unescapeHtml4(xmlString);
  }
  
  static void formatJsonObjectAsXml(StringBuilder sb, JSONObject objectJson, String elementName, int level, int indent){
    if (elementName != null)
      sb.append("<" + elementName + ">");
    Map<String, Value> treeMap = new TreeMap<String, Value>();
    for (Iterator<String> it = objectJson.keys(); it.hasNext(); )
      treeMap.put(it.next(), null);
    for (String key: treeMap.keySet()){
      Object object = objectJson.opt(key);
      if (object instanceof JSONObject)
        formatJsonObjectAsXml(sb, (JSONObject)object, key, level + 1, indent);
      else if (object instanceof JSONArray)
        formatJsonArrayAsXml(sb, (JSONArray)object, key, level + 1, indent);
      else if (object instanceof String)
        // TODO: Need to escape entities
        sb.append("<" + key + ">" + escapeEntities(object.toString()) + "</" + key + ">");
      else
        sb.append("<" + key + ">" + escapeEntities(object.toString()) + "</" + key + ">");
    }
    if (elementName != null)
      sb.append("</" + elementName + ">");
  }

  static void formatJsonArrayAsXml(StringBuilder sb, JSONArray arrayJson, String elementName, int level, int indent){
    int numElements = arrayJson.length();
    for (int i = 0; i < numElements; i++){
      Object object = arrayJson.opt(i);
      if (object instanceof JSONObject)
        formatJsonObjectAsXml(sb, (JSONObject)object, elementName, level + 1, indent);
      else if (object instanceof JSONArray)
        formatJsonArrayAsXml(sb, (JSONArray)object, elementName, level + 1, indent);
      else if (object instanceof String)
        // TODO: Need to escape entities
        sb.append((elementName == null ? "" : "<" + elementName + ">") + escapeEntities(object.toString()) + (elementName == null ? "" : "</" + elementName + ">"));
    }
  }

  static public String formatJsonAsXml(JSONObject objectJson){
    return formatJsonAsXml(objectJson, -1);
  }

  static public String formatJsonAsXml(JSONObject objectJson, int indent){
    StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    if (indent >= 0)
      sb.append(System.getProperty("line.separator"));
    formatJsonObjectAsXml(sb, objectJson, null, 0, indent);
    return sb.toString();
  }
}
