/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.*;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.service.GroupService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(WorkFlowProcessRest.CATEGORY + "." + WorkFlowProcessRest.NAME)
public class WorkflowProcessRestRepository extends DSpaceObjectRestRepository<WorkflowProcess, WorkFlowProcessRest> {

    private static final Logger log = LogManager.getLogger(WorkflowProcessRestRepository.class);

    @Autowired
    WorkFlowProcessConverter workFlowProcessConverter;

    @Autowired
    GroupService groupService;

    @Autowired
    WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;

    @Autowired
    WorkFlowProcessMasterService workFlowProcessMasterService;

    @Autowired
    private WorkflowProcessService workflowProcessService;
    @Autowired
    private WorkflowProcessReferenceDocService workflowProcessReferenceDocService;
    @Autowired
    private WorkflowProcessSenderDiaryService workflowProcessSenderDiaryService;
    @Autowired
    private WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;
    @Autowired
    JbpmServerImpl jbpmServer;
    @Autowired
    ModelMapper modelMapper ;
    @Autowired
    private ValidatorFactory validatorFactory;
    public WorkflowProcessRestRepository(WorkflowProcessService dsoService) {
        super(dsoService);
    }
    @Override
    @PreAuthorize("hasPermission(#id, 'WORKSPACEITEM', 'WRITE')")
    public WorkFlowProcessRest findOne(Context context, UUID id) throws SQLException {
        WorkflowProcess workflowProcess= workflowProcessService.find(context,id);
        return workFlowProcessConverter.convert(workflowProcess,utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    public Page<WorkFlowProcessRest> findAll(Context context, Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes=new ArrayList<WorkFlowProcessRest>();
        try {
            UUID statusid=WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            System.out.println("status id:"+statusid);
            WorkFlowProcessMaster workFlowProcessMaster= workFlowProcessMasterService.findByName(context,"Workflow Type");
            UUID statusdraftid=null;
            if(workFlowProcessMaster!=null){
                statusdraftid= workFlowProcessMasterValueService.findByName(context,"Draft",workFlowProcessMaster).getID();
            }
            int count=workflowProcessService.countfindNotCompletedByUser(context,context.getCurrentUser().getID(),statusid,statusdraftid);
            List<WorkflowProcess> workflowProcesses= workflowProcessService.findNotCompletedByUser(context,context.getCurrentUser().getID(),statusid,statusdraftid,Math.toIntExact(pageable.getOffset()),pageable.getPageSize());
           workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context,d, utils.obtainProjection());
            }).collect(toList());

           return new PageImpl(workflowsRes, pageable,count);
            //return converter.toRestPage(workflowProcesses, pageable,count , utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }


    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "getByWorkFlowType")
    public Page<WorkFlowProcessRest> getByWorkFlowType(@Parameter(value = "uuid", required = true) UUID typeid, Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes=new ArrayList<WorkFlowProcessRest>();
        System.out.println("in workflow type ");
        try {
            Context context = obtainContext();
            UUID statusid=WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            int count=workflowProcessService.countfindNotCompletedByUser(context,context.getCurrentUser().getID(),statusid,typeid);
            List<WorkflowProcess> workflowProcesses= workflowProcessService.findNotCompletedByUser(context,context.getCurrentUser().getID(),statusid,typeid,Math.toIntExact(pageable.getOffset()),pageable.getPageSize());
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context,d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(workflowsRes, pageable,count);
           // return converter.toRestPage(workflowProcesses, pageable,count , utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "gethistory")
    public Page<WorkflowProcessDTO> gethistory(Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes=new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            UUID statusid=WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            int count=workflowProcessService.countgetHistoryByNotOwnerAndNotDraft(context,context.getCurrentUser().getID(),statusid);
            List<WorkflowProcess> workflowProcesses= workflowProcessService.getHistoryByNotOwnerAndNotDraft(context,context.getCurrentUser().getID(),statusid,Math.toIntExact(pageable.getOffset()),pageable.getPageSize());
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context,d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(workflowsRes, pageable,count);
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "dashboard")
    public Page<WorkFlowProcessRest> dashboard(Context context, Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes=new ArrayList<WorkFlowProcessRest>();
        try {
            UUID statusid=WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            System.out.println("Statis id isDraft"+statusid);
            int count=workflowProcessService.countgetHistoryByNotOwnerAndNotDraft(context,context.getCurrentUser().getID(),statusid);
            List<WorkflowProcess> workflowProcesses= workflowProcessService.getHistoryByNotOwnerAndNotDraft(context,context.getCurrentUser().getID(),statusid,Math.toIntExact(pageable.getOffset()),pageable.getPageSize());
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context,d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(workflowsRes, pageable,count);
            //return converter.toRestPage(workflowProcesses, pageable,count , utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }


    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "getDraft")
    public Page<WorkFlowProcessRest> getDraft(Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes=new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            UUID statusid=WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            System.out.println("Draft Id"+statusid);
            int count=workflowProcessService.countgetHistoryByOwnerAndIsDraft(context,context.getCurrentUser().getID(),statusid);
            List<WorkflowProcess> workflowProcesses= workflowProcessService.getHistoryByOwnerAndIsDraft(context,context.getCurrentUser().getID(),statusid,Math.toIntExact(pageable.getOffset()),pageable.getPageSize());
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context,d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(workflowsRes, pageable,count);

            // return converter.toRestPage(workflowProcesses, pageable,count , utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "getDraftNotePendingWorkflow")
    public Page<WorkFlowProcessRest> getDraftNotePendingWorkflow(Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes=new ArrayList<WorkFlowProcessRest>();
        try {
            Context context = obtainContext();
            UUID statuscloseid=WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
            UUID statusdraft=WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            WorkFlowProcessMaster workFlowProcessMaster= workFlowProcessMasterService.findByName(context,"Workflow Type");
            UUID statusdraftid=null;
            if(workFlowProcessMaster!=null){
                statusdraftid= workFlowProcessMasterValueService.findByName(context,"Draft",workFlowProcessMaster).getID();
            }
            System.out.println("statuscloseid>>"+statuscloseid);
            System.out.println("statusdraftid>>"+statusdraftid);
            System.out.println("statusdraft>>"+statusdraft);
            int count=workflowProcessService.countfindDraftPending(context,context.getCurrentUser().getID(),statuscloseid,statusdraftid,statusdraft);
            List<WorkflowProcess> workflowProcesses= workflowProcessService.findDraftPending(context,context.getCurrentUser().getID(),statuscloseid,statusdraftid,statusdraft,Math.toIntExact(pageable.getOffset()),pageable.getPageSize());
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context,d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(workflowsRes, pageable,count);
            // return converter.toRestPage(workflowProcesses, pageable,count , utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "getReferWorkflow")
    public Page<WorkflowProcessDTO> getReferWorkflow(Pageable pageable) {
        List<WorkFlowProcessRest> workflowsRes=new ArrayList<WorkFlowProcessRest>();
        List<WorkflowProcessDTO> workflowsRes1=new ArrayList<WorkflowProcessDTO>();
        try {
            Context context = obtainContext();
            UUID referstatusid=WorkFlowStatus.REFER.getUserTypeFromMasterValue(context).get().getID();
            UUID statusdraft=WorkFlowStatus.DRAFT.getUserTypeFromMasterValue(context).get().getID();
            WorkFlowProcessMaster workFlowProcessMaster= workFlowProcessMasterService.findByName(context,"Workflow Type");
            UUID statusdraftid=null;
            if(workFlowProcessMaster!=null){
                statusdraftid= workFlowProcessMasterValueService.findByName(context,"Draft",workFlowProcessMaster).getID();
            }
            System.out.println("statuscloseid>>"+referstatusid);
            System.out.println("statusdraftid>>"+statusdraftid);
            System.out.println("statusdraft>>"+statusdraft);
            int count=workflowProcessService.countRefer(context,context.getCurrentUser().getID(),referstatusid,statusdraftid,statusdraft);
            List<WorkflowProcess> workflowProcesses= workflowProcessService.findReferList(context,context.getCurrentUser().getID(),referstatusid,statusdraftid,statusdraft,Math.toIntExact(pageable.getOffset()),pageable.getPageSize());
            workflowsRes = workflowProcesses.stream().map(d -> {
                return workFlowProcessConverter.convertByDashbord(context,d, utils.obtainProjection());
            }).collect(toList());

            return new PageImpl(workflowsRes, pageable,count);

            //return converter.toRestPage(workflowProcesses, pageable,count , utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }
    @SearchRestMethod(name = "getDocumentByItemID")
    public WorkFlowProcessRest getDocumentByItemID(@Parameter(value = "itemid", required = true) UUID itemid) {
        try {
            Context context = obtainContext();
            WorkflowProcess witems = workflowProcessService.getNoteByItemsid(context, itemid);
            return workFlowProcessConverter.convert(witems, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @Override
    public Class<WorkFlowProcessRest> getDomainClass() {
        return null;
    }
    @Override
    @PreAuthorize("hasPermission(#id, 'WORKSPACEITEM', 'WRITE')")
    protected WorkFlowProcessRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessRest workFlowProcessRest = null;
        WorkflowProcess workflowProcess=null;
        try {
            workFlowProcessRest = mapper.readValue(req.getInputStream(), WorkFlowProcessRest.class);

            System.out.println(">>>>>>>>>>>>>>>>>>>json"+workFlowProcessRest);
            Set<ConstraintViolation<WorkFlowProcessRest>> violations=validatorFactory.getValidator().validate(workFlowProcessRest);
            if (!violations.isEmpty()){
                //throw new WorkFlowValiDationException(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList()));
            }
            boolean isDraft=workFlowProcessRest.getDraft();
            if(isDraft){
                workFlowProcessRest.getWorkflowProcessEpersonRests().clear();
                //clear user if workflowis Draft
            }
            //set submitorUser
            if(context.getCurrentUser() != null && !isDraft){
                WorkflowProcessEpersonRest workflowProcessEpersonSubmitor=new WorkflowProcessEpersonRest();
                EPersonRest ePersonRest=new EPersonRest();
                ePersonRest.setUuid(context.getCurrentUser().getID().toString());
                workflowProcessEpersonSubmitor.setIndex(0);
                Optional<WorkFlowProcessMasterValue> workFlowUserTypOptional = WorkFlowUserType.INITIATOR.getUserTypeFromMasterValue(context);
                if(workFlowUserTypOptional.isPresent()){
                    workflowProcessEpersonSubmitor.setUserType(workFlowProcessMasterValueConverter.convert(workFlowUserTypOptional.get(),utils.obtainProjection()));
                }
                workflowProcessEpersonSubmitor.setePersonRest(ePersonRest);
                workFlowProcessRest.getWorkflowProcessEpersonRests().add(workflowProcessEpersonSubmitor);
            }
            workflowProcess= createworkflowProcessFromRestObject(context,workFlowProcessRest);
            workFlowProcessRest=workFlowProcessConverter.convert(workflowProcess,utils.obtainProjection());
            try {
                System.out.println("isDraft:::"+isDraft);
                if(!isDraft) {
                    WorkFlowAction create= WorkFlowAction.CREATE;
                    create.perfomeAction(context,workflowProcess,workFlowProcessRest);
                }
                context.commit();
            }catch (RuntimeException | SQLException e){
                e.printStackTrace();
                throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
            }
        } catch (RuntimeException | IOException e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return workFlowProcessRest;
    }
    private WorkflowProcess createworkflowProcessFromRestObject(Context context, WorkFlowProcessRest workFlowProcessRest) throws AuthorizeException {
        WorkflowProcess workflowProcess =null;
        try {

            workflowProcess=workFlowProcessConverter.convert(workFlowProcessRest,context);
            Optional<WorkflowProcessSenderDiary> workflowProcessSenderDiaryOptional=Optional.ofNullable(workflowProcessSenderDiaryService.findByEmailID(context,workflowProcess.getWorkflowProcessSenderDiary().getEmail()));
            if(workflowProcessSenderDiaryOptional.isPresent()){
                workflowProcess.setWorkflowProcessSenderDiary(workflowProcessSenderDiaryOptional.get());
            }
            WorkFlowProcessMasterValue workflowstatusopOptionalWorkFlowProcessMasterValue=null;
            System.out.println("workFlowProcessRest.getDraft()::"+workFlowProcessRest.getDraft());
            if(!workFlowProcessRest.getDraft()){
                System.out.println(">>>>>>>>>>>>>>>>>>>>"+WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get().getPrimaryvalue());
                workflowstatusopOptionalWorkFlowProcessMasterValue =WorkFlowStatus.INPROGRESS.getUserTypeFromMasterValue(context).get();
            }else{
                workflowstatusopOptionalWorkFlowProcessMasterValue =WorkFlowStatus.SUSPEND.getUserTypeFromMasterValue(context).get();
            }
            if(workflowstatusopOptionalWorkFlowProcessMasterValue!=null) {
                System.out.println(">>>>>>>>>>>>>>>>>{{}}>>>"+workflowstatusopOptionalWorkFlowProcessMasterValue.getPrimaryvalue());

                workflowProcess.setWorkflowStatus(workflowstatusopOptionalWorkFlowProcessMasterValue);
            }
            workflowProcess = workflowProcessService.create(context,workflowProcess);
            WorkflowProcess finalWorkflowProcess = workflowProcess;
            workflowProcess.setWorkflowProcessReferenceDocs(workFlowProcessRest.getWorkflowProcessReferenceDocRests().stream().map(d -> {
                try {
                    WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocConverter.convertByService(context, d);
                    workflowProcessReferenceDoc.setWorkflowProcess(finalWorkflowProcess);
                    return workflowProcessReferenceDoc;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(toList()));
            System.out.println("workflowProcess::: update ......");
            workflowProcessService.update(context,workflowProcess);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return workflowProcess;
    }

}