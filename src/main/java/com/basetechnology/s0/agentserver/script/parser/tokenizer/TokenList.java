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

import java.util.ArrayList;
import java.util.List;

import com.basetechnology.s0.agentserver.script.intermediate.GreaterNode;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.EndToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.GreaterGreaterGreaterOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.GreaterGreaterOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.GreaterOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.Token;

public class TokenList {
  protected List<Token> tokenList = new ArrayList<Token>();
  protected int nextToken = 0;
  protected EndToken endToken = new EndToken();
  
  public TokenList (){
  }

  public Token get(int index){
    if (index < tokenList.size())
      return tokenList.get(index);
    else
      return endToken;
  }
  
  public boolean atEnd(){
    return nextToken >= tokenList.size();
  }
  
  public int size(){
    return tokenList.size();
  }
  
  public Token getNext(){
    skipToken();
    return get();
  }
  
  public Token get(){
    if (nextToken < tokenList.size())
      return tokenList.get(nextToken);
    else
      return endToken;
  }
  
  public Token peek(int k){
    return get(nextToken + k);
  }
  
  public void skipToken(){
    if (nextToken < tokenList.size())
      nextToken++;
  }
  
  public void add(Token token){
    tokenList.add(token);
  }
  
  public int getPosition(){
    return nextToken;
  }
  
  public void setPosition(int position){
    nextToken = position;
  }
  
  
  public Token getRightAngleBracket(){
    // Get the next taken
    Token token = get();
    
    // Check for all tokens that could start with a right angle bracket character ('>')
    if (token instanceof GreaterGreaterOperatorToken){
      // Split off the second angle bracket as a separate token
      tokenList.add(nextToken + 1, new GreaterOperatorToken());
      
      // Return a single right angle bracket token
      token = new GreaterOperatorToken();
      tokenList.set(nextToken, token);
    } else if (token instanceof GreaterGreaterGreaterOperatorToken){
      // Split off the second and third angle brackets as a separate token
      tokenList.add(nextToken + 1, new GreaterGreaterOperatorToken());
      
      // Return a single right angle bracket token
      token = new GreaterOperatorToken();
      tokenList.set(nextToken, token);
    }
    
    return token;
  }
}
