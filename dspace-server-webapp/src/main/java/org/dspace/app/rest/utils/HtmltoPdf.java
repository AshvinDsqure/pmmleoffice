package org.dspace.app.rest.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dspace.content.Bitstream;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HtmltoPdf {

    public static void main(String[] args) throws IOException {
        String htmlContent="<html><body><h2>Sample Table</h2><table border='1' cellpadding='10'><thead><tr><th>Header 1</th><th>Header 2</th><th>Header 3</th></tr></thead><tbody><tr><td>Row 1, Cell 1</td><td>Row 1, Cell 2</td><td>Row 1, Cell 3</td></tr><tr><td>Row 2, Cell 1</td><td>Row 2, Cell 2</td><td>Row 2, Cell 3</td></tr><tr><td>Row 3, Cell 1</td><td>Row 3, Cell 2</td><td>Row 3, Cell 3</td></tr></tbody></table></body></html>";
       // convertHtmlToPdf2(htmlContent);
    }

    public static void  convertHtmlToPdf2(String htmlContent, File tempPdfFile) throws IOException {
        String url = "http://lab.d2t.co:36/api/admin/dspace/htmltopdf";
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

            // Check if the response is successful
            if (statusCode == 200) { // HTTP 200 OK
                // Get the response body for further processing
                org.apache.http.HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    byte[] responseBody = EntityUtils.toByteArray(responseEntity); // Read from responseEntity

                    // Create temporary file for PDF
                    // Write the PDF bytes to the specified file
                    try (FileOutputStream fos = new FileOutputStream(tempPdfFile)) {
                        fos.write(responseBody);
                    }

                    System.out.println("PDF file created at: " + tempPdfFile.getAbsolutePath());
                     // Return an InputStream for further processing if needed
                } else {
                    System.out.println("No response content found.");
                }
            } else {
                System.out.println("Error: Received status code " + statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InputStream convertHtmlToPdf(String htmlContent) {
        // URL of the API
        String url = "http://lab.d2t.co:36/api/admin/dspace/htmltopdf";

        // Creating a RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();
        // Setting up the headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");  // You can add more headers if needed
        // Creating the request body with the HTML content
        String requestBody = "{\"htmlContent\": \"" + htmlContent + "\"}";
        // Wrapping the headers and body into an HttpEntity
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        // Sending the POST request
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );
        // Checking the response
        if (response.getStatusCode().is2xxSuccessful()) {
            return convertByteArrayToInputStream(response.getBody().getBytes());
        } else {
            System.out.println("Error occurred: " + response.getStatusCode());
            return null;
        }
    }
    public static InputStream convertByteArrayToInputStream(byte[] byteArray) {
        if (byteArray != null) {
            return new ByteArrayInputStream(byteArray);
        } else {
            throw new IllegalArgumentException("Byte array cannot be null");
        }
    }
    public static void writeInputStreamToFile(InputStream inputStream, File file) throws IOException {
        // Define buffer size
        byte[] buffer = new byte[1024];
        int bytesRead;

        // Try with resources to auto-close streams
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            // Read from InputStream and write to FileOutputStream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Close InputStream
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

}
