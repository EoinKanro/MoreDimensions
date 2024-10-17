package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class DeleteDimensionCommand {

  public static int perform(CommandContext<CommandSourceStack> context) {
    String name = StringArgumentType.getString(context, "name");
    MinecraftServer server = context.getSource().getServer();

    return DimensionManager.deleteDimension(server, name, context.getSource());
  }

}
