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
package edu.kit.dama.rest.client.generic.helper.test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sun.jersey.api.client.ClientResponse;

import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.rest.admin.client.impl.UserGroupRestClient;
import edu.kit.dama.rest.admin.types.UserGroupWrapper;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
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
import edu.kit.dama.rest.client.generic.helper.RESTClientHelper;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;

@PrepareForTest({UserGroupRestClient.class, SimpleRESTContext.class, RESTClientHelper.class, UserDataWrapper.class, BaseMetaDataRestClient.class, StagingServiceRESTClient.class})
@RunWith(PowerMockRunner.class)
public class RESTClientHelperTest {

  private static final String NANOSCOPY_GROUP = "NANOSCOPY_GROUP";

  private RESTClientHelper clientHelper;

  private SimpleRESTContext restContext;

  private UserGroupRestClient groupRestClient;

  private BaseMetaDataRestClient baseMetaDataRestClient;

  private StagingServiceRESTClient stagingClient;

  @Before
  public void setup() throws Exception {
    restContext = PowerMockito.mock(SimpleRESTContext.class);
    groupRestClient = PowerMockito.mock(UserGroupRestClient.class);
    baseMetaDataRestClient = PowerMockito.mock(BaseMetaDataRestClient.class);
    stagingClient = PowerMockito.mock(StagingServiceRESTClient.class);
    clientHelper = new RESTClientHelper(restContext, "http://kit.dm.demourl/");
  }

  /**
   * +ve Get specific user from the database
   *
   * @throws Exception
   */
  @Test
  public void getSpecificUserTest() throws Exception {

    PowerMockito.whenNew(UserGroupRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_USER_GROUP_PATH, restContext).thenReturn(groupRestClient);

    UserDataWrapper userWrapper = Mockito.mock(UserDataWrapper.class);
    UserDataWrapper userGroups = new UserDataWrapper();

    userWrapper.setCount(1);
    Mockito.when(groupRestClient.getUserCount(restContext)).thenReturn(userWrapper);

    @SuppressWarnings("unchecked")
    List<UserData> pEntities = new ArrayList<UserData>();

    UserData userData1 = new UserData();
    userData1.setUserId(12L);
    userData1.setDistinguishedName("dama");
    pEntities.add(userData1);

    userGroups.setEntities(pEntities);
    userGroups.setWrappedEntities(pEntities);
    userGroups.setCount(1);

    Mockito.when(groupRestClient.getAllUsers(0, userWrapper.getCount(), restContext)).thenReturn(userGroups);

    UserDataWrapper foundUser = new UserDataWrapper();
    foundUser.setEntities(pEntities);
    Mockito.when(groupRestClient.getUserById(12L, restContext)).thenReturn(foundUser);

    UserData specificUser = clientHelper.getSpecificUser("dama");
    Assert.assertEquals("dama", specificUser.getDistinguishedName());
  }

  /**
   * Test for fetching non existing user from the database
   *
   * @throws Exception
   */
  @Test
  public void getSpecificUserNullTest() throws Exception {

    PowerMockito.whenNew(UserGroupRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_USER_GROUP_PATH, restContext).thenReturn(groupRestClient);

    UserDataWrapper userWrapper = Mockito.mock(UserDataWrapper.class);
    UserDataWrapper userGroups = new UserDataWrapper();

    userWrapper.setCount(1);
    Mockito.when(groupRestClient.getUserCount(restContext)).thenReturn(userWrapper);

    @SuppressWarnings("unchecked")
    List<UserData> pEntities = new ArrayList<UserData>();

    UserData userData1 = new UserData();
    userData1.setUserId(12L);
    userData1.setDistinguishedName("dama");
    pEntities.add(userData1);

    userGroups.setEntities(pEntities);
    userGroups.setWrappedEntities(pEntities);
    userGroups.setCount(1);

    Mockito.when(groupRestClient.getAllUsers(0, userWrapper.getCount(), restContext)).thenReturn(userGroups);

    UserDataWrapper foundUser = new UserDataWrapper();
    foundUser.setEntities(pEntities);
    Mockito.when(groupRestClient.getUserById(12L, restContext)).thenReturn(foundUser);

    UserData specificUser = clientHelper.getSpecificUser("dama2");
    Assert.assertNull(specificUser);
  }

