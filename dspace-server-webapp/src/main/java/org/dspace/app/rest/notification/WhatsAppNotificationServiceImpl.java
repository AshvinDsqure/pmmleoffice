package org.dspace.app.rest.notification;
import org.dspace.core.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service("whatsAppNotificationServiceImpl")
public class WhatsAppNotificationServiceImpl implements NotificationService{

    private static final String INSTANCE_ID = "instance149654";
    private static final String TOKEN = "rhn7of8bski39zbg";// Replace with your actual instance ID

    @Value("${dspace.ultramsg.instance.id}")
    private String instanceId;

    @Value("${dspace.ultramsg.token}")
    private String token;

    @Value("${dspace.ultramsg.base.url}")
    private String baseUrl;


    @Override
    public void send(String from, String totelephoneNumber, String message, Email email) throws IOException, MessagingException {
        String url = baseUrl + "/" + instanceId + "/messages/chat";

        System.out.println("url::"+baseUrl);
        System.out.println("token::"+token);
        System.out.println("instanceId::"+instanceId);
        System.out.println("totelephoneNumber::"+totelephoneNumber);
        System.out.println("message::"+message);


        Map<String, String> payload = new HashMap<>();
        payload.put("to", totelephoneNumber);
        payload.put("body", message);
        payload.put("token", token);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(url, payload, String.class);
    }
}
