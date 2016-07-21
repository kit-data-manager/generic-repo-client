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
package edu.kit.dama.rest.client.generic.helper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;

import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.jcommander.generic.status.Status;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.dama.rest.admin.client.impl.UserGroupRestClient;
import edu.kit.dama.rest.admin.types.UserGroupWrapper;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.transfer.client.impl.InProcStagingClient;
import edu.kit.dama.transfer.client.interfaces.IStagingCallback;
import edu.kit.dama.transfer.client.interfaces.ITransferTaskListener;
import edu.kit.dama.transfer.client.types.TransferTask;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import edu.kit.dama.rest.basemetadata.types.InvestigationWrapper;
import edu.kit.dama.rest.basemetadata.types.StudyWrapper;
import edu.kit.dama.rest.client.IDataManagerRestUrl;
import edu.kit.dama.rest.staging.client.impl.StagingServiceRESTClient;
import edu.kit.dama.rest.staging.types.DownloadInformationWrapper;
import edu.kit.dama.rest.staging.types.IngestInformationWrapper;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import static edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS.DOWNLOAD_READY;
import static edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS.DOWNLOAD_REMOVED;
import static edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS.PREPARATION_FAILED;
import static edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS.PREPARING;
import static edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS.SCHEDULED;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.exceptions.ContainerInitializationException;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.transfer.client.impl.AbstractTransferClient;

/**
 * This class contains all the atomic operation which are required to interact
 * with the KIT Data Manger
 *
 * @author kb3353
 *
 */
public class RESTClientHelper implements IStagingCallback, ITransferTaskListener, IDataManagerRestUrl {

    /**
     * Logger for logging.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RESTClientHelper.class);

    /**
     * Path separator.
     */
    public static final String PATH_SEPARATOR = "/";
    /**
     * Authentication context for REST services.
     */
    private final SimpleRESTContext context;
    /**
     * URL for admin services.
     */
    private final String userGroupRESTURL;
    /**
     * URL for base metadata services.
     */
    private final String baseMetadataRESTURL;
    /**
     * URL for staging services.
     */
    private final String stagingRESTURL;
    /**
     * Flag for download status.
     */
    private boolean breakRecursion = false;
    /**
     * Status of download.
     */
    private int downloadStatus = -1;
//	private final String dataOrganizationRESTURL;

    /**
     * Constructor.
     *
     * @param context SimpleRESTContext consisting of the OAuth credentials
     * @param baseURL the URL which identifies the location of KIT Data Manager
     */
    public RESTClientHelper(SimpleRESTContext context, String baseURL) {
        this.context = context;
        if (baseURL.endsWith(PATH_SEPARATOR)) {
            baseURL = baseURL.substring(0, baseURL.length() - 1);
        }

        this.userGroupRESTURL = baseURL + REST_USER_GROUP_PATH;
        this.baseMetadataRESTURL = baseURL + REST_BASE_META_DATA_PATH;
        this.stagingRESTURL = baseURL + REST_STAGING_PATH;
//		this.dataOrganizationRESTURL = baseURL + REST_DATA_ORGANIZATION_PATH;
    }

