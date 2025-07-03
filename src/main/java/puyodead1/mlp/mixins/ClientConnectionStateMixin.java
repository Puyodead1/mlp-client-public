package puyodead1.mlp.mixins;

import puyodead1.mlp.modules.StreamerMode;
import net.minecraft.client.network.ClientConnectionState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientConnectionState.class)
public class ClientConnectionStateMixin {

    @Inject(method = "serverBrand", at = @At("HEAD"), cancellable = true)
    private void mlp$fakeServerBrand(CallbackInfoReturnable<String> cir) {
        if(!StreamerMode.isStreaming()) return;

        String fakeBrand = StreamerMode.spoofServerBrand();
        if (!fakeBrand.isEmpty()) {
            cir.setReturnValue(fakeBrand);
        }
    }
}
