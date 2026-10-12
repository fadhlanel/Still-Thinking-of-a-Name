package com.stillthinkingofaname.welcome;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public final class AutoRestartCommand implements TabExecutor {
    private static final Pattern DELAY = Pattern.compile("^(\\d+)([hms])$", Pattern.CASE_INSENSITIVE);
    private final AutoRestartManager manager;
    public AutoRestartCommand(AutoRestartManager manager) { this.manager = manager; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { help(sender, label); return true; }
        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 3) { help(sender, label); return true; }
                String recurring = (args.length > 3 ? args[3] : "Daily") + ";" + args[1] + ":" + args[2];
                sender.sendMessage(manager.schedule(recurring, true) ? ChatColor.GREEN + "Restart berulang: " + recurring : ChatColor.RED + "Waktu tidak valid atau sudah terjadwal."); return true;
            case "once":
                if (args.length != 3) { help(sender, label); return true; }
                String once = args[1] + ":" + args[2];
                if (!manager.scheduleOnce(once, true)) sender.sendMessage(ChatColor.RED + "Waktu WIB harus masih akan datang."); else { manager.saveOneTime(once); sender.sendMessage(ChatColor.GREEN + "Restart satu kali: " + once + " WIB"); } return true;
            case "in":
                if (args.length != 2) { help(sender, label); return true; }
                Matcher delay = DELAY.matcher(args[1]);
                if (!delay.matches()) { sender.sendMessage(ChatColor.RED + "Format: /" + label + " in <angka><h|m|s>, contoh: /" + label + " in 30m"); return true; }
                long amount; try { amount = Long.parseLong(delay.group(1)); } catch (NumberFormatException e) { sender.sendMessage(ChatColor.RED + "Durasi terlalu besar."); return true; }
                long seconds = switch (delay.group(2).toLowerCase()) { case "h" -> amount * 3600; case "m" -> amount * 60; default -> amount; };
                sender.sendMessage(manager.scheduleRelative(seconds, true) ? ChatColor.GREEN + "Restart satu kali dalam " + args[1] + "." : ChatColor.RED + "Durasi harus lebih dari nol."); return true;
            case "blindness":
                if (args.length != 2 || !(args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("off"))) { sender.sendMessage(ChatColor.YELLOW + "Gunakan /" + label + " blindness <on|off>"); return true; }
                boolean enabled = args[1].equalsIgnoreCase("on"); manager.setBlindness(enabled); sender.sendMessage(ChatColor.GREEN + "Countdown blindness " + (enabled ? "diaktifkan." : "dimatikan.")); return true;
            case "cancel": sender.sendMessage(manager.cancelNext() ? ChatColor.YELLOW + "Restart berikutnya dibatalkan." : ChatColor.RED + "Tidak ada restart."); return true;
            case "resume": sender.sendMessage(manager.resumeNext() ? ChatColor.GREEN + "Restart dilanjutkan." : ChatColor.RED + "Tidak ada restart dibatalkan."); return true;
            case "clear": manager.clear(); sender.sendMessage(ChatColor.GREEN + "Semua rencana restart dihapus."); return true;
            case "status":
                List<ZonedDateTime> restarts = new ArrayList<>(manager.getScheduled()); restarts.sort(ZonedDateTime::compareTo);
                if (restarts.isEmpty()) { sender.sendMessage(ChatColor.YELLOW + "Tidak ada restart terjadwal."); return true; }
                sender.sendMessage(ChatColor.GOLD + "Restart terjadwal (WIB):"); DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (ZonedDateTime restart : restarts) sender.sendMessage((manager.getCanceled().contains(restart) ? ChatColor.RED + "Dibatalkan: " : ChatColor.GREEN + "Aktif: ") + restart.format(format)); return true;
            case "reload": manager.reload(); sender.sendMessage(ChatColor.GREEN + "Konfigurasi dimuat ulang."); return true;
            default: help(sender, label); return true;
        }
    }
    private void help(CommandSender s, String label) { s.sendMessage(ChatColor.GOLD + "--- AutoRestart Help (WIB) ---"); s.sendMessage(ChatColor.YELLOW + "/" + label + " set <jam> <menit> [hari]" + ChatColor.GRAY + " - Berulang"); s.sendMessage(ChatColor.YELLOW + "/" + label + " once <jam> <menit>" + ChatColor.GRAY + " - Sekali"); s.sendMessage(ChatColor.YELLOW + "/" + label + " in <angka><h|m|s>" + ChatColor.GRAY + " - Sekali, relative"); s.sendMessage(ChatColor.YELLOW + "/" + label + " blindness <on|off>" + ChatColor.GRAY + " - Efek countdown"); s.sendMessage(ChatColor.YELLOW + "/" + label + " cancel|resume|clear|status|reload"); }
    @Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) { if (args.length == 1) return filter(args[0], "set", "once", "in", "blindness", "cancel", "resume", "clear", "status", "reload"); if (args.length == 2 && args[0].equalsIgnoreCase("blindness")) return filter(args[1], "on", "off"); if ((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("once")) && args.length == 2) return filter(args[1], hours()); if ((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("once")) && args.length == 3) return filter(args[2], minutes()); return List.of(); }
    private List<String> filter(String input, String... values) { List<String> result = new ArrayList<>(); for (String value : values) if (value.startsWith(input)) result.add(value); return result; }
    private String[] hours() { String[] values = new String[24]; for (int i = 0; i < 24; i++) values[i] = String.format("%02d", i); return values; }
    private String[] minutes() { String[] values = new String[60]; for (int i = 0; i < 60; i++) values[i] = String.format("%02d", i); return values; }
}