  /**
   * +ve Test the getSpecifiedGroupID() method to check correct and existing
   * group
   *
   * @throws Exception
   */
  @Test
  public void getSpecificGroupTest() throws Exception {

    PowerMockito.whenNew(UserGroupRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_USER_GROUP_PATH, restContext).thenReturn(groupRestClient);

    UserGroupWrapper userGroups = new UserGroupWrapper();
    userGroups.setCount(1);
    List<UserGroup> pEntities = new ArrayList<UserGroup>();
    UserGroup group = new UserGroup();
    group.setId(12L);
    group.setGroupId("NANOSCOPY_GROUP");
    pEntities.add(group);
    userGroups.setEntities(pEntities);

    Mockito.when(groupRestClient.getGroupCount(restContext)).thenReturn(userGroups);
    Mockito.when(groupRestClient.getAllGroups(0, 1, restContext)).thenReturn(userGroups);
    Mockito.when(groupRestClient.getGroupById(12L, restContext)).thenReturn(userGroups);

    UserGroup specificGroupID = clientHelper.getSpecificGroupID(NANOSCOPY_GROUP);
    Assert.assertEquals(NANOSCOPY_GROUP, specificGroupID.getGroupId());
  }

  /**
   * -ve Test the getSpecifiedGroupID() method to check a non existing group
   *
   * @throws Exception
   */
  @Test
  public void getIncorrectSpecificGroupTest() throws Exception {

    PowerMockito.whenNew(UserGroupRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_USER_GROUP_PATH, restContext).thenReturn(groupRestClient);

    UserGroupWrapper userGroups = new UserGroupWrapper();
    userGroups.setCount(1);
    List<UserGroup> pEntities = new ArrayList<UserGroup>();
    UserGroup group = new UserGroup();
    group.setId(12L);
    group.setGroupName("ARCHIEOLOGY_GROUP");
    pEntities.add(group);
    userGroups.setEntities(pEntities);

    Mockito.when(groupRestClient.getGroupCount(restContext)).thenReturn(userGroups);
    Mockito.when(groupRestClient.getAllGroups(0, 1, restContext)).thenReturn(userGroups);
    Mockito.when(groupRestClient.getGroupById(12L, restContext)).thenReturn(userGroups);

    UserGroup specificGroupID = clientHelper.getSpecificGroupID(NANOSCOPY_GROUP);
    Assert.assertNull(specificGroupID);
  }

  /**
   * -ve Test the getSpecifiedGroupID() method where there are no groups created
   * in the database i.e. empty groups table
   *
   * @throws Exception
   */
  @Test
  public void getGroupFromEmptyGroupsTableTest() throws Exception {

    PowerMockito.whenNew(UserGroupRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_USER_GROUP_PATH, restContext).thenReturn(groupRestClient);

    UserGroupWrapper userGroups = new UserGroupWrapper();
    userGroups.setCount(0);

    Mockito.when(groupRestClient.getGroupCount(restContext)).thenReturn(userGroups);
    Mockito.when(groupRestClient.getAllGroups(0, 0, restContext)).thenReturn(userGroups);

    UserGroup specificGroupID = clientHelper.getSpecificGroupID(NANOSCOPY_GROUP);
    Assert.assertNull(specificGroupID);
  }

