package features.layout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import config.UIConfig;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.File;

public class DashboardPanel extends JPanel {
    private JPanel statsPanel;
    private JTable recentReportsTable;
    private DefaultTableModel tableModel;
    
    private String[] statIconPaths = UIConfig.STAT_ICON_PATHS;
    private String[] statLabels = UIConfig.STAT_LABEL_PATHS;
    
    
    private Color[] statColors = UIConfig.STAT_COLORS;
    
    private int[] statValues = {12, 12, 12, 12};
    private int statIconSize = 40;
    
    // Glass effect settings
    private float glassOpacity = 0.90f;
    private int cornerRadius = 15;
    
    public DashboardPanel() {
        setOpaque(false); // REMOVED GRAY BACKGROUND
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false); // REMOVED GRAY BACKGROUND
        
        statsPanel = createStatsPanel();
        wrapper.add(statsPanel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel tablePanel = createTablePanel();
        wrapper.add(tablePanel);
        wrapper.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel bottomPanel = createBottomPanel();
        wrapper.add(bottomPanel);
        
        JPanel panel = new JPanel();
        panel.add(wrapper);
        panel.setOpaque(false);        
        panel.setBorder(null);        
        add(panel, BorderLayout.CENTER);
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.setOpaque(false); // REMOVED GRAY BACKGROUND
        
        for (int i = 0; i < 4; i++) {
            JPanel card = createStatCard(statLabels[i], statValues[i], 
                statColors[i], statIconPaths[i]);
            panel.add(card);
        }
        
        return panel;
    }
    
    private JPanel createStatCard(String label, int value, Color bgColor, String iconPath) {
    // Glass morphism card
    JPanel card = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // Shadow
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fillRoundRect(2, 2, w - 2, h - 2, cornerRadius, cornerRadius);
            
            // Glass background with stat color
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, w - 2, h - 2, cornerRadius, cornerRadius);
            
            // Top shine
            GradientPaint shine = new GradientPaint(
                0, 0, new Color(255, 255, 255, 80),
                0, h / 2, new Color(255, 255, 255, 0)
            );
            g2.setPaint(shine);
            g2.fillRoundRect(0, 0, w - 2, h / 2, cornerRadius, cornerRadius);
            
