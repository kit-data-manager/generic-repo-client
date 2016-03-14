/*
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.rest.client;

/**
 * All supported properties. Each property has a description and a key used in
 * the properties file storing the values.
 *
 * @author hartmann-v
 */
public enum DataManagerProperties {

  /**
   * Label of the REST server url.
   */
  REST_SERVER_LABEL("RestServer", "URL of the REST server (e.g.: http://datamanager.kit.edu:8080/KITDM"),
  /**
   * Label of the accessPoint.
   */
  ACCESS_POINT_LABEL("AccessPoint", "Access point for ingest to/download from KIT Data Manager"),
  /**
   * userid of the 'user'
   */
  USER_ID("userId", "User id of the user."),
  /**
   * accesskey of the 'user'
   */
  ACCESS_KEY("accessKey", "Credentials (key) for accessing KIT DM via REST"),
  /**
   * access secret of the user.
   */
  ACCESS_SECRET("accessSecret", "Credentials (secret) for accessing KIT DM via REST"),
  /**
   * investigation to which the actions belong to.
   */
  INVESTIGATION("investigation", "Investigation the ingest/access belongs to."),
  /**
   * group to which the actions belong to.
   */
  USER_GROUP("group", "Group the ingest/access belongs to."),
  /**
   * Key for the username field (webDAV). Don't change key!
   */
  USERNAME("Username", "Username of the webDAV user."),
  /**
   * Key for the password field (webDAV). Don't change key!
   */
  PASSWORD("Password", "Password of the webDAV user.");
  /**
   * Key of the property.
   */
  private String key;
  /**
   * Description of the property.
   */
  private String description;

  /**
   * Constructor.
   *
   * @param pKey key of the property.
   * @param pDescription description of the property.
   */
  DataManagerProperties(String pKey, String pDescription) {
    key = pKey;
    description = pDescription;
  }

  /**
   * Get key of the property.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Get description of the property.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get property by key.
   *
   * @param pKey Key.
   * @return Matching property.
   */
  public static DataManagerProperties getPropertyByKey(String pKey) {
    for (DataManagerProperties item : DataManagerProperties.values()) {
      if (item.getKey().equals(pKey)) {
        return item;
      }
    }
    throw new IllegalArgumentException("Illegal key name: " + pKey);
  }
}
