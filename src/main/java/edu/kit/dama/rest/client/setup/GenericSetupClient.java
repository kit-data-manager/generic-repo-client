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
package edu.kit.dama.rest.client.setup;

import edu.kit.dama.rest.client.DataManagerPropertiesImpl;
import edu.kit.dama.rest.client.DataManagerPropertiesHelper;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.jcommander.generic.status.Status;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Relation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;
import edu.kit.dama.rest.basemetadata.types.InvestigationWrapper;
import edu.kit.dama.rest.basemetadata.types.MetadataSchemaWrapper;
import edu.kit.dama.rest.basemetadata.types.OrganizationUnitWrapper;
import edu.kit.dama.rest.basemetadata.types.StudyWrapper;
import edu.kit.dama.rest.basemetadata.types.TaskWrapper;
import edu.kit.dama.rest.basemetadata.types.UserDataWrapper;
import edu.kit.dama.rest.client.AbstractGenericRestClient;
import edu.kit.dama.rest.mdm.base.client.InvestigationBuilder;
import edu.kit.dama.rest.mdm.base.client.MetadataSchemaBuilder;
import edu.kit.dama.rest.mdm.base.client.OrganizationUnitBuilder;
import edu.kit.dama.rest.mdm.base.client.TaskBuilder;
import edu.kit.dama.rest.mdm.base.client.UpdateInvestigation;
import edu.kit.dama.rest.mdm.base.client.UpdateOrganizationUnit;
import edu.kit.dama.rest.mdm.base.client.UpdateStudy;
import edu.kit.dama.util.StdIoUtils;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic class for generating base metadata for repository. All methods
 * require properly configured settings. To validate settings the following code
 * snippet may be used:
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
public class GenericSetupClient extends AbstractGenericRestClient {

