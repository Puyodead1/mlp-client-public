package puyodead1.mlp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import puyodead1.mlp.client.ui.serverlist.MLPMultiplayerScreen;

import static meteordevelopment.meteorclient.MeteorClient.LOG;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MLPMod implements ModInitializer {
    public static final MLPService mlpService = new MLPService();

    public static final Identifier CAPE_TEXTURE = Identifier.of("mlp:cape.png");

    private static MLPMod INSTANCE;
    private static boolean isFromSSScreen = false;
    private MLPMultiplayerScreen multiplayerScreen;

    public static MLPService getMLPService() {
        return mlpService;
    }

    public static MLPMultiplayerScreen getMultiplayerScreen() {
        return MLPMod.INSTANCE.multiplayerScreen;
    }

    public static void setMultiplayerScreen(MLPMultiplayerScreen multiplayerScreen) {
        MLPMod.INSTANCE.multiplayerScreen = multiplayerScreen;
    }

    public static MLPMultiplayerScreen getOrCreateMultiplayerScreen(Screen parent) {
        if (MLPMod.INSTANCE.multiplayerScreen == null) {
            MLPMod.INSTANCE.multiplayerScreen = new MLPMultiplayerScreen(parent, mlpService);
        }
        return MLPMod.INSTANCE.multiplayerScreen;
    }


    public static boolean isFromSSScreen() {
        return isFromSSScreen;
    }

    public static void setIsFromSSScreen(boolean value) {
        isFromSSScreen = value;
    }

    @Override
    public void onInitialize() {
        LOG.info("MLPMod initializing");

        INSTANCE = new MLPMod();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            mlpService.setLastServerInfo(mc.getCurrentServerEntry());
            mlpService.updateJoined();
        });
    }
}
