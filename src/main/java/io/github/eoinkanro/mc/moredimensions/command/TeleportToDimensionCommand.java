package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.context.CommandContext;
import io.github.eoinkanro.mc.moredimensions.tools.ActionResponse;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

import static io.github.eoinkanro.mc.moredimensions.MoreDimensions.MOD_ID;

public class TeleportToDimensionCommand extends AbstractNamedCommand{

  public TeleportToDimensionCommand(DimensionManager dimensionManager) {
    super(dimensionManager);
  }

  @Override
  protected ActionResponse process(CommandContext<CommandSourceStack> context, String name) {
    String dimensionName = dimensionManager.toDimensionName(name);

    if (!dimensionManager.isDimensionAvailable(dimensionName)) {
      return new ActionResponse(0, "Dimension '" + dimensionName + "' is not available for teleportation");
    }

    ResourceLocation dimensionId = getDimension(dimensionName);
    ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
    ServerLevel targetLevel = context.getSource().getServer().getLevel(dimensionKey);

    if (targetLevel != null) {
      ServerPlayer player = context.getSource().getPlayer();
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
      return new ActionResponse(1, "Teleported to dimension '" + dimensionName + "'.");
    } else {
      return new ActionResponse(0, "Dimension '" + dimensionName + "' does not exist or is not loaded.");
    }
  }

  private ResourceLocation getDimension(String dimensionName) {
    if (DimensionManager.OVERWORLD_NAMES.contains(dimensionName)) {
      return ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");
    } else {
      return ResourceLocation.fromNamespaceAndPath(MOD_ID, dimensionName);
    }
  }

}
