package io.github.eoinkanro.mc.moredimensions.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static io.github.eoinkanro.mc.moredimensions.MoreDimensions.MOD_ID;

public class DimensionManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean createDimension(MinecraftServer server, String name, CommandSourceStack source) {
        String dimensionName = name.toLowerCase().replaceAll("[^a-z0-9_\\-]", "");

        Path dimensionJsonPath = getDimensionJsonPath(server, dimensionName);
        if (Files.exists(dimensionJsonPath)) {
            source.sendFailure(Component.literal("Dimension '" + name + "' already exists."));
            return false;
        }

        try {
            JsonObject dimensionJson = generateDimensionJson();

            Files.createDirectories(dimensionJsonPath.getParent());
            Files.writeString(dimensionJsonPath, GSON.toJson(dimensionJson));

            source.sendSuccess(() -> Component.literal("Dimension '" + name + "' created. Please restart the server to load the new dimension."), false);

            return true;

        } catch (IOException e) {
            //TODO
            e.printStackTrace();
            source.sendFailure(Component.literal("Failed to create dimension '" + name + "'. See server log for details."));
            return false;
        }
    }

    private static JsonObject generateDimensionJson() {
        // Create the JSON object representing the dimension
        JsonObject dimensionJson = new JsonObject();

        // Set the type to "minecraft:overworld"
        dimensionJson.addProperty("type", "minecraft:overworld");

        // Create the generator object
        JsonObject generator = new JsonObject();
        generator.addProperty("type", "minecraft:noise");
        generator.addProperty("seed", new Random().nextLong());

        // Use the Overworld settings
        generator.addProperty("settings", "minecraft:overworld");

        // Set the biome source
        JsonObject biomeSource = new JsonObject();
        biomeSource.addProperty("type", "minecraft:multi_noise");
        biomeSource.addProperty("preset", "minecraft:overworld");

        generator.add("biome_source", biomeSource);

        // Add the generator to the dimension JSON
        dimensionJson.add("generator", generator);

        return dimensionJson;
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

    private static Path getDimensionPath(MinecraftServer server) {
        return getDatapackDataPath(server)
                .resolve("dimension");
    }

    private static Path getDimensionTypePath(MinecraftServer server) {
        return getDatapackDataPath(server)
                .resolve("dimension_type");
    }

    private static Path getDimensionJsonPath(MinecraftServer server, String dimensionName) {
        return getDimensionPath(server)
                .resolve(dimensionName + ".json");
    }

    /**
     * Create default files for future dimensions
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

        Path dimensionTypePath = getDimensionTypePath(server);
        if (!Files.exists(dimensionTypePath)) {
            Files.createDirectories(dimensionTypePath);
        }

        Path defaultDimensionTypePath = dimensionTypePath.resolve(MOD_ID + ".json");
        if (!Files.exists(defaultDimensionTypePath)) {
            Files.createFile(defaultDimensionTypePath);
            Files.writeString(defaultDimensionTypePath, GSON.toJson(generateDefaultDimensionType()));
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
}
