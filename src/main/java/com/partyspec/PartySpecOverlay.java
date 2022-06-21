package com.partyspec;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class PartySpecOverlay extends Overlay
{
    private final Client client;
    private final PartySpecConfig config;
    private final PartySpecPlugin plugin;

    @Inject
    PartySpecOverlay(Client client, PartySpecConfig config, PartySpecPlugin plugin)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        graphics.setFont(new Font(FontManager.getRunescapeFont().toString(), Font.BOLD, this.config.fontSize()));

        //track player locations for vertical-offsetting purposes, when players are stacked their names/hp(if rendered) should stack instead of overlapping
        List<WorldPoint> trackedLocations = new ArrayList<>();

        for(Player player : client.getPlayers()){

            if (player == null || player.getName() == null)
            {
                continue;
            }

            String name = player.getName();
            if(!plugin.getMembers().containsKey(name)){
                continue;
            }

            if(!plugin.getVisiblePlayers().isEmpty() && !plugin.getVisiblePlayers().contains(name.toLowerCase())){
                continue;
            }

            Color col = config.getSpecColor();

            int playersTracked = 0;
            WorldPoint currentLoc = player.getWorldLocation();
            for(int i=0; i<trackedLocations.size(); i++){
                WorldPoint compareLoc = trackedLocations.get(i);
                if(compareLoc.getX() == currentLoc.getX() && compareLoc.getY() == currentLoc.getY()){
                    playersTracked++;
                }
            }
            trackedLocations.add(player.getWorldLocation());

            renderPlayerOverlay(graphics, player, col, playersTracked);
        }

        return null;
    }

    private void renderPlayerOverlay(Graphics2D graphics, Player actor, Color color, int playersTracked)
    {
        int currentSpec = plugin.getMembers().get(actor.getName()).getSpec() / 10;

        String spec = "";

        spec += currentSpec;
        spec += "%";

        Point textLocation = actor.getCanvasTextLocation(graphics, spec, config.offSetTextZ()/*(playersTracked*20)*/);

        float verticalOffSetMultiplier = 1f + (playersTracked*0.1f);

        if(textLocation != null)
        {
            textLocation = new Point(textLocation.getX() + config.offSetTextHorizontal(), (-config.offSetTextVertical())+(int) (textLocation.getY() * verticalOffSetMultiplier));
            OverlayUtil.renderTextLocation(graphics, textLocation, spec, color);
        }

    }





}
