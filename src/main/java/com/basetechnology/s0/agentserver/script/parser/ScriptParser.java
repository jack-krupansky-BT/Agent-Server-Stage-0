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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.basetechnology.s0.agentserver.AgentInstance;
import com.basetechnology.s0.agentserver.script.intermediate.AddNode;
import com.basetechnology.s0.agentserver.script.intermediate.AssignmentNode;
import com.basetechnology.s0.agentserver.script.intermediate.AssignmentStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.BlockStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.BreakStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.CaseStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.CatchStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.ContinueStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.DivideNode;
import com.basetechnology.s0.agentserver.script.intermediate.DoStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.EqualsNode;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionListNode;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionNode;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionStatementListNode;
import com.basetechnology.s0.agentserver.script.intermediate.ExpressionStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.FieldNode;
import com.basetechnology.s0.agentserver.script.intermediate.ForStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.FunctionCallNode;
import com.basetechnology.s0.agentserver.script.intermediate.GreaterEqualsNode;
import com.basetechnology.s0.agentserver.script.intermediate.GreaterNode;
import com.basetechnology.s0.agentserver.script.intermediate.IfStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.LessEqualsNode;
import com.basetechnology.s0.agentserver.script.intermediate.LessNode;
import com.basetechnology.s0.agentserver.script.intermediate.ListTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.LogicalAndNode;
import com.basetechnology.s0.agentserver.script.intermediate.LogicalNotNode;
import com.basetechnology.s0.agentserver.script.intermediate.LogicalOrNode;
import com.basetechnology.s0.agentserver.script.intermediate.MapTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.MethodReferenceNode;
import com.basetechnology.s0.agentserver.script.intermediate.MultiplyNode;
import com.basetechnology.s0.agentserver.script.intermediate.NameReferenceNode;
import com.basetechnology.s0.agentserver.script.intermediate.NegationNode;
import com.basetechnology.s0.agentserver.script.intermediate.NewNode;
import com.basetechnology.s0.agentserver.script.intermediate.NotEqualsNode;
import com.basetechnology.s0.agentserver.script.intermediate.NullExpressionNode;
import com.basetechnology.s0.agentserver.script.intermediate.NullStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.PostDecrementNode;
import com.basetechnology.s0.agentserver.script.intermediate.PostIncrementNode;
import com.basetechnology.s0.agentserver.script.intermediate.PreDecrementNode;
import com.basetechnology.s0.agentserver.script.intermediate.PreIncrementNode;
import com.basetechnology.s0.agentserver.script.intermediate.ReferenceNode;
import com.basetechnology.s0.agentserver.script.intermediate.RemainderNode;
import com.basetechnology.s0.agentserver.script.intermediate.ReturnStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.ScriptNode;
import com.basetechnology.s0.agentserver.script.intermediate.StatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.StringTypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.SubscriptedReferenceNode;
import com.basetechnology.s0.agentserver.script.intermediate.SubtractNode;
import com.basetechnology.s0.agentserver.script.intermediate.SwitchStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.Symbol;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolException;
import com.basetechnology.s0.agentserver.script.intermediate.SymbolManager;
import com.basetechnology.s0.agentserver.script.intermediate.TernaryConditionNode;
import com.basetechnology.s0.agentserver.script.intermediate.ThrowStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.TryStatementNode;
import com.basetechnology.s0.agentserver.script.intermediate.TypeNode;
import com.basetechnology.s0.agentserver.script.intermediate.VariableReferenceNode;
import com.basetechnology.s0.agentserver.script.intermediate.WhileStatementNode;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenList;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.Tokenizer;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenizerException;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.AnyEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.AsteriskEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.AsteriskOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.BreakKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ColonOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.CommaOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ContinueKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.DoKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ElseKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.EndToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.EqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.EqualsOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.FalseToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.FloatToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ForKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.GreaterEqualsOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.GreaterOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.IdentifierToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.IfKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.IntegerToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LeftBraceOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LeftParenthesisOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LeftSquareBracketOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LessEqualsOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LessOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ListKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LogicalAndOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LogicalNotOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LogicalOrOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.MapKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.MinusEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.MinusMinusOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.MinusOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.NotEqualsOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.NowKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.NullToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PercentSignOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PeriodOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PlusEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PlusOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PlusPlusOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.QuestionMarkOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ReturnKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.RightBraceOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.RightParenthesisOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.RightSquareBracketOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.SemicolonOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.SlashEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.SlashOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.StringToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.SwitchKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.Token;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.TrueToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.TryKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.TypeKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.WhileKeywordToken;
import com.basetechnology.s0.agentserver.script.runtime.value.FalseValue;
import com.basetechnology.s0.agentserver.script.runtime.value.FloatValue;
import com.basetechnology.s0.agentserver.script.runtime.value.IntegerValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NowValue;
import com.basetechnology.s0.agentserver.script.runtime.value.NullValue;
import com.basetechnology.s0.agentserver.script.runtime.value.StringValue;
import com.basetechnology.s0.agentserver.script.runtime.value.TrueValue;

public class ScriptParser {
  public AgentInstance agentInstance;
  public SymbolManager symbolManager;
  public SymbolManager blockSymbolManager;
  int blockId = 0;
  String blockCategoryName;
  List<Symbol> localVariables;
  Map<String, Symbol> localVariableMap;
  public String scriptString;
  public TokenList tokens;

  public ScriptParser(AgentInstance agentInstance){
    this.agentInstance = agentInstance;
    this.symbolManager = agentInstance.symbolManager;
  }

  public ExpressionNode parseExpressionString(String scriptString) throws TokenizerException, ParserException  {
    this.blockSymbolManager = null;
    this.blockCategoryName = null;
    this.localVariables = new ArrayList<Symbol>();
    this.localVariableMap = new HashMap<String, Symbol>();
    this.scriptString = scriptString;

    Tokenizer tzer = new Tokenizer();
    tokens = tzer.tokenizeString(scriptString);

    // Return null tree if expression is completely empty
    if (tokens == null || tokens.size() == 0)
      return null;

    // Parse the expression tokens
    ExpressionNode expressionNode = parseExpression();

    // Make sure no junk left at the end of the expression
    Token token = tokens.get();
    if (! (token instanceof EndToken))
      throw new ParserException("Expected end of expresion, but found: " + token);

    // Return the generated expression node
    return expressionNode;
  }

  public ScriptNode parseScriptString(String scriptString) throws TokenizerException, ParserException  {
    return parseScriptString(null, scriptString);
  }

  public ScriptNode parseScriptString(SymbolManager symbolManager, String scriptString) throws TokenizerException, ParserException  {
    if (symbolManager != null)
      this.symbolManager = symbolManager;
    this.blockSymbolManager = new SymbolManager();
    this.scriptString = scriptString;

    Tokenizer tzer = new Tokenizer();
    tokens = tzer.tokenizeString(scriptString);

    // Return null tree if expression is completely empty
    if (tokens == null || tokens.size() == 0)
      return null;

    ScriptNode scriptNode = parseScript();
    return scriptNode;
  }

