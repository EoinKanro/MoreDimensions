package io.github.eoinkanro.mc.moredimensions;

import com.mojang.logging.LogUtils;
import io.github.eoinkanro.mc.moredimensions.command.CreateDimensionCommand;
import io.github.eoinkanro.mc.moredimensions.command.TpDimensionCommand;
import io.github.eoinkanro.mc.moredimensions.tools.DimensionManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(MoreDimensions.MOD_ID)
public class MoreDimensions {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "moredimensions";

    // Valid constructor, only uses two of the available argument types
    public MoreDimensions(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        try {
            DimensionManager.init(event.getServer());
        } catch (Exception e) {
            LOGGER.error("Failed to initialize DimensionManager", e);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CreateDimensionCommand.register(event.getDispatcher());
        TpDimensionCommand.register(event.getDispatcher());
    }


}
