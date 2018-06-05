/*
 * Copyright 2017 Karlsruhe Institute of Technology.
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
package edu.kit.dama.rest.client.access.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import static edu.kit.dama.rest.util.RestClientUtils.prepareWebResource;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST client for search. For next version of KIT Data Manager this has to be
 * extended. Tasks for the search module: indexing, update and search. Possible
 * separators could be: type, ...?
 *
 * @author hartmann-v
 */
public class SearchRestClient extends AbstractRestClient {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchRestClient.class);
  //<editor-fold defaultstate="collapsed" desc="Parameter names">
  /**
   * The groupId.
   */
  protected static final String QUERY_PARAMETER_GROUP_ID = "groupId";
  /**
   * The index of the search.
   */
  protected static final String QUERY_PARAMETER_INDEX = "index";
  /**
   * The type of the search.
   */
  protected static final String QUERY_PARAMETER_TYPE = "type";

  /**
   * The term looking for.
   */
  protected static final String QUERY_PARAMETER_TERM = "term";
  /**
   * The maximum number of hits.
   */
  protected static final String QUERY_PARAMETER_MAX_NO_OF_HITS = "size";
  /**
   * The joker value. (all types, indices,...)
   */
  protected static final String ALL_VALUES = "_all";

  //</editor-fold>
  // <editor-fold defaultstate="collapsed" desc="URL components">
  /**
   * 'url' for search
   */
  private static final String MASI_SEARCH_URL = "rest/metastore/xml/search";

// </editor-fold>
  /**
   * Create a REST client with a predefined context.
   *
   * @param rootUrl root url of the staging service. (e.g.:
   * "http://dama.lsdf.kit.edu/KITDM/rest/StagingService")
   * @param pContext initial context
   */
  public SearchRestClient(String rootUrl, SimpleRESTContext pContext) {
    super(rootUrl, pContext);
  }

  /**
   * Get list of hits.
   *
   * @param pGroupId groupId the authenticator belongs to.
   * @param pIndices indices which should be looked at.
   * @param pTypes types which should be looked at.
   * @param pTerms terms which should be looked for.
   * @param pMaxNoOfHits maximum number of hits.
   * @param pSecurityContext initial context
   * @return String with all documents.
   */
  public String getSearchResultList(String pGroupId, String[] pIndices, String[] pTypes, String[] pTerms, int pMaxNoOfHits, SimpleRESTContext pSecurityContext) {
    String returnValue = null;
    MultivaluedMap queryParams = new MultivaluedMapImpl();
    setFilterFromContext(pSecurityContext);
    if (pGroupId != null) {
      queryParams.add(QUERY_PARAMETER_GROUP_ID, pGroupId);
    }
    if (pIndices != null) {
      for (String index : pIndices) {
        queryParams.add(QUERY_PARAMETER_INDEX, index);
      }
    } else {
      queryParams.add(QUERY_PARAMETER_INDEX, ALL_VALUES);
    }
    if (pTypes != null) {
      for (String index : pTypes) {
        queryParams.add(QUERY_PARAMETER_TYPE, index);
      }
    } else {
      queryParams.add(QUERY_PARAMETER_TYPE, ALL_VALUES);
    }
    if (pTerms != null) {
      for (String index : pTerms) {
        queryParams.add(QUERY_PARAMETER_TERM, index);
      }
    }
    queryParams.add(QUERY_PARAMETER_MAX_NO_OF_HITS, Integer.toString(pMaxNoOfHits));
    
    WebResource webResource = prepareWebResource(getWebResource(MASI_SEARCH_URL), queryParams);
    ClientResponse response = webResource.type(MediaType.APPLICATION_XML).get(ClientResponse.class);
    try {
      returnValue = new String(IOUtils.toByteArray(response.getEntityInputStream()));
    } catch (IOException ex) {
      LOGGER.error("Error reading response!", ex);
    }
    return returnValue;
  }

}
