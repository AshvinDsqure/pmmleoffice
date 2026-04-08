package org.dspace.app.rest.notification;

import org.dspace.core.Email;
import org.springframework.stereotype.Service;

@Service("sMSNotificationServiceImpl")
public class SMSNotificationServiceImpl implements NotificationService{
    @Override
    public void send(String from, String to, String message, Email email) {

    }
}