/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.WorkflowProcessNoteConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessDefinitionRest;
import org.dspace.app.rest.model.WorkflowProcessNoteRest;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocVersionRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkflowProcessNote;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.content.service.WorkflowProcessNoteService;
import org.dspace.core.Context;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is the rest repository responsible for managing WorkflowDefinition Rest objects
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@Component(WorkflowProcessNoteRest.CATEGORY + "." + WorkflowProcessNoteRest.NAME)
public class WorkflowProcessNoteRestRepository extends DSpaceObjectRestRepository<WorkflowProcessNote, WorkflowProcessNoteRest> {
    @Autowired
    private WorkflowProcessNoteService workflowProcessNoteService;
    @Autowired
    private EPersonService ePersonService;
    @Autowired
    private WorkflowProcessNoteConverter workflowProcessNoteConverter;
    @Autowired
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    public WorkflowProcessNoteRestRepository(WorkflowProcessNoteService dso) {
        super(dso);
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public WorkflowProcessNoteRest findOne(Context context, UUID id) {
        WorkflowProcessNoteRest workflowProcessNoteRest=null;
        try {
            Optional<WorkflowProcessNote> workflowProcessDefinitionOption = Optional.ofNullable(workflowProcessNoteService.find(context, id));

            if(workflowProcessDefinitionOption.isPresent()){

                System.out.println(">>>>>>>>>>>>>>>>>>name"+workflowProcessDefinitionOption.get().getDescription());
                workflowProcessNoteRest =converter.toRest(workflowProcessDefinitionOption.get(),utils.obtainProjection());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return workflowProcessNoteRest;
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')") @Override
    public Page<WorkflowProcessNoteRest> findAll(Context context, Pageable pageable) {
        try {
            List<WorkflowProcessNote> workflowProcessDefinitions= workflowProcessNoteService.findAll(context,pageable.getPageSize(),Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(workflowProcessDefinitions, pageable, 10, utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }
    @Override
    public Class<WorkflowProcessNoteRest> getDomainClass() {
        return null;
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @Override
    protected WorkflowProcessNoteRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkflowProcessNoteRest wroWorkflowProcessNoteRest = null;
        WorkflowProcessNote workflowProcessNote=null;
        try {
            wroWorkflowProcessNoteRest = mapper.readValue(req.getInputStream(), WorkflowProcessNoteRest.class);
            workflowProcessNote= createworkflowProcessDefinitionFromRestObject(context,wroWorkflowProcessNoteRest);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return converter.toRest(workflowProcessNote, utils.obtainProjection());
    }
    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    protected WorkflowProcessNoteRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                   JsonNode jsonNode) throws SQLException, AuthorizeException {
        WorkflowProcessNoteRest workflowProcessNoteRest = new Gson().fromJson(jsonNode.toString(), WorkflowProcessNoteRest.class);
        WorkflowProcessNote WorkflowProcessNote = workflowProcessNoteService.find(context, id);
        if (WorkflowProcessNote == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }

        workflowProcessNoteService.update(context, WorkflowProcessNote);
        return converter.toRest(workflowProcessNoteRest, utils.obtainProjection());
    }
    private WorkflowProcessNote createworkflowProcessDefinitionFromRestObject(Context context, WorkflowProcessNoteRest workflowProcessNoteRest) throws AuthorizeException {
        WorkflowProcessNote workflowProcessNote =new WorkflowProcessNote();
        try {
            workflowProcessNote= workflowProcessNoteConverter.convert(workflowProcessNoteRest);
            workflowProcessNote.setSubmitter(context.getCurrentUser());
            workflowProcessNote = workflowProcessNoteService.create(context,workflowProcessNote);
            WorkflowProcessNote finalWorkflowProcessNote = workflowProcessNote;
            workflowProcessNote.setWorkflowProcessReferenceDocs(workflowProcessNoteRest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                    workflowProcessReferenceDoc.setWorkflowprocessnote(finalWorkflowProcessNote);
                    return workflowProcessReferenceDoc;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toSet()));
            System.out.println("workflowProcess Note::: update ......");
            workflowProcessNoteService.update(context,workflowProcessNote);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return workflowProcessNote;
    }
    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, uuid, patch);
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected void delete(Context context, UUID id) throws AuthorizeException {
        WorkflowProcessNote workflowProcessNote = null;
        try {
            workflowProcessNote = workflowProcessNoteService.find(context, id);
            if (workflowProcessNote == null) {
                throw new ResourceNotFoundException(WorkFlowProcessDefinitionRest.CATEGORY + "." + WorkFlowProcessDefinitionRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workflowProcessNoteService.delete(context, workflowProcessNote);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getDocumentByItemID")
    public Page<WorkflowProcessNote> getDocumentByItemID(@Parameter(value = "itemid", required = true) UUID itemid, Pageable pageable) {
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            UUID statusid= WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
           // System.out.println("status id:"+statusid);
            long total = workflowProcessNoteService.countDocumentByItemid(context, itemid,statusid);
            List<WorkflowProcessNote> witems = workflowProcessNoteService.getDocumentByItemid(context, itemid,statusid, Math.toIntExact(pageable.getOffset()),
                    Math.toIntExact(pageable.getPageSize()));
            return converter.toRestPage(witems, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
