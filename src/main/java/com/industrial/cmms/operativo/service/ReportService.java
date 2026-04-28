package com.industrial.cmms.operativo.service;

import com.industrial.cmms.operativo.model.ActivityType;
import com.industrial.cmms.operativo.model.MaintenanceReport;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contiene la lógica de negocio, cálculos de Streams para el Dashboard y persistencia de datos.
 */
public class ReportService {

    private static final String FILE_NAME = "reportes_mantenimiento.dat";
    private List<MaintenanceReport> reports;

    public ReportService() {
        this.reports = loadReports();
    }

    // --- LÓGICA DE NEGOCIO (DASHBOARD USANDO STREAMS) ---

    /**
     * Calcula el total de horas invertidas en la planta.
     * EXPLICACIÓN: Utiliza mapToDouble para extraer la cantidad de horas de cada reporte
     * y luego sum() para totalizarlas y reducir el stream a un escalar numérico.
     */
    public double calculateTotalHours() {
        return reports.stream()
                .mapToDouble(MaintenanceReport::getTotalHours)
                .sum();
    }

    /**
     * Devuelve el conteo de actividades agrupadas por su Categoría.
     * EXPLICACIÓN: Utiliza Collectors.groupingBy para indexar el mapa por ActivityType, 
     * y Collectors.counting() que cuenta cuántos elementos cayeron en cada grupo en la reducción.
     */
    public Map<ActivityType, Long> countActivitiesByType() {
        return reports.stream()
                .collect(Collectors.groupingBy(MaintenanceReport::getActivityType, Collectors.counting()));
    }

    /**
     * Devuelve la cantidad de reportes procesados agrupados por el número del Turno (1, 2, 3).
     * EXPLICACIÓN: Idéntico al método anterior, pero el clasificador de la llave del Mapa
     * utiliza el método referenciado al getter del turno entero.
     */
    public Map<Integer, Long> countReportsByShift() {
        return reports.stream()
                .collect(Collectors.groupingBy(MaintenanceReport::getShift, Collectors.counting()));
    }

    public List<MaintenanceReport> getAllReports() {
        return new ArrayList<>(reports);
    }

    public void addReport(MaintenanceReport report) {
        this.reports.add(report);
        saveReports(); // Auto-guardado
    }

    // --- PERSISTENCIA (SERIALIZACIÓN) ---

    private void saveReports() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(reports);
        } catch (IOException e) {
            System.err.println("Error fatal de I/O al guardar: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<MaintenanceReport> loadReports() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<MaintenanceReport>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar data: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
