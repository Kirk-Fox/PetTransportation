package me.jjm_223.pt.listeners;

import me.jjm_223.pt.PetTransportation;
import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Listens for a right click of an egg.
 */

public class EggClick implements Listener {

    private final PetTransportation plugin;
    private final int serverVersion;

    public EggClick(PetTransportation plugin, int serverVersion) {
        this.plugin = plugin;
        this.serverVersion = serverVersion;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("pt.restore")
                && event.getItem() != null
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && (event.getItem().getType().toString().endsWith("SPAWN_EGG")
                || event.getItem().getType().toString().equals("MONSTER_EGG"))
                && event.getItem().getItemMeta() != null
                && event.getItem().getItemMeta().getLore() != null
                && event.getItem().getItemMeta().getLore().size() == 2
                && plugin.getStorage().contains(event.getItem().getItemMeta().getLore().get(1))) {
            event.setCancelled(true);

            if (player.hasPermission("pt.restore")) {
                String uuidString = event.getItem().getItemMeta().getLore().get(1);
                UUID uuid = UUID.fromString(uuidString);
                final Block clickedBlock = event.getClickedBlock();

                assert clickedBlock != null;
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
                LivingEntity mob;
                if (serverVersion > 11) {
                    mob = (LivingEntity) clickedBlock.getWorld().spawn(spawnLoc, type.getEntityClass(),
                            (m) -> dataStorage.restorePet((LivingEntity) m, uuid));
                } else {
                    mob = (LivingEntity) clickedBlock.getWorld().spawnEntity(spawnLoc, type);
                    new BukkitRunnable() {
                        public void run() {
                            dataStorage.restorePet(mob, uuid);
                        }
                    }.runTaskLater(plugin, 2L);
                }

                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to use that.");
            }
        }
    }
}