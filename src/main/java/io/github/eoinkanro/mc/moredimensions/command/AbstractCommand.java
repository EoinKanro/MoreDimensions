package io.github.eoinkanro.mc.moredimensions.command;

import com.mojang.brigadier.context.CommandContext;
import io.github.eoinkanro.mc.moredimensions.tools.ActionResponse;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public abstract class AbstractCommand {

  protected final DimensionManager dimensionManager;

  protected AbstractCommand(DimensionManager dimensionManager) {
    this.dimensionManager = dimensionManager;
  }

  public final int perform(CommandContext<CommandSourceStack> context) {
    try {
      var result = process(context);

      sendResponseToSource(context, result);
      return result.getCode();
    } catch (Exception e) {
      context.getSource().sendFailure(Component.literal(e.getMessage()));
      return 0;
    }
  }

  private void sendResponseToSource(CommandContext<CommandSourceStack> context, ActionResponse response) {
    var result = Component.literal(response.getMessage());
    if (response.getCode() == 1) {
      context.getSource().sendSuccess(() -> result, false);
    } else {
      context.getSource().sendFailure(result);
    }
  }

  protected abstract ActionResponse process(CommandContext<CommandSourceStack> context);

}
