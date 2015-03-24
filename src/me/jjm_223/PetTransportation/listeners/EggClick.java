package me.jjm_223.PetTransportation.listeners;

import me.jjm_223.PetTransportation.PTMain;
import me.jjm_223.PetTransportation.utils.DataStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Listens for a right click of an egg.
 */

@SuppressWarnings({"unused", "deprecation"})
public class EggClick implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() != null) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem().getType() == Material.MONSTER_EGG && event.getPlayer().hasPermission("pt.restore")) {
                if (event.getItem().getItemMeta().getLore() != null) {
                    if (event.getItem().getItemMeta().getLore().size() == 2) {
                        event.setCancelled(true);
                        String uuidString = event.getItem().getItemMeta().getLore().get(1);
                        UUID uuid = UUID.fromString(uuidString);

                        Entity entity;
                        int x = event.getClickedBlock().getX();
                        int y = event.getClickedBlock().getWorld().getHighestBlockYAt(event.getClickedBlock().getLocation());
                        int z = event.getClickedBlock().getZ();

                        DataStorage dataStorage = new DataStorage(PTMain.getPlugin(PTMain.class).getConfig());

                        if (event.getItem().getData().getData() == (byte) 98) {
                            entity = event.getPlayer().getWorld().spawnEntity(new Location(event.getClickedBlock().getWorld(), x, y, z), EntityType.OCELOT);
                        } else {
                            entity = event.getPlayer().getWorld().spawnEntity(new Location(event.getClickedBlock().getWorld(), x, y, z), EntityType.WOLF);
                        }

                        event.getPlayer().getInventory().setItemInHand(new ItemStack(Material.AIR));

                        try {
                            dataStorage.restorePet(entity, uuid);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
