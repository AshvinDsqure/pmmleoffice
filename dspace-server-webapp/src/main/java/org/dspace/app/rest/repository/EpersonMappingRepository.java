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
import org.dspace.app.rest.converter.EpersonMappingConverter;
import org.dspace.app.rest.converter.WorkflowProcessReferenceDocConverter;
import org.dspace.app.rest.exception.AlreadyDataExistException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.EpersonMappingRest;
import org.dspace.app.rest.model.WorkFlowProcessDefinitionRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.EpersonMapping;
import org.dspace.content.service.EpersonMappingService;
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

/**
 * This is the rest repository responsible for managing WorkflowDefinition Rest objects
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@Component(EpersonMappingRest.CATEGORY + "." + EpersonMappingRest.NAME)
public class EpersonMappingRepository extends DSpaceObjectRestRepository<EpersonMapping, EpersonMappingRest> {
    @Autowired
    private EpersonMappingService epersonMappingService;
    @Autowired
    private EPersonService ePersonService;
    @Autowired
    private EpersonMappingConverter epersonMappingConverter;
    @Autowired
    private WorkflowProcessReferenceDocConverter workflowProcessReferenceDocConverter;
    public EpersonMappingRepository(EpersonMappingService dso) {
        super(dso);
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public EpersonMappingRest findOne(Context context, UUID id) {
        EpersonMappingRest EpersonMappingRest=null;
        try {
            Optional<EpersonMapping> workflowProcessDefinitionOption = Optional.ofNullable(epersonMappingService.find(context, id));
            if(workflowProcessDefinitionOption.isPresent()){
                EpersonMappingRest =converter.toRest(workflowProcessDefinitionOption.get(),utils.obtainProjection());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return EpersonMappingRest;
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public Page<EpersonMappingRest> findAll(Context context, Pageable pageable) {
        try {
            List<EpersonMapping> workflowProcessDefinitions= epersonMappingService.findAll(context,pageable.getPageSize(),Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(workflowProcessDefinitions, pageable, 10, utils.obtainProjection());
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException(e.getMessage(),e);
        }
    }
    @Override
    public Class<EpersonMappingRest> getDomainClass() {
        return null;
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @Override
    protected EpersonMappingRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        EpersonMappingRest wroEpersonMappingRest = null;
        EpersonMapping EpersonMapping=null;
        try {
            wroEpersonMappingRest = mapper.readValue(req.getInputStream(), EpersonMappingRest.class);
            EpersonMapping= createworkflowProcessDefinitionFromRestObject(context,wroEpersonMappingRest);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return converter.toRest(EpersonMapping, utils.obtainProjection());
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected EpersonMappingRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                          JsonNode jsonNode) throws SQLException, AuthorizeException {
        EpersonMappingRest EpersonMappingRest = new Gson().fromJson(jsonNode.toString(), EpersonMappingRest.class);
        EpersonMapping EpersonMapping = epersonMappingService.find(context, id);
        if (EpersonMapping == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }

        epersonMappingService.update(context, EpersonMapping);
        return converter.toRest(EpersonMappingRest, utils.obtainProjection());
    }
    private EpersonMapping createworkflowProcessDefinitionFromRestObject(Context context, EpersonMappingRest epersonMappingRest) throws AuthorizeException {
        EpersonMapping epersonMapping =new EpersonMapping();
        try {
            epersonMapping= epersonMappingConverter.convertbycreatenew(context,epersonMappingRest);
            epersonMapping = epersonMappingService.create(context,epersonMapping);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return epersonMapping;
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, uuid, patch);
    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    protected void delete(Context context, UUID id) throws AuthorizeException {
        EpersonMapping epersonMapping = null;
        try {
            epersonMapping = epersonMappingService.find(context, id);
            if (epersonMapping == null) {
                throw new ResourceNotFoundException(WorkFlowProcessDefinitionRest.CATEGORY + "." + WorkFlowProcessDefinitionRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            epersonMappingService.delete(context, epersonMapping);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    @SearchRestMethod(name = "isMapping")
    public EpersonMappingRest isMapping(@Parameter(value = "office", required = true) UUID office,
                                                        @Parameter(value = "department", required = true) UUID department,
                                                        @Parameter(value = "designation", required = true) UUID designation) {
        EpersonMappingRest epersonMappingRest=null;
        Context context = obtainContext();
        try {
            EpersonMapping epersonMapping =  epersonMappingService.findByOfficeAndDepartmentAndDesignation(context,office,department,designation);
            if(epersonMapping!=null){
                epersonMappingRest =converter.toRest(epersonMapping,utils.obtainProjection());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return epersonMappingRest;
    }


//    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
//    @SearchRestMethod(name = "getDocumentByItemID")
//    public Page<EpersonMapping> getDocumentByItemID(@Parameter(value = "itemid", required = true) UUID itemid, Pageable pageable) {
//        try {
//            Context context = obtainContext();
//            context.turnOffAuthorisationSystem();
//            UUID statusid= WorkFlowStatus.CLOSE.getUserTypeFromMasterValue(context).get().getID();
//            // System.out.println("status id:"+statusid);
//            long total = epersonMappingService.countDocumentByItemid(context, itemid,statusid);
//            List<EpersonMapping> witems = EpersonMappingService.getDocumentByItemid(context, itemid,statusid, Math.toIntExact(pageable.getOffset()),
//                    Math.toIntExact(pageable.getPageSize()));
//            return converter.toRestPage(witems, pageable, total, utils.obtainProjection());
//        } catch (SQLException e) {
//            throw new RuntimeException(e.getMessage(), e);
//        }
//    }

}
