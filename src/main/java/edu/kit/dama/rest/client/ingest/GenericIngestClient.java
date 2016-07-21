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
package edu.kit.dama.rest.client.ingest;

import edu.kit.dama.rest.client.AbstractGenericRestClient;
import edu.kit.dama.rest.client.DataManagerPropertiesImpl;
import edu.kit.dama.rest.client.DataManagerPropertiesHelper;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.cmdline.generic.parameter.IngestParameters;
import edu.kit.dama.cmdline.generic.parameter.InitParameters.CommandLineFlags;
import edu.kit.dama.rest.admin.client.impl.UserGroupRestClient;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import edu.kit.dama.rest.client.generic.KIT_DM_REST_CLIENT;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic class for ingesting data to repository. All methods require properly
 * configured settings. To validate settings the following code snippet may be
 * used:
 * <pre>
 * {@code
 *   try {
 *     GenericIngestClient ingestClient = new GenericIngestClient();
 *     DataManagerPropertiesImpl myProperties = ingestClient.testDataManagerSettings();
 *   } catch (IllegalArgumentException iae) {
 *     iae.printStackTrace();
 *   }
 * }
 * </pre>
 *
 * @see DataManagerPropertiesHelper
 * @see edu.kit.dama.rest.client.DataManagerProperties
 *
 * @author hartmann-v
 */
