package com.krisped;

import javax.inject.Inject;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;

@PluginDescriptor(
        name = "[KP] Mouse Tracker",
        description = "En plugin for å spore item-, spell- og player-klikk samt logge tid mellom klikk",
        tags = {"mt", "mouse", "tracker"}
)
public class MTPlugin extends Plugin
{
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

    private NavigationButton navButton;
    private long lastClickTime = -1;

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
    }

    @Override
    protected void shutDown() throws Exception
    {
        if (navButton != null)
            clientToolbar.removeNavigation(navButton);
        overlayManager.remove(overlay);
    }

    @Provides
    public MTConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(MTConfig.class);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        String option = event.getMenuOption().toUpperCase();

        boolean isItemAction = option.equals("WIELD") || option.equals("WEAR") ||
                option.equals("TAKE") || option.equals("DROP") ||
                option.equals("EXAMINE") || option.equals("REMOVE") ||
                option.startsWith("DEPOSIT") || option.startsWith("WITHDRAW");

        boolean isSpellOrPlayerAction = false;
        if (panel != null && panel.isSpellsPlayersEnabled()) {
            isSpellOrPlayerAction = option.equals("CAST") || option.equals("SPELL") ||
                    option.equals("ATTACK") || option.equals("TALK-TO") ||
                    option.equals("TRADE") || option.equals("FOLLOW");
        }

        if (!(isItemAction || isSpellOrPlayerAction))
            return;

        if (panel != null && panel.isLoggingEnabled())
        {
            long currentTime = System.currentTimeMillis();
            if (panel.isTimerLoggingEnabled() && lastClickTime != -1)
            {
                long diff = currentTime - lastClickTime;
                panel.logClick(diff + " ms");
            }
            lastClickTime = currentTime;

            String target = event.getMenuTarget().replaceAll("<[^>]*>", "");
            String logLine;

            if (isItemAction)
            {
                logLine = "[" + option + "] " + target;
            }
            else if (isSpellOrPlayerAction)
            {
                if (option.equals("CAST") || option.equals("SPELL"))
                    logLine = "[" + option + "] " + target;
                else
                    logLine = target.contains("(")
                            ? "[" + option + "] NPC <" + target + ">"
                            : "[" + option + "] Player <" + target + ">";
            }
            else
            {
                logLine = "[" + option + "] " + target;
            }
            panel.logClick(logLine);
        }
    }
}
