/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.enums;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessHistory;
import org.dspace.content.WorkFlowProcessMaster;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.service.WorkFlowProcessHistoryService;
import org.dspace.content.service.WorkFlowProcessMasterService;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;

public enum WorkFlowUserType {
    MASTER("Workflow User Type"),
    INITIATOR("Initiator"),
    NORMAL("Normal"),
    DISPATCH("Dispatch_User"),
    REFER("Refer"),
    CLOSER("Closer");
    private String userType;
    private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
    private WorkFlowProcessMasterService workFlowProcessMasterService;
    @Component
    public static class ServiceInjector {
                @Autowired
        private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;
        @Autowired
        private WorkFlowProcessMasterService workFlowProcessMasterService;

        @PostConstruct
        public void postConstruct() {
            for (WorkFlowUserType rt : EnumSet.allOf(WorkFlowUserType.class)) {
                rt.setWorkFlowProcessMasterValueService(workFlowProcessMasterValueService);
                rt.setWorkFlowProcessMasterService(workFlowProcessMasterService);
            }
        }
    }
    WorkFlowUserType(String userType) {
        this.userType = userType;
    }

    public String getAction() {
        return userType;
    }
    public Optional<WorkFlowProcessMasterValue> getUserTypeFromMasterValue(Context context) throws SQLException {
        WorkFlowProcessMaster workFlowProcessMaster = MASTER.getMaster(context);
        WorkFlowProcessMasterValue workFlowProcessMasterValue= this.getWorkFlowProcessMasterValueService().findByName(context, this.getAction(), workFlowProcessMaster);
        return Optional.ofNullable(workFlowProcessMasterValue );
    }
    public WorkFlowProcessMaster getMaster(Context context) throws SQLException {
        return this.getWorkFlowProcessMasterService().findByName(context, this.getAction());
    }
    public WorkFlowProcessMasterValueService getWorkFlowProcessMasterValueService() {
        return workFlowProcessMasterValueService;
    }

    public void setWorkFlowProcessMasterValueService(WorkFlowProcessMasterValueService workFlowProcessMasterValueService) {
        this.workFlowProcessMasterValueService = workFlowProcessMasterValueService;
    }

    public  WorkFlowProcessMasterService getWorkFlowProcessMasterService() {
        return workFlowProcessMasterService;
    }

    public  void setWorkFlowProcessMasterService(WorkFlowProcessMasterService workFlowProcessMasterService) {
        this.workFlowProcessMasterService = workFlowProcessMasterService;
    }
}
