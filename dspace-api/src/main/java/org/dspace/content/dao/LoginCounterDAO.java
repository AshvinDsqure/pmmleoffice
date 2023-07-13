/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.LoginCounter;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public interface LoginCounterDAO extends DSpaceObjectLegacySupportDAO<LoginCounter>{
    int countRows(Context context) throws SQLException;

    LoginCounter getbyToken(Context context,String token) throws SQLException;
    List<Object[]> filter(Context context) throws SQLException;

}
