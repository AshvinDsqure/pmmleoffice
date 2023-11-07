package org.dspace.app.rest.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static void main(String[] args) {

       /* File s=new File("D://a.docx");
        Optional<String> ss=getExtensionByStringHandling(s.getName());
        System.out.println(ss.get());*/

        try {
            // Create an HttpClient instance
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // Define the URL you want to send the POST request to
            String url = "http://202.21.38.245:8989/api/infospace/procdetails/callback";

            // Create an HttpPost request
            HttpPost httpPost = new HttpPost(url);

            // Set the request body, headers, etc.
            String requestBody = "{\"queueid\": \"qqaa\",\"procstatus\": \"inprogress\"}"; // Your request body JSON
            httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));
            httpPost.setHeader("Content-Type", "application/json"); // Set request content type

            // Execute the POST request
            HttpResponse response = httpClient.execute(httpPost);

            // Process the response
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            // Print the response
            System.out.println("HTTP Status Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);

            // Close the HttpClient when done
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static String getNameWithoutExtension(String file) {
        int dotIndex = file.lastIndexOf('.');
        return (dotIndex == -1) ? file : file.substring(0, dotIndex);
    }

    public static  Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}
