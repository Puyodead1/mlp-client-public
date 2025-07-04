package puyodead1.mlp.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import puyodead1.mlp.MLPMod;
import puyodead1.mlp.MLPService;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameMenuScreen.class, priority = 100)
public abstract class GameMenuScreenMixin extends Screen {
    public GameMenuScreenMixin(Text text) {
        super(text);
    }

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;refreshPositions()V"))
    private void mlp$addBookmark(CallbackInfo ci, @Local GridWidget.Adder adder) {
        if(!MLPMod.isFromSSScreen()) return;
        ServerInfo entry = MinecraftClient.getInstance().getCurrentServerEntry();
        if (entry == null) return;

        String serverAddress = MLPService.Server.displayForServerAddress(entry.address);

        adder.add(ButtonWidget.builder(Text.of("Bookmark " + serverAddress), btn -> {
            ServerList serverList = new ServerList(MeteorClient.mc);
            serverList.loadFile();
            serverList.add(entry, false);
            serverList.saveFile();
            MinecraftClient.getInstance().keyboard.setClipboard(entry.address);
            btn.active = false;
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, Text.of("Bookmarked"), Text.of("Server has been saved.")));
        }).width(204).build(), 2);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ServerInfo serverEntry;
        if (Screen.isCopy(keyCode) && (serverEntry = MeteorClient.mc.getCurrentServerEntry()) != null) {
            MeteorClient.mc.keyboard.setClipboard(serverEntry.address);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Inject(method = "disconnect", at = @At("HEAD"), cancellable = true)
    private static void mlp$changeMultiplayerScreen(MinecraftClient client, Text disconnectReason, CallbackInfo ci) {
        if (!client.isInSingleplayer()) {
//            client.setScreen(new DisconnectScreen());
            ServerInfo serverInfo = client.getCurrentServerEntry();
            if (client.world != null) {
                client.world.disconnect(disconnectReason);
            }

            client.disconnectWithProgressScreen();

            TitleScreen titleScreen = new TitleScreen();
            if (serverInfo != null && serverInfo.isRealm()) {
                client.setScreen(new RealmsMainScreen(titleScreen));
            } else {
                if(MLPMod.isFromSSScreen()) {
                    client.setScreen(MLPMod.getOrCreateMultiplayerScreen(titleScreen));
                } else {
                    client.setScreen(new MultiplayerScreen(titleScreen));
                }
            }
            ci.cancel();
        }
    }
}
