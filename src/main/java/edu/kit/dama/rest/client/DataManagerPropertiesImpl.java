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
package edu.kit.dama.rest.client;

import edu.kit.dama.util.StdIoUtils;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reading properties to access the KIT Data Manager via REST.
 * <h2>Properties:</h2>
 * <h3>Authentication</h3>
 * <i>If given values are empty or invalid a 'dialog' ask for the needed
 * values.</i>
 *
 * <ul>
 * <li>userId = login name of the user </li>
 * <li>accessKey = accesskey of the 'user' for the REST-services </li>
 * <li>accessSecret = accessSecret of the 'user' for the REST-services</li>
 * <li>group = group the ingest belongs to</li>
 * <li>investigation = investigation the ingest belongs to</li>
 * <li>URL = URL of the REST server</li>
 * <li>username = username for webDAV</li>
 * <li>password = password for webDAV</li>
 * <li>accessPoint = unique id of the access point</li>
 * </ul>
 *
 * @author hartmann-v
 */
public class DataManagerPropertiesImpl {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DataManagerPropertiesImpl.class);
  /**
   * separates sections from each other.
   */
  private static final String sectionSeparator = "################################################################################\n";
  /**
   * Header of the properties file.
   */
  private static final String HEADER = "# Settings for the ingest client using REST-API of KIT Data Manager.\n"
          + "# Settings should contain the following\n"
          + "# properties:\n"
          + sectionSeparator
          + "# REST server\n"
          + "# If given values are empty or invalid a 'dialog' ask for the needed values.\n"
          + sectionSeparator
          + "# - RestServer = URL of the REST server (e.g.: https://dama.lsdf.kit.edu/KITDM)\n"
          + "# - AccessPoint = Id of the transfer protocol (e.g.: ebc5a918-0c3c-4dbe-b8fe-babf50ac3459\n"
          + sectionSeparator
          + "# mapping to Group and Investigation\n"
          + "# If given values are invalid or empty a 'dialog' ask for the needed values.\n"
          + sectionSeparator
          + "# - group = Group to which the ingest belongs to. (e.g.: USERS)\n"
          + "# - investigation = investigation to which the ingest belongs to. (e.g.: 5)\n"
          + sectionSeparator
          + "# Authentication\n"
          + "# If given values are empty or invalid a 'dialog' ask for the needed values.\n"
          + sectionSeparator
          + "# - userId = login name of the user (e.g.: ab1234)\n"
          + "# - accessKey = accesskey of the 'user' for the REST-services\n"
          + "# - accessSecret = accessSecret of the 'user' for the REST-services\n"
          + sectionSeparator
          + "# Authentication for webDAV\n"
          + "# If given values are empty a 'dialog' ask for the needed values.\n"
          + sectionSeparator
          + "# - Username = login name of the user (e.g.: webdav)\n"
          + "# - Password = password of the 'user' for webDAV (e.g.: test123)\n"
          + sectionSeparator;

  /**
   * file name of the properties file containing all properties needed for
   * ingest via KIT Data Manager.
   */
  private static final String HOME_DIR = System.getProperty("user.home") + File.separator + ".repoClient";
  /**
   * file name of the properties file containing all properties needed for
   * ingest via KIT Data Manager.
   */
  private static final String PROPERTIES_FILE = "RepoSettings.properties";
  /**
   * path of the credential file in the classpath.
   */
  private static final String CREDENTIAL_RESOURCE = "/edu/kit/dama/rest/" + PROPERTIES_FILE;
  /**
   * System environment variable pointing to the credential property file.
   */
  private static final String REPO_SETTINGS = "REPO_SETTINGS";
  /**
   * Stream to write to.
   */
  private static final PrintStream output = System.out;
  /**
   * name of the file.
   */
  private String propertiesFile;
  /**
   * Instance holding all properties.
   */
  private Properties properties;
  /**
   * Singleton reading properties from
   */
  private static DataManagerPropertiesImpl singletonImpl = new DataManagerPropertiesImpl();

  static {
    // Test for directory
    File homeDir = new File(HOME_DIR);

    if (!homeDir.exists()) {
      if (!homeDir.mkdir()) {
        LOGGER.error("Can't create directory '{}'!", HOME_DIR);
      } else {
        LOGGER.debug("Directory '{}' created!", HOME_DIR);
      }
    }
  }

  /**
   * Read properties from file 'DatabaseArchiver.properties'.
   *
   * @see PROPERTIES_FILE
   */
  private DataManagerPropertiesImpl() {
    this(HOME_DIR + File.separator + PROPERTIES_FILE);
  }

  /**
   * Read properties from given file.
   *
   * @param pPropertiesFile file which contains the properties.
   */
  private DataManagerPropertiesImpl(String pPropertiesFile) {
    propertiesFile = pPropertiesFile;
    properties = loadProperties();
  }

  /**
   * Get the default instance of this class.
   *
   * @return instance of this class.
   */
  public static DataManagerPropertiesImpl getDefaultInstance() {
    return singletonImpl;
  }

  /**
   * Get an instance of this class. Properties were loaded form given file (if
   * file exists).
   *
   * @param pPropertiesFile File name of the properties file.
   * @return instance of this class.
   */
  public static DataManagerPropertiesImpl getInstance(String pPropertiesFile) {
    return new DataManagerPropertiesImpl(pPropertiesFile);
  }

  /**
   * Read property from StdIn.
   *
   * @param pProperty label of the property.
   * @param pDescription Description of the property.
   * @return new value of the given property.
   */
  private String readPropertyFromStdInput(final String pProperty, final String pDescription) {
    String newValue = properties.getProperty(pProperty);
    output.format("Please input new value for '%s' (%s)!\n", pProperty, pDescription);
    output.format("Return will sustain old value '%s':\n", properties.getProperty(pProperty));
    newValue = StdIoUtils.readStdInput(newValue);
    properties.put(pProperty, newValue);
    return newValue;
  }

  /**
   * Read property from StdIn. If there is a limited number of possibility it
   * has to be chosen from the given list via the appropriate number.
   *
   * @param pProperty label of the property.
   * @param pPossibleValues list of values. NULL if any value is allowed.
   * @param pDescriptions list of descriptions. NULL if no description
   * available.
   * @return index of the new value of the given property.
   */
  private int readPropertyFromStdInput(final String pProperty, final String[] pPossibleValues, final String[] pDescriptions) {
    String actualValue = properties.getProperty(pProperty);
    String formatString = "%3d: %-12s ";
    int oldIndex = Arrays.asList(pPossibleValues).indexOf(actualValue) + 1;
    output.format("Please input new value for '%s'!\n", pProperty);
    if (oldIndex > 0) {
      output.format("Return will retain old value '%s':\n", actualValue);
    } else {
      output.format("Old value '%s' is no longer(?) valid!\n", actualValue);
    }
    if (pPossibleValues != null) {
      int index = 1;
      output.println("Please choose via given index.");
      for (int pVIndex = 0; pVIndex < pPossibleValues.length; pVIndex++) {
        String adaptedFormatString = formatString;
        if (pDescriptions != null) {
          if (pDescriptions[pVIndex] != null) {
            adaptedFormatString = adaptedFormatString + "- %s ";
          }
        }
        if (oldIndex == index) {
          adaptedFormatString = "*" + adaptedFormatString + "*";
        }
        output.println(String.format(adaptedFormatString, index++, pPossibleValues[pVIndex], pDescriptions[pVIndex]));
      }
    }
    int index = -1;
    if (pPossibleValues != null) {
      index = StdIoUtils.readIntFromStdInput(pPossibleValues.length, oldIndex) - 1;
      actualValue = pPossibleValues[index];

    } else {
      actualValue = StdIoUtils.readStdInput(actualValue);
    }
    properties.put(pProperty, actualValue);
    return index;
  }

  /**
   * Read value from stdin for given properties. Prints a summarize at the end.
   *
   * @param pProperty Property.
   * @param pFormatString String for summarizing.
   * @return If there are any changes.
   */
  public final boolean readProperty(DataManagerProperties pProperty, String pFormatString) {
    return readProperty(new DataManagerProperties[]{pProperty}, pFormatString);
  }

  /**
   * Read value from stdin for given properties. Prints a summarize at the end.
   *
   * @param pProperties Array holding all properties.
   * @param pFormatString String for summarizing entries.
   * @return If there are any changes.
   */
  public final boolean readProperty(DataManagerProperties[] pProperties, String pFormatString) {
    boolean propertyChanged = false;
    for (DataManagerProperties item : pProperties) {
      String lastValue = getPropertyValue(item);
      String newValue = readPropertyFromStdInput(item.getKey(), item.getDescription());
      if (!lastValue.equals(newValue)) {
        propertyChanged = true;
      }

    }
    Object[] newValueArray = new Object[pProperties.length];
    for (int index = 0; index < newValueArray.length; index++) {
      newValueArray[index] = getPropertyValue(pProperties[index]);

    }

    StdIoUtils.printSummary(output, String.format(pFormatString, newValueArray));
    return propertyChanged;
  }

  /**
   * Read value from stdin for given properties. As there is a limited number of
   * possibilities the property has to be chosen from the given list via the
   * appropriate number.
   *
   * Prints a summarize at the end.
   *
   * @param pProperty Property.
   * @param pValues Possible values for the property.
   * @param pFormatString String for summarizing entries.
   * @return If there are any changes.
   */
  public final boolean readPropertyIndex(DataManagerProperties pProperty, String[] pValues, String pFormatString) {
    return readPropertyIndex(pProperty, pValues, null, pFormatString);
  }

  /**
   * Read value from stdin for given properties. Prints a summarize at the end.
   *
   * @param pProperty Property.
   * @param pValues Possible values for the property.
   * @param pDescriptions Descriptions of the values.
   * @param pFormatString String for summarizing entries.
   * @return If there are any changes.
   */
  public final boolean readPropertyIndex(DataManagerProperties pProperty, String[] pValues, String[] pDescriptions, String pFormatString) {
    boolean propertyChanged = false;
    String lastValue = getPropertyValue(pProperty);
    int index = readPropertyFromStdInput(pProperty.getKey(), pValues, pDescriptions);
    StdIoUtils.printSummary(output, String.format(pFormatString, pValues[index], pDescriptions[index]));
    if (!lastValue.equals(pValues[index])) {
      propertyChanged = true;
    }
    return propertyChanged;
  }

  /**
   * Save properties to properties file.
   */
  public final void saveProperties() {
    FileWriter fw = null;
    if (propertiesFile != null) {
      try {
        fw = new FileWriter(propertiesFile);
        properties.store(fw, HEADER);
      } catch (IOException ex) {
        LOGGER.error("Error writing properties to " + propertiesFile, ex);
      } finally {
        try {
          if (fw != null) {
            fw.close();
          }
        } catch (IOException ex) {
          LOGGER.error(null, ex);
        }
      }
    }
  }

  // <editor-fold defaultstate="collapsed" desc="Getters">
  /**
   * Get the login name of the user.
   *
   * @return the userId
   */
  public final String getUserId() {
    return getPropertyValue(DataManagerProperties.USER_ID);
  }

  /**
   * Get the accessKey of the user.
   *
   * @return the accessKey
   */
  public final String getAccessKey() {
    return getPropertyValue(DataManagerProperties.ACCESS_KEY);
  }

  /**
   * Get the accessSecret of the user.
   *
   * @return the accessSecret
   */
  public final String getAccessSecret() {
    return getPropertyValue(DataManagerProperties.ACCESS_SECRET);
  }

  /**
   * Get the URL of the REST server. This URL is the root URL of all REST
   * services of the KIT Data Manager. Trailing forward slashes will be removed.
   *
   * @return the URL of the REST server.
   */
  public final String getRestUrl() {
    String restUrl = getPropertyValue(DataManagerProperties.REST_SERVER_LABEL);
    while (restUrl.endsWith("/")) {
      restUrl = restUrl.substring(0, restUrl.length() - 1);
      properties.setProperty(DataManagerProperties.REST_SERVER_LABEL.getKey(), restUrl);
    }
    return getPropertyValue(DataManagerProperties.REST_SERVER_LABEL);
  }

  /**
   * Get the accessPoint of the transfer protocol.
   *
   * @return accessPoint of the transfer protocol.
   */
  public final String getAccessPoint() {
    return getPropertyValue(DataManagerProperties.ACCESS_POINT_LABEL);
  }

  /**
   * Get the investigation which the actions should belong to.
   *
   * @return the investigation
   */
  public final String getInvestigation() {
    return getPropertyValue(DataManagerProperties.INVESTIGATION);
  }

  /**
   * Get the group of the user which the actions belong to.
   *
   * @return the userGroup
   */
  public final String getUserGroup() {
    return getPropertyValue(DataManagerProperties.USER_GROUP);
  }

  /**
   * Get the user name for webDAV access.
   *
   * @return user name for webDAV access.
   */
  public final String getUserName() {
    return getPropertyValue(DataManagerProperties.USERNAME);
  }

  /**
   * Get the password for webDAV access.
   *
   * @return the password for webDAV access.
   */
  public final String getPassword() {
    return getPropertyValue(DataManagerProperties.PASSWORD);
  }

  /**
   * Get the value of the given property.
   *
   * @param pProperty Property.
   *
   * @return the value of the given property
   */
  public final String getPropertyValue(DataManagerProperties pProperty) {
    return properties.getProperty(pProperty.getKey());
  }
