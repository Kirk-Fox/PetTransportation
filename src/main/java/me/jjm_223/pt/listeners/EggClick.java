package me.jjm_223.pt.listeners;

import me.jjm_223.pt.PetTransportation;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Listens for a right click of an egg.
 */

@SuppressWarnings({"unused", "deprecation"})
public class EggClick implements Listener {

    JavaPlugin plugin;

    public EggClick(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    //Respawns stored cat.
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        //Make sure the player is clicking with an item. (Prevents NPE)
        if (event.getItem() != null) {
            //Makes sure they are right clicking, that they have a monster egg in their hand, and that they have permission.
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem().getType() == Material.MONSTER_EGG) {
                //Makes sure the item is what we want.
                if (event.getItem().getItemMeta().getLore() != null) {
                    if (event.getItem().getItemMeta().getLore().size() == 2) {
                        //Make double sure that the item is what we want. (Trying to be compatible with other plugins.)
                        if (DataStorage.config.contains("pets." + event.getItem().getItemMeta().getLore().get(1))) {
                            //Cancel event so the spawn egg doesn't spawn something we don't want.
                            event.setCancelled(true);

                            if (event.getPlayer().hasPermission("pt.restore")) {
                                //Convert string from lore to UUID.
                                String uuidString = event.getItem().getItemMeta().getLore().get(1);
                                UUID uuid = UUID.fromString(uuidString);

                                //Declare Entity for if statement.
                                Entity entity;

                                //Set up the location.
                                double x = event.getClickedBlock().getX();
                                //Make sure entity is spawned in the middle of a block. (Prevents suffocation)
                                x = x + ((int) Math.signum(x) * 0.5);
                                //Set y to either the block above the clicked block.
                                double y = event.getClickedBlock().getRelative(0, 1, 0).getY();
                                double z = event.getClickedBlock().getZ();
                                //Make sure entity is spawned in the middle of a block. (Prevents suffocation)
                                z = z + ((int) Math.signum(x) * 0.5);

                                DataStorage dataStorage = new DataStorage(plugin);

                                //Spawn the appropriate entity.
                                if (event.getItem().getData().getData() == (byte) 98) {
                                    //If the target location is of block air, then spawn the block at the target location. Otherwise, spawn it where the player is standing.
                                    entity = event.getPlayer().getWorld().spawnEntity(new Location(event.getClickedBlock().getWorld(), x, y, z).getBlock().getType() == Material.AIR ? new Location(event.getClickedBlock().getWorld(), x, y, z) : event.getPlayer().getLocation(), EntityType.OCELOT);
                                } else if (event.getItem().getData().getData() == (byte) 95) {
                                    entity = event.getPlayer().getWorld().spawnEntity(new Location(event.getClickedBlock().getWorld(), x, y, z).getBlock().getType() == Material.AIR ? new Location(event.getClickedBlock().getWorld(), x, y, z) : event.getPlayer().getLocation(), EntityType.WOLF);
                                } else {
                                    entity = event.getPlayer().getWorld().spawnEntity(new Location(event.getClickedBlock().getWorld(), x, y, z).getBlock().getType() == Material.AIR ? new Location(event.getClickedBlock().getWorld(), x, y, z) : event.getPlayer().getLocation(), EntityType.HORSE);
                                }

                                //Remove egg from hand.
                                event.getPlayer().getInventory().setItemInHand(new ItemStack(Material.AIR));

                                //Try to restore the pet, this shouldn't fail. More of a debug thing.
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
            }
        }
    }
}
