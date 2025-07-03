package puyodead1.mlp.client.ui.serverlist;

import com.mojang.logging.LogUtils;
import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import com.viaversion.viafabricplus.screen.impl.ProtocolSelectionScreen;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import com.viaversion.vialoader.util.ProtocolVersionList;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import puyodead1.mlp.MLPMod;
import puyodead1.mlp.MLPService;
import puyodead1.mlp.client.ui.NotesScreen;
import puyodead1.mlp.client.ui.ServerInfoScreen;
import puyodead1.mlp.modules.StreamerMode;
import puyodead1.mlp.utils.MLPSystem;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class MLPMultiplayerScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MultiplayerServerListPinger serverListPinger = new MultiplayerServerListPinger();
    private final Screen parent;
    private final MLPService mlpService;
    protected MLPMultiplayerServerListWidget serverListWidget;
    private MLPServerList serverList;
//    private List<MLPServerInfo> savedServerList;
    private ButtonWidget buttonJoin;
    private ButtonWidget buttonDelete;
    private ButtonWidget buttonNotes;
    private ButtonWidget buttonModded;
    private ButtonWidget buttonWhitelisted;
    private ButtonWidget buttonGriefed;
    private ButtonWidget buttonBookmark;
    private ButtonWidget buttonCopy;
    private ButtonWidget buttonInfo;
    private ServerInfo selectedEntry;
    private boolean initialized;
    private List<MLPService.Server> servers;

    public MLPMultiplayerScreen(Screen parent, MLPService mlpService) {
        super(Text.of("MCSDC"));
        this.parent = parent;
        this.mlpService = mlpService;
    }

    @Override
    protected void init() {
        if (!this.initialized)
        {
            this.serverList = new MLPServerList(this.client);
//            this.savedServerList = new ArrayList<>();

            this.serverListWidget = new MLPMultiplayerServerListWidget(this, this.client, this.width, this.height - 64 - 32, 32, 36);
            this.serverListWidget.setServerList(this.serverList);
//            this.serverListWidget.setSavedServers(this.savedServerList);

            this.refreshList();

            this.initialized = true;
        }

        this.addDrawableChild(this.serverListWidget);

        this.buttonInfo = this.addDrawableChild(ButtonWidget.builder(Text.of("Info"), (button) -> this.showServerInfo()).width(80).build());

        this.buttonJoin = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.select"), (button) -> {
            this.connect();
        }).width(80).build());

        this.buttonDelete = this.addDrawableChild(ButtonWidget.builder(Text.of("Hide"), (button) -> this.hideEntry()).width(80).build());
        this.buttonBookmark = this.addDrawableChild(ButtonWidget.builder(Text.of("Bookmark"), (button) -> this.bookmarkEntry()).width(80).build());

        ButtonWidget buttonSearch = this.addDrawableChild(ButtonWidget.builder(Text.of("Update Search"), (button) -> {
            if (this.client == null) return;
            this.client.setScreen(new SearchParametersScreen(this, this.mlpService));
        }).width(80).build());

        ButtonWidget buttonRefresh = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.refresh"), (button) -> {
            if (!this.mlpService.loading.get()) {
                this.refreshList();
            }
        }).width(80).build());

        this.buttonNotes = this.addDrawableChild(ButtonWidget.builder(Text.of("Notes"), (button) -> {
            MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
            if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
                MLPService.Server server = this.servers.stream().filter(x -> x.address.equals(((MLPMultiplayerServerListWidget.ServerEntry) entry).getServer().address)).findFirst().orElse(null);
                if (server != null) {
                    this.client.setScreen(new NotesScreen(server, mlpService));
                } else {
                    LOGGER.warn("notes: server null");
                }
            }
        }).width(80).build());

        this.buttonWhitelisted = this.addDrawableChild(ButtonWidget.builder(Text.of("Whitelisted"), (button) -> {
            MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
            if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
                MLPService.UpdateServerRequest req = new MLPService.UpdateServerRequest();
                req.update.flags.setWhitelist(SearchParametersScreen.Flag.YES);
                this.updateServer(req, ((MLPMultiplayerServerListWidget.ServerEntry) entry));
                this.removeEntry();
            }
        }).width(80).build());

        this.buttonModded = this.addDrawableChild(ButtonWidget.builder(Text.of("Modded"), (button) -> {
            MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
            if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
                MLPService.UpdateServerRequest req = new MLPService.UpdateServerRequest();
                req.update.flags.setModded(SearchParametersScreen.Flag.YES);
                this.updateServer(req, ((MLPMultiplayerServerListWidget.ServerEntry) entry));
                this.removeEntry();
            }
        }).width(80).build());

        this.buttonGriefed = this.addDrawableChild(ButtonWidget.builder(Text.of("Griefed"), (button) -> {
            MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
            if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
                MLPService.UpdateServerRequest req = new MLPService.UpdateServerRequest();
                req.update.flags.setGriefed(SearchParametersScreen.Flag.YES);
                this.updateServer(req, ((MLPMultiplayerServerListWidget.ServerEntry) entry));
                this.removeEntry();
            }
        }).width(80).build());

        this.buttonCopy = this.addDrawableChild(ButtonWidget.builder(Text.of("Copy"), (button) -> this.copyEntry()).width(80).build());

        ButtonWidget buttonBack = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
            MLPMod.setMultiplayerScreen(null);
            this.close();
        }).width(80).build());

        // width = button count * button width + (2 * (button count - 1))
        DirectionalLayoutWidget directionalLayoutWidget = DirectionalLayoutWidget.vertical();
        AxisGridWidget axisGridWidget = directionalLayoutWidget.add(new AxisGridWidget(490, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        axisGridWidget.add(this.buttonInfo);
        axisGridWidget.add(this.buttonJoin);
        axisGridWidget.add(this.buttonDelete);
        axisGridWidget.add(this.buttonBookmark);
        axisGridWidget.add(buttonSearch);
        axisGridWidget.add(buttonRefresh);
        directionalLayoutWidget.add(EmptyWidget.ofHeight(4));

        // width = button count * button width + (2 * (button count - 1))
        AxisGridWidget axisGridWidget2 = directionalLayoutWidget.add(new AxisGridWidget(490, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        axisGridWidget2.add(this.buttonNotes);
        axisGridWidget2.add(this.buttonWhitelisted);
        axisGridWidget2.add(this.buttonModded);
        axisGridWidget2.add(this.buttonGriefed);
        axisGridWidget2.add(this.buttonCopy);
        axisGridWidget2.add(buttonBack);

        directionalLayoutWidget.refreshPositions();
        SimplePositioningWidget.setPos(directionalLayoutWidget, 0, this.height - 64, this.width, 64);

        // adds viafabricplus to our custom server list if its installed
        if (FabricLoader.getInstance().isModLoaded("viafabricplus")) {
            final int buttonPosition = GeneralSettings.INSTANCE.multiplayerScreenButtonOrientation.getIndex();
            if (buttonPosition == 0) { // Off
                return;
            }
            ButtonWidget.Builder builder = ButtonWidget.builder(Text.literal("ViaFabricPlus"), button -> ProtocolSelectionScreen.INSTANCE.open(this)).size(98, 20);

            // Set the button's position according to the configured orientation and add the button to the screen
            this.addDrawableChild(GeneralSettings.withOrientation(builder, buttonPosition, width, height).build());
        }

        this.updateButtonActivationStates();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    public void tick() {
        super.tick();
        this.serverListPinger.tick();
    }

    public void removed() {
        this.serverListPinger.cancel();
        this.serverListWidget.onRemoved();
    }

    public void refreshList() {
        if (this.serverListWidget != null) {
            this.serverListWidget.setServerList(this.serverList);
            this.mlpService.find(this::setServers);
        }
    }

    private void setServers(List<MLPService.Server> servers) {
        this.serverList = new MLPServerList(this.client);
//        this.savedServerList = new ArrayList<>();
        this.serverList.loadFile();
        this.mapServers(servers);
        this.serverListWidget.setSelected(null);
        this.serverListWidget.setServerList(this.serverList);
//        this.serverListWidget.setSavedServers(this.savedServerList);
    }

    private void removeEntry() {
        MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
            this.serverList.remove(((MLPMultiplayerServerListWidget.ServerEntry) entry).getServer());
            this.serverList.saveFile();
            this.serverListWidget.setSelected(null);
            this.serverListWidget.setServerList(this.serverList);
//            this.serverListWidget.setSavedServers(this.savedServerList);
        }

        this.client.setScreen(this);
    }

    private void hideEntry() {
        MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
            ServerInfo serverInfo = ((MLPMultiplayerServerListWidget.ServerEntry) entry).getServer();
            this.serverList.hide(serverInfo);
            this.serverList.saveFile();
            this.serverListWidget.setSelected(null);
            this.serverListWidget.setServerList(this.serverList);
//            this.serverListWidget.setSavedServers(this.savedServerList);
        }

        this.client.setScreen(this);
    }

    private void bookmarkEntry() {
        MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();

        if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
            ServerInfo serverInfo = ((MLPMultiplayerServerListWidget.ServerEntry) entry).getServer();
            ServerList serverList = new ServerList(MeteorClient.mc);
            serverList.loadFile();
            serverList.add(serverInfo, false);
            serverList.saveFile();

            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of("Bookmarked"), Text.of("Server has been saved to multiplayer list.")));
            this.buttonBookmark.active = false;
        }
    }

    private void copyEntry() {
        MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();

        if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
            ServerInfo serverInfo = ((MLPMultiplayerServerListWidget.ServerEntry) entry).getServer();
            MinecraftClient.getInstance().keyboard.setClipboard(serverInfo.address);

            MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of("Copied"), Text.of("Address copied to clipboard.")));
            this.buttonCopy.active = false;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_F5) {
            this.refreshList();
            return true;
        } else if (this.serverListWidget.getSelectedOrNull() != null) {
            if (KeyCodes.isToggle(keyCode)) {
                this.connect();
                return true;
            } else {
                return this.serverListWidget.keyPressed(keyCode, scanCode, modifiers);
            }
        } else {
            return false;
        }
    }

    private String getLoadingText() {
        long dotsCount = Instant.now().getEpochSecond() % 5L;
        StringBuilder text = new StringBuilder("Loading");
        int i = 0;
        while ((long) i <= dotsCount) {
            text.append(".");
            i++;
        }
        return text.toString();
    }

    private void updateServer(MLPService.UpdateServerRequest req, MLPMultiplayerServerListWidget.ServerEntry entry) {
        ForkJoinPool.commonPool().submit(() -> {
            req.update.setAddress(entry.getServer().address);
            this.mlpService.update(req, null);
        });
    }

    private void showServerInfo() {
        final MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();

        if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
            final MLPMultiplayerServerListWidget.ServerEntry serverEntry = (MLPMultiplayerServerListWidget.ServerEntry) entry;
            final ServerInfo serverInfo = serverEntry.getServer();

            MLPService.Server server = this.servers.stream().filter(x -> Objects.equals(x.address, serverInfo.address)).findFirst().orElse(null);
            if (server != null) {
                this.client.setScreen(new ServerInfoScreen(server, serverInfo));
            }
            else {
                this.buttonInfo.active = false;
            }
        }
    }

    private void mapServers(List<MLPService.Server> servers) {
        this.servers = servers;

//        final List<MLPService.Server> savedServers = servers.stream().filter(x -> x.status.save_for_later).toList();
//        final List<MLPService.Server> otherServers = servers.stream().filter(x -> !x.status.save_for_later).toList();

//        this.addSavedServers(savedServers, MLPService.Server::displayServerAddress);
        this.addServers(servers, MLPService.Server::displayServerAddress);
    }

    private void addServers(List<MLPService.Server> servers, Function<MLPService.Server, String> name) {
        servers.stream().map(found -> new MLPServerInfo(name.apply(found), found)).forEach(serverInfo -> {
            this.serverList.add(serverInfo, false);
        });
        this.serverListWidget.setServerList(this.serverList);
    }

