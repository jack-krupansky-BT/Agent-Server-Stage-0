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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.basetechnology.s0.agentserver.AgentScheduler;
import com.basetechnology.s0.agentserver.util.DateUtils;
import com.basetechnology.s0.agentserver.util.ListMap;

public class PersistentFile implements Iterable<String> {
  static final Logger log = Logger.getLogger(PersistentFile.class);
  public final static int BLOCK_SIZE = 2048;
  public final static String formatVersion = "1.0";
  public final static String header1 = "Persistent JSON File\n" +
      "Format version: " + formatVersion + "\r\n" +
      "Created: ";
  public final static String header2 = "Application: ";
  public final static String header3 = "Application format version: ";
  public final static String header4 = " tables\r\n-- Table of Contents --\r\n";
  public final static String header5 = "-- Data --\r\n";
  public String path;
  public String applicationName;
  public String applicationFormatVersion;
  public RandomAccessFile file = null;
  public ListMap<String, PersistentTable> persistentTables;

  public void close() throws IOException {
    if (file != null)
      file.close();
    file = null;
    path = null;
    applicationName = null;
    applicationFormatVersion = null;
    persistentTables = null;
  }
  
  public void create(String path, String applicationName, String applicationFormatVersion, List<String> tableNames) throws FileNotFoundException, IOException, PersistentFileException {
    // Get number of tables
    int numTables = tableNames.size();
    
    // Delete any existing file
    File pathFile = new File(path);
    String fullPath = pathFile.getCanonicalPath();
    if (pathFile.exists())
      pathFile.delete();
    if (pathFile.exists())
      throw new PersistentFileException("Unable to delete file - it still exists - path: " + fullPath);

    // Create and open the file
    RandomAccessFile file = new RandomAccessFile(fullPath, "rw");

    // Generate the header text as a Java string
    StringBuilder sb = new StringBuilder();
    sb.append(header1
         + DateUtils.toRfcString(System.currentTimeMillis()) + "\r\n" +
        header2 + applicationName + "\r\n" +
        header3 + applicationFormatVersion + "\r\n" +
        numTables + header4);
    
    // Append the initial, dummy, table positions
    for (String tableName: tableNames)
      sb.append(tableName + "\t000000000000\r\n");

    // Mark end of header
    sb.append(header5);
    
    // Get the UTF-8 byte encoding of the header
    String headerString = sb.toString();
    int numCharsHeader = headerString.length();
    byte[] headerBytes = headerString.getBytes("UTF-8");
    int numBytesHeader = headerBytes.length;

    // Write out the header
    log.info("Writing persistent file " + fullPath + " header of " + numCharsHeader + " characters in " + numBytesHeader + " bytes");
    file.write(headerBytes);
    
    // Get the new length of the file
    long fileLength = file.length();
    
    // Close the file
    file.close();
    
    // Reopen file just to check size
    //file = new RandomAccessFile(fullPath, "rws");
    //long fileLength = file.length();
    //byte[] buffer = new byte[20000];
    //int numButesRead = file.read(buffer);

    log.info("Persistent file " + fullPath + " created - length of " + fileLength + " bytes");
  }

