package me.jjm_223.pt.listeners;

import me.jjm_223.pt.PetTransportation;
import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Listens for an egg hit.
 */

public class EggHit implements Listener {

    private final PetTransportation plugin;

    //TODO prevent baby chicken spawns when capturing pets

    public EggHit(PetTransportation plugin) {
        this.plugin = plugin;
    }

    // When an entity is damaged by another entity...
    @EventHandler(ignoreCancelled = true)
    public void onEgg(EntityDamageByEntityEvent event) {
        // Make sure damager is an egg projectile, make sure the shooter is a player (safety first), make sure the victim is a pet, and make sure the pet has an owner.
        if (event.getDamager() instanceof Projectile
                && event.getDamager().getType() == EntityType.EGG
                && ((Projectile) event.getDamager()).getShooter() instanceof Player
                && event.getEntity() instanceof Mob) {

            Player player = (Player) ((Projectile) event.getDamager()).getShooter();
            Mob mob = (Mob) event.getEntity();

            // Make sure that either the shooter is the owner of the pet, or the player has permission to override this, and the player has permission to capture mobs.
            if (player.hasPermission("pt.capture")) {
                if (canPlayerCapture(player, mob)) {
                    if (plugin.getConfigHandler().canCapture(mob)) {
                        // Cancel the event, we don't want them to get hurt, do we? (I hope you didn't answer yes D:)
                        event.setCancelled(true);

                        DataStorage storage = plugin.getStorage();
                        // Generate a random UUID to identify the pet in the config.
                        UUID storageID = UUID.randomUUID();

                        // Create a new spawn egg. (The Bukkit API does not allow changing the type of a spawn egg as of 1.9.)
                        String itemName = event.getEntityType() +"_SPAWN_EGG";
                        Material spawnEgg = Material.getMaterial(itemName);
                        ItemStack item = new ItemStack((spawnEgg != null) ? spawnEgg : Material.WOLF_SPAWN_EGG, 1);

                        List<String> lore = new ArrayList<>();
                        String animalName;
                        if (event.getEntityType() == EntityType.WOLF) {
                            animalName = "Dog";
                        } else {
                            animalName = mob.getName();
                        }
                        lore.add(mob.getCustomName() != null ? ChatColor.ITALIC + mob.getCustomName() : ChatColor.ITALIC + animalName);
                        // Add the UUID to the second line as identification when being respawned.
                        lore.add(storageID.toString());
                        // Add lore to metadata.
                        ItemMeta meta = item.getItemMeta();
                        meta.setLore(lore);

                        if (spawnEgg == null) {
                            StringBuilder eggName = new StringBuilder();
                            String[] words = itemName.split("_");
                            for (String s : words) {
                                eggName.append(" ").append(s.charAt(0)).append(s.substring(1).toLowerCase());
                            }
                            meta.setDisplayName("\u00A7f"+eggName.substring(1));
                        }

                        item.setItemMeta(meta);

                        // Drop inventory contents of horse.
                        if (mob instanceof InventoryHolder) {
                            InventoryHolder invHolder = (InventoryHolder) mob;
                            for (ItemStack inventoryItem : invHolder.getInventory().getContents()) {
                                if (inventoryItem != null) {
                                    mob.getWorld().dropItemNaturally(mob.getLocation(), inventoryItem);
                                }
                            }
                        }

                        // Drop the spawn egg with the data where the pet is sitting/standing.
                        player.getWorld().dropItemNaturally(mob.getLocation(), item);

                        // Remove pet from world.
                        mob.remove();

                        // Attempt to save the pet. This should not fail, as it is only for debug.
                        try {
                            storage.savePet(mob, storageID);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // If the pet that was supposed to be captured was not owned by the egg thrower (and they don't have bypass perms), tell them off.
                    player.sendMessage(ChatColor.RED + "You don't have permission to capture that mob! It's either not your pet or a forbidden mob type.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use PetTransportation!");
            }
        }
    }

    private boolean canPlayerCapture(Player player, Mob mob) {
        if (mob instanceof Tameable && player.hasPermission("pt.capture.pets")) {
            Tameable t = (Tameable) mob;
            if (player.hasPermission("pt.override")) return true;
            if (t.getOwner() != null) return t.getOwner().getUniqueId().equals(player.getUniqueId());
        } else if (mob instanceof Fox && player.hasPermission("pt.capture.pets")) {
            Fox f = (Fox) mob;
            if (player.hasPermission("pt.override")) return true;
            if (f.getFirstTrustedPlayer() != null){
                return f.getFirstTrustedPlayer().getUniqueId().equals(player.getUniqueId())
                        || (f.getSecondTrustedPlayer() != null
                        && f.getSecondTrustedPlayer().getUniqueId().equals(player.getUniqueId()));
            }
        } else if (mob instanceof Monster) {
            return player.hasPermission("pt.capture.monster");
        }
        return player.hasPermission("pt.capture.passive");
    }
}
