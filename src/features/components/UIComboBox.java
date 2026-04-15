package features.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import config.UIConfig;

public class UIComboBox<E> extends JComboBox<E> {

    private int radius = 12;
    private boolean isHovered = false;

    public enum ValidationState {
        IDLE,
        INVALID,
        VALID
    }

    private ValidationState state = ValidationState.IDLE;

    public UIComboBox(E[] items) {
        super(items);

        setFont(UIConfig.BODY);
        setBackground(Color.WHITE);
        setForeground(UIConfig.TEXT_PRIMARY);
        setFocusable(false);
        setOpaque(false);
        
        setBorder(new EmptyBorder(6, 12, 6, 10));

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setBorder(new EmptyBorder(4, 8, 4, 8));
                label.setFont(UIConfig.BODY);
                label.setForeground(UIConfig.TEXT_PRIMARY);

                if (isSelected) {
                    label.setBackground(new Color(240, 240, 240));
                } else {
                    label.setBackground(Color.WHITE);
                }
                return label;
            }
        });

        // Clear error on valid selection
        addActionListener(e -> {
            if (!isInvalidSelection() && state == ValidationState.INVALID) {
                setValid();
            }
            repaint();
        });

        // Mouse listener for hover and click effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                // Show dropdown on click
                showPopup();
            }
            
            @Override
            public void mouseEntered(MouseEvent evt) {
                isHovered = true;
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                isHovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }
        });

        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = super.createArrowButton();
                btn.setContentAreaFilled(false);
                btn.setBorder(new EmptyBorder(0, 0, 0, 10));
                btn.setFocusPainted(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {}
        });
    }

    public void setError() {
        state = ValidationState.INVALID;
        repaint();
    }

    public void setValid() {
        state = ValidationState.VALID;
        repaint();
    }

    public void clearError() {
        state = ValidationState.IDLE;
        repaint();
    }

    public ValidationState getState() {
        return state;
    }

    public boolean isInvalidSelection() {
        Object value = getSelectedItem();
        return value == null ||
                value.toString().trim().isEmpty() ||
                value.toString().toLowerCase().startsWith("select");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // BACKGROUND - change on hover
        if (isHovered) {
            g2.setColor(new Color(250, 250, 250));
        } else {
            g2.setColor(getBackground());
        }
        g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);

        // BORDER
        Color borderColor;
        switch (state) {
            case INVALID -> borderColor = new Color(220, 60, 60);
            case VALID -> borderColor = new Color(0, 170, 80);
            default -> borderColor = isHovered ? new Color(180, 180, 180) : new Color(200, 200, 200);
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

        g2.dispose();

        super.paintComponent(g);
    }
}