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
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.client.setup.GenericSetupClient;
import edu.kit.jcommander.converter.DateConverter;
import edu.kit.jcommander.validator.DateValidator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commandline parameters for the study.
 * @author hartmann-v
 */
@Parameters(commandNames = "createstudy", commandDescription = "Create a study.")
public class StudyBuilder extends SetupCommandLineParameters {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(StudyBuilder.class);
    /**
   * Formatter for date.
   */
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
/**
   * Topic of the study.
   */
  @Parameter(names = {"-t", "--topic"}, description = "'Topic' of the study. ", required = false)
  String topic = "My first study";
  /**
   * Note for the study.
   */
  @Parameter(names = {"-n", "--note"}, description = "'note' of the study. ", required = false)
  String note = "Any note.";
  /**
   * Legal note for the study.
   */
  @Parameter(names = {"-l", "--legalNote"}, description = "'legal note' of the study. ", required = false)
  String legalNote = "Any legal note.";
  /**
   * Manager of the study.
   */
  @Parameter(names = {"-m", "--managerId"}, description = "'User Id' of the manager of the study. ", required = false)
  Long managerId = null;
  /**
   * Start date of the study.
   */
  @Parameter(names = {"-s", "--startDate"}, description = "'Start date' of the study. Format: yyyy-MM-dd (e.g.: 2015-03-11)", required = false, converter = DateConverter.class, validateWith = DateValidator.class)
  Date startDate = new Date();
  /**
   * End date of the study.
   */
  @Parameter(names = {"-e", "--endDate"}, description = "'End date' of the study. Format: yyyy-MM-dd (e.g.: 2015-03-11)", required = false, converter = DateConverter.class, validateWith =  DateValidator.class)
  Date endDate;


 /**
   * Default constructor.
   */
  public StudyBuilder() {
    this("createstudy");
  }
 /**
   * Default constructor.
   * @param pCommand Command name of the CLI.
   */
  public StudyBuilder(String pCommand) {
    super(pCommand);
    try {
      endDate = sdf.parse("2020-12-31");
    } catch (ParseException ex) {
      LOGGER.error(null, ex);
    }
  }
  /**
   * Set the topic.
   *
   * @param pTopic topic
   * @return instance of builder
   */
  public final StudyBuilder topic(String pTopic) {
    topic = pTopic;
    return this;
  }

  /**
   * Set the legalNote.
   *
   * @param pLegalNote legalNote
   * @return instance of builder
   */
  public final StudyBuilder legalNote(String pLegalNote) {
    legalNote = pLegalNote;
    return this;
  }

  /**
   * Set the note.
   *
   * @param pNote note
   * @return instance of builder
   */
  public final StudyBuilder note(String pNote) {
    note = pNote;
    return this;
  }

  /**
   * Set the manager id.
   *
   * @param pManagerId user id of the manager
   * @return instance of builder
   */
  public final StudyBuilder managerId(Long pManagerId) {
    managerId = pManagerId;
    return this;
  }

  /**
   * Set the start date.
   *
   * @param pStartDate start date
   * @return instance of builder
   */
  public final StudyBuilder startDate(String pStartDate) {
    try {
      startDate = sdf.parse(pStartDate);
    } catch (ParseException ex) {
      LOGGER.error(null, ex);
    }
    return this;
  }

  /**
   * Set the end date.
   *
   * @param pEndDate start date
   * @return instance of builder
   */
  public final StudyBuilder endDate(String pEndDate) {
    try {
      endDate = sdf.parse(pEndDate);
    } catch (ParseException ex) {
      LOGGER.error(null, ex);
    }
    return this;
  }

  /**
   * Build study instance using set attributes.
   *
   * @return Instance of Study.
   */
  public Study build() {
    Study study = new Study();
    study.setTopic(topic);
    study.setLegalNote(legalNote);
    study.setNote(note);
    study.setStartDate(startDate);
    study.setEndDate(endDate);
    if (managerId != null) {
      UserData user = new UserData();
      user.setUserId(managerId);
      study.setManager(user);
    }
    return study;

  }

  @Override
  public CommandStatus executeCommand() {
    GenericSetupClient.setVerbose(verbose);
    return GenericSetupClient.executeCommand(this.build(), interactive);
  }

}
