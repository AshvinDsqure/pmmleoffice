/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.WorkFlowProcessHistoryRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.content.WorkFlowProcessHistory;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.service.WorkflowProcessEpersonService;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class WorkFlowProcessHistoryConverter extends DSpaceObjectConverter<WorkFlowProcessHistory, WorkFlowProcessHistoryRest> {
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    WorkFlowProcessEpersonConverter workFlowProcessEpersonConverter;
    @Autowired
    WorkflowProcessEpersonService workflowProcessEpersonService;

    @Autowired
    EPersonService ePersonService;
    @Override
    public Class<WorkFlowProcessHistory> getModelClass() {
        return WorkFlowProcessHistory.class;
    }

    @Override
    protected WorkFlowProcessHistoryRest newInstance() {
        return new WorkFlowProcessHistoryRest();
    }


    @Override
    public WorkFlowProcessHistoryRest convert(WorkFlowProcessHistory obj, Projection projection) {
        WorkFlowProcessHistoryRest rest = new WorkFlowProcessHistoryRest();
        if(obj.getAction()!= null) {
           rest.setAction(workFlowProcessMasterValueConverter.convert(obj.getAction(), projection));
        }
        if(obj.getWorkflowProcessEpeople()!= null){
            rest.setWorkflowProcessEpersonRest(workFlowProcessEpersonConverter.convert(obj.getWorkflowProcessEpeople(),projection));
        }
        if(obj.getSentto()!= null){
            try {
                rest.setSenttoRest(workFlowProcessEpersonConverter.convertByHistory(obj.getSentto(), projection));
            }catch (Exception e){
               // e.printStackTrace();
                System.out.println("ERror :::in WorkFlowProcessHistoryConverter 63 line ::::::"+e.getMessage());
            }
        }
        if(obj.getSentbyname()!=null){
            rest.setSentbyname(obj.getSentbyname());
        }
        if(obj.getSenttoname()!=null){
            rest.setSenttoname(obj.getSenttoname());
        }
        rest.setUuid(obj.getID().toString());
        rest.setComment(obj.getComment());
        rest.setActionDate(obj.getActionDate());
        rest.setReceivedDate(obj.getReceivedDate());
        return rest;
    }

    public WorkFlowProcessHistoryRest convertbyEP(Context context,WorkFlowProcessHistory obj, Projection projection) {
        WorkFlowProcessHistoryRest rest = new WorkFlowProcessHistoryRest();
        if(obj.getAction()!= null) {
            rest.setAction(workFlowProcessMasterValueConverter.convert(obj.getAction(), projection));
        }
        if(obj.getWorkflowProcessEpeople()!= null){
            rest.setWorkflowProcessEpersonRest(workFlowProcessEpersonConverter.convert(obj.getWorkflowProcessEpeople(),projection));
        }
        if(obj.getSentto()!= null){
            try {
                System.out.println("ep id"+obj.getSentto().getID());
                rest.setSenttoRest(workFlowProcessEpersonConverter.convertByHistory(obj.getSentto(), projection));
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("eerrr "+e.getMessage());
            }
        }
        if(obj.getSentbyname()!=null){
            rest.setSentbyname(obj.getSentbyname());
        }
        if(obj.getSenttoname()!=null){
            rest.setSenttoname(obj.getSenttoname());
        }
        rest.setUuid(obj.getID().toString());
        rest.setComment(obj.getComment());
        rest.setActionDate(obj.getActionDate());
        rest.setReceivedDate(obj.getReceivedDate());
        return rest;
    }

    public WorkFlowProcessHistory convert(Context context, WorkFlowProcessHistoryRest obj) throws SQLException {
        WorkFlowProcessHistory workFlowProcessHistory=new WorkFlowProcessHistory();
        workFlowProcessHistory.setAction(workFlowProcessMasterValueConverter.convert(context, obj.getAction()));
        workFlowProcessHistory.setWorkflowProcessEpeople(workFlowProcessEpersonConverter.convert(context, obj.getWorkflowProcessEpersonRest()));
        workFlowProcessHistory.setComment(obj.getComment());
        return workFlowProcessHistory;
    }

}
