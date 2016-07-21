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
package edu.kit.dama.rest.client.generic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.dama.client.exception.BaseMetadataException;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.jcommander.generic.status.Status;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.client.generic.helper.CustomOutputObject;
import edu.kit.dama.rest.client.generic.helper.RESTClientHelper;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;

/**
 * This is the generic client which will perform various operations such as 1.
 * Create base metadata i.e. Base metadata for Study Investigation and
 * DigitalObject 2. Perform DataIngest in KIT DM 3. Get List of all IngestedData
 * from KITDM 4. Perform Download of data specified by the digital object ID
 *
 * @author kb3353
 *
 */
public final class KIT_DM_REST_CLIENT {

  /**
   * Logger for debug messages.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(KIT_DM_REST_CLIENT.class);
  /**
   * Instance accessing REST services on a higher level.
   */
  private static RESTClientHelper clientHelper;

  /**
   * Initialize the REST client.
   * <b>This method has to be called first!</b>
   *
   * @param restContext AAI context for the REST services.
   * @param baseURL Base URL of the KIT DataManager.
   */
  public static void initialize(SimpleRESTContext restContext, String baseURL) {
    clientHelper = new RESTClientHelper(restContext, baseURL);
  }

  /**
   * This is a Utility Class and hence does not require any public constructor
   *
   */
  private KIT_DM_REST_CLIENT() {

  }

  /**
   * This method can be used to create the base metadata for the data which you
   * wish to ingest/upload. The method will create the base metadata for Study,
   * Investigation and Digital Data.
   *
   * @param defaultStudy The default study metadata
   * @param defaultInvestigation The default investigation metadata
   * @param digitalObject The default digital object metadata
   * @param group the group under which the basemetadata will be created for
   * e.g. eCodicology_group or NANOSCOPY_GROUP
   *
   * @return CommandStatus or OperationStatus
   */
  public static CommandStatus createBaseMetaData(Study defaultStudy, Investigation defaultInvestigation, DigitalObject digitalObject, String group) {
    LOGGER.debug("Creating base metadata for study: '{}'\ninvestigation: '{}'\ndigital data '{}' for group '{}'",
            defaultStudy.getTopic(),
            defaultInvestigation.getTopic(),
            digitalObject.getLabel(),
            group);

    CommandStatus status = null;

    // Check user validity
    UserGroup nanoscoypGroupID = clientHelper.getSpecificGroupID(group);

    if (nanoscoypGroupID.getGroupId() == null) {
      String message = "Unable to create the study as following group: " + group + " does not exists";
      LOGGER.error(message);
      status = new CommandStatus(Status.FAILED, new BaseMetadataException(message), defaultStudy);
      return status;
    }
    Study nanoscopyStudy = clientHelper.createStudy(nanoscoypGroupID.getGroupId(), defaultStudy);

    Investigation nanoscopyInvestigation = clientHelper.createInvestigation(group, nanoscopyStudy, defaultInvestigation);
    if (nanoscopyInvestigation == null) {
      String message = "Unable to create the investigation: " + defaultInvestigation.getTopic();
      LOGGER.error(message);
      status = new CommandStatus(Status.FAILED, new BaseMetadataException(message), defaultInvestigation);
      return status;
    }
    DigitalObject nanoscopyDigitalObject;
    try {
      nanoscopyDigitalObject = clientHelper.createNanoscopyDigitalObject(group, nanoscopyInvestigation.getInvestigationId(), digitalObject);
    } catch (Exception ex) {
      String message = "Unable to add the digital object: " + digitalObject.getLabel()
              + " to the investigation: " + defaultInvestigation.getTopic();
      LOGGER.error(message + ex);
      status = new CommandStatus(Status.FAILED, new BaseMetadataException(message + ex), digitalObject);
      return status;
    }

    status = new CommandStatus(Status.SUCCESSFUL, null, nanoscopyDigitalObject);
    return status;
  }

