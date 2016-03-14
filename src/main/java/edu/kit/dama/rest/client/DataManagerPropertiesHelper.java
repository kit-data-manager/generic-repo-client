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
package edu.kit.dama.rest.client;

import edu.kit.dama.client.exception.InvalidDataManagerPropertiesException;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.jcommander.generic.status.Status;
import edu.kit.dama.util.StdIoUtils;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.util.ProtocolSettings;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.admin.client.impl.UserGroupRestClient;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.rest.admin.types.UserGroupWrapper;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.staging.client.impl.StagingServiceRESTClient;
import edu.kit.dama.cmdline.generic.parameter.InitParameters;
import edu.kit.dama.cmdline.generic.parameter.InitParameters.CommandLineFlags;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.rest.base.exceptions.DeserializationException;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;
import edu.kit.dama.rest.basemetadata.types.InvestigationWrapper;
import edu.kit.dama.rest.basemetadata.types.StudyWrapper;
import edu.kit.dama.rest.client.setup.GenericSetupClient;
import edu.kit.dama.rest.mdm.base.client.InvestigationBuilder;
import edu.kit.dama.rest.mdm.base.client.StudyBuilder;
import edu.kit.dama.rest.staging.types.StagingAccessPointConfigurationWrapper;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for querying and/or testing the settings accessing the KIT Data
 * Manager.
 *
 * @author hartmann-v
 */
public class DataManagerPropertiesHelper implements IDataManagerRestUrl {

