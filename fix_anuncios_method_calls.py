with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

content = content.replace("btnConfigAtributos.setOnAction(e -> abrirConfiguracionAtributos());", "btnConfigAtributos.setOnAction(e -> abrirConfiguracionAtributosProd());")
content = content.replace("cargarCamposPersonalizados();", "cargarCamposPersonalizadosDocumento();\n            cargarCamposPersonalizadosProducto();")

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Fixed method calls in AnunciosDialog.java!")
