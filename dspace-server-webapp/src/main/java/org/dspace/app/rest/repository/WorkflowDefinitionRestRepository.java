/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.dspace.app.rest.converter.WorkFlowProcessDefinitionConverter;
import org.dspace.app.rest.converter.WorkFlowProcessEpersonConverter;
import org.dspace.app.rest.converter.WorkFlowProcessMasterValueConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkFlowProcessMasterValue;
import org.dspace.content.WorkflowProcessDefinition;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.content.service.WorkflowProcessDefinitionService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.apache.logging.log4j.util.Strings.isBlank;

/**
 * This is the rest repository responsible for managing WorkflowDefinition Rest objects
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@Component(WorkFlowProcessDefinitionRest.CATEGORY + "." + WorkFlowProcessDefinitionRest.NAME)
public class WorkflowDefinitionRestRepository extends DSpaceObjectRestRepository<WorkflowProcessDefinition, WorkFlowProcessDefinitionRest> {

    @Autowired
    private WorkflowProcessDefinitionService workflowProcessDefinitionService;
    @Autowired
    private EPersonService ePersonService;
    @Autowired
    private WorkFlowProcessMasterValueService workFlowProcessMasterValueService;

    @Autowired
    private WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;

    @Autowired
    WorkFlowProcessEpersonConverter workFlowProcessEpersonConverter;
    @Autowired
    WorkFlowProcessDefinitionConverter workFlowProcessDefinitionConverter;

    public WorkflowDefinitionRestRepository(WorkflowProcessDefinitionService dso) {
        super(dso);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    public WorkFlowProcessDefinitionRest findOne(Context context, UUID id) {
        WorkFlowProcessDefinitionRest workflowProcessDefinitionRest = null;
        try {
            Optional<WorkflowProcessDefinition> workflowProcessDefinitionOption = Optional.ofNullable(workflowProcessDefinitionService.find(context, id));
            if (workflowProcessDefinitionOption.isPresent()) {
                workflowProcessDefinitionRest = converter.toRest(workflowProcessDefinitionOption.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workflowProcessDefinitionRest;
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    @Override
    public Page<WorkFlowProcessDefinitionRest> findAll(Context context, Pageable pageable) {
        try {
            List<WorkflowProcessDefinition> workflowProcessDefinitions = workflowProcessDefinitionService.findAll(context, pageable.getPageSize(), Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(workflowProcessDefinitions, pageable, 0, utils.obtainProjection());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<WorkFlowProcessDefinitionRest> getDomainClass() {
        return null;
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    @Override
    protected WorkFlowProcessDefinitionRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkFlowProcessDefinitionRest workflowProcessDefinitionRest = null;
        WorkflowProcessDefinition workflowProcessDefinition = null;
        try {
            workflowProcessDefinitionRest = mapper.readValue(req.getInputStream(), WorkFlowProcessDefinitionRest.class);
            workflowProcessDefinition = createworkflowProcessDefinitionFromRestObject(context, workflowProcessDefinitionRest);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return converter.toRest(workflowProcessDefinition, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    protected WorkFlowProcessDefinitionRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                                JsonNode jsonNode) throws SQLException, AuthorizeException {
        WorkFlowProcessDefinitionRest workflowProcessDefinitionRest = new Gson().fromJson(jsonNode.toString(), WorkFlowProcessDefinitionRest.class);
        if (isBlank(workflowProcessDefinitionRest.getWorkflowprocessdefinitionname())) {
            throw new UnprocessableEntityException("Documenttypename element (in request body) cannot be blank");
        }
        WorkflowProcessDefinition workflowProcessDefinition = workflowProcessDefinitionService.find(context, id);
        if (workflowProcessDefinition == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }
        workflowProcessDefinition  = workFlowProcessDefinitionConverter.convert(context, workflowProcessDefinitionRest);
        workflowProcessDefinitionService.update(context, workflowProcessDefinition);
        context.commit();
        return converter.toRest(workflowProcessDefinition, utils.obtainProjection());
    }

    private WorkflowProcessDefinition createworkflowProcessDefinitionFromRestObject(Context context, WorkFlowProcessDefinitionRest workflowProcessDefinitionRest) throws AuthorizeException, SQLException {
        return workflowProcessDefinitionService.create(context, workFlowProcessDefinitionConverter.convert(context, workflowProcessDefinitionRest));
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, uuid, patch);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'DELETE')")
    protected void delete(Context context, UUID id) throws AuthorizeException {
        WorkflowProcessDefinition workflowProcessDefinition = null;
        try {
            workflowProcessDefinition = workflowProcessDefinitionService.find(context, id);
            if (workflowProcessDefinition == null) {
                throw new ResourceNotFoundException(WorkFlowProcessDefinitionRest.CATEGORY + "." + WorkFlowProcessDefinitionRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            workflowProcessDefinitionService.delete(context, workflowProcessDefinition);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
