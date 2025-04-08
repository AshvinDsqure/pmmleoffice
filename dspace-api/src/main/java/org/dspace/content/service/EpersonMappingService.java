/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.EpersonMapping;
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


public interface EpersonMappingService extends DSpaceObjectService<EpersonMapping>,DSpaceObjectLegacySupportService<EpersonMapping> {

    public EpersonMapping create(Context context, EpersonMapping EpersonMapping) throws SQLException, AuthorizeException;
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
    EpersonMapping findByOfficeAndDepartmentAndDesignation(Context context,UUID office,UUID department,UUID designation) throws SQLException;
    EpersonMapping findByOfficeAndDepartmentAndDesignationTableNo(Context context,UUID office,UUID department,UUID designation,Integer tbl) throws SQLException;

    List<EpersonMapping>getByCountryId(Context context, UUID countryid) throws SQLException;
    public List<EpersonMapping> findAll(Context context) throws SQLException;
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
    public List<EpersonMapping> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    List<EpersonMapping> findByOffice(Context context,UUID office) throws SQLException;
    List<EpersonMapping> findOfficeAndDepartment(Context context,UUID office,UUID department) throws SQLException;
    List<EpersonMapping> getByOfficeAndDepartmentAndDesignation(Context context,UUID office,UUID department,UUID designation) throws SQLException;

}
