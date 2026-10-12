package com.stillthinkingofaname.welcome;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class UpdateChecker implements CommandExecutor {
    public static final String UPDATE_PERMISSION = "masihmikirnamaplugin.update";
    private static final String RELEASE_URL = "https://github.com/fadhlanel/Still-Thinking-of-a-Name/releases/latest";
    private static final String API_URL = "https://api.github.com/repos/fadhlanel/Still-Thinking-of-a-Name/releases/latest";
    private static final Pattern TAG_PATTERN = Pattern.compile("\\\"tag_name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private final JavaPlugin plugin;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private volatile String latestVersion;
    private volatile boolean checking;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        check();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::check, 20L * 60L * 60L * 6L, 20L * 60L * 60L * 6L);
    }

    public void check() {
        if (checking) return;
        checking = true;
        plugin.getLogger().info("Checking for plugin updates...");
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "MasihMikirNamaPlugin")
                .GET()
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200 ? response.body() : "")
                .thenAccept(body -> {
                    Matcher matcher = TAG_PATTERN.matcher(body);
                    if (matcher.find()) {
                        latestVersion = normalize(matcher.group(1));
                        plugin.getLogger().info("Plugin update check complete. Latest version: v" + latestVersion);
                    } else {
                        plugin.getLogger().warning("Plugin update check returned no release version.");
                    }
                })
                .exceptionally(error -> { plugin.getLogger().warning("Plugin update check failed: " + error.getMessage()); return null; })
                .whenComplete((ignored, error) -> checking = false);
    }

    public void notifyIfAvailable(Player player) {
        if (isUpdateAvailable() && player.hasPermission(UPDATE_PERMISSION)) {
            player.sendMessage(ChatColor.GOLD + "[MasihMikirNamaPlugin] Versi baru tersedia: " + ChatColor.YELLOW + "v" + latestVersion);
            player.sendMessage(ChatColor.GRAY + "Download: " + ChatColor.WHITE + RELEASE_URL);
        }
    }

    public boolean isUpdateAvailable() {
        return latestVersion != null && compare(latestVersion, normalize(plugin.getDescription().getVersion())) > 0;
    }

    public String getCurrentVersion() {
        return normalize(plugin.getDescription().getVersion());
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getReleaseUrl() {
        return RELEASE_URL;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(UPDATE_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        if (latestVersion == null) {
            sender.sendMessage(ChatColor.YELLOW + "Belum ada informasi versi terbaru. Cek sedang dilakukan.");
            check();
            return true;
        }
        sender.sendMessage(ChatColor.GRAY + "Current version: " + ChatColor.WHITE + "v" + getCurrentVersion());
        sender.sendMessage(ChatColor.GRAY + "Latest version: " + ChatColor.WHITE + "v" + latestVersion);
        if (isUpdateAvailable()) sender.sendMessage(ChatColor.GRAY + "Download: " + ChatColor.WHITE + RELEASE_URL);
        else sender.sendMessage(ChatColor.GREEN + "Plugin sudah menggunakan versi terbaru.");
        return true;
    }

    private static String normalize(String version) {
        return version.toLowerCase().startsWith("v") ? version.substring(1) : version;
    }

    private static int compare(String left, String right) {
        String[] a = left.split("\\.");
        String[] b = right.split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? number(a[i]) : 0;
            int y = i < b.length ? number(b[i]) : 0;
            if (x != y) return Integer.compare(x, y);
        }
        return 0;
    }

    private static int number(String value) {
        Matcher matcher = Pattern.compile("\\d+").matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }
}
