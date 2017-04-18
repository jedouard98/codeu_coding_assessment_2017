// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codeu.codingchallenge;

import java.io.IOException;

final class MyJSONParser implements JSONParser {
  private final static char[] ESCAPABLE_CHAR = {'t', 'n', '"', '\\'};
  // all allowable escapable characters in JSON-lite
  
  @Override
  public JSON parse(String in) throws IOException {
    String json = new String(in); // defensive copy of original string
    return checkJSON(json);
  }
  
  //checks the legality of the string and returns the value to go into memory
  private String checkString(String string) throws IOException {
    // if the string is less than 2, than it is impossible for it to be valid
    if (string.length() <= 1)
      throw new IOException("String not found: Missing quotation marks.");
    // if the string is less than 2, than it is impossible for it to be valid
    if (!(string.charAt(0) == '"' && string.charAt(string.length()-1) == '"'))
      throw new IOException("String not found: Missing quotation marks.");
    // variable denoting that a backslash was found and an escapable character
    // follow
    boolean lookingForEscapable = false;
    // iterating through string, not checking the leading and ending quotation mark
    for (int i = 1; i < string.length() - 1; i++) {
      // if currently looking for escapable character, check the current character
      // for its validity
      if (lookingForEscapable) {
        if (!isEscapable(string.charAt(i)))
          throw new IOException("String not found: Invalid escapable character in string.");
        else
          lookingForEscapable = false; // found the character and no longer looking for it
      }
      // if a backslash is found, then the next character should be escapable
      else if (string.charAt(i) == '\\')
        lookingForEscapable = true;
      // if for some reason a quotation mark was found (for the case it's in the beginning
      // and not proceeded by a backslash, then throw an exception
      else if (string.charAt(i) == '\"')
        throw new IOException("String not found: Quotation mark not escaped.");
    }
    // if still looking for escapable character, then the ending backslash is illegal
    if (lookingForEscapable)
      throw new IOException("String not found: Backslash not escaped.");
    return string.substring(1, string.length() - 1);
  }
  
