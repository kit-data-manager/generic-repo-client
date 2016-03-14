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

import edu.kit.jcommander.generic.status.CommandStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic REST Client for all tasks.
 *
 * @author hartmann-v
 */
public abstract class AbstractGenericRestClient implements IDataManagerRestUrl {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGenericRestClient.class);

  /**
   * Status of the command on exit.
   */
  protected CommandStatus returnStatus;

  /**
   * Check command line arguments for correctness and validity. Usually done by
   * JCommander.
   */
  protected abstract void checkArguments() throws IllegalArgumentException;

  /**
   * Test settings of the DataManager.
   * Only tests will be executed. Only REST URL
   * and the rest authentication (accessKey, accessSecret) will be tested.

   * @return Deliver the settings.
   */
  protected final DataManagerPropertiesImpl testDataManagerSettings() {
    DataManagerPropertiesImpl properties = DataManagerPropertiesImpl.getDefaultInstance();
    if (!DataManagerPropertiesHelper.testRestSettings(properties)) {
      String message = "Invalid settings found! Please initialize the application (bin/setupRepo init -b -r).";
      LOGGER.error(message);
      IllegalArgumentException iae = new IllegalArgumentException(message);
      returnStatus = new CommandStatus(iae);
      throw iae;
    }
    return properties;
  }

  /**
   * Get return status of the command.
   *
   * @return the returnStatus
   */
  public CommandStatus getReturnStatus() {
    return returnStatus;
  }

}
