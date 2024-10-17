package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.arguments.StringArgumentType;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.registries.Registries;

import static io.github.eoinkanro.mc.moredimensions.MoreDimensions.MOD_ID;

public class TeleportToDimensionCommand {

  public static int perform(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    //TODO TP TO OVERWORLD

    String name = StringArgumentType.getString(context, "name").toLowerCase()
        .replaceAll("[^a-z0-9_\\-]", "");
    CommandSourceStack source = context.getSource();

    if (!DimensionManager.isDimensionAvailable(name)) {
      source.sendFailure(Component.literal(
          "Dimension '" + name + "' is not available for teleportation"));
      return 0;
    }

    ServerPlayer player = source.getPlayerOrException();
    MinecraftServer server = source.getServer();

    ResourceLocation dimensionId = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION,
        dimensionId);

    ServerLevel targetLevel = server.getLevel(dimensionKey);

    if (targetLevel != null) {
      BlockPos spawnPos = targetLevel.getSharedSpawnPos();
      float spawnAngle = targetLevel.getSharedSpawnAngle();

      player.teleportTo(
          targetLevel,
          spawnPos.getX() + 0.5,
          spawnPos.getY(),
          spawnPos.getZ() + 0.5,
          spawnAngle,
          player.getXRot()
      );
      source.sendSuccess(() -> Component.literal("Teleported to dimension '" + name + "'."), false);
      return 1;
    } else {
      source.sendFailure(Component.literal("Dimension '" + name + "' does not exist or is not loaded."));
      return 0;
    }
  }
}
