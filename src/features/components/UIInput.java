package features.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import config.UIConfig;
import services.middleware.UIValidator;

public class UIInput extends JTextField {
    private String placeholder;
    private ImageIcon icon;
    private int cornerRadius = 15;
    
    private UIValidator.FieldType fieldType = UIValidator.FieldType.TEXT;

    public enum ValidationState { IDLE, INVALID, VALID }
    private ValidationState state = ValidationState.IDLE;

    public UIInput(int columns) {
        super(columns);
        setOpaque(false);
        setFont(UIConfig.BODY);
        setBackground(Color.WHITE);
        setForeground(UIConfig.TEXT_PRIMARY);
        setCaretColor(UIConfig.PRIMARY);
        applyPadding(12);

        getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateLive(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateLive(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateLive(); }
        });
    }

    public UIInput(int columns, String iconPath) {
        this(columns);
        if (iconPath != null) {
            this.icon = new ImageIcon(new ImageIcon(iconPath).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            applyPadding(36);
        }
    }

    // --- CRITICAL METHODS ---

    public UIValidator.FieldType getFieldType() { return fieldType; }
    public void setFieldType(UIValidator.FieldType type) { this.fieldType = type; }

    public void setLimit(int max, boolean numericOnly) {
        ((AbstractDocument) getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                if (numericOnly && !text.matches("\\d*")) return;
                int currentLength = fb.getDocument().getLength();
                if ((currentLength + text.length() - length) <= max) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private void validateLive() {
        String value = getValue();
        if (value.isEmpty()) { clearError(); return; }
        if (UIValidator.isValidField(this.fieldType, value)) setValid();
        else setError();
    }

    public String getValue() { return getText() == null ? "" : getText().trim(); }
    public void setError() { state = ValidationState.INVALID; repaint(); }
    public void setValid() { state = ValidationState.VALID; repaint(); }
    public void clearError() { state = ValidationState.IDLE; repaint(); }
    public void setPlaceholder(String p) { this.placeholder = p; repaint(); }
    private void applyPadding(int left) { setBorder(new EmptyBorder(8, left, 8, 12)); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, cornerRadius, cornerRadius);

        Color border = switch (state) {
            case INVALID -> new Color(220, 60, 60);
            case VALID -> new Color(0, 170, 80);
            default -> new Color(220, 220, 220);
        };

        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, cornerRadius, cornerRadius);

        if (icon != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2.drawImage(icon.getImage(), 10, (getHeight()-18)/2, 18, 18, null);
        }

        super.paintComponent(g);
        if (placeholder != null && getText().isEmpty()) {
            g2.setColor(new Color(160, 160, 160));
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, getInsets().left, y);
        }
        g2.dispose();
    }
}