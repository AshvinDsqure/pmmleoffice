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
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.WorkFlowProcessCommentConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessCommentRest;
import org.dspace.app.rest.model.WorkFlowProcessHistoryRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessComment;
import org.dspace.content.service.WorkFlowProcessCommentService;
import org.dspace.core.Context;
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

@Component(WorkFlowProcessCommentRest.CATEGORY + "." + WorkFlowProcessCommentRest.NAME)

public class WorkFlowProcessCommentRepository extends DSpaceObjectRestRepository<WorkFlowProcessComment, WorkFlowProcessCommentRest> {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WorkFlowProcessCommentRepository.class);
    @Autowired
    WorkFlowProcessCommentService workFlowProcessCommentService;
    @Autowired
    WorkFlowProcessCommentConverter workFlowProcessCommentConverter;
    public WorkFlowProcessCommentRepository(WorkFlowProcessCommentService dsoService) {
        super(dsoService);
    }
    @Override
    protected WorkFlowProcessCommentRest createAndReturn(Context context)
            throws AuthorizeException {
        log.info("::::::start::::createAndReturn::::::::::");
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessCommentRest workFlowProcessCommentRest = null;
        WorkFlowProcessComment workFlowProcessComment = null;
        try {
            workFlowProcessCommentRest = mapper.readValue(req.getInputStream(), WorkFlowProcessCommentRest.class);
            workFlowProcessComment = createWorkFlowProcessCommentFromRestObject(context, workFlowProcessCommentRest);
        } catch (Exception e1) {
            log.info("::::::error::::createAndReturn::::::::::");
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        log.info("::::::complate::::createAndReturn::::::::::");
        return converter.toRest(workFlowProcessComment, utils.obtainProjection());
    }
    private WorkFlowProcessComment createWorkFlowProcessCommentFromRestObject(Context context, WorkFlowProcessCommentRest workFlowProcessCommentRest) throws AuthorizeException {
        log.info("::::::start::::createWorkFlowProcessCommentFromRestObject::::::::::");
        WorkFlowProcessComment workFlowProcessComment = new WorkFlowProcessComment();
        try {
            workFlowProcessComment=workFlowProcessCommentConverter.convert(context,workFlowProcessCommentRest);
            workFlowProcessCommentService.create(context, workFlowProcessComment);
        } catch (Exception e) {
            log.info("::::::error::::createWorkFlowProcessCommentFromRestObject::::::::::");
            throw new RuntimeException(e.getMessage(), e);
        }
        log.info("::::::complate::::createWorkFlowProcessCommentFromRestObject::::::::::");
        return workFlowProcessComment;
    }
    @Override
    protected WorkFlowProcessCommentRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                                  JsonNode jsonNode) throws Exception {
        log.info("::::::start::::put::::::::::");
        WorkFlowProcessCommentRest workFlowProcessCommentRest  = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessCommentRest.class);

        WorkFlowProcessComment workFlowProcessComment = workFlowProcessCommentService.find(context, id);
        if (workFlowProcessComment == null) {
            System.out.println("workFlowProcessComment id ::: is Null  workFlowProcessComment tye null"+id);
            throw new ResourceNotFoundException("workFlowProcessComment  field with id: " + id + " not found");
        }
        workFlowProcessComment=workFlowProcessCommentConverter.convert(context,workFlowProcessCommentRest);
        workFlowProcessCommentService.update(context, workFlowProcessComment);
        context.commit();
        log.info("::::::End::::put::::::::::");
        return converter.toRest(workFlowProcessComment, utils.obtainProjection());
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @Override
    public WorkFlowProcessCommentRest findOne(Context context, UUID uuid) {
        context.turnOffAuthorisationSystem();
        WorkFlowProcessCommentRest workFlowProcessCommentRest =null;
        log.info("::::::start::::findOne::::::::::");
        try {
            Optional<WorkFlowProcessComment> workFlowProcessComment = Optional.ofNullable(workFlowProcessCommentService.find(context, uuid));
            if (workFlowProcessComment.isPresent()) {
                workFlowProcessCommentRest = converter.toRest(workFlowProcessComment.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            log.info("::::::error::::findOne::::::::::");
            e.printStackTrace();
        }
        log.info("::::::End::::findOne::::::::::");
        return workFlowProcessCommentRest;
    }
    @Override
    public Page<WorkFlowProcessCommentRest> findAll(Context context, Pageable pageable) throws SQLException {
        int total = workFlowProcessCommentService.countRows(context);
        List<WorkFlowProcessComment>  workFlowProcessComment= workFlowProcessCommentService.findAll(context,
                Math.toIntExact(pageable.getPageSize()), Math.toIntExact(pageable.getOffset()));
        return converter.toRestPage(workFlowProcessComment, pageable, total, utils.obtainProjection());
    }
    protected void delete(Context context, UUID id) throws AuthorizeException {
        log.info("::::::in::::delete::::::::::");
        WorkFlowProcessComment workFlowProcessComment = null;
        try {
            workFlowProcessComment = workFlowProcessCommentService.find(context, id);
            if (workFlowProcessComment == null) {
                log.info("::::::id not found::::delete::::::::::");
                throw new ResourceNotFoundException(WorkFlowProcessCommentRest.CATEGORY + "." + WorkFlowProcessCommentRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workFlowProcessCommentService.delete(context, workFlowProcessComment);
            context.commit();
            log.info(":::::completed:::delete::::::::::");
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getComments")
    public Page<WorkFlowProcessHistoryRest> getComments(@Parameter(value = "workflowprocessid", required = true) UUID workflowprocessid, Pageable pageable) {
        try {
            Context context = obtainContext();
            context.turnOffAuthorisationSystem();
            long total = workFlowProcessCommentService.countComment(context, workflowprocessid);
            List<WorkFlowProcessComment> witems = workFlowProcessCommentService.getComments(context, workflowprocessid);
            return converter.toRestPage(witems, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @Override
    public Class<WorkFlowProcessCommentRest> getDomainClass() {
        return null;
    }
}
