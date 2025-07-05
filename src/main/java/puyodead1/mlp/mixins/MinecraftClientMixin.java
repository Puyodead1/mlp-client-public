package puyodead1.mlp.mixins;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.QuickPlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import puyodead1.mlp.client.ui.LoginScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// Shows login screen on init
@Mixin(value = MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    protected abstract boolean createInitScreens(List<Function<Runnable, Screen>> list);

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Shadow
    @Final
    private Window window;

    @ModifyConstant(method = "getWindowTitle", constant = @Constant(stringValue = "Minecraft"))
    String modifyMinecraftConst(String constant) {
        return "MLPI | " + constant;
    }

    /**
     * @author Puyodead1
     * @reason return Login screen on init
     */
    @Overwrite
    private Runnable onInitFinished(@Nullable MinecraftClient.LoadingContext loadingContext) {
        ArrayList<Function<Runnable, Screen>> list = new ArrayList<>();
        this.createInitScreens(list);
        Runnable runnable = () -> {
            if (loadingContext != null && loadingContext.quickPlayData().isEnabled()) {
                QuickPlay.startQuickPlay(MinecraftClient.getInstance(), loadingContext.quickPlayData().variant(), loadingContext.realmsClient());
            } else {
                this.setScreen(new LoginScreen());
            }
        };
        for (Function<Runnable, Screen> function : Lists.reverse(list)) {
            Screen screen = function.apply(runnable);
            runnable = () -> this.setScreen(screen);
        }
        return runnable;
    }

    /**
     * Maximize the window
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void mlp$maximizeWindow(CallbackInfo ci) {
        GLFW.glfwMaximizeWindow(this.window.getHandle());
    }
}
