package puyodead1.mlp.mixins;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import puyodead1.mlp.utils.MLPSystem;

@Mixin(value = Systems.class, remap = false)
public abstract class SystemsMixin {
    @Shadow
    private static System<?> add(System<?> system) {
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private static void onInit(CallbackInfo ci) {
        System<?> mlpSystem = add(new MLPSystem());
        mlpSystem.init();
        mlpSystem.load(MeteorClient.FOLDER);
    }
}
