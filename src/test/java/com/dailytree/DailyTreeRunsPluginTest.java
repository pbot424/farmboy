package com.dailytree;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DailyTreeRunsPluginTest
{
        public static void main(String[] args) throws Exception
        {
                ExternalPluginManager.loadBuiltin(DailyTreeRunsPlugin.class);
                RuneLite.main(args);
        }
}