  /**
   * This method can be used to list the already ingested and available digital
   * data from the KIT DM.
   * <b>Attention:</b> If there are more than 10 entries only the last 10
   * entries are listed.
   *
   * @param group specifying under which the ingested metadata will be searched
   * @return CommandStatus
   */
  public static CommandStatus listContent(String group) {

    LOGGER.debug("Generating list of available data for group: " + group);

    List<IngestInformation> listEntries = clientHelper.getIngestInformationIDs(Integer.MAX_VALUE, INGEST_STATUS.INGEST_FINISHED.getId());
    CommandStatus status = new CommandStatus(Status.SUCCESSFUL);

    // Get all IngestID
    // Get all Information for 'maxEntries' IDs
    int maxEntries = 10;
    int startIndex = 0;
    int noOfEntries = listEntries.size();

    List<IngestInformation> subList;
    if (noOfEntries < maxEntries) {
      subList = listEntries.subList(startIndex, noOfEntries);
    } else {
      // If more than 'maxEntries' the show only the last 'maxEntries'
      startIndex = noOfEntries - maxEntries;
      subList = listEntries.subList(startIndex, noOfEntries);
    }
    List<IngestInformation> ingestInformation = clientHelper.getIngestInformation(subList);
    // Get Detailed Information for each DigitalObject
    List<DigitalObject> digitalObjectInformationById = clientHelper.getDigitalObjectInformationById(group, ingestInformation);
    List<CustomOutputObject> customObjectsList = new ArrayList<>();

    for (DigitalObject digitalObject : digitalObjectInformationById) {
      UserData specificUser = clientHelper.getSpecificUser(digitalObject.getUploader().getUserId());
      CustomOutputObject customObject = new CustomOutputObject.CustomOutputObjectBuilder(specificUser, digitalObject).build();
      customObjectsList.add(customObject);
    }
    status.setReturnObject(customObjectsList);

    return status;
  }

  /**
   * This method can be used to upload/ingest the data into the KIT Data
   * Manager. The data will be transfered to KIT Data Manger via the WebDav
   * protocol using ADALAPI AbstractFile
   *
   * @param digitalObjectID The data identified by the digital object which will
   * be downloaded
   * @param accessMethod The accessmethod is the protocol that will be used to
   * perform download for e.g. WebDav
   * @param dataSource The directory on the local machine the data will ingested
   * from.
   * @return CommandStatus or OperationStatus
   * @throws FileNotFoundException will be thrown if the dataSource is not a
   * valid File or Directory
   */
  public static CommandStatus performDataIngest(String digitalObjectID, String accessMethod, File dataSource) throws FileNotFoundException {
    return performDataIngest(digitalObjectID, accessMethod, dataSource, null);
  }

  /**
   * This method can be used to upload/ingest the data into the KIT Data
   * Manager. The data will be transfered to KIT Data Manger via the WebDav
   * protocol using ADALAPI AbstractFile
   *
   * @param digitalObjectID The data identified by the digital object which will
   * be downloaded
   * @param accessMethod The accessmethod is the protocol that will be used to
   * perform download for e.g. WebDav
   * @param dataSource The directory on the local machine the data will ingested
   * from.
   * @param groupId The group the digital object belongs to.
   * @return CommandStatus or OperationStatus
   */
  public static CommandStatus performDataIngest(String digitalObjectID, String accessMethod, File dataSource, String groupId) throws FileNotFoundException {

    LOGGER.debug("Performing data ingest for digital object identified by: " + digitalObjectID + " from data source at path: " + dataSource.getAbsolutePath());

    if (!(dataSource.isFile()) && (!dataSource.isDirectory())) {
      LOGGER.error("Incorrect data source", new FileNotFoundException(dataSource.getAbsolutePath()));
    }
    CommandStatus status = new CommandStatus(Status.FAILED);

    IngestInformation createdIngestEntity = clientHelper.createIngestEntity(digitalObjectID, accessMethod, groupId);
    LOGGER.debug("Created ingest entity in database for digital data: " + digitalObjectID);
    IngestInformation ingestInformation = clientHelper.getSpecifiedIngestInformation(createdIngestEntity.getId());

    if (ingestInformation.getStatus() == INGEST_STATUS.PRE_INGEST_SCHEDULED.getId()) {
      status = clientHelper.performIngestADALAPI(dataSource, ingestInformation);
    }
    return status;
  }

