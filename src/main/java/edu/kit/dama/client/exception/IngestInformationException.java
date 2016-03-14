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
package edu.kit.dama.client.exception;

/**
 * Can't fetch ingest information
 * @author kb3353
 */
public class IngestInformationException extends Exception {

  /**
   * Version uid of class.
   */
	private static final long serialVersionUID = 1L;

	/**
   * Constructor with specific message.
   * 
   * @param message message of the exception.
   */
	public IngestInformationException(String message){
		super(message);
	}
}
