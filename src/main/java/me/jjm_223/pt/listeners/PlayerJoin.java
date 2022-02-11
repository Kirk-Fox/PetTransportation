package me.jjm_223.pt.listeners;

import me.jjm_223.pt.PetTransportation;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final PetTransportation plugin;

    public PlayerJoin(PetTransportation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getNewVersion() != null && event.getPlayer().hasPermission("pt.updatecheck"))
            event.getPlayer().sendMessage(ChatColor.AQUA + "There is a new version of PetTransportation available!",
                    ChatColor.AQUA + "Download version " + ChatColor.GOLD + plugin.getNewVersion()
                            + ChatColor.AQUA + " of the plugin at:",
                    ChatColor.GOLD + "https://www.spigotmc.org/resources/pettransportation.99647");
    }

}
