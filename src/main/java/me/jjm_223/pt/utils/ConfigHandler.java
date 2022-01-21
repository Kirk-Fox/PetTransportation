package me.jjm_223.pt.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class ConfigHandler {

    private final boolean onlyPets;
    private final boolean captureMonsters;
    private final boolean whitelist;
    private final Set<EntityType> blacklist = new HashSet<>();

    public ConfigHandler(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        ConfigurationSection config = plugin.getConfig();
        onlyPets = config.getBoolean("capture-pets-only");
        captureMonsters = config.getBoolean("capture-monsters");
        whitelist = config.getBoolean("use-blacklist-as-whitelist");
        config.getStringList("mob-blacklist").forEach(m -> blacklist.add(EntityType.valueOf(m.toUpperCase())));
    }

    public boolean canCapture(Mob m) {
        if (blacklist.contains(m.getType()) ^ whitelist) return false;
        if (onlyPets) return (m instanceof Tameable && ((Tameable) m).isTamed())
                || (m instanceof Fox && ((Fox) m).getFirstTrustedPlayer() != null);
        if (!captureMonsters) return !(m instanceof Monster);
        return !(m instanceof Axolotl || m instanceof Fish);
    }
}
