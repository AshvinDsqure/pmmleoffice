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
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.WorkFlowProcessDefinitionRest;
import org.dspace.app.rest.model.WorkflowProcessEpersonRest;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocRest;
import org.dspace.app.rest.model.WorkflowProcessReferenceDocVersionRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkflowProcessEperson;
import org.dspace.content.WorkflowProcessReferenceDoc;
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
    private EPersonService ePersonService;
    @Autowired
    private WorkFlowProcessEpersonConverter workflowProcessEpersonConverter;
    @Autowired
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    public WorkflowProcessEpersonRestRepository(WorkflowProcessEpersonService dso) {
        super(dso);
    }
    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    public WorkflowProcessEpersonRest findOne(Context context, UUID id) {
        WorkflowProcessEpersonRest WorkflowProcessEpersonRest=null;
        try {
            Optional<WorkflowProcessEperson> workflowProcessDefinitionOption = Optional.ofNullable(workflowProcessEpersonService.find(context, id));
            if(workflowProcessDefinitionOption.isPresent()){
                System.out.println(">>>>>>>>>>>>>>>>>>name"+workflowProcessDefinitionOption.get());
                WorkflowProcessEpersonRest =converter.toRest(workflowProcessDefinitionOption.get(),utils.obtainProjection());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return WorkflowProcessEpersonRest;
    }
    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    @Override
    public Page<WorkflowProcessEpersonRest> findAll(Context context, Pageable pageable) {
        try {
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
    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
    protected WorkflowProcessEpersonRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                          JsonNode jsonNode) throws SQLException, AuthorizeException {
        WorkflowProcessEpersonRest rest = new Gson().fromJson(jsonNode.toString(), WorkflowProcessEpersonRest.class);

        System.out.println(":::::::::::::::E persion Update");
        WorkflowProcessEperson workflowProcessEperson = workflowProcessEpersonService.find(context, id);
        if (workflowProcessEperson == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }
        if(workflowProcessEperson.getIsapproved()){
            System.out.println("already Approved !");
        }else{
            workflowProcessEperson.setIsapproved(true);
        }
        workflowProcessEpersonService.update(context, workflowProcessEperson);
        System.out.println("::::::::::::::E persion Update done!");
        return converter.toRest(workflowProcessEperson, utils.obtainProjection());
    }
    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'STATUS') || hasPermission(#id, 'ITEM', 'READ')")
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
            workflowProcessEpersonService.delete(context, eperson);
            context.commit();
            System.out.println("delete done !");
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
