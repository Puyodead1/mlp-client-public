package puyodead1.mlp.client.ui;

import meteordevelopment.meteorclient.mixin.DisconnectedScreenMixin;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import puyodead1.mlp.MLPMod;

public class DisconnectScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(DisconnectScreen.class);
    private static final Text GRIEFED_LABEL = Text.of("Mark Griefed");
    private static final Text PROTECTED_LABEL = Text.of("Mark Protected");
    private static final Text NOT_GRIEFED_LABEL = Text.of("Disconnect");
    private static final Text RETURN_TO_GAME_TEXT = Text.translatable("menu.returnToGame");

    public DisconnectScreen() {
        super(Text.of("MLPDisconnect"));
    }

    @Override
    protected void init() {
        this.initWidgets();
    }


    private void initWidgets() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(4).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

//        adder.add(ButtonWidget.builder(GRIEFED_LABEL, (button) -> this.markServer(MLPService.ServerUpdateType.GRIEFED)).width(100).build());
//        adder.add(ButtonWidget.builder(PROTECTED_LABEL, (button) -> this.markServer(MLPService.ServerUpdateType.PROTECTED)).width(100).build());
        adder.add(ButtonWidget.builder(NOT_GRIEFED_LABEL, (button) -> this.disconnect()).width(204).build(), 2, gridWidget.copyPositioner().marginTop(50));
        adder.add(ButtonWidget.builder(RETURN_TO_GAME_TEXT, (button) -> {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
        }).width(204).build(), 2, gridWidget.copyPositioner().marginTop(5));

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5F, 0.25F);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

//    private void markServer(MLPService.ServerUpdateType updateType) {
//        MLPService.UpdateServerRequest req = new MLPService.UpdateServerRequest();
//        switch (updateType) {
//            case GRIEFED:
//                req.isGriefed = true;
//                break;
//            case PROTECTED:
//                req.isProtected = true;
//                break;
//            case WHITELISTED:
//                req.isWhitelisted = true;
//                break;
//            case MODDED:
//                req.isModded = true;
//                break;
//        }
//
//        // ensure at least one is true
//        if (req.isGriefed == null && req.isWhitelisted == null && req.isModded == null && req.isProtected == null)
//            throw new RuntimeException("DisconnectScreen(markServer): At least one type to update should be specified...");
//
//        ServerInfo info = MinecraftClient.getInstance().getCurrentServerEntry();
//        if (info != null) {
//            req.server = info.address;
//            MLPMod.mlpService.update(req, server -> {
//                TitleScreen titleScreen = new TitleScreen();
//                LOGGER.info("Updated server: {}", server.serverAddress);
//                MLPMultiplayerScreen multiplayerScreen = MLPMod.getOrCreateMultiplayerScreen(titleScreen);
//                LOGGER.debug("Multiplayer screen: {multiplayerScreen}");
//                if (multiplayerScreen != null) {
//                    multiplayerScreen.getServerList().remove(info);
//                }
//            });
//        }
//
//        this.disconnect();
//    }

    private void disconnect() {
        // should never be SP here
        ServerInfo serverInfo = this.client.getCurrentServerEntry();
        if (client.world != null)
            client.world.disconnect(Text.of(""));

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
    }
}
