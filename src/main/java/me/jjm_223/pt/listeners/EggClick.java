package me.jjm_223.pt.listeners;

import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Listens for a right click of an egg.
 */

public class EggClick implements Listener {

    JavaPlugin plugin;

    public EggClick(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().hasPermission("pt.restore")
                && event.getItem() != null
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getItem().getType() == Material.MONSTER_EGG
                && event.getItem().getItemMeta().getLore() != null
                && event.getItem().getItemMeta().getLore().size() == 2
                && DataStorage.config.contains("pets." + event.getItem().getItemMeta().getLore().get(1))) {
            event.setCancelled(true);

            if (event.getPlayer().hasPermission("pt.restore")) {
                // Convert string from lore to UUID.
                String uuidString = event.getItem().getItemMeta().getLore().get(1);
                UUID uuid = UUID.fromString(uuidString);

                // Set up the location.
                double x = event.getClickedBlock().getX();
                // Make sure entity is spawned in the middle of a block. (Prevents suffocation)
                x += 0.5;
                // Set y to the block above the clicked block.
                double y = event.getClickedBlock().getRelative(0, 1, 0).getY();
                double z = event.getClickedBlock().getZ();
                z += 0.5;

                DataStorage dataStorage = new DataStorage(plugin);

                // Create spawn location.
                Location spawnLoc = new Location(event.getClickedBlock().getWorld(), x, y, z);

                // If location is not air, spawn it where the player is. (Prevents suffocation)
                if (spawnLoc.getBlock().getType() != Material.AIR) {
                    spawnLoc = event.getPlayer().getLocation();
                }

                // Get spawn egg type.
                EntityType type = dataStorage.identifyPet(uuid.toString());
                Entity entity = event.getClickedBlock().getWorld().spawnEntity(spawnLoc, type);

                // Remove egg from hand.
                event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                // Try to restore the pet, this shouldn't fail. More of a debug thing.
                try {
                    dataStorage.restorePet(entity, uuid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to use that.");
            }
        }
    }
}