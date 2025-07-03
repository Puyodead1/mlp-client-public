package puyodead1.mlp.modules;

import meteordevelopment.meteorclient.events.entity.player.InteractItemEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import puyodead1.mlp.MLPMod;

import java.util.List;

public class Gun extends Module {
    public Gun() {
        super(MLPAddOn.MLP_CATEGORY, "Gun", "my gun go");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ItemStack hand = this.mc.player.getStackInHand(Hand.MAIN_HAND);
        if (hand.getItem() == Items.BOW) {
            hand.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(), List.of(), List.of(), List.of(123)));
        }
    }

    @EventHandler
    private void onInteractItem(InteractItemEvent event) {
        if (this.mc.world != null && this.mc.player.getStackInHand(Hand.MAIN_HAND).getItem() == Items.BOW) {
            this.mc.world.playSound(this.mc.player, this.mc.player.getBlockPos(), MLPMod.cockSoundEvent, SoundCategory.VOICE, 1.0f, 1.0f);
        }
    }

    public boolean shouldShoot() {
        return this.isActive() && this.mc.player.getMainHandStack().getItem() == Items.BOW;
    }

    public void shoot() {
        this.mc.world.playSound(this.mc.player, this.mc.player.getBlockPos(), MLPMod.shotgunSoundEvent, SoundCategory.VOICE, 1.0f, 1.0f);
    }
}
