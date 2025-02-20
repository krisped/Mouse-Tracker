package com.krisped;

import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class MTOverlay extends Overlay
{
    public MTOverlay()
    {
        // Overlay plasseres øverst til venstre, over widgets
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Ingen overlay-innhold – denne klassen er minimert
        return null;
    }
}
