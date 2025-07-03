package puyodead1.mlp.modules.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

public class BloatCMD extends Command {
    public BloatCMD() {
        super("bloat", "Adds bloat to the item data", "inflate");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("bytes", IntegerArgumentType.integer(1)).executes(context -> {
            ItemStack item = mc.player.getMainHandStack();
            int bytes = context.getArgument("bytes", Integer.class);

            NbtCompound nbt = StringNbtReader.readCompound("{a:[{}" + ",{}".repeat((bytes - (bytes % 3)) / 3) + "]}");
            item.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));
            return SINGLE_SUCCESS;
        }));
    }
}