  // <editor-fold defaultstate="collapsed" desc="variable declarations">
  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DataManagerPropertiesHelper.class);

  /**
   * Placeholder if no suitable id is available.
   */
  private static final long NO_ID = -1;

  /**
   * Output if there is any error accessing root directory of webDav server.
   */
  private static final String invalidWebDavSettings = "Please check your settings belonging to webDAV(accesspoint, username, password)!";
  /**
   * Definition for agreement.
   */
  private static final String YES = "y".toLowerCase();
  /**
   * Definition for rejection.
   */
  private static final String NO = "n".toLowerCase();
  /**
   * Output stream for console.
   */
  private static final PrintStream output = System.out;
  /**
   * Error stream for console.
   */
  private static final PrintStream error = System.err;
  /**
   * Base URL of the REST server.
   */
  private static String restServerUrl = "https://dama.lsdf.kit.edu/KITDM";
  /**
   * Holding all properties needed for ingest and download of digital data
   * objects.
   */
  private DataManagerPropertiesImpl properties;
  /**
   * Only test settings. No queries were triggered!.
   */
  private boolean testSettings = false;

  /**
   * Boolean for 'querying' all ingest properties.
   */
  private boolean queryAllProperties = false;
  /**
   * Flags for query. Which categories should be queried.
   */
  private EnumSet<CommandLineFlags> clFlags = InitParameters.getDefaultFlags();
  /**
   * REST client for base metadata.
   */
  private BaseMetaDataRestClient baseMetaDataClient = null;
  /**
   * REST client for user management.
   */
  private UserGroupRestClient userGroupClient = null;
  /**
   * Instance for security context.
   */
  private SimpleRESTContext context;
  // </editor-fold>

  /**
   * Constructor with given security context.
   *
   * @param pProperties All properties.
   */
  public DataManagerPropertiesHelper(DataManagerPropertiesImpl pProperties) {
    setProperties(pProperties);
  }

  /**
   * Set the url of the REST server.
   *
   * @param pRestServerUrl the restServerUrl to set
   */
  public void setRestServerUrl(String pRestServerUrl) {
    restServerUrl = pRestServerUrl;
    initClients();
  }

  /**
   * Initialize the REST clients.
   */
  private void initClients() {
    baseMetaDataClient = new BaseMetaDataRestClient(restServerUrl + REST_BASE_META_DATA_PATH, context);
    userGroupClient = new UserGroupRestClient(restServerUrl + REST_USER_GROUP_PATH, context);
  }

  /**
   * Set the properties for accessing KIT DM.
   *
   * @param pProperties the restServerUrl to set
   */
  private void setProperties(DataManagerPropertiesImpl pProperties) {
    properties = pProperties;
    context = new SimpleRESTContext(pProperties.getAccessKey(), pProperties.getAccessSecret());
    setRestServerUrl(properties.getRestUrl());
  }

  /**
   * Get the user id by its distinguished name.
   *
   * @param pDistinguishedName distinguished name or at least part of it. The
   * label should be unique otherwise the first user matching the label will be
   * returned.
   *
   * @return id of the user.
   */
  public final Long getUserIdByDistinguishedName(String pDistinguishedName) {
    Long returnValue = NO_ID;
    UserDataWrapper groupCount = userGroupClient.getUserCount(context);
    //@TODO Change this...its a mess from the performance point of view
    UserDataWrapper groups = userGroupClient.getAllUsers(0, groupCount.getCount(), context);
    for (UserData item : groups.getEntities()) {
      UserData detailedUser = userGroupClient.getUserById(item.getUserId(), context).getEntities().get(0);
      if (detailedUser.getDistinguishedName().contains(pDistinguishedName)) {
        returnValue = detailedUser.getUserId();
        break;
      }
    }
    return returnValue;
  }

  /**
   * Get the investigation id by its label for given study. If no study is set,
   * all investigation will be returned.
   *
   * @param pGroupId current groupId of the user. For authorization purposes
   * only.
   * @param pStudyId set study.
   * @param pLabel label of the investigation. The label should be unique
   * otherwise the first investigation matching the label will be returned.
   *
   * @return id of the investigation.
   */
  public final long getInvestigationIdByLabel(String pGroupId, Long pStudyId, String pLabel) {
    long returnValue = NO_ID;
    InvestigationWrapper investigationCount = baseMetaDataClient.getInvestigationCount(pStudyId, pGroupId, context);
    InvestigationWrapper investigations = baseMetaDataClient.getAllInvestigations(pStudyId, 0, investigationCount.getCount(), pGroupId, context);
    for (Investigation item : investigations.getEntities()) {
      Investigation detailedInvestigation = baseMetaDataClient.getInvestigationById(item.getInvestigationId(), pGroupId, context).getEntities().get(0);
      if (detailedInvestigation.getTopic().equals(pLabel)) {
        returnValue = detailedInvestigation.getInvestigationId();
        break;
      }
    }
    return returnValue;
  }

  /**
   * Test settings for proper values.
   *
   * @param properties actual properties
   * @return success.
   */
  public static boolean testRestSettings(DataManagerPropertiesImpl properties) {
    DataManagerPropertiesHelper dmph = new DataManagerPropertiesHelper(properties);
    dmph.queryAllProperties = false;
    dmph.testSettings = true;
    EnumSet<CommandLineFlags> flags = EnumSet.noneOf(CommandLineFlags.class);
    flags.add(CommandLineFlags.DATA_MANAGER_BASE);
    flags.add(CommandLineFlags.REST_AUTHENTICATION);

    dmph.clFlags = flags;
    return dmph.testSettings();
  }

  /**
   * Test settings for proper values. Only tests will be executed.
   *
   * @param pProperties actual properties
   * @param pFlags Contains the flags for the query/test.
   * @return success.
   */
  public static boolean testSettings(DataManagerPropertiesImpl pProperties, EnumSet<CommandLineFlags> pFlags) {
    DataManagerPropertiesHelper dmph = new DataManagerPropertiesHelper(pProperties);
    dmph.queryAllProperties = false;
    dmph.testSettings = true;
    dmph.clFlags = pFlags;
    return dmph.testSettings();
  }

  /**
   * Query and test settings for proper values. All settings will be queried.
   *
   * @param pProperties actual properties
   * @param pFlags Contains the flags for the query/test.
   * @return success.
   */
  public static boolean querySettings(DataManagerPropertiesImpl pProperties, EnumSet<CommandLineFlags> pFlags) {
    DataManagerPropertiesHelper dmph = new DataManagerPropertiesHelper(pProperties);
    dmph.queryAllProperties = true;
    dmph.testSettings = false;
    dmph.clFlags = pFlags;
    return dmph.testSettings();
  }

  /**
   * Test settings for proper values. If there are any suspicious settings new
   * settings will be read in via command line.
   *
   * @return success.
   */
  private boolean testSettings() {
    boolean success = false;
    boolean propertiesChanged = false;

    try {
      if (clFlags.contains(CommandLineFlags.DATA_MANAGER_BASE)) {
        propertiesChanged |= testRestUrl();
      } else {
        test4Rest();
      }
      if (clFlags.contains(CommandLineFlags.REST_AUTHENTICATION)) {
        propertiesChanged |= testRestAuthentication();
      }
      if (clFlags.contains(CommandLineFlags.DATA_MANAGER_CONTEXT)) {
        test4RestAuthentication();
        propertiesChanged |= testDataManagerContext();
      }
      if (clFlags.contains(CommandLineFlags.ACCESSPOINT)) {
        propertiesChanged |= testAccessPoint();
      }
      if (clFlags.contains(CommandLineFlags.WEBDAV)) {
        propertiesChanged |= testWebDavAuthentication();
      }

      // <editor-fold defaultstate="collapsed" desc="Ask for save settings if there are any changes.">
      if (propertiesChanged) {
        output.println(StdIoUtils.separator);
        output.println("New settings:");
        output.println("Given REST URL: " + properties.getRestUrl());
        output.println("Chosen accessPoint: " + properties.getAccessPoint());
        output.println("Given accessKey: " + properties.getAccessKey());
        output.println("Given accessSecret: " + properties.getAccessSecret());
        output.println("Given user id: " + properties.getUserId());
        output.println("Chosen user group: " + properties.getUserGroup());
//      output.println("Chosen study: " + properties.getStudy());
        output.println("Chosen investigation: " + properties.getInvestigation());
        output.println("Given username (webDAV): " + properties.getUserName());
        output.println("Given password (webDAV): " + properties.getPassword());
        output.println(StdIoUtils.separator);
        String defaultAnswer = NO;
        output.format("\nSave settings? (%s/%s)[%s]\n", YES, NO, defaultAnswer);
        String inputValue = StdIoUtils.readStdInput(defaultAnswer);

        if (inputValue.toLowerCase().contains(YES)) {
          properties.saveProperties();
          output.println("Settings saved successfully!");
        }
      }
      // </editor-fold>
      success = true;
    } catch (InvalidDataManagerPropertiesException idmpe) {
      LOGGER.error("KIT Data Manager settings seems to be invalid!", idmpe);
    }

    return success;
  }

  /**
   * Test accesspoint settings of KIT Data Manager. If 'testSettings' is true no
   * query will be triggered.
   *
   * @return Are there any changes?
   * @throws InvalidDataManagerPropertiesException Setting of a property seems
   * to be invalid or KIT Data Manager is not well configured.
   */
  private boolean testAccessPoint() throws InvalidDataManagerPropertiesException {
    boolean propertiesChanged = false;

    // <editor-fold defaultstate="collapsed" desc="Select access point">
    StagingAccessPointConfigurationWrapper allAccessPoints;
    StagingServiceRESTClient ssrc;
    ssrc = new StagingServiceRESTClient(restServerUrl + REST_STAGING_PATH, context);
    allAccessPoints = ssrc.getAllAccessPoints(properties.getUserGroup(), null);
    // Test for proper initialized KIT Data Manager
    if (allAccessPoints.getCount() == 0) {
      String noAccessPointDefined = "There should at least one access point defined!\nPlease initialize KIT Data Manager first!";
      error.println(noAccessPointDefined);
      LOGGER.error(noAccessPointDefined);
      throw new InvalidDataManagerPropertiesException(noAccessPointDefined);
    }
    ArrayList<String> accessPointList = new ArrayList<>();
    ArrayList<String> accessPointDescriptionList = new ArrayList<>();
    for (StagingAccessPointConfiguration item : allAccessPoints.getEntities()) {
      StagingAccessPointConfiguration accessPoint = ssrc.getAccessPointById(item.getId(), context).getEntities().get(0);
      accessPointList.add(accessPoint.getUniqueIdentifier());
      accessPointDescriptionList.add(String.format("%s [group: %s]", accessPoint.getDescription(), accessPoint.getGroupId()));
    }
    if (testSettings) {
      if (!accessPointList.contains(properties.getAccessPoint())) {
        String invalidAccessPoint = properties.getAccessPoint() + " -> No valid access point!";
        LOGGER.error(invalidAccessPoint);
        error.println(invalidAccessPoint);
        throw new InvalidDataManagerPropertiesException(properties, DataManagerProperties.ACCESS_POINT_LABEL);

      }
    }
    // If 'accessPoint' is invalid or all properties should be queried -> query accesspoint
    if ((!accessPointList.contains(properties.getAccessPoint())) || (properties.getAccessPoint() == null) || queryAllProperties) {
      propertiesChanged |= properties.readPropertyIndex(DataManagerProperties.ACCESS_POINT_LABEL,
              accessPointList.toArray(new String[accessPointList.size()]),
              accessPointDescriptionList.toArray(new String[accessPointDescriptionList.size()]),
              "Chosen accessPoint: '%s' - %s");
    }
    // </editor-fold>
    return propertiesChanged;
  }

  /**
   * Test base settings of KIT Data Manager. (REST URL) Test should be executed
   * if flag for rest url isn't set.
   *
   * @throws InvalidDataManagerPropertiesException Setting of a property seems
   * to be invalid or KIT Data Manager is not well configured.
   */
  private void test4Rest() throws InvalidDataManagerPropertiesException {
    boolean test = testSettings;
    testSettings = true;
    testRestUrl();
    testSettings = test;
  }

  /**
   * Test REST authentication settings of KIT Data Manager. (accesskey,
   * accesssecret) Test should be executed if flag for rest url isn't set.
   *
   * @throws InvalidDataManagerPropertiesException Setting of a property seems
   * to be invalid or KIT Data Manager is not well configured.
   */
  private void test4RestAuthentication() throws InvalidDataManagerPropertiesException {
    boolean test = testSettings;
    boolean queryAll = queryAllProperties;
    if (!clFlags.contains(CommandLineFlags.REST_AUTHENTICATION)) {
      testSettings = true;
      queryAllProperties = false;
      testRestAuthentication();
      testSettings = test;
      queryAllProperties = queryAll;
    }
  }

  /**
   * Test base settings of KIT Data Manager. (REST URL) If 'testSettings' is
   * true no query will be triggered.
   *
   * @return Are there any changes?
   * @throws InvalidDataManagerPropertiesException Setting of a property seems
   * to be invalid or KIT Data Manager is not well configured.
   */
  private boolean testRestUrl() throws InvalidDataManagerPropertiesException {
    boolean success = false;
    boolean propertiesChanged = false;

    // <editor-fold defaultstate="collapsed" desc="Select REST URL">
    do {
      if (!testSettings) {
        propertiesChanged = properties.readProperty(DataManagerProperties.REST_SERVER_LABEL, "Given URL for the REST server: '%s'");
        if (propertiesChanged) {
          setRestServerUrl(properties.getRestUrl());
        }
      }

      // </editor-fold>
      try {
        success = userGroupClient.checkService();
        if (!success) {
          throw new Exception("No valid URL");
        }
      } catch (Exception e) {
        String noValidUrl = restServerUrl + " -> No valid URL?\nPlease input a valid URL!";
        LOGGER.error(noValidUrl, e);
        error.println(noValidUrl);
        if (testSettings) {
          throw new InvalidDataManagerPropertiesException(properties, DataManagerProperties.REST_SERVER_LABEL);
        }
      }
    } while (!success);

    return propertiesChanged;
  }

  /**
   * Test REST OAuth settings of KIT Data Manager. (accessKey, accessSecret) If
   * 'testSettings' is true no query will be triggered.
   *
   * @return Are there any changes?
   * @throws InvalidDataManagerPropertiesException Setting of a property seems
   * to be invalid or KIT Data Manager is not well configured.
   */
  private boolean testRestAuthentication() throws InvalidDataManagerPropertiesException {
    boolean propertiesChanged = false;
    int noOfGroups = 0;

    // <editor-fold defaultstate="collapsed" desc="Test credentials">
    SimpleRESTContext newContext;
    if (context != null) {
      newContext = context;
    } else {
      newContext = new SimpleRESTContext("accessKey", "accessSecret");
    }

    boolean validCredentials = false;
    DataManagerProperties[] items = new DataManagerProperties[2];
    items[0] = DataManagerProperties.ACCESS_KEY;
    items[1] = DataManagerProperties.ACCESS_SECRET;
    if (queryAllProperties) {
      propertiesChanged |= properties.readProperty(items, "Given accessKey/accessSecret: %s / %s");
      newContext = new SimpleRESTContext(properties.getAccessKey(), properties.getAccessSecret());
    }

    while (noOfGroups < 1) {
      try {
        noOfGroups = userGroupClient.getGroupCount(newContext).getCount();
        if (noOfGroups == 0) {
          //invalid database content...at least one group is expected
          throw new IllegalStateException("Test query for group count returned 0. Each KIT Data Manager instance should have at least one default group 'USERS' configured. "
                  + "Please check your repository system instance for any misconfiguration.");
        }
        validCredentials = true;
      } catch (Exception ex) {
        String invalidCredentials = String.format("Credentials seems to be invalid! (%s / %s)",
                properties.getAccessKey(),
                properties.getAccessSecret());
        LOGGER.error(invalidCredentials);
        error.println(invalidCredentials);
        validCredentials = false;
        if (testSettings) {
          error.println("Input correct REST credentials by using the '-r' flag.");
          throw new InvalidDataManagerPropertiesException(invalidCredentials);
        }
        //ask the user for new credentials
        propertiesChanged |= properties.readProperty(items, "Given accessKey/accessSecret: %s / %s");
        newContext = new SimpleRESTContext(properties.getAccessKey(), properties.getAccessSecret());
        LOGGER.info("Checking again using context {}", newContext);
      }
    }

    if (validCredentials && !testSettings) {
      output.println("REST credentials are valid!");
    }
    if (context != null) {
      context.setAccessKey(newContext.getAccessKey());
      context.setAccessSecret(newContext.getAccessSecret());
    } else {
      context = newContext;
    }
    // </editor-fold>

//    // <editor-fold defaultstate="collapsed" desc="Set user id">
    boolean queryUserId = false;
    UserDataWrapper user = userGroupClient.getUserById(-1); // Get actual user
    String distName = user.getEntities().get(0).getDistinguishedName();
    if ((properties.getUserId() != null) && (!properties.getUserId().equals(distName))) {
      output.println(String.format("Given user id: '%s' but should be '%s'!", properties.getUserId(), distName));
      queryUserId = true;
    }
    if ((queryUserId || queryAllProperties) && !testSettings) {
      propertiesChanged |= properties.readProperty(DataManagerProperties.USER_ID, "Given user id: '%s'");
    }

    // </editor-fold>
    return propertiesChanged;
  }

  /**
   * Query and test context of KIT Data Manager. (group, investigation) If
   * 'testSettings' is true no query will be triggered.
   *
   * @return Are there any changes?
   * @throws InvalidDataManagerPropertiesException Setting of a property seems
   * to be invalid or KIT Data Manager is not well configured.
   */
  private boolean testDataManagerContext() throws InvalidDataManagerPropertiesException {
    boolean propertiesChanged = false;
    propertiesChanged |= testGroup();
    propertiesChanged |= testInvestigation();
    return propertiesChanged;
  }

  /**
   * Query and test context of KIT Data Manager. (group, investigation) If
   * 'testSettings' is true no query will be triggered.
   *
   * @return Are there any changes?
   * @throws InvalidDataManagerPropertiesException Setting of a property seems
   * to be invalid or KIT Data Manager is not well configured.
   */
  private boolean testGroup() throws InvalidDataManagerPropertiesException {
    boolean propertiesChanged = false;

    // <editor-fold defaultstate="collapsed" desc="Select group!">
    UserGroupWrapper allGroups;
    try {
      UserGroupWrapper groupCount = userGroupClient.getGroupCount(this.context);
      allGroups = userGroupClient.getAllGroups(0, groupCount.getCount(), context);
    } catch (DeserializationException dse) {
      String invalidCredentials = String.format("Credentials seems to be invalid! (%s / %s)",
              properties.getAccessKey(),
              properties.getAccessSecret());
      LOGGER.error(invalidCredentials);
      error.println(invalidCredentials);
      throw new InvalidDataManagerPropertiesException(invalidCredentials);
    }
    String[] allGroupLabels = new String[allGroups.getCount()];
    String[] allGroupDescriptions = new String[allGroups.getCount()];
    int index = 0;
    for (UserGroup item : allGroups.getEntities()) {
      UserGroup detailedGroup = userGroupClient.getGroupById(item.getId(), context).getEntities().get(0);
      allGroupLabels[index] = detailedGroup.getGroupId();
      allGroupDescriptions[index] = String.format("%s (%s)", detailedGroup.getGroupName(), detailedGroup.getDescription());
      index++;
    }
    if (testSettings) {
      if (!Arrays.asList(allGroupLabels).contains(properties.getUserGroup())) {
        String invalidGroup = String.format("No valid group Name: '%s'!", properties.getUserGroup());
        LOGGER.error(invalidGroup);
        error.println(invalidGroup);
        throw new InvalidDataManagerPropertiesException(invalidGroup);
      }
    }
    if ((!Arrays.asList(allGroupLabels).contains(properties.getUserGroup()))
            || (properties.getUserGroup() == null)
            || queryAllProperties) {
      propertiesChanged |= properties.readPropertyIndex(DataManagerProperties.USER_GROUP,
              allGroupLabels,
              allGroupDescriptions,
              "Chosen user group: '%s' - %s");
    }
    // </editor-fold>

    return propertiesChanged;
  }

  /**
   * Query and test context of KIT Data Manager. (investigation) If
   * 'testSettings' is true no query will be triggered.
   *
   * @return Are there any changes?
   * @throws InvalidDataManagerPropertiesException Setting of a property seems
   * to be invalid or KIT Data Manager is not well configured.
   */
  private boolean testInvestigation() throws InvalidDataManagerPropertiesException {
    boolean propertiesChanged = false;
    BaseMetaDataRestClient baseMetaDataClient = new BaseMetaDataRestClient(restServerUrl + REST_BASE_META_DATA_PATH, context);

    // <editor-fold defaultstate="collapsed" desc="Select investigation">
    int index = 0;
    long studyId = 0;
    int noOfInvestigations = baseMetaDataClient.getInvestigationCount(studyId, properties.getUserGroup(), context).getCount();
    if (noOfInvestigations > 0) {
      InvestigationWrapper allInvestigations = baseMetaDataClient.getAllInvestigations(studyId, 0, noOfInvestigations, properties.getUserGroup(), context);
      String values[] = new String[noOfInvestigations];
      String descriptions[] = new String[noOfInvestigations];
      index = 0;
      HashMap<Long, Study> allStudies = new HashMap();
      for (Investigation item : allInvestigations.getEntities()) {
        Investigation detailedGroup = baseMetaDataClient.getInvestigationById(item.getInvestigationId(), properties.getUserGroup(), context).getEntities().get(0);
        values[index] = detailedGroup.getInvestigationId().toString();
        studyId = detailedGroup.getStudy().getStudyId();
        if (!allStudies.containsKey(studyId)) {
          StudyWrapper studyById = baseMetaDataClient.getStudyById(studyId, properties.getUserGroup());
          allStudies.put(studyId, studyById.getEntities().get(0));
        }
        descriptions[index] = allStudies.get(studyId).getTopic() + "->" + detailedGroup.getTopic();
        index++;
      }
      if (testSettings) {
        if (!Arrays.asList(values).contains(properties.getInvestigation())) {
          String invalidInvestigation = String.format("No valid investigation: '%s'!", properties.getInvestigation());
          LOGGER.error(invalidInvestigation);
          error.println(invalidInvestigation);
          throw new InvalidDataManagerPropertiesException(invalidInvestigation);
        }
      }
      if ((!Arrays.asList(values).contains(properties.getInvestigation()))
              || (properties.getInvestigation() == null)
              || queryAllProperties) {
        propertiesChanged |= properties.readPropertyIndex(DataManagerProperties.INVESTIGATION,
                values,
                descriptions,
                "Chosen investigation: %s (%s)");
      }
    } else {
      String noInvestigationDefined = "No investigation exists!\nThere has to be at least one study containing one investigation.\nPlease create at least one study/investigation.";
      LOGGER.error(noInvestigationDefined);
      error.println(noInvestigationDefined);
      String line_Separator = "-------------------------------------------\n";
      output.print(line_Separator);
      String pQuestion = "Do you want to save actual settings and generate a default study / investigation?";
      output.format("\n%s (%s/%s)[%s]\n", pQuestion, YES, NO, YES);
      String inputValue = StdIoUtils.readStdInput(YES);
      if (inputValue.toLowerCase().contains(YES)) {
        properties.saveProperties();
        StudyBuilder studyBuilder = new StudyBuilder();
        Study study = studyBuilder.build();
        CommandStatus createStudy = GenericSetupClient.executeCommand(study, false);
        if (createStudy.getStatus().isSuccess()) {
          InvestigationBuilder investigationBuilder = new InvestigationBuilder();
          Investigation investigation = investigationBuilder.build();
          investigation.setStudy(study);
          CommandStatus createInvestigation = GenericSetupClient.executeCommand(investigation, false);
          if (createInvestigation.getStatus().isSuccess()) {
            propertiesChanged |= testInvestigation();
          }
        }
      } else {
        throw new InvalidDataManagerPropertiesException(noInvestigationDefined);
      }
    }
    // </editor-fold>

    return propertiesChanged;
  }

  /**
   * Query and test WebDAV credentials. (username, password) If 'testSettings'
   * is true no query will be triggered.
   *
   * @return Are there any changes?
   * @throws InvalidDataManagerPropertiesException Setting of a property seems
   * to be invalid or KIT Data Manager is not well configured.
   */
  private boolean testWebDavAuthentication() throws InvalidDataManagerPropertiesException {
    boolean propertiesChanged = false;

    DataManagerProperties list[] = {DataManagerProperties.USERNAME, DataManagerProperties.PASSWORD};
    if (testSettings) {
      if (!initWebDavAccess()) {
        error.println(invalidWebDavSettings);
        throw new InvalidDataManagerPropertiesException(invalidWebDavSettings);
      }
    } else {
      boolean webDavOk = false;
      while (!webDavOk) {
        propertiesChanged |= properties.readProperty(list, "Given values for webDAV access: %s - %s");
        if (testWebDavAccess()) {
          output.println("WebDAV access is working!");
          webDavOk = true;
        } else {
          error.println(invalidWebDavSettings);
          String defaultAnswer = YES;
          output.format("\nDo you want to insert new credentials for testing? (%s/%s)[%s]\n", YES, NO, defaultAnswer);
          boolean abort = StdIoUtils.readStdInput(defaultAnswer).toLowerCase().contains(NO);
          if (abort) {
            throw new InvalidDataManagerPropertiesException(invalidWebDavSettings);
          }
        }
      }
    }
    return propertiesChanged;
  }

  /**
   * Test access to webDAV server.
   *
   * @return success
   */
  private boolean initWebDavAccess() {
    return genericWebDavAccess("edu.kit.lsdf.adalapi.authentication.UserPasswordAuthenticationFromFile");
  }

  /**
   * Test access to webDAV server.
   * <b>Attention:<b><br/>
   * This method should only chosen if the credentials are not fixed. The chosen
   * authentication is slow but allows you to test several credentials.
   *
   * @return success
   */
  private boolean testWebDavAccess() {
    return genericWebDavAccess("edu.kit.lsdf.adalapi.authentication.DynamicUserPasswordAuthenticationFromFile");
  }

  /**
   * Test access to webDAV server.
   * <b>Attention:<b><br/>
   * This method should only chosen if the credentials are not fixed. The chosen
   * authentication is slow but allows you to test several credentials.
   *
   * @param pAuthenticationClass Class used for authentication.
   * @return success
   */
  private boolean genericWebDavAccess(String pAuthenticationClass) {
    boolean returnValue = false;
    StagingServiceRESTClient ssrc = new StagingServiceRESTClient(restServerUrl + REST_STAGING_PATH, context);
    try {
      StagingAccessPointConfigurationWrapper allAccessPoints = ssrc.getAllAccessPoints(properties.getAccessPoint(), properties.getUserGroup(), context);
      String webDavUrl = ssrc.getAccessPointById(allAccessPoints.getEntities().get(0).getId(), context).getEntities().get(0).getRemoteBaseUrl();
      // Method to overwrite the Webdav configuration class with a custom configuration class
      for (String protocol : new String[]{"http", "https"}) {
        Configuration configuration = ProtocolSettings.getSingleton().getConfiguration(protocol);
        configuration.setProperty("authClass", pAuthenticationClass);
        Iterator iter = configuration.getKeys();
        while (iter.hasNext()) {
          String key = (String) iter.next();
          String value = configuration.getString(key);
          configuration.clearProperty(key);
          configuration.addProperty(protocol + "." + key, value);
        }
        ProtocolSettings.getSingleton().overwriteConfiguration(configuration);
      }
      LOGGER.debug("Overwriting the default configuration with the specific configuration of generic ingest client.");
      AbstractFile af = new AbstractFile(new URL(webDavUrl));
      af.list();
      returnValue = true;
    } catch (MalformedURLException | AdalapiException e) {
      LOGGER.error(invalidWebDavSettings, e);
    }
    return returnValue;

  }

  /**
   * Set configuration class for authentication of WebDAV using ADALAPI.
   *
   * @param pProperties instance holding accessKey and accessSecret.
   * @return success or not
   */
  public static boolean initializeWebDav(DataManagerPropertiesImpl pProperties) {
    DataManagerPropertiesHelper dmph = new DataManagerPropertiesHelper(pProperties);

    return dmph.initWebDavAccess();
  }

  /**
   * Execute command using jcommander. Parameters already parsed by JCommander.
   *
   * @param ip parsed arguments.
   * @return Status of the command.
   */
  public static CommandStatus executeCommand(InitParameters ip) {
    boolean returnValue;
    Status exitCode;
    CommandStatus status;
    DataManagerPropertiesImpl myProperties = DataManagerPropertiesImpl.getDefaultInstance();
    try {

      if (ip.getFlags().contains(CommandLineFlags.TEST_ONLY)) {
        returnValue = DataManagerPropertiesHelper.testSettings(myProperties, ip.getFlags());
      } else {
        returnValue = DataManagerPropertiesHelper.querySettings(myProperties, ip.getFlags());
      }
      exitCode = returnValue ? Status.SUCCESSFUL : Status.FAILED;
      status = new CommandStatus(exitCode);
    } catch (IllegalArgumentException iae) {
      LOGGER.error(null, iae);
      status = new CommandStatus(iae);
    }

    return status;
  }

}
