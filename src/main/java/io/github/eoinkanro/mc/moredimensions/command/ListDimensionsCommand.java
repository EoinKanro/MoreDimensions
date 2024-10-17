package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.context.CommandContext;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class ListDimensionsCommand {

  public static int perform(CommandContext<CommandSourceStack> context) {
    MinecraftServer server = context.getSource().getServer();
    return DimensionManager.getAllDimensions(server, context.getSource());
  }

}
