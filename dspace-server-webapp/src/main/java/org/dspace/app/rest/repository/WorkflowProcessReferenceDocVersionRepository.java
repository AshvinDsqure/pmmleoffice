/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocVersionConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocVersionRest;
import org.dspace.app.rest.model.WorkflowProcessSenderDiaryRest;
import org.dspace.app.rest.utils.DateUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkflowProcessReferenceDoc;
import org.dspace.content.WorkflowProcessReferenceDocVersion;
import org.dspace.content.service.WorkflowProcessReferenceDocService;
import org.dspace.content.service.WorkflowProcessReferenceDocVersionService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component(WorkflowProcessReferenceDocVersionRest.CATEGORY + "." + WorkflowProcessReferenceDocVersionRest.NAME)

public class WorkflowProcessReferenceDocVersionRepository extends DSpaceObjectRestRepository<WorkflowProcessReferenceDocVersion, WorkflowProcessReferenceDocVersionRest> {
    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(WorkflowProcessReferenceDocVersionRepository.class);
    @Autowired
    WorkflowProcessReferenceDocVersionService WorkflowProcessReferenceDocVersionService;

    @Autowired
    WorkflowProcessReferenceDocService workflowProcessReferenceDocService;
    @Autowired
    WorkflowProcessReferenceDocVersionConverter workflowProcessReferenceDocVersionConverter;


    @Autowired
    WorkflowProcessReferenceDocVersionConverter WorkflowProcessReferenceDocVersionConverter;

    public WorkflowProcessReferenceDocVersionRepository(WorkflowProcessReferenceDocVersionService dsoService) {
        super(dsoService);
    }

    @Override
    protected WorkflowProcessReferenceDocVersionRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkflowProcessReferenceDocVersionRest WorkflowProcessReferenceDocVersionRest = null;
        WorkflowProcessReferenceDocVersion WorkflowProcessReferenceDocVersion = null;
        try {
            WorkflowProcessReferenceDocVersionRest = mapper.readValue(req.getInputStream(), WorkflowProcessReferenceDocVersionRest.class);
            WorkflowProcessReferenceDocVersion = createWorkflowProcessReferenceDocVersionFromRestObject(context, WorkflowProcessReferenceDocVersionRest);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return WorkflowProcessReferenceDocVersionConverter.convert(WorkflowProcessReferenceDocVersion, utils.obtainProjection());
    }

