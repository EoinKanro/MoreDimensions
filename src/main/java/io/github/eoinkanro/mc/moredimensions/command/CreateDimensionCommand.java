package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.eoinkanro.mc.moredimensions.tools.ActionResponse;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import io.github.eoinkanro.mc.moredimensions.tools.GeneratorType;
import net.minecraft.commands.CommandSourceStack;

public class CreateDimensionCommand extends AbstractNamedCommand {

  public static final String TYPE_FIELD = "type";

  public CreateDimensionCommand(DimensionManager dimensionManager) {
    super(dimensionManager);
  }

  @Override
  protected ActionResponse process(CommandContext<CommandSourceStack> context, String name) {
    String typeStr = StringArgumentType.getString(context, TYPE_FIELD);
    return dimensionManager.createDimension(context.getSource().getServer(), name, GeneratorType.ofName(typeStr));
  }

}