  /**
   * This method can be used to upload/ingest the data into the KIT Data
   * Manager. The data will be transfered to KIT Data Manger via the WebDav
   * protocol using Data Transfer Client
   *
   * @param digitalObjectID The data identified by the digital object which will
   * be downloaded
   * @param accessMethod The accessmethod is the protocol that will be used to
   * perform download for e.g. WebDav
   * @param dataSource The directory on the local machine the data will ingested
   * from.
   * @return CommandStatus object containing the result if the transfer was
   * successful or not
   * @throws FileNotFoundException will be thrown if the dataSource is not a
   * valid File or Directory
   */
  public static CommandStatus performDataIngestTransferClient(String digitalObjectID, String accessMethod, File dataSource) throws FileNotFoundException {
    return performDataIngestTransferClient(digitalObjectID, accessMethod, dataSource, null);
  }

  /**
   * This method can be used to upload/ingest the data into the KIT Data
   * Manager. The data will be transfered to KIT Data Manger via the WebDav
   * protocol using Data Transfer Client
   *
   * @param digitalObjectID The data identified by the digital object which will
   * be downloaded
   * @param accessMethod The accessmethod is the protocol that will be used to
   * perform download for e.g. WebDav
   * @param dataSource The directory on the local machine the data will ingested
   * from.
   * @param groupId The groupID the digital object belongs to.
   * @return CommandStatus object containing the result if the transfer was
   * successful or not
   * @throws FileNotFoundException will be thrown if the dataSource is not a
   * valid File or Directory
   */
  public static CommandStatus performDataIngestTransferClient(String digitalObjectID, String accessMethod, File dataSource, String groupId) throws FileNotFoundException {

    LOGGER.debug("Performing data ingest for digital object identified by: " + digitalObjectID + " from data source at path: " + dataSource.getAbsolutePath());
    LOGGER.debug("User group: " + groupId);

    if (!(dataSource.isFile()) && (!dataSource.isDirectory())) {
      LOGGER.error("Incorrect data source", new FileNotFoundException(dataSource.getAbsolutePath()));
    }
    CommandStatus status = new CommandStatus(Status.FAILED);

    IngestInformation createdIngestEntity = clientHelper.createIngestEntity(digitalObjectID, accessMethod, groupId);
    LOGGER.debug("Created ingest entity in database for digital data: " + digitalObjectID);
    IngestInformation ingestInformation = clientHelper.getSpecifiedIngestInformation(createdIngestEntity.getId());

    if (ingestInformation.getStatus() == INGEST_STATUS.PRE_INGEST_SCHEDULED.getId()) {
      status = clientHelper.performIngestDataTransferClient(dataSource, ingestInformation);
    }
    return status;
  }

  /**
   * This method can be used to download the actual digital data onto your local
   * machine using ADALAPI The method uses WebDav protocol to download the data.
   *
   * @param accessMethod The accessmethod is the protocol that will be used to
   * perform download for e.g. WebDav
   * @param digitalObjectID The data identified by the digital object which will
   * be downloaded
   * @param destination The directory on the local machine where the data will
   * be downloaded
   *
   * @return CommandStatus Status of the command (success or failed).
   * @throws FileNotFoundException will be thrown if the destination is not a
   * valid directory
   */
  public static CommandStatus performDataDownload(String accessMethod, String digitalObjectID, File destination) throws FileNotFoundException {
    return performDataDownload(accessMethod, digitalObjectID, destination, null);
  }

