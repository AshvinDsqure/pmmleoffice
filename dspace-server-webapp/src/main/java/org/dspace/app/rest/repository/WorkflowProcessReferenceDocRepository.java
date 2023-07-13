/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.WorkFlowProcessConverter;
import org.dspace.app.rest.converter.WorkFlowProcessMasterValueConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.enums.WorkFlowAction;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.jbpm.JbpmServerImpl;
import org.dspace.app.rest.model.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(WorkflowProcessReferenceDocRest.CATEGORY + "." + WorkflowProcessReferenceDocRest.NAME)
public class WorkflowProcessReferenceDocRepository extends DSpaceObjectRestRepository<WorkflowProcessReferenceDoc, WorkflowProcessReferenceDocRest> {

    private static final Logger log = LogManager.getLogger(WorkflowProcessReferenceDocRepository.class);
    @Autowired
    private WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    @Autowired
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    @Autowired
    private WorkflowProcessReferenceDocService workflowProcessReferenceDocService;

    @Autowired
    private WorkFlowProcessHistoryService workFlowProcessHistoryService;

    @Autowired
    private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;


    @Autowired
    ModelMapper modelMapper;

    public WorkflowProcessReferenceDocRepository(WorkflowProcessReferenceDocService dsoService) {
        super(dsoService);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'WORKSPACEITEM', 'WRITE')")
    public WorkflowProcessReferenceDocRest findOne(Context context, UUID id) throws SQLException {
        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, id);
        return workflowProcessReferenceDocConverter.convert(workflowProcessReferenceDoc, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'WORKSPACEITEM', 'WRITE')")
    public Page<WorkflowProcessReferenceDocRest> findAll(Context context, Pageable pageable) {
        try {
            List<WorkflowProcessReferenceDoc> workflowProcessReferenceDocs = workflowProcessReferenceDocService.findAll(context, pageable.getPageSize(), Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(workflowProcessReferenceDocs, pageable, 0, utils.obtainProjection());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<WorkflowProcessReferenceDocRest> getDomainClass() {
        return null;
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'WORKSPACEITEM', 'WRITE')")
    protected WorkflowProcessReferenceDocRest createAndReturn(Context context)
            throws AuthorizeException {
        return null;
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'WORKSPACEITEM', 'WRITE')")

    protected void delete(Context context, UUID id) throws AuthorizeException {

        WorkflowProcessReferenceDoc workflowProcessReferenceDoc = null;
        try {
            workflowProcessReferenceDoc = workflowProcessReferenceDocService.find(context, id);
            storeWorkFlowHistoryforDocumentDelete(context, workflowProcessReferenceDoc);
            if (workflowProcessReferenceDoc == null) {
                throw new ResourceNotFoundException(WorkflowProcessReferenceDocRest.CATEGORY + "." + WorkflowProcessReferenceDocRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            workflowProcessReferenceDocService.delete(context, workflowProcessReferenceDoc);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SearchRestMethod(name = "getDocumentBydraftTypeId")
    public Page<WorkflowProcessReferenceDocVersionRest> getDocumentBydraftTypeId(@Parameter(value = "drafttypeid", required = true) UUID drafttypeid, Pageable pageable) {
        try {
            Context context = obtainContext();
            long total = workflowProcessReferenceDocService.countDocumentByType(context, drafttypeid);
            List<WorkflowProcessReferenceDoc> witems = workflowProcessReferenceDocService.getDocumentByType(context, drafttypeid, Math.toIntExact(pageable.getOffset()),
                    Math.toIntExact(pageable.getPageSize()));
            System.out.println("lisy" + witems);
            return converter.toRestPage(witems, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SearchRestMethod(name = "getDocumentByItemID")
    public Page<WorkflowProcessReferenceDocVersionRest> getDocumentByItemID(@Parameter(value = "itemid", required = true) UUID itemid, Pageable pageable) {
        try {
            System.out.println("get Correspondence Lis");
            Context context = obtainContext();
            long total = workflowProcessReferenceDocService.countDocumentByItemid(context, itemid);
            List<WorkflowProcessReferenceDoc> witems = workflowProcessReferenceDocService.getDocumentByItemid(context, itemid, Math.toIntExact(pageable.getOffset()),
                    Math.toIntExact(pageable.getPageSize()));
            List<WorkflowProcessReferenceDoc> filterlist = witems.stream()
                    .filter(f -> f.getBitstream() != null)
                    .filter(f -> !f.getBitstream().getName().contains("Note#")).filter(f -> f.getDrafttype() != null)
                    .filter(f -> !f.getDrafttype().getPrimaryvalue().equalsIgnoreCase("Note"))
                    .collect(Collectors.toList());
            return converter.toRestPage(filterlist, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void storeWorkFlowHistoryforDocumentDelete(Context context, WorkflowProcessReferenceDoc doc) throws Exception {
        System.out.println("::::::IN :storeWorkFlowHistory::delete::Document:::::: ");
        try {
            WorkflowProcess workflowProcess = doc.getWorkflowProcess();
            WorkFlowProcessHistory workFlowAction = null;
            workFlowAction = new WorkFlowProcessHistory();
            WorkFlowProcessMaster workFlowProcessMaster = WorkFlowAction.MASTER.getMaster(context);
            workFlowAction.setWorkflowProcessEpeople(workflowProcess.getWorkflowProcessEpeople().stream().filter(d -> d.getOwner() != null).filter(d -> d.getOwner()).findFirst().get());
            WorkFlowProcessMasterValue workFlowProcessMasterValue = workFlowProcessMasterValueService.findByName(context, WorkFlowAction.REJECTED.getAction(), workFlowProcessMaster);
            workFlowAction.setActionDate(new Date());
            workFlowAction.setAction(workFlowProcessMasterValue);
            workFlowAction.setWorkflowProcess(workflowProcess);
            workFlowAction.setComment("Deleted " + doc.getDrafttype().getPrimaryvalue());
            workFlowProcessHistoryService.create(context, workFlowAction);
            System.out.println("::::::OUT :storeWorkFlowHistory: delete :Document:::::::: ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SearchRestMethod(name = "getDocumentByworkflowprocessid")
    public Page<WorkflowProcessReferenceDocVersionRest> getDocumentByworkflowprocessid(@Parameter(value = "workflowprocessid", required = true) UUID workflowprocessid, Pageable pageable) {
        try {
            System.out.println("get Correspondence Lis");
            Context context = obtainContext();
            List<WorkflowProcessReferenceDoc> witems = workflowProcessReferenceDocService.getDocumentByworkflowprocessid(context, workflowprocessid);
            return converter.toRestPage(witems, pageable, 100, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
