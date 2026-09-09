import re

with open("src/main/java/com/logistics/packinglist/ui/BackOrdersDialog.java", "r") as f:
    content = f.read()

# 1. Bind table heights so they don't show empty rows
old_det_height = """        tablaDetalles.setMinHeight(250);
        tablaDetalles.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");"""
new_det_height = """        tablaDetalles.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        tablaDetalles.setFixedCellSize(28);
        tablaDetalles.prefHeightProperty().bind(javafx.beans.binding.Bindings.size(tablaDetalles.getItems()).multiply(tablaDetalles.getFixedCellSize()).add(30));
        tablaDetalles.minHeightProperty().bind(tablaDetalles.prefHeightProperty());
        tablaDetalles.maxHeightProperty().bind(tablaDetalles.prefHeightProperty());"""
content = content.replace(old_det_height, new_det_height)

# Remove VBox.setVgrow(tablaDetalles, Priority.ALWAYS); if it exists
content = content.replace("VBox.setVgrow(tablaDetalles, Priority.ALWAYS);", "")

old_hist_height = """        tablaHistorial.setPrefHeight(250);
        tablaHistorial.setMinHeight(250);"""
new_hist_height = """        tablaHistorial.setFixedCellSize(28);
        tablaHistorial.prefHeightProperty().bind(javafx.beans.binding.Bindings.size(tablaHistorial.getItems()).multiply(tablaHistorial.getFixedCellSize()).add(30));
        tablaHistorial.minHeightProperty().bind(tablaHistorial.prefHeightProperty());
        tablaHistorial.maxHeightProperty().bind(tablaHistorial.prefHeightProperty());"""
content = content.replace(old_hist_height, new_hist_height)

# 2. Extract quantities from JSON in Cantidades Procesadas
old_hist_det = """        colHistDet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDetallesProcesados()));"""
new_hist_det = """        colHistDet.setCellValueFactory(c -> {
            String jsonRaw = c.getValue().getDetallesProcesados();
            if (jsonRaw == null || jsonRaw.isEmpty()) return new SimpleStringProperty("-");
            try {
                int total = 0;
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\":\\\\s*(\\\\d+)").matcher(jsonRaw);
                while(m.find()) {
                    total += Integer.parseInt(m.group(1));
                }
                return new SimpleStringProperty(String.valueOf(total));
            } catch(Exception e) {
                return new SimpleStringProperty(jsonRaw);
            }
        });"""
content = content.replace(old_hist_det, new_hist_det)

# 3. Move actionRow out of rightBox and into root.setBottom(bottomBar)
# Find the actionRow creation
old_action_row = """        // Barra inferior de acciones (como actionRow en NotasPedidoDialog)
        HBox actionRow = new HBox(10);
        actionRow.setPadding(new Insets(15, 0, 0, 0));
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.CLOSE);
        iconClose.setFill(Color.WHITE);
        iconClose.setSize("16");
        btnAnularBO = new Button(null, iconClose);
        btnAnularBO.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnAnularBO.setOnAction(e -> confirmarAnulacionBO());
        Tooltip.install(btnAnularBO, new Tooltip("Anular Back Order"));

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnProcesarBO = new Button(null, iconSave);
        btnProcesarBO.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnProcesarBO.setOnAction(e -> abrirDialogoProcesarBO());
        Tooltip.install(btnProcesarBO, new Tooltip("Procesar BO ➔ Crear NP"));

        actionRow.getChildren().addAll(btnProcesarBO, btnAnularBO);

        rightBox.getChildren().addAll(lblDetailTitle, infoGrid, detailBox, lblHistTitle, histBox, actionRow);
        
        ScrollPane scrollRight = new ScrollPane(rightBox);
        scrollRight.setFitToWidth(true);
        scrollRight.setStyle("-fx-background-color: transparent; -fx-background: #f8fafc;");
        
        splitMain.getItems().addAll(leftBox, scrollRight);
        splitMain.setDividerPositions(0.45);

        root.setCenter(splitMain);"""

new_bottom_bar = """        rightBox.getChildren().addAll(lblDetailTitle, infoGrid, detailBox, lblHistTitle, histBox);
        
        ScrollPane scrollRight = new ScrollPane(rightBox);
        scrollRight.setFitToWidth(true);
        scrollRight.setStyle("-fx-background-color: transparent; -fx-background: #f8fafc;");
        
        splitMain.getItems().addAll(leftBox, scrollRight);
        splitMain.setDividerPositions(0.45);

        root.setCenter(splitMain);
        
        // Barra inferior de acciones (como el dise\\u00f1o original)
        HBox bottomBar = new HBox(12);
        bottomBar.setPadding(new Insets(15, 0, 0, 0));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);

        FontAwesomeIconView iconClose = new FontAwesomeIconView(FontAwesomeIcon.CLOSE);
        iconClose.setFill(Color.WHITE);
        iconClose.setSize("16");
        btnAnularBO = new Button(null, iconClose);
        btnAnularBO.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnAnularBO.setOnAction(e -> confirmarAnulacionBO());
        Tooltip.install(btnAnularBO, new Tooltip("Anular Back Order"));

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnProcesarBO = new Button(null, iconSave);
        btnProcesarBO.setStyle("-fx-background-color: #0F3E6E; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnProcesarBO.setOnAction(e -> abrirDialogoProcesarBO());
        Tooltip.install(btnProcesarBO, new Tooltip("Procesar BO \\u2794 Crear NP"));

        bottomBar.getChildren().addAll(btnProcesarBO, btnAnularBO);
        root.setBottom(bottomBar);"""

content = content.replace(old_action_row, new_bottom_bar)

with open("src/main/java/com/logistics/packinglist/ui/BackOrdersDialog.java", "w") as f:
    f.write(content)
print("Updated BackOrdersDialog")
