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
package edu.kit.dama.rest.client.access;

import edu.kit.dama.rest.client.DataManagerPropertiesImpl;
import edu.kit.dama.rest.client.DataManagerPropertiesHelper;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.jcommander.generic.status.Status;
import edu.kit.dama.cmdline.generic.parameter.AccessParameters;
import edu.kit.dama.cmdline.generic.parameter.ListParameters;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.dataorganization.service.exception.DataOrganizationException;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import edu.kit.dama.rest.client.AbstractGenericRestClient;
import static edu.kit.dama.rest.client.IDataManagerRestUrl.REST_BASE_META_DATA_PATH;
import edu.kit.dama.rest.client.generic.KIT_DM_REST_CLIENT;
import edu.kit.dama.rest.dataorganization.client.impl.DataOrganizationRestClient;
import edu.kit.dama.util.StdIoUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.ws.WebServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic class for accessing data from repository. All methods require
 * properly configured settings. To validate settings the following code snippet
 * may be used:
 *
 * @see AbstractGenericRestClient#testDataManagerSettings()  <pre>
 * {@code
 *   try {
 *     GenericAccessClient accessClient = new GenericAccessClient();
 *     DataManagerPropertiesImpl myProperties = accessClient.testDataManagerSettings();
 *   } catch (IllegalArgumentException iae) {
 *     iae.printStackTrace();
 *   }
 * }
 * </pre>
 *
 * @see DataManagerPropertiesHelper
 * @see edu.kit.dama.rest.client.DataManagerProperties
 * @author hartmann-v
 */
public class GenericAccessClient extends AbstractGenericRestClient {

  private File outputDir;

  private String digitalObjectId;

