with open("src/main/java/com/logistics/packinglist/ui/UbicacionesDialog.java", "r") as f:
    content = f.read()

old_eliminar_zona = """    private void eliminarZonaSeleccionada() {
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

new_eliminar_zona = """    private void eliminarZonaSeleccionada() {
        if (selectedZoneContext == null) {
            mostrarWarning("Estructura", "Debe seleccionar una Zona en el árbol para eliminar.");
            return;
        }

        new Thread(() -> {
            try {
                List<WarehouseZoneModel> allZonesInWh = warehouseZonesMap.get(selectedWarehouseContext.getId());
                List<String> allZoneIdsInTree = obtenerTodasLasSubzonasIds(selectedZoneContext.getId(), allZonesInWh);

                int totalZoneStock = 0;
                for (String zid : allZoneIdsInTree) {
                    List<LocationModel> locsInZone = service.obtenerUbicacionesPorZona(zid);
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
                }
                final int fTotalZoneStock = totalZoneStock;
                javafx.application.Platform.runLater(() -> {
                    if (fTotalZoneStock > 0) {
                        mostrarWarning("Imposible Eliminar Zona", "No se puede eliminar la zona/sector '" + selectedZoneContext.getNombre() + "' ni sus sub-zonas porque contienen ubicaciones con un total de " + fTotalZoneStock + " unidad(es) de productos en stock.");
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
    }

    private List<String> obtenerTodasLasSubzonasIds(String zoneId, List<WarehouseZoneModel> allZonesInWh) {
        List<String> result = new ArrayList<>();
        result.add(zoneId);
        if (allZonesInWh != null) {
            for (WarehouseZoneModel z : allZonesInWh) {
                if (zoneId.equals(z.getParentZoneId())) {
                    result.addAll(obtenerTodasLasSubzonasIds(z.getId(), allZonesInWh));
                }
            }
        }
        return result;
    }"""

content = content.replace(old_eliminar_zona, new_eliminar_zona)

with open("src/main/java/com/logistics/packinglist/ui/UbicacionesDialog.java", "w") as f:
    f.write(content)

print("Updated UbicacionesDialog.java with recursive subzone stock check!")
