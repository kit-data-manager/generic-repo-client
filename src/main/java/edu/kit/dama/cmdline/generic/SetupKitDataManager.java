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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.jcommander.generic.parameter.CommandLineParameters;
import edu.kit.dama.cmdline.generic.parameter.InitParameters;
import edu.kit.dama.rest.mdm.base.client.InvestigationBuilder;
import edu.kit.dama.rest.mdm.base.client.MetadataSchemaBuilder;
import edu.kit.dama.rest.mdm.base.client.OrganizationUnitBuilder;
import edu.kit.dama.rest.mdm.base.client.StudyBuilder;
import edu.kit.dama.rest.mdm.base.client.TaskBuilder;
import edu.kit.dama.rest.mdm.base.client.UpdateInvestigation;
import edu.kit.dama.rest.mdm.base.client.UpdateOrganizationUnit;
import edu.kit.dama.rest.mdm.base.client.UpdateStudy;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SetupKitDataManager {

  /**
   * List holding all commands available by jCommander.
   */
  static final List<CommandLineParameters> commands = new ArrayList<>();

  static {
    commands.add(new InitParameters());
    commands.add(new StudyBuilder());
    commands.add(new InvestigationBuilder());
    commands.add(new MetadataSchemaBuilder());
    commands.add(new OrganizationUnitBuilder());
    commands.add(new TaskBuilder());
    commands.add(new UpdateStudy());
    commands.add(new UpdateInvestigation());
    commands.add(new UpdateOrganizationUnit());
  }

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SetupKitDataManager.class);

  /**
   * Program parser and scheduler.
   *
   * @param args command line arguments.
   */
  public static void main(String[] args) {
    int returnValue = 0;
    JCommander jCommander = registerCommands();
    jCommander.setProgramName("setupKITDataManager");

    if (args.length == 0) {
      jCommander.usage();
    } else {
      try {
        jCommander.parse(args);

        String command = jCommander.getParsedCommand();
        CommandLineParameters clp = (CommandLineParameters) jCommander.getCommands().get(command).getObjects().get(0);

        if (clp.isHelp()) {
          printUsage(jCommander);
        } else {
          CommandStatus status = clp.executeCommand();
          returnValue = status.getStatusCode();
          System.out.println(status.getStatusMessage());
          if (!status.getStatus().isSuccess()) {
            Exception exception = status.getException();
            if (exception != null) {
              String message = exception.getMessage();
              if (message != null) {
                System.out.println(message);
              }
            }
          }

        }
      } catch (ParameterException pe) {
        System.err.println("Error parsing parameters!\nERROR -> " + pe.getMessage());
        printUsage(jCommander);
      }
    }
    System.exit(returnValue);
  }

  /**
   * Register all commands.
   *
   * @return Instance holding all commands.
   */
  private static JCommander registerCommands() {
    JCommander jCommander = new JCommander();
    for (CommandLineParameters clp : commands) {
      jCommander.addCommand(clp.getCommandName(), clp);
    }

    return jCommander;
  }

  /**
   * Print usage on STDOUT.
   *
   * @param jCommander instance holding parameters and descriptions.
   */
  private static void printUsage(JCommander jCommander) {
    String command = jCommander.getParsedCommand();
    if (command != null) {
      jCommander.usage(command);
    } else {
      jCommander.usage();
    }

  }
}
