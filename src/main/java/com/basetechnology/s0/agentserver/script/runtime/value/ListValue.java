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

import java.util.ArrayList;
import java.util.List;

import com.basetechnology.s0.agentserver.RuntimeException;
import com.basetechnology.s0.agentserver.script.intermediate.AddNode;
import com.basetechnology.s0.agentserver.script.intermediate.DivideNode;
import com.basetechnology.s0.agentserver.script.intermediate.ListTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.ObjectTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.runtime.ScriptState;

public class ListValue extends Value {
  public TypeNode type;
  public List<Value> value;

  public ListValue(){
    this.type = ObjectTypeNode.one;
    this.value = new ArrayList<Value>();
  }

  public ListValue(TypeNode type){
    this.type = type;
    this.value = new ArrayList<Value>();
  }

  public ListValue(TypeNode type, List<Value> value){
    this.type = type;
    this.value = value;
  }

  public TypeNode getType(){
    return ListTypeNode.one;
  }

  public Object getValue(){
    return value;
  }

  public boolean getBooleanValue(){
    return value != null && value.size() > 0;
  }

  public long getLongValue(){
    return value.size();
  }

  public double getDoubleValue(){
    return value.size();
  }

  public String getStringValue(){
    return Integer.toString(value.size());
  }

  public void appendValue(Value newValue){
    // Append the new value
    value.add(newValue);
  }

  public Value getNamedValue(ScriptState scriptState, String name) throws RuntimeException {
    if (name.equals("avg") || name.equals("average")){
      // See if any elements in list
      int numElements = value.size();
      if (numElements == 0)
        // No, then it has no average value
        return NullValue.one;

      // Iterate over list to sum values
      Value sum = new IntegerValue(0);
      for (Value valueNode: value)
        sum = AddNode.add(sum, valueNode);

      // Calculate and return the average
      return DivideNode.divide(sum, new IntegerValue(numElements));
    } if (name.equals("length") || name.equals("size")){
      return new IntegerValue(value.size());
    } else if (name.equals("clear")){
      // Clear the list
      value.clear();

      // No return value
      return NullValue.one;
    } else if (name.equals("concat")){
      // Combine all elements into a single string with space as a delimiter
      StringBuilder sb = new StringBuilder();
      for (Value valueNode: value){
        if (sb.length() > 0)
          sb.append(' ');
        sb.append(valueNode.toString());
      }

      // Return the combined string
      return new StringValue(sb.toString());
    } else if (name.equals("max")){
      // See if any elements in list
      int numElements = value.size();
      if (numElements == 0)
        // No, then it has no maximum value
        return NullValue.one;

      // Iterate over list looking for maximum value
      Value maxValueNode = value.get(0);
      for (Value valueNode: value)
        if (valueNode.compareValue(maxValueNode) > 0)
          maxValueNode = valueNode;

      // Return the maximum value
      return maxValueNode;
    } else if (name.equals("min")){
      // See if any elements in list
      int numElements = value.size();
      if (numElements == 0)
        // No, then it has no minimum value
        return NullValue.one;

      // Iterate over list looking for minimum value
      Value minValueNode = value.get(0);
      for (Value valueNode: value)
        if (valueNode.compareValue(minValueNode) < 0)
          minValueNode = valueNode;

      // Return the minimum value
      return minValueNode;
    } else if (name.equals("sum")){
      // See if any elements in list
      int numElements = value.size();
      if (numElements == 0)
        // No, then it has no average value
        return NullValue.one;

      // Iterate over list to sum values
      Value sum = new IntegerValue(0);
      for (Value valueNode: value)
        sum = AddNode.add(sum, valueNode);

      // Return the sum
      return sum;
    } else if (name.equals("text")){
      // Accumulate text value of elements as a single space-delimited string of text
      // See if any elements in list
      int numElements = value.size();
      if (numElements == 0)
        // No, then it has no average value
        return NullValue.one;

      // Iterate over list
      Value sum = new IntegerValue(0);
      for (Value valueNode: value)
        sum = AddNode.add(sum, valueNode);

      // Return the sum
      return sum;
    } else
      return super.getNamedValue(scriptState, name);
  }

