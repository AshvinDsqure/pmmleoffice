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
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.converter.WorkflowProcessSenderDiaryEpersonConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkflowProcessSenderDiaryEpersonRest;
import org.dspace.app.rest.model.WorkflowProcessSenderDiaryRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkflowProcess;
import org.dspace.content.WorkflowProcessSenderDiary;
import org.dspace.content.WorkflowProcessSenderDiaryEperson;
import org.dspace.content.service.WorkFlowProcessHistoryService;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.content.service.WorkflowProcessSenderDiaryEpersonService;
import org.dspace.content.service.WorkflowProcessService;
import org.dspace.core.Context;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This is the rest repository responsible for managing WorkflowDefinition Rest objects
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@Component(WorkflowProcessSenderDiaryEpersonRest.CATEGORY + "." + WorkflowProcessSenderDiaryEpersonRest.NAME)
public class WorkflowProcessSenderDiaryEpersonRestRepository extends DSpaceObjectRestRepository<WorkflowProcessSenderDiaryEperson, WorkflowProcessSenderDiaryEpersonRest> {
    @Autowired
    private WorkflowProcessSenderDiaryEpersonService WorkflowProcessSenderDiaryEpersonService;

    @Autowired
    private WorkflowProcessSenderDiaryEpersonConverter workflowProcessSenderDiaryEpersonConverter;

    @Autowired
    private WorkFlowProcessHistoryService workFlowProcessHistoryService;
    @Autowired
    private EPersonService ePersonService;

    @Autowired
    WorkflowProcessService workflowProcessService;

    @Autowired
    private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;

    @Autowired
    private WorkflowProcessSenderDiaryEpersonConverter WorkflowProcessSenderDiaryEpersonConverter;
    @Autowired
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    public WorkflowProcessSenderDiaryEpersonRestRepository(WorkflowProcessSenderDiaryEpersonService dso) {
        super(dso);
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public WorkflowProcessSenderDiaryEpersonRest findOne(Context context, UUID id) {
        context.turnOffAuthorisationSystem();
        WorkflowProcessSenderDiaryEpersonRest WorkflowProcessSenderDiaryEpersonRest=null;
        try {
            Optional<WorkflowProcessSenderDiaryEperson> workflowProcessDefinitionOption = Optional.ofNullable(WorkflowProcessSenderDiaryEpersonService.find(context, id));
            if(workflowProcessDefinitionOption.isPresent()){
                WorkflowProcessSenderDiaryEpersonRest =converter.toRest(workflowProcessDefinitionOption.get(),utils.obtainProjection());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return WorkflowProcessSenderDiaryEpersonRest;
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public Page<WorkflowProcessSenderDiaryEpersonRest> findAll(Context context, Pageable pageable) {
        try {
            context.turnOffAuthorisationSystem();
            List<WorkflowProcessSenderDiaryEperson> workflowProcessDefinitions= WorkflowProcessSenderDiaryEpersonService.findAll(context,pageable.getPageSize(),Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(workflowProcessDefinitions, pageable, 10, utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }
    @Override
    public Class<WorkflowProcessSenderDiaryEpersonRest> getDomainClass() {
        return null;
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected WorkflowProcessSenderDiaryEpersonRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                             JsonNode jsonNode) throws Exception {
        context.turnOffAuthorisationSystem();
        WorkflowProcessSenderDiaryEpersonRest rest = new Gson().fromJson(jsonNode.toString(), WorkflowProcessSenderDiaryEpersonRest.class);
        System.out.println(":::::::::::::::E persion Update:::::::::");

        WorkflowProcessSenderDiaryEperson WorkflowProcessSenderDiaryEperson = WorkflowProcessSenderDiaryEpersonService.find(context, id);
        if (WorkflowProcessSenderDiaryEperson == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }
        WorkflowProcessSenderDiaryEpersonService.update(context, WorkflowProcessSenderDiaryEperson);
       System.out.println("::::::::::::::E persion Update done!");
        return converter.toRest(WorkflowProcessSenderDiaryEperson, utils.obtainProjection());
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, uuid, patch);
    }

    @Override
    protected WorkflowProcessSenderDiaryEpersonRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkflowProcessSenderDiaryEpersonRest workflowProcessSenderDiaryRest = null;
        WorkflowProcessSenderDiaryEperson workflowProcessSenderDiary = null;
        try {
            workflowProcessSenderDiaryRest = mapper.readValue(req.getInputStream(), WorkflowProcessSenderDiaryEpersonRest.class);
            workflowProcessSenderDiary = createworkflowProcessFromRestObject(context, workflowProcessSenderDiaryRest);

        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return converter.toRest(workflowProcessSenderDiary, utils.obtainProjection());
    }

    private WorkflowProcessSenderDiaryEperson createworkflowProcessFromRestObject(Context context, WorkflowProcessSenderDiaryEpersonRest workflowProcessSenderDiaryRest) throws AuthorizeException {
        WorkflowProcessSenderDiaryEperson workflowProcessSenderDiary = new WorkflowProcessSenderDiaryEperson();

        try {
            workflowProcessSenderDiary = workflowProcessSenderDiaryEpersonConverter.convert(context,workflowProcessSenderDiary, workflowProcessSenderDiaryRest);
            if(workflowProcessSenderDiaryRest.getWorkFlowProcessRest()!=null&&workflowProcessSenderDiaryRest.getWorkFlowProcessRest().getId()!=null){
                List<WorkflowProcessSenderDiaryEperson> list=new ArrayList<>();
                UUID wpid=UUID.fromString(workflowProcessSenderDiaryRest.getWorkFlowProcessRest().getId());
                WorkflowProcess wp=workflowProcessService.find(context,wpid);
                workflowProcessSenderDiary.setWorkflowProcess(wp);
                System.out.println("Done Create ::::::::::::::::::::done!");
            }
            workflowProcessSenderDiary=   WorkflowProcessSenderDiaryEpersonService.create(context, workflowProcessSenderDiary);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return workflowProcessSenderDiary;
    }

    protected void delete(Context context, UUID id) throws AuthorizeException {

        WorkflowProcessSenderDiaryEperson eperson = null;
        try {
            eperson = WorkflowProcessSenderDiaryEpersonService.find(context, id);
            if (eperson == null) {
                throw new ResourceNotFoundException(WorkflowProcessSenderDiaryEpersonRest.CATEGORY + "." + WorkflowProcessSenderDiaryEpersonRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            eperson.setIsdelete(true);
            eperson.setWorkflowProcess(null);
            WorkflowProcessSenderDiaryEpersonService.update(context, eperson);
            context.commit();
            System.out.println("delete done !");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
