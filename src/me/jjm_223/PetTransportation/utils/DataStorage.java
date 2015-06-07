package me.jjm_223.PetTransportation.utils;

import me.jjm_223.PetTransportation.PTMain;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Wolf;

import java.util.UUID;

/**
 * Class for saving data to a config file.
 */
public class DataStorage {

    private FileConfiguration config;

    public DataStorage(FileConfiguration config) {
        //Sets config for this instance
        this.config = config;
    }

    public void savePet(Entity entity, UUID uuid) throws Exception {
        //Saves entity, throws an InvalidArgumentException when the entity specified is not an Ocelot or a Wolf.
        if (entity instanceof Ocelot || entity instanceof Wolf || entity instanceof Horse) {
            //If it is a cat, go to saveCat(), otherwise it must be a dog, so go to saveDog().
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
            throw new Exception("The entity specified was neither a wolf, nor was it an ocelot.");
        }
    }

    //Saves a wolf entity
    private void saveDog(Wolf wolf, UUID uuid) {
        //Convert UUID to string for storage.
        String uuidString = uuid.toString();

        //Store important dog info in variables.
        String petName = wolf.getCustomName();
        DyeColor collarColor = wolf.getCollarColor();
        int colorRGB = collarColor.getColor().asRGB();
        String petOwnerUUID = wolf.getOwner().getUniqueId().toString();
        boolean isSitting = wolf.isSitting();
        int age = wolf.getAge();
        double wolfHealth = wolf.getHealth();

        //Save dog info.
        config.set("pets." + uuidString + ".petName", petName);
        config.set("pets." + uuidString + ".collarColor", colorRGB);
        config.set("pets." + uuidString + ".petOwner", petOwnerUUID);
        config.set("pets." + uuidString + ".isSitting", isSitting);
        config.set("pets." + uuidString + ".age", age);
        config.set("pets." + uuidString + ".health", wolfHealth);

        PTMain.getPlugin(PTMain.class).saveConfig();
    }

    private void saveCat(Ocelot ocelot, UUID uuid) {
        //Convert UUID to string for storage.
        String uuidString = uuid.toString();

        //Store important cat info in variables.
        String petName = ocelot.getCustomName();
        Ocelot.Type breed = ocelot.getCatType();
        String breedString = breed.toString();
        String petOwnerUUID = ocelot.getOwner().getUniqueId().toString();
        boolean isSitting = ocelot.isSitting();
        int age = ocelot.getAge();
        double catHealth = ocelot.getHealth();

        //Save cat info.
        config.set("pets." + uuidString + ".petName", petName);
        config.set("pets." + uuidString + ".breed", breedString);
        config.set("pets." + uuidString + ".petOwner", petOwnerUUID);
        config.set("pets." + uuidString + ".isSitting", isSitting);
        config.set("pets." + uuidString + ".age", age);
        config.set("pets." + uuidString + ".health", catHealth);

        PTMain.getPlugin(PTMain.class).saveConfig();
    }

    private void saveHorse(Horse horse, UUID uuid) {
        //Convert UUID to string for storage.
        String uuidString = uuid.toString();

        //Store horse info to variables.
        String petName = horse.getCustomName();
        String petOwnerUUID = horse.getOwner().getUniqueId().toString();
        Horse.Color color = horse.getColor();
        Horse.Style style = horse.getStyle();
        Horse.Variant variant = horse.getVariant();
        int age = horse.getAge();
        double maxHealth = horse.getMaxHealth();
        double health = horse.getHealth();

        //Save horse info.
        config.set("pets." + uuidString + ".petName", petName);
        config.set("pets." + uuidString + ".petOwner", petOwnerUUID);
        config.set("pets." + uuidString + ".color", color.toString());
        config.set("pets." + uuidString + ".style", style.toString());
        config.set("pets." + uuidString + ".variant", variant.toString());
        config.set("pets." + uuidString + ".age", age);
        config.set("pets." + uuidString + ".maxHealth", maxHealth);
        config.set("pets." + uuidString + ".health", health);

        PTMain.getPlugin(PTMain.class).saveConfig();
    }

