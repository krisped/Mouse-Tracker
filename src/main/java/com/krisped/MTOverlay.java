package com.krisped;

import java.awt.Dimension;
import java.awt.Graphics2D;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Minimal overlay – tegner ingenting, men må legges til i OverlayManager.
 */
public class MTOverlay extends Overlay
{
    public MTOverlay()
    {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Tomt overlay
        return null;
    }
}
