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
package edu.kit.dama.cmdline.generic.parameter;

import edu.kit.jcommander.generic.parameter.CommandLineParameters;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import edu.kit.jcommander.generic.status.CommandStatus;
import edu.kit.dama.rest.client.DataManagerPropertiesHelper;
import java.util.EnumSet;

/**
 * Class holding the commandline parameters. This class contains the parameters
 * needed for ingest local data to the repository.
 *
 * @author hartmann-v
 */
@Parameters(commandNames = "init", commandDescription = "Initialize ingest settings for KIT Data Manager.")
public class InitParameters extends CommandLineParameters {

  /**
   * All available commandline flags. If defaultFlag is true the linked
   * properties will be queried when no flag is chosen.
   */
  public static enum CommandLineFlags {

    /**
     * REST URL.
     */
    DATA_MANAGER_BASE(true),
    /**
     * group and investigation.
     */
    DATA_MANAGER_CONTEXT(true),
    /**
     * User id, access key and secret.
     */
    REST_AUTHENTICATION(true),
    /**
     * AcessPoint
     */
    ACCESSPOINT(true),
    /**
     * WebDAV credentials.
     */
    WEBDAV(true),
    /**
     * test settings.
     */
    TEST_ONLY(false);

    /**
     * Should be a default flag.
     */
    private final boolean defaultFlag;

    /**
     * Constructor.
     *
     * @param pDefault is a default flag or not.
     */
    CommandLineFlags(boolean pDefault) {
      this.defaultFlag = pDefault;
    }

    /**
     * Is default flag or not.
     *
     * @return default or not.
     */
    public boolean isDefaultFlag() {
      return defaultFlag;
    }

  }

  /**
   * Parameter for flags.
   */
  @Parameter(names = {"-a", "--all"}, description = "Initialize all (overwrites all other flags).", required = false)
  private boolean all = false;

  /**
   * Parameter for flags.
   */
  @Parameter(names = {"-b", "--base"}, description = "Initialize base settings for KIT DM (URL).", required = false)
  private boolean base = false;

  /**
   * Parameter for flags.
   */
  @Parameter(names = {"-r", "--rest"}, description = "Initialize REST authentication settings for KIT DM (accessKey, accessSecret, userId).", required = false)
  private boolean rest = false;

  /**
   * Parameter for flags.
   */
  @Parameter(names = {"-c", "--context"}, description = "Initialize context settings for KIT DM (group, investigation).", required = false)
  private boolean context = false;

  /**
   * Parameter for flags.
   */
  @Parameter(names = {"-t", "--transfer"}, description = "Initialize accesspoint inclusive authentication for webDAV (username, password)", required = false)
  private boolean webDav = false;

  /**
   * Parameter for flags.
   */
  @Parameter(names = {"-d", "--dryRun"}, description = "Don't query values. Only test selected settings.", required = false)
  private boolean testOnly = false;

  /**
   * Default constructor.
   */
  public InitParameters() {
    super("init");
  }

  /**
   * Get all Flags set by command line arguments. If no flag is set use default
   * flags.
   *
   * @return all marked flags.
   */
  public EnumSet<CommandLineFlags> getFlags() {
    EnumSet<CommandLineFlags> returnValue = EnumSet.noneOf(CommandLineFlags.class);
    if (all) { // all overwrites all other flags except test.
      returnValue = EnumSet.allOf(CommandLineFlags.class);
      returnValue.remove(CommandLineFlags.TEST_ONLY);
    } else {
      // check single flags.
      if (base) {
        returnValue.add(CommandLineFlags.DATA_MANAGER_BASE);
      }
      if (context) {
        returnValue.add(CommandLineFlags.DATA_MANAGER_CONTEXT);
      }
      if (rest) {
        returnValue.add(CommandLineFlags.REST_AUTHENTICATION);
      }
      if (webDav) {
        returnValue.add(CommandLineFlags.WEBDAV);
        returnValue.add(CommandLineFlags.ACCESSPOINT);
      }
      // fallback if no flags are selected
      if (returnValue.isEmpty()) {
        returnValue = getDefaultFlags();
      }

    }
    if (testOnly) {
      returnValue.add(CommandLineFlags.TEST_ONLY);
    }
    return returnValue;
  }

  /**
   * Get all flags marked as default.
   *
   * @return enumset containing all flags marked as default.
   */
  public static EnumSet<CommandLineFlags> getDefaultFlags() {
    EnumSet<CommandLineFlags> returnValue = EnumSet.noneOf(CommandLineFlags.class);
    for (CommandLineFlags item : CommandLineFlags.values()) {
      if (item.isDefaultFlag()) {
        returnValue.add(item);
      }
    }
    return returnValue;
  }

  @Override
  public CommandStatus executeCommand() {
    return DataManagerPropertiesHelper.executeCommand(this);
  }
}
