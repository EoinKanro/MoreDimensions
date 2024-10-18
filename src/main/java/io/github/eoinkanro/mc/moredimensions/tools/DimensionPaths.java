package io.github.eoinkanro.mc.moredimensions.tools;

import static io.github.eoinkanro.mc.moredimensions.MoreDimensions.MOD_ID;

import java.nio.file.Path;
import net.minecraft.server.MinecraftServer;

public class DimensionPaths {

  /**
   * @return ./world/datapacks/moredimensions_dimensions
   */
  public static Path getDatapackPath(MinecraftServer server) {
    return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
        .resolve("datapacks")
        .resolve(MOD_ID + "_dimensions");
  }

  /**
   * @return ./world/datapacks/moredimensions_dimensions/data/moredimensions
   */
  public static Path getDatapackDataPath(MinecraftServer server) {
    return getDatapackPath(server)
        .resolve("data")
        .resolve(MOD_ID);
  }

  /**
   * @return ./world/datapacks/moredimensions_dimensions/data/moredimensions/dimension/
   */
  public static Path getDatapackDimensionPath(MinecraftServer server) {
    return getDatapackDataPath(server)
        .resolve("dimension");
  }

  /**
   * @return ./world/datapacks/moredimensions_dimensions/data/moredimensions/dimension/${dimensionName}.json
   */
  public static Path getDimensionJsonPath(MinecraftServer server, String dimensionName) {
    return getDatapackDimensionPath(server)
        .resolve(dimensionName + ".json");
  }


  /**
   * @return ./world/datapacks/moredimensions_dimensions/data/moredimensions/dimension_type/${dimensionName}.json
   */
  public static Path getDimensionTypeJsonPath(MinecraftServer server, String dimensionName) {
    return getDatapackDataPath(server)
        .resolve("dimension_type")
        .resolve(dimensionName + ".json");
  }

  /**
   * @return ./world/dimensions/moredimensions/${dimensionName}
   */
  public static Path getDimensionDataPath(MinecraftServer server, String dimensionName) {
    return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
        .resolve("dimensions")
        .resolve(MOD_ID)
        .resolve(dimensionName);
  }

  /**
   * @return ./world/datapacks/moredimensions_dimensions/toDelete.json
   */
  public static Path getPathToDeleteJson(MinecraftServer server) {
    return getDatapackPath(server)
        .resolve("toDelete.json");
  }

  /**
   * @return ./world/datapacks/moredimensions_dimensions/exclude.json
   */
  public static Path getConfigExcludePath(MinecraftServer server) {
    return getDatapackPath(server)
        .resolve("exclude.json");
  }

}
