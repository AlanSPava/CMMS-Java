package com.industrial.cmms.operativo.ui;

import com.industrial.cmms.operativo.model.ActivityType;
import com.industrial.cmms.operativo.model.MaintenanceReport;
import com.industrial.cmms.operativo.service.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Interfaz Gráfica principal para el sistema de Reportes Operativos de Mantenimiento.
 * Implementa Flat Design con javax.swing puro — sin librerías externas.
 *
 * Arquitectura: 3 pestañas en un JTabbedPane
 *   1. Registro de nuevos reportes
 *   2. Dashboard Analítico (métricas calculadas con Streams en ReportService)
 *   3. Histórico completo con JTable estilizado
 */
public class ReportGuiApp extends JFrame {

    // ───── Capa de Servicio ─────
    private final ReportService reportService;

    // ───── Paleta de Colores (Flat Design) ─────
    private static final Color BG_COLOR         = new Color(245, 246, 250);   // Fondo general gris ultraligero
    private static final Color PANEL_COLOR      = Color.WHITE;                // Fondo de paneles internos
    private static final Color TEXT_BTN_COLOR   = new Color(0, 35, 102);      // Azul Rey Oscuro (textos de botones)
    private static final Color PRIMARY_BLUE     = new Color(20, 110, 220);    // Azul primario para acentos
    private static final Color SUCCESS_GREEN    = new Color(46, 204, 113);    // Verde éxito
    private static final Color ACCENT_AMBER     = new Color(243, 156, 18);    // Ámbar acento
    private static final Color DANGER_RED       = new Color(231, 76, 60);     // Rojo peligro
    private static final Color DARK_TEXT        = new Color(44, 62, 80);      // Texto oscuro principal
    private static final Color SUBTITLE_GRAY   = new Color(127, 140, 141);   // Subtítulos
    private static final Color HEADER_BG       = new Color(236, 240, 245);   // Fondo de headers de tabla
    private static final Color ROW_ALT         = new Color(248, 249, 252);   // Fila alternada en tabla
    private static final Color BORDER_LIGHT    = new Color(220, 224, 230);   // Borde de inputs
    private static final Color TAB_INDICATOR   = new Color(0, 35, 102);      // Indicador de tab activo

    // ───── Tipografía ─────
    private static final Font FONT_REGULAR     = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD        = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_KPI_VALUE   = new Font("Segoe UI", Font.BOLD, 42);
    private static final Font FONT_KPI_LABEL   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BTN         = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_TABLE       = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TABLE_HEAD  = new Font("Segoe UI", Font.BOLD, 13);

    // ───── Componentes del Dashboard (se refrescan dinámicamente) ─────
    private JLabel lblTotalHoras;
    private JPanel panelActividadCards;
    private JPanel panelTurnoCards;

    // ───── Componente del Histórico ─────
    private DefaultTableModel tableModel;
    private JTable historyTable;

    // ───── Componentes del Formulario (para limpiar tras guardar) ─────
    private JTextField txtZona, txtEquipo, txtTecnico, txtHoras, txtFecha;
    private JComboBox<String> cbxTurno;
    private JComboBox<String> cbxTipoActividad;
    private JTextArea txtComentarios;

    // ══════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════

    public ReportGuiApp() {
        // Configuración global de UIManager para neutralizar bordes nativos
        configurarLookAndFeel();

        reportService = new ReportService();

        setTitle("CMMS Operativo — Reportes de Mantenimiento Industrial");
        setSize(1050, 740);
        setMinimumSize(new Dimension(900, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        // ── JTabbedPane principal ──
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_BOLD);
        tabbedPane.setBackground(BG_COLOR);
        tabbedPane.setBorder(new EmptyBorder(8, 8, 8, 8));
        tabbedPane.setFocusable(false);

        tabbedPane.addTab("  \uD83D\uDCCB  Registro  ", crearPanelRegistro());
        tabbedPane.addTab("  \uD83D\uDCCA  Dashboard Analítico  ", crearPanelDashboard());
        tabbedPane.addTab("  \uD83D\uDDC2  Histórico  ", crearPanelHistorico());

        // Refrescar datos del Dashboard y Tabla al cambiar de pestaña
        tabbedPane.addChangeListener(e -> {
            refrescarDashboard();
            refrescarTablaHistorica();
        });

        add(tabbedPane);

        // Carga inicial
        refrescarDashboard();
        refrescarTablaHistorica();
    }

    /**
     * Configura UIManager para lograr un estilo Flat limpio
     * eliminando bordes nativos y estandarizando fuentes.
     */
    private void configurarLookAndFeel() {
        try {
            // Usar el look and feel del sistema como base
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("TabbedPane.font", FONT_BOLD);
        UIManager.put("TabbedPane.background", BG_COLOR);
        UIManager.put("TabbedPane.selected", PANEL_COLOR);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(4, 8, 0, 8));
        UIManager.put("OptionPane.messageFont", FONT_REGULAR);
        UIManager.put("OptionPane.buttonFont", FONT_BOLD);
    }

    // ══════════════════════════════════════════════════════════════════
    //  UTILIDADES DE COMPONENTES (Flat Design)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Crea un botón estilizado con fondo plano y texto en Azul Rey Oscuro.
     */
    private JButton crearBotonFlat(String texto, Color bgColor) {
        JButton btn = new JButton(texto);
        btn.setFont(FONT_BTN);
        btn.setBackground(bgColor);
        btn.setForeground(TEXT_BTN_COLOR); // Azul Rey Oscuro según requerimiento
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));

