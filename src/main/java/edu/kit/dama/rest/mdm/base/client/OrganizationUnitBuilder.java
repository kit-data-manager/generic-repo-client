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
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.client.setup.GenericSetupClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commandline parameters for the organization unit.
 * 
 * @author hartmann-v
 */
@Parameters(commandNames = "createorganizationunit", commandDescription = "Create an organization unit.")
public class OrganizationUnitBuilder extends SetupCommandLineParameters {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationUnitBuilder.class);
  /**
   * OUName of the organization unit.
   */
  @Parameter(names = {"-n", "--ouname"}, description = "'Organization unit name' of the organization unit.", required = false)
  String ouName = "Research center";
  /**
   * Address of the organization unit.
   */
  @Parameter(names = {"-a", "--address"}, description = "'Address' of the organization unit.", required = false)
  String address = "Einsteinstr. 1";
  /**
   * City of the organization unit.
   */
  @Parameter(names = {"-y", "--city"}, description = "'City' of the organization unit.", required = false)
  String city = "Albertville";
  /**
   * Zip code of the organization unit.
   */
  @Parameter(names = {"-z", "--zipcode"}, description = "'Zip code' of the organization unit.", required = false)
  String zipCode = "12345";
  /**
   * Country of the organization unit.
   */
  @Parameter(names = {"-c", "--country"}, description = "'Country' of the organization unit.", required = false)
  String country = "Germany";
  /**
   * Website of the organization unit.
   */
  @Parameter(names = {"-w", "--website"}, description = "'Website' of the organization unit.", required = false)
  String website = "http://www.example.edu";
  /**
   * Manager of the study.
   */
  @Parameter(names = {"-m", "--managerId"}, description = "'User Id' of the manager of the study. ", required = false)
  Long managerId = null;

 /**
   * Default constructor.
   */
  public OrganizationUnitBuilder() {
    super("createorganizationunit");
  }
  /**
   * Set the name.
   *
   * @param pOuName name of organization unit
   * @return instance of builder
   */
  public OrganizationUnitBuilder ouName(String pOuName) {
    ouName = pOuName;
    return this;
  }
  /**
   * Set the address.
   *
   * @param pAddress address of organization unit
   * @return instance of builder
   */
  public OrganizationUnitBuilder address(String pAddress) {
    address = pAddress;
    return this;
  }
  /**
   * Set the zip code.
   *
   * @param pZipCode zip code of organization unit
   * @return instance of builder
   */
  public OrganizationUnitBuilder zipCode(String pZipCode) {
    zipCode = pZipCode;
    return this;
  }
  /**
   * Set the city.
   *
   * @param pCity city of organization unit
   * @return instance of builder
   */
  public OrganizationUnitBuilder city(String pCity) {
    city = pCity;
    return this;
  }

  /**
   * Set the country.
   *
   * @param pCountry country of organization unit
   * @return instance of builder
   */
  public OrganizationUnitBuilder country(String pCountry) {
    country = pCountry;
    return this;
  }

  /**
   * Set the web site.
   *
   * @param pWebsite web site of organization unit
   * @return instance of builder
   */
  public OrganizationUnitBuilder website(String pWebsite) {
    website = pWebsite;
    return this;
  }

  /**
   * Set the manager id.
   *
   * @param pManagerId user id of the manager
   * @return instance of builder
   */
  public final OrganizationUnitBuilder managerId(Long pManagerId) {
    managerId = pManagerId;
    return this;
  }

  /**
   * Build investigation instance using set attributes.
   *
   * @return Instance of Organization Unit.
   */
  public OrganizationUnit build() {
    OrganizationUnit organization = new OrganizationUnit();
    organization.setOuName(ouName);
    organization.setAddress(address);
    organization.setCity(city);
    organization.setZipCode(zipCode);
    organization.setCountry(country);
    organization.setWebsite(website);
    if (managerId != null) {
      UserData user = new UserData();
      user.setUserId(managerId);
      organization.setManager(user);
    }
    
    return organization;

  }

  @Override
  public CommandStatus executeCommand() {
    GenericSetupClient.setVerbose(verbose);
    return GenericSetupClient.executeCommand(this.build(), interactive);
  }

}
