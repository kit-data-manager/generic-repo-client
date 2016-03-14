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
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.rest.client.setup.GenericSetupClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commandline parameters for the metadata schema.
 * 
 * @author hartmann-v
 */
@Parameters(commandNames = "createschema", commandDescription = "Create a metadata schema.")
public class MetadataSchemaBuilder extends SetupCommandLineParameters {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSchemaBuilder.class);
  /**
   * Topic of the investigation.
   */
  @Parameter(names = {"-u", "--url"}, description = "'Metadata schema URL' of the metadata schema.", required = false)
  String metadataSchemaUrl = "http://example.org/schema/2015-01";
  /**
   * Note for the investigation.
   */
  @Parameter(names = {"-d", "--identifier"}, description = "'Metadata schema identifier' of the metadata schema. ", required = false)
  String identifier = "example";


 /**
   * Default constructor.
   */
  public MetadataSchemaBuilder() {
    super("createschema");
  }
  /**
   * Set the url.
   *
   * @param pUrl url
   * @return instance of builder
   */
  public MetadataSchemaBuilder url(String pUrl) {
    metadataSchemaUrl = pUrl;
    return this;
  }

  /**
   * Set the identifier.
   *
   * @param pIdentifier identifier
   * @return instance of builder
   */
  public MetadataSchemaBuilder identifier(String pIdentifier) {
    identifier = pIdentifier;
    return this;
  }

  /**
   * Build investigation instance using set attributes.
   *
   * @return Instance of MetaDataSchema.
   */
  public MetaDataSchema build() {
    MetaDataSchema mds = new MetaDataSchema();
    mds.setMetaDataSchemaUrl(metadataSchemaUrl);
    mds.setSchemaIdentifier(identifier);
    return mds;

  }

  @Override
  public CommandStatus executeCommand() {
    GenericSetupClient.setVerbose(verbose);
    return GenericSetupClient.executeCommand(this.build(), interactive);
  }

}