  /**
   * +ve Get specific user using the ID in Long format
   *
   * @throws Exception
   */
  @Test
  public void getSpecificUserWithLongIDTest() throws Exception {

    UserDataWrapper userGroups = new UserDataWrapper();
    userGroups.setCount(1);

    List<UserData> pEntities = new ArrayList<UserData>();
    UserData userUnderTest = new UserData();
    userUnderTest.setUserId(12L);
    pEntities.add(userUnderTest);
    userGroups.setEntities(pEntities);

    PowerMockito.whenNew(UserGroupRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_USER_GROUP_PATH, restContext).thenReturn(groupRestClient);

    Mockito.when(groupRestClient.getUserById(12L, restContext)).thenReturn(userGroups);

    UserData specificUser = clientHelper.getSpecificUser(12L);

    Assert.assertEquals(new Long(12), specificUser.getUserId());

  }

  /**
   * -ve Get specific user using the ID in Long Format
   *
   * @throws Exception
   */
  @Test
  public void getWrongSpecificUserWithLongIDTest() throws Exception {
    PowerMockito.whenNew(UserGroupRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_USER_GROUP_PATH, restContext).thenReturn(groupRestClient);

    UserDataWrapper userGroups = new UserDataWrapper();
    userGroups.setCount(1);

    List<UserData> pEntities = new ArrayList<UserData>();
    UserData userUnderTest = new UserData();
    userUnderTest.setUserId(12L);
    pEntities.add(userUnderTest);
    userGroups.setEntities(pEntities);

    Mockito.when(groupRestClient.getUserById(22L, restContext)).thenReturn(userGroups);

    UserData specificUser = clientHelper.getSpecificUser(12L);
    Assert.assertNull(specificUser);
  }

  // Basic Metadata Study Investigation DO tests
  /**
   * +ve Create study test for creating a Study which is already existing in the
   * database Result - The existing study should be returned
   *
   * @throws Exception
   */
  @Test
  public void createExistingStudyTest() throws Exception {
    PowerMockito.whenNew(BaseMetaDataRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_BASE_META_DATA_PATH, restContext).thenReturn(baseMetaDataRestClient);
    StudyWrapper studyWrapper = new StudyWrapper();
    studyWrapper.setCount(1);
    List<Study> pEntities = new ArrayList<Study>();
    Study testStudy = new Study();
    testStudy.setTopic("nanoscopy");
    testStudy.setStudyId(12L);
    pEntities.add(testStudy);
    studyWrapper.setEntities(pEntities);

    Mockito.when(baseMetaDataRestClient.getStudyCount(restContext)).thenReturn(studyWrapper);
    Mockito.when(baseMetaDataRestClient.getAllStudies(1, 0, NANOSCOPY_GROUP, restContext)).thenReturn(studyWrapper);
    Mockito.when(baseMetaDataRestClient.getStudyById(12L, NANOSCOPY_GROUP, restContext)).thenReturn(studyWrapper);
    Study createdStudy = clientHelper.createStudy(NANOSCOPY_GROUP, testStudy);
    Assert.assertEquals("nanoscopy", createdStudy.getTopic());
  }

  /**
   * -ve Create study test for creating a non-existing study. Result - The newly
   * created study will be returned
   *
   * @throws Exception
   */
  @Test
  public void createNewStudyTest() throws Exception {
    PowerMockito.whenNew(BaseMetaDataRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_BASE_META_DATA_PATH, restContext).thenReturn(baseMetaDataRestClient);
    StudyWrapper studyWrapper = new StudyWrapper();
    studyWrapper.setCount(1);
    Study testStudy = new Study();
    testStudy.setTopic("nanoscopy");
    testStudy.setStudyId(12L);
    List<Study> pEntities = new ArrayList<Study>();
    pEntities.add(testStudy);

    studyWrapper.setEntities(pEntities);

    StudyWrapper studyWrapper2 = new StudyWrapper();
    studyWrapper2.setCount(1);
    Study newStudy = new Study();
    newStudy.setTopic("ecodicology");
    newStudy.setStudyId(14L);
    List<Study> pEntities2 = new ArrayList<Study>();

    pEntities2.add(newStudy);
    studyWrapper2.setEntities(pEntities2);

    Mockito.when(baseMetaDataRestClient.getStudyCount(restContext)).thenReturn(studyWrapper);
    Mockito.when(baseMetaDataRestClient.getAllStudies(0, 1, NANOSCOPY_GROUP, restContext)).thenReturn(studyWrapper);
    Mockito.when(baseMetaDataRestClient.getStudyById(12L, NANOSCOPY_GROUP, restContext)).thenReturn(studyWrapper);

    Mockito.when(baseMetaDataRestClient.addStudy(newStudy, NANOSCOPY_GROUP, restContext)).thenReturn(studyWrapper2);

    Study createdStudy = clientHelper.createStudy(NANOSCOPY_GROUP, newStudy);
    Assert.assertEquals("ecodicology", createdStudy.getTopic());
  }