  public ScriptNode parseScript() throws ParserException {
    // Parse the Script
    BlockStatementNode blockNode = parseBlockBody();

    // Make sure no junk left at the end of the script
    Token token = tokens.get();
    if (! (token instanceof EndToken))
      throw new ParserException("Expected end of script, but found: " + token);

    // Generate the script node
    ScriptNode scriptNode = new ScriptNode(blockNode);
    return scriptNode;
  }

  protected BlockStatementNode parseBlockStatement() throws ParserException {
    BlockStatementNode blockNode = null;

    // Skip the opening {
    tokens.getNext();

    blockNode = parseBlockBody();

    Token token = tokens.get();
    if (! (token instanceof RightBraceOperatorToken))
      throw new ParserException("Expected '{' to start block statement but found " + token.getClass().getSimpleName());
    tokens.getNext();

    return blockNode;
  }

  protected BlockStatementNode parseBlockBody() throws ParserException {
    // Save previous block state
    String savedBlockCategoryName = blockCategoryName;
    List<Symbol> savedLocalVariables = localVariables;
    Map<String, Symbol> savedLocalVariableMap = localVariableMap;

    // Start a new symbol category for this block
    int blockId = this.blockId++;
    blockCategoryName = "block-" + blockId;

    // Keep track of local variables
    localVariables = new ArrayList<Symbol>();
    localVariableMap = new HashMap<String, Symbol>();

    List<StatementNode> bodySequence = new ArrayList<StatementNode>();
    while (! tokens.atEnd()){
      Token token = tokens.get();
      if (token instanceof RightBraceOperatorToken)
        break;
      StatementNode statement = null;

      // Check for variable declaration - leading type keyword
      if (token instanceof TypeKeywordToken){
        // Parse variable declaration

        // Parse the variable type
        TypeNode varType = parseType();
        token = tokens.get();

        // Parse comma-separated list of variable names
        do {
          // Parse the variable name
          if (! (token instanceof IdentifierToken))
            // TODO: Add offset for each token and report in all error messages
            throw new ParserException("Expected variable name after type but found " + token.getClass().getSimpleName());
          String varName = ((IdentifierToken)token).identifier;
          token = tokens.getNext();

          // Make sure user hasn't already declared the name
          if (localVariableMap.containsKey(varName))
            throw new ParserException("Local Variable '" + varName + "' is already declared in this block");

          // Add new variable name for this block
          Symbol varSymbol = null;
          try {
            varSymbol = blockSymbolManager.put(blockCategoryName, varName, varType);
          } catch (SymbolException e){
            throw new ParserException("Parser Exception: " + e.getMessage());
          }
          localVariableMap.put(varName, varSymbol);
          localVariables.add(varSymbol);

          // Parse optional initializer
          if (token instanceof EqualOperatorToken){
            tokens.skipToken();
            ExpressionNode initializerNode = parseExpression();

            // Generate an implicit assignment statement for the initializer

            // Create a variable reference node
            VariableReferenceNode varRefNode = new VariableReferenceNode(varSymbol);

            // Generate the implicit assignment statement node
            StatementNode implicitStatement = new AssignmentStatementNode(varRefNode, initializerNode);
            bodySequence.add(implicitStatement);

            token = tokens.get();
          }

          // Skip possible comma if a list of vars
          if (token instanceof CommaOperatorToken)
            token = tokens.getNext();

          // May also change to a new type in the comma-separated variable list
          if (token instanceof TypeKeywordToken){
            varType = parseType();
            token = tokens.get();
          }
        } while (tokens.get() instanceof IdentifierToken);

        // Parse the semicolon after the variable declaration
        parseSemicolon();
      } else
        statement = parseStatement();

      // Add statement to list for block - skip null which indicates a variable declaration
      if (statement != null)
        bodySequence.add(statement);
    }

    // Generate node for the block statement
    BlockStatementNode blockNode = new BlockStatementNode(localVariables, bodySequence);

    // Restore previous block state
    // TODO: Put this in try finally
    blockSymbolManager.removeCategory(blockCategoryName);
    blockCategoryName = savedBlockCategoryName;
    localVariables = savedLocalVariables;
    localVariableMap = savedLocalVariableMap;

    return blockNode;
  }

  protected TypeNode parseType() throws ParserException {
    // Make sure we have a type
    Token token = tokens.get();
    if (! (token instanceof TypeKeywordToken))
      throw new ParserException("Expected type, but found: " + token.toString(), token);

    // Get type type node for the type keyword token
    TypeNode varType = ((TypeKeywordToken)token).getTypeNode();

    // Skip over the type token
    token = tokens.getNext();

    // Check for element type specification (for lists and maps
    if (varType instanceof ListTypeNode){
      // Check for '<' for specification of element type
      if (token instanceof LessOperatorToken){
        // Skip over the opening '<'
        tokens.skipToken();
        
        // Recursively parse the element type
        TypeNode elementType = parseType();
        
        // Build the composite type
        varType = new ListTypeNode(elementType);
        
        // Skip closing '>'
        token = tokens.getRightAngleBracket();
        if (! (token instanceof GreaterOperatorToken))
          throw new ParserException("Expected '>' to complete type specification, but found " + token.toString(), token);
        token = tokens.getNext();
      }
    } else if (varType instanceof MapTypeNode){
      // Check for '<' for specification of element type
      if (token instanceof LessOperatorToken){
        // Skip over the opening '<'
        tokens.skipToken();
        
        // Recursively parse the element type, expecting key type next
        TypeNode keyType = parseType();
        
        // Check for a second type, the entry type - map can be <key-type, entry-type> or just <entry-type>
        token = tokens.get();
        TypeNode entryType = null;
        if (token instanceof CommaOperatorToken){
          // skip over the ','
          tokens.getNext();
          
          // Parse the second, entry type
          entryType = parseType();
          token = tokens.get();
        } else {
          // The single type is actually the entry type and key type is 'string'
          entryType = keyType;
          keyType = StringTypeNode.one;
        }
        
        // Build the composite type
        varType = new MapTypeNode(keyType, entryType);
        
        // Skip closing '>'
        token = tokens.getRightAngleBracket();
        if (! (token instanceof GreaterOperatorToken))
          throw new ParserException("Expected '>' to complete type specification, but found " + token.toString(), token);
        tokens.skipToken();
      }
    } 


    // Return the parsed type
    return varType;
  }

  protected BreakStatementNode parseBreakStatement() throws ParserException {
    // Skip the opening 'break' keyword
    tokens.getNext();

    // Parse the required semicolon that ends the statement
    parseSemicolon();

    return new BreakStatementNode();
  }

