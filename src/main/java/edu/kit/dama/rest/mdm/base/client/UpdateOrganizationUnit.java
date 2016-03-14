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
package edu.kit.dama.rest.mdm.base.client;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.dama.rest.client.setup.GenericSetupClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commandline parameters for the organization unit.
 * @author hartmann-v
 */
@Parameters(commandNames = "updateorganization", commandDescription = "Update an organization unit.")
public class UpdateOrganizationUnit extends SetupCommandLineParameters {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateOrganizationUnit.class);
  /**
   * Id of the study.
   */
  @Parameter(names = {"-o", "--organizationUnitId"}, description = "'Id' of the organization unit. ", required = false)
  Long studyId;


 /**
   * Default constructor.
   */
  public UpdateOrganizationUnit() {
    super("updateorganization");
  }

  /**
   * Get the study id.
   *
   * @return study id of the study.
   */
  public final Long getOrganizationUnitId() {
    return studyId;
  }

  @Override
  public CommandStatus executeCommand() {
    GenericSetupClient.setVerbose(verbose);
    return GenericSetupClient.executeCommand(this, interactive);
  }

}
