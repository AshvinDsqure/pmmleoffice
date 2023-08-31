/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.WorkFlowProcessMasterValueRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.core.Context;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;

@Component
public class WorkFlowProcessMasterValueConverter extends DSpaceObjectConverter<WorkFlowProcessMasterValue, WorkFlowProcessMasterValueRest> {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    WorkFlowProcessMasterValueService masterValueService;
    @Override
    public Class<WorkFlowProcessMasterValue> getModelClass() {
        return WorkFlowProcessMasterValue.class;
    }
    @Override
    protected WorkFlowProcessMasterValueRest newInstance() {
        return new WorkFlowProcessMasterValueRest();
    }

    @Override
    public WorkFlowProcessMasterValueRest convert(WorkFlowProcessMasterValue obj, Projection projection) {
        WorkFlowProcessMasterValueRest rest = new WorkFlowProcessMasterValueRest();
        if (obj.getPrimaryvalue() != null) {
            rest.setPrimaryvalue(obj.getPrimaryvalue());
        }
        if (obj.getSecondaryvalue() != null) {
            rest.setSecondaryvalue(obj.getSecondaryvalue());
        }
        if (obj.getLegacyId() != null) {
            rest.setLegacyId(obj.getLegacyId());
        }
        rest.setUuid(obj.getID().toString());
        return rest;
    }

    public WorkFlowProcessMasterValue convert(WorkFlowProcessMasterValue obj, WorkFlowProcessMasterValueRest rest) {
        if (rest.getPrimaryvalue() != null) {
            obj.setPrimaryvalue(rest.getPrimaryvalue());
        }
        if (rest.getSecondaryvalue() != null) {
            obj.setSecondaryvalue(rest.getSecondaryvalue());
        }
        if (rest.getWorkFlowProcessMaster() != null && rest.getWorkFlowProcessMaster().getLegacyId() != null) {
            obj.setLegacyId(rest.getWorkFlowProcessMaster().getLegacyId());
        }
        if (rest.getWorkFlowProcessMaster() != null) {
            obj.setWorkflowprocessmaster(rest.getWorkFlowProcessMaster());
        }
        return obj;
    }

    public WorkFlowProcessMasterValue convert(Context context, WorkFlowProcessMasterValueRest rest) throws SQLException {
        WorkFlowProcessMasterValue workFlowProcessMasterValue = null;
        if (rest != null && rest.getUuid() != null && rest.getUuid().trim().length() != 0) {
            workFlowProcessMasterValue = masterValueService.find(context, UUID.fromString(rest.getUuid()));
        }
        return workFlowProcessMasterValue;
    }

}
