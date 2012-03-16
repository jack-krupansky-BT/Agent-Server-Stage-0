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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.basetechnology.s0.agentserver.util.TextAnalyzer;
import com.basetechnology.s0.agentserver.util.Word;

public class TextAnalyzerTest extends TextAnalyzer {

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
  public void test() {
    TextAnalyzer tx = new TextAnalyzer();
  
    {
      List<Word> words = tx.analyze(" $0.02. ");
      assertEquals("words", "[$0.02]", words.toString());
      
    }
    // Test null and empty and whitespace input text
    List<Word> words = tx.analyze(null);
    assertEquals("Count of analyzed words", 0, words.size());
    words = tx.analyze("");
    assertEquals("Count of analyzed words", 0, words.size());
    words = tx.analyze("   ");
    assertEquals("Count of analyzed words", 0, words.size());
    words = tx.analyze("   \n  \t  \t  \n  \t \n\n");
    assertEquals("Count of analyzed words", 0, words.size());

    // Test a single simple word
    words = tx.analyze("a");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "a", words.get(0).proper);
    assertEquals("word[0]", "a", words.get(0).properLower);

    words = tx.analyze(" a ");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "a", words.get(0).proper);
    assertEquals("word[0]", "a", words.get(0).properLower);

    words = tx.analyze(" a \n\t  ");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "a", words.get(0).proper);
    assertEquals("word[0]", "a", words.get(0).properLower);

    words = tx.analyze(" A \n\t  ");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "A", words.get(0).proper);
    assertEquals("word[0]", "a", words.get(0).properLower);

    words = tx.analyze(" Cat. ");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "Cat", words.get(0).proper);
    assertEquals("word[0]", "cat", words.get(0).properLower);

    words = tx.analyze(" U.S. ");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "U.S.", words.get(0).proper);
    assertEquals("word[0]", "u.s.", words.get(0).properLower);

    words = tx.analyze(" 3.5 ");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "3.5", words.get(0).proper);
    assertEquals("word[0]", "3.5", words.get(0).properLower);

    words = tx.analyze(" $3.5 ");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "$3.5", words.get(0).proper);
    assertEquals("word[0]", "$3.5", words.get(0).properLower);

    words = tx.analyze(" One-time ");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "One-time", words.get(0).proper);
    assertEquals("word[0]", "one-time", words.get(0).properLower);

    words = tx.analyze(" shouldn't've ");
    assertEquals("Count of analyzed words", 1, words.size());
    assertEquals("word[0]", "shouldn't've", words.get(0).proper);
    assertEquals("word[0]", "shouldn't've", words.get(0).properLower);

    // Now some phrases
    words = tx.analyze(" The cat in the hat. ");
    assertEquals("Count of analyzed words", 5, words.size());
    assertEquals("word[0]", "The", words.get(0).proper);
    assertEquals("word[0]", "the", words.get(0).properLower);
    assertEquals("word[1]", "cat", words.get(1).proper);
    assertEquals("word[1]", "cat", words.get(1).properLower);
    assertEquals("word[2]", "in", words.get(2).proper);
    assertEquals("word[2]", "in", words.get(2).properLower);
    assertEquals("word[3]", "the", words.get(3).proper);
    assertEquals("word[3]", "the", words.get(3).properLower);
    assertEquals("word[4]", "hat", words.get(4).proper);
    assertEquals("word[5]", "hat", words.get(4).properLower);
    assertEquals("words", "[The, cat, in, the, hat]", words.toString());

    words = tx.analyze(" What is the BIG Deal??!! ");
    assertEquals("Count of analyzed words", 5, words.size());
    assertEquals("words", "[What, is, the, BIG, Deal]", words.toString());

    words = tx.analyze("Caroline Humer\nTue Mar");
    assertEquals("Count of analyzed words", 4, words.size());
    assertEquals("words", "[Caroline, Humer, Tue, Mar]", words.toString());

    words = tx.analyze("Lehman emerges from 3.5-year bankruptcy\nBy Caroline Humer\nTue Mar 6, 2012 5:47pm EST\n(Reuters) - One-time financial powerhouse Lehman Brothers");
    assertEquals("Count of analyzed words", 20, words.size());
    assertEquals("words", "[Lehman, emerges, from, 3.5-year, bankruptcy, By, Caroline, Humer, Tue, Mar, 6, 2012, 5:47pm, EST, Reuters, One-time, financial, powerhouse, Lehman, Brothers]", words.toString());

    words = tx.analyze("Lehman emerges from 3.5-year bankruptcy\nBy Caroline Humer\nTue Mar 6, 2012 5:47pm EST\n(Reuters) - One-time financial powerhouse Lehman Brothers emerged from bankruptcy on Tuesday and is now a liquidating company whose main business in the coming years will be paying back its creditors and investors.\nLehman, whose September 2008 collapse is often regarded as the height of the financial crisis, will start distributing what it expects to be a total of about $65 billion to creditors on April 17, it said in a statement.");
    assertEquals("Count of analyzed words", 85, words.size());
    assertEquals("words", "[Lehman, emerges, from, 3.5-year, bankruptcy, By, Caroline, Humer, Tue, Mar, 6, 2012, 5:47pm, EST, Reuters, One-time, financial, powerhouse, Lehman, Brothers, emerged, from, bankruptcy, on, Tuesday, and, is, now, a, liquidating, company, whose, main, business, in, the, coming, years, will, be, paying, back, its, creditors, and, investors, Lehman, whose, September, 2008, collapse, is, often, regarded, as, the, height, of, the, financial, crisis, will, start, distributing, what, it, expects, to, be, a, total, of, about, $65, billion, to, creditors, on, April, 17, it, said, in, a, statement]", words.toString());
  }

}
