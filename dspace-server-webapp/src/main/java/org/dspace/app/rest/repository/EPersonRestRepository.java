/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.DiscoverableEndpointsService;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.EPersonConverter;
import org.dspace.app.rest.converter.EpersonMappingConverter;
import org.dspace.app.rest.converter.WorkFlowProcessMasterValueConverter;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowUserType;
import org.dspace.app.rest.exception.*;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.app.util.AuthorizeUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ValidatePasswordService;
import org.dspace.content.*;
import org.dspace.content.service.EpersonMappingService;
import org.dspace.content.service.EpersonToEpersonMappingService;
import org.dspace.content.service.WorkFlowProcessMasterValueService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.EmptyWorkflowGroupException;
import org.dspace.eperson.Group;
import org.dspace.eperson.RegistrationData;
import org.dspace.eperson.service.AccountService;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.eperson.service.RegistrationDataService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;


/**
 * This is the repository responsible to manage EPerson Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(EPersonRest.CATEGORY + "." + EPersonRest.NAME)
public class EPersonRestRepository extends DSpaceObjectRestRepository<EPerson, EPersonRest>
        implements InitializingBean {

    private static final Logger log = LogManager.getLogger();

    @Autowired
    AuthorizeService authorizeService;

    @Autowired
    WorkFlowProcessMasterValueConverter workFlowProcessMasterValueConverter;


    @Autowired
    WorkFlowProcessMasterValueService workFlowProcessMasterValueService;


    @Autowired
    EpersonMappingService epersonMappingService;


    @Autowired
    EpersonToEpersonMappingService epersonToEpersonMappingService;



    @Autowired
    DiscoverableEndpointsService discoverableEndpointsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ValidatePasswordService validatePasswordService;

    @Autowired
    private RegistrationDataService registrationDataService;

    private final EPersonService es;

    @Autowired
    EPersonConverter ePersonConverter;

    @Autowired
    GroupService groupService;


    public EPersonRestRepository(EPersonService dsoService) {
        super(dsoService);
        this.es = dsoService;
    }

    @Override
    protected EPersonRest createAndReturn(Context context)
            throws AuthorizeException {
        System.out.println("createAndReturn:::::::::::::::::::::1");
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        EPersonRest epersonRest = null;
        try {
            epersonRest = mapper.readValue(req.getInputStream(), EPersonRest.class);
        } catch (IOException e1) {
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        String token = req.getParameter("token");
        // If a token is available, we'll swap to the execution that is token based
        if (StringUtils.isNotBlank(token)) {
            try {
                return createAndReturn(context, epersonRest, token);
            } catch (SQLException e) {
                log.error("Something went wrong in the creation of an EPerson with token: " + token, e);
                throw new RuntimeException("Something went wrong in the creation of an EPerson with token: " + token);
            }
        }
        // If no token is present, we simply do the admin execution
        EPerson eperson = createEPersonFromRestObject(context, epersonRest);

        try {
            Group note = groupService.findByName(context, "NOTE");
           if(note!=null){
               System.out.println("::::: add in note Group ::::");
               groupService.addMember(context, note, eperson);
               groupService.update(context, note);
               System.out.println("::::: add in note Group :done:::");
           }
        }catch (Exception e){
            System.out.println("error in NOTE group "+e.getMessage());
        }
        return converter.toRest(eperson, utils.obtainProjection());
    }

    private EPerson createEPersonFromRestObject(Context context, EPersonRest epersonRest) throws AuthorizeException {
        EPerson eperson = null;
        EPerson ePersonfinal1=null;
        try {
            eperson = es.create(context);
            // this should be probably moved to the converter (a merge method?)
            EPerson ePersonfinal=eperson;
            ePersonfinal1=ePersonfinal;
            eperson.setCanLogIn(epersonRest.isCanLogIn());
            eperson.setRequireCertificate(epersonRest.isRequireCertificate());
            eperson.setEmail(epersonRest.getEmail());
            eperson.setNetid(epersonRest.getNetid());
            eperson.setTablenumber(epersonRest.getTablenumber());
            eperson.setEmployeeid(epersonRest.getEmployeeid());
            if (epersonRest.getDepartmentRest() != null) {
                eperson.setDepartment(workFlowProcessMasterValueConverter.convert(context, epersonRest.getDepartmentRest()));
            }
            if (epersonRest.getOfficeRest() != null) {
                eperson.setOffice(workFlowProcessMasterValueConverter.convert(context, epersonRest.getOfficeRest()));
            }
            if (epersonRest.getDesignationRest() != null) {
                eperson.setDesignation(workFlowProcessMasterValueConverter.convert(context, epersonRest.getDesignationRest()));
            }
            if (epersonRest.getPassword() != null) {
                if (!validatePasswordService.isPasswordValid(epersonRest.getPassword())) {
                    throw new PasswordNotValidException();
                }
                es.setPassword(eperson, epersonRest.getPassword());
            }
            if(epersonRest.getEpersonmapping()!=null&&!epersonRest.getEpersonmapping().isEmpty()){
                if(epersonRest.getEpersonmapping().contains(",")) {
                    System.out.println("in Multiple  mapping");
                    List<String> list = Arrays.asList(epersonRest.getEpersonmapping().split(","));
                    List<EpersonToEpersonMapping> epersonToEpersonMappings = list.stream()
                            .filter(d -> d != null).map(we -> {
                                try {
                                    EpersonToEpersonMapping epersonDepartment = new EpersonToEpersonMapping();
                                    epersonDepartment.setEperson(ePersonfinal);
                                    epersonDepartment.setEpersonmapping(epersonMappingService.find(context, UUID.fromString(we)));
                                    epersonDepartment.setIsactive(false);
                                    return epersonDepartment;
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }).collect(Collectors.toList());
                    eperson.setEpersonToEpersonMappings(epersonToEpersonMappings);
                }else{
                    System.out.println("in single mapping");
                    EpersonToEpersonMapping epersonDepartment = new EpersonToEpersonMapping();
                    epersonDepartment.setEperson(ePersonfinal);
                    epersonDepartment.setEpersonmapping(epersonMappingService.find(context, UUID.fromString(epersonRest.getEpersonmapping())));
                    List<EpersonToEpersonMapping> epersonToEpersonMappings=new ArrayList<>();
                    epersonToEpersonMappings.add(epersonDepartment);
                    eperson.setEpersonToEpersonMappings(epersonToEpersonMappings);
                }

            }
            es.update(context, eperson);
            metadataConverter.setMetadata(context, eperson, epersonRest.getMetadata());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return ePersonfinal1;
    }

    /**
     * This method will perform checks on whether or not the given Request was valid for the creation of an EPerson
     * with a token or not.
     * It'll check that the token exists, that the token doesn't yet resolve to an actual eperson already,
     * that the email in the given json is equal to the email for the token and that other properties are set to
     * what we expect in this creation.
     * It'll check if all of those constraints hold true and if we're allowed to register new accounts.
     * If this is the case, we'll create an EPerson without any authorization checks and delete the token
     *
     * @param context     The DSpace context
     * @param epersonRest The EPersonRest given to be created
     * @param token       The token to be used
     * @return The EPersonRest after the creation of the EPerson object
     * @throws AuthorizeException If something goes wrong
     * @throws SQLException       If something goes wrong
     */
    private EPersonRest createAndReturn(Context context, EPersonRest epersonRest, String token)
            throws AuthorizeException, SQLException {


        System.out.println("in createAndReturn:::::");

        if (!AuthorizeUtil.authorizeNewAccountRegistration(context, requestService
                .getCurrentRequest().getHttpServletRequest())) {
            throw new DSpaceBadRequestException(
                    "Registration is disabled, you are not authorized to create a new Authorization");
        }
        RegistrationData registrationData = registrationDataService.findByToken(context, token);
        if (registrationData == null) {
            throw new DSpaceBadRequestException("The token given as parameter: " + token + " does not exist" +
                    " in the database");
        }
        if (es.findByEmail(context, registrationData.getEmail()) != null) {
            throw new DSpaceBadRequestException("The token given already contains an email address that resolves" +
                    " to an eperson");
        }
        String emailFromJson = epersonRest.getEmail();
        if (StringUtils.isNotBlank(emailFromJson)) {
            if (!StringUtils.equalsIgnoreCase(registrationData.getEmail(), emailFromJson)) {
                throw new DSpaceBadRequestException("The email resulting from the token does not match the email given"
                        + " in the json body. Email from token: " +
                        registrationData.getEmail() + " email from the json body: "
                        + emailFromJson);
            }
        }
        if (epersonRest.isSelfRegistered() != null && !epersonRest.isSelfRegistered()) {
            throw new DSpaceBadRequestException("The self registered property cannot be set to false using this method"
                    + " with a token");
        }
        checkRequiredProperties(epersonRest);
        // We'll turn off authorisation system because this call isn't admin based as it's token based
        context.turnOffAuthorisationSystem();
        EPerson ePerson = createEPersonFromRestObject(context, epersonRest);
        context.restoreAuthSystemState();
        // Restoring authorisation state right after the creation call
        accountService.deleteToken(context, token);
        if (context.getCurrentUser() == null) {
            context.setCurrentUser(ePerson);
        }
        return converter.toRest(ePerson, utils.obtainProjection());
    }

    private void checkRequiredProperties(EPersonRest epersonRest) {
        MetadataRest metadataRest = epersonRest.getMetadata();
        if (metadataRest != null) {
            List<MetadataValueRest> epersonFirstName = metadataRest.getMap().get("eperson.firstname");
            List<MetadataValueRest> epersonLastName = metadataRest.getMap().get("eperson.lastname");
            if (epersonFirstName == null || epersonLastName == null ||
                    epersonFirstName.isEmpty() || epersonLastName.isEmpty()) {
                throw new EPersonNameNotProvidedException();
            }
        }
        String password = epersonRest.getPassword();
        if (StringUtils.isBlank(password)) {
            throw new DSpaceBadRequestException("A password is required");
        }
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public EPersonRest findOne(Context context, UUID id) {
        context.turnOffAuthorisationSystem();
        EPerson eperson = null;
        try {
            eperson = es.find(context, id);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (eperson == null) {
            return null;
        }
        return converter.toRest(eperson, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public Page<EPersonRest> findAll(Context context, Pageable pageable) {
        context.turnOffAuthorisationSystem();
        List<EPersonRest> ePersonRests;
        try {
            long total = es.countTotal(context);
            List<EPerson> epersons = es.findAll(context, EPerson.EMAIL, pageable.getPageSize(),
                    Math.toIntExact(pageable.getOffset()));
            ePersonRests = epersons.stream().map(d -> {
                return ePersonConverter.convertBYUSer(d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(ePersonRests, pageable,total);
           // return converter.toRestPage(epersons, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Find the eperson with the provided email address if any. The search is delegated to the
     * {@link EPersonService#findByEmail(Context, String)} method
     *
     * @param email is the *required* email address
     * @return a Page of EPersonRest instances matching the user query
     */
    @SearchRestMethod(name = "byEmail")
    public EPersonRest findByEmail(@Parameter(value = "email", required = true) String email) {
        EPerson eperson = null;
        try {
            Context context = obtainContext();
            eperson = es.findByEmail(context, email);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (eperson == null) {
            return null;
        }
        return converter.toRest(eperson, utils.obtainProjection());
    }

    @SearchRestMethod(name = "wildcardSearchByEmail")
    public Page<EPersonRest> wildcardSearchByEmail(@Parameter(value = "email", required = true) String email, Pageable pageable) {
        List<EPerson> eperson = null;
        try {
            Context context = obtainContext();
            eperson = es.wildcardSearchByEmail(context, email);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (eperson == null) {
            return null;
        }
        return converter.toRestPage(eperson, pageable, 100, utils.obtainProjection());
    }

    /**
     * Find the epersons matching the query parameter. The search is delegated to the
     * {@link EPersonService#search(Context, String, int, int)} method
     *
     * @param query    is the *required* query string
     * @param pageable contains the pagination information
     * @return a Page of EPersonRest instances matching the user query
     */
    @PreAuthorize("hasAuthority('ADMIN') || hasAuthority('MANAGE_ACCESS_GROUP')")
    @SearchRestMethod(name = "byMetadata")
    public Page<EPersonRest> findByMetadata(@Parameter(value = "query", required = true) String query,
                                            Pageable pageable) {

        try {
            Context context = obtainContext();
            long total = es.searchResultCount(context, query);
            List<EPersonRest> epersonsRest=new ArrayList<>();
            List<EPerson> epersons = es.search(context, query, Math.toIntExact(pageable.getOffset()),
                    Math.toIntExact(pageable.getPageSize()));

            epersonsRest = epersons.stream().map(d -> {
                return ePersonConverter.convert(d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(epersonsRest, pageable, total);
            //return converter.toRestPage(epersons, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#uuid, 'EPERSON', #patch)")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                         Patch patch) throws AuthorizeException, SQLException {
        try {
            EPerson ePerson = es.find(context, uuid);
            boolean passwordChangeFound = false;
            for (Operation operation : patch.getOperations()) {
                if (StringUtils.equalsIgnoreCase(operation.getPath(), "/password")) {
                    passwordChangeFound = true;
                }
            }
            if (StringUtils.isNotBlank(request.getParameter("token"))) {
                if (!passwordChangeFound) {
                    throw new AccessDeniedException("Refused to perform the EPerson patch based on a token without " +
                            "changing the password");
                }
            } else {
                if (passwordChangeFound && !StringUtils.equals(context.getAuthenticationMethod(), "password")) {
                    throw new AccessDeniedException("Refused to perform the EPerson patch based to change the password " +
                            "for non \"password\" authentication");
                }
            }
            if(StringUtils.isNotBlank(request.getParameter("token")) && passwordChangeFound){
                System.out.println("in update Password::::::::::::::");
                patchDSpaceObject(apiCategory, model, uuid, patch);
            }else {
                System.out.println(":::::::::::::::::::::::::::::::::IN UPDATE EPERSON::::::::::::::::::::::::::::::;");
                //System.out.println("ep..........." + ePerson.getEmail());
                for (Operation operation : patch.getOperations()) {
                    if (operation.getPath().equalsIgnoreCase("/metadata/dspace.agreements.end-user")) {
                        //System.out.println("::::::::::::::::::::::::::::::in::aggrement::::::::::::::::::::::::::::::;");
                        patchDSpaceObject(apiCategory, model, uuid, patch);
                       // System.out.println(":::::::::::::::::::::::::::::::::out aggrement:done:::::::::::::::::::::::::::;");
                    } else if (operation.getPath().equalsIgnoreCase("/metadata/dspace.agreements.cookies")) {
                       // System.out.println(":::::::::::::::::::::::::::::::cookies::::::::::::::::::::::::::;");
                        patchDSpaceObject(apiCategory, model, uuid, patch);
                        //System.out.println("::::::::::::::::::::::::::::::::cookies::done:::::::::::::::::::::::::::;");
                    } else if (operation.getPath().equalsIgnoreCase("/departmentRest")) {
                        WorkFlowProcessMasterValueRest rest = new WorkFlowProcessMasterValueRest();
                        rest.setUuid(operation.getValue().toString());
                        ePerson.setDepartment(workFlowProcessMasterValueConverter.convert(context, rest));
                    } else if (operation.getPath().equalsIgnoreCase("/officeRest")) {
                        WorkFlowProcessMasterValueRest rest = new WorkFlowProcessMasterValueRest();
                        rest.setUuid(operation.getValue().toString());
                        ePerson.setOffice(workFlowProcessMasterValueConverter.convert(context, rest));
                    } else if (operation.getPath().equalsIgnoreCase("/designationRest")) {
                        WorkFlowProcessMasterValueRest rest = new WorkFlowProcessMasterValueRest();
                        rest.setUuid(operation.getValue().toString());
                        ePerson.setDesignation(workFlowProcessMasterValueConverter.convert(context, rest));
                    } else if (operation.getPath().equalsIgnoreCase("/metadata/eperson.lastname/0/value")) {
                        ePerson.setLastName(context, operation.getValue().toString());
                    } else if (operation.getPath().equalsIgnoreCase("/metadata/eperson.firstname/0/value")) {
                        ePerson.setFirstName(context, operation.getValue().toString());
                    } else if (operation.getPath().equalsIgnoreCase("/employeeid")) {
                        ePerson.setEmployeeid(operation.getValue().toString());
                    }else if(operation.getPath().equalsIgnoreCase("/epersonmapping")){
//                       EPerson ePersonfinal=ePerson;
//                      String mapping=  operation.getValue().toString();
//                        if (mapping != null && !mapping.isEmpty()) {
//                            List<EpersonToEpersonMapping> epersonToEpersonMappings = new ArrayList<>();
//
//                            List<String> mappingList = mapping.contains(",")
//                                    ? Arrays.asList(mapping.split(","))
//                                    : Collections.singletonList(mapping);
//
//                            for (String mapId : mappingList) {
//                                try {
//
//                                    System.out.println("mapp id ::"+mapId);
//                                    System.out.println("epeeee id ::"+ePersonfinal.getID());
//                                    System.out.println("getEmail id ::"+ePersonfinal.getEmail());
//
//
//                                    UUID mappingUUID = UUID.fromString(mapId);
//                                    EpersonToEpersonMapping existingMapping = epersonToEpersonMappingService
//                                            .findByEpersonAndEpersonMapping(context, mappingUUID, ePersonfinal.getID());
//
//                                    if (existingMapping == null) {
//                                        System.out.println("add new mapping ::::::::::::::");
//                                        EpersonToEpersonMapping newMapping = new EpersonToEpersonMapping();
//                                        newMapping.setEperson(ePersonfinal);
//                                        newMapping.setEpersonmapping(epersonMappingService.find(context, mappingUUID));
//                                        newMapping.setIsactive(false);
//                                        epersonToEpersonMappings.add(newMapping);
//                                    }
//                                } catch (SQLException e) {
//                                    throw new RuntimeException("Error processing eperson mapping", e);
//                                }
//                            }
//
//                            if (!epersonToEpersonMappings.isEmpty()) {
//
//                                System.out.println("size ::::"+epersonToEpersonMappings.size());
//
//                                ePerson.setEpersonToEpersonMappings(epersonToEpersonMappings);
//                            }
//                        }

                    } else if (operation.getPath().equalsIgnoreCase("/tablenumber")) {
                        if (!operation.getValue().toString().isEmpty()&&operation.getValue()!=null&&!StringUtils.isNotBlank(operation.getValue().toString())) {
                            ePerson.setTablenumber(Integer.parseInt(operation.getValue().toString()));
                        } else {
                            ePerson.setTablenumber(null);
                        }
                    } else if (operation.getPath().equalsIgnoreCase("/email")) {
                        ePerson.setEmail(operation.getValue().toString());
                    } else if (operation.getPath().equalsIgnoreCase("/password")) {
                        if (!validatePasswordService.isPasswordValid(operation.getValue().toString())) {
                            throw new PasswordNotValidException();
                        }
                        if (operation.getValue() instanceof String) {
                            es.setPassword(ePerson, operation.getValue().toString());
                        }else {
                           // System.out.println("pass:::not string:::::::" + operation.getValue());
                            patchDSpaceObject(apiCategory, model, uuid, patch);
                        }
                    } else {
                        patchDSpaceObject(apiCategory, model, uuid, patch);
                    }
                }
            }
            es.update(context, ePerson);
            context.commit();
            System.out.println(":::::::::::::::::::::::::::::::::   DONE UPDATE EPERSON  ! ::::::::::::::::::::::::::::::;");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("errr" + e.getMessage());
        }
    }

    @Override
    protected void delete(Context context, UUID id) throws AuthorizeException {
        EPerson eperson = null;
        try {
            eperson = es.find(context, id);
            es.delete(context, eperson);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (EmptyWorkflowGroupException e) {
            throw new RESTEmptyWorkflowGroupException(e);
        } catch (IllegalStateException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }
    }

    @SearchRestMethod(name = "findByEmployeeID")
    public EPersonRest findByEmployeeID(HttpServletResponse res, @Parameter(value = "employeeid", required = true) String employeeid) throws IOException {
        try {
            System.out.println("employeeid::"+employeeid);
           Context context = obtainContext();
           EPerson e= es.findByEmployeeID(context,employeeid);
           if(e!=null){
               throw new AlreadyDataExistException("THIS EmployeeID Already EXIST.");
           }
            return new EPersonRest();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        } catch (AlreadyDataExistException e) {
            e.printStackTrace();
            res.sendError(HttpStatus.NOT_ACCEPTABLE.value(), e.getMessage());
            throw new AlreadyDataExistException(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    @SearchRestMethod(name = "searchByDepartment")
    public Page<EPersonRest> searchByDepartment(
            @Parameter(value = "searchdepartmentorofficeid", required = true) UUID searchdepartmentorofficeid,
            Pageable pageable) {
        List<EPersonRest> ePersonRests=null;
        try {
            Context context = obtainContext();
            List<EPerson> witems = es.getByDepartment(context, searchdepartmentorofficeid);
            ePersonRests = witems.stream().filter(d->!d.getEmail().equalsIgnoreCase(context.getCurrentUser().getEmail())).map(d -> {
                    return ePersonConverter.convertBYUSer(d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(ePersonRests, pageable,witems.size());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SearchRestMethod(name = "getAllUsers")
    public Page<EPersonRest> getAllUsers(
            @Parameter(value = "searchdepartmentorofficeid", required = true) UUID searchdepartmentorofficeid,
            Pageable pageable) {
        List<EPersonRest> ePersonRests=null;
        try {
            //System.out.println("search value :" + searchdepartmentorofficeid);
            Context context = obtainContext();
            List<EPerson> witems = es.getByDepartment(context, searchdepartmentorofficeid);
            ePersonRests = witems.stream().filter(d->d!=null).map(d -> {
                return ePersonConverter.convertBYUSer(d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(ePersonRests, pageable,witems.size());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'NOTE', 'READ') || hasPermission(#uuid, 'ITEAM', 'WRITE') || hasPermission(#uuid, 'BITSTREAM','WRITE') || hasPermission(#uuid, 'COLLECTION', 'READ')")
    public Page<EPersonRest> getAll(Context context, Pageable pageable) {
        context.turnOffAuthorisationSystem();
        List<EPersonRest> ePersonRests;
        try {
            long total = es.countTotal(context);
            List<EPerson> epersons = es.findAll(context, EPerson.EMAIL, pageable.getPageSize(),
                    Math.toIntExact(pageable.getOffset()));
            ePersonRests = epersons.stream().map(d -> {
                return ePersonConverter.convertBYUSer(d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(ePersonRests, pageable,total);
            // return converter.toRestPage(epersons, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @Override
    public Class<EPersonRest> getDomainClass() {
        return EPersonRest.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverableEndpointsService.register(this, Arrays.asList(
                Link.of("/api/" + EPersonRest.CATEGORY + "/registrations", EPersonRest.NAME + "-registration")));
    }
}
