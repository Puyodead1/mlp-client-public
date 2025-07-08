/*
 * This file is taken from McsdcMeteor (https://github.com/Nxyi/McsdcMeteor).
 */

package puyodead1.mlp.modules.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import puyodead1.mlp.modules.MLPAddOn;

public class TicketIDCommand extends Command {
    public TicketIDCommand() {
        super("ticketID", "Get the current servers MCSDC Ticket ID");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.executes(context -> {
            mc.keyboard.setClipboard(MLPAddOn.getTicketID());
            return 1;
        });
    }
}
