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
package edu.kit.dama.cmdline.generic;

import edu.kit.dama.cmdline.generic.parameter.InitParameters;
import edu.kit.dama.rest.mdm.base.client.InvestigationBuilder;
import edu.kit.dama.rest.mdm.base.client.MetadataSchemaBuilder;
import edu.kit.dama.rest.mdm.base.client.OrganizationUnitBuilder;
import edu.kit.dama.rest.mdm.base.client.StudyBuilder;
import edu.kit.dama.rest.mdm.base.client.TaskBuilder;
import edu.kit.dama.rest.mdm.base.client.UpdateInvestigation;
import edu.kit.dama.rest.mdm.base.client.UpdateOrganizationUnit;
import edu.kit.dama.rest.mdm.base.client.UpdateStudy;
import java.io.File;

/**
 * Main class holding all commands available for this project. This class is the
 * starting point for some commandline commands. See pom.xml for details.
 * <p>
 * <b>Implemented commands:</b></p>
 * <ul>
 * <li>init</li>
 * <li>createstudy</li>
 * <li>createinvestigation</li>
 * <li>createmetadataschema</li>
 * <li>createorganization</li>
 * <li>createtask</li>
 * <li>updatestudy</li>
 * <li>updateinvestigation</li>
 * <li>updateorganization</li>
 * </ul>
 *
 * @see InitParameters
 * @see StudyBuilder
 * @see InvestigationBuilder
 * @see MetadataSchemaBuilder
 * @see OrganizationUnitBuilder
 * @see TaskBuilder
 * @see UpdateStudy
 * @see UpdateInvestigation
 * @see UpdateOrganizationUnit
 * @author hartmann-v
 */
public class SetupKitDataManager extends BasicExecuter {

  /**
   * Program parser and scheduler.
   *
   * @param args command line arguments.
   */
  public static void main(String[] args) {
    programName = "bin" + File.separator + "setupRepo";

    // <editor-fold defaultstate="collapsed" desc="Initialize commands">
    commands.clear();
    commands.add(new InitParameters());
    commands.add(new StudyBuilder());
    commands.add(new InvestigationBuilder());
    commands.add(new MetadataSchemaBuilder());
    commands.add(new OrganizationUnitBuilder());
    commands.add(new TaskBuilder());
    commands.add(new UpdateStudy());
    commands.add(new UpdateInvestigation());
    commands.add(new UpdateOrganizationUnit());
    // </editor-fold>

    executeCommand(args);
  }
}
