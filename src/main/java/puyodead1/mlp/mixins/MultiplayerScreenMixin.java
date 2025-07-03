package puyodead1.mlp.mixins;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MultiplayerScreen.class, priority = 1)
public abstract class MultiplayerScreenMixin extends Screen {

    @Shadow
    public ButtonWidget buttonDelete;

    @Shadow
    public MultiplayerServerListWidget serverListWidget;

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void removeEntry(boolean confirmAction);

    @Inject(method = "init", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 6))
    protected void init(CallbackInfo ci) {
        this.buttonDelete = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.delete"), button -> {
            MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
            if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
                String string = ((MultiplayerServerListWidget.ServerEntry) entry).getServer().name;
                if (string != null) {
                    this.removeEntry(true);
                }
            }
        }).width(74).build());
        System.out.println("INJECT DELETE BUTTON");
    }
}
