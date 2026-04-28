package com.industrial.cmms.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Representa un Plan de Mantenimiento macro que agrupa equipos por zona.
 */
public class MaintenancePlan implements Serializable {
    private static final long serialVersionUID = 1L;

    private String planId;
    private String zone;
    private LocalDate scheduledDate;
    private String creationComments;
    private List<MaintenanceActivity> activities;

    public MaintenancePlan(String zone, LocalDate scheduledDate, String creationComments, List<MaintenanceActivity> activities) {
        this.planId = UUID.randomUUID().toString();
        this.zone = zone;
        this.scheduledDate = scheduledDate;
        this.creationComments = creationComments;
        this.activities = activities;
    }

    /**
     * Marca todas las actividades contenidas en el plan como completadas (reporte grupal).
     * @param closingDate Fecha de cierre compartida.
     * @param reportComments Comentario aplicable a todas las actividades.
     */
    public void markAllAsCompleted(LocalDate closingDate, String reportComments) {
        if (activities != null) {
            for (MaintenanceActivity act : activities) {
                if (!act.isCompleted()) {
                    act.markAsCompleted(closingDate, reportComments);
                }
            }
        }
    }

    public String getPlanId() {
        return planId;
    }

    public String getZone() {
        return zone;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public String getCreationComments() {
        return creationComments;
    }

    public List<MaintenanceActivity> getActivities() {
        return activities;
    }

    /**
     * Verifica si todas las actividades del plan están completadas.
     */
    public boolean isFullyCompleted() {
        return activities != null && activities.stream().allMatch(MaintenanceActivity::isCompleted);
    }
}
