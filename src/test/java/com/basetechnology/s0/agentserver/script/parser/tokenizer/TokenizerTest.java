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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.basetechnology.s0.agentserver.script.parser.tokenizer.NewKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.TokenList;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.Tokenizer;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.AsteriskEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.AsteriskOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.AtSignOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.BitwiseAndEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.BitwiseAndOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.BitwiseOrEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.BooleanKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.CaretOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.CaseKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.CatchKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.CharKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ColonOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.CommaOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.DateKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.DefaultKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.DoKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.DollarSignOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.DoubleKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ElseKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.EqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.EqualsOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ExclusiveOrEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.FalseToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.FloatKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.FloatToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.GreaterEqualsOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.GreaterGreaterGreaterOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.GreaterGreaterOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.GreaterOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.IdentifierToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.IfKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.IntKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.IntegerToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LeftBraceOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LeftParenthesisOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LeftSquareBracketOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LessEqualsOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LessLessOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LessOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ListKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LogicalAndOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LogicalNotOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LogicalOrOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.LongKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.MapKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.MinusEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.MinusMinusOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.MinusOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.NotEqualsOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.NowKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.NullToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ObjectKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PercentSignOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PeriodOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PlusEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PlusOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PlusPlusOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.PoundSignOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.QuestionMarkOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ReturnKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.RightBraceOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.RightParenthesisOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.RightSquareBracketOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.SemicolonOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.SlashEqualOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.SlashOperatorToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.StringKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.StringToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.SwitchKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.ThrowKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.TrueToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.TryKeywordToken;
import com.basetechnology.s0.agentserver.script.parser.tokenizer.token.WhileKeywordToken;

public class TokenizerTest {
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void test() throws Exception {
    // Test tokenize of null string
    Tokenizer tokenizeString = new Tokenizer();
    TokenList tokenList = tokenizeString.tokenizeString(null);
    assertEquals("Tokens for null string", null, tokenList);

    // Test tokenize of empty string
    tokenList = tokenizeString.tokenizeString("");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 0, tokenList.size());

