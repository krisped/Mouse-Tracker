package com.krisped;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;

/**
 * Plugin-panel med tekstlogg og checkbokser, samt en knapp for å tømme loggen.
 * Her bruker vi RuneLites standard scrollbar.
 */
public class MTPanel extends PluginPanel
{
    private final JTextArea logArea;

    private final JCheckBox loggingCheckbox;
    private final JCheckBox timerLoggingCheckbox;
    private final JCheckBox spellsPlayersCheckbox;
    private final JCheckBox varbitsCheckbox;
    private final JCheckBox varplayersCheckbox;
    private final JCheckBox opcodeCheckbox;
    private final JCheckBox npcsCheckbox;

    public MTPanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Overskrift
        JLabel header = new JLabel("MT Plugin - Super Logging");
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setForeground(ColorScheme.BRAND_ORANGE);
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(header, BorderLayout.NORTH);

        // Tekstområde for logging
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        logArea.setForeground(Color.WHITE);
        logArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.setBorder(null); // Ingen kant/border
        add(scrollPane, BorderLayout.CENTER);

        // Panel nederst for checkbokser og "Tøm logg"-knapp
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        bottomPanel.setLayout(new BorderLayout());

        // Panel for checkbokser (venstre)
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridLayout(0, 1, 0, 4));
        checkboxPanel.setOpaque(false);

        loggingCheckbox = createCheckbox("Aktiver item-click logging", Color.RED);
        timerLoggingCheckbox = createCheckbox("Aktiver timer logging (ms/ticks)", Color.RED);
        spellsPlayersCheckbox = createCheckbox("Aktiver spell-/player-logging", Color.RED);
        varbitsCheckbox = createCheckbox("Aktiver varbits-logging", Color.RED);
        varplayersCheckbox = createCheckbox("Aktiver varplayers-logging", Color.RED);
        opcodeCheckbox = createCheckbox("Aktiver opcode-logging", Color.RED);
        npcsCheckbox = createCheckbox("Aktiver npc-logging", Color.RED);

        checkboxPanel.add(loggingCheckbox);
        checkboxPanel.add(timerLoggingCheckbox);
        checkboxPanel.add(spellsPlayersCheckbox);
        checkboxPanel.add(varbitsCheckbox);
        checkboxPanel.add(varplayersCheckbox);
        checkboxPanel.add(opcodeCheckbox);
        checkboxPanel.add(npcsCheckbox);

        bottomPanel.add(checkboxPanel, BorderLayout.CENTER);

        // Knapp for å tømme loggen (høyre)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        JButton clearLogButton = new JButton("Tøm logg");
        clearLogButton.addActionListener(e -> logArea.setText(""));
        buttonPanel.add(clearLogButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JCheckBox createCheckbox(String text, Color notSelectedColor)
    {
        JCheckBox box = new JCheckBox(text);
        box.setFont(new Font("Arial", Font.BOLD, 13));
        box.setForeground(notSelectedColor);
        box.setOpaque(false);
        box.addItemListener(e -> {
            box.setForeground(box.isSelected() ? Color.GREEN : notSelectedColor);
        });
        return box;
    }

    public void logClick(String message)
    {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // Gettere for checkbokser
    public boolean isLoggingEnabled()            { return loggingCheckbox.isSelected(); }
    public boolean isTimerLoggingEnabled()       { return timerLoggingCheckbox.isSelected(); }
    public boolean isSpellsPlayersEnabled()      { return spellsPlayersCheckbox.isSelected(); }
    public boolean isVarbitsLoggingEnabled()     { return varbitsCheckbox.isSelected(); }
    public boolean isVarplayersLoggingEnabled()  { return varplayersCheckbox.isSelected(); }
    public boolean isOpcodeLoggingEnabled()      { return opcodeCheckbox.isSelected(); }
    public boolean isNpcsLoggingEnabled()        { return npcsCheckbox.isSelected(); }
}
