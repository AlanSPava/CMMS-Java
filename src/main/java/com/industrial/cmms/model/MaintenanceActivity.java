package com.industrial.cmms.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representa una tarea/actividad individual sobre un equipo dentro de un plan.
 */
public class MaintenanceActivity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String activityId;
    private Equipment equipment;
    private LocalDate scheduledDate;
    private boolean completed;
    private LocalDate closingDate;
    private String reportComments;

    public MaintenanceActivity(Equipment equipment, LocalDate scheduledDate) {
        this.activityId = UUID.randomUUID().toString();
        this.equipment = equipment;
        this.scheduledDate = scheduledDate;
        this.completed = false;
        this.closingDate = null;
        this.reportComments = "";
    }

    /**
     * Marca la actividad como completada (Reportada individualmente o globalmente).
     * @param closingDate La fecha de cierre.
     * @param reportComments Comentarios emitidos por el operario/técnico a la hora del reporte.
     */
    public void markAsCompleted(LocalDate closingDate, String reportComments) {
        if (closingDate == null) {
            throw new IllegalArgumentException("La fecha de cierre no puede ser nula");
        }
        this.completed = true;
        this.closingDate = closingDate;
        this.reportComments = reportComments;
    }

    public String getActivityId() {
        return activityId;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public String getReportComments() {
        return reportComments;
    }

    @Override
    public String toString() {
        return "Actividad [" + activityId.substring(0, 8) + "] - " + equipment.getName() + " - " + 
               (completed ? "Completada" : "Pendiente");
    }
}
