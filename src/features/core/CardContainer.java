package features.core;

import javax.swing.*;
import java.awt.*;

/**
 * A reusable card-based container for multi-step forms.
 * Manages CardLayout and provides navigation methods.
 * Useful for registration wizards, settings pages, etc.
 */
public class CardContainer extends JPanel {
    private CardLayout cardLayout;
    private JPanel container;
    
    public CardContainer() {
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.setOpaque(false);
        setLayout(new BorderLayout());
        add(container, BorderLayout.CENTER);
        setOpaque(false);
    }
    
    /**
     * Adds a panel with a name for navigation.
     */
    public void addCard(JPanel panel, String name) {
        panel.setOpaque(false);
        container.add(panel, name);
    }
    
    /**
     * Shows the card with the given name.
     */
    public void showCard(String name) {
        cardLayout.show(container, name);
    }
    
    /**
     * Gets the next card name (for wizard-style navigation).
     */
    public String getCurrentCard() {
        // CardLayout doesn't expose current card, so track externally if needed
        return null;
    }
    
    public CardLayout getCardLayout() {
        return cardLayout;
    }
}