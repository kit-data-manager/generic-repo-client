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
package edu.kit.dama.rest.client.generic.helper;

import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.UserData;


/**
 * The creation of CustomOutputObject is based on builder pattern. 
 * The output to be displayed is build from multiple objects 
 *  * @author kb3353
 *
 */
public final class CustomOutputObject {
	/**
   * Digital object.
   */
	private final DigitalObject digitalObject;
  /** 
   * Corresponding user data.
   */
	private final UserData userData;

	/**
	 * Constructor for creating the custom object
	 * @param builder
	 */
	private CustomOutputObject(CustomOutputObjectBuilder builder) {
		this.digitalObject = builder.digitalObject;
		this.userData = builder.userData;
	}
  
  // <editor-fold defaultstate="collapsed" desc="Getters">
	/**
   * Get digital object.
   * @return digital object.
   */
	public DigitalObject getDigitalObject() {
		return digitalObject;
	}
  /**
   * 
   * Get user data.
   * @return user data.
   */
	public UserData getUserData() {
		return userData;
	}
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Builder for instance.">
  /**
   * Builder class for class 'CustomOutputObject'.
   */
	public static class CustomOutputObjectBuilder {
    /** 
     * User of the digital object.
     */
		private UserData userData;
    /** 
     * Digital object.
     */
		private DigitalObject digitalObject;

    /** Constructor containing all information.
     * 
     * @param userData user.
     * @param digitalObject digital object.
     */
		public CustomOutputObjectBuilder(UserData userData, DigitalObject digitalObject) {
			this.userData = userData;
			this.digitalObject = digitalObject;
		}
    /** 
     * Builder for user.
     * @param userData user.
     * @return builder instance.
     */
		public CustomOutputObjectBuilder userData(UserData userData) {
			this.userData = userData;
			return this;
		}
    /**
     * Builder for digital object.
     * 
     * @param digitalObject digital object.
     * @return builder instance.
     */
		public CustomOutputObjectBuilder digitalObject(DigitalObject digitalObject) {
			this.digitalObject = digitalObject;
			return this;
		}
    /** 
     * Build instance of CustomOutputObject.
     * @return instance of CustomOutputObject.
     */
		public CustomOutputObject build() {
			return new CustomOutputObject(this);
		}
	}
  // </editor-fold>
}