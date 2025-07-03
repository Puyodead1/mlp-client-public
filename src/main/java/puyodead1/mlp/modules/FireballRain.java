package puyodead1.mlp.modules;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class FireballRain extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("In what radius the fireballs should spawn.")
        .defaultValue(100)
        .min(1)
        .sliderMax(255)
        .build()
    );

    private final Setting<Double> power = sgGeneral.add(new DoubleSetting.Builder()
        .name("fireball-power")
        .description("How powerful the fireballs should be.")
        .defaultValue(5)
        .min(0)
        .sliderMax(127)
        .build()
    );

    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("fireball-height")
        .description("At what Y level the fireballs should spawn.")
        .defaultValue(100)
        .min(-60)
        .sliderMax(320)
        .build()
    );

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("fireball-speed")
        .description("How fast the fireballs should be fired.")
        .defaultValue(10)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between fireball spawns.")
        .defaultValue(0)
        .min(0)
        .sliderMax(50)
        .build()
    );

    private final Setting<Boolean> atPlayerPos = sgGeneral.add(new BoolSetting.Builder()
        .name("at-player-pos")
        .description("Whether or not to spawn the fireballs at the player's position.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables module on kick.")
        .defaultValue(true)
        .build()
    );

    public FireballRain() {
        super(MLPAddOn.MLP_CATEGORY, "fireball-rain", "what do you think it does...");
    }

    private int ticks;
    private Vec3d origin = null;

    private Vec3d pickRandomPos() {
        double x = new Random().nextDouble(radius.get() * 2) - radius.get() + (atPlayerPos.get() ? mc.player.getPos().x : origin.x);
        double y = height.get();
        double z = new Random().nextDouble(radius.get() * 2) - radius.get() + (atPlayerPos.get() ? mc.player.getPos().z : origin.z);
        return new Vec3d(x, y, z);
    }

    @Override
    public void onActivate() {
        if (!mc.player.getAbilities().creativeMode) {
            error("You must be in creative mode to use this.");
            toggle();
            return;
        }
        ticks = 0;
        origin = mc.player.getPos();
    }

    @Override
    public void onDeactivate() {
        mc.player.getInventory().setSelectedSlot(0);
        mc.interactionManager.clickCreativeStack(new ItemStack(Items.AIR), 36);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!mc.player.getAbilities().creativeMode) {
            error("You must be in creative mode to use this.");
            toggle();
            return;
        }
        ticks++;
        if (ticks <= delay.get() && delay.get() != 0) return;
        ItemStack stack = new ItemStack(Items.BAT_SPAWN_EGG);
        NbtCompound tag = new NbtCompound();
        tag.put("id", NbtString.of("minecraft:fireball"));
        NbtList pos = new NbtList();
        pos.add(NbtDouble.of(pickRandomPos().x));
        pos.add(NbtDouble.of(height.get()));
        pos.add(NbtDouble.of(pickRandomPos().z));
        tag.put("Pos", pos);
        NbtList motion = new NbtList();
        motion.add(NbtDouble.of(0));
        motion.add(NbtDouble.of(-speed.get()));
        motion.add(NbtDouble.of(0));
        tag.put("power", motion);
        tag.put("ExplosionPower", NbtDouble.of(power.get()));
        stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(tag));
        BlockHitResult bhr = new BlockHitResult(mc.player.getPos(), Direction.DOWN, mc.player.getBlockPos(), false);
        mc.interactionManager.clickCreativeStack(stack, 36);
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, 0));
        mc.player.getInventory().setSelectedSlot(0);
        ticks = 0;
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }
}
