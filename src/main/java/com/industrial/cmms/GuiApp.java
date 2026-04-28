package com.industrial.cmms;

import com.industrial.cmms.model.Equipment;
import com.industrial.cmms.model.MaintenanceActivity;
import com.industrial.cmms.model.MaintenancePlan;
import com.industrial.cmms.persistence.PersistenceManager;
import com.industrial.cmms.service.MaintenanceService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interfaz Gráfica con Soporte Jerárquico para Planes y Tareas, con mejoras UX/UI.
 */
public class GuiApp extends JFrame {

    private final PersistenceManager persistenceManager;
    private final MaintenanceService maintenanceService;
    private final List<MaintenancePlan> allPlans;
    private final List<Equipment> knownEquipments;

    // Componentes principales
    private JComboBox<String> cbxZones;
    private DefaultListModel<EquipmentItem> multiEqModel;
    private JList<EquipmentItem> multiEqList;

    private DefaultTableModel topPlanModel;
    private JTable topPlanTable;
    private DefaultTableModel bottomActModel;
    private JTable bottomActTable;

    private JLabel lblCompliance;
    private JLabel lblTotalAct;
    private JTextArea dashboardArea;

    // Estilos Flat
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);

    private final Color PRIMARY_COLOR = new Color(20, 150, 220); // Azul claro para llamar atención al fondo del boton
    private final Color TEXT_BTN_COLOR = new Color(0, 35, 102);  // Azul Rey Oscuro (solicitado)
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color ACCENT_COLOR = new Color(243, 156, 18);
    private final Color BG_COLOR = new Color(245, 246, 250);
    private final Color PANEL_COLOR = Color.WHITE;

    public GuiApp() {
        UIManager.put("TabbedPane.font", BOLD_FONT);
        UIManager.put("TabbedPane.background", BG_COLOR);
        UIManager.put("TabbedPane.selected", PANEL_COLOR);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));

        persistenceManager = new PersistenceManager();
        maintenanceService = new MaintenanceService();
        allPlans = persistenceManager.loadPlans();
        knownEquipments = new ArrayList<>();

        extractKnownEquipments();

        setTitle("CMMS - Planes de Mantenimiento por Zona");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                persistenceManager.savePlans(allPlans);
                System.exit(0);
            }
        });

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_COLOR);
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        tabbedPane.addTab("1. Base de Equipos", createEquipmentPanel());
        tabbedPane.addTab("2. Crear Plan x Zona", createProgramPanel());
        tabbedPane.addTab("3. Reporte (Ejecución)", createExecutionPanel());
        tabbedPane.addTab("4. Dashboard", createDashboardPanel());

        tabbedPane.addChangeListener(e -> refreshDynamicComponents());

        add(tabbedPane);
        refreshDynamicComponents();
    }

    private void extractKnownEquipments() {
        List<MaintenanceActivity> allActs = maintenanceService.getAllActivities(allPlans);
        for (MaintenanceActivity act : allActs) {
            boolean exists = knownEquipments.stream().anyMatch(eq -> eq.getId().equals(act.getEquipment().getId()));
            if (!exists) knownEquipments.add(act.getEquipment());
        }
    }

    private List<String> getUniqueZones() {
        return knownEquipments.stream().map(eq -> eq.getZone() != null ? eq.getZone() : "N/A").distinct().collect(Collectors.toList());
    }

    private JButton createFlatButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bgColor);
        btn.setForeground(TEXT_BTN_COLOR); // Letras Azul Rey Oscuro
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createHeader(String title, String subtitle) {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(PANEL_COLOR);
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(new Color(44, 62, 80));
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(MAIN_FONT);
        lblSub.setForeground(Color.GRAY);
        header.add(lblTitle);
        header.add(lblSub);
        return header;
    }

    private JTextField createFlatTextField() {
        JTextField tf = new JTextField();
        tf.setFont(MAIN_FONT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return tf;
    }

    // --- PESTAÑA 1: EQUIPOS ---
    private JPanel createEquipmentPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(PANEL_COLOR);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtId = createFlatTextField(); txtId.setColumns(20);
        JTextField txtName = createFlatTextField();
        JTextField txtFreq = createFlatTextField();
        JTextField txtZone = createFlatTextField();

        JLabel[] labels = { new JLabel("ID del Equipo:"), new JLabel("Nombre Completo:"), new JLabel("Frecuencia (días):"), new JLabel("Zona Asignada:") };
        JTextField[] fields = { txtId, txtName, txtFreq, txtZone };

        for (int i = 0; i < labels.length; i++) {
            labels[i].setFont(BOLD_FONT);
            gbc.gridx = 0; gbc.gridy = i; content.add(labels[i], gbc);
            gbc.gridx = 1; gbc.gridy = i; content.add(fields[i], gbc);
        }

        JButton btnSave = createFlatButton("Alta de Equipo", PRIMARY_COLOR);
        btnSave.addActionListener(e -> {
            try {
                if (txtId.getText().isEmpty() || txtName.getText().isEmpty() || txtZone.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Debe completar ID, Nombre y Zona.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Equipment eq = new Equipment(txtId.getText().trim(), txtName.getText().trim(), 
                                             Integer.parseInt(txtFreq.getText().trim()), txtZone.getText().trim());
                knownEquipments.add(eq);
                JOptionPane.showMessageDialog(this, "Equipo anexado a la base local.\n(Aún no guardado en archivo permanente)");
                txtId.setText(""); txtName.setText(""); txtFreq.setText(""); txtZone.setText("");
                refreshDynamicComponents();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "La frecuencia debe ser número.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 1; gbc.gridy = labels.length; gbc.insets = new Insets(25, 10, 10, 10);
        content.add(btnSave, gbc);

        main.add(createHeader("Base de Maquinaria", "Registra un equipo asociándolo a una Zona y Frecuencia."), BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        return main;
    }

    // --- PESTAÑA 2: PROGRAMAR PLAN ---
    private JPanel createProgramPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(PANEL_COLOR);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        cbxZones = new JComboBox<>();
        cbxZones.setFont(MAIN_FONT);
        cbxZones.addActionListener(e -> updateEqListForSelectedZone());

        multiEqModel = new DefaultListModel<>();
        multiEqList = new JList<>(multiEqModel);
        multiEqList.setFont(MAIN_FONT);
        multiEqList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollEq = new JScrollPane(multiEqList);
        scrollEq.setPreferredSize(new Dimension(300, 120));
        scrollEq.setBorder(BorderFactory.createTitledBorder("Equipos en la Zona (Multi-selección)"));

        JTextField txtDate = createFlatTextField();
        JTextArea txtComments = new JTextArea(4, 30);
        txtComments.setFont(MAIN_FONT);
        txtComments.setLineWrap(true);
        txtComments.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weighty = 0;
        content.add(new JLabel("Seleccione la Zona del Plan:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; content.add(cbxZones, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weighty = 1.0;
        content.add(scrollEq, gbc);

        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = 2; gbc.weighty = 0;
        content.add(new JLabel("Fecha (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; content.add(txtDate, gbc);

        gbc.gridx = 0; gbc.gridy = 3; content.add(new JLabel("Comentarios del Plan:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; content.add(new JScrollPane(txtComments), gbc);

        JButton btnProgram = createFlatButton("Agrupar y Crear Plan", SUCCESS_COLOR);
        btnProgram.addActionListener(e -> {
            String zone = (String) cbxZones.getSelectedItem();
            List<EquipmentItem> selectedEqs = multiEqList.getSelectedValuesList();
            if (zone == null || selectedEqs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una zona y al menos un equipo de la lista.");
                return;
            }

            LocalDate scheduledDate;
            try {
                scheduledDate = txtDate.getText().trim().isEmpty() ? LocalDate.now() : LocalDate.parse(txtDate.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Formato YYYY-MM-DD inválido."); return;
            }

            List<MaintenanceActivity> acts = new ArrayList<>();
            for (EquipmentItem ei : selectedEqs) {
                acts.add(new MaintenanceActivity(ei.getEquipment(), scheduledDate));
            }

            MaintenancePlan plan = new MaintenancePlan(zone, scheduledDate, txtComments.getText().trim(), acts);
            allPlans.add(plan);
            JOptionPane.showMessageDialog(this, "Plan Grupal creado con éxito para la zona: " + zone);
            
            txtDate.setText(""); txtComments.setText(""); multiEqList.clearSelection();
        });

        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        content.add(btnProgram, gbc);

        main.add(createHeader("Configurador de Planes", "Arma un paquete de mantenimiento para toda una zona específica."), BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        return main;
    }

    private void updateEqListForSelectedZone() {
        String z = (String) cbxZones.getSelectedItem();
        multiEqModel.clear();
        if (z == null) return;
        for (Equipment eq : knownEquipments) {
            String eqZone = eq.getZone() != null ? eq.getZone() : "N/A";
            if (eqZone.equals(z)) {
                multiEqModel.addElement(new EquipmentItem(eq));
            }
        }
    }

    // --- PESTAÑA 3: EJECUCIÓN (Vistas Grupales e Individuales) ---
    private JPanel createExecutionPanel() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        // TABLA SUPERIOR (PLANES)
        topPlanModel = new DefaultTableModel(new String[]{"ID Plan", "Zona", "F. P.", "Comentarios del Plan"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        topPlanTable = createStyledTable(topPlanModel);
        topPlanTable.getSelectionModel().addListSelectionListener(e -> updateBottomActTable());

        JPanel topWrap = new JPanel(new BorderLayout());
        topWrap.setBackground(PANEL_COLOR); topWrap.setBorder(BorderFactory.createTitledBorder("Planes Pendientes (SELECCIONAR PARA VER EQUIPOS)"));
        topWrap.add(new JScrollPane(topPlanTable), BorderLayout.CENTER);

        // TABLA INFERIOR (ACTIVIDADES)
        bottomActModel = new DefaultTableModel(new String[]{"ID Actividad", "Equipo", "Estado (Completada)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bottomActTable = createStyledTable(bottomActModel);
        
        JPanel botWrap = new JPanel(new BorderLayout());
        botWrap.setBackground(PANEL_COLOR); botWrap.setBorder(BorderFactory.createTitledBorder("Equipos en el Plan seleccionado"));
        botWrap.add(new JScrollPane(bottomActTable), BorderLayout.CENTER);

        // BOTONES CERRAR
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        actionsPanel.setBackground(BG_COLOR);
        
        JButton btnReportPlan = createFlatButton("Reportar Plan Completo", SUCCESS_COLOR);
        JButton btnReportEq = createFlatButton("Reportar Sólo Equipo Seleccionado", ACCENT_COLOR);

        btnReportPlan.addActionListener(e -> executeReportSequence(true));
        btnReportEq.addActionListener(e -> executeReportSequence(false));

        actionsPanel.add(btnReportPlan);
        actionsPanel.add(btnReportEq);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topWrap, botWrap);
        split.setDividerLocation(200);
        split.setBackground(BG_COLOR);

        main.add(createHeader("Consola de Ejecución", "Reporta un plan grupal de un solo toque, o equipos de manera individual."), BorderLayout.NORTH);
        main.add(split, BorderLayout.CENTER);
        main.add(actionsPanel, BorderLayout.SOUTH);

        return main;
    }

    private void updateBottomActTable() {
        bottomActModel.setRowCount(0);
        int row = topPlanTable.getSelectedRow();
        if (row < 0) return;
        
        String planId = (String) topPlanModel.getValueAt(row, 0);
        MaintenancePlan plan = maintenanceService.getPendingPlans(allPlans).stream()
                .filter(p -> p.getPlanId().startsWith(planId)).findFirst().orElse(null);

        if (plan != null) {
            for (MaintenanceActivity act : plan.getActivities()) {
                bottomActModel.addRow(new Object[]{
                        act.getActivityId().substring(0, 8),
                        act.getEquipment().getName(),
                        act.isCompleted() ? "Sí" : "Pendiente"
                });
            }
        }
    }

    private void executeReportSequence(boolean isGroup) {
        int planRow = topPlanTable.getSelectedRow();
        if (planRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un Plan de la tabla superior."); return;
        }

        MaintenancePlan plan = maintenanceService.getPendingPlans(allPlans).get(planRow);
        MaintenanceActivity actToClose = null;

        if (!isGroup) {
            int actRow = bottomActTable.getSelectedRow();
            if (actRow < 0) {
                JOptionPane.showMessageDialog(this, "Para reporte individual, seleccione el Equipo en la tabla inferior."); return;
            }
            if (plan.getActivities().get(actRow).isCompleted()) {
                JOptionPane.showMessageDialog(this, "Este equipo ya fue reportado previamente."); return;
            }
            actToClose = plan.getActivities().get(actRow);
        }

        // Dialog customizado para Comandos y Fecha
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField txtDate = createFlatTextField(); txtDate.setText(LocalDate.now().toString());
        JTextField txtComm = createFlatTextField();
        p.add(new JLabel("Fecha Cierre:")); p.add(txtDate);
        p.add(new JLabel("Comentarios del Operario:")); p.add(txtComm);

        int result = JOptionPane.showConfirmDialog(this, p, "Datos de Cierre " + (isGroup ? "(GRUPAL)" : "(INDIVIDUAL)"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                LocalDate date = LocalDate.parse(txtDate.getText().trim());
                String comms = txtComm.getText().trim();
                
                if (isGroup) {
                    plan.markAllAsCompleted(date, comms);
                    JOptionPane.showMessageDialog(this, "Todo el plan fue cerrado exitosamente.");
                } else {
                    actToClose.markAsCompleted(date, comms);
                    JOptionPane.showMessageDialog(this, "Equipo individual reportado.");
                }
                refreshDynamicComponents();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error validando datos. Use YYYY-MM-DD.");
            }
        }
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(MAIN_FONT);
        table.setRowHeight(35);
        table.setShowGrid(false);
        JTableHeader th = table.getTableHeader();
        th.setFont(BOLD_FONT);
        th.setBackground(new Color(230, 230, 230));
        th.setForeground(new Color(60, 60, 60));
        return table;
    }

    // --- PESTAÑA 4: DASHBOARD ---
    private JPanel createDashboardPanel() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel kpiBox = new JPanel(new GridLayout(1, 2, 20, 20));
        kpiBox.setBackground(BG_COLOR);

        JPanel p1 = new JPanel(new BorderLayout()); p1.setBackground(PRIMARY_COLOR); p1.setBorder(new EmptyBorder(20, 20, 20, 20));
        lblCompliance = new JLabel("0%", SwingConstants.CENTER); lblCompliance.setFont(new Font("Segoe UI", Font.BOLD, 40)); lblCompliance.setForeground(Color.WHITE);
        JLabel l1 = new JLabel("Cumplimiento Tareas", SwingConstants.CENTER); l1.setFont(BOLD_FONT); l1.setForeground(Color.WHITE);
        p1.add(lblCompliance, BorderLayout.CENTER); p1.add(l1, BorderLayout.SOUTH);

        JPanel p2 = new JPanel(new BorderLayout()); p2.setBackground(SUCCESS_COLOR); p2.setBorder(new EmptyBorder(20, 20, 20, 20));
        lblTotalAct = new JLabel("0", SwingConstants.CENTER); lblTotalAct.setFont(new Font("Segoe UI", Font.BOLD, 40)); lblTotalAct.setForeground(Color.WHITE);
        JLabel l2 = new JLabel("Planes Aprobados", SwingConstants.CENTER); l2.setFont(BOLD_FONT); l2.setForeground(Color.WHITE);
        p2.add(lblTotalAct, BorderLayout.CENTER); p2.add(l2, BorderLayout.SOUTH);

        kpiBox.add(p1); kpiBox.add(p2);

        dashboardArea = new JTextArea(); dashboardArea.setEditable(false); dashboardArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        dashboardArea.setBorder(new EmptyBorder(15,15,15,15));
        
        main.add(createHeader("Estadísticas Macro", "Análisis volumétrico de los Planes."), BorderLayout.NORTH);
        main.add(kpiBox, BorderLayout.CENTER);
        main.add(new JScrollPane(dashboardArea), BorderLayout.SOUTH);
        main.getComponent(2).setPreferredSize(new Dimension(800, 200));

        return main;
    }

    private void refreshDynamicComponents() {
        // Tab 2 -> Combo Zonas
        Object selectedCbZone = cbxZones.getSelectedItem();
        cbxZones.removeAllItems();
        getUniqueZones().forEach(cbxZones::addItem);
        if (selectedCbZone != null && getUniqueZones().contains(selectedCbZone)) cbxZones.setSelectedItem(selectedCbZone);
        updateEqListForSelectedZone();

        // Tab 3 -> Planes Pendientes
        topPlanModel.setRowCount(0);
        List<MaintenancePlan> pendings = maintenanceService.getPendingPlans(allPlans);
        for (MaintenancePlan p : pendings) {
            topPlanModel.addRow(new Object[]{ p.getPlanId().substring(0,8), p.getZone(), p.getScheduledDate().toString(), p.getCreationComments() });
        }
        bottomActModel.setRowCount(0);

        // Tab 4 -> KPIs
        lblCompliance.setText(String.format("%.1f%%", maintenanceService.calculateCompliancePercentage(allPlans)));
        lblTotalAct.setText(String.valueOf(allPlans.size()));

        StringBuilder db = new StringBuilder("=== DESGLOSE RECIENTE POR PLAN ===\n\n");
        for (MaintenancePlan plan : allPlans) {
            db.append(String.format("PLAN [%s] | Zona: %s | F. Programada: %s\n", plan.getPlanId().substring(0,8), plan.getZone(), plan.getScheduledDate()));
            db.append(String.format("  * Comentarios al Crear: %s\n", plan.getCreationComments().isEmpty() ? "Ninguno" : plan.getCreationComments()));
            for (MaintenanceActivity act : plan.getActivities()) {
                db.append(String.format("    - Equipo: %-15s [%s] -> %s\n", 
                    act.getEquipment().getName(), act.isCompleted() ? "COMPLETADO" : "PENDIENTE", act.getReportComments()));
            }
            db.append("\n");
        }
        dashboardArea.setText(db.toString()); dashboardArea.setCaretPosition(0);
    }

    private static class EquipmentItem {
        private final Equipment equipment;
        public EquipmentItem(Equipment eq) { this.equipment = eq; }
        public Equipment getEquipment() { return equipment; }
        @Override public String toString() { return equipment.getName() + " (ID: " + equipment.getId() + ")"; }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored){}
        SwingUtilities.invokeLater(() -> new GuiApp().setVisible(true));
    }
}
