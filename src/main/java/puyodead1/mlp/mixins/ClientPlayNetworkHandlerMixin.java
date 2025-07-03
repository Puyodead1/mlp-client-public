package puyodead1.mlp.mixins;

import puyodead1.mlp.events.PlayerSpawnPositionEvent;
import puyodead1.mlp.events.SpawnPlayerEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPlayNetworkHandler.class, priority = 100)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onPlayerSpawnPosition", at = @At("TAIL"))
    private void mlp$playerSpawnPositionEvent(PlayerSpawnPositionS2CPacket packet, CallbackInfo cinfo) {
        MeteorClient.EVENT_BUS.post(new PlayerSpawnPositionEvent(packet.getPos()));
    }

    @Inject(method = "createEntity", at = @At(value = "NEW", target = "(Lnet/minecraft/client/world/ClientWorld;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/network/OtherClientPlayerEntity;"))
    private void mlp$playerSpawnEvent(EntitySpawnS2CPacket packet, CallbackInfoReturnable<Entity> cir) {
        MeteorClient.EVENT_BUS.post(new SpawnPlayerEvent(packet.getUuid(), new BlockPos((int) packet.getX(), (int) packet.getY(), (int) packet.getZ())));
    }
}
