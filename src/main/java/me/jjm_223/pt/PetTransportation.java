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
 * Main class for PetTransportation.
 */
public class PetTransportation extends JavaPlugin {

    private DataStorage storage;

    @Override
    public void onEnable() {
        storage = new DataStorage(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new EggHit(this), this);
        pm.registerEvents(new EggClick(this), this);
        pm.registerEvents(new ItemDespawn(this), this);
    }

    @Override
    public void onDisable() {
        try {
            storage.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataStorage getStorage()
    {
        return this.storage;
    }
}