  public void open(String path) throws FileNotFoundException, IOException, PersistentFileException {
    // Open the persistent file and read the header
    RandomAccessFile file = new RandomAccessFile(path, "rws");
    long fileLength = file.length();
    byte[] buffer = new byte[20000];
    int numButesRead = file.read(buffer);
    log.info("Opened persistent file " + path + " of length " + fileLength + " and read " + numButesRead + " bytes");
    
    // Change raw UTF-8 bytes into Java string
    String header = new String(buffer);
    int headerLen = header.length();
    
    // Parse the header
    if (! header.startsWith(header1))
      throw new PersistentFileException("Not a valid persistent JSON file - header not present: " + header1);
    int index1 = header1.length();
    int index2 = header.indexOf('\n', index1);
    if (index2 == -1)
      throw new PersistentFileException("Not a valid persistent JSON file - no newline after timestamp in header");
    int len = header2.length();
    int index3 = ++index2 + len;
    String header2Substring = header.substring(index2, index3);
    if (! header2.equals(header2Substring))
      throw new PersistentFileException("Not a valid persistent JSON file - missing header2: " + header2);
    int index4 = header.indexOf('\n', index3);
    if (index4 < 0)
      throw new PersistentFileException("Not a valid persistent JSON file - missing newline after application name");
    String applicationName = header.substring(index3, index4 - 1);
    len = header3.length();
    int index5 = ++index4 + len;
    String header3Substring = header.substring(index4, index5);
    if (! header3.equals(header3Substring))
      throw new PersistentFileException("Not a valid persistent JSON file - missing header3: " + header3);
    int index6 = header.indexOf('\n', index5);
    if (index6 < 0)
      throw new PersistentFileException("Not a valid persistent JSON file - missing newline after application format version");
    String applicationFormatVersion = header.substring(index5, index6 - 1);
    char ch = header.charAt(++index6);
    if (! Character.isDigit(ch))
      throw new PersistentFileException("Not a valid persistent JSON file - expected numeric count of tables on line after application format version");
    int tableCount = ch - '0';
    while ((ch = header.charAt(++index6)) != ' ' && ch != '\r' && ch != '\n' && index6 < headerLen){ 
      if (! Character.isDigit(ch))
        throw new PersistentFileException("Not a valid persistent JSON file - expected numeric count of tables on line after application format version");
      tableCount = tableCount * 10 + ch - '0';
    }
    len = header4.length();
    int index7 = index6 + len;
    String header4Substring = header.substring(index6, index7);
    if (! header4.equals(header.substring(index6, index7)))
      throw new PersistentFileException("Not a valid persistent JSON file - after count count, missing header4: " + header4);
    log.info("Header verified - Application: " + applicationName + " Application format version: " + applicationFormatVersion + " contains " + tableCount + " tables");
    
    // Now parse the table of contents, containing each table name and the file position of its table
    ListMap<String, PersistentTable> tableList = new ListMap<String, PersistentTable>();
    for (int i = 0; i < tableCount; i++){
      int index8 = header.indexOf('\t', index7);
      if (index8 < 0)
        throw new PersistentFileException("Not a valid persistent JSON file - missing tab after table name #" + (i + 1));
      String tableName = header.substring(index7, index8);
      int index9 = header.indexOf('\n', index8);
      if (index9 < 0)
        throw new PersistentFileException("Not a valid persistent JSON file - missing newline after table file position for table name #" + (i + 1));
      long entryPosition = ++index8;
      String tablePositionString = header.substring(index8, index9 - 1);
      if (tablePositionString.length() != 12)
        throw new PersistentFileException("Not a valid persistent JSON file - table file position is not a 12-digit decimal integerfor table name #" + (i + 1) + ": " + tablePositionString);
      long tablePosition = -1;
      try {
        tablePosition = Long.parseLong(tablePositionString.trim());
      } catch (NumberFormatException e){
        throw new PersistentFileException("Not a valid persistent JSON file - table file position is not a long number for table name #" + (i + 1));
      }
      
      // Position to next table entry in TOC
      index7 = index9 + 1;
      
      // Add this table entry to list
      PersistentTable table = new PersistentTable(this, tableName, entryPosition, tablePosition);
      tableList.put(tableName, table);
    }
    log.info("Persistent file Table of Contents read");

    // Now read the tables
    for (String tableName: tableList)
      loadTable(file, tableList.get(tableName));
    
    // Now that header was successfully read, store the header info
    this.file = file;
    this.path = path;
    this.applicationName = applicationName;
    this.applicationFormatVersion = applicationFormatVersion;
    this.persistentTables = tableList;
  }

