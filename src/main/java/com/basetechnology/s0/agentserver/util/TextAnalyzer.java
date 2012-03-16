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
import java.util.List;

public class TextAnalyzer {

  public TextAnalyzer(){
    
  }
  
  public List<Word> analyze(String text){
    List<Word> words = new ArrayList<Word>();
    if (text == null)
      return words;

    Word previousWord = null;
    int len = text.length();
    for (int i = 0; i < len; i++){
      // Start a new word
      Word word = new Word();
      
      // TODO: What to include in raw - exclude spaces, space ends non-space punctuation
      // Strip leading punctuation and collect new word
      for (int j = i; j < len; j++){
        char ch = text.charAt(j);
        if (Character.isDigit(ch) || ch == '$'){
          // Scan a number
          String simpleWord = "";
          for (int j1 = j; j1 < len; j1++){
            ch = text.charAt(j1);
            j = j1;
            if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '\''){
              simpleWord += ch;
            } else if (ch == '.'){
              if (j1 < len - 1 && Character.isDigit(text.charAt(j1 + 1)))
                simpleWord += ch;
              j = j1;
            } else if (ch == ','){
              if (j1 < len - 1 && Character.isDigit(text.charAt(j1 + 1)))
                simpleWord += ch;
              j = j1;
            } else if (ch == ':'){
              if (j1 < len - 1 && Character.isDigit(text.charAt(j1 + 1)))
                simpleWord += ch;
              j = j1;
            } else if (ch == '-'){
              if (j1 < len - 1 && Character.isLetterOrDigit(text.charAt(j1 + 1)))
                simpleWord += ch;
              j = j1;
            } else if (Character.isDigit(ch) || ch == '$'){
              simpleWord += ch;
              j = j1;
            } else if (ch == '%'){
              simpleWord += ch;
              j = j1;
            } else {
              j = j1;
              break;
            }
          }
          if (simpleWord.length() > 0){
            word.proper = simpleWord;
            word.properLower = simpleWord.toLowerCase();
            words.add(word);
          }
          i = j;
          break;
        } else if (Character.isLetter(ch)){
          // Scan a word
          String simpleWord = "";
          for (int j1 = i; j1 < len; j1++){
            ch = text.charAt(j1);
            j = j1;
            if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '\''){
              simpleWord += ch;
            } else if (ch == '.'){
              if ((j1 < len - 1 && Character.isLetterOrDigit(text.charAt(j1 + 1))) ||
                  simpleWord.contains("."))
                simpleWord += ch;
              j = j1;
            } else if (ch == ','){
              if (j1 < len - 1 && Character.isDigit(text.charAt(j1 + 1)))
                simpleWord += ch;
              j = j1;
            } else if (ch == ':'){
              if (j1 < len - 1 && Character.isDigit(text.charAt(j1 + 1)))
                simpleWord += ch;
              j = j1;
            } else if (ch == '-'){
              if (j1 < len - 1 && Character.isLetterOrDigit(text.charAt(j1 + 1)))
                simpleWord += ch;
              j = j1;
            } else if (Character.isDigit(ch) || ch == '$'){
              simpleWord += ch;
              j = j1;
            } else if (ch == '%'){
              simpleWord += ch;
              j = j1;
            } else {
              j = j1;
              break;
            }
          }
          if (simpleWord.length() > 0){
            word.proper = simpleWord;
            word.properLower = simpleWord.toLowerCase();
            words.add(word);
          }
          i = j;
          break;
        } else{
          // Record the leading punctuation
          word.precedingPunctuation += ch;
          i = j;
        }
      }
    }
    return words;
    
  }
}
