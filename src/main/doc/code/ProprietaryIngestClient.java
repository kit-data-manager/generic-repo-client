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

import edu.kit.dama.client.exception.BaseMetadataException;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.rest.client.ingest.GenericIngestClient;
import edu.kit.dama.rest.client.ingest.IMetadata4Ingest;
import edu.kit.jcommander.generic.status.CommandStatus;
import java.io.File;
import javax.swing.JFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example code adding proprietary information to the metadata of the digital
 * object.
 *
 * @author hartmann-v
 */
public class ProprietaryIngestClient implements IMetadata4Ingest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProprietaryIngestClient.class);
  /**
   * Digital Object ID (DOID) of the data ingested to the repository. The DOID
   * will be determined during the registration process.
   */
  String digitalObjectId;

  /**
   * Example code for ingesting a directory to repository with some proprietary
   * changes during ingest. Before executing this code you should execute the
   * following program: bin/setupRepo init -a At the end please save the
   * settings. When you finished the program successfully your settings will be
   * stored at: HomeDir/.repoClient/RepoSettings.xml Once the settings stored
   * the ingest should work without further changes.
   *
   * @param args
   */
  public static void main(String[] args) {
    int returnValue = 0;
    // Create instance to enable modifications during ingest preparatation.
    ProprietaryIngestClient metadata4Ingest = new ProprietaryIngestClient();

    // Open file chooser to select directory.
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new java.io.File("."));
    chooser.setDialogTitle("Select directory");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      LOGGER.debug("Start ingest of data!");
      // Ingest selected directory
      // Modification will take place during execution of the ingest.
      CommandStatus status = GenericIngestClient.ingestData(chooser.getCurrentDirectory(), "Any note.", metadata4Ingest);

      returnValue = status.getStatusCode();
      if (!status.getStatus().isSuccess()) {
        Exception exception = status.getException();
        if (exception != null) {
          String message = exception.getMessage();
          LOGGER.error(message, exception);
        }
      } else {
        LOGGER.info("Digital object ingested with digital object id: '{}'", metadata4Ingest.digitalObjectId);
      }
      LOGGER.info("Status code: '{}'", returnValue);
      LOGGER.info(status.getStatusMessage());
    }
  }

  @Override
  public DigitalObject modifyMetadata(File pInputDir, DigitalObject pDigitalObject) throws BaseMetadataException {
    LOGGER.debug("Modify metadata before registering digital object at repository.");
    pDigitalObject.setLabel("Set a specific label");
    return pDigitalObject;
  }

  @Override
  public void preTransfer(File pInputDir, String pDigitalObjectId) throws BaseMetadataException {
    LOGGER.info("Digital object is registered with the following id: '{}'", pDigitalObjectId);
    digitalObjectId = pDigitalObjectId;
  }

}
