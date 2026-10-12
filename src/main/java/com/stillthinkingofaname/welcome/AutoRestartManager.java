package com.stillthinkingofaname.welcome;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoRestartManager {
    private static final ZoneId SERVER_ZONE = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final JavaPlugin plugin;
    private final Set<ZonedDateTime> scheduled = new HashSet<>(), canceled = new HashSet<>();
    private Timer timer = new Timer(true);
    public AutoRestartManager(JavaPlugin plugin) { this.plugin = plugin; }

    public void load() {
        for (String time : plugin.getConfig().getStringList("auto-restart.restart-times")) schedule(time, false);
        String once = plugin.getConfig().getString("auto-restart.one-time-restart", "");
        if (!once.isBlank()) try { scheduleAt(parseDate(once), once, true, false); } catch (RuntimeException ignored) { saveOneTime(""); }
        if (blindnessEnabled()) plugin.getLogger().warning("Countdown blindness is ENABLED. Operators can disable it with /sar blindness off.");
    }
    public boolean schedule(String value, boolean announce) { try { return scheduleAt(parseRecurring(value), value, false, announce); } catch (RuntimeException e) { return false; } }
    public boolean scheduleOnce(String value, boolean announce) { try { return scheduleAt(parseOnce(value), value, true, announce); } catch (RuntimeException e) { return false; } }
    public boolean scheduleRelative(long seconds, boolean announce) {
        if (seconds <= 0) return false;
        ZonedDateTime when = now().plusSeconds(seconds);
        String value = when.format(DATE_FORMAT);
        if (!scheduleAt(when, value, true, announce)) return false;
        saveOneTime(value);
        return true;
    }
    private boolean scheduleAt(ZonedDateTime when, String source, boolean oneTime, boolean announce) {
        if (scheduled.contains(when)) return false;
        long seconds = Duration.between(now(), when).getSeconds(); if (seconds < 0) return false;
        scheduled.add(when);
        for (Map.Entry<String, Object> entry : sectionValues("messages").entrySet()) {
            long warning; try { warning = Long.parseLong(entry.getKey()); } catch (NumberFormatException e) { continue; }
            if (warning <= seconds && entry.getValue() instanceof String message) timer.schedule(new TimerTask() { public void run() { if (!canceled.contains(when)) runTask(() -> Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message))); }}, (seconds - warning) * 1000L);
        }
        Map<String, Object> subtitles = sectionValues("subtitles");
        for (Map.Entry<String, Object> entry : sectionValues("titles").entrySet()) {
            long warning; try { warning = Long.parseLong(entry.getKey()); } catch (NumberFormatException e) { continue; }
            if (warning <= seconds && entry.getValue() instanceof String title) { String subtitle = String.valueOf(subtitles.getOrDefault(entry.getKey(), "")); timer.schedule(new TimerTask() { public void run() { if (!canceled.contains(when)) runTask(() -> showTitle(title, subtitle)); }}, (seconds - warning) * 1000L); }
        }
        for (int countdown = 5; countdown >= 1 && countdown <= seconds; countdown--) { int left = countdown; timer.schedule(new TimerTask() { public void run() { if (!canceled.contains(when)) runTask(() -> showCountdown(left)); }}, (seconds - left) * 1000L); }
        timer.schedule(new TimerTask() { public void run() { if (!canceled.contains(when)) runTask(() -> executeRestart(oneTime)); scheduled.remove(when); canceled.remove(when); if (!oneTime) schedule(source, false); }}, seconds * 1000L);
        if (announce) plugin.getLogger().info("Auto restart scheduled: " + source); return true;
    }
    private void executeRestart(boolean oneTime) { showCountdown(0); if (oneTime) saveOneTime(""); for (String command : plugin.getConfig().getStringList("auto-restart.commands")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command); }
    public boolean blindnessEnabled() { return plugin.getConfig().getBoolean("auto-restart.blindness.enabled", true); }
    public void setBlindness(boolean enabled) { plugin.getConfig().set("auto-restart.blindness.enabled", enabled); plugin.saveConfig(); }
    public boolean cancelNext() { ZonedDateTime next = getNext(); if (next == null) return false; canceled.add(next); return true; }
    public boolean resumeNext() { ZonedDateTime next = getNext(); return next != null && canceled.remove(next); }
    public void clear() { scheduled.clear(); canceled.clear(); plugin.getConfig().set("auto-restart.restart-times", List.of()); plugin.saveConfig(); saveOneTime(""); resetTimer(); }
    public ZonedDateTime getNext() { return scheduled.stream().min(ZonedDateTime::compareTo).orElse(null); }
    public Set<ZonedDateTime> getScheduled() { return new HashSet<>(scheduled); }
    public Set<ZonedDateTime> getCanceled() { return new HashSet<>(canceled); }
    public void saveOneTime(String value) { plugin.getConfig().set("auto-restart.one-time-restart", value); plugin.saveConfig(); }
    public void reload() { scheduled.clear(); canceled.clear(); resetTimer(); load(); }
    public void shutdown() { timer.cancel(); }
    private ZonedDateTime parseRecurring(String value) { String[] parts = value.split(";", -1); if (parts.length != 2) throw new IllegalArgumentException(); int[] time = time(parts[1]); ZonedDateTime now = now(), result = now.withHour(time[0]).withMinute(time[1]).withSecond(0).withNano(0); int target = parts[0].equalsIgnoreCase("Daily") ? 0 : java.time.DayOfWeek.valueOf(parts[0].toUpperCase()).getValue(); int days = target == 0 ? (result.isAfter(now) ? 0 : 1) : (target - now.getDayOfWeek().getValue() + 7) % 7; if (target != 0 && days == 0 && !result.isAfter(now)) days = 7; return result.plusDays(days); }
    private ZonedDateTime parseDate(String value) { String[] parts = value.split(" ", -1); if (parts.length != 2) return parseOnce(value); String[] date = parts[0].split("-", -1), clock = parts[1].split(":", -1); if (date.length != 3 || clock.length < 2 || clock.length > 3) throw new IllegalArgumentException(); return ZonedDateTime.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]), Integer.parseInt(clock[0]), Integer.parseInt(clock[1]), clock.length == 3 ? Integer.parseInt(clock[2]) : 0, 0, SERVER_ZONE); }
    private ZonedDateTime parseOnce(String value) { int[] time = time(value); ZonedDateTime result = now().withHour(time[0]).withMinute(time[1]).withSecond(0).withNano(0); if (!result.isAfter(now())) throw new IllegalArgumentException(); return result; }
    private int[] time(String value) { String[] parts = value.split(":", -1); if (parts.length != 2) throw new IllegalArgumentException(); int hour = Integer.parseInt(parts[0]), minute = Integer.parseInt(parts[1]); if (hour < 0 || hour > 23 || minute < 0 || minute > 59) throw new IllegalArgumentException(); return new int[] {hour, minute}; }
    private Map<String, Object> sectionValues(String name) { var section = plugin.getConfig().getConfigurationSection("auto-restart." + name); return section == null ? Map.of() : section.getValues(false); }
    private void showTitle(String title, String subtitle) { Bukkit.getOnlinePlayers().forEach(p -> { p.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle), 10, 40, 10); p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1, 1); }); }
    private void showCountdown(int secondsLeft) { Bukkit.getOnlinePlayers().forEach(p -> { p.sendTitle(ChatColor.DARK_RED + "" + ChatColor.BOLD + "WARNING!", ChatColor.GOLD + "" + ChatColor.BOLD + "Server Restarting... ", 0, 25, 0); if (blindnessEnabled()) p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, false)); }); }
    private ZonedDateTime now() { return ZonedDateTime.now(SERVER_ZONE); }
    private void resetTimer() { timer.cancel(); timer = new Timer(true); }
    private void runTask(Runnable task) { Bukkit.getScheduler().runTask(plugin, task); }
}
