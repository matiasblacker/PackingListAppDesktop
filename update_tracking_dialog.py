with open("src/main/java/com/logistics/packinglist/ui/TrackingDialog.java", "r") as f:
    content = f.read()

# 1. Update SplitPane orientation and 50/50 divider
old_split = """        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightScroll);
        mainSplit.setDividerPositions(0.6);"""

new_split = """        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightScroll);
        mainSplit.setDividerPositions(0.5);"""

content = content.replace(old_split, new_split)

# 2. Update Header font sizes
content = content.replace(
    'lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");',
    'lblTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");'
)
content = content.replace(
    'lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");',
    'lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");'
)
content = content.replace(
    'lblLeftTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");',
    'lblLeftTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");'
)
content = content.replace(
    'lblRightTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");',
    'lblRightTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");'
)

# 3. Update search field font size
content = content.replace(
    'txtSearch.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtSearch.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 11px;");'
)

# 4. Update info grid and form text font sizes to 11px
content = content.replace(
    'lblFolio.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");',
    'lblFolio.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");'
)
content = content.replace(
    'lblTrackingNum.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b6cb0;");',
    'lblTrackingNum.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2b6cb0;");'
)
content = content.replace(
    'lblCliente.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");',
    'lblCliente.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");'
)
content = content.replace(
    'lblEstadoNP.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");',
    'lblEstadoNP.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");'
)
content = content.replace(
    'lblEstadoTracking.setStyle("-fx-font-weight: bold; -fx-text-fill: #2f855a;");',
    'lblEstadoTracking.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2f855a;");'
)

content = content.replace(
    'lblFNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");',
    'lblFNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");'
)
content = content.replace(
    'lblTRK.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");',
    'lblTRK.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");'
)
content = content.replace(
    'lblCLI.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");',
    'lblCLI.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");'
)
content = content.replace(
    'lblENP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");',
    'lblENP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");'
)
content = content.replace(
    'lblETRK.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");',
    'lblETRK.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");'
)
content = content.replace(
    'lblURL.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");',
    'lblURL.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");'
)
content = content.replace(
    'txtLinkPublico.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e40af; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");',
    'txtLinkPublico.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e40af; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-font-size: 11px;");'
)

# 5. Update form inputs font sizes
content = content.replace(
    'cbEstado.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");',
    'cbEstado.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-font-size: 11px;");'
)
content = content.replace(
    'cbCourier.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");',
    'cbCourier.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-font-size: 11px;");'
)
content = content.replace(
    'txtNumeroCourier.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtNumeroCourier.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 11px;");'
)
content = content.replace(
    'txtRetiradoNombre.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtRetiradoNombre.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 11px;");'
)
content = content.replace(
    'txtRetiradoRut.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtRetiradoRut.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 11px;");'
)
content = content.replace(
    'txtRetiradoPatente.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtRetiradoPatente.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 11px;");'
)
content = content.replace(
    'txtComentario.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtComentario.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 11px;");'
)

# 6. Update ALL buttons to Icon-Only with White Icons and Tooltips
old_left_btns = """        Button btnConsolidar = new Button("Consolidar Trackings", new FontAwesomeIconView(FontAwesomeIcon.COMPRESS));
        btnConsolidar.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnConsolidar.setOnAction(e -> consolidarSeleccionados());

        Button btnRefrescar = new Button("Refrescar", new FontAwesomeIconView(FontAwesomeIcon.REFRESH));
        btnRefrescar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand;");
        btnRefrescar.setOnAction(e -> cargarDatos());"""

new_left_btns = """        FontAwesomeIconView iconConsolidar = new FontAwesomeIconView(FontAwesomeIcon.COMPRESS);
        iconConsolidar.setFill(Color.WHITE);
        Button btnConsolidar = new Button("", iconConsolidar);
        btnConsolidar.setStyle("-fx-background-color: #6f42c1; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnConsolidar.setOnAction(e -> consolidarSeleccionados());
        Tooltip.install(btnConsolidar, new Tooltip("Consolidar Trackings"));

        FontAwesomeIconView iconRefrescar = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        iconRefrescar.setFill(Color.WHITE);
        Button btnRefrescar = new Button("", iconRefrescar);
        btnRefrescar.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnRefrescar.setOnAction(e -> cargarDatos());
        Tooltip.install(btnRefrescar, new Tooltip("Refrescar Lista"));"""

content = content.replace(old_left_btns, new_left_btns)

old_copy_btn = """        Button btnCopyLink = new Button("Copiar URL", new FontAwesomeIconView(FontAwesomeIcon.COPY));
        btnCopyLink.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");"""

new_copy_btn = """        FontAwesomeIconView iconCopy = new FontAwesomeIconView(FontAwesomeIcon.COPY);
        iconCopy.setFill(Color.WHITE);
        Button btnCopyLink = new Button("", iconCopy);
        btnCopyLink.setStyle("-fx-background-color: #3182ce; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        Tooltip.install(btnCopyLink, new Tooltip("Copiar URL Pública"));"""

content = content.replace(old_copy_btn, new_copy_btn)

old_form_btns = """        Button btnRegistrarHito = new Button("Registrar Hito de Tracking", new FontAwesomeIconView(FontAwesomeIcon.SAVE));
        btnRegistrarHito.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 8 16;");
        btnRegistrarHito.setOnAction(e -> registrarHito());

        // Tema 2: Botón Anular Tracking
        btnAnularTracking = new Button("Anular Tracking", new FontAwesomeIconView(FontAwesomeIcon.BAN));
        btnAnularTracking.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16;");
        btnAnularTracking.setOnAction(e -> confirmarAnulacionTracking());
        btnAnularTracking.setDisable(true);"""

new_form_btns = """        FontAwesomeIconView iconSaveHito = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        iconSaveHito.setFill(Color.WHITE);
        Button btnRegistrarHito = new Button("", iconSaveHito);
        btnRegistrarHito.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnRegistrarHito.setOnAction(e -> registrarHito());
        Tooltip.install(btnRegistrarHito, new Tooltip("Registrar Hito de Tracking"));

        // Tema 2: Botón Anular Tracking
        FontAwesomeIconView iconBan = new FontAwesomeIconView(FontAwesomeIcon.BAN);
        iconBan.setFill(Color.WHITE);
        btnAnularTracking = new Button("", iconBan);
        btnAnularTracking.setStyle("-fx-background-color: #dc2626; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");
        btnAnularTracking.setOnAction(e -> confirmarAnulacionTracking());
        btnAnularTracking.setDisable(true);
        Tooltip.install(btnAnularTracking, new Tooltip("Anular Tracking"));"""

content = content.replace(old_form_btns, new_form_btns)

# 7. Import Color if not imported
if "import javafx.scene.paint.Color;" not in content:
    content = content.replace("import javafx.scene.Scene;", "import javafx.scene.Scene;\nimport javafx.scene.paint.Color;")

with open("src/main/java/com/logistics/packinglist/ui/TrackingDialog.java", "w") as f:
    f.write(content)

print("Updated TrackingDialog.java to 50/50 split, smaller legible text, and white icon-only buttons!")
