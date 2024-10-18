package io.github.eoinkanro.mc.moredimensions.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import static io.github.eoinkanro.mc.moredimensions.MoreDimensions.MOD_ID;

public class DimensionManager {

    public static final Set<String> OVERWORLD_NAMES = Set.of("overworld", "main");


    private final Logger log = LogUtils.getLogger();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Short names without ....:
     */
    private final Set<String> dimensionsToDelete = new HashSet<>();

    /**
     * Full names like minecraft:forest
     */
    private final Set<String> excludedBiomes = new HashSet<>();

    /**
     * Transform short name of dimension to MC friendly format
     */
    public String toDimensionName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_\\-]", "");
    }

    /**
     * If dimensions doesn't mark for deletion
     */
    public boolean isDimensionAvailable(String dimensionName) {
        return !dimensionsToDelete.contains(dimensionName);
    }

    /**
     * Create files of dimension in datapack folder
     */
    public synchronized ActionResponse createDimension(MinecraftServer server, String name, GeneratorType type) {
        String dimensionName = toDimensionName(name);

        if (dimensionsToDelete.contains(dimensionName)) {
            return new ActionResponse(0, "Dimension '" + dimensionName + "' marked for deletion.");
        }

        Path dimensionJsonPath = DimensionPaths.getDimensionJsonPath(server, dimensionName);
        if (Files.exists(dimensionJsonPath)) {
            return new ActionResponse(0, "Dimension '" + dimensionName + "' already exists.");
        }

        try {
            JsonObject dimensionJson = type == GeneratorType.OVERWORLD ?
                DimensionGenerator.generateOverworldDimensionJson(name)
                : DimensionGenerator.generateRandomDimensionJson(server, name, excludedBiomes);

            Path dimensionTypeJsonPath = DimensionPaths.getDimensionTypeJsonPath(server, dimensionName);
            Files.createDirectories(dimensionTypeJsonPath.getParent());
            Files.writeString(dimensionTypeJsonPath, gson.toJson(DimensionGenerator.generateDimensionType()));

            Files.createDirectories(dimensionJsonPath.getParent());
            Files.writeString(dimensionJsonPath, gson.toJson(dimensionJson));

            return new ActionResponse(1, "Dimension with type '" + type + "' and name '" + dimensionName + "' created. Please restart the server to load the new dimension.");
        } catch (IOException e) {
            log.error("Failed to create dimension '{}'.", name, e);
            return new ActionResponse(0, "Failed to create dimension '" + dimensionName + "'. See server log for details.");
        }
    }

    /**
     * Mark dimension for deletion on server shutdown.
     * Create file with dimensions for deletion to try to delete after restart if server crushes.
     * Teleport all players from the dimension.
     */
    public synchronized ActionResponse deleteDimension(MinecraftServer server, String name) {
        String dimensionName = toDimensionName(name);
        if (dimensionsToDelete.contains(dimensionName)) {
            return new ActionResponse(0, "Dimension '" + dimensionName + "' has been marked for deletion already.");
        }

        Path dimensionJsonPath = DimensionPaths.getDimensionJsonPath(server, dimensionName);
        if (!Files.exists(dimensionJsonPath)) {
            return new ActionResponse(0, "Dimension '" + dimensionName + "' does not exist.");
        }

        teleportPlayersFromDimension(server, dimensionName);
        dimensionsToDelete.add(dimensionName);

        Path toDeletePath = DimensionPaths.getPathToDeleteJson(server);
        try {
            if (!Files.exists(toDeletePath)) {
                Files.createDirectories(toDeletePath.getParent());
                Files.createFile(toDeletePath);
            }

            try (FileWriter fileWriter = new FileWriter(toDeletePath.toFile(), false)) {
                fileWriter.write(gson.toJson(dimensionsToDelete));
                fileWriter.flush();
            }
        } catch (Exception e) {
            log.warn("Failed to save dimensions for deletion to file", e);
        }

        return new ActionResponse(1, "Dimension '" + dimensionName + "' will be deleted on restart.");
    }

    private void teleportPlayersFromDimension(MinecraftServer server, String dimensionName) {
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
            log.error("Failed to teleport player from dimension '{}'.", dimensionName, e);
        }
    }

    /**
     * Get list of all available dimensions except for dimensions for deletion
     */
    public ActionResponse getAllDimensions(MinecraftServer server) {
        List<String> result = new ArrayList<>(OVERWORLD_NAMES);

        Path dimensionsPath = DimensionPaths.getDatapackDimensionPath(server);
        if (Files.exists(dimensionsPath)) {
            File dimensionsFile = dimensionsPath.toFile();

            File[] files = dimensionsFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        String name = file.getName().replaceAll(".json", "");

                        if (!dimensionsToDelete.contains(name)) {
                            result.add(name);
                        }
                    }
                }
            }
        }

        return new ActionResponse(1, "Available dimensions: " + result);
    }

    /**
     * Create datapack folder and pack.mcmeta file
     * Try to delete dimensions data from file if exists
     * Add try to read exclude file
     */
    public void init(MinecraftServer server) throws IOException {
        log.info("Initializing DimensionManager...");
        Path datapackPath = DimensionPaths.getDatapackPath(server);
        if (!Files.exists(datapackPath)) {
            Files.createDirectories(datapackPath);
        }

        Path packMcmetaPath = datapackPath.resolve("pack.mcmeta");
        if (!Files.exists(packMcmetaPath)) {
            Files.createFile(packMcmetaPath);
            Files.writeString(packMcmetaPath, gson.toJson(generatePackMcmeta()));
        }

        tryToDeleteOnInit(server);
        tryToAddExcludeBiomes(server);
    }

    private void tryToDeleteOnInit(MinecraftServer server) {
        Path toDeletePath = DimensionPaths.getPathToDeleteJson(server);
        if (!Files.exists(toDeletePath)) {
            return;
        }

        Set<String> toDelete;
        try {
            toDelete = pathToSet(toDeletePath);
        } catch (Exception e) {
            log.error("Critical error during deletion of dimensions. "
                + "Beware, they will not be deleted of startup. "
                + "You can try to delete them again and restart server normally.", e);
            return;
        }

        try {
            Set<String> undeleted = cleanDimensionsData(server, toDelete);
            if (!undeleted.isEmpty()) {
                log.error("Can't delete dimensions: {}", undeleted);
            }

            dimensionsToDelete.addAll(undeleted);
        } catch (Exception e) {
            dimensionsToDelete.addAll(toDelete);
            log.error("Failed to clean dimensions from backup file.", e);
        }
    }

    private void tryToAddExcludeBiomes(MinecraftServer server) {
        Path excludePath = DimensionPaths.getConfigExcludePath(server);
        if (!Files.exists(excludePath)) {
            return;
        }

        try {
            excludedBiomes.addAll(pathToSet(excludePath));
            log.info("Added biomes for exclusion: {}", excludedBiomes);
        } catch (Exception e) {
            log.error("Can't load file for exclusion", e);
        }
    }

    private Set<String> pathToSet(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        StringBuilder jsonBuilder = new StringBuilder();
        lines.forEach(jsonBuilder::append);

        JsonArray jsonArray = gson.fromJson(jsonBuilder.toString(), JsonArray.class);
        return gson.fromJson(jsonArray, TypeToken.getParameterized(Set.class, String.class).getType());
    }

    /**
     * File that is necessary for loading the datapack
     */
    private JsonObject generatePackMcmeta() {
        JsonObject packMcmeta = new JsonObject();

        JsonObject pack = new JsonObject();
        pack.addProperty("description", "More Dimensions worlds");
        pack.addProperty("pack_format", 48);

        packMcmeta.add("pack", pack);

        return packMcmeta;
    }

    /**
     * Try to delete marked dimensions
     */
    public void stop(MinecraftServer server) {
        log.info("Stopping DimensionManager...");
        cleanDimensionsData(server, dimensionsToDelete);
    }

    /**
     * Delete data of marked dimensions for deletion
     *
     * @return dimensions that can't be deleted
     */
    private Set<String> cleanDimensionsData(MinecraftServer server, Set<String> dimensions) {
        log.info("Deleting dimensions data...");

        Set<String> toDelete = new HashSet<>();
        for (String dimensionName : dimensions) {
            try {
                Path dimensionJsonPath = DimensionPaths.getDimensionJsonPath(server, dimensionName);
                deletePath(dimensionJsonPath);

                Path dimensionTypeJsonPath = DimensionPaths.getDimensionTypeJsonPath(server, dimensionName);
                deletePath(dimensionTypeJsonPath);

                Path dimensionDataPath = DimensionPaths.getDimensionDataPath(server, dimensionName);
                deletePath(dimensionDataPath);

                log.info("Dimension '{}' deleted", dimensionName);
            } catch (Exception e) {
                log.error("Can't delete dimension '{}'.", dimensionName, e);
                toDelete.add(dimensionName);
            }
        }

        if (toDelete.isEmpty()) {
            Path toDeletePath = DimensionPaths.getPathToDeleteJson(server);
            try {
                Files.deleteIfExists(toDeletePath);
            } catch (Exception e) {
                log.error("Can't remove to delete dimensions file.", e);
            }
        }

        return toDelete;
    }

    private void deletePath(Path path) {
        if (Files.exists(path)) {
            deleteFileRecursive(path.toFile());
        }
    }

    private void deleteFileRecursive(File file) {
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
