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
import java.util.Arrays;
import java.util.List;

/**
 * Class holding the commandline parameters. This class contains the parameters
 * needed for lookup possible digital data objects archived in the repository.
 *
 * @author hartmann-v
 */
@Parameters(commandNames = "search", commandDescription = "Search for digital objects linked to predefined user/group/investigation. "
        + "ATTENTION: This command is only available if MetaStore is installed.")
public class SearchParameters extends CommandLineParameters {

  /**
   * Which types should be used for search. (multiple types are allowed)
   */
  @Parameter(names = {"-y", "--type"}, description = "Which type(s) should be used for search.(not supported yet!)", required = false)
  public List<String> type = Arrays.asList("_all");

  /**
   * Which index should be used for search. (multiple indices are allowed)
   */
  @Parameter(names = {"-i", "--index"}, description = "Which indices should be used for search.(not supported yet!)", required = false)
  public List<String> index = Arrays.asList("_all");
  /**
   * For which terms should be searched.
   */
  @Parameter(names = {"-t", "--term"}, description = "Search term(s). Each term has to contain at least 3 characters!")
  public List<String> term;
  /**
   * Which fields should be used for search. (multiple fields are allowed) *Not*
   * supported yet!
   */
  @Parameter(names = {"-f", "--field"}, description = "Fields used for search. (not supported yet!)")
  public List<String> field;

  /**
   * Default constructor.
   */
  public SearchParameters() {
    super("search");
  }

  @Override
  public CommandStatus executeCommand() {
    return GenericAccessClient.executeCommand(this);
  }
}
