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

package com.basetechnology.s0.agentserver.script.runtime;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.basetechnology.s0.agentserver.AgentServerException;
import com.basetechnology.s0.agentserver.AgentServerTestBase;
import com.basetechnology.s0.agentserver.appserver.AgentAppServer;
import com.basetechnology.s0.agentserver.config.AgentServerProperties;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionNode;
import com.basetechnology.s0.agentserver.script.intermediate.IntegerTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.ScriptNode;
import com.basetechnology.s0.agentserver.script.intermediate.StringTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolValues;
import com.basetechnology.s0.agentserver.script.parser.ParserException;
import com.basetechnology.s0.agentserver.script.parser.ScriptParser;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.script.runtime.ExceptionInfo;
import com.basetechnology.s0.agentserver.script.runtime.ScriptRuntime;
import com.basetechnology.s0.agentserver.script.runtime.value.BooleanValue;
import com.basetechnology.s0.agentserver.script.runtime.value.FalseValue;
import com.basetechnology.s0.agentserver.script.runtime.value.FloatValue;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.ListValue;
import com.basetechnology.s0.agentserver.script.runtime.value.MapValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.TrueValue;
import com.basetechnology.s0.agentserver.script.runtime.value.Value;

public class ScriptRuntimeTest extends AgentServerTestBase {

