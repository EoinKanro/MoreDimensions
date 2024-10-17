package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;

public class CreateDimensionCommand {

  public static int perform(CommandContext<CommandSourceStack> context) {
    String name = StringArgumentType.getString(context, "name");
    MinecraftServer server = context.getSource().getServer();

    return DimensionManager.createDimension(server, name, context.getSource());
  }

}
