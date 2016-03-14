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
package edu.kit.dama.rest.client.setup;

import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Relation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.UserData;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author hartmann-v
 */
public final class PrintUtil {

  /**
   * Parse date to 'yyyy-MM-dd'.
   */
  final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  /**
   * Print to stdout!
   */
  final static PrintStream output = System.out;

  /**
   * Print instance to printstream. Only new
   *
   * @param pStudy Study to print.
   * @param pRecursive Print also containing objects.
   */
  public final static void printStudy(Study pStudy, boolean pRecursive) {
    output.println("Study:\n------");
    output.println("     Topic: " + pStudy.getTopic());
    output.println("      Note: " + pStudy.getNote());
    output.println("Legal Note: " + pStudy.getLegalNote());
    UserData manager = pStudy.getManager();
    if (manager != null) {
      output.format("   Manager: %s (%s) - %s\n", manager.getFullname(), manager.getDistinguishedName(), manager.getEmail());
    }
    Date date = pStudy.getStartDate();
    if (date != null) {
      output.println("Start Date:" + sdf.format(date));
    }
    date = pStudy.getEndDate();
    if (date != null) {
      output.println("  End Date:" + sdf.format(date));
    }
    if (pRecursive) {
      if (pStudy.getInvestigations() != null) {
        if (pStudy.getInvestigations().size() > 0) {
          output.println("Investigations:\n--------------");
          for (Investigation item : pStudy.getInvestigations()) {
            printInvestigation(item, pRecursive);
          }
        }
      }
      if (pStudy.getOrganizationUnits() != null) {
        if (pStudy.getOrganizationUnits().size() > 0) {
          output.println("Organization Units:\n------------------");
          for (Relation item : pStudy.getOrganizationUnits()) {
            printRelation(item);
          }
        }
      }
    }
  }

  /**
   * Print instance to stdout.
   *
   * @param pInvestigation Investigation to print.
   * @param pRecursive Print also containing objects.
   */
  public final static void printInvestigation(Investigation pInvestigation, boolean pRecursive) {
    output.println("Investigation:\n-------------");
    output.println("      Topic: " + pInvestigation.getTopic());
    output.println("       Note: " + pInvestigation.getNote());
    output.println("Description: " + pInvestigation.getDescription());
    Date date = pInvestigation.getStartDate();
    if (date != null) {
      output.println("Start Date:" + sdf.format(date));
    }
    date = pInvestigation.getEndDate();
    if (date != null) {
      output.println("  End Date:" + sdf.format(date));
    }
    if (pRecursive) {
      if (pInvestigation.getParticipants() != null) {
        if (pInvestigation.getParticipants().size() > 0) {
          output.println("Participants:\n--------------");
          for (Participant item : pInvestigation.getParticipants()) {
            printParticipant(item);
          }
        }
      }
      if (pInvestigation.getMetaDataSchema() != null) {
        if (pInvestigation.getMetaDataSchema().size() > 0) {
          output.println("MetadataSchemas:\n----------------");
          for (MetaDataSchema item : pInvestigation.getMetaDataSchema()) {
            printMetadataSchema(item);
          }
        }
      }
    }
  }
    /**
     * Print instance to stdout.
     *
     * @param pParticipant Participant to print.
     */
  public final static void printParticipant(Participant pParticipant) {
    output.println("Participant:\n--------------");
    if (pParticipant.getUser() == null) {
      output.println("ID: #" + pParticipant.getParticipantId());
    } else {
      printUser(pParticipant.getUser());
      printTask(pParticipant.getTask());
    }
  }

    /**
     * Print instance to stdout.
     *
     * @param pRelation Participant to print.
     */
  public final static void printRelation(Relation pRelation) {
    output.println("Relation:\n--------------");
    if (pRelation.getOrganizationUnit() == null) {
      output.println("ID: #" + pRelation.getRelationId());
    } else {
      printOrganizationUnit(pRelation.getOrganizationUnit());
      printTask(pRelation.getTask());
    }
  }

  /**
   * Print instance to stdout.
   *
   * @param pUser Participant to print.
   */
  public final static void printUser(UserData pUser) {
    if (pUser != null) {
      output.print("User: ");
      if (pUser.getDistinguishedName() == null) {
        output.println("ID: #" + pUser.getUserId());
      } else {
        output.format("%s (%s) - %s\n", pUser.getFullname(), pUser.getDistinguishedName(), pUser.getEmail());
      }
    }
  }

  /**
   * Print instance to stdout.
   *
   * @param pTask Participant to print.
   */
  public final static void printTask(Task pTask) {
    if (pTask != null) {
      output.print("Task: ");
      if (pTask.getTask() == null) {
        output.println("ID: #" + pTask.getTaskId());
      } else {
        output.format("%s\n", pTask.getTask());
      }
    }
  }

  /**
   * Print instance to stdout.
   *
   * @param pMetadataSchema Participant to print.
   */
  public final static void printMetadataSchema(MetaDataSchema pMetadataSchema) {
    if (pMetadataSchema != null) {
      output.print("Metadata Schema: ");
      if (pMetadataSchema.getMetaDataSchemaUrl() == null) {
        output.println("ID: #" + pMetadataSchema.getId());
      } else {
        output.format("%s - %s\n", pMetadataSchema.getSchemaIdentifier(), pMetadataSchema.getMetaDataSchemaUrl());
      }
    }
  }

  /**
   * Print instance to stdout.
   *
   * @param pOrganizationUnit Participant to print.
   */
  public final static void printOrganizationUnit(OrganizationUnit pOrganizationUnit) {
    if (pOrganizationUnit != null) {
      output.println("Organization Unit: ");
      if (pOrganizationUnit.getOuName() == null) {
        output.println("ID: #" + pOrganizationUnit.getOrganizationUnitId());
      } else {
        output.format("%s, %s, %s %s, %s %s\n", pOrganizationUnit.getOuName(),
                pOrganizationUnit.getAddress(),
                pOrganizationUnit.getZipCode(),
                pOrganizationUnit.getCity(),
                pOrganizationUnit.getCountry(),
                pOrganizationUnit.getWebsite());
        output.println("Manager:");
        printUser(pOrganizationUnit.getManager());
      }
    }
  }

}
