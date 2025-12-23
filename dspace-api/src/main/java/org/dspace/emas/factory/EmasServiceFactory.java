/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.emas.factory;


import org.dspace.emas.service.EmasService;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * Abstract factory to get services for the Emas package, use EmasServiceFactory.getInstance() to retrieve an
 * implementation
 *
 * @author kevinvandevelde at atmire.com
 */
public abstract class EmasServiceFactory {

    public abstract EmasService getEmasService();


    public static EmasServiceFactory getInstance() {
        return DSpaceServicesFactory.getInstance().getServiceManager()
                .getServiceByName("EmasServiceFactory", EmasServiceFactory.class);
    }


}
