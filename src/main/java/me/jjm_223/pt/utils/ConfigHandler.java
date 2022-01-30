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

    /**
     * Checks if the config allows a mob to be captured.
     * For example, if the config uses the blacklist as a whitelist,
     * and the mob is not on that list, this returns false.
     *
     * @param mob the mob being checked
     * @return if the mob is allowed by the config
     */
    public boolean canCapture(Mob mob) {
        // Return false if mob is an axolotl or fish (because of the vanilla method of capturing these mobs).
        // Return false if mob is on blacklist or if mob is not on whitelist.
        if (mob instanceof Axolotl || mob instanceof Fish || (blacklist.contains(mob.getType()) ^ whitelist))
            return false;
        if (onlyPets) return (mob instanceof Tameable && ((Tameable) mob).isTamed())
                || (mob instanceof Fox && ((Fox) mob).getFirstTrustedPlayer() != null);
        return captureMonsters || !(mob instanceof Monster);
    }
}
