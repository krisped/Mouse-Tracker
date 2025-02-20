package com.krisped;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class MTPanel extends PluginPanel
{
    private final JTextArea logArea;
    private final JCheckBox loggingCheckbox;
    private final JCheckBox timerLoggingCheckbox;
    private final JCheckBox spellsPlayersCheckbox;

    public MTPanel()
    {
        // Bruk standard PluginPanel-stil – ikke overstyrer borders eller insets
        setLayout(new BorderLayout());

        // Overskrift (legges øverst)
        JLabel header = new JLabel("MT Plugin");
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setForeground(ColorScheme.BRAND_ORANGE);
        add(header, BorderLayout.NORTH);

        // Tekstområde for logging – vi bruker intern padding via setMargin
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setMargin(new Insets(10, 10, 10, 10)); // intern padding

        // Scrollpane for logArea – lar PluginPanel og parent styre utseendet
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.setBorder(null);
        // Sett en tynn scrollbar via en egendefinert UI
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setUI(new ThinScrollBarUI());
        verticalBar.setPreferredSize(new Dimension(6, verticalBar.getPreferredSize().height));

        add(scrollPane, BorderLayout.CENTER);

        // Panel for checkbokser – la dette arve standard bakgrunn (transparent)
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setOpaque(false); // slik at bakgrunn fra PluginPanel vises
        loggingCheckbox = new JCheckBox("Aktiver item-click logging");
        timerLoggingCheckbox = new JCheckBox("Aktiver timer logging (ms)");
        spellsPlayersCheckbox = new JCheckBox("Aktiver spell-/player-logging");

        // Konfigurer checkboksene med ønsket font og farger – vi lar de beholde sin standard border
        loggingCheckbox.setFont(new Font("Arial", Font.BOLD, 14));
        loggingCheckbox.setForeground(Color.RED);
        loggingCheckbox.setOpaque(false);
        loggingCheckbox.addItemListener(e -> {
            loggingCheckbox.setForeground(loggingCheckbox.isSelected() ? Color.GREEN : Color.RED);
        });

        timerLoggingCheckbox.setFont(new Font("Arial", Font.BOLD, 14));
        timerLoggingCheckbox.setForeground(Color.RED);
        timerLoggingCheckbox.setOpaque(false);
        timerLoggingCheckbox.addItemListener(e -> {
            timerLoggingCheckbox.setForeground(timerLoggingCheckbox.isSelected() ? Color.GREEN : Color.RED);
        });

        spellsPlayersCheckbox.setFont(new Font("Arial", Font.BOLD, 14));
        spellsPlayersCheckbox.setForeground(Color.RED);
        spellsPlayersCheckbox.setOpaque(false);
        spellsPlayersCheckbox.addItemListener(e -> {
            spellsPlayersCheckbox.setForeground(spellsPlayersCheckbox.isSelected() ? Color.GREEN : Color.RED);
        });

        checkboxPanel.add(loggingCheckbox);
        checkboxPanel.add(timerLoggingCheckbox);
        checkboxPanel.add(spellsPlayersCheckbox);

        add(checkboxPanel, BorderLayout.SOUTH);
    }

    public void logClick(String message)
    {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public boolean isLoggingEnabled()
    {
        return loggingCheckbox.isSelected();
    }

    public boolean isTimerLoggingEnabled()
    {
        return timerLoggingCheckbox.isSelected();
    }

    public boolean isSpellsPlayersEnabled()
    {
        return spellsPlayersCheckbox.isSelected();
    }

    /**
     * En tynn ScrollBarUI for en moderne, flat scrollbar.
     */
    private static class ThinScrollBarUI extends BasicScrollBarUI
    {
        private static final int THUMB_WIDTH = 6;

        @Override
        protected Dimension getMinimumThumbSize() {
            return new Dimension(THUMB_WIDTH, THUMB_WIDTH);
        }

        @Override
        protected Dimension getMaximumThumbSize() {
            return new Dimension(THUMB_WIDTH, Integer.MAX_VALUE);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!c.isEnabled() || thumbBounds.height <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ColorScheme.BRAND_ORANGE);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, THUMB_WIDTH, thumbBounds.height, 6, 6);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(ColorScheme.DARK_GRAY_COLOR);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            Dimension zeroDim = new Dimension(0, 0);
            button.setPreferredSize(zeroDim);
            button.setMinimumSize(zeroDim);
            button.setMaximumSize(zeroDim);
            return button;
        }
    }
}
