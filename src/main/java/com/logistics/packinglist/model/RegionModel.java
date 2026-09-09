package com.logistics.packinglist.model;

import java.util.List;

public class RegionModel {
    private String region;
    private List<String> comunas;

    public RegionModel() {}

    public RegionModel(String region, List<String> comunas) {
        this.region = region;
        this.comunas = comunas;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<String> getComunas() {
        return comunas;
    }

    public void setComunas(List<String> comunas) {
        this.comunas = comunas;
    }
}
