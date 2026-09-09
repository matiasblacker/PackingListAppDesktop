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

public class LocationApiService extends BaseApiService {


    public List<RegionModel> obtenerRegiones() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/locations/regions"))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        processErrorResponse(response);

        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        if (json.has("success") && json.get("success").getAsBoolean()) {
            JsonArray data = json.getAsJsonArray("data");
            List<RegionModel> list = new ArrayList<>();
            for (JsonElement elem : data) {
                list.add(gson.fromJson(elem, RegionModel.class));
            }
            return list;
        } else {
            String msg = json.has("message") && !json.get("message").isJsonNull() ? json.get("message").getAsString() : "Error en el servidor";
            throw new Exception(msg);
}

}
}
