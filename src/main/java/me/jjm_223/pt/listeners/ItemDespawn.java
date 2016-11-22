package me.jjm_223.pt.listeners;

import me.jjm_223.pt.PetTransportation;
import me.jjm_223.pt.utils.DataStorage;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

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
        if (event.getEntity().getItemStack().getItemMeta().getLore() != null) {
            if (event.getEntity().getItemStack().getItemMeta().getLore().size() == 2) {
                storage.configClean(event.getEntity().getItemStack().getItemMeta().getLore().get(1));
            }
        }
    }

    // Called when an item is destroyed by cactus/explosion/etc.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDestroy(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item) {
            Item item = (Item) event.getEntity();
            if (item.getItemStack().getItemMeta().getLore() != null) {
                if (item.getItemStack().getItemMeta().getLore().size() == 2) {
                    if (item.getItemStack().getItemMeta().getLore().get(1).length() == 36) {
                        storage.configClean(item.getItemStack().getItemMeta().getLore().get(1));
                    }
                }
            }
        }
    }
}
