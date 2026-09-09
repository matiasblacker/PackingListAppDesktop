package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import com.logistics.packinglist.model.SalesTargetModel;
import com.logistics.packinglist.service.SalesTargetStorageService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;

public class ConfigurarMetaVentasDialog {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    public interface OnTargetSavedListener {
        void onTargetSaved(SalesTargetModel target);
    }

    private final SalesTargetStorageService storageService = SalesTargetStorageService.getInstance();
    private OnTargetSavedListener onTargetSavedListener;

    public void setOnTargetSavedListener(OnTargetSavedListener listener) {
        this.onTargetSavedListener = listener;
    }

    public void show(Window ownerWindow) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (ownerWindow != null) {
            stage.initOwner(ownerWindow);
        }
        stage.setTitle("Configurar Meta de Ventas Mensual");
        stage.setResizable(false);
        stage.setWidth(520);
        stage.setHeight(490);

        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #0f172a; -fx-font-family: 'Segoe UI', sans-serif;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblIcon = new Label("🎯");
        lblIcon.setStyle("-fx-font-size: 28px;");

        VBox headerText = new VBox(2);
        Label lblTitle = new Label("Definir Meta de Ventas");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Label lblSub = new Label("Establece la meta comercial mensual en USD y CLP.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        headerText.getChildren().addAll(lblTitle, lblSub);
        header.getChildren().addAll(lblIcon, headerText);

        // Date Selectors
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        ComboBox<Integer> cbYear = new ComboBox<>();
        for (int y = currentYear - 2; y <= currentYear + 5; y++) {
            cbYear.getItems().add(y);
        }
        cbYear.setValue(currentYear);
        cbYear.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbMonth = new ComboBox<>();
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        cbMonth.getItems().addAll(meses);
        cbMonth.setValue(meses[currentMonth - 1]);
        cbMonth.setMaxWidth(Double.MAX_VALUE);

        // Custom Cell Factories for White Text in ComboBoxes
        String cbStyle = "-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-border-color: #334155; -fx-border-radius: 6;";
        cbYear.setStyle(cbStyle);
        cbMonth.setStyle(cbStyle);

        cbYear.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: white; -fx-background-color: #1e293b;");
                }
            }
        });
        cbYear.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: white;");
                }
            }
        });

        cbMonth.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: #1e293b;");
                }
            }
        });
        cbMonth.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white;");
                }
            }
        });

        VBox vMonth = new VBox(5, createLabel("Mes:"), cbMonth);
        VBox vYear = new VBox(5, createLabel("Año:"), cbYear);
        HBox dateBox = new HBox(15, vMonth, vYear);
        HBox.setHgrow(vMonth, Priority.ALWAYS);
        HBox.setHgrow(vYear, Priority.ALWAYS);

        // Input Fields
        TextField txtTargetUsd = createStyledTextField();
        TextField txtExchangeRate = createStyledTextField();
        TextField txtTargetClp = createStyledTextField();

        // Bidirectional recalculation listeners
        boolean[] isUpdating = {false};

        Runnable updateFromUsd = () -> {
            if (isUpdating[0]) return;
            isUpdating[0] = true;
            try {
                String usdStr = txtTargetUsd.getText().replaceAll("[^0-9.]", "");
                String rateStr = txtExchangeRate.getText().replaceAll("[^0-9.]", "");
                if (!usdStr.isEmpty() && !rateStr.isEmpty()) {
                    double usd = Double.parseDouble(usdStr);
                    double rate = Double.parseDouble(rateStr);
                    double clp = usd * rate;
                    DecimalFormat df = new DecimalFormat("#,##0", new DecimalFormatSymbols(Locale.US));
                    txtTargetClp.setText(df.format(clp));
                }
            } catch (Exception ignored) {
            } finally {
                isUpdating[0] = false;
            }
        };

        Runnable updateFromClp = () -> {
            if (isUpdating[0]) return;
            isUpdating[0] = true;
            try {
                String clpStr = txtTargetClp.getText().replaceAll("[^0-9.]", "");
                String rateStr = txtExchangeRate.getText().replaceAll("[^0-9.]", "");
                if (!clpStr.isEmpty() && !rateStr.isEmpty()) {
                    double clp = Double.parseDouble(clpStr);
                    double rate = Double.parseDouble(rateStr);
                    if (rate > 0) {
                        double usd = clp / rate;
                        DecimalFormat df = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.US));
                        txtTargetUsd.setText(df.format(usd));
                    }
                }
            } catch (Exception ignored) {
            } finally {
                isUpdating[0] = false;
            }
        };

        // Load existing values for current month/year
        Runnable cargarValoresMeta = () -> {
            isUpdating[0] = true;
            try {
                int selectedYear = cbYear.getValue() != null ? cbYear.getValue() : currentYear;
                int selectedMonth = cbMonth.getSelectionModel().getSelectedIndex() + 1;
                SalesTargetModel existing = storageService.getTarget(selectedYear, selectedMonth);

                DecimalFormat df = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.US));
                txtTargetUsd.setText(df.format(existing.getTargetUsd()));
                txtExchangeRate.setText(df.format(existing.getExchangeRate()));
                txtTargetClp.setText(df.format(existing.getTargetClp()));
            } finally {
                isUpdating[0] = false;
            }
        };

        cargarValoresMeta.run();

        txtTargetUsd.textProperty().addListener((obs, oldV, newV) -> {
            if (txtTargetUsd.isFocused()) {
                updateFromUsd.run();
            }
        });

        txtTargetClp.textProperty().addListener((obs, oldV, newV) -> {
            if (txtTargetClp.isFocused()) {
                updateFromClp.run();
            }
        });

        txtExchangeRate.textProperty().addListener((obs, oldV, newV) -> {
            if (txtExchangeRate.isFocused()) {
                updateFromUsd.run();
            }
        });

        cbYear.setOnAction(e -> cargarValoresMeta.run());
        cbMonth.setOnAction(e -> cargarValoresMeta.run());

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(12);

        formGrid.add(createLabel("Meta en USD ($):"), 0, 0);
        formGrid.add(txtTargetUsd, 1, 0);

        formGrid.add(createLabel("Tipo Cambio CLP/USD:"), 0, 1);
        formGrid.add(txtExchangeRate, 1, 1);

        formGrid.add(createLabel("Meta en CLP ($):"), 0, 2);
        formGrid.add(txtTargetClp, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(42);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(58);
        formGrid.getColumnConstraints().addAll(col1, col2);

        // Buttons
        Button btnSave = new Button("💾 Guardar Meta");
        btnSave.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSave.setMaxWidth(Double.MAX_VALUE);

        Button btnCancel = new Button("", new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.TIMES));
        btnCancel.getStyleClass().add("btn-cancelar");
        btnCancel.setStyle("-fx-background-color: #334155; -fx-text-fill: #cbd5e1; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnCancel.setMaxWidth(Double.MAX_VALUE);

        btnCancel.setOnAction(e -> stage.close());

        btnSave.setOnAction(e -> {
            try {
                int selectedYear = cbYear.getValue();
                int selectedMonth = cbMonth.getSelectionModel().getSelectedIndex() + 1;
                String monthName = cbMonth.getValue();

                double usd = Double.parseDouble(txtTargetUsd.getText().replaceAll("[^0-9.]", ""));
                double rate = Double.parseDouble(txtExchangeRate.getText().replaceAll("[^0-9.]", ""));
                double clp = Double.parseDouble(txtTargetClp.getText().replaceAll("[^0-9.]", ""));

                String monthKey = storageService.generateKey(selectedYear, selectedMonth);
                SalesTargetModel target = SalesTargetModel.builder()
                        .monthKey(monthKey)
                        .year(selectedYear)
                        .month(selectedMonth)
                        .monthName(monthName)
                        .targetUsd(usd)
                        .targetClp(clp)
                        .exchangeRate(rate)
                        .build();

                storageService.saveTarget(target);

                if (onTargetSavedListener != null) {
                    onTargetSavedListener.onTargetSaved(target);
                }

                stage.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor verifique los montos e intente nuevamente: " + ex.getMessage(), ButtonType.OK);
                alert.showAndWait();
            }
        });

        HBox btnBox = new HBox(12, btnCancel, btnSave);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(btnCancel, Priority.ALWAYS);
        HBox.setHgrow(btnSave, Priority.ALWAYS);

        root.getChildren().addAll(header, new Separator(), dateBox, formGrid, new Separator(), btnBox);

        Scene scene = new Scene(root, 520, 490);
        stage.setScene(scene);
        ScreenUtil.centerOnOwner(stage, ownerWindow);
        stage.show();
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px; -fx-font-weight: bold;");
        return label;
    }

    private TextField createStyledTextField() {
        TextField tf = new TextField();
        tf.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #f8fafc; -fx-border-color: #334155; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 13px; -fx-padding: 8;");
        return tf;
    }
}
