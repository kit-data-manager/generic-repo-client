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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for commandline commands.
 *
 * @author hartmann-v
 */
public class BasicExecuter {

  /**
   * List holding all commands available by jCommander.
   */
  protected static final List<CommandLineParameters> commands = new ArrayList<>();

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(BasicExecuter.class);

  protected static String programName;

  /**
   * Program parser and scheduler.
   *
   * @param args command line arguments.
   */
  public static void executeCommand(String[] args) {
    int returnValue = 0;
    JCommander jCommander = registerCommands();
    jCommander.setProgramName(programName);

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
          CommandStatus status;
          try {
            status = clp.executeCommand();
          } catch (Exception e) {
            // if there are any uncatched errors catch them here.
            LOGGER.error("Error executing command '" + command + "'!", e);
            status = new CommandStatus(e);
          }
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
  protected static JCommander registerCommands() {
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
  protected static void printUsage(JCommander jCommander) {
    String command = jCommander.getParsedCommand();
    if (command != null) {
      jCommander.usage(command);
    } else {
      jCommander.usage();
    }

  }
}
