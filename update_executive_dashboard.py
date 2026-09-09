with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "r") as f:
    content = f.read()

# 1. Update crearCardKpi
old_kpi = """    private VBox crearCardKpi(String titulo, FontAwesomeIcon icon, String colorHex, Label lblValor, Label lblSubtext) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 12, 16, 12));
        card.setMinHeight(130);
        card.setPrefHeight(130);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("28px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");

        lblValor.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        lblSubtext.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95); -fx-alignment: center;");
        lblSubtext.setWrapText(true);

        card.getChildren().addAll(iconView, lblTitle, lblValor, lblSubtext);

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
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setMinHeight(125);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 3px 0px 0px 0px;" +
                "-fx-border-color: " + colorHex + " transparent transparent transparent;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("18px");
        iconView.setFill(Color.WHITE);

        StackPane iconBg = new StackPane(iconView);
        iconBg.setPrefSize(36, 36);
        iconBg.setMaxSize(36, 36);
        iconBg.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8px;");

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        HBox topRow = new HBox(10, iconBg, lblTitle);
        topRow.setAlignment(Pos.CENTER_LEFT);

        lblValor.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblSubtext.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        card.getChildren().addAll(topRow, lblValor, lblSubtext);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 3px 0px 0px 0px;" +
                "-fx-border-color: " + colorHex + " transparent transparent transparent;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 12, 0, 0, 5);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 3px 0px 0px 0px;" +
                "-fx-border-color: " + colorHex + " transparent transparent transparent;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        ));

        return card;
    }"""

content = content.replace(old_kpi, new_kpi)

# 2. Update crearCardAccesoRapido
old_acc = """    private VBox crearCardAccesoRapido(String titulo, String subtext, FontAwesomeIcon icon, String colorHex,
            Runnable action) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 12, 16, 12));
        card.setMinHeight(130);
        card.setPrefHeight(130);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("32px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");

        Label lblSub = new Label(subtext);
        lblSub.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95); -fx-alignment: center;");
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
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setMinHeight(115);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 1px;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 6, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("16px");
        iconView.setFill(Color.WHITE);

        StackPane iconBg = new StackPane(iconView);
        iconBg.setPrefSize(34, 34);
        iconBg.setMaxSize(34, 34);
        iconBg.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8px;");

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        FontAwesomeIconView iconArrow = new FontAwesomeIconView(FontAwesomeIcon.CHEVRON_RIGHT);
        iconArrow.setSize("12px");
        iconArrow.setFill(Color.web("#94a3b8"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(10, iconBg, lblTitle, spacer, iconArrow);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSub = new Label(subtext);
        lblSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        lblSub.setWrapText(true);

        card.getChildren().addAll(topRow, lblSub);

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #f8fafc;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-color: " + colorHex + ";" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);"
            );
            iconArrow.setFill(Color.web(colorHex));
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-color: #e2e8f0;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 6, 0, 0, 2);"
            );
            iconArrow.setFill(Color.web("#94a3b8"));
        });
        card.setOnMouseClicked(e -> action.run());

        return card;
    }"""

content = content.replace(old_acc, new_acc)

# 3. Update Header & KPI card calls in crearDashboardInicio
old_header_and_kpis = """        // 1. Saludo y Encabezado
        AuthService auth = AuthService.getInstance();
        Label lblSaludo = new Label("¡Hola, " + auth.getNombre() + "! 👋");
        lblSaludo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");

        Label lblSubsaludo = new Label(
                "Panel de Control Operativo WMS. Aquí tienes un resumen de la bodega en tiempo real.");
        lblSubsaludo.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");

        VBox headerBox = new VBox(4, lblSaludo, lblSubsaludo);

        // 2. Tarjetas KPI Operativos (Row of 4)
        lblKpiStock = new Label("Calculando...");
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

new_header_and_kpis = """        // 1. Saludo y Encabezado Ejecutivo (Hero Box)
        AuthService auth = AuthService.getInstance();
        Label lblSaludo = new Label("¡Hola, " + auth.getNombre() + "! 👋");
        lblSaludo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblSubsaludo = new Label(
                "Panel de Control Operativo WMS • Resumen ejecutivo en tiempo real.");
        lblSubsaludo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.88);");

        VBox titleBox = new VBox(4, lblSaludo, lblSubsaludo);

        Label lblBadge = new Label("🟢 BODEGA ACTIVA", new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE));
        lblBadge.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6px 14px; -fx-background-radius: 20px;");

        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);

        HBox headerBox = new HBox(12, titleBox, heroSpacer, lblBadge);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(20, 24, 20, 24));
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #0F3E6E, #1E508C);" +
                "-fx-background-radius: 12px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(15,62,110,0.25), 10, 0, 0, 4);"
        );

        // 2. Tarjetas KPI Operativos (Row of 4)
        lblKpiStock = new Label("Calculando...");
        lblKpiStock.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblKpiStockSub = new Label("Cargando inventario...");
        lblKpiStockSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        VBox cardKpiStock = crearCardKpi("Stock General", FontAwesomeIcon.DATABASE, "cornflowerblue", lblKpiStock,
                lblKpiStockSub);

        lblKpiNotas = new Label("Calculando...");
        lblKpiNotas.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblKpiNotasSub = new Label("Cargando notas...");
        lblKpiNotasSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        VBox cardKpiNotas = crearCardKpi("Notas de Pedido", FontAwesomeIcon.FILE_TEXT, "#FF8A65", lblKpiNotas,
                lblKpiNotasSub);

        lblKpiRecepciones = new Label("Calculando...");
        lblKpiRecepciones.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblKpiRecepcionesSub = new Label("Cargando recepciones...");
        lblKpiRecepcionesSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        VBox cardKpiRecepciones = crearCardKpi("Recepciones", FontAwesomeIcon.INBOX, "#38a169", lblKpiRecepciones,
                lblKpiRecepcionesSub);

        lblKpiPackings = new Label("Calculando...");
        lblKpiPackings.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblKpiPackingsSub = new Label("Cargando packings...");
        lblKpiPackingsSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        VBox cardKpiPackings = crearCardKpi("Packing Lists", FontAwesomeIcon.LIST_ALT, "#805ad5", lblKpiPackings,
                lblKpiPackingsSub);"""

content = content.replace(old_header_and_kpis, new_header_and_kpis)

with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "w") as f:
    f.write(content)

print("Updated MainController.java with Executive Modern Dashboard design successfully!")
