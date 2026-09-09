import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

# 1. Imports
imports_add = """import com.logistics.packinglist.model.LocationModel;
import com.logistics.packinglist.model.StockModel;
import com.logistics.packinglist.service.PdfAnuncioService;
import javafx.stage.FileChooser;
import java.io.File;"""

content = content.replace("package com.logistics.packinglist.ui;", "package com.logistics.packinglist.ui;\n\n" + imports_add)

# 2. Add btnPdf to actionButtons
old_btn_row = "actionButtons.getChildren().addAll(btnGuardarAnuncio, btnEliminarAnuncio, btnLimpiarAnuncio);"
new_btn_row = """Button btnPdf = UiComponentFactory.createPdfButton(this::descargarPDF);
        actionButtons.getChildren().addAll(btnPdf, btnGuardarAnuncio, btnEliminarAnuncio, btnLimpiarAnuncio);"""

content = content.replace(old_btn_row, new_btn_row)

# 3. Add descargarPDF method
descargar_pdf_code = """
    private void descargarPDF() {
        ReceptionAnnouncementModel selected = getSelectedAnnouncement();
        
        if (selected == null) {
            WarehouseModel bod = cbBodega.getValue();
            SupplierModel prov = cbProveedor.getValue();
            if (bod == null || prov == null || detailRows.isEmpty()) {
                mostrarWarning("Exportar PDF", "Debe seleccionar un Anuncio de Carga de la lista o ingresar datos en el formulario.");
                return;
            }
            selected = new ReceptionAnnouncementModel();
            selected.setFolio("AN-NUEVO");
            selected.setWarehouseId(bod.getId());
            selected.setSupplierId(prov.getId());
            selected.setObservaciones(txtObservaciones.getText().trim());
            selected.setEstado("PENDIENTE");
            selected.setFecha(java.time.LocalDateTime.now().toString());

            List<AnnouncementDetailModel> details = new ArrayList<>();
            for (DetailRow row : detailRows) {
                AnnouncementDetailModel d = new AnnouncementDetailModel();
                d.setProductId(row.getProductId());
                d.setCantidad(row.getCantidad());
                details.add(d);
            }
            selected.setDetails(details);
        }

        final ReceptionAnnouncementModel targetAnnouncement = selected;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF de Anuncio de Carga");
        String folioName = targetAnnouncement.getFolio() != null ? targetAnnouncement.getFolio() : "ANUNCIO";
        fileChooser.setInitialFileName(folioName + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            new Thread(() -> {
                try {
                    PdfAnuncioService pdfService = new PdfAnuncioService();
                    
                    SupplierModel supplier = supplierMap.get(targetAnnouncement.getSupplierId());
                    WarehouseModel warehouse = warehouseMap.get(targetAnnouncement.getWarehouseId());
                    
                    CompanyModel compObj = null;
                    String companyId = targetAnnouncement.getCompanyId() != null ? targetAnnouncement.getCompanyId() : (warehouse != null ? warehouse.getCompanyId() : null);
                    if (companyId != null) {
                        try {
                            compObj = service.obtenerEmpresaPorId(companyId);
                        } catch (Exception ignored) {}
                    }

                    List<StockModel> stocks = new ArrayList<>();
                    List<LocationModel> locs = new ArrayList<>();
                    try {
                        stocks = service.obtenerStocks();
                        locs = service.obtenerUbicaciones();
                    } catch (Exception ignored) {}

                    Map<String, LocationModel> locationMap = new java.util.HashMap<>();
                    for (LocationModel l : locs) {
                        locationMap.put(l.getId(), l);
                    }

                    pdfService.exportar(targetAnnouncement, compObj, supplier, warehouse, productMap, stocks, locationMap, file);
                    
                    javafx.application.Platform.runLater(() -> {
                        mostrarInformacion("PDF Creado", "El PDF del Anuncio de Carga se ha exportado con éxito.");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("Error", "No se pudo generar el PDF del Anuncio: " + e.getMessage());
                    });
                }
            }).start();
        }
    }
"""

# Insert before end of class
content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + descargar_pdf_code + "\n}\n"

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Added descargarPDF and PDF button to AnunciosDialog")
