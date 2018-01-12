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

import edu.kit.dama.client.exception.BaseMetadataException;
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
import edu.kit.jcommander.generic.status.Status;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.fzk.grid.util.JWhich;
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
   * Ingest data to repository using default settings. Each directory contains
   * one digital object.
   *
   * @param pInputDir input list of directories to transfer to repository.
   * @param pNote note for digital object.
   * @return command status.
   */
  public static CommandStatus ingestData(List<File> pInputDir, String pNote) {
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
  public static CommandStatus ingestData(List<File> pInputDir, String pNote, IMetadata4Ingest pMetadata4Ingest) {
    GenericIngestClient gic = new GenericIngestClient();
    registerPlugin(gic, pMetadata4Ingest);
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
    return ingestData(Arrays.asList(pInputDir), pNote, pMetadata4Ingest);
  }

  /**
   * Register plugin for additional tasks. The plugins are found dynamically
   * (via service loader) if they are placed in the classpath. If a plugin is
   * already provided the service loader will not be called.
   *
   * @param pGic Generic Ingest Client for ingesting digital object(s).
   * @param pPlugin Selected plugin if there is already one.
   */
  protected static void registerPlugin(GenericIngestClient pGic, IMetadata4Ingest pPlugin) {
    if (pPlugin != null) {
      pGic.registerMetadata4Ingest(pPlugin);
    } else {
      IMetadata4Ingest selectedPlugin = null;

      for (IMetadata4Ingest plugin : ServiceLoader.load(IMetadata4Ingest.class)) {
        if (selectedPlugin != null) {
          LOGGER.warn("More than one plugin found! First plugin will be used!");
          String plugin1 = selectedPlugin.getClass().getName();
          String plugin2 = plugin.getClass().getName();
          LOGGER.warn("Selected plugin: '{}'\n"
                  + "Skipped plugin: '{}'", JWhich.whichJar(plugin1),
                  JWhich.whichJar(plugin2));
        } else {
          selectedPlugin = plugin;
          LOGGER.debug("Found plugin '{}' for metadata ingest!", selectedPlugin.getClass().toString());
        }
      }
      pGic.registerMetadata4Ingest(selectedPlugin);
    }

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
      commandStatus = gic.executeCommand(pProperties, Arrays.asList(pInputDir), pNote);
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
   * @param pProperties Properties to connect to KIT Data Manager.
   * @param pInputDir input directories to transfer to repository. Files are not
   * allowed.
   * @param pNote note for digital object.
   * @return command status.
   */
  private CommandStatus executeCommand(DataManagerPropertiesImpl pProperties, List<File> pInputDir, String pNote) {
    int exitValue = 0;
    returnStatus = new CommandStatus(Status.SUCCESSFUL);
    if (pProperties != null) {
      SimpleRESTContext context = new SimpleRESTContext(pProperties.getAccessKey(), pProperties.getAccessSecret());
      UserGroupRestClient ugrc = new UserGroupRestClient(pProperties.getRestUrl() + REST_USER_GROUP_PATH, context);
      UserData user = ugrc.getUserById(-1).getEntities().get(0); // Get actual user
      KIT_DM_REST_CLIENT.initialize(context, pProperties.getRestUrl());
      Map<Status, List<CommandStatus>> collect = pInputDir.parallelStream().map((inputDirectory) -> ingestDirectory(pProperties, inputDirectory, pNote, user)).collect(Collectors.groupingBy(CommandStatus::getStatus));

      // <editor-fold defaultstate="collapsed" desc="Summarize ingests and set status">
      List<CommandStatus> succeededIngests = collect.get(Status.SUCCESSFUL);
      int noOfSucceededIngests = 0;
      int noOfFailedIngests = 0;
      if (succeededIngests != null) {
        noOfSucceededIngests = succeededIngests.size();
      }
      List<CommandStatus> failedIngests = collect.get(Status.FAILED);
      if (failedIngests != null) {
        noOfFailedIngests = failedIngests.size();
        if (noOfFailedIngests > 0) {
          returnStatus = failedIngests.get(0);
          String message = String.format("%d of %d ingest(s) failed!", noOfFailedIngests, noOfFailedIngests + noOfSucceededIngests);
          returnStatus.setException(new Exception(message));
        }
      }
      LOGGER.info("{} ingests were made!", noOfSucceededIngests);
      // </editor-fold>
    }
    exitValue = returnStatus.getStatusCode();
    LOGGER.debug("Exit value: " + exitValue);

    return getReturnStatus();
  }

  /**
   * Ingest single directory.
   *
   * @param pProperties Properties to connect to KIT Data Manager.
   * @param pInputDir input directory to transfer to repository. Files are not
   * allowed.
   * @param pNote note for digital object.
   * @param pUser User who ingests the directory.
   * @return command status.
   */
  private CommandStatus ingestDirectory(DataManagerPropertiesImpl pProperties, File pInputDir, String pNote, UserData pUser) {
    CommandStatus commandStatus;
    try {
      DigitalObject digitalObject = registerDigitalObject(pProperties, pInputDir, pNote, pUser);
      // <editor-fold defaultstate="collapsed" desc="Initialize REST">
      // Read ids
      LOGGER.info("Start ingest for directory '{}'", pInputDir.getAbsolutePath());
//        returnStatus = KIT_DM_REST_CLIENT.performDataIngestTransferClient(digitalObj.getDigitalObjectId().getStringRepresentation(), pProperties.getAccessPoint(), inputDir, pProperties.getUserGroup());
      commandStatus = KIT_DM_REST_CLIENT.performDataIngest(digitalObject.getDigitalObjectId().getStringRepresentation(), pProperties.getAccessPoint(), pInputDir, pProperties.getUserGroup());
      LOGGER.info("Ingest for directory '{}' finished! Status: {} - {}", pInputDir.getAbsolutePath(), commandStatus.getStatusCode(), commandStatus.getStatusMessage());
    } catch (BaseMetadataException | FileNotFoundException e) {
      LOGGER.error("Error during ingest!", e);
      commandStatus = new CommandStatus(e);
      LOGGER.error("Ingest for directory '{}' finished! Status: {} - {}", pInputDir.getAbsolutePath(), commandStatus.getStatusCode(), commandStatus.getStatusMessage());
      LOGGER.info("Exception: {}", commandStatus.getException().toString());

    }
    return commandStatus;
  }

  /**
   * Register one digital object in KIT Data Manager. Attention: Settings for
   * KIT Data Manager should be tested in beforehand!
   * <br/> Example:
   * <pre>
   * {@code
   *  DataManagerPropertiesImpl properties = testDataManagerSettings();
   * // if webDAV is needed also add the following line.
   *  DataManagerPropertiesHelper.initializeWebDav(properties);
   * }
   * </pre>
   *
   * @param pProperties Properties to connect to KIT Data Manager.
   * @param inputDirectory input directory to transfer to repository. Files are
   * not allowed.
   * @param pNote note for digital object.
   * @param pUser user executing this ingest
   * @return Map with digital objects and their related directories.
   */
  private DigitalObject registerDigitalObject(DataManagerPropertiesImpl pProperties, File inputDirectory, String pNote, UserData pUser) throws BaseMetadataException {
    note = pNote;
    inputDir = inputDirectory;
    DigitalObject digitalObject = null;
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
    if (pProperties != null) {
      SimpleRESTContext context = new SimpleRESTContext(pProperties.getAccessKey(), pProperties.getAccessSecret());
      // Read ids
      BaseMetaDataRestClient bmdrc = new BaseMetaDataRestClient(pProperties.getRestUrl() + REST_BASE_META_DATA_PATH, context);
      synchronized (this) {
        // <editor-fold defaultstate="collapsed" desc="Prepare digital object for ingest.">
        digitalObject = createDigitalObject(pUser);
        // Maybe some adaptions from properitary client.
        digitalObject = metadata4Ingest.modifyMetadata(inputDirectory, digitalObject);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Register digital object at repository.">
        Long investigationId = Long.parseLong(pProperties.getInvestigation());
        DigitalObjectWrapper registeredDigitalObject = bmdrc.addDigitalObjectToInvestigation(investigationId, digitalObject, pProperties.getUserGroup());
        digitalObject = registeredDigitalObject.getEntities().get(0);
        LOGGER.trace("Digital Object registered at repository: {}", digitalObject);
        // </editor-fold>

        // Mabe some additional stuff from properitary client.
        metadata4Ingest.preTransfer(inputDirectory, digitalObject.getDigitalObjectIdentifier());
      }
    }
    return digitalObject;
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
   * @return digital object with some prefilled values.
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
   * Register a pre index operation if necessary. If parameter is null the
   * default operations implemented in this class will be executed.
   *
   * @param pMetadata4Ingest Instance holding pre ingest method.
   */
  public void registerMetadata4Ingest(IMetadata4Ingest pMetadata4Ingest) {
    if (metadata4Ingest != null) {
      LOGGER.debug("Remove metadata ingest plugin: '{}'", metadata4Ingest.getClass().toString());
    }
    if (pMetadata4Ingest != null) {
      LOGGER.debug("Register metadata ingest plugin: '{}'", pMetadata4Ingest.getClass().toString());
    }

    metadata4Ingest = pMetadata4Ingest;
  }

  @Override
  public void preTransfer(File pInputDir, String pDigitalObjectId) throws BaseMetadataException {
    // nothing to do during generic ingest.
    LOGGER.debug("Nothing to do during pre ingest for digital object id '{}'!", pDigitalObjectId);
  }

  @Override
  public DigitalObject modifyMetadata(File pInputDir, DigitalObject pDigitalObject) throws BaseMetadataException {
    // nothing to do during generic ingest.
    LOGGER.debug("Nothing to do. Digital object is already filled!");
    return pDigitalObject;
  }
// </editor-fold>

}
