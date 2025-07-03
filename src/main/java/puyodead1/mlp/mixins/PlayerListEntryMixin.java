package puyodead1.mlp.mixins;

import com.mojang.authlib.GameProfile;
import puyodead1.mlp.MLPMod;
import puyodead1.mlp.MLPService;
import puyodead1.mlp.modules.StreamerMode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {
    //    @Shadow
//    @Final
//    private Map<MinecraftProfileTexture.Type, Identifier> textures;
    @Shadow
    @Final
    private GameProfile profile;
//    @Shadow
//    @Nullable
//    private String model;

//    @Shadow
//    public abstract GameProfile getProfile();

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void mlp$modifyPlayerDisplayName(CallbackInfoReturnable<Text> cir) {
        if (!this.profile.getId().equals(MeteorClient.mc.player.getUuid())) {
            if (StreamerMode.isGenerifyNames()) {
                String fakeName = MLPMod.genericNames.getName(this.profile.getId());
                cir.setReturnValue(Text.of(fakeName));
            }
        }
    }

//    @Redirect(method = "getSkinTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;loadTextures()V"))
//    private void mlp$modifyPlayerSkinTexture(PlayerListEntry instance) {
//        PlayerListEntryMixin playerListEntryMixin = this;
//        synchronized (playerListEntryMixin) {
//            if (this.profile.getId().equals(MeteorClient.mc.player.getUuid())) {
//                LarpModule larpModule = Modules.get().get(LarpModule.class);
//                if (larpModule.isActive()) {
//                    UUID larpUid = UUID.fromString(larpModule.alias.get());
//                    NMod.profileCache.texture(larpUid).ifPresent(this::setSkinTexture);
//                } else {
//                    NMod.profileCache.texture(this.profile.getId()).ifPresent(this::setSkinTexture);
//                }
//            } else {
//                List<CopeService.Griefer> griefers = NMod.getCopeService().griefers();
//                if (StreamerMode.isGenerifyNames()) {
//                    this.model = "default";
//                }
//                for (CopeService.Griefer griefer : griefers) {
//                    if (this.profile.getId().equals(griefer.playerId)) {
//                        NMod.profileCache.findPlayerName(griefer.playerNameAlias).flatMap(NMod.profileCache::texture).ifPresent(this::setSkinTexture);
//                    }
//                }
//            }
//        }
//    }

//    private void setSkinTexture(ProfileCache.TextureResult textureResult) {
//        this.textures.put(textureResult.type, textureResult.id);
//        if (textureResult.type == MinecraftProfileTexture.Type.SKIN) {
//            String modelName = textureResult.texture.getMetadata("model");
//            this.model = modelName == null ? "default" : modelName;
//        }
//    }

//    @Inject(method = "getSkinTextures", at = @At("TAIL"), cancellable = true)
//    private void mlp$modifyCapeTexture(CallbackInfoReturnable<SkinTextures> info) {
//        NService nSerivce = NMod.getNService();
//
//        if (MeteorClient.mc.player != null && MeteorClient.mc.player.getGameProfile().getName().equals(this.getProfile().getName()) || nSerivce.griefers().stream().anyMatch(griefer -> this.getProfile().getName().equals(griefer.playerName))) {
//            SkinTextures prev = info.getReturnValue();
//            SkinTextures newTextures = new SkinTextures(prev.texture(), prev.textureUrl(), NMod.CAPE_TEXTURE, NMod.CAPE_TEXTURE, prev.model(), prev.secure());
//            info.setReturnValue(newTextures);
//        }
//    }
}
