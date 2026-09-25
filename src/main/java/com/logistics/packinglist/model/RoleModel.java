package com.logistics.packinglist.model;

import com.google.gson.annotations.SerializedName;

public class RoleModel {
    private String id;
    private String companyId;

    @SerializedName(value = "nombre", alternate = {"name"})
    private String nombre;

    @SerializedName(value = "descripcion", alternate = {"description"})
    private String descripcion;

    private String permisos;
    private Long createdAt;
    
    public RoleModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getName() { return nombre; }
    public void setName(String name) { this.nombre = name; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getDescription() { return descripcion; }
    public void setDescription(String description) { this.descripcion = description; }
    
    public String getPermisos() { return permisos; }
    public void setPermisos(String permisos) { this.permisos = permisos; }
    
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return nombre != null ? nombre : "";
    }
}
