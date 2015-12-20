package me.jjm_223.pt.listeners;

import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Listens for an egg hit.
 */

@SuppressWarnings("unused")
public class EggHit implements Listener {

    private JavaPlugin plugin;

    public EggHit(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // When an entity is damaged by another entity...
    @EventHandler
    public void onEgg(EntityDamageByEntityEvent event) {
        // Make sure damager is an egg projectile, make sure the shooter is a player (safety first), make sure the victim is a pet, and make sure the pet has an owner.
        if (event.getDamager() instanceof Projectile
                && event.getDamager().getType() == EntityType.EGG
                && ((Projectile) event.getDamager()).getShooter() instanceof Player
                && (event.getEntity() instanceof Wolf || event.getEntity() instanceof Ocelot || event.getEntity() instanceof Horse)
                && ((Tameable) event.getEntity()).getOwner() != null) {

            Player player = (Player) ((Projectile) event.getDamager()).getShooter();

            // Make sure that either the shooter is the owner of the pet, or the player has permission to override this, and the player has permission to capture mobs.
            if ((((Tameable) event.getEntity()).getOwner().getUniqueId().equals(player.getUniqueId()) || player.hasPermission("pt.override"))
                    && player.hasPermission("pt.capture")) {

                // Cancel the event, we don't want them to get hurt, do we? (I hope you didn't answer yes D:)
                event.setCancelled(true);

                DataStorage storage = new DataStorage(plugin);
                // Generate a random UUID to identify the pet in the config.
                UUID storageID = UUID.randomUUID();

                // Create a new itemstack. If the entity is an ocelot, give it a data value of 98, if it is a wolf give it a data value of 95 (wolf), otherwise give it a data value of 100 (horse).
                byte type;
                if (event.getEntityType() == EntityType.OCELOT) {
                    type = 98;
                } else if (event.getEntityType() == EntityType.WOLF) {
                    type = 95;
                } else {
                    type = 100;
                }
                ItemStack item = new ItemStack(Material.MONSTER_EGG, 1, type);

                List<String> lore = new ArrayList<String>();
                String animalName;
                if (event.getEntityType() == EntityType.OCELOT) {
                    animalName = "Cat";
                } else if (event.getEntityType() == EntityType.WOLF) {
                    animalName = "Dog";
                } else {
                    animalName = "Horse";
                }
                lore.add(event.getEntity().getCustomName() != null ? ChatColor.ITALIC + event.getEntity().getCustomName() : ChatColor.ITALIC + animalName);
                // Add the UUID to the second line as identification when being respawned.
                lore.add(storageID.toString());
                // Add lore to metadata.
                ItemMeta meta = item.getItemMeta();
                meta.setLore(lore);
                item.setItemMeta(meta);

                // Drop inventory contents of horse.
                if (event.getEntityType() == EntityType.HORSE) {
                    Horse horse = (Horse) event.getEntity();
                    for (ItemStack inventoryItem : horse.getInventory().getContents()) {
                        if (inventoryItem != null) {
                            horse.getWorld().dropItemNaturally(horse.getLocation(), inventoryItem);
                        }
                    }
                }

                // Drop the spawn egg with the data where the pet is sitting/standing.
                player.getWorld().dropItemNaturally(event.getEntity().getLocation(), item);

                // Remove pet from world.
                event.getEntity().remove();

                // Attempt to save the pet. This should not fail, as it is only for debug.
                try {
                    storage.savePet(event.getEntity(), storageID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            // If the pet that was supposed to be captured was not owned by the egg thrower (and they don't have bypass perms), tell them off.
            } else {
                player.sendMessage(ChatColor.RED + "You can't capture that pet. It isn't yours! (Or you don't have permission)");
            }
        }
    }
}
