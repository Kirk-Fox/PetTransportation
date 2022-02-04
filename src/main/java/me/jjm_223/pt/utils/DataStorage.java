package me.jjm_223.pt.utils;

import org.bukkit.DyeColor;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class for saving data to a config file.
 */
@SuppressWarnings("ConstantConditions")
public class DataStorage {

    private final Server server;
    private final int serverVersion;

    private final FileConfiguration config;
    private final File saveFile;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public DataStorage(JavaPlugin plugin, int serverVersion) {
        server = plugin.getServer();
        this.serverVersion = serverVersion;

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

    /**
     * Saves a captured pet and its data to persistent storage.
     *
     * @param mob the captured mob
     * @param uuid the randomly generated UUID created upon capturing this mob.
     */
    public void savePet(LivingEntity mob, UUID uuid) {
        Map<String, Object> mobData = new HashMap<>();

        mobData.put("entityType", mob.getType().name());
        mobData.put("name", mob.getCustomName());
        mobData.put("health", mob.getHealth());

        if (mob instanceof Tameable) {
            Tameable t = (Tameable) mob;
            mobData.put("isTamed", t.isTamed());
            if (t.getOwner() != null) mobData.put("owner", t.getOwner().getUniqueId().toString());
        }

        if (mob instanceof Ageable) mobData.put("age", ((Ageable) mob).getAge());

        if (serverVersion > 10) {
            if (mob instanceof AbstractHorse) {
                AbstractHorse h = (AbstractHorse) mob;
                mobData.put("jumpStrength", h.getJumpStrength());
                mobData.put("maxHealth", h.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                mobData.put("speed", h.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue());
                if (h instanceof ChestedHorse) mobData.put("isCarryingChest", ((ChestedHorse) h).isCarryingChest());
            }
        } else {
            if (mob instanceof Horse) {
                Horse h = (Horse) mob;
                mobData.put("jumpStrength", h.getJumpStrength());
                mobData.put("maxHealth", h.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                mobData.put("speed", h.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue());
                mobData.put("isCarryingChest", h.isCarryingChest());
            }
        }

        if (mob instanceof Slime) mobData.put("size", ((Slime) mob).getSize());

        switch (mob.getType()) {
            case WOLF:
                mobData.put("isSitting", ((Wolf) mob).isSitting());
                mobData.put("collarColor", ((Wolf) mob).getCollarColor().name());
                break;
            case CAT:
                mobData.put("isSitting", ((Cat) mob).isSitting());
                mobData.put("breed", ((Cat) mob).getCatType().name());
                break;
            case HORSE:
                Horse h = (Horse) mob;
                mobData.put("color", h.getColor().name());
                mobData.put("style", h.getStyle().name());
                if (serverVersion < 11) mobData.put("variant", h.getVariant().name());
                break;
            case PARROT:
                mobData.put("isSitting", ((Parrot) mob).isSitting());
                mobData.put("variant", ((Parrot) mob).getVariant().name());
                break;
            case BEE:
                Bee b = (Bee) mob;
                mobData.put("anger", b.getAnger());
                mobData.put("cannotEnterHiveTicks", b.getCannotEnterHiveTicks());
                mobData.put("hasNectar", b.hasNectar());
                mobData.put("hasStung", b.hasStung());
                break;
            case CREEPER:
                mobData.put("isPowered", ((Creeper) mob).isPowered());
                break;
            case ENDERMAN:
                BlockData bd = ((Enderman) mob).getCarriedBlock();
                if (bd != null) mobData.put("carriedBlock", bd.getAsString());
                break;
            case FOX:
                Fox f = (Fox) mob;
                mobData.put("isSitting", f.isSitting());
                AnimalTamer tp1 = f.getFirstTrustedPlayer();
                AnimalTamer tp2 = f.getSecondTrustedPlayer();
                if (tp1 != null) {
                    mobData.put("firstTrustedPlayer", tp1.getUniqueId().toString());
                    if (tp2 != null) mobData.put("secondTrustedPlayer", tp2.getUniqueId().toString());
                }
                mobData.put("foxType", f.getFoxType().name());
                mobData.put("isCrouching", f.isCrouching());
                break;
            case GLOW_SQUID:
                mobData.put("darkTicksRemaining", ((GlowSquid) mob).getDarkTicksRemaining());
                break;
            case GOAT:
                mobData.put("isScreaming", ((Goat) mob).isScreaming());
                break;
            case IRON_GOLEM:
                mobData.put("isPlayerCreated", ((IronGolem) mob).isPlayerCreated());
                break;
            case LLAMA:
            case TRADER_LLAMA:
                mobData.put("color", ((Llama) mob).getColor().name());
                mobData.put("strength", ((Llama) mob).getStrength());
                break;
            case MUSHROOM_COW:
                mobData.put("variant", ((MushroomCow) mob).getVariant().name());
                break;
            case OCELOT:
                if (serverVersion > 13) {
                    mobData.put("isTrusting", ((Ocelot) mob).isTrusting());
                } else {
                    if (serverVersion > 11) mobData.put("isSitting", ((Sittable) mob).isSitting());
                    mobData.put("breed", ((Ocelot) mob).getCatType().name());
                }
                break;
            case PANDA:
                mobData.put("mainGene", ((Panda) mob).getMainGene().name());
                mobData.put("hiddenGene", ((Panda) mob).getHiddenGene().name());
                break;
            case PIG:
                mobData.put("hasSaddle", ((Pig) mob).hasSaddle());
                break;
            case ZOMBIFIED_PIGLIN:
                mobData.put("anger", ((PigZombie) mob).getAnger());
                break;
            case PUFFERFISH:
                mobData.put("puffState", ((PufferFish) mob).getPuffState());
                break;
            case RABBIT:
                mobData.put("rabbitType", ((Rabbit) mob).getRabbitType().name());
                break;
            case SHEEP:
                mobData.put("isSheared", ((Sheep) mob).isSheared());
                break;
            case SNOWMAN:
                mobData.put("isDerp", ((Snowman) mob).isDerp());
                break;
            case STRIDER:
                mobData.put("hasSaddle", ((Strider) mob).hasSaddle());
                break;
            case VILLAGER:
                Villager v = (Villager) mob;
                mobData.put("profession", v.getProfession().name());
                mobData.put("type", v.getVillagerType().name());
                mobData.put("level", v.getVillagerLevel());
                mobData.put("experience", v.getVillagerExperience());
                break;
            case ZOMBIE_VILLAGER:
                ZombieVillager z = (ZombieVillager) mob;
                mobData.put("conversionPlayer", z.getConversionPlayer().getUniqueId().toString());
                mobData.put("conversionTime", z.getConversionTime());
                mobData.put("profession", z.getVillagerProfession().name());
                mobData.put("type", z.getVillagerType().name());
                break;
        }

        // If the mob is a villager or wandering trader, save the trades.

        if (mob instanceof Villager || mob.getType().name().equals("WANDERING_TRADER")) {
            List<MerchantRecipe> trades;
            if (serverVersion > 10) {
                trades = ((Merchant) mob).getRecipes();
            } else {
                trades = ((Villager) mob).getRecipes();
            }
            int i = 0;
            for (MerchantRecipe t : trades) {
                String path = "trades." + i + ".";
                mobData.put(path + "result", t.getResult());
                mobData.put(path + "uses", t.getUses());
                mobData.put(path + "maxUses", t.getMaxUses());
                mobData.put(path + "experienceReward", t.hasExperienceReward());
                mobData.put(path + "villagerExperience", t.getVillagerExperience());
                mobData.put(path + "priceMultiplier", t.getPriceMultiplier());
                mobData.put(path + "demand", t.getDemand());
                mobData.put(path + "specialPrice", t.getSpecialPrice());
                List<ItemStack> ingredients = t.getIngredients();
                for (int j = 0; j < ingredients.size(); j++) mobData.put(path + "ingredients." + j, ingredients.get(j));
                i++;
            }
        }

        set(uuid, mobData);
    }

    /**
     * Retrieves a captured pet and its data from persistent storage and applies that data to a spawned mob.
     *
     * @param mob the mob spawned that will have its data set
     * @param uuid the UUID associated with the captured pet
     */
    public void restorePet(LivingEntity mob, UUID uuid) {
        Map<String, Object> mobData = get(uuid);

        mob.setCustomName((String) mobData.get("name"));

        if (mob instanceof Tameable) {
            Tameable t = (Tameable) mob;
            t.setTamed((Boolean) mobData.get("isTamed"));
            String owner = (String) mobData.get("owner");
            if (owner != null) t.setOwner(server.getOfflinePlayer(UUID.fromString(owner)));
        }

        if (mob instanceof Ageable) ((Ageable) mob).setAge((Integer) mobData.get("age"));

        if (serverVersion > 10) {
            if (mob instanceof AbstractHorse) {
                AbstractHorse h = (AbstractHorse) mob;
                h.setJumpStrength((Double) mobData.get("jumpStrength"));
                h.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue((Double) mobData.get("maxHealth"));
                h.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue((Double) mobData.get("speed"));
                if (h instanceof ChestedHorse) ((ChestedHorse) h).setCarryingChest((Boolean) mobData.get("isCarryingChest"));
            }
        } else {
            if (mob instanceof Horse) {
                Horse h = (Horse) mob;
                h.setJumpStrength((Double) mobData.get("jumpStrength"));
                h.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue((Double) mobData.get("maxHealth"));
                h.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue((Double) mobData.get("speed"));
                h.setCarryingChest((Boolean) mobData.get("isCarryingChest"));
            }
        }

        if (mob instanceof Slime) ((Slime) mob).setSize((Integer) mobData.get("size"));

        mob.setHealth((Double) mobData.get("health"));

        switch (mob.getType()) {
            case WOLF:
                ((Wolf) mob).setSitting((Boolean) mobData.get("isSitting"));
                ((Wolf) mob).setCollarColor(DyeColor.valueOf((String) mobData.get("collarColor")));
                break;
            case CAT:
                ((Cat) mob).setSitting((Boolean) mobData.get("isSitting"));
                ((Cat) mob).setCatType(Cat.Type.valueOf((String) mobData.get("breed")));
                break;
            case HORSE:
                Horse h = (Horse) mob;
                h.setColor(Horse.Color.valueOf((String) mobData.get("color")));
                h.setStyle(Horse.Style.valueOf((String) mobData.get("style")));
                if (serverVersion < 11) h.setVariant(Horse.Variant.valueOf((String) mobData.get("variant")));
                break;
            case PARROT:
                ((Parrot) mob).setSitting((Boolean) mobData.get("isSitting"));
                ((Parrot) mob).setVariant(Parrot.Variant.valueOf((String) mobData.get("variant")));
                break;
            case BEE:
                Bee b = (Bee) mob;
                b.setAnger((Integer) mobData.get("anger"));
                b.setCannotEnterHiveTicks((Integer) mobData.get("cannotEnterHiveTicks"));
                b.setHasNectar((Boolean) mobData.get("hasNectar"));
                b.setHasStung((Boolean) mobData.get("hasStung"));
                break;
            case CREEPER:
                ((Creeper) mob).setPowered((Boolean) mobData.get("isPowered"));
                break;
            case ENDERMAN:
                String bd = (String) mobData.get("carriedBlock");
                if (bd != null) ((Enderman) mob).setCarriedBlock(server.createBlockData(bd));
                break;
            case FOX:
                Fox f = (Fox) mob;
                f.setSitting((Boolean) mobData.get("isSitting"));
                String tp1 = (String) mobData.get("firstTrustedPlayer");
                String tp2 = (String) mobData.get("secondTrustedPlayer");
                if (tp1 != null) {
                    f.setFirstTrustedPlayer(server.getOfflinePlayer(UUID.fromString(tp1)));
                    if (tp2 != null) f.setSecondTrustedPlayer(server.getOfflinePlayer(UUID.fromString(tp2)));
                }
                f.setFoxType(Fox.Type.valueOf((String) mobData.get("foxType")));
                f.setCrouching((Boolean) mobData.get("isCrouching"));
                break;
            case GLOW_SQUID:
                ((GlowSquid) mob).setDarkTicksRemaining((Integer) mobData.get("darkTicksRemaining"));
                break;
            case GOAT:
                ((Goat) mob).setScreaming((Boolean) mobData.get("isScreaming"));
                break;
            case IRON_GOLEM:
                ((IronGolem) mob).setPlayerCreated((Boolean) mobData.get("isPlayerCreated"));
                break;
            case LLAMA:
            case TRADER_LLAMA:
                ((Llama) mob).setColor(Llama.Color.valueOf((String) mobData.get("color")));
                ((Llama) mob).setStrength((Integer) mobData.get("strength"));
                break;
            case MUSHROOM_COW:
                ((MushroomCow) mob).setVariant(MushroomCow.Variant.valueOf((String) mobData.get("variant")));
                break;
            case OCELOT:
                if (serverVersion > 13) {
                    ((Ocelot) mob).setTrusting((Boolean) mobData.get("isTrusting"));
                } else {
                    if (serverVersion > 11) ((Sittable) mob).setSitting((Boolean) mobData.get("isSitting"));
                    ((Ocelot) mob).setCatType(Ocelot.Type.valueOf((String) mobData.get("breed")));
                }
                break;
            case PANDA:
                ((Panda) mob).setMainGene(Panda.Gene.valueOf((String) mobData.get("mainGene")));
                ((Panda) mob).setHiddenGene(Panda.Gene.valueOf((String) mobData.get("hiddenGene")));
                break;
            case PIG:
                ((Pig) mob).setSaddle((Boolean) mobData.get("hasSaddle"));
                break;
            case ZOMBIFIED_PIGLIN:
                ((PigZombie) mob).setAnger((Integer) mobData.get("anger"));
                break;
            case PUFFERFISH:
                ((PufferFish) mob).setPuffState((Integer) mobData.get("puffState"));
                break;
            case RABBIT:
                ((Rabbit) mob).setRabbitType(Rabbit.Type.valueOf((String) mobData.get("rabbitType")));
                break;
            case SHEEP:
                ((Sheep) mob).setSheared((Boolean) mobData.get("isSheared"));
                break;
            case SNOWMAN:
                ((Snowman) mob).setDerp((Boolean) mobData.get("isDerp"));
                break;
            case STRIDER:
                ((Strider) mob).setSaddle((Boolean) mobData.get("hasSaddle"));
                break;
            case VILLAGER:
                Villager v = (Villager) mob;
                v.setProfession(Villager.Profession.valueOf((String) mobData.get("profession")));
                v.setVillagerType(Villager.Type.valueOf((String) mobData.get("type")));
                v.setVillagerLevel((Integer) mobData.get("level"));
                v.setVillagerExperience((Integer) mobData.get("experience"));
                break;
            case ZOMBIE_VILLAGER:
                ZombieVillager z = (ZombieVillager) mob;
                z.setConversionPlayer(server.getOfflinePlayer(UUID.fromString((String) mobData.get("conversionPlayer"))));
                z.setConversionTime((Integer) mobData.get("conversionTime"));
                z.setVillagerProfession(Villager.Profession.valueOf((String) mobData.get("profession")));
                z.setVillagerType(Villager.Type.valueOf((String) mobData.get("type")));
                break;
        }

        // If the mob is a villager or wandering trader, retrieve the saved trades and apply them to the merchant.
        if (mob instanceof Villager || mob.getType().name().equals("WANDERING_TRADER")) {
            List<MerchantRecipe> trades = new ArrayList<>();
            for (int i = 0; i < config.getConfigurationSection("pets." + uuid + ".trades").getKeys(false).size(); i++) {
                String path = "trades." + i + ".";
                MerchantRecipe t = new MerchantRecipe((ItemStack) mobData.get(path + "result"),
                        (Integer) mobData.get(path + "uses"), (Integer) mobData.get(path + "maxUses"),
                        (Boolean) mobData.get(path + "experienceReward"),
                        (Integer) mobData.get(path + "villagerExperience"),
                        (Float) mobData.get(path + "priceMultiplier"), (Integer) mobData.get(path + "demand"),
                        (Integer) mobData.get(path + "specialPrice"));
                List<ItemStack> ingredients = new ArrayList<>();
                for (int j = 0; j < config.getConfigurationSection("pets." + uuid + "." + path + "ingredients")
                        .getKeys(false).size(); j++) {
                    ingredients.add((ItemStack) mobData.get(path + "ingredients." + j));
                }
                t.setIngredients(ingredients);
                trades.add(t);
            }

            if (serverVersion > 10) {
                ((Merchant) mob).setRecipes(trades);
            } else {
                ((Villager) mob).setRecipes(trades);
            }
        }

        removePet(uuid.toString());
    }

    /**
     * Gets the {@link EntityType} associated with a stored pet
     *
     * @param uuid the UUID associated with the stored pet
     * @return the type of the stored pet
     * @throws IllegalArgumentException if the storage has no pet with the associated UUID
     */
    public EntityType identifyPet(String uuid) {
        ConfigurationSection section = config.getConfigurationSection("pets." + uuid);
        if (section == null)
            throw new IllegalArgumentException("No such UUID exists in the configuration.");

        return EntityType.valueOf(section.getString("entityType"));
    }

    /**
     * Removes a pet from storage
     *
     * @param uuid the UUID associated with the stored pet
     */
    public void removePet(String uuid) {
        config.set("pets." + uuid, null);
    }

    /**
     * Saves captured pet's data to YAML config
     *
     * @param uuid the UUID associated with the stored pet
     * @param mobData a map of the mob's data
     */
    private void set(UUID uuid, Map<String, Object> mobData) {
        mobData.forEach((l, v) -> config.set("pets." + uuid.toString() + "." + l, v));
    }

    /**
     * Gets captured pet's data from YAML config
     *
     * @param uuid the UUID associated with the stored pet
     * @return a map of the mob's data
     */
    private Map<String, Object> get(UUID uuid) {
        return config.getConfigurationSection("pets." + uuid.toString()).getValues(true);
    }

    /**
     * Checks if storage contains a mob with a specified UUID
     *
     * @param uuid the UUID being checked
     * @return if storage contains a mob with the specified UUID
     */
    public boolean contains(String uuid) {
        return config.contains("pets." + uuid);
    }

    public void save() throws IOException {
        config.save(saveFile);
    }
}
