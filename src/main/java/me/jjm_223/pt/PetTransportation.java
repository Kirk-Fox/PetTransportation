package me.jjm_223.pt;

import me.jjm_223.pt.listeners.*;
import me.jjm_223.pt.utils.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

/**
 * Main class for PetTransportation.
 */
public class PetTransportation extends JavaPlugin {

    private DataStorage storage;
    private ConfigHandler configHandler;

    private static final int BSTATS_ID = 14000;

    @Override
    public void onEnable() {
        int serverVersion = Integer.parseInt(getServer().getVersion().split("\\.")[1]);

        storage = new DataStorage(this, serverVersion);
        configHandler = new ConfigHandler(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new EggHit(this, serverVersion), this);
        pm.registerEvents(new EggClick(this, serverVersion), this);
        pm.registerEvents(new ItemDespawn(this), this);

        new Metrics(this, BSTATS_ID);
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

    public ConfigHandler getConfigHandler() {
        return this.configHandler;
    }
}
