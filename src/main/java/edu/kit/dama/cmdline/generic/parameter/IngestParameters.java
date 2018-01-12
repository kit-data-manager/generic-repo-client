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
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.dama.rest.client.ingest.GenericIngestClient;
import edu.kit.jcommander.converter.FileConverter;
import edu.kit.jcommander.validator.DirectoryValidator;
import java.util.List;

/**
 * Class holding the commandline parameters for an ingest.
 *
 * @author hartmann-v
 */
@Parameters(commandNames = "ingest", commandDescription = "Ingest data from input directories "
        + "to repository. Each input directory "
        + "will be ingested independently as a digital object.")
public class IngestParameters extends CommandLineParameters {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestParameters.class);

  /**
   * Parameter holding output directory for the output files.
   */
  @Parameter(names = {"-i", "--inputdir"}, description = "One or more input directories. All files from each directory will be ingested to repository. Example -i C:\\data\\folder1 C:\\data\\folder2", required = true, variableArity = true, converter = FileConverter.class, validateWith = DirectoryValidator.class)
  public List<File> inputDir;

  /**
   * Parameter holding output directory for the output files.
   */
  @Parameter(names = {"-n", "--note"}, description = "Note for the administrative metadata. Example -n \"Any important data.\"", required = true)
  public String note;

  // ToDo: Add parameter for parent digital object and maybe also provenance metadata as XML
  // idea: Allow also multiple parameters.
  // @Parameter(names = {"-p", "--parent"}, description = "Digital object ID for the Note for the administrative metadata. Example -p bda80b0a-0c4d-463b-845c-66793875be9c")
  // @Parameter(names = {"-r", "--provenance"}, description = "Add provenance metadata as XML (maybe as a file???). Example -r provenanceMetadata.xml")
  /**
   * Default constructor.
   */
  public IngestParameters() {
    super("ingest");
  }

  @Override
  public final CommandStatus executeCommand() {
    return GenericIngestClient.executeCommand(this);
  }

}
