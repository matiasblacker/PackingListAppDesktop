import os

files_to_patch = {
    "src/main/java/com/logistics/packinglist/ui/DespachosDialog.java": [
        ("new SimpleStringProperty(c.getValue().getFecha())", "new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFecha()))")
    ],
    "src/main/java/com/logistics/packinglist/ui/PackingListDialog.java": [
        ("new SimpleStringProperty(c.getValue().getFecha())", "new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFecha()))")
    ],
    "src/main/java/com/logistics/packinglist/ui/BackOrdersDialog.java": [
        ("c.getValue().getFechaHora() != null ? c.getValue().getFechaHora().replace(\"T\", \" \") : \"-\"", "DateFormatterUtil.format(c.getValue().getFechaHora())"),
        ("c.getValue().getFecha() != null ? c.getValue().getFecha().replace(\"T\", \" \") : \"-\"", "DateFormatterUtil.format(c.getValue().getFecha())"),
        ("bo.getFecha() != null ? bo.getFecha().replace(\"T\", \" \") : \"-\"", "DateFormatterUtil.format(bo.getFecha())")
    ],
    "src/main/java/com/logistics/packinglist/ui/NotasPedidoDialog.java": [
        ("""        colFecha.setCellValueFactory(c -> {
            String f = c.getValue().getFecha();
            return new SimpleStringProperty(f != null ? f.replace("T", " ") : "-");
        });""", """        colFecha.setCellValueFactory(c -> new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFecha())));"""),
        ("newSel.getFecha() != null ? newSel.getFecha().replace(\"T\", \" \") : \"-\"", "DateFormatterUtil.format(newSel.getFecha())")
    ],
    "src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java": [
        ("""        colFecha.setCellValueFactory(c -> {
            String f = c.getValue().getFecha();
            return new SimpleStringProperty(f != null ? f.replace("T", " ") : "-");
        });""", """        colFecha.setCellValueFactory(c -> new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFecha())));""")
    ],
    "src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java": [
        ("""        colFecha.setCellValueFactory(c -> {
            String f = c.getValue().getFecha();
            return new SimpleStringProperty(f != null ? f.replace("T", " ") : "-");
        });""", """        colFecha.setCellValueFactory(c -> new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFecha())));""")
    ],
    "src/main/java/com/logistics/packinglist/ui/StockDialog.java": [
        ("""        colHistFecha.setCellValueFactory(c -> {
            String f = c.getValue().getFechaHora();
            return new SimpleStringProperty(f != null ? f.replace("T", " ") : "-");
        });""", """        colHistFecha.setCellValueFactory(c -> new SimpleStringProperty(DateFormatterUtil.format(c.getValue().getFechaHora())));""")
    ]
}

for filepath, replacements in files_to_patch.items():
    if not os.path.exists(filepath):
        continue
    with open(filepath, "r") as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(filepath, "w") as f:
        f.write(content)
    print(f"Patched {filepath}")

