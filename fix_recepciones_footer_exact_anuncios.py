with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

old_footer_block = """        ScrollPane rightScroll = new ScrollPane(rightPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(rightScroll, Priority.ALWAYS);

        // Botones de acción inferior FIJOS en el pie de página
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(8, 14, 8, 14));
        actionButtons.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        FontAwesomeIconView iconPdf = new FontAwesomeIconView(FontAwesomeIcon.FILE_PDF_ALT);
        iconPdf.setFill(Color.WHITE);
        iconPdf.setSize("16");
        Button btnPdf = new Button(null, iconPdf);
        btnPdf.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnPdf.setOnAction(e -> descargarPDF());
        Tooltip.install(btnPdf, new Tooltip("Exportar Documento a PDF"));

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.TRUCK);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnGuardarRecepcion = new Button(null, iconSave);
        btnGuardarRecepcion.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnGuardarRecepcion.setOnAction(e -> procesarRecepcion());
        Tooltip.install(btnGuardarRecepcion, new Tooltip("Procesar Recepción Física"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        btnLimpiarRecepcion = new Button(null, iconRefresh);
        btnLimpiarRecepcion.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiarRecepcion.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnLimpiarRecepcion, new Tooltip("Limpiar Formulario"));

        actionButtons.getChildren().addAll(btnPdf, btnGuardarRecepcion, btnLimpiarRecepcion);

        VBox rightSideContainer = new VBox(8, rightScroll, actionButtons);
        VBox.setVgrow(rightScroll, Priority.ALWAYS);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightSideContainer);
        mainSplit.setDividerPositions(0.45);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit);"""

new_footer_block = """        // Botones de acción inferior idénticos a Anuncios
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(15, 0, 0, 0));

        Button btnPdf = UiComponentFactory.createPdfButton(this::descargarPDF);

        FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.TRUCK);
        iconSave.setFill(Color.WHITE);
        iconSave.setSize("16");
        btnGuardarRecepcion = new Button(null, iconSave);
        btnGuardarRecepcion.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnGuardarRecepcion.setOnAction(e -> procesarRecepcion());
        Tooltip.install(btnGuardarRecepcion, new Tooltip("Procesar Recepción Física"));

        FontAwesomeIconView iconRefresh = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefresh.setFill(Color.WHITE);
        iconRefresh.setSize("16");
        btnLimpiarRecepcion = new Button(null, iconRefresh);
        btnLimpiarRecepcion.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnLimpiarRecepcion.setOnAction(e -> limpiarFormulario());
        Tooltip.install(btnLimpiarRecepcion, new Tooltip("Limpiar Formulario"));

        actionButtons.getChildren().addAll(btnPdf, btnGuardarRecepcion, btnLimpiarRecepcion);

        ScrollPane rightScroll = new ScrollPane(rightPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightScroll);
        mainSplit.setDividerPositions(0.5);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit, actionButtons);"""

if old_footer_block in content:
    content = content.replace(old_footer_block, new_footer_block)
    with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
        f.write(content)
    print("Replaced footer block successfully in RecepcionesDialog.java!")
else:
    print("old_footer_block not matched in RecepcionesDialog.java!")
