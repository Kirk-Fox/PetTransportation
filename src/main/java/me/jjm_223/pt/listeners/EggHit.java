package me.jjm_223.pt.listeners;

import me.jjm_223.pt.PetTransportation;
import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Listens for an egg hit.
 */

public class EggHit implements Listener {

    private final PetTransportation plugin;
    private final int serverVersion;

    public EggHit(PetTransportation plugin, int serverVersion) {
        this.plugin = plugin;
        this.serverVersion = serverVersion;
    }

    // When a projectile hits an object...
    @EventHandler(ignoreCancelled = true)
    public void onEgg(ProjectileHitEvent event) {
        if (serverVersion > 10) {
            // Make sure the projectile is an egg, the shooter is a player (safety first),
            // the egg hit an entity, and that entity is a LivingEntity.
            if (event.getEntityType() == EntityType.EGG
                    && event.getEntity().getShooter() instanceof Player
                    && event.getHitEntity() != null
                    && event.getHitEntity() instanceof LivingEntity) {

                Player player = (Player) event.getEntity().getShooter();
                LivingEntity mob = (LivingEntity) event.getHitEntity();

                // Make sure the player has permission to capture, the player can capture this specific mob,
                // and the configuration allows this mob to be captured.
                if (player.hasPermission("pt.capture")) {
                    if (canPlayerCapture(player, mob)) {
                        if(plugin.getConfigHandler().canCapture(mob)) captureMob(event, player, mob);
                    } else {
                        // If the pet that was supposed to be captured was not owned by the egg thrower (and they don't have bypass perms), tell them off.
                        player.sendMessage(ChatColor.RED + (isOwner(player, mob)
                                ? "You don't have permission to capture this mob type!"
                                : "You can't capture that pet. It's not yours!"));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use PetTransportation!");
                }
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEggDamage(EntityDamageByEntityEvent event) {
        // Make sure the damaging entity is an egg, the ProjectileSource is a player, and the entity is a LivingEntity.
        if (event.getDamager().getType() == EntityType.EGG
                && ((Projectile) event.getDamager()).getShooter() instanceof Player
                && event.getEntity() instanceof LivingEntity) {

            Player player = (Player) ((Projectile) event.getDamager()).getShooter();
            LivingEntity mob = (LivingEntity) event.getEntity();
            assert player != null;

            // Make sure the player has permission to capture, the player can capture this specific mob,
            // and the configuration allows this mob to be captured.
            if (player.hasPermission("pt.capture")) {
                if (canPlayerCapture(player, mob)) {
                    if (plugin.getConfigHandler().canCapture(mob)) {
                        if (serverVersion > 10) {
                            // Cancel damage to pet on versions before 1.16
                            event.setCancelled(true);
                            return;
                        }
                        // Replace missing ProjectileHitEvent on versions before 1.11
                        captureMob(event, player, mob);
                    }
                } else {
                    if (serverVersion < 11) player.sendMessage(ChatColor.RED + (isOwner(player, mob)
                            ? "You don't have permission to capture this mob type!"
                            : "You can't capture that pet. It's not yours!"));
                }
            } else {
                if (serverVersion < 11) player.sendMessage(ChatColor.RED + "You don't have permission to use PetTransportation!");
            }
        }
    }

    // Method called by one of the above events (depending on the version) to capture the mob
    private void captureMob(EntityEvent event, Player player, LivingEntity mob) {
        // On versions before 1.16, ProjectileHitEvent isn't cancellable.
        if (event instanceof Cancellable) {
            // Cancel the event, we don't want them to get hurt, do we? (I hope you didn't answer yes D:)
            ((Cancellable) event).setCancelled(true);
            // Remove the egg. (Otherwise it will pass right through the mob)
            if (event instanceof ProjectileHitEvent) event.getEntity().remove();
        }

        DataStorage storage = plugin.getStorage();
        // Generate a random UUID to identify the pet in the config.
        UUID storageID = UUID.randomUUID();

        String itemName;
        // Assign the name of the spawn egg that will be given with special cases.
        switch (mob.getType()) {
            case MUSHROOM_COW:
                // There is no "MUSHROOM_COW_SPAWN_EGG" item.
                itemName = "MOOSHROOM_SPAWN_EGG";
                break;
            case SNOWMAN:
                // Will name the egg "Snow Golem Spawn Egg" instead of "Snowman Spawn Egg"
                itemName = "SNOW_GOLEM_SPAWN_EGG";
                break;
            default:
                itemName = mob.getType() + "_SPAWN_EGG";
                break;
        }
        Material spawnEgg = Material.getMaterial(itemName);
        Material wolfSpawnEgg = Material.getMaterial("WOLF_SPAWN_EGG");
        // If this mob type has a spawn egg, create that egg. Otherwise, create a wolf egg.
        // On versions prior to 1.13, creates a blank spawn egg to be manipulated.
        ItemStack item = new ItemStack((spawnEgg != null) ? spawnEgg
                : (wolfSpawnEgg != null) ? wolfSpawnEgg : Material.getMaterial("MONSTER_EGG"), 1);

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (serverVersion > 10 && serverVersion < 13) {
            ((SpawnEggMeta) meta).setSpawnedType(mob.getType());
        }
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
        meta.setLore(lore);

        // If this mob type has no corresponding spawn egg, rename the default egg.
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

    // Checks permissions and pet ownership to determine if a specified player can capture a mob.
    private boolean canPlayerCapture(Player player, LivingEntity mob) {
        // If the mob is a monster, return the relevant player permission.
        if (mob instanceof Monster) return player.hasPermission("pt.capture.monster");

        // If the mob is a pet (tameable or a fox) then check the pet's ownership and player permissions.
        if (mob instanceof Tameable && player.hasPermission("pt.capture.pets")) {
            if (player.hasPermission("pt.override")) return true;
            Tameable t = (Tameable) mob;
            if (t.getOwner() != null) return t.getOwner().getUniqueId().equals(player.getUniqueId());
        } else if (mob.getType().name().equals("FOX") && player.hasPermission("pt.capture.pets")) {
            if (player.hasPermission("pt.override")) return true;
            Fox f = (Fox) mob;
            if (f.getFirstTrustedPlayer() != null) {
                return f.getFirstTrustedPlayer().getUniqueId().equals(player.getUniqueId())
                        || (f.getSecondTrustedPlayer() != null
                        && f.getSecondTrustedPlayer().getUniqueId().equals(player.getUniqueId()));
            }
        }

        // If the mob is an untamed pet or a passive mob, return the relevant player permission.
        return player.hasPermission("pt.capture.passive");
    }

    // Checks to see if the player "owns" the pet or if the mob isn't a pet.
    private boolean isOwner(Player player, LivingEntity mob) {
        if (player.hasPermission("pt.override")) return true;
        if (mob instanceof Tameable) {
            Tameable t = (Tameable) mob;
            return t.getOwner() != null && t.getOwner().getUniqueId().equals(player.getUniqueId());
        }
        if (mob.getType().name().equals("FOX")) {
            Fox f = (Fox) mob;
            return f.getFirstTrustedPlayer() != null
                    && (f.getFirstTrustedPlayer().getUniqueId().equals(player.getUniqueId())
                    || (f.getSecondTrustedPlayer() != null
                    && f.getSecondTrustedPlayer().getUniqueId().equals(player.getUniqueId())));
        }
        return true;
    }
}
