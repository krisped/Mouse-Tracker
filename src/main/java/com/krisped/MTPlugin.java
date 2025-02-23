package com.krisped;

import javax.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
        name = "[KP] Mouse Tracker",
        description = "En plugin for å spore alt mulig: items, NPC, varbits, varplayers, m.m.",
        tags = {"mt", "mouse", "tracker"}
)
public class MTPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private MTPanel panel;

    @Inject
    private MTOverlay overlay;

    @Inject
    private MTConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ClientThread clientThread;

    private NavigationButton navButton;

    // For ms-logging
    private long lastClickTimeMs = -1;

    // For tick-logging – vi bruker vårt eget tick-tellerfelt
    private int lastClickTick = -1;
    private int tickCounter = 0;

    private final Map<Integer, Integer> previousVarbits = new HashMap<>();
    private final Map<Integer, Integer> previousVarplayers = new HashMap<>();

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);

        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/navigation_button.png");
        navButton = NavigationButton.builder()
                .tooltip("MT Plugin")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);

        // Metoder som kaller client.getVarbitValue() m.m. må kjøres på klienttråden
        clientThread.invokeLater(() -> {
            cacheAllVarbits();
            cacheAllVarplayers();
        });
    }

    @Override
    protected void shutDown() throws Exception
    {
        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
        }
        overlayManager.remove(overlay);

        previousVarbits.clear();
        previousVarplayers.clear();
    }

    @Provides
    public MTConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(MTConfig.class);
    }

    // Abonnerer på GameTick-event for å telle ticks korrekt (ca. 1 tick = 0,6 sek)
    @Subscribe
    public void onGameTick(GameTick event)
    {
        tickCounter++;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (panel == null) return;

        // Logger tid mellom klikk: enten i ms eller i ticks
        if (panel.isTimerLoggingEnabled() || config.logTimeBetweenClicks())
        {
            if (config.timeMode() == TimeMode.MILLISECONDS)
            {
                long now = System.currentTimeMillis();
                if (lastClickTimeMs != -1)
                {
                    long diff = now - lastClickTimeMs;
                    panel.logClick("[TimeBetweenClicks] " + diff + " ms");
                }
                lastClickTimeMs = now;
            }
            else // TICKS – bruker vårt tickCounter
            {
                if (lastClickTick != -1)
                {
                    int diffTicks = tickCounter - lastClickTick;
                    panel.logClick("[TimeBetweenClicks] " + diffTicks + " ticks");
                }
                lastClickTick = tickCounter;
            }
        }

        // Logge opcode hvis aktivert
        final MenuAction menuAction = MenuAction.of(event.getMenuAction().getId());
        final int identifier = event.getId();

        if (panel.isOpcodeLoggingEnabled() || config.logMenuOpcode())
        {
            panel.logClick("MenuAction: " + menuAction
                    + " (opcode=" + event.getMenuAction().getId() + ")"
                    + ", id=" + identifier);
        }

        String option = event.getMenuOption();
        String target = event.getMenuTarget().replaceAll("<[^>]*>", "");

        // NPC-klikk?
        boolean isNpcClick =
                menuAction == MenuAction.NPC_FIRST_OPTION
                        || menuAction == MenuAction.NPC_SECOND_OPTION
                        || menuAction == MenuAction.NPC_THIRD_OPTION
                        || menuAction == MenuAction.NPC_FOURTH_OPTION
                        || menuAction == MenuAction.NPC_FIFTH_OPTION
                        || menuAction == MenuAction.EXAMINE_NPC
                        || menuAction == MenuAction.WALK;

        if (isNpcClick && (panel.isNpcsLoggingEnabled() || config.logNpcs()))
        {
            NPC npc = client.getNpcs().stream()
                    .filter(n -> n.getIndex() == identifier)
                    .findFirst()
                    .orElse(null);

            if (npc != null)
            {
                String npcName = npc.getName() != null ? npc.getName() : "UnknownNPC";
                panel.logClick("[NPC] " + option + " -> " + npcName + " (id: " + npc.getId() + ")");
                return;
            }
        }

        // Item-handlinger: Bruker event.getParam0() for slot og getParam1() for container
        boolean isItemAction = option.equalsIgnoreCase("Wield")
                || option.equalsIgnoreCase("Wear")
                || option.equalsIgnoreCase("Take")
                || option.equalsIgnoreCase("Drop")
                || option.equalsIgnoreCase("Examine")
                || option.equalsIgnoreCase("Remove")
                || option.startsWith("Deposit")
                || option.startsWith("Withdraw");

        if ((isItemAction || option.equalsIgnoreCase("Bank") || option.equalsIgnoreCase("Collect"))
                && (panel.isLoggingEnabled() || config.logItems()))
        {
            int slot = event.getParam0();
            int containerId = event.getParam1();

            // Finn riktig InventoryID med vår hjelpemetode
            InventoryID invId = getInventoryIDById(containerId);
            int realItemId = -1;
            if (invId != null && client.getItemContainer(invId) != null)
            {
                Item[] items = client.getItemContainer(invId).getItems();
                if (slot >= 0 && slot < items.length)
                {
                    realItemId = items[slot].getId();
                }
            }

            if (option.equalsIgnoreCase("Bank")
                    || option.equalsIgnoreCase("Collect")
                    || option.startsWith("Deposit")
                    || option.startsWith("Withdraw"))
            {
                panel.logClick("[Bank] " + option + " -> " + target
                        + " (itemId=" + realItemId + ", slot=" + slot + ")");
                return;
            }
            else
            {
                panel.logClick("[Item] " + option + " -> " + target
                        + " (itemId=" + realItemId + ", slot=" + slot + ")");
                return;
            }
        }

        // Spell-/Player-logging
        if (panel.isSpellsPlayersEnabled())
        {
            if (option.equalsIgnoreCase("Cast")
                    || option.toLowerCase().contains("spell"))
            {
                panel.logClick("[Spell] " + option + " -> " + target + " (id=" + identifier + ")");
                return;
            }
            else if (option.equalsIgnoreCase("Attack")
                    || option.equalsIgnoreCase("Talk-to")
                    || option.equalsIgnoreCase("Trade")
                    || option.equalsIgnoreCase("Follow"))
            {
                panel.logClick("[Player/NPC] " + option + " -> " + target + " (id=" + identifier + ")");
                return;
            }
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (panel == null) return;

        // Varbits
        if (panel.isVarbitsLoggingEnabled() || config.logVarbits())
        {
            int varbitId = event.getVarbitId();
            int newValue = client.getVarbitValue(varbitId);
            Integer oldValue = previousVarbits.get(varbitId);

            if (oldValue == null || oldValue != newValue)
            {
                panel.logClick("[Varbit Changed] ID=" + varbitId + " fra " + oldValue + " til " + newValue);
                previousVarbits.put(varbitId, newValue);
            }
        }

        // VarPlayers
        if (panel.isVarplayersLoggingEnabled() || config.logVarplayers())
        {
            for (int i = 0; i < 2000; i++)
            {
                int currentVal = client.getVarpValue(i);
                Integer oldVal = previousVarplayers.get(i);
                if (oldVal == null || !oldVal.equals(currentVal))
                {
                    panel.logClick("[VarPlayer Changed] ID=" + i + " fra " + oldVal + " til " + currentVal);
                    previousVarplayers.put(i, currentVal);
                }
            }
        }
    }

    private void cacheAllVarbits()
    {
        for (int i = 0; i < 3000; i++)
        {
            try
            {
                int val = client.getVarbitValue(i);
                previousVarbits.put(i, val);
            }
            catch (Exception ignored) {}
        }
    }

    private void cacheAllVarplayers()
    {
        for (int i = 0; i < 2000; i++)
        {
            int val = client.getVarpValue(i);
            previousVarplayers.put(i, val);
        }
    }

    /**
     * Hjelpemetode for å finne riktig InventoryID basert på int-verdi.
     */
    private InventoryID getInventoryIDById(int id)
    {
        for (InventoryID inv : InventoryID.values())
        {
            if (inv.getId() == id)
            {
                return inv;
            }
        }
        return null;
    }
}
