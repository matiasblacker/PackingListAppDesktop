package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.OrderNoteModel;
import com.logistics.packinglist.service.MantenimientoService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;

public class GestionTrackingDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

        private final OrderNoteModel note;

    private TextField txtTrackingNumber;
    private ComboBox<String> cbEstado;
    private TextField txtUbicacion;
    private ComboBox<String> cbCourier;
    private TextField txtNumCourier;
    private TextArea txtDescripcion;
    private TextArea txtFotosUrls;
    private TextField txtLatitud;
    private TextField txtLongitud;
    private Label lblPublicUrl;

    public GestionTrackingDialog(Window owner, MantenimientoService service, OrderNoteModel note) {
                this.note = note;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Gestión de Tracking de Pedido - " + note.getFolio());

        setMinWidth(520);
        setMinHeight(500);

        construirUI();
    }

    private void construirUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header Title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.MAP_MARKER);
        icon.setSize("24px");
        icon.setFill(Color.web("#0F3E6E"));

        Label lblTitle = new Label("Tracking y Eventos de Envío (" + note.getFolio() + ")");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        header.getChildren().addAll(icon, lblTitle);

        // Tracking Info Card & Public Link
        VBox trackingCard = new VBox(8);
        trackingCard.setPadding(new Insets(12));
        trackingCard.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label lblTrackingTitle = new Label("Código de Tracking Asignado:");
        lblTrackingTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568; -fx-font-size: 12px;");

        HBox trackingBox = new HBox(10);
        trackingBox.setAlignment(Pos.CENTER_LEFT);

        txtTrackingNumber = new TextField(note.getTrackingNumber() != null ? note.getTrackingNumber() : "Generando...");
        txtTrackingNumber.setEditable(false);
        txtTrackingNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #007bff; -fx-background-color: #f1f5f9;");
        HBox.setHgrow(txtTrackingNumber, Priority.ALWAYS);

        Button btnCopyLink = new Button("Copiar Link Público", new FontAwesomeIconView(FontAwesomeIcon.COPY));
        btnCopyLink.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnCopyLink.setOnAction(e -> copiarLinkPublico());

        trackingBox.getChildren().addAll(txtTrackingNumber, btnCopyLink);

        String publicUrl = "http://localhost:8080/tracking?code=" + (note.getTrackingNumber() != null ? note.getTrackingNumber() : "");
        lblPublicUrl = new Label("URL Pública: " + publicUrl);
        lblPublicUrl.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");

        trackingCard.getChildren().addAll(lblTrackingTitle, trackingBox, lblPublicUrl);

        // Form para nuevo hito
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        // Estado
        Label lblEst = new Label("Nuevo Estado:");
        lblEst.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        grid.add(lblEst, 0, 0);
        cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("REGISTRADO", "EN_PREPARACION", "EN_TRANSITO", "ENTREGADO_A_CLIENTE");
        cbEstado.setValue(note.getEstadoTracking() != null ? note.getEstadoTracking() : "REGISTRADO");
        cbEstado.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbEstado.setMaxWidth(Double.MAX_VALUE);
        grid.add(cbEstado, 1, 0);

        // Ubicación
        Label lblUbi = new Label("Ubicación/Hito:");
        lblUbi.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        grid.add(lblUbi, 0, 1);
        txtUbicacion = new TextField("Bodega Central Santiago");
        txtUbicacion.setPromptText("Ej: Bodega Central, Aduana Valparaíso");
        txtUbicacion.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        grid.add(txtUbicacion, 1, 1);

        // Transportista / Courier
        Label lblCour = new Label("Courier / Transportista:");
        lblCour.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        grid.add(lblCour, 0, 2);
        cbCourier = new ComboBox<>();
        cbCourier.getItems().addAll("Starken", "Chilexpress", "DHL", "FedEx", "BlueExpress", "Retiro Cliente", "Despacho Interno");
        cbCourier.setEditable(true);
        cbCourier.setPromptText("Seleccione o escriba courier");
        cbCourier.setStyle("-fx-font-size: 10px; -fx-background-radius: 4px;");
        cbCourier.setMaxWidth(Double.MAX_VALUE);
        grid.add(cbCourier, 1, 2);

        // N° Seguimiento Courier
        Label lblNCour = new Label("N° Seguimiento Courier:");
        lblNCour.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        grid.add(lblNCour, 0, 3);
        txtNumCourier = new TextField();
        txtNumCourier.setPromptText("Número de seguimiento del transportista externo");
        txtNumCourier.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        grid.add(txtNumCourier, 1, 3);

        // Coordenadas opcionales
        Label lblCoord = new Label("Coordenadas (Lat / Lng):");
        lblCoord.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        grid.add(lblCoord, 0, 4);
        HBox coordBox = new HBox(8);
        txtLatitud = new TextField();
        txtLatitud.setPromptText("Latitud (-33.4489)");
        txtLatitud.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        txtLongitud = new TextField();
        txtLongitud.setPromptText("Longitud (-70.6693)");
        txtLongitud.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        coordBox.getChildren().addAll(txtLatitud, txtLongitud);
        grid.add(coordBox, 1, 4);

        // Comentario / Descripción
        Label lblDesc = new Label("Comentario / Observación:");
        lblDesc.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        grid.add(lblDesc, 0, 5);
        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Detalles de la preparación, despacho, inspección de aduanas o firma de recepción...");
        txtDescripcion.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        txtDescripcion.setPrefRowCount(3);
        grid.add(txtDescripcion, 1, 5);

        // URLs de Fotos de Evidencia
        Label lblFotos = new Label("Fotos Evidencia (URLs):");
        lblFotos.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 9.5px;");
        grid.add(lblFotos, 0, 6);
        txtFotosUrls = new TextArea();
        txtFotosUrls.setPromptText("URLs de fotos separadas por coma (ej. Firestore/Cloud Storage)...");
        txtFotosUrls.setStyle("-fx-font-size: 10px; -fx-padding: 2.5px 5px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1;");
        txtFotosUrls.setPrefRowCount(2);
        grid.add(txtFotosUrls, 1, 6);

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(150);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        // Action Buttons
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnGuardar = new Button("Registrar Hito de Tracking", new FontAwesomeIconView(FontAwesomeIcon.CHECK));
        btnGuardar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16;");
        btnGuardar.setOnAction(e -> registrarHito());

        Button btnCerrar = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.TIMES));
        btnCerrar.getStyleClass().add("btn-cancelar");
        btnCerrar.setOnAction(e -> close());

        actions.getChildren().addAll(btnCerrar, btnGuardar);

        root.getChildren().addAll(header, trackingCard, grid, actions);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 560, 560, 520, 500);
    }

    private void copiarLinkPublico() {
        String trackingCode = txtTrackingNumber.getText();
        String link = "http://localhost:8080/tracking?code=" + trackingCode;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(link);
        clipboard.setContent(content);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "El enlace público de tracking ha sido copiado al portapapeles:\n" + link, ButtonType.OK);
        alert.setTitle("Enlace Copiado");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void registrarHito() {
        try {
            String estado = cbEstado.getValue();
            String desc = txtDescripcion.getText().trim();
            String ubicacion = txtUbicacion.getText().trim();
            String courier = cbCourier.getValue() != null ? cbCourier.getValue().trim() : null;
            String numCourier = txtNumCourier.getText().trim();
            String fotos = txtFotosUrls.getText().trim();

            Double lat = null;
            Double lng = null;
            try {
                if (!txtLatitud.getText().trim().isEmpty()) lat = Double.parseDouble(txtLatitud.getText().trim());
                if (!txtLongitud.getText().trim().isEmpty()) lng = Double.parseDouble(txtLongitud.getText().trim());
            } catch (Exception ignored) {}

            service.registrarEventoTracking(
                    note.getId(),
                    note.getTrackingNumber(),
                    estado,
                    desc.isEmpty() ? "Actualización de estado de envío." : desc,
                    ubicacion.isEmpty() ? "Bodega Central" : ubicacion,
                    lat,
                    lng,
                    courier,
                    numCourier.isEmpty() ? null : numCourier,
                    fotos.isEmpty() ? null : fotos
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Hito de tracking registrado exitosamente.", ButtonType.OK);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.showAndWait();

            close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al registrar hito: " + ex.getMessage(), ButtonType.OK);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }
}
