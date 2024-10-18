package io.github.eoinkanro.mc.moredimensions;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import io.github.eoinkanro.mc.moredimensions.command.CreateDimensionCommand;
import io.github.eoinkanro.mc.moredimensions.command.DeleteDimensionCommand;
import io.github.eoinkanro.mc.moredimensions.command.ListDimensionsCommand;
import io.github.eoinkanro.mc.moredimensions.command.TeleportToDimensionCommand;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

@Mod(MoreDimensions.MOD_ID)
public class MoreDimensions {

  private static final Logger LOGGER = LogUtils.getLogger();
  public static final String MOD_ID = "moredimensions";
  public static final String COMMAND_ID = "moredim";

  private final DimensionManager dimensionManager = new DimensionManager();
  private final CreateDimensionCommand createDimensionCommand = new CreateDimensionCommand(dimensionManager);
  private final DeleteDimensionCommand deleteDimensionCommand = new DeleteDimensionCommand(dimensionManager);
  private final ListDimensionsCommand listDimensionsCommand = new ListDimensionsCommand(dimensionManager);
  private final TeleportToDimensionCommand teleportToDimensionCommand = new TeleportToDimensionCommand(dimensionManager);

  // Valid constructor, only uses two of the available argument types
  public MoreDimensions(IEventBus modBus, ModContainer container) {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onServerAboutToStart(ServerAboutToStartEvent event) {
    try {
      dimensionManager.init(event.getServer());
    } catch (Exception e) {
      LOGGER.error("Failed to initialize DimensionManager", e);
    }
  }

  @SubscribeEvent
  public void onServerStopped(ServerStoppedEvent event) {
    try {
      dimensionManager.stop(event.getServer());
    } catch (Exception e) {
      LOGGER.error("Failed to stop DimensionManager", e);
    }
  }

  @SubscribeEvent
  public void onRegisterCommands(RegisterCommandsEvent event) {
    event.getDispatcher().register(
        Commands.literal(COMMAND_ID)
            .then(
                Commands.literal("create")
                    .requires(source -> source.hasPermission(3))
                    .then(
                        Commands.argument("name", StringArgumentType.string())
                            .executes(createDimensionCommand::perform)
                    )
            )
            .then(
                Commands.literal("delete")
                    .requires(source -> source.hasPermission(3))
                    .then(
                        Commands.argument("name", StringArgumentType.string())
                            .executes(deleteDimensionCommand::perform)
                    )
            ).then(
                Commands.literal("tp")
                    .then(
                        Commands.argument("name", StringArgumentType.string())
                            .executes(teleportToDimensionCommand::perform)
                    )
            )
            .then(
                Commands.literal("list")
                    .executes(listDimensionsCommand::perform)
            )
    );
  }


}
