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

package com.basetechnology.s0.agentserver.script.parser;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.basetechnology.s0.agentserver.AgentDefinition;
import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.AgentServer;
import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.script.intermediate.AddNode;
import com.basetechnology.s0.agentserver.script.intermediate.AssignmentNode;
import com.basetechnology.s0.agentserver.script.intermediate.AssignmentStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.BlockStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.BooleanTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.DateTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.DivideNode;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionNode;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionStatementListNode;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.FloatTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.ForStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.IfStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.IntegerTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.LessNode;
import com.basetechnology.s0.agentserver.script.intermediate.ListTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.LogicalAndNode;
import com.basetechnology.s0.agentserver.script.intermediate.LogicalNotNode;
import com.basetechnology.s0.agentserver.script.intermediate.LogicalOrNode;
import com.basetechnology.s0.agentserver.script.intermediate.MapTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.MoneyTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.MultiplyNode;
import com.basetechnology.s0.agentserver.script.intermediate.NegationNode;
import com.basetechnology.s0.agentserver.script.intermediate.Node;
import com.basetechnology.s0.agentserver.script.intermediate.NullStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.ObjectTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.PostDecrementNode;
import com.basetechnology.s0.agentserver.script.intermediate.PostIncrementNode;
import com.basetechnology.s0.agentserver.script.intermediate.PreDecrementNode;
import com.basetechnology.s0.agentserver.script.intermediate.PreIncrementNode;
import com.basetechnology.s0.agentserver.script.intermediate.RemainderNode;
import com.basetechnology.s0.agentserver.script.intermediate.ReturnStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.ScriptNode;
import com.basetechnology.s0.agentserver.script.intermediate.StatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.StringTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.SubtractNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.VariableReferenceNode;
import com.basetechnology.s0.agentserver.script.parser.ScriptParser;
import com.basetechnology.s0.agentserver.script.runtime.value.FalseValue;
import com.basetechnology.s0.agentserver.script.runtime.value.FloatValue;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NowValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.TrueValue;

public class ScriptParserTest {

  AgentAppServer agentAppServer = null;
  AgentServer agentServer = null;
  AgentDefinition dummyAgentDefinition;
  AgentInstance dummyAgentInstance;
  SymbolManager symbols;
  Map<String, SymbolValues> values;
  ScriptParser parser;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    if (agentAppServer != null){
      agentAppServer.stop();
      agentAppServer = null;
      agentServer = null;
    }
    File pf = new File(AgentServer.defaultPersistencePath);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServer.defaultPersistencePath, ! pf.exists());

    agentAppServer = new AgentAppServer();
    agentServer = agentAppServer.agentServer;
    dummyAgentDefinition = new AgentDefinition(agentServer);
    dummyAgentInstance = new AgentInstance(dummyAgentDefinition);
    parser = new ScriptParser(dummyAgentInstance);
    symbols = dummyAgentInstance.symbolManager;
  }

  @After
  public void tearDown() throws Exception {
    if (agentServer != null){
      agentServer.stop();
    }
    File pf = new File(AgentServer.defaultPersistencePath);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServer.defaultPersistencePath, ! pf.exists());
    agentServer = null;
  }
  
  @Test
  public void test() throws Exception {
    // Test expression parse of null
    ExpressionNode tree = parser.parseExpressionString(null);
    assertTrue("Unexpected expression tree returned", tree == null);
    
    // Test expression parse of empty string
    tree = parser.parseExpressionString("");
    assertTrue("Unexpected expression tree returned", tree == null);
    
    // Test expression parse of string with only white space
    tree = parser.parseExpressionString("  \t  \n  \n\n  ");
    assertTrue("Unexpected expression tree returned", tree == null);
    
    // Test expression parse of string with only white space and comments
    tree = parser.parseExpressionString("  \t  \n  // comment \n\n  /* comment \n more comment\n*/ ");
    assertTrue("Unexpected expression tree returned", tree == null);

    // Test trivial expressions that are only a single value
    tree = parser.parseExpressionString("0");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a IntegerValueNode: " + tree.getClass().getSimpleName(), tree instanceof IntegerValue);
    assertEquals("Integer value", 0, ((IntegerValue)tree).value);
    tree = parser.parseExpressionString("123");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a IntegerValueNode: " + tree.getClass().getSimpleName(), tree instanceof IntegerValue);
    assertEquals("Integer value", 123, ((IntegerValue)tree).value);
    tree = parser.parseExpressionString("0.0");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a FloatValueNode: " + tree.getClass().getSimpleName(), tree instanceof FloatValue);
    assertEquals("Float value", 0.0, ((FloatValue)tree).value, 0.001);
    tree = parser.parseExpressionString("123.456");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a FloatValueNode: " + tree.getClass().getSimpleName(), tree instanceof FloatValue);
    assertEquals("Float value", 123.456, ((FloatValue)tree).value, 0.001);
    tree = parser.parseExpressionString("null");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a NullValueNode: " + tree.getClass().getSimpleName(), tree instanceof NullValue);
    tree = parser.parseExpressionString("false");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a FalseValueNode: " + tree.getClass().getSimpleName(), tree instanceof FalseValue);
    tree = parser.parseExpressionString("true");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a TrueValueNode: " + tree.getClass().getSimpleName(), tree instanceof TrueValue);
    tree = parser.parseExpressionString("now");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a NowValueNode: " + tree.getClass().getSimpleName(), tree instanceof NowValue);
    tree = parser.parseExpressionString(" \"Hello World\" ");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a StringValueNode: " + tree.getClass().getSimpleName(), tree instanceof StringValue);
    assertEquals("String value", "Hello World", ((StringValue)tree).value);
    tree = parser.parseExpressionString(" 'Hello World' ");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a StringValueNode: " + tree.getClass().getSimpleName(), tree instanceof StringValue);
    assertEquals("String value", "Hello World", ((StringValue)tree).value);
    
    // Setup a dummy symbol table
    SymbolManager symbols = new SymbolManager();
    parser.symbolManager = symbols;

    // Test trivial expressions that are only a single identifier
    symbols.put("alpha");
    tree = parser.parseExpressionString("alpha");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a VariableReferenceNode: " + tree.getClass().getSimpleName(), tree instanceof VariableReferenceNode);
    assertEquals("Identifier name", "alpha", ((VariableReferenceNode)tree).symbol.name);
    symbols.put("_alpha_");
    tree = parser.parseExpressionString("_alpha_");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a VariableReferenceNode: " + tree.getClass().getSimpleName(), tree instanceof VariableReferenceNode);
    assertEquals("Identifier name", "_alpha_", ((VariableReferenceNode)tree).symbol.name);
    symbols.put("_");
    tree = parser.parseExpressionString("_");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a VariableReferenceNode: " + tree.getClass().getSimpleName(), tree instanceof VariableReferenceNode);
    assertEquals("Identifier name", "_", ((VariableReferenceNode)tree).symbol.name);
    symbols.put("__");
    tree = parser.parseExpressionString("__");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a VariableReferenceNode: " + tree.getClass().getSimpleName(), tree instanceof VariableReferenceNode);
    assertEquals("Identifier name", "__", ((VariableReferenceNode)tree).symbol.name);
    symbols.put("_123_");
    tree = parser.parseExpressionString("_123_");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a VariableReferenceNode: " + tree.getClass().getSimpleName(), tree instanceof VariableReferenceNode);
    assertEquals("Identifier name", "_123_", ((VariableReferenceNode)tree).symbol.name);

    // Test simple operations
    symbols.put("a");
    tree = parser.parseExpressionString("- a");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a NegationNode: " + tree.getClass().getSimpleName(), tree instanceof NegationNode);
    Node node = ((NegationNode)tree).node;
    assertTrue("Left node is not an VariableReferenceNode: " + node.getClass().getSimpleName(), node instanceof VariableReferenceNode);
    assertEquals("Variable name", "a", ((VariableReferenceNode)node).symbol.name);

    tree = parser.parseExpressionString("a + 2");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a AddNode: " + tree.getClass().getSimpleName(), tree instanceof AddNode);
    Node leftNode = ((AddNode)tree).leftNode;
    assertTrue("Left node is not an VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "a", ((VariableReferenceNode)leftNode).symbol.name);
    Node rightNode = ((AddNode)tree).rightNode;
    assertTrue("Right node is not an IntegerValueNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof IntegerValue);
    assertEquals("Integer value", 2, ((IntegerValue)rightNode).value);

    symbols.put("ace");
    tree = parser.parseExpressionString("ace - 0.1");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a SubtractNode: " + tree.getClass().getSimpleName(), tree instanceof SubtractNode);
    leftNode = ((SubtractNode)tree).leftNode;
    assertTrue("Left node is not an VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "ace", ((VariableReferenceNode)leftNode).symbol.name);
    rightNode = ((SubtractNode)tree).rightNode;
    assertTrue("Right node is not an FloatValueNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof FloatValue);
    assertEquals("Float value", 0.1, ((FloatValue)rightNode).value, 0.001);

    symbols.put("beta");
    tree = parser.parseExpressionString("beta * 32");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a MultiplyNode: " + tree.getClass().getSimpleName(), tree instanceof MultiplyNode);
    leftNode = ((MultiplyNode)tree).leftNode;
    assertTrue("Left node is not an VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "beta", ((VariableReferenceNode)leftNode).symbol.name);
    rightNode = ((MultiplyNode)tree).rightNode;
    assertTrue("Right node is not an IntegerValueNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof IntegerValue);
    assertEquals("Integer value", 32, ((IntegerValue)rightNode).value);

    symbols.put("gamma2_3");
    tree = parser.parseExpressionString("gamma2_3 / 67");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a DivideNode: " + tree.getClass().getSimpleName(), tree instanceof DivideNode);
    leftNode = ((DivideNode)tree).leftNode;
    assertTrue("Left node is not an VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "gamma2_3", ((VariableReferenceNode)leftNode).symbol.name);
    rightNode = ((DivideNode)tree).rightNode;
    assertTrue("Right node is not an IntegerValueNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof IntegerValue);
    assertEquals("Integer value", 67, ((IntegerValue)rightNode).value);

    symbols.put("fox");
    tree = parser.parseExpressionString("fox % 10");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a RemainderNode: " + tree.getClass().getSimpleName(), tree instanceof RemainderNode);
    leftNode = ((RemainderNode)tree).leftNode;
    assertTrue("Left node is not an VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "fox", ((VariableReferenceNode)leftNode).symbol.name);
    rightNode = ((RemainderNode)tree).rightNode;
    assertTrue("Right node is not an IntegerValueNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof IntegerValue);
    assertEquals("Integer value", 10, ((IntegerValue)rightNode).value);

    symbols.put("abc");
    symbols.put("def");
    tree = parser.parseExpressionString("abc && def");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a LogicalAndNode: " + tree.getClass().getSimpleName(), tree instanceof LogicalAndNode);
    leftNode = ((LogicalAndNode)tree).leftNode;
    assertTrue("Left node is not an VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "abc", ((VariableReferenceNode)leftNode).symbol.name);
    rightNode = ((LogicalAndNode)tree).rightNode;
    assertTrue("Right node is not an VariableReferenceNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "def", ((VariableReferenceNode)rightNode).symbol.name);

    tree = parser.parseExpressionString("abc || def");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a LogicalOrNode: " + tree.getClass().getSimpleName(), tree instanceof LogicalOrNode);
    leftNode = ((LogicalOrNode)tree).leftNode;
    assertTrue("Left node is not an VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "abc", ((VariableReferenceNode)leftNode).symbol.name);
    rightNode = ((LogicalOrNode)tree).rightNode;
    assertTrue("Right node is not an VariableReferenceNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "def", ((VariableReferenceNode)rightNode).symbol.name);

    tree = parser.parseExpressionString("! abc");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a LogicalNotNode: " + tree.getClass().getSimpleName(), tree instanceof LogicalNotNode);
    node = ((LogicalNotNode)tree).node;
    assertTrue("Left node is not an VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    assertEquals("Variable name", "abc", ((VariableReferenceNode)leftNode).symbol.name);

    // Test pre-increment
    symbols.put("i");
    tree = parser.parseExpressionString("++i");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a PreIncrementNode: " + tree.toString(), tree instanceof PreIncrementNode);
    PreIncrementNode preIncrementNode = (PreIncrementNode)tree;
    node = preIncrementNode.node;
    assertTrue("PreIncrement node is missing", node != null);
    assertTrue("PreIncrement node is not a VariableReferenceNode: " + tree.toString(), node instanceof VariableReferenceNode);
    VariableReferenceNode varRef = (VariableReferenceNode)node;
    Symbol symbol = varRef.symbol;
    assertTrue("Variable symbol is null", symbol != null);
    String variableName = symbol.name;
    assertEquals("Variable name", "i", variableName);

    tree = parser.parseExpressionString("--i");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a PreIncrementNode: " + tree.toString(), tree instanceof PreDecrementNode);
    PreDecrementNode preDecrementNode = (PreDecrementNode)tree;
    node = preDecrementNode.node;
    assertTrue("PreDecrement node is missing", node != null);
    assertTrue("PreDecrement node is not a VariableReferenceNode: " + tree.toString(), node instanceof VariableReferenceNode);
    varRef = (VariableReferenceNode)node;
    symbol = varRef.symbol;
    assertTrue("Variable symbol is null", symbol != null);
    variableName = symbol.name;
    assertEquals("Variable name", "i", variableName);

    tree = parser.parseExpressionString("i++");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a PreIncrementNode: " + tree.toString(), tree instanceof PostIncrementNode);
    PostIncrementNode postIncrementNode = (PostIncrementNode)tree;
    node = postIncrementNode.node;
    assertTrue("PostIncrement node is missing", node != null);
    assertTrue("PostIncrement node is not a VariableReferenceNode: " + tree.toString(), node instanceof VariableReferenceNode);
    varRef = (VariableReferenceNode)node;
    symbol = varRef.symbol;
    assertTrue("Variable symbol is null", symbol != null);
    variableName = symbol.name;
    assertEquals("Variable name", "i", variableName);

    tree = parser.parseExpressionString("i--");
    assertTrue("No expression tree returned", tree != null);
    assertTrue("Tree is not a PostIncrementNode: " + tree.toString(), tree instanceof PostDecrementNode);
    PostDecrementNode postDecrementNode = (PostDecrementNode)tree;
    node = postDecrementNode.node;
    assertTrue("PreDecrement node is missing", node != null);
    assertTrue("PreDecrement node is not a VariableReferenceNode: " + tree.toString(), node instanceof VariableReferenceNode);
    varRef = (VariableReferenceNode)node;
    symbol = varRef.symbol;
    assertTrue("Variable symbol is null", symbol != null);
    variableName = symbol.name;
    assertEquals("Variable name", "i", variableName);

    // TODO: rel ops, log ops, log op plus rel op, 3-ops, complex ops
    // (e) a++ a-- ++a --a

    // Test parse of empty script
    node = parser.parseScriptString(null);
    assertTrue("Unexpected node for null script", node == null);
    
    // Test expression parse of empty string
    node = parser.parseScriptString("");
    assertTrue("Unexpected script node returned", node == null);
    
    // Test expression parse of string with only white space
    node = parser.parseScriptString("  \t  \n  \n\n  ");
    assertTrue("Unexpected script node returned", node == null);
    
    // Test expression parse of string with only white space and comments
    node = parser.parseScriptString("  \t  \n  // comment \n\n  /* comment \n more comment\n*/ ");
    assertTrue("Unexpected script node returned", node == null);
    
    // Test trivial expressions that are only a single value
    ScriptNode scriptNode = parser.parseScriptString("0;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    List<StatementNode> statementSequence = blockNode.statementSequence;
    assertEquals("Statement count", 1, statementSequence.size());
    StatementNode statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not a ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    ExpressionStatementNode expressionStatementNode = (ExpressionStatementNode)statementNode;
    node = expressionStatementNode.expressionNode;
    assertTrue("Expression node is not a IntegerValueNode: " + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Integer value", 0, ((IntegerValue)node).value);

    // Test comma list expression statement
    scriptNode = parser.parseScriptString("123, 2.0, false, 'Hello';");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    assertEquals("Statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementListNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementListNode);
    ExpressionStatementListNode expressionStatementListNode = (ExpressionStatementListNode)statementNode;
    statementSequence = expressionStatementListNode.expressionStatements;
    assertEquals("Expression statement list statement count", 4, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Expression statement list statement[0] not a ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    node = expressionStatementNode.expressionNode;
    assertTrue("Expression node is not a IntegerValueNode: " + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Integer value", 123, ((IntegerValue)node).value);
    statementNode = statementSequence.get(1);
    assertTrue("Expression statement list statement[1] not a ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    node = expressionStatementNode.expressionNode;
    assertTrue("Expression node is not a FloatValueNode: " + node.getClass().getSimpleName(), node instanceof FloatValue);
    assertEquals("Integer value", 2.0, ((FloatValue)node).value, 0.001);
    statementNode = statementSequence.get(2);
    assertTrue("Expression statement list statement[2] not a ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    node = expressionStatementNode.expressionNode;
    assertTrue("Expression node is not a FalseValueNode: " + node.getClass().getSimpleName(), node instanceof FalseValue);
    statementNode = statementSequence.get(3);
    assertTrue("Expression statement list statement[3] not a ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    node = expressionStatementNode.expressionNode;
    assertTrue("Expression node is not a StringValueNode: " + node.getClass().getSimpleName(), node instanceof StringValue);
    assertEquals("Integer value", "Hello", ((StringValue)node).value);

    scriptNode = parser.parseScriptString("123, int i = 456, float f = 2.0;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    assertEquals("Statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementListNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementListNode);
    expressionStatementListNode = (ExpressionStatementListNode)statementNode;
    statementSequence = expressionStatementListNode.expressionStatements;
    assertEquals("Expression statement list statement count", 3, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Expression statement list statement[0] not a ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    node = expressionStatementNode.expressionNode;
    assertTrue("Expression node is not a IntegerValueNode: " + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Integer value", 123, ((IntegerValue)node).value);
    statementNode = statementSequence.get(1);
    assertTrue("Expression statement list statement[0] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    AssignmentStatementNode assignmentStatementNode = (AssignmentStatementNode)statementNode;
    VariableReferenceNode varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    String varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 456, ((IntegerValue)node).value);
    statementNode = statementSequence.get(2);
    assertTrue("Expression statement list statement[1] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "f", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof FloatValue);
    assertEquals("Assignment float value", 2.0, ((FloatValue)node).value, 0.001);

    // Test 'return' statement without expression
    scriptNode = parser.parseScriptString("return;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not a ReturnStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ReturnStatementNode);
    assertTrue("Unexpected return expr node", ((ReturnStatementNode)statementNode).returnExpr == null);
    
    // Test 'return' statement with expression
    scriptNode = parser.parseScriptString("return 123;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not a ReturnStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ReturnStatementNode);
    ExpressionNode returnExpressionNode = ((ReturnStatementNode)statementNode).returnExpr;
    assertTrue("Missing return expr node", returnExpressionNode != null);
    assertTrue("Return expression is not a IntegerValueNode: " + returnExpressionNode.getClass().getSimpleName(), returnExpressionNode instanceof IntegerValue);
    assertEquals("Return expression value", 123, ((IntegerValue)returnExpressionNode).value);

    // Test 'if' with 'return'
    scriptNode = parser.parseScriptString("if(abc) return 456;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an IfStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof IfStatementNode);
    IfStatementNode ifStatementNode = (IfStatementNode)statementNode;
    StatementNode thenStatement = ifStatementNode.thenStatement;
    assertTrue("'if' statement does not have a 'then' statement", thenStatement != null);
    assertTrue("'then' statement is not a ReturnStatementNode: " + thenStatement.getClass().getSimpleName(), thenStatement instanceof ReturnStatementNode);
    returnExpressionNode = ((ReturnStatementNode)thenStatement).returnExpr;
    assertTrue("Missing return expr node", returnExpressionNode != null);
    assertTrue("Return expression is not a IntegerValueNode: " + returnExpressionNode.getClass().getSimpleName(), returnExpressionNode instanceof IntegerValue);
    assertEquals("Return expression value", 456, ((IntegerValue)returnExpressionNode).value);
    assertTrue("'if' statement has unexpected 'else' statement", ifStatementNode.elseStatement == null);

    // Test 'if' with relational and logical operators
    symbols.put("ab");
    symbols.put("cd");
    symbols.put("ef");
    symbols.put("gh");
    symbols.put("p");
    symbols.put("q");
    symbols.put("rr");
    scriptNode = parser.parseScriptString("if(ab == cd || ef <= gh && p != q && ! rr) return 789; else return 'ab cd';");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an IfStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof IfStatementNode);
    ifStatementNode = (IfStatementNode)statementNode;
    thenStatement = ifStatementNode.thenStatement;
    assertTrue("'if' statement does not have a 'then' statement", thenStatement != null);
    assertTrue("'then' statement is not a ReturnStatementNode: " + thenStatement.getClass().getSimpleName(), thenStatement instanceof ReturnStatementNode);
    returnExpressionNode = ((ReturnStatementNode)thenStatement).returnExpr;
    assertTrue("Missing return expr node", returnExpressionNode != null);
    assertTrue("Return expression is not a IntegerValueNode: " + returnExpressionNode.getClass().getSimpleName(), returnExpressionNode instanceof IntegerValue);
    assertEquals("Return expression value", 789, ((IntegerValue)returnExpressionNode).value);
    StatementNode elseStatement = ifStatementNode.elseStatement;
    assertTrue("'if' statement does not have an 'else' statement", elseStatement != null);

    // Test simple assignment statements
    symbols.put("i");
    scriptNode = parser.parseScriptString("i = 125;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    ExpressionNode expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[0] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    AssignmentNode assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 125, ((IntegerValue)node).value);

    symbols.put("i2");
    scriptNode = parser.parseScriptString("i2 = i * 23;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 0, localVariables.size());
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[0] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i2", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not a MultiplyNode:" + node.getClass().getSimpleName(), node instanceof MultiplyNode);
    MultiplyNode multiplyNode = (MultiplyNode)node;
    leftNode = multiplyNode.leftNode;
    assertTrue("Multiply left node is missing", leftNode != null);
    assertTrue("Multiply left node is not an VariableReferenceNode:" + node.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)leftNode;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    rightNode = multiplyNode.rightNode;
    assertTrue("Multiply right node is missing", rightNode != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + rightNode.getClass().getSimpleName(), rightNode instanceof IntegerValue);
    assertEquals("Assignment integer value", 23, ((IntegerValue)rightNode).value);

    // Test simple variable declaration for script with no explicit blocks
    symbols.clear();
    scriptNode = parser.parseScriptString("int i; i = 123;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[0] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 123, ((IntegerValue)node).value);

    // Test simple variable declaration for script with an explicit block
    symbols.clear();
    scriptNode = parser.parseScriptString("{int i; i = 123;}");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 0, localVariables.size());
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not a BlockStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof BlockStatementNode);
    blockNode = ((BlockStatementNode)statementNode);
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol varSymbol = localVariables.get(0);
    assertEquals("Local Variable name", "i", varSymbol.name);
    assertTrue("Local Variable 'i' is not an IntTypeNode: " + varSymbol.name, varSymbol.type instanceof IntegerTypeNode);
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[0] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 123, ((IntegerValue)node).value);

    // Test multiple variable declaration for script with an explicit block
    symbols.clear();
    scriptNode = parser.parseScriptString("{int i, j; i = 123; j = 456; float f; f = 1.23;}");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 0, localVariables.size());
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not a BlockStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof BlockStatementNode);
    blockNode = ((BlockStatementNode)statementNode);
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 3, localVariables.size());
    varSymbol = localVariables.get(0);
    assertEquals("Local Variable name", "i", varSymbol.name);
    assertTrue("Local Variable 'i' is not an IntTypeNode: " + varSymbol.name, varSymbol.type instanceof IntegerTypeNode);
    varSymbol = localVariables.get(1);
    assertEquals("Local Variable name", "j", varSymbol.name);
    assertTrue("Local Variable '' is not an IntTypeNode: " + varSymbol.name, varSymbol.type instanceof IntegerTypeNode);
    varSymbol = localVariables.get(2);
    assertEquals("Local Variable name", "f", varSymbol.name);
    assertTrue("Local Variable 'f' is not a FloatTypeNode: " + varSymbol.name, varSymbol.type instanceof FloatTypeNode);
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 3, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[0] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 123, ((IntegerValue)node).value);
    statementNode = statementSequence.get(1);
    assertTrue("Block node statement[1] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[1] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "j", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 456, ((IntegerValue)node).value);
    statementNode = statementSequence.get(2);
    assertTrue("Block node statement[2] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[2] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "f", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not a FloatValueNode:" + node.getClass().getSimpleName(), node instanceof FloatValue);
    assertEquals("Assignment integer value", 1.23, ((FloatValue)node).value, 0.001);

    // Test simple variable declaration with initializer for script with no explicit blocks
    symbols.clear();
    scriptNode = parser.parseScriptString("int i = 123;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 123, ((IntegerValue)node).value);

    // Test multiple variable declaration with initializers for script with an explicit block
    symbols.clear();
    scriptNode = parser.parseScriptString("{int i = 123, j = 456; float f = 1.23;}");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 0, localVariables.size());
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not a BlockStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof BlockStatementNode);
    blockNode = ((BlockStatementNode)statementNode);
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 3, localVariables.size());
    varSymbol = localVariables.get(0);
    assertEquals("Local Variable name", "i", varSymbol.name);
    assertTrue("Local Variable 'i' is not an IntTypeNode: " + varSymbol.name, varSymbol.type instanceof IntegerTypeNode);
    varSymbol = localVariables.get(1);
    assertEquals("Local Variable name", "j", varSymbol.name);
    assertTrue("Local Variable '' is not an IntTypeNode: " + varSymbol.name, varSymbol.type instanceof IntegerTypeNode);
    varSymbol = localVariables.get(2);
    assertEquals("Local Variable name", "f", varSymbol.name);
    assertTrue("Local Variable 'f' is not a FloatTypeNode: " + varSymbol.name, varSymbol.type instanceof FloatTypeNode);
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 3, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 123, ((IntegerValue)node).value);
    statementNode = statementSequence.get(1);
    assertTrue("Block node statement[1] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "j", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 456, ((IntegerValue)node).value);
    statementNode = statementSequence.get(2);
    assertTrue("Block node statement[2] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "f", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not a FloatValueNode:" + node.getClass().getSimpleName(), node instanceof FloatValue);
    assertEquals("Assignment integer value", 1.23, ((FloatValue)node).value, 0.001);

    // Test changing to a new type in a multi-variable declaration
    symbols.clear();
    scriptNode = parser.parseScriptString("{int i = 123, j = 456, float f = 1.23, g = 4.56;}");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 0, localVariables.size());
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 1, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not a BlockStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof BlockStatementNode);
    blockNode = ((BlockStatementNode)statementNode);
    localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 4, localVariables.size());
    varSymbol = localVariables.get(0);
    assertEquals("Local Variable name", "i", varSymbol.name);
    assertTrue("Local Variable 'i' is not an IntTypeNode: " + varSymbol.name, varSymbol.type instanceof IntegerTypeNode);
    varSymbol = localVariables.get(1);
    assertEquals("Local Variable name", "j", varSymbol.name);
    assertTrue("Local Variable '' is not an IntTypeNode: " + varSymbol.name, varSymbol.type instanceof IntegerTypeNode);
    varSymbol = localVariables.get(2);
    assertEquals("Local Variable name", "f", varSymbol.name);
    assertTrue("Local Variable 'f' is not a FloatTypeNode: " + varSymbol.name, varSymbol.type instanceof FloatTypeNode);
    statementSequence = blockNode.statementSequence;
    assertTrue("Block node does not have a statement sequence", statementSequence != null);
    assertEquals("Block node statement count", 4, statementSequence.size());
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 123, ((IntegerValue)node).value);
    statementNode = statementSequence.get(1);
    assertTrue("Block node statement[1] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "j", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 456, ((IntegerValue)node).value);
    statementNode = statementSequence.get(2);
    assertTrue("Block node statement[2] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "f", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not a FloatValueNode:" + node.getClass().getSimpleName(), node instanceof FloatValue);
    assertEquals("Assignment integer value", 1.23, ((FloatValue)node).value, 0.001);
    statementNode = statementSequence.get(3);
    assertTrue("Block node statement[2] not an AssignmentStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "g", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not a FloatValueNode:" + node.getClass().getSimpleName(), node instanceof FloatValue);
    assertEquals("Assignment integer value", 4.56, ((FloatValue)node).value, 0.001);

    // Test assignment to a category-specific variable
    symbols.put("i");
    symbols.put("mycat", "i");
    symbols.put("othercat", "i");
    scriptNode = parser.parseScriptString("mycat.i = 125;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[0] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    assertTrue("Variable reference node symbol is missing", varRefNode.symbol != null);
    symbol = varRefNode.symbol;
    assertEquals("Variable reference node symbol category", "mycat", symbol.symbolTable.categoryName);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 125, ((IntegerValue)node).value);

    scriptNode = parser.parseScriptString("othercat.i = 125;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[0] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    assertTrue("Variable reference node symbol is missing", varRefNode.symbol != null);
    symbol = varRefNode.symbol;
    assertEquals("Variable reference node symbol category", "othercat", symbol.symbolTable.categoryName);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 125, ((IntegerValue)node).value);

    // Test reference to category specific variable names

    scriptNode = parser.parseScriptString("int i; othercat.i = mycat.i + i * 125;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[0] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    assertTrue("Variable reference node symbol is missing", varRefNode.symbol != null);
    symbol = varRefNode.symbol;
    assertEquals("Variable reference node symbol category", "othercat", symbol.symbolTable.categoryName);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an AddNode:" + node.getClass().getSimpleName(), node instanceof AddNode);
    AddNode addNode = (AddNode)node;
    assertTrue("Add node left node is missing", addNode.leftNode != null);
    assertTrue("Add node right node is missing", addNode.rightNode != null);
    leftNode = addNode.leftNode;
    rightNode = addNode.rightNode;
    assertTrue("Left node of add node is not VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)leftNode;
    assertTrue("Variable reference node symbol is missing", varRefNode.symbol != null);
    symbol = varRefNode.symbol;
    assertEquals("Variable reference node symbol category", "mycat", symbol.symbolTable.categoryName);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    assertTrue("Right node of add node is not MultiplyNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof MultiplyNode);
    multiplyNode = (MultiplyNode)rightNode;
    assertTrue("Multiply node left node is missing", multiplyNode.leftNode != null);
    assertTrue("Multiply node right node is missing", multiplyNode.rightNode != null);
    leftNode = multiplyNode.leftNode;
    rightNode = multiplyNode.rightNode;
    assertTrue("Left node of multiply node is not VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)leftNode;
    assertTrue("Variable reference node symbol is missing", varRefNode.symbol != null);
    symbol = varRefNode.symbol;
    String categoryName = symbol.symbolTable.categoryName;
    assertTrue("Variable reference node symbol category is not a block: " + categoryName, categoryName.startsWith("block-"));
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    assertTrue("Multiply right node is not an IntegerValueNode:" + rightNode.getClass().getSimpleName(), rightNode instanceof IntegerValue);
    assertEquals("Assignment integer value", 125, ((IntegerValue)rightNode).value);

    // Test basic 'for' statement
    scriptNode = parser.parseScriptString("for (int i = 0; i < 10; i++) ;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 1, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not a ForStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ForStatementNode);
    ForStatementNode forStatementNode = (ForStatementNode)statementNode;
    StatementNode initialStatement = forStatementNode.initialExpression;
    ExpressionNode conditionNode = forStatementNode.conditionExpression;
    ExpressionNode incrementExpressionNode = forStatementNode.incrementExpression;
    StatementNode bodyStatement = forStatementNode.bodyStatement;
    assertTrue("'for' statement does not have an initial expression", initialStatement != null);
    assertTrue("Initial expression is not an AssignmentStatementNode: " + initialStatement.toString(), initialStatement instanceof AssignmentStatementNode);
    assignmentStatementNode = (AssignmentStatementNode)initialStatement;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 0, ((IntegerValue)node).value);
    assertTrue("'for' statement does not have a condition expression", conditionNode != null);
    assertTrue("Condition node is not a LessNode: " + conditionNode.getClass().getSimpleName(), conditionNode instanceof LessNode);
    LessNode lessNode = (LessNode)conditionNode;
    leftNode = lessNode.leftNode;
    rightNode = lessNode.rightNode;
    assertTrue("Less node leftNode is null", leftNode != null);
    assertTrue("Left node is not VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)leftNode;
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    assertTrue("Less node rightNode is missing", rightNode != null);
    assertTrue("Right node is not an IntegerValueNode:" + rightNode.getClass().getSimpleName(), rightNode instanceof IntegerValue);
    assertEquals("Assignment integer value", 10, ((IntegerValue)rightNode).value);
    assertTrue("'for' statement does not have an increment expression", incrementExpressionNode != null);
    assertTrue("Increment expression is not a : " + incrementExpressionNode.getClass().getSimpleName(), incrementExpressionNode instanceof PostIncrementNode);
    postIncrementNode = (PostIncrementNode)incrementExpressionNode;
    node = postIncrementNode.node;
    assertTrue("Post increment node is not a : VariableReferenceNode" + node.getClass().getSimpleName(), node instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)node;
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "i", varName);
    assertTrue("'for' body statement is null", bodyStatement != null);
    assertTrue("'for' body statement is not a NullStatement: " + bodyStatement.getClass().getSimpleName(), bodyStatement instanceof NullStatementNode);
    
    scriptNode = parser.parseScriptString("float sum; for (sum = 0.0, int j = 0, float f = 0.11; j < 15; j++) sum += f * 0.11;  return sum;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    blockNode = scriptNode.blockNode;
    assertTrue("Block node does not have a statement sequence", blockNode.statementSequence != null);
    assertEquals("Block node statement count", 2, blockNode.statementSequence.size());
    statementSequence = blockNode.statementSequence;
    statementNode = statementSequence.get(0);
    assertTrue("Block node statement[0] not a ForStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ForStatementNode);
    forStatementNode = (ForStatementNode)statementNode;
    initialStatement = forStatementNode.initialExpression;
    conditionNode = forStatementNode.conditionExpression;
    bodyStatement = forStatementNode.bodyStatement;
    incrementExpressionNode = forStatementNode.incrementExpression;
    assertTrue("'for' statement does not have an initial expression", initialStatement != null);
    assertTrue("Initial expression is not an ExpressionStatementListNode: " + initialStatement.toString(), initialStatement instanceof ExpressionStatementListNode);
    ExpressionStatementListNode initialListNode = (ExpressionStatementListNode)initialStatement;
    List<StatementNode> statementList = initialListNode.expressionStatements;
    assertEquals("Number of initial statements", 3, statementList.size());
    statementNode = statementList.get(0);
    assertTrue("Block node statement[0] not an ExpressionStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)statementNode;
    expressionNode = expressionStatementNode.expressionNode;
    assertTrue("Expression statement[0] not an AssignmentNode: " + statementNode.getClass().getSimpleName(), expressionNode instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)expressionNode;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "sum", varName);
    node = assignmentNode.node;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not a FloatValueNode:" + node.getClass().getSimpleName(), node instanceof FloatValue);
    assertEquals("Assignment float value", 0.0, ((FloatValue)node).value, 0.001);
    statementNode = statementList.get(1);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "j", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not an IntegerValueNode:" + node.getClass().getSimpleName(), node instanceof IntegerValue);
    assertEquals("Assignment integer value", 0, ((IntegerValue)node).value);
    statementNode = statementList.get(2);
    assignmentStatementNode = (AssignmentStatementNode)statementNode;
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "f", varName);
    node = assignmentStatementNode.value;
    assertTrue("Assignment value is missing", node != null);
    assertTrue("Assignment value is not a FloatValueNode:" + node.getClass().getSimpleName(), node instanceof FloatValue);
    assertEquals("Assignment float value", 0.11, ((FloatValue)node).value, 0.001);
    assertTrue("'for' statement does not have a condition expression", conditionNode != null);
    assertTrue("Condition node is not a LessNode: " + conditionNode.getClass().getSimpleName(), conditionNode instanceof LessNode);
    lessNode = (LessNode)conditionNode;
    leftNode = lessNode.leftNode;
    rightNode = lessNode.rightNode;
    assertTrue("Less node leftNode is null", leftNode != null);
    assertTrue("Left node is not VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)leftNode;
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "j", varName);
    assertTrue("Less node rightNode is missing", rightNode != null);
    assertTrue("Right node is not an IntegerValueNode:" + rightNode.getClass().getSimpleName(), rightNode instanceof IntegerValue);
    assertEquals("Assignment integer value", 15, ((IntegerValue)rightNode).value);
    assertTrue("'for' statement does not have an increment expression", incrementExpressionNode != null);
    assertTrue("Increment expression is not a : " + incrementExpressionNode.getClass().getSimpleName(), incrementExpressionNode instanceof PostIncrementNode);
    postIncrementNode = (PostIncrementNode)incrementExpressionNode;
    node = postIncrementNode.node;
    assertTrue("Post increment node is not a : VariableReferenceNode" + node.getClass().getSimpleName(), node instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)node;
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "j", varName);
    assertTrue("'for' body statement is null", bodyStatement != null);
    assertTrue("'for' body statement is not a ExpressionStatementNode: " + bodyStatement.getClass().getSimpleName(), bodyStatement instanceof ExpressionStatementNode);
    expressionStatementNode = (ExpressionStatementNode)bodyStatement;
    node = expressionStatementNode.expressionNode;
    assertTrue("Expression node is not a AssignmentNode: " + node.getClass().getSimpleName(), node instanceof AssignmentNode);
    assignmentNode = (AssignmentNode)node;
    varRefNode = (VariableReferenceNode)assignmentNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "sum", varName);
    node = assignmentNode.node;
    assertTrue("Expression node is not a AddNode: " + node.getClass().getSimpleName(), node instanceof AddNode);
    addNode = (AddNode)node;
    leftNode = addNode.leftNode;
    rightNode = addNode.rightNode;
    assertTrue("Left node of add is null", leftNode != null);
    assertTrue("Left node of add is not a VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "f", varName);
    assertTrue("Right node of add is null", rightNode != null);
    assertTrue("Right node of add is not a MultiplyNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof MultiplyNode);
    multiplyNode = (MultiplyNode)rightNode;
    leftNode = multiplyNode.leftNode;
    rightNode = multiplyNode.rightNode;
    assertTrue("Left node of multiply is null", leftNode != null);
    assertTrue("Left node of multiply is not a VariableReferenceNode: " + leftNode.getClass().getSimpleName(), leftNode instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)assignmentStatementNode.variable;
    assertTrue("Variable reference node is missing", varRefNode != null);
    varName = varRefNode.symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "f", varName);
    assertTrue("Right node of multiply is null", rightNode != null);
    assertTrue("Right node of multiply is not a FloatValueNode: " + rightNode.getClass().getSimpleName(), rightNode instanceof FloatValue);
    FloatValue floatValueNode = (FloatValue)rightNode;
    assertEquals("Float value", 0.11, floatValueNode.getDoubleValue(), 0.001);
    statementNode = blockNode.statementSequence.get(1);
    assertTrue("Block node statement[1] not a ReturnStatementNode: " + statementNode.getClass().getSimpleName(), statementNode instanceof ReturnStatementNode);
    ReturnStatementNode returnStatement = (ReturnStatementNode)statementNode;
    returnExpressionNode = returnStatement.returnExpr;
    assertTrue("Missing return expr node", returnExpressionNode != null);
    assertTrue("Return expression is not a VariableReferenceNode: " + returnExpressionNode.getClass().getSimpleName(), returnExpressionNode instanceof VariableReferenceNode);
    varRefNode = (VariableReferenceNode)returnExpressionNode;
    assertTrue("Variable reference node is missing", varRefNode != null);
    symbol = varRefNode.symbol;
    varName = symbol.name;
    assertTrue("Variable name is missing", varName != null);
    assertEquals("Variable name", "sum", varName);
    categoryName = symbol.symbolTable.categoryName;
    assertTrue("Variable category name is not a block: " + categoryName, categoryName.startsWith("block-"));

    // TODO: More block body testing
    
    // TODO: test local vars for all types
    
    // TODO: Add testing for list and map
    
    // TODO: Test 'new' operator?
    
    // TODO: initializers on local variables

    // TODO: Add 'while' statement

    // TODO: Add 'do' statement

    // TODO: Add 'continue' statement

    // TODO: Add 'break' statement

    // TODO: Add 'for' statement
    
    // TODO: Test exceptions
    // Missing operator, operand
    // Extra operator, operand
    // Missing semicolon before brace
    // Missing semicolon at end of line
    // Missing close paren
    // Backslash at end
    // Unterminated string
    // var name defined in multiple categories
    // Line number and Position for location of exceptions
    
  }
  
  @Test
  public void testTypes() throws Exception {
    // Test simple variable declarations
    {
    ScriptNode scriptNode = parser.parseScriptString("int i;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "i", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not IntegerTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof IntegerTypeNode);
    assertEquals("Local variable[0]", "integer i", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("long l;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "l", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not IntegerTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof IntegerTypeNode);
    assertEquals("Local variable[0]", "integer l", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("byte b;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "b", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not IntegerTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof IntegerTypeNode);
    assertEquals("Local variable[0]", "integer b", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("short s;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "s", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not IntegerTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof IntegerTypeNode);
    assertEquals("Local variable[0]", "integer s", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("float f;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "f", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not FloatTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "float f", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("double d;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "d", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not FloatTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "float d", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("boolean b;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "b", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not BooleanTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof BooleanTypeNode);
    assertEquals("Local variable[0]", "boolean b", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("string s;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "s", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not StringTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof StringTypeNode);
    assertEquals("Local variable[0]", "string s", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("object o;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "o", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not ObjectTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof ObjectTypeNode);
    assertEquals("Local variable[0]", "object o", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("list l;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "l", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not ListTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof ListTypeNode);
    ListTypeNode listTypeNode = (ListTypeNode) symbol.type;
    assertTrue("List type is missing", listTypeNode.elementType != null);
    assertTrue("List type is not ObjectTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof ObjectTypeNode);
    assertEquals("Local variable[0]", "list<object> l", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("list<object> l;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "l", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not ListTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof ListTypeNode);
    ListTypeNode listTypeNode = (ListTypeNode) symbol.type;
    assertTrue("List type is missing", listTypeNode.elementType != null);
    assertTrue("List type is not ObjectTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof ObjectTypeNode);
    assertEquals("Local variable[0]", "list<object> l", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("list<string> l;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "l", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not ListTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof ListTypeNode);
    ListTypeNode listTypeNode = (ListTypeNode) symbol.type;
    assertTrue("List type is missing", listTypeNode.elementType != null);
    assertTrue("List type is not StringTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof StringTypeNode);
    assertEquals("Local variable[0]", "list<string> l", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("list<list<float>> l;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "l", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not ListTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof ListTypeNode);
    ListTypeNode listTypeNode = (ListTypeNode) symbol.type;
    assertTrue("List type is missing", listTypeNode.elementType != null);
    assertTrue("List type is not ListTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof ListTypeNode);
    listTypeNode = (ListTypeNode) listTypeNode.elementType;
    assertTrue("List element type is missing", listTypeNode.elementType != null);
    assertTrue("List element type is not FloatTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "list<list<float>> l", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("List key type is missing", mapTypeNode.keyType != null);
    assertTrue("List key type is not ObjectTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("List entry type is missing", mapTypeNode.entryType != null);
    assertTrue("List entry type is not ObjectTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof ObjectTypeNode);
    assertEquals("Local variable[0]", "map<string, object> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<object> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not ObjectTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not ObjectTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof ObjectTypeNode);
    assertEquals("Local variable[0]", "map<string, object> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<string, object> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not ObjectTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not ObjectTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof ObjectTypeNode);
    assertEquals("Local variable[0]", "map<string, object> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<float, object> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not FloatTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof FloatTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not ObjectTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof ObjectTypeNode);
    assertEquals("Local variable[0]", "map<float, object> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<string> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not StringTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof StringTypeNode);
    assertEquals("Local variable[0]", "map<string, string> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<boolean, object> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not BooleanTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof BooleanTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not ObjectTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof ObjectTypeNode);
    assertEquals("Local variable[0]", "map<boolean, object> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<float> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not FloatTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "map<string, float> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<map<float>> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not MapTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof MapTypeNode);
    mapTypeNode = (MapTypeNode) mapTypeNode.entryType;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not FloatTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "map<string, map<string, float>> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<map<string, float>> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not MapTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof MapTypeNode);
    mapTypeNode = (MapTypeNode) mapTypeNode.entryType;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not FloatTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "map<string, map<string, float>> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<string, map<float, float>> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not MapTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof MapTypeNode);
    mapTypeNode = (MapTypeNode) mapTypeNode.entryType;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not FloatTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof FloatTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not FloatTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "map<string, map<float, float>> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("map<list<float>> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MapTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) symbol.type;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not ListTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof ListTypeNode);
    ListTypeNode listTypeNode = (ListTypeNode) mapTypeNode.entryType;
    assertTrue("List element type is missing", listTypeNode.elementType != null);
    assertTrue("List element type is not FloatTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "map<string, list<float>> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("list<map<float>> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not ListTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof ListTypeNode);
    ListTypeNode listTypeNode = (ListTypeNode) symbol.type;
    assertTrue("List type is missing", listTypeNode.elementType != null);
    assertTrue("List type is not MapTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof MapTypeNode);
    assertTrue("List element type is missing", listTypeNode.elementType != null);
    assertTrue("List element type is not MapTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) listTypeNode.elementType;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not FloatTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "list<map<string, float>> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("list<map<string, float>> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not ListTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof ListTypeNode);
    ListTypeNode listTypeNode = (ListTypeNode) symbol.type;
    assertTrue("List type is missing", listTypeNode.elementType != null);
    assertTrue("List type is not MapTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof MapTypeNode);
    assertTrue("List element type is missing", listTypeNode.elementType != null);
    assertTrue("List element type is not MapTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) listTypeNode.elementType;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertTrue("Map key type is not StringTypeNode: " + mapTypeNode.keyType.getClass().getSimpleName(), mapTypeNode.keyType instanceof StringTypeNode);
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    assertTrue("Map entry type is not FloatTypeNode: " + mapTypeNode.entryType.getClass().getSimpleName(), mapTypeNode.entryType instanceof FloatTypeNode);
    assertEquals("Local variable[0]", "list<map<string, float>> m", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("list<map<list<web>, list<map<boolean, list<float>>>>> m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not ListTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof ListTypeNode);
    ListTypeNode listTypeNode = (ListTypeNode) symbol.type;
    assertTrue("List type is missing", listTypeNode.elementType != null);
    assertTrue("List type is not MapTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof MapTypeNode);
    assertTrue("List element type is missing", listTypeNode.elementType != null);
    assertTrue("List element type is not MapTypeNode: " + listTypeNode.elementType.getClass().getSimpleName(), listTypeNode.elementType instanceof MapTypeNode);
    MapTypeNode mapTypeNode = (MapTypeNode) listTypeNode.elementType;
    assertTrue("Map key type is missing", mapTypeNode.keyType != null);
    assertEquals("Map key type", "list<web>", mapTypeNode.keyType.toString());
    assertTrue("Map entry type is missing", mapTypeNode.entryType != null);
    ListTypeNode listTypeNode1 = (ListTypeNode)mapTypeNode.entryType;
    MapTypeNode mapTypeNode1 = (MapTypeNode) listTypeNode1.elementType;
    TypeNode keyType = mapTypeNode1.keyType;
    assertEquals("Map entry type key type map key type", "boolean",  ((MapTypeNode)((ListTypeNode)mapTypeNode.entryType).elementType).keyType.toString());
    assertEquals("Map entry type key type map key type", "list<float>",  ((MapTypeNode)((ListTypeNode)mapTypeNode.entryType).elementType).entryType.toString());
    assertEquals("Map entry type key type", "map<boolean, list<float>>",  ((ListTypeNode)mapTypeNode.entryType).elementType.toString());
    assertEquals("Map entry type", "list<map<boolean, list<float>>>",  mapTypeNode.entryType.toString());
    assertEquals("Local variable[0]", "list<map<list<web>, list<map<boolean, list<float>>>>> m", symbol.toStringNoCategory());
    }
    
    {
    ScriptNode scriptNode = parser.parseScriptString("date d;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "d", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not DateTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof DateTypeNode);
    assertEquals("Local variable[0]", "date d", symbol.toStringNoCategory());
    }

    {
    ScriptNode scriptNode = parser.parseScriptString("money m;");
    assertTrue("No script tree returned", scriptNode != null);
    assertTrue("Script node is not a ScriptNode: " + scriptNode.getClass().getSimpleName(), scriptNode instanceof ScriptNode);
    assertTrue("Script node does not have a BlockStatementNode", scriptNode.blockNode != null);
    assertTrue("Script block node is not a BlockStatementNode: " + scriptNode.blockNode.getClass().getSimpleName(), scriptNode.blockNode instanceof BlockStatementNode);
    BlockStatementNode blockNode = scriptNode.blockNode;
    List<Symbol> localVariables = blockNode.localVariables;
    assertTrue("Block node does not have local variables", localVariables != null);
    assertEquals("Block node local variable count", 1, localVariables.size());
    Symbol symbol = localVariables.get(0);
    assertTrue("Local variable[0] symbol is missing", symbol != null);
    assertTrue("Local variable[0] name is missing", symbol.name != null);
    assertEquals("Local variable[0] name", "m", symbol.name);
    assertTrue("Local variable[0] type is missing", symbol.type != null);
    assertTrue("Local variable[0] name type is not MoneyTypeNode: " + symbol.type.getClass().getSimpleName(), symbol.type instanceof MoneyTypeNode);
    assertEquals("Local variable[0]", "money m", symbol.toStringNoCategory());
    }

  }

}
