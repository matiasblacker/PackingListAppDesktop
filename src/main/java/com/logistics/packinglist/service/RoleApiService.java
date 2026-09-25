package com.logistics.packinglist.service;

import com.logistics.packinglist.model.RoleModel;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoleApiService extends BaseApiService {
    private final String endpoint = baseUrl + "/api/v1/roles";

    public List<RoleModel> getAll(String queryParams) throws Exception {
        String url = endpoint + (queryParams != null ? queryParams : "");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
        if (response.body() == null || response.body().trim().isEmpty()) {
            return new ArrayList<>();
        }
        RoleModel[] array = gson.fromJson(response.body(), RoleModel[].class);
        return array != null ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
    }
    
    public List<RoleModel> getAll() throws Exception {
        return getAll(null);
    }

    public RoleModel create(RoleModel rol) throws Exception {
        String jsonBody = gson.toJson(rol);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
        return gson.fromJson(response.body(), RoleModel.class);
    }

    public RoleModel update(String id, RoleModel rol) throws Exception {
        String jsonBody = gson.toJson(rol);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "/" + id))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
        return gson.fromJson(response.body(), RoleModel.class);
    }

    public void delete(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "/" + id))
                .header("Authorization", "Bearer " + getToken())
                .DELETE()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
    }
}
