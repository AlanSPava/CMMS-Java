package com.industrial.cmms.service;

import com.industrial.cmms.model.MaintenanceActivity;
import com.industrial.cmms.model.MaintenancePlan;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio adaptado para la nueva jerarquía de Planes -> Tareas (Actividades).
 */
public class MaintenanceService {

    /**
     * Aplana toda la estructura para obtener la lista de todas las actividades individuales.
     */
    public List<MaintenanceActivity> getAllActivities(List<MaintenancePlan> plans) {
        if (plans == null) return new ArrayList<>();
        return plans.stream()
                .flatMap(plan -> plan.getActivities().stream())
                .collect(Collectors.toList());
    }

    /**
     * Filtra las actividades individuales según su estado.
     */
    public List<MaintenanceActivity> filterActivitiesByStatus(List<MaintenancePlan> plans, boolean completed) {
        return getAllActivities(plans).stream()
                .filter(activity -> activity.isCompleted() == completed)
                .collect(Collectors.toList());
    }

    /**
     * Filtra solo los planes abiertos (que tienen al menos una actividad pendiente).
     */
    public List<MaintenancePlan> getPendingPlans(List<MaintenancePlan> plans) {
        if (plans == null) return new ArrayList<>();
        return plans.stream()
                .filter(p -> !p.isFullyCompleted())
                .collect(Collectors.toList());
    }

    public double calculateCompliancePercentage(List<MaintenancePlan> plans) {
        List<MaintenanceActivity> all = getAllActivities(plans);
        if (all.isEmpty()) return 0.0;

        long completedCount = all.stream().filter(MaintenanceActivity::isCompleted).count();
        return (double) completedCount / all.size() * 100.0;
    }

    public long countActivitiesByEquipment(List<MaintenancePlan> plans, String equipmentId) {
        return getAllActivities(plans).stream()
                .filter(a -> a.getEquipment() != null && equipmentId.equals(a.getEquipment().getId()))
                .count();
    }
}
