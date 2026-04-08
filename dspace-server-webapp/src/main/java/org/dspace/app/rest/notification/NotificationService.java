package org.dspace.app.rest.notification;

import org.dspace.core.Email;

import javax.mail.MessagingException;
import java.io.IOException;

@FunctionalInterface
public interface NotificationService {

    public void send(String from, String to, String message, Email email) throws IOException, MessagingException;

}