  // determines if characters are escapable
  private boolean isEscapable(char c) {
    for (int i = 0; i < ESCAPABLE_CHAR.length; i++)
      if (c == ESCAPABLE_CHAR[i])
        return true;
    return false;
  }
  /*
   * This function is divided up into four stages of looking through the JSON:
   * 1. Processing the key
   * 2. Processing the colon
   * 3. Processing the value
   * 4. Processing the comma/end of document
   * 
   */
  private MyJSON checkJSON(String json) throws IOException {
    if (!(json.charAt(0) == '{' && json.charAt(json.length() - 1) == '}'))
      throw new IOException("JSON not found: Missing brackets.");
    // the object that contains all stored information
    MyJSON jsonObject = new MyJSON(); 
    // variable to iterate through entire string
    int i = 1;
    // a loop that goes through the entirety of the json string
    while (i < json.length() - 1) {
      // two values for the key value pair
      String key = null;
      Object value = null;
      int beginIndex = -1; // the start of the key substring
      int endIndex = -1; // the end of the key substring
      
      // Stage One: Processing the Key
      // Stage One-A: Looking for the start of the key
      while (beginIndex == -1 && i < json.length() - 1 ) {
        // stops the search when the first quotation mark is found
        if (json.charAt(i) == '"') {
          beginIndex = i;
        } 
        // however if the first character is not a quotation mark, then it couldn't
        // possibly be a legal string, which a key must be.
        else if (json.charAt(i) != ' ' && json.charAt(i) != '\n') {
          throw new IOException("JSON not found: Misformatted String for beginning key");
        }
        i++;
      }
      // Stage One-B (Optional): Looking for the end of the key
      // This stage is only necessary for if the start of a key was found. This
      // is specifically for cases such as: { }, which has no start of key, but is
      // legal.
      if (beginIndex != -1) {
        while (endIndex == -1 && i < json.length() - 1) {
          if (json.charAt(i) == '"') {
            if (json.charAt(i - 1) != '\\') {
              endIndex = ++i;
            }
          }
          else {
            i++;
          }
        }
        // if the search is done, but no legal end of key was found, then throw an exception
        if (endIndex == -1)
          throw new IOException("JSON not found: End of String for key not found");
        // make sure the contents of the string are legal, and return it for the key!
        key = checkString(json.substring(beginIndex, endIndex));
      }
      // Stage Two: Processing the Colon
      // there should be nothing in between the colon and the key besides 
      // an arbitrary amount of white space and potential line breaks.
      while (json.charAt(i) != ':' && i < json.length() - 1) {
          if (json.charAt(i) != ' ' || json.charAt(i) != '\n')
            throw new IOException("JSON not found: Colon separating key and value not found");
          i++;
     }
     // move the i forward off the colon character
     i++;
     
     // Stage Three: Processing the value part as either a JSON or a string
     // This should only be done if the key is null for the corner case
     // { }, where it won't have any key
     if (key != null) {
       beginIndex = -1; // the start of the value substring
       endIndex = -1; // the end of the value substring
       //Stage Three-A: Looking for either a quotation mark or bracket
       char endChar = 0;
       while (beginIndex == -1 && i < json.length() - 1) {
         if (json.charAt(i) == '"') {
           // value is a string
           beginIndex = i;
           endChar = '"';
         }
         else if (json.charAt(i) == '{') {
           // value is an object
           beginIndex = i;
           endChar = '}';
         }
         else if (json.charAt(i) != ' ' && json.charAt(i) != '\n' ) {
           // value is neither; an illegal character was between the colon and value
           throw new IOException("JSON not found: Value misformatted");
         }
         i++;
       }
       // a value of some kind is expected if the key is not null
       if (beginIndex == -1)
         throw new IOException("JSON not found: No Value found for key");
       
       // Stage Three-B: Looking for either an end quotation mark or end bracket
       
       // First case: if we are looking for a string, end when a non-escaped quotation
       // appears
       if (endChar == '"') {
         while (endIndex == -1 && i < json.length() - 1) {
           if (json.charAt(i) == endChar) {
             // if a quotation mark was found and not escaped (the end of string)
             if (endChar == '"' && json.charAt(i - 1) != '\\')
               endIndex = ++i;
             }
           else {
             i++;
           }   
         }
       }
       // Second case: if we are looking for an end bracket, end when there is another
       // bracket found that is NOT inside quotation marks (a string) or part of an
       // object found within an object
       else if(endChar == '}') {
         // variable that denotes seeking the end of an object within an object
         boolean lookingForEndBracket = false;
         // variable that denotes seeking the end of a string within an object
         boolean lookingForEndQuotation = false;
         while (endIndex == -1 && i < json.length() - 1) {
           if (!lookingForEndQuotation && json.charAt(i) == '}') {
             if (!lookingForEndBracket)
               endIndex = ++i;
             else
               lookingForEndBracket = false;
           }
           else if (lookingForEndQuotation && json.charAt(i) == '"') {
             lookingForEndQuotation = false;
             i++;
           }
           else if (json.charAt(i) == '"') {
             lookingForEndQuotation = true;
             i++;
           }
           else if (json.charAt(i) == '{') {
             lookingForEndBracket = true;
           }
           else {
             i++;
           }
         }
       }
       // if the end of a value isn't found, then the json is misformatted
       if (endIndex == -1)
         throw new IOException("JSON not found: End of value not found");
       
       String valueString = json.substring(beginIndex, endIndex);
       
       // place the caught values into memory for later access
       // use the appropriate method for if the json object is a string or a 
       // object
       if (endChar == '"') {
         value = checkString(valueString);
         jsonObject.setString(key, (String) value);
       }
       if (endChar == '}') {
         // perform the same operations if there is an object inside the object
         value = checkJSON(valueString);
         jsonObject.setObject(key, (MyJSON) value);
        }
      }
     // Stage Four: Processing the end of a key value pair by looking for a comma
     // or the end of document
     while (i < json.length() - 1 && json.charAt(i) != ',') {
       // another character found instead of the end of the document or a comma
       if (json.charAt(i) != ' ' && json.charAt(i) != '\n')
         throw new IOException("JSON not found: Comma separating key value pairs not found");
       i++;
     }
     // moving the iterator off the comma character for the next run
     i++;
    }
    return jsonObject;
  }
}
