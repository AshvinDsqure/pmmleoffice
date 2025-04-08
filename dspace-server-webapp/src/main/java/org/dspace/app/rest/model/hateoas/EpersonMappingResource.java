package org.dspace.app.rest.model.hateoas;
/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

import org.dspace.app.rest.model.EpersonMappingRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.dspace.app.rest.utils.Utils;

/**
 * EPerson Rest HAL Resource. The HAL Resource wraps the REST Resource
 * adding support for the links and embedded resources
 *
 * @author ashvinmajethiya
 */
@RelNameDSpaceResource(EpersonMappingRest.NAME)
public class EpersonMappingResource extends DSpaceResource<EpersonMappingRest> {
    public EpersonMappingResource(EpersonMappingRest EpersonMappingRest, Utils utils) {
        super(EpersonMappingRest, utils);
    }
}
