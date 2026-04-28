package com.industrial.cmms.operativo.model;

/**
 * Enumerador que define las posibles clasificaciones de una actividad de mantenimiento.
 */
public enum ActivityType {
    RONDA_DE_PLANTA("Ronda de Planta"),
    REPARACION_CORRECTIVA("Reparación Correctiva"),
    REPARACION_PREVENTIVA("Reparación Preventiva"),
    APOYO_OPERACION("Apoyo en operación de equipos"),
    LLENADO_BITACORA("Llenado de Bitácora");

    private final String description;

    ActivityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
