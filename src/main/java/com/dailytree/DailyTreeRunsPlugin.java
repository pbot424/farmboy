package com.dailytree;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
        name = "Daily Tree Runs"
)
public class DailyTreeRunsPlugin extends Plugin
{
        private static final String CONFIG_GROUP = "dailytreeruns";
        private static final String LAST_COMPLETED_KEY_PREFIX = "lastCompleted.";
        private static final Duration RESET_DURATION = Duration.ofDays(1);

        @Inject
        private Client client;

        @Inject
        private DailyTreeRunsConfig config;

        @Inject
        private ConfigManager configManager;

        private final Map<TreePatch, Boolean> patchStates = new EnumMap<>(TreePatch.class);

        @Override
        protected void startUp() throws Exception
        {
                log.info("Daily Tree Runs started!");
                checkPatches();
        }

        @Override
        protected void shutDown() throws Exception
        {
                log.info("Daily Tree Runs stopped!");
                patchStates.clear();
        }

        @Subscribe
        public void onGameStateChanged(GameStateChanged gameStateChanged)
        {
                if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
                {
                        checkPatches();
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Daily Tree Runs says " + config.greeting(), null);
                }
        }

        @Subscribe
        public void onChatMessage(ChatMessage chatMessage)
        {
                if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE)
                {
                        return;
                }

                String message = chatMessage.getMessage();
                for (TreePatch patch : TreePatch.values())
                {
                        if (message.contains(patch.getDisplayName()))
                        {
                                markPatchCompleted(patch);
                        }
                }
        }

        private void checkPatches()
        {
                Instant now = Instant.now();
                for (TreePatch patch : TreePatch.values())
                {
                        long lastCompletion = getLastCompletion(patch);
                        boolean completed = lastCompletion > 0 && now.minus(RESET_DURATION).toEpochMilli() < lastCompletion;
                        patchStates.put(patch, completed);

                        if (!completed)
                        {
                                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", patch.getDisplayName() + " is ready for a new run.", null);
                        }
                }
        }

        private long getLastCompletion(TreePatch patch)
        {
                String key = LAST_COMPLETED_KEY_PREFIX + patch.name();
                String value = configManager.getConfiguration(CONFIG_GROUP, key);
                return value == null ? 0L : Long.parseLong(value);
        }

        private void setLastCompletion(TreePatch patch, long time)
        {
                String key = LAST_COMPLETED_KEY_PREFIX + patch.name();
                configManager.setConfiguration(CONFIG_GROUP, key, time);
        }

        private void markPatchCompleted(TreePatch patch)
        {
                long now = Instant.now().toEpochMilli();
                setLastCompletion(patch, now);
                patchStates.put(patch, true);
        }

        @Provides
        DailyTreeRunsConfig provideConfig(ConfigManager configManager)
        {
                return configManager.getConfig(DailyTreeRunsConfig.class);
        }
}
