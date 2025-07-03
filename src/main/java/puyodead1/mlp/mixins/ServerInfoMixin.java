package puyodead1.mlp.mixins;

import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerInfo.class)
public abstract class ServerInfoMixin {
    @Shadow
    private ServerInfo.Status status;

    @Inject(method = "setStatus", at = @At("HEAD"), cancellable = true)
    private void setStatus(ServerInfo.Status status, CallbackInfo ci) {
        if(status == ServerInfo.Status.INCOMPATIBLE)
            status = ServerInfo.Status.SUCCESSFUL;

        this.status = status;

        ci.cancel();
    }
}
