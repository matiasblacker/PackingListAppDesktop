package com.logistics.packinglist.service;

import com.logistics.packinglist.model.InventarioItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class InventarioService {

    private final String csvFilePath;
    private final List<InventarioItem> items = new ArrayList<>();

    public InventarioService() {
        AuthService auth = AuthService.getInstance();
        Path base = Paths.get("packings");
        Path dir;
        if ("ADMINSIS".equalsIgnoreCase(auth.getRole())) {
            dir = base.resolve("admin_global");
        } else {
            String company = auth.getCompanyId() != null ? auth.getCompanyId() : "default_company";
            String warehouse = auth.getWarehouseId() != null ? auth.getWarehouseId() : "default_warehouse";
            dir = base.resolve(company).resolve(warehouse);
        }
        
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            System.err.println("Error creando directorio de inventario: " + e.getMessage());
        }
        
        this.csvFilePath = dir.resolve("inventario.csv").toString();
        cargarDesdeCSV();
    }

    private String normalizarEntradaSKU(String sku) {
        if (sku == null) return "";
        // Reemplazar comillas por guiones y ELIMINAR todos los espacios
        return sku.replace("'", "-").replaceAll("\\s+", "");
    }

    public List<InventarioItem> getItems() {
        return items;
    }

    public InventarioItem buscarPorSKU(String sku) {
        if (sku == null || sku.trim().isEmpty()) return null;
        
        String inputTrimmmed = sku.trim();
        
        // 1. Intento búsqueda exacta (ignorando mayúsculas/minúsculas)
        InventarioItem exacto = items.stream()
                .filter(i -> i.getPartNumber().equalsIgnoreCase(inputTrimmmed))
                .findFirst()
                .orElse(null);
        
        if (exacto != null) return exacto;

        // 2. Intento búsqueda alfanumérica (ignorando símbolos como -, ', espacios)
        String inputSimplificado = simplificarSKU(inputTrimmmed);
        if (inputSimplificado.isEmpty()) return null;

        return items.stream()
                .filter(i -> simplificarSKU(i.getPartNumber()).equals(inputSimplificado))
                .findFirst()
                .orElse(null);
    }

    /**
     * Remueve todos los caracteres no alfanuméricos y convierte a mayúsculas.
     * Esto permite comparar "04-50027-00" con "04'50027'00" o "045002700".
     */
    public String simplificarSKU(String sku) {
        if (sku == null) return "";
        return sku.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    public void agregarItem(InventarioItem item) {
        String skuNorm = normalizarEntradaSKU(item.getPartNumber());
        item.setPartNumber(skuNorm);
        // Evitar duplicados por SKU
        items.removeIf(i -> i.getPartNumber().equalsIgnoreCase(skuNorm));
        items.add(item);
        guardarEnCSV();
    }

    public void eliminarItem(InventarioItem item) {
        String skuNorm = normalizarEntradaSKU(item.getPartNumber());
        items.removeIf(i -> i.getPartNumber().equalsIgnoreCase(skuNorm));
        guardarEnCSV();
    }

    public void guardarEnCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFilePath))) {
            for (InventarioItem item : items) {
                // Escape commas and quotes basic implementation
                String sku = escapeCsv(item.getPartNumber());
                String desc = escapeCsv(item.getDescripcion());
                String ubic = escapeCsv(item.getUbicacion());
                pw.println(sku + "," + desc + "," + ubic);
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }

    private void cargarDesdeCSV() {
        items.clear();
        File file = new File(csvFilePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (parts.length >= 2) {
                    String sku = normalizarEntradaSKU(unescapeCsv(parts[0]));
                    String desc = unescapeCsv(parts[1]);
                    String ubic = parts.length > 2 ? unescapeCsv(parts[2]) : "";
                    items.add(new InventarioItem(sku, desc, ubic));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
        }
    }

    public void cargarDesdeExcel(File excelFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Leer la primera hoja
            boolean headerSkipped = false;

            for (Row row : sheet) {
                if (!headerSkipped) {
                    headerSkipped = true; // Asumir primera fila es cabecera
                    continue;
                }

                Cell cellSku = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell cellDesc = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell cellUbic = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                if (cellSku != null && cellDesc != null) {
                    String sku = cellToString(cellSku);
                    String desc = cellToString(cellDesc);
                    String ubic = cellUbic != null ? cellToString(cellUbic) : "";

                    if (!sku.isEmpty() && !desc.isEmpty()) {
                        String skuNorm = normalizarEntradaSKU(sku);
                        // Avoid duplicates, replace
                        items.removeIf(i -> i.getPartNumber().equalsIgnoreCase(skuNorm));
                        items.add(new InventarioItem(skuNorm, desc, ubic));
                    }
                }
            }
            guardarEnCSV();
        }
    }

    private String cellToString(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            default:
                return "";
        }
    }

    private String escapeCsv(String str) {
        if (str == null)
            return "";
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private String unescapeCsv(String str) {
        if (str == null)
            return "";
        str = str.trim();
        if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1) {
            str = str.substring(1, str.length() - 1);
            return str.replace("\"\"", "\"");
        }
        return str;
    }
}
