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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class for saving data to a config file.
 */
@SuppressWarnings("ConstantConditions")
public class DataStorage {

    private final Server server;

    private final FileConfiguration config;
    private final File saveFile;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public DataStorage(JavaPlugin plugin) {
        server = plugin.getServer();

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

    public void savePet(Mob mob, UUID uuid) {
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
        if (mob instanceof Sittable) mobData.put("isSitting", ((Sittable) mob).isSitting());
        if (mob instanceof Steerable) mobData.put("hasSaddle", ((Steerable) mob).hasSaddle());

        if (mob instanceof AbstractHorse) {
            AbstractHorse h = (AbstractHorse) mob;
            mobData.put("jumpStrength", h.getJumpStrength());
            mobData.put("maxHealth", h.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            mobData.put("speed", getHorseSpeed(h));
            if (h instanceof ChestedHorse) mobData.put("isCarryingChest", ((ChestedHorse) h).isCarryingChest());
        }

        switch (mob.getType()) {
            case WOLF:
                mobData.put("collarColor", ((Wolf) mob).getCollarColor().name());
                break;
            case CAT:
                mobData.put("breed", ((Cat) mob).getCatType().name());
                break;
            case HORSE:
                mobData.put("color", ((Horse) mob).getColor().name());
                mobData.put("style", ((Horse) mob).getStyle().name());
                break;
            case LLAMA:
                mobData.put("color", ((Llama) mob).getColor().name());
                mobData.put("strength", ((Llama) mob).getStrength());
                break;
            case PARROT:
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
            case MUSHROOM_COW:
                mobData.put("variant", ((MushroomCow) mob).getVariant().name());
                break;
            case OCELOT:
                mobData.put("isTrusting", ((Ocelot) mob).isTrusting());
                break;
            case PANDA:
                mobData.put("mainGene", ((Panda) mob).getMainGene().name());
                mobData.put("hiddenGene", ((Panda) mob).getHiddenGene().name());
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
            case SLIME:
                mobData.put("size", ((Slime) mob).getSize());
                break;
            case SNOWMAN:
                mobData.put("isDerp", ((Snowman) mob).isDerp());
                break;
        }

        set(uuid, mobData);
    }

    public void restorePet(Mob mob, UUID uuid) {
        Map<String, Object> mobData = get(uuid);

        mob.setCustomName((String) mobData.get("name"));

        if (mob instanceof Tameable) {
            Tameable t = (Tameable) mob;
            t.setTamed((Boolean) mobData.get("isTamed"));
            String owner = (String) mobData.get("owner");
            if (owner != null) t.setOwner(server.getOfflinePlayer(UUID.fromString(owner)));
        }

        if (mob instanceof Ageable) ((Ageable) mob).setAge((Integer) mobData.get("age"));
        if (mob instanceof Sittable) ((Sittable) mob).setSitting((Boolean) mobData.get("isSitting"));
        if (mob instanceof Steerable) ((Steerable) mob).setSaddle((Boolean) mobData.get("hasSaddle"));

        if (mob instanceof AbstractHorse) {
            AbstractHorse h = (AbstractHorse) mob;
            h.setJumpStrength((Double) mobData.get("jumpStrength"));
            h.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue((Double) mobData.get("maxHealth"));
            setHorseSpeed(h, (Double) mobData.get("speed"));
            if (h instanceof ChestedHorse) ((ChestedHorse) h).setCarryingChest((Boolean) mobData.get("isCarryingChest"));
        }

        mob.setHealth((Double) mobData.get("health"));

        switch (mob.getType()) {
            case WOLF:
                ((Wolf) mob).setCollarColor(DyeColor.valueOf((String) mobData.get("collarColor")));
                break;
            case CAT:
                ((Cat) mob).setCatType(Cat.Type.valueOf((String) mobData.get("breed")));
                break;
            case HORSE:
                ((Horse) mob).setColor(Horse.Color.valueOf((String) mobData.get("color")));
                ((Horse) mob).setStyle(Horse.Style.valueOf((String) mobData.get("style")));
                break;
            case LLAMA:
                ((Llama) mob).setColor(Llama.Color.valueOf((String) mobData.get("color")));
                ((Llama) mob).setStrength((Integer) mobData.get("strength"));
                break;
            case PARROT:
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
            case MUSHROOM_COW:
                ((MushroomCow) mob).setVariant(MushroomCow.Variant.valueOf((String) mobData.get("variant")));
                break;
            case OCELOT:
                ((Ocelot) mob).setTrusting((Boolean) mobData.get("isTrusting"));
                break;
            case PANDA:
                ((Panda) mob).setMainGene(Panda.Gene.valueOf((String) mobData.get("mainGene")));
                ((Panda) mob).setHiddenGene(Panda.Gene.valueOf((String) mobData.get("hiddenGene")));
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
            case SLIME:
                ((Slime) mob).setSize((Integer) mobData.get("size"));
                break;
            case SNOWMAN:
                ((Snowman) mob).setDerp((Boolean) mobData.get("isDerp"));
                break;
        }

        removePet(uuid.toString());
    }

    public EntityType identifyPet(String uuid) {
        ConfigurationSection section = config.getConfigurationSection("pets." + uuid);
        if (section == null)
            throw new IllegalArgumentException("No such UUID exists in the configuration.");

        return EntityType.valueOf(section.getString("entityType"));
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

    private void set(UUID uuid, Map<String, Object> mobData)
    {
        mobData.forEach((l, v) -> config.set("pets." + uuid.toString() + "." + l, v));
    }

    private Map<String, Object> get(UUID uuid)
    {
        return config.getConfigurationSection("pets." + uuid.toString()).getValues(false);
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
