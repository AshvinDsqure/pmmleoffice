package org.dspace.app.rest.authn;

import org.dspace.app.rest.security.DSpaceAuthentication;
import org.dspace.app.rest.security.RestAuthenticationService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.app.rest.utils.ContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/authn")
public class VerifyOtpController {

    @Autowired
    private OTPService otpService;

    @Autowired
    private RestAuthenticationService restAuthenticationService;

    @Autowired
    private OtpRateLimiter otpRateLimiter;

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest otpRequest,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {

        String clientIp = request.getRemoteAddr(); // or getClientIp() if behind Apache proxy

        if (!otpRateLimiter.isAllowed(clientIp)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many OTP requests. Try again after 5 minutes.");
        }

        OtpVerificationStatus status =
                otpService.verifyOtp(otpRequest.getOtpSessionId(), otpRequest.getOtp());

        switch (status) {

            case OTP_EXPIRED:
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("OTP expired. Please request a new OTP.");

            case WRONG_OTP:
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid OTP. Please try again.");

            case SUCCESS:
                break;
        }

        EPerson eperson = otpService.getEpersonFromSession(otpRequest.getOtpSessionId());
        if (eperson == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 🔑 THIS is the correct way
        DSpaceAuthentication auth =
                new DSpaceAuthentication(eperson.getEmail(), null);

        restAuthenticationService.addAuthenticationDataForUser(
                request,
                response,
                auth,
                false
        );

        return ResponseEntity.ok().build();
    }

    // ✅ RESEND OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String otpSessionId,HttpServletRequest request,HttpServletResponse response) throws MessagingException, IOException {


        Context context = ContextUtil.obtainContext(request);
        boolean resent = otpService.resendOtp(otpSessionId,context);

        if (!resent) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("OTP session expired. Please login again.");
        }

        return ResponseEntity.ok(
                Map.of("status", "OTP_RESENT")
        );
    }

}