    private WorkflowProcessReferenceDocVersion createWorkflowProcessReferenceDocVersionFromRestObject(Context context, WorkflowProcessReferenceDocVersionRest WorkflowProcessReferenceDocVersionRest) throws AuthorizeException {
        WorkflowProcessReferenceDocVersion workflowProcessReferenceDocVersion = new WorkflowProcessReferenceDocVersion();
        try {
            workflowProcessReferenceDocVersion = WorkflowProcessReferenceDocVersionConverter.convert(context, WorkflowProcessReferenceDocVersionRest);
            workflowProcessReferenceDocVersion.setCreator(context.getCurrentUser());
            workflowProcessReferenceDocVersion=  WorkflowProcessReferenceDocVersionService.create(context, workflowProcessReferenceDocVersion);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return workflowProcessReferenceDocVersion;
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected WorkflowProcessReferenceDocVersionRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                             JsonNode jsonNode) throws SQLException, AuthorizeException {
        log.info("::::::start::::put::::::::::");
        WorkflowProcessReferenceDocVersionRest workflowProcessReferenceDocVersionRest = null;//new Gson().fromJson(jsonNode.toString(), WorkflowProcessReferenceDocVersionRest.class);
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        //= new Gson().fromJson(jsonNode.toString(), WorkflowProcessSenderDiaryRest.class);
        try {
            workflowProcessReferenceDocVersionRest = jsonObjectMapper.readValue(jsonNode.toString(), WorkflowProcessReferenceDocVersionRest.class);
        } catch (Exception e) {

        }
        WorkflowProcessReferenceDocVersion workflowProcessReferenceDocVersion = WorkflowProcessReferenceDocVersionService.find(context, id);
        if (workflowProcessReferenceDocVersion == null) {
            System.out.println("WorkflowProcessReferenceDocVersionrest id ::: is Null  WorkflowProcessReferenceDocVersionrest tye null" + id);
            throw new ResourceNotFoundException("WorkflowProcessReferenceDocVersionrest  field with id: " + id + " not found");
        }
        System.out.println("is active :"+workflowProcessReferenceDocVersion.getIsactive());
        workflowProcessReferenceDocVersion.setIsactive(workflowProcessReferenceDocVersionRest.getIsactive());
        WorkflowProcessReferenceDocVersionService.update(context, workflowProcessReferenceDocVersion);
        workflowProcessReferenceDocVersionRest =converter.toRest(workflowProcessReferenceDocVersion, utils.obtainProjection());
        context.commit();
        return workflowProcessReferenceDocVersionRest;
    }
    @Override
    public WorkflowProcessReferenceDocVersionRest findOne(Context context, UUID uuid) {
        WorkflowProcessReferenceDocVersionRest WorkflowProcessReferenceDocVersionRest = null;
        try {
            Optional<WorkflowProcessReferenceDocVersion> WorkflowProcessReferenceDocVersion = Optional.ofNullable(WorkflowProcessReferenceDocVersionService.find(context, uuid));
            if (WorkflowProcessReferenceDocVersion.isPresent()) {
                WorkflowProcessReferenceDocVersionRest = converter.toRest(WorkflowProcessReferenceDocVersion.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return WorkflowProcessReferenceDocVersionRest;
    }
    @Override
    public Page<WorkflowProcessReferenceDocVersionRest> findAll(Context context, Pageable pageable) throws SQLException {
       int total = WorkflowProcessReferenceDocVersionService.countRow(context);
        List<WorkflowProcessReferenceDocVersion> workFlowProcessHistories = WorkflowProcessReferenceDocVersionService.findAll(context,
                Math.toIntExact(pageable.getPageSize()), Math.toIntExact(pageable.getOffset()));
        return converter.toRestPage(workFlowProcessHistories, pageable, total, utils.obtainProjection());
    }
    protected void delete(Context context, UUID id) throws AuthorizeException {
        WorkflowProcessReferenceDocVersion WorkflowProcessReferenceDocVersion = null;
        try {
            WorkflowProcessReferenceDocVersion = WorkflowProcessReferenceDocVersionService.find(context, id);
            if (WorkflowProcessReferenceDocVersion == null) {
                throw new ResourceNotFoundException(WorkflowProcessReferenceDocVersionRest.CATEGORY + "." + WorkflowProcessReferenceDocVersionRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            WorkflowProcessReferenceDocVersionService.delete(context, WorkflowProcessReferenceDocVersion);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getVersionByDocId")
    public Page<WorkflowProcessReferenceDocVersionRest> getVersionByDocId(@Parameter(value = "documentid", required = true) UUID documentid, Pageable pageable) {
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            List<WorkflowProcessReferenceDocVersionRest>rests = new ArrayList<>();
            long total = WorkflowProcessReferenceDocVersionService.countDocumentID(context, documentid);
            List<WorkflowProcessReferenceDocVersion> witems = WorkflowProcessReferenceDocVersionService.getDocVersionBydocumentID(context, documentid, Math.toIntExact(pageable.getOffset()),
                    Math.toIntExact(pageable.getPageSize()));
            rests = witems.stream().map(d -> {
                return workflowProcessReferenceDocVersionConverter.convert(d, utils.obtainProjection());
            }).collect(toList());
            log.info("in getDraftNotePendingWorkflow stop!");
            return new PageImpl(rests, pageable, total);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "updateVersion")
        public WorkflowProcessReferenceDocVersionRest updateVersion(@Parameter(value = "versionid", required = true) UUID versionid) {
        try {
            System.out.println("in update version id:"+versionid);
            Context context = obtainContext();
            WorkflowProcessReferenceDocVersion v=  WorkflowProcessReferenceDocVersionService.find(context, versionid);
            WorkflowProcessReferenceDoc doc=  workflowProcessReferenceDocService.find(context,v.getWorkflowProcessReferenceDoc().getID());
            for (WorkflowProcessReferenceDocVersion vv:doc.getWorkflowProcessReferenceDocVersion()) {
                WorkflowProcessReferenceDocVersion dd=  WorkflowProcessReferenceDocVersionService.find(context, vv.getID());
                dd.setIsactive(false);
                WorkflowProcessReferenceDocVersionService.update(context,dd);
            }
            System.out.println("sdsdsd"+v.getIsactive());
            if(v.getIsactive()){
                v.setIsactive(false);
            }else {
                v.setIsactive(true);
            }
            WorkflowProcessReferenceDocVersionService.update(context,v);
             System.out.println("is done :"+v.getIsactive());
            WorkflowProcessReferenceDocVersionRest workflowProcessReferenceDocVersionRest= converter.toRest(v, utils.obtainProjection());
            context.commit();
           return workflowProcessReferenceDocVersionRest;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (AuthorizeException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public Class<WorkflowProcessReferenceDocVersionRest> getDomainClass() {
        return null;
    }
}