//    private void addSavedServers(List<MLPService.Server> servers, Function<MLPService.Server, String> name) {
//        servers.stream().map(found -> new MLPServerInfo(name.apply(found), found)).forEach((serverInfo) -> this.savedServerList.add(serverInfo));
//        this.serverListWidget.setSavedServers(this.savedServerList);
//    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, Colors.WHITE);

        if (this.mlpService.loading.get() && this.serverListWidget.getServers().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, this.getLoadingText(), this.width / 2, 50, Colors.WHITE);
        }

        if (!StreamerMode.isHideAccountEnabled()) {
            String username = MeteorClient.mc.getSession().getUsername();
            context.drawTextWithShadow(this.textRenderer, Text.of("MCSDC Username: " + MLPSystem.get().username), 10, 10, Colors.WHITE);
            context.drawTextWithShadow(this.textRenderer, Text.of("Player Username: " + username), 10, 20, Colors.WHITE);
        }
    }

    public void connect() {
        MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
            this.connect(((MLPMultiplayerServerListWidget.ServerEntry) entry).getServer());
        }
    }

    private void connect(ServerInfo entry) {
//        MLPMod.setIsFromSSScreen(true);
        if (FabricLoader.getInstance().isModLoaded("viafabricplus")) {
            final Optional<ProtocolVersion> protocolVersion = ProtocolVersionList.getProtocolsNewToOld().stream().filter(x -> {
                if (x.isSnapshot()) {
                    return (x.getSnapshotVersion() == entry.protocolVersion) || (x.getFullSnapshotVersion() == entry.protocolVersion);
                } else {
                    return x.getVersion() == entry.protocolVersion;
                }
            }).findFirst();
            protocolVersion.ifPresent(version -> {
                ((IServerInfo) entry).viaFabricPlus$forceVersion(version);
                LOGGER.info("Setting version for server {} to {} ({})", entry.address, version.getName(), version.getVersion());
            });
        }
        ConnectScreen.connect(this, this.client, ServerAddress.parse(entry.address), entry, false, null);
    }

    public void select(MLPMultiplayerServerListWidget.Entry entry) {
        this.serverListWidget.setSelected(entry);
        this.updateButtonActivationStates();
    }

    protected void updateButtonActivationStates() {
        this.buttonJoin.active = false;
        this.buttonDelete.active = false;
        this.buttonBookmark.active = false;
        this.buttonWhitelisted.active = false;
        this.buttonModded.active = false;
        this.buttonGriefed.active = false;
        this.buttonCopy.active = false;
        this.buttonInfo.active = false;
        MLPMultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        // don't activate on divider select
        if (entry != null && !(entry instanceof MLPMultiplayerServerListWidget.DividerEntry) && !(entry instanceof MLPMultiplayerServerListWidget.SavedServersDividerEntry)) {
            this.buttonJoin.active = true;
            // don't activate for active server entries (they shouldn't be removable or anything)
            if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry && !(entry instanceof MLPMultiplayerServerListWidget.SavedServerEntry)) {
                this.buttonDelete.active = true;
                this.buttonBookmark.active = true;
                this.buttonWhitelisted.active = true;
                this.buttonModded.active = true;
                this.buttonGriefed.active = true;
                this.buttonCopy.active = true;
                this.buttonInfo.active = true;
            }
        }

    }

    public MultiplayerServerListPinger getServerListPinger() {
        return this.serverListPinger;
    }

    public MLPServerList getServerList() {
        return this.serverList;
    }
}
