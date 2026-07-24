package client.frontend.api;

import client.frontend.util.SessionManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.File;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static void addAuthHeader(HttpRequest.Builder requestBuilder) {
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.trim().isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
    }

    public static HttpResponse<String> get(String endpoint) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .GET();
        addAuthHeader(requestBuilder);
        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> post(String endpoint, String jsonBody) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody == null ? "" : jsonBody));
        addAuthHeader(requestBuilder);
        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> put(String endpoint, String jsonBody) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody == null ? "" : jsonBody));
        addAuthHeader(requestBuilder);
        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> delete(String endpoint) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .DELETE();
        addAuthHeader(requestBuilder);
        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> uploadFile(String endpoint, File file) throws Exception {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) mimeType = "application/octet-stream";

        StringBuilder builder = new StringBuilder();
        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
        builder.append("Content-Type: ").append(mimeType).append("\r\n\r\n");

        byte[] header = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[header.length + fileBytes.length + footer.length];
        System.arraycopy(header, 0, body, 0, header.length);
        System.arraycopy(fileBytes, 0, body, header.length, fileBytes.length);
        System.arraycopy(footer, 0, body, header.length + fileBytes.length, footer.length);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));
        addAuthHeader(requestBuilder);

        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
}