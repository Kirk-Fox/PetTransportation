package me.jjm_223.pt.listeners;

import me.jjm_223.pt.PetTransportation;
import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Listens for a right click of an egg.
 */

public class EggClick implements Listener {

    private final PetTransportation plugin;

    public EggClick(PetTransportation plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("pt.restore")
                && event.getItem() != null
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getItem().getType().toString().endsWith("SPAWN_EGG")
                && event.getItem().getItemMeta().getLore() != null
                && event.getItem().getItemMeta().getLore().size() == 2
                && plugin.getStorage().contains(event.getItem().getItemMeta().getLore().get(1))) {
            event.setCancelled(true);

            if (player.hasPermission("pt.restore")) {
                String uuidString = event.getItem().getItemMeta().getLore().get(1);
                UUID uuid = UUID.fromString(uuidString);
                final Block clickedBlock = event.getClickedBlock();

                double x = clickedBlock.getX();
                // Make sure entity is spawned in the middle of a block.
                x += 0.5;
                // Set y to the block above the clicked block.
                double y = clickedBlock.getRelative(0, 1, 0).getY();
                double z = clickedBlock.getZ();
                z += 0.5;

                DataStorage dataStorage = plugin.getStorage();

                // Create spawn location.
                Location spawnLoc = new Location(clickedBlock.getWorld(), x, y, z);

                // If location is not air, spawn it where the player is.
                if (spawnLoc.getBlock().getType() != Material.AIR) {
                    spawnLoc = player.getLocation();
                }

                EntityType type = dataStorage.identifyPet(uuid.toString());
                Entity entity = clickedBlock.getWorld().spawnEntity(spawnLoc, type);
                if (entity == null)
                {
                    player.sendMessage(ChatColor.RED + "Cannot restore pet to this location.");
                    return;
                }

                boolean isSurvival = player.getGameMode() != GameMode.CREATIVE;

                if (isSurvival) {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }

                dataStorage.restorePet(entity, uuid, isSurvival);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to use that.");
            }
        }
    }
}