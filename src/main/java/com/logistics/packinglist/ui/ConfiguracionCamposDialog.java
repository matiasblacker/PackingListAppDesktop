package com.logistics.packinglist.ui;

import com.logistics.packinglist.model.ProductFieldDefinitionModel;
import com.logistics.packinglist.service.MantenimientoService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class ConfiguracionCamposDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    private final String companyId;
    private final String targetEntity; // "ANUNCIO" or "RECEPCION"
    private final String warehouseId;
    private final String supplierId;
    private final Runnable onFieldsChanged;

    private TableView<ProductFieldDefinitionModel> tabla;
    private ObservableList<ProductFieldDefinitionModel> listCampos;

    private TextField txtLabel;
    private ComboBox<String> cbType;
    private CheckBox chkRequired;
    private CheckBox chkActive;

    private Button btnSave;
    private Button btnClear;
    private Button btnDelete;
    private FontAwesomeIconView iconSave;

    private ProductFieldDefinitionModel selectedForEdit = null;

    public ConfiguracionCamposDialog(Window owner, String companyId, String warehouseId, String supplierId, Runnable onFieldsChanged) {
        this(owner, companyId, "ANUNCIO", warehouseId, supplierId, onFieldsChanged);
    }

    public ConfiguracionCamposDialog(Window owner, String companyId, String targetEntity, String warehouseId, String supplierId, Runnable onFieldsChanged) {
        this.companyId = companyId;
        this.targetEntity = targetEntity != null ? targetEntity : "ANUNCIO";
        this.warehouseId = warehouseId;
        this.supplierId = supplierId;
        this.onFieldsChanged = onFieldsChanged;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);

        String entityName = switch (this.targetEntity.toUpperCase()) {
            case "ANUNCIO_DOC" -> "Anuncios de Carga (Documento)";
            case "ANUNCIO_PROD", "ANUNCIO" -> "Anuncios de Carga (Productos)";
            case "RECEPCION_DOC" -> "Recepciones de Carga (Documento)";
            case "RECEPCION_PROD", "RECEPCION" -> "Recepciones de Carga (Productos)";
            default -> this.targetEntity;
        };
        setTitle("Configuración de Atributos - " + entityName);

        setMinWidth(840);
        setMinHeight(600);

        construirUI(entityName);
        cargarCampos();
    }

    private void construirUI(String entityName) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.getStyleClass().add("dialog-root");
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header descriptivo
        Label lblTitulo = new Label("Atributos Personalizados - " + entityName);
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F3E6E;");
        Label lblSubtitulo = new Label("Configure, active o desactive atributos independientes específicos para " + entityName + ".");
        lblSubtitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        VBox header = new VBox(2, lblTitulo, lblSubtitulo);

        // Tabla de campos existentes
        tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductFieldDefinitionModel, String> colLabel = new TableColumn<>("Nombre del Campo");
        colLabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFieldLabel()));

        TableColumn<ProductFieldDefinitionModel, String> colType = new TableColumn<>("Tipo");
        colType.setCellValueFactory(c -> {
            String type = c.getValue().getFieldType();
            if (type == null) return new SimpleStringProperty("-");
            return new SimpleStringProperty(switch (type.toUpperCase()) {
                case "TEXT" -> "Texto";
                case "NUMBER" -> "Número";
                case "DATE" -> "Fecha";
                case "BOOLEAN" -> "Sí/No";
                default -> type;
            });
        });

        TableColumn<ProductFieldDefinitionModel, String> colReq = new TableColumn<>("Requerido");
        colReq.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isRequired() ? "Sí" : "No"));

        TableColumn<ProductFieldDefinitionModel, String> colActive = new TableColumn<>("Estado");
        colActive.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Activo" : "Inactivo"));

        tabla.getColumns().addAll(colLabel, colType, colReq, colActive);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        listCampos = FXCollections.observableArrayList();
        tabla.setItems(listCampos);

        // Listener al seleccionar fila de la tabla para editar
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                cargarFormularioEdicion(newSel);
            }
        });

        // Formulario para agregar / editar campo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 10, 0));

        txtLabel = new TextField();
        txtLabel.setPromptText("Ej: Commercial Invoice, Fecha Vencimiento");
        txtLabel.setPrefWidth(200);
        txtLabel.setStyle("-fx-background-radius: 4px; -fx-border-color: #cbd5e1;");

        cbType = new ComboBox<>();
        cbType.getItems().addAll("Texto", "Número", "Fecha", "Sí/No");
        cbType.setValue("Texto");
        cbType.setPrefWidth(110);
        cbType.setStyle("-fx-background-radius: 4px;");

        chkRequired = new CheckBox("Requerido");
        chkRequired.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        chkActive = new CheckBox("Activo");
        chkActive.setSelected(true);
        chkActive.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d6efd;");

        // Botón Añadir / Guardar
        iconSave = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        iconSave.setFill(Color.WHITE);
        btnSave = new Button(null, iconSave);
        btnSave.setStyle("-fx-background-color: #28a745; -fx-padding: 6px 14px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnSave.setTooltip(new Tooltip("Añadir / Guardar Campo"));
        btnSave.setOnAction(e -> guardarOActualizarCampo());

        // Botón Limpiar / Cancelar Selección
        FontAwesomeIconView iconClear = new FontAwesomeIconView(FontAwesomeIcon.UNDO);
        iconClear.setFill(Color.WHITE);
        btnClear = new Button(null, iconClear);
        btnClear.setStyle("-fx-background-color: #6c757d; -fx-padding: 6px 14px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnClear.setTooltip(new Tooltip("Limpiar Selección"));
        btnClear.setOnAction(e -> limpiarFormulario());

        // Botón Eliminar
        FontAwesomeIconView iconTrash = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconTrash.setFill(Color.WHITE);
        btnDelete = new Button(null, iconTrash);
        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-padding: 6px 14px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btnDelete.setTooltip(new Tooltip("Eliminar Campo Seleccionado"));
        btnDelete.setOnAction(e -> eliminarCampo());

        HBox btnGroup = new HBox(6, btnSave, btnClear, btnDelete);
        btnGroup.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Etiqueta:"), 0, 0);
        grid.add(txtLabel, 1, 0);
        grid.add(new Label("Tipo:"), 2, 0);
        grid.add(cbType, 3, 0);
        grid.add(chkRequired, 4, 0);
        grid.add(chkActive, 5, 0);
        grid.add(btnGroup, 6, 0);

        root.getChildren().addAll(header, tabla, grid);

        Scene scene = new Scene(root, 840, 600);

        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        setScene(scene);
    }

    private void cargarCampos() {
        new Thread(() -> {
            try {
                List<ProductFieldDefinitionModel> fields = service.obtenerDefinicionesCampos(companyId, targetEntity, warehouseId, null);
                javafx.application.Platform.runLater(() -> {
                    listCampos.setAll(fields);
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error al cargar", "No se pudieron obtener los campos: " + e.getMessage());
                });
            }
        }).start();
    }

    private void cargarFormularioEdicion(ProductFieldDefinitionModel field) {
        selectedForEdit = field;
        txtLabel.setText(field.getFieldLabel() != null ? field.getFieldLabel() : "");
        
        String type = field.getFieldType();
        if (type != null) {
            switch (type.toUpperCase()) {
                case "TEXT" -> cbType.setValue("Texto");
                case "NUMBER" -> cbType.setValue("Número");
                case "DATE" -> cbType.setValue("Fecha");
                case "BOOLEAN" -> cbType.setValue("Sí/No");
                default -> cbType.setValue("Texto");
            }
        }
        chkRequired.setSelected(field.isRequired());
        chkActive.setSelected(field.isActive());

        // Cambiar ícono a SAVE / CHECK al estar editando
        iconSave.setIcon(FontAwesomeIcon.SAVE);
        btnSave.setStyle("-fx-background-color: #0d6efd; -fx-padding: 6px 14px; -fx-background-radius: 4px; -fx-cursor: hand;");
    }

    private void limpiarFormulario() {
        selectedForEdit = null;
        tabla.getSelectionModel().clearSelection();
        txtLabel.clear();
        cbType.setValue("Texto");
        chkRequired.setSelected(false);
        chkActive.setSelected(true);

        // Restaurar ícono PLUS y color verde
        iconSave.setIcon(FontAwesomeIcon.PLUS);
        btnSave.setStyle("-fx-background-color: #28a745; -fx-padding: 6px 14px; -fx-background-radius: 4px; -fx-cursor: hand;");
    }

    private void guardarOActualizarCampo() {
        String label = txtLabel.getText().trim();
        if (label.isEmpty()) {
            mostrarWarning("Validación", "Debe ingresar una etiqueta para el campo.");
            return;
        }

        String typeStr = cbType.getValue();
        String fieldType = switch (typeStr) {
            case "Texto" -> "TEXT";
            case "Número" -> "NUMBER";
            case "Fecha" -> "DATE";
            case "Sí/No" -> "BOOLEAN";
            default -> "TEXT";
        };

        if (selectedForEdit != null) {
            // Actualización de campo existente
            selectedForEdit.setFieldLabel(label);
            selectedForEdit.setFieldType(fieldType);
            selectedForEdit.setRequired(chkRequired.isSelected());
            selectedForEdit.setActive(chkActive.isSelected());
            selectedForEdit.setTargetEntity(targetEntity);

            ProductFieldDefinitionModel toUpdate = selectedForEdit;
            new Thread(() -> {
                try {
                    ProductFieldDefinitionModel updated = service.actualizarDefinicionCampo(toUpdate.getId(), toUpdate);
                    javafx.application.Platform.runLater(() -> {
                        tabla.refresh();
                        limpiarFormulario();
                        mostrarInformacion("Éxito", "Campo personalizado actualizado correctamente.");
                        if (onFieldsChanged != null) {
                            onFieldsChanged.run();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error", "No se pudo actualizar el campo: " + e.getMessage());
                    });
                }
            }).start();
        } else {
            // Creación de nuevo campo
            String fieldKey = label.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "_");

            if (fieldKey.isEmpty()) {
                mostrarWarning("Validación", "La etiqueta contiene caracteres inválidos.");
                return;
            }

            java.util.Set<String> reservedKeys = java.util.Set.of(
                "sku", "nombre", "descripcion", "categoria", "packing", "precio", "moneda",
                "pais_origen", "paisorigen", "precio_lista", "preciolista",
                "unidad_medida", "unidadmedida", "barcode"
            );
            if (reservedKeys.contains(fieldKey.toLowerCase().trim())) {
                mostrarWarning("Validación", "El nombre de este campo coincide con un atributo estándar del producto.");
                return;
            }

            for (ProductFieldDefinitionModel existing : listCampos) {
                if (existing.getFieldKey().equals(fieldKey)) {
                    mostrarWarning("Validación", "Ya existe un campo con este nombre o llave.");
                    return;
                }
            }

            ProductFieldDefinitionModel nuevo = ProductFieldDefinitionModel.builder()
                    .companyId(companyId)
                    .targetEntity(targetEntity)
                    .warehouseId(warehouseId == null || warehouseId.isEmpty() ? null : warehouseId)
                    .supplierId(null)
                    .fieldKey(fieldKey)
                    .fieldLabel(label)
                    .fieldType(fieldType)
                    .required(chkRequired.isSelected())
                    .active(chkActive.isSelected())
                    .build();

            new Thread(() -> {
                try {
                    ProductFieldDefinitionModel created = service.crearDefinicionCampo(nuevo);
                    javafx.application.Platform.runLater(() -> {
                        listCampos.add(created);
                        limpiarFormulario();
                        mostrarInformacion("Éxito", "Campo personalizado agregado correctamente.");
                        if (onFieldsChanged != null) {
                            onFieldsChanged.run();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error", "No se pudo crear el campo: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void eliminarCampo() {
        ProductFieldDefinitionModel seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarWarning("Selección", "Debe seleccionar un campo de la tabla.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de que desea eliminar el campo '" + seleccionado.getFieldLabel() + "'?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    service.eliminarDefinicionCampo(seleccionado.getId());
                    javafx.application.Platform.runLater(() -> {
                        listCampos.remove(seleccionado);
                        limpiarFormulario();
                        mostrarInformacion("Éxito", "Campo eliminado correctamente.");
                        if (onFieldsChanged != null) {
                            onFieldsChanged.run();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error", "No se pudo eliminar el campo: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void mostrarInformacion(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarWarning(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
