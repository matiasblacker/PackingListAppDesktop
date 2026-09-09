package com.logistics.packinglist.service;

import com.logistics.packinglist.model.DepositModel;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Arrays;

public class DepositApiService extends BaseApiService {

    public List<DepositModel> obtenerDepositos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/deposits"))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);

        DepositModel[] array = gson.fromJson(response.body(), DepositModel[].class);
        return Arrays.asList(array);
    }

    public DepositModel crearDeposito(DepositModel model) throws Exception {
        String json = gson.toJson(model);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/deposits"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
        return gson.fromJson(response.body(), DepositModel.class);
    }

    public DepositModel actualizarDeposito(String id, DepositModel model) throws Exception {
        String json = gson.toJson(model);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/deposits/" + id))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
        return gson.fromJson(response.body(), DepositModel.class);
    }

    public void eliminarDeposito(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/deposits/" + id))
                .header("Authorization", "Bearer " + getToken())
                .DELETE()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
    }

    public List<com.logistics.packinglist.model.WarehouseModel> obtenerBodegasPorDeposito(String depositId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/deposits/" + depositId + "/warehouses"))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
        com.logistics.packinglist.model.WarehouseModel[] array = gson.fromJson(response.body(), com.logistics.packinglist.model.WarehouseModel[].class);
        return java.util.Arrays.asList(array);
    }
}