  /**
   * +ve Create investigation test for creating a existing investigation Result
   * - Existing investigation will be returned
   *
   * @throws Exception
   */
  @Test
  public void createExistingInvestigationTest() throws Exception {
    PowerMockito.whenNew(BaseMetaDataRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_BASE_META_DATA_PATH, restContext).thenReturn(baseMetaDataRestClient);
    Study testStudy = new Study();
    testStudy.setTopic("nanoscopy");
    testStudy.setStudyId(12L);

    InvestigationWrapper investigationWrapper = new InvestigationWrapper();
    investigationWrapper.setCount(1);
    List<Investigation> pEntities = new ArrayList<Investigation>();
    Investigation testInvestigation = new Investigation();
    testInvestigation.setTopic("HELA");
    testInvestigation.setInvestigationId(10L);
    testInvestigation.setStudy(testStudy);

    pEntities.add(testInvestigation);
    investigationWrapper.setEntities(pEntities);

    Mockito.when(baseMetaDataRestClient.getInvestigationCount(12L, NANOSCOPY_GROUP, restContext)).thenReturn(investigationWrapper);
    Mockito.when(baseMetaDataRestClient.getAllInvestigations(12L, 0, 1, NANOSCOPY_GROUP, restContext)).thenReturn(investigationWrapper);
    Mockito.when(baseMetaDataRestClient.getInvestigationById(10L, NANOSCOPY_GROUP, restContext)).thenReturn(investigationWrapper);

    Investigation investigation = clientHelper.createInvestigation(NANOSCOPY_GROUP, testStudy, testInvestigation);
    Assert.assertEquals("HELA", investigation.getTopic());
  }

  /**
   * -ve Create investigation test for creating a non-existing investigation
   * Result - Existing investigation will be returned
   *
   * @throws Exception
   */
  @Test
  public void createNonExistingInvestigationTest() throws Exception {
    PowerMockito.whenNew(BaseMetaDataRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_BASE_META_DATA_PATH, restContext).thenReturn(baseMetaDataRestClient);
    Study testStudy = new Study();
    testStudy.setTopic("nanoscopy");
    testStudy.setStudyId(12L);

    InvestigationWrapper investigationWrapper = new InvestigationWrapper();
    investigationWrapper.setCount(1);
    List<Investigation> pEntities = new ArrayList<Investigation>();
    Investigation testInvestigation = new Investigation();
    testInvestigation.setTopic("HELA");
    testInvestigation.setInvestigationId(10L);
    testInvestigation.setStudy(testStudy);

    pEntities.add(testInvestigation);
    investigationWrapper.setEntities(pEntities);

    Mockito.when(baseMetaDataRestClient.getInvestigationCount(12L, NANOSCOPY_GROUP, restContext)).thenReturn(investigationWrapper);
    Mockito.when(baseMetaDataRestClient.getAllInvestigations(12L, 0, 1, NANOSCOPY_GROUP, restContext)).thenReturn(investigationWrapper);
    Mockito.when(baseMetaDataRestClient.getInvestigationById(10L, NANOSCOPY_GROUP, restContext)).thenReturn(investigationWrapper);

    Investigation testInvestigation2 = new Investigation();
    testInvestigation2.setTopic("Fibroplast");
    testInvestigation2.setInvestigationId(9L);

    InvestigationWrapper investigationWrapper2 = new InvestigationWrapper();
    investigationWrapper2.setCount(1);
    List<Investigation> pEntities2 = new ArrayList<Investigation>();
    pEntities2.add(testInvestigation2);
    investigationWrapper2.setEntities(pEntities2);

    Mockito.when(baseMetaDataRestClient.addInvestigationToStudy(12L, testInvestigation2, NANOSCOPY_GROUP, restContext)).thenReturn(investigationWrapper2);

    Investigation investigation = clientHelper.createInvestigation(NANOSCOPY_GROUP, testStudy, testInvestigation2);
    Assert.assertEquals("Fibroplast", investigation.getTopic());
  }

