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

public class WarehouseZoneApiService extends BaseApiService {


    public List<com.logistics.packinglist.model.WarehouseZoneModel> obtenerZonasPorBodega(String warehouseId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/warehouse-zones/warehouse/" + warehouseId))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        if (json.has("success") && json.get("success").getAsBoolean()) {
            JsonArray data = json.getAsJsonArray("data");
            List<com.logistics.packinglist.model.WarehouseZoneModel> list = new ArrayList<>();
            for (JsonElement elem : data) {
                list.add(gson.fromJson(elem, com.logistics.packinglist.model.WarehouseZoneModel.class));
            }
            return list;
        } else {
            String msg = json.has("message") && !json.get("message").isJsonNull() ? json.get("message").getAsString() : "Error en el servidor";
            throw new Exception(msg);
        }
    }

    public com.logistics.packinglist.model.WarehouseZoneModel crearZona(com.logistics.packinglist.model.WarehouseZoneModel zone) throws Exception {
        String jsonBody = gson.toJson(zone);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/warehouse-zones"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), com.logistics.packinglist.model.WarehouseZoneModel.class);
    }

    public com.logistics.packinglist.model.WarehouseZoneModel actualizarZona(String id, com.logistics.packinglist.model.WarehouseZoneModel zone) throws Exception {
        String jsonBody = gson.toJson(zone);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/warehouse-zones/" + id))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), com.logistics.packinglist.model.WarehouseZoneModel.class);
    }

    public void eliminarZona(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/warehouse-zones/" + id))
                .header("Authorization", "Bearer " + getToken())
                .DELETE()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkSuccess(response.body(), "eliminar zona");
}

}
