package com.stillthinkingofaname.welcome;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.meta.FireworkMeta;

public final class WelcomeListener implements Listener {
    private final BossBarService bar;
    private final StarterKitService kit;
    private final DeathLocationService death;
    private final UpdateChecker updates;
    private final JavaPlugin plugin;

    public WelcomeListener(JavaPlugin p, BossBarService b, StarterKitService k, DeathLocationService d, UpdateChecker u) {
        plugin = p;
        bar = b;
        kit = k;
        death = d;
        updates = u;
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Player q = e.getPlayer();
        bar.join(q);
        q.sendMessage(ChatColor.GOLD + "=======================================");
        q.sendMessage(ChatColor.AQUA + "  Selamat Datang di " + ChatColor.YELLOW + ChatColor.BOLD + "AestherVale Survival");
        q.sendMessage(ChatColor.GOLD + "=======================================");
        if (bar.visible(q.getUniqueId())) {
            q.sendMessage(ChatColor.GRAY + "Tip: Gunakan " + ChatColor.WHITE + "/togglebar" + ChatColor.GRAY + " untuk menyembunyikan HUD Bar.");
        }
        kit.join(q);
        updates.notifyIfAvailable(q);
        if (q.isOp() && plugin.getConfig().getBoolean("auto-restart.blindness.enabled", true)) q.sendMessage(ChatColor.YELLOW + "Countdown blindness aktif. Gunakan /sar blindness off untuk mematikannya.");
        Firework f = q.getWorld().spawn(q.getLocation(), Firework.class);
        FireworkMeta m = f.getFireworkMeta();
        m.addEffect(FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.AQUA, Color.YELLOW, Color.WHITE)
                .withFade(Color.BLUE)
                .build());
        m.setPower(1);
        f.setFireworkMeta(m);
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        bar.quit(e.getPlayer());
        death.quit(e.getPlayer());
    }
}
