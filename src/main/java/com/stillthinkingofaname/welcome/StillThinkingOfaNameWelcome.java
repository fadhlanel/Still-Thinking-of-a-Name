package com.stillthinkingofaname.welcome;

import org.bukkit.plugin.java.JavaPlugin;

public final class StillThinkingOfaNameWelcome extends JavaPlugin {
    private AutoRestartManager auto;
    private BossBarService bar;
    private UpdateChecker updates;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        bar = new BossBarService(this);
        StarterKitService kit = new StarterKitService(this);
        DeathLocationService death = new DeathLocationService(this);
        updates = new UpdateChecker(this);
        getServer().getPluginManager().registerEvents(new WelcomeListener(this, bar, kit, death, updates), this);
        getServer().getPluginManager().registerEvents(death, this);
        getCommand("togglebar").setExecutor(bar);
        StarterKitCommand starterKit = new StarterKitCommand(kit);
        getCommand("starterkit").setExecutor(starterKit);
        getCommand("sk").setExecutor(starterKit);
        getCommand("returnlastdeath").setExecutor(death);
        getCommand("mmnupdate").setExecutor(updates);
        updates.start();
        auto = new AutoRestartManager(this);
        auto.load();
        getCommand("autorestart").setExecutor(new AutoRestartCommand(auto));
    }

    @Override
    public void onDisable() {
        if (bar != null) bar.shutdown();
        if (auto != null) auto.shutdown();
    }
}
