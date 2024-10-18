package io.github.eoinkanro.mc.moredimensions.tools;

import static io.github.eoinkanro.mc.moredimensions.MoreDimensions.MOD_ID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;

public class DimensionGenerator {

  private static final Set<String> FORBIDDEN_BIOMES = Set.of("minecraft:crimson_forest",
      "minecraft:end_midlands", "minecraft:nether_wastes", "minecraft:the_void", "minecraft:end_highlands",
      "minecraft:end_barrens", "minecraft:small_end_islands", "minecraft:the_end", "minecraft:soul_sand_valley",
      "minecraft:warped_forest");

  /**
   * Random overworld dimension JSON
   */
  public static JsonObject generateDimensionJson(MinecraftServer server, String dimensionName,
      Set<String> excludedBiomes) {
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
      if (FORBIDDEN_BIOMES.contains(biomeName) || excludedBiomes.contains(biomeName)) {
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

  private static String getFullDimensionName(String dimensionName) {
    return MOD_ID + ":" + dimensionName;
  }

  /**
   * Default dimension type that is used in all new dimensions
   */
  public static JsonObject generateDimensionType() {
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
