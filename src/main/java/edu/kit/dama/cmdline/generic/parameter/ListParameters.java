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
package edu.kit.dama.cmdline.generic.parameter;

import edu.kit.jcommander.generic.parameter.CommandLineParameters;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.dama.rest.client.access.GenericAccessClient;

/** 
 * Class holding the commandline parameters.
 * This class contains the parameters needed for lookup possible digital data
 * objects archived in the repository. 
 * 
 * @author hartmann-v
 */
@Parameters(commandNames = "access", commandDescription = "List all digital objects linked to predefined user/group/investigation.")
public class ListParameters extends CommandLineParameters {

  /**
   * Print the output in a human readable format enriched with additional information.
   */
  @Parameter(names = {"-r", "--humanReadable"}, description = "List in human readable format.")
  public boolean humanReadable;
  
  /**
   * Parameter for output messages.
   */
  @Parameter(names = {"-v", "--verbose"}, description = "Show also predefined settings.", required = false)
  public boolean verbose = false;

  /**
   * Default constructor.
   */
  public ListParameters() {
    super("list");
    this.humanReadable = false;
  }

  @Override
  public CommandStatus executeCommand() {
    return GenericAccessClient.executeCommand(this);
  }
}