  /**
   * Switch on/off verbosing output.
   */
  private static boolean verbose;

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericSetupClient.class);

  /**
   * Placeholder if no suitable id is available. May also imply that a new
   * instance has to be created.
   */
  private static final long NO_ID = -1;

  /**
   * Placeholder for cancel operation.
   */
  private static final long CANCEL_ID = -2;

  /**
   * Output for query data.
   */
  private static final String DEFAULT = "If no input is given the default value printed in square brackets is choosen.";
  /**
   * Definition for agreement.
   */
  private static final String YES = "y".toLowerCase();
  /**
   * Definition for rejection.
   */
  private static final String NO = "n".toLowerCase();
  /**
   * Separator between sections.
   */
  private static final String LINE_SEPARATOR = "-------------------------------------------\n";

  /**
   * Switch on/off verbose output.
   *
   * @param aVerbose the verbose to set
   */
  public static void setVerbose(boolean aVerbose) {
    verbose = aVerbose;
  }

  /**
   * Rest client.
   */
  private BaseMetaDataRestClient bmdrc;
  /**
   * Properties for REST.
   */
  DataManagerPropertiesImpl properties = null;

  /**
   * Output to console.
   */
  private final PrintStream output = System.out;

  /**
   * Constructor. Only for internal usage.
   */
  protected GenericSetupClient() {
    initializeRest();
  }

  /**
   * Initialize Rest settings.
   */
  private void initializeRest() {
    try {
      properties = testDataManagerSettings();

      if (properties != null) {
        // <editor-fold defaultstate="collapsed" desc="Initialize REST">
        SimpleRESTContext context = new SimpleRESTContext(properties.getAccessKey(), properties.getAccessSecret());
        bmdrc = new BaseMetaDataRestClient(properties.getRestUrl() + REST_BASE_META_DATA_PATH, context);
      }
    } catch (IllegalArgumentException ex) {
      LOGGER.error(null, ex);
      returnStatus = new CommandStatus(ex);
    }
    LOGGER.info("ReturnValue (CommandStatus): " + returnStatus);
  }

  // <editor-fold defaultstate="collapsed" desc="Starting points for the jCommander commands.">
  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param pStudy parsed arguments.
   * @param pInteractive query the parameters.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(Study pStudy, boolean pInteractive) {
    GenericSetupClient gac = new GenericSetupClient();

    return gac.createStudy(pStudy, pInteractive);
  }

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param pStudy parsed arguments.
   * @param pInteractive query the parameters.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(UpdateStudy pStudy, boolean pInteractive) {
    GenericSetupClient gac = new GenericSetupClient();

    return gac.updateStudy(pStudy.getStudyId(), pInteractive);
  }

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param pOrganizationUnit parsed arguments.
   * @param pInteractive query the parameters.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(UpdateOrganizationUnit pOrganizationUnit, boolean pInteractive) {
    GenericSetupClient gac = new GenericSetupClient();

    return gac.updateOrganizationUnit(pOrganizationUnit.getOrganizationUnitId(), pInteractive);
  }

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param pInvestigation parsed arguments.
   * @param pInteractive query the parameters.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(UpdateInvestigation pInvestigation, boolean pInteractive) {
    GenericSetupClient gac = new GenericSetupClient();

    return gac.updateInvestigation(pInvestigation.getInvestigationId(), pInteractive);
  }

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param pInvestigation parsed arguments.
   * @param pInteractive query the parameters.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(Investigation pInvestigation, boolean pInteractive) {
    GenericSetupClient gac = new GenericSetupClient();

    return gac.createInvestigation(pInvestigation, pInteractive);
  }

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param pOrganizationUnit parsed arguments.
   * @param pInteractive query the parameters.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(OrganizationUnit pOrganizationUnit, boolean pInteractive) {
    GenericSetupClient gac = new GenericSetupClient();

    return gac.createOrganizationUnit(pOrganizationUnit, pInteractive);
  }

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param pMetadataSchema parsed arguments.
   * @param pInteractive query the parameters.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(MetaDataSchema pMetadataSchema, boolean pInteractive) {
    GenericSetupClient gac = new GenericSetupClient();

    return gac.createMetadataSchema(pMetadataSchema, pInteractive);
  }

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param pTask parsed arguments.
   * @param pInteractive query the parameters.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(Task pTask, boolean pInteractive) {
    GenericSetupClient gac = new GenericSetupClient();

    return gac.createTask(pTask, pInteractive);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="create... (Study, investigation, organization unit, metadata schema, task)">
  /**
   * Create a study.
   *
   * @param pStudy Study to create.
   * @param pInteractive Query the settings on command line.
   * @return Status of the command.
   */
  private CommandStatus createStudy(Study pStudy, boolean pInteractive) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    properties = testDataManagerSettings();
    if (pInteractive) {
      queryStudy(pStudy);
    }
    PrintUtil.printStudy(pStudy, true);
    if (queryYesNoAnswer("Do you want to write the study to the server?", NO)) {
      returnValue = writeStudy(pStudy);
    }
    returnStatus = returnValue;
    return getReturnStatus();
  }

  /**
   * Create an investigation.
   *
   * @param pInvestigation Investigation to create.
   * @param pInteractive Query the settings on command line.
   * @return Status of the command.
   */
  private CommandStatus createInvestigation(Investigation pInvestigation, boolean pInteractive) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    properties = testDataManagerSettings();
    if (pInteractive) {
      returnValue = queryInvestigation(pInvestigation, true);
    }
    PrintUtil.printInvestigation(pInvestigation, true);
    if (queryYesNoAnswer("Do you want to write the investigation to the server?", NO)) {
      Study selectStudy = pInvestigation.getStudy();
      if ((selectStudy == null) || (selectStudy.getStudyId() == null)) {
        selectStudy = selectStudy();
      }
      if (selectStudy != null) {
        returnValue = writeInvestigation(selectStudy.getStudyId(), pInvestigation);
      }
    }

    returnStatus = returnValue;
    return getReturnStatus();
  }

  /**
   * Create a metadata schema.
   *
   * @param pMetadataSchema Metadata schema to create.
   * @param pInteractive Query the settings on command line.
   * @return Status of the command.
   */
  private CommandStatus createMetadataSchema(MetaDataSchema pMetadataSchema, boolean pInteractive) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    properties = testDataManagerSettings();
    if (pInteractive) {
      returnValue = queryMetadataSchema(pMetadataSchema);
    }
    PrintUtil.printMetadataSchema(pMetadataSchema);
    if (queryYesNoAnswer("Do you want to write the metadata schema to the server?", NO)) {
      returnValue = writeMetadataSchema(null, pMetadataSchema);
    }
    returnStatus = returnValue;
    return getReturnStatus();
  }

  /**
   * Create an organization unit.
   *
   * @param pOrganizationUnit Organization unit to create.
   * @param pInteractive Query the settings on command line.
   * @return Status of the command.
   */
  private CommandStatus createOrganizationUnit(OrganizationUnit pOrganizationUnit, boolean pInteractive) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    properties = testDataManagerSettings();
    if (pInteractive) {
      returnValue = queryOrganizationUnit(pOrganizationUnit, false);
    }
    PrintUtil.printOrganizationUnit(pOrganizationUnit);
    if (queryYesNoAnswer("Do you want to write the organization unit to the server?", NO)) {
      returnValue = writeOrganizationUnit(pOrganizationUnit);
    }
    returnStatus = returnValue;
    return getReturnStatus();
  }

  /**
   * Create a task.
   *
   * @param pTask Task to create.
   * @param pInteractive Query the settings on command line.
   * @return Status of the command.
   */
  private CommandStatus createTask(Task pTask, boolean pInteractive) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    properties = testDataManagerSettings();
    if (pInteractive) {
      returnValue = queryTask(pTask);
    }
    PrintUtil.printTask(pTask);
    if (queryYesNoAnswer("Do you want to write the task to the server?", NO)) {
      returnValue = writeTask(pTask);
    }
    returnStatus = returnValue;
    return getReturnStatus();
  }

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc="update ... (Study, investigation, organization unit)">
  /**
   * Update a study.
   *
   * @param pStudyId Study to update.
   * @param pInteractive Query the settings on command line.
   * @return Status of the command.
   */
  private CommandStatus updateStudy(Long pStudyId, boolean pInteractive) {
    CommandStatus returnValue;
    properties = testDataManagerSettings();
    Study study;
    Long studyId = pStudyId;
    if (pInteractive || (pStudyId == null)) {
      study = selectStudy();
      if (study == null) {
        return new CommandStatus(Status.FAILED);
      }
    } else {
      study = bmdrc.getStudyById(studyId, properties.getUserGroup()).getEntities().get(0);
      if (study.getManager() != null) {
        study.setManager(bmdrc.getUserDataById(study.getManager().getUserId(), properties.getUserGroup()).getEntities().get(0));
      }
    }
    returnValue = queryStudy(study);

    PrintUtil.printStudy(study, true);
    if (queryYesNoAnswer("Do you want to update the study?", NO)) {
      writeStudy(study);
    }

    returnStatus = returnValue;
    return getReturnStatus();
  }

  /**
   * Update an investigation.
   *
   * @param pInvestigationId Investigation to update.
   * @param pInteractive Query the settings on command line.
   * @return Status of the command.
   */
  private CommandStatus updateInvestigation(Long pInvestigationId, boolean pInteractive) {
    CommandStatus returnValue;
    properties = testDataManagerSettings();
    Investigation investigation;
    Long investigationId = pInvestigationId;
    if (pInteractive || (pInvestigationId == null)) {
      investigation = selectInvestigation(0l);
      investigationId = investigation.getInvestigationId();
    } else {
      investigation = bmdrc.getInvestigationById(investigationId, properties.getUserGroup()).getEntities().get(0);
    }
    returnValue = queryInvestigation(investigation, true);

    PrintUtil.printInvestigation(investigation, true);
    if (queryYesNoAnswer("Do you want to update the investigation?", NO)) {
      output.println("Update investigation: ");
      bmdrc.updateInvestigation(investigationId, investigation, properties.getUserGroup());
      output.println("Update investigation: " + returnValue.getStatus());
      Set<MetaDataSchema> metaDataSchema = investigation.getMetaDataSchema();
      for (MetaDataSchema item : metaDataSchema) {
        if (item.getMetaDataSchemaUrl() != null) {
          writeMetadataSchema(investigationId, item);
        }
      }
      Set<Participant> participants = investigation.getParticipants();
      for (Participant item : participants) {
        if (item.getParticipantId() == null) {
          writeParticipants(investigationId, item);
        }
      }
    }
    returnStatus = returnValue;
    return getReturnStatus();
  }

  /**
   * Update an investigation.
   *
   * @param pOrganizationUnitId Investigation to update.
   * @param pInteractive Query the settings on command line.
   * @return Status of the command.
   */
  private CommandStatus updateOrganizationUnit(Long pOrganizationUnitId, boolean pInteractive) {
    CommandStatus returnValue;
    properties = testDataManagerSettings();
    OrganizationUnit organization;
    Long organizationId = pOrganizationUnitId;
    if (pInteractive || (pOrganizationUnitId == null)) {
      organization = selectOrganizationUnit();
      if (organization == null) {
        return new CommandStatus(Status.FAILED);
      }
      organizationId = organization.getOrganizationUnitId();
    } else {
      organization = bmdrc.getOrganizationUnitById(organizationId, properties.getUserGroup()).getEntities().get(0);
      if (organization.getManager() != null) {
        organization.setManager(bmdrc.getUserDataById(organization.getManager().getUserId(), properties.getUserGroup()).getEntities().get(0));
      }
    }
    returnValue = queryOrganizationUnit(organization, true);

    PrintUtil.printOrganizationUnit(organization);
    if (queryYesNoAnswer("Do you want to update the organization unit?", NO)) {
      SimpleRESTContext context = new SimpleRESTContext(properties.getAccessKey(), properties.getAccessSecret());
      output.println("Update organization unit: ");
      bmdrc.updateOrganizationUnit(organizationId, organization, properties.getUserGroup(), context);
      output.println("Update organization unit: " + returnValue.getStatus());
    }

    returnStatus = returnValue;
    return getReturnStatus();
  }

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc="query... (Study, investigation, organization unit, metadata schema, task)">
  /**
   * Query values for study.
   *
   * @param pStudy Study holding preset values.
   * @return Status of the command.
   */
  private CommandStatus queryStudy(Study pStudy) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    output.println("Please input the new values for the study\n" + DEFAULT);
    pStudy.setTopic(queryStringWithDefaultValue("Topic", pStudy.getTopic()));
    pStudy.setNote(queryStringWithDefaultValue("Note", pStudy.getNote()));
    pStudy.setLegalNote(queryStringWithDefaultValue("Legal note", pStudy.getLegalNote()));
    pStudy.setStartDate(queryDateWithDefaultValue("Start date", pStudy.getStartDate()));
    pStudy.setEndDate(queryDateWithDefaultValue("End date", pStudy.getEndDate()));
    UserData manager = pStudy.getManager();
    if (manager != null) {
      output.format("Actual manager: %s (%s) - %s", manager.getFullname(), manager.getDistinguishedName(), manager.getEmail());
    }
    if (queryYesNoAnswer("Do you want to select a (new) manager?", NO)) {
      UserData selectUser = selectUser();
      pStudy.setManager(selectUser);

    }
    while (queryYesNoAnswer("Do you want to add a relation to the study?", NO)) {
      // remove all existing relations in beforehand
      // Due to the test for duplicates during adding a relation this has
      // to be done to avoid loading of all relations via rest.
      // It's a ugly workaround but shouldn't matter in most cases.
      pStudy.setOrganizationUnits(new HashSet<Relation>());
      if (!addRelationToStudy(pStudy).getStatus().isSuccess()) {
        break;
      }
    }
    if (queryYesNoAnswer("Do you want to add an investigation to the study?", NO)) {
      addInvestigationToStudy(pStudy);
    }
    return returnValue;
  }

  /**
   * Query values for organization unit.
   *
   * @param pOrganizationUnit Organization unit holding preset values.
   * @param pUpdate For update manager can not be set any more.
   * @return Status of the command.
   */
  private CommandStatus queryOrganizationUnit(OrganizationUnit pOrganizationUnit, boolean pUpdate) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    output.println("Please input the new values for the organization unit\n" + DEFAULT);
    pOrganizationUnit.setOuName(queryStringWithDefaultValue("OU name", pOrganizationUnit.getOuName()));
    pOrganizationUnit.setAddress(queryStringWithDefaultValue("Address", pOrganizationUnit.getAddress()));
    pOrganizationUnit.setCity(queryStringWithDefaultValue("City", pOrganizationUnit.getCity()));
    pOrganizationUnit.setZipCode(queryStringWithDefaultValue("Zipcode", pOrganizationUnit.getZipCode()));
    pOrganizationUnit.setCountry(queryStringWithDefaultValue("Country", pOrganizationUnit.getCountry()));
    pOrganizationUnit.setWebsite(queryStringWithDefaultValue("Website", pOrganizationUnit.getWebsite()));
    UserData ouManager;
    if (!pUpdate) {
      ouManager = pOrganizationUnit.getManager();
      if (ouManager != null) {
        output.format("Actual manager: %s (%s) - %s", ouManager.getFullname(), ouManager.getDistinguishedName(), ouManager.getEmail());
      }
      if (queryYesNoAnswer("Do you want to select a (new) manager?", NO)) {
        UserData selectUser = selectUser();
        pOrganizationUnit.setManager(selectUser);

      }
    }
    return returnValue;
  }

  /**
   * Query values for investigation.
   *
   * @param pInvestigation Investigation holding preset values.
   * @param pRecursive Recursive queries.
   * @return Status of the command.
   */
  private CommandStatus queryInvestigation(Investigation pInvestigation, boolean pRecursive) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    output.println("Please input the new values for the investigation\n" + DEFAULT);
    pInvestigation.setTopic(queryStringWithDefaultValue("Topic", pInvestigation.getTopic()));
    pInvestigation.setNote(queryStringWithDefaultValue("Note", pInvestigation.getNote()));
    pInvestigation.setDescription(queryStringWithDefaultValue("Description", pInvestigation.getDescription()));
    pInvestigation.setStartDate(queryDateWithDefaultValue("Start date", pInvestigation.getStartDate()));
    pInvestigation.setEndDate(queryDateWithDefaultValue("End date", pInvestigation.getEndDate()));
    if (pRecursive) {
      while (queryYesNoAnswer("Would you like to add a metadata schema to investigation?", NO)) {
        if (!addMetadataSchemaToInvestigation(pInvestigation).getStatus().isSuccess()) {
          break;
        }
      }
      while (queryYesNoAnswer("Would you like to add a participant to investigation?", NO)) {
        if (!addParticipantToInvestigation(pInvestigation).getStatus().isSuccess()) {
          break;
        }
      }
    }
    return returnValue;
  }

  /**
   * Query for a new metadata schema.
   *
   * @param pMetadataSchema Instance with preset values.
   * @return
   */
  private CommandStatus queryMetadataSchema(MetaDataSchema pMetadataSchema) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    output.println("Please input the new value for metadata schema");
    pMetadataSchema.setMetaDataSchemaUrl(queryStringWithDefaultValue("Metadata schema URL", pMetadataSchema.getMetaDataSchemaUrl()));
    pMetadataSchema.setSchemaIdentifier(queryStringWithDefaultValue("Metadata schema identifier", pMetadataSchema.getSchemaIdentifier()));

    return returnValue;
  }

  /**
   * Query for a new task.
   *
   * @param pTask Instance with preset values.
   * @return
   */
  private CommandStatus queryTask(Task pTask) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    output.println("Please input a new task");
    pTask.setTask(queryStringWithDefaultValue("Label", pTask.getTask()));

    return returnValue;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="add... (Relation, Participant, MetadataSchema, Investigation)">
  /**
   * Add metadata schema to investigation.
   *
   * @param pInvestigation Investigation which should contain the (new) metadata
   * schema.
   * @return Status of the command.
   */
  private CommandStatus addMetadataSchemaToInvestigation(Investigation pInvestigation) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    // lookup for all defined metadata schemas.
    List<Long> ids = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    // add one item for new metadata schema
    MetadataSchemaWrapper allMetadataSchemas = bmdrc.getAllMetadataSchemas(0, Integer.MAX_VALUE, properties.getUserGroup());
    for (MetaDataSchema item : allMetadataSchemas.getEntities()) {
      MetaDataSchema buffer = bmdrc.getMetadataSchemaById(item.getId(), properties.getUserGroup()).getEntities().get(0);
      ids.add(buffer.getId());
      descriptions.add(buffer.getSchemaIdentifier() + " -> " + buffer.getMetaDataSchemaUrl());
    }
    output.println("Please select a metadata schema!");
    Long selectedId = queryFromList(ids, descriptions, true);
    MetaDataSchema mds = new MetadataSchemaBuilder().build();
    switch (selectedId.intValue()) {
      case (int) NO_ID:
        returnValue = queryMetadataSchema(mds);
        break;
      case (int) CANCEL_ID:
        returnValue = new CommandStatus(Status.FAILED);
        break;
      default:
        // add selected Id to investigation.
        // read id from server
        mds = bmdrc.getMetadataSchemaById(selectedId, properties.getUserGroup()).getEntities().get(0);
    }
    pInvestigation.addMetaDataSchema(mds);
    return returnValue;
  }

  /**
   * Add an investigation to an existing study.
   *
   * @param pStudy Study which should contain the (new) investigation
   * @return command status
   */
  private CommandStatus addInvestigationToStudy(Study pStudy) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    Investigation investigation = new InvestigationBuilder().build();
    CommandStatus status = queryInvestigation(investigation, true);
    if (status.getStatus().isSuccess()) {
      pStudy.addInvestigation(investigation);
    } else {
      returnValue = new CommandStatus(Status.FAILED);
    }
    return returnValue;
  }

  /**
   * Add a participant to an existing investigation.
   *
   * @param pInvestigation Investigation which should contain the participant.
   * @return command status
   */
  private CommandStatus addParticipantToInvestigation(Investigation pInvestigation) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    UserData user = selectUser();
    Task task = selectTask();
    if ((task != null) && (user != null)) {
      Participant participant = new Participant(user, task);
      pInvestigation.addParticipant(participant);
    } else {
      returnValue = new CommandStatus(Status.FAILED);
    }
    return returnValue;
  }

  /**
   * Add a relation to an existing study.
   *
   * @param pStudy Study which should contain the relation.
   * @return command status
   */
  private CommandStatus addRelationToStudy(Study pStudy) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    OrganizationUnit organization = selectOrganizationUnit();
    if (organization != null) {
      Task task = selectTask();
      Relation relation = new Relation(organization, task);
      pStudy.addRelation(relation);
    } else {
      returnValue = new CommandStatus(Status.FAILED);
    }

    return returnValue;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="select... (User, Task, OrganizationUnit)">
  /**
   * Select a study.
   *
   * @return study (null if canceled)
   */
  private Study selectStudy() {
    Study returnValue = null;
    // lookup for all defined metadata schemas.
    List<Long> ids = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    // add one item for new metadata schema
    StudyWrapper studies = bmdrc.getAllStudies(0, Integer.MAX_VALUE, properties.getUserGroup());
    for (Study item : studies.getEntities()) {
      Study buffer = bmdrc.getStudyById(item.getStudyId(), properties.getUserGroup()).getEntities().get(0);
      ids.add(buffer.getStudyId());
      descriptions.add(buffer.getTopic());
    }
    output.println("Please select a study!");
    Long selectedId = queryFromList(ids, descriptions, false);
    switch (selectedId.intValue()) {
      case (int) NO_ID:
      case (int) CANCEL_ID:
        break;
      default:
        // add selected Id to investigation.
        // read id from server
        returnValue = bmdrc.getStudyById(selectedId, properties.getUserGroup()).getEntities().get(0);
    }
    return returnValue;
  }

  /**
   * Select a investigation of a study.
   *
   * @return investigation (null if canceled)
   */
  private Investigation selectInvestigation(Long pStudyId) {
    Investigation returnValue = null;
    // lookup for all defined metadata schemas.
    List<Long> ids = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    // add one item for new metadata schema
    InvestigationWrapper investigations = bmdrc.getAllInvestigations(pStudyId, 0, Integer.MAX_VALUE, properties.getUserGroup());
    for (Investigation item : investigations.getEntities()) {
      Investigation buffer = bmdrc.getInvestigationById(item.getInvestigationId(), properties.getUserGroup()).getEntities().get(0);
      ids.add(buffer.getInvestigationId());
      descriptions.add(buffer.getTopic());
    }
    output.println("Please select an investigation!");
    Long selectedId = queryFromList(ids, descriptions, false);
    switch (selectedId.intValue()) {
      case (int) NO_ID:
      case (int) CANCEL_ID:
        break;
      default:
        // add selected Id to investigation.
        // read id from server
        returnValue = bmdrc.getInvestigationById(selectedId, properties.getUserGroup()).getEntities().get(0);
        PrintUtil.printInvestigation(returnValue, true);
    }
    return returnValue;
  }

  /**
   * Select or create a new organization unit.
   *
   * @return organization unit (null if canceled)
   */
  private OrganizationUnit selectOrganizationUnit() {
    OrganizationUnit returnValue = null;
    // lookup for all defined metadata schemas.
    List<Long> ids = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    // add one item for new metadata schema
    OrganizationUnitWrapper tasks = bmdrc.getAllOrganizationUnits(0, Integer.MAX_VALUE, properties.getUserGroup());
    for (OrganizationUnit item : tasks.getEntities()) {
      OrganizationUnit buffer = bmdrc.getOrganizationUnitById(item.getOrganizationUnitId(), properties.getUserGroup()).getEntities().get(0);
      ids.add(buffer.getOrganizationUnitId());
      descriptions.add(String.format("%s %s (%s)", buffer.getOuName(), buffer.getCity(), buffer.getCountry()));
    }
    output.println("Please select an organization unit!");
    Long selectedId = queryFromList(ids, descriptions, true);
    switch (selectedId.intValue()) {
      case (int) NO_ID:
        OrganizationUnit organization = new OrganizationUnitBuilder().build();
        queryOrganizationUnit(organization, false);
        returnValue = organization;
        break;
      case (int) CANCEL_ID:
        break;
      default:
        // add selected Id to investigation.
        // read id from server
        returnValue = bmdrc.getOrganizationUnitById(selectedId, properties.getUserGroup()).getEntities().get(0);
    }
    return returnValue;
  }

  /**
   * Select or create a new task.
   *
   * @return task (null if canceled)
   */
  private Task selectTask() {
    Task returnValue = null;
    // lookup for all defined metadata schemas.
    List<Long> ids = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    // add one item for new metadata schema
    TaskWrapper tasks = bmdrc.getAllTasks(0, Integer.MAX_VALUE, properties.getUserGroup());
    for (Task item : tasks.getEntities()) {
      Task buffer = bmdrc.getTaskById(item.getTaskId(), properties.getUserGroup()).getEntities().get(0);
      ids.add(buffer.getTaskId());
      descriptions.add(buffer.getTask());
    }
    output.println("Please select a task!");
    Long selectedId = queryFromList(ids, descriptions, true);
    switch (selectedId.intValue()) {
      case (int) NO_ID:
        Task task = new TaskBuilder().build();
        queryTask(task);
        returnValue = task;
        break;
      case (int) CANCEL_ID:
        break;
      default:
        // add selected Id to investigation.
        // read id from server
        returnValue = bmdrc.getTaskById(selectedId, properties.getUserGroup()).getEntities().get(0);
    }
    return returnValue;
  }

  /**
   * Select a user.
   *
   * @return user (null if canceled)
   */
  private UserData selectUser() {
    UserData returnValue = null;
    // lookup for all defined metadata schemas.
    List<Long> ids = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    // add one item for new metadata schema
    UserDataWrapper allUserData = bmdrc.getAllUserData(0, Integer.MAX_VALUE, properties.getUserGroup());
    for (UserData item : allUserData.getEntities()) {
      UserData buffer = bmdrc.getUserDataById(item.getUserId(), properties.getUserGroup()).getEntities().get(0);
      ids.add(buffer.getUserId());
      descriptions.add(buffer.getDistinguishedName() + " -> " + buffer.getEmail());
    }
    output.println("Please select an user!");
    Long selectedId = queryFromList(ids, descriptions, false);
    switch (selectedId.intValue()) {
      case (int) CANCEL_ID:
        break;
      default:
        // add selected Id to investigation.
        // read id from server
        returnValue = bmdrc.getUserDataById(selectedId, properties.getUserGroup()).getEntities().get(0);
    }
    return returnValue;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Write instances">
  /**
   * Write a study to server.
   *
   * @param pStudy Study to write.
   * @return Status of the command.
   */
  private CommandStatus writeStudy(Study pStudy) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    if (pStudy.getStudyId() == null) {
      output.println("Write study: ");
      if (verbose) {
        PrintUtil.printStudy(pStudy, true);
      }
      Study dummy = bmdrc.addStudy(pStudy, properties.getUserGroup()).getEntities().get(0);
      pStudy.setStudyId(dummy.getStudyId());
      output.println("Write study: " + returnValue.getStatus());
    } else {
      // As study already exists do an update.
      output.println("Update study: ");
      bmdrc.updateStudy(pStudy.getStudyId(), pStudy, properties.getUserGroup());
      output.println("Update study: " + returnValue.getStatus());
    }
    // Write investigations. Only new investigations are written to the server.
    Set<Investigation> investigations = pStudy.getInvestigations();
    if (investigations != null) {
      Iterator<Investigation> iterator = investigations.iterator();
      while (iterator.hasNext()) {
        returnValue = writeInvestigation(pStudy.getStudyId(), iterator.next());
      }
    }
    // Write relations. Only new relations are written to server.
    Set<Relation> relations = pStudy.getOrganizationUnits();
    if (relations != null) {
      Iterator<Relation> iterator = relations.iterator();
      while (iterator.hasNext()) {
        returnValue = writeRelation(pStudy.getStudyId(), iterator.next());
      }
    }
    return returnValue;
  }

  /**
   * Write an investigation to server.
   *
   * @param pStudyId Study containing investigation.
   * @param pInvestigation Investigation to write.
   * @return Status of the command.
   */
  private CommandStatus writeInvestigation(Long pStudyId, Investigation pInvestigation) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    if (pInvestigation.getInvestigationId() == null) {
      // investigation is not known to the server yet.
      if (pStudyId == null) {
        output.println("Select study to add investigation into:");
        Study selectStudy = selectStudy();
        if (selectStudy != null) {
          pStudyId = selectStudy.getStudyId();
        }
      }
      if (pStudyId != null) {
        output.println("Add investigation to study: ");
        if (verbose) {
          PrintUtil.printInvestigation(pInvestigation, false);
        }
        Investigation investigation = bmdrc.addInvestigationToStudy(pStudyId, pInvestigation, properties.getUserGroup()).getEntities().get(0);
        pInvestigation.setInvestigationId(investigation.getInvestigationId());
        output.println("Add investigation to study: " + returnValue.getStatus());
      } else {
        returnValue = new CommandStatus(Status.FAILED);
      }
    }
    Set<Participant> participants = pInvestigation.getParticipants();
    if (participants != null) {
      Iterator<Participant> iterator = participants.iterator();
      while (iterator.hasNext()) {
        returnValue = writeParticipants(pInvestigation.getInvestigationId(), iterator.next());
      }
    }
    Set<MetaDataSchema> metadataschema = pInvestigation.getMetaDataSchema();
    if (metadataschema != null) {
      Iterator<MetaDataSchema> iterator_1 = metadataschema.iterator();
      while (iterator_1.hasNext()) {
        returnValue = writeMetadataSchema(pInvestigation.getInvestigationId(), iterator_1.next());
      }
    }
    return returnValue;
  }

  /**
   * Write an relation to server.
   *
   * @param pStudyId Study containing relation.
   * @param pRelation Relation to write.
   * @return Status of the command.
   */
  private CommandStatus writeRelation(Long pStudyId, Relation pRelation) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    if (pRelation.getRelationId() == null) {
      // relation is not known to the server yet.
      if (pStudyId == null) {
        output.println("Select study to add relation into:");
        Study selectStudy = selectStudy();
        if (selectStudy != null) {
          pStudyId = selectStudy.getStudyId();
        }
      }
      if ((pStudyId != null) && (pRelation.getRelationId() == null)) {
        OrganizationUnit organization = pRelation.getOrganizationUnit();
        Task task = pRelation.getTask();
        if (organization != null) {
          if (organization.getOrganizationUnitId() == null) {
            writeOrganizationUnit(organization);
          }
          if (task != null) {
            if (task.getTaskId() == null) {
              writeTask(task);
            }
          }
          output.println("Add relation to study: ");
          if (verbose) {
            PrintUtil.printRelation(pRelation);
          }
          // method return study wrapper which may already contain one or more relations!?
          StudyWrapper addRelationToStudy = bmdrc.addRelationToStudy(pStudyId, pRelation, properties.getUserGroup());
          output.println("New no of Relations: " + addRelationToStudy.getEntities().get(0).getOrganizationUnits().size());
          output.println("Add relation to study: finished");
        } else {
          returnValue = new CommandStatus(Status.FAILED);
        }
      } else {
        returnValue = new CommandStatus(Status.FAILED);
      }
    }
    return returnValue;
  }

  /**
   * Write an participant to server.
   *
   * @param pInvestigationId Investigation containing participant.
   * @param pParticipant Participant to write.
   * @return Status of the command.
   */
  private CommandStatus writeParticipants(Long pInvestigationId, Participant pParticipant) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    if (pParticipant.getParticipantId() == null) {
      // relation is not known to the server yet.
      if (pInvestigationId == null) {
        output.println("Select investigation to add participant into:");
        output.println("First you have to select a study:");
        Long studyId = null;
        Study selectStudy = selectStudy();
        if (selectStudy != null) {
          studyId = selectStudy.getStudyId();
        }
        if (studyId != null) {
          output.println("Now you have to select a study:");
          pInvestigationId = selectInvestigation(studyId).getInvestigationId();
        }
      }
      if (pInvestigationId != null) {
        UserData user = pParticipant.getUser();
        Task task = pParticipant.getTask();
        if (user != null) {
          if (task != null) {
            if (task.getTaskId() == null) {
              writeTask(task);
            }
          }
          output.println("Add participant to investigation: ");
          if (verbose) {
            PrintUtil.printParticipant(pParticipant);
          }
          // method return investigation wrapper which may already contain one or more participants!?
          InvestigationWrapper addParticipantToInvestigation = bmdrc.addParticipantToInvestigation(pInvestigationId, pParticipant, properties.getUserGroup());
          output.println("New no of participants: " + addParticipantToInvestigation.getEntities().get(0).getParticipants().size());
          output.println("Add participant to investigation: " + returnValue.getStatus());
        } else {
          returnValue = new CommandStatus(Status.FAILED);
        }
      } else {
        returnValue = new CommandStatus(Status.FAILED);
      }
    }
    return returnValue;
  }

  /**
   * Write a organization unit to server.
   *
   * @param pOrganizationUnit organization unit to write.
   * @return Status of the command.
   */
  private CommandStatus writeOrganizationUnit(OrganizationUnit pOrganizationUnit) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    if (pOrganizationUnit.getOrganizationUnitId() == null) {
      output.println("Write organization unit: ");
      if (verbose) {
        PrintUtil.printOrganizationUnit(pOrganizationUnit);
      }
      OrganizationUnit oUnit = bmdrc.addOrganizationUnit(pOrganizationUnit, properties.getUserGroup()).getEntities().get(0);
      pOrganizationUnit.setOrganizationUnitId(oUnit.getOrganizationUnitId());
      output.println("Write organization unit: " + returnValue.getStatus());
    }
    return returnValue;
  }

  /**
   * Write a task to server.
   *
   * @param pTask task unit to write.
   * @return Status of the command.
   */
  private CommandStatus writeTask(Task pTask) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    if (pTask.getTaskId() == null) {
      output.println("Write task: ");
      if (verbose) {
        PrintUtil.printTask(pTask);
      }
      Task task = bmdrc.addTask(pTask, properties.getUserGroup()).getEntities().get(0);
      pTask.setTaskId(task.getTaskId());
      output.println("Write task: " + returnValue.getStatus());
    }
    return returnValue;
  }

  /**
   * Write meta data schema to server.
   *
   * @param pInvestigationId id of the investigation.
   * @param pMetadataSchema meta data schema to write.
   * @return Status of the command.
   */
  private CommandStatus writeMetadataSchema(Long pInvestigationId, MetaDataSchema pMetadataSchema) {
    CommandStatus returnValue = new CommandStatus(Status.SUCCESSFUL);
    if (pMetadataSchema.getId() == null) {
      output.println("Write metadata schema: ");
      if (verbose) {
        PrintUtil.printMetadataSchema(pMetadataSchema);
      }
      MetaDataSchema mds = bmdrc.addMetadataSchema(pMetadataSchema, properties.getUserGroup()).getEntities().get(0);
      pMetadataSchema.setId(mds.getId());
      output.println("Write metadata schema: " + returnValue.getStatus());
    }
    if ((pInvestigationId != null) && (pMetadataSchema.getMetaDataSchemaUrl() != null)) {
      output.println("Add metadata schema to investigation: ");
      if (verbose) {
        PrintUtil.printMetadataSchema(pMetadataSchema);
      }
      InvestigationWrapper addMetadataSchemaToInvestigation = bmdrc.addMetadataSchemaToInvestigation(pInvestigationId, pMetadataSchema, properties.getUserGroup());
      output.println("No of new metadata schemas: " + addMetadataSchemaToInvestigation.getEntities().get(0).getMetaDataSchema().size());
      output.println("Add metadata schema to investigation: " + returnValue.getStatus());
    }
    return returnValue;
  }

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc="Helper methods for querying commandline.">
  /**
   * Query for a key. Default value may be available.
   *
   * @param pKey Key of the value.
   * @param pValue Old/default value.
   * @return New value.
   */
  private String queryStringWithDefaultValue(String pKey, String pValue) {
    output.format("%s [%s]:\n", pKey, pValue);
    return StdIoUtils.readStdInput(pValue);

  }

  /**
   * Query for a date with format: yyyy-MM-dd (e.g.: 2015-01-29)
   *
   * @param pKey Key of the value.
   * @param pValue Old/default value.
   * @return New value.
   */
  private Date queryDateWithDefaultValue(String pKey, Date pValue) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    if (pValue == null) {
      pValue = new Date();
    }
    String date = queryStringWithDefaultValue(pKey, sdf.format(pValue));
    Date newDate = null;
    try {
      newDate = sdf.parse(date);
    } catch (ParseException ex) {
      LOGGER.error(null, ex);
    }
    return newDate;
  }

  /**
   * Query for yes or no.
   *
   * @param pQuestion Question
   * @param pDefault YES or NO {@link #YES}, {@link #NO}
   * @return true if yes.
   */
  private boolean queryYesNoAnswer(String pQuestion, String pDefault) {
    String defaultAnswer = pDefault;
    output.print(LINE_SEPARATOR);
    output.format("\n%s (%s/%s)[%s]\n", pQuestion, YES, NO, defaultAnswer);
    String inputValue = StdIoUtils.readStdInput(defaultAnswer);

    return inputValue.toLowerCase().contains(YES);
  }

  @Override
  protected void checkArguments() {
    // do nothing at the moment as arguments already checked by jCommander
  }

  /**
   * If there is a limited number of possibility it has to be chosen from the
   * given list via the appropriate number.
   *
   * @param pProperty actualValue.
   * @param pPossibleValues list of values. NULL if any value is allowed.
   * @param pDescriptions list of descriptions. NULL if no description
   * @param pAllowCreation are new instances allowed or not. available.
   * @return id of the selected instance ({@link #NO_ID} = create new instance,
   * {@link #CANCEL_ID} = cancel operation)
   */
  private Long queryFromList(final List<Long> pPossibleValues, final List<String> pDescriptions, boolean pAllowCreation) {
    Long actualValue = CANCEL_ID;
    String formatString = "%3d: %4d ";
    String description = "- %s ";
    String marker = "*";
    int oldIndex = 1;
    if (pAllowCreation) {
      pPossibleValues.add(NO_ID);
      if (oldIndex <= 0) {
        oldIndex = pPossibleValues.size();
      }
      pDescriptions.add("+++ Create new instance. +++");
    }
    pPossibleValues.add(CANCEL_ID);
    pDescriptions.add("--- Cancel ---");

    int index = 1;
    output.println("Please choose via given index.");
    for (int pVIndex = 0; pVIndex < pPossibleValues.size(); pVIndex++) {
      String adaptedFormatString = formatString;
      if (pDescriptions.get(pVIndex) != null) {
        adaptedFormatString = adaptedFormatString + description;
      }
      if (oldIndex == index) {
        adaptedFormatString = marker + adaptedFormatString + marker;
      }
      output.println(String.format(adaptedFormatString, index++, pPossibleValues.get(pVIndex), pDescriptions.get(pVIndex)));
    }

    index = StdIoUtils.readIntFromStdInput(index, oldIndex) - 1;
    if (index < pPossibleValues.size()) {
      actualValue = pPossibleValues.get(index);
    }

    return actualValue;
  }
  // </editor-fold>

}