  /**
   * +ve Create a new digital object Return - Newly created digital object
   *
   * @throws Exception
   */
  @Test
  public void createDigitalObjecttest() throws Exception {
    PowerMockito.whenNew(BaseMetaDataRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_BASE_META_DATA_PATH, restContext).thenReturn(baseMetaDataRestClient);

    DigitalObject pDigitalObject = new DigitalObject();

    DigitalObjectWrapper digitalObjectWrapper = new DigitalObjectWrapper();
    List<DigitalObject> pEntities = new ArrayList<DigitalObject>();
    pEntities.add(pDigitalObject);
    digitalObjectWrapper.setEntities(pEntities);
    digitalObjectWrapper.setCount(1);;

    Mockito.when(baseMetaDataRestClient.addDigitalObjectToInvestigation(10L, pDigitalObject, NANOSCOPY_GROUP, restContext)).thenReturn(digitalObjectWrapper);
    DigitalObject createNanoscopyDigitalObject = clientHelper.createNanoscopyDigitalObject(NANOSCOPY_GROUP, 10L, pDigitalObject);
    Assert.assertNotNull(createNanoscopyDigitalObject);

  }

  /**
   * +ve Get digital object info using the DigitalObjectID Result - list of all
   * Digital Objects
   *
   * @throws Exception
   */
  @Test
  public void getDigitalObjectInfoByIDtest() throws Exception {
    PowerMockito.whenNew(BaseMetaDataRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_BASE_META_DATA_PATH, restContext).thenReturn(baseMetaDataRestClient);
    DigitalObjectWrapper doiWrapper = new DigitalObjectWrapper();
    List<DigitalObject> pEntities = new ArrayList<DigitalObject>();
    DigitalObject doi = new DigitalObject();

    pEntities.add(doi);
    doiWrapper.setEntities(pEntities);
    Mockito.when(baseMetaDataRestClient.getDigitalObjectByDOI(NANOSCOPY_GROUP, "abcd", restContext)).thenReturn(doiWrapper);
    List<IngestInformation> ingestInformation = new ArrayList<IngestInformation>();
    IngestInformation ingestInfo = new IngestInformation();
    ingestInfo.setDigitalObjectId("abcd");
    ingestInformation.add(ingestInfo);

    List<DigitalObject> digitalObjectInformationById = clientHelper.getDigitalObjectInformationById(NANOSCOPY_GROUP, ingestInformation);
    Assert.assertEquals(1, digitalObjectInformationById.size());
  }

  /**
   * -ve Get digital object info using the DigitalObjectID Result - Empty list
   *
   * @throws Exception
   */
  @Test
  public void getDigitalObjectInfoByIDEmptyListtest() throws Exception {
    PowerMockito.whenNew(BaseMetaDataRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_BASE_META_DATA_PATH, restContext).thenReturn(baseMetaDataRestClient);
    DigitalObjectWrapper doiWrapper = new DigitalObjectWrapper();
    List<DigitalObject> pEntities = new ArrayList<DigitalObject>();
    DigitalObject doi = new DigitalObject();

    pEntities.add(doi);
    doiWrapper.setEntities(pEntities);
    Mockito.when(baseMetaDataRestClient.getDigitalObjectByDOI(NANOSCOPY_GROUP, "abcd", restContext)).thenReturn(doiWrapper);
    List<IngestInformation> ingestInformation = new ArrayList<IngestInformation>();

    List<DigitalObject> digitalObjectInformationById = clientHelper.getDigitalObjectInformationById(NANOSCOPY_GROUP, ingestInformation);
    Assert.assertEquals(0, digitalObjectInformationById.size());
  }

