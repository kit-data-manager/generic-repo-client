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
 * Interface holding the urls for the different services.
 * @author hartmann-v
 */
public interface IDataManagerRestUrl {
	/**
	 * Base path for all REST services.
	 */
	String REST_BASE_PATH = "/rest/";
  /**
   * Path for base metadata rest services.
   */
  String REST_BASE_META_DATA_PATH = REST_BASE_PATH + "basemetadata/";
  /**
   * Path for data organization services. (Not used yet.)
   */
  String REST_DATA_ORGANIZATION_PATH = REST_BASE_PATH + "dataorganization/";
  /**
   * Path for staging rest services.
   */
  String REST_STAGING_PATH = REST_BASE_PATH + "staging/";
  /**
   * Path for user/group rest services.
   */
  String REST_USER_GROUP_PATH = REST_BASE_PATH + "usergroup/";
  
}
