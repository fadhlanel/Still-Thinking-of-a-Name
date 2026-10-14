package com.stillthinkingofaname.welcome;

import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public final class DeathLocationService implements Listener, CommandExecutor {
    private final JavaPlugin p;
    private final Map<UUID, Location> deaths = new HashMap<>();
    private final Set<UUID> busy = new HashSet<>();

    public DeathLocationService(JavaPlugin p) {
        this.p = p;
    }

    @EventHandler
    public void death(PlayerDeathEvent e) {
        Player q = e.getEntity();
        Location l = q.getLocation();
        deaths.put(q.getUniqueId(), l);
        q.sendMessage(ChatColor.RED + "☠ " + ChatColor.GRAY + "You died at coordinates: " + ChatColor.YELLOW + "X: " + l.getBlockX() + ", Y: " + l.getBlockY() + ", Z: " + l.getBlockZ());
        q.sendMessage(ChatColor.AQUA + "Use " + ChatColor.WHITE + "/returnlastdeath" + ChatColor.AQUA + " to return to your death location.");
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player q)) {
            s.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        UUID id = q.getUniqueId();
        if (!deaths.containsKey(id)) {
            q.sendMessage(ChatColor.RED + "You do not have a saved death location.");
            return true;
        }
        if (!busy.add(id)) {
            q.sendMessage(ChatColor.RED + "You are already teleporting!");
            return true;
        }
        new BukkitRunnable() {
            int n = 3;
            public void run() {
                if (!q.isOnline()) {
                    busy.remove(id);
                    cancel();
                } else if (n > 0) {
                    q.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.YELLOW + "Teleporting in " + ChatColor.RED + n + ChatColor.YELLOW + "..."));
                    q.playSound(q.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    n--;
                } else {
                    Location dest = deaths.remove(id);
                    if (dest != null) {
                        q.teleport(safe(dest));
                    }
                    q.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Teleportation successful!"));
                    q.playSound(q.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    busy.remove(id);
                    cancel();
                }
            }
        }.runTaskTimer(p, 0, 20);
        return true;
    }

    public void quit(Player q) {
        busy.remove(q.getUniqueId());
    }

    private Location safe(Location l) {
        if (ok(l)) return l;
        for (int y = -10; y <= 10; y++) {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    Location q = l.clone().add(x, y, z);
                    if (ok(q)) {
                        return new Location(q.getWorld(), q.getBlockX() + 0.5, q.getBlockY(), q.getBlockZ() + 0.5, l.getYaw(), l.getPitch());
                    }
                }
            }
        }
        return l;
    }

    private boolean ok(Location l) {
        Block f = l.getBlock();
        Block h = l.clone().add(0, 1, 0).getBlock();
        Block g = l.clone().add(0, -1, 0).getBlock();
        if (f.getType().isSolid() || f.getType() == Material.LAVA || f.getType() == Material.FIRE) return false;
        if (h.getType().isSolid() || h.getType() == Material.LAVA || h.getType() == Material.FIRE) return false;
        if (!g.getType().isSolid() || g.getType() == Material.LAVA || g.getType() == Material.MAGMA_BLOCK || g.getType() == Material.CACTUS || g.getType() == Material.CAMPFIRE) return false;
        return true;
    }
}
