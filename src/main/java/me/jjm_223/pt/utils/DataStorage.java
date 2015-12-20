package me.jjm_223.pt.utils;

import com.sun.javaws.exceptions.InvalidArgumentException;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Class for saving data to a config file.
 */
public class DataStorage {

    public static FileConfiguration config;
    public static boolean hasLoaded;
    public JavaPlugin plugin;
    public static File save;

    public DataStorage(JavaPlugin plugin) {
        if (!hasLoaded) {
            config = new YamlConfiguration();
            this.plugin = plugin;

            save = new File(plugin.getDataFolder() + File.separator + "pets.yml");

            if (!save.exists()) {
                try {
                    if (!save.getParentFile().exists()) {
                        save.getParentFile().mkdir();
                    }
                    save.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            try {
                config.load(save);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }

            new BukkitRunnable() {
                public void run() {
                    try {
                        config.save(save);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 200L);

            hasLoaded = true;
        }
    }

    public void savePet(Entity entity, UUID uuid) throws InvalidArgumentException {
        // Saves entity, throws an InvalidArgumentException when the entity specified is not an Ocelot, Wolf, or Horse.
        if (entity instanceof Ocelot || entity instanceof Wolf || entity instanceof Horse) {
            // If it is a cat, go to saveCat(), otherwise it must be a dog, so go to saveDog().
            if (entity instanceof Ocelot) {
                Ocelot pet = (Ocelot) entity;
                saveCat(pet, uuid);
            } else if (entity instanceof Wolf){
                Wolf pet = (Wolf) entity;
                saveDog(pet, uuid);
            } else {
                Horse pet = (Horse) entity;
                saveHorse(pet, uuid);
            }
        } else {
            throw new InvalidArgumentException(new String[] {"The entity specified was not a Wolf, Ocelot, or Horse."});
        }
    }

    // Saves a wolf entity
    private void saveDog(Wolf wolf, UUID uuid) {
        // Convert UUID to string for storage.
        String uuidString = uuid.toString();

        // Store important dog info in variables.
        String petName = wolf.getCustomName();
        DyeColor collarColor = wolf.getCollarColor();
        int colorRGB = collarColor.getColor().asRGB();
        String petOwnerUUID = wolf.getOwner().getUniqueId().toString();
        boolean isSitting = wolf.isSitting();
        int age = wolf.getAge();
        double wolfHealth = wolf.getHealth();

        // Save dog info.
        config.set("pets." + uuidString + ".petName", petName);
        config.set("pets." + uuidString + ".collarColor", colorRGB);
        config.set("pets." + uuidString + ".petOwner", petOwnerUUID);
        config.set("pets." + uuidString + ".isSitting", isSitting);
        config.set("pets." + uuidString + ".age", age);
        config.set("pets." + uuidString + ".health", wolfHealth);
    }

    private void saveCat(Ocelot ocelot, UUID uuid) {
        // Convert UUID to string for storage.
        String uuidString = uuid.toString();

        // Store important cat info in variables.
        String petName = ocelot.getCustomName();
        Ocelot.Type breed = ocelot.getCatType();
        String breedString = breed.toString();
        String petOwnerUUID = ocelot.getOwner().getUniqueId().toString();
        boolean isSitting = ocelot.isSitting();
        int age = ocelot.getAge();
        double catHealth = ocelot.getHealth();

        // Save cat info.
        config.set("pets." + uuidString + ".petName", petName);
        config.set("pets." + uuidString + ".breed", breedString);
        config.set("pets." + uuidString + ".petOwner", petOwnerUUID);
        config.set("pets." + uuidString + ".isSitting", isSitting);
        config.set("pets." + uuidString + ".age", age);
        config.set("pets." + uuidString + ".health", catHealth);
    }

    private void saveHorse(Horse horse, UUID uuid) {
        // Convert UUID to string for storage.
        String uuidString = uuid.toString();

        // Store horse info to variables.
        String petName = horse.getCustomName();
        String petOwnerUUID = horse.getOwner().getUniqueId().toString();
        Horse.Color color = horse.getColor();
        Horse.Style style = horse.getStyle();
        Horse.Variant variant = horse.getVariant();
        double jump = horse.getJumpStrength();
        int age = horse.getAge();
        double maxHealth = horse.getMaxHealth();
        double health = horse.getHealth();
        double speed = getHorseSpeed(horse);

        // Save horse info.
        config.set("pets." + uuidString + ".petName", petName);
        config.set("pets." + uuidString + ".petOwner", petOwnerUUID);
        config.set("pets." + uuidString + ".color", color.toString());
        config.set("pets." + uuidString + ".style", style.toString());
        config.set("pets." + uuidString + ".variant", variant.toString());
        config.set("pets." + uuidString + ".jump", jump);
        config.set("pets." + uuidString + ".age", age);
        config.set("pets." + uuidString + ".maxHealth", maxHealth);
        config.set("pets." + uuidString + ".health", health);
        config.set("pets." + uuidString + ".speed", speed);
    }

    public void restorePet(Entity entity, UUID uuid) throws InvalidArgumentException {
        // Make sure entity is an Ocelot, Wolf, or Horse if it isn't, throw an InvalidArgumentException.
        if (entity instanceof Ocelot || entity instanceof Wolf || entity instanceof Horse) {
            if (entity instanceof Ocelot) {
                Ocelot pet = (Ocelot) entity;
                restoreCat(pet, uuid);
            } else if (entity instanceof Wolf){
                Wolf pet = (Wolf) entity;
                restoreDog(pet, uuid);
            } else {
                Horse pet = (Horse) entity;
                restoreHorse(pet, uuid);
            }
            configClean(uuid.toString());
        } else {
            throw new InvalidArgumentException(new String[] {"The entity specified was not a Wolf, Ocelot, or Horse."});
        }
    }

    private void restoreDog(Wolf wolf, UUID uuid) {
        // Convert UUID to string for loading from config.
        String uuidString = uuid.toString();

        // Get pet data from config.
        String petName = config.getString("pets." + uuidString + ".petName");
        int colorRGB = config.getInt("pets." + uuidString + ".collarColor");
        UUID petOwnerUUID = UUID.fromString(config.getString("pets." + uuidString + ".petOwner"));
        boolean isSitting = config.getBoolean("pets." + uuidString + ".isSitting");
        int age = config.getInt("pets." + uuidString + ".age");
        double wolfHealth = config.getDouble("pets." + uuidString + ".health");

        // Sets pet data.
        wolf.setCustomName(petName);
        wolf.setCollarColor(DyeColor.getByColor(Color.fromRGB(colorRGB)));
        wolf.setOwner(Bukkit.getOfflinePlayer(petOwnerUUID));
        wolf.setSitting(isSitting);
        wolf.setAge(age);
        wolf.setHealth(wolfHealth);
        wolf.setRemoveWhenFarAway(false);
    }

    private void restoreCat(Ocelot ocelot, UUID uuid) {
        // Convert UUID to a string for reading the config.
        String uuidString = uuid.toString();

        // Get values from config.
        String petName = config.getString("pets." + uuidString + ".petName");
        String breedString = config.getString("pets." + uuidString + ".breed");
        Ocelot.Type breed = Ocelot.Type.valueOf(breedString);
        UUID petOwnerUUID = UUID.fromString(config.getString("pets." + uuidString + ".petOwner"));
        boolean isSitting = config.getBoolean("pets." + uuidString + ".isSitting");
        int age = config.getInt("pets." + uuidString + ".age");
        double catHealth = config.getDouble("pets." + uuidString + ".health");

        // Set cat info.
        ocelot.setCustomName(petName);
        ocelot.setCatType(breed);
        ocelot.setOwner(Bukkit.getOfflinePlayer(petOwnerUUID));
        ocelot.setSitting(isSitting);
        ocelot.setAge(age);
        ocelot.setHealth(catHealth);
        ocelot.setRemoveWhenFarAway(false);
    }

    private void restoreHorse(Horse horse, UUID uuid) {
        // Convert UUID to string for reading config.
        String uuidString = uuid.toString();

        // Get values from config.
        String petName = config.getString("pets." + uuidString + ".petName");
        UUID petOwnerUUID = UUID.fromString(config.getString("pets." + uuidString + ".petOwner"));
        Horse.Color color = Horse.Color.valueOf(config.getString("pets." + uuidString + ".color"));
        Horse.Style style = Horse.Style.valueOf(config.getString("pets." + uuidString + ".style"));
        Horse.Variant variant = Horse.Variant.valueOf(config.getString("pets." + uuidString + ".variant"));
        double jump = config.getDouble("pets." + uuidString + ".jump");
        int age = config.getInt("pets." + uuidString + ".age");
        double maxHealth = config.getDouble("pets." + uuidString + ".maxHealth");
        double health = config.getDouble("pets." + uuidString + ".health");
        double speed = config.getDouble("pets." + uuidString + ".speed");

        // Set horse info.
        horse.setCustomName(petName);
        horse.setOwner(Bukkit.getOfflinePlayer(petOwnerUUID));
        horse.setColor(color);
        horse.setStyle(style);
        horse.setVariant(variant);
        horse.setJumpStrength(jump);
        horse.setAge(age);
        horse.setMaxHealth(maxHealth);
        horse.setHealth(health);
        horse.setTamed(true);
        horse.setRemoveWhenFarAway(false);
        setHorseSpeed(horse, speed);
    }

    // Removes specified UUID from config. Used to keep the file small if possible.
    public void configClean(String uuid) {
        // Make sure the config has the UUID in it. This may not be necessary, but it is precautionary.
        if (config.contains("pets." + uuid)) {
            // Nulls the section (thereby deleting the values).
            config.set("pets." + uuid, null);
        }
    }

    // Get horse speed.
    public double getHorseSpeed(Horse horse) {
        EntityHorse nmsHorse = ((CraftHorse) horse).getHandle();
        return nmsHorse.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
    }

    // Set horse speed.
    public void setHorseSpeed(Horse horse, double value) {
        EntityHorse nmsHorse = ((CraftHorse) horse).getHandle();
        nmsHorse.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(value);
    }
}
