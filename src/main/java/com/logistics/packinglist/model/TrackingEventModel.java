package com.logistics.packinglist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventModel {
    private String id;
    private String orderNoteId;
    private String trackingNumber;
    private String estado;
    private String descripcion;
    private String ubicacionNombre;
    private Double latitud;
    private Double longitud;
    private String nombreCourier;
    private String numeroSeguimientoCourier;
    private String fotosUrls;
    private String retiradoPorNombre;
    private String retiradoPorRut;
    private String retiradoPorPatente;
    private String fechaHora;
}