  public Value getMethodValue(ScriptState scriptState, String name, List<Value> arguments) throws RuntimeException {
    int numArguments = arguments.size();
    if ((name.equals("length") || name.equals("size")) && numArguments == 0)
      return new IntegerValue(value.size());
    else if ((name.equals("add") || name.equals("put") || name.equals("set")) && numArguments == 1){
      // Append the new value
      appendValue(arguments.get(0));
      // TODO: Find out what this Java return value is really all about
      return TrueValue.one;
    } if ((name.equals("avg") || name.equals("average")) && numArguments == 0){
      // See if any elements in list
      int numElements = value.size();
      if (numElements == 0)
        // No, then it has no average value
        return NullValue.one;

      // Iterate over list to sum values
      Value sum = new IntegerValue(0);
      for (Value valueNode: value)
        sum = AddNode.add(sum, valueNode);

      // Calculate and return the average
      return DivideNode.divide(sum, new IntegerValue(numElements));
    } else if (name.equals("clear") && numArguments == 0){
      // Clear the list
      value.clear();

      // No return value
      return NullValue.one;
      // TODO: Add "contains"
    } else if (name.equals("concat") && numArguments <= 1){
      // Combine all elements into a single string specified delimiter (or space) between them
      StringBuilder sb = new StringBuilder();
      String delimiter = " ";
      if (numArguments == 1)
        delimiter = arguments.get(0).getStringValue();
      for (Value valueNode: value){
        if (sb.length() > 0)
          sb.append(delimiter);
        sb.append(valueNode.toString());
      }

      // Return the combined string
      return new StringValue(sb.toString());
    } else if (name.equals("count") && (numArguments == 1 || numArguments == 2)){
      // Get the search term (string or list)
      Value term = arguments.get(0);
      
      // Get the optional starting index
      int startIndex = 0;
      if (numArguments == 2)
        startIndex = arguments.get(1).getIntValue();
      if (startIndex < 0)
        startIndex = 0;
      
      // Handle string and list (phrase) search as distinct cases
      int count = 0;
      if (term instanceof ListValue){
        // Search for a phrase
        // No match if search phrase is empty
        ListValue valuePhrase = (ListValue)term;
        int numElementsPhrase = valuePhrase.value.size();
        if (numElementsPhrase == 0)
          return new IntegerValue(0);
        
        // No need to search if phrase is longer than list (starting at start index)
        int numElements = value.size();
        if (numElementsPhrase > numElements - startIndex)
          return new IntegerValue(0);

        // Get local copy of the strings in the phrase
        List<String> phraseStrings = valuePhrase.getStrings(0, numElementsPhrase - 1);
        
        // Pre-fetch the initial candidate subset of list to match against
        List<String> thisStrings = getStrings(startIndex, startIndex + numElementsPhrase - 1);
        
        // Search through the list for a match
        for (int i = startIndex; i < numElements - numElementsPhrase + 1; i++){
          // Check match of this subset
          boolean match = true;
          for (int j = 0; j < numElementsPhrase; j++)
            if (! thisStrings.get(j).equals(phraseStrings.get(j))){
              match = false;
              break;
            }
          if (match)
            count++;
          
          // Now fetch the next string from this list and shift onto pre-fetched list
          if (i < numElements - numElementsPhrase){
            thisStrings.remove(0);
            thisStrings.add(value.get(i + numElementsPhrase).getStringValue());
          }
        }
      } else {
        // Search for a single term
        String termText = term.getStringValue();
        int numElements = value.size();
        for (int i = startIndex; i < numElements; i++)
          if (value.get(i).toString().equals(termText))
            count++;
      }
      
      // Return the count of the matched term(s)
      return new IntegerValue(count);
    } else if (name.equals("find") && (numArguments == 1 || numArguments == 2)){
      // Get the search term (string or list)
      Value term = arguments.get(0);
      
      // Get the optional starting index
      int startIndex = 0;
      if (numArguments == 2)
        startIndex = arguments.get(1).getIntValue();
      if (startIndex < 0)
        startIndex = 0;
      
      // Handle string and list (phrase) search as distinct cases
      int foundIndex = -1;
      if (term instanceof ListValue){
        // Search for a phrase
        // No match if search phrase is empty
        ListValue valuePhrase = (ListValue)term;
        int numElementsPhrase = valuePhrase.value.size();
        if (numElementsPhrase == 0)
          return new IntegerValue(-1);
        
        // No need to search if phrase is longer than list (starting at start index)
        int numElements = value.size();
        if (numElementsPhrase > numElements - startIndex)
          return new IntegerValue(-1);

        // Get local copy of the strings in the phrase
        List<String> phraseStrings = valuePhrase.getStrings(0, numElementsPhrase - 1);
        
        // Pre-fetch the initial candidate subset of list to match against
        List<String> thisStrings = getStrings(startIndex, startIndex + numElementsPhrase - 1);
        
        // Search through the list for a match
        for (int i = startIndex; i < numElements - numElementsPhrase + 1; i++){
          // Check match of this subset
          boolean match = true;
          for (int j = 0; j < numElementsPhrase; j++)
            if (! thisStrings.get(j).equals(phraseStrings.get(j))){
              match = false;
              break;
            }
          if (match){
            foundIndex = i;
            break;
          }

          // Now fetch the next string from this list and shift onto pre-fetched list
          if (i < numElements - numElementsPhrase){
            thisStrings.remove(0);
            thisStrings.add(value.get(i + numElementsPhrase).getStringValue());
          }
        }
      } else {
        // Search for a single term
        String termText = term.getStringValue();
        int numElements = value.size();
        for (int i = startIndex; i < numElements; i++){
          if (value.get(i).toString().equals(termText)){
            foundIndex = i;
            break;
          }
        }
      }
      
      // Return the index of the matched term(s)
      return new IntegerValue(foundIndex);
    } else if (name.equals("get") && numArguments == 1){
      // Fetch element at that index
      int len = value.size();
      int index = arguments.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("List index less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("List index of " + index + " is greater than list length of " + len + " (minus one)");
      else
        return (Value)value.get(index);
    } else if (name.equals("get") && numArguments == 2){
      // if first argument is a string, treat as a lookup of list of maps
      if (arguments.get(0) instanceof StringValue){
        // First argument is the map field name
        String fieldName = arguments.get(0).getStringValue();

        // Second argument is the value for that field to search for
        Value fieldValueNode = arguments.get(1);

        // Search through the list
        Value foundElementValueNode = NullValue.one;
        for (Value elementValueNode: value){
          if (elementValueNode instanceof MapValue){
            MapValue mapValueNode = (MapValue)elementValueNode;
            Value aFieldValueNode = mapValueNode.value.get(fieldName);
            if (aFieldValueNode != null && aFieldValueNode.compareValue(fieldValueNode) == 0){
              foundElementValueNode = elementValueNode;
              break;
            }
          }
        }

        // Return the element we found (or a null value node)
        return foundElementValueNode;
      } else {
        // Create new list which is a selected range from the list
        int len = value.size();
        int index = arguments.get(0).getIntValue();
        if (index < 0)
          throw new RuntimeException("List index less than zero: " + index);
        else if (index >= len)
          throw new RuntimeException("List index of " + index + " is greater than list length of " + len + " (minus one)");
        int endIndex = arguments.get(1).getIntValue();
        if (endIndex < 0)
          throw new RuntimeException("List index less than zero: " + endIndex);
        else if (endIndex > len)
          throw new RuntimeException("List index of " + endIndex + " is greater than list length of " + len + " minus 1");
        List<Value> newValue = new ArrayList<Value>();
        for (int i = index; i < endIndex; i++)
          newValue.add(value.get(i));
        return new ListValue(type, newValue);
      }
    } else if ((name.equals("add") || name.equals("put") || name.equals("set")) && numArguments == 2){
      // Replace element at that index
      int len = value.size();
      int index = arguments.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("List index less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("List index of " + index + " is greater than list length of " + len + " (minus one)");
      
      Value newValue = arguments.get(1);
      value.set(index, newValue);

      // Return the new element
      // TODO: Consider whether this should return the list
      return newValue;
    } else if (name.equals("remove") && numArguments == 1){
      // Get the index of element to remove
      int index = arguments.get(0).getIntValue();

      // Validate the index
      int numElements = value.size();
      if (index < 0)
        throw new RuntimeException("List index less than zero: " + index);
      else if (index >= numElements)
        throw new RuntimeException("List index of " + index + " is greater than list length of " + numElements + " (minus one)");

      // Remove the value
      Value removedValue = value.remove(index);

      // Return the removed value
      return removedValue;

    } else if (name.equals("sum") && numArguments == 0){
      // See if any elements in list
      int numElements = value.size();
      if (numElements == 0)
        // No, then it has no average value
        return NullValue.one;

      // Iterate over list to sum values
      Value sum = new IntegerValue(0);
      for (Value valueNode: value)
        sum = AddNode.add(sum, valueNode);

      // Return the sum
      return sum;
    } else
      // TODO: Add 'get', 'set', and 'put' operations
      return super.getMethodValue(scriptState, name, arguments);
  }

  public Value getSubscriptedValue(ScriptState scriptState, List<Value> subscriptValues) throws RuntimeException {
    int numSubscripts = subscriptValues.size();
    if (numSubscripts == 1){
      // Fetch element at that index
      int len = value.size();
      int index = subscriptValues.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("List index less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("List index of " + index + " is greater than list length of " + len + " (minus one)");
      else
        return (Value)value.get(index);
    } else if (numSubscripts == 2){
      // if first argument is a string, treat as a lookup of list of maps
      if (subscriptValues.get(0) instanceof StringValue){
        // First argument is the map field name
        String fieldName = subscriptValues.get(0).getStringValue();

        // Second argument is the value for that field to search for
        Value fieldValueNode = subscriptValues.get(1);

        // Search through the list
        Value foundElementValueNode = NullValue.one;
        for (Value elementValueNode: value){
          if (elementValueNode instanceof MapValue){
            MapValue mapValueNode = (MapValue)elementValueNode;
            Value aFieldValueNode = mapValueNode.value.get(fieldName);
            if (aFieldValueNode != null && aFieldValueNode.compareValue(fieldValueNode) == 0){
              foundElementValueNode = elementValueNode;
              break;
            }
          }
        }

        // Return the element we found (or a null value node)
        return foundElementValueNode;
      } else {
        // Create new list which is a selected range from the list
        int len = value.size();
        int index = subscriptValues.get(0).getIntValue();
        if (index < 0)
          throw new RuntimeException("List index less than zero: " + index);
        else if (index >= len)
          throw new RuntimeException("List index of " + index + " is greater than list length of " + len + " (minus one)");
        int endIndex = subscriptValues.get(1).getIntValue();
        if (endIndex < 0)
          throw new RuntimeException("List index less than zero: " + endIndex);
        else if (endIndex > len)
          throw new RuntimeException("List index of " + endIndex + " is greater than list length of " + len + " minus 1");
        List<Value> newValue = new ArrayList<Value>();
        for (int i = index; i < endIndex; i++)
          newValue.add(value.get(i));
        return new ListValue(type, newValue);
      }
    } else
      throw new RuntimeException("Lists do not support " + numSubscripts + " subscripts");
  }

  public Value putSubscriptedValue(ScriptState scriptState, List<Value> subscriptValues, Value newValue) throws RuntimeException {
    int numSubscripts = subscriptValues.size();
    if (numSubscripts == 1){
      // Replace element at that index
      int len = value.size();
      int index = subscriptValues.get(0).getIntValue();
      if (index < 0)
        throw new RuntimeException("List index less than zero: " + index);
      else if (index >= len)
        throw new RuntimeException("List index of " + index + " is greater than list length of " + len + " (minus one)");
      else {
        value.set(index, newValue);
        return newValue;
      }
    } else
      throw new RuntimeException("Lists do not support " + numSubscripts + " subscripts for assignment");
  }

  public Value clone(){
    List<Value> newList = new ArrayList<Value>();
    for (Value element: value)
      newList.add(element.clone());
    return new ListValue(type, newList);
  }

  public String toJson(){
    // Return JSON-format comma-separated list of element values within square brackets
    StringBuilder sb = new StringBuilder("[");
    for (Value valueNode: value){
      if (sb.length() > 1)
        sb.append(", ");
      sb.append(valueNode.toJson());
    }
    sb.append(']');
    return sb.toString();
  }

  public String toString(){
    // Return comma-separated list of element values within square brackets
    StringBuilder sb = new StringBuilder("[");
    for (Value valueNode: value){
      if (sb.length() > 1)
        sb.append(", ");
      sb.append(valueNode.toString());
    }
    sb.append(']');
    return sb.toString();
  }

  public String toText(){
    // Return space-delimited list of element values
    StringBuilder sb = new StringBuilder();
    for (Value valueNode: value){
      // Get the text of the next element
      String text = valueNode.toText();
      
      // Ignore empty text
      if (text == null || text.length() == 0)
        continue;
      
      // Separate text with single space
      if (sb.length() > 1)
        sb.append(" ");
      
      // Tack on the text for this element
      sb.append(text);
    }
    
    // Return the acculated text
    return sb.toString();
  }

  public String toXml(){
    return toXml(null);
  }

  public String toXml(String elementName){
    StringBuilder sb = new StringBuilder();
    for (Value valueNode: value){
      if (elementName != null)
        sb.append("<" + elementName + ">");
      else if (sb.length() > 0)
        sb.append(' ');

      // Get the XML text of the next element
      String xmlText = valueNode.toXml();
      sb.append(xmlText);

      if (elementName != null)
        sb.append("</" + elementName + ">");
    }
    
    // Return the accumulated text
    return sb.toString();
  }

  public String getTypeString(){
    return "list<" + type.toString() + ">";
  }

  public boolean equals(Value valueNode){
    // Other value must also be a list
    if (valueNode instanceof ListValue){
      // Sizes must agree
      int len1 = value.size();
      ListValue value2 = (ListValue)valueNode;
      int len2 = value2.value.size();
      if (len1 != len2)
        return false;

      // And each list element must match recursively
      for (int i = 0; i < len1; i++)
        if (! value.get(i).equals(value2.value.get(i)))
          return false;

      // Everything matches
      return true;
    } else
      // No match
      return false;
  }

  public List<String> getStrings(int startIndex, int endIndex){
    List<String> strings = new ArrayList<String>();
    int numElements = value.size();
    if (startIndex < 0)
      startIndex = 0;
    if (endIndex >= numElements)
      endIndex = numElements - 1;
    for (int i = startIndex; i <= endIndex; i++)
      strings.add(value.get(i).toString());
    
    return strings;
  }
}
