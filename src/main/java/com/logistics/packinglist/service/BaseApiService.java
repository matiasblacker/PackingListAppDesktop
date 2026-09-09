package com.logistics.packinglist.service;

import com.google.gson.*;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.logistics.packinglist.model.*;

public abstract class BaseApiService {
    protected final HttpClient httpClient;
    protected final Gson gson;
    protected final String baseUrl = "http://localhost:8080";

    public BaseApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.gson = new Gson();
    }

    protected String getToken() {
        return AuthService.getInstance().getToken();
    }

    private void checkAndThrowError(String responseBody) throws Exception {
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            try {
                JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                if (json != null && json.has("code")) {
                    String code = json.get("code").getAsString();
                    String message = json.has("message") && !json.get("message").isJsonNull()
                            ? json.get("message").getAsString() : "Error en el servidor";
                    throw new com.logistics.packinglist.exception.ApiException(code, message);
                }
            } catch (com.logistics.packinglist.exception.ApiException e) {
                throw e;
            } catch (Exception e) {
                // Not structured JSON error, ignore
            }
        }
    }

    protected <T> List<T> parsePagedContent(String responseBody, Class<T> clazz) throws Exception {
        checkAndThrowError(responseBody);
        try {
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            if (json.has("success") && json.get("success").getAsBoolean()) {
                JsonObject data = json.getAsJsonObject("data");
                if (data != null && data.has("content")) {
                    JsonArray content = data.getAsJsonArray("content");
                    List<T> list = new ArrayList<>();
                    for (JsonElement elem : content) {
                        list.add(gson.fromJson(elem, clazz));
                    }
                    return list;
                }
            }
        } catch (Exception e) {
            if (e instanceof com.logistics.packinglist.exception.ApiException) {
                throw e;
            }
            System.err.println("Error parsing paged content: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public CompanyModel actualizarTipoCambio(String companyId, String tipoMoneda, double valorCambio) throws Exception {
        Map<String, Object> body = new HashMap<>();
        if (companyId != null) {
            body.put("companyId", companyId);
        }
        body.put("tipoMoneda", tipoMoneda);
        body.put("valorCambio", valorCambio);
        String jsonBody = gson.toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/companies/exchange-rate"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), CompanyModel.class);
    }

    protected <T> List<T> parseList(String responseBody, Class<T> clazz) throws Exception {
        checkAndThrowError(responseBody);
        try {
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            if (json.has("success") && json.get("success").getAsBoolean()) {
                JsonArray data = json.getAsJsonArray("data");
                if (data != null) {
                    List<T> list = new ArrayList<>();
                    for (JsonElement elem : data) {
                        list.add(gson.fromJson(elem, clazz));
                    }
                    return list;
                }
            }
        } catch (Exception e) {
            if (e instanceof com.logistics.packinglist.exception.ApiException) {
                throw e;
            }
            System.err.println("Error parsing list: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    protected void processErrorResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 400) {
            String body = response.body();
            checkAndThrowError(body);
            throw new Exception("Error en el servidor: Código HTTP " + response.statusCode());
        }
    }

    protected <T> T parseSingle(String responseBody, Class<T> clazz) throws Exception {
        checkAndThrowError(responseBody);
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        if (json.has("success") && json.get("success").getAsBoolean()) {
            return gson.fromJson(json.get("data"), clazz);
        } else {
            String msg = json.has("message") && !json.get("message").isJsonNull() ? json.get("message").getAsString() : "Error en el servidor";
            throw new Exception(msg);
        }
    }

    protected void checkSuccess(String responseBody, String opName) throws Exception {
        checkAndThrowError(responseBody);
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        if (!json.has("success") || !json.get("success").getAsBoolean()) {
            String msg = json.has("message") && !json.get("message").isJsonNull() ? json.get("message").getAsString() : "Error en el servidor al " + opName;
            throw new Exception(msg);
        }
    }
}
