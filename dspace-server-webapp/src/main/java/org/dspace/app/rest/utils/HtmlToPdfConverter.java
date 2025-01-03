package org.dspace.app.rest.utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

public class HtmlToPdfConverter {
    public static void main(String[] args) {

        String url = "http://lab.d2t.co:36/api/admin/dspace/htmltopdf";
        String htmlContent = "<!DOCTYPE html><html><head><title>Test</title></head><body><h1>Hello, World!</h1></body></html>"; // Your HTML content here
        HttpPost httpPost = new HttpPost(url);
        // Create a JSON string containing the HTML content
        String json = String.format("{\"htmlContent\": \"%s\"}", htmlContent.replace("\"", "\\\"")); // Escape quotes in HTML content
        // Set the entity with JSON content
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            // Get the status code
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Status code: " + statusCode);

            // Get the response body for further processing
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                System.out.println("Response body: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