  AgentAppServer agentAppServer = null;
  AgentServer agentServer = null;
  AgentDefinition dummyAgentDefinition;
  AgentInstance dummyAgentInstance;
  SymbolManager symbols;
  Map<String, SymbolValues> values;
  ScriptParser parser;
  ScriptRuntime scriptRuntime;

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
    File pf = new File(AgentServerProperties.DEFAULT_PERSISTENT_STORE_PATH);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServerProperties.DEFAULT_PERSISTENT_STORE_PATH, ! pf.exists());

    agentAppServer = new AgentAppServer();
    agentServer = agentAppServer.agentServer;
    dummyAgentDefinition = new AgentDefinition(agentServer);
    dummyAgentInstance = new AgentInstance(dummyAgentDefinition);
    parser = new ScriptParser(dummyAgentInstance);
    symbols = dummyAgentInstance.symbolManager;
    values = dummyAgentInstance.categorySymbolValues;
    scriptRuntime = new ScriptRuntime(dummyAgentInstance);
  }

  @After
  public void tearDown() throws Exception {
    if (agentAppServer != null){
      agentAppServer.stop();
    }
    File pf = new File(AgentServerProperties.DEFAULT_PERSISTENT_STORE_PATH);
    pf.delete();
    assertTrue("Persistent store not deleted: " + AgentServerProperties.DEFAULT_PERSISTENT_STORE_PATH, ! pf.exists());
    agentAppServer = null;
    agentServer = null;
  }

  public void assertReturnedSomething(Object object){
    assertTrue("Nothing was returned", object != null);
  }

  public void assertType(String message, Value value, String expectedClassName){
    String actualClassName = value.getClass().getSimpleName();
    if ((! expectedClassName.equalsIgnoreCase("BooleanValue")) ||
        (! actualClassName.equalsIgnoreCase("TrueValue") &&
            ! actualClassName.equalsIgnoreCase("FalseValue")))
    assertEquals(message, expectedClassName, actualClassName);
  }

  public void assertValue(String message, String expected, Value value){
    assertJsonSourceEquals(message, expected, value.toJson());
  }

  public void assertValueText(String message, String expected, Value value){
    assertEquals(message, expected, value.toString());
  }

  public void assertValueInt(String message, int expected, Value value){
    assertEquals(message, expected, value.getIntValue());
  }

  public Value eval(String expressionString, String type) throws TokenizerException, ParserException, AgentServerException {
    return eval(expressionString, type, 0);
  }

  public Value eval(String expressionString, String type, int numExceptionsExpected) throws TokenizerException, ParserException, AgentServerException {
    // Clear out old exceptions
    dummyAgentInstance.exceptionHistory.clear();
    
    ExpressionNode expressionNode = parser.parseExpressionString(expressionString);
    assertTrue("Null was returned from expression parser", expressionNode != null);
    Value valueNode = scriptRuntime.evaluateExpression("", expressionNode);
    
    // Check for exceptions
    List<ExceptionInfo> exceptions = dummyAgentInstance.exceptionHistory;
    int numExceptions = exceptions.size();
    String message = null;
    String exceptionType = null;
    if (numExceptions > 0){
      message = exceptions.get(0).message;
      exceptionType = exceptions.get(0).type;
    }
    if (numExceptions > 0 && numExceptionsExpected == 0)
      fail("Unexpected exception: type: " + exceptionType + " message: " + message);
    else
      assertEquals("Number of exceptions that occurred" + (message != null ? "(last was '" + message + "')" : ""), numExceptionsExpected, numExceptions);
    if (numExceptions == 0){
      assertReturnedSomething(valueNode);
      assertType("Returned", valueNode, type);
    }
    return valueNode;
  }

  public void eval(String expressionString, String exceptionTypeExpected,
      String exceptionMessageExpected) throws TokenizerException, ParserException, AgentServerException {
    // Clear out old exceptions
    dummyAgentInstance.exceptionHistory.clear();
    
    ExpressionNode expressionNode = parser.parseExpressionString(expressionString);
    assertTrue("Null was returned from expression parser", expressionNode != null);
    Value valueNode = scriptRuntime.evaluateExpression("", expressionNode);
    
    // Check for exceptions
    List<ExceptionInfo> exceptions = dummyAgentInstance.exceptionHistory;
    int numExceptions = exceptions.size();
    String message = null;
    String exceptionType = null;
    if (numExceptions > 0){
      message = exceptions.get(0).message;
      exceptionType = exceptions.get(0).type;
    }
    if (numExceptions == 0)
      fail("No exception occurred, but expected exception of type " + exceptionTypeExpected + " and message \"" + exceptionMessageExpected + "\"");
    else {
      assertEquals("Exception type", exceptionTypeExpected, exceptionType);
      assertEquals("Exception message", exceptionMessageExpected, message);
    }
  }

  public Value evalNull(String expressionString) throws AgentServerException, TokenizerException, ParserException{
    Value value = eval(expressionString, "NullValue");
    return value;
  }

  public Value evalNull(String expressionString, int numExceptionsExpected) throws AgentServerException, TokenizerException, ParserException{
    Value value = eval(expressionString, "NullValue", numExceptionsExpected);
    return value;
  }

  public Value evalBoolean(String expressionString, boolean expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = eval(expressionString, "BooleanValue");
    assertEquals("Boolean return value", expected, value.getBooleanValue());
    return value;
  }

  public Value evalFloat(String expressionString, double expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = eval(expressionString, "FloatValue");
    assertEquals("Float  return value", expected, value.getFloatValue(), 0.0001);
    return value;
  }

  public Value evalInt(String expressionString, int expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = eval(expressionString, "IntegerValue");
    assertEquals("Integer return value", expected, value.getIntValue());
    return value;
  }

  public Value evalString(String expressionString, String expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = eval(expressionString, "StringValue");
    assertEquals("String return value", expected, value.getStringValue());
    return value;
  }

  public Value runScript(String script, String type) throws TokenizerException, ParserException, AgentServerException {
    return runScript(script, type, 0);
  }

  public Value runScript(String script, String type, int numExceptionsExpected) throws TokenizerException, ParserException, AgentServerException {
    // Clear out old exceptions
    dummyAgentInstance.exceptionHistory.clear();
    
    ScriptNode scriptNode = parser.parseScriptString(script);
    assertTrue("Null was returned from script parser", scriptNode != null);
    Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    
    // Check for exceptions
    List<ExceptionInfo> exceptions = dummyAgentInstance.exceptionHistory;
    int numExceptions = exceptions.size();
    String message = null;
    String exceptionType = null;
    if (numExceptions > 0){
      message = exceptions.get(0).message;
      exceptionType = exceptions.get(0).type;
    }
    if (numExceptions > 0 && numExceptionsExpected == 0)
      fail("Unexpected exception: type: " + exceptionType + " message: " + message);
    else
      assertEquals("Number of exceptions that occurred" + (message != null ? "(last was '" + message + "')" : ""), numExceptionsExpected, numExceptions);
    if (numExceptions == 0){
      assertReturnedSomething(valueNode);
      assertType("Returned", valueNode, type);
    }
    return valueNode;
  }

  public Value runNullScript(String script) throws AgentServerException, TokenizerException, ParserException{
    return runNullScript(script, 0);
  }

  public Value runNullScript(String script, int numExceptionsExpected) throws AgentServerException, TokenizerException, ParserException{
    Value value = runScript(script, "NullValue", numExceptionsExpected);
    return value;
  }

  public Value runBooleanScript(String script, boolean expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = runScript(script, "BooleanValue");
    assertEquals("Boolean return value", expected, value.getBooleanValue());
    return value;
  }

  public Value runIntScript(String script, int expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = runScript(script, "IntegerValue");
    assertEquals("Integer return value", expected, value.getIntValue());
    return value;
  }

  public Value runFloatScript(String script, double expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = runScript(script, "FloatValue");
    assertEquals("Float return value", expected, value.getFloatValue(), 0.0001);
    return value;
  }

  public Value runStringScript(String script, String expected) throws AgentServerException, TokenizerException, ParserException{
    return runStringScript(script, expected, 0);
  }

  public Value runStringScript(String script, String expected, int numExceptionsExpected) throws AgentServerException, TokenizerException, ParserException{
    Value value = runScript(script, "StringValue", numExceptionsExpected);
    if (dummyAgentInstance.exceptionHistory.size() == 0)
      assertEquals("String return value", expected, value.getStringValue());
    return value;
  }

  public Value runMapScript(String script, String expected) throws AgentServerException, TokenizerException, ParserException{
    return runMapScript(script, expected, 0);
  }

  public Value runMapScript(String script, String expected, int numExceptionsExpected) throws AgentServerException, TokenizerException, ParserException{
    Value value = runScript(script, "MapValue", numExceptionsExpected);
    if (dummyAgentInstance.exceptionHistory.size() == 0)
      assertEquals("Map return value", expected, value.toString());
    return value;
  }

  public Value runListScript(String script, String expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = runScript(script, "ListValue");
    assertEquals("List return value", expected, value.toString());
    return value;
  }

  public Value callFunction(String script, List<Value> arguments, String type) throws TokenizerException, ParserException, AgentServerException {
    return callFunction(script, arguments, type, 0);
  }

  public Value callFunction(String script, List<Value> arguments, String type, int numExceptionsExpected) throws TokenizerException, ParserException, AgentServerException {
    // Clear out old exceptions
    dummyAgentInstance.exceptionHistory.clear();
    
    ScriptNode scriptNode = parser.parseScriptString(script);
    assertTrue("Null was returned from script parser", scriptNode != null);
    Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode, arguments);
    
    // Check for exceptions
    List<ExceptionInfo> exceptions = dummyAgentInstance.exceptionHistory;
    int numExceptions = exceptions.size();
    String message = null;
    String exceptionType = null;
    if (numExceptions > 0){
      message = exceptions.get(0).message;
      exceptionType = exceptions.get(0).type;
    }
    if (numExceptions > 0 && numExceptionsExpected == 0)
      fail("Unexpected exception: type: " + exceptionType + " message: " + message);
    else
      assertEquals("Number of exceptions that occurred" + (message != null ? "(last was '" + message + "')" : ""), numExceptionsExpected, numExceptions);
    if (numExceptions == 0){
      assertReturnedSomething(valueNode);
      assertType("Returned", valueNode, type);
    }
    return valueNode;
  }

  public Value callNullFunction(String script, List<Value> arguments) throws AgentServerException, TokenizerException, ParserException{
    return callNullFunction(script, arguments, 0);
  }

  public Value callNullFunction(String script, List<Value> arguments, int numExceptionsExpected) throws AgentServerException, TokenizerException, ParserException{
    Value value = callFunction(script, arguments, "NullValue", numExceptionsExpected);
    return value;
  }

  public Value callBooleanFunction(String script, List<Value> arguments, boolean expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = callFunction(script, arguments, "BooleanValue");
    assertEquals("Boolean return value", expected, value.getBooleanValue());
    return value;
  }

  public Value callIntFunction(String script, List<Value> arguments, int expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = callFunction(script, arguments, "IntegerValue");
    assertEquals("Integer return value", expected, value.getIntValue());
    return value;
  }

  public Value callFloatFunction(String script, List<Value> arguments, double expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = callFunction(script, arguments, "FloatValue");
    assertEquals("Float return value", expected, value.getFloatValue(), 0.0001);
    return value;
  }

  public Value callStringFunction(String script, List<Value> arguments, String expected) throws AgentServerException, TokenizerException, ParserException{
    return callStringFunction(script, arguments, expected, 0);
  }

  public Value callStringFunction(String script, List<Value> arguments, String expected, int numExceptionsExpected) throws AgentServerException, TokenizerException, ParserException{
    Value value = callFunction(script, arguments, "StringValue", numExceptionsExpected);
    if (dummyAgentInstance.exceptionHistory.size() == 0)
      assertEquals("String return value", expected, value.getStringValue());
    return value;
  }

  public Value callMapFunction(String script, List<Value> arguments, String expected) throws AgentServerException, TokenizerException, ParserException{
    return callMapFunction(script, arguments, expected, 0);
  }

  public Value callMapFunction(String script, List<Value> arguments, String expected, int numExceptionsExpected) throws AgentServerException, TokenizerException, ParserException{
    Value value = callFunction(script, arguments, "MapValue", numExceptionsExpected);
    if (dummyAgentInstance.exceptionHistory.size() == 0)
      assertEquals("Map return value", expected, value.toString());
    return value;
  }

  public Value callListFunction(String script, List<Value> arguments, String expected) throws AgentServerException, TokenizerException, ParserException{
    Value value = callFunction(script, arguments, "ListValue");
    assertEquals("List return value", expected, value.toString());
    return value;
  }
  
  @Test
  public void test() throws Exception {
    // Test expression evaluation for empty expressions
    ExpressionNode expressionNode = parser.parseExpressionString(null);
    assertTrue("Null was not returned from parser", expressionNode == null);
    Value valueNode = scriptRuntime.evaluateExpression(parser.scriptString, null);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);

    expressionNode = parser.parseExpressionString("");
    assertTrue("Null was not returned from parser", expressionNode == null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, null);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);

    expressionNode = parser.parseExpressionString("   \t  \n  \t \t \n\n ");
    assertTrue("Null was not returned from parser", expressionNode == null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, null);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);

    expressionNode = parser.parseExpressionString(" /* comment */ // comment \n   ");
    assertTrue("Null was not returned from parser", expressionNode == null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, null);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);

    // Test expression evaluation for simple literals
    expressionNode = parser.parseExpressionString("0");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Integer value", 0, valueNode.getLongValue());

    expressionNode = parser.parseExpressionString("123");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Integer value", 123, valueNode.getLongValue());

    expressionNode = parser.parseExpressionString("null");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);
    assertEquals("Null value", null, valueNode.getValue());

    expressionNode = parser.parseExpressionString("true");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", true, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("false");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", false, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("1.23");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Boolean value", 1.23, valueNode.getDoubleValue(), 0.001);

    expressionNode = parser.parseExpressionString("'Hello World'");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Stringvalue", "Hello World", valueNode.getStringValue());

    // Test evaluation of simple expressions

    expressionNode = parser.parseExpressionString("2 + 2");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Integer value", 4, valueNode.getLongValue());

    expressionNode = parser.parseExpressionString("123 + 456");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Integer value", 579, valueNode.getLongValue());

    expressionNode = parser.parseExpressionString("123.1 + 456.2");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Float value", 579.3, valueNode.getDoubleValue(), 0.001);

    expressionNode = parser.parseExpressionString("'Hello' + ' World'");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Stringvalue", "Hello World", valueNode.getStringValue());

    expressionNode = parser.parseExpressionString("'Hello' + ' ' + 'World'");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Stringvalue", "Hello World", valueNode.getStringValue());

    expressionNode = parser.parseExpressionString("123.1 - 10.6");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Float value", 112.5, valueNode.getDoubleValue(), 0.001);

    expressionNode = parser.parseExpressionString("123.1 * 2");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Float value", 246.2, valueNode.getDoubleValue(), 0.001);

    expressionNode = parser.parseExpressionString("3.14 * 5");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Float value", 15.7, valueNode.getDoubleValue(), 0.001);

    expressionNode = parser.parseExpressionString("7 + 3.14 * 5");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Float value", 22.7, valueNode.getDoubleValue(), 0.001);

    expressionNode = parser.parseExpressionString("2 + 2 == 4");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", true, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("2 + 2 != 4");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", false, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("2 + 2 == 5");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", false, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("2 + 2 != 5");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", true, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("'Hello' == 'Hel' + 'lo'");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", true, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("'Hello' != 'Hel' + 'lo'");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", false, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("'Hello' == 'World'");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", false, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("'Hello' != 'World'");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", true, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("0 % 2");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Integer value", 0, valueNode.getLongValue());

    expressionNode = parser.parseExpressionString("1 % 2");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Integer value", 1, valueNode.getLongValue());

    expressionNode = parser.parseExpressionString("2 % 2");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Integer value", 0, valueNode.getLongValue());

    expressionNode = parser.parseExpressionString("3 % 2");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Integer value", 1, valueNode.getLongValue());

    expressionNode = parser.parseExpressionString("1 <= 6 && 1 % 2 == 0");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", false, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("2 <= 6 && 2 % 2 == 0");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", true, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("3 <= 6 && 2 % 3 == 0");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", false, valueNode.getBooleanValue());

    expressionNode = parser.parseExpressionString("4 <= 6 && 4 % 2 == 0");
    assertTrue("Null returned from parser", expressionNode != null);
    valueNode = scriptRuntime.evaluateExpression(parser.scriptString, expressionNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Boolean value", true, valueNode.getBooleanValue());

    // Test empty scripts
    ScriptNode scriptNode = parser.parseScriptString(null);
    assertTrue("Null was not returned from script parser", scriptNode == null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);

    scriptNode = parser.parseScriptString("");
    assertTrue("Null was not returned from script parser", scriptNode == null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);

    scriptNode = parser.parseScriptString("   \t  \n  \t \t \n\n ");
    assertTrue("Null was not returned from script parser", scriptNode == null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);

    scriptNode = parser.parseScriptString(" /* comment */ // comment \n   ");
    assertTrue("Null was not returned from script parser", scriptNode == null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);

    // Test script with 'if' and 'return'
    scriptNode = parser.parseScriptString(" if (2 + 2 == 4) return 'Yes'; else return 'No';");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "Yes", valueNode.getStringValue());

    scriptNode = parser.parseScriptString(" if (2 + 2 != 4) return 'Yes'; else return 'No';");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "No", valueNode.getStringValue());

    scriptNode = parser.parseScriptString(" if (2 + 2 != 5) return 'Yes'; else return 'No';");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "Yes", valueNode.getStringValue());

    // Test handling of default values for unintialized variables
    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("string s; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("boolean b; return b;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Return boolean value", false, valueNode.getValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("int i; return i;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", (long)0, valueNode.getValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("integer i; return i;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", (long)0, valueNode.getValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("long i; return i;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", (long)0, valueNode.getValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("byte b; return b;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", (long)0, valueNode.getValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("short s; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", (long)0, valueNode.getValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("float f; return f;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 0.0, valueNode.getValue());

    // Test pre and post increment and decrement operators
    Symbol symbol = symbols.put("testcat", "i", new IntegerTypeNode());
    values.put("testcat", new SymbolValues("testcat"));
    values.get("testcat").put(symbol, new IntegerValue(125));
    scriptNode = parser.parseScriptString(" return ++i;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 126, valueNode.getLongValue());
    assertEquals("Final value of i", 126, values.get("testcat").get(symbol).getLongValue());

    symbol = symbols.put("testcat", "i", new IntegerTypeNode());
    values.get("testcat").put(symbol, new IntegerValue(125));
    scriptNode = parser.parseScriptString(" return i++;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 125, valueNode.getLongValue());
    assertEquals("Final value of i", 126, values.get("testcat").get(symbol).getLongValue());

    symbol = symbols.put("testcat", "i", new IntegerTypeNode());
    values.get("testcat").put(symbol, new IntegerValue(125));
    scriptNode = parser.parseScriptString(" return --i;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 124, valueNode.getLongValue());
    assertEquals("Final value of i", 124, values.get("testcat").get(symbol).getLongValue());

    symbol = symbols.put("testcat", "i", new IntegerTypeNode());
    values.get("testcat").put(symbol, new IntegerValue(125));
    scriptNode = parser.parseScriptString(" return i--;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 125, valueNode.getLongValue());
    assertEquals("Final value of i", 124, values.get("testcat").get(symbol).getLongValue());

    symbol = symbols.put("testcat", "f", new IntegerTypeNode());
    values.get("testcat").put(symbol, new FloatValue(234.1));
    scriptNode = parser.parseScriptString(" return ++f;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 235.1, valueNode.getDoubleValue(), 0.001);
    assertEquals("Final value of f", 235.1, values.get("testcat").get(symbol).getDoubleValue(), 0.001);

    symbol = symbols.put("testcat", "f", new IntegerTypeNode());
    values.get("testcat").put(symbol, new FloatValue(234.1));
    scriptNode = parser.parseScriptString(" return f++;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 234.1, valueNode.getDoubleValue(), 0.001);
    assertEquals("Final value of f", 235.1, values.get("testcat").get(symbol).getFloatValue(), 0.001);

    symbol = symbols.put("testcat", "f", new IntegerTypeNode());
    values.get("testcat").put(symbol, new FloatValue(234.1));
    scriptNode = parser.parseScriptString(" return --f;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 233.1, valueNode.getDoubleValue(), 0.001);
    assertEquals("Final value of f", 233.1, values.get("testcat").get(symbol).getValue());

    symbol = symbols.put("testcat", "f", new IntegerTypeNode());
    values.get("testcat").put(symbol, new FloatValue(234.1));
    scriptNode = parser.parseScriptString(" return f--;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 234.1, valueNode.getDoubleValue(), 0.001);
    assertEquals("Final value of f", 233.1, values.get("testcat").get(symbol).getValue());

    symbol = symbols.put("testcat", "s", new IntegerTypeNode());
    values.get("testcat").put(symbol, new StringValue("Hello World"));
    scriptNode = parser.parseScriptString(" return ++s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return String value", " Hello World", valueNode.getStringValue());
    assertEquals("Final value of s", " Hello World", values.get("testcat").get(symbol).getValue());

    symbol = symbols.put("testcat", "s", new IntegerTypeNode());
    values.get("testcat").put(symbol, new StringValue("Hello World"));
    scriptNode = parser.parseScriptString(" return s++;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return String value", "Hello World", valueNode.getStringValue());
    assertEquals("Final value of s", "Hello World ", values.get("testcat").get(symbol).getValue());

    symbol = symbols.put("testcat", "s", new IntegerTypeNode());
    values.get("testcat").put(symbol, new StringValue("Hello World"));
    scriptNode = parser.parseScriptString(" return --s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return String value", "ello World", valueNode.getStringValue());
    assertEquals("Final value of s", "ello World", values.get("testcat").get(symbol).getStringValue());

    symbol = symbols.put("testcat", "s", new IntegerTypeNode());
    values.get("testcat").put(symbol, new StringValue("Hello World"));
    scriptNode = parser.parseScriptString(" return s--;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return String value", "Hello World", valueNode.getStringValue());
    assertEquals("Final value of s", "Hello Worl", values.get("testcat").get(symbol).getStringValue());

    // Test increment and decrement for non-integers, strings, expressions
    // TODO:

    // Parenthesized expressions

    // Test basic 'for' loop
    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("int sum; for (sum = 0, int i = 1; i <= 10; i++) sum += i;  return sum;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 55, valueNode.getIntValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("int sum = 0; for (int i = 1; i <= 10; i++) sum += i;  return sum;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 55, valueNode.getIntValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("int sum = 0, i; for (i = 1; i <= 10; i++) sum += i;  return sum;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 55, valueNode.getIntValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("string s; for (int i = 1; i <= 5; i++) s += ' ' + i;  return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return integer value", " 1 2 3 4 5", valueNode.getStringValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("float sum; for (sum = 0.0, int j = 0, float f = 0.11; j < 7; j++, f++) sum += f * 0.11;  return sum;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return Float value", 2.3947, valueNode.getDoubleValue(), 0.001);

    // Test endless loops
    agentServer.config.putDefaultExecutionLevel(3);
    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("int sum = 0, int i = 0; for (;;){if (i > 10) break; sum += i++;}  return sum;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 55, valueNode.getIntValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("int sum = 0, int i = 0; while (true){if (i > 10) break; sum += i++;}  return sum;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 55, valueNode.getIntValue());

    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("int sum = 0, int i = 0; do {if (i > 10) break; sum += i++;} while (true);  return sum;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 55, valueNode.getIntValue());

    // Simple math function calls
    symbols.clear();
    values.clear();
    scriptNode = parser.parseScriptString("return sqrt(2);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 1.41421, valueNode.getFloatValue(), 0.0001);

    scriptNode = parser.parseScriptString("return sqrt(2.0);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 1.41421, valueNode.getFloatValue(), 0.0001);

    scriptNode = parser.parseScriptString("return sqrt('2.00');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 1.41421, valueNode.getFloatValue(), 0.0001);

    scriptNode = parser.parseScriptString("return (2).sqrt;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 1.41421, valueNode.getFloatValue(), 0.0001);

    scriptNode = parser.parseScriptString("return (2).sqrt();");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 1.41421, valueNode.getFloatValue(), 0.0001);

    scriptNode = parser.parseScriptString("return (2.0).sqrt;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 1.41421, valueNode.getFloatValue(), 0.0001);

    scriptNode = parser.parseScriptString("return (2.0).sqrt();");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 1.41421, valueNode.getFloatValue(), 0.0001);

    evalFloat("pi()", 3.14159);
    evalFloat("pi() * 2", 6.28318);
    evalFloat("pi() / 2", 1.57079);
    evalFloat("pi() * 0.5", 1.57079);
    
    // Other function calls
    scriptNode = parser.parseScriptString("return 'abc'.length;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return interger value", 3, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc'.length();");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return interger value", 3, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; return s.length;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return interger value", 3, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc'.type;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "string", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return (123).type;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "integer", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return null.type;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "null", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return false.type;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "boolean", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return true.type;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "boolean", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("list lst; return lst.type;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "list<object>", valueNode.getStringValue());

    // TODO: now, date, money, list, map

    // Test subscripts references for strings
    scriptNode = parser.parseScriptString("return 'abc'[0];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "a", valueNode.getStringValue());
    evalString("'abc'.get(0)", "a");

    scriptNode = parser.parseScriptString("return 'abc'[1];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "b", valueNode.getStringValue());
    evalString("'abc'.get(1)", "b");

    scriptNode = parser.parseScriptString("return 'abc'[2];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "c", valueNode.getStringValue());
    evalString("'abc'.get(2)", "c");

    scriptNode = parser.parseScriptString("return 'abc'[0, 1];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "a", valueNode.getStringValue());
    evalString("'abc'.get(0, 1)", "a");

    scriptNode = parser.parseScriptString("return 'abc'[1, 2];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "b", valueNode.getStringValue());
    evalString("'abc'.get(1, 2)", "b");

    scriptNode = parser.parseScriptString("return 'abc'[2, 3];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "c", valueNode.getStringValue());
    evalString("'abc'.get(2, 3)", "c");

    scriptNode = parser.parseScriptString("return 'abc'[0, 2];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "ab", valueNode.getStringValue());
    evalString("'abc'.get(0, 2)", "ab");

    scriptNode = parser.parseScriptString("return 'abc'[1, 3];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "bc", valueNode.getStringValue());
    evalString("'abc'.get(1, 3)", "bc");

    scriptNode = parser.parseScriptString("return 'abc'[0, 3];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());
    evalString("'abc'.get(0, 3)", "abc");

    symbols.clear();
    values.clear();
    symbol = symbols.put("testcat", "s", new StringTypeNode());
    values.put("testcat", new SymbolValues("testcat"));
    values.get("testcat").put(symbol, new StringValue("abc"));
    scriptNode = parser.parseScriptString("return s[0, 3];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());
    evalString("s.get(0, 3)", "abc");

    scriptNode = parser.parseScriptString("return testcat.s[0, 3];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());
    evalString("testcat.s.get(0, 3)", "abc");

    scriptNode = parser.parseScriptString("string s = 'abc'; return s[0];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "a", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.get(0);", "a");
    runStringScript("string s = 'abc'; return s.charAt(0);", "a");

    scriptNode = parser.parseScriptString("string s = 'abc'; return s[1];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "b", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.get(1);", "b");
    runStringScript("string s = 'abc'; return s.charAt(1);", "b");

    scriptNode = parser.parseScriptString("string s = 'abc'; return s[2];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "c", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.get(2);", "c");

    scriptNode = parser.parseScriptString("string s = 'abc'; return s[1, 3];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "bc", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.get(1, 3);", "bc");

    // Test assignment into string characters
    scriptNode = parser.parseScriptString("string s = 'abc'; s[0]='x'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "xbc", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(0, 'x');", "xbc");
    runStringScript("string s = 'abc'; return s.set(0, 'x');", "xbc");

    scriptNode = parser.parseScriptString("string s = 'abc'; s[1]='x'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "axc", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(1, 'x');", "axc");
    runStringScript("string s = 'abc'; return s.set(1, 'x');", "axc");

    scriptNode = parser.parseScriptString("string s = 'abc'; s[2]='x'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abx", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(2, 'x');", "abx");

    // Test assignment of multi-char strings into strings
    scriptNode = parser.parseScriptString("string s = 'abc'; s[0]='xyz'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "xyzbc", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(0, 'xyz');", "xyzbc");

    scriptNode = parser.parseScriptString("string s = 'abc'; s[1]='xyz'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "axyzc", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(1, 'xyz');", "axyzc");

    scriptNode = parser.parseScriptString("string s = 'abc'; s[2]='xyz'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abxyz", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(2, 'xyz');", "abxyz");

    // Test sub-string range replacement
    scriptNode = parser.parseScriptString("string s = 'abc'; s[0, 1]='xyz'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "xyzbc", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(0, 1, 'xyz');", "xyzbc");

    scriptNode = parser.parseScriptString("string s = 'abc'; s[0, 2]='xyz'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "xyzc", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(0, 2, 'xyz');", "xyzc");

    scriptNode = parser.parseScriptString("string s = 'abc'; s[0, 3]='xyz'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "xyz", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(0, 3, 'xyz');", "xyz");

    scriptNode = parser.parseScriptString("string s = 'abc'; s[1, 2]='xyz'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "axyzc", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(1, 2, 'xyz');", "axyzc");

    scriptNode = parser.parseScriptString("string s = 'abc'; s[1, 3]='xyz'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "axyz", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(1, 3, 'xyz');", "axyz");

    scriptNode = parser.parseScriptString("string s = 'abc'; s[2, 3]='xyz'; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abxyz", valueNode.getStringValue());
    runStringScript("string s = 'abc'; return s.put(2, 3, 'xyz');", "abxyz");

    // Test string 'insert'
    scriptNode = parser.parseScriptString("string s = 'abc'; s = s.insert(0, 'xyz'); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "xyzabc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; s.insert(1, 'xyz'); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "axyzbc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; s.insert(2, 'xyz'); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abxyzc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; s.insert(3, 'xyz'); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abcxyz", valueNode.getStringValue());

    // Test string 'remove'
    scriptNode = parser.parseScriptString("string s = 'abc'; s.remove(0); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "bc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; s.remove(1); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "ac", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; s.remove(2); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "ab", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; s.remove(0,2); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "c", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; s.remove(1,3); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "a", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; s.remove(0,3); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; string s2 = s.copy; s.remove(0,3); return s2;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; string s2 = s; s.remove(0,3); return s2;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("string s = 'abc'; string s2 = s; s.remove(0,3); return s2;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());

    // Test 'upper' and 'lower' for strings
    scriptNode = parser.parseScriptString("return 'abc'.lower;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'ABC'.lower;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'Abc'.lower;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.upper;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "ABC", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'ABC'.upper;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "ABC", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'Abc'.upper;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "ABC", valueNode.getStringValue());

    // Test 'indexOf' for strings
    scriptNode = parser.parseScriptString("return 'abc'.indexOf('a');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 0, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc'.indexOf('b');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 1, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc'.indexOf('c');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 2, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc'.indexOf('d');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", -1, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc'.indexOf('ab');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 0, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.indexOf('ab');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 0, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.indexOf('ab', 0);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 0, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.indexOf('ab', 1);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 4, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("return 'abc'.indexOf('bc');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 1, valueNode.getIntValue());

    // Test 'before' for strings
    scriptNode = parser.parseScriptString("return 'abc'.before('a');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.before('b');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "a", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.before('c');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "ab", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.before('d');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.before('ab');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.before('bc');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "a", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.before('ab');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.before('ab', 0);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.before('ab', 1);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc-", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.before('ab', 5);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    // Test 'after' for strings
    scriptNode = parser.parseScriptString("return 'abc'.after('a');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "bc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.after('b');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "c", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.after('c');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.after('d');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.after('ab');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "c", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc'.after('bc');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.after('ab');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "c-abc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.after('ab', 0);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "c-abc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.after('ab', 1);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "c", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-abc'.after('ab', 5);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    // Test 'between' for strings
    scriptNode = parser.parseScriptString("return 'abc-def'.between('abc', 'def');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "-", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'xabc-p-defy'.between('abc', 'def');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "-p-", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return '-def-xabc-p-defy-abc'.between('abc', 'def');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "-p-", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-def--abc**def'.between('abc', 'def', 0);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "-", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'abc-def--abc**def'.between('abc', 'def', 1);");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "**", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return 'ab-c-def'.between('abc', 'def');");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    // Test equality, inequality, 'equals', and 'equalsIgnoreCase' for strings
    evalBoolean("'abc' == 'abc'", true);
    evalBoolean("'abc' == 'abcd'", false);
    evalBoolean("'abc' == 'def'", false);
    evalBoolean("'abc' != 'def'", true);
    evalBoolean("'abc' != 'Abc'", true);
    evalBoolean("'abc'.equals('abc')", true);
    evalBoolean("'abc'.equals('Abc')", false);
    evalBoolean("'abc'.equalsIgnoreCase('abc')", true);
    evalBoolean("'abc'.equalsIgnoreCase('Abc')", true);
    
    // Test 'toString'
    scriptNode = parser.parseScriptString("return null.toString;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "null", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return null.toString();");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "null", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return false.toString;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "false", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return true.toString();");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "true", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return (123).toString();");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "123", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return (2.13).toString();");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "2.13", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("return ('Hello World').toString();");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "Hello World", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World');lst.add('The End'); return lst.toString;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "[Hello, World, The End]", valueNode.getStringValue());

    // Test min and max for string list
    scriptNode = parser.parseScriptString("list lst; lst.add('wxy'); lst.add('xyz'); lst.add('abc'); lst.add('def'); return lst.min;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "abc", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("list lst; lst.add('wxy'); lst.add('xyz'); lst.add('abc'); lst.add('def'); return lst.max;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "xyz", valueNode.getStringValue());

    // TODO: min/max for list
    // TODO: More basic ops: /, rel ops, log-ops
    // Add parser tests as well

    // Test local variable default initialization
    scriptNode = parser.parseScriptString("boolean b; return b;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
    assertEquals("Return boolean value", false, valueNode.getBooleanValue());

    scriptNode = parser.parseScriptString("int i; return i;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 0, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("long i; return i;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    assertEquals("Return integer value", 0, valueNode.getIntValue());

    scriptNode = parser.parseScriptString("float f; return f;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 0.0, valueNode.getFloatValue(), 0.0001);

    scriptNode = parser.parseScriptString("double f; return f;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    assertEquals("Return float value", 0.0, valueNode.getFloatValue(), 0.0001);

    scriptNode = parser.parseScriptString("string s; return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("list l; return l;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    assertTrue("Return value not ArrayList", valueNode.getValue() instanceof ArrayList);
    assertEquals("Return value ArrayList size", 0, ((ArrayList)valueNode.getValue()).size());

    // Test simple list access
    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); return lst;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    Object value = valueNode.getValue();
    assertTrue("Return value not ArrayList", value instanceof ArrayList);
    ArrayList<Object> list = (ArrayList<Object>)value;
    assertEquals("Return value ArrayList size", 2, list.size());
    Object element = list.get(0);
    assertTrue("List element[0] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[0] string value", "Hello", valueNode.getStringValue());
    element = list.get(1);
    assertTrue("List element[1] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[1] string value", "World", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); lst.clear; return lst;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    value = valueNode.getValue();
    assertTrue("Return value not ArrayList", value instanceof ArrayList);
    list = (ArrayList<Object>)value;

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); lst.remove(0); return lst;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    value = valueNode.getValue();
    assertTrue("Return value not ArrayList", value instanceof ArrayList);
    list = (ArrayList<Object>)value;
    assertEquals("Return value ArrayList size", 1, list.size());
    element = list.get(0);
    assertTrue("List element[0] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[0] string value", "World", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); lst.remove(1); return lst;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    value = valueNode.getValue();
    assertTrue("Return value not ArrayList", value instanceof ArrayList);
    list = (ArrayList<Object>)value;
    assertEquals("Return value ArrayList size", 1, list.size());
    element = list.get(0);
    assertTrue("List element[0] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[0] string value", "Hello", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); return lst[0];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "Hello", valueNode.getStringValue());
    runStringScript("list lst; lst.add('Hello'); lst.add('World'); return lst.get(0);", "Hello");
    runStringScript("list lst; lst.put('Hello'); lst.put('World'); return lst.get(0);", "Hello");
    runStringScript("list lst; lst.set('Hello'); lst.set('World'); return lst.get(0);", "Hello");

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); return lst[1];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "World", valueNode.getStringValue());
    runStringScript("list lst; lst.add('Hello'); lst.add('World'); return lst.get(1);", "World");

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); lst.add('test'); return lst[0,3];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    value = valueNode.getValue();
    assertTrue("Return value not ArrayList", value instanceof ArrayList);
    list = (ArrayList<Object>)value;
    assertEquals("Return value ArrayList size", 3, list.size());
    element = list.get(0);
    assertTrue("List element[0] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[0] string value", "Hello", valueNode.getStringValue());
    element = list.get(1);
    assertTrue("List element[1] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[1] string value", "World", valueNode.getStringValue());
    element = list.get(2);
    assertTrue("List element[2] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[2] string value", "test", valueNode.getStringValue());
    runListScript("list lst; lst.add('Hello'); lst.add('World'); lst.add('test'); return lst.get(0,3);", "[Hello, World, test]");

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); lst.add('test'); return lst[0,2];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    value = valueNode.getValue();
    assertTrue("Return value not ArrayList", value instanceof ArrayList);
    list = (ArrayList<Object>)value;
    assertEquals("Return value ArrayList size", 2, list.size());
    element = list.get(0);
    assertTrue("List element[0] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[0] string value", "Hello", valueNode.getStringValue());
    element = list.get(1);
    assertTrue("List element[1] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[1] string value", "World", valueNode.getStringValue());
    runListScript("list lst; lst.add('Hello'); lst.add('World'); lst.add('test'); return lst.get(0, 2);", "[Hello, World]");

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); lst.add('test'); return lst[1,2];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    value = valueNode.getValue();
    assertTrue("Return value not ArrayList", value instanceof ArrayList);
    list = (ArrayList<Object>)value;
    assertEquals("Return value ArrayList size", 1, list.size());
    element = list.get(0);
    assertTrue("List element[0] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[0] string value", "World", valueNode.getStringValue());
    runListScript("list lst; lst.add('Hello'); lst.add('World'); lst.add('test'); return lst.get(1, 2);", "[World]");

    runListScript("list lst; lst.add('Hello'); lst.add('World'); lst.add('test'); return lst.get(1, 3);", "[World, test]");

    runListScript("list lst; lst.add('Hello'); lst.add('World'); lst.add('test'); return lst.get(2, 3);", "[test]");

    // TODO: Test index out of range for list subscripts

    // Test assignment to list index
    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); lst[0] = 'NewWord'; return lst;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    value = valueNode.getValue();
    assertTrue("Return value not ArrayList", value instanceof ArrayList);
    list = (ArrayList<Object>)value;
    assertEquals("Return value ArrayList size", 2, list.size());
    element = list.get(0);
    assertTrue("List element[0] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[0] string value", "NewWord", valueNode.getStringValue());
    element = list.get(1);
    assertTrue("List element[1] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[1] string value", "World", valueNode.getStringValue());
    runListScript("list lst; lst.add('Hello'); lst.add('World'); lst.put(0, 'NewWord'); return lst;", "[NewWord, World]");

    scriptNode = parser.parseScriptString("list lst; lst.add('Hello'); lst.add('World'); lst[1] = 'NewWord'; return lst;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("List  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
    value = valueNode.getValue();
    assertTrue("Return value not ArrayList", value instanceof ArrayList);
    list = (ArrayList<Object>)value;
    assertEquals("Return value ArrayList size", 2, list.size());
    element = list.get(0);
    assertTrue("List element[0] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[0] string value", "Hello", valueNode.getStringValue());
    element = list.get(1);
    assertTrue("List element[1] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("List element[1] string value", "NewWord", valueNode.getStringValue());
    runListScript("list lst; lst.add('Hello'); lst.add('World'); lst.put(1, 'NewWord'); return lst;", "[Hello, NewWord]");

    // Test simple map access
    scriptNode = parser.parseScriptString("map m; m.put('name', 'John Doe'); m.put('address', 'Here'); m.put('rank', 123); m.put('amount', 4.56); m.put('valid', true); return m;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
    value = valueNode.getValue();
    assertTrue("Return value not HashMap", value instanceof Map);
    Map<String, Value> map = (Map<String, Value>)value;
    assertEquals("Return value ArrayList size", 2, list.size());
    element = map.get("name");
    assertTrue("Map element['name'] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("Map element['name'] string value", "John Doe", valueNode.getStringValue());
    element = map.get("address");
    assertTrue("Map element['address'] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("Map element['address'] string value", "Here", valueNode.getStringValue());
    element = map.get("rank");
    assertTrue("Map element['rank'] is not an integer value:" + element.getClass().getSimpleName(), element instanceof IntegerValue);
    valueNode = (Value)element;
    assertEquals("Map element['rank'] integer value", 123, valueNode.getIntValue());
    element = map.get("amount");
    assertTrue("Map element['amount'] is not a float value:" + element.getClass().getSimpleName(), element instanceof FloatValue);
    valueNode = (Value)element;
    assertEquals("Map element['amount'] float value", 4.56, valueNode.getFloatValue(), 0.001);
    element = map.get("valid");
    assertTrue("Map element['valid'] is not a boolean value:" + element.getClass().getSimpleName(), element instanceof BooleanValue);
    valueNode = (Value)element;
    assertEquals("Map element['valid'] boolean value", true, valueNode.getBooleanValue());

    // Test subscripted map access
    scriptNode = parser.parseScriptString("map m; m['name'] = 'John Doe'; m['address'] = 'Here'; m['rank'] = 123; m['amount'] = 4.56; m['valid'] = true; return m;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
    value = valueNode.getValue();
    assertTrue("Return value not HashMap", value instanceof Map);
    map = (Map<String, Value>)value;
    assertEquals("Return value ArrayList size", 2, list.size());
    element = map.get("name");
    assertTrue("Map element['name'] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("Map element['name'] string value", "John Doe", valueNode.getStringValue());
    element = map.get("address");
    assertTrue("Map element['address'] is not a string value:" + element.getClass().getSimpleName(), element instanceof StringValue);
    valueNode = (Value)element;
    assertEquals("Map element['address'] string value", "Here", valueNode.getStringValue());
    element = map.get("rank");
    assertTrue("Map element['rank'] is not an integer value:" + element.getClass().getSimpleName(), element instanceof IntegerValue);
    valueNode = (Value)element;
    assertEquals("Map element['rank'] integer value", 123, valueNode.getIntValue());
    element = map.get("amount");
    assertTrue("Map element['amount'] is not a float value:" + element.getClass().getSimpleName(), element instanceof FloatValue);
    valueNode = (Value)element;
    assertEquals("Map element['amount'] float value", 4.56, valueNode.getFloatValue(), 0.001);
    element = map.get("valid");
    assertTrue("Map element['valid'] is not a boolean value:" + element.getClass().getSimpleName(), element instanceof BooleanValue);
    valueNode = (Value)element;
    assertEquals("Map element['valid'] boolean value", true, valueNode.getBooleanValue());

    scriptNode = parser.parseScriptString("map m; m['name'] = 'John Doe'; return m['name'];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "John Doe", valueNode.getStringValue());
    runStringScript("map m; m['name'] = 'John Doe'; return m['name'];", "John Doe");
    runStringScript("map m; m.put('name', 'John Doe'); return m.get('name');", "John Doe");
    runStringScript("map m; m.set('name', 'John Doe'); return m.get('name');", "John Doe");
    runStringScript("map m; m.add('name', 'John Doe'); return m.get('name');", "John Doe");
    runStringScript("map m; m.name = 'John Doe'; return m.name;", "John Doe");

    scriptNode = parser.parseScriptString("map m; m['name'] = 'John Doe'; return m['notfound'];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Null value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);
    runNullScript("map m; m.put('name', 'John Doe'); return m.get('notfound');");

    scriptNode = parser.parseScriptString("map m; m['name'] = 'John Doe'; return m['notfound'] == null;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("True value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof TrueValue);
    runBooleanScript("map m; m.put('name', 'John Doe'); return m.get('notfound') == null;", true);

    scriptNode = parser.parseScriptString("map m; m['name'] = 'John Doe'; return m['notfound'] != null;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("False  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FalseValue);
    runBooleanScript("map m; m.put('name', 'John Doe'); return m.get('notfound') != null;", false);

    scriptNode = parser.parseScriptString("map m; m['name'] = 'John Doe'; return m['name'] == null;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("False  value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FalseValue);
    runBooleanScript("map m; m.put('name', 'John Doe'); return m.get('name') == null;", false);
    runBooleanScript("map m; m.put('name', 'John Doe'); return m.name == null;", false);

    scriptNode = parser.parseScriptString("map m; m['name'] = 'John Doe'; return m['name'] != null;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("True value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof TrueValue);
    runBooleanScript("map m; m.put('name', 'John Doe'); return m.get('name') != null;", true);
    runBooleanScript("map m; m.put('name', 'John Doe'); return m.name != null;", true);

    // Test toString for map
    scriptNode = parser.parseScriptString("map m; m.put('name', 'John Doe'); m.put('address', 'Here'); m.put('rank', 123); m.put('amount', 4.56); m.put('valid', true); return m.toString;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "{name: John Doe, address: Here, rank: 123, amount: 4.56, valid: true}", valueNode.getStringValue());

    // Test toJson for map
    scriptNode = parser.parseScriptString("map m; m.put('name', 'John Doe'); m.put('address', 'Here'); m.put('rank', 123); m.put('amount', 4.56); m.put('valid', true); return m.toJson;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "{\"name\": \"John Doe\", \"address\": \"Here\", \"rank\": 123, \"amount\": 4.56, \"valid\": true}", valueNode.getStringValue());

    // Test map.keys
    scriptNode = parser.parseScriptString("map m; m.put('name', 'John Doe'); m.put('address', 'Here'); m.put('rank', 123); m.put('amount', 4.56); m.put('valid', true); return m.keys.toString;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "[name, address, rank, amount, valid]", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("map m; m.put('name', 'John Doe'); m.put('address', 'Here'); m.put('rank', 123); m.put('amount', 4.56); m.put('valid', true); return m.keys.toJson;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "[\"name\", \"address\", \"rank\", \"amount\", \"valid\"]", valueNode.getStringValue());

    // TODO: Test list of maps and map of lists

  }

  @Test
  public void testLiterals() throws Exception {
    // Test a 'list' literal
    {
      ScriptNode scriptNode = parser.parseScriptString("list lst = list(123, 456, 'Hello', 2.34, null, true); return lst;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("List value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
      ListValue listValueNode = (ListValue)valueNode;
      List<Value> valueList = listValueNode.value;
      int numElements = valueList.size();
      assertEquals("Number of list elements", 6, numElements);
      valueNode = valueList.get(0);
      assertTrue("List[0] value is not an Integer:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("List[0] integer value", 123, valueNode.getIntValue());
      valueNode = valueList.get(1);
      assertTrue("List[1] value is not an Integer:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("List[1] integer value", 456, valueNode.getIntValue());
      valueNode = valueList.get(2);
      assertTrue("List[2] value is not a string:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("List[2] string value", "Hello", valueNode.getStringValue());
      valueNode = valueList.get(3);
      assertTrue("List[3] value is not a float:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
      assertEquals("List[3] float value", 2.34, valueNode.getFloatValue(), 0.0001);
      valueNode = valueList.get(4);
      assertTrue("List[4] value is not a null:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);
      valueNode = valueList.get(5);
      assertTrue("List[5] value is not boolean true:" + valueNode.getClass().getSimpleName(), valueNode instanceof TrueValue);
    }

    // Test a 'map' literal
    {
      ScriptNode scriptNode = parser.parseScriptString("map m = map(company: 'Microsoft Corp.', ticker: 'MSFT', price: 30.66, own: true); return m;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode = (MapValue)valueNode;
      Map<String, Value> valueMap = mapValueNode.value;
      int numElements = valueMap.size();
      assertEquals("Number of list elements", 4, numElements);
      valueNode = valueMap.get("company");
      assertTrue("Company value is missing", valueNode != null);
      assertTrue("Company value is not a string:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Company integer value", "Microsoft Corp.", valueNode.getStringValue());
      valueNode = valueMap.get("ticker");
      assertTrue("Ticker value is missing", valueNode != null);
      assertTrue("Ticker value is not a string:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Ticker string value", "MSFT", valueNode.getStringValue());
      valueNode = valueMap.get("price");
      assertTrue("Price value is missing", valueNode != null);
      assertTrue("Price value is not a float:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
      assertEquals("Price float value", 30.66, valueNode.getFloatValue(), 0.0001);
      valueNode = valueMap.get("own");
      assertTrue("Own value is missing", valueNode != null);
      assertTrue("Own value is not boolean true:" + valueNode.getClass().getSimpleName(), valueNode instanceof TrueValue);

      scriptNode = parser.parseScriptString("map m = map(company: 'Microsoft Corp.', ticker: 'MSFT', price: 30.66, own: true); return m.json;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Return string value", "{\"company\": \"Microsoft Corp.\", \"ticker\": \"MSFT\", \"price\": 30.66, \"own\": true}", valueNode.getStringValue());

      // TODO: Why was this repeated? What should it be?
      scriptNode = parser.parseScriptString("map m = map(company= 'Microsoft Corp.', ticker= 'MSFT', price= 30.66, own= true); return m.json;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Return string value", "{\"company\": \"Microsoft Corp.\", \"ticker\": \"MSFT\", \"price\": 30.66, \"own\": true}", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("map m = map(company= 'Microsoft Corp.', ticker= 'MSFT', price= 30.66, own= true); m['extra']= 'Maybe'; return m.json;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Return string value", "{\"company\": \"Microsoft Corp.\", \"ticker\": \"MSFT\", \"price\": 30.66, \"own\": true, \"extra\": \"Maybe\"}", valueNode.getStringValue());

    }

    // Test a 'list' literal using square brackets rather than the 'list' type keyword
    {
      ScriptNode scriptNode = parser.parseScriptString("list lst = [123, 456, 'Hello', 2.34, null, true]; return lst;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("List value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
      ListValue listValueNode = (ListValue)valueNode;
      List<Value> valueList = listValueNode.value;
      int numElements = valueList.size();
      assertEquals("Number of list elements", 6, numElements);
      valueNode = valueList.get(0);
      assertTrue("List[0] value is not an Integer:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("List[0] integer value", 123, valueNode.getIntValue());
      valueNode = valueList.get(1);
      assertTrue("List[1] value is not an Integer:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("List[1] integer value", 456, valueNode.getIntValue());
      valueNode = valueList.get(2);
      assertTrue("List[2] value is not a string:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("List[2] string value", "Hello", valueNode.getStringValue());
      valueNode = valueList.get(3);
      assertTrue("List[3] value is not a float:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
      assertEquals("List[3] float value", 2.34, valueNode.getFloatValue(), 0.0001);
      valueNode = valueList.get(4);
      assertTrue("List[4] value is not a null:" + valueNode.getClass().getSimpleName(), valueNode instanceof NullValue);
      valueNode = valueList.get(5);
      assertTrue("List[5] value is not boolean true:" + valueNode.getClass().getSimpleName(), valueNode instanceof TrueValue);
    }

    // Test a 'map' literal using braces rather than the 'map' type keyword
    {
      ScriptNode scriptNode = parser.parseScriptString("map m = {company: 'Microsoft Corp.', ticker: 'MSFT', price: 30.66, own: true}; return m;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode = (MapValue)valueNode;
      Map<String, Value> valueMap = mapValueNode.value;
      int numElements = valueMap.size();
      assertEquals("Number of list elements", 4, numElements);
      valueNode = valueMap.get("company");
      assertTrue("Company value is missing", valueNode != null);
      assertTrue("Company value is not a string:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Company integer value", "Microsoft Corp.", valueNode.getStringValue());
      valueNode = valueMap.get("ticker");
      assertTrue("Ticker value is missing", valueNode != null);
      assertTrue("Ticker value is not a string:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Ticker string value", "MSFT", valueNode.getStringValue());
      valueNode = valueMap.get("price");
      assertTrue("Price value is missing", valueNode != null);
      assertTrue("Price value is not a float:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
      assertEquals("Price float value", 30.66, valueNode.getFloatValue(), 0.0001);
      valueNode = valueMap.get("own");
      assertTrue("Own value is missing", valueNode != null);
      assertTrue("Own value is not boolean true:" + valueNode.getClass().getSimpleName(), valueNode instanceof TrueValue);

      scriptNode = parser.parseScriptString("map m = map(company: 'Microsoft Corp.', ticker: 'MSFT', price: 30.66, own: true); return m.json;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Return string value", "{\"company\": \"Microsoft Corp.\", \"ticker\": \"MSFT\", \"price\": 30.66, \"own\": true}", valueNode.getStringValue());

      // TODO: Why was this repeated? What should it be?
      scriptNode = parser.parseScriptString("map m = map(company= 'Microsoft Corp.', ticker= 'MSFT', price= 30.66, own= true); return m.json;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Return string value", "{\"company\": \"Microsoft Corp.\", \"ticker\": \"MSFT\", \"price\": 30.66, \"own\": true}", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("map m = map(company= 'Microsoft Corp.', ticker= 'MSFT', price= 30.66, own= true); m['extra']= 'Maybe'; return m.json;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Return string value", "{\"company\": \"Microsoft Corp.\", \"ticker\": \"MSFT\", \"price\": 30.66, \"own\": true, \"extra\": \"Maybe\"}", valueNode.getStringValue());
    }

  }

  @Test
  public void testXml() throws Exception {
    // Test parsing of Reuters "Most Read Stories" XML RSS web page
    {

      ScriptNode scriptNode = parser.parseScriptString("web w; return w.get('http://feeds.reuters.com/reuters/MostRead?format=xml').xml;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode = (MapValue)valueNode;
      Map<String, Value> map = mapValueNode.value;
      assertTrue("Map value is null", map != null);
      int numElements = map.size();
      //        assertEquals("Number of map elements", 1, numElements);
      valueNode = map.get("rss");
      assertTrue("rss value is null", valueNode != null);
      assertTrue("rss is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      mapValueNode = (MapValue)valueNode;
      map = mapValueNode.value;
      assertTrue("Map value is null", map != null);
      valueNode = map.get("channel");
      assertTrue("channel value is null", valueNode != null);
      assertTrue("channel is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      mapValueNode = (MapValue)valueNode;
      map = mapValueNode.value;
      assertTrue("Map value is null", map != null);
      valueNode = map.get("item");
      assertTrue("item value is null", valueNode != null);
      assertTrue("item is not a list: " + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
      ListValue listValueNode = (ListValue)valueNode;
      List<Value> elements = listValueNode.value;
      assertTrue("elements value is null", elements != null);
      numElements = elements.size();
      assertEquals("Number of list elements", 10, numElements);
      for (int i = 0; i < numElements; i++){
        valueNode = elements.get(i);
        assertTrue("item element is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
        mapValueNode = (MapValue)valueNode;
        map = mapValueNode.value;
        assertTrue("Map value is null", map != null);
        int numMapElements = map.size();
        assertEquals("item list element map size", 7, numMapElements);
        valueNode = map.get("guid");
        assertTrue("guid value is null", valueNode != null);
        assertTrue("guid is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
        MapValue mapValueNode2a = (MapValue)valueNode;
        Map<String, Value> map2a = mapValueNode2a.value;
        assertTrue("Map2a value is null", map != null);
        int numMap2aElements = map2a.size();
        assertEquals("guid map size", 2, numMap2aElements);
        valueNode = map2a.get("isPermaLink");
        assertTrue("isPermaLink value is null", valueNode != null);
        assertTrue("isPermaLink is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
        String valueString = valueNode.getStringValue();
        assertEquals("isPermaLink value", "false", valueString);
        valueNode = map2a.get("text_1");
        valueString = valueNode.getStringValue();
        int len = valueString.length();
        assertTrue("guid string is too short: " + len, len > 50);
        String prefixString = valueString.substring(0, 31);
        assertEquals("guid string", "http://www.reuters.com/article/", prefixString);
        valueNode = map.get("pubDate");
        assertTrue("pubDate value is null", valueNode != null);
        assertTrue("pubDate is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
        valueString = valueNode.getStringValue();
        len = valueString.length();
        assertTrue("pubDate string is too short: " + len, len > 20);
        valueNode = map.get("category");
        assertTrue("category value is null", valueNode != null);
        assertTrue("category is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
        valueString = valueNode.getStringValue();
        len = valueString.length();
        assertTrue("category string is too short: " + len, len > 3);
        valueNode = map.get("title");
        assertTrue("title value is null", valueNode != null);
        assertTrue("title is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
        valueString = valueNode.getStringValue();
        len = valueString.length();
        assertTrue("title string is too short: " + len, len > 15);
        valueNode = map.get("description");
        assertTrue("description value is null", valueNode != null);
        assertTrue("description is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
        valueString = valueNode.getStringValue();
        len = valueString.length();
        assertTrue("description string is too short: " + len, len > 50);
        valueNode = map.get("feedburner:origLink");
        assertTrue("feedburner:origLink value is null", valueNode != null);
        assertTrue("feedburner:origLink is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
        valueString = valueNode.getStringValue();
        len = valueString.length();
        assertTrue("feedburner:origLink string is too short: " + len, len > 50);
        prefixString = valueString.substring(0, 30);
        assertEquals("feedburner:origLink string", "http://www.reuters.com/article", prefixString);
        valueNode = map.get("link");
        assertTrue("link value is null", valueNode != null);
        assertTrue("link is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
        valueString = valueNode.getStringValue();
        len = valueString.length();
        assertTrue("link string is too short: " + len, len > 50);
        prefixString = valueString.substring(0, 30);
        assertEquals("link string", "http://feeds.reuters.com/~r/re", prefixString);
      }
    }

    // Test parsing of NY Fed Daily Fed Funds rate XML RSS web page
    {

      ScriptNode scriptNode = parser.parseScriptString("web w; return w.get('http://www.newyorkfed.org/rss/feeds/fedfunds.xml').xml;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue topMapValueNode = (MapValue)valueNode;
      Map<String, Value> topMap = topMapValueNode.value;
      assertTrue("topMap value is null", topMap != null);
      int numTopElements = topMap.size();
      assertEquals("Number of map elements", 1, numTopElements);
      valueNode = topMap.get("rdf:RDF");
      assertTrue("rdf:RDF value is null", valueNode != null);
      assertTrue("rdf:RDF is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode = (MapValue)valueNode;
      Map<String, Value> map = mapValueNode.value;
      assertTrue("Map value is null", map != null);
      int numElements = map.size();
      assertEquals("Number of map elements", 10, numElements);
      valueNode = map.get("xmlns");
      assertTrue("xmlns value is null", valueNode != null);
      assertTrue("xmlns is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      String valueString = valueNode.getStringValue();
      assertEquals("xmlns string", "http://purl.org/rss/1.0/", valueString);
      valueNode = map.get("xmlns:dc");
      assertTrue("xmlns:dc value is null", valueNode != null);
      assertTrue("xmlns:dc is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("xmlns:dc string", "http://purl.org/dc/elements/1.1/", valueString);
      valueNode = map.get("xmlns:dcterms");
      assertTrue("xmlns:dcterms value is null", valueNode != null);
      assertTrue("xmlns:dcterms is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("xmlns:dcterms string", "http://purl.org/dc/terms/", valueString);
      valueNode = map.get("xmlns:cb");
      assertTrue("xmlns:cb value is null", valueNode != null);
      assertTrue("xmlns:cb is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("xmlns:cb string", "http://www.cbwiki.net/wiki/index.php/Specification_1.1", valueString);
      valueNode = map.get("xmlns:nyfed");
      assertTrue("xmlns:nyfed value is null", valueNode != null);
      assertTrue("xmlns:nyfed is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("xmlns:nyfed string", "http://www.newyorkfed.org/xml/schemas/metadata/", valueString);
      valueNode = map.get("xmlns:xsi");
      assertTrue("xmlns:xsi value is null", valueNode != null);
      assertTrue("xmlns:xsi is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("xmlns:xsi string", "http://www.w3.org/2001/XMLSchema-instance", valueString);
      valueNode = map.get("xsi:schemaLocation");
      assertTrue("xsi:schemaLocation value is null", valueNode != null);
      assertTrue("xsi:schemaLocation is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("xsi:schemaLocation string", "http://www.w3.org/1999/02/22-rdf-syntax-ns# rdf.xsd", valueString);
      valueNode = map.get("xmlns:rdf");
      assertTrue("xmlns:rdf value is null", valueNode != null);
      assertTrue("xmlns:rdf is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("xmlns:rdf string", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", valueString);
      valueNode = map.get("channel");
      assertTrue("channel value is null", valueNode != null);
      assertTrue("channel is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode1 = (MapValue)valueNode;
      Map<String, Value> map1 = mapValueNode1.value;
      assertTrue("Map1 value is null", map1 != null);
      int numMap1Elements = map1.size();
      assertEquals("channel element map size", 7, numMap1Elements);
      valueNode = map1.get("rdf:about");
      assertTrue("rdf:about value is null", valueNode != null);
      assertTrue("rdf:about is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("rdf:about string", "http://www.newyorkfed.org/rss/feeds/fedfunds.xml", valueString);
      valueNode = map1.get("title");
      assertTrue("title value is null", valueNode != null);
      assertTrue("title is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("title string", "NY Fed | Fed Funds", valueString);
      valueNode = map1.get("link");
      assertTrue("link value is null", valueNode != null);
      assertTrue("link is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("link string", "http://newyorkfed.org/rss/index.html", valueString);
      valueNode = map1.get("description");
      assertTrue("description value is null", valueNode != null);
      assertTrue("description is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("description string", "The federal funds rate is the interest rate at which depository institutions lend balances to each other overnight.", valueString.trim());
      valueNode = map1.get("dc:publisher");
      assertTrue("dc:publisher value is null", valueNode != null);
      assertTrue("dc:publisher is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("dc:publisher string", "Federal Reserve Bank of New York", valueString);
      valueNode = map1.get("dcterms:license");
      assertTrue("dcterms:license value is null", valueNode != null);
      assertTrue("dcterms:license is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      assertEquals("dcterms:license string", "http://www.newyorkfed.org/markets/omo/dmm/fedfundsdata.cfm#disclaimer", valueString.trim());
      valueNode = map1.get("items");
      assertTrue("items value is null", valueNode != null);
      assertTrue("items is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode1a = (MapValue)valueNode;
      Map<String, Value> map1a = mapValueNode1a.value;
      assertTrue("Map1a value is null", map1a != null);
      int numMap1aElements = map1a.size();
      assertEquals("channel items element map size", 1, numMap1aElements);
      valueNode = map1a.get("rdf:Seq");
      assertTrue("rdf:Seq value is null", valueNode != null);
      assertTrue("rdf:Seq is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode1b = (MapValue)valueNode;
      Map<String, Value> map1b = mapValueNode1b.value;
      assertTrue("Map1b value is null", map1b != null);
      int numMap1bElements = map1b.size();
      assertEquals("rdf:Seq items element map size", 1, numMap1bElements);
      valueNode = map1b.get("rdf:li");
      assertTrue("rdf.li is null", valueNode != null);
      assertTrue("rdf.li is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode1c = (MapValue)valueNode;
      Map<String, Value> map1c = mapValueNode1c.value;
      assertTrue("Map1c value is null", map1c != null);
      int numMap1cElements = map1c.size();
      assertEquals("rdf:li items element map size", 1, numMap1cElements);
      valueNode = map1c.get("rdf:resource");
      assertTrue("rdf:resource is null", valueNode != null);
      assertTrue("rdf:resource is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      StringValue stringValueNode = (StringValue)valueNode;
      valueString = stringValueNode.getStringValue();
      String itemRdfResourceString = valueString;
      int len = valueString.length();
      String prefixString = valueString.substring(0, len < 59 ? len : 59);
      assertEquals("rdf:resource string prefix", "http://www.newyorkfed.org/markets/omo/dmm/fedfundsdata.cfm/", prefixString);
      valueNode = map.get("item");
      assertTrue("item value is null", valueNode != null);
      assertTrue("item is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      mapValueNode = (MapValue)valueNode;
      map = mapValueNode.value;
      assertTrue("Map value is null", map != null);
      int numMapElements = map.size();
      assertEquals("item list element map size", 7, numMapElements);
      valueNode = map.get("rdf:about");
      assertTrue("rdf:about value is null", valueNode != null);
      assertTrue("rdf:about is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      len = valueString.length();
      assertTrue("rdf:about string is too short or too long: " + len, len == 69);
      String noDateString = valueString.substring(0, len - 10);
      assertEquals("rdf:about string", "http://www.newyorkfed.org/markets/omo/dmm/fedfundsdata.cfm/", noDateString);
      assertEquals("rdf:about in item and rdf:resource in rdf:li", itemRdfResourceString, valueString);
      valueNode = map.get("dc:date");
      assertTrue("dc:date value is null", valueNode != null);
      assertTrue("dc:date is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      len = valueString.length();
      assertTrue("dc:date string is too short or too long: " + len, len > 10 && len < 50);
      valueNode = map.get("dc:language");
      assertTrue("dc:language value is null", valueNode != null);
      assertTrue("dc:language is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      len = valueString.length();
      assertEquals("dc:language string length", 2, len);
      assertEquals("dc:language string", "en", valueString);
      valueNode = map.get("title");
      assertTrue("title value is null", valueNode != null);
      assertTrue("title is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      len = valueString.length();
      assertTrue("title string is too short: " + len, len > 15);
      valueNode = map.get("description");
      assertTrue("description value is null", valueNode != null);
      assertTrue("description is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode2a = (MapValue)valueNode;
      Map<String, Value> map2a = mapValueNode2a.value;
      assertTrue("Map2a value is null", map != null);
      int numMap2aElements = map2a.size();
      assertEquals("description map size", 2, numMap2aElements);
      valueNode = map2a.get("xml:lang");
      assertTrue("xml:lang value is null", valueNode != null);
      assertTrue("xml:lang is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("xml:lang string", "en", valueNode.getStringValue());
      valueNode = map2a.get("text_1");
      valueString = valueNode.getStringValue();
      len = valueString.length();
      assertTrue("description text string is too short: " + len, len > 50);
      valueNode = map.get("link");
      assertTrue("link value is null", valueNode != null);
      assertTrue("link is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      valueString = valueNode.getStringValue();
      len = valueString.length();
      assertTrue("link string is too short: " + len, len > 50);
      prefixString = valueString.substring(0, 30);
      assertEquals("link string", "http://www.newyorkfed.org/mark", prefixString);
      valueNode = map.get("cb:statistics");
      assertTrue("cb:statistics value is null", valueNode != null);
      assertTrue("cb:statistics is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode2 = (MapValue)valueNode;
      Map<String, Value> map2 = mapValueNode2.value;
      assertTrue("Map2 value is null", map != null);
      int numMap2Elements = map2.size();
      assertEquals("cb:statistics map size", 3, numMap2Elements);
      valueNode = map2.get("cb:country");
      assertTrue("cb:country value is null", valueNode != null);
      assertTrue("cb:country is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("cb:country string", "US", valueNode.getStringValue());
      valueNode = map2.get("cb:institutionAbbrev");
      assertTrue("cb:institutionAbbrev value is null", valueNode != null);
      assertTrue("cb:institutionAbbrev is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("cb:institutionAbbrev string", "NYFed", valueNode.getStringValue());
      valueNode = map2.get("cb:interestRate");
      assertTrue("cb:interestRate value is null", valueNode != null);
      assertTrue("cb:interestRate is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      MapValue mapValueNode3 = (MapValue)valueNode;
      Map<String, Value> map3 = mapValueNode3.value;
      assertTrue("Map3 value is null", map != null);
      int numMap3Elements = map3.size();
      assertEquals("cb:interestRate map size", 2, numMap3Elements);
      valueNode = map3.get("cb:rateType");
      assertTrue("cb:rateType value is null", valueNode != null);
      assertTrue("cb:rateType is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("cb:rateType string", "daily fed funds effective", valueNode.getStringValue());
      valueNode = map3.get("cb:value");
      assertTrue("cb:value value is null", valueNode != null);
      assertTrue("cb:value is not a map: " + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      mapValueNode2a = (MapValue)valueNode;
      map2a = mapValueNode2a.value;
      assertTrue("Map2a value is null", map != null);
      numMap2aElements = map2a.size();
      assertEquals("cb:value map size", 3, numMap2aElements);
      valueNode = map2a.get("frequency");
      assertTrue("frequency value is null", valueNode != null);
      assertTrue("frequency is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("frequency string", "daily", valueNode.getStringValue());
      valueNode = map2a.get("decimal");
      assertTrue("decimal value is null", valueNode != null);
      assertTrue("decimal is not a string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("decimal string", "2", valueNode.getStringValue());
      valueNode = map2a.get("text_1");
      valueString = valueNode.getStringValue();
      assertTrue("cb:value does not contain a dot: '" + valueString + "'", valueString.contains("."));
      double rate = Double.parseDouble(valueString);
      assertTrue("cb:value not within range of 0.03 to 1.00: " + rate, rate >= 0.03 && rate <= 1.0);

    }

    // Test accessing a single simple string field of parsed NY Fed Daily Fed Funds rate XML RSS web page
    {

      ScriptNode scriptNode = parser.parseScriptString("web w; return w.get('http://www.newyorkfed.org/rss/feeds/fedfunds.xml').xml['rdf:RDF']['channel']['description'];");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Description string prefix", "The federal funds rate is the interest rate at which depository institutions lend balances to each other overnight.", valueNode.getStringValue());
    }

    // Test accessing a single string field (which has attributes) of parsed NY Fed Daily Fed Funds rate XML RSS web page
    {

      ScriptNode scriptNode = parser.parseScriptString("web w; return w.get('http://www.newyorkfed.org/rss/feeds/fedfunds.xml').xml['rdf:RDF']['item']['description']['text_1'];");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      StringValue stringValueNode = (StringValue)valueNode;
      String valueString = stringValueNode.getStringValue();
      int len = valueString.length();
      String prefixString = valueString.substring(0, len < 38 ? len : 38);
      assertEquals("Description string prefix", "daily federal funds effective rate for", prefixString);
    }

    // Test accessing a single float field of parsed NY Fed Daily Fed Funds rate XML RSS web page
    {

      ScriptNode scriptNode = parser.parseScriptString("web w; return w.get('http://www.newyorkfed.org/rss/feeds/fedfunds.xml').xml['rdf:RDF']['item']['cb:statistics']['cb:interestRate']['cb:value']['text_1'].float;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
      FloatValue floatValueNode = (FloatValue)valueNode;
      double rate = floatValueNode.value;
      assertTrue("cb:value not within range of 0.03 to 1.00: " + rate, rate >= 0.03 && rate <= 1.0);
    }

    // Test parse HTML web page for price of gold

  }

  @Test
  public void testStringMethods() throws Exception {
    // Test the string 'matches' method
    {
      ScriptNode scriptNode = parser.parseScriptString("return 'abc'.matches('xyz');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", false, valueNode.getBooleanValue());

      scriptNode = parser.parseScriptString("return 'abc'.matches('abc');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", true, valueNode.getBooleanValue());

      scriptNode = parser.parseScriptString("return 'abc'.matches('ab');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", false, valueNode.getBooleanValue());

      scriptNode = parser.parseScriptString("return 'abc'.matches('bc');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", false, valueNode.getBooleanValue());

      scriptNode = parser.parseScriptString("return 'abc'.matches('abcd');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", false, valueNode.getBooleanValue());

      scriptNode = parser.parseScriptString("return 'abc'.matches('.*');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", true, valueNode.getBooleanValue());

      scriptNode = parser.parseScriptString("return 'abc'.matches('...');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", true, valueNode.getBooleanValue());

      scriptNode = parser.parseScriptString("return 'abc'.matches('....');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", false, valueNode.getBooleanValue());

      scriptNode = parser.parseScriptString("return 'abc'.matches('\\\\w\\\\w\\\\w');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", true, valueNode.getBooleanValue());

      scriptNode = parser.parseScriptString("return 'abc'.matches('\\\\d\\\\d\\\\d');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Boolean value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof BooleanValue);
      assertEquals("Boolean result of matches", false, valueNode.getBooleanValue());
    }

    // Test the string 'split' method
    {
      ScriptNode scriptNode = parser.parseScriptString("return ''.split(',');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("List value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
      ListValue listValueNode = (ListValue)valueNode;
      List<Value> stringList = listValueNode.value;
      assertEquals("Number of strings returned", 0, stringList.size());

      scriptNode = parser.parseScriptString("return 'abcdefghi'.split('');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("List value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
      listValueNode = (ListValue)valueNode;
      stringList = listValueNode.value;
      assertEquals("Number of strings returned", 1, stringList.size());
      assertEquals("String value", "abcdefghi", stringList.get(0).getStringValue());

      scriptNode = parser.parseScriptString("return 'abcdefghi'.split(',');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("List value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
      listValueNode = (ListValue)valueNode;
      stringList = listValueNode.value;
      assertEquals("Number of strings returned", 1, stringList.size());
      assertEquals("String value", "abcdefghi", stringList.get(0).getStringValue());

      scriptNode = parser.parseScriptString("return 'abc,defghi'.split(',');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("List value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
      listValueNode = (ListValue)valueNode;
      stringList = listValueNode.value;
      assertEquals("Number of strings returned", 2, stringList.size());
      assertEquals("String value", "abc", stringList.get(0).getStringValue());
      assertEquals("String value", "defghi", stringList.get(1).getStringValue());

      scriptNode = parser.parseScriptString("return 'abc,def,ghi'.split(',');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("List value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
      listValueNode = (ListValue)valueNode;
      stringList = listValueNode.value;
      assertEquals("Number of strings returned", 3, stringList.size());
      assertEquals("String value", "abc", stringList.get(0).getStringValue());
      assertEquals("String value", "def", stringList.get(1).getStringValue());
      assertEquals("String value", "ghi", stringList.get(2).getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.split('-\\\\d*-');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("List value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValue);
      listValueNode = (ListValue)valueNode;
      stringList = listValueNode.value;
      assertEquals("Number of strings returned", 3, stringList.size());
      assertEquals("String value", "abc", stringList.get(0).getStringValue());
      assertEquals("String value", "def", stringList.get(1).getStringValue());
      assertEquals("String value", "ghi", stringList.get(2).getStringValue());

    }

    // Test the string 'afterRegex' method
    {
      ScriptNode scriptNode = parser.parseScriptString("return ''.afterRegex('');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc'.afterRegex('');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "abc", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.afterRegex('-\\\\d*-');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "def-45-ghi", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.afterRegex('-\\\\d*-', 2);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "def-45-ghi", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.afterRegex('-\\\\d*-', 4);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "ghi", valueNode.getStringValue());

    }

    // Test the string 'beforeRegex' method
    {
      ScriptNode scriptNode = parser.parseScriptString("return ''.beforeRegex('');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc'.beforeRegex('');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.beforeRegex('-\\\\d*-');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "abc", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.beforeRegex('-\\\\d*-', 2);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "abc", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.beforeRegex('-\\\\d*-', 4);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "abc-123-def", valueNode.getStringValue());

    }

    // Test the string 'betweenRegex' method
    {
      ScriptNode scriptNode = parser.parseScriptString("return ''.betweenRegex('', '');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc'.betweenRegex('', '');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.betweenRegex('-\\\\d*-', '-\\\\d*-');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "def", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.betweenRegex('-\\\\d*-', '-\\\\d*-', 2);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "def", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.betweenRegex('-\\\\d*-', '-\\\\d*-', 4);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("String result", "", valueNode.getStringValue());

    }

    // Test the string 'indexOfRegex' method
    {
      ScriptNode scriptNode = parser.parseScriptString("return ''.indexOfRegex('');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", -1, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("return 'abc'.indexOfRegex('');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", -1, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.indexOfRegex('-\\\\d*-');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", 3, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.indexOfRegex('-\\\\d*-', 2);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", 3, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.indexOfRegex('-\\\\d*-', 4);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", 11, valueNode.getIntValue());

    }

    // Test the string 'endIndexOfRegex' method
    {
      ScriptNode scriptNode = parser.parseScriptString("return ''.endIndexOfRegex('');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", -1, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("return 'abc'.endIndexOfRegex('');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", -1, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.endIndexOfRegex('-\\\\d*-');");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", 8, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.endIndexOfRegex('-\\\\d*-', 2);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", 8, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("return 'abc-123-def-45-ghi'.endIndexOfRegex('-\\\\d*-', 4);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
      assertEquals("Integer result", 15, valueNode.getIntValue());

    }

  }


  @Test
  public void testWebAccess() throws Exception {
    // Set throttling down to speed tests
    agentServer.webAccessConfig.setMinimumWebAccessInterval(10);
    agentServer.webAccessConfig.setMinimumWebPageRefreshInterval(10);
    agentServer.webAccessConfig.setMinimumWebSiteAccessInterval(10);
    agentServer.webAccessConfig.setDefaultWebPageRefreshInterval(10);
    
    // Test 'web' to fetch the text of a web page
    /*      scriptNode = parser.parseScriptString("web w; string s = w.get('http://twitter.com'); return s.substr(0, 100);");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValueNode);
      assertEquals("Return string value", "<!DOCTYPE html>\n<html data-nav-highlight-class-name=\"highlight-global-nav-home\">\n  <head>\n    \n    <", valueNode.getStringValue());

      scriptNode = parser.parseScriptString("web w; string s = w.get('http://twitter.com'); return w.statusCode;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValueNode);
      assertEquals("Return integer value", 200, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("web w; string s = w.get('http://twitter.com'); return w.reasonPhrase;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValueNode);
      assertEquals("Return string value", "OK", valueNode.getStringValue());

      // Test fetch of Twitter public timeline in JSON format
      scriptNode = parser.parseScriptString("web w; string s = w.get('https://api.twitter.com/1/statuses/public_timeline.json?count=1'); return s.json;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("List value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof ListValueNode);

      scriptNode = parser.parseScriptString("web w; string s = w.get('https://api.twitter.com/1/statuses/public_timeline.json?count=1'); return s.json.size;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValueNode);
      assertEquals("Return integer value", 20, valueNode.getIntValue());

      scriptNode = parser.parseScriptString("web w; string s = w.get('https://api.twitter.com/1/statuses/public_timeline.json?count=1'); return s.json[0];");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValueNode);

      scriptNode = parser.parseScriptString("web w; string s = w.get('https://api.twitter.com/1/statuses/public_timeline.json?count=1'); return s.json[0]['user']['name'];");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValueNode);
      //assertEquals("Return string value", "OK", valueNode.getStringValue());
     */
    // Test fetch of the Twitter API help test page
    ScriptNode scriptNode = parser.parseScriptString("web w; string s = w.get('https://api.twitter.com/1/help/test.json'); return s;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "\"ok\"", valueNode.getStringValue());

    // Test extraction of stock quote from a Yahoo finance web page
    scriptNode = parser.parseScriptString("web w; string s = w.get('http://finance.yahoo.com/q?s=msft'); string companyName = s.between('\"title\"><h2>', '</h2>'); string price = s.between('<span id=\"yfs_l84_msft\">', '</span>'); return 'Company: ' + companyName;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "Company: Microsoft Corporation (MSFT)", valueNode.getStringValue());

    // Test extraction of stock quote from a NASDAQ web page
    boolean doNasdaq = true;
    if (doNasdaq){
      scriptNode = parser.parseScriptString("web w; string s = w.get('http://www.nasdaq.com/symbol/msft'); string companyName = s.between('<div id=\"sq_symbol-info\" class=\"symbolinfo\">MSFT', '</a>').after('\">'); string price = s.between('<label id=\\'MSFT_LastSale1\\'>$&nbsp;', '</label>'); return 'Company: ' + companyName;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Return string value", "Company: Microsoft Corporation", valueNode.getStringValue());
    }

    // Test extraction of stock quote from a Bloomberg web page
    scriptNode = parser.parseScriptString("web w; string s = w.get('http://www.bloomberg.com/quote/MSFT%3AUS'); string companyName = s.after('<div class=\"ticker_header_top clearfix\">').between('<h2>', '</h2>'); string price = s.between('<span class=\"ticker_data\">', '</span>'); return 'Company: ' + companyName;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "Company: Microsoft Corp", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("web w; string s = w.get('http://www.bloomberg.com/quote/MSFT%3AUS'); string companyName = s.after('<div class=\"ticker_header_top clearfix\">').between('<h2>', '</h2>'); float price = s.between('<span class=\"ticker_data\">', '</span>').float; map m; m['company'] = companyName; m['price'] = price; return m;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
    MapValue mapValueNode = (MapValue)valueNode;
    Map<String, Value> map = mapValueNode.value;
    assertTrue("Map value is null", map != null);
    int numElements = map.size();
    assertEquals("Number of map elements", 2, numElements);
    valueNode = map.get("company");
    assertTrue("Company value is null", valueNode != null);
    assertTrue("Company name is not string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    StringValue stringValueNode = (StringValue)valueNode;
    String companyName = stringValueNode.getStringValue();
    assertEquals("Company name string value", "Microsoft Corp", companyName);
    valueNode = map.get("price");
    assertTrue("Price value is null", valueNode != null);
    assertTrue("Price is not float: " + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    FloatValue floatValueNode = (FloatValue)valueNode;
    double price = floatValueNode.value;
    assertTrue("Price not in range 20.0 to 40.0: " + price, price > 20.0 && price < 40.0);

    // Need to disable admin throttling so we can access a second Yahoo page quickly
    AgentServer.getSingleton().webAccessConfig.setMinimumWebSiteAccessInterval(0);

    /* Oops... Yahoo reorged page - no more gold quote!
    // Test extraction of gold quote from Yahoo finance web page
    scriptNode = parser.parseScriptString("web w; return w.get('http://finance.yahoo.com/').between('<span id=\"yfs_l10_gcj12.cmx\" class=\"l10\">', '</span>').float;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Float value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof FloatValue);
    double goldPrice = valueNode.getFloatValue();
    assertTrue("Price of gold is not in range 1,000 to 2,500: " + goldPrice, goldPrice > 1000.00 && goldPrice < 2500.00);

    
    // Test extraction of integer gold quote from Yahoo finance web page
    scriptNode = parser.parseScriptString("web w; return w.get('http://finance.yahoo.com/').between('<span id=\"yfs_l10_gcj12.cmx\" class=\"l10\">', '</span>').int;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Integer value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof IntegerValue);
    long goldPriceInt = valueNode.getIntValue();
    assertTrue("Price of gold is not in range 1,000 to 2,500: " + goldPriceInt, goldPriceInt > 1000 && goldPriceInt < 2500);
*/
    // Test extraction of info from weather page for my zip code
    scriptNode = parser.parseScriptString("web w; string s = w.get('http://www.weather.com/weather/today/10022'); string city = s.between('<h1 id=\"twc_loc_head\">', ' <nobr>'); string weather = s.between('class=\"twc-col-1\">', '\\n'); return 'City: ' + city;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "City: New York, NY (10022)", valueNode.getStringValue());

    // Test fetch of Twitter TOS (Terms of Service) as JSON
    scriptNode = parser.parseScriptString("web w; return w.get('https://api.twitter.com/1/legal/tos.json').json;");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
    mapValueNode = (MapValue)valueNode;
    map = mapValueNode.value;
    assertTrue("Map value is null", map != null);
    numElements = map.size();
    assertEquals("Number of map elements", 1, numElements);
    valueNode = map.get("tos");
    assertTrue("tos value is null", valueNode != null);
    assertTrue("tos is not string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    stringValueNode = (StringValue)valueNode;
    String tos = stringValueNode.getStringValue();
    String tosExpected = "Terms of Service\n\n\nThese Terms of Service (\"Terms\") govern your access";
    String tosStart = tos.substring(0, tosExpected.length());
    assertEquals("TOS start", tosExpected, tosStart);

    /* TODO: WTF - Twitter no longer supports English??
    // Test using Twitter to lookup country names and codes
    scriptNode = parser.parseScriptString("web w; return w.get('https://api.twitter.com/1/help/languages.json').json['name', 'English']['code'];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "en", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("web w; return w.get('https://api.twitter.com/1/help/languages.json').json['code', 'en']['name'];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "English", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("web w; return w.get('https://api.twitter.com/1/help/languages.json').json['code', 'en']['status'];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    assertEquals("Return string value", "production", valueNode.getStringValue());

    scriptNode = parser.parseScriptString("web w; return w.get('https://api.twitter.com/1/help/languages.json').json['code', 'en'];");
    assertTrue("Null was returned from script parser", scriptNode != null);
    valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
    assertTrue("No value node returned from executor", valueNode != null);
    assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
    mapValueNode = (MapValue)valueNode;
    map = mapValueNode.value;
    assertTrue("Map value is null", map != null);
    numElements = map.size();
    assertEquals("Number of map elements", 3, numElements);
    valueNode = map.get("name");
    assertTrue("name value is null", valueNode != null);
    assertTrue("name is not string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    stringValueNode = (StringValue)valueNode;
    String name = stringValueNode.getStringValue();
    assertEquals("name string value", "English", name);
    valueNode = map.get("code");
    assertTrue("code value is null", valueNode != null);
    assertTrue("code is not string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    stringValueNode = (StringValue)valueNode;
    String code = stringValueNode.getStringValue();
    assertEquals("code string value", "en", code);
    valueNode = map.get("status");
    assertTrue("status value is null", valueNode != null);
    assertTrue("status is not string: " + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
    stringValueNode = (StringValue)valueNode;
    String status = stringValueNode.getStringValue();
    assertEquals("status string value", "production", status);
*/
    // Can re-enable admin throttling
    AgentServer.getSingleton().webAccessConfig.setMinimumWebSiteAccessInterval(-1);

  }


  @Test
  public void testMapAccess() throws Exception {
    // Test read and write of map keys using subscripted field names
    {
      ScriptNode scriptNode = parser.parseScriptString("map m; m['company'] = 'Microsoft Corp'; m['ticker'] = 'MSFT'; m['price'] = 31.6604; m['own'] = true; return m;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      assertEquals("Map value", "{company: Microsoft Corp, ticker: MSFT, price: 31.6604, own: true}", valueNode.toString());
      assertEquals("Map value JSON", "{\"company\": \"Microsoft Corp\", \"ticker\": \"MSFT\", \"price\": 31.6604, \"own\": true}", valueNode.toJson());
    }

    // Test write of map keys using dotted field names
    {
      ScriptNode scriptNode = parser.parseScriptString("map m; m.company = 'Microsoft Corp'; m.ticker = 'MSFT'; m.price = 31.6604; m.own = true; return m;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("Map value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof MapValue);
      assertEquals("Map value", "{company: Microsoft Corp, ticker: MSFT, price: 31.6604, own: true}", valueNode.toString());
      assertEquals("Map value JSON", "{\"company\": \"Microsoft Corp\", \"ticker\": \"MSFT\", \"price\": 31.6604, \"own\": true}", valueNode.toJson());
    }

    // Test read and write of map keys using dotted field names
    {
      ScriptNode scriptNode = parser.parseScriptString("map m; m.company = 'Microsoft Corp'; m.ticker = 'MSFT'; m.price = 31.6604; m.own = true; return 'Company=' + m.company + '/ticker=' + m.ticker + '/price=' + m.price + '/own=' + m.own;");
      assertTrue("Null was returned from script parser", scriptNode != null);
      Value valueNode = scriptRuntime.runScript(parser.scriptString, scriptNode);
      assertTrue("No value node returned from executor", valueNode != null);
      assertTrue("String value not returned from evaluate:" + valueNode.getClass().getSimpleName(), valueNode instanceof StringValue);
      assertEquals("Map value", "Company=Microsoft Corp/ticker=MSFT/price=31.6604/own=true", valueNode.toString());
      assertEquals("Map value JSON", "\"Company=Microsoft Corp/ticker=MSFT/price=31.6604/own=true\"", valueNode.toJson());
    }

  }


  @Test
  public void testFloatEquals() throws Exception {
    Value f1 = new FloatValue(3.14);
    Value f2 = new FloatValue(3.14);
    assertTrue("Identical float value fails to compare as equal", f1.equals(f2));
  }

  @Test
  public void testTextAnalyzer() throws Exception {
    // Test for empty strings
    {
      Value value = runScript("return ''.words;", "ListValue");
      assertValue("Returned", "[]", value);

      value = runScript("return '    \t  \n  \t\n  '.words;", "ListValue");
      assertValue("Returned", "[]", value);
    }

    // Test for simple words
    {
      Value value = runScript("return 'a'.words;", "ListValue");
      assertValue("Returned", "[a]", value);

      value = runScript("return 'Cat'.words;", "ListValue");
      assertValue("Returned", "[\"Cat\"]", value);

      value = runScript("return '123'.words;", "ListValue");
      assertValue("Returned", "[\"123\"]", value);

      value = runScript("return '34.56'.words;", "ListValue");
      assertValue("Returned", "[\"34.56\"]", value);

      value = runScript("return 'one-time'.words;", "ListValue");
      assertValue("Returned", "[\"one-time\"]", value);

      value = runScript("return ' Jack\\'s '.words;", "ListValue");
      assertValue("Returned", "[\"Jack's\"]", value);

      value = runScript("return ' 1,000.00 '.words;", "ListValue");
      assertValue("Returned", "[\"1,000.00\"]", value);

      value = runScript("return ' $2k '.words;", "ListValue");
      assertValue("Returned", "[\"$2k\"]", value);
    }


    // Test for phrases
    {
      Value value = runScript("return ' Hello World! '.words;", "ListValue");
      assertValue("Returned", "[\"Hello\",\"World\"]", value);

      value = runScript("return ' Hello World! '.words.toString;", "StringValue");
      assertValueText("Returned", "[Hello, World]", value);

      value = runScript("return ' This is a test worth $0.02.\n\tThe end. '.words.toString;", "StringValue");
      assertValueText("Returned", "[This, is, a, test, worth, $0.02, The, end]", value);

      value = runScript("return ' This is a test worth $0.02.\n\tThe end. '.words;", "ListValue");
      assertValue("Returned", "[\"This\", \"is\", \"a\", \"test\", \"worth\", \"$0.02\", \"The\", \"end\"]", value);

      value = runScript("return ' This is a test worth $0.02.\n\tThe end. '.words.size;", "IntegerValue");
      assertValueText("Returned", "8", value);

      value = runScript("return ' This is a test worth $0.02.\n\tThe end. '.words.size;", "IntegerValue");
      assertValueInt("Returned", 8, value);


      value = runIntScript("return ' This is a test worth $0.02.\n\tThe end. '.words.size;", 8);

      runStringScript("return 'Hello World!'.words.concat;", "Hello World");

      runStringScript("return 'Hello World!'.words.concat();", "Hello World");

      runStringScript("return 'Hello World!'.words.concat(' ');", "Hello World");

      runStringScript("return 'Hello World!'.words.concat('-');", "Hello-World");

      runStringScript("return 'Hello World!'.words.concat(' -- ');", "Hello -- World");

      runStringScript("return 'Hello World!'.words.concat('/');", "Hello/World");

      runStringScript("return 'This is a test. The end'.words[2, 5].concat('/');", "a/test/The");

    }
  }

  @Test
  public void testTextSearch() throws Exception {

    // Test find of term for lists
    
    {
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('x');", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('a');", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('b');", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('c');", 2);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('d');", 3);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('a', 0);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('a', -1);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('a', 1);", 4);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('b', 1);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('b', -1);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('b', 1);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('b', 2);", 5);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('c', 2);", 2);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('c', -1);", 2);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('c', 3);", 9);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('d', 3);", 3);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('d', 4);", 14);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('a', 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('b', 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('c', 18);", 18);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('d', 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('a', 19);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('b', 19);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('c', 19);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find('d', 19);", -1);
    }

    // Test find of phrase for lists
    {
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find([], 0);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find([], -1);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find([], 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find([], 19);", -1);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['x'], 0);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['x'], -1);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['x'], 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['x'], 19);", -1);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['x', 'y'], 0);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['x', 'y'], -1);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['x', 'y'], 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['x', 'y'], 19);", -1);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a']);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b']);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['c']);", 2);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['d']);", 3);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a'], 0);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a'], -1);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a'], 1);", 4);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b'], 1);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b'], -1);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b'], 1);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b'], 2);", 5);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['c'], 2);", 2);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['c'], -1);", 2);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['c'], 3);", 9);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['d'], 3);", 3);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['d'], 4);", 14);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a'], 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b'], 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['c'], 18);", 18);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['d'], 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a'], 19);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b'], 19);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['c'], 19);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['d'], 19);", -1);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b']);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c']);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['c', 'd']);", 2);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'e']);", 5);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['d', 'a']);", 3);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['d', 'g']);", 14);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['f', 'a']);", 10);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c']);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c'], 0);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c'], 1);", 7);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c'], 7);", 7);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c'], 8);", 11);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c'], 12);", 16);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c'], 17);", -1);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd']);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd'], 0);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd'], 1);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd'], 2);", 12);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd'], 13);", -1);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c']);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 0);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 1);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 19);", -1);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x']);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x'], 0);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x'], 1);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x'], 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x'], 19);", -1);

      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c']);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 0);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 1);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 18);", -1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.find(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 19);", -1);

    }

    // Test count of term for lists

    {
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('x');", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('a');", 5);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('b');", 5);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('c');", 4);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('d');", 2);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('a', 0);", 5);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('a', 1);", 4);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('b', 1);", 5);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('b', 2);", 4);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('c', 2);", 4);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('c', 3);", 3);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('d', 3);", 2);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('d', 4);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('a', 18);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('b', 18);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('c', 18);", 1);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('d', 18);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('a', 19);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('b', 19);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('c', 19);", 0);
      runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count('d', 19);", 0);
    }

    // Test count of phrase for lists
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count([], 0);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count([], 0);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count([], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count([], 19);", 0);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['x'], 0);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['x'], 0);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['x'], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['x'], 19);", 0);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['x', 'y'], 0);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['x', 'y'], 0);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['x', 'y'], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['x', 'y'], 19);", 0);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a']);", 5);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b']);", 5);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['c']);", 4);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['d']);", 2);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a'], 0);", 5);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a'], -1);", 5);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a'], 1);", 4);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b'], 1);", 5);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b'], 0);", 5);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b'], -1);", 5);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b'], 2);", 4);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['c'], 2);", 4);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['c'], 0);", 4);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['c'], 3);", 3);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['c'], 10);", 2);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['c'], 14);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['d'], 3);", 2);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['d'], 4);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['d'], 14);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['d'], 15);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a'], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b'], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['c'], 18);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['d'], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a'], 19);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b'], 19);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['c'], 19);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['d'], 19);", 0);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b']);", 5);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c']);", 4);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['c', 'd']);", 2);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'e']);", 1);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['d', 'a']);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['d', 'g']);", 1);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['f', 'a']);", 1);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c']);", 4);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c'], 0);", 4);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c'], 1);", 3);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c'], 7);", 3);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c'], 8);", 2);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c'], 12);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c'], 17);", 0);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd']);", 2);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd'], 0);", 2);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd'], 1);", 2);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd'], 2);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd'], 13);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd'], 15);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd'], 16);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd'], 17);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd'], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd'], 19);", 0);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c']);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 0);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 1);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 19);", 0);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x']);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x'], 0);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x'], 1);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x'], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['a', 'b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c', 'x'], 19);", 0);

    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c']);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 0);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 1);", 1);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 18);", 0);
    runIntScript("return 'a b c d a b e a b c f a b c d g a b c'.words.count(['b', 'c', 'd', 'a', 'b', 'e', 'a', 'b', 'c', 'f', 'a', 'b', 'c', 'd', 'g', 'a', 'b', 'c'], 19);", 0);
  }

  @Test
  public void testListOps() throws Exception {
    // Test list.concat
    runStringScript("return [2, 5, 3, 9].concat;", "2 5 3 9");
    runStringScript("return [2, 5, 3, 9].concat();", "2 5 3 9");
    runStringScript("return [2, 5, 3, 9].concat(',');", "2,5,3,9");
    runStringScript("return [2, 5, 3, 9].concat(', ');", "2, 5, 3, 9");

    // Test map.concat
    runStringScript("return {abc: 123, def: 4.56, ghi: 'xyz', jkl: true}.concat(', ');", "abc:123, def:4.56, ghi:xyz, jkl:true");
    
    // Test list.sum
    runIntScript("return [2, 5, 3, 9].sum;", 19);
    runIntScript("return [2, 5, 3, 9].sum();", 19);
    runFloatScript("return [2, 5.2, 3.14, 9].sum;", 19.34);

    // Test list.avg
    runIntScript("return [2, 5, 3, 9].avg;", 4);
    runIntScript("return [2, 5, 3, 9].avg();", 4);
    runIntScript("return [2, 5, 3, 9].average;", 4);
    runIntScript("return [2, 5, 3, 9].average();", 4);
    runFloatScript("return [2, 5.2, 3.14, 9].avg;", 4.835);

  }

  @Test
  public void testExecutionLimit() throws Exception {
    // Test execution limit check
    agentServer.config.putDefaultExecutionLevel(2);
    runScript("for (int i = 0, int k = 0; i < 15; i++){k++;}", "NullValue");
    runScript("for (int i = 0; i < 23; i++){}", "NullValue");
    long now = System.currentTimeMillis();
    runScript("for (int i = 0; i < 24; i++){}", "NullValue", 1);
    List<ExceptionInfo> exceptions = dummyAgentInstance.exceptionHistory;
    ExceptionInfo exception = exceptions.get(0);
    assertEquals("Exception type", "com.basetechnology.s0.agentserver.script.runtime.NodeExecutionLimitException", exception.type);
    long delta = exception.time - now;
    assertTrue("Time of exception is not in range " + delta, delta >= 0 && delta < 300);
    assertEquals("Exception message", "Script for (int i = 0; i < 24; i++){} has excceded operation execution limit of 100 operations", exception.message);
  }

  @Test
  public void testExceptionHistory() throws Exception {
    // Test exception history
    runScript("1/0;", "NullValue", 1);
    assertEquals("Count of exceptions", 1, dummyAgentInstance.exceptionHistory.size());
    ExceptionInfo exception = dummyAgentInstance.exceptionHistory.get(0);
    assertEquals("Exception type", "java.lang.ArithmeticException", exception.type);
    assertEquals("Exception message", "/ by zero", exception.message);
  }

  @Test
  public void testXmlText() throws Exception {
    // Test text processing for XML
    runMapScript("return 'abc</aa>'.xml;", "", 1);
    runMapScript("return 'abc'.xml;", "{text_1: abc}");
    runStringScript("return 'abc'.xml.text;", "abc");
    runMapScript("return '<aa>abc</aa>'.xml;", "{aa: abc}");
    runStringScript("return '<aa>abc</aa>'.xml.text;", "abc");
    runMapScript("return 'abc<aa>def</aa>'.xml;", "{text_1: abc, aa: def}");
    runStringScript("return 'abc<aa>def</aa>'.xml.text;", "abc def");
    runMapScript("return '<aa>abc</aa>'.xml;", "{aa: abc}");
    runMapScript("return '<aa>a&gt;b&#82;c&ugrave;d&Ccedil;e</aa>'.xml;", "{aa: a>bRc\u00f9d\u00c7e}");
    runMapScript("return '<aa>abc<bb>def</bb>ghi</aa>'.xml;", "{aa: {text_1: abc, bb: def, text_2: ghi}}");
    runStringScript("return '<aa>abc<bb>def</bb>ghi</aa>'.xml.text;", "abc def ghi");
    runStringScript("return '<aa>abc<bb>def</bb><bb>ghi</bb>jkl</aa>'.xml.text;", "abc def ghi jkl");
    runStringScript("return '<description>TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week.&lt;div class=\"feedflare\"&gt;&lt;a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"&gt;&lt;img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?d=yIl2AUoC8zA\" border=\"0\"&gt;&lt;/img&gt;&lt;/a&gt; &lt;a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\"&gt;&lt;img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\" border=\"0\"&gt;&lt;/img&gt;&lt;/a&gt; &lt;a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\"&gt;&lt;img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\" border=\"0\"&gt;&lt;/img&gt;&lt;/a&gt;&lt;/div&gt;&lt;img src=\"http://feeds.feedburner.com/~r/reuters/topNews/~4/7UvuaJPjuyw\" height=\"1\" width=\"1\"/&gt;</description>'.xml.text;", "TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week.<div class=\"feedflare\"><a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?d=yIl2AUoC8zA\" border=\"0\"></img></a> <a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\" border=\"0\"></img></a> <a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\" border=\"0\"></img></a></div><img src=\"http://feeds.feedburner.com/~r/reuters/topNews/~4/7UvuaJPjuyw\" height=\"1\" width=\"1\"/>");
    runMapScript("return 'TOPEKA <div class=\"feedflare\"></div>'.xml;", "{text_1: TOPEKA, div: {class: feedflare}}");
    runStringScript("return 'TOPEKA <div class=\"feedflare\"></div>'.xml.text;", "TOPEKA feedflare");
    runMapScript("return 'TOPEKA<div class=\"feedflare\"><a href=\"http\"></a></div>'.xml;", "{text_1: TOPEKA, div: {class: feedflare, a: {href: http}}}");
    runStringScript("return 'TOPEKA<div class=\"feedflare\"><a href=\"http\"></a></div>'.xml.text;", "TOPEKA feedflare http");
    runMapScript("return '<a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"></a>'.xml;", "{a: {href: http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA}}");
    runStringScript("return '<a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"></a>'.xml.text;", "http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA");
    runMapScript("return 'TOPEKA<a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"></a>'.xml;", "{text_1: TOPEKA, a: {href: http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA}}");
    runStringScript("return 'TOPEKA<a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"></a>'.xml.text;", "TOPEKA http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA");
    runMapScript("return 'TOPEKA<div class=\"feedflare\"><a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"></a></div>'.xml;", "{text_1: TOPEKA, div: {class: feedflare, a: {href: http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA}}}");
    runStringScript("return 'TOPEKA<div class=\"feedflare\"><a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"></a></div>'.xml.text;", "TOPEKA feedflare http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA");
    runStringScript("return 'TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week.<div class=\"feedflare\"><a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"></a></div>'.xml.text;", "TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week. feedflare http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA");
    runMapScript("return 'TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week.<div class=\"feedflare\"><a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?d=yIl2AUoC8zA\" border=\"0\"></img></a> <a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\" border=\"0\"></img></a> <a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\" border=\"0\"></img></a></div><img src=\"http://feeds.feedburner.com/~r/reuters/topNews/~4/7UvuaJPjuyw\" height=\"1\" width=\"1\"/>'.xml;", "{text_1: TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week., div: {class: feedflare, a: [{href: http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA, img: {src: http://feeds.feedburner.com/~ff/reuters/topNews?d=yIl2AUoC8zA, border: 0}}, {href: http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU, img: {src: http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU, border: 0}}, {href: http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI, img: {src: http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI, border: 0}}]}, img: {src: http://feeds.feedburner.com/~r/reuters/topNews/~4/7UvuaJPjuyw, height: 1, width: 1}}");
    runMapScript("return 'abc<aa>def</aa>'.xml;", "{text_1: abc, aa: def}");
    runStringScript("return 'abc<aa>def</aa>'.xml.text;", "abc def");
    runMapScript("return 'abc<aa>def</aa>ghi'.xml;", "{text_1: abc, aa: def, text_2: ghi}");
    runStringScript("return 'abc<aa>def</aa>ghi'.xml.text;", "abc def ghi");
    runMapScript("return 'abc<aa>def</aa>ghi<bb>jkl</bb>'.xml;", "{text_1: abc, aa: def, text_2: ghi, bb: jkl}");
    runStringScript("return 'abc<aa>def</aa>ghi<bb>jkl</bb>'.xml.text;", "abc def ghi jkl");
    runStringScript("return 'TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week.<div class=\"feedflare\"><a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?d=yIl2AUoC8zA\" border=\"0\"></img></a> <a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\" border=\"0\"></img></a> <a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\" border=\"0\"></img></a></div><img src=\"http://feeds.feedburner.com/~r/reuters/topNews/~4/7UvuaJPjuyw\" height=\"1\" width=\"1\"/>'.xml.text;", "TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week. feedflare http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA http://feeds.feedburner.com/~ff/reuters/topNews?d=yIl2AUoC8zA 0 http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU 0 http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI 0 http://feeds.feedburner.com/~r/reuters/topNews/~4/7UvuaJPjuyw 1 1");
    runMapScript("return 'abc<aa>def</aa>ghi<bb>jkl</bb>yzaxyz'.xml;", "{text_1: abc, aa: def, text_2: ghi, bb: jkl, text_3: yzaxyz}");
    runStringScript("return 'abc<aa>def</aa>ghi<bb>jkl</bb>yzaxyz'.xml.text;", "abc def ghi jkl yzaxyz");
    runMapScript("return 'abc<aa>def</aa>ghi<bb>jkl</bb>yza'.xml;", "{text_1: abc, aa: def, text_2: ghi, bb: jkl, text_3: yza}");
    runStringScript("return 'abc<aa>def</aa>ghi<bb>jkl</bb>yza'.xml.text;", "abc def ghi jkl yza");
    runMapScript("return 'abc<aa>def</aa>ghi<bb>jkl<cc>mno</cc>pqr<cc>stu</cc>vwx</bb>yza'.xml;", "{text_1: abc, aa: def, text_2: ghi, bb: {text_1: jkl, cc: [mno, stu], text_2: pqr, text_3: vwx}, text_3: yza}");
    runStringScript("return 'abc<aa>def</aa>ghi<bb>jkl<cc>mno</cc>pqr<cc>stu</cc>vwx</bb>yza'.xml.text;", "abc def ghi jkl mno stu pqr vwx yza");

    // Parse as HTML, which gives unique names to repeated elements to preserve order
    runMapScript("return 'abc<aa>def</aa>ghi<bb>jkl<cc>mno</cc>pqr<cc>stu</cc>vwx</bb>yza'.html;", "{text_1: abc, aa: def, text_2: ghi, bb: {text_1: jkl, cc: mno, text_2: pqr, cc_1: stu, text_3: vwx}, text_3: yza}");
    runStringScript("return 'abc<aa>def</aa>ghi<bb>jkl<cc>mno</cc>pqr<cc>stu</cc>vwx</bb>yza'.html.text;", "abc def ghi jkl mno pqr stu vwx yza");
    runStringScript("return 'TOPEKA <div class=\"feedflare\"></div>'.html.text;", "TOPEKA");
    runStringScript("return 'TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week.<div class=\"feedflare\"><a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:yIl2AUoC8zA\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?d=yIl2AUoC8zA\" border=\"0\"></img></a> <a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:V_sGLiPBpWU\" border=\"0\"></img></a> <a href=\"http://feeds.reuters.com/~ff/reuters/topNews?a=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\"><img src=\"http://feeds.feedburner.com/~ff/reuters/topNews?i=7UvuaJPjuyw:QpGdcGtVxrY:-BTjWOF_DHI\" border=\"0\"></img></a></div><img src=\"http://feeds.feedburner.com/~r/reuters/topNews/~4/7UvuaJPjuyw\" height=\"1\" width=\"1\"/>'.html.text;", "TOPEKA, Kansas (Reuters) - Republican presidential hopeful Rick Santorum swept the Kansas caucuses on Saturday with 53 percent of the vote, giving him a boost going into crucial primary votes in the South next week.");

    // Test mapping of special charactes to entities in xml
    runStringScript("return (null).toXml;", "");    
    runStringScript("return (0).toXml;", "0");    
    runStringScript("return (1.25).toXml;", "1.25");    
    runStringScript("return (true).toXml;", "true");    
    runStringScript("return (false).toXml;", "false");    
    runStringScript("return ('Hello World!').toXml;", "Hello World!");    
    runStringScript("return ('a<b&c\u00c7d').toXml;", "a&lt;b&amp;c&Ccedil;d");    
    runStringScript("return list('a<b', 'c&d', 'xx\u00c7yy').toXml;", "a&lt;b c&amp;d xx&Ccedil;yy");    
    runStringScript("return map(abc: 'a<b', def: 'c&d', ghi: 'xx\u00c7yy').toXml;", "<abc>a&lt;b</abc><def>c&amp;d</def><ghi>xx&Ccedil;yy</ghi>");    
    runStringScript("return map(abc: 'a<b', def: 'c&d', ghi: 'xx\u00c7yy').toText;", "a<b c&d xx\u00c7yy");    
    runStringScript("return map(abc: 'a<b', def: 'c&d', ghi: 'xx\u00c7yy').toJson;", "{\"abc\": \"a<b\", \"def\": \"c&d\", \"ghi\": \"xx\u00c7yy\"}");    
    runStringScript("return '{\"abc\": \"a<b\", \"def\": \"c&d\", \"ghi\": \"xx\u00c7yy\"}'.json.toXml;", "<abc>a&lt;b</abc><ghi>xx&Ccedil;yy</ghi><def>c&amp;d</def>");    
    runStringScript("return '<abc>a&lt;b</abc><ghi>xx&Ccedil;yy</ghi><def>c&amp;d</def>'.xml.text;", "a<b xx\u00c7yy c&d");    
    //runStringScript("return '{\"abc\": \"a<b\", \"def\": \"c&d\", \"ghi\": \"xx\u00c7yy\"}'.json.toText;", "a<b c&d xx\u00c7yy");    
  }

  @Test
  public void testUrlEncode() throws Exception {
    // Test encode and decode of URL
    runStringScript("return 'Hello-World'.urlEncode;", "Hello-World");
    runStringScript("return 'Hello-World'.urlDecode;", "Hello-World");
    runStringScript("return 'Hello World'.urlEncode;", "Hello+World");
    runStringScript("return 'Hello+World'.urlDecode;", "Hello World");
    runStringScript("return '(Hello World?)'.urlEncode;", "%28Hello+World%3F%29");
    runStringScript("return '%28Hello+World%3F%29'.urlDecode;", "(Hello World?)");
    runStringScript("return 'I/O'.urlEncode;", "I%2FO");
    runStringScript("return 'I%2FO'.urlDecode;", "I/O");
    runStringScript("return '/mypath/abc/def'.urlEncode;", "%2Fmypath%2Fabc%2Fdef");
    runStringScript("return '%2Fmypath%2Fabc%2Fdef'.urlDecode;", "/mypath/abc/def");
    runStringScript("return 'http://x.com/x/y.html?param=Hello+World'.urlEncode;", "http%3A%2F%2Fx.com%2Fx%2Fy.html%3Fparam%3DHello%2BWorld");
    runStringScript("return 'http%3A%2F%2Fx.com%2Fx%2Fy.html%3Fparam%3DHello%2BWorld'.urlDecode;", "http://x.com/x/y.html?param=Hello+World");
  }

  @Test
  public void testTernaryCondition() throws Exception {
    // Test ternary condition operator
    evalBoolean("true ? true : false", true);
    evalBoolean("false ? true : false", false);

    evalInt("true ? 123 : 456", 123);
    evalInt("false ? 123 : 456", 456);

    evalFloat("true ? 123.4 : 456.7", 123.4);
    evalFloat("false ? 123.4 : 456.7", 456.7);

    evalString("true ? 'abc' : 'def'", "abc");
    evalString("false ? 'abc' : 'def'", "def");

    evalInt("true ? 123 : true ? 456 : 789", 123);
    evalInt("true ? 123 : false ? 456 : 789", 123);
    evalInt("false ? 123 : true ? 456 : 789", 456);
    evalInt("false ? 123 : false ? 456 : 789", 789);

    evalInt("77 > 66 && 'a' < 'b' || 'x' == 'y' ? 123 : 77 > 66 && 'a' < 'b' || 'x' == 'y' ? 456 : 789", 123);
    evalInt("77 > 66 && 'a' < 'b' || 'x' == 'y' ? 123 : 77 > 66 && 'a' > 'b' || 'x' == 'y' ? 456 : 789", 123);
    evalInt("77 > 66 && 'a' > 'b' || 'x' == 'y' ? 123 : 77 > 66 && 'a' < 'b' || 'x' == 'y' ? 456 : 789", 456);
    evalInt("77 > 66 && 'a' > 'b' || 'x' == 'y' ? 123 : 77 > 66 && 'a' > 'b' || 'x' == 'y' ? 456 : 789", 789);

    evalInt("true ? 123 : 'abc'", 123);
    evalString("false ? 123 : 'abc'", "abc");

    evalInt("true ? 123 : null", 123);
    evalString("true ? 'abc' : null", "abc");
    evalInt("false ? null : 123", 123);
    evalString("false ? null : 'abc'", "abc");
    evalNull("false ? 123 : null");
    evalNull("false ? 'abc' : null");

  }

  @Test
  public void testLogicalOps() throws Exception {
    evalBoolean("false && false", false);
    evalBoolean("false && true", false);
    evalBoolean("true && false", false);
    evalBoolean("true && true", true);

    evalBoolean("false || false", false);
    evalBoolean("false || true", true);
    evalBoolean("true || false", true);
    evalBoolean("true || true", true);
    
    evalBoolean("false && false || false", false);
    evalBoolean("false && false || true", true);
    evalBoolean("false && true || false", false);
    evalBoolean("false && true || true", true);
    evalBoolean("true && false || false", false);
    evalBoolean("true && false || true", true);
    evalBoolean("true && true || false", true);
    evalBoolean("true && true || true", true);
    
    evalBoolean("false || false && false", false);
    evalBoolean("false || false && true", false);
    evalBoolean("false || true && false", false);
    evalBoolean("false || true && true", true);
    evalBoolean("true || false && false", true);
    evalBoolean("true || false && true", true);
    evalBoolean("true || true && false", true);
    evalBoolean("true || true && true", true);

    evalBoolean("-false", true);
    evalBoolean("-true", false);

    evalInt("-3", -3);
    evalInt("-(3)", -3);
    evalInt("- -3", 3);
    evalInt("- 3", -3);
    evalInt("- (2 - 5)", 3);
    evalInt("- (5 - 2)", -3);

    evalFloat("-3.125", -3.125);
    evalFloat("-(3.125)", -3.125);
    evalFloat("- -3.125", 3.125);
    evalFloat("- 3.125", -3.125);
    evalFloat("- (2 - 5.125)", 3.125);
    evalFloat("- (5.125 - 2)", -3.125);

    eval("! 1", "com.basetechnology.s0.agentserver.RuntimeException",
        "Operand of the logical NOT operator must be a boolean, but has type IntegerValue value: 1");

    evalBoolean("! (2 < 3)", false);
    evalBoolean("! (2 > 3)", true);

    eval("! 2 < 3", "com.basetechnology.s0.agentserver.RuntimeException",
        "Operand of the logical NOT operator must be a boolean, but has type IntegerValue value: 2");

    evalBoolean("! false && false", false);
    evalBoolean("! false && true", true);
    evalBoolean("! true && false", false);
    evalBoolean("! true && true", false);

    evalBoolean("false && ! false", false);
    evalBoolean("false && ! true", false);
    evalBoolean("true && ! false", true);
    evalBoolean("true && ! true", false);

    eval("! 1 > 2 && 1 > 2", "com.basetechnology.s0.agentserver.RuntimeException",
        "Operand of the logical NOT operator must be a boolean, but has type IntegerValue value: 1");
    evalBoolean("! (1 > 2) && 1 > 2", false);
    evalBoolean("! (1 > 2) && 3 < 4", true);
    evalBoolean("! (3 < 4) && 1 > 2", false);
    evalBoolean("! (3 < 4) && 3 < 4", false);

    // Note: && is conditional, so right side is not evaluated if left side is true
    evalBoolean("1 > 2 && ! 1 > 2", false);
    evalBoolean("1 > 2 && ! (1 > 2)", false);
    evalBoolean("1 > 2 && ! (3 < 4)", false);
    eval("3 < 4 && ! 1 > 2", "com.basetechnology.s0.agentserver.RuntimeException",
        "Operand of the logical NOT operator must be a boolean, but has type IntegerValue value: 1");
    evalBoolean("3 < 4 && ! (1 > 2)", true);
    evalBoolean("3 < 4 && ! (3 < 4)", false);

  }

  @Test
  public void testObjectType() throws Exception {
    runIntScript("object ob = 123; return ob;", 123);
    runListScript("object ob = list(123, 456); return ob;", "[123, 456]");
    runNullScript("object ob; return ob;");

  }

  @Test
  public void testMinMax() throws Exception {
    evalInt("min(123)", 123);
    evalInt("min(123, 456)", 123);
    evalInt("min(456, 123)", 123);
    evalInt("min(123, 456, 789)", 123);
    evalInt("min(789, 456, 123)", 123);
    evalInt("min(789, 123, 456)", 123);

    evalFloat("min(123.125)", 123.125);
    evalFloat("min(123.125, 456.25)", 123.125);
    evalFloat("min(456.25, 123.125)", 123.125);
    evalFloat("min(123.125, 456.25, 789.5)", 123.125);
    evalFloat("min(789.5, 456.25, 123.125)", 123.125);
    evalFloat("min(789.5, 123.125, 456.25)", 123.125);

    evalInt("max(123)", 123);
    evalInt("max(123, 456)", 456);
    evalInt("max(456, 123)", 456);
    evalInt("max(123, 456, 789)", 789);
    evalInt("max(789, 456, 123)", 789);
    evalInt("max(456, 789, 123)", 789);

    evalFloat("max(123.125)", 123.125);
    evalFloat("max(123.125, 456.25)", 456.25);
    evalFloat("max(456.25, 123.125)", 456.25);
    evalFloat("max(123.125, 456.25, 789.5)", 789.5);
    evalFloat("max(789.5, 456.25, 123.125)", 789.5);
    evalFloat("max(456.25, 789.5, 123.125)", 789.5);

  }

  @Test
  public void testSumAvg() throws Exception {
    evalInt("sum(123)", 123);
    evalInt("sum(123, 456)", 579);
    evalInt("sum(123, 456, 789)", 1368);
    evalInt("sum(456, 123, 789)", 1368);

    evalFloat("sum(123.125)", 123.125);
    evalFloat("sum(123.125, 456.25)", 579.375);
    evalFloat("sum(123.125, 456.25, 789.5)", 1368.875);
    evalFloat("sum(456.25, 123.125, 789.5)", 1368.875);

    evalInt("avg(123)", 123);
    evalInt("avg(123, 456)", 289);
    evalInt("avg(123, 456, 789)", 456);
    evalInt("avg(456, 123, 789)", 456);

    evalFloat("avg(123.125)", 123.125);
    evalFloat("avg(123.125, 456.25)", 289.6875);
    evalFloat("avg(123.125, 456.25, 789.5)", 456.291666);
    evalFloat("avg(456.25, 123.125, 789.5)", 456.291666);

  }

  @Test
  public void testUserFunctionCall() throws Exception {
    // Simple functions
    callIntFunction("int f(int x, int y){return x + y;}",
        Arrays.asList((Value)(new IntegerValue(123)), new IntegerValue(456)), 579);
    callFloatFunction("float f(float x, float y){return x + y;}",
        Arrays.asList((Value)(new FloatValue(123.125)), new FloatValue(456.25)), 579.375);
    callFloatFunction("int f(int x, int y){return x + y;}",
        Arrays.asList((Value)(new FloatValue(123.125)), new FloatValue(456.25)), 579.375);
    callStringFunction("string f(string x, string y){return x + y;}",
        Arrays.asList((Value)(new StringValue("abc")), new StringValue("def")), "abcdef");
    
    // Zero-arg functions
    callIntFunction("int f(){return 123;}", null, 123);
    callFloatFunction("int f(){return 123.25;}", null, 123.25);
    callStringFunction("string f(){return 'abc';}", null, "abc");
    
    // No return type
    callIntFunction("f(){return 123;}", null, 123);
    callFloatFunction("f(){return 123.25;}", null, 123.25);
    callStringFunction("f(){return 'abc';}", null, "abc");

    // Use 'object' type for any type of arguments
    callIntFunction("object f(object x, object y){return x + y;}",
        Arrays.asList((Value)(new IntegerValue(123)), new IntegerValue(456)), 579);
    callFloatFunction("object f(object x, object y){return x + y;}",
        Arrays.asList((Value)(new FloatValue(123.125)), new FloatValue(456.25)), 579.375);
    callStringFunction("object f(object x, object y){return x + y;}",
        Arrays.asList((Value)(new StringValue("abc")), new StringValue("def")), "abcdef");
    callStringFunction("object f(object x, object y){return x + y;}",
        Arrays.asList((Value)(new StringValue("abc")), new IntegerValue(123)), "abc123");

    // Pass a 'list' argument
    ListValue listValue = new ListValue(IntegerTypeNode.one, Arrays.asList((Value)(
        new IntegerValue(123)), new IntegerValue(456), new IntegerValue(789)));
    callIntFunction("int f(list lst){return lst.min;}", Arrays.asList((Value)(listValue)), 123);
    callIntFunction("int f(list lst){return lst.max;}", Arrays.asList((Value)(listValue)), 789);
    callIntFunction("int f(list lst){return lst.sum();}", Arrays.asList((Value)(listValue)), 1368);
    callIntFunction("int f(list lst){return lst.avg();}", Arrays.asList((Value)(listValue)), 456);
    callIntFunction("int get(list lst, int i){return lst[i];}",
        Arrays.asList((Value)(listValue), new IntegerValue(0)), 123);
    callIntFunction("int get(list lst, int i){return lst[i];}",
        Arrays.asList((Value)(listValue), new IntegerValue(1)), 456);
    callIntFunction("int get(list lst, int i){return lst[i];}",
        Arrays.asList((Value)(listValue), new IntegerValue(2)), 789);
    callIntFunction("int add(list lst, int i, int j){return lst[i] + lst[j];}",
        Arrays.asList((Value)(listValue), new IntegerValue(0), new IntegerValue(0)), 246);
    callIntFunction("int add(list lst, int i, int j){return lst[i] + lst[j];}",
        Arrays.asList((Value)(listValue), new IntegerValue(0), new IntegerValue(1)), 579);
    callIntFunction("int add(list lst, int i, int j){return lst[i] + lst[j];}",
        Arrays.asList((Value)(listValue), new IntegerValue(0), new IntegerValue(2)), 912);
    callIntFunction("int add(list lst, int i, int j){return lst[i] + lst[j];}",
        Arrays.asList((Value)(listValue), new IntegerValue(1), new IntegerValue(2)), 1245);
    
    // Eval a string passed as an argument
    callIntFunction("int evalx(string expr){return eval(expr);}",
        Arrays.asList((Value)(new StringValue("2 * 7"))), 14);

  }

  @Test
  public void testExit() throws Exception {
    assertTrue("Should not be deleted", ! dummyAgentInstance.deleted);
    evalNull("exit()");
    assertTrue("Should be deleted", dummyAgentInstance.deleted);
    dummyAgentInstance.deleted = false;

    assertTrue("Should not be deleted", ! dummyAgentInstance.deleted);
    runNullScript("exit();");
    assertTrue("Should be deleted", dummyAgentInstance.deleted);
    dummyAgentInstance.deleted = false;
  }
  
}
