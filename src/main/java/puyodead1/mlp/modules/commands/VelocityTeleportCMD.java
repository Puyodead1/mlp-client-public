package puyodead1.mlp.modules.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AntiHunger;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import puyodead1.mlp.MLPService;

import java.util.List;
import java.util.stream.Collectors;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class VelocityTeleportCMD extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(VelocityTeleportCMD.class);
    public static final List<Module> CONFLICTING_MODULES = List.of(VelocityTeleportCMD.getModule(AntiHunger.class));

    public VelocityTeleportCMD() {
        super("tp", "Jank op-less teleporting");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("x", DoubleArgumentType.doubleArg())
            .then(argument("y", DoubleArgumentType.doubleArg())
                .then(argument("z", DoubleArgumentType.doubleArg())
                    .executes(this::runTeleport))));
    }

    private int runTeleport(CommandContext<CommandSource> context) {
        assert (MeteorClient.mc.player != null);

        double x = context.getArgument("x", Double.class);
        double y = context.getArgument("y", Double.class);
        double z = context.getArgument("z", Double.class);
        LOGGER.debug("Teleporting to {}, {}, {}", x, y, x);

        ClientPlayerEntity player = MeteorClient.mc.player;
        double selfX = player.getX();
        double selfY = player.getY();
        double selfZ = player.getZ();

        double distance = Math.pow(Math.pow(x - selfX, 2.0) + Math.pow(y - selfY, 2.0) + Math.pow(z - selfZ, 2.0), 0.5);
        double packetsNeeded = Math.ceil(distance / 0.038 - 1.54);

        float yaw = (float) (Math.atan2(x - selfX, z - selfZ) * (180 / Math.PI));
        float pitch = (float) Math.asin((y - selfY) / distance);

        LOGGER.debug("Traveling {} blocks, need {} packets.", distance, packetsNeeded);

        List<Module> modules =  disengageConflictingModules();

        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, true, false));
        player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        for (double i = 0.0; i < packetsNeeded; i += 1.0) {
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(selfX, selfY - 1.0E-9, selfZ, true, false));
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(selfX, selfY + 1.0E-9, selfZ, false, false));
        }
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, false));

        reengageModules(modules);

        return SINGLE_SUCCESS;
    }

    public static List<Module> disengageConflictingModules() {
        List<Module> modules = CONFLICTING_MODULES.stream().filter(Module::isActive).collect(Collectors.toList());
        modules.forEach(Module::toggle);
        return modules;
    }

    public static void reengageModules(List<Module> modules) {
        modules.forEach(Module::toggle);
    }

    private static Module getModule(Class<? extends Module> module) {
        return Modules.get().get(module);
    }
}
