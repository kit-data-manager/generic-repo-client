/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.client.example;

import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.rest.client.access.GenericAccessClient;
import edu.kit.jcommander.generic.status.CommandStatus;
import javax.swing.JFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example code downloading digital object from repository.
 *
 * @author hartmann-v
 */
public class ProprietaryAccessClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProprietaryAccessClient.class);

  /**
   * Example code to download a directory from repository. Before executing
   * this code you should execute the following program: 
   * bin/setupRepo init -a
   * At the end please save the settings. When you finished the program
   * successfully your settings will be stored at:
   * HomeDir/.repoClient/RepoSettings.xml Once the settings stored the ingest
   * should work without further changes.
   *
   * @param args
   */
  public static void main(String[] args) {
    int returnValue = 0;
    /**
     * Digital Object ID (DOID) of the data ingested to the repository. The DOID
     * will be determined during the registration process.
     */
    String digitalObjectId = "pleaseInsertAValidIdAccessibleWithGivenContext";

    // Open file chooser to select directory.
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new java.io.File("."));
    chooser.setDialogTitle("Select directory");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      LOGGER.debug("Start download of data!");
      // Download to selected directory
      // Warning: Existing files may be overwritten.
      CommandStatus status = GenericAccessClient.accessData(chooser.getCurrentDirectory(), digitalObjectId);

      returnValue = status.getStatusCode();
      if (!status.getStatus().isSuccess()) {
        Exception exception = status.getException();
        if (exception != null) {
          String message = exception.getMessage();
          LOGGER.error(message, exception);
        }
      } else {
        LOGGER.info("Digital object downloaded with digital object id: '{}'", digitalObjectId);
      }
      LOGGER.info("Status code: '{}'", returnValue);
      LOGGER.info(status.getStatusMessage());
    }
  }
}
