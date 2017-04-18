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

import java.util.Collection;
import java.util.HashMap;

final class MyJSON implements JSON {
  // map holding all memory found from the json-lite
  private HashMap<String, Object> json = new HashMap<>();
  
  @Override
  public JSON getObject(String name) {
    Object obj = json.get(name);
    if(obj instanceof JSON)
      return (JSON) obj;
    return null;
  }

  @Override
  public JSON setObject(String name, JSON value) {
    json.put(name, value);
    return this;
  }
  
  @Override
  public String getString(String name) {
    Object obj = json.get(name);
    if(obj instanceof String)
      return (String) obj;
    return null;
  }

  @Override
  public JSON setString(String name, String value) {
    json.put(name, value);
    return this;
  }
  
  @Override
  public void getObjects(Collection<String> names) {
    for (String name : json.keySet())
      if (json.get(name) instanceof JSON)
        names.add(name);
  }

  @Override
  public void getStrings(Collection<String> names) {
    for (String name : json.keySet())
      if (json.get(name) instanceof String)
        names.add(name);
  }
}
