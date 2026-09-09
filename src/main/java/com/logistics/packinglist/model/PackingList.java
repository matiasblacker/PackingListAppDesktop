package com.logistics.packinglist.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * El packing list completo: metadatos del documento y lista de bultos.
 */
@Data
@NoArgsConstructor
public class PackingList {

    private String id;
    private String nombreArchivo  = "";
    private String numeroOrden    = "";   // extraído del nombre del archivo si aplica
    private String fecha          = "";
    private String creadorNombre  = "";
    private String editorNombre   = "";
    private String warehouseId    = "";
    private String companyId      = "";
    private String fotosUrls      = "";

    public String getCreadorNombre() { return creadorNombre; }
    public void setCreadorNombre(String creadorNombre) { this.creadorNombre = creadorNombre; }

    public String getEditorNombre() { return editorNombre; }
    public void setEditorNombre(String editorNombre) { this.editorNombre = editorNombre; }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    // Campos de auditoría
    private String fechaCreacion   = "";
    private String usuarioCreacion = "";
    private String fechaEdicion    = "";
    private String usuarioEdicion  = "";

    private final List<Bulto> bultos = new ArrayList<>();
    
    // Estado de contenedores (Persistencia del modelador)
    private final List<ContainerState> contenedores = new ArrayList<>();

    // Totales
    private int    totalPiezas   = 0;
    private double pesoTotalKg   = 0;
    private double pesoNetoKg    = 0;

    public void addBulto(Bulto b) { bultos.add(b); }
    public int getNumeroBultos(){ return bultos.size(); }

    @Data
    @NoArgsConstructor
    public static class ContainerState {
        private String nombre;
        private boolean is40ft;
        private List<BultoSnapshot> itemsColocados = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class BultoSnapshot {
        private int bultoNumero;
        private double x;
        private double y;
        private double z;
        private boolean rotado;
        private Integer apoyadoSobreBultoNumero;
    }
}
