/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dspace.app.rest.authn.OTPService;
import org.dspace.app.rest.authn.OtpRateLimiter;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.dspace.services.RequestService;
import org.dspace.utils.DSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * This class will filter /api/authn/login requests to try and authenticate them. Keep in mind, this filter runs *after*
 * StatelessAuthenticationFilter (which looks for authentication data in the request itself). So, in some scenarios
 * (e.g. after a Shibboleth login) the StatelessAuthenticationFilter does the actual authentication, and this Filter
 * just ensures the auth token (JWT) is sent back in an Authorization header.
 *
 * @author Frederic Van Reet (frederic dot vanreet at atmire dot com)
 * @author Tom Desair (tom dot desair at atmire dot com)
 */
public class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger log = LoggerFactory.getLogger(StatelessLoginFilter.class);
    protected RequestService requestService = new DSpace().getRequestService();

   // protected  AnalyticsServerImpl analyticsServerImp;
    protected AuthenticationManager authenticationManager;

    protected RestAuthenticationService restAuthenticationService;
    private EPersonService epersonService;
    private OTPService otpService;

    private OtpRateLimiter otpRateLimiter;

    private ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() {
    }

    public StatelessLoginFilter(String url, AuthenticationManager authenticationManager,
                                RestAuthenticationService restAuthenticationService) {
        super(new AntPathRequestMatcher(url));
        this.authenticationManager = authenticationManager;
        this.restAuthenticationService = restAuthenticationService;
    }

    // ✅ ADD SETTERS
    public void setOtpService(OTPService otpService) {
        this.otpService = otpService;
    }

    public void setOtpRateLimiter(OtpRateLimiter otpRateLimiter) {
        this.otpRateLimiter = otpRateLimiter;
    }

    public void setEpersonService(EPersonService epersonService) {
        this.epersonService = epersonService;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Attempt to authenticate the user by using Spring Security's AuthenticationManager.
     * The AuthenticationManager will delegate this task to one or more AuthenticationProvider classes.
     * <P>
     * For DSpace, our custom AuthenticationProvider is {@link EPersonRestAuthenticationProvider}, so that
     * is the authenticate() method which is called below.
     *
     * @param req current request
     * @param res current response
     * @return a valid Spring Security Authentication object if authentication succeeds
     * @throws AuthenticationException if authentication fails
     * @see EPersonRestAuthenticationProvider
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {

        String user = req.getParameter("user");
        String password = req.getParameter("password");



        // Attempt to authenticate by passing user & password (if provided) to AuthenticationProvider class(es)
        // NOTE: This method will check if the user was already authenticated by StatelessAuthenticationFilter,
        // and, if so, just refresh their token.
        return authenticationManager.authenticate(new DSpaceAuthentication(user, password));
    }

    /**
     * If the above attemptAuthentication() call was successful (no authentication error was thrown),
     * then this method will take the returned {@link DSpaceAuthentication} class (which includes all
     * the data from the authenticated user) and add the authentication data to the response.
     * <P>
     * For DSpace, this is calling our {@link org.dspace.app.rest.security.jwt.JWTTokenRestAuthenticationServiceImpl}
     * in order to create a JWT based on the authentication data & send that JWT back in the response.
     *
     * @param req current request
     * @param res response
     * @param chain FilterChain
     * @param auth Authentication object containing info about user who had a successful authentication
     * @throws IOException
     * @throws ServletException
     * @see org.dspace.app.rest.security.jwt.JWTTokenRestAuthenticationServiceImpl
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {


        DSpaceAuthentication dSpaceAuthentication = (DSpaceAuthentication) auth;
        Context context=ContextUtil.obtainContext(req);
        EPerson eperson;
        String username = auth.getName();

        try {
            // 2️⃣ Create DSpace context to fetch EPerson
            context = new Context();
            eperson = epersonService.findByEmail(context, username);

            if (eperson == null) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "User not found");
                return;
            }

        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "User lookup failed");
            return;

        } finally {
            // 3️⃣ ALWAYS close context to avoid DB leaks
            if (context != null && context.isValid()) {
                context.abort();
            }
        }
        // 4️⃣ Generate and send OTP (DO NOT log user in)
        String otpSessionId = null;
        try {
            otpSessionId = otpService.createOtpSession(eperson,context);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        // 5️⃣ Send OTP-required response
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("application/json;charset=UTF-8");

        res.getWriter().write(
                "{\"status\":\"OTP_REQUIRED\",\"otpSessionId\":\"" + otpSessionId + "\"}"
        );
        res.getWriter().flush();
        // 6️⃣ CRITICAL: stop filter chain (no JWT, no session)
//        try{
//            trackDspaceEvent(context, Event.LOGIN,"Login",req);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        return;
    }
//    public void trackDspaceEvent(Context context, int action, String title,HttpServletRequest req) {
//        try {
//            DspaceEventInfo dspaceEventInfo = analyticsServerImp.getDspaceEventInfo(action, null, Constants.LOGIN);
//            if (context.getCurrentUser() != null) {
//                dspaceEventInfo.setUserid(context.getCurrentUser().getID());
//            }
//            dspaceEventInfo.setTitle(title);
//            dspaceEventInfo.setIp(req.getRemoteAddr());
//            analyticsServerImp.storeEvent(dspaceEventInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new ResourceNotFoundException("server unavailable");
//        }
//    }

    /**
     * If the above attemptAuthentication() call was unsuccessful, then ensure that the response is a 401 Unauthorized
     * AND it includes a WWW-Authentication header. We use this header in DSpace to return all the enabled
     * authentication options available to the UI (along with the path to the login URL for each option)
     * @param request current request
     * @param response current response
     * @param failed exception that was thrown by attemptAuthentication()
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {

        String authenticateHeaderValue = restAuthenticationService.getWwwAuthenticateHeaderValue(request, response);
        response.setHeader("WWW-Authenticate", authenticateHeaderValue);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed!");
        log.error("Authentication failed (status:{})",
                HttpServletResponse.SC_UNAUTHORIZED, failed);
    }

//    public AnalyticsServerImpl getAnalyticsServerImp() {
//        return analyticsServerImp;
//    }
//
//    public void setAnalyticsServerImp(AnalyticsServerImpl analyticsServerImp) {
//        this.analyticsServerImp = analyticsServerImp;
//    }
}
