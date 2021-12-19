package me.jjm_223.pt.utils;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Class for saving data to a config file.
 */
public class DataStorage {

    private final FileConfiguration config;
    private final File saveFile;

    public DataStorage(JavaPlugin plugin) {
        config = new YamlConfiguration();

        saveFile = new File(plugin.getDataFolder() + File.separator + "pets.yml");

        if (!saveFile.exists()) {
            try {
                if (!saveFile.getParentFile().exists()) {
                    saveFile.getParentFile().mkdir();
                }
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            config.load(saveFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        new BukkitRunnable() {
            public void run() {
                try {
                    config.save(saveFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 200L);
    }

    public void savePet(Entity entity, UUID uuid) throws IllegalArgumentException {
        if (entity instanceof Cat) {
            saveCat((Cat) entity, uuid);
        } else if (entity instanceof Wolf) {
            saveDog((Wolf) entity, uuid);
        } else if (entity instanceof AbstractHorse) {
            saveAbstractHorse((AbstractHorse) entity, uuid);
        } else if (entity instanceof Parrot) {
            saveParrot((Parrot) entity, uuid);
        } else {
            throw new IllegalArgumentException("The entity specified was not a Wolf, Cat, AbstractHorse, or Parrot.");
        }

        saveBaseData(entity, uuid);

        set(uuid, "entitytype", entity.getType().name());
    }

    private void saveBaseData(Entity entity, UUID uuid) {
        Validate.isTrue(entity instanceof Tameable);

        set(uuid, "petName", entity.getCustomName());
        set(uuid, "petOwner", ((Tameable) entity).getOwner().getUniqueId().toString());
        set(uuid, "health", ((Damageable) entity).getHealth());
    }

    private void saveDog(Wolf wolf, UUID uuid) {
        set(uuid, "collarColor", wolf.getCollarColor().toString());
        set(uuid, "isSitting", wolf.isSitting());
        set(uuid, "age", wolf.getAge());
    }

    private void saveCat(Cat cat, UUID uuid) {
        set(uuid, "breed", cat.getCatType().toString());
        set(uuid, "isSitting", cat.isSitting());
        set(uuid, "age", cat.getAge());
    }

    private void saveAbstractHorse(AbstractHorse abstractHorse, UUID uuid) {
        set(uuid, "jump", abstractHorse.getJumpStrength());
        set(uuid, "age", abstractHorse.getAge());
        set(uuid, "maxHealth", abstractHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        set(uuid, "speed", getHorseSpeed(abstractHorse));

        if (abstractHorse instanceof Horse) {
            set(uuid, "color", ((Horse) abstractHorse).getColor().toString());
            set(uuid, "style", ((Horse) abstractHorse).getStyle().toString());
        } else {
            if (abstractHorse instanceof ChestedHorse) {
                set(uuid, "chested", ((ChestedHorse) abstractHorse).isCarryingChest());
            }
            if (abstractHorse instanceof Llama) {
                set(uuid, "color", ((Llama) abstractHorse).getColor().toString());
                set(uuid, "strength", ((Llama) abstractHorse).getStrength());
            }
        }
    }

    private void saveParrot(Parrot parrot, UUID uuid) {
        set(uuid, "variant", parrot.getVariant().toString());
        set(uuid, "isSitting", parrot.isSitting());
    }

    public void restorePet(Entity entity, UUID uuid, boolean remove) {
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setRemoveWhenFarAway(false);
        }

        if (entity instanceof Cat) {
            Cat pet = (Cat) entity;
            restoreCat(pet, uuid);
        } else if (entity instanceof Wolf) {
            Wolf pet = (Wolf) entity;
            restoreDog(pet, uuid);
        } else if (entity instanceof AbstractHorse) {
            AbstractHorse pet = (AbstractHorse) entity;
            restoreAbstractHorse(pet, uuid);
        } else if (entity instanceof Parrot) {
            restoreParrot((Parrot) entity, uuid);
        } else {
            throw new IllegalArgumentException("The entity specified was not a Wolf, Cat, AbstractHorse, or Parrot.");
        }

        // Base data restored last to avoid an error on exceeding max health for horses
        restoreBaseData(entity, uuid);

        if (remove) removePet(uuid.toString());
    }

    private void restoreBaseData(Entity entity, UUID uuid) {
        Validate.isTrue(entity instanceof Tameable);

        entity.setCustomName(this.get(uuid, "petName"));
        ((Tameable) entity).setOwner(Bukkit.getOfflinePlayer(UUID.fromString(this.get(uuid, "petOwner"))));
        ((Damageable) entity).setHealth(this.get(uuid, "health"));
    }

    private void restoreDog(Wolf wolf, UUID uuid) {
        wolf.setCollarColor(DyeColor.valueOf(this.get(uuid, "collarColor")));
        wolf.setSitting(this.get(uuid, "isSitting"));
        wolf.setAge(this.get(uuid, "age"));
    }

    private void restoreCat(Cat cat, UUID uuid) {
        cat.setCatType(Cat.Type.valueOf(this.<String>get(uuid, "breed")));
        cat.setSitting(this.get(uuid, "isSitting"));
        cat.setAge(this.get(uuid, "age"));
    }

    private void restoreAbstractHorse(AbstractHorse horse, UUID uuid) {
        horse.setJumpStrength(this.get(uuid, "jump"));
        horse.setAge(this.get(uuid, "age"));
        horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.get(uuid, "maxHealth"));
        horse.setTamed(true);
        setHorseSpeed(horse, this.get(uuid, "speed"));

        if (horse instanceof Horse) {
            ((Horse) horse).setColor(Horse.Color.valueOf(this.get(uuid, "color")));
            ((Horse) horse).setStyle(Horse.Style.valueOf(this.get(uuid, "style")));
        } else {
            if (horse instanceof ChestedHorse) {
                ((ChestedHorse) horse).setCarryingChest(this.get(uuid, "chested"));
            }
            if (horse instanceof Llama) {
                ((Llama) horse).setColor(Llama.Color.valueOf(this.get(uuid, "color")));

                int strength = this.get(uuid, "strength");
                // This was implemented later on- config may not have an option set.
                if (strength > 0) {
                    ((Llama) horse).setStrength(strength);
                }
            }
        }
    }

    private void restoreParrot(Parrot parrot, UUID uuid) {
        parrot.setVariant(Parrot.Variant.valueOf(this.get(uuid, "variant")));
        parrot.setSitting(this.get(uuid, "isSitting"));
    }

    public EntityType identifyPet(String uuid) {
        ConfigurationSection section = config.getConfigurationSection("pets." + uuid);
        if (section == null)
            throw new IllegalArgumentException("No such UUID exists in the configuration.");

        return EntityType.valueOf(section.getString("entitytype"));
    }

    public void removePet(String uuid) {
        config.set("pets." + uuid, null);
    }

    // Get horse speed.
    private double getHorseSpeed(AbstractHorse horse) {
        return horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
    }

    // Set horse speed.
    private void setHorseSpeed(AbstractHorse horse, double value) {
        horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(value);
    }

    private void set(UUID uuid, String label, Object value)
    {
        config.set("pets." + uuid.toString() + "." + label, value);
    }

    private <T> T get(UUID uuid, String label)
    {
        return (T) config.get("pets." + uuid.toString() + "." + label);
    }

    public boolean contains(UUID uuid) {
        return config.contains("pets." + uuid);
    }

    public boolean contains(String string) {
        return contains(UUID.fromString(string));
    }

    public void save() throws IOException {
        config.save(saveFile);
    }
}
