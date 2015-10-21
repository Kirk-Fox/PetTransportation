package me.jjm_223.pt;

import me.jjm_223.pt.listeners.EggClick;
import me.jjm_223.pt.listeners.EggHit;
import me.jjm_223.pt.listeners.ItemDespawn;
import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Main class for pt.
 */
public class PetTransportation extends JavaPlugin {

    @Override
    public void onEnable() {
        //Register relevant events.
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new EggHit(this), this);
        pm.registerEvents(new EggClick(this), this);
        pm.registerEvents(new ItemDespawn(this), this);
    }

    @Override
    public void onDisable() {
        try {
            DataStorage.config.save(new File(this.getDataFolder() + File.separator + "pets.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
