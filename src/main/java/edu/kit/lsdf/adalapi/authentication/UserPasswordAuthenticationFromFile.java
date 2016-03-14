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
package edu.kit.lsdf.adalapi.authentication;

import edu.kit.lsdf.adalapi.exception.AuthenticationInputMethodException;
import edu.kit.dama.rest.client.DataManagerPropertiesImpl;
import edu.kit.dama.rest.client.DataManagerProperties;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication with username and password. Overwrites the existing class of
 * ADALAPI so this class should be placed in the beginning of the classpath. The
 * credentials will be read out from a properties file. Mandatory properties
 * are:
  * <ul><li>Username</li><li>Password</li></ul>
 *
 * @author hartmann-v
 */
public class UserPasswordAuthenticationFromFile extends AbstractAuthentication {

  /**
   * Logger for debug messages.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(UserPasswordAuthenticationFromFile.class);

  @Override
  public final void createVector() {
    AuthField userField = new AuthField(DataManagerProperties.USERNAME.getKey(), "", "Please insert username!", false, false);
    AuthField groupField = new AuthField(DataManagerProperties.PASSWORD.getKey(), "", "Please insert your password!", true, false);

    super.getUserInteractionVector().add(userField);
    super.getUserInteractionVector().add(groupField);
  }

  @Override
  public final Collection<AuthField> authenticate() throws AuthenticationInputMethodException {
    Collection<AuthField> authVector = getUserInteractionVector();
    DataManagerPropertiesImpl defaultInstance = DataManagerPropertiesImpl.getDefaultInstance();
    String newValue;
    for (AuthField field : authVector) {
      newValue = defaultInstance.getPropertyValue(DataManagerProperties.getPropertyByKey(field.getLabel()));
      field.setValue(newValue);
      LOGGER.trace("New Value for '" + field.getLabel() + "': " + newValue);
    }
    return authVector;
  }
}
