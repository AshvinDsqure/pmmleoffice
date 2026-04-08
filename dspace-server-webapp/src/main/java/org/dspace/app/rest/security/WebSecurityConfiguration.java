package org.dspace.app.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dspace.app.rest.authn.OTPService;
import org.dspace.app.rest.exception.DSpaceAccessDeniedHandler;
import org.dspace.authenticate.service.AuthenticationService;
import org.dspace.eperson.service.EPersonService;
import org.dspace.services.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String ADMIN_GRANT = "ADMIN";
    public static final String AUTHENTICATED_GRANT = "AUTHENTICATED";
    public static final String ANONYMOUS_GRANT = "ANONYMOUS";

    @Autowired
    private EPersonRestAuthenticationProvider ePersonRestAuthenticationProvider;

    @Autowired
    private RestAuthenticationService restAuthenticationService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private CustomLogoutHandler customLogoutHandler;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DSpaceAccessDeniedHandler accessDeniedHandler;

//    @Autowired
//    private UltraMsgSender ultraMsgSender;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EPersonService epersonService; // ✅ ADD THIS

    @Autowired
    private ObjectMapper objectMapper; // ✅ ADD THIS

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

//    @Override
//    public void configure(WebSecurity webSecurity) throws Exception {
//        webSecurity
//                .ignoring()
//                .antMatchers(HttpMethod.GET, "/api/authn/login")
//                .antMatchers(HttpMethod.PUT, "/api/authn/login")
//                .antMatchers(HttpMethod.PATCH, "/api/authn/login")
//                .antMatchers(HttpMethod.DELETE, "/api/authn/login")
//                // ✅ ADD THIS: Ignore OTP verification methods
//                .antMatchers(HttpMethod.GET, "/api/authn/verify-otp")
//                .antMatchers(HttpMethod.PUT, "/api/authn/verify-otp")
//                .antMatchers(HttpMethod.PATCH, "/api/authn/verify-otp")
//                .antMatchers(HttpMethod.DELETE, "/api/authn/verify-otp");
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // ✅ CREATE FILTER WITH DEPENDENCIES
        StatelessLoginFilter loginFilter = createStatelessLoginFilter();

        http.requestMatchers()
                .antMatchers("/api/**", "/iiif/**", actuatorBasePath + "/**")
                .and()
                .authorizeRequests()
                // ✅ EXISTING
                .antMatchers(HttpMethod.POST, "/api/authn/login").permitAll()
                // ✅ ADD THIS: OTP verification endpoint permission
                .antMatchers(HttpMethod.POST, "/api/authn/verify-otp").permitAll()
                .antMatchers(HttpMethod.GET, "/api/authn/validateOTP").permitAll()
                .antMatchers(HttpMethod.POST, "/api/authn/resend-otp").permitAll()
                .antMatchers(HttpMethod.GET, "/api/authn/status").permitAll()
                .antMatchers(HttpMethod.GET, actuatorBasePath + "/info").hasAnyAuthority(ADMIN_GRANT)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .anonymous().authorities(ANONYMOUS_GRANT).and()
                .servletApi().and()
                .cors().and()
                .csrf()
                .ignoringAntMatchers("/api/authn/login")
                .ignoringAntMatchers("/api/authn/validateOTP")
                // ✅ ADD THIS: Ignore CSRF for OTP verification
                .ignoringAntMatchers("/api/authn/verify-otp")
                .ignoringAntMatchers("/api/authn/resend-otp")
                .csrfTokenRepository(this.csrfTokenRepository())
                .sessionAuthenticationStrategy(this.sessionAuthenticationStrategy())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new DSpace401AuthenticationEntryPoint(restAuthenticationService))
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .logout()
                .addLogoutHandler(customLogoutHandler)
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/authn/logout", HttpMethod.POST.name()))
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                .permitAll()
                .and()
                .addFilterBefore(new AnonymousAdditionalAuthorizationFilter(authenticationManager(), authenticationService),
                        StatelessAuthenticationFilter.class)
                // ✅ USE THE CREATED FILTER
                .addFilterBefore(loginFilter, LogoutFilter.class)
                .addFilterBefore(new ShibbolethLoginFilter("/api/authn/shibboleth", authenticationManager(),
                                restAuthenticationService),
                        LogoutFilter.class)
                .addFilterBefore(new OrcidLoginFilter("/api/authn/orcid", authenticationManager(),
                                restAuthenticationService),
                        LogoutFilter.class)
                .addFilterBefore(new OidcLoginFilter("/api/authn/oidc", authenticationManager(),
                                restAuthenticationService),
                        LogoutFilter.class)
                .addFilterBefore(new StatelessAuthenticationFilter(authenticationManager(), restAuthenticationService,
                                ePersonRestAuthenticationProvider, requestService),
                        StatelessLoginFilter.class);
    }

    // ✅ HELPER METHOD TO CREATE FILTER WITH DEPENDENCIES
    private StatelessLoginFilter createStatelessLoginFilter() throws Exception {
        StatelessLoginFilter filter = new StatelessLoginFilter(
                "/api/authn/login",
                authenticationManager(),
                restAuthenticationService
        );

        // ✅ SET DEPENDENCIES MANUALLY
        filter.setOtpService(otpService);
        filter.setEpersonService(epersonService);
        filter.setObjectMapper(objectMapper);

        return filter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(ePersonRestAuthenticationProvider);
    }

    @Lazy
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new DSpaceCsrfTokenRepository();
    }

    private SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new DSpaceCsrfAuthenticationStrategy(csrfTokenRepository());
    }
}