  protected ContinueStatementNode parseContinueStatement() throws ParserException {
    // Skip the opening 'continue' keyword
    tokens.getNext();

    // Parse the required semicolon that ends the statement
    parseSemicolon();

    return new ContinueStatementNode();
  }

  protected StatementNode parseStatement() throws ParserException {
    Token token = tokens.get();
    if (token instanceof BreakKeywordToken)
      return parseBreakStatement();
    else if (token instanceof ContinueKeywordToken)
      return parseContinueStatement();
    else if (token instanceof DoKeywordToken)
      return parseDoStatement();
    else if (token instanceof ForKeywordToken)
      return parseForStatement();
    else if (token instanceof IfKeywordToken)
      return parseIfStatement();
    else if (token instanceof WhileKeywordToken)
      return parseWhileStatement();
    else if (token instanceof ReturnKeywordToken)
      return parseReturnStatement();
    else if (token instanceof SwitchKeywordToken)
      return parseSwitchStatement();
    else if (token instanceof TryKeywordToken)
      return parseTryStatement();
    else if (token instanceof LeftBraceOperatorToken)
      return parseBlockStatement();
    else if (token instanceof SemicolonOperatorToken){
      // Eat the semicolon that terminates a null (empty) statement
      tokens.getNext();

      // Generate a null statement node
      return new NullStatementNode();
    } else
      return parseExpressionStatement();
  }

  protected ForStatementNode parseForStatement() throws ParserException {
    // Save previous block state
    String savedBlockCategoryName = blockCategoryName;
    List<Symbol> savedLocalVariables = localVariables;
    Map<String, Symbol> savedLocalVariableMap = localVariableMap;

    // Start a new symbol category for this block
    int blockId = this.blockId++;
    blockCategoryName = "block-" + blockId;

    // Keep track of local variables
    localVariables = new ArrayList<Symbol>();
    localVariableMap = new HashMap<String, Symbol>();

    StatementNode initialExpression = NullStatementNode.one;
    ExpressionNode conditionExpression = NullExpressionNode.one;
    ExpressionNode incrementExpression = NullExpressionNode.one;
    StatementNode bodyStatement = NullStatementNode.one;

    // TODO: Support iterators "enhanced for statement" of Java

    // Skip the opening 'for' keyword
    tokens.getNext();

    // Skip the '(' after 'for'
    Token token = tokens.get();
    if (! (token instanceof LeftParenthesisOperatorToken))
      throw new ParserException("Expected '(' after 'for' but found " + token.getClass().getSimpleName());
    token = tokens.getNext();

    // Parse the optional initialization expression (statement) - and its required semicolon
    if (token instanceof SemicolonOperatorToken)
      token = tokens.getNext();
    else
      initialExpression = parseExpressionStatement();

    // Parse the optional condition expression 
    if (! (token instanceof SemicolonOperatorToken))
      conditionExpression = parseExpression();

    // Parse the required semicolon after the condition expression
    token = tokens.get();
    if (! (token instanceof SemicolonOperatorToken))
      throw new ParserException("Expected ';' after condition expression of 'for' statement but found " + token.getClass().getSimpleName());
    token = tokens.getNext();

    // Parse the optional increment expression
    if (! (token instanceof RightParenthesisOperatorToken))
      incrementExpression = parseExpressionList();

    // Parse the ')' after increment expression
    token = tokens.get();
    if (! (token instanceof RightParenthesisOperatorToken))
      throw new ParserException("Expected ')' after 'for' increment expression but found " + token.getClass().getSimpleName());
    tokens.getNext();

    // Parse the 'for' body statement
    bodyStatement = parseStatement();

    // Restore previous block state
    // TODO: Put this in try finally
    // Remove symbols defined in this statement
    blockSymbolManager.removeCategory(blockCategoryName);
    blockCategoryName = savedBlockCategoryName;
    localVariables = savedLocalVariables;
    localVariableMap = savedLocalVariableMap;

    return new ForStatementNode(initialExpression, conditionExpression, incrementExpression, bodyStatement);
  }

  protected IfStatementNode parseIfStatement() throws ParserException {
    ExpressionNode condition = null;
    StatementNode thenStatement = null;
    StatementNode elseStatement = null;

    // Skip the opening 'if' keyword
    tokens.getNext();

    // Skip the '(' after 'if'
    Token token = tokens.get();
    if (! (token instanceof LeftParenthesisOperatorToken))
      throw new ParserException("Expected '(' after 'if' but found " + token.getClass().getSimpleName());
    tokens.getNext();

    // Parse the condition expression 
    condition = parseExpression();

    // Skip the ')' after condition expression
    token = tokens.get();
    if (! (token instanceof RightParenthesisOperatorToken))
      throw new ParserException("Expected ')' after 'if' condition but found " + token.getClass().getSimpleName());
    tokens.getNext();

    // Parse the 'then' statement
    thenStatement = parseStatement();

    // Parse the optional 'else' clause
    token = tokens.get();
    if (token instanceof ElseKeywordToken){
      // Skip 'else'
      tokens.getNext();

      // Parse the 'else' statement
      elseStatement = parseStatement();
    }

    return new IfStatementNode(condition, thenStatement, elseStatement);
  }

  protected DoStatementNode parseDoStatement() throws ParserException {
    ExpressionNode conditionExpression = NullExpressionNode.one;
    StatementNode bodyStatement = NullStatementNode.one;

    // Skip the opening 'do' keyword
    tokens.getNext();

    // Parse the 'do' body statement
    bodyStatement = parseStatement();

    // Parse the while clause
    Token token = tokens.get();
    if (! (token instanceof WhileKeywordToken))
      throw new ParserException("Expected 'while' after 'do' statement but found " + token.getClass().getSimpleName());
    tokens.getNext();

    // Skip the '(' after 'while'
    token = tokens.get();
    if (! (token instanceof LeftParenthesisOperatorToken))
      throw new ParserException("Expected '(' after 'while' but found " + token.getClass().getSimpleName());
    token = tokens.getNext();

    // Parse the condition expression 
    conditionExpression = parseExpression();

    // Parse the ')' after condition expression
    token = tokens.get();
    if (! (token instanceof RightParenthesisOperatorToken))
      throw new ParserException("Expected ')' after 'while' condition expression but found " + token.getClass().getSimpleName());
    tokens.getNext();

    // Parse required semicolon
    parseSemicolon();

    // Generate the 'do' statement node
    return new DoStatementNode(conditionExpression, bodyStatement);
  }

