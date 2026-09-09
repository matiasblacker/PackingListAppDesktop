package com.logistics.packinglist.service;

import com.google.gson.*;
import com.logistics.packinglist.model.CompanyModel;
import com.logistics.packinglist.model.UserModel;
import com.logistics.packinglist.model.WarehouseModel;
import com.logistics.packinglist.model.RegionModel;
import com.logistics.packinglist.model.SupplierModel;
import com.logistics.packinglist.model.CustomerModel;
import com.logistics.packinglist.model.ProductModel;
import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.model.ProductFieldDefinitionModel;
import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.model.DispatchModel;
import com.logistics.packinglist.model.ReceptionAnnouncementModel;
import com.logistics.packinglist.model.ReceptionModel;
import com.logistics.packinglist.model.NotificationModel;
import com.logistics.packinglist.model.PackingList;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.List;

public class ProductFieldApiService extends BaseApiService {


    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String warehouseId, String supplierId) throws Exception {
        return obtenerDefinicionesCampos(companyId, null, warehouseId, supplierId);
    }

    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String targetEntity, String warehouseId, String supplierId) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(baseUrl).append("/api/v1/products/fields-definition?companyId=").append(companyId);
        if (targetEntity != null && !targetEntity.isEmpty()) {
            urlBuilder.append("&targetEntity=").append(targetEntity);
        }
        if (warehouseId != null && !warehouseId.isEmpty()) {
            urlBuilder.append("&warehouseId=").append(warehouseId);
        }
        if (supplierId != null && !supplierId.isEmpty()) {
            urlBuilder.append("&supplierId=").append(supplierId);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseList(response.body(), ProductFieldDefinitionModel.class);
    }

    public ProductFieldDefinitionModel crearDefinicionCampo(ProductFieldDefinitionModel model) throws Exception {
        String jsonBody = gson.toJson(model);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/products/fields-definition"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), ProductFieldDefinitionModel.class);
    }

    public ProductFieldDefinitionModel actualizarDefinicionCampo(String id, ProductFieldDefinitionModel model) throws Exception {
        String jsonBody = gson.toJson(model);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/products/fields-definition/" + id))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), ProductFieldDefinitionModel.class);
    }

    public void eliminarDefinicionCampo(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/products/fields-definition/" + id))
                .header("Authorization", "Bearer " + getToken())
                .DELETE()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkSuccess(response.body(), "eliminar definición de campo");
    }

    public List<NotificationModel> obtenerNotificacionesFiltradas(String entityType, String startDate, String endDate, int page, int size) throws Exception {
        return obtenerNotificacionesFiltradas(entityType, startDate, endDate, false, page, size);
    }

    public List<NotificationModel> obtenerNotificacionesFiltradas(String entityType, String startDate, String endDate, boolean onlyUndismissed, int page, int size) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(baseUrl)
                .append("/api/v1/notifications?page=").append(page)
                .append("&size=").append(size)
                .append("&onlyUndismissed=").append(onlyUndismissed);

        if (entityType != null && !entityType.isEmpty() && !"TODOS".equalsIgnoreCase(entityType)) {
            urlBuilder.append("&entityType=").append(entityType);
        }
        if (startDate != null && !startDate.isEmpty()) {
            urlBuilder.append("&startDate=").append(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            urlBuilder.append("&endDate=").append(endDate);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
        return parsePagedContent(response.body(), NotificationModel.class);
    }

    public void descartarNotificacion(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/notifications/" + id))
                .header("Authorization", "Bearer " + getToken())
                .DELETE()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
    }

    public void descartarTodasNotificaciones() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/notifications"))
                .header("Authorization", "Bearer " + getToken())
                .DELETE()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);
    }
}
