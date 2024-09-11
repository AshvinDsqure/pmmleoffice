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
import org.dspace.app.rest.converter.EPersonConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.converter.WorkflowProcessTemplateConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessDefinitionRest;
import org.dspace.app.rest.model.WorkFlowProcessRest;
import org.dspace.app.rest.model.WorkflowProcessTemplateRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkflowProcessTemplate;
import org.dspace.content.service.WorkflowProcessTemplateService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
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

/**
 * This is the rest repository responsible for managing WorkflowDefinition Rest objects
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@Component(WorkflowProcessTemplateRest.CATEGORY + "." + WorkflowProcessTemplateRest.NAME)
public class WorkflowProcessTemplateRestRepository extends DSpaceObjectRestRepository<WorkflowProcessTemplate, WorkflowProcessTemplateRest> {
    @Autowired
    private WorkflowProcessTemplateService workflowProcessTemplateService;
    @Autowired
    private EPersonService ePersonService;
    @Autowired
    private EPersonConverter ePersonConverter;
    @Autowired
    private WorkflowProcessTemplateConverter workflowProcessTemplateConverter;
    @Autowired
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;

    public WorkflowProcessTemplateRestRepository(WorkflowProcessTemplateService dso) {
        super(dso);
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public WorkflowProcessTemplateRest findOne(Context context, UUID id) {
        context.turnOffAuthorisationSystem();
        WorkflowProcessTemplateRest workflowProcessTemplateRest = null;
        try {
            Optional<WorkflowProcessTemplate> workflowProcessDefinitionOption = Optional.ofNullable(workflowProcessTemplateService.find(context, id));
            if (workflowProcessDefinitionOption.isPresent()) {
                workflowProcessTemplateRest = converter.toRest(workflowProcessDefinitionOption.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workflowProcessTemplateRest;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @Override
    public Page<WorkflowProcessTemplateRest> findAll(Context context, Pageable pageable) {
        try {
            List<WorkflowProcessTemplate> workflowProcessDefinitions = workflowProcessTemplateService.findAll(context, pageable.getPageSize(), Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(workflowProcessDefinitions, pageable, 10, utils.obtainProjection());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<WorkflowProcessTemplateRest> getDomainClass() {
        return null;
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @Override
    protected WorkflowProcessTemplateRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        WorkflowProcessTemplateRest wroWorkflowProcessTemplateRest = null;
        WorkflowProcessTemplate workflowProcessTemplate = null;
        try {
            wroWorkflowProcessTemplateRest = mapper.readValue(req.getInputStream(), WorkflowProcessTemplateRest.class);
            workflowProcessTemplate = createworkflowProcessDefinitionFromRestObject(context, wroWorkflowProcessTemplateRest);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return converter.toRest(workflowProcessTemplate, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")protected WorkflowProcessTemplateRest
    put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                              JsonNode jsonNode) throws SQLException, AuthorizeException {
        WorkflowProcessTemplateRest workflowProcessTemplateRest = new Gson().fromJson(jsonNode.toString(), WorkflowProcessTemplateRest.class);
        System.out.println("in update workflowProcessTemplateRest");
        WorkflowProcessTemplate workflowProcessTemplate = workflowProcessTemplateService.find(context, id);
        if (workflowProcessTemplate == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }
        workflowProcessTemplate = workflowProcessTemplateConverter.convert(context,workflowProcessTemplate,workflowProcessTemplateRest);
        workflowProcessTemplateService.update(context, workflowProcessTemplate);
        WorkflowProcessTemplateRest workflowProcessTemplateRest1= workflowProcessTemplateConverter.convert(workflowProcessTemplate,utils.obtainProjection());
        context.commit();
        System.out.println("in update workflowProcessTemplateRest done");
        return workflowProcessTemplateRest1;
    }

    private WorkflowProcessTemplate createworkflowProcessDefinitionFromRestObject(Context context, WorkflowProcessTemplateRest workflowProcessTemplateRest) throws AuthorizeException {
        WorkflowProcessTemplate workflowProcessTemplate = new WorkflowProcessTemplate();
        try {
            workflowProcessTemplate = workflowProcessTemplateConverter.convert(context,workflowProcessTemplateRest);
            workflowProcessTemplate = workflowProcessTemplateService.create(context, workflowProcessTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return workflowProcessTemplate;
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')") protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, uuid, patch);
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected void delete(Context context, UUID id) throws AuthorizeException {
        try {
            WorkflowProcessTemplate workflowProcessTemplate = workflowProcessTemplateService.find(context, id);
            if (workflowProcessTemplate == null) {
                throw new ResourceNotFoundException(WorkFlowProcessDefinitionRest.CATEGORY + "." + WorkFlowProcessDefinitionRest.NAME +
                        " with id: " + id + " not found");
            }
            workflowProcessTemplate.setIsdelete(true);
            workflowProcessTemplateService.update(context,workflowProcessTemplate);
            context.commit();
            System.out.println("delete done!");
        } catch (SQLException  e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "getDocumentByItemID")
    public Page<WorkflowProcessTemplate> getDocumentByItemID(@Parameter(value = "template", required = true) UUID template, Pageable pageable) {
        try {
            Context context = obtainContext();
            System.out.println("template id :"+template);
            List<WorkflowProcessTemplateRest>workflowProcessTemplateRests= new ArrayList<WorkflowProcessTemplateRest>();;
            long total = workflowProcessTemplateService.getCountWorkflowProcessByTemplate(context, template);
            List<WorkflowProcessTemplate> witems = workflowProcessTemplateService.getWorkflowProcessByTemplate(context,template, Math.toIntExact(pageable.getOffset()),
                    Math.toIntExact(pageable.getPageSize()));

            System.out.println("size :"+witems.size());
            workflowProcessTemplateRests = witems.stream().map(d -> {
                return workflowProcessTemplateConverter.convert(d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("out getDraftByWorkFlowTypeID");
            return new PageImpl(workflowProcessTemplateRests, pageable, total);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
