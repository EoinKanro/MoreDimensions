package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;

public class DeleteDimensionCommand {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
        Commands.literal("deletedim")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(context -> {
                  String name = StringArgumentType.getString(context, "name");
                  MinecraftServer server = context.getSource().getServer();

                  return DimensionManager.deleteDimension(server, name, context.getSource());
                }))
    );
  }

}
