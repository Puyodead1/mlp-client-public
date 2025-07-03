package puyodead1.mlp.mixins;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.QuickPlay;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import puyodead1.mlp.client.ui.LoginScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// Sets a custom window title and shows login screen on init
@Mixin(value = MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    protected abstract boolean createInitScreens(List<Function<Runnable, Screen>> list);

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @ModifyConstant(method = "getWindowTitle", constant = @Constant(stringValue = "Minecraft"))
    String modifyMinecraftConst(String constant) {
        return "MLP Client | " + constant;
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
}
