package puyodead1.mlp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.SharedConstants;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import puyodead1.mlp.client.ui.SearchParametersScreen;
import puyodead1.mlp.modules.StreamerMode;
import puyodead1.mlp.utils.FlagSerializer;
import puyodead1.mlp.utils.MLPSystem;
import puyodead1.mlp.utils.VersionSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class MLPService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MLPService.class);
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(SearchParametersScreen.Flag.class, new FlagSerializer())
        .registerTypeAdapter(Version.class, new VersionSerializer())
        .create();

    public static final String API_URL = "https://interact.mcsdc.online/api";
    public final FindServersRequest currentFindRequest = new FindServersRequest(true);
    public final Executor executor = Executors.newFixedThreadPool(3, r -> {
        Thread thread = new Thread(r);
        thread.setName("MLPService");
        return thread;
    });
    private final long HTTP_TIMEOUT = 30L;
    private final HttpClient clientDelegate = HttpClient.newBuilder().build();

    public AtomicBoolean loading = new AtomicBoolean(false);
    private ServerInfo currentServer;
    private ServerInfo serverInfo;

    public void find(Consumer<List<Server>> consumer) {
        this.doFind(consumer);
    }

    private void doFind(Consumer<List<Server>> consumer) {
        this.loading.set(true);
        this.executor.execute(() -> {
            try {
                String content = this.post(API_URL, this.currentFindRequest);
                Type type = new TypeToken<List<Server>>(){}.getType();
                List<Server> servers = GSON.fromJson(content, type);
                Collections.shuffle(servers);
                this.loading.set(false);
                consumer.accept(servers);
            } catch (Throwable e) {
                LOGGER.error("MLPService Find Error", e);
                this.loading.set(false);
            }
        });
    }

    public Server searchServer(ServerSearchRequest req) {
        try {
            String content = this.post(API_URL, req);
            LOGGER.debug("MLPService Search Request: {}", content);
            return GSON.fromJson(content, Server.class);
        } catch (Throwable e) {
            LOGGER.error("MLPService searchServer Error", e);
        }

        return null;
    }

    public void update(UpdateServerRequest req, Runnable callback) {
        this.executor.execute(() -> {
            try {
                String content = this.post(API_URL, req);
                // just a status
                LOGGER.debug(content);
//                serverConsumer.accept(GSON.fromJson(content, Server.class));
                if(callback != null) callback.run();
            } catch (Throwable e) {
                LOGGER.error("MLPService Update Error", e);
            }
        });
    }

    public void findHistoricalPlayers(Consumer<FindPlayersResponse> resultConsumer) {
        this.createFindPlayersRequest().ifPresent(req -> this.executor.execute(() -> {
            try {
                String content = this.post(API_URL + "api/servers/findPlayers", req);
                FindPlayersResponse result = GSON.fromJson(content, FindPlayersResponse.class);
                resultConsumer.accept(result);
            } catch (Throwable e) {
                LOGGER.error("MLPService FindPlayers Error", e);
            }
        }));
    }

    public LoginResponse login(LoginRequest req) {
        try {
            String content = this.post(API_URL, req);
            if (content.contains("error")) {
                LoginErrorResponse error = GSON.fromJson(content, LoginErrorResponse.class);
                throw new Exception(error.error);
            }
            return GSON.fromJson(content, LoginResponse.class);
        } catch (Throwable e) {
            LOGGER.error("MLPService Login Error", e);
        }

        return null;
    }


    private String post(String url, Object req) {
        String body = GSON.toJson(req);
        System.out.println(body);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(HTTP_TIMEOUT)).header("Authorization", "Bearer " + MLPSystem.get().accessToken).headers("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return this.execute(request);
    }

    private String httpGet(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(HTTP_TIMEOUT)).header("Authorization", "Bearer " + MLPSystem.get().accessToken).GET().build();
        return this.execute(request);
    }

    private String execute(HttpRequest request) {
        HttpResponse<String> response;
        try {
            response = this.clientDelegate.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        int code = response.statusCode();
        if (code != 200 &&  code != 302) {
            throw new RuntimeException("status: " + code + " body: " + response.body());
        }
        return response.body();
    }

    public Optional<FindPlayersRequest> createFindPlayersRequest() {
        ServerInfo currentServerEntry = mc.getCurrentServerEntry();

        if (currentServerEntry == null) return Optional.empty();
        if (currentServerEntry.equals(this.currentServer)) return Optional.empty();

        this.currentServer = currentServerEntry;

        FindPlayersRequest request = new FindPlayersRequest();
        request.serverAddress = currentServerEntry.address;

        return Optional.of(request);
    }

    public void updateJoined() {
        if (MeteorClient.mc == null) return;

        ServerInfo serverEntry = MeteorClient.mc.getCurrentServerEntry();
        if (serverEntry == null) return;

        UpdateServerRequest req = new UpdateServerRequest();
        req.update.setAddress(serverEntry.address);
        req.update.setJoined(true);

        this.executor.execute(() -> {
            try {
                String content = this.post(API_URL, req);
                LOGGER.debug(content);
                // response is just "status": "ok"
//                serverConsumer.accept(GSON.fromJson(content, Server.class));
            } catch (Throwable e) {
                LOGGER.error("MLPService Update Active Error", e);
            }
        });
    }

    public ServerInfo getLastServerInfo() {
        return this.serverInfo;
    }

    public void setLastServerInfo(ServerInfo currentServerEntry) {
        this.serverInfo = currentServerEntry;
    }

    public static final class Auth {
        public String login;

        public void setLogin(String login) {
            this.login = login;
        }
    }

    public static final class LoginRequest {
        public Auth auth = new Auth();
    }

    public static final class UserData {
        public String name;
        public String token;
        public int perms;
    }

    public static final class LoginResponse {
        public String status;
        public UserData data;
    }

    public static final class LoginErrorResponse {
        String error;
    }

    public static final class UpdateServerFlags {
        public SearchParametersScreen.Flag banned;
        public SearchParametersScreen.Flag griefed;
        public SearchParametersScreen.Flag modded;
        public SearchParametersScreen.Flag save_for_later;
        public SearchParametersScreen.Flag visited;
        public SearchParametersScreen.Flag whitelist;

        public void setBanned(SearchParametersScreen.Flag banned) {
            this.banned = banned;
        }

        public void setGriefed(SearchParametersScreen.Flag griefed) {
            this.griefed = griefed;
        }

        public void setModded(SearchParametersScreen.Flag modded) {
            this.modded = modded;
        }

        public void setSaveForLater(SearchParametersScreen.Flag save_for_later) {
            this.save_for_later = save_for_later;
        }

        public void setVisited(SearchParametersScreen.Flag visited) {
            this.visited = visited;
        }

        public void setWhitelist(SearchParametersScreen.Flag whitelist) {
            this.whitelist = whitelist;
        }
    }

    public static final class SearchFlags {
        public SearchParametersScreen.Flag active = SearchParametersScreen.Flag.ANY;
        public SearchParametersScreen.Flag cracked = SearchParametersScreen.Flag.ANY;
        public SearchParametersScreen.Flag griefed = SearchParametersScreen.Flag.NO;
        public SearchParametersScreen.Flag modded = SearchParametersScreen.Flag.NO;
        public SearchParametersScreen.Flag saved = SearchParametersScreen.Flag.ANY;
        public SearchParametersScreen.Flag visited = SearchParametersScreen.Flag.ANY;
        public SearchParametersScreen.Flag whitelist = SearchParametersScreen.Flag.NO;

        public void setActive(SearchParametersScreen.Flag active) {
            this.active = active;
        }

        public void setCracked(SearchParametersScreen.Flag cracked) {
            this.cracked = cracked;
        }

        public void setGriefed(SearchParametersScreen.Flag griefed) {
            this.griefed = griefed;
        }

        public void setModded(SearchParametersScreen.Flag modded) {
            this.modded = modded;
        }

        public void setSaved(SearchParametersScreen.Flag saved) {
            this.saved = saved;
        }

        public void setVisited(SearchParametersScreen.Flag visited) {
            this.visited = visited;
        }

        public void setWhitelist(SearchParametersScreen.Flag whitelist) {
            this.whitelist = whitelist;
        }
    }

    public static final class Version {
        public Integer protocol = SharedConstants.getGameVersion().protocolVersion();
        public String name;

        public void setProtocol(Integer protocol) {
            this.protocol = protocol;
            this.name = null;
        }

        public void setName(String name) {
            this.name = name;
            this.protocol = null;
        }

        public void clear() {
            this.protocol = null;
            this.name = null;
        }

        public Boolean isNull() {
            return this.protocol == null && this.name == null;
        }
    }

    public static final class ExtraParameterMOTD {
        @SerializedName("default")
        private Boolean default_ = false;
        private Boolean community = false;
        private Boolean creative = false;
        private Boolean bigotry = false;
        private Boolean furry = false;
        private Boolean lgbt = false;

        public void setDefault(Boolean value) {
            this.default_ = value;
        }

        public void setCommunity(Boolean value) {
            this.community = value;
        }

        public void setCreative(Boolean value) {
            this.creative = value;
        }

        public void setBigotry(Boolean value) {
            this.bigotry = value;
        }

        public void setFurry(Boolean value) {
            this.furry = value;
        }

        public void setLgbt(Boolean value) {
            this.lgbt = value;
        }
    }

    public static final class ExtraParameter {
        public SearchParametersScreen.Flag has_history = SearchParametersScreen.Flag.ANY;
        public SearchParametersScreen.Flag has_notes = SearchParametersScreen.Flag.ANY;
        public final ExtraParameterMOTD motd = new ExtraParameterMOTD();

        public void setHasHistory(SearchParametersScreen.Flag has_history) {
            this.has_history = has_history;
        }

        public void setHasNotes(SearchParametersScreen.Flag has_notes) {
            this.has_notes = has_notes;
        }
    }

    public static final class SearchParameters {
        public SearchFlags flags;
        public ExtraParameter extra;
        public Version version;
        private String address;

        public SearchParameters(Boolean flagSearch) {
            if(flagSearch) {
                this.flags = new SearchFlags();
                this.extra = new ExtraParameter();
                this.version = new Version();
            }
        }

        public  void setAddress(String address) {
            this.address = address;
        }
    }

    public static final class FindServersRequest {
        public SearchParameters search;

        public FindServersRequest(Boolean flagSearch) {
            this.search  = new SearchParameters(flagSearch);
        }
    }

    public static final class ServerSearchRequest {
        public SearchParameters search = new SearchParameters(false);
    }

    public static final class UpdateServerParameters {
        public String address;
        public final UpdateServerFlags flags = new UpdateServerFlags();
        public Boolean joined;
        public String notes;

        public void setAddress(String address) {
            this.address = address;
        }
        public void setJoined(Boolean joined) {
            this.joined = joined;
        }
        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static final class UpdateServerRequest {
        public final UpdateServerParameters update = new UpdateServerParameters();
    }

    public static final class Position {
        public static final String OVERWORLD = "OVERWORLD";
        public static final String END = "END";
        public static final String NETHER = "NETHER";

        public double x;
        public double y;
        public double z;
        public String dimension;

        public static Position from(PlayerEntity player) {
            Position location = new Position();
            location.x = player.getPos().x;
            location.y = player.getPos().y;
            location.z = player.getPos().z;
            RegistryKey<World> world = player.getWorld().getRegistryKey();
            String dimension = world == World.OVERWORLD ? OVERWORLD : (world == World.END ? END : (world == World.NETHER ? NETHER : OVERWORLD));
            location.dimension = dimension;
            return location;
        }

        @Override
        public String toString() {
            return "Position{" + "x=" + x + ", y=" + y + ", z=" + z + ", dimension='" + dimension + '\'' + '}';
        }
    }

    public static final class FindPlayersRequest {
        public String serverAddress;
    }

    public static final class ServerStatus {
        public Boolean visited;
        public Boolean griefed;
        public Boolean modded;
        public Boolean whitelist;
        public Boolean banned;
        public Boolean save_for_later;
        public Boolean cracked;
        public Boolean players_online;
    }

    public static final class Server {
        public String address;
        public String version;
        public ServerStatus status;
        public long last_seen_online;
        public long last_scanned;
        public long last_joined;
        public List<ServerPlayer> historical;
        public String notes;

        public static String displayForServerAddress(String serverAddress) {
            StreamerMode streamerMode = Modules.get().get(StreamerMode.class);
            if (streamerMode != null && streamerMode.isActive()) {
                try {
                    int ipOffset = streamerMode.useRandomIpOffset.get() ? new Random().nextInt(1, 254) : 0;
                    int ipHeader = (Integer.parseInt(serverAddress.substring(0, serverAddress.indexOf("."))) + ipOffset) % 255;
                    return ipHeader + ".xxx.xxx.xxx";
                } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
                    return "Server";
                }
            }
            return serverAddress;
        }

        public String displayServerAddress() {
            return Server.displayForServerAddress(this.address);
        }

//        public String displayDescription() {
//            if (StreamerMode.isHideServerInfoEnabled()) return "No peeking ;)";
//            if (this.description == null) return "";
//            return this.description.replaceAll("[\ud83c\udf00-\ud83d\ude4f]|[\ud83d\ude80-\ud83d\udeff]", "");
//        }

//        public Optional<String> iconData() {
//            if (this.icon != null && this.icon.startsWith("data:image/png;base64,")) {
//                return Optional.of(this.icon.substring("data:image/png;base64,".length()));
//            }
//            return Optional.empty();
//        }
    }

    public static class FindPlayersResponse {
        public Boolean isCracked;
        public List<ServerPlayer> players;
    }

    public static final class ServerPlayer {
        public String uuid;
        public String name;
        public long first_seen;
    }
}
