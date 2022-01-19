package me.jjm_223.pt.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class ConfigHandler {

    private final boolean onlyPets;
    private final boolean captureMonsters;
    private final Set<EntityType> blacklist = new HashSet<>();

    public ConfigHandler(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        ConfigurationSection config = plugin.getConfig();
        onlyPets = config.getBoolean("capture-pets-only");
        captureMonsters = config.getBoolean("capture-monsters");
        config.getStringList("mob-blacklist").forEach(m -> blacklist.add(EntityType.valueOf(m.toUpperCase())));
    }

    public boolean canCapture(Mob m) {
        if (onlyPets) return m instanceof Tameable && ((Tameable) m).isTamed();
        if (!captureMonsters) return !(m instanceof Monster);
        return !(blacklist.contains(m.getType()) || m instanceof Axolotl || m instanceof Fish);
    }
}