  /**
   * +ve Create a new ingest entity for staring a upload Return - Ingest Info
   * object with Status PRE_INGEST_SCHEDULED
   *
   * @throws Exception
   */
  @Test
  public void createIngestEntitytest() throws Exception {
    PowerMockito.whenNew(StagingServiceRESTClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_STAGING_PATH, restContext).thenReturn(stagingClient);
    IngestInformationWrapper ingestWrapper = new IngestInformationWrapper();
    List<IngestInformation> pEntities = new ArrayList<IngestInformation>();
    IngestInformation ingestInfo = new IngestInformation();
    ingestInfo.setStatusEnum(INGEST_STATUS.PRE_INGEST_SCHEDULED);
    pEntities.add(ingestInfo);
    ingestWrapper.setEntities(pEntities);

    Mockito.when(stagingClient.createIngest("abcd", "accessMethod", new ArrayList<Long>(),"USERS", restContext)).thenReturn(ingestWrapper);
    IngestInformation createIngestEntity = clientHelper.createIngestEntity("abcd", "accessMethod", "USERS");
    Assert.assertEquals(INGEST_STATUS.PRE_INGEST_SCHEDULED, createIngestEntity.getStatusEnum());
  }

  /**
   * Get the specific ingest information requested by long ID Return -
   * IngestInfo
   *
   * @throws Exception
   */
  @Test
  public void getSpecifiedIngestInfotest() throws Exception {
    PowerMockito.whenNew(StagingServiceRESTClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_STAGING_PATH, restContext).thenReturn(stagingClient);
    IngestInformationWrapper ingestWrapper = new IngestInformationWrapper();
    List<IngestInformation> pEntities = new ArrayList<IngestInformation>();
    IngestInformation ingestInfo = new IngestInformation();
    ingestInfo.setStatusEnum(INGEST_STATUS.PRE_INGEST_RUNNING);
    pEntities.add(ingestInfo);
    ingestWrapper.setEntities(pEntities);

    Mockito.when(stagingClient.getIngestById(12L, restContext)).thenReturn(ingestWrapper);

    IngestInformation specifiedIngestInformation = clientHelper.getSpecifiedIngestInformation(12L);
    Assert.assertEquals(INGEST_STATUS.PRE_INGEST_RUNNING, specifiedIngestInformation.getStatusEnum());
  }

  /**
   * +ve Update the specific ingest information requested by long ID Return -
   * IngestInfo
   *
   * @throws Exception
   */
  @Test
  public void updateIngestStatustest() throws Exception {
    PowerMockito.whenNew(StagingServiceRESTClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_STAGING_PATH, restContext).thenReturn(stagingClient);
    ClientResponse ingestResponse = new ClientResponse(0, null, null, null);

    Mockito.when(stagingClient.updateIngest(12L, null, INGEST_STATUS.PRE_INGEST_FINISHED.getId())).thenReturn(ingestResponse);
    ClientResponse updateIngestStatus = clientHelper.updateIngestStatus(12L, INGEST_STATUS.PRE_INGEST_FINISHED.getId());
    Assert.assertEquals(0, updateIngestStatus.getStatus());
  }

  /**
   * Get ingest count from database Return - Int value
   *
   * @throws Exception
   */
  @Test
  public void getIngestCounttest() throws Exception {
    PowerMockito.whenNew(StagingServiceRESTClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_STAGING_PATH, restContext).thenReturn(stagingClient);
    IngestInformationWrapper ingestWrapper = new IngestInformationWrapper();
    ingestWrapper.setCount(20);
    Mockito.when(stagingClient.getIngestCount(restContext)).thenReturn(ingestWrapper);
    int ingestCount = clientHelper.getIngestCount();
    Assert.assertEquals(20, ingestCount);

  }

