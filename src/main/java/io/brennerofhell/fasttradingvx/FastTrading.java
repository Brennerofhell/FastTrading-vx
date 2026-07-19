package io.brennerofhell.fasttradingvx;

import eu.midnightdust.lib.config.MidnightConfig;
import io.brennerofhell.fasttradingvx.config.ModConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = FastTrading.MOD_ID, dist = Dist.CLIENT)
public class FastTrading {
    public static final String MOD_ID = "fasttradingvx";
    public static final String MOD_NAME = "Fast Trading vX";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public FastTrading(IEventBus modEventBus, ModContainer modContainer) {
        MidnightConfig.init(MOD_ID, ModConfig.class);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, modListScreen) -> MidnightConfig.getScreen(modListScreen, MOD_ID));
        modEventBus.addListener(ModKeyBindings::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Waste your hard-earned emeralds with ease!");
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        SpeedTradeTimer.onClientWorldTick();
    }
}
