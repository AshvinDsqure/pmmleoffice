/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.EPersonConverter;
import org.dspace.app.rest.converter.EpersonToEpersonMappingConverter;
import org.dspace.app.rest.converter.WorkFlowProcessConverter;
import org.dspace.app.rest.converter.WorkFlowProcessMasterValueConverter;
import org.dspace.app.rest.enums.WorkFlowStatus;
import org.dspace.app.rest.enums.WorkFlowType;
import org.dspace.app.rest.model.*;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.ExcelHelper;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.emas.Emas;
import org.dspace.emas.service.EmasService;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Controller to upload bitstreams to a certain bundle, indicated by a uuid in the request
 * Usage: POST /api/core/bundles/{uuid}/bitstreams (with file and properties of file in request)
 * Example:
 * <pre>
 * {@code
 * curl https://<dspace.server.url>/api/core/bundles/d3599177-0408-403b-9f8d-d300edd79edb/bitstreams
 *  -XPOST -H 'Content-Type: multipart/form-data' \
 *  -H 'Authorization: Bearer eyJhbGciOiJI...' \
 *  -F "file=@Downloads/test.html" \
 *  -F 'properties={ "name": "test.html", "metadata": { "dc.description": [ { "value": "example file", "language": null,
 *          "authority": null, "confidence": -1, "place": 0 } ]}, "bundleName": "ORIGINAL" };type=application/json'
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/" + WorkFlowProcessRest.CATEGORY + "/" + WorkFlowProcessRest.PLURAL_NAME + "/emas")
public class EmasController{

    @Autowired
    WorkflowProcessService workflowProcessService;
    @Autowired
    private DiscoverableEndpointsService discoverableEndpointsService;

    private static final Logger log = LogManager.getLogger();

    @Autowired
    protected Utils utils;
    @Autowired
    protected EmasService emasService;


    public void afterPropertiesSet() throws Exception {
        this.discoverableEndpointsService.register(this,
                Arrays.asList(new Link[]{Link.of("/api/workflowprocesse", "workflowprocesses")}));
    }

    /**
     * Method to upload a Bitstream to a Bundle with the given UUID in the URL. This will create a Bitstream with the
     * file provided in the request and attach this to the Item that matches the UUID in the URL.
     * This will only work for uploading one file, any extra files will silently be ignored
     *
     * @return The created BitstreamResource
     */

