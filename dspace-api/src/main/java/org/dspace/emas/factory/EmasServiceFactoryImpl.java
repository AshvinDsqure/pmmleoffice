/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.emas.factory;

import org.dspace.emas.service.EmasService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory implementation to get services for the Emas package, use EmasServiceFactory.getInstance() to
 * retrieve an implementation
 *
 * @author ashvinmajethiya
 */
public class EmasServiceFactoryImpl extends EmasServiceFactory {


    @Autowired(required = true)
    private EmasService EmasService;
    @Override
    public EmasService getEmasService() {
        return EmasService;
    }

}