    /*
   * User Group Specific Methods
   * 
     */
    /**
     * This method can be used to get the specific user identified by its
     * distinguished name
     *
     * @param pDistinguishedName The unique name which specifies a single user
     *
     * @return An object of UserData which contains the information about the
     * requested user
     */
    public UserData getSpecificUser(String pDistinguishedName) {
        UserGroupRestClient userGroupClient = new UserGroupRestClient(this.userGroupRESTURL, this.context);
        UserData returnValue = null;
        try {
            UserDataWrapper userCount = userGroupClient.getUserCount(this.context);

            UserDataWrapper users = userGroupClient.getAllUsers(0, userCount.getCount(), this.context);
            for (UserData item : users.getEntities()) {
                UserData detailedUser = userGroupClient.getUserById(item.getUserId(), this.context).getEntities().get(0);
                if (detailedUser.getDistinguishedName().equals(pDistinguishedName)) {
                    returnValue = detailedUser;
                    break;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to get the requested user '" + pDistinguishedName + "'!", ex);
        }
        return returnValue;
    }

    /**
     * The getSpecificGroupID method searches and find the group for the given
     * groupID
     *
     * @param groupID Unique string value which represents a specific group. for
     * e.g. USERS, NANOSCOPY_GROUP, etc. This group is generally associated for
     * each community for e.g. Nanoscopy community, Archeology community, Bess
     * community or ECodicoclogy community
     *
     * @return the group requested by the user
     *
     */
    public final UserGroup getSpecificGroupID(String groupID) {
        UserGroupRestClient userGroupClient = new UserGroupRestClient(this.userGroupRESTURL, this.context);

        UserGroup returnValue = null;
        try {
            UserGroupWrapper groupCount = userGroupClient.getGroupCount(this.context);

            UserGroupWrapper groups = userGroupClient.getAllGroups(0, groupCount.getCount(), this.context);
            for (UserGroup item : groups.getEntities()) {
                UserGroup detailedUser = userGroupClient.getGroupById(item.getId(), this.context).getEntities().get(0);
                if (detailedUser.getGroupId().contains(groupID)) {
                    returnValue = detailedUser;
                    break;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to get the requestd group by GroupID '" + groupID + "'!", ex);
        }
        return returnValue;
    }

    /**
     * The getSpecificUser method searches and retrieves the user requested by
     * its Long format ID
     *
     * @param userID The unique ID which specifies a single user
     * @return An object of UserData which contains the information about the
     * requested user
     */
    public final UserData getSpecificUser(Long userID) {
        UserData returnValue = null;
        try {
            UserGroupRestClient userGroupClient = new UserGroupRestClient(this.userGroupRESTURL, this.context);
            UserDataWrapper userById = userGroupClient.getUserById(userID, this.context);
            if (userById != null) {
                returnValue = userById.getEntities().get(0);
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to get the specified user, identified by userID '" + userID + "'!", ex);
        }
        return returnValue;
    }

    /**
     * The method get the investigation by its investigationID.
     *
     * @param pInvestigationID The unique ID which specifies a single
     * investigation
     * @return An object of Investigation which contains the information about
     * the requested investigation.
     */
    public final Investigation getSpecificInvestigation(Long pInvestigationID) {
        Investigation returnValue = null;
        BaseMetaDataRestClient baseMetaDataClient = new BaseMetaDataRestClient(this.baseMetadataRESTURL, this.context);
        try {
            InvestigationWrapper investigationById = baseMetaDataClient.getInvestigationById(pInvestigationID, null, context);
            returnValue = investigationById.getEntities().get(0);
        } catch (Exception ex) {
            LOGGER.error("Unable to get the specified investigation, identified by investigationID '" + pInvestigationID + "'!", ex);
        }
        return returnValue;
    }

    /*
   * 
   * Methods specific for creating and getting base meta data information in KIT Data Manager
   * 
     */
    /**
     * The method createStudy will create a new study only after checking
     * whether the requested study exists or not. If the Study exists then this
     * existing study will be returned
     *
     * @param groupID The group for which this Study will be created
     * @param defaultStudy the study created for the specific community
     *
     * @return Returns the created Study
     */
    public Study createStudy(String groupID, Study defaultStudy) {

        BaseMetaDataRestClient baseMetaDataClient = new BaseMetaDataRestClient(this.baseMetadataRESTURL, this.context);
        StudyWrapper createdStudy = null;
        Study studyByTopic = null;

        try {
            studyByTopic = getStudyByTopic(groupID, defaultStudy.getTopic());

            if (studyByTopic == null) {
                createdStudy = baseMetaDataClient.addStudy(defaultStudy, groupID, this.context);
                studyByTopic = createdStudy.getEntities().get(0);
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to add the study '" + defaultStudy.getTopic() + "'", ex);
        }
        return studyByTopic;
    }

    /**
     * The getStudyByTopic method can be used to get the Study identified by its
     * topic.
     *
     * @param groupID The group under which the Study will be searched. for e.g
     * Study existing under nanoscopy group will be searched and returned or
     * Study existing under eCodicology group will be searched and returned
     *
     * @param topic The requested topic to check if a study with the similar
     * topic exists or not
     *
     * @return Returns the existing study identified by the topic
     */
    public final Study getStudyByTopic(String groupID, String topic) {
        Study detailedStudy = null;
        try {
            BaseMetaDataRestClient baseMetaDataClient = new BaseMetaDataRestClient(this.baseMetadataRESTURL, this.context);
            StudyWrapper studyCount = baseMetaDataClient.getStudyCount(this.context);
            StudyWrapper studies = baseMetaDataClient.getAllStudies(studyCount.getCount(), 0, groupID, this.context);
            for (Study item : studies.getEntities()) {
                detailedStudy = baseMetaDataClient.getStudyById(item.getStudyId(), groupID, this.context).getEntities().get(0);
                if (detailedStudy.getTopic() != null) {
                    if (detailedStudy.getTopic().equalsIgnoreCase(topic)) {
                        break;
                    } else {
                        detailedStudy = null;
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to get study for the requested topic '" + topic + "'", ex);
        }
        return detailedStudy;
    }

    /**
     * The method creates a new investigation only after checking if an
     * investigation with the same topic does not exists If the investigation
     * exists then the existing investigation will be returned, if not then the
     * newly created investigation
     *
     * @param group The group under which the investigation needs to be created
     * @param study The Study to which this investigation will be added
     * @param investigation The investigation which needs to be created
     *
     * @return The created investigation or the existing investigation
     */
    public Investigation createInvestigation(String group, Study study, Investigation investigation) {

        Investigation createdInvestigation = null;
        InvestigationWrapper allInvestigations;

        BaseMetaDataRestClient baseMetaDataClient = new BaseMetaDataRestClient(this.baseMetadataRESTURL, this.context);
        InvestigationWrapper investigationWrapper = baseMetaDataClient.getInvestigationCount(study.getStudyId(), group, this.context);

        if (investigationWrapper != null) {
            allInvestigations = baseMetaDataClient.getAllInvestigations(study.getStudyId(), 0, investigationWrapper.getCount(), group, this.context);
            if (allInvestigations.getCount() != 0) {
                for (Investigation existingInvestigation : allInvestigations.getEntities()) {
                    InvestigationWrapper investigationById = baseMetaDataClient.getInvestigationById(existingInvestigation.getInvestigationId(), group, this.context);
                    Investigation foundInvestigation = investigationById.getEntities().get(0);

                    if (foundInvestigation.getStudy().getStudyId().equals(study.getStudyId()) && foundInvestigation.getTopic().trim().equals(investigation.getTopic().trim())) {
                        // Log the information that this is an existing investigation and hence digital object will be appended to this investigation
                        createdInvestigation = foundInvestigation;
                        break;
                    } else {
                        InvestigationWrapper addInvestigationToStudy = baseMetaDataClient.addInvestigationToStudy(study.getStudyId(), investigation, group, this.context);
                        createdInvestigation = addInvestigationToStudy.getEntities().get(0);
                    }
                }
            } else {
                InvestigationWrapper addInvestigationToStudy = baseMetaDataClient.addInvestigationToStudy(study.getStudyId(), investigation, group, this.context);
                createdInvestigation = addInvestigationToStudy.getEntities().get(0);
            }
        } else {
            LOGGER.error("The study needs to be created prior to creating any investigation, the following study " + study.getTopic() + " was expected but was not found");
        }

        return createdInvestigation;
    }

    /**
     * This method can be used to create the base metadata for the digtial
     * object.
     *
     * @param group The group under which the digital object will be created
     * @param investigationId The investigationId is the parent for the digital
     * object, under which the digital object will be added
     * @param digitalObject The default digital object which will be added to
     * the investigation
     *
     * @return The created digital object will be returned
     */
    public DigitalObject createNanoscopyDigitalObject(String group, Long investigationId, DigitalObject digitalObject) {
        DigitalObject createdDigitalObject = null;
        try {
            BaseMetaDataRestClient baseMetaDataClient = new BaseMetaDataRestClient(this.baseMetadataRESTURL, this.context);
            createdDigitalObject = baseMetaDataClient.addDigitalObjectToInvestigation(investigationId, digitalObject, group, this.context).getEntities().get(0);
        } catch (Exception ex) {
            LOGGER.error("Unable to add the digital object " + digitalObject.getLabel() + " for the the given investigation id '" + investigationId + "'!", ex);
        }
        return createdDigitalObject;
    }

    /**
     * This method will return the list of the DigitalObjects requested via
     * their digitalObjectIDs
     *
     * @param group The group under which the digital object will be created
     * @param ingestInformation List of all the ingestInformaton containing the
     * digitalObjectID
     *
     * @return List of available DigitalObjects which are for a specified Group
     */
    public List<DigitalObject> getDigitalObjectInformationById(String group, List<IngestInformation> ingestInformation) {
        List<DigitalObject> resultDOList = new ArrayList<DigitalObject>();
        DigitalObjectWrapper digitalObjectByDOI = new DigitalObjectWrapper();

        // try {
        BaseMetaDataRestClient baseMetaDataClient = new BaseMetaDataRestClient(this.baseMetadataRESTURL, this.context);

        for (IngestInformation ingestInfo : ingestInformation) {
            digitalObjectByDOI = baseMetaDataClient.getDigitalObjectByDOI(group, ingestInfo.getDigitalObjectId(), this.context);
            resultDOList.add(digitalObjectByDOI.getEntities().get(0));
        }
        /*   } catch (Exception ex) {
      LOGGER.info("Unable to get the information for digital object indentified by digital object id " + digitalObjectByDOI.getEntities().get(0));
      LOGGER.error("Unable to perform the GET request for getting a digital object by digital object id!", ex);
    }*/
        return resultDOList;
    }

    /*
   * 
   * Methods are for Staging service Ingest, Access and Download
   * 
     */
    /**
     * This method is used to create the ingest entity in the KIT Data Manger.
     * Ingest Entity needs to be created prior to the actual ingest process.
     *
     * @param digitalObjectID The Digital Object ID for which the ingest entity
     * will be created
     * @param accessMethod The access method, i.e.for e.g. Webdav
     * @param groupId The groupId the ingest belongs to.
     *
     * @return The created Ingest entity in form of IngestInformation object is
     * returned
     */
    public IngestInformation createIngestEntity(String digitalObjectID, String accessMethod, String groupId) {
        IngestInformation ingestInformation = null;
        try {
            StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(this.stagingRESTURL, this.context);
            IngestInformationWrapper postNewIngest = stagingClient.createIngest(digitalObjectID, accessMethod, new ArrayList<Long>(), groupId, this.context);
            ingestInformation = postNewIngest.getEntities().get(0);
        } catch (Exception ex) {
            LOGGER.error("Unable to create the ingest entity!", ex);

        }
        return ingestInformation;
    }

    /**
     * This method is used to find the specific ingest information identified by
     * the ingestID.
     *
     * @param ingestID The ingestID with which to get the complete
     * IngestInformation
     *
     * @return The IngestInformation object of the requested ingest
     */
    public IngestInformation getSpecifiedIngestInformation(Long ingestID) {
        IngestInformation ingestInformation = null;
        try {
            StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(this.stagingRESTURL, this.context);
            ingestInformation = stagingClient.getIngestById(ingestID, this.context).getEntities().get(0);
        } catch (Exception ex) {
            LOGGER.error("Unable to get the ingest information requested by ingestID '" + ingestID + "'", ex);
        }
        return ingestInformation;
    }

    /**
     * This method is used to actually ingest/upload the digital data into the
     * KIT Data Manger. The method uses ADALAPI for uploading the data.
     *
     * @param filesToIngest List of files that needs to be ingested in the KIT
     * DM
     * @param ingestInfo The IngestInformation object for which the files will
     * be ingested to KIT DM.
     *
     *
     * @return The finished Ingest Information object is returned, If the ingest
     * failed then the null object will be returned
     */
    public CommandStatus performIngestADALAPI(File filesToIngest, IngestInformation ingestInfo) {
        /*
     * This method requires to some refactoring to check the clinetResponse when the Status for the ingest is requested
     * 
         */
        // the updateIngestStatus is wrong the methods needs to be fixed in the BaseUserClient - updateIngestInformationById
        AbstractFile uploadedFile = null;
        IngestInformation ingestInformation = null;
        CommandStatus commandStatus;

        // Override the default configuration which are set in the xml file with yours
        //		LOGGER.info(configuration.getString("identifier"));
        //		LOGGER.info(configuration.getString("authClass"));
        //		LOGGER.info(configuration.getString("compression"));
        //		LOGGER.info(configuration.getString("bufferSize"));
        updateIngestStatus(ingestInfo.getId(), INGEST_STATUS.PRE_INGEST_RUNNING.getId());

        try {
            //File[] listFiles = filesToIngest.listFiles();
            AbstractFile remoteLocation = new AbstractFile(ingestInfo.getDataFolderUrl());

            //for (File file : listFiles) {
            AbstractFile directoryToUpload = new AbstractFile(filesToIngest);
            uploadedFile = directoryToUpload.upload(remoteLocation);
            if (uploadedFile == null) {
                updateIngestStatus(ingestInfo.getId(), INGEST_STATUS.PRE_INGEST_FAILED.getId());
                commandStatus = new CommandStatus(Status.FAILED, null, ingestInformation);
                // Log the information for the eachFile object as that is the one which failed to upload
                // Throw an exception stating which file failed to upload
                return commandStatus;
            }

        } catch (AdalapiException e) {
            LOGGER.error("The upload via Adalapi WebDAV failed!", e);
            updateIngestStatus(ingestInfo.getId(), INGEST_STATUS.PRE_INGEST_FAILED.getId());
            commandStatus = new CommandStatus(e);
            return commandStatus;
        }

        updateIngestStatus(ingestInfo.getId(), INGEST_STATUS.PRE_INGEST_FINISHED.getId());
        ingestInformation = getSpecifiedIngestInformation(ingestInfo.getId());
        commandStatus = new CommandStatus(Status.SUCCESSFUL, null, ingestInformation);
        return commandStatus;
    }

    /**
     * Method to ingest data using the DataTransferClient. The number of
     * parallel transfer tasks are defined in datamanger.xml file, they can be
     * changed as per your requirement. This file should be placed in the
     * resources folder of your client project
     *
     * For e.g. the nanoscopy command line client, the datamanager.xml has been
     * placed in the src/main/resource folder
     *
     * @param filesToIngest List of files that needs to be ingested in the KIT
     * DM
     * @param ingestInfo The IngestInformation object for which the files will
     * be ingested to KIT DM.
     *
     *
     * @return The finished Ingest Information object is returned, If the ingest
     * failed then the null object will be returned
     */
    public CommandStatus performIngestDataTransferClient(File filesToIngest, IngestInformation ingestInfo) {
        CommandStatus dtcStatus;
        IngestInformation ingestInformation;

        try {
            AbstractFile remoteLocation = new AbstractFile(new URL(ingestInfo.getStagingUrl()));

            // Understanding-
            // The createCompatibleTree maintains the folder structure but is not so intuitive bcos the createCompatibleTree object is used
            // as a reference from the memory which can cause confusion about its usage.
            /*
       IFileTree createCompatibleTree = TransferTaskContainer.createCompatibleTree(ingestInfo);
       TransferTaskContainer.addDataFile(createCompatibleTree, ingestInfo, filesToIngest);
       TransferTaskContainer factoryIngestContainer = TransferTaskContainer.factoryIngestContainer(ingestInfo, createCompatibleTree, "http://ipelsdf2.ipe.kit.edu:9090/KDMCore/rest/staging/StagingService/");
             */
            // Correct and easy way to use the DataTransferClient
            TransferTaskContainer factoryIngestContainer = TransferTaskContainer.factoryIngestContainer(ingestInfo.getId(), stagingRESTURL, this.context.getAccessKey(), this.context.getAccessSecret());
            factoryIngestContainer.addDataFile(filesToIngest);
            factoryIngestContainer.close();

//      DataOrganizationUtils.printTree(factoryIngestContainer.getFileTree().getRootNode(), true);
            updateIngestStatus(ingestInfo.getId(), INGEST_STATUS.PRE_INGEST_RUNNING.getId());

            InProcStagingClient client = new InProcStagingClient(factoryIngestContainer, remoteLocation);
            client.addStagingCallbackListener(this);
            client.addTransferTaskListener(this);
            client.start();

            while (client.isTransferRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error(" Thread interrupted : " + e);
                }
            }
            ingestInformation = getSpecifiedIngestInformation(ingestInfo.getId());
            if (AbstractTransferClient.TRANSFER_STATUS.FAILED.equals(client.getStatus())) {
                updateIngestStatus(ingestInfo.getId(), INGEST_STATUS.PRE_INGEST_FAILED.getId());
                dtcStatus = new CommandStatus(Status.FAILED, null, ingestInformation);
            } else {
                updateIngestStatus(ingestInfo.getId(), INGEST_STATUS.PRE_INGEST_FINISHED.getId());
                dtcStatus = new CommandStatus(Status.SUCCESSFUL, null, ingestInformation);
            }
        } catch (IOException e) {
            LOGGER.error("The upload via Data Transfer Client failed!", e);
            dtcStatus = new CommandStatus(e);
        } catch (ContainerInitializationException e1) {
            LOGGER.error("Unable to initialize the container.", e1);
            dtcStatus = new CommandStatus(e1);
        }

        return dtcStatus;
    }

    /**
     * This method is used to update the status of the ingest entity in the KIT
     * Data Manager
     *
     * @param ingestID The ingestID for which the status will be updated
     * @param statusID The status to which the Ingest needs to be set
     *
     * @return ClientResponse specifying whether the status update was
     * successful or not
     */
    public ClientResponse updateIngestStatus(Long ingestID, int statusID) {
        LOGGER.debug("Updating ingest entity " + ingestID + " to status " + statusID);

        ClientResponse ingestInfoWrapper = null;
        try {
            StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(this.stagingRESTURL, this.context);
            ingestInfoWrapper = stagingClient.updateIngest(ingestID, null, statusID);
        } catch (Exception e) {
            LOGGER.error("Unable to update the ingest status!", e);
        }
        return ingestInfoWrapper;
    }

    /**
     * The method is used to get the total count of the ingest entities from the
     * KIT Data Manager
     *
     * @return The the total count of ingests entities available from the
     * database
     */
    public int getIngestCount() {
        int count = 0;
        try {
            StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(this.stagingRESTURL, this.context);
            IngestInformationWrapper ingestCount = stagingClient.getIngestCount(this.context);
            count = ingestCount.getCount();
        } catch (Exception ex) {
            LOGGER.error("Unable to get the ingest count!", ex);
        }
        return count;
    }

    /**
     * This is method will get all the IngestInformation IDs as per the request
     * Status
     *
     * @param fetchSize this parameter must not be required
     * @param ingestStatus the ingestStatus will be used to filter the returned
     * IngestInformation accordingly. for e.g. IngestStatus 128 is PRE_INGEST
     * finalized state and all the Ingests with status 128 will be returned
     *
     * @return The list of all IngestInformation according to the ingestStatus
     */
    public List<IngestInformation> getIngestInformationIDs(int fetchSize, int ingestStatus) {
        StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(this.stagingRESTURL, this.context);
        List<IngestInformation> finalizedIngests = new ArrayList<>();
        //Get Ingest count for querying the next rest call

        try {
            if (fetchSize > 0) {
                IngestInformationWrapper allIngestInformation = stagingClient.getAllIngestInformation(null, null, ingestStatus, 0, fetchSize, this.context);
                for (IngestInformation ingestInfo : allIngestInformation.getEntities()) {
                    IngestInformationWrapper ingestById = stagingClient.getIngestById(ingestInfo.getId(), this.context);
                    finalizedIngests.add(ingestById.getEntities().get(0));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to get the ingest information.", ex);
        }
        return finalizedIngests;
    }

    /**
     * This methods will request for the complete details of the
     * IngestInformation using the IDs received from the
     * getIngestInformationIDs() methods
     *
     * @param listEntries List of IngestInformation, containing the
     * IDs(primarykey)
     *
     * @return List of IngestInformation Objects
     */
    public List<IngestInformation> getIngestInformation(List<IngestInformation> listEntries) {
        StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(this.stagingRESTURL, this.context);
        List<IngestInformation> resultIngestList = new ArrayList<>();

        for (IngestInformation ingestInformation : listEntries) {
            IngestInformationWrapper ingestInfo = stagingClient.getIngestById(ingestInformation.getId(), this.context);
            resultIngestList.add(ingestInfo.getEntities().get(0));
        }
        return resultIngestList;
    }

    /**
     * This method is used to get the download information from the KIT Data
     * Manager, for the digital data you wish to download.
     *
     * @param downloadID The long downloadID associated with the download entity
     *
     * @return DownloadInformation for the digital data that needs to be
     * downloaded
     */
    public DownloadInformation getDownloadInformation(Long downloadID) {
        DownloadInformation downloadInformation = null;
        try {
            StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(this.stagingRESTURL, this.context);
            DownloadInformationWrapper downloadById = stagingClient.getDownloadById(downloadID, this.context);
            downloadInformation = downloadById.getEntities().get(0);
        } catch (Exception ex) {
            LOGGER.error("Unable to get download information for requested donwload ID '" + downloadID + "'", ex);
        }
        return downloadInformation;
    }

    /**
     * This method is used to create the download entity in the KIT Data
     * Manager, so that the requested digital data can be downloaded
     *
     * @param digitalObjectID The digitalObject for which the download entity
     * will be created
     *
     * @param accessMethod The access method with which to download the data,
     * i.e.for e.g. Webdav
     * @return DownloadInformation for the digital data that needs to be
     * downloaded
     */
    public DownloadInformation createDownloadEntity(String digitalObjectID, String accessMethod) {
        DownloadInformation downloadInformation = null;
        try {
            StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(this.stagingRESTURL, this.context);
            DownloadInformationWrapper createdDownload = stagingClient.createDownload(digitalObjectID, accessMethod, this.context);
            downloadInformation = createdDownload.getEntities().get(0);
        } catch (Exception ex) {
            LOGGER.error("Unable to create download entity for the given digital object id '" + digitalObjectID + "'.", ex);
        }

        return downloadInformation;
    }

    /**
     * This method is used to get the IngestInformation identified by the
     * digital object ID from the KIT Data Manager
     *
     * @param digitalObjectID The digital object for which you wish to get the
     * ingest information
     *
     * @return IngestInformation object containing the information for the
     * requested digitalObjectID
     */
    public IngestInformation getIngestInformationByDOI(String digitalObjectID) {
        IngestInformation ingestInformation = null;
        try {
            StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(this.stagingRESTURL, this.context);
            IngestInformationWrapper ingestInfoID = stagingClient.getAllIngestInformation(null, digitalObjectID, 0, 0, 0, this.context);
            IngestInformationWrapper informationWrapper = stagingClient.getIngestById(ingestInfoID.getEntities().get(0).getId());
            ingestInformation = informationWrapper.getEntities().get(0);
        } catch (Exception ex) {
            LOGGER.error("Unable to get ingest information using the digital object id '" + digitalObjectID + "'", ex);
        }
        return ingestInformation;
    }

    /*
   * Methods to perform the actual data transfer i.e. Ingest or Download
   * and the methods uses ADALAPI library to perform these tasks.
   * 
     */
    /**
     * The performDataDownload_ADALAPI method is responsible for downloading the
     * data from the remote repository to your local machine. Currently it uses
     * the Webdav protocol. In future it will support all protocols
     *
     * @param accessMethod The protocol to be used to perform the download from
     * remote repository. for e.g. Webdav
     * @param dataToDownload The downloadinformation specifying the actual
     * digital data that you wish to download
     * @param destination The destination to which the data will be downloaded.
     * for e.g. An existing folder on your local machine. C:\mydownloadeddata\
     * @return the abstract file object which was downloaded to the local
     * machine
     */
    public AbstractFile performDataDownloadADALAPI(String accessMethod, DownloadInformation dataToDownload, File destination) {

        File localDestination = destination;
        AbstractFile downloadedContent = null;

        try {
            AbstractFile downloadFrom = new AbstractFile(dataToDownload.getDataFolderUrl());
            AbstractFile downloadTo = new AbstractFile(localDestination);
            downloadedContent = downloadFrom.downloadDirectory(downloadTo);
            return downloadedContent;
        } catch (AdalapiException e) {
            LOGGER.error("Download via WebDAV failed!", e);
        }
        return downloadedContent;
    }

    /**
     * Method to download data using the DataTransferClient. The number of
     * parallel transfer tasks are defined in datamanger.xml file, they can be
     * changed as per your requirement. This file should be placed in the
     * resources folder of your client project
     *
     * For e.g. the nanoscopy command line client, the datamanager.xml has been
     * placed in the src/main/resource folder
     *
     *
     * @param accessMethod The protocol to be used to perform the download from
     * remote repository. for e.g. Webdav
     * @param dataToDownload The downloadinformation specifying the actual
     * digital data that you wish to download
     * @param localDestination The destination to which the data will be
     * downloaded. for e.g. An existing folder on your local machine.
     * C:\mydownloadeddata\
     *
     * @return AbstractFile containing the location of data where it is
     * downloaded
     */
    public AbstractFile performDataDownloadDataTransferClient(String accessMethod, DownloadInformation dataToDownload, File localDestination) {

        IFileTree pFileTree;
        AbstractFile downloadTo = new AbstractFile(localDestination);

        try {
            AbstractFile downloadFrom = new AbstractFile(new URL(dataToDownload.getStagingUrl()));
            pFileTree = DataOrganizationUtils.createTreeFromFile(dataToDownload.getDigitalObjectId(), downloadFrom, false);
            TransferTaskContainer factoryIngestContainer = TransferTaskContainer.factoryDownloadContainer(dataToDownload.getId(), pFileTree, stagingRESTURL);
            InProcStagingClient downloadClient = new InProcStagingClient(factoryIngestContainer, downloadTo);

            factoryIngestContainer.setTransferInformation(dataToDownload);
            downloadClient.addStagingCallbackListener(this);
            downloadClient.initializeTransfer();
            downloadClient.start();

            while (downloadClient.isTransferRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    LOGGER.error("Thread interrupted.", ie);
                }
            }
        } catch (IOException e) {
            LOGGER.error("The data download was not successful", e);
        }
        return downloadTo;
    }

    /**
     * Check download status. Wait 5 seconds if status is 'scheduled' and check
     * again!? (but only once) In case of 'preparing' wait 5 seconds and check
     * again!? (but only once)
     *
     * @param downloadID id of the download.
     * @return status as int
     */
    public int checkDownloadStatus(Long downloadID) {

        if (breakRecursion == false) {
            DownloadInformation downloadInformation = getDownloadInformation(downloadID);

            switch (DOWNLOAD_STATUS.idToStatus(downloadInformation.getStatus())) {

                case SCHEDULED:
                    try {
                        Thread.sleep(5000);
                        downloadStatus = checkDownloadStatus(downloadInformation.getId());
                    } catch (InterruptedException e) {
                        // Log thread interrupted exception
                    }
                    break;

                case DOWNLOAD_READY:
                    downloadStatus = downloadInformation.getStatus();
                    breakRecursion = true;
                    break;

                case DOWNLOAD_REMOVED:
                    // Print info that the download is already removed
                    break;

                case PREPARATION_FAILED:
                    // Print error on systemout stating the prepartion of download failed
                    break;

                case PREPARING:
                    try {
                        Thread.sleep(5000);
                        checkDownloadStatus(downloadInformation.getId());
                    } catch (InterruptedException e) {
                        // Log thread interrupted exception
                    }
                    break;

                case UNKNOWN:
                    //Print error on systemout stating the download is unknown
                    break;

                default:
                    // Invalid state exit
                    break;
            }
        }
        return downloadStatus;
    }

    /**
     * Staging started.
     *
     * @param pTransferId Transfer ID of transfer.
     */
    @Override
    public void stagingStarted(String pTransferId) {
        LOGGER.info("Started " + pTransferId);

    }

    @Override
    public void stagingRunning(String pTransferId) {
        LOGGER.info("Running " + pTransferId + "(" + hashCode());
    }

    @Override
    public void stagingFinished(String pTransferId, boolean pSuccess) {
        LOGGER.info("Finished " + pTransferId + ((pSuccess) ? " successful." : " with errors."));

    }

    @Override
    public void transferFailed(TransferTask task) {
        LOGGER.error("File transfer Failed: " + task.getTargetFile().getFile());

    }

    @Override
    public void transferFinished(TransferTask task) {
        LOGGER.info("File transfer finished: " + task.getTargetFile().getFile());

    }

    @Override
    public void transferStarted(TransferTask task) {
        LOGGER.trace("File under transfer: " + task.getTargetFile().getFile() + " task id " + task.getId());

    }

}
