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

package com.basetechnology.s0.agentserver.persistence.persistentfile;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.basetechnology.s0.agentserver.AgentServerTest;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentEntry;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentFile;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentFileException;
import com.basetechnology.s0.agentserver.persistence.persistentfile.PersistentTable;
import com.basetechnology.s0.agentserver.util.ListMap;

public class PersistentFileTest {
  static final Logger log = Logger.getLogger(PersistentFileTest.class);

  static public String pathToDelete = null;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    if (pathToDelete != null){
      File file = new File(pathToDelete);
      file.delete();
      assertTrue("Unable to delete persisten file: " + pathToDelete, ! file.exists());
      pathToDelete = null;
    }
  }

  @After
  public void tearDown() throws Exception {
    if (pathToDelete != null){
      File file = new File(pathToDelete);
      file.delete();
      assertTrue("Unable to delete persisten file: " + pathToDelete, ! file.exists());
      pathToDelete = null;
    }
  }

  @Test
  public void testCreate() throws IOException, PersistentFileException {
    // Generate a temp file name that we can use
    File tempFile = File.createTempFile("Test-", "-Persistence.pjson");
    tempFile.delete();
    String fullPath = tempFile.getCanonicalPath();
    pathToDelete = fullPath;
    log.info("Temp file will be " + fullPath);
    
    // Create a persistent file with a few tables
    String table1Name = "Users";
    String table2Name = "Agent-Definitions";
    String table3Name = "Agent-Instances";
    String table4Name = "Agent-State";
    String table5Name = "Activities";
    List<String> tableNames = new ArrayList<String>(Arrays.asList(table1Name, table2Name, table3Name, table4Name, table5Name));
    PersistentFile pf = new PersistentFile();
    String applicationName = "Test-Persistent-File";
    String applicationFormatVersion = "0.0";
    pf.create(fullPath, applicationName, applicationFormatVersion, tableNames);

    // Make sure the file got created
    assertTrue("Persistent file was not created", tempFile.exists());
    
    // Now open the persistent file
    pf.open(fullPath);
    
    // Check that the file header was read properly
    assertTrue("Application name is missing", pf.applicationName != null);
    assertEquals("Application name", pf.applicationName, applicationName);
    assertTrue("Application format version is missing", pf.applicationFormatVersion != null);
    assertEquals("Application format version", pf.applicationFormatVersion, applicationFormatVersion);
    
    // Now check the table names and file positions
    ListMap<String, PersistentTable> tables = pf.persistentTables;
    assertTrue("Tables list is missing", tables != null);
    int numTables = tables.size();
    assertEquals("Number of tables", tableNames.size(), numTables);
    for (int i = 0; i < numTables; i++){
      // Get the next table entry from file
      PersistentTable table = tables.get(i);
      
      // Check table name
      assertEquals("Table name #" + (i + 1), tableNames.get(i), table.name);
      
      // Semi-check file position for the file position in the table entry
      assertTrue("Entry position for table #" + (i + 1) + " is zero", table.tocPosition != 0);
      assertTrue("Entry position for table #" + (i + 1) + " is too large", table.tocPosition < 1000);

      // Check file position
      assertEquals("File Position for table #" + (i + 1), 0, table.position);
    }
    
    // Close the file
    pf.close();
    
    // Make sure everything was cleaned up
    assertTrue("Persistent file object is not null", pf.file == null);
    assertTrue("Persistent file path is not null", pf.path == null);
    assertTrue("Application name is not null", pf.applicationName == null);
    assertTrue("Application format version is not null", pf.applicationFormatVersion == null);
    assertTrue("Persistent file table list is not null/empty", pf.persistentTables == null || pf.persistentTables.size() == 0);
    
    // Build a list of test data
    List<String> testKeys = Arrays.asList("testuser", "testuser-2", "testuser-3", "testuser-4", "testuser-5", "testuser-6", "testuser-7", "testuser-8", "testuser-9", "testuser-10");
    List<String> testValues = Arrays.asList(
        "{\"id\": \"testuser\", \"display_name\": \"Test User\"}",
        "{\"id\": \"testuser-2\", \"display_name\": \"Test User #2\"}",
        "{\"id\": \"testuser-3\", \"display_name\": \"Test User #3\"}",
        "{\"id\": \"testuser-4\", \"display_name\": \"Test User #4\"}",
        "{\"id\": \"testuser-5\", \"display_name\": \"Test User #5\"}",
        "{\"id\": \"testuser-6\", \"display_name\": \"Test User #6\"}",
        "{\"id\": \"testuser-7\", \"display_name\": \"Test User #7\"}",
        "{\"id\": \"testuser-8\", \"display_name\": \"Test User #8\"}",
        "{\"id\": \"testuser-9\", \"display_name\": \"Test User #9\"}",
        "{\"id\": \"testuser-10\", \"display_name\": \"Test User #10\"}"
        );
    
    // Now add a single item to one of the tables
    pf.open(fullPath);

    // Remember file size before any keys or values added
    long initialFileSize = pf.file.length();

    // Add a key and value
    pf.put("Users", testKeys.get(0), testValues.get(0));

    // Check that the in-memory value is returned properly
    String value = pf.get("Users", testKeys.get(0));
    assertTrue("Missing value for key", value != null);
    assertEquals("Key value", value, testValues.get(0));
    
    // Check that first table key block went where expected
    PersistentTable table = pf.persistentTables.get("Users");
    assertEquals("Initial key block position", initialFileSize, table.position);
    
    // Check that free position and space available are set properly
    int firstKeySize = testKeys.get(0).length() + 1 + 12 + 2;
    long freePositionAfterFirstKey = initialFileSize + firstKeySize;
    assertEquals("Initial free position", freePositionAfterFirstKey, table.freePosition);
    int numBytesFreeAfterFirstKey = PersistentFile.BLOCK_SIZE - firstKeySize;
    assertEquals("Initial empty byte count", numBytesFreeAfterFirstKey, table.numBytesFree);
    
    // Check that first key went where expected
    assertTrue("Missing entries", table.entries != null);
    PersistentEntry entry = table.entries.get(0);
    assertEquals("Key", testKeys.get(0), entry.key);
    assertEquals("Key position", initialFileSize + testKeys.get(0).length() + 1, entry.entryPosition);

    // Check that first key value went where expected
    assertEquals("Key value position", initialFileSize + PersistentFile.BLOCK_SIZE, entry.valuePosition);
    
    // Close the file, reopen, and check that table entry counts are correct
    pf.close();
    pf.open(fullPath);
    assertTrue("Application name is missing", pf.applicationName != null);
    assertEquals("Application name", pf.applicationName, applicationName);
    assertTrue("Application format version is missing", pf.applicationFormatVersion != null);
    assertEquals("Application format version", pf.applicationFormatVersion, applicationFormatVersion);
    tables = pf.persistentTables;
    assertTrue("Tables list is missing", tables != null);
    numTables = tables.size();
    assertEquals("Number of tables", tableNames.size(), numTables);
    for (int i = 0; i < numTables; i++){
      // Get the next table from file
      table = tables.get(i);
      
      // Check table name
      assertEquals("Table name #" + (i + 1), tableNames.get(i), table.name);
      
      // Semi-check file position for the file position in the table entry
      assertTrue("Entry position for table #" + (i + 1) + " is zero", table.tocPosition != 0);
      assertTrue("Entry position for table #" + (i + 1) + " is too large", table.tocPosition < 1000);

      if (table.name.equals("Users")){
        // Check file position
        assertTrue("File Position for table #" + (i + 1) + " should be non-zero", table.position != 0);
        
        // Check free position
        assertEquals("File free position for table #" + (i + 1), freePositionAfterFirstKey, table.freePosition);
        
        // Check bytes free
        assertEquals("File number of bytes free for table #" + (i + 1), numBytesFreeAfterFirstKey, table.numBytesFree);
      } else {
        // Check file position
        assertEquals("File Position for table #" + (i + 1), 0, table.position);
        
        // Check free position
        assertEquals("File free position for table #" + (i + 1), 0, table.freePosition);
        
        // Check bytes free
        assertEquals("File number of bytes free for table #" + (i + 1), 0, table.numBytesFree);
      }
    }

    // Check that first key info is still correct
    // Check that first table key block went where expected
    table = pf.persistentTables.get("Users");
    assertEquals("Initial key block position", initialFileSize, table.position);
    
    // Check that free position got recomputed correctly on reload
    assertEquals("Initial free position after reload", freePositionAfterFirstKey, table.freePosition);
    
    // Check that number of free bytes got recomputed correctly on reload
    assertEquals("Initial num empty after reload", numBytesFreeAfterFirstKey, table.numBytesFree);
    
    // Check that first key went where expected
    assertTrue("Missing entries", table.entries != null);
    entry = table.entries.get(0);
    assertEquals("Key", testKeys.get(0), entry.key);
    assertEquals("Key position", initialFileSize + testKeys.get(0).length() + 1, entry.entryPosition);

    // Check that first key value went where expected
    assertEquals("Key value position", initialFileSize + PersistentFile.BLOCK_SIZE, entry.valuePosition);

    // Check that the value associated with the first key is still okay
    value = pf.get("Users", testKeys.get(0));
    assertTrue("Missing value for key", value != null);
    assertEquals("Key value", testValues.get(0), value);

    // Now add the remaining 9 test keys
    for (int i = 1; i < testKeys.size(); i++)
      pf.put("Users", testKeys.get(i), testValues.get(i));

    // Check that all keys got added
    assertTrue("Missing entries", table.entries != null);
    assertEquals("Number of inserted keys", testKeys.size(), table.entries.size());

    // Check that the keys and values went where expected
    long expectedKeyPosition = initialFileSize;
    long expectedValuePosition = initialFileSize + PersistentFile.BLOCK_SIZE;
    for (int i = 0; i < testKeys.size(); i++){
      entry = table.entries.get(i);
      assertEquals("Key #" + i, testKeys.get(i), entry.key);
      assertEquals("Key position #" + i, expectedKeyPosition + testKeys.get(i).length() + 1, entry.entryPosition);

      // Check that first key value went where expected
      assertEquals("Key value position #" + i, expectedValuePosition, entry.valuePosition);
      
      // Advance to expected positions of next key and value
      expectedKeyPosition += entry.key.length() + PersistentTable.ENTRY_SIZE_BASE;
      expectedValuePosition += entry.key.length() + 1 + 28 + 1 + 12 + 1 + 2 + 1 + testValues.get(i).length() + 2;
    }

    // Close and reopen file and do checks again
    pf.close();
    pf.open(fullPath);

    // Check that first key info is still correct
    // Check that first table key block went where expected
    table = pf.persistentTables.get("Users");
    assertEquals("Initial key block position", initialFileSize, table.position);
    
    // Check that first key went where expected
    assertTrue("Missing entries", table.entries != null);
    entry = table.entries.get(0);
    assertEquals("Key", testKeys.get(0), entry.key);
    assertEquals("Key position", initialFileSize + testKeys.get(0).length() + 1, entry.entryPosition);

    // Check that first key value went where expected
    assertEquals("Key value position", initialFileSize + PersistentFile.BLOCK_SIZE, entry.valuePosition);

    // Check that all keys got added
    assertTrue("Missing entries", table.entries != null);
    assertEquals("Number of inserted keys", testKeys.size(), table.entries.size());

    // Check that the keys and values went where expected
    expectedKeyPosition = initialFileSize;
    expectedValuePosition = initialFileSize + PersistentFile.BLOCK_SIZE;
    for (int i = 0; i < testKeys.size(); i++){
      entry = table.entries.get(i);
      assertEquals("Key #" + i, testKeys.get(i), entry.key);
      assertEquals("Key position #" + i, expectedKeyPosition + testKeys.get(i).length() + 1, entry.entryPosition);

      // Check that first key value went where expected
      assertEquals("Key value position #" + i, expectedValuePosition, entry.valuePosition);
      
      // Advance to expected positions of next key and value
      expectedKeyPosition += entry.key.length() + PersistentTable.ENTRY_SIZE_BASE;
      expectedValuePosition += entry.key.length() + 1 + 28 + 1 + 12 + 1 + 2 + 1 + testValues.get(i).length() + 2;
    }

    pf.close();
    
  }

  @Test
  public void testPerformance() throws IOException, PersistentFileException {
    // Generate a temp file name that we can use
    File tempFile = File.createTempFile("Test-", "-Persistence.pjson");
    tempFile.delete();
    String fullPath = tempFile.getCanonicalPath();
    pathToDelete = fullPath;
    log.info("Temp file will be " + fullPath);
    
    // Create a persistent file with a few tables
    String table1Name = "Users";
    String table2Name = "Agent-Definitions";
    String table3Name = "Agent-Instances";
    String table4Name = "Agent-State";
    String table5Name = "Activities";
    List<String> tableNames = new ArrayList<String>(Arrays.asList(table1Name, table2Name, table3Name, table4Name, table5Name));
    PersistentFile pf = new PersistentFile();
    String applicationName = "Test-Persistent-File";
    String applicationFormatVersion = "0.0";
    pf.create(fullPath, applicationName, applicationFormatVersion, tableNames);
    
    // Open the file and write a lot of values
    int numKeys = 500;
    pf.open(fullPath);
    String keyPrefix = "reasonably-long-test-key---------------";
    String valuePrefix = "(({{[[=== a reasonably long value for the data ------------------------------------------------------------------ ]]}}))";
    for (int i = 0; i < numKeys; i++)
      pf.put("Users", keyPrefix + i, valuePrefix + i);
    pf.close();
    pf.open(fullPath);
    pf.close();
    
    // Now read all keys
    pf.open(fullPath);
    for (int i = 0; i < numKeys; i++)
      assertEquals("Value[" + i + "]", valuePrefix + i, pf.get("Users", keyPrefix + i));
    pf.close();
  }

  @Test
  public void testDeleteFile() throws IOException, PersistentFileException {
    // Generate a temp file name that we can use
    File tempFile = File.createTempFile("Test-", "-Persistence.pjson");
    tempFile.delete();
    String fullPath = tempFile.getCanonicalPath();
    pathToDelete = fullPath;
    log.info("Temp file will be " + fullPath);
    
    // Create file and close file with no data
    RandomAccessFile raf = new RandomAccessFile(fullPath, "rw");
    raf.close();
    
    // Now try to delete the file
    tempFile.delete();
    assertTrue("Unable to delete file: " + fullPath, ! tempFile.exists());
    
    // Now open the file after creating it, close without writing any data, and try to delete
    raf = new RandomAccessFile(fullPath, "rw");
    raf.close();
    raf = new RandomAccessFile(fullPath, "rw");
    raf.close();
    tempFile.delete();
    assertTrue("Unable to delete file: " + fullPath, ! tempFile.exists());
    
    // Now open the file nd write some data after creating it, close, and try to delete
    raf = new RandomAccessFile(fullPath, "rw");
    raf.close();
    raf = new RandomAccessFile(fullPath, "rw");
    raf.writeChars("Some test data");
    raf.close();
    tempFile.delete();
    assertTrue("Unable to delete file: " + fullPath, ! tempFile.exists());

  }

}
