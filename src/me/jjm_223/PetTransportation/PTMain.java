package me.jjm_223.PetTransportation;

import me.jjm_223.PetTransportation.listeners.EggClick;
import me.jjm_223.PetTransportation.listeners.EggHit;
import me.jjm_223.PetTransportation.listeners.ItemDespawn;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for PetTransportation.
 * It's hard coming up with the name of the main class ;_;
 */
public class PTMain extends JavaPlugin {

    @Override
    public void onEnable() {
        //Save the default config file.
        saveDefaultConfig();

        //Register relevant events.
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new EggHit(), this);
        pm.registerEvents(new EggClick(), this);
        pm.registerEvents(new ItemDespawn(), this);
    }

    @Override
    public void onDisable() {
        //Save the config on disable.
        saveConfig();
    }

}
