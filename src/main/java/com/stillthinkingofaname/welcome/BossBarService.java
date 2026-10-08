package com.stillthinkingofaname.welcome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class BossBarService implements CommandExecutor {
    private final JavaPlugin plugin; private final BossBar bar; private final Set<UUID> hidden=new HashSet<>();
    public BossBarService(JavaPlugin p){plugin=p; for(String s:p.getConfig().getStringList("hidden-bossbar-players"))try{hidden.add(UUID.fromString(s));}catch(IllegalArgumentException ignored){} bar=Bukkit.createBossBar(ChatColor.GOLD+"✦ "+ChatColor.YELLOW+ChatColor.BOLD+"StillThinkingOfaName Survival"+ChatColor.GOLD+" ✦",BarColor.PURPLE,BarStyle.SOLID);bar.setProgress(1);bar.setVisible(true);Bukkit.getOnlinePlayers().forEach(this::join);}
    public void join(Player p){if(!hidden.contains(p.getUniqueId()))bar.addPlayer(p);} public void quit(Player p){bar.removePlayer(p);}
    private void save(){plugin.getConfig().set("hidden-bossbar-players",new ArrayList<>(hidden.stream().map(UUID::toString).toList()));plugin.saveConfig();}
    @Override public boolean onCommand(CommandSender s,Command c,String l,String[] a){if(!(s instanceof Player p)){s.sendMessage(ChatColor.RED+"Perintah ini hanya bisa digunakan oleh pemain!");return true;} UUID id=p.getUniqueId();if(hidden.remove(id)){bar.addPlayer(p);p.sendMessage(ChatColor.GREEN+"✔ HUD Bar "+ChatColor.YELLOW+"StillThinkingOfaName"+ChatColor.GREEN+" ditampilkan.");}else{hidden.add(id);bar.removePlayer(p);p.sendMessage(ChatColor.GRAY+"✖ HUD Bar "+ChatColor.YELLOW+"StillThinkingOfaName"+ChatColor.GRAY+" disembunyikan. Ketik "+ChatColor.WHITE+"/togglebar"+ChatColor.GRAY+" untuk menampilkan kembali.");}save();return true;}
    public boolean visible(UUID id){return !hidden.contains(id);} public void shutdown(){bar.removeAll();}
}