    @RequestMapping(method = RequestMethod.POST, value = "/addEmasEperson")
    public ResponseEntity<?> addEmasEperson(HttpServletRequest request, @RequestBody EmasDTO rest) throws SQLException {
        Context context = null;
        try {
            context = ContextUtil.obtainContext(request);
            if (rest.getKey() != null && !rest.getKey().isEmpty()&&rest.getEpersonid() != null && !rest.getEpersonid().isEmpty()) {
                if(emasService.getEmasByEpersonANDKey(context,UUID.fromString(rest.getEpersonid()), rest.getKey())){
                    return EmasResponse.buildBadRequestResponse( "This Key & User already registered.");
                } else  {
                    if (emasService.getEmasByKey(context, rest.getKey())) {
                        return EmasResponse.buildBadRequestResponse("This Key already registered.");
                    }
                    if (emasService.getEmasByEperson(context, UUID.fromString(rest.getEpersonid()))) {
                        return EmasResponse.buildBadRequestResponse("This Eperson already registered.");
                    }
                }
                Emas entity = new Emas();
                entity.setKey(rest.getKey());
                entity.setEperson(context.getCurrentUser());
                Emas savedEmas = emasService.create(context, entity);
                EmasDTO responseDto = new EmasDTO();
                responseDto.setUuid(savedEmas.getID().toString());
                responseDto.setKey(savedEmas.getKey());
                responseDto.setEpersonid(savedEmas.getEperson().getID().toString());
                context.commit();
                return ResponseEntity.ok(responseDto);
            } else {
                return EmasResponse.buildBadRequestResponse("Key must not be null or empty.");
            }
        } catch (SQLException | AuthorizeException e) {
            log.error("Error occurred while adding Emas: ", e);
            return EmasResponse.buildErrorResponse("Error occurred while adding addEmasEperson: " + e.getMessage());

        } catch (Exception e) {
            return EmasResponse.buildErrorResponse("Unexpected error occurred: " + e.getMessage());
        }
    }
    @RequestMapping(method = RequestMethod.POST, value = "/checkExistences")
    public ResponseEntity<?> checkEmasExistence(HttpServletRequest request, @RequestBody EmasDTO checkDto) {
        Context context = null;
        try {
            context = ContextUtil.obtainContext(request);
            Map<String, Object> response = new HashMap<>();
            if (checkDto.getKey() == null || checkDto.getKey().isEmpty()
                    || checkDto.getEpersonid() == null || checkDto.getEpersonid().isEmpty()) {
                return EmasResponse.buildBadRequestResponse("Key and EpersonId must not be null or empty.");
            }
            Boolean isexistEmasByEpersonANDKey = emasService.getEmasByEpersonANDKey(context,  UUID.fromString(checkDto.getEpersonid()), checkDto.getKey());
            if(isexistEmasByEpersonANDKey){
                response.put("exists", isexistEmasByEpersonANDKey);
                return ResponseEntity.ok(response);
            } else {
                response.put("exists", isexistEmasByEpersonANDKey);
                return ResponseEntity.ok(response);
            }
        } catch (SQLException e) {
            return EmasResponse.buildErrorResponse("Error occurred while checkExistences: " + e.getMessage());

        } catch (NumberFormatException e) {
            return EmasResponse.buildErrorResponse("Error checkExistences Emas: " + e.getMessage());
        } catch (Exception e) {
             return EmasResponse.buildErrorResponse("Error Exception Emas: " + e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/isKeyRegistered")
    public ResponseEntity<?> isKeyRegistered(HttpServletRequest request, @RequestBody EmasDTO checkDto) {
        Context context = null;
        try {
            context = ContextUtil.obtainContext(request);
            Map<String, Object> response = new HashMap<>();
            if (checkDto.getKey() == null || checkDto.getKey().isEmpty()) {
                return EmasResponse.buildBadRequestResponse("Key and EpersonId must not be null or empty.");
            }
            Boolean isexistEmasByEpersonANDKey = emasService.getEmasByKey(context,  checkDto.getKey());
            if(isexistEmasByEpersonANDKey){
                response.put("exists", isexistEmasByEpersonANDKey);
                return ResponseEntity.ok(response);
            } else {
                response.put("exists", isexistEmasByEpersonANDKey);
                return ResponseEntity.ok(response);
            }
        } catch (SQLException e) {
            return EmasResponse.buildErrorResponse("Error occurred while isKeyRegistered: " + e.getMessage());

        } catch (NumberFormatException e) {
            return EmasResponse.buildErrorResponse("Error isKeyRegistered Emas: " + e.getMessage());
        } catch (Exception e) {
            return EmasResponse.buildErrorResponse("Error Exception isKeyRegistered: " + e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/isEpersonRegistered")
    public ResponseEntity<?> isEpersonRegistered(HttpServletRequest request, @RequestBody EmasDTO checkDto) {
        Context context = null;
        try {
            context = ContextUtil.obtainContext(request);
            Map<String, Object> response = new HashMap<>();
            if (checkDto.getEpersonid() == null || checkDto.getEpersonid().isEmpty()) {
                return EmasResponse.buildBadRequestResponse("Key and EpersonId must not be null or empty.");
            }
            Boolean exist = emasService.getEmasByEperson(context,  UUID.fromString(checkDto.getEpersonid()));
            if(exist){
                response.put("exists", exist);
                return ResponseEntity.ok(response);
            } else {
                response.put("exists", exist);
                return ResponseEntity.ok(response);
            }
        } catch (SQLException e) {
            return EmasResponse.buildErrorResponse("Error occurred while isEpersonRegistered: " + e.getMessage());

        } catch (NumberFormatException e) {
            return EmasResponse.buildErrorResponse("Error NumberFormatException isEpersonRegistered: " + e.getMessage());
        } catch (Exception e) {
            return EmasResponse.buildErrorResponse("Error Exception isEpersonRegistered: " + e.getMessage());
        }
    }
}
