package com.dailytree;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import com.dailytree.overlay.PatchReadyOverlay;

@Slf4j
@PluginDescriptor(
        name = "Daily Tree Runs"
)
public class DailyTreeRunsPlugin extends Plugin
{
        @Inject
        private Client client;

        @Inject
        private DailyTreeRunsConfig config;

        @Inject
        private OverlayManager overlayManager;

        @Inject
        private PatchReadyOverlay overlay;

        /**
         * Tracks the next time a patch will be ready to harvest.
         */
        private final Map<TreePatch, Instant> patchReadyTimes = new EnumMap<>(TreePatch.class);

        /**
         * Patches that have already triggered a notification to avoid spam.
         */
        private final Set<TreePatch> notified = new HashSet<>();

        /**
         * Whether we have notified that all patches are ready.
         */
        private boolean runNotified = false;

	@Override
        protected void startUp() throws Exception
        {
                log.info("Daily Tree Runs started!");
                overlayManager.add(overlay);
        }

	@Override
        protected void shutDown() throws Exception
        {
                log.info("Daily Tree Runs stopped!");
                overlayManager.remove(overlay);
                patchReadyTimes.clear();
                notified.clear();
                runNotified = false;
        }

	@Subscribe
        public void onGameStateChanged(GameStateChanged gameStateChanged)
        {
                if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
                {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Daily Tree Runs says " + config.greeting(), null);
                }
        }

        @Subscribe
        public void onVarbitChanged(VarbitChanged event)
        {
                TreePatch patch = TreePatch.fromVarbit(event.getVarbitId());
                if (patch == null)
                {
                        return;
                }

                // Determine state of the patch using the current varbit value
                int value = client.getVarbitValue(patch.getVarbit());

                if (value == patch.getGrownValue())
                {
                        // Patch is fully grown and ready to harvest
                        patchReadyTimes.put(patch, Instant.now());
                        notified.remove(patch);
                        runNotified = false;
                }
                else if (value > 0)
                {
                        // Patch is growing; schedule next ready time
                        Instant ready = Instant.now().plus(patch.getGrowthTime());
                        patchReadyTimes.put(patch, ready);
                        notified.remove(patch);
                        runNotified = false;
                }
                else
                {
                        // Patch cleared or unplanted
                        patchReadyTimes.remove(patch);
                        notified.remove(patch);
                        runNotified = false;
                }
        }

        @Subscribe
        public void onGameTick(GameTick tick)
        {
                Instant now = Instant.now();

                for (Map.Entry<TreePatch, Instant> entry : patchReadyTimes.entrySet())
                {
                        TreePatch patch = entry.getKey();
                        Instant ready = entry.getValue();

                        if (!notified.contains(patch) && ready != null && !ready.isAfter(now))
                        {
                                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", patch.getDisplayName() + " tree patch is ready!", null);
                                notified.add(patch);
                        }
                }

                if (!runNotified && !patchReadyTimes.isEmpty() && patchReadyTimes.values().stream().allMatch(t -> t != null && !t.isAfter(now)))
                {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "All tracked tree patches are ready for a run!", null);
                        runNotified = true;
                }
        }

        @Provides
        DailyTreeRunsConfig provideConfig(ConfigManager configManager)
        {
                return configManager.getConfig(DailyTreeRunsConfig.class);
        }

        public Map<TreePatch, Instant> getPatchReadyTimes()
        {
                return Collections.unmodifiableMap(patchReadyTimes);
        }

        /**
         * Enumeration of tree patches that we track. Each patch knows its world location, varbit id
         * and the value that represents a fully grown tree. Growth times are simplified to a flat
         * eight hours which is sufficient for daily run tracking.
         */
        public enum TreePatch
        {
               VARROCK(new WorldPoint(3213, 3459, 0), 4771, 3, Duration.ofHours(8)),
               FALADOR(new WorldPoint(3006, 3374, 0), 4772, 3, Duration.ofHours(8)),
               TAVERLEY(new WorldPoint(2936, 3438, 0), 4773, 3, Duration.ofHours(8)),
               LUMBRIDGE(new WorldPoint(3190, 3233, 0), 4774, 3, Duration.ofHours(8)),
               GNOME_STRONGHOLD(new WorldPoint(2434, 3418, 0), 4775, 3, Duration.ofHours(8)),
               GNOME_VILLAGE(new WorldPoint(2488, 3446, 0), 7904, 3, Duration.ofHours(8));

                private final WorldPoint location;
                private final int varbit;
                private final int grownValue;
                private final Duration growthTime;

                TreePatch(WorldPoint location, int varbit, int grownValue, Duration growthTime)
                {
                        this.location = location;
                        this.varbit = varbit;
                        this.grownValue = grownValue;
                        this.growthTime = growthTime;
                }

                public WorldPoint getLocation()
                {
                        return location;
                }

                public int getVarbit()
                {
                        return varbit;
                }

                public int getGrownValue()
                {
                        return grownValue;
                }

                public Duration getGrowthTime()
                {
                        return growthTime;
                }

                public String getDisplayName()
                {
                        return name().charAt(0) + name().substring(1).toLowerCase();
                }

                static TreePatch fromVarbit(int varbit)
                {
                        for (TreePatch patch : values())
                        {
                                if (patch.varbit == varbit)
                                {
                                        return patch;
                                }
                        }
                        return null;
                }
        }
}
