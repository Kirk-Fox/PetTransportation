package me.jjm_223.PetTransportation.listeners;

import me.jjm_223.PetTransportation.PTMain;
import me.jjm_223.PetTransportation.utils.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Listens for an egg hit.
 */

@SuppressWarnings("unused")
public class EggHit implements Listener {
    @EventHandler
    public void onEgg(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Projectile
                && event.getDamager().getType() == EntityType.EGG
                && ((Projectile) event.getDamager()).getShooter() instanceof Player
                && event.getEntity() instanceof Tameable
                && ((Tameable) event.getEntity()).getOwner() != null) {

            Player player = (Player) ((Projectile) event.getDamager()).getShooter();

            if ((((Tameable) event.getEntity()).getOwner().getUniqueId().equals(player.getUniqueId()) || player.hasPermission("pt.override"))
                    && player.hasPermission("pt.capture")) {

                event.setCancelled(true);

                DataStorage storage = new DataStorage(PTMain.getPlugin(PTMain.class).getConfig());
                UUID storageID = UUID.randomUUID();

                ItemStack item = new ItemStack(Material.MONSTER_EGG, 1, event.getEntity().getType() == EntityType.OCELOT ? (short) 98 : (short) 95);

                List<String> lore = new ArrayList<String>();
                lore.add(event.getEntity().getCustomName() != null ? ChatColor.ITALIC + event.getEntity().getCustomName() : event.getEntity().getType() == EntityType.OCELOT ? ChatColor.ITALIC + "Cat" : ChatColor.ITALIC + "Dog");
                lore.add(storageID.toString());
                ItemMeta meta = item.getItemMeta();
                meta.setLore(lore);
                item.setItemMeta(meta);

                player.getWorld().dropItemNaturally(event.getEntity().getLocation(), item);

                event.getEntity().remove();

                try {
                    storage.savePet(event.getEntity(), storageID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                player.sendMessage(ChatColor.RED + "You can't capture that pet. It isn't yours!");
            }
        }
    }
}
