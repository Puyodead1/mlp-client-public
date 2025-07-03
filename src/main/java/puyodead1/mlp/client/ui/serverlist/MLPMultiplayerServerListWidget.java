package puyodead1.mlp.client.ui.serverlist;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;
import puyodead1.mlp.modules.StreamerMode;
import puyodead1.mlp.utils.MCVersionUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Environment(EnvType.CLIENT)
public class MLPMultiplayerServerListWidget extends AlwaysSelectedEntryListWidget<MLPMultiplayerServerListWidget.Entry> {
    static final Identifier UNREACHABLE_TEXTURE = Identifier.of("server_list/unreachable");
    static final Identifier PING_1_TEXTURE = Identifier.of("server_list/ping_1");
    static final Identifier PING_2_TEXTURE = Identifier.of("server_list/ping_2");
    static final Identifier PING_3_TEXTURE = Identifier.of("server_list/ping_3");
    static final Identifier PING_4_TEXTURE = Identifier.of("server_list/ping_4");
    static final Identifier PING_5_TEXTURE = Identifier.of("server_list/ping_5");
    static final Identifier PINGING_1_TEXTURE = Identifier.of("server_list/pinging_1");
    static final Identifier PINGING_2_TEXTURE = Identifier.of("server_list/pinging_2");
    static final Identifier PINGING_3_TEXTURE = Identifier.of("server_list/pinging_3");
    static final Identifier PINGING_4_TEXTURE = Identifier.of("server_list/pinging_4");
    static final Identifier PINGING_5_TEXTURE = Identifier.of("server_list/pinging_5");
    static final Identifier JOIN_HIGHLIGHTED_TEXTURE = Identifier.of("server_list/join_highlighted");
    static final Identifier JOIN_TEXTURE = Identifier.of("server_list/join");
    static final Logger LOGGER = LogUtils.getLogger();
    static final ThreadPoolExecutor SERVER_PINGER_THREAD_POOL;
    private static final Identifier UNKNOWN_SERVER_TEXTURE;
    static final Text CANNOT_RESOLVE_TEXT;
    static final Text CANNOT_CONNECT_TEXT;
    static final Text NO_CONNECTION_TEXT;
    static final Text PINGING_TEXT;
    static final Text ONLINE_TEXT;
    private final MLPMultiplayerScreen screen;
    private final List<MLPMultiplayerServerListWidget.ServerEntry> servers = Lists.newArrayList();
//    private final Entry savedServersDividerEntry = new SavedServersDividerEntry();
//    private final Entry dividerEntry = new DividerEntry();
//    private final List<MLPMultiplayerServerListWidget.ServerEntry> savedServers = Lists.newArrayList();

    public MLPMultiplayerServerListWidget(MLPMultiplayerScreen screen, MinecraftClient client, int width, int height, int top, int bottom) {
        super(client, width, height, top, bottom);
        this.screen = screen;
    }

    public List<MLPMultiplayerServerListWidget.ServerEntry> getServers() {
        return this.servers;
    }

    private void updateEntries() {
        this.clearEntries();
//
//        if (!this.savedServers.isEmpty()) {
//            this.addEntry(savedServersDividerEntry);
//            this.savedServers.forEach(this::addEntry);
//        }

        if (!this.servers.isEmpty()) {
            this.servers.forEach(this::addEntry);
        }
    }

    public void setSelected(@Nullable MLPMultiplayerServerListWidget.Entry entry) {
        super.setSelected(entry);
        this.screen.updateButtonActivationStates();
    }

