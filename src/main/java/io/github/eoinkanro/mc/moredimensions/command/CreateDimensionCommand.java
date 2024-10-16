package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CreateDimensionCommand {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
        Commands.literal("createdim")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(context -> {
                  String name = StringArgumentType.getString(context, "name");
                  MinecraftServer server = context.getSource().getServer();

                  boolean success = DimensionManager.createDimension(server, name, context.getSource());

                  return success ? 1 : 0;
                }))
    );
  }

}
