package puyodead1.mlp.modules.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class MLPVanityTagCMD extends Command {
    public MLPVanityTagCMD() {
        super("mlp", "Sends MLP discord link in chat");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ChatUtils.sendPlayerMsg("https://discord.gg/mlpi");
            return SINGLE_SUCCESS;
        });
    }
}