    public void removeSelectedServerEntry() {
        MLPMultiplayerServerListWidget.Entry entry = this.getSelectedOrNull();
        if (entry instanceof MLPMultiplayerServerListWidget.ServerEntry) {
            ServerInfo serverInfo = ((MLPMultiplayerServerListWidget.ServerEntry) entry).getServer();
            this.screen.getServerList().remove(serverInfo);
            this.screen.getServerList().add(serverInfo, true);
            this.updateEntries();
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        MLPMultiplayerServerListWidget.Entry entry = this.getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void setServerList(MLPServerList servers) {
        this.servers.clear();
        this.screen.getServerListPinger().cancel();

        for (int i = 0; i < servers.size(); ++i) {
            this.servers.add(new MLPMultiplayerServerListWidget.ServerEntry(this.screen, servers.get(i)));
        }

        this.updateEntries();
    }

//    public void setSavedServers(List<MLPServerInfo> servers) {
//        this.savedServers.clear();
//        this.screen.getServerListPinger().cancel();
//
//        for (MLPServerInfo server : servers) {
//            this.savedServers.add(new SavedServerEntry(this.screen, server));
//        }
//
//        this.updateEntries();
//    }

    public int getRowWidth() {
        return 305;
    }

    public void onRemoved() {
    }

    static {
        SERVER_PINGER_THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER)).build());
        UNKNOWN_SERVER_TEXTURE = Identifier.of("textures/misc/unknown_server.png");
        CANNOT_RESOLVE_TEXT = Text.translatable("multiplayer.status.cannot_resolve").withColor(-65536);
        CANNOT_CONNECT_TEXT = Text.translatable("multiplayer.status.cannot_connect").withColor(-65536);
        NO_CONNECTION_TEXT = Text.translatable("multiplayer.status.no_connection");
        PINGING_TEXT = Text.translatable("multiplayer.status.pinging");
        ONLINE_TEXT = Text.translatable("multiplayer.status.online");
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> implements AutoCloseable {
        public Entry() {
        }

        public void close() {
        }
    }

    @Environment(EnvType.CLIENT)
    public class ServerEntry extends MLPMultiplayerServerListWidget.Entry {
        private final MLPMultiplayerScreen screen;
        private final MinecraftClient client;
        private final ServerInfo server;
        private final WorldIcon icon;
        private byte @Nullable [] favicon;
        private long time;
        @Nullable
        private List<Text> playerListSummary;
        @Nullable
        private Identifier statusIconTexture;
        @Nullable
        private Text statusTooltipText;

        protected ServerEntry(final MLPMultiplayerScreen screen, final ServerInfo server) {
            this.screen = screen;
            this.server = server;
            this.client = MinecraftClient.getInstance();
            this.icon = WorldIcon.forServer(this.client.getTextureManager(), server.address);
            this.update();
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            byte[] bs;
            int i;

            if (this.server.getStatus() == ServerInfo.Status.INITIAL) {
                this.server.setStatus(ServerInfo.Status.PINGING);
                this.server.ping = -2L;
                this.server.label = ScreenTexts.EMPTY;
                this.server.playerCountLabel = ScreenTexts.EMPTY;
                MLPMultiplayerServerListWidget.SERVER_PINGER_THREAD_POOL.submit(() -> {
                    try {
                        this.screen.getServerListPinger().add(this.server, () -> this.client.execute(this::saveFile), () -> {
                            this.server.setStatus(this.server.protocolVersion == SharedConstants.getGameVersion().protocolVersion() ? ServerInfo.Status.SUCCESSFUL : ServerInfo.Status.INCOMPATIBLE);
                            this.client.execute(this::update);
                        });
                    } catch (UnknownHostException unknownHostException) {
                        this.server.setStatus(ServerInfo.Status.UNREACHABLE);
                        this.server.label = CANNOT_RESOLVE_TEXT;
                        this.client.execute(this::update);
                    } catch (Exception exception) {
                        this.server.setStatus(ServerInfo.Status.UNREACHABLE);
                        this.server.label = CANNOT_CONNECT_TEXT;
                        this.client.execute(this::update);
                    }
                });
            }

            context.drawText(this.client.textRenderer, this.server.name, x + 32 + 3, y + 1, Colors.WHITE, false);
            Text labelText = StreamerMode.isHideServerInfoEnabled() ? Text.of("No peeking ;)") : MoreObjects.firstNonNull(this.server.label, Text.empty());
            List<OrderedText> list = this.client.textRenderer.wrapLines(labelText, entryWidth - 32 - 2);

            for (i = 0; i < Math.min(list.size(), 2); ++i) {
                context.drawText(this.client.textRenderer, list.get(i), x + 32 + 3, y + 12 + this.client.textRenderer.fontHeight * i, Colors.GRAY, false);
            }

            this.draw(context, x, y, this.icon.getTextureId());
            if (this.server.getStatus() == ServerInfo.Status.PINGING) {
                i = (int)(Util.getMeasuringTimeMs() / 100L + (long)(index * 2) & 7L);
                if (i > 4) {
                    i = 8 - i;
                }

                this.statusIconTexture = switch (i) {
                    case 1 -> MLPMultiplayerServerListWidget.PINGING_2_TEXTURE;
                    case 2 -> MLPMultiplayerServerListWidget.PINGING_3_TEXTURE;
                    case 3 -> MLPMultiplayerServerListWidget.PINGING_4_TEXTURE;
                    case 4 -> MLPMultiplayerServerListWidget.PINGING_5_TEXTURE;
                    default -> MLPMultiplayerServerListWidget.PINGING_1_TEXTURE;
                };
            }
            i = x + entryWidth - 10 - 5;

            if (this.statusIconTexture != null) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.statusIconTexture, i, y, 10, 8);
            }

