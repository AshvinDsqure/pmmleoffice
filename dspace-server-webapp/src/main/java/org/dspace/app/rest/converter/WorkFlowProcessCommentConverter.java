/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.model.WorkFlowProcessCommentRest;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.utils.PdfUtils;
import org.dspace.content.WorkFlowProcessComment;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.content.service.WorkFlowProcessCommentService;
import org.dspace.content.service.WorkFlowProcessHistoryService;
import org.dspace.core.Context;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class WorkFlowProcessCommentConverter extends DSpaceObjectConverter<WorkFlowProcessComment, WorkFlowProcessCommentRest> {
    @Autowired
    WorkFlowProcessHistoryConverter workFlowProcessHistoryConverter;

    @Autowired
    EPersonConverter ePersonConverter;

    @Autowired
    WorkFlowProcessHistoryService workFlowProcessHistoryService;
    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Autowired
    WorkFlowProcessConverter workFlowProcessConverter;
   @Autowired
   WorkFlowProcessCommentService workFlowProcessCommentService;

    @Override
    public Class<WorkFlowProcessComment> getModelClass() {
        return WorkFlowProcessComment.class;
    }
    @Override
    protected WorkFlowProcessCommentRest newInstance() {
        return new WorkFlowProcessCommentRest();
    }
    @Override
    public WorkFlowProcessCommentRest convert(WorkFlowProcessComment obj, Projection projection) {
        WorkFlowProcessCommentRest rest = new WorkFlowProcessCommentRest();
        if (obj.getWorkFlowProcessHistory() != null) {
            rest.setWorkFlowProcessHistoryRest(workFlowProcessHistoryConverter.convert(obj.getWorkFlowProcessHistory(), projection));
        }
        if (obj.getWorkflowProcessReferenceDoc() != null) {
            rest.setWorkflowProcessReferenceDocRest(obj.getWorkflowProcessReferenceDoc().stream().map(we -> {
                try {
                    if(we.getDrafttype()!=null&&we.getDrafttype().getPrimaryvalue()!=null&&we.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Note")){
                        rest.setMargeddocuuid(we.getID().toString());
                    }
                    WorkflowProcessReferenceDocRest workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convert(we, projection);
                    return workflowProcessReferenceDoc;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
       }
        if(obj.getNote()!=null){
            rest.setNoteRest(workflowProcessReferenceDocConverter.convert(obj.getNote(),projection));
        }
        if (obj.getSubmitter() != null) {
            rest.setSubmitterRest(ePersonConverter.convert(obj.getSubmitter(), projection));
        }
        if(obj.getWorkFlowProcess()!=null){
            rest.setWorkflowProcessRest(workFlowProcessConverter.convert(obj.getWorkFlowProcess(),projection));
        }
        if(obj.getComment()!=null){
            rest.setComment(obj.getComment());
        }
        if(obj.getActionDate()!=null){
            rest.setActionDate(obj.getActionDate());
        }
        rest.setIsdraftsave(obj.getIsdraftsave());
        rest.setUuid(obj.getID().toString());
        return rest;
    }
    public WorkFlowProcessComment convert(Context context, WorkFlowProcessCommentRest rest) throws Exception {
        WorkFlowProcessComment obj = new WorkFlowProcessComment();
        if(rest.getComment()!=null){
            String htmlcomment = "<div>" + rest.getComment()+ "</div>";
           // System.out.println("::::::html::::::::::" + htmlcomment);
            //System.out.println("::::::text:::::" + PdfUtils.htmlToText(htmlcomment));
            obj.setComment(htmlcomment);
        }
        if (rest.getWorkFlowProcessHistoryRest() != null) {
            obj.setWorkFlowProcessHistory(workFlowProcessHistoryService.find(context,UUID.fromString(rest.getWorkFlowProcessHistoryRest().getId())));
        }
        if (rest.getWorkflowProcessReferenceDocRest() != null) {
            obj.setWorkflowProcessReferenceDoc(rest.getWorkflowProcessReferenceDocRest().stream().map(we -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convert(context,we);
                    return workflowProcessReferenceDoc;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
        }

        if(rest.getWorkflowProcessRest()!=null){
            obj.setWorkFlowProcess(workFlowProcessConverter.convertByService(context,rest.getWorkflowProcessRest()));
        }
        if(rest.getActionDate()!=null){
            obj.setActionDate(rest.getActionDate());
        }
        if(rest.getNoteRest()!=null){
            System.out.println("in note doc");
            obj.setNote(workflowProcessReferenceDocConverter.convertByService(context,rest.getNoteRest()));
        }
        obj.setSubmitter(context.getCurrentUser());
        obj.setIsdraftsave(rest.getIsdraftsave());
        return obj;
    }
    public WorkFlowProcessComment convert(Context context,  WorkFlowProcessComment obj, WorkFlowProcessCommentRest rest) throws Exception {
        if(rest.getComment()!=null){
            String htmlcomment = "<div>" + rest.getComment()+ "</div>";
            System.out.println("::::::html::::::::::" + htmlcomment);
            System.out.println("::::::text:::::" + PdfUtils.htmlToText(htmlcomment));
            obj.setComment(htmlcomment);
        }
        if (rest.getWorkFlowProcessHistoryRest() != null) {
            obj.setWorkFlowProcessHistory(workFlowProcessHistoryService.find(context,UUID.fromString(rest.getWorkFlowProcessHistoryRest().getId())));
        }
        if (rest.getWorkflowProcessReferenceDocRest() != null) {
            obj.setWorkflowProcessReferenceDoc(rest.getWorkflowProcessReferenceDocRest().stream().map(we -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convert(context,we);
                    return workflowProcessReferenceDoc;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));
        }
        if (rest.getSubmitterRest() != null) {
            obj.setSubmitter(ePersonConverter.convert(context, rest.getSubmitterRest()));
        }
        if(rest.getWorkflowProcessRest()!=null){
            obj.setWorkFlowProcess(workFlowProcessConverter.convert(rest.getWorkflowProcessRest(),context));
        }
        if(rest.getActionDate()!=null){
            obj.setActionDate(rest.getActionDate());
        }
        if(rest.getNoteRest()!=null){
            System.out.println("in note doc");
            obj.setNote(workflowProcessReferenceDocConverter.convertByService(context,rest.getNoteRest()));
        }

        obj.setIsdraftsave(rest.getIsdraftsave());
        return obj;
    }

    public WorkFlowProcessComment convertByService(Context context,WorkFlowProcessCommentRest rest) throws Exception {
        if (rest != null && rest.getUuid() != null && rest.getUuid().trim().length() != 0) {
            return workFlowProcessCommentService.find(context, UUID.fromString(rest.getId()));
        }
        return null;
    }
}