  protected WhileStatementNode parseWhileStatement() throws ParserException {
    // Save previous block state
    String savedBlockCategoryName = blockCategoryName;
    List<Symbol> savedLocalVariables = localVariables;
    Map<String, Symbol> savedLocalVariableMap = localVariableMap;

    // Start a new symbol category for this block
    int blockId = this.blockId++;
    blockCategoryName = "block-" + blockId;

    // Keep track of local variables
    localVariables = new ArrayList<Symbol>();
    localVariableMap = new HashMap<String, Symbol>();

    ExpressionNode conditionExpression = NullExpressionNode.one;
    StatementNode bodyStatement = NullStatementNode.one;

    // Skip the opening 'while' keyword
    tokens.getNext();

    // Skip the '(' after 'while'
    Token token = tokens.get();
    if (! (token instanceof LeftParenthesisOperatorToken))
      throw new ParserException("Expected '(' after 'while' but found " + token.getClass().getSimpleName());
    token = tokens.getNext();

    // Parse the condition expression 
    conditionExpression = parseExpression();

    // Parse the ')' after condition expression
    token = tokens.get();
    if (! (token instanceof RightParenthesisOperatorToken))
      throw new ParserException("Expected ')' after 'while' condition expression but found " + token.getClass().getSimpleName());
    tokens.getNext();

    // Parse the 'while' body statement
    bodyStatement = parseStatement();

    // Restore previous block state
    // TODO: Put this in try finally
    // Remove symbols defined in this statement
    blockSymbolManager.removeCategory(blockCategoryName);
    blockCategoryName = savedBlockCategoryName;
    localVariables = savedLocalVariables;
    localVariableMap = savedLocalVariableMap;

    return new WhileStatementNode(conditionExpression, bodyStatement);
  }

  protected ReturnStatementNode parseReturnStatement() throws ParserException {
    // Skip the opening 'return' keyword
    tokens.getNext();

    // Parse the optional return value expression 
    ExpressionNode returnExpr = null;
    if (! (tokens.get() instanceof SemicolonOperatorToken))
      returnExpr = parseExpression();

    // Parse the required semicolon that ends the statement
    parseSemicolon();

    return new ReturnStatementNode(returnExpr);
  }

  protected void parseSemicolon() throws ParserException {
    // Parse the required semicolon that ends the statement
    Token token = tokens.get();
    if (! (token instanceof SemicolonOperatorToken))
      throw new ParserException("Expected ';' to end statement but found " + token.toString());
    tokens.getNext();
  }

  protected SwitchStatementNode parseSwitchStatement(){
    ExpressionNode expression = null;
    List<CaseStatementNode> caseStatements = null;

    return new SwitchStatementNode(expression, caseStatements);
  }

  protected ThrowStatementNode parseThrowStatement() throws ParserException {
    // Skip the opening 'throw' keyword
    tokens.getNext();

    // Parse the optional throw value expression 
    ExpressionNode throwExpr = null;
    if (! (tokens.get() instanceof SemicolonOperatorToken))
      throwExpr = parseExpression();

    // Parse the required semicolon that ends the statement
    Token token = tokens.get();
    if (! (token instanceof SemicolonOperatorToken))
      throw new ParserException("Expected ';' to end statement but found " + token.getClass().getSimpleName());
    tokens.getNext();

    return new ThrowStatementNode(throwExpr);
  }

  protected TryStatementNode parseTryStatement(){
    BlockStatementNode blockStatement = null;
    List<CatchStatementNode> catchStatements = new ArrayList<CatchStatementNode>();

    return new TryStatementNode(blockStatement, catchStatements);
  }

  protected StatementNode parseExpressionStatement() throws ParserException {
    // Parse a list of comma-separated statement expressions
    // Each statement expression could be either a simple expression or a variable declaraction

    List<StatementNode> expressionStatementList = new ArrayList<StatementNode>();
    Token token;
    while (true) {
      // TODO: Make this more sophisticated, support list/map references

      // Parse optional var declaration
      token = tokens.get();
      StatementNode statementNode = null;
      if (token instanceof TypeKeywordToken){
        // Parse the variable type
        TypeNode varType = parseType();

        // Parse variable name
        Token varNameToken = tokens.get();
        if (! (varNameToken instanceof IdentifierToken))
          throw new ParserException("Expected identifier for assignment statement but found: " + varNameToken.getClass().getSimpleName());
        String variableName = ((IdentifierToken)varNameToken).identifier;
        token = tokens.getNext();

        // Make sure user hasn't already declared the name
        if (localVariableMap.containsKey(variableName))
          throw new ParserException("Local Variable '" + variableName + "' is already declared in this block");

        // Add new variable name for this block
        Symbol symbol = null;
        try {
          symbol = blockSymbolManager.put(blockCategoryName, variableName, varType);
        } catch (SymbolException e){
          throw new ParserException("Parser Exception: " + e.getMessage());
        }
        localVariableMap.put(variableName, symbol);
        localVariables.add(symbol);

        // Create a variable reference node
        VariableReferenceNode varRefNode = new VariableReferenceNode(symbol);

        // Parse the '='
        if (! (token instanceof EqualOperatorToken))
          throw new ParserException("Expected '=' after variable name for assignment statement but found: " + varNameToken.getClass().getSimpleName());
        tokens.getNext();

        // Parse the expression
        ExpressionNode expressionNode = parseExpression();

        // Generate the assignment statement node
        statementNode = new AssignmentStatementNode(varRefNode, expressionNode);
      } else {
        // Parse an expression that may be a variable reference followed by '=' for assigment
        Token startToken = token;

        // Parse the expression
        ExpressionNode expressionNode = parseExpression();

        // Check for '=' for an assignment
        token = tokens.get();
        if (token instanceof EqualOperatorToken){
          // Skip over the '='
          tokens.getNext();

          // Make sure left hand side expression was a reference
          if (! (expressionNode instanceof ReferenceNode))
            throw new ParserException("Left hand side of assignment is not a variable: " + expressionNode.toString(), startToken);
          ReferenceNode refNode = (ReferenceNode)expressionNode;

          // Parse the expression on the right hand side of the assignment
          ExpressionNode rightExpressionNode = parseExpression();

          // Generate the assignment statement node
          statementNode = new AssignmentStatementNode(refNode, rightExpressionNode);
        } else {
          // Generate an expression statement node for simple expression
          statementNode = new ExpressionStatementNode(expressionNode);
        }
      }

      // Add statement to the list
      expressionStatementList.add(statementNode);

      // See if we have a comma
      token = tokens.get();
      if (token instanceof CommaOperatorToken){
        // Skip the comma
        token = tokens.getNext();
      } else {
        // That's the end of the comma-separated expression list
        break;
      }
    }

    // Parse the required semicolon that ends the statement
    parseSemicolon();

    // Generate the expression statement list node
    // But skip it if there is only one expression statement
    int numStatements = expressionStatementList.size();
    if (numStatements == 0)
      return null;
    else if (numStatements == 1)
      return expressionStatementList.get(0);
    else
      return new ExpressionStatementListNode(expressionStatementList);
  }