// </editor-fold>

  @Override
  public String toString() {
    StringWriter sw;
    String returnValue = "";
    try {
      sw = new StringWriter();
      properties.store(sw, HEADER);
      returnValue = sw.toString();
    } catch (IOException ex) {
      LOGGER.error(null, ex);
    }
    return returnValue;
  }

  /**
   * Read new settings based on the following initialization order:<BR/>
   * <ul>
   * <li>Check the environment variable '{@value #REPO_SETTINGS}'</li>
   * <li>Search for credentials.properties in current directory</li>
   * <li>Search resource credentials.properties</li>
   * </ul>
   * All found settings will be loaded in reverse order. Last input will
   * overwrite the previous ones.
   *
   * @return properties instance holding all properties.\
   * @see #REPO_SETTINGS
   */
  private Properties loadProperties() {
    Properties newProperties = new Properties();
    // look in classpath
    URL resourceURL = DataManagerPropertiesImpl.class.getResource(CREDENTIAL_RESOURCE);
    if (resourceURL != null) {
      try (InputStream inputStream = DataManagerPropertiesImpl.class.getResourceAsStream(CREDENTIAL_RESOURCE)) {
        newProperties.load(inputStream);
        LOGGER.debug("Load ingest properties from resource '{}'!", CREDENTIAL_RESOURCE);
      } catch (Exception e) {
        LOGGER.error("Failed to read LSDF server settings from resource", e);
      }
    }
    // look in file system 
    // if there is a file properties from resource will be overwritten.
    try (FileReader fileReader = new FileReader(propertiesFile)) {
      newProperties.load(fileReader);
      LOGGER.debug("Load ingest properties from file '{}'!", propertiesFile);
    } catch (IOException ex) {
      LOGGER.error(null, ex);
    } catch (Exception npex) {
      LOGGER.error(null, npex);
    }
    // check for content of environment variable
    // if available content of file system will be overwritten.
    String settingsFileByEnvVar = System.getenv(REPO_SETTINGS);
    if (settingsFileByEnvVar == null) {
      //try -D argument
      settingsFileByEnvVar = System.getProperty(REPO_SETTINGS);
      LOGGER.debug("Found environment variable '{}': '{}'", REPO_SETTINGS, settingsFileByEnvVar);
    } else {
      LOGGER.debug("Found system variable '{}': '{}'", REPO_SETTINGS, settingsFileByEnvVar);
    }
    if (settingsFileByEnvVar != null) {
      if (!settingsFileByEnvVar.isEmpty()) {
        File iniFile = new File(settingsFileByEnvVar);
        if (iniFile.exists()) {
          LOGGER.debug("Load ingest properties from file '{}'!", settingsFileByEnvVar);
          try (FileReader fileReader = new FileReader(iniFile)) {
            newProperties.load(fileReader);
          } catch (IOException ioe) {
            // do nothing.
            LOGGER.error(null, ioe);
          }
        }
        if (propertiesFile != null) {
          propertiesFile = settingsFileByEnvVar;
        }
      }
    }
    return newProperties;
  }

  /**
   * Override settings from file by other values.
   */
  public class PropertiesBuilder {

    /**
     * Holding the default values of the KIT Data Manager.
     */
    DataManagerPropertiesImpl dmpi;
    /**
     * Collecting the new properties.
     */
    Properties properties;

    public PropertiesBuilder() {
      dmpi = new DataManagerPropertiesImpl();
      properties = dmpi.properties;

    }

    public PropertiesBuilder accessKey(String pAccessKey) {
      properties.put(DataManagerProperties.ACCESS_KEY, pAccessKey);
      return this;
    }

    public PropertiesBuilder accessSecret(String pAccessSecret) {
      properties.put(DataManagerProperties.ACCESS_SECRET, pAccessSecret);
      return this;
    }

    public PropertiesBuilder accessPointLabel(String pAccesssPointLabel) {
      properties.put(DataManagerProperties.ACCESS_POINT_LABEL, pAccesssPointLabel);
      return this;
    }

    public PropertiesBuilder investigation(String pInvestigation) {
      properties.put(DataManagerProperties.INVESTIGATION, pInvestigation);
      return this;
    }

    public PropertiesBuilder password(String pPassword) {
      properties.put(DataManagerProperties.PASSWORD, pPassword);
      return this;
    }

    public PropertiesBuilder restServerLabel(String pRestServerLabel) {
      properties.put(DataManagerProperties.REST_SERVER_LABEL, pRestServerLabel);
      return this;
    }

    public PropertiesBuilder username(String pUsername) {
      properties.put(DataManagerProperties.USERNAME, pUsername);
      return this;
    }

    public PropertiesBuilder userGroup(String pUserGroup) {
      properties.put(DataManagerProperties.USER_GROUP, pUserGroup);
      return this;
    }

    public PropertiesBuilder userId(String pUserId) {
      properties.put(DataManagerProperties.USER_ID, pUserId);
      return this;
    }

    public DataManagerPropertiesImpl build() {
      // Disable storing properties to file.
      dmpi.propertiesFile = null;
      return singletonImpl;
    }
  }

}
