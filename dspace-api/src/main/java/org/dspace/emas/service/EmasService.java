/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.emas.service;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.service.DSpaceObjectLegacySupportService;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.emas.Emas;
import org.dspace.core.Context;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Service interface class for the LatterCategory object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author ashvinmajethiya
 */


public interface EmasService extends DSpaceObjectService<Emas>, DSpaceObjectLegacySupportService<Emas> {

    public Emas create(Context context, Emas Emas) throws SQLException, AuthorizeException;
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
   Boolean getEmasByEperson(Context context, UUID eperson) throws SQLException;
    Boolean getEmasByEpersonANDKey(Context context, UUID eperson,String key) throws SQLException;
    Boolean getEmasByKey(Context context,String key) throws SQLException;

}
