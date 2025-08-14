package com.dailytree.overlay;

import com.dailytree.DailyTreeRunsConfig;
import com.dailytree.DailyTreeRunsPlugin;
import com.dailytree.DailyTreeRunsPlugin.TreePatch;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Instant;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class PatchReadyOverlay extends Overlay
{
    private final Client client;
    private final DailyTreeRunsPlugin plugin;
    private final DailyTreeRunsConfig config;

    @Inject
    public PatchReadyOverlay(Client client, DailyTreeRunsPlugin plugin, DailyTreeRunsConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showOverlay())
        {
            return null;
        }

        Instant now = Instant.now();
        for (Map.Entry<TreePatch, Instant> entry : plugin.getPatchReadyTimes().entrySet())
        {
            Instant ready = entry.getValue();
            if (ready == null || ready.isAfter(now))
            {
                continue;
            }

            LocalPoint lp = LocalPoint.fromWorld(client, entry.getKey().getLocation());
            if (lp == null)
            {
                continue;
            }

            java.awt.Polygon poly = Perspective.getCanvasTilePoly(client, lp);
            if (poly != null)
            {
                OverlayUtil.renderPolygon(graphics, poly, Color.GREEN);
            }
        }

        return null;
    }
}
