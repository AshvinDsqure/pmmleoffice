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
import org.dspace.app.rest.converter.LoginCounterConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.LoginCounterRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.LoginCounter;
import org.dspace.content.service.LoginCounterService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Component(LoginCounterRest.CATEGORY + "." + LoginCounterRest.NAME)

public class LoginCounterRepository extends DSpaceObjectRestRepository<LoginCounter, LoginCounterRest> {


    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(LoginCounterRepository.class);
    @Autowired
    LoginCounterService LoginCounterService;

    @Autowired
    LoginCounterConverter LoginCounterConverter;

    public LoginCounterRepository(LoginCounterService dsoService) {
        super(dsoService);
    }

    @Override
    protected LoginCounterRest createAndReturn(Context context)
            throws AuthorizeException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        LoginCounterRest LoginCounterRest = null;
        LoginCounter LoginCounter = null;
        try {
            LoginCounterRest = mapper.readValue(req.getInputStream(), LoginCounterRest.class);
            LoginCounter = createLoginCounterFromRestObject(context, LoginCounterRest);

        } catch (Exception e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }

        return converter.toRest(LoginCounter, utils.obtainProjection());
    }

    private LoginCounter createLoginCounterFromRestObject(Context context, LoginCounterRest loginCounterRest) throws AuthorizeException {
        LoginCounter loginCounter = new LoginCounter();
        try {
            loginCounter = LoginCounterConverter.convert(loginCounter, loginCounterRest);
            LoginCounterService.create(context, loginCounter);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return loginCounter;
    }

    @Override
    protected LoginCounterRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                             JsonNode jsonNode) throws SQLException, AuthorizeException {
        log.info("::::::start::::put::::::::::");
        LoginCounterRest loginCounterRest = new Gson().fromJson(jsonNode.toString(), LoginCounterRest.class);
        LoginCounter LoginCounter = LoginCounterService.find(context, id);
        if (LoginCounter == null) {
            System.out.println("LoginCounterrest id ::: is Null  LoginCounterrest tye null" + id);
            throw new ResourceNotFoundException("LoginCounterrest  field with id: " + id + " not found");
        }
      //  LoginCounter = LoginCounterConverter.convert(context, loginCounterRest);
        LoginCounterService.update(context, LoginCounter);
        context.commit();
        return converter.toRest(LoginCounter, utils.obtainProjection());
    }
    @Override
    public LoginCounterRest findOne(Context context, UUID uuid) {
        LoginCounterRest LoginCounterRest = null;
        try {
            Optional<LoginCounter> LoginCounter = Optional.ofNullable(LoginCounterService.find(context, uuid));
            if (LoginCounter.isPresent()) {
                LoginCounterRest = converter.toRest(LoginCounter.get(), utils.obtainProjection());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return LoginCounterRest;
    }
    @Override
    public Page<LoginCounterRest> findAll(Context context, Pageable pageable) throws SQLException {

        return converter.toRestPage(null, pageable, 100, utils.obtainProjection());

    }
    protected void delete(Context context, UUID id) throws AuthorizeException {
        LoginCounter LoginCounter = null;
        try {
            LoginCounter = LoginCounterService.find(context, id);
            if (LoginCounter == null) {
                throw new ResourceNotFoundException(LoginCounterRest.CATEGORY + "." + LoginCounterRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            LoginCounterService.delete(context, LoginCounter);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
/*    @SearchRestMethod(name = "getHistory")
    public Page<LoginCounterRest> getHistory(@Parameter(value = "workflowprocessid", required = true) UUID workflowprocessid, Pageable pageable) {
        try {
            Context context = obtainContext();
            long total = LoginCounterService.countHistory(context, workflowprocessid);
            List<LoginCounter> witems = LoginCounterService.getHistory(context, workflowprocessid);
            return converter.toRestPage(witems, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }*/

    @Override
    public Class<LoginCounterRest> getDomainClass() {
        return null;
    }
}
