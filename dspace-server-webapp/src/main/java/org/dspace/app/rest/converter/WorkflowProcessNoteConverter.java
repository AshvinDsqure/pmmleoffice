/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.model.WorkflowProcessNoteRest;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.WorkflowProcessNote;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.content.service.WorkflowProcessNoteService;
import org.dspace.core.Context;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class WorkflowProcessNoteConverter extends DSpaceObjectConverter<WorkflowProcessNote, WorkflowProcessNoteRest> {
    @Autowired
    WorkflowProcessNoteService workflowProcessNoteService;

    @Autowired
    EPersonConverter ePersonConverter;
    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Override
    public Class<WorkflowProcessNote> getModelClass() {
        return WorkflowProcessNote.class;
    }

    @Override
    protected WorkflowProcessNoteRest newInstance() {
        return new WorkflowProcessNoteRest();
    }

    @Override
    public WorkflowProcessNoteRest convert(WorkflowProcessNote obj, Projection projection) {
        WorkflowProcessNoteRest rest = new WorkflowProcessNoteRest();
        try {
            if (obj.getWorkflowProcessReferenceDocs() != null) {
                rest.setWorkflowProcessReferenceDocRests(obj.getWorkflowProcessReferenceDocs().stream().map(d -> {
                    WorkflowProcessReferenceDocRest restdoc= workflowProcessReferenceDocConverter.convert(d, projection);
                    WorkFlowProcessRest workFlowProcessRest=new WorkFlowProcessRest();
                    workFlowProcessRest.setUuid(d.getWorkflowProcess().getID().toString());
                    restdoc.setWorkFlowProcessRest(workFlowProcessRest);
                    return restdoc;
                }).collect(Collectors.toList()));
            }
            if (obj.getDescription() != null) {
                rest.setDescription(obj.getDescription());
            }
            if(obj.getSubject()!=null) {
                rest.setSubject(obj.getSubject());
            }
            if(obj.getInitDate()!=null){
                rest.setInitDate(obj.getInitDate());
            }
            rest.setUuid(obj.getID().toString());
            if(obj.getSubmitter()!=null) {
                rest.setSubmitter(ePersonConverter.convert(ePersonConverter.convert(obj.getSubmitter(), projection)));
                if(obj.getSubmitter().getFullName()!=null){
                    rest.setFullName(obj.getSubmitter().getFullName());
                }
            }
            return rest;
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }
        return null;
    }

    public WorkflowProcessNote convert(WorkflowProcessNoteRest rest) {
        WorkflowProcessNote obj = new WorkflowProcessNote();
        obj.setSubject(rest.getSubject());
        obj.setDescription(rest.getDescription());
        return obj;
    }

    public WorkflowProcessNote convert(Context context, WorkflowProcessNoteRest rest) throws SQLException {
        if (rest != null && rest.getId() != null) {
            return workflowProcessNoteService.find(context, UUID.fromString(rest.getId()));
        }
        return null;
    }
}