  protected ExpressionNode parseExpressionList() throws ParserException {
    // Parse a single expression
    ExpressionNode expressionNode = parseExpression();
    Token token = tokens.get();

    // Do we have a comma-separated list of expressions?
    if (! (token instanceof CommaOperatorToken)){
      // No, simply return the single expression
      return expressionNode;
    }

    // We have a comma-separated list, so begin accumulating the list of expressions
    List<ExpressionNode> expressionList = new ArrayList<ExpressionNode>();
    expressionList.add(expressionNode);

    // Parse a comma-separated list of expressions
    while (token instanceof CommaOperatorToken) {
      // Skip over the comma
      tokens.skipToken();

      // Parse the next expression in the list
      ExpressionNode newExpressionNode = parseExpression();
      token = tokens.get();

      // Append to list of parsed expressions
      expressionList.add(newExpressionNode);
    }

    // Generate the expression list node
    return new ExpressionListNode(expressionList);
  }
  protected ExpressionNode parseExpression() throws ParserException {
    // TODO: Maybe allow a type?
    // Check for assignment expression
    Token token = tokens.get();
    if (((token instanceof TypeKeywordToken) && ! (tokens.peek(1) instanceof LeftParenthesisOperatorToken)) ||
        ((token instanceof IdentifierToken) &&
            ((tokens.peek(1) instanceof AnyEqualOperatorToken) ||
                (tokens.peek(1) instanceof PeriodOperatorToken) &&
                (tokens.peek(2) instanceof IdentifierToken) &&
                (tokens.peek(3) instanceof AnyEqualOperatorToken)))){
      // Parse assignment expression
      TypeNode varType = null;
      ReferenceNode varRefNode = null;
      if (token instanceof TypeKeywordToken){
        // Parse the variable type
        varType = parseType();

        // Parse variable name
        Token varNameToken = tokens.get();
        if (! (varNameToken instanceof IdentifierToken))
          throw new ParserException("Expected identifier for assignment statement but found: " + varNameToken.getClass().getSimpleName());
        String variableName = ((IdentifierToken)varNameToken).identifier;
        token = tokens.getNext();

        // Make sure user hasn't already declared the name
        if (localVariableMap.containsKey(variableName))
          throw new ParserException("Local Variable '" + variableName + "' is already declared in this block");

        // Add new variable name for this block
        Symbol symbol = null;
        try {
          symbol = blockSymbolManager.put(blockCategoryName, variableName, varType);
        } catch (SymbolException e){
          throw new ParserException("Parser Exception: " + e.getMessage());
        }
        localVariableMap.put(variableName, symbol);
        localVariables.add(symbol);

        // Create a variable reference node
        varRefNode = new VariableReferenceNode(symbol);

        // Parse the '='
        if (! (token instanceof EqualOperatorToken))
          throw new ParserException("Expected '=' after variable name for assignment statement but found: " + varNameToken.getClass().getSimpleName());
        tokens.getNext();

        // Parse the expression
        ExpressionNode expressionNode = parseExpression();

        // Generate the assignment node
        return new AssignmentNode(varRefNode, expressionNode);
      } else {
        // Parse variable name
        Token varNameToken = tokens.get();
        if (! (varNameToken instanceof IdentifierToken))
          throw new ParserException("Expected identifier for assignment statement but found: " + varNameToken.getClass().getSimpleName());
        String variableName = ((IdentifierToken)varNameToken).identifier;
        token = tokens.getNext();

        // Check for dotted reference for category name or map field reference
        Symbol symbol = null;
        if (token instanceof PeriodOperatorToken){
          // Skip over the dot
          varNameToken = tokens.getNext();

          // Parse the variable name
          if (! (varNameToken instanceof IdentifierToken))
            throw new ParserException("Expected identifier for assignment statement but found: " + varNameToken.toString());
          String categoryName = variableName;
          variableName = ((IdentifierToken)varNameToken).identifier;
          token = tokens.getNext();

          // This may be a category reference or a name/field reference
          if (symbolManager.isCategory(categoryName)){

            // Look up the named variable in the named category
            try {
              symbol = symbolManager.get(categoryName, variableName);
            } catch (SymbolException e){
              throw new ParserException(e.getMessage());
            }

            // Create a variable reference node
            varRefNode = new VariableReferenceNode(symbol);
          } else {
            // Make sure variable is defined
            try {
              // Look in block stack first
              symbol = blockSymbolManager.get(categoryName);
              if (symbol == null)
                symbol = symbolManager.get(categoryName);
            } catch (SymbolException e){
              throw new ParserException(e.getMessage());
            }

            // Create a variable reference node for the variable
            varRefNode = new VariableReferenceNode(symbol);
            
            // Generate a name/field reference node
            varRefNode = new NameReferenceNode(varRefNode, variableName);
          }
        } else {
          // Make sure variable is defined
          try {
            // Look in block stack first
            symbol = blockSymbolManager.get(variableName);
            if (symbol == null)
              symbol = symbolManager.get(variableName);
          } catch (SymbolException e){
            throw new ParserException(e.getMessage());
          }

          // Create a variable reference node
          varRefNode = new VariableReferenceNode(symbol);
        }

        // Parse the '=' or +=, -=, *=, /=, et al
        if (! (token instanceof AnyEqualOperatorToken))
          throw new ParserException("Expected '=' after variable name for assignment statement but found: " + varNameToken.getClass().getSimpleName());
        tokens.getNext();

        // Parse the expression
        ExpressionNode expressionNode = parseExpression();

        // Generate the operation implied by the specific assignment
        if (token instanceof PlusEqualOperatorToken)
          expressionNode = new AddNode(varRefNode, expressionNode);
        else if (token instanceof MinusEqualOperatorToken)
          expressionNode = new SubtractNode(varRefNode, expressionNode);
        else if (token instanceof AsteriskEqualOperatorToken)
          expressionNode = new MultiplyNode(varRefNode, expressionNode);
        else if (token instanceof SlashEqualOperatorToken)
          expressionNode = new DivideNode(varRefNode, expressionNode);
        // TODO: The others: %=, |=, &=, ^=

        // Generate the assignment node
        return new AssignmentNode(varRefNode, expressionNode);
      }
    } else
      // Parse non-assignment expression
      return parseTernaryCondition();
  }

  protected ExpressionNode parseTernaryCondition() throws ParserException {
    // Parse initial logical expression
    ExpressionNode node = parseLogicalOrOperatorSequence();

    // Check for the '?' conditional operator
    Token token = tokens.get();
    if (token instanceof QuestionMarkOperatorToken){
      // Skip over the '?' operator
      tokens.skipToken();

      // Parse the left conditional term
      ExpressionNode leftNode = parseLogicalOrOperatorSequence();

      // Check for mandatory ':' operator
      token = tokens.get();
      if (! (token instanceof ColonOperatorToken))
        throw new ParserException("Expected ':' operator to match '?' operator, but found " + token.toString(), token);
      // Skip over the ':' operator
      tokens.skipToken();

      // Parse the right conditional term (? : is right-associative)
      ExpressionNode rightNode = parseTernaryCondition();

      // Compose a node for the operation
      node = new TernaryConditionNode(node, leftNode, rightNode);
    }

    return node;
  }

