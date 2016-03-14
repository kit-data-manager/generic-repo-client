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
import edu.kit.jcommander.generic.parameter.CommandLineParameters;

/**
 * Class holding the commandline parameters. This class contains the base
 * parameters needed by most commands.
 *
 * @author hartmann-v
 */
public abstract class SetupCommandLineParameters extends CommandLineParameters {

  
  /**
   * Parameter for usage.
   */
  @Parameter(names = {"-i", "--interactive"}, description = "Query all values interactively via commandline.", required = false)
  public boolean interactive;

  /**
   * Parameter for usage.
   */
  @Parameter(names = {"-v", "--verbose"}, description = "Turn on verbosing report.", required = false)
  public boolean verbose;

  /**
   * Constructor used by derived classes.
   * @param pCommandName Command name listed when help is displayed.
   */
  protected SetupCommandLineParameters(String pCommandName) {
    super(pCommandName);
  }

}
