package me.jjm_223.PetTransportation;

import me.jjm_223.PetTransportation.listeners.EggClick;
import me.jjm_223.PetTransportation.listeners.EggHit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for PetTransportation.
 * It's hard coming up with the name of the main class ;_;
 */
public class PTMain extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new EggHit(), this);
        pm.registerEvents(new EggClick(), this);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

}