public class GenericIngestClient extends AbstractGenericRestClient implements IMetadata4Ingest {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericIngestClient.class);
  /**
   * Directory to ingest.
   */
  private File inputDir;
  /**
   * Note for the ingest.
   */
  private String note;

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param ip parsed arguments.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(IngestParameters ip) {
    return ingestData(ip.inputDir, ip.note);
  }

  /**
   * Ingest data to repository using default settings.
   *
   * @param pInputDir input directory or file to transfer to repository.
   * @param pNote note for digital object.
   * @return command status.
   */
  public static CommandStatus ingestData(File pInputDir, String pNote) {
    return ingestData(pInputDir, pNote, null);
  }
  /**
   * Ingest data to repository using default settings.
   *
   * @param pInputDir input directory or file to transfer to repository.
   * @param pNote note for digital object.
   * @param pMetadata4Ingest instance for additional operations during ingest.
   * @see IMetadata4Ingest
   * @return command status.
   */
  public static CommandStatus ingestData(File pInputDir, String pNote, IMetadata4Ingest pMetadata4Ingest) {
    GenericIngestClient gic = new GenericIngestClient();
    gic.registerMetadata4Ingest(pMetadata4Ingest);
    DataManagerPropertiesImpl properties;
    CommandStatus commandStatus;
    try {
      properties = gic.testDataManagerSettings();
      DataManagerPropertiesHelper.initializeWebDav(properties);
      commandStatus = gic.executeCommand(properties, pInputDir, pNote);
    } catch (IllegalArgumentException ex) {
      LOGGER.error(null, ex);
      commandStatus = new CommandStatus(ex);
    }

    return commandStatus;
  }

  /**
   * Ingest data to repository using default settings.
   *
   * @param pProperties Properties to connect to KIT Data Manager.
   * @param pInputDir input directory or file to transfer to repository.
   * @param pNote note for digital object.
   * @return command status.
   */
  public static CommandStatus ingestData(DataManagerPropertiesImpl pProperties, File pInputDir, String pNote) {
    return ingestData(pProperties, pInputDir, pNote, null);
  }

  /**
   * Ingest data to repository using default settings.
   *
   * @param pProperties Properties to connect to KIT Data Manager.
   * @param pInputDir input directory or file to transfer to repository.
   * @param pNote note for digital object.
   * @param pMetadata4Ingest instance for additional operations during ingest.
   * @return command status.
   */
  public static CommandStatus ingestData(DataManagerPropertiesImpl pProperties, 
          File pInputDir, String pNote, IMetadata4Ingest pMetadata4Ingest) {
    GenericIngestClient gic = new GenericIngestClient();
    gic.registerMetadata4Ingest(pMetadata4Ingest);
    CommandStatus commandStatus;
    try {
      EnumSet<CommandLineFlags> flags = EnumSet.noneOf(CommandLineFlags.class);
      flags.add(CommandLineFlags.DATA_MANAGER_BASE);
      flags.add(CommandLineFlags.REST_AUTHENTICATION);
      flags.add(CommandLineFlags.DATA_MANAGER_CONTEXT);
      flags.add(CommandLineFlags.ACCESSPOINT);
      flags.add(CommandLineFlags.REST_AUTHENTICATION);
      DataManagerPropertiesHelper.testSettings(pProperties, flags);
      commandStatus = gic.executeCommand(pProperties, pInputDir, pNote);
    } catch (IllegalArgumentException ex) {
      LOGGER.error(null, ex);
      commandStatus = new CommandStatus(ex);
    }

    return commandStatus;
  }

  /**
   * Ingest data in KIT Data Manager. Attention: Settings for KIT Data Manager
   * should be tested in beforehand!
   * <br/> Example:
   * <pre>
   * {@code
   *  DataManagerPropertiesImpl properties = testDataManagerSettings();
   * // if webDAV is needed also add the following line.
   *  DataManagerPropertiesHelper.initializeWebDav(properties);
   * }
   * </pre>
   *
   * @param pProperties
   * @param pInputDir
   * @param pNote
   * @return
   */
  private CommandStatus executeCommand(DataManagerPropertiesImpl pProperties, File pInputDir, String pNote) {
    inputDir = pInputDir;
    note = pNote;
    int exitValue = 0;

    // Test for valid arguments.
    checkArguments();
    // Workflow for ingest: 
    // 1. initialize REST
    // 2. Create new digital object
    // 3. Register new digital object
    // 4. PreIngest 
    // 5. Prepare ingest
    // 6. Wait for ingest status to continue.
    // 7. Get WebDAV-URL
    // 8. Transfer data via ADALAPI? to staging URL.
    // 9. Register transfer to be satisfied.
    // Steps 2-7 are now done by the generic client.
    // Read settings
    if (metadata4Ingest == null) {
      metadata4Ingest = this;
    }
    try {
      if (pProperties != null) {
        // <editor-fold defaultstate="collapsed" desc="Initialize REST">
        SimpleRESTContext context = new SimpleRESTContext(pProperties.getAccessKey(), pProperties.getAccessSecret());
        // Read ids
        BaseMetaDataRestClient bmdrc = new BaseMetaDataRestClient(pProperties.getRestUrl() + REST_BASE_META_DATA_PATH, context);
        UserGroupRestClient ugrc = new UserGroupRestClient(pProperties.getRestUrl() + REST_USER_GROUP_PATH, context);
        UserDataWrapper user = ugrc.getUserById(-1); // Get actual user
        DigitalObject digitalObj = createDigitalObject(user.getEntities().get(0));

        // Maybe some adaptions from properitary client.
        digitalObj = metadata4Ingest.modifyMetadata(digitalObj);

        Long investigationId = Long.parseLong(pProperties.getInvestigation());
        DigitalObjectWrapper registeredDigitalObject = bmdrc.addDigitalObjectToInvestigation(investigationId, digitalObj, pProperties.getUserGroup());
        digitalObj = registeredDigitalObject.getEntities().get(0);
        LOGGER.info("digitalObj.getInvestigation" + digitalObj.getInvestigation());

        // Mabe some additional stuff from properitary client.
        metadata4Ingest.preTransfer(digitalObj.getDigitalObjectIdentifier());

        returnStatus = null;
        KIT_DM_REST_CLIENT.initialize(context, pProperties.getRestUrl());
        LOGGER.debug("User group1: " + pProperties.getUserGroup());
        returnStatus = KIT_DM_REST_CLIENT.performDataIngestTransferClient(digitalObj.getDigitalObjectId().getStringRepresentation(), pProperties.getAccessPoint(), inputDir, pProperties.getUserGroup());
      }
    } catch (FileNotFoundException | IllegalArgumentException ex) {
      LOGGER.error(null, ex);
      returnStatus = new CommandStatus(ex);
    }
    LOGGER.info("ReturnValue (CommandStatus): " + getReturnStatus());
    LOGGER.info("Exit value: " + exitValue);

    return getReturnStatus();
  }

  @Override
  protected void checkArguments() throws IllegalArgumentException {
    if (!inputDir.isDirectory()) {
      String message = String.format("Input directory '%s' is not a directory or doesn't exist!", inputDir.getAbsolutePath());
      LOGGER.error(message);
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Create digital object for registering dataset in KIT Datamanager.
   *
   * @param pProperties Label of the digital object. (Should be more or less
   * unique.)
   * @param pUploader User which will be registered as uploader and
   * experimenter.
   * @return
   */
  private DigitalObject createDigitalObject(UserData pUploader) {
    DigitalObject digitalObject = new DigitalObject();
    // Determine start and end date according to the last modified dates.
    SimpleDateFormat sdf = new SimpleDateFormat("YYYY_MM_dd'T'HH_mm");
    Date endDate = new Date(inputDir.lastModified());
    Date startDate = new Date(endDate.getTime() - 1000); // if inputDir is a file instead of a directory.
    if (inputDir.isDirectory()) {
      File[] listFiles = inputDir.listFiles();
      long startDateLong = startDate.getTime();
      for (File file : listFiles) {
        long timeStamp = file.lastModified();
        if (timeStamp < startDateLong) {
          startDateLong = timeStamp;
        }
      }
      startDate = new Date(startDateLong);
    }
    if (!startDate.before(endDate)) {
      startDate = new Date(endDate.getTime() - 1000);
    }
    digitalObject.setStartDate(startDate);
    digitalObject.setEndDate(endDate);
    digitalObject.setLabel("DigitalObject_" + sdf.format(startDate));

    digitalObject.setUploader(pUploader);
    digitalObject.setUploadDate(new Date());
    digitalObject.addExperimenter(pUploader);
    digitalObject.setNote(note);
    return digitalObject;
  }

// <editor-fold defaultstate="collapsed" desc="PreIngest">
  /**
   * Instance implementing the pre ingest. Default: this.
   */
  private IMetadata4Ingest metadata4Ingest = null;

  /**
   * Register a pre index operation if necessary.
   * If parameter is null the default operations implemented
   * in this class will be executed.
   *
   * @param pMetadata4Ingest Instance holding pre ingest method.
   */
  public void registerMetadata4Ingest(IMetadata4Ingest pMetadata4Ingest) {
      metadata4Ingest = pMetadata4Ingest;
  }

  @Override
  public void preTransfer(String pDigitalObjectId) {
    // nothing to do during generic ingest.
    LOGGER.debug("Nothing to do during pre ingest!");
  }

  @Override
  public DigitalObject modifyMetadata(DigitalObject pDigitalObject) {
    // nothing to do during generic ingest.
    LOGGER.debug("Nothing to do. Digital object is already filled!");
    return pDigitalObject;
  }
// </editor-fold>

}
