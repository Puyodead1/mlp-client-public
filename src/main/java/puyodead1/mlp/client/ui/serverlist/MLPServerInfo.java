package puyodead1.mlp.client.ui.serverlist;

import puyodead1.mlp.MLPService;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MLPServerInfo extends ServerInfo {
    public static final Identifier TNT_BLOCK_TEXTURE = Identifier.of("minecraft:textures/block/tnt_side.png");
    private final MLPService.Server server;

    public MLPServerInfo(String name, MLPService.Server server) {
        super(name, server.address, ServerType.OTHER);

        this.server = server;
        this.playerCountLabel = Text.of("Updating...");
    }
}