            if (!Arrays.equals(bs = this.server.getFavicon(), this.favicon)) {
                if (this.uploadFavicon(bs)) {
                    this.favicon = bs;
                } else {
                    this.server.setFavicon(null);
                    this.saveFile();
                }
            }

            this.draw(context, x, y, this.icon.getTextureId());

            Text text;
            if (StreamerMode.isHideServerInfoEnabled() && this.server.players != null) {
                int players = this.server.players.online() > 0 ? this.server.players.online() + StreamerMode.addFakePlayers() : this.server.players.max() % (StreamerMode.addFakePlayers() + 1);
                text = Text.of(players + " online");
            } else {
                text = this.server.playerCountLabel;
            }

            int j = this.client.textRenderer.getWidth(text);
            int k = i - j - 5;
            context.drawText(this.client.textRenderer, text, k, y + 1, Colors.GRAY, false);
            if (this.statusTooltipText != null && mouseX >= i && mouseX <= i + 10 && mouseY >= y && mouseY <= y + 8) {
                context.drawTooltip(this.statusTooltipText, mouseX, mouseY);
            } else if (this.playerListSummary != null && mouseX >= k && mouseX <= k + j && mouseY >= y && mouseY <= y - 1 + this.client.textRenderer.fontHeight) {
                context.drawTooltip(Lists.transform(this.playerListSummary, Text::asOrderedText), mouseX, mouseY);
            }

            if(this.server.getStatus() == ServerInfo.Status.INCOMPATIBLE) {
                // draw the version next to the player count
                String version = MCVersionUtil.protocolToVersion(this.server.protocolVersion);
                if(version == null) version = "???";
                Text versionText = Text.of(version).copy().formatted(Formatting.YELLOW);
                int w = this.client.textRenderer.getWidth(versionText);
                context.drawText(this.client.textRenderer, versionText,  x + entryWidth - j - 15 - 8 - w, y + 1, Colors.GRAY, false);
            }

