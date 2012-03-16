package com.basetechnology.s0.agentserver.script.parser.tokenizer.token;

import com.basetechnology.s0.agentserver.script.intermediate.FloatTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;


public class DoubleKeywordToken extends TypeKeywordToken {

  public String toString(){
    return "double";
  }

  public TypeNode getTypeNode(){
    return FloatTypeNode.one;
  }

}
