package com.logistics.packinglist.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.github.javakeyring.Keyring;

public class AuthService {

    private static AuthService instance;
    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl = "http://localhost:8080";

    // Datos de la sesión activa
    private String token;
    private String email;
    private String nombre;
    private String apellido;
    private String role;
    private String companyId;
    private String warehouseId;
    private String logoUrl;

    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "LogoDownloaderThread");
        t.setDaemon(true);
        return t;
    });

    private AuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.gson = new Gson();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    /**
     * Intenta iniciar sesión contra el backend de Spring Boot.
     */
    public void login(String email, String password) throws Exception {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("email", email);
        requestBody.addProperty("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new Exception("No se pudo establecer conexión con el servidor. ¿Está el backend encendido?");
        }

        JsonObject jsonResponse;
        try {
            jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        } catch (Exception e) {
            throw new Exception("Respuesta del servidor inválida.");
        }

        if (jsonResponse != null && jsonResponse.has("code")) {
            String code = jsonResponse.get("code").getAsString();
            String message = jsonResponse.has("message") && !jsonResponse.get("message").isJsonNull()
                    ? jsonResponse.get("message").getAsString() : "Error en el servidor";
            throw new com.logistics.packinglist.exception.ApiException(code, message);
        }

        boolean success = jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean();

        if (success && response.statusCode() == 200) {
            JsonObject data = jsonResponse.getAsJsonObject("data");
            this.token = data.get("token").getAsString();
            this.email = data.get("email").getAsString();
            this.nombre = data.get("nombre").getAsString();
            this.apellido = data.get("apellido").getAsString();
            this.role = data.get("role").getAsString();
            this.companyId = data.has("companyId") && !data.get("companyId").isJsonNull() ? data.get("companyId").getAsString() : null;
            this.warehouseId = data.has("warehouseId") && !data.get("warehouseId").isJsonNull() ? data.get("warehouseId").getAsString() : null;
            this.logoUrl = data.has("logoUrl") && !data.get("logoUrl").isJsonNull() ? data.get("logoUrl").getAsString() : null;
            descargarLogoLocal();
            WebSocketManager.getInstance().connect();
        } else {
            String errorMsg = "Credenciales incorrectas o error en el servidor.";
            if (jsonResponse.has("message") && !jsonResponse.get("message").isJsonNull()) {
                errorMsg = jsonResponse.get("message").getAsString();
            }
            throw new Exception(errorMsg);
        }
    }

    /**
     * Cierra la sesión activa notificando al backend de forma asíncrona y limpiando la sesión local.
     */
    public void logout() {
        if (token != null) {
            WebSocketManager.getInstance().disconnect();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/auth/logout"))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(5))
                    .build();

            // Llamada no bloqueante
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        }
        clearSession();
    }

    public void clearSession() {
        try {
            if (this.email != null) {
                Keyring.create().deletePassword("PackingListApp", this.email);
            }
        } catch (Exception e) {}
        this.token = null;
        this.email = null;
        this.nombre = null;
        this.apellido = null;
        this.role = null;
        this.companyId = null;
        this.warehouseId = null;
        this.logoUrl = null;
        borrarLogoLocal();
    }

    // Getters para consultar los datos de la sesión
    public String getBaseUrl() { return baseUrl; }
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getRole() { return role; }
    public String getCompanyId() { return companyId; }
    public String getWarehouseId() { return warehouseId; }
    public String getLogoUrl() { return logoUrl; }

    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

    /**
     * Descarga el logo corporativo de la sesión actual de forma asíncrona a un archivo temporal local.
     */
    public void descargarLogoLocal() {
        if (logoUrl == null || logoUrl.isEmpty()) {
            borrarLogoLocal();
            return;
        }

        // Ejecutar descarga en segundo plano usando ExecutorService
        executor.submit(() -> {
            try {
                java.net.URL url = new java.net.URL(logoUrl);
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "packinglistapp");
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                File localLogo = new File(tempDir, "company_logo.png");
                
                try (java.io.InputStream in = url.openStream();
                     java.io.FileOutputStream out = new java.io.FileOutputStream(localLogo)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                System.out.println("Logo descargado correctamente en: " + localLogo.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error al descargar logo desde la URL " + logoUrl + ": " + e.getMessage());
            }
        });
    }

    public File getLogoLocalFile() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "packinglistapp");
        File localLogo = new File(tempDir, "company_logo.png");
        return localLogo.exists() ? localLogo : null;
    }

    public void borrarLogoLocal() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "packinglistapp");
        File localLogo = new File(tempDir, "company_logo.png");
        if (localLogo.exists()) {
            localLogo.delete();
        }
    }
}