            if (this.client.options.getTouchscreen().getValue() || hovered) {
                context.fill(x, y, x + 32, y + 32, -1601138544);
                int l = mouseX - x;
                if (this.canConnect()) {
                    if (l < 32 && l > 16) {
                        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, JOIN_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                    } else {
                        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, JOIN_TEXTURE, x, y, 32, 32);
                    }
                }
            }
        }

        private void update() {
            this.playerListSummary = null;
            switch (this.server.getStatus()) {
                case INITIAL:
                case PINGING: {
                    this.statusIconTexture = PING_1_TEXTURE;
                    this.statusTooltipText = PINGING_TEXT;
                    break;
                }
//                case INCOMPATIBLE: {
//                    this.statusIconTexture = INCOMPATIBLE_TEXTURE;
//                    this.statusTooltipText = INCOMPATIBLE_TEXT;
//                    this.playerListSummary = this.server.playerListSummary;
//                    break;
//                }
                case UNREACHABLE: {
                    this.statusIconTexture = UNREACHABLE_TEXTURE;
                    this.statusTooltipText = NO_CONNECTION_TEXT;
                    break;
                }
                case INCOMPATIBLE:
                case SUCCESSFUL: {
                    this.statusIconTexture = this.server.ping < 150L ? PING_5_TEXTURE : (this.server.ping < 300L ? PING_4_TEXTURE : (this.server.ping < 600L ? PING_3_TEXTURE : (this.server.ping < 1000L ? PING_2_TEXTURE : PING_1_TEXTURE)));
                    this.statusTooltipText = Text.translatable("multiplayer.status.ping", this.server.ping);
                    this.playerListSummary = this.server.playerListSummary;
                }
            }
        }

        public void saveFile() {
            this.screen.getServerList().saveFile();
        }

        protected void draw(DrawContext context, int x, int y, Identifier textureId) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, textureId, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        }

        private boolean canConnect() {
            return true;
        }

        private boolean uploadFavicon(byte @Nullable [] bytes) {
            if (bytes == null) {
                this.icon.destroy();
            } else {
                try {
                    this.icon.load(NativeImage.read(bytes));
                } catch (Throwable var3) {
                    MLPMultiplayerServerListWidget.LOGGER.error("Invalid icon for server {} ({})", this.server.name, this.server.address, var3);
                    return false;
                }
            }

            return true;
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            double d = mouseX - (double) MLPMultiplayerServerListWidget.this.getRowLeft();
            double e = mouseY - (double) MLPMultiplayerServerListWidget.this.getRowTop(MLPMultiplayerServerListWidget.this.children().indexOf(this));
            if (d <= 32.0) {
                if (d < 32.0 && d > 16.0 && this.canConnect()) {
                    this.screen.select(this);
                    this.screen.connect();
                    return true;
                }
            }

            this.screen.select(this);
            if (Util.getMeasuringTimeMs() - this.time < 250L) {
                this.screen.connect();
            }

            this.time = Util.getMeasuringTimeMs();
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public ServerInfo getServer() {
            return this.server;
        }

        @Override
        public Text getNarration() {
            // TODO: this doesnt respect streamer mode
            MutableText mutableText = Text.empty();
            mutableText.append(Text.translatable("narrator.select", this.server.name));
            mutableText.append(ScreenTexts.SENTENCE_SEPARATOR);
            switch (this.server.getStatus()) {
//                case INCOMPATIBLE: {
//                    mutableText.append(INCOMPATIBLE_TEXT);
//                    mutableText.append(ScreenTexts.SENTENCE_SEPARATOR);
//                    mutableText.append(Text.translatable("multiplayer.status.version.narration", this.server.version));
//                    mutableText.append(ScreenTexts.SENTENCE_SEPARATOR);
//                    mutableText.append(Text.translatable("multiplayer.status.motd.narration", this.server.label));
//                    break;
//                }
                case UNREACHABLE: {
                    mutableText.append(NO_CONNECTION_TEXT);
                    break;
                }
                case PINGING: {
                    mutableText.append(PINGING_TEXT);
                    break;
                }
                default: {
                    mutableText.append(ONLINE_TEXT);
                    mutableText.append(ScreenTexts.SENTENCE_SEPARATOR);
                    mutableText.append(Text.translatable("multiplayer.status.ping.narration", this.server.ping));
                    mutableText.append(ScreenTexts.SENTENCE_SEPARATOR);
                    mutableText.append(Text.translatable("multiplayer.status.motd.narration", this.server.label));
                    if (this.server.players == null) break;
                    mutableText.append(Text.translatable("multiplayer.status.player_count.narration", this.server.players.online(), this.server.players.max()));
                    mutableText.append(ScreenTexts.SENTENCE_SEPARATOR);
                    mutableText.append(Texts.join(this.server.playerListSummary, Text.literal(", ")));
                }
            }

            return mutableText;
        }

        public void close() {
            this.icon.close();
        }
    }

    @Environment(EnvType.CLIENT)
    public static class SavedServersDividerEntry extends Entry {
        private final MinecraftClient client = MinecraftClient.getInstance();

        public SavedServersDividerEntry() {
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int var10000 = y + entryHeight / 2;
            Objects.requireNonNull(this.client.textRenderer);
            int i = var10000 - 9 / 2;
            context.drawText(this.client.textRenderer, Text.of("Active Servers"), this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth(Text.of("Active Servers")) / 2, i, Colors.WHITE, false);
            String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
            int var10003 = this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth(string) / 2;
            Objects.requireNonNull(this.client.textRenderer);
            context.drawText(this.client.textRenderer, string, var10003, i + 9, Colors.GRAY, false);
        }

        public Text getNarration() {
            return Text.of("Active Servers");
        }
    }

    @Environment(EnvType.CLIENT)
    public static class DividerEntry extends Entry {
        private final MinecraftClient client = MinecraftClient.getInstance();

        public DividerEntry() {
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int var10000 = y + entryHeight / 2;
            Objects.requireNonNull(this.client.textRenderer);
            int i = var10000 - 9 / 2;
            Text text = Text.of("Server List");
            context.drawText(this.client.textRenderer, text, this.client.currentScreen.width / 2 - this.client.textRenderer.getWidth(text) / 2, i, Colors.WHITE, false);
        }

        public Text getNarration() {
            return Text.of("Active Servers");
        }
    }

    @Environment(EnvType.CLIENT)
    public class SavedServerEntry extends MLPMultiplayerServerListWidget.ServerEntry {

        protected SavedServerEntry(MLPMultiplayerScreen screen, ServerInfo server) {
            super(screen, server);
        }
    }
}