        // Efecto hover sutil
        Color originalBg = bgColor;
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(originalBg.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalBg);
            }
        });

        return btn;
    }

    /**
     * Crea un JTextField con estilo flat: borde ligero y padding interno.
     */
    private JTextField crearCampoTexto() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        tf.setBackground(Color.WHITE);
        return tf;
    }

    /**
     * Crea una etiqueta de campo de formulario.
     */
    private JLabel crearEtiquetaCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(DARK_TEXT);
        return lbl;
    }

    /**
     * Crea un panel de encabezado con título y subtítulo.
     */
    private JPanel crearEncabezado(String titulo, String subtitulo) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(PANEL_COLOR);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(DARK_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(FONT_SUBTITLE);
        lblSub.setForeground(SUBTITLE_GRAY);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSub.setBorder(new EmptyBorder(4, 0, 0, 0));

        header.add(lblTitle);
        header.add(lblSub);
        return header;
    }

    /**
     * Crea una tarjeta KPI con un valor grande y una etiqueta descriptiva.
     */
    private JPanel crearTarjetaKPI(String valorTexto, String etiqueta, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel lblVal = new JLabel(valorTexto, SwingConstants.CENTER);
        lblVal.setFont(FONT_KPI_VALUE);
        lblVal.setForeground(Color.WHITE);

        JLabel lblEtiq = new JLabel(etiqueta, SwingConstants.CENTER);
        lblEtiq.setFont(FONT_KPI_LABEL);
        lblEtiq.setForeground(new Color(255, 255, 255, 210));

        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblEtiq, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Crea una mini-tarjeta informativa para contadores (Tipo de Actividad / Turno).
     */
    private JPanel crearMiniTarjeta(String etiqueta, String valor, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(PANEL_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblName = new JLabel(etiqueta);
        lblName.setFont(FONT_REGULAR);
        lblName.setForeground(DARK_TEXT);

        JLabel lblVal = new JLabel(valor);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(accentColor);
        lblVal.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(lblName, BorderLayout.CENTER);
        card.add(lblVal, BorderLayout.EAST);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PESTAÑA 1: REGISTRO DE REPORTES
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelRegistro() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Contenedor de formulario con fondo blanco
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(PANEL_COLOR);
        formCard.setBorder(new EmptyBorder(30, 35, 30, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // ─── Campos del formulario ───

        txtZona = crearCampoTexto();
        txtEquipo = crearCampoTexto();
        txtTecnico = crearCampoTexto();
        txtHoras = crearCampoTexto();
        txtFecha = crearCampoTexto();
        txtFecha.setText(LocalDate.now().toString());

        // JComboBox de Turno
        cbxTurno = new JComboBox<>(new String[]{"1", "2", "3"});
        cbxTurno.setFont(FONT_REGULAR);
        cbxTurno.setBackground(Color.WHITE);
        cbxTurno.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        // JComboBox de Tipo de Actividad
        String[] tiposActividad = new String[ActivityType.values().length];
        for (int i = 0; i < ActivityType.values().length; i++) {
            tiposActividad[i] = ActivityType.values()[i].getDescription();
        }
        cbxTipoActividad = new JComboBox<>(tiposActividad);
        cbxTipoActividad.setFont(FONT_REGULAR);
        cbxTipoActividad.setBackground(Color.WHITE);
        cbxTipoActividad.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        // JTextArea de comentarios
        txtComentarios = new JTextArea(3, 30);
        txtComentarios.setFont(FONT_REGULAR);
        txtComentarios.setLineWrap(true);
        txtComentarios.setWrapStyleWord(true);
        txtComentarios.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        JScrollPane scrollComentarios = new JScrollPane(txtComentarios);
        scrollComentarios.setBorder(BorderFactory.createEmptyBorder());

        // Disposición del formulario (2 columnas: etiqueta + campo)
        String[] etiquetas = {"Zona:", "Equipo Intervenido:", "Técnico / Persona a cargo:",
                              "Tiempo Total (horas):", "Fecha (YYYY-MM-DD):", "Turno Operativo:",
                              "Tipo de Actividad:", "Comentarios:"};

        JComponent[] campos = {txtZona, txtEquipo, txtTecnico, txtHoras, txtFecha,
                               cbxTurno, cbxTipoActividad, scrollComentarios};

        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            formCard.add(crearEtiquetaCampo(etiquetas[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            formCard.add(campos[i], gbc);
        }

        // Botón Guardar
        JButton btnGuardar = crearBotonFlat("  Guardar Reporte  ", new Color(46, 204, 113));
        btnGuardar.addActionListener(e -> accionGuardarReporte());

        // Botón Limpiar
        JButton btnLimpiar = crearBotonFlat("  Limpiar Campos  ", new Color(189, 195, 199));
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        botonesPanel.setBackground(PANEL_COLOR);
        botonesPanel.add(btnLimpiar);
        botonesPanel.add(btnGuardar);

        gbc.gridx = 0;
        gbc.gridy = etiquetas.length;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(24, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formCard.add(botonesPanel, gbc);

        main.add(crearEncabezado("Registro de Actividad",
                "Capture los detalles de cada actividad de mantenimiento realizada en planta."), BorderLayout.NORTH);
        main.add(formCard, BorderLayout.CENTER);

        return main;
    }

    /**
     * Lógica de validación y guardado del reporte.
     */
    private void accionGuardarReporte() {
        // Validar campos obligatorios no vacíos
        if (txtZona.getText().trim().isEmpty() || txtEquipo.getText().trim().isEmpty()
                || txtTecnico.getText().trim().isEmpty() || txtHoras.getText().trim().isEmpty()
                || txtFecha.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Todos los campos son obligatorios.\nVerifique que no haya campos vacíos.",
                    "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar horas numéricas
        double horas;
        try {
            horas = Double.parseDouble(txtHoras.getText().trim());
            if (horas < 0) {
                JOptionPane.showMessageDialog(this,
                        "El tiempo total invertido no puede ser negativo.",
                        "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "El campo de horas debe contener un valor numérico válido.\nEjemplo: 2.5",
                    "Error de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar fecha
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(txtFecha.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "El formato de fecha debe ser YYYY-MM-DD.\nEjemplo: 2026-04-27",
                    "Error de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Extraer turno del ComboBox (siempre válido: 1, 2 o 3)
        int turno = Integer.parseInt((String) cbxTurno.getSelectedItem());

        // Obtener el ActivityType correspondiente al índice seleccionado
        ActivityType tipoSeleccionado = ActivityType.values()[cbxTipoActividad.getSelectedIndex()];

        // Crear el reporte (las validaciones de negocio se hacen en el constructor)
        try {
            MaintenanceReport reporte = new MaintenanceReport(
                    txtZona.getText().trim(),
                    txtEquipo.getText().trim(),
                    txtTecnico.getText().trim(),
                    horas,
                    fecha,
                    turno,
                    txtComentarios.getText().trim(),
                    tipoSeleccionado
            );

            reportService.addReport(reporte);

            JOptionPane.showMessageDialog(this,
                    "✓ Reporte registrado y guardado exitosamente.\n\n"
                            + "Tipo: " + tipoSeleccionado.getDescription() + "\n"
                            + "Zona: " + txtZona.getText().trim() + "\n"
                            + "Equipo: " + txtEquipo.getText().trim(),
                    "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            refrescarDashboard();
            refrescarTablaHistorica();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error de validación del modelo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        txtZona.setText("");
        txtEquipo.setText("");
        txtTecnico.setText("");
        txtHoras.setText("");
        txtFecha.setText(LocalDate.now().toString());
        cbxTurno.setSelectedIndex(0);
        cbxTipoActividad.setSelectedIndex(0);
        txtComentarios.setText("");
    }

    // ══════════════════════════════════════════════════════════════════
    //  PESTAÑA 2: DASHBOARD ANALÍTICO
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelDashboard() {
        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ── Encabezado ──
        JPanel encabezado = crearEncabezado("Dashboard Analítico",
                "Métricas clave calculadas en tiempo real a partir de los reportes persistidos.");
        encabezado.setBackground(BG_COLOR);

        // ── Tarjeta KPI principal: Total de Horas ──
        JPanel kpiPanel = new JPanel(new GridLayout(1, 1));
        kpiPanel.setBackground(BG_COLOR);
        kpiPanel.setBorder(new EmptyBorder(0, 0, 6, 0));

        lblTotalHoras = new JLabel("0.00", SwingConstants.CENTER);
        lblTotalHoras.setFont(FONT_KPI_VALUE);
        lblTotalHoras.setForeground(Color.WHITE);

        JPanel tarjetaHoras = new JPanel(new BorderLayout());
        tarjetaHoras.setBackground(PRIMARY_BLUE);
        tarjetaHoras.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel lblHorasTitle = new JLabel("Total de Horas Invertidas en Planta", SwingConstants.CENTER);
        lblHorasTitle.setFont(FONT_KPI_LABEL);
        lblHorasTitle.setForeground(new Color(255, 255, 255, 210));

        tarjetaHoras.add(lblTotalHoras, BorderLayout.CENTER);
        tarjetaHoras.add(lblHorasTitle, BorderLayout.SOUTH);
        kpiPanel.add(tarjetaHoras);

        // ── Panel central dividido: Actividades por Tipo + Productividad por Turno ──
        JPanel centroPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        centroPanel.setBackground(BG_COLOR);

        // Columna izquierda: Conteo por Tipo de Actividad
        JPanel columnaActividades = new JPanel(new BorderLayout(0, 10));
        columnaActividades.setBackground(PANEL_COLOR);
        columnaActividades.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel lblTituloAct = new JLabel("Repeticiones por Tipo de Actividad");
        lblTituloAct.setFont(FONT_BOLD);
        lblTituloAct.setForeground(DARK_TEXT);
        lblTituloAct.setBorder(new EmptyBorder(0, 0, 8, 0));
        columnaActividades.add(lblTituloAct, BorderLayout.NORTH);

        panelActividadCards = new JPanel();
        panelActividadCards.setLayout(new BoxLayout(panelActividadCards, BoxLayout.Y_AXIS));
        panelActividadCards.setBackground(PANEL_COLOR);
        JScrollPane scrollAct = new JScrollPane(panelActividadCards);
        scrollAct.setBorder(BorderFactory.createEmptyBorder());
        scrollAct.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        columnaActividades.add(scrollAct, BorderLayout.CENTER);

        // Columna derecha: Productividad por Turno
        JPanel columnaTurnos = new JPanel(new BorderLayout(0, 10));
        columnaTurnos.setBackground(PANEL_COLOR);
        columnaTurnos.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel lblTituloTurno = new JLabel("Productividad Indexada por Turno");
        lblTituloTurno.setFont(FONT_BOLD);
        lblTituloTurno.setForeground(DARK_TEXT);
        lblTituloTurno.setBorder(new EmptyBorder(0, 0, 8, 0));
        columnaTurnos.add(lblTituloTurno, BorderLayout.NORTH);

        panelTurnoCards = new JPanel();
        panelTurnoCards.setLayout(new BoxLayout(panelTurnoCards, BoxLayout.Y_AXIS));
        panelTurnoCards.setBackground(PANEL_COLOR);
        JScrollPane scrollTurnos = new JScrollPane(panelTurnoCards);
        scrollTurnos.setBorder(BorderFactory.createEmptyBorder());
        scrollTurnos.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        columnaTurnos.add(scrollTurnos, BorderLayout.CENTER);

        centroPanel.add(columnaActividades);
        centroPanel.add(columnaTurnos);

        // ── Ensamblado ──
        JPanel topSection = new JPanel(new BorderLayout(0, 12));
        topSection.setBackground(BG_COLOR);
        topSection.add(encabezado, BorderLayout.NORTH);
        topSection.add(kpiPanel, BorderLayout.SOUTH);

        main.add(topSection, BorderLayout.NORTH);
        main.add(centroPanel, BorderLayout.CENTER);

        return main;
    }

    /**
     * Refresca todas las métricas del Dashboard consultando el ReportService.
     * Los cálculos en ReportService usan Java Streams API.
     */
    private void refrescarDashboard() {
        if (lblTotalHoras == null) return; // Todavía no inicializado

        // KPI: Total de horas
        double totalHoras = reportService.calculateTotalHours();
        lblTotalHoras.setText(String.format("%.2f h", totalHoras));

        // Conteo por Tipo de Actividad
        Map<ActivityType, Long> porTipo = reportService.countActivitiesByType();
        panelActividadCards.removeAll();

        Color[] coloresActividad = {PRIMARY_BLUE, SUCCESS_GREEN, ACCENT_AMBER, DANGER_RED, new Color(155, 89, 182)};
        ActivityType[] todosLosTipos = ActivityType.values();

        for (int i = 0; i < todosLosTipos.length; i++) {
            long conteo = porTipo.getOrDefault(todosLosTipos[i], 0L);
            JPanel miniCard = crearMiniTarjeta(
                    todosLosTipos[i].getDescription(),
                    String.valueOf(conteo),
                    coloresActividad[i % coloresActividad.length]
            );
            miniCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            panelActividadCards.add(miniCard);
            panelActividadCards.add(Box.createVerticalStrut(8));
        }

        // Productividad por Turno
        Map<Integer, Long> porTurno = reportService.countReportsByShift();
        panelTurnoCards.removeAll();

        String[] nombresTurno = {"Turno 1 — Matutino", "Turno 2 — Vespertino", "Turno 3 — Nocturno"};
        Color[] coloresTurno = {new Color(52, 152, 219), new Color(230, 126, 34), new Color(142, 68, 173)};

        for (int t = 1; t <= 3; t++) {
            long count = porTurno.getOrDefault(t, 0L);
            JPanel turnoCard = crearMiniTarjeta(
                    nombresTurno[t - 1],
                    count + " reportes",
                    coloresTurno[t - 1]
            );
            turnoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            panelTurnoCards.add(turnoCard);
            panelTurnoCards.add(Box.createVerticalStrut(8));
        }

        panelActividadCards.revalidate();
        panelActividadCards.repaint();
        panelTurnoCards.revalidate();
        panelTurnoCards.repaint();
    }

    // ══════════════════════════════════════════════════════════════════
    //  PESTAÑA 3: HISTÓRICO (JTable sin bordes cuadriculados)
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelHistorico() {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Columnas de la tabla
        String[] columnas = {"Fecha", "Turno", "Zona", "Equipo", "Técnico",
                             "Horas", "Tipo de Actividad", "Comentarios"};

        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(FONT_TABLE);
        historyTable.setRowHeight(38);
        historyTable.setShowGrid(false);                    // Sin bordes cuadriculados
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        historyTable.setSelectionBackground(new Color(220, 235, 252));
        historyTable.setSelectionForeground(DARK_TEXT);
        historyTable.setFillsViewportHeight(true);
        historyTable.setBackground(PANEL_COLOR);

        // Header de la tabla
        JTableHeader header = historyTable.getTableHeader();
        header.setFont(FONT_TABLE_HEAD);
        header.setBackground(HEADER_BG);
        header.setForeground(DARK_TEXT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_LIGHT));
        header.setReorderingAllowed(false);

        // Renderizador personalizado para filas alternadas (zebra-striping)
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? PANEL_COLOR : ROW_ALT);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Ancho preferido de columnas
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // Fecha
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(55);   // Turno
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Zona
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(130);  // Equipo
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(130);  // Técnico
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(60);   // Horas
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(165);  // Tipo
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(200);  // Comentarios

        JScrollPane scrollTabla = new JScrollPane(historyTable);
        scrollTabla.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollTabla.getViewport().setBackground(PANEL_COLOR);

        main.add(crearEncabezado("Histórico de Actividades",
                "Visualización tabular de todos los reportes de mantenimiento registrados."), BorderLayout.NORTH);
        main.add(scrollTabla, BorderLayout.CENTER);

        return main;
    }

    /**
     * Refresca los datos de la tabla histórica desde el servicio.
     */
    private void refrescarTablaHistorica() {
        if (tableModel == null) return;

        tableModel.setRowCount(0);

        List<MaintenanceReport> reportes = reportService.getAllReports();
        for (MaintenanceReport r : reportes) {
            tableModel.addRow(new Object[]{
                    r.getDate().toString(),
                    "Turno " + r.getShift(),
                    r.getZone(),
                    r.getEquipment(),
                    r.getTechnicianName(),
                    String.format("%.1f", r.getTotalHours()),
                    r.getActivityType().getDescription(),
                    r.getComments()
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA
    // ══════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ReportGuiApp app = new ReportGuiApp();
            app.setVisible(true);
        });
    }
}
