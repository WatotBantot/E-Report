package features.core.dashboardpanel.secretary;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import features.components.GlassPanel;

public class RecentReportsPanel extends GlassPanel {

    // ── Data ──────────────────────────────────────────────────────
    private final List<Object[]> allData = new ArrayList<>();
    private int currentPage = 0;
    private int rowsPerPage = 6;

    // ── UI ────────────────────────────────────────────────────────
    private DashboardTable table;
    private DefaultTableModel tableModel;
    private final String title;

    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageLabel;
    private JTextField jumpField;

    // ── Constructor ───────────────────────────────────────────────
    public RecentReportsPanel(String title, String[] columnNames) {
        super(new BorderLayout(0, 8));
        this.title = title;
        setBorder(BorderFactory.createEmptyBorder(15, 15, 12, 15));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        initializeUI(columnNames);
    }

    // ── UI Init ───────────────────────────────────────────────────
    private void initializeUI(String[] columnNames) {
        add(buildHeader(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // disable editor entirely — clicks handled by MouseListener
            }
        };

        table = new DashboardTable(columnNames);
        table.setModel(tableModel);
        table.setFillsViewportHeight(false);

        // ── Single MouseListener handles both cursor AND click ──
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(col == 6
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                table.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (col == 6 && row >= 0) {
                    onViewClicked(row);
                }
            }
        });

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
        add(buildPaginationBar(), BorderLayout.SOUTH);
    }

    // Override this in subclass or set via a callback
    private Consumer<Integer> viewClickCallback = null;

    public void setOnViewClicked(Consumer<Integer> callback) {
        this.viewClickCallback = callback;
    }

    private void onViewClicked(int visibleRow) {
        if (viewClickCallback != null)
            viewClickCallback.accept(getAbsoluteRowIndex(visibleRow));
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        header.add(titleLabel, BorderLayout.WEST);

        return header;
    }

    private JPanel buildPaginationBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setBackground(new Color(220, 220, 225));
        bar.add(separator, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        controls.setOpaque(false);

        pageLabel = new JLabel();
        pageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        pageLabel.setForeground(new Color(120, 120, 130));

        // Jump field
        jumpField = new JTextField(3) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 195, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        jumpField.setHorizontalAlignment(JTextField.CENTER);
        jumpField.setFont(new Font("Arial", Font.PLAIN, 12));
        jumpField.setForeground(new Color(180, 175, 195));
        jumpField.setBackground(new Color(248, 247, 252));
        jumpField.setOpaque(false);
        jumpField.setPreferredSize(new Dimension(44, 28));
        jumpField.setText("pg #");

        jumpField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (jumpField.getText().equals("pg #")) {
                    jumpField.setText("");
                    jumpField.setForeground(new Color(60, 60, 70));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (jumpField.getText().isBlank()) {
                    jumpField.setText("pg #");
                    jumpField.setForeground(new Color(180, 175, 195));
                }
            }
        });

        jumpField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    handleJump();
            }
        });

        JLabel jumpLabel = new JLabel("Go to:");
        jumpLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        jumpLabel.setForeground(new Color(120, 120, 130));

        JButton goButton = buildNavButton("Go");
        goButton.setPreferredSize(new Dimension(50, 28));
        goButton.addActionListener(e -> handleJump());

        prevButton = buildNavButton("‹ Prev");
        nextButton = buildNavButton("Next ›");
        prevButton.addActionListener(e -> goToPage(currentPage - 1));
        nextButton.addActionListener(e -> goToPage(currentPage + 1));

        controls.add(jumpLabel);
        controls.add(jumpField);
        controls.add(goButton);
        controls.add(Box.createHorizontalStrut(8));
        controls.add(pageLabel);
        controls.add(Box.createHorizontalStrut(4));
        controls.add(prevButton);
        controls.add(nextButton);

        bar.add(controls, BorderLayout.CENTER);
        updatePaginationControls();
        return bar;
    }

    private JButton buildNavButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(240, 240, 243));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(100, 150, 255, 230));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(110, 160, 255, 240));
                } else {
                    g2.setColor(new Color(90, 140, 255, 250));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(72, 28));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addPropertyChangeListener("enabled",
                evt -> btn.setForeground(btn.isEnabled() ? Color.WHITE : new Color(180, 180, 185)));

        return btn;
    }

    private void handleJump() {
        String text = jumpField.getText().trim();
        try {
            int requested = Integer.parseInt(text); // user types 1-based
            int target = requested - 1; // convert to 0-based
            if (target < 0 || target >= getTotalPages()) {
                jumpField.setBackground(new Color(255, 220, 220));
                Timer t = new Timer(350, e -> jumpField.setBackground(new Color(248, 247, 252)));
                t.setRepeats(false);
                t.start();
                return;
            }
            goToPage(target);
            jumpField.setText("pg #");
            jumpField.setForeground(new Color(180, 175, 195));
        } catch (NumberFormatException ex) {
            jumpField.setBackground(new Color(255, 220, 220));
            Timer t = new Timer(350, e -> jumpField.setBackground(new Color(248, 247, 252)));
            t.setRepeats(false);
            t.start();
        }
    }

    // ── Pagination Logic ──────────────────────────────────────────
    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) allData.size() / rowsPerPage));
    }

    private void goToPage(int page) {
        currentPage = Math.max(0, Math.min(page, getTotalPages() - 1));
        refreshPage();
    }

    /** Wipes the model and loads ONLY the rows that belong to currentPage. */
    private void refreshPage() {
        tableModel.setRowCount(0);

        int from = currentPage * rowsPerPage;
        int to = Math.min(from + rowsPerPage, allData.size());

        for (int i = from; i < to; i++)
            tableModel.addRow(allData.get(i));

        updatePaginationControls();
    }

    private void updatePaginationControls() {
        if (pageLabel == null)
            return;
        int total = getTotalPages();
        pageLabel.setText("Page " + (currentPage + 1) + " of " + total);
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < total - 1);
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Appends a row to the dataset.
     * Does NOT refresh the table mid-batch — call commitReports() when done.
     * For single additions outside a batch, use addReportAndRefresh().
     */
    public void addReport(Object[] reportData) {
        allData.add(reportData);
        refreshPage();
    }

    /**
     * Call this once after all addReport() calls in a batch
     * to render page 1 cleanly.
     */
    public void commitReports() {
        currentPage = 0;
        refreshPage();
    }

    public void clearReports() {
        allData.clear();
        currentPage = 0;
        refreshPage();
    }

    public DashboardTable getTable() {
        return table;
    }

    public void setRowsPerPage(int rows) {
        rowsPerPage = Math.max(1, rows);
        currentPage = 0;
        refreshPage();
    }

    /**
     * Converts a visible (page-relative) row index to the absolute
     * index in allData. Always use this in ButtonEditor callbacks.
     */
    public int getAbsoluteRowIndex(int visibleRow) {
        return currentPage * rowsPerPage + visibleRow;
    }

    public void setButtonColumn(int columnIndex, String buttonText, Color buttonColor) {
        table.getColumnModel().getColumn(columnIndex)
                .setCellRenderer(new ButtonRenderer(buttonText, buttonColor));
    }
}