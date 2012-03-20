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

package com.basetechnology.s0.agentserver;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.basetechnology.s0.agentserver.appserver.HandlePut;
import com.basetechnology.s0.agentserver.util.JsonUtils;
import com.basetechnology.s0.agentserver.util.ShaUtils;

public class User {
  static final Logger log = Logger.getLogger(User.class);

  public static final int DEFAULT_MAX_USERS = 100;
  public static final boolean DEFAULT_ADMIN_ONLY_USER_CREATE = false;
  public static final boolean DEFAULT_MAIL_CONFIRM_USER_CREATE = false;
  final public static int MIN_ID_LENGTH = 4;
  final public static int MIN_PASSWORD_LENGTH = 4;
  public long timeCreated;
  public long timeLastEdited;
  public String id;
  public String password;
  public String passwordHint;
  public String fullName;
  public String displayName;
  public String nickName;
  public String organization;
  public String bio;
  public String interests;
  public String email;
  public String shaId;
  public String shaPassword;
  public Boolean incognito;
  public String comment;
  public Boolean approved;
  
  public static User noUser = new User("none");
  public static User nullUser = new User("null");
  public static User publicUser = new User("public");
  public static User allUser = new User("*");
  
  public User(String id){
    this(id, "", "", "", "", "", "", "", "", "", false, "", true, null, null);
  }
  
  public User(
      String id,
      String password,
      String passwordHint,
      String fullName,
      String displayName,
      String nickName,
      String organization,
      String bio,
      String interests,
      String email,
      Boolean incognito,
      String comment,
      Boolean approved,
      String shaId,
      String shaPassword){
    this.timeCreated = System.currentTimeMillis();
    this.timeLastEdited = this.timeCreated;
    this.id = id;
    this.password = password;
    this.passwordHint = passwordHint;
    this.fullName = fullName;
    this.displayName = displayName;
    this.nickName = nickName;
    this.organization = organization;
    this.bio = bio;
    this.interests = interests;
    this.email = email;
    this.incognito = incognito;
    this.comment = comment;
    this.approved = approved;
    this.shaId = shaId;
    this.shaPassword = shaPassword;
  }
  
  public void generateSha(){
    shaId = ShaUtils.createSha(id);
    shaPassword = ShaUtils.createSha(password);
  }
  
  public void update(AgentServer agentServer, User updated) throws AgentServerException {
    // TODO: Only update time if there are any actual changes
    this.timeLastEdited = this.timeCreated;
    if (updated.password != null)
      this.password = updated.password;
    if (updated.passwordHint != null)
      this.passwordHint = updated.passwordHint;
    if (updated.fullName != null)
      this.fullName = updated.fullName;
    if (updated.displayName != null)
      this.displayName = updated.displayName;
    if (updated.nickName != null)
      this.nickName = updated.nickName;
    if (updated.bio != null)
      this.bio = updated.bio;
    if (updated.organization != null)
      this.organization = updated.organization;
    if (updated.interests != null)
      this.interests = updated.interests;
    if (updated.incognito != null)
      this.incognito = updated.incognito;
    if (updated.email != null)
      this.email = updated.email;
    if (updated.comment != null)
      this.comment = updated.comment;
    // User cannot update the "approved" field
    
    // Update may have changed password, so regenerate SHAa
    generateSha();
    
    // Persist the changes
    agentServer.persistence.put(this);
  }

  static public User fromJson(JSONObject userJson) throws AgentServerException {
    return fromJson(userJson, false);
  }
  
  static public User fromJson(JSONObject userJson, boolean update) throws AgentServerException {
    // TODO: Whether empty fields should be null or empty strings
    if (! userJson.has("id") && ! update)
      throw new AgentServerException("User id is missing");
    String id = userJson.optString("id", null);
    String password = userJson.optString("password", null);
    String passwordHint = userJson.optString("password_hint", null);
    String fullName = userJson.optString("full_name", null);
    String displayName = userJson.optString("display_name", null);
    String nickName = userJson.optString("nick_name", null);
    String organization = userJson.optString("organization", null);
    String bio = userJson.optString("bio", null);
    String interests = userJson.optString("interests", null);
    Boolean incognito = userJson.has("incognito") ? userJson.optBoolean("incognito") : null;
    String email = userJson.optString("email", null);
    String comment = userJson.optString("comment", null);
    Boolean approved = userJson.has("approved") ? userJson.optBoolean("approved") : null;
    
    // Ignore SHAs on update, but preserve them for non-update
    String shaId = null;
    String shaPassword = null;
    if (! update){
      shaId = userJson.optString("sha_id", null);
      shaPassword = userJson.optString("sha_password", null);
    }
    JsonUtils.validateKeys(userJson, "User", new ArrayList<String>(Arrays.asList(
        "id", "password", "password_hint", "full_name", "display_name", "nick_name",
        "organization", "bio", "interests", "email", "incognito", "comment", "approved",
        "sha_id", "sha_password")));
    return new User(id, password, passwordHint, fullName, displayName, nickName,
        organization, bio, interests, email, incognito, comment, approved, shaId, shaPassword);

  }
  
  public JSONObject toJson(){
    return toJson(true, true);
  }

  public JSONObject toJson(boolean withPassword, boolean withPasswordHint){
    JSONObject userJson = new JSONObject();
    try {
      userJson.put("id", id);
      if (withPassword)
        userJson.put("password", password);
      if (withPasswordHint)
        userJson.put("password_hint", passwordHint);
      userJson.put("full_name", fullName);
      userJson.put("display_name", displayName);
      userJson.put("nick_name", nickName);
      userJson.put("organization", organization);
      userJson.put("bio", bio);
      userJson.put("interests", interests);
      userJson.put("incognito", incognito);
      userJson.put("email", email);
      userJson.put("comment", comment);
      userJson.put("approved", approved);
      userJson.put("sha_id", shaId);
      userJson.put("sha_password", shaPassword);
    } catch (JSONException e){
      e.printStackTrace();
      log.error("Exception generating User JSON - " + e.getMessage());
    }
    return userJson;
  }

  public String toJsonString(){
    return toJsonString(true, true);
  }

  public String toJsonString(boolean withPassword, boolean withPasswordHint){
    return "{\"id\": \"" + (id == null ? "null" : id) +
        (withPassword ? "\", \"password\": \"" + (password == null ? "" : password) : "") +
        (withPasswordHint ? "\", \"password_hint\": \"" + (passwordHint == null ? "" : passwordHint) : "") +
        "\", \"full_name\": \"" + (fullName == null ? "" : fullName) +
        "\", \"display_name\": \"" + (displayName == null ? "" : displayName) +
        "\", \"nick_name\": \"" + (nickName == null ? "" : nickName) +
        "\", \"organization\": \"" + (organization == null ? "" : organization) +
        "\", \"bio\": \"" + (bio == null ? "" : bio) +
        "\", \"interests\": \"" + (interests == null ? "" : interests) +
        "\", \"incognito\": " + (incognito == null ? "" : incognito) +
        ", \"email\": \"" + (email == null ? "" : email) +
        "\", \"comment\": \"" + (comment == null ? "" : comment) +
        "\", \"approved\": " + (approved == null ? "" : approved) +
        ", \"sha_id\": \"" + (shaId == null ? "" : shaId) +
        "\", \"sha_password\": \"" + (shaPassword == null ? "" : shaPassword) + "\"}"; 
  }
  
  public String toString(){
    return toJsonString();
  }
}
