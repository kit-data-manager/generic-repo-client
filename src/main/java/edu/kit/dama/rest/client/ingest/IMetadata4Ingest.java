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
package edu.kit.dama.rest.client.ingest;

import edu.kit.dama.client.exception.BaseMetadataException;
import edu.kit.dama.mdm.base.DigitalObject;
import java.io.File;

/**
 * Interface for additional operations to be done during ingest before
 * <ol>
 * <li> the metadata of digital object will be registered at the repository
 * </li>
 * <li> the data of digital object will be transferred to the repository</li>
 * </ol>
 * This interface implements the 'inversion of control' pattern.
 *
 * @author hartmann-v
 */
public interface IMetadata4Ingest {

  /**
   * Possibility to modify metadata of digital object which is used for
   * registering dataset at repository (KIT Datamanager). The digital object is
   * partly filled with valid information but may be adapted on client side to
   * the specific needs of the client. Most important values may be:
   * <ul>
   * <li>Label</li>
   * <li>Note</li>
   * <li>Start date</li>
   * <li>End date</li>
   * </ul>
   *
   * @param pInputDirectory Directory of the data to ingest.
   * @param pDigitalObject Prefilled digital object.
   *
   * @return Modified Digital Object.
   * @throws BaseMetadataException Error during registration. Ingest will be
   * skipped.
   */
  DigitalObject modifyMetadata(File pInputDirectory, DigitalObject pDigitalObject) throws BaseMetadataException;

  /**
   * This method will be called after the metadata is registered but before the
   * data is already transfered to the repository.. Therefore the digital object
   * ID is already known. You may manage the digitalObjectId on your side.
   *
   * @param pInputDirectory Directory of the data to ingest.
   * @param pDigitalObjectId DigitalObjectId of the digital object which will be
   * ingested.
   *
   * @throws BaseMetadataException Error during registration. Ingest will be
   * skipped.
   */
  void preTransfer(File pInputDirectory, String pDigitalObjectId) throws BaseMetadataException;
}
