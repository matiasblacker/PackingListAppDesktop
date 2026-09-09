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
import com.logistics.packinglist.model.BackOrderModel;
import com.logistics.packinglist.model.TrackingEventModel;
import com.logistics.packinglist.model.BackOrderDetailModel;
import com.logistics.packinglist.model.BackOrderHistoryModel;
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

public class OrderNoteApiService extends BaseApiService {


    public List<OrderNoteModel> obtenerNotasPedido() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/order-notes?page=0&size=1000"))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parsePagedContent(response.body(), OrderNoteModel.class);
    }

    public String obtenerSiguienteFolio() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/order-notes/next-folio"))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        if (json.has("success") && json.get("success").getAsBoolean()) {
            return json.get("data").getAsString();
        }
        throw new Exception("Error al obtener el siguiente folio");
    }

    public OrderNoteModel crearNotaPedido(OrderNoteModel note) throws Exception {
        String jsonBody = gson.toJson(note);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/order-notes"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), OrderNoteModel.class);
    }

    public OrderNoteModel actualizarNotaPedido(String id, OrderNoteModel note) throws Exception {
        String jsonBody = gson.toJson(note);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/order-notes/" + id))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), OrderNoteModel.class);
    }

    public void eliminarNotaPedido(String id, String motivo) throws Exception {
        String queryParam = motivo != null ? "?motivo=" + java.net.URLEncoder.encode(motivo, java.nio.charset.StandardCharsets.UTF_8) : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/order-notes/" + id + queryParam))
                .header("Authorization", "Bearer " + getToken())
                .DELETE()
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkSuccess(response.body(), "anular nota de pedido");
    }

    public OrderNoteModel procesarBackorderABoNP(String boId, Map<String, Integer> quantityMap) throws Exception {
        String jsonBody = gson.toJson(quantityMap != null ? quantityMap : Collections.emptyMap());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/order-notes/" + boId + "/process-backorder"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), OrderNoteModel.class);
    }

    public void registrarEventoTracking(String orderNoteId, String trackingNumber, String estado, String descripcion, String ubicacionNombre, Double latitud, Double longitud, String nombreCourier, String numeroSeguimientoCourier, String fotosUrls) throws Exception {
        registrarEventoTracking(orderNoteId, trackingNumber, estado, descripcion, ubicacionNombre, latitud, longitud, nombreCourier, numeroSeguimientoCourier, fotosUrls, null, null, null, null);
    }

    public void registrarEventoTracking(String orderNoteId, String trackingNumber, String estado, String descripcion, String ubicacionNombre, Double latitud, Double longitud, String nombreCourier, String numeroSeguimientoCourier, String fotosUrls, String retiradoPorNombre, String retiradoPorRut, String retiradoPorPatente, String fechaHoraISO) throws Exception {
        JsonObject eventJson = new JsonObject();
        if (orderNoteId != null) eventJson.addProperty("orderNoteId", orderNoteId);
        if (trackingNumber != null) eventJson.addProperty("trackingNumber", trackingNumber);
        if (estado != null) eventJson.addProperty("estado", estado);
        if (descripcion != null) eventJson.addProperty("descripcion", descripcion);
        if (ubicacionNombre != null) eventJson.addProperty("ubicacionNombre", ubicacionNombre);
        if (latitud != null) eventJson.addProperty("latitud", latitud);
        if (longitud != null) eventJson.addProperty("longitud", longitud);
        if (nombreCourier != null) eventJson.addProperty("nombreCourier", nombreCourier);
        if (numeroSeguimientoCourier != null) eventJson.addProperty("numeroSeguimientoCourier", numeroSeguimientoCourier);
        if (fotosUrls != null) eventJson.addProperty("fotosUrls", fotosUrls);
        if (retiradoPorNombre != null) eventJson.addProperty("retiradoPorNombre", retiradoPorNombre);
        if (retiradoPorRut != null) eventJson.addProperty("retiradoPorRut", retiradoPorRut);
        if (retiradoPorPatente != null) eventJson.addProperty("retiradoPorPatente", retiradoPorPatente);
        if (fechaHoraISO != null) eventJson.addProperty("fechaHora", fechaHoraISO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/tracking/events"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(eventJson.toString()))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkSuccess(response.body(), "registrar evento de tracking");
    }

    public String subirFotoTracking(String trackingNumber, File archivoFoto) throws Exception {
        String boundary = "---PackingListBoundary" + System.currentTimeMillis();
        byte[] fileBytes = java.nio.file.Files.readAllBytes(archivoFoto.toPath());
        String filename = archivoFoto.getName();

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        baos.write(("--" + boundary + "\r\n").getBytes());
        baos.write("Content-Disposition: form-data; name=\"trackingNumber\"\r\n\r\n".getBytes());
        baos.write(trackingNumber.getBytes());
        baos.write("\r\n".getBytes());

        baos.write(("--" + boundary + "\r\n").getBytes());
        baos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n").getBytes());
        baos.write("Content-Type: image/jpeg\r\n\r\n".getBytes());
        baos.write(fileBytes);
        baos.write("\r\n".getBytes());

        baos.write(("--" + boundary + "--\r\n").getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/tracking/upload-photo"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkSuccess(response.body(), "subir foto de tracking");
        JsonObject root = gson.fromJson(response.body(), JsonObject.class);
        JsonObject data = root.getAsJsonObject("data");
        return data.get("url").getAsString();
    }

    public void consolidarTrackings(List<String> orderNoteIds) throws Exception {
        JsonArray array = new JsonArray();
        for (String id : orderNoteIds) {
            array.add(id);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/tracking/consolidate"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(array.toString()))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkSuccess(response.body(), "consolidar trackings");
    }

    public List<BackOrderModel> obtenerBackOrders() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/back-orders"))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseList(response.body(), BackOrderModel.class);
    }

    public BackOrderModel obtenerBackOrderPorId(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/back-orders/" + id))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), BackOrderModel.class);
    }

    public List<BackOrderHistoryModel> obtenerHistorialBackOrder(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/back-orders/" + id + "/history"))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseList(response.body(), BackOrderHistoryModel.class);
    }

    public OrderNoteModel procesarBackOrder(String id, Map<String, Integer> quantityMap) throws Exception {
        String jsonBody = gson.toJson(quantityMap != null ? quantityMap : Collections.emptyMap());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/back-orders/" + id + "/process"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), OrderNoteModel.class);
    }

    public BackOrderModel anularBackOrder(String id, String motivo) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("motivo", motivo);
        String jsonBody = gson.toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/back-orders/" + id + "/cancel"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), BackOrderModel.class);
    }

    public void registrarImpresionPicking(String noteId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/order-notes/" + noteId + "/picking-printed"))
                .header("Authorization", "Bearer " + getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkSuccess(response.body(), "registrar impresion de picking");
    }

    public List<TrackingEventModel> obtenerEventosTracking(String noteId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/tracking/note/" + noteId + "/events"))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseList(response.body(), TrackingEventModel.class);
    }

    public List<OrderNoteModel> obtenerNotasPedidoConsolidadas(String masterTracking) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/order-notes/consolidated/" + java.net.URLEncoder.encode(masterTracking, "UTF-8")))
                .header("Authorization", "Bearer " + getToken())
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseList(response.body(), OrderNoteModel.class);
    }
}
