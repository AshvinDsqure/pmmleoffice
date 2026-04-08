package org.dspace.app.rest.notification;
import org.dspace.core.Email;
import org.springframework.stereotype.Service;

@Service("whatsAppNotificationServiceImpl")
public class WhatsAppNotificationServiceImpl implements NotificationService{



    @Override
    public void send(String from, String to, String message, Email email) {

    }


}
