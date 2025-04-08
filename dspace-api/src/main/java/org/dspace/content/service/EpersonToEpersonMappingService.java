/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.EpersonToEpersonMapping;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface class for the LatterCategory object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author ashvinmajethiya
 */


public interface EpersonToEpersonMappingService extends DSpaceObjectService<EpersonToEpersonMapping>,DSpaceObjectLegacySupportService<EpersonToEpersonMapping> {

    public EpersonToEpersonMapping create(Context context, EpersonToEpersonMapping EpersonToEpersonMapping) throws SQLException, AuthorizeException;
    /**
     * Get All WorkflowProcess based on limit and offset
     * set are included. The order of the list is indeterminate.
     *
     * @param context DSpace context object
     * @param limit   limit
     * @param offset  offset
     * @return an iterator over the items in the archive.
     * @throws SQLException if database error
     */
    public int countRows(Context context) throws SQLException;
    EpersonToEpersonMapping findByEpersonAndEpersonMapping(Context context, UUID epersonmapping,UUID eperson) throws SQLException;
    EpersonToEpersonMapping findByEpersonbyEP(Context context,UUID eperson) throws SQLException;

    List<EpersonToEpersonMapping>findByEperson(Context context, UUID eperson,Integer offset,Integer limit) throws SQLException;
    int countfindByEperson(Context context, UUID eperson) throws SQLException;
    List<EpersonToEpersonMapping>findByofficeandDepartmentanddesignation(Context context, UUID office,UUID department,UUID designation) throws SQLException;

    public List<EpersonToEpersonMapping> findAll(Context context) throws SQLException;
    /**
     * Get All WorkflowProcess based on limit and offset
     * set are included. The order of the list is indeterminate.
     *
     * @param context DSpace context object
     * @param limit   limit
     * @param offset  offset
     * @return an iterator over the items in the archive.
     * @throws SQLException if database error
     */
    public List<EpersonToEpersonMapping> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public boolean existsByEpersonToEpersonMappingId(Context context,UUID epersonmapping);

}
