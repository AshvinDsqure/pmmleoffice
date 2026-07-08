package org.dspace.app.rest.authn;

import org.dspace.app.rest.notification.NotificationService;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.dspace.services.ConfigurationService;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class OTPService {

    private static final Logger log = LoggerFactory.getLogger(OTPService.class);


    private int getOtpTtlSeconds() {
        return configurationService.getIntProperty(
                "dspace.otp.ttl.seconds",
                300 // fallback default (safety)
        );
    }


    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private EPersonService epersonService;
    @Autowired
    @Qualifier("whatsAppNotificationServiceImpl")
    private NotificationService whatsAppNotificationServiceImpl;

    @Autowired
    @Qualifier("sMSNotificationServiceImpl")
    private NotificationService sMSNotificationServiceImpl;

    @Autowired
    @Qualifier("emailNotificationServiceImpl")
    private NotificationService emailNotificationServiceImpl;


    private final RedisCache<OtpSessionData> otpCache =
            RedisCache.getInstance();


    private String getOtpSecret() {
        return configurationService.getProperty("dspace.otp.secret");
    }

    // ✅ Generate OTP
    public String generateOtp() {
        String otp =
                String.valueOf(100000 + new SecureRandom().nextInt(900000));
        log.debug("OTP GENERATED (RAW) => {}", otp);
        return otp;
    }

    // 🔐 HMAC(email + sessionId + otp)
    private String hashOtp(String otp, String email, String sessionId) {
        try {
            String dataToHash = email + "|" + sessionId + "|" + otp;


            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    getOtpSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));


            String encodedHash = Base64.getEncoder()
                    .encodeToString(mac.doFinal(
                            dataToHash.getBytes(StandardCharsets.UTF_8)));

            log.debug("OTP HASH OUTPUT => {}", encodedHash);
            return encodedHash;

        } catch (Exception e) {
            log.error("OTP hashing failed", e);
            throw new RuntimeException("OTP hashing failed", e);
        }
    }

    // ✅ CREATE OTP SESSION + SEND OTP
    public String createOtpSession(EPerson eperson, Context context)
            throws MessagingException, IOException {

        if (eperson == null) {
            throw new IllegalArgumentException("EPerson cannot be null");
        }

        String email = eperson.getEmail();
        String mobile = eperson.getMobile();

        String otp = generateOtp();

        System.out.println("otp:::" + otp);

        String sessionId = UUID.randomUUID().toString();

        log.info("CREATING OTP SESSION");
        log.info("Email      => {}", email);
        log.info("Mobile     => {}", mobile);
        log.info("Session ID => {}", sessionId);

        String otpHash = hashOtp(otp, email, sessionId);

        log.info("STORING OTP HASH IN REDIS => {}", otpHash);

        OtpSessionData data = new OtpSessionData(email, otpHash);

        int ttl = getOtpTtlSeconds();

        otpCache.put(sessionId, data, ttl);

        String emailonoff =
                configurationService.getProperty(
                        "dspace.notification.channel.email"
                );

        String whatsapponoff =
                configurationService.getProperty(
                        "dspace.notification.channel.whatsapp"
                );



        /* EMAIL */

        if (email != null
                && !email.isEmpty()
                && emailonoff != null
                && emailonoff.equalsIgnoreCase("on")) {

            try {

                sendOtpByChannel(
                        otp,
                        email,
                        null,
                        context,
                        "email"
                );

                log.info("EMAIL OTP SENT SUCCESSFULLY");

            } catch (Exception e) {

                log.error("EMAIL OTP FAILED", e);
            }
        }



        /* WHATSAPP */

        if (mobile != null
                && !mobile.isEmpty()
                && whatsapponoff != null
                && whatsapponoff.equalsIgnoreCase("on")) {

            try {

                data.setMobile(mobile);

                sendOtpByChannel(
                        otp,
                        email,
                        mobile,
                        context,
                        "whatsapp"
                );

                log.info("WHATSAPP OTP SENT SUCCESSFULLY");

            } catch (Exception e) {

                log.error("WHATSAPP OTP FAILED", e);
            }
        }

        return sessionId;
    }


    // otp channel
    private void sendOtpByChannel(
            String otp,
            String email,
            String phoneNumber, Context context, String channel) throws MessagingException, IOException {

        String message =
                "Dear " + email + ",\n\n" +
                        "Your OTP is: " + otp + " for multi-factor authentication login to PMML eOffice.\n" +
                        "Please do not share this code with anyone. If you did not request this, please ignore this message.\n\n" +
                        "Regards,\n" +
                        "Prime Minister’s Museum & Library (PMML)";

        switch (channel.toLowerCase()) {
            case "email":
                Email emailobject = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "email_otp_notification"));
                String from_email = configurationService.getProperty("mail.from.address", "support@pmml.in");
                emailNotificationServiceImpl.send(
                        from_email,
                        email,
                        otp,
                        emailobject
                );
                log.info("OTP sent via Email");
                System.out.println("OTP sent via Email");
                break;
            case "whatsapp":
                whatsAppNotificationServiceImpl.send(
                        null,
                        phoneNumber,
                        message,
                        null
                );
                log.info("OTP sent via WhatsApp");
                System.out.println("OTP sent via WhatsApp");
                break;

            case "sms":
            default:
                sMSNotificationServiceImpl.send(
                        null,
                        null,
                        message,
                        null
                );
                log.info("OTP sent via SMS");
                break;

        }
    }


    // ✅ VERIFY OTP
    public OtpVerificationStatus verifyOtp(String sessionId, String otp) {

        log.info("VERIFYING OTP");
        log.info("Session ID => {}", sessionId);
        log.info("Provided OTP => {}", otp);


        try {
            String key = "STORE1970qswlaqw";
            String iv = "STORE1970qswlaqw";
            System.out.println("BEFORE::::::::OTP"+otp);
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] encryptedusername = decoder.decode(otp);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] originaluser = cipher.doFinal(encryptedusername);
            String originalusers = new String(originaluser);
            otp=originalusers.trim();
            System.out.println("AFTER::::::::OTP\t"+otp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OtpSessionData data =
                otpCache.get(sessionId, OtpSessionData.class);

        //  OTP expired / session not found
        if (data == null || data.isExpired()) {
            otpCache.remove(sessionId);
            return OtpVerificationStatus.OTP_EXPIRED;
        }

        String expectedHash =
                hashOtp(otp, data.getEmail(), sessionId);

        boolean match = MessageDigest.isEqual(
                expectedHash.getBytes(StandardCharsets.UTF_8),
                data.getOtpHash().getBytes(StandardCharsets.UTF_8)
        );

        // ❌ Wrong OTP
        if (!match) {
            return OtpVerificationStatus.WRONG_OTP;
        }

        log.info("OTP HASH MATCH => {}", match);

        //  Success
        data.setVerified(true);
        otpCache.put(sessionId, data, getOtpTtlSeconds());
        return OtpVerificationStatus.SUCCESS;
    }


    //  GET USER AFTER VERIFIED OTP
    public EPerson getEpersonFromSession(String sessionId) {

        OtpSessionData data =
                otpCache.get(sessionId, OtpSessionData.class);

        if (data == null || !data.isVerified()) {
            return null;
        }

        Context context = null;
        try {
            context = new Context();
            return epersonService.findByEmail(context, data.getEmail());
        } catch (Exception e) {
            log.error("FAILED TO LOAD EPERSON", e);
            return null;
        } finally {
            if (context != null && context.isValid()) {
                context.abort();
            }
        }
    }


    // ✅ RESEND OTP + SEND AGAIN
    public boolean resendOtp(String sessionId, Context context) throws MessagingException, IOException {

        log.info("RESENDING OTP | sessionId={}", sessionId);

        OtpSessionData data =
                otpCache.get(sessionId, OtpSessionData.class);

        if (data == null || data.isExpired()) {
            log.warn("SESSION NOT FOUND OR EXPIRED");
            otpCache.remove(sessionId);
            return false;
        }

        long now = System.currentTimeMillis();

        // ✅ 30 seconds cooldown
        if (data.getLastResendTime() != 0 &&
                now - data.getLastResendTime() < 30_000) {

            log.warn("RESEND COOLDOWN ACTIVE");
            return false;
        }

        // ✅ Optional max resend limit (recommended)
        if (data.getResendCount() >= 5) {
            log.warn("MAX RESEND ATTEMPTS REACHED");
            return false;
        }
        // Generate new OTP
        String newOtp = generateOtp();
        String newHash =
                hashOtp(newOtp, data.getEmail(), sessionId);

        data.setOtpHash(newHash);
        data.setCreatedTime(new Date());
        data.setLastResendTime(now);
        data.setResendCount(data.getResendCount() + 1);

        int ttl = getOtpTtlSeconds();
        data.setExpiryTime(
                new Date(System.currentTimeMillis() + ttl * 1000L)
        );
        otpCache.put(sessionId, data, ttl);
        //Email emailobject = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "registration_approve"));
        String emailonoff=configurationService.getProperty("dspace.notification.channel.email");
        String whatsapponoff=configurationService.getProperty("dspace.notification.channel.whatsapp");

        if(data.getEmail()!=null&&!data.getEmail().isEmpty()&&emailonoff!=null&&emailonoff.equalsIgnoreCase("on")) {
           sendOtpByChannel(
                   newOtp,
                   data.getEmail(),
                   null,
                   context,
                   "email"
           );
           log.info("OTP resent successfully Email.");
       }
       if(data.getMobile()!=null&&!data.getMobile().isEmpty()&&whatsapponoff!=null&&whatsapponoff.equalsIgnoreCase("on")) {
           sendOtpByChannel(
                   newOtp,
                   data.getEmail(),
                   data.getMobile(),
                   context,
                   "whatsapp"
           );
           log.info("OTP resent successfully Mobile.");
       }
        return true;
    }
}