    public void restorePet(Entity entity, UUID uuid) throws Exception {
        //Make sure entity is an Ocelot or a Wolf, if it isn't, throw an InvalidArgumentException.
        if (entity instanceof Ocelot || entity instanceof Wolf || entity instanceof Horse) {
            //If entity is a cat, then pass it on to restoreCat(), otherwise it must be a dog, so pass it on to restoreDog().
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
        } else {
            throw new Exception("The entity specified was neither a wolf, nor was it an ocelot.");
        }
    }

    private void restoreDog(Wolf wolf, UUID uuid) {
        //Convert UUID to string for loading from config.
        String uuidString = uuid.toString();

        //Get pet data from config.
        String petName = config.getString("pets." + uuidString + ".petName");
        int colorRGB = config.getInt("pets." + uuidString + ".collarColor");
        UUID petOwnerUUID = UUID.fromString(config.getString("pets." + uuidString + ".petOwner"));
        boolean isSitting = config.getBoolean("pets." + uuidString + ".isSitting");
        int age = config.getInt("pets." + uuidString + ".age");
        double wolfHealth = config.getDouble("pets." + uuidString + ".health");

        //Sets pet data.
        wolf.setCustomName(petName);
        wolf.setCollarColor(DyeColor.getByColor(Color.fromRGB(colorRGB)));
        wolf.setOwner(Bukkit.getOfflinePlayer(petOwnerUUID));
        wolf.setSitting(isSitting);
        wolf.setAge(age);
        wolf.setHealth(wolfHealth);
        wolf.setRemoveWhenFarAway(false);

        configClean(uuidString);
    }

    private void restoreCat(Ocelot ocelot, UUID uuid) {
        //Convert UUID to a string for reading the config.
        String uuidString = uuid.toString();

        //Get values from config.
        String petName = config.getString("pets." + uuidString + ".petName");
        String breedString = config.getString("pets."+ uuidString + ".breed");
        Ocelot.Type breed = Ocelot.Type.valueOf(breedString);
        UUID petOwnerUUID = UUID.fromString(config.getString("pets." + uuidString + ".petOwner"));
        boolean isSitting = config.getBoolean("pets." + uuidString + ".isSitting");
        int age = config.getInt("pets." + uuidString + ".age");
        double catHealth = config.getDouble("pets." + uuidString + ".health");

        //Set cat info.
        ocelot.setCustomName(petName);
        ocelot.setCatType(breed);
        ocelot.setOwner(Bukkit.getOfflinePlayer(petOwnerUUID));
        ocelot.setSitting(isSitting);
        ocelot.setAge(age);
        ocelot.setHealth(catHealth);
        ocelot.setRemoveWhenFarAway(false);

        configClean(uuidString);
    }

    private void restoreHorse(Horse horse, UUID uuid) {
        //Convert UUID to string for reading config.
        String uuidString = uuid.toString();

        //Get values from config.
        String petName = config.getString("pets." + uuidString + ".petName");
        UUID petOwnerUUID = UUID.fromString(config.getString("pets." + uuidString + ".petOwner"));
        Horse.Color color = Horse.Color.valueOf(config.getString("pets." + uuidString + ".color"));
        Horse.Style style = Horse.Style.valueOf(config.getString("pets." + uuidString + ".style"));
        Horse.Variant variant = Horse.Variant.valueOf(config.getString("pets." + uuidString + ".variant"));
        int age = config.getInt("pets." + uuidString + ".age");
        double maxHealth = config.getDouble("pets." + uuidString + ".maxHealth");
        double health = config.getDouble("pets." + uuidString + ".health");

        //Set horse info.
        horse.setCustomName(petName);
        horse.setOwner(Bukkit.getOfflinePlayer(petOwnerUUID));
        horse.setColor(color);
        horse.setStyle(style);
        horse.setVariant(variant);
        horse.setAge(age);
        horse.setMaxHealth(maxHealth);
        horse.setHealth(health);
        horse.setTamed(true);
        horse.setRemoveWhenFarAway(false);

        configClean(uuidString);
    }

    //Removes specified UUID from config. Used to keep the file small if possible.
    public void configClean(String uuid) {
        //Make sure the config has the UUID in it. This may not be necessary, but it is precautionary.
        if (config.contains("pets." + uuid)) {
            //Nulls the section (thereby deleting the values).
            config.set("pets." + uuid, null);
            //Saves config for safety.
            PTMain.getPlugin(PTMain.class).saveConfig();
        }
    }
}
