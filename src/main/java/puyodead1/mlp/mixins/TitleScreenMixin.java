package puyodead1.mlp.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import puyodead1.mlp.MLPMod;
import puyodead1.mlp.client.ui.serverlist.MLPMultiplayerScreen;
import puyodead1.mlp.utils.MLPSystem;

@Environment(EnvType.CLIENT)
@Mixin(value = TitleScreen.class, priority = 1001)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger(TitleScreenMixin.class);

    @Shadow
    @Nullable
    private SplashTextRenderer splashText;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void mlp$modifySplashText(CallbackInfo ci) {
        if (this.splashText == null) {
            this.splashText = new SplashTextRenderer("uhh, hello?");
        }
    }

    @Inject(method = "addNormalWidgets", at = @At("TAIL"))
    private void mlp$addServerSeekerButton(int y, int spacingY, CallbackInfoReturnable<Integer> cir) {
        this.addDrawableChild(ButtonWidget.builder(Text.of("MCSDC"), button -> {
            MLPMultiplayerScreen multiplayerScreen = MLPMod.getOrCreateMultiplayerScreen(this);
            this.client.setScreen(multiplayerScreen);
            MLPMod.setIsFromSSScreen(true);
        }).width(200).dimensions(this.width / 2 - 100, y + spacingY * 3, 200, 20).build());
    }
}
