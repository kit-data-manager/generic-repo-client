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
import edu.kit.dama.rest.client.ingest.IMetadata4Ingest;
import java.awt.LayoutManager;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Example code for adding custom information via a graphical dialog to the 
 * metadata of the digital object.</p>
 *
 * <b>Implement Interface</b>
 * <p>
 * Create a new maven project with dependency to GenericRepoClient. Implement a
 * class implementing the IMetadata4Ingest interface. </p>
 *
 * <b>Register new plugin</b>
 * <p>
 * To register a new plugin the pom.xml has to be prepared like the
 * following:</p>
 *
 * <pre>
 * {@code
 * <plugin>
 *   <groupId>eu.somatik.serviceloader-maven-plugin</groupId>
 *   <artifactId>serviceloader-maven-plugin</artifactId>
 *   <version>1.0.7</version>
 *   <configuration>
 *     <services>
 *       <param>edu.kit.dama.rest.client.ingest.IMetadata4Ingest</param>
 *     </services>
 *   </configuration>
 *   <executions>
 *     <execution>
 *       <goals>
 *         <goal>generate</goal>
 *       </goals>
 *     </execution>
 *   </executions>
 * </plugin>
 * <dependencies>
 *   <dependency>
 *     <groupId>edu.kit.dama.rest</groupId>
 *     <artifactId>GenericRepoClient</artifactId>
 *     <version>1.5</version>
 *   </dependency>
 * </dependencies>
 * }
 * </pre> That’s it. Now you may build and add the jar file in the lib directory
 * and start GenericRepoClient.
 *
 * @author hartmann-v
 */
public class DialogPlugin4Ingest implements IMetadata4Ingest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DialogPlugin4Ingest.class);

  @Override
  public DigitalObject modifyMetadata(File pInputDir, DigitalObject pDigitalObject) throws BaseMetadataException {
    LOGGER.debug("Modify metadata before registering digital object at repository.");

    // <editor-fold defaultstate="collapsed" desc="Validate arguments">
    if ((pInputDir == null) || (pDigitalObject == null)) {
      throw new BaseMetadataException("Invalid arguments calling 'modifyMetadata'");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Defining dialog panel">
    JTextField labelField = new JTextField();
    JTextField noteField = new JTextField();
    labelField.setText(pDigitalObject.getLabel());
    noteField.setText(pDigitalObject.getNote());
    JPanel dialogPanel = new JPanel();
    LayoutManager layout = new BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS);
    dialogPanel.setLayout(layout);
    dialogPanel.add(new JLabel("Please input metadata for the digital object at '"
            + pInputDir.getAbsolutePath()
            + "' you want to ingest:"));
    dialogPanel.add(Box.createVerticalStrut(20)); // add some space
    dialogPanel.add(new JLabel("Title:"));
    dialogPanel.add(labelField);
    dialogPanel.add(Box.createVerticalStrut(15));
    dialogPanel.add(new JLabel("Note:"));
    dialogPanel.add(noteField);
    dialogPanel.add(Box.createVerticalStrut(10));
    // </editor-fold>

    int result = JOptionPane.showConfirmDialog(null, dialogPanel,
            "Please enter metadata", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      pDigitalObject.setNote(noteField.getText());
      pDigitalObject.setLabel(labelField.getText());
      LOGGER.debug("Set title -> '{}'\nnote -> '{}'", labelField.getText(), noteField.getText());
    } else {
      throw new BaseMetadataException("Ingest canceled by user!");
    }
    return pDigitalObject;
  }

  @Override
  public void preTransfer(File pInputDir, String pDigitalObjectId) throws BaseMetadataException {
    LOGGER.info("Digital object is registered with the following id: '{}'", pDigitalObjectId);
  }
}
