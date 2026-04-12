package features.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import config.UIConfig;
import services.middleware.UIValidator;

public class UIPasswordInput extends JPasswordField {

    private String placeholder;
    private ImageIcon lockIcon, eyeOnIcon, eyeOffIcon;
    private UIPasswordInput referenceField;
    private boolean isPasswordVisible = false;
    private int cornerRadius = 15;
    private int paddingLeft = 12, paddingRight = 12, paddingTop = 12, paddingBottom = 12;

    private UIValidator.FieldType fieldType = UIValidator.FieldType.TEXT;

    public enum ValidationState {
        IDLE, INVALID, VALID
    }

    private ValidationState state = ValidationState.IDLE;

    public UIPasswordInput(int columns) {
        super(columns);
        setFont(UIConfig.BODY);
        setBackground(Color.WHITE);
        setForeground(UIConfig.TEXT_PRIMARY);
        setCaretColor(UIConfig.PRIMARY);
        setOpaque(false);
        applyPadding();

        try {
            this.lockIcon = scaleIcon(UIConfig.LOCK_ICON_PATH);
            this.eyeOnIcon = scaleIcon(UIConfig.EYE_ICON_PATH);
            this.eyeOffIcon = scaleIcon(UIConfig.EYE_OFF_ICON_PATH);
            paddingLeft = 36;
            paddingRight = 36;
            applyPadding();
        } catch (Exception e) {
        }

        getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateLive();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateLive();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateLive();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getX() > getWidth() - 40)
                    togglePasswordVisibility();
            }
        });
    }

    public void setMatchTarget(UIPasswordInput other) {
        this.referenceField = other;
    }

    private void validateLive() {
        String value = getValue();
        if (value.isEmpty()) {
            clearError();
            return;
        }
        boolean isValid = UIValidator.isValidField(this.fieldType, value);
        if (isValid)
            setValid();
        else
            setError();

        // If this is a confirmation field, check if it matches the primary
        if (referenceField != null) {
            if (value.equals(referenceField.getValue())) {
                setValid();
            } else {
                setError();
            }
        } else {
            // Normal password validation (e.g., length > 0)
            setValid();
        }
    }

    private void applyPadding() {
        setBorder(new EmptyBorder(paddingTop, paddingLeft, paddingBottom, paddingRight));
    }

    private ImageIcon scaleIcon(String path) {
        if (path == null)
            return null;
        return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        setEchoChar(isPasswordVisible ? (char) 0 : '\u2022');
        repaint();
    }

    public void setFieldType(UIValidator.FieldType type) {
        this.fieldType = type;
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

    public String getValue() {
        return new String(getPassword()).trim();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        Color borderColor = switch (state) {
            case INVALID -> new Color(220, 60, 60);
            case VALID -> new Color(0, 170, 80);
            default -> new Color(220, 220, 220);
        };

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        if (lockIcon != null) {
            int y = (h - 18) / 2;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.drawImage(lockIcon.getImage(), 10, y, 18, 18, null);
        }

        if (eyeOnIcon != null) {
            int y = (h - 18) / 2;
            Image eye = isPasswordVisible ? eyeOffIcon.getImage() : eyeOnIcon.getImage();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.drawImage(eye, w - 28, y, 18, 18, null);
        }

        super.paintComponent(g);

        if (placeholder != null && getPassword().length == 0) {
            g2.setColor(new Color(160, 160, 160));
            FontMetrics fm = g2.getFontMetrics();
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, getInsets().left, y);
        }
        g2.dispose();
    }
}