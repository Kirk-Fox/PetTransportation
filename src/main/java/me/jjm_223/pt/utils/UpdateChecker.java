package me.jjm_223.pt.utils;

import me.jjm_223.pt.PetTransportation;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    private final PetTransportation plugin;
    private final int resourceId;

    public UpdateChecker(PetTransportation plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getLatestVersion(Consumer<String> consumer) {
        new BukkitRunnable() {
            public void run() {
                try (InputStream inputStream = new URL(
                        "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openStream();
                     Scanner scanner = new Scanner(inputStream)) {
                    if (scanner.hasNext()) {
                        consumer.accept(scanner.next());
                    }
                } catch (IOException e) {
                    plugin.outputLog("Cannot look for updates: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

}