  public void loadTable(RandomAccessFile file, PersistentTable table) throws IOException, PersistentFileException {
    // Start with first key block of table
    long blockPosition = table.position;
    
    // Proceed until no more key blocks
    while (blockPosition != 0){
      // Read this block of keys
      file.seek(blockPosition);
      byte[] keyBlockBuffer = new byte[BLOCK_SIZE];
      int numBytesRead = file.read(keyBlockBuffer);
      if (numBytesRead != BLOCK_SIZE)
        throw new PersistentFileException("Internal error: Incomplete key block size read of only " + numBytesRead + " bytes");

      // Decode the block
      String blockString = new String(keyBlockBuffer, "UTF-8");
      int blockStringLen = blockString.length();
      
      // Scan the block, one key at a time
      long endPosition = blockPosition + BLOCK_SIZE - 1;
      //for (long position = blockPosition; position < endPosition; ){
      for (int i = 0; i < BLOCK_SIZE; i++){
        // Check for link or end marker
        if (keyBlockBuffer[i] == '\t'){
          // Is this a link or the end of table?
          if (keyBlockBuffer[i + 1] == '\r'){
            // End of table; no more key blocks
            // Record free position and free byte count in this last block
            table.freePosition = blockPosition + i;
            table.numBytesFree = BLOCK_SIZE - i;

            // Signal the end
            blockPosition = 0;
            break;
          } else {
            // This is a link to the next block of table
            String nextBlockString = new String(keyBlockBuffer, i + 1, 12);
            try {
              blockPosition = Long.parseLong(nextBlockString);
            } catch (NumberFormatException e){
              throw new PersistentFileException("Data corruption: Link pointer is not decimal: " + nextBlockString);
            }
            break;
          }
        } else {
          // Decode this key entry
          // Scan for end of key
          int keyBytesLen = 0;
          for (int j = i; j < BLOCK_SIZE && keyBlockBuffer[j] != '\r' && keyBlockBuffer[j] != '\t'; j++)
            keyBytesLen++;

          // Get the key string
          String key = new String(keyBlockBuffer, i, keyBytesLen);
          
          // Make sure we have the tab delimiter
          if (keyBlockBuffer[i + keyBytesLen] != '\t')
            throw new PersistentFileException("Data corruption: Did not find tab delimiter after key at offset " + i + " in block at position " + blockPosition + " of table " + table.name);

          // Scan the value position
          int positionBytesLen = 0;
          for (int j = i + keyBytesLen + 1; j < BLOCK_SIZE && keyBlockBuffer[j] != '\r'; j++)
            positionBytesLen++;
          if (positionBytesLen != 12)
            throw new PersistentFileException("Data corruption: Link pointer is not 12 characters: " + new String(keyBlockBuffer, i + 1, positionBytesLen));

          // Scan the key value position
          String keyValuePositionString = new String(keyBlockBuffer, i + keyBytesLen + 1, positionBytesLen);
          long keyValuePosition = 0;
          try {
            keyValuePosition = Long.parseLong(keyValuePositionString);
          } catch (NumberFormatException e){
            throw new PersistentFileException("Data corruption: Link pointer is not decimal: " + keyValuePositionString);
          }
          
          // Get position for value position in the block
          long entryPosition = blockPosition + i + keyBytesLen + 1;

          // Add entry to internal table
          PersistentEntry entry = new PersistentEntry(key, entryPosition, keyValuePosition);
          table.entries.put(key, entry);
          
          // Adjust scan position to skip over what we just scanned
          i = i + keyBytesLen + 1 + positionBytesLen + 2 - 1;
        }
        
      }
    }
    
  }

  public PersistentTable getTable(String tableName){
    return persistentTables.get(tableName);
  }
  
  public void put(String tableName, String key, String value) throws PersistentFileException, IOException {
    // Get the named table
    if (persistentTables == null)
      throw new PersistentFileException("No persistent file open");
    PersistentTable table = persistentTables.get(tableName);
    if (table == null)
      throw new PersistentFileException("Undefined table name: " + tableName);
    
    // Add the value to the table
    table.add(key, value);
  }

