with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "r") as f:
    content = f.read()

# 1. Update crearCardKpi
old_kpi = """    private VBox crearCardKpi(String titulo, FontAwesomeIcon icon, String colorHex, Label lblValor, Label lblSubtext) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(16));
        card.setMinHeight(115);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("24px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox topHeader = new HBox(10, iconView, lblTitle);
        topHeader.setAlignment(Pos.CENTER_LEFT);

        lblValor.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblSubtext.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255, 255, 255, 0.9);");

        card.getChildren().addAll(topHeader, lblValor, lblSubtext);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 0.92;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.22), 8, 0, 0, 4);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 1.0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        ));

        return card;
    }"""

new_kpi = """    private VBox crearCardKpi(String titulo, FontAwesomeIcon icon, String colorHex, Label lblValor, Label lblSubtext) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setMinHeight(62);
        card.setMaxHeight(62);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("16px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox topHeader = new HBox(6, iconView, lblTitle);
        topHeader.setAlignment(Pos.CENTER_LEFT);

        lblValor.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblSubtext.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95);");

        card.getChildren().addAll(topHeader, lblValor, lblSubtext);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-opacity: 0.92;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 6, 0, 0, 3);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-opacity: 1.0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        ));

        return card;
    }"""

content = content.replace(old_kpi, new_kpi)

# 2. Update crearCardAccesoRapido
old_acc = """    private VBox crearCardAccesoRapido(String titulo, String subtext, FontAwesomeIcon icon, String colorHex,
            Runnable action) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18, 14, 18, 14));
        card.setMinHeight(125);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("32px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");

        Label lblSub = new Label(subtext);
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255, 255, 255, 0.9); -fx-alignment: center;");
        lblSub.setWrapText(true);

        card.getChildren().addAll(iconView, lblTitle, lblSub);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 0.92;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.22), 8, 0, 0, 4);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 1.0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        ));
        card.setOnMouseClicked(e -> action.run());

        return card;
    }"""

new_acc = """    private VBox crearCardAccesoRapido(String titulo, String subtext, FontAwesomeIcon icon, String colorHex,
            Runnable action) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(8, 10, 8, 10));
        card.setMinHeight(65);
        card.setMaxHeight(65);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("20px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");

        Label lblSub = new Label(subtext);
        lblSub.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95); -fx-alignment: center;");
        lblSub.setWrapText(true);

        card.getChildren().addAll(iconView, lblTitle, lblSub);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-opacity: 0.92;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 6, 0, 0, 3);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-opacity: 1.0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        ));
        card.setOnMouseClicked(e -> action.run());

        return card;
    }"""

content = content.replace(old_acc, new_acc)

# 3. Update KPI calls (filled icons + soft salmon #FF8A65 + bold text styles)
old_kpi_calls = """        lblKpiStock = new Label("Calculando...");
        lblKpiStock.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblKpiStockSub = new Label("Cargando inventario...");
        lblKpiStockSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255, 255, 255, 0.9);");
        VBox cardKpiStock = crearCardKpi("Stock General", FontAwesomeIcon.DATABASE, "cornflowerblue", lblKpiStock,
                lblKpiStockSub);

        lblKpiNotas = new Label("Calculando...");
        lblKpiNotas.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblKpiNotasSub = new Label("Cargando notas...");
        lblKpiNotasSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255, 255, 255, 0.9);");
        VBox cardKpiNotas = crearCardKpi("Notas de Pedido", FontAwesomeIcon.FILE_TEXT_ALT, "#FF7043", lblKpiNotas,
                lblKpiNotasSub);

        lblKpiRecepciones = new Label("Calculando...");
        lblKpiRecepciones.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblKpiRecepcionesSub = new Label("Cargando recepciones...");
        lblKpiRecepcionesSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255, 255, 255, 0.9);");
        VBox cardKpiRecepciones = crearCardKpi("Recepciones", FontAwesomeIcon.DOWNLOAD, "cornflowerblue", lblKpiRecepciones,
                lblKpiRecepcionesSub);

        lblKpiPackings = new Label("Calculando...");
        lblKpiPackings.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblKpiPackingsSub = new Label("Cargando packings...");
        lblKpiPackingsSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255, 255, 255, 0.9);");
        VBox cardKpiPackings = crearCardKpi("Packing Lists", FontAwesomeIcon.LIST, "#FF7043", lblKpiPackings,
                lblKpiPackingsSub);"""

