with open("src/main/java/com/logistics/packinglist/ui/UbicacionesDialog.java", "r") as f:
    content = f.read()

# 1. Update SplitPane divider position to 0.3
content = content.replace("mainSplit.setDividerPositions(0.5);", "mainSplit.setDividerPositions(0.3);")

# 2. Update Tree action buttons to Icon-Only with white icons
old_tree_btns = """        // Barra de acciones para Zonas/Sectores
        Button btnNuevaZona = new Button("Nueva Zona", new FontAwesomeIconView(FontAwesomeIcon.PLUS));
        btnNuevaZona.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px;");
        btnNuevaZona.setOnAction(e -> crearNuevaZona());

        Button btnEditarZona = new Button("Editar", new FontAwesomeIconView(FontAwesomeIcon.EDIT));
        btnEditarZona.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px;");
        btnEditarZona.setOnAction(e -> editarZonaSeleccionada());

        Button btnEliminarZona = new Button("Eliminar", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
        btnEliminarZona.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px;");
        btnEliminarZona.setOnAction(e -> eliminarZonaSeleccionada());"""

new_tree_btns = """        // Barra de acciones para Zonas/Sectores (Íconos Blancos Solo Ícono)
        FontAwesomeIconView iconPlusZ = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        iconPlusZ.setFill(Color.WHITE);
        Button btnNuevaZona = new Button("", iconPlusZ);
        btnNuevaZona.setStyle("-fx-background-color: #28a745; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnNuevaZona.setOnAction(e -> crearNuevaZona());
        Tooltip.install(btnNuevaZona, new Tooltip("Nueva Zona / Sector"));

        FontAwesomeIconView iconEditZ = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
        iconEditZ.setFill(Color.WHITE);
        Button btnEditarZona = new Button("", iconEditZ);
        btnEditarZona.setStyle("-fx-background-color: #ffc107; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnEditarZona.setOnAction(e -> editarZonaSeleccionada());
        Tooltip.install(btnEditarZona, new Tooltip("Editar Zona / Sector"));

        FontAwesomeIconView iconDelZ = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDelZ.setFill(Color.WHITE);
        Button btnEliminarZona = new Button("", iconDelZ);
        btnEliminarZona.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btnEliminarZona.setOnAction(e -> eliminarZonaSeleccionada());
        Tooltip.install(btnEliminarZona, new Tooltip("Eliminar Zona / Sector"));"""

content = content.replace(old_tree_btns, new_tree_btns)

# 3. Add StockModel import if missing
if "import com.logistics.packinglist.model.StockModel;" not in content:
    content = content.replace("import com.logistics.packinglist.model.LocationModel;", "import com.logistics.packinglist.model.LocationModel;\nimport com.logistics.packinglist.model.StockModel;")

# 4. Update guardarUbicacion to validate duplicate location code
old_guardar_head = """        LocationModel model = selectedLocation != null ? selectedLocation : new LocationModel();"""
new_guardar_head = """        String targetCode = txtCodigoGenerado.getText().trim();
        if (targetCode.isEmpty()) {
            mostrarWarning("Validación", "Debe generar o ingresar las coordenadas para el código de ubicación.");
            return;
        }

        // Validar que el código no se repita en la bodega activa
        try {
            List<LocationModel> existingLocs = service.obtenerUbicacionesPorBodega(selectedWarehouseContext.getId());
            if (existingLocs != null) {
                for (LocationModel existing : existingLocs) {
                    if (existing.getCodigoUbicacion() != null && existing.getCodigoUbicacion().equalsIgnoreCase(targetCode)) {
                        if (selectedLocation == null || !existing.getId().equalsIgnoreCase(selectedLocation.getId())) {
                            mostrarWarning("Código Duplicado", "Ya existe una ubicación física registrada con el código '" + targetCode + "' en esta bodega.");
                            return;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        LocationModel model = selectedLocation != null ? selectedLocation : new LocationModel();"""

content = content.replace(old_guardar_head, new_guardar_head)

# 5. Update eliminarUbicacion to validate product stock presence
old_eliminar_loc = """    private void eliminarUbicacion() {
        if (selectedLocation == null) {
            mostrarWarning("Validación", "Seleccione una ubicación de la tabla.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar la ubicación " + selectedLocation.getCodigoUbicacion() + "?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            new Thread(() -> {
                try {
                    service.eliminarUbicacion(selectedLocation.getId());
                    javafx.application.Platform.runLater(() -> {
                        locationsList.remove(selectedLocation);
                        limpiarFormularioUbicacion();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo eliminar la ubicación: " + e.getMessage()));
                }
            }).start();
        }
    }"""

