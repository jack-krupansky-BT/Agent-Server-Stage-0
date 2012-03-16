package com.basetechnology.s0.agentserver.script.parser.tokenizer.token;

import com.basetechnology.s0.agentserver.script.intermediate.DateTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;


public class DateKeywordToken extends TypeKeywordToken {

  public String toString(){
    return "date";
  }

  public TypeNode getTypeNode(){
    return DateTypeNode.one;
  }

}
