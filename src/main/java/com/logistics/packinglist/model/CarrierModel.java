package com.logistics.packinglist.model;

public class CarrierModel {
    private String id;
    private String companyId;
    private String nombre;
    private String rut;
    private String telefono;
    private String email;
    private String trackingUrlTemplate;
    private Boolean activo;

    public CarrierModel() {}

    public CarrierModel(String id, String companyId, String nombre, String rut, String telefono, String email, String trackingUrlTemplate, Boolean activo) {
        this.id = id;
        this.companyId = companyId;
        this.nombre = nombre;
        this.rut = rut;
        this.telefono = telefono;
        this.email = email;
        this.trackingUrlTemplate = trackingUrlTemplate;
        this.activo = activo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTrackingUrlTemplate() { return trackingUrlTemplate; }
    public void setTrackingUrlTemplate(String trackingUrlTemplate) { this.trackingUrlTemplate = trackingUrlTemplate; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre != null ? nombre : "";
    }
}