new_eliminar_loc = """    private void eliminarUbicacion() {
        if (selectedLocation == null) {
            mostrarWarning("Validación", "Seleccione una ubicación de la tabla.");
            return;
        }

        new Thread(() -> {
            try {
                List<StockModel> stocks = service.obtenerStocksPorUbicacion(selectedLocation.getId());
                int totalStock = 0;
                if (stocks != null) {
                    for (StockModel s : stocks) {
                        if (s.getCantidad() != null && s.getCantidad() > 0) {
                            totalStock += s.getCantidad();
                        }
                    }
                }
                final int fTotalStock = totalStock;
                javafx.application.Platform.runLater(() -> {
                    if (fTotalStock > 0) {
                        mostrarWarning("Imposible Eliminar", "No se puede eliminar la ubicación '" + selectedLocation.getCodigoUbicacion() + "' porque contiene " + fTotalStock + " unidad(es) de productos en stock.");
                        return;
                    }

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar la ubicación " + selectedLocation.getCodigoUbicacion() + "?", ButtonType.YES, ButtonType.NO);
                    alert.setTitle("Confirmar eliminación");
                    alert.setHeaderText(null);
                    if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                        new Thread(() -> {
                            try {
                                service.eliminarUbicacion(selectedLocation.getId());
                                javafx.application.Platform.runLater(() -> {
                                    locationsList.remove(selectedLocation);
                                    limpiarFormularioUbicacion();
                                    mostrarInformacion("Éxito", "Ubicación eliminada correctamente.");
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo eliminar la ubicación: " + e.getMessage()));
                            }
                        }).start();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo verificar el stock de la ubicación: " + ex.getMessage()));
            }
        }).start();
    }"""

content = content.replace(old_eliminar_loc, new_eliminar_loc)

# 6. Update eliminarZonaSeleccionada to validate product stock presence
old_eliminar_zona = """    private void eliminarZonaSeleccionada() {
        if (selectedZoneContext == null) {
            mostrarWarning("Estructura", "Debe seleccionar una Zona en el árbol para eliminar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el sector o zona '" + selectedZoneContext.getNombre() + "'? Se eliminarán recursivamente sus subzonas.", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Eliminar Zona");
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            new Thread(() -> {
                try {
                    service.eliminarZona(selectedZoneContext.getId());
                    javafx.application.Platform.runLater(this::cargarDatos);
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo eliminar el sector: " + e.getMessage()));
                }
            }).start();
        }
    }"""

new_eliminar_zona = """    private void eliminarZonaSeleccionada() {
        if (selectedZoneContext == null) {
            mostrarWarning("Estructura", "Debe seleccionar una Zona en el árbol para eliminar.");
            return;
        }

        new Thread(() -> {
            try {
                List<LocationModel> locsInZone = service.obtenerUbicacionesPorZona(selectedZoneContext.getId());
                int totalZoneStock = 0;
                if (locsInZone != null) {
                    for (LocationModel loc : locsInZone) {
                        List<StockModel> stocks = service.obtenerStocksPorUbicacion(loc.getId());
                        if (stocks != null) {
                            for (StockModel s : stocks) {
                                if (s.getCantidad() != null && s.getCantidad() > 0) {
                                    totalZoneStock += s.getCantidad();
                                }
                            }
                        }
                    }
                }
                final int fTotalZoneStock = totalZoneStock;
                javafx.application.Platform.runLater(() -> {
                    if (fTotalZoneStock > 0) {
                        mostrarWarning("Imposible Eliminar Zona", "No se puede eliminar la zona/sector '" + selectedZoneContext.getNombre() + "' porque contiene ubicaciones con un total de " + fTotalZoneStock + " unidad(es) de productos en stock.");
                        return;
                    }

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el sector o zona '" + selectedZoneContext.getNombre() + "'? Se eliminarán recursivamente sus subzonas.", ButtonType.YES, ButtonType.NO);
                    alert.setTitle("Eliminar Zona");
                    alert.setHeaderText(null);
                    if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                        new Thread(() -> {
                            try {
                                service.eliminarZona(selectedZoneContext.getId());
                                javafx.application.Platform.runLater(() -> {
                                    cargarDatos();
                                    mostrarInformacion("Éxito", "Zona eliminada correctamente.");
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo eliminar el sector: " + e.getMessage()));
                            }
                        }).start();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> mostrarError("Error", "No se pudo verificar el stock de las ubicaciones de la zona: " + ex.getMessage()));
            }
        }).start();
    }"""

content = content.replace(old_eliminar_zona, new_eliminar_zona)

with open("src/main/java/com/logistics/packinglist/ui/UbicacionesDialog.java", "w") as f:
    f.write(content)

print("Updated UbicacionesDialog.java with 30/70 split, white icon-only buttons, duplicate code validation, and stock presence checks!")
