package com.industrial.cmms.operativo.ui;

import com.industrial.cmms.operativo.model.ActivityType;
import com.industrial.cmms.operativo.model.MaintenanceReport;
import com.industrial.cmms.operativo.service.ReportService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Capa de Presentación interactiva por consola.
 */
public class ConsoleUI {

    private final ReportService reportService;
    private final Scanner scanner;

    public ConsoleUI() {
        this.reportService = new ReportService();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=============================================");
            System.out.println("  CMMS OPERATIVO - REGISTRO Y DIAGNÓSTICO");
            System.out.println("=============================================");
            System.out.println("1. Registrar nueva Actividad (Reporte)");
            System.out.println("2. Ver Dashboard de Planta (Análisis)");
            System.out.println("3. Ver Listado Histórico de Actividades");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1": createReport(); break;
                case "2": showDashboard(); break;
                case "3": showHistorical(); break;
                case "4": exit = true; System.out.println("Cerrando sesión de sistema..."); break;
                default: System.out.println("[Error] Opción no válida. Intente nuevamente.");
            }
        }
    }

    private void createReport() {
        System.out.println("\n--- ALTA DE REPORTE DE MANTENIMIENTO ---");
        
        System.out.print("Zona: ");
        String zone = scanner.nextLine().trim();

        System.out.print("Equipo Intervenido: ");
        String eq = scanner.nextLine().trim();

        System.out.print("Técnico a cargo: ");
        String techn = scanner.nextLine().trim();

        double hours = -1;
        while (hours < 0) {
            System.out.print("Tiempo total invertido (horas): ");
            try {
                hours = Double.parseDouble(scanner.nextLine().trim());
                if (hours < 0) System.out.println("Error: El tiempo no puede ser negativo.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Formato numérico inválido.");
            }
        }

        LocalDate date = null;
        while (date == null) {
            System.out.print("Fecha de ejecución (YYYY-MM-DD): ");
            try {
                date = LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Error: Formato de fecha requerida es YYYY-MM-DD.");
            }
        }

        int shift = 0;
        while (shift < 1 || shift > 3) {
            System.out.print("Turno de Operación (1, 2, o 3): ");
            try {
                shift = Integer.parseInt(scanner.nextLine().trim());
                if (shift < 1 || shift > 3) System.out.println("Error: Turno fuera de rango.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Ingrese un dígito válido.");
            }
        }

        System.out.println("Seleccione el Tipo de Actividad:");
        ActivityType[] types = ActivityType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println((i + 1) + ". " + types[i].getDescription());
        }
        
        ActivityType selectedType = null;
        while (selectedType == null) {
            System.out.print("Opciones (1-" + types.length + "): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= types.length) {
                    selectedType = types[choice - 1];
                } else {
                    System.out.println("Error: Opción de actividad fuera de rango.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Formato de opción inválido.");
            }
        }

        System.out.print("Comentarios/Observaciones: ");
        String comments = scanner.nextLine().trim();

        MaintenanceReport rw = new MaintenanceReport(zone, eq, techn, hours, date, shift, comments, selectedType);
        reportService.addReport(rw);
        
        System.out.println(">> Reporte guardado de forma permanente <<");
    }

    private void showDashboard() {
        System.out.println("\n--------------- DASHBOARD ANALÍTICO ---------------");
        
        System.out.printf(">> Total de horas invertidas en planta: %.2f horas\n", reportService.calculateTotalHours());
        
        System.out.println("\n>> Desglose Técnico por Tipo de Actividad:");
        Map<ActivityType, Long> byType = reportService.countActivitiesByType();
        if (byType.isEmpty()) System.out.println("   (No hay datos)");
        
        // Uso de forEach (metodo inferido de Streams) en el Map
        byType.forEach((type, count) -> {
            System.out.println("   - " + type.getDescription() + ": " + count + " reporte(s).");
        });

        System.out.println("\n>> Productividad por Turno Operativo:");
        Map<Integer, Long> byShift = reportService.countReportsByShift();
        if (byShift.isEmpty()) System.out.println("   (No hay datos)");
        byShift.forEach((shift, count) -> {
            System.out.println("   - Turno " + shift + ": " + count + " reporte(s).");
        });
        
        System.out.println("---------------------------------------------------");
    }

    private void showHistorical() {
        System.out.println("\n--- HITÓRICO DETALLADO DE ACTIVIDADES ---");
        List<MaintenanceReport> all = reportService.getAllReports();
        if (all.isEmpty()) {
            System.out.println("El registro general está vacío.");
        } else {
            all.forEach(System.out::println);
        }
    }

    public static void main(String[] args) {
        new ConsoleUI().start();
    }
}
