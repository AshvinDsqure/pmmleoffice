package org.dspace.app.rest.authn;

import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class OtpRateLimiter {

    @Autowired
    private ConfigurationService configurationService;

    //private static final int MAX_REQUESTS = 3;

    private static final long WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private final Map<String, Long> firstRequestTime = new ConcurrentHashMap<>();
    private final Map<String, Integer> requestCount = new ConcurrentHashMap<>();

    public synchronized boolean isAllowed(String ip) {
        System.out.println("Checking rate limit for IP: " + ip + ", current count: " + requestCount.getOrDefault(ip, 0) + ", first request time: " + firstRequestTime.get(ip));


        String MAX_REQUESTS =
                configurationService.getProperty(
                        "otp.max.request"
                );
        System.out.println("Max requests from configuration: " + MAX_REQUESTS);
        long now = System.currentTimeMillis();
        Long windowStart = firstRequestTime.get(ip);

        // window expired -> reset
        if (windowStart == null || now - windowStart > WINDOW_MILLIS) {
            firstRequestTime.put(ip, now);
            requestCount.put(ip, 1);
            return true;
        }

        int count = requestCount.getOrDefault(ip, 0);
        if (count >= Integer.parseInt(MAX_REQUESTS)) {
            return false;
        }

        requestCount.put(ip, count + 1);
        return true;
    }
}