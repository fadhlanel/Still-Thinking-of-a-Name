package com.stillthinkingofaname.welcome;

import java.util.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class StarterKitCommand implements TabExecutor {
    private final StarterKitService s;

    public StarterKitCommand(StarterKitService s) {
        this.s = s;
    }

    @Override
    public boolean onCommand(CommandSender x, Command c, String l, String[] a) {
        if (!(x instanceof Player p)) {
            x.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        if (c.getName().equalsIgnoreCase("starterkit")) {
            return s.claim(p);
        }
        if (a.length == 0) {
            help(p);
            return true;
        }
        if (!p.isOp()) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            help(p);
            return true;
        }
        String z = a[0].toLowerCase();
        if (z.equals("help")) {
            help(p);
            return true;
        }
        if (z.equals("clone")) {
            clone(p);
            return true;
        }
        if (z.equals("reload")) {
            s.p.reloadConfig();
            p.sendMessage(ChatColor.GREEN + "✔ Configuration reloaded! Kit items, player data, and settings refreshed.");
            return true;
        }
        if (a.length < 2) {
            p.sendMessage(ChatColor.RED + "Usage: /sk " + z + " <target>");
            return true;
        }
        List<Player> ts = targets(p, a[1]);
        if (ts.isEmpty()) {
            p.sendMessage(ChatColor.RED + "Player not found or offline.");
            return true;
        }
        if (z.equals("give")) {
            int n = 0;
            for (Player q : ts) {
                if (s.claimed.add(q.getUniqueId())) {
                    s.give(q);
                    q.sendMessage(ChatColor.GREEN + "✔ You received the starter kit from an admin!");
                    n++;
                }
            }
            s.saveClaimed();
            p.sendMessage(ChatColor.GREEN + "✔ Starter kit given to " + n + " player(s). Skipped: " + (ts.size() - n));
        } else if (z.equals("remove")) {
            for (Player q : ts) {
                s.claimed.remove(q.getUniqueId());
            }
            s.saveClaimed();
            p.sendMessage(ChatColor.GREEN + "✔ Claimed status reset for " + ts.size() + " player(s).");
        } else if (z.equals("reset")) {
            for (Player q : ts) {
                s.claimed.remove(q.getUniqueId());
                s.warnings.remove(q.getUniqueId());
                s.first.put(q.getUniqueId(), System.currentTimeMillis());
            }
            s.saveClaimed();
            s.save("warning-shown-count", s.warnings);
            s.save("first-join-times", s.first);
            p.sendMessage(ChatColor.GREEN + "✔ Full reset completed for " + ts.size() + " player(s).");
        } else {
            help(p);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("sk")) return List.of();
        if (args.length == 1) return filter(args[0], "help", "clone", "give", "remove", "reset", "reload");
        if (args.length == 2 && List.of("give", "remove", "reset").contains(args[0].toLowerCase())) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.startsWith(args[1])).toList();
        }
        return List.of();
    }

    private void help(Player p) {
        p.sendMessage(ChatColor.GOLD + "--- " + ChatColor.GREEN + "StarterKit Help" + ChatColor.GOLD + " ---");
        p.sendMessage(ChatColor.YELLOW + "/sk help" + ChatColor.GRAY + " - View this menu");
        p.sendMessage(ChatColor.YELLOW + "/sk clone" + ChatColor.GRAY + " - Copy your inventory to the kit");
        p.sendMessage(ChatColor.YELLOW + "/sk give <target>" + ChatColor.GRAY + " - Give kit to player(s)");
        p.sendMessage(ChatColor.YELLOW + "/sk remove <target>" + ChatColor.GRAY + " - Remove claimed status");
        p.sendMessage(ChatColor.YELLOW + "/sk reset <target>" + ChatColor.GRAY + " - Full reset (claimed+warning+time)");
        p.sendMessage(ChatColor.YELLOW + "/sk reload" + ChatColor.GRAY + " - Refresh the config file");
    }

    private void clone(Player p) {
        List<Map<String, Object>> inv = new ArrayList<>();
        for (org.bukkit.inventory.ItemStack i : p.getInventory().getStorageContents()) {
            if (i != null && i.getType() != Material.AIR) {
                inv.add(i.serialize());
            }
        }
        Map<String, Object> armor = new HashMap<>();
        if (p.getInventory().getHelmet() != null && p.getInventory().getHelmet().getType() != Material.AIR) {
            armor.put("helmet", p.getInventory().getHelmet().serialize());
        }
        if (p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() != Material.AIR) {
            armor.put("chestplate", p.getInventory().getChestplate().serialize());
        }
        if (p.getInventory().getLeggings() != null && p.getInventory().getLeggings().getType() != Material.AIR) {
            armor.put("leggings", p.getInventory().getLeggings().serialize());
        }
        if (p.getInventory().getBoots() != null && p.getInventory().getBoots().getType() != Material.AIR) {
            armor.put("boots", p.getInventory().getBoots().serialize());
        }
        Map<String, Object> offhand = null;
        if (p.getInventory().getItemInOffHand() != null && p.getInventory().getItemInOffHand().getType() != Material.AIR) {
            offhand = p.getInventory().getItemInOffHand().serialize();
        }
        s.p.getConfig().set("starter-kit.inventory-items", inv);
        s.p.getConfig().set("starter-kit.armor", armor);
        s.p.getConfig().set("starter-kit.offhand", offhand);
        s.p.saveConfig();
        int total = inv.size() + armor.size() + (offhand != null ? 1 : 0);
        p.sendMessage(ChatColor.GREEN + "✔ Inventory successfully copied to starter kit! (" + total + " items)");
    }

    private List<Player> targets(Player sender, String targetStr) {
        List<Player> result = new ArrayList<>();
        if (targetStr.equals("@a")) {
            result.addAll(Bukkit.getOnlinePlayers());
        } else if (targetStr.equals("@p")) {
            Player closest = null;
            double minDist = Double.MAX_VALUE;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld() == sender.getWorld()) {
                    double dist = p.getLocation().distanceSquared(sender.getLocation());
                    if (dist < minDist) {
                        minDist = dist;
                        closest = p;
                    }
                }
            }
            if (closest != null) result.add(closest);
        } else if (targetStr.equals("@r")) {
            List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (!online.isEmpty()) {
                result.add(online.get((int) (Math.random() * online.size())));
            }
        } else if (targetStr.equals("@s")) {
            result.add(sender);
        } else {
            Player target = Bukkit.getPlayer(targetStr);
            if (target != null) result.add(target);
        }
        return result;
    }

    private List<String> filter(String input, String... values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.startsWith(input.toLowerCase())) result.add(value);
        }
        return result;
    }
}