  private boolean interactive;

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericAccessClient.class);

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param lp parsed arguments.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(ListParameters lp) {
    GenericAccessClient gac = new GenericAccessClient();

    boolean humanReadable = lp.humanReadable;
    boolean verbose = lp.verbose;
    return gac.listDigitalObjects(humanReadable, verbose);
  }

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param ap parsed arguments.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(AccessParameters ap) {
    return new GenericAccessClient().accessData(ap.outputDir, ap.digitalObjectId, ap.interactive);
  }

  /**
   * Ingest data to repository using default settings.
   *
   * @param pOutputDir input directory or file to transfer to repository.
   * @param pDigitalObjectIdentifier note for digital object.
   * @return command status.
   */
  public static CommandStatus accessData(File pOutputDir, String pDigitalObjectIdentifier) {
    GenericAccessClient gac = new GenericAccessClient();
    CommandStatus commandStatus = gac.accessData(pOutputDir, pDigitalObjectIdentifier, false);

    return commandStatus;
  }

  /**
   * Download data from repository.
   *
   * @param pOutputDir Existing target directory.
   * @param pDigitalObjectId 'digitalObjectIdentifier' of digital object.
   * @param pInteractive interactive mode.
   * @return status of the command.
   */
  private CommandStatus accessData(File pOutputDir, String pDigitalObjectId, boolean pInteractive) {
    int exitValue = 0;
    returnStatus = new CommandStatus(Status.SUCCESSFUL);
    outputDir = pOutputDir;
    digitalObjectId = pDigitalObjectId;
    interactive = pInteractive;

    // Test for valid arguments.
    checkArguments();
        // Workflow for access: 
    // 1. initialize REST
    // 2. Select digital object
    // 3. Prepare download
    // 4. Wait for download status to continue.
    // 5. Get WebDAV-URL
    // 6. Transfer data via ADALAPI? from staging URL to output URL.

    // Read settings
    try {
      DataManagerPropertiesImpl properties = testDataManagerSettings();
      DataManagerPropertiesHelper.initializeWebDav(properties);

      if (properties != null) {
        // <editor-fold defaultstate="collapsed" desc="Initialize REST">
        SimpleRESTContext context = new SimpleRESTContext(properties.getAccessKey(), properties.getAccessSecret());
        digitalObjectId = selectDigitalObject(properties, context);
        LOGGER.info("digitalObjId = " + digitalObjectId);
        KIT_DM_REST_CLIENT.initialize(context, properties.getRestUrl());
        returnStatus = KIT_DM_REST_CLIENT.performDataDownloadDataTransferClient(properties.getAccessPoint(), digitalObjectId, outputDir);
      }
    } catch (FileNotFoundException | DataOrganizationException | IllegalArgumentException ex) {
      LOGGER.error(null, ex);
      returnStatus = new CommandStatus(ex);
    }
    LOGGER.info("ReturnValue (CommandStatus): " + returnStatus);
    LOGGER.info("Exit value: " + exitValue);
    return getReturnStatus();
  }

  /**
   * List digital objects accessible by defined user/group/investigation.
   *
   * @param pHumanReadable Human readable or only listing.
   * @param pVerbose Show also actual settings.
   * @return Status of the command.
   */
  private CommandStatus listDigitalObjects(boolean pHumanReadable, boolean pVerbose) {
    try {
      DataManagerPropertiesImpl properties = testDataManagerSettings();

      if (properties != null) {
        if (pVerbose) {
          PrintStream output = System.out;
          output.println(properties.toString());
        }
        // <editor-fold defaultstate="collapsed" desc="Initialize REST">
        SimpleRESTContext context = new SimpleRESTContext(properties.getAccessKey(), properties.getAccessSecret());
        BaseMetaDataRestClient bmdrc = new BaseMetaDataRestClient(properties.getRestUrl() + REST_BASE_META_DATA_PATH, context);
        String header = String.format("List of digital objects (group: '%s'):", properties.getUserGroup());
        printDigitalObjects(bmdrc, header, pHumanReadable, properties.getUserGroup());
        returnStatus = new CommandStatus(Status.SUCCESSFUL);

      }
    } catch (IllegalArgumentException iae) {
      LOGGER.error(null, iae);
      returnStatus = new CommandStatus(iae);
    }
    return getReturnStatus();
  }

  @Override
  protected void checkArguments() {
    // do nothing at the moment as arguments already checked by jCommander
    if (!outputDir.isDirectory()) {
      String message = String.format("Output directory '%s' is not a directory or doesn't exist!", outputDir.getAbsolutePath());
      LOGGER.error(message);
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Determine digitalobjectidentifier for download from KIT Datamanager. Checks
   * if selected digital object has (already) a data organization.
   *
   * @param pProperties Properties for access KIT Datamanager.
   * @param pContext AAI context for REST access
   * @return digital object identifier
   * @throws DataOrganizationException if no data organization is available.
   */
  private String selectDigitalObject(DataManagerPropertiesImpl pProperties, SimpleRESTContext pContext) throws DataOrganizationException {
    String digitalObjectIdentifier = null;
    BaseMetaDataRestClient bmdrc = new BaseMetaDataRestClient(pProperties.getRestUrl() + REST_BASE_META_DATA_PATH, pContext);
    if (interactive || digitalObjectId == null) {
      String[] allDigitalObjIds = printDigitalObjects(bmdrc, "Please choose a digital object via given index:", true, pProperties.getUserGroup());
      int index;
      index = StdIoUtils.readIntFromStdInput(allDigitalObjIds.length, 1) - 1;
      digitalObjectIdentifier = allDigitalObjIds[index];
    } else {
      if (digitalObjectId != null) {
        digitalObjectIdentifier = digitalObjectId;
      }
    }
    // check for validity
    DigitalObjectWrapper digitalObjectWrapper = bmdrc.getDigitalObjectByDOI(digitalObjectIdentifier, pProperties.getUserGroup());
    if (digitalObjectWrapper.getCount() < 1) {
      throw new IllegalArgumentException("No valid digital object identifier chosen!");
    }
    Long baseId = digitalObjectWrapper.getEntities().get(0).getBaseId();
    DataOrganizationRestClient dorc = new DataOrganizationRestClient(pProperties.getRestUrl() + REST_DATA_ORGANIZATION_PATH + "organization/", pContext);
    try {
      dorc.getRootNode(pProperties.getUserGroup(), baseId, null, null, null);
    } catch (WebServiceException wse) {
      throw new DataOrganizationException("Digital object not valid (yet)! (No data organization defined!)");
    }
    return digitalObjectIdentifier;
  }

  /**
   * Write all digital objects of given group to a string array.
   *
   * @param pBmdrc REST client for base metadata.
   * @param pHeader Head line of the output.
   * @param pHumanReadable human readable format (add note and date of digital
   * object)
   * @param pGroupId show only digital objects of the given group (null ->
   * USERS)
   * @return array holding all digital objects of given group.
   */
  private String[] printDigitalObjects(BaseMetaDataRestClient pBmdrc, String pHeader, boolean pHumanReadable, String pGroupId) {
    final int ALL_INVESTIGATIONS = 0;
    // Get all digital Objects.
    DigitalObject digitalObject;
    // Determine start and end date according to the last modified dates.
    SimpleDateFormat sdf = new SimpleDateFormat("YYYY_MM_dd'T'HH_mm");
    PrintStream output = System.out;
    DigitalObjectWrapper allDigitalObjects = pBmdrc.getAllDigitalObjects(ALL_INVESTIGATIONS, 0, Integer.MAX_VALUE, pGroupId);

    String[] allDigitalObjIds = new String[allDigitalObjects.getCount()];
    String[] allDigitalObjIdDescriptions = new String[allDigitalObjects.getCount()];
    int index = 0;
    for (DigitalObject item : allDigitalObjects.getEntities()) {
      digitalObject = pBmdrc.getDigitalObjectById(item.getBaseId(), pGroupId).getEntities().get(0);
      allDigitalObjIds[index] = digitalObject.getDigitalObjectIdentifier();
      Date startDate = digitalObject.getStartDate();
      if (startDate == null) {
        startDate = new Date(0);
      }
      allDigitalObjIdDescriptions[index] = String.format("date: %s, note: %s",
              sdf.format(startDate),
              digitalObject.getNote());
      index++;
    }
    index = 1;
    output.println(pHeader);
    for (int pVIndex = 0; pVIndex < allDigitalObjIds.length; pVIndex++) {
      StringBuilder adaptedFormatString = new StringBuilder("%3d: %s");
      if (pHumanReadable) {
        adaptedFormatString.append(" %s");
      }
      output.println(String.format(adaptedFormatString.toString(), index++,
              allDigitalObjIds[pVIndex],
              allDigitalObjIdDescriptions[pVIndex]));
    }
    return allDigitalObjIds;
  }

}
