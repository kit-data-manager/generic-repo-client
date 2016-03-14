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
import edu.kit.jcommander.generic.parameter.CommandLineParameters;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.rest.client.setup.GenericSetupClient;
import edu.kit.jcommander.converter.DateConverter;
import edu.kit.jcommander.validator.DateValidator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commandline parameters for the investigation
 * .
 * @author hartmann-v
 */
@Parameters(commandNames = "createinvestigation", commandDescription = "Create an investigation.")
public class InvestigationBuilder extends SetupCommandLineParameters {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineParameters.class);
  /**
   * Formatter for date.
   */
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  /**
   * Topic of the investigation.
   */
  @Parameter(names = {"-t", "--topic"}, description = "'Topic' of the investigation.", required = false)
  String topic = "My first investigation";
  /**
   * Note for the investigation.
   */
  @Parameter(names = {"-n", "--note"}, description = "'note' of the investigation. ", required = false)
  String note = "Any note about the investigation '" + topic + "'.";
  /**
   * Legal not for the investigation.
   */
  @Parameter(names = {"-d", "--description"}, description = "'description' of the investigation. ", required = false)
  String description = "Any description.";
  /**
   * Start date of the investigation.
   */
  @Parameter(names = {"-s", "--startDate"}, description = "'Start date' of the investigation. Format: yyyy-MM-dd (e.g.: 2015-03-11)", required = false, converter = DateConverter.class, validateWith =  DateValidator.class)
  Date startDate = new Date();
  /**
   * End date of the investigation.
   */
  @Parameter(names = {"-e", "--endDate"}, description = "'End date' of the investigation. Format: yyyy-MM-dd (e.g.: 2015-03-11)", required = false, converter = DateConverter.class, validateWith =  DateValidator.class)
  Date endDate;
  /**
   * End date of the investigation.
   */
  @Parameter(names = {"-u", "--studyId"}, description = "'StudyId' of the investigation."
          + "If no Id is given interactive mode will be activated.", required = false)
  Long studyId;


 /**
   * Default constructor.
   */
  public InvestigationBuilder() {
    super("createinvestigation");
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
  public InvestigationBuilder topic(String pTopic) {
    topic = pTopic;
    return this;
  }

  /**
   * Set the description.
   *
   * @param pDescription description
   * @return instance of builder
   */
  public InvestigationBuilder description(String pDescription) {
    description = pDescription;
    return this;
  }

  /**
   * Set the note.
   *
   * @param pNote note
   * @return instance of builder
   */
  public InvestigationBuilder note(String pNote) {
    note = pNote;
    return this;
  }

  /**
   * Set the start date.
   *
   * @param pStartDate start date
   * @return instance of builder
   */
  public InvestigationBuilder startDate(String pStartDate) {
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
  public InvestigationBuilder endDate(String pEndDate) {
    try {
      endDate = sdf.parse(pEndDate);
    } catch (ParseException ex) {
      LOGGER.error(null, ex);
    }
    return this;
  }

  /**
   * Build investigation instance using set attributes.
   *
   * @return Instance of investigation.
   */
  public Investigation build() {
    Investigation investigation = new Investigation();
    investigation.setTopic(topic);
    investigation.setDescription(description);
    investigation.setNote(note);
    investigation.setStartDate(startDate);
    investigation.setEndDate(endDate);
    Study study = new Study();
    study.setStudyId(studyId);
    investigation.setStudy(study);
    return investigation;

  }

  @Override
  public CommandStatus executeCommand() {
    GenericSetupClient.setVerbose(verbose);
    return GenericSetupClient.executeCommand(this.build(), interactive);
  }

}
