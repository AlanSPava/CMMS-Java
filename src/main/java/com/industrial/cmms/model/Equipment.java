package com.industrial.cmms.model;

import java.io.Serializable;

/**
 * Representa un equipo en la planta industrial.
 */
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private int maintenanceFrequencyDays;
    private String zone;

    public Equipment(String id, String name, int maintenanceFrequencyDays, String zone) {
        this.id = id;
        this.name = name;
        this.maintenanceFrequencyDays = maintenanceFrequencyDays;
        this.zone = zone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaintenanceFrequencyDays() {
        return maintenanceFrequencyDays;
    }

    public void setMaintenanceFrequencyDays(int maintenanceFrequencyDays) {
        this.maintenanceFrequencyDays = maintenanceFrequencyDays;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return "Equipo [ID: " + id + ", Nombre: " + name + ", Frecuencia: " + maintenanceFrequencyDays + " días, Zona: " + zone + "]";
    }
}