    // Test tokenize of white space
    tokenList = tokenizeString.tokenizeString("     \t\t  \n  \t\n   \n");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 0, tokenList.size());

    // Test tokenize of inline comment (and whitespace)
    tokenList = tokenizeString.tokenizeString("     // A comment\n// More comment");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 0, tokenList.size());

    // Test tokenize of C-style comment (and whitespace)
    tokenList = tokenizeString.tokenizeString("     /* A comment\nMore comment\nmore */    ");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 0, tokenList.size());

    // Test tokenize of the single-character operators
    tokenList = tokenizeString.tokenizeString(" ! @ # $ % ^ & * ( ) . , ; : { } [ ] - + = ? / < >");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 25, tokenList.size());
    assertTrue("Logical NOT operator not properly detected", tokenList.get(0) instanceof LogicalNotOperatorToken);
    assertTrue("At-sign operator not properly detected", tokenList.get(1) instanceof AtSignOperatorToken);
    assertTrue("Pound-sign operator not properly detected", tokenList.get(2) instanceof PoundSignOperatorToken);
    assertTrue("Dollar-sign operator not properly detected", tokenList.get(3) instanceof DollarSignOperatorToken);
    assertTrue("Percent-sign operator not properly detected", tokenList.get(4) instanceof PercentSignOperatorToken);
    assertTrue("Caret operator not properly detected", tokenList.get(5) instanceof CaretOperatorToken);
    assertTrue("Bitwise AND operator not properly detected", tokenList.get(6) instanceof BitwiseAndOperatorToken);
    assertTrue("Asterisk operator not properly detected", tokenList.get(7) instanceof AsteriskOperatorToken);
    assertTrue("Left parenthesis operator not properly detected", tokenList.get(8) instanceof LeftParenthesisOperatorToken);
    assertTrue("Right parenthesis operator not properly detected", tokenList.get(9) instanceof RightParenthesisOperatorToken);
    assertTrue("Period operator not properly detected", tokenList.get(10) instanceof PeriodOperatorToken);
    assertTrue("Comma operator not properly detected", tokenList.get(11) instanceof CommaOperatorToken);
    assertTrue("Semicolon operator not properly detected", tokenList.get(12) instanceof SemicolonOperatorToken);
    assertTrue("Colon operator not properly detected", tokenList.get(13) instanceof ColonOperatorToken);
    assertTrue("Left brace operator not properly detected", tokenList.get(14) instanceof LeftBraceOperatorToken);
    assertTrue("Right brace operator not properly detected", tokenList.get(15) instanceof RightBraceOperatorToken);
    assertTrue("Left square bracket operator not properly detected", tokenList.get(16) instanceof LeftSquareBracketOperatorToken);
    assertTrue("Right square bracket operator not properly detected", tokenList.get(17) instanceof RightSquareBracketOperatorToken);
    assertTrue("Minus operator not properly detected", tokenList.get(18) instanceof MinusOperatorToken);
    assertTrue("Plus operator not properly detected", tokenList.get(19) instanceof PlusOperatorToken);
    assertTrue("Equal operator not properly detected", tokenList.get(20) instanceof EqualOperatorToken);
    assertTrue("Question mark operator not properly detected", tokenList.get(21) instanceof QuestionMarkOperatorToken);
    assertTrue("Slash operator not properly detected", tokenList.get(22) instanceof SlashOperatorToken);
    assertTrue("Less than operator not properly detected", tokenList.get(23) instanceof LessOperatorToken);
    assertTrue("Greater than operator not properly detected", tokenList.get(24) instanceof GreaterOperatorToken);

    // Test tokenize of the double-character operators
    tokenList = tokenizeString.tokenizeString(" <= == != >= && || += -= *= /= &= |= ^= ++ -- << >> >>>");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 18, tokenList.size());
    assertTrue("Less equals operator not properly detected", tokenList.get(0) instanceof LessEqualsOperatorToken);
    assertTrue("Equals operator not properly detected", tokenList.get(1) instanceof EqualsOperatorToken);
    assertTrue("Not equals operator not properly detected", tokenList.get(2) instanceof NotEqualsOperatorToken);
    assertTrue("Greater equals operator not properly detected", tokenList.get(3) instanceof GreaterEqualsOperatorToken);
    assertTrue("Logical AND operator not properly detected", tokenList.get(4) instanceof LogicalAndOperatorToken);
    assertTrue("Logical OR operator not properly detected", tokenList.get(5) instanceof LogicalOrOperatorToken);
    assertTrue("Plus equal operator not properly detected", tokenList.get(6) instanceof PlusEqualOperatorToken);
    assertTrue("Minus equal operator not properly detected", tokenList.get(7) instanceof MinusEqualOperatorToken);
    assertTrue("Asterisk equal operator not properly detected", tokenList.get(8) instanceof AsteriskEqualOperatorToken);
    assertTrue("Slash equal operator not properly detected", tokenList.get(9) instanceof SlashEqualOperatorToken);
    assertTrue("Bitwise AND equal operator not properly detected", tokenList.get(10) instanceof BitwiseAndEqualOperatorToken);
    assertTrue("Bitwise OR equal operator not properly detected", tokenList.get(11) instanceof BitwiseOrEqualOperatorToken);
    assertTrue("Exclusive OR equal operator not properly detected", tokenList.get(12) instanceof ExclusiveOrEqualOperatorToken);
    assertTrue("Plus plus operator not properly detected", tokenList.get(13) instanceof PlusPlusOperatorToken);
    assertTrue("Minus minus operator not properly detected", tokenList.get(14) instanceof MinusMinusOperatorToken);
    assertTrue("Less less operator not properly detected", tokenList.get(15) instanceof LessLessOperatorToken);
    assertTrue("Greater greater operator not properly detected", tokenList.get(16) instanceof GreaterGreaterOperatorToken);
    assertTrue("Greater greater greater operator not properly detected", tokenList.get(17) instanceof GreaterGreaterGreaterOperatorToken);

    // Test tokenize of the statement keywords
    tokenList = tokenizeString.tokenizeString(" if else do while switch case default try catch throw return new ");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 12, tokenList.size());
    assertTrue("'if' keyword not properly detected", tokenList.get(0) instanceof IfKeywordToken);
    assertTrue("'else' keyword not properly detected", tokenList.get(1) instanceof ElseKeywordToken);
    assertTrue("'do' keyword not properly detected", tokenList.get(2) instanceof DoKeywordToken);
    assertTrue("'while' keyword not properly detected", tokenList.get(3) instanceof WhileKeywordToken);
    assertTrue("'switch' keyword not properly detected", tokenList.get(4) instanceof SwitchKeywordToken);
    assertTrue("'case' keyword not properly detected", tokenList.get(5) instanceof CaseKeywordToken);
    assertTrue("'default' keyword not properly detected", tokenList.get(6) instanceof DefaultKeywordToken);
    assertTrue("'try' keyword not properly detected", tokenList.get(7) instanceof TryKeywordToken);
    assertTrue("'catch' keyword not properly detected", tokenList.get(8) instanceof CatchKeywordToken);
    assertTrue("'throw' keyword not properly detected", tokenList.get(9) instanceof ThrowKeywordToken);
    assertTrue("'return' keyword not properly detected", tokenList.get(10) instanceof ReturnKeywordToken);
    assertTrue("'new' keyword not properly detected", tokenList.get(11) instanceof NewKeywordToken);

    // Test tokenize of the built-in values
    tokenList = tokenizeString.tokenizeString(" null true false now ");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 4, tokenList.size());
    assertTrue("'null' keyword not properly detected", tokenList.get(0) instanceof NullToken);
    assertTrue("'true' keyword not properly detected", tokenList.get(1) instanceof TrueToken);
    assertTrue("'false' keyword not properly detected", tokenList.get(2) instanceof FalseToken);
    assertTrue("'now' keyword not properly detected", tokenList.get(3) instanceof NowKeywordToken);

    // Test tokenize of the built-in type keywords
    tokenList = tokenizeString.tokenizeString(" object boolean int long float double char string date list map ");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 11, tokenList.size());
    assertTrue("'object' type keyword not properly detected", tokenList.get(0) instanceof ObjectKeywordToken);
    assertTrue("'boolean' type keyword not properly detected", tokenList.get(1) instanceof BooleanKeywordToken);
    assertTrue("'int' type keyword not properly detected", tokenList.get(2) instanceof IntKeywordToken);
    assertTrue("'long' type keyword not properly detected", tokenList.get(3) instanceof LongKeywordToken);
    assertTrue("'float' type keyword not properly detected", tokenList.get(4) instanceof FloatKeywordToken);
    assertTrue("'double' type keyword not properly detected", tokenList.get(5) instanceof DoubleKeywordToken);
    assertTrue("'char' type keyword not properly detected", tokenList.get(6) instanceof CharKeywordToken);
    assertTrue("'string' type keyword not properly detected", tokenList.get(7) instanceof StringKeywordToken);
    assertTrue("'date' type keyword not properly detected", tokenList.get(8) instanceof DateKeywordToken);
    assertTrue("'list' type keyword not properly detected", tokenList.get(9) instanceof ListKeywordToken);
    assertTrue("'map' type keyword not properly detected", tokenList.get(10) instanceof MapKeywordToken);

    // Test tokenize of without whitespace
    tokenList = tokenizeString.tokenizeString("if(a<b||c>=d)x+=y;else{p.a-=q++;a=b*c+d/e;");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 33, tokenList.size());
    assertTrue("'if' keyword token not properly detected", tokenList.get(0) instanceof IfKeywordToken);
    assertTrue("Left parenthesis token not properly detected", tokenList.get(1) instanceof LeftParenthesisOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(2) instanceof IdentifierToken);
    assertEquals("Identifier value", "a", ((IdentifierToken)tokenList.get(2)).identifier);
    assertTrue("Less token not properly detected", tokenList.get(3) instanceof LessOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(4) instanceof IdentifierToken);
    assertEquals("Identifier value", "b", ((IdentifierToken)tokenList.get(4)).identifier);
    assertTrue("Logical OR token not properly detected", tokenList.get(5) instanceof LogicalOrOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(6) instanceof IdentifierToken);
    assertEquals("Identifier value", "c", ((IdentifierToken)tokenList.get(6)).identifier);
    assertTrue("Greater equals token not properly detected", tokenList.get(7) instanceof GreaterEqualsOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(8) instanceof IdentifierToken);
    assertEquals("Identifier value", "d", ((IdentifierToken)tokenList.get(8)).identifier);
    assertTrue("Right parenthesis token not properly detected", tokenList.get(9) instanceof RightParenthesisOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(10) instanceof IdentifierToken);
    assertEquals("Identifier value", "x", ((IdentifierToken)tokenList.get(10)).identifier);
    assertTrue("Plus equal token not properly detected", tokenList.get(11) instanceof PlusEqualOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(12) instanceof IdentifierToken);
    assertEquals("Identifier value", "y", ((IdentifierToken)tokenList.get(12)).identifier);
    assertTrue("Semicolon token not properly detected", tokenList.get(13) instanceof SemicolonOperatorToken);
    assertTrue("'else' keyword token not properly detected", tokenList.get(14) instanceof ElseKeywordToken);
    assertTrue("Left brace token not properly detected", tokenList.get(15) instanceof LeftBraceOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(16) instanceof IdentifierToken);
    assertEquals("Identifier value", "p", ((IdentifierToken)tokenList.get(16)).identifier);
    assertTrue("Period token not properly detected", tokenList.get(17) instanceof PeriodOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(18) instanceof IdentifierToken);
    assertEquals("Identifier value", "a", ((IdentifierToken)tokenList.get(18)).identifier);
    assertTrue("Minus equal token not properly detected", tokenList.get(19) instanceof MinusEqualOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(20) instanceof IdentifierToken);
    assertEquals("Identifier value", "q", ((IdentifierToken)tokenList.get(20)).identifier);
    assertTrue("Plus plus token not properly detected", tokenList.get(21) instanceof PlusPlusOperatorToken);
    assertTrue("Semicolon token not properly detected", tokenList.get(22) instanceof SemicolonOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(23) instanceof IdentifierToken);
    assertEquals("Identifier value", "a", ((IdentifierToken)tokenList.get(23)).identifier);
    assertTrue("Equal token not properly detected", tokenList.get(24) instanceof EqualOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(25) instanceof IdentifierToken);
    assertEquals("Identifier value", "b", ((IdentifierToken)tokenList.get(25)).identifier);
    assertTrue("Asterisk token not properly detected", tokenList.get(26) instanceof AsteriskOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(27) instanceof IdentifierToken);
    assertEquals("Identifier value", "c", ((IdentifierToken)tokenList.get(27)).identifier);
    assertTrue("Plus token not properly detected", tokenList.get(28) instanceof PlusOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(29) instanceof IdentifierToken);
    assertEquals("Identifier value", "d", ((IdentifierToken)tokenList.get(29)).identifier);
    assertTrue("Slash token not properly detected", tokenList.get(30) instanceof SlashOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(31) instanceof IdentifierToken);
    assertEquals("Identifier value", "e", ((IdentifierToken)tokenList.get(31)).identifier);
    assertTrue("Semicolon token not properly detected", tokenList.get(32) instanceof SemicolonOperatorToken);
    
    // Test identifier tokens
    tokenList = tokenizeString.tokenizeString(" a ab abc a1 a12 _ _a a_ a_b c_89_d 1a Abc A$b x-y ");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 19, tokenList.size());
    assertTrue("Identifier token not properly detected", tokenList.get(0) instanceof IdentifierToken);
    assertEquals("Identifier value", "a", ((IdentifierToken)tokenList.get(0)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(1) instanceof IdentifierToken);
    assertEquals("Identifier value", "ab", ((IdentifierToken)tokenList.get(1)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(2) instanceof IdentifierToken);
    assertEquals("Identifier value", "abc", ((IdentifierToken)tokenList.get(2)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(3) instanceof IdentifierToken);
    assertEquals("Identifier value", "a1", ((IdentifierToken)tokenList.get(3)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(4) instanceof IdentifierToken);
    assertEquals("Identifier value", "a12", ((IdentifierToken)tokenList.get(4)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(5) instanceof IdentifierToken);
    assertEquals("Identifier value", "_", ((IdentifierToken)tokenList.get(5)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(6) instanceof IdentifierToken);
    assertEquals("Identifier value", "_a", ((IdentifierToken)tokenList.get(6)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(7) instanceof IdentifierToken);
    assertEquals("Identifier value", "a_", ((IdentifierToken)tokenList.get(7)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(8) instanceof IdentifierToken);
    assertEquals("Identifier value", "a_b", ((IdentifierToken)tokenList.get(8)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(9) instanceof IdentifierToken);
    assertEquals("Identifier value", "c_89_d", ((IdentifierToken)tokenList.get(9)).identifier);
    assertTrue("Integer token not properly detected", tokenList.get(10) instanceof IntegerToken);
    assertEquals("Integer value", 1, ((IntegerToken)tokenList.get(10)).number);
    assertTrue("Identifier token not properly detected", tokenList.get(11) instanceof IdentifierToken);
    assertEquals("Identifier value", "a", ((IdentifierToken)tokenList.get(11)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(12) instanceof IdentifierToken);
    assertEquals("Identifier value", "Abc", ((IdentifierToken)tokenList.get(12)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(13) instanceof IdentifierToken);
    assertEquals("Identifier value", "A", ((IdentifierToken)tokenList.get(13)).identifier);
    assertTrue("Dollar sign token not properly detected", tokenList.get(14) instanceof DollarSignOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(15) instanceof IdentifierToken);
    assertEquals("Identifier value", "b", ((IdentifierToken)tokenList.get(15)).identifier);
    assertTrue("Identifier token not properly detected", tokenList.get(16) instanceof IdentifierToken);
    assertEquals("Identifier value", "x", ((IdentifierToken)tokenList.get(16)).identifier);
    assertTrue("Minus sign token not properly detected", tokenList.get(17) instanceof MinusOperatorToken);
    assertTrue("Identifier token not properly detected", tokenList.get(18) instanceof IdentifierToken);
    assertEquals("Identifier value", "y", ((IdentifierToken)tokenList.get(18)).identifier);
    
    // Test number tokens
    tokenList = tokenizeString.tokenizeString(" 0 12 1234567890 1.23 .456 789. 0234 ");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 7, tokenList.size());
    assertTrue("Integer token not properly detected", tokenList.get(0) instanceof IntegerToken);
    assertEquals("Integer value", 0, ((IntegerToken)tokenList.get(0)).number);
    assertTrue("Integer token not properly detected", tokenList.get(1) instanceof IntegerToken);
    assertEquals("Integer value", 12, ((IntegerToken)tokenList.get(1)).number);
    assertTrue("Integer token not properly detected", tokenList.get(2) instanceof IntegerToken);
    assertEquals("Integer value", 1234567890, ((IntegerToken)tokenList.get(2)).number);
    assertTrue("Float token not properly detected", tokenList.get(3) instanceof FloatToken);
    assertEquals("Float value", 1.23, ((FloatToken)tokenList.get(3)).number, 0.001);
    assertTrue("Float token not properly detected", tokenList.get(4) instanceof FloatToken);
    assertEquals("Float value", 0.456, ((FloatToken)tokenList.get(4)).number, 0.001);
    assertTrue("Float token not properly detected", tokenList.get(5) instanceof FloatToken);
    assertEquals("Float value", 789.0, ((FloatToken)tokenList.get(5)).number, 0.001);
    assertTrue("Integer token not properly detected", tokenList.get(6) instanceof IntegerToken);
    assertEquals("Integer value", 234, ((IntegerToken)tokenList.get(6)).number);
    
    // Test string tokens
    tokenList = tokenizeString.tokenizeString(" \"\" \" \" \" a b \" \"-\\\\+\" \"-\\\"+\" \"-'+\" ");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 6, tokenList.size());
    assertTrue("String token not properly detected", tokenList.get(0) instanceof StringToken);
    assertEquals("String value", "", ((StringToken)tokenList.get(0)).string);
    assertTrue("String token not properly detected", tokenList.get(1) instanceof StringToken);
    assertEquals("String value", " ", ((StringToken)tokenList.get(1)).string);
    assertTrue("String token not properly detected", tokenList.get(2) instanceof StringToken);
    assertEquals("String value", " a b ", ((StringToken)tokenList.get(2)).string);
    assertTrue("String token not properly detected", tokenList.get(3) instanceof StringToken);
    assertEquals("String value", "-\\+", ((StringToken)tokenList.get(3)).string);
    assertTrue("String token not properly detected", tokenList.get(4) instanceof StringToken);
    assertEquals("String value", "-\"+", ((StringToken)tokenList.get(4)).string);
    assertTrue("String token not properly detected", tokenList.get(5) instanceof StringToken);
    assertEquals("String value", "-'+", ((StringToken)tokenList.get(5)).string);
    
    // Test string tokens using apostrophes
    tokenList = tokenizeString.tokenizeString(" '' ' ' ' a b ' '-\\\\+' '-\\\"+' '-\"+' '-\\'+' ");
    assertTrue("Token list not returned", tokenList != null);
    assertEquals("Number of tokens returned", 7, tokenList.size());
    assertTrue("String token not properly detected", tokenList.get(0) instanceof StringToken);
    assertEquals("String value", "", ((StringToken)tokenList.get(0)).string);
    assertTrue("String token not properly detected", tokenList.get(1) instanceof StringToken);
    assertEquals("String value", " ", ((StringToken)tokenList.get(1)).string);
    assertTrue("String token not properly detected", tokenList.get(2) instanceof StringToken);
    assertEquals("String value", " a b ", ((StringToken)tokenList.get(2)).string);
    assertTrue("String token not properly detected", tokenList.get(3) instanceof StringToken);
    assertEquals("String value", "-\\+", ((StringToken)tokenList.get(3)).string);
    assertTrue("String token not properly detected", tokenList.get(4) instanceof StringToken);
    assertEquals("String value", "-\"+", ((StringToken)tokenList.get(4)).string);
    assertTrue("String token not properly detected", tokenList.get(5) instanceof StringToken);
    assertEquals("String value", "-\"+", ((StringToken)tokenList.get(5)).string);
    assertTrue("String token not properly detected", tokenList.get(6) instanceof StringToken);
    assertEquals("String value", "-'+", ((StringToken)tokenList.get(6)).string);
}

}
