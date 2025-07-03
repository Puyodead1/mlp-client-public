package puyodead1.mlp.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayNetworkHandlerAccessor;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.util.*;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;

public class Detect extends Module {
    private static final Set<String> PLUGINS_TO_DETECT = Set.of(
        "nocheatplus", "negativity", "warden", "horizon", "illegalstack",
        "coreprotect", "exploitsx", "vulcan", "abc", "spartan", "kauri",
        "anticheatreloaded", "witherac", "godseye", "matrix", "wraith",
        "antixrayheuristics", "grimac", "discordsrv", "essentialsdiscord"
    );

    private static final Set<String> VERSION_ALIASES = Set.of(
        "version", "ver", "about", "bukkit:version", "bukkit:ver", "bukkit:about"
    );

    private final List<String> detectedPlugins = new ArrayList<>();
    private final List<String> commandTreePlugins = new ArrayList<>();
    private String alias = null;
    private boolean requestSent = false;
    private int tickCounter = 0;
    private boolean detectionSent = false; // Flag to track if detection message is already sent

    public Detect() {
        super(MLPAddOn.MLP_CATEGORY, "PluginDetect", "Detects specific plugins running on the server.");
    }

    // This will be triggered when the module is enabled
    @Override
    public void onActivate() {
        super.onActivate();
        if (!detectionSent) {
            initiatePluginDetection();
        }
    }

    private void initiatePluginDetection() {
        sendCommandTreeRequest();
    }

    private void sendCommandTreeRequest() {
        mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(new Random().nextInt(200), "/"));
        requestSent = true;
        tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!requestSent || alias == null) return;

        tickCounter++;
        if (tickCounter >= 100) { // Timeout after 5 seconds (100 ticks)
            if (!detectionSent) {
                printDetectedPlugins("Timeout reached. No plugins detected.");
                detectionSent = true; // Mark as sent
            }
            resetDetectionState();  // Reset after timeout
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof CommandTreeS2CPacket packet) {
            handleCommandTreePacket(packet);
        } else if (event.packet instanceof CommandSuggestionsS2CPacket packet) {
            handleCommandSuggestionsPacket(packet);
        }
    }

    private void handleCommandTreePacket(CommandTreeS2CPacket packet) {
        ClientPlayNetworkHandlerAccessor handler = (ClientPlayNetworkHandlerAccessor) mc.getNetworkHandler();
        CommandRegistryAccess commandRegistryAccess = CommandRegistryAccess.of(handler.getCombinedDynamicRegistries(), handler.getEnabledFeatures());

        alias = null;
        commandTreePlugins.clear();

        packet.getCommandTree(commandRegistryAccess, ClientPlayNetworkHandlerAccessor.getCommandNodeFactory()).getChildren().forEach(node -> {
            String[] split = node.getName().split(":");
            System.out.println(node.getName());
            System.out.println(split[0]);
            if (split.length > 1 && !commandTreePlugins.contains(split[0])) {
                commandTreePlugins.add(split[0]);
            }

            if (alias == null && VERSION_ALIASES.contains(node.getName())) {
                alias = node.getName();
            }
        });

        if (alias != null) {
            mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(new Random().nextInt(200), alias + " "));
            requestSent = true;
            tickCounter = 0;
        }
    }

    private void handleCommandSuggestionsPacket(CommandSuggestionsS2CPacket packet) {
        packet.getSuggestions().getList().forEach(suggestion -> {
            String pluginName = suggestion.getText().toLowerCase();
            if (!detectedPlugins.contains(pluginName)) {
                detectedPlugins.add(pluginName);
            }
        });

        // Only send the message when both packets have been processed (CommandTree and CommandSuggestions)
        if (!detectionSent) {
            printDetectedPlugins("Suggestions packet processed.");
            detectionSent = true; // Mark as sent
        }
        resetDetectionState();  // Reset after processing suggestions
    }

    private void printDetectedPlugins(String message) {
        Set<String> allDetectedPlugins = new HashSet<>(detectedPlugins);
        allDetectedPlugins.addAll(commandTreePlugins);

        List<String> matchedPlugins = new ArrayList<>();
        for (String plugin : allDetectedPlugins) {
            if (PLUGINS_TO_DETECT.contains(plugin.toLowerCase())) {
                matchedPlugins.add(plugin);
            }
        }

        if (!matchedPlugins.isEmpty()) {
            String pluginsList = String.join(", ", matchedPlugins);
            if (matchedPlugins.size() == 1) {
                ChatUtils.info("§cDetected plugin: " + pluginsList);
            } else {
                ChatUtils.info("§cDetected plugins: " + pluginsList);
            }
        } else {
            ChatUtils.info("§aNo detected plugins from the given list. " + message);
        }
    }

    private void resetDetectionState() {
        detectedPlugins.clear();
        commandTreePlugins.clear();
        requestSent = false;
        tickCounter = 0;
    }
}
