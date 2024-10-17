package io.github.eoinkanro.mc.moredimensions.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import static io.github.eoinkanro.mc.moredimensions.MoreDimensions.MOD_ID;

public class DimensionManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Set<String> DIMENSIONS_TO_DELETE = new HashSet<>();

    public static boolean isDimensionAvailable(String dimensionName) {
        return !DIMENSIONS_TO_DELETE.contains(dimensionName);
    }

    public static int createDimension(MinecraftServer server, String name, CommandSourceStack source) {
        String dimensionName = name.toLowerCase().replaceAll("[^a-z0-9_\\-]", "");

        if (DIMENSIONS_TO_DELETE.contains(dimensionName)) {
            source.sendFailure(Component.literal("Dimension '" + name + "' marked for deletion."));
            return 0;
        }

        Path dimensionJsonPath = DimensionPath.getDimensionJsonPath(server, dimensionName);
        if (Files.exists(dimensionJsonPath)) {
            source.sendFailure(Component.literal("Dimension '" + name + "' already exists."));
            return 0;
        }

        try {
            Path dimensionTypeJsonPath = DimensionPath.getDimensionTypeJsonPath(server, dimensionName);
            Files.createDirectories(dimensionTypeJsonPath.getParent());
            Files.writeString(dimensionTypeJsonPath, GSON.toJson(DimensionGenerator.generateDimensionType()));

            Files.createDirectories(dimensionJsonPath.getParent());
            Files.writeString(dimensionJsonPath, GSON.toJson(DimensionGenerator.generateDimensionJson(server, dimensionName)));

            source.sendSuccess(() -> Component.literal("Dimension '" + name + "' created. Please restart the server to load the new dimension."), false);
            return 1;
        } catch (IOException e) {
            LOGGER.error("Failed to create dimension '{}'.", name, e);
            source.sendFailure(Component.literal("Failed to create dimension '" + name + "'. See server log for details."));
            return 0;
        }
    }

    public static int deleteDimension(MinecraftServer server, String dimensionName, CommandSourceStack source) {
        if (DIMENSIONS_TO_DELETE.contains(dimensionName)) {
            source.sendFailure(Component.literal("Dimension '" + dimensionName + "' has been marked for deletion already."));
            return 0;
        }

        Path dimensionJsonPath = DimensionPath.getDimensionJsonPath(server, dimensionName);
        if (!Files.exists(dimensionJsonPath)) {
            source.sendFailure(Component.literal("Dimension '" + dimensionName + "' does not exist."));
            return 0;
        }

        teleportPlayersFromDimension(server, dimensionName);
        DIMENSIONS_TO_DELETE.add(dimensionName);

        Path toDeletePath = DimensionPath.getPathToDeleteJson(server);
        try {
            if (!Files.exists(toDeletePath)) {
                Files.createDirectories(toDeletePath.getParent());
                Files.createFile(toDeletePath);
            }

            try (FileWriter fileWriter = new FileWriter(toDeletePath.toFile(), false)) {
                fileWriter.write(GSON.toJson(DIMENSIONS_TO_DELETE));
                fileWriter.flush();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to save dimensions fro deletion to file", e);
        }

        source.sendSuccess(() -> Component.literal("Dimension '" + dimensionName + "' will be deleted on restart. "
            + "Please restart the server."), false);
        return 1;
    }

    private static void teleportPlayersFromDimension(MinecraftServer server, String dimensionName) {
        try {
            ResourceLocation dimensionLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, dimensionName);
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);

            ServerLevel targetDimension = server.getLevel(dimensionKey);
            List<ServerPlayer> playersInDimension = server.getPlayerList().getPlayers().stream()
                .filter(player -> player.level() == targetDimension)
                .toList();

            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            for (ServerPlayer player : playersInDimension) {
                BlockPos spawnPos = overworld.getSharedSpawnPos();
                float spawnAngle = overworld.getSharedSpawnAngle();

                if (player.getRespawnDimension() == targetDimension.dimension()) {
                    player.setRespawnPosition(Level.OVERWORLD, spawnPos, spawnAngle, false, false);
                }

                player.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, spawnAngle, 0.0F);
                player.sendSystemMessage(Component.literal("You have been teleported to the overworld."));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to teleport player from dimension '{}'.", dimensionName, e);
        }
    }

    /**
     * Create datapack folder and pack.mcmeta file
     * Try to delete dimensions data from file if exists
     */
    public static void init(MinecraftServer server) throws IOException {
        LOGGER.info("Initializing DimensionManager...");
        Path datapackPath = DimensionPath.getDatapackPath(server);
        if (!Files.exists(datapackPath)) {
            Files.createDirectories(datapackPath);
        }

        Path packMcmetaPath = datapackPath.resolve("pack.mcmeta");
        if (!Files.exists(packMcmetaPath)) {
            Files.createFile(packMcmetaPath);
            Files.writeString(packMcmetaPath, GSON.toJson(generatePackMcmeta()));
        }

        Path toDeletePath = DimensionPath.getPathToDeleteJson(server);
        if (Files.exists(toDeletePath)) {
            Set<String> toDelete;
            try {
                List<String> lines = Files.readAllLines(toDeletePath);
                StringBuilder jsonBuilder = new StringBuilder();
                lines.forEach(jsonBuilder::append);

                JsonArray jsonArray = GSON.fromJson(jsonBuilder.toString(), JsonArray.class);
                toDelete = GSON.fromJson(jsonArray, TypeToken.getParameterized(Set.class, String.class).getType());
            } catch (Exception e) {
                LOGGER.error("Critical error during deletion of dimensions. "
                    + "Beware, they will not be deleted of startup. "
                    + "You can try to delete them again and restart server normally.", e);
                return;
            }

            try {
                Set<String> undeleted = cleanDimensionsData(server, toDelete);
                if (!undeleted.isEmpty()) {
                    LOGGER.error("Can't delete dimensions: {}", undeleted);
                }

                DIMENSIONS_TO_DELETE.addAll(undeleted);
            } catch (Exception e) {
                LOGGER.error("Failed to clean dimensions from backup file.", e);
            }
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

    public static void stop(MinecraftServer server) {
        LOGGER.info("Stopping DimensionManager...");
        cleanDimensionsData(server, DIMENSIONS_TO_DELETE);
    }

    /**
     * Delete data of marked for deletion dimensions
     *
     * @return dimensions that can't delete
     */
    private static Set<String> cleanDimensionsData(MinecraftServer server, Set<String> dimensions) {
        LOGGER.info("Deleting dimensions data...");

        Set<String> toDelete = new HashSet<>();
        for (String dimensionName : dimensions) {
            try {
                Path dimensionJsonPath = DimensionPath.getDimensionJsonPath(server, dimensionName);
                deletePath(dimensionJsonPath);

                Path dimensionTypeJsonPath = DimensionPath.getDimensionTypeJsonPath(server, dimensionName);
                deletePath(dimensionTypeJsonPath);

                Path dimensionDataPath = DimensionPath.getDimensionDataPath(server, dimensionName);
                deletePath(dimensionDataPath);

                LOGGER.info("Dimension '{}' deleted", dimensionName);
            } catch (Exception e) {
                LOGGER.error("Can't delete dimension '{}'.", dimensionName, e);
                toDelete.add(dimensionName);
            }
        }

        if (toDelete.isEmpty()) {
            Path toDeletePath = DimensionPath.getPathToDeleteJson(server);
            try {
                Files.deleteIfExists(toDeletePath);
            } catch (Exception e) {
                LOGGER.error("Can't remove to delete dimensions file.", e);
            }
        }

        return toDelete;
    }

    private static void deletePath(Path path) {
        if (Files.exists(path)) {
            deleteFileRecursive(path.toFile());
        }
    }

    private static void deleteFileRecursive(File file) {
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

}
