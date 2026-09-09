# 1. Update ProductModel.java
with open("src/main/java/com/logistics/packinglist/model/ProductModel.java", "r") as f:
    content_m = f.read()

if "imagenUrl" not in content_m:
    target_m = "@Builder.Default\n    private String unidadMedida = \"UNIDADES\";"
    replacement_m = """private String imagenUrl;
    @Builder.Default
    private Double peso = 0.0;
    @Builder.Default
    private Double largo = 0.0;
    @Builder.Default
    private Double ancho = 0.0;
    @Builder.Default
    private Double alto = 0.0;
    @Builder.Default
    private Double volumen = 0.0;

    @Builder.Default
    private String unidadMedida = "UNIDADES";"""
    content_m = content_m.replace(target_m, replacement_m)
    with open("src/main/java/com/logistics/packinglist/model/ProductModel.java", "w") as f:
        f.write(content_m)

# 2. Update ProductApiService.java
with open("src/main/java/com/logistics/packinglist/service/ProductApiService.java", "r") as f:
    content_api = f.read()

if "subirImagenProducto" not in content_api:
    new_upload_api = """
    public String subirImagenProducto(String warehouseId, String productId, File file) throws Exception {
        String boundary = "------------" + System.currentTimeMillis();
        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
        
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        
        if (warehouseId != null && !warehouseId.isEmpty()) {
            out.write(("--" + boundary + "\\r\\n").getBytes());
            out.write("Content-Disposition: form-data; name=\\"warehouseId\\"\\r\\n\\r\\n".getBytes());
            out.write((warehouseId + "\\r\\n").getBytes());
        }
        if (productId != null && !productId.isEmpty()) {
            out.write(("--" + boundary + "\\r\\n").getBytes());
            out.write("Content-Disposition: form-data; name=\\"productId\\"\\r\\n\\r\\n".getBytes());
            out.write((productId + "\\r\\n").getBytes());
        }
        
        out.write(("--" + boundary + "\\r\\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\\"file\\"; filename=\\"" + file.getName() + "\\"\\r\\n").getBytes());
        out.write("Content-Type: application/octet-stream\\r\\n\\r\\n".getBytes());
        out.write(fileBytes);
        out.write("\\r\\n".getBytes());
        out.write(("--" + boundary + "--\\r\\n").getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/products/upload-image"))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()))
                .timeout(Duration.ofSeconds(15))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseSingle(response.body(), String.class);
    }
}"""
    content_api = content_api.rstrip().rstrip("}") + new_upload_api + "\n"
    with open("src/main/java/com/logistics/packinglist/service/ProductApiService.java", "w") as f:
        f.write(content_api)

# 3. Update MantenimientoService.java
with open("src/main/java/com/logistics/packinglist/service/MantenimientoService.java", "r") as f:
    content_mant = f.read()

if "subirImagenProducto" not in content_mant:
    new_upload_mant = """
    public String subirImagenProducto(String warehouseId, String productId, File file) throws Exception {
        return productApiService.subirImagenProducto(warehouseId, productId, file);
    }
}"""
    content_mant = content_mant.rstrip().rstrip("}") + new_upload_mant + "\n"
    with open("src/main/java/com/logistics/packinglist/service/MantenimientoService.java", "w") as f:
        f.write(content_mant)

print("Updated ProductModel, ProductApiService, and MantenimientoService successfully!")
