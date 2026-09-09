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

public class StockApiService extends BaseApiService {


    public List<StockModel> obtenerStocks() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/inventory-stocks?page=0&size=2000"))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parsePagedContent(response.body(), StockModel.class);
    }

    public List<StockModel> obtenerStocksPorProducto(String productId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/inventory-stocks/product/" + productId))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        if (json.has("success") && json.get("success").getAsBoolean()) {
            JsonArray data = json.getAsJsonArray("data");
            List<StockModel> list = new ArrayList<>();
            for (JsonElement elem : data) {
                list.add(gson.fromJson(elem, StockModel.class));
            }
            return list;
        } else {
            String msg = json.has("message") && !json.get("message").isJsonNull() ? json.get("message").getAsString() : "Error en el servidor";
            throw new Exception(msg);
        }
    }

    public List<StockModel> obtenerStocksPorUbicacion(String locationId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/inventory-stocks/location/" + locationId))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        if (json.has("success") && json.get("success").getAsBoolean()) {
            JsonArray data = json.getAsJsonArray("data");
            List<StockModel> list = new ArrayList<>();
            for (JsonElement elem : data) {
                list.add(gson.fromJson(elem, StockModel.class));
            }
            return list;
        } else {
            String msg = json.has("message") && !json.get("message").isJsonNull() ? json.get("message").getAsString() : "Error en el servidor";
            throw new Exception(msg);
        }
    }


    public StockModel ajustarStock(String productId, String locationId, int cantidad) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/inventory-stocks/adjust?productId=" + productId + "&locationId=" + locationId + "&cantidad=" + cantidad))
                .header("Authorization", "Bearer " + getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), StockModel.class);
    }

    public void transferirStock(String productId, String fromLocationId, String toLocationId, int cantidad, String tipoStock) throws Exception {
        String url = baseUrl + "/api/v1/inventory-stocks/transfer?productId=" + productId 
                + "&fromLocationId=" + fromLocationId 
                + "&toLocationId=" + toLocationId 
                + "&cantidad=" + cantidad
                + "&tipoStock=" + tipoStock;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkSuccess(response.body(), "transferir stock");
    }

    public void eliminarStock(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/inventory-stocks/" + id))
                .header("Authorization", "Bearer " + getToken())
                .DELETE()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkSuccess(response.body(), "eliminar stock");
}

}