            // Border
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 3, h - 3, cornerRadius, cornerRadius);
            
            g2.dispose();
        }
    };
    
    card.setOpaque(false);
    card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    card.setMaximumSize(new Dimension(250, 100));
    
    ImageIcon smoothIcon = tintIcon(loadAntiPixelatedImage(iconPath, statIconSize, statIconSize), new Color(255, 255, 255));
    JLabel iconLabel = new JLabel(smoothIcon != null ? smoothIcon : null);
    iconLabel.setPreferredSize(new Dimension(statIconSize, statIconSize));
    
    if (smoothIcon == null) {
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(255, 255, 255, 100));
    }
    
    // ADD SPACE: Empty border on right side of icon
    iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
    
    card.add(iconLabel, BorderLayout.WEST);
    
    JPanel textPanel = new JPanel(new GridLayout(2, 1));
    textPanel.setOpaque(false);
    
    // ADD SPACE: Padding on left side of text panel
    textPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    
    JLabel valueLabel = new JLabel(String.valueOf(value));
    valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
    valueLabel.setForeground(Color.WHITE);
    
    JLabel titleLabel = new JLabel(label);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
    titleLabel.setForeground(Color.WHITE);
    
    textPanel.add(valueLabel);
    textPanel.add(titleLabel);
    card.add(textPanel, BorderLayout.CENTER);
    
    return card;
}
    
    private JPanel createTablePanel() {
        // Glass morphism table panel
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(2, 2, w - 2, h - 2, cornerRadius, cornerRadius);
                
                // White glass background
                g2.setColor(new Color(255, 255, 255, (int)(255 * glassOpacity)));
                g2.fillRoundRect(0, 0, w - 2, h - 2, cornerRadius, cornerRadius);
                
                // Border
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 3, h - 3, cornerRadius, cornerRadius);
                
                g2.dispose();
            }
        };
        
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        
        JLabel title = new JLabel("Recent Reports");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(50, 50, 50));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
        
        String[] columns = {"Report ID", "Category", "Purok", "Date Submitted", 
                          "Last Update", "Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0);
        tableModel.addRow(new Object[]{"001", "Theft", "Purok 1", "01/01/2026", 
                                     "01/01/2026", "Submitted", "View"});
        
        recentReportsTable = new JTable(tableModel);
        recentReportsTable.setRowHeight(40);
        recentReportsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        recentReportsTable.setOpaque(false); // Table transparent
        recentReportsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        recentReportsTable.getTableHeader().setBackground(new Color(240, 240, 240, 200));
        recentReportsTable.getTableHeader().setOpaque(false);
        
        recentReportsTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        recentReportsTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        
        JScrollPane tableScroll = new JScrollPane(recentReportsTable);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(tableScroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false); // REMOVED GRAY BACKGROUND
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        
        JPanel activitiesPanel = createInfoPanel("Recent Activities", new String[]{
            "Report Status Updated - You updated Report #180 (ID: C180) to In Progress",            
        });
        panel.add(activitiesPanel);
        
        JPanel tasksPanel = createInfoPanel("Tasks", new String[]{
            "Review - Review new Reports",
            "Update - Update status of Ongoing cases",
            "Add - Add walk-in complaints"
        });
        panel.add(tasksPanel);
        
        return panel;
    }
    
    private JPanel createInfoPanel(String title, String[] items) {
        // Glass morphism info panel
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(2, 2, w - 2, h - 2, cornerRadius, cornerRadius);
                
                // White glass background
                g2.setColor(new Color(255, 255, 255, (int)(255 * glassOpacity)));
                g2.fillRoundRect(0, 0, w - 2, h - 2, cornerRadius, cornerRadius);
                
                // Border
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 3, h - 3, cornerRadius, cornerRadius);
                
                g2.dispose();
            }
        };
        
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        
        for (String item : items) {
            JLabel itemLabel = new JLabel("<html>• " + item + "</html>");
            itemLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            itemLabel.setForeground(new Color(60, 60, 60));
            itemLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            itemsPanel.add(itemLabel);
        }
        
        panel.add(itemsPanel, BorderLayout.CENTER);
        return panel;
    }
    
    // Image loading methods remain the same...
    private ImageIcon loadAntiPixelatedImage(String path, int targetW, int targetH) {
        try {
            BufferedImage original = ImageIO.read(new File(path));
            if (original == null) return null;
            return new ImageIcon(smoothScale(original, targetW, targetH));
        } catch (Exception e) {
            return null;
        }
    }
    
    private BufferedImage smoothScale(BufferedImage src, int w, int h) {
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return scaled;
    }
    
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(Color.WHITE);
            setBackground(new Color(120, 100, 200));
            setFont(new Font("Arial", Font.BOLD, 12));
            setText("View");
            setBorderPainted(false);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }


    private ImageIcon tintIcon(ImageIcon icon, Color color) {
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();

    BufferedImage tinted = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = tinted.createGraphics();

    // Draw original image
    g2.drawImage(icon.getImage(), 0, 0, null);

    // Apply color tint
    g2.setComposite(AlphaComposite.SrcAtop);
    g2.setColor(color);
    g2.fillRect(0, 0, w, h);

    g2.dispose();
    return new ImageIcon(tinted);
}

class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            button = new JButton("View");
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setBorderPainted(false);

            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            return button;
        }

        public Object getCellEditorValue() {
            String reportId = tableModel.getValueAt(row, 0).toString();
            JOptionPane.showMessageDialog(null, "View clicked: " + reportId);
            return "View";
        }
    }
}