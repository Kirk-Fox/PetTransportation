package me.jjm_223.pt.listeners;

import me.jjm_223.pt.PetTransportation;
import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
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

    public EggHit(PetTransportation plugin) {
        this.plugin = plugin;
    }

    // When a projectile hits an object...
    @EventHandler(ignoreCancelled = true)
    public void onEgg(ProjectileHitEvent event) {
        // Make sure the projectile is an egg, the shooter is a player (safety first),
        // the egg hit an entity, and that entity is a mob.
        if (event.getEntityType() == EntityType.EGG
                && event.getEntity().getShooter() instanceof Player
                && event.getHitEntity() != null
                && event.getHitEntity() instanceof Mob) {

            Player player = (Player) event.getEntity().getShooter();
            Mob mob = (Mob) event.getHitEntity();

            // Make sure the player has permission to capture, the player can capture this specific mob,
            // and the configuration allows this mob to be captured.
            if (player.hasPermission("pt.capture")) {
                if (canPlayerCapture(player, mob)) {
                    if (plugin.getConfigHandler().canCapture(mob)) {
                        // Cancel the event, we don't want them to get hurt, do we? (I hope you didn't answer yes D:)
                        event.setCancelled(true);
                        // Remove the egg. (Otherwise it will pass right through the mob)
                        event.getEntity().remove();

                        DataStorage storage = plugin.getStorage();
                        // Generate a random UUID to identify the pet in the config.
                        UUID storageID = UUID.randomUUID();

                        String itemName = mob.getType() +"_SPAWN_EGG";
                        Material spawnEgg = Material.getMaterial(itemName);
                        // If this mob type has a spawn egg, create that egg. Otherwise, create a wolf egg.
                        ItemStack item = new ItemStack((spawnEgg != null) ? spawnEgg : Material.WOLF_SPAWN_EGG, 1);

                        List<String> lore = new ArrayList<>();
                        String animalName;
                        if (mob.getType() == EntityType.WOLF) {
                            animalName = "Dog";
                        } else {
                            animalName = mob.getName();
                        }
                        lore.add(mob.getCustomName() != null ? ChatColor.ITALIC + mob.getCustomName() : ChatColor.ITALIC + animalName);
                        // Add the UUID to the second line as identification when being respawned.
                        lore.add(storageID.toString());
                        // Add lore to metadata.
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setLore(lore);

                        // If this mob type has no corresponding spawn egg, rename the default wolf egg.
                        if (spawnEgg == null) {
                            StringBuilder eggName = new StringBuilder();
                            String[] words = itemName.split("_");
                            for (String s : words) {
                                eggName.append(" ").append(s.charAt(0)).append(s.substring(1).toLowerCase());
                            }
                            meta.setDisplayName("\u00A7f"+eggName.substring(1));
                        }

                        item.setItemMeta(meta);

                        // Drop inventory contents of mobs with inventories.
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
                    player.sendMessage(ChatColor.RED + "You don't have permission to capture that mob!" +
                            "It's either not your pet or a forbidden mob type.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use PetTransportation!");
            }
        }
    }

    /**
     * Checks permissions and pet ownership to determine if a specified player can capture a mob.
     *
     * @param player the player attempting to capture the mob
     * @param mob the mob being captured
     * @return if the player can capture the mob
     */
    private boolean canPlayerCapture(Player player, Mob mob) {
        // If the mob is a pet (tameable or a fox) then check the pet's ownership and player permissions.
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
        // If the mob is an untamed pet or a passive mob, return the relevant player permission.
        return player.hasPermission("pt.capture.passive");
    }
}
