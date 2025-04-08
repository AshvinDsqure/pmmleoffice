/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.EpersonMapping;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface EpersonMappingDAO extends DSpaceObjectLegacySupportDAO<EpersonMapping>{
    int countRows(Context context) throws SQLException;
    List<EpersonMapping>getByCountryId(Context context, UUID countryid) throws SQLException;

    EpersonMapping findByOfficeAndDepartmentAndDesignation(Context context,UUID office,UUID department,UUID designation) throws SQLException;
    EpersonMapping findByOfficeAndDepartmentAndDesignationTableNo(Context context,UUID office,UUID department,UUID designation,Integer tbl) throws SQLException;


    List<EpersonMapping> findByOffice(Context context,UUID office) throws SQLException;
    List<EpersonMapping> findOfficeAndDepartment(Context context,UUID office,UUID department) throws SQLException;
    List<EpersonMapping> getByOfficeAndDepartmentAndDesignation(Context context,UUID office,UUID department,UUID designation) throws SQLException;
}
