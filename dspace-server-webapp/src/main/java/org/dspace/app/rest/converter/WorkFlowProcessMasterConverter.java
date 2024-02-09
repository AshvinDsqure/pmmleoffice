/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.WorkFlowProcessMasterRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.content.WorkFlowProcessMaster;
import org.springframework.stereotype.Component;

@Component
public class WorkFlowProcessMasterConverter extends DSpaceObjectConverter<WorkFlowProcessMaster, WorkFlowProcessMasterRest> {

    @Override
    public Class<WorkFlowProcessMaster> getModelClass() {
        return WorkFlowProcessMaster.class;
    }

    @Override
    protected WorkFlowProcessMasterRest newInstance() {
        return new WorkFlowProcessMasterRest();
    }


    @Override
    public WorkFlowProcessMasterRest convert(WorkFlowProcessMaster obj, Projection projection) {
        WorkFlowProcessMasterRest rest = new WorkFlowProcessMasterRest();
        if (!DateUtils.isNullOrEmptyOrBlank(obj.getMastername())) {
            rest.setMastername(obj.getMastername());
        }
        if (!DateUtils.isNullOrEmptyOrBlank(obj.getID().toString())) {
            rest.setUuid(obj.getID().toString());
        }
        return rest;
    }

    public WorkFlowProcessMaster convert(WorkFlowProcessMaster workFlowProcessMaster, WorkFlowProcessMasterRest workFlowProcessMasterRest) {
        if (!DateUtils.isNullOrEmptyOrBlank(workFlowProcessMasterRest.getMastername())) {
            workFlowProcessMaster.setMastername(workFlowProcessMasterRest.getMastername());
        }
        return workFlowProcessMaster;
    }
}
