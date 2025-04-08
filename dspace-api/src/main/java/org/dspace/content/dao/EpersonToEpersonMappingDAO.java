/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.EpersonToEpersonMapping;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface EpersonToEpersonMappingDAO extends DSpaceObjectLegacySupportDAO<EpersonToEpersonMapping>{
    int countRows(Context context) throws SQLException;
    List<EpersonToEpersonMapping>findByEperson(Context context, UUID eperson,Integer offset,Integer limit) throws SQLException;
    int countfindByEperson(Context context, UUID eperson) throws SQLException;
    EpersonToEpersonMapping findByEpersonAndEpersonMapping(Context context, UUID epersonmapping,UUID eperson) throws SQLException;

    EpersonToEpersonMapping findByEpersonbyEP(Context context,UUID eperson) throws SQLException;

    List<EpersonToEpersonMapping>findByofficeandDepartmentanddesignation(Context context, UUID office,UUID department,UUID designation) throws SQLException;

    public boolean existsByEpersonToEpersonMappingId(Context context,UUID epersonmapping);



}
