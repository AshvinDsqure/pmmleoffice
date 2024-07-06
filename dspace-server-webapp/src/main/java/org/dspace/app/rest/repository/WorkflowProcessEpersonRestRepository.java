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
import org.dspace.app.rest.converter.WorkFlowProcessEpersonConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessDefinitionRest;
import org.dspace.app.rest.model.WorkflowProcessEpersonRest;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocRest;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocVersionRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.app.rest.utils.PdfUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.WorkFlowProcessHistoryService;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.content.service.WorkflowProcessEpersonService;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is the rest repository responsible for managing WorkflowDefinition Rest objects
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@Component(WorkflowProcessEpersonRest.CATEGORY + "." + WorkflowProcessEpersonRest.NAME)
public class WorkflowProcessEpersonRestRepository extends DSpaceObjectRestRepository<WorkflowProcessEperson, WorkflowProcessEpersonRest> {
    @Autowired
    private WorkflowProcessEpersonService workflowProcessEpersonService;
    @Autowired
    private WorkFlowProcessHistoryService workFlowProcessHistoryService;
    @Autowired
    private EPersonService ePersonService;

    @Autowired
    private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;

    @Autowired
    private WorkFlowProcessEpersonConverter workflowProcessEpersonConverter;
    @Autowired
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    public WorkflowProcessEpersonRestRepository(WorkflowProcessEpersonService dso) {
        super(dso);
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public WorkflowProcessEpersonRest findOne(Context context, UUID id) {
        context.turnOffAuthorisationSystem();
        WorkflowProcessEpersonRest WorkflowProcessEpersonRest=null;
        try {
            Optional<WorkflowProcessEperson> workflowProcessDefinitionOption = Optional.ofNullable(workflowProcessEpersonService.find(context, id));
            if(workflowProcessDefinitionOption.isPresent()){
                WorkflowProcessEpersonRest =converter.toRest(workflowProcessDefinitionOption.get(),utils.obtainProjection());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return WorkflowProcessEpersonRest;
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public Page<WorkflowProcessEpersonRest> findAll(Context context, Pageable pageable) {
        try {
            context.turnOffAuthorisationSystem();
            List<WorkflowProcessEperson> workflowProcessDefinitions= workflowProcessEpersonService.findAll(context,pageable.getPageSize(),Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(workflowProcessDefinitions, pageable, 10, utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }
    @Override
    public Class<WorkflowProcessEpersonRest> getDomainClass() {
        return null;
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected WorkflowProcessEpersonRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                          JsonNode jsonNode) throws Exception {
        context.turnOffAuthorisationSystem();
        WorkflowProcessEpersonRest rest = new Gson().fromJson(jsonNode.toString(), WorkflowProcessEpersonRest.class);
        System.out.println(":::::::::::::::E persion Update:::::::::");

        WorkflowProcessEperson workflowProcessEperson = workflowProcessEpersonService.find(context, id);
        if (workflowProcessEperson == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }
        workflowProcessEperson.setIsapproved(true);
        workflowProcessEpersonService.update(context, workflowProcessEperson);
        storeWorkFlowHistoryForApprovedPerralare(context,workflowProcessEperson,rest.getComment());
        System.out.println("::::::::::::::E persion Update done!");
        return converter.toRest(workflowProcessEperson, utils.obtainProjection());
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, uuid, patch);
    }

    protected void delete(Context context, UUID id) throws AuthorizeException {

        WorkflowProcessEperson eperson = null;
        try {
            eperson = workflowProcessEpersonService.find(context, id);
            if (eperson == null) {
                throw new ResourceNotFoundException(WorkflowProcessEpersonRest.CATEGORY + "." + WorkflowProcessEpersonRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
          //  eperson.setWorkflowProcess(null);
            eperson.setIsdelete(true);
            workflowProcessEpersonService.update(context, eperson);
            context.commit();
            System.out.println("delete done !");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void storeWorkFlowHistoryForApprovedPerralare(Context context,WorkflowProcessEperson workflowProcessEperson,String comment) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistory:ApprovedPerralare:::::: ");
        WorkFlowProcessHistory workFlowAction = null;
        WorkflowProcess workflowProcess = workflowProcessEperson.getWorkflowProcess();
        workFlowAction = new WorkFlowProcessHistory();
        WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
        workFlowAction.setWorkflowProcessEpeople(workflowProcessEperson);
        WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.FORWARD.getAction(), workFlowProcessMaster);
        workFlowAction.setActionDate(new Date());
        workFlowAction.setAction(workFlowProcessMasterValue);
        workFlowAction.setWorkflowProcess(workflowProcess);
        workflowProcess.getWorkflowProcessNote().getSubject();
        workFlowAction.setComment(comment);
        //add comment
        WorkFlowProcessComment workFlowProcessComment = new WorkFlowProcessComment();
        workFlowProcessComment.setComment(comment);
        workFlowProcessComment.setWorkFlowProcessHistory(workFlowAction);
        workFlowProcessComment.setSubmitter(context.getCurrentUser());
        workFlowProcessComment.setWorkFlowProcess(workflowProcess);
        workFlowAction.setWorkFlowProcessComment(workFlowProcessComment);

        workFlowProcessHistoryService.create(context, workFlowAction);
        System.out.println("::::::OUT :storeWorkFlowHistory::ApprovedPerralare::: ");
    }
}
