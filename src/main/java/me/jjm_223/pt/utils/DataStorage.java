package me.jjm_223.pt.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
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

    private FileConfiguration config;
    private File saveFile;

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
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
        // Saves entity, throws an IllegalArgumentException when the entity specified is not an Ocelot, Wolf, or AbstractHorse.
        if (entity instanceof Ocelot) {
            Ocelot pet = (Ocelot) entity;
            saveCat(pet, uuid);
        } else if (entity instanceof Wolf) {
            Wolf pet = (Wolf) entity;
            saveDog(pet, uuid);
        } else if (entity instanceof AbstractHorse) {
            AbstractHorse pet = (AbstractHorse) entity;
            saveAbstractHorse(pet, uuid);
        } else {
            throw new IllegalArgumentException("The entity specified was not a Wolf, Ocelot, or AbstractHorse.");
        }

        config.set("pets." + uuid + ".entitytype", entity.getType().name());
    }

    // Saves a wolf entity
    private void saveDog(Wolf wolf, UUID uuid) {
        // Convert UUID to string for storage.
        String uuidString = uuid.toString();

        // Store important dog info in variables.
        String petName = wolf.getCustomName();
        String colorString = wolf.getCollarColor().toString();
        String petOwnerUUID = wolf.getOwner().getUniqueId().toString();
        boolean isSitting = wolf.isSitting();
        int age = wolf.getAge();
        double wolfHealth = wolf.getHealth();

        // Save dog info.
        config.set("pets." + uuidString + ".petName", petName);
        config.set("pets." + uuidString + ".collarColor", colorString);
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

    private void saveAbstractHorse(AbstractHorse abstractHorse, UUID uuid) {
        // Convert UUID to string for storage.
        String uuidString = uuid.toString();

        // Store abstract horse info to variables.
        String petName = abstractHorse.getCustomName();
        String petOwnerUUID = abstractHorse.getOwner().getUniqueId().toString();
        double jump = abstractHorse.getJumpStrength();
        int age = abstractHorse.getAge();
        double maxHealth = abstractHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double health = abstractHorse.getHealth();
        double speed = getHorseSpeed(abstractHorse);

        config.set("pets." + uuidString + ".petName", petName);
        config.set("pets." + uuidString + ".petOwner", petOwnerUUID);
        config.set("pets." + uuidString + ".jump", jump);
        config.set("pets." + uuidString + ".age", age);
        config.set("pets." + uuidString + ".maxHealth", maxHealth);
        config.set("pets." + uuidString + ".health", health);
        config.set("pets." + uuidString + ".speed", speed);

        if (abstractHorse instanceof Horse) {
            Horse.Color color = ((Horse) abstractHorse).getColor();
            Horse.Style style = ((Horse) abstractHorse).getStyle();

            config.set("pets." + uuidString + ".color", color.toString());
            config.set("pets." + uuidString + ".style", style.toString());
        } else if (abstractHorse instanceof Llama) {
            Llama.Color color = ((Llama) abstractHorse).getColor();
            int strength = ((Llama) abstractHorse).getStrength();
            boolean chested = ((Llama) abstractHorse).isCarryingChest();

            config.set("pets." + uuidString + ".color", color.toString());
            config.set("pets." + uuidString + ".strength", strength);
            config.set("pets." + uuidString + ".chested", chested);
        }
    }

    public void restorePet(Entity entity, UUID uuid) throws IllegalArgumentException {
        // Make sure entity is an Ocelot, Wolf, or Horse if it isn't, throw an IllegalArgumentException.
        if (entity instanceof Ocelot) {
            Ocelot pet = (Ocelot) entity;
            restoreCat(pet, uuid);
        } else if (entity instanceof Wolf) {
            Wolf pet = (Wolf) entity;
            restoreDog(pet, uuid);
        } else if (entity instanceof AbstractHorse) {
            AbstractHorse pet = (AbstractHorse) entity;
            restoreAbstractHorse(pet, uuid);
        } else {
            throw new IllegalArgumentException("The entity specified was not a Wolf, Ocelot, or AbstractHorse.");
        }

        configClean(uuid.toString());
    }

    private void restoreDog(Wolf wolf, UUID uuid) {
        // Convert UUID to string for loading from config.
        String uuidString = uuid.toString();

        // Get pet data from config.
        String petName = config.getString("pets." + uuidString + ".petName");
        Object color = config.get("pets." + uuidString + ".collarColor");
        UUID petOwnerUUID = UUID.fromString(config.getString("pets." + uuidString + ".petOwner"));
        boolean isSitting = config.getBoolean("pets." + uuidString + ".isSitting");
        int age = config.getInt("pets." + uuidString + ".age");
        double wolfHealth = config.getDouble("pets." + uuidString + ".health");

        // Sets pet data.
        wolf.setCustomName(petName);
        if (color instanceof String)
        {
            wolf.setCollarColor(DyeColor.valueOf(((String) color)));
        }
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

    private void restoreAbstractHorse(AbstractHorse horse, UUID uuid) {
        // Convert UUID to string for reading config.
        String uuidString = uuid.toString();

        // Get abstract horse values from config.
        String petName = config.getString("pets." + uuidString + ".petName");
        UUID petOwnerUUID = UUID.fromString(config.getString("pets." + uuidString + ".petOwner"));
        double jump = config.getDouble("pets." + uuidString + ".jump");
        int age = config.getInt("pets." + uuidString + ".age");
        double maxHealth = config.getDouble("pets." + uuidString + ".maxHealth");
        double health = config.getDouble("pets." + uuidString + ".health");
        double speed = config.getDouble("pets." + uuidString + ".speed");

        // Set abstract horse info.
        horse.setCustomName(petName);
        horse.setOwner(Bukkit.getOfflinePlayer(petOwnerUUID));
        horse.setJumpStrength(jump);
        horse.setAge(age);
        horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        horse.setHealth(health);
        horse.setTamed(true);
        horse.setRemoveWhenFarAway(false);
        setHorseSpeed(horse, speed);

        if (horse instanceof Horse) {
            Horse.Color color = Horse.Color.valueOf(config.getString("pets." + uuidString + ".color"));
            Horse.Style style = Horse.Style.valueOf(config.getString("pets." + uuidString + ".style"));

            ((Horse) horse).setColor(color);
            ((Horse) horse).setStyle(style);
        } else if (horse instanceof Llama) {
            Llama.Color color = Llama.Color.valueOf(config.getString("pets." + uuidString + ".color"));
            int strength = config.getInt("pets." + uuidString + ".strength");
            boolean chested = config.getBoolean("pets." + uuidString + ".chested");

            ((Llama) horse).setColor(color);
            ((Llama) horse).setCarryingChest(chested);
            // This was implemented later on- config may not have an option set.
            if (strength > 0) {
                ((Llama) horse).setStrength(strength);
            }
        }
    }

    public EntityType identifyPet(String uuid) {
        ConfigurationSection section = config.getConfigurationSection("pets." + uuid);
        if (section == null)
            throw new IllegalArgumentException("No such UUID exists in the configuration.");

        if (section.contains("entitytype"))
            return EntityType.valueOf(section.getString("entitytype"));

        if (section.contains("variant")) {
            Horse.Variant variant = Horse.Variant.valueOf(section.getString("variant"));
            switch (variant) {
                case HORSE:
                    return EntityType.HORSE;
                case UNDEAD_HORSE:
                    return EntityType.ZOMBIE_HORSE;
                case SKELETON_HORSE:
                    return EntityType.SKELETON_HORSE;
                case DONKEY:
                    return EntityType.DONKEY;
                case MULE:
                    return EntityType.MULE;
            }

            return EntityType.HORSE;
        } else if (section.contains("breed")) {
            return EntityType.OCELOT;
        } else if (section.contains("collarColor")) {
            return EntityType.WOLF;
        } else {
            return null;
        }
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
    private double getHorseSpeed(AbstractHorse horse) {
        return horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
    }

    // Set horse speed.
    private void setHorseSpeed(AbstractHorse horse, double value) {
        horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(value);
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
