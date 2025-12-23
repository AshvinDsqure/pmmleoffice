/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.emas.dao;

import org.dspace.content.dao.DSpaceObjectLegacySupportDAO;
import org.dspace.core.Context;
import org.dspace.emas.Emas;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface EmasDAO extends DSpaceObjectLegacySupportDAO<Emas> {
    int countRows(Context context) throws SQLException;

    int getEmasByEperson(Context context, UUID eperson) throws SQLException;
    int getEmasByEpersonANDKey(Context context, UUID eperson,String key) throws SQLException;
    int getEmasByKey(Context context,String key) throws SQLException;

}
