package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import io.github.eoinkanro.mc.moredimensions.tools.ActionResponse;
import net.minecraft.commands.CommandSourceStack;

public abstract class AbstractNamedCommand extends AbstractCommand {

  public static final String NAME_FIELD = "name";

  public AbstractNamedCommand(DimensionManager dimensionManager) {
    super(dimensionManager);
  }

  @Override
  protected final ActionResponse process(CommandContext<CommandSourceStack> context) {
    String name = StringArgumentType.getString(context, NAME_FIELD);
    return process(context, name);
  }

  protected abstract ActionResponse process(CommandContext<CommandSourceStack> context, String name);

}
