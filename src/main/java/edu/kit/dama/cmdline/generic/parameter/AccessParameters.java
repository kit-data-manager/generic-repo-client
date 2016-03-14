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
import edu.kit.jcommander.converter.FileConverter;
import edu.kit.jcommander.validator.DirectoryValidator;
import java.io.File;

/** 
 * Class holding the commandline parameters.
 * This class contains the parameters needed for downloading data archived in 
 * the repository. 
 * 
 * @author hartmann-v
 */
@Parameters(commandNames = "access", commandDescription = "Download digital object.")
public class AccessParameters extends CommandLineParameters {

   /**
   * Parameter holding end of the time period for archiving.
   */
  @Parameter(names = {"-i", "--interactive"}, description = "Select one of the listed digital objects.")
  public boolean interactive = false;
  
  /**
   * Parameter for usage.
   */
  @Parameter(names = {"-d", "--digitalObjectId"}, description = "'DigitalObjectId' of the digital object. "
          + "If no Id is given interactive mode will be activated.", required = false)
  public String digitalObjectId;
  /**
   * Parameter holding output directory for the output files.
   */
  @Parameter(names = {"-o", "--outputdir"}, description = "The output directory. All files from digital object will be downloaded to this directory. Example -o C:\\data\\folder", required = true, converter = FileConverter.class, validateWith = DirectoryValidator.class)
  public File outputDir;

 /**
   * Default constructor.
   */
  public AccessParameters() {
    super("access");
  }
  
  @Override
  public CommandStatus executeCommand() {
    return GenericAccessClient.executeCommand(this);
  }
}
