package org.dspace.app.rest.notification;

import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;

@Service("emailNotificationServiceImpl")
public class EmailNotificationServiceImpl implements NotificationService{
    @Override
    public void send(String from, String to, String message, Email email) throws IOException, MessagingException {
        //Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "registration_approve"));
        email.setSubject("User Account Registration Confirmation");
        email.addRecipient(to);
        email.setSubject("OTP for PMML eOffice");
        email.addArgument(to);
        email.addArgument(message);
        email.send();
    }
}
