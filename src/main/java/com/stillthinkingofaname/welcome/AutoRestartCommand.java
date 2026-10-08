package com.stillthinkingofaname.welcome;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public final class AutoRestartCommand implements TabExecutor {
    private final AutoRestartManager manager;
    public AutoRestartCommand(AutoRestartManager manager) { this.manager = manager; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { sendHelp(sender, label); return true; }
        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 3) { sendHelp(sender, label); return true; }
                String recurring = (args.length > 3 ? args[3] : "Daily") + ";" + args[1] + ":" + args[2];
                sender.sendMessage(manager.schedule(recurring, true) ? ChatColor.GREEN + "Restart berulang: " + recurring : ChatColor.RED + "Waktu tidak valid atau sudah terjadwal.");
                return true;
            case "once":
                if (args.length != 3) { sendHelp(sender, label); return true; }
                String once = args[1] + ":" + args[2];
                if (!manager.scheduleOnce(once, true)) { sender.sendMessage(ChatColor.RED + "Waktu WIB harus masih akan datang."); return true; }
                manager.saveOneTime(once);
                sender.sendMessage(ChatColor.GREEN + "Restart satu kali: " + once + " WIB");
                return true;
            case "cancel":
                sender.sendMessage(manager.cancelNext() ? ChatColor.YELLOW + "Restart berikutnya dibatalkan." : ChatColor.RED + "Tidak ada restart."); return true;
            case "resume":
                sender.sendMessage(manager.resumeNext() ? ChatColor.GREEN + "Restart dilanjutkan." : ChatColor.RED + "Tidak ada restart dibatalkan."); return true;
            case "clear":
                manager.clear(); sender.sendMessage(ChatColor.GREEN + "Semua rencana restart dihapus."); return true;
            case "remove":
                if (args.length != 3) { sender.sendMessage(ChatColor.YELLOW + "Gunakan /" + label + " remove <YYYY-MM-DD> <HH:MM>"); return true; }
                sender.sendMessage(manager.remove(args[1] + " " + args[2]) ? ChatColor.GREEN + "Rencana dihapus." : ChatColor.RED + "Rencana tidak ditemukan."); return true;
            case "status":
                List<ZonedDateTime> restarts = new ArrayList<>(manager.getScheduled()); restarts.sort(ZonedDateTime::compareTo);
                if (restarts.isEmpty()) { sender.sendMessage(ChatColor.YELLOW + "Tidak ada restart terjadwal."); return true; }
                sender.sendMessage(ChatColor.GOLD + "Restart terjadwal (WIB):");
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (ZonedDateTime restart : restarts) sender.sendMessage((manager.getCanceled().contains(restart) ? ChatColor.RED + "Dibatalkan: " : ChatColor.GREEN + "Aktif: ") + restart.format(format));
                return true;
            case "reload": manager.reload(); sender.sendMessage(ChatColor.GREEN + "Konfigurasi dimuat ulang."); return true;
            default: sendHelp(sender, label); return true;
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GOLD + "--- AutoRestart Help (WIB) ---");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " set <jam> <menit> [hari]" + ChatColor.GRAY + " - Berulang");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " once <jam> <menit>" + ChatColor.GRAY + " - Sekali");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " cancel" + ChatColor.GRAY + " - Batalkan terdekat");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " resume" + ChatColor.GRAY + " - Lanjutkan terdekat");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " remove <YYYY-MM-DD HH:MM>" + ChatColor.GRAY + " - Hapus satu");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " clear" + ChatColor.GRAY + " - Hapus semua");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " status" + ChatColor.GRAY + " - Lihat rencana");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " reload" + ChatColor.GRAY + " - Muat ulang dari file konfigurasi");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return filter(args[0], "set", "once", "cancel", "resume", "remove", "clear", "status", "reload");
        if ((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("once")) && args.length == 2) return filter(args[1], hours());
        if ((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("once")) && args.length == 3) return filter(args[2], minutes());
        if (args[0].equalsIgnoreCase("set") && args.length == 4) return filter(args[3], "Daily(Harian)", "Monday(Senin)", "Tuesday(Selasa)", "Wednesday(Rabu)", "Thursday(Kamis)", "Friday(Jumat)", "Saturday(Sabtu)", "Sunday(Minggu)");
        return List.of();
    }
    private List<String> filter(String input, String... values) { List<String> result = new ArrayList<>(); for (String value : values) if (value.startsWith(input)) result.add(value); return result; }
    private String[] hours() { String[] values = new String[24]; for (int i = 0; i < 24; i++) values[i] = String.format("%02d", i); return values; }
    private String[] minutes() { String[] values = new String[60]; for (int i = 0; i < 60; i++) values[i] = String.format("%02d", i); return values; }
}
