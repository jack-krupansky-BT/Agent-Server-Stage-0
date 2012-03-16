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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.crypto.spec.PSource;

import org.apache.log4j.Logger;

import com.basetechnology.s0.agentserver.util.ListMap;


public class PersistentTable implements Iterable<String> {
  static final Logger log = Logger.getLogger(PersistentFile.class);
  public PersistentFile file;
  public String name;
  public long tocPosition;
  public long position;
  public ListMap<String, PersistentEntry> entries;
  public long freePosition = 0;
  public int numBytesFree = 0;
  public static final String CRLF = "\r\n";
  public static final String KEY_BLOCK_END_MARKER = '\t' + CRLF;
  public static final int ENTRY_SIZE_BASE = 15; // 12 digits plus tab and CRLF plus UTF-8 for key
  public static final int LINK_SIZE = 15; // tab plus 12 digits plus CRLF
  public static final int END_MARKER_SIZE = 3; // tab plus CRLF to mark end of key block

  public PersistentTable(PersistentFile file, String name, long tocPosition, long position){
    this.file = file;
    this.name = name;
    this.tocPosition = tocPosition;
    this.position = position;
    this.entries = new ListMap<String, PersistentEntry>();
  }

  public void add(String key, String value) throws IOException, PersistentFileException {
    // Get current entry and position, if any
    PersistentEntry entry = null;
    long prevPosition = 0;
    if (entries.containsKey(key)) {
      entry = entries.get(key);
      prevPosition = entry.valuePosition;
    }

    // Make sure we have an initial key block before adding any data
    if (freePosition == 0)
      addBlock();

    // Check and no-op if value has not changed from previous cached value
    if (entry != null){
      // We have an entry for the key, but do we have a cached value?
      if (entry.value == null){
        // No, fetch the current value for the key
        get(key);
      }

      // Now check if new value has changed
      if (entry.value.equals(value)){
        log.info("Ignore put for table '" + name + "' key '" + key + "' since value is unchanged");
        return;
      }
    }

    // Store new string value at end of file
    long valuePosition = file.appendString(key, prevPosition, value);

    // Update/add entry to point to new string value
    if (entry != null){
      entry.valuePosition = position;
    } else {
      // Need to allocate table space for new entry
      String keyEntryString = key + '\t' + PersistentFile.formatPosition(valuePosition) + CRLF +
          KEY_BLOCK_END_MARKER;
      byte[] keyBytesBuffer = keyEntryString.getBytes("UTF-8");
      int numBytesNeeded = keyBytesBuffer.length;

      // Make sure there is room in current block
      if (numBytesFree < numBytesNeeded)
        addBlock();

      // Grab the first free position in tables current block
      long keyPosition = freePosition;
      freePosition += numBytesNeeded - END_MARKER_SIZE;
      numBytesFree -= numBytesNeeded - END_MARKER_SIZE;

      // Write the new key in table
      // Technically, we don't need to write the position portion since later code will be doing that
      file.file.seek(keyPosition);
      file.file.write(keyBytesBuffer);

      // Point to the position of the 12-digit reference to value
      long entryPosition = keyPosition + numBytesNeeded - 12 - 2 - KEY_BLOCK_END_MARKER.length();

      // Add the new internal table entry
      entry = new PersistentEntry(key, entryPosition, valuePosition);
      entries.put(key, entry);
    }

    // Write the updated Table entry
    String positionString = PersistentFile.formatPosition(valuePosition);
    byte[] bytes = positionString.getBytes("UTF-8");
    file.file.seek(entry.entryPosition);
    file.file.write(bytes);
    
    // Cache the new value
    entry.value = value;
  }

