import re

with open("src/main/java/com/logistics/packinglist/ui/BackOrdersDialog.java", "r") as f:
    content = f.read()

# 1. Modify txtSearch
old_txt_search = """        txtSearch = new TextField();
        txtSearch.setPromptText("Buscar por Folio, Cliente o Fecha...");
        txtSearch.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");"""
new_txt_search = """        txtSearch = new TextField();
        txtSearch.setPromptText("Buscar por Folio, Cliente o Fecha...");
        txtSearch.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1;");
        txtSearch.setMaxWidth(180);"""
content = content.replace(old_txt_search, new_txt_search)

# 2. Change GridPane to SplitPane
old_grid = """        // Contenedor principal 50/50
        GridPane gridMain = new GridPane();
        gridMain.setHgap(15);
        gridMain.setVgap(10);
        VBox.setVgrow(gridMain, Priority.ALWAYS);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridMain.getColumnConstraints().addAll(col1, col2);"""
        
new_split = """        // Contenedor principal 50/50
        SplitPane splitMain = new SplitPane();
        VBox.setVgrow(splitMain, Priority.ALWAYS);
        splitMain.setStyle("-fx-background-color: transparent;");"""
content = content.replace(old_grid, new_split)

# 3. Add to SplitPane instead of gridMain
content = content.replace("gridMain.add(leftBox, 0, 0);", "")

# 4. Right Box additions
old_right_add = """        rightBox.getChildren().addAll(lblDetailTitle, infoGrid, tablaDetalles, lblHistTitle, tablaHistorial);
        gridMain.add(rightBox, 1, 0);

        root.setCenter(gridMain);"""
new_right_add = """        rightBox.getChildren().addAll(lblDetailTitle, infoGrid, tablaDetalles, lblHistTitle, tablaHistorial);
        
        ScrollPane scrollRight = new ScrollPane(rightBox);
        scrollRight.setFitToWidth(true);
        scrollRight.setStyle("-fx-background-color: transparent; -fx-background: #f8fafc;");
        
        splitMain.getItems().addAll(leftBox, scrollRight);
        splitMain.setDividerPositions(0.45);

        root.setCenter(splitMain);"""
content = content.replace(old_right_add, new_right_add)

# 5. Fix heights for tables in scrollpane
content = content.replace("tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);", "tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);\n        tablaDetalles.setMinHeight(250);")
content = content.replace("tablaHistorial.setPrefHeight(150);", "tablaHistorial.setPrefHeight(250);\n        tablaHistorial.setMinHeight(250);")

# 6. Buttons
old_btn_anular = """        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
        iconClose.setFill(Color.WHITE);
        iconClose.setSize("16");
        btnAnularBO = new Button(null, iconClose);"""
new_btn_anular = """        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
        iconClose.setFill(Color.WHITE);
        iconClose.setSize("16");
        btnAnularBO = new Button("Anular", iconClose);"""
content = content.replace(old_btn_anular, new_btn_anular)

old_btn_procesar = """        btnProcesarBO = new Button("Procesar BO ➔ Crear NP", new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE));"""
new_btn_procesar = """        FontAwesomeIconView iconProc = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
        iconProc.setFill(Color.WHITE);
        btnProcesarBO = new Button("Procesar BO ➔ Crear NP", iconProc);"""
content = content.replace(old_btn_procesar, new_btn_procesar)

with open("src/main/java/com/logistics/packinglist/ui/BackOrdersDialog.java", "w") as f:
    f.write(content)
print("Updated structural layout for BackOrdersDialog")
