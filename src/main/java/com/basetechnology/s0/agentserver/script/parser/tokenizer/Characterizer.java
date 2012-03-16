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

package com.basetechnology.s0.agentserver.script.parser.tokenizer;

public class Characterizer {
  public String s;
  public int len;
  public char nextChar;
  public int nextCharIndex;
  public boolean isLetter;
  public boolean isDigit;
  public boolean isIdentifier;
  public boolean isIdentifierStart;
  public boolean isWhiteSpace; 
  public boolean isEnd;
  
  public Characterizer(String s){
    setString(s);
  }
  
  public void setString(String s){
    this.s = s;
    this.len = s.length();
    this.nextCharIndex = 0;
    processThisChar();
  }

  public char processThisChar(){
    if (nextCharIndex < len){
      nextChar = s.charAt(nextCharIndex);
      isLetter = Character.isLetter(nextChar);
      isDigit = Character.isDigit(nextChar);
      isIdentifierStart = isLetter || nextChar == '_';;
      isIdentifier = isIdentifierStart  || isDigit ;
      isWhiteSpace = Character.isWhitespace(nextChar);
      isEnd = false;
    } else {
      nextChar = 0;
      isLetter = false;
      isDigit = false;
      isIdentifier = false;
      isWhiteSpace = false;
      isEnd = true;
    }
    return nextChar;
  }
  
  public char getChar(){
    return nextChar;
  }
  
  public char peekNextChar(){
    if (isEnd)
      return 0;
    else if (nextCharIndex == len - 1)
      return 0;
    else
      return s.charAt(nextCharIndex + 1);
  }
  
  public void skipChar(){
    if (nextCharIndex < len)
      nextCharIndex++;
    processThisChar();
  }
  
  public char getCharNonBlank(){
    if (isWhiteSpace)
      getNextNonBlankChar();
    return nextChar;
  }
  
  public char getNextChar(){
    if (nextCharIndex < len)
      nextCharIndex++;
    processThisChar();
    return nextChar;
  }
  
  public char getNextNonBlankChar(){
    do {
      getNextChar();
    } while (isWhiteSpace && ! isEnd);
    return nextChar;
  }
}
