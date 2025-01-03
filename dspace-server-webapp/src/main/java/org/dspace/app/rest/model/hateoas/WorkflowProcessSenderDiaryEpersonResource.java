/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model.hateoas;


import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.model.WorkflowProcessSenderDiaryEpersonRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.dspace.app.rest.utils.Utils;

/**
 * EPerson Rest HAL Resource. The HAL Resource wraps the REST Resource
 * adding support for the links and embedded resources
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@RelNameDSpaceResource(WorkflowProcessSenderDiaryEpersonRest.NAME)
public class WorkflowProcessSenderDiaryEpersonResource extends DSpaceResource<WorkflowProcessSenderDiaryEpersonRest> {
    public WorkflowProcessSenderDiaryEpersonResource(WorkflowProcessSenderDiaryEpersonRest WorkflowProcessSenderDiaryEpersonRest, Utils utils) {
        super(WorkflowProcessSenderDiaryEpersonRest, utils);
    }
}
