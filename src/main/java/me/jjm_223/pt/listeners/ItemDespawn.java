package me.jjm_223.pt.listeners;

import me.jjm_223.pt.PetTransportation;
import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Helps config cleanup by keeping track of despawns and removals.
 */

@SuppressWarnings("unused")
public class ItemDespawn implements Listener {

    private DataStorage storage;

    public ItemDespawn(PetTransportation plugin) {
        storage = plugin.getStorage();
    }

    // Called when an item despawns.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDespawn(ItemDespawnEvent event) {
        ItemStack itemStack = event.getEntity().getItemStack();
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta.hasLore() && meta.getLore().size() == 2) {
            storage.removePet(meta.getLore().get(1));
        }
    }

    // Called when an item is destroyed by cactus/explosion/etc.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDestroy(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.DROPPED_ITEM) {
            return;
        }

        ItemStack itemStack = ((Item) event.getEntity()).getItemStack();
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta.hasLore() && meta.getLore().size() == 2) {
            storage.removePet(meta.getLore().get(1));
        }
    }
}