  protected ExpressionNode parseLogicalOrOperatorSequence() throws ParserException {
    // Parse initial logical expression OR term
    ExpressionNode node = parseLogicalAndOperatorSequence();

    // Now parse a sequence of logical OR operators
    do {
      Token token = tokens.get();

      if (! (token instanceof LogicalOrOperatorToken))
        // Done parsing the sequence of logical OR terms
        return node;

      // Skip over the operator
      tokens.skipToken();

      // Parse the next logical OR term in the sequence
      ExpressionNode rightNode = parseLogicalAndOperatorSequence();

      // Compose a node for the operation
      if (token instanceof LogicalAndOperatorToken)
        node = new LogicalAndNode(node, rightNode);
      else
        node = new LogicalOrNode(node, rightNode);
    } while (true);
  }

  protected ExpressionNode parseLogicalAndOperatorSequence() throws ParserException {
    // Parse initial logical expression AND term
    ExpressionNode node = parseRelationalOperator();

    // Now parse a sequence of logical AND operators
    do {
      Token token = tokens.get();

      if (! (token instanceof LogicalAndOperatorToken))
        // Done parsing the sequence of logical AND terms
        return node;

      // Skip over the operator
      tokens.skipToken();

      // Parse the next logical AND term in the sequence
      ExpressionNode rightNode = parseRelationalOperator();

      // Compose a node for the operation
      if (token instanceof LogicalAndOperatorToken)
        node = new LogicalAndNode(node, rightNode);
      else
        node = new LogicalOrNode(node, rightNode);
    } while (true);
  }

  protected ExpressionNode parseRelationalOperator() throws ParserException {
    // Parse initial expression term sequence
    ExpressionNode node = parseExpressionTermSequence();

    // Check for a relational operator: <, <=, ==, !=, >=, >
    Token token = tokens.get();
    if (! (token instanceof LessOperatorToken) &&
        ! (token instanceof LessEqualsOperatorToken) &&
        ! (token instanceof EqualsOperatorToken) &&
        ! (token instanceof NotEqualsOperatorToken) &&
        ! (token instanceof GreaterEqualsOperatorToken) &&
        ! (token instanceof GreaterOperatorToken))
      // Done parsing the relational operator - it's really just the term sequence
      return node;

    // Skip over the relational operator
    tokens.skipToken();

    // Parse the term sequence after the relational operator
    ExpressionNode rightNode = parseExpressionTermSequence();

    // Compose a node for the relational operation
    if (token instanceof LessOperatorToken)
      return new LessNode(node, rightNode);
    else if (token instanceof LessEqualsOperatorToken)
      return new LessEqualsNode(node, rightNode);
    else if (token instanceof EqualsOperatorToken)
      return new EqualsNode(node, rightNode);
    else if (token instanceof NotEqualsOperatorToken)
      return new NotEqualsNode(node, rightNode);
    else if (token instanceof GreaterEqualsOperatorToken)
      return new GreaterEqualsNode(node, rightNode);
    else
      return new GreaterNode(node, rightNode);
  }

  protected ExpressionNode parseExpressionTermSequence() throws ParserException {
    // Parse initial expression term
    ExpressionNode node = parseExpressionTerm();

    // Now parse a sequence of '+' and '-' operators
    do {
      Token token = tokens.get();

      if (! (token instanceof PlusOperatorToken) &&
          ! (token instanceof MinusOperatorToken))
        // Done parsing the sequence of terms
        return node;

      // Skip over the operator
      tokens.skipToken();

      // Parse the next term in the sequence
      ExpressionNode rightNode = parseExpressionTerm();

      // Compose a node for the operation
      if (token instanceof PlusOperatorToken)
        node = new AddNode(node, rightNode);
      else
        node = new SubtractNode(node, rightNode);
    } while (true);
  }

  protected ExpressionNode parseExpressionTerm() throws ParserException {
    // Parse initial expression factor
    ExpressionNode node = parseExpressionFactor();

    // Now parse a sequence of '*', '/', and '%' operators
    do {
      Token token = tokens.get();

      if (! (token instanceof AsteriskOperatorToken) &&
          ! (token instanceof SlashOperatorToken) &&
          ! (token instanceof PercentSignOperatorToken))
        // Done parsing the sequence of factors
        return node;

      // Skip over the operator
      tokens.skipToken();

      // Parse the next factor in the sequence
      ExpressionNode rightNode = parseExpressionFactor();

      // Compose a node for the operation
      if (token instanceof AsteriskOperatorToken)
        node = new MultiplyNode(node, rightNode);
      else if (token instanceof SlashOperatorToken)
        node = new DivideNode(node, rightNode);
      else
        node = new RemainderNode(node, rightNode);
    } while (true);
  }

  protected ExpressionNode parseExpressionFactor() throws ParserException {
    // Check for pre-increment/decrement operators, '++' or '--'
    Token prefixToken = tokens.get();
    Token token;
    if ((prefixToken instanceof PlusPlusOperatorToken) || (prefixToken instanceof MinusMinusOperatorToken))
      // Skip the operator, for now
      token = tokens.getNext();
    else
      token = prefixToken;

    // Parse the expression primary
    ExpressionNode node = parseExpressionPrimary();
    
    // Apply the pre-increment/decrement operator, is present
    if (prefixToken instanceof PlusPlusOperatorToken)
      node = new PreIncrementNode(node);
    else if (prefixToken instanceof MinusMinusOperatorToken)
      node = new PreDecrementNode(node);

    // Check for optional post-increment/decrement operator, ++ or --
    token = tokens.get();
    if (token instanceof PlusPlusOperatorToken){
      node = new PostIncrementNode(node);
      tokens.skipToken();
    } else if (token instanceof MinusMinusOperatorToken){
      node = new PostDecrementNode(node);
      tokens.skipToken();
    }

    return node;
  }

  protected ExpressionNode parseExpressionPrimary() throws ParserException {
    // Peek at the next token
    Token token = tokens.get();

    ExpressionNode node;
    if (token instanceof NullToken){
      node = NullValue.one;
      tokens.skipToken();
    } else if (token instanceof FalseToken){
      node = FalseValue.one;
      tokens.skipToken();
    } else if (token instanceof TrueToken){
      node = TrueValue.one;
      tokens.skipToken();
    } else if (token instanceof NowKeywordToken){
      node = NowValue.one;
      tokens.skipToken();
    } else if (token instanceof IntegerToken){
      node = new IntegerValue(((IntegerToken)token).number);
      tokens.skipToken();
    } else if (token instanceof FloatToken){
      node = new FloatValue(((FloatToken)token).number);
      tokens.skipToken();
    } else if (token instanceof StringToken){
      node = new StringValue(((StringToken)token).string);
      tokens.skipToken();
    } else if (token instanceof IdentifierToken){
      // Initial name may be a field name, a category name, or a local variable name
      String name = ((IdentifierToken)token).identifier;

      // Skip over the identifier
      token = tokens.getNext();

      // Check for simple function call
      if (token instanceof LeftParenthesisOperatorToken){
        // Skip over the opening '('
        token = tokens.getNext();

        // parse the comma-separated list of function arguments
        List<ExpressionNode> argumentList = new ArrayList<ExpressionNode>();
        while (true){
          if (token instanceof RightParenthesisOperatorToken){
            // Eat the close ')' that terminates the function argument list
            token = tokens.getNext();
            break;
          }
          if (token instanceof EndToken || token instanceof SemicolonOperatorToken ||
              token instanceof RightBraceOperatorToken)
            throw new ParserException("Unterminated function argument list; expected ',' or ')', but encountered " + token.toString(), token);

          // Parse the next function argument expression
          ExpressionNode argumentExpression = parseExpression();

          // Check for comma to continue list
          token = tokens.get();
          if (token instanceof CommaOperatorToken) {
            // Skip over the comma
            token = tokens.getNext();
          }

          // Append to accumulated function argument list
          argumentList.add(argumentExpression);
        }

        // Generate the function call node
        node = new FunctionCallNode(name, argumentList);
      } else {
        // Check for dotted reference for category name or field reference
        Symbol symbol = null;
        if (token instanceof PeriodOperatorToken && symbolManager.isCategory(name)){
          // Skip over the dot
          token = tokens.getNext();

          // Parse the variable name
          if (! (token instanceof IdentifierToken))
            throw new ParserException("Expected identifier for assignment statement but found: " + token.toString(), token);
          String variableName = ((IdentifierToken)token).identifier;
          token = tokens.getNext();

          // Look up the named variable in the named category
          String categoryName = name;
          try {
            symbol = symbolManager.get(categoryName, variableName, false);
          } catch (SymbolException e){
            throw new ParserException(e.getMessage());
          }
        } else {
          // Lookup the symbol name
          try {
            if (blockSymbolManager != null)
              symbol = blockSymbolManager.get(null, name, true);
            if (symbol == null)
              symbol = symbolManager.get(null, name, false);
          } catch (SymbolException e){
            throw new ParserException(e.getMessage());
          }
        }

        // Generate a variable reference node
        node = new VariableReferenceNode(symbol);
      }
    } else if (token instanceof LeftParenthesisOperatorToken){
      // Skip over the '('
      tokens.skipToken();

      // Parse the optional parenthesized expression, which may be a comma-separated list
      ExpressionNode expressionNode = parseExpressionList();

      // Skip over the ')'
      if (! (tokens.get() instanceof RightParenthesisOperatorToken))
        throw new ParserException("Expected expression primary, but found: " + token.getClass().getSimpleName());
      tokens.skipToken();

      // Returned the parsed parenthesized expression
      node = expressionNode;
    } else if (token instanceof MinusOperatorToken){
      // Skip over the unary '-'
      tokens.skipToken();

      // Parse the primary to be negated
      ExpressionNode expressionNode = parseExpressionFactor();

      // Returned the parsed negation expression
      node = new NegationNode(expressionNode);
    } else if (token instanceof LogicalNotOperatorToken){
      // Skip over the unary '!'
      tokens.skipToken();

      // Parse the primary to be negated
      ExpressionNode expressionNode = parseExpressionFactor();

      // Returned the parsed negation expression
      node = new LogicalNotNode(expressionNode);
    } else if (token instanceof ListKeywordToken){
      // 'list' should be followed by '(' to start a list literal
      token = tokens.getNext();
      if (! (token instanceof LeftParenthesisOperatorToken))
        throw new ParserException("Expected '(' after 'list' for a list literal, but found: " + token.toString(), token);

      // Skip over the opening '('
      token = tokens.getNext();

      // parse the comma-separated list of list elements
      List<ExpressionNode> elementList = new ArrayList<ExpressionNode>();
      while (true){
        if (token instanceof RightParenthesisOperatorToken){
          // Eat the close ')' that terminates the element list
          token = tokens.getNext();
          break;
        }
        if (token instanceof EndToken || token instanceof SemicolonOperatorToken ||
            token instanceof RightBraceOperatorToken)
          throw new ParserException("Unterminated list; expected ',' or ')', but encountered " + token.toString(), token);

        // Parse the next function argument expression
        ExpressionNode elementExpression = parseExpression();

        // Append to accumulated element list
        elementList.add(elementExpression);

        // Check for comma to continue list
        token = tokens.get();
        if (token instanceof CommaOperatorToken) {
          // Skip over the comma
          token = tokens.getNext();
        } else if (! (token instanceof RightParenthesisOperatorToken)) {
          throw new ParserException("Unterminated list; expected ',' or ')', but encountered " + token.toString(), token);
        }
      }

      // Generate the 'new' node to create instance of a list
      node = new NewNode(ListTypeNode.one, elementList);
    } else if (token instanceof LeftSquareBracketOperatorToken){
      // '[' introduces a comma-separated list literal
      token = tokens.getNext();

      // parse the comma-separated list of list elements
      List<ExpressionNode> elementList = new ArrayList<ExpressionNode>();
      while (true){
        if (token instanceof RightSquareBracketOperatorToken){
          // Eat the close ']' that terminates the element list
          token = tokens.getNext();
          break;
        }
        if (token instanceof EndToken || token instanceof SemicolonOperatorToken ||
            token instanceof RightBraceOperatorToken)
          throw new ParserException("Unterminated list; expected ',' or ']', but encountered " + token.toString(), token);

        // Parse the next list element expression
        ExpressionNode elementExpression = parseExpression();

        // Append to accumulated element list
        elementList.add(elementExpression);

        // Check for comma to continue list
        token = tokens.get();
        if (token instanceof CommaOperatorToken) {
          // Skip over the comma
          token = tokens.getNext();
        } else if (! (token instanceof RightSquareBracketOperatorToken)) {
          throw new ParserException("Unterminated list; expected ',' or ')', but encountered " + token.toString(), token);
        }
      }

      // Generate the 'new' node to create instance of a list
      node = new NewNode(ListTypeNode.one, elementList);
    } else if (token instanceof MapKeywordToken){
      // 'map' should be followed by '(' to start a map literal
      token = tokens.getNext();
      if (! (token instanceof LeftParenthesisOperatorToken))
        throw new ParserException("Expected '(' after 'map' for a map literal, but found: " + token.toString(), token);

      // Skip over the opening '('
      token = tokens.getNext();

      // parse the comma-separated map of map elements
      List<ExpressionNode> elementList = new ArrayList<ExpressionNode>();
      while (true){
        if (token instanceof RightParenthesisOperatorToken){
          // Eat the close ')' that terminates the element map
          token = tokens.getNext();
          break;
        }
        if (token instanceof EndToken || token instanceof SemicolonOperatorToken ||
            token instanceof RightBraceOperatorToken)
          throw new ParserException("Unterminated map; expected ',' or ')', but encountered " + token.toString(), token);

        // Parse the next map element field name
        if (! (token instanceof IdentifierToken))
          throw new ParserException("Expected map field name, but found " + token.toString(), token);
        String fieldName = ((IdentifierToken)token).identifier;
        token = tokens.getNext();
        
        // Parse ':' or '=' that separates map field name from its value expression
        if (! (token instanceof ColonOperatorToken) && ! (token instanceof EqualOperatorToken))
          throw new ParserException("Expected ':' after map field name, but found " + token.toString(), token);
        token = tokens.getNext();
        
        // Parse the next field value expression
        ExpressionNode elementExpression = parseExpression();

        // Generate a field name and expression node pair
        FieldNode fieldNode = new FieldNode(fieldName, elementExpression);
        
        // Append to accumulated element map
        elementList.add(fieldNode);

        // Check for comma to continue to map
        token = tokens.get();
        if (token instanceof CommaOperatorToken) {
          // Skip over the comma
          token = tokens.getNext();
        } else if (! (token instanceof RightParenthesisOperatorToken)) {
          throw new ParserException("Unterminated map; expected ',' or ')', but encountered " + token.toString(), token);
        }
      }

      // Generate the 'new' node to create instance of a map
      node = new NewNode(MapTypeNode.one, elementList);
    } else if (token instanceof LeftBraceOperatorToken){
      // '{' introduces a comma-separated map literal
      token = tokens.getNext();

      // parse the comma-separated map of map elements
      List<ExpressionNode> elementList = new ArrayList<ExpressionNode>();
      while (true){
        if (token instanceof RightBraceOperatorToken){
          // Eat the close '}' that terminates the element map
          token = tokens.getNext();
          break;
        }
        if (token instanceof EndToken || token instanceof SemicolonOperatorToken ||
            token instanceof RightBraceOperatorToken)
          throw new ParserException("Unterminated map; expected ',' or ')', but encountered " + token.toString(), token);

        // Parse the next map element field name
        if (! (token instanceof IdentifierToken))
          throw new ParserException("Expected map field name, but found " + token.toString(), token);
        String fieldName = ((IdentifierToken)token).identifier;
        token = tokens.getNext();
        
        // Parse ':' or '=' that separates map field name from its value expression
        if (! (token instanceof ColonOperatorToken) && ! (token instanceof EqualOperatorToken))
          throw new ParserException("Expected ':' after map field name, but found " + token.toString(), token);
        token = tokens.getNext();
        
        // Parse the next field value expression
        ExpressionNode elementExpression = parseExpression();

        // Generate a field name and expression node pair
        FieldNode fieldNode = new FieldNode(fieldName, elementExpression);
        
        // Append to accumulated element map
        elementList.add(fieldNode);

        // Check for comma to continue to map
        token = tokens.get();
        if (token instanceof CommaOperatorToken) {
          // Skip over the comma
          token = tokens.getNext();
        } else if (! (token instanceof RightBraceOperatorToken)) {
          throw new ParserException("Unterminated map; expected ',' or ')', but encountered " + token.toString(), token);
        }
      }

      // Generate the 'new' node to create instance of a map
      node = new NewNode(MapTypeNode.one, elementList);
    } else
      throw new ParserException("Expected expression primary, but found: " + token.getClass().getSimpleName());

    // Now parse accessors such as dot field references, method function calls, and subscripts
    node = parseAccessors(node);
    
    return node;
  }

