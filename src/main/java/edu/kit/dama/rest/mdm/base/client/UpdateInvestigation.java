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
 * Commandline parameters for the investigation.
 * @author hartmann-v
 */
@Parameters(commandNames = "updateinvestigation", commandDescription = "Update an investigation.")
public class UpdateInvestigation extends SetupCommandLineParameters {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateInvestigation.class);
  /**
   * Id of the study.
   */
  @Parameter(names = {"-d", "--investigationId"}, description = "'Id' of the investigation. ", required = false)
  Long investigationId;


 /**
   * Default constructor.
   */
  public UpdateInvestigation() {
    super("updateinvestigation");
  }

  /**
   * Get investigation id.
   *
   * @return investigation id of the investigation.
   */
  public final Long getInvestigationId() {
    return investigationId;
  }

  @Override
  public CommandStatus executeCommand() {
    GenericSetupClient.setVerbose(verbose);
    return GenericSetupClient.executeCommand(this, interactive);
  }

}