new_kpi_calls = """        lblKpiStock = new Label("Calculando...");
        lblKpiStock.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblKpiStockSub = new Label("Cargando inventario...");
        lblKpiStockSub.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95);");
        VBox cardKpiStock = crearCardKpi("Stock General", FontAwesomeIcon.DATABASE, "cornflowerblue", lblKpiStock,
                lblKpiStockSub);

        lblKpiNotas = new Label("Calculando...");
        lblKpiNotas.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblKpiNotasSub = new Label("Cargando notas...");
        lblKpiNotasSub.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95);");
        VBox cardKpiNotas = crearCardKpi("Notas de Pedido", FontAwesomeIcon.FILE_TEXT, "#FF8A65", lblKpiNotas,
                lblKpiNotasSub);

        lblKpiRecepciones = new Label("Calculando...");
        lblKpiRecepciones.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblKpiRecepcionesSub = new Label("Cargando recepciones...");
        lblKpiRecepcionesSub.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95);");
        VBox cardKpiRecepciones = crearCardKpi("Recepciones", FontAwesomeIcon.INBOX, "cornflowerblue", lblKpiRecepciones,
                lblKpiRecepcionesSub);

        lblKpiPackings = new Label("Calculando...");
        lblKpiPackings.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblKpiPackingsSub = new Label("Cargando packings...");
        lblKpiPackingsSub.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95);");
        VBox cardKpiPackings = crearCardKpi("Packing Lists", FontAwesomeIcon.LIST_ALT, "#FF8A65", lblKpiPackings,
                lblKpiPackingsSub);"""

content = content.replace(old_kpi_calls, new_kpi_calls)

# 4. Update Quick Access calls (filled icons + soft salmon #FF8A65)
old_acc_calls = """        VBox cardAcc1 = crearCardAccesoRapido("Crear Packing Manual", "Crear documento packing list manualmente",
                FontAwesomeIcon.PLUS, "#FF7043", () -> {
                    ManualPackingDialog dialog = new ManualPackingDialog(getScene().getWindow(), inventarioService);
                    dialog.showAndWait();
                    if (dialog.getResultado() != null) {
                        guardarYMostrar(dialog.getResultado());
                    }
                });
        VBox cardAcc2 = crearCardAccesoRapido("Recepciones de Carga", "Ingresar y verificar cargas entrantes",
                FontAwesomeIcon.DOWNLOAD, "cornflowerblue", () -> {
                    if (validarContextoAdminSis()) {
                        abrirTab("Recepciones de Carga", () -> new RecepcionesDialog(getScene().getWindow()));
                    }
                });
        VBox cardAcc3 = crearCardAccesoRapido("Inventario General", "Consultar stock y movimientos",
                FontAwesomeIcon.DATABASE, "#FF7043", () -> {
                    if (validarContextoAdminSis()) {
                        abrirTab("Inventario General", () -> new StockDialog(getScene().getWindow()));
                    }
                });
        VBox cardAcc4 = crearCardAccesoRapido("Notas de Pedido", "Gestionar ordenes y despachos",
                FontAwesomeIcon.FILE_TEXT_ALT, "cornflowerblue", () -> {
                    if (validarContextoAdminSis()) {
                        abrirTab("Notas de Pedido", () -> new NotasPedidoDialog(getScene().getWindow()));
                    }
                });"""

new_acc_calls = """        VBox cardAcc1 = crearCardAccesoRapido("Crear Packing Manual", "Crear documento packing list manualmente",
                FontAwesomeIcon.PLUS_SQUARE, "#FF8A65", () -> {
                    ManualPackingDialog dialog = new ManualPackingDialog(getScene().getWindow(), inventarioService);
                    dialog.showAndWait();
                    if (dialog.getResultado() != null) {
                        guardarYMostrar(dialog.getResultado());
                    }
                });
        VBox cardAcc2 = crearCardAccesoRapido("Recepciones de Carga", "Ingresar y verificar cargas entrantes",
                FontAwesomeIcon.INBOX, "cornflowerblue", () -> {
                    if (validarContextoAdminSis()) {
                        abrirTab("Recepciones de Carga", () -> new RecepcionesDialog(getScene().getWindow()));
                    }
                });
        VBox cardAcc3 = crearCardAccesoRapido("Inventario General", "Consultar stock y movimientos",
                FontAwesomeIcon.DATABASE, "#FF8A65", () -> {
                    if (validarContextoAdminSis()) {
                        abrirTab("Inventario General", () -> new StockDialog(getScene().getWindow()));
                    }
                });
        VBox cardAcc4 = crearCardAccesoRapido("Notas de Pedido", "Gestionar ordenes y despachos",
                FontAwesomeIcon.FILE_TEXT, "cornflowerblue", () -> {
                    if (validarContextoAdminSis()) {
                        abrirTab("Notas de Pedido", () -> new NotasPedidoDialog(getScene().getWindow()));
                    }
                });"""

content = content.replace(old_acc_calls, new_acc_calls)

with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "w") as f:
    f.write(content)

print("Updated compact tiles with filled icons and soft salmon successfully!")
