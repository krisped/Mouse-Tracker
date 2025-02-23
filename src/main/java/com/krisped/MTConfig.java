package com.krisped;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Konfigurasjonsverdier som vises i RuneLites plugin-config.
 */
@ConfigGroup("mtplugin")
public interface MTConfig extends Config
{
    @ConfigItem(
            keyName = "logItems",
            name = "Log Items",
            description = "Log item-handlinger (Wield, Drop, Withdraw, etc.)",
            position = 1
    )
    default boolean logItems() { return true; }

    @ConfigItem(
            keyName = "logNpcs",
            name = "Log NPCs",
            description = "Log NPC-handlinger (Attack, Talk-to, Bank, etc.)",
            position = 2
    )
    default boolean logNpcs() { return true; }

    @ConfigItem(
            keyName = "logVarbits",
            name = "Log Varbits",
            description = "Log endringer i Varbits (bl.a. prayers)",
            position = 3
    )
    default boolean logVarbits() { return false; }

    @ConfigItem(
            keyName = "logVarplayers",
            name = "Log VarPlayers",
            description = "Log endringer i VarPlayers",
            position = 4
    )
    default boolean logVarplayers() { return false; }

    @ConfigItem(
            keyName = "logMenuOpcode",
            name = "Log Opcode",
            description = "Log hvilke MenuAction/Opcode en hendelse har",
            position = 5
    )
    default boolean logMenuOpcode() { return false; }

    @ConfigItem(
            keyName = "logTimeBetweenClicks",
            name = "Log Tid Mellom Klikk",
            description = "Logger tid siden forrige klikk (i ms eller ticks)",
            position = 6
    )
    default boolean logTimeBetweenClicks() { return true; }

    @ConfigItem(
            keyName = "timeMode",
            name = "Tid-enhet",
            description = "Velg om du vil logge millisekunder eller game ticks (1 tick = 0,6 sek)",
            position = 7
    )
    default TimeMode timeMode()
    {
        return TimeMode.MILLISECONDS; // standard
    }
}
