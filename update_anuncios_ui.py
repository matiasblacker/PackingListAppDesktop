import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

# Add import for ProductFieldDefinitionModel
if "import com.logistics.packinglist.model.ProductFieldDefinitionModel;" not in content:
    content = content.replace("import com.logistics.packinglist.model.ProductModel;", "import com.logistics.packinglist.model.ProductModel;\nimport com.logistics.packinglist.model.ProductFieldDefinitionModel;")

# Update DetailRow
old_detail_row = """    public static class DetailRow {
        private final String productId;
        private final String productoDesc;
        private int cantidad;

        public DetailRow(String productId, String productoDesc, int cantidad) {
            this.productId = productId;
            this.productoDesc = productoDesc;
            this.cantidad = cantidad;
        }

        public String getProductId() { return productId; }
        public String getProductoDesc() { return productoDesc; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    }"""

new_detail_row = """    public static class DetailRow {
        private final String productId;
        private final String productoDesc;
        private int cantidad;
        private java.util.Map<String, String> atributosPersonalizados;

        public DetailRow(String productId, String productoDesc, int cantidad) {
            this(productId, productoDesc, cantidad, new java.util.HashMap<>());
        }

        public DetailRow(String productId, String productoDesc, int cantidad, java.util.Map<String, String> atributosPersonalizados) {
            this.productId = productId;
            this.productoDesc = productoDesc;
            this.cantidad = cantidad;
            this.atributosPersonalizados = atributosPersonalizados != null ? atributosPersonalizados : new java.util.HashMap<>();
        }

        public String getProductId() { return productId; }
        public String getProductoDesc() { return productoDesc; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public java.util.Map<String, String> getAtributosPersonalizados() { return atributosPersonalizados; }
        public void setAtributosPersonalizados(java.util.Map<String, String> atributosPersonalizados) { this.atributosPersonalizados = atributosPersonalizados; }
    }"""

content = content.replace(old_detail_row, new_detail_row)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)
print("Updated DetailRow in AnunciosDialog")
