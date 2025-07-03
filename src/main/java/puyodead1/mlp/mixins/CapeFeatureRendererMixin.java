package puyodead1.mlp.mixins;

// modified from meteor client (https://github.com/MeteorDevelopment/meteor-client)

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import puyodead1.mlp.MLPMod;
import puyodead1.mlp.MLPService;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import puyodead1.mlp.modules.CapeModule;

@Mixin(value = CapeFeatureRenderer.class, priority = 900)
public class CapeFeatureRendererMixin {
    @ModifyExpressionValue(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SkinTextures;capeTexture()Lnet/minecraft/util/Identifier;"))
    private Identifier modifyCapeTexture(Identifier original, MatrixStack matrices, VertexConsumerProvider consumers, int i, PlayerEntityRenderState state, float f, float g) {
        if (Modules.get().isActive(CapeModule.class) && MeteorClient.mc.player != null && MeteorClient.mc.player.getGameProfile().getName().equals(state.name)) {

            return MLPMod.CAPE_TEXTURE;
        }

        return original;
    }
}