  public long addBlock() throws IOException, PersistentFileException {
    // Allocate a new block
    long newBlockPosition = file.addBlock();

    if (freePosition == 0){
      // Link to initial block from TOC entry for table
      String positionString = PersistentFile.formatPosition(newBlockPosition);
      byte[] positionBytesBuffer = positionString.getBytes("UTF-8");
      int positionBytesSize = positionBytesBuffer.length;
      if (positionBytesSize != 12)
        throw new PersistentFileException("Internal error: Position size is not 12: " + positionBytesSize);
      file.file.seek(tocPosition);
      file.file.write(positionBytesBuffer);

      // Remember position of first block
      position = newBlockPosition;
    } else {
      // Append a link to new block at end of current block
      // Link is a tab (empty key) plus 12-digit position link to new block plus CRLF
      String linkString = '\t' + PersistentFile.formatPosition(newBlockPosition) + CRLF;
      byte[] linkBytesBuffer = linkString.getBytes("UTF-8");
      file.file.seek(freePosition);
      file.file.write(linkBytesBuffer);
    }

    // Update 'free' and space remaining (before adding new key
    freePosition = newBlockPosition;
    numBytesFree = PersistentFile.BLOCK_SIZE;

    // Return the position of the new block
    return newBlockPosition;
  }

  public String get(String key) throws IOException, PersistentFileException {
    // Check if there is currently an entry for this key
    if (entries.containsKey(key)) {
      // Yes, get the entry for the key
      PersistentEntry entry = entries.get(key);

      // Has the value for the key been loaded yet?
      if (entry.value == null){
        // No, read the value
        // Read arbitrary amount since we don't know length yet
        int initialReadAmount = 256;
        byte[] headerBuffer = new byte[initialReadAmount];
        file.file.seek(entry.valuePosition);
        int initialNumBytesRead = file.file.read(headerBuffer);
        String headerString = new String(headerBuffer);
        int i = headerString.indexOf('\t');
        if (i < 0)
          throw new PersistentFileException("Data corruption: Missing tab delimiter for value length");
        String lengthString = headerString.substring(0, i);
        int valueLength = 0;
        try {
          valueLength = Integer.parseInt(lengthString);
        } catch (NumberFormatException e){
          throw new PersistentFileException("Data corruption: Value length is not decimal numeric: " + lengthString);
        }
        
        // Find start of value bytes
        // Skip over tab, timestamp, tab, old value position, and tab before we hit key
        i += 1 + 28 + 1 + 12 + 1;
        i = headerString.indexOf('\t', i);
        if (i < 0)
          throw new PersistentFileException("Data corruption: Missing tab delimiter before key before value");
        // Skip over the key value
        i = headerString.indexOf('\t', i);
        if (i < 0)
          throw new PersistentFileException("Data corruption: Missing tab delimiter before key before value");
        // Skip over the tab immediately before value
        i = headerString.indexOf('\t', i);
        if (i < 0)
          throw new PersistentFileException("Data corruption: Missing tab delimiter before value");
        i++;

        // Get number of value bytes that were read with header
        int numValueBytesInHeader = initialNumBytesRead - i;

        // Now that we know actual length, did we ready enough?
        String value = null;
        if (numValueBytesInHeader < valueLength){
          // Get the leading portion of the value that we read
          value = new String(headerBuffer, i, numValueBytesInHeader);

          // Read and extract the rest of the value
          int numBytesLeftToRead = valueLength - numValueBytesInHeader;
          byte[] headerBuffer2 = new byte[numBytesLeftToRead];
          int nextNumBytesRead = file.file.read(headerBuffer2);
          String value2 = new String(headerBuffer2);

          // Combine the two parts of the value
          value += value2;
        } else {
          // Get the full value from the header read - ignore trailing CRLF
          value = new String(headerBuffer, i, valueLength);
        }

        // Cache the value in the in-memory entry
        entry.value = value;
        
        // Return the string value
        return value;
      }

      // Return the cached value
      return entry.value;
    } else
      // No entry, so implicitly no value for the key
      return null;
  }

  public ListMap<String, String> get() throws IOException, PersistentFileException {
    // Iterate over all keys and build a list of them
    ListMap<String, String> all = new ListMap<String, String>();
    for (String key: this)
      all.put(key, get(key));
    
    // Return the list of all keys and their values for the table
    return all;
  }

  public Iterator<String> iterator() {
    // Return iterator for all table names
    return new PersistentTableKeyIterator(this);
  }

  public String toString(){
    return "{Persistent table name: " + name + ", tocPosition: " + tocPosition +
        ", position: " + position + ", freePosition: " + freePosition +
        ", numBytesFree: " + numBytesFree + "}";  
  }
}
