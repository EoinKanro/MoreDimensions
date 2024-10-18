package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.context.CommandContext;
import io.github.eoinkanro.mc.moredimensions.tools.ActionResponse;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.commands.CommandSourceStack;

public class DeleteDimensionCommand extends AbstractNamedCommand {

  public DeleteDimensionCommand(DimensionManager dimensionManager) {
    super(dimensionManager);
  }

  @Override
  protected ActionResponse process(CommandContext<CommandSourceStack> context, String name) {
    return dimensionManager.deleteDimension(context.getSource().getServer(), name);
  }

}