  /**
   * This method can be used to download the actual digital data onto your local
   * machine using ADALAPI The method uses WebDav protocol to download the data.
   *
   * @param accessMethod The accessmethod is the protocol that will be used to
   * perform download for e.g. WebDav
   * @param digitalObjectID The data identified by the digital object which will
   * be downloaded
   * @param destination The directory on the local machine where the data will
   * be downloaded
   * @param groupId The groupID the digital object belongs to.
   *
   * @return CommandStatus Status of the command (success or failed).
   * @throws FileNotFoundException will be thrown if the destination is not a
   * valid directory
   */
  public static CommandStatus performDataDownload(String accessMethod, String digitalObjectID, File destination, String groupId) throws FileNotFoundException {
    LOGGER.debug("Performing data download for digital object identified by: " + digitalObjectID + " to data destination at path: " + destination.getAbsolutePath());
    LOGGER.debug("User group: " + groupId);

    if (!destination.isDirectory()) {
      LOGGER.error("Invalid directory: " + destination.getAbsolutePath());
      throw new FileNotFoundException(destination.getAbsolutePath());
    }

    CommandStatus commandStatus = new CommandStatus(Status.FAILED);
    LOGGER.debug("Creating download entity in database for digital data: " + digitalObjectID);
    DownloadInformation createdDownload = clientHelper.createDownloadEntity(digitalObjectID, accessMethod);

    if (createdDownload == null) {
      LOGGER.error("ERROR Unable to create download entity for digital data: " + digitalObjectID);
      return commandStatus;
    }

    int status = clientHelper.checkDownloadStatus(createdDownload.getId());

    if (status == DOWNLOAD_STATUS.DOWNLOAD_READY.getId()) {
      DownloadInformation dataToDownload = clientHelper.getDownloadInformation(createdDownload.getId());
      if (dataToDownload != null) {
        LOGGER.debug("Starting download for requested digital data: " + digitalObjectID);
        AbstractFile downloadedContent = clientHelper.performDataDownloadADALAPI(accessMethod, dataToDownload, destination);
        commandStatus.setReturnObject(downloadedContent);
        commandStatus.setStatusCode(Status.SUCCESSFUL);
      }
    }
    return commandStatus;
  }

  /**
   * This method can be used to download the actual digital data onto your local
   * machine using Data Transfer Client The method uses WebDav protocol to
   * download the data.
   *
   * @param accessMethod The accessmethod is the protocol that will be used to
   * perform download for e.g. WebDav
   * @param digitalObjectID The data identified by the digital object which will
   * be downloaded
   * @param localDestination The directory on the local machine where the data
   * will be downloaded
   *
   * @return CommandStatus
   * @throws FileNotFoundException will be thrown if the destination is not a
   * valid directory
   */
  public static CommandStatus performDataDownloadDataTransferClient(String accessMethod, String digitalObjectID, File localDestination) throws FileNotFoundException {
    return performDataDownloadDataTransferClient(accessMethod, digitalObjectID, localDestination, null);
  }

  /**
   * This method can be used to download the actual digital data onto your local
   * machine using Data Transfer Client The method uses WebDav protocol to
   * download the data.
   *
   * @param accessMethod The accessmethod is the protocol that will be used to
   * perform download for e.g. WebDav
   * @param digitalObjectID The data identified by the digital object which will
   * be downloaded
   * @param localDestination The directory on the local machine where the data
   * will be downloaded
   * @param groupId The groupID the digital object belongs to.
   *
   * @return CommandStatus
   * @throws FileNotFoundException will be thrown if the destination is not a
   * valid directory
   */
  public static CommandStatus performDataDownloadDataTransferClient(String accessMethod, String digitalObjectID, File localDestination, String groupId) throws FileNotFoundException {

    LOGGER.debug("Performing data download for digital object identified by: " + digitalObjectID + " to data destination at path: " + localDestination.getAbsolutePath());
    LOGGER.debug("User group: " + groupId);
    if (!localDestination.isDirectory()) {
      LOGGER.error("Invalid directory: " + localDestination.getAbsolutePath());
      throw new FileNotFoundException(localDestination.getAbsolutePath());
    }

    CommandStatus commandStatus = new CommandStatus(Status.FAILED);
    LOGGER.debug("Creating download entity in database for digital data: " + digitalObjectID);
    DownloadInformation createdDownload = clientHelper.createDownloadEntity(digitalObjectID, accessMethod);

    if (createdDownload == null) {
      LOGGER.error("ERROR Unable to create download entity for digital data: " + digitalObjectID);
      return commandStatus;
    }

    int status = clientHelper.checkDownloadStatus(createdDownload.getId());

    if (status == DOWNLOAD_STATUS.DOWNLOAD_READY.getId()) {
      DownloadInformation dataToDownload = clientHelper.getDownloadInformation(createdDownload.getId());
      if (dataToDownload != null) {
        LOGGER.debug("Starting download for requested digital data: " + digitalObjectID);
        AbstractFile downloadedContent = clientHelper.performDataDownloadDataTransferClient(accessMethod, dataToDownload, localDestination);
        commandStatus.setReturnObject(downloadedContent);
        commandStatus.setStatusCode(Status.SUCCESSFUL);
      }
    }
    return commandStatus;
  }

}
