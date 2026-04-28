package com.industrial.cmms.operativo.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Clase de datos que representa un reporte de actividad diaria en la planta.
 */
public class MaintenanceReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private String zone;
    private String equipment;
    private String technicianName;
    private double totalHours;
    private LocalDate date;
    private int shift;
    private String comments;
    private ActivityType activityType;

    public MaintenanceReport(String zone, String equipment, String technicianName, double totalHours, 
                             LocalDate date, int shift, String comments, ActivityType activityType) {
        // Validaciones requeridas
        if (totalHours < 0) {
            throw new IllegalArgumentException("El tiempo total no puede ser negativo.");
        }
        if (shift < 1 || shift > 3) {
            throw new IllegalArgumentException("El turno debe ser 1, 2 o 3.");
        }

        this.zone = zone;
        this.equipment = equipment;
        this.technicianName = technicianName;
        this.totalHours = totalHours;
        this.date = date;
        this.shift = shift;
        this.comments = comments;
        this.activityType = activityType;
    }

    // --- Getters ---

    public String getZone() { return zone; }
    public String getEquipment() { return equipment; }
    public String getTechnicianName() { return technicianName; }
    public double getTotalHours() { return totalHours; }
    public LocalDate getDate() { return date; }
    public int getShift() { return shift; }
    public String getComments() { return comments; }
    public ActivityType getActivityType() { return activityType; }

    @Override
    public String toString() {
        return String.format("[%s] Turno %d | %s | %s - %s | Horas: %.1f | Tipo: %s | Obs: %s",
                date, shift, zone, equipment, technicianName, totalHours, 
                activityType.getDescription(), comments);
    }
}
