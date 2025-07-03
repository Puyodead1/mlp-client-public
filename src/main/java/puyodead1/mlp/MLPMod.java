package puyodead1.mlp;

import meteordevelopment.meteorclient.MeteorClient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.NameGenerator;
import puyodead1.mlp.client.ProfileCache;
import puyodead1.mlp.client.ui.serverlist.MLPMultiplayerScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.LOG;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MLPMod implements ModInitializer {
    public static final MLPService mlpService = new MLPService();

    public static final Identifier CAPE_TEXTURE = Identifier.of("mlp:cape.png");
    public static final Identifier cockSound = Identifier.of("mlp:cock");
    public static final Identifier shotgunSound = Identifier.of("mlp:shot");

    public static SoundEvent shotgunSoundEvent = SoundEvent.of(shotgunSound);
    public static SoundEvent cockSoundEvent = SoundEvent.of(cockSound);

    public static ProfileCache profileCache = new ProfileCache();
    public static GenericNames genericNames = new GenericNames();

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

    public static boolean is2b2t() {
        ServerInfo serverEntry = MeteorClient.mc.getCurrentServerEntry();
        if (serverEntry == null) {
            return false;
        }
        return serverEntry.address.contains("2b2t.org");
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

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            genericNames.clear();
        });

        Registry.register(Registries.SOUND_EVENT, shotgunSound, shotgunSoundEvent);
        Registry.register(Registries.SOUND_EVENT, cockSound, cockSoundEvent);
    }

    public static class GenericNames {
        private final Map<UUID, String> names = new HashMap<>();

        public String getName(UUID uuid) {
            this.names.computeIfAbsent(uuid, k -> NameGenerator.name(uuid));
            return this.names.get(uuid);
        }

        public void clear() {
            this.names.clear();
        }
    }
}