  protected ExpressionNode parseAccessors(ExpressionNode node) throws ParserException {
    Token token = tokens.get();

    do {
      // Check for dot
      if (token instanceof PeriodOperatorToken){
        // Skip over the dot
        token = tokens.getNext();
        
        // Check for name or type name (for type conversions)
        String dottedName;
        if (token instanceof IdentifierToken){
          // Get the name
          dottedName = ((IdentifierToken)token).identifier;
        } else if (token instanceof TypeKeywordToken){
          // Get the type keyword and use as name for conversion functions
          dottedName = token.toString();
        } else
          throw new ParserException("Expected identifier or type keyword after dot, but found: " + token.toString(), token);
        
        // Skip over the name or type keyword
        token = tokens.getNext();

        // Check for '(' for function call
        if (token instanceof LeftParenthesisOperatorToken){
          // Parse function call argument list
          // Skip over the opening '('
          token = tokens.getNext();

          // Parse the optional function call argument expression list
          List<ExpressionNode> arguments = new ArrayList<ExpressionNode>();
          do {
            // Check if we hit the end of the argument list
            token = tokens.get();
            if (token instanceof RightParenthesisOperatorToken)
              // End of argument list
              break;

            // Parse the next argument expression
            ExpressionNode argument = parseExpression();

            // Add the subscript to the list
            arguments.add(argument);

            // Check for comma for continuing list
            token = tokens.get();
            if (token instanceof CommaOperatorToken)
              // Skip the comma
              token = tokens.getNext();
            else
              // Done with argument list
              break;
          } while (true);

          // Parse the closing ')'
          if (! (token instanceof RightParenthesisOperatorToken))
            throw new ParserException("Expected ')' to terminate argument list, but found " + token.toString(), token);
          token = tokens.getNext();

          // Generate the function call reference node
          node = new MethodReferenceNode(node, dottedName, arguments);
        } else {
          // This is a simple name reference
          node = new NameReferenceNode(node, dottedName);
        }
      } else if (token instanceof LeftSquareBracketOperatorToken){
        // Parse a subscript reference
        // Skip over the opening '['
        token = tokens.getNext();

        // Parse the subscript expression list
        List<ExpressionNode> subscripts = new ArrayList<ExpressionNode>();
        do {
          // Parse the next subscript
          ExpressionNode subscript = parseExpression();

          // Add the subscript to the list
          subscripts.add(subscript);

          // Check for comma for continuing list
          token = tokens.get();
          if (token instanceof CommaOperatorToken)
            // Skip the comma
            token = tokens.getNext();
          else
            // Done with subscript list
            break;
        } while (true);

        // Parse the closing ']'
        if (! (token instanceof RightSquareBracketOperatorToken))
          throw new ParserException("Expected ']' to terminate subscript list, but found " + token.toString(), token);
        token = tokens.getNext();

        // Generate the subscript reference node
        node = new SubscriptedReferenceNode(node, subscripts);
      } else
        break;

      // Peek at the next token
      token = tokens.get();
    } while (true);

    // Return the primary after the accessors have been applied
    return node;
  }
}
