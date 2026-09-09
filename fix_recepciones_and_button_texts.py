# 1. Update AnunciosDialog.java button texts
with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content_a = f.read()

content_a = content_a.replace(
    'Button btnConfigAtributosDoc = new Button("Configurar Atributos Documento", iconCogDoc);',
    'Button btnConfigAtributosDoc = new Button("Configurar Documento", iconCogDoc);'
)

content_a = content_a.replace(
    'Button btnConfigAtributosProd = new Button("Configurar Atributos Producto", iconCogProd);',
    'Button btnConfigAtributosProd = new Button("Configurar Productos", iconCogProd);'
)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content_a)

# 2. Update RecepcionesDialog.java button text and initialize docCustomFieldsPane
with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content_r = f.read()

content_r = content_r.replace(
    'Button btnConfigAtributos = new Button("Configurar Atributos", iconCog);',
    'Button btnConfigAtributos = new Button("Configurar Documento", iconCog);'
)

# Fix docCustomFieldsPane initialization
target_init = """        customFieldsPane = new HBox(8);
        customFieldsPane.setAlignment(Pos.CENTER_LEFT);
        customFieldsPane.setPadding(new Insets(4, 0, 4, 0));"""

replacement_init = """        docCustomFieldsPane = new HBox(8);
        docCustomFieldsPane.setAlignment(Pos.CENTER_LEFT);
        docCustomFieldsPane.setPadding(new Insets(4, 0, 4, 0));
        customFieldsPane = new HBox(8);"""

content_r = content_r.replace(target_init, replacement_init)

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content_r)

print("Updated AnunciosDialog and RecepcionesDialog button texts and docCustomFieldsPane initialization successfully!")
