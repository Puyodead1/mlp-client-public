package puyodead1.mlp.mixins;

import puyodead1.mlp.modules.StreamerMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Shadow
    public abstract SyncedClientOptions getSyncedOptions();

    @ModifyArg(
        method = "sendClientSettings",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;syncOptions(Lnet/minecraft/network/packet/c2s/common/SyncedClientOptions;)V"
        ))
    private SyncedClientOptions mlp$forceDisableServerListing(SyncedClientOptions syncedOptions) {
        if (StreamerMode.isStreaming()) {
            SyncedClientOptions oldOptions = this.getSyncedOptions();
            return new SyncedClientOptions(oldOptions.language(), oldOptions.viewDistance(), oldOptions.chatVisibility(), oldOptions.chatColorsEnabled(), oldOptions.playerModelParts(), oldOptions.mainArm(), oldOptions.filtersText(), false, oldOptions.particleStatus());
        }
        return syncedOptions;
    }
}