  public String get(String tableName, String key) throws IOException, PersistentFileException {
    // Get the named table
    PersistentTable table = persistentTables.get(tableName);
    if (table == null)
      throw new PersistentFileException("Undefined table name: " + tableName);
    
    // Get and return the value associated with the key
    return table.get(key);
  }

  public ListMap<String, String> get(String tableName) throws IOException, PersistentFileException {
    // Get the named table
    PersistentTable table = persistentTables.get(tableName);
    if (table == null)
      throw new PersistentFileException("Undefined table name: " + tableName);
    
    // Get all keys and their values
    return table.get();
  }

  public Iterator<String> iterator() {
    // Return iterator for all table names
    return new PersistentTableIterator(this);
  }

  public Iterable<String> iterable(String tableName) throws PersistentFileException {
    // Get the named table
    PersistentTable table = persistentTables.get(tableName);
    if (table == null)
      throw new PersistentFileException("Undefined table name: " + tableName);
    
    // Return iterator for all keys of table
    return table;
  }

  public Iterator<String> iterator(String tableName) throws PersistentFileException {
    // Get the named table
    PersistentTable table = persistentTables.get(tableName);
    if (table == null)
      throw new PersistentFileException("Undefined table name: " + tableName);
    
    // Return iterator for all keys of table
    return new PersistentTableKeyIterator(table);
  }

  
  // TODO: Get older values for key
  // By index or by timestamp or timestamp range
  
  
  static public String formatPosition(long position){
    return String.format("%012d",position);
  }
  
  public long appendString(String key, long prevPosition, String s) throws IOException {
    // Get position of end of file
    long position = file.length();

    // Get the UTF-8 byte encoding of the string
    byte[] stringBytes = s.getBytes("UTF-8");
    int stringLengthBytes = stringBytes.length;
    
    // Construct full text of string entry:
    // key, tab, time stamp, position of previous value, tab, length of string, tab,
    // length of UTF-8 string in bytes, tab, the UTF-8 encoding of string
    String fullString = stringLengthBytes + "\t" + DateUtils.toString(System.currentTimeMillis()) + '\t' +
        formatPosition(prevPosition) + '\t' + key + '\t' + s + "\r\n";

    // Get the UTF-8 byte encoding of the full string
    byte[] stringAllBytes = fullString.getBytes("UTF-8");
    int stringLengthAllBytes = stringAllBytes.length;
    
    // Write the encoded bytes to EOF
    file.seek(position);
    file.write(stringAllBytes);
    
    // Return the position of string at end of file
    return position;
  }
  
  public long addBlock() throws IOException {
    // Get position of end of file
    long position = file.length();

    // Generate an empty block - filled with spaces and a CRLF at end
    byte[] emptyBlockBuffer = new byte[BLOCK_SIZE];
    Arrays.fill(emptyBlockBuffer, (byte)' ');
    emptyBlockBuffer[BLOCK_SIZE - 2] = '\r';
    emptyBlockBuffer[BLOCK_SIZE - 1] = '\n';
    
    // Write the empty block at EOF
    file.seek(position);
    file.write(emptyBlockBuffer);
    
    // Return the position of string at end of file
    return position;
  }

  public String toString(){
    StringBuilder sb = new StringBuilder("[Persistent file path: " + path + (file == null ? " (closed)" : " (open)") + (persistentTables.size() > 0 ? " " + persistentTables.size() + " tables: " : ""));
    boolean first = true;
    for (String tableName: this){
      sb.append((! first ? ", " : "") + tableName);
      first = false;
    }
    sb.append(']');
    return sb.toString();
  }
  // Flush/sync
  
  // Load the tables list
  
  // Load a block from file
  
  // Parse block as JSON object
  
  // Load block of table entries
  
  // Search table entries for named table
  
  
  // Add a new named entry to a table
  
}
