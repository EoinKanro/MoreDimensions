package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.context.CommandContext;
import io.github.eoinkanro.mc.moredimensions.tools.ActionResponse;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.commands.CommandSourceStack;

public class ListDimensionsCommand extends AbstractCommand {

  public ListDimensionsCommand(DimensionManager dimensionManager) {
    super(dimensionManager);
  }

  @Override
  protected ActionResponse process(CommandContext<CommandSourceStack> context) {
    return dimensionManager.getAllDimensions(context.getSource().getServer());
  }

}
