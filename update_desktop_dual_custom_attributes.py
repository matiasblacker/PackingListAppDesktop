import re

# 1. Update ReceptionAnnouncementModel.java
with open("src/main/java/com/logistics/packinglist/model/ReceptionAnnouncementModel.java", "r") as f:
    content = f.read()

if "atributosPersonalizados" not in content:
    target = "@Builder.Default\n    private List<AnnouncementDetailModel> details = new ArrayList<>();"
    replacement = """@Builder.Default
    private java.util.Map<String, String> atributosPersonalizados = new java.util.HashMap<>();

    @Builder.Default
    private List<AnnouncementDetailModel> details = new ArrayList<>();"""
    content = content.replace(target, replacement)
    with open("src/main/java/com/logistics/packinglist/model/ReceptionAnnouncementModel.java", "w") as f:
        f.write(content)

# 2. Update ReceptionModel.java
with open("src/main/java/com/logistics/packinglist/model/ReceptionModel.java", "r") as f:
    content = f.read()

if "atributosPersonalizados" not in content:
    target = "@Builder.Default\n    private List<ReceptionDetailModel> details = new ArrayList<>();"
    replacement = """@Builder.Default
    private java.util.Map<String, String> atributosPersonalizados = new java.util.HashMap<>();

    @Builder.Default
    private List<ReceptionDetailModel> details = new ArrayList<>();"""
    content = content.replace(target, replacement)
    with open("src/main/java/com/logistics/packinglist/model/ReceptionModel.java", "w") as f:
        f.write(content)

# 3. Update ConfiguracionCamposDialog.java titles
with open("src/main/java/com/logistics/packinglist/ui/ConfiguracionCamposDialog.java", "r") as f:
    content = f.read()

target_cfg_title = "String entityName = \"RECEPCION\".equalsIgnoreCase(this.targetEntity) ? \"Recepciones de Carga\" : \"Anuncios de Carga\";"
replacement_cfg_title = """String entityName = switch (this.targetEntity.toUpperCase()) {
            case "ANUNCIO_DOC" -> "Anuncios de Carga (Documento)";
            case "ANUNCIO_PROD", "ANUNCIO" -> "Anuncios de Carga (Productos)";
            case "RECEPCION_DOC" -> "Recepciones de Carga (Documento)";
            case "RECEPCION_PROD", "RECEPCION" -> "Recepciones de Carga (Productos)";
            default -> this.targetEntity;
        };"""

content = content.replace(target_cfg_title, replacement_cfg_title)
with open("src/main/java/com/logistics/packinglist/ui/ConfiguracionCamposDialog.java", "w") as f:
    f.write(content)

print("Updated Models and ConfiguracionCamposDialog successfully!")
