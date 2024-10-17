package io.github.eoinkanro.mc.moredimensions.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;

import static io.github.eoinkanro.mc.moredimensions.MoreDimensions.MOD_ID;

public class DimensionManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Set<String> FORBIDDEN_BIOMES = Set.of("minecraft:crimson_forest",
        "minecraft:end_midlands", "minecraft:nether_wastes", "minecraft:the_void", "minecraft:end_highlands",
        "minecraft:end_barrens", "minecraft:small_end_islands", "minecraft:the_end", "minecraft:soul_sand_valley",
        "minecraft:warped_forest");


    public static int createDimension(MinecraftServer server, String name, CommandSourceStack source) {
        String dimensionName = name.toLowerCase().replaceAll("[^a-z0-9_\\-]", "");

        Path dimensionJsonPath = getDimensionJsonPath(server, dimensionName);
        if (Files.exists(dimensionJsonPath)) {
            source.sendFailure(Component.literal("Dimension '" + name + "' already exists."));
            return 0;
        }

        try {
            Files.createDirectories(dimensionJsonPath.getParent());
            Files.writeString(dimensionJsonPath, GSON.toJson(generateDimensionJson(server, dimensionName)));

            Path dimensionTypeJsonPath = getDimensionTypeJsonPath(server, dimensionName);
            Files.createDirectories(dimensionTypeJsonPath.getParent());
            Files.writeString(dimensionTypeJsonPath, GSON.toJson(generateDefaultDimensionType()));

            source.sendSuccess(() -> Component.literal("Dimension '" + name + "' created. Please restart the server to load the new dimension."), false);
            return 1;
        } catch (IOException e) {
            LOGGER.error("Failed to create dimension '{}'.", name, e);
            source.sendFailure(Component.literal("Failed to create dimension '" + name + "'. See server log for details."));
            return 0;
        }
    }

    private static JsonObject generateDimensionJson(MinecraftServer server, String dimensionName) {
        JsonObject dimensionJson = new JsonObject();

        dimensionJson.addProperty("type", getFullDimensionName(dimensionName));

        JsonObject generator = new JsonObject();
        generator.addProperty("type", "minecraft:noise");
        generator.addProperty("settings", "minecraft:overworld");

        // Set the biome source
        JsonObject biomeSource = new JsonObject();
        biomeSource.addProperty("type", "minecraft:multi_noise");

        JsonArray biomes = new JsonArray();

        RegistryAccess registryAccess = server.registryAccess();
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
        Random random = new Random();
        biomeRegistry.keySet().forEach(biomeLocation -> {
            String biomeName = biomeLocation.getNamespace() + ":" + biomeLocation.getPath();
            if (FORBIDDEN_BIOMES.contains(biomeName)) {
                return;
            }

            JsonObject biome = new JsonObject();
            biome.addProperty("biome", biomeName);

            JsonObject parameters = new JsonObject();

            parameters.add("temperature", createBiomeParameterRandomArray(random));
            parameters.add("humidity", createBiomeParameterRandomArray(random));
            parameters.add("continentalness", createBiomeParameterRandomArray(random));
            parameters.add("erosion", createBiomeParameterRandomArray(random));
            parameters.add("weirdness", createBiomeParameterRandomArray(random));
            parameters.add("depth", createBiomeParameterRandomArray(random));
            parameters.addProperty("offset", getBiomeOffsetRandom(random));

            biome.add("parameters", parameters);
            biomes.add(biome);
        });

        biomeSource.add("biomes", biomes);
        generator.add("biome_source", biomeSource);
        dimensionJson.add("generator", generator);

        return dimensionJson;
    }

    /**
     * Default dimension type that is used in all new dimensions
     */
    private static JsonObject generateDefaultDimensionType() {
        JsonObject defaultDimensionType = new JsonObject();

        defaultDimensionType.addProperty("ultrawarm", false);
        defaultDimensionType.addProperty("natural", true);
        defaultDimensionType.addProperty("piglin_safe", false);
        defaultDimensionType.addProperty("respawn_anchor_works", false);
        defaultDimensionType.addProperty("bed_works", true);
        defaultDimensionType.addProperty("has_raids", true);
        defaultDimensionType.addProperty("has_skylight", true);
        defaultDimensionType.addProperty("has_ceiling", false);
        defaultDimensionType.addProperty("coordinate_scale", 1);
        defaultDimensionType.addProperty("ambient_light", 0);
        defaultDimensionType.addProperty("logical_height", 384);
        defaultDimensionType.addProperty("height", 384);
        defaultDimensionType.addProperty("min_y", -64);
        defaultDimensionType.addProperty("effects", "minecraft:overworld");
        defaultDimensionType.addProperty("infiniburn", "#minecraft:infiniburn_overworld");
        defaultDimensionType.addProperty("monster_spawn_block_light_limit", 0);

        JsonObject monsterSpawnLightLevel = new JsonObject();
        monsterSpawnLightLevel.addProperty("type", "minecraft:uniform");
        monsterSpawnLightLevel.addProperty("min_inclusive", 0);
        monsterSpawnLightLevel.addProperty("max_inclusive", 0);

        defaultDimensionType.add("monster_spawn_light_level", monsterSpawnLightLevel);

        return defaultDimensionType;
    }

    private static JsonArray createBiomeParameterRandomArray(Random random) {
        JsonArray array = new JsonArray();
        double first = getBiomeParameterRandom(random);
        double second = getBiomeParameterRandom(random);

        array.add(Math.min(first, second));
        array.add(Math.max(first, second));
        return array;
    }

    private static float getBiomeParameterRandom(Random random) {
        return random.nextFloat(-2, 2);
    }

    private static float getBiomeOffsetRandom(Random random) {
        return random.nextFloat(0, 1);
    }

    public static int deleteDimension(MinecraftServer server, String dimensionName, CommandSourceStack source) {
        Path dimensionJsonPath = getDimensionJsonPath(server, dimensionName);
        if (!Files.exists(dimensionJsonPath)) {
            source.sendFailure(Component.literal("Dimension '" + dimensionName + "' does not exist."));
            return 0;
        }

        tryToCloseDimension(server, dimensionName);

        try {
            deletePath(dimensionJsonPath);

            Path dimensionTypeJsonPath = getDimensionTypeJsonPath(server, dimensionName);
            deletePath(dimensionTypeJsonPath);

            Path dimensionDataPath = getDimensionDataPath(server, dimensionName);
            deletePath(dimensionDataPath);

            source.sendSuccess(() -> Component.literal("Dimension '" + dimensionName + "' deleted. Please restart the server."), false);
            return 1;
        } catch (Exception e) {
          LOGGER.error("Can't delete dimension '{}'.", dimensionName, e);
            source.sendFailure(Component.literal("Can't delete dimension '" + dimensionName + "'."));
            return 0;
        }
    }

    private static void deletePath(Path path) throws IOException {
        if (Files.exists(path)) {
            deleteFileRecursive(path.toFile());
        }
    }

    private static void deleteFileRecursive(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFileRecursive(f);
                }
            }
        }
        file.delete();
    }

    private static void tryToCloseDimension(MinecraftServer server, String dimensionName) {
        try {
            ResourceLocation dimensionLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, dimensionName);
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);

            ServerLevel targetDimension = server.getLevel(dimensionKey);
            List<ServerPlayer> playersInDimension = server.getPlayerList().getPlayers().stream()
                .filter(player -> player.level() == targetDimension)
                .toList();

            ServerLevel overworld = server.getLevel(Level.OVERWORLD); // Overworld dimension
            for (ServerPlayer player : playersInDimension) {
                BlockPos spawnPos = overworld.getSharedSpawnPos();
                float spawnAngle = overworld.getSharedSpawnAngle();

                if (player.getRespawnDimension() == targetDimension.dimension()) {
                    player.setRespawnPosition(Level.OVERWORLD, spawnPos, spawnAngle, false, false);
                }

                player.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, spawnAngle, 0.0F);
                player.sendSystemMessage(Component.literal("You have been teleported to the overworld."));
            }

            targetDimension.close();
        } catch (Exception e) {
            LOGGER.error("Failed to close dimension '{}'.", dimensionName, e);
        }
    }

    private static Path getDatapackPath(MinecraftServer server) {
        return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("datapacks")
                .resolve(MOD_ID + "_dimensions");
    }

    private static Path getDatapackDataPath(MinecraftServer server) {
        return getDatapackPath(server)
                .resolve("data")
                .resolve(MOD_ID);
    }

    private static Path getDimensionJsonPath(MinecraftServer server, String dimensionName) {
        return getDatapackDataPath(server)
                .resolve("dimension")
                .resolve(dimensionName + ".json");
    }

    private static Path getDimensionTypeJsonPath(MinecraftServer server, String dimensionName) {
        return getDatapackDataPath(server)
            .resolve("dimension_type")
            .resolve(dimensionName + ".json");
    }

    private static Path getDimensionDataPath(MinecraftServer server, String dimensionName) {
        return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("dimensions")
            .resolve(MOD_ID)
            .resolve(dimensionName);
    }

    private static String getFullDimensionName(String dimensionName) {
        return MOD_ID + ":" + dimensionName;
    }

    /**
     * Create datapack folder and pack.mcmeta file
     */
    public static void init(MinecraftServer server) throws IOException {
        Path datapackPath = getDatapackPath(server);
        if (!Files.exists(datapackPath)) {
            Files.createDirectories(datapackPath);
        }

        Path packMcmetaPath = datapackPath.resolve("pack.mcmeta");
        if (!Files.exists(packMcmetaPath)) {
            Files.createFile(packMcmetaPath);
            Files.writeString(packMcmetaPath, GSON.toJson(generatePackMcmeta()));
        }
    }

    /**
     * File that is necessary for loading the datapack
     */
    private static JsonObject generatePackMcmeta() {
        JsonObject packMcmeta = new JsonObject();

        JsonObject pack = new JsonObject();
        pack.addProperty("description", "More Dimensions worlds");
        pack.addProperty("pack_format", 48);

        packMcmeta.add("pack", pack);

        return packMcmeta;
    }

}
