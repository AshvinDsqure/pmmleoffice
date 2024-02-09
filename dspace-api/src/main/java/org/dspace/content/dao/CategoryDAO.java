/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */

package org.dspace.content.dao;

import org.dspace.content.Category;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;

public interface CategoryDAO extends DSpaceObjectLegacySupportDAO<Category> {
    int countRows(Context context) throws SQLException;
    List<Category> getAll(Context c) throws SQLException;
}