  /**
   * Get list of IDs for performed Ingest
   *
   * @throws Exception
   */
  @Test
  public void getIngestInfoIDstest() throws Exception {
    PowerMockito.whenNew(StagingServiceRESTClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_STAGING_PATH, restContext).thenReturn(stagingClient);
    IngestInformationWrapper ingestWrapper = new IngestInformationWrapper();
    List<IngestInformation> pEntities = new ArrayList<IngestInformation>();
    IngestInformation ingestInfo1 = new IngestInformation();
    ingestInfo1.setId(1L);
    pEntities.add(ingestInfo1);
    IngestInformation ingestInfo2 = new IngestInformation();
    ingestInfo2.setId(2L);
    pEntities.add(ingestInfo2);
    ingestWrapper.setEntities(pEntities);

    Mockito.when(stagingClient.getAllIngestInformation(null, null, INGEST_STATUS.INGEST_FINISHED.getId(), 0, 2, restContext)).thenReturn(ingestWrapper);
    Mockito.when(stagingClient.getIngestById(1L, restContext)).thenReturn(ingestWrapper);
    Mockito.when(stagingClient.getIngestById(2L, restContext)).thenReturn(ingestWrapper);

    List<IngestInformation> ingestInformationIDs = clientHelper.getIngestInformationIDs(2, INGEST_STATUS.INGEST_FINISHED.getId());
    Assert.assertEquals(2, ingestInformationIDs.size());
  }

  /**
   * Get list of all performed Ingest
   *
   * @throws Exception
   */
  @Test
  public void getIngestInfotest() throws Exception {
    PowerMockito.whenNew(StagingServiceRESTClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_STAGING_PATH, restContext).thenReturn(stagingClient);
    IngestInformationWrapper ingestWrapper = new IngestInformationWrapper();
    List<IngestInformation> pEntities = new ArrayList<IngestInformation>();
    IngestInformation ingestInfo1 = new IngestInformation();
    ingestInfo1.setId(1L);
    pEntities.add(ingestInfo1);
    IngestInformation ingestInfo2 = new IngestInformation();
    ingestInfo2.setId(2L);
    pEntities.add(ingestInfo2);
    ingestWrapper.setEntities(pEntities);

    Mockito.when(stagingClient.getIngestById(1L, restContext)).thenReturn(ingestWrapper);
    Mockito.when(stagingClient.getIngestById(2L, restContext)).thenReturn(ingestWrapper);

    List<IngestInformation> listEntries = new ArrayList<IngestInformation>();
    listEntries.add(ingestInfo1);
    listEntries.add(ingestInfo2);
    List<IngestInformation> ingestInformationIDs = clientHelper.getIngestInformation(listEntries);
    Assert.assertEquals(2, ingestInformationIDs.size());
  }

  /**
   * Get ingest information using the digital object id
   *
   * @throws Exception
   */
  @Test
  public void getIngestInformationByDOItest() throws NoSuchMethodException, SecurityException, Exception {
    PowerMockito.whenNew(StagingServiceRESTClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_STAGING_PATH, restContext).thenReturn(stagingClient);
    IngestInformationWrapper ingestWrapper = new IngestInformationWrapper();
    List<IngestInformation> pEntities = new ArrayList<IngestInformation>();
    IngestInformation ingestInfo = new IngestInformation();
    ingestInfo.setId(1L);

    pEntities.add(ingestInfo);
    ingestWrapper.setEntities(pEntities);

    Mockito.when(stagingClient.getAllIngestInformation(null, "abcd", 0, 0, 0, restContext)).thenReturn(ingestWrapper);
    Mockito.when(stagingClient.getIngestById(1L)).thenReturn(ingestWrapper);
    IngestInformation ingestInformationByDOI = clientHelper.getIngestInformationByDOI("abcd");
    Assert.assertNotNull(ingestInformationByDOI);

  }

