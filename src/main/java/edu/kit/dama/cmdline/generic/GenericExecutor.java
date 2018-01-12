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

import edu.kit.dama.cmdline.generic.parameter.AccessParameters;
import edu.kit.dama.cmdline.generic.parameter.IngestParameters;
import edu.kit.dama.cmdline.generic.parameter.ListParameters;
import edu.kit.dama.cmdline.generic.parameter.SearchParameters;
import java.io.File;

/**
 * Main class holding all commands available for this project. This class is the
 * starting point for some commandline commands. See pom.xml for details.
 * <p>
 * <b>Implemented commands:</b></p>
 * <ul>
 * <li>ingest</li>
 * <li>list</li>
 * <li>access</li>
 * </ul>
 *
 * @see IngestParameters
 * @see ListParameters
 * @see AccessParameters
 * @author hartmann-v
 */
public class GenericExecutor extends BasicExecuter {

  /**
   * Program parser and scheduler.
   *
   * @param args command line arguments.
   */
  public static void main(String[] args) {
    programName = "bin" + File.separator + "repoClient";

    // <editor-fold defaultstate="collapsed" desc="Initialize commands">
    commands.clear();
    commands.add(new IngestParameters());
    commands.add(new ListParameters());
    commands.add(new AccessParameters());
    commands.add(new SearchParameters());
    // </editor-fold>

    executeCommand(args);
  }

}
