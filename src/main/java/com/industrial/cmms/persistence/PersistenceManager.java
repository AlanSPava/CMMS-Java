package com.industrial.cmms.persistence;

import com.industrial.cmms.model.MaintenancePlan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de persistencia que ahora guarda Planes de Mantenimiento.
 */
public class PersistenceManager {
    
    private static final String FILE_NAME = "data.dat";

    public void savePlans(List<MaintenancePlan> plans) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(plans);
            System.out.println("[Persistencia] " + plans.size() + " Planes guardados exitosamente.");
        } catch (IOException e) {
            System.err.println("[Persistencia-Error] Ocurrió un error al guardar los datos: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<MaintenancePlan> loadPlans() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<MaintenancePlan>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Persistencia-Error] Ocurrió un error al cargar los datos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