  /**
   * Get specific investigation using the Long ID Result - Matching
   * Investigation
   *
   * @throws Exception
   */
  @Test
  public void getSpecificInvestigationtest() throws Exception {
    PowerMockito.whenNew(BaseMetaDataRestClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_BASE_META_DATA_PATH, restContext).thenReturn(baseMetaDataRestClient);
    InvestigationWrapper investigationWrapper = new InvestigationWrapper();
    List<Investigation> pEntities = new ArrayList<Investigation>();
    Investigation investigation = new Investigation();
    pEntities.add(investigation);
    investigationWrapper.setEntities(pEntities);
    Mockito.when(baseMetaDataRestClient.getInvestigationById(12L, null,  restContext)).thenReturn(investigationWrapper);

    Investigation specificInvestigation = clientHelper.getSpecificInvestigation(12L);
    Assert.assertNotNull(specificInvestigation);
  }

  /**
   * Get specific download info using the Long ID Result - Matching DownloadInfo
   *
   * @throws Exception
   */
  @Test
  public void getSpecificDownloadInformationtest() throws Exception {
    PowerMockito.whenNew(StagingServiceRESTClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_STAGING_PATH, restContext).thenReturn(stagingClient);
    DownloadInformationWrapper downloadWrapper = new DownloadInformationWrapper();
    List<DownloadInformation> pEntities = new ArrayList<DownloadInformation>();
    DownloadInformation downloadInfo = new DownloadInformation();
    pEntities.add(downloadInfo);

    downloadWrapper.setEntities(pEntities);
    Mockito.when(stagingClient.getDownloadById(20L, restContext)).thenReturn(downloadWrapper);

    DownloadInformation specificInvestigation = clientHelper.getDownloadInformation(20L);
    Assert.assertNotNull(specificInvestigation);
  }

  /**
   * Create download entity test Return - Download info containing the
   * information about the created download
   *
   * @throws Exception
   */
  @Test
  public void createDownloadEntitytest() throws Exception {
    PowerMockito.whenNew(StagingServiceRESTClient.class.getConstructor(String.class, SimpleRESTContext.class)).withArguments("http://kit.dm.demourl" + IDataManagerRestUrl.REST_STAGING_PATH, restContext).thenReturn(stagingClient);
    DownloadInformationWrapper downloadWrapper = new DownloadInformationWrapper();
    List<DownloadInformation> pEntities = new ArrayList<DownloadInformation>();
    DownloadInformation downloadInfo = new DownloadInformation();
    pEntities.add(downloadInfo);
    downloadWrapper.setEntities(pEntities);
    Mockito.when(stagingClient.createDownload("doiobjetid", "accessmoethod", restContext)).thenReturn(downloadWrapper);
    DownloadInformation createDownloadEntity = clientHelper.createDownloadEntity("doiobjetid", "accessmoethod");
    Assert.assertNotNull(createDownloadEntity);
  }

  @Test
  @Ignore
  public void performDataDownloadADALAPItest() throws Exception {
    URL testURL = new URL("http://testurl/kit.edu");
    File downloadDestination = new File("C:\\destination");
    AbstractFile abstractFile = new AbstractFile(downloadDestination);
    Configuration config = new CombinedConfiguration();
    PowerMockito.whenNew(AbstractFile.class.getConstructor(URL.class, Configuration.class)).withArguments(testURL, config).thenReturn(abstractFile);
    PowerMockito.whenNew(AbstractFile.class.getConstructor(File.class)).withArguments(downloadDestination).thenReturn(abstractFile);

    Mockito.when(abstractFile.downloadDirectory(abstractFile)).thenReturn(abstractFile);
    DownloadInformation dataToDownload = new DownloadInformation();

    AbstractFile performDataDownloadADALAPI = clientHelper.performDataDownloadADALAPI("accessMethod", dataToDownload, downloadDestination);
    Assert.assertNotNull(performDataDownloadADALAPI);
  }

}
