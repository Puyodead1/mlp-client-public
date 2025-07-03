package puyodead1.mlp.client.ui.serverlist;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.util.thread.SimpleConsecutiveExecutor;
import puyodead1.mlp.MLPService;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MLPServerList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleConsecutiveExecutor IO_EXECUTOR = new SimpleConsecutiveExecutor(Util.getMainWorkerExecutor(), "server-list-io");
    private final MinecraftClient client;
    private final List<ServerInfo> servers = Lists.newArrayList();
    private final List<ServerInfo> hiddenServers = Lists.newArrayList();

    public MLPServerList(MinecraftClient client) {
        this.client = client;
    }

    public void loadFile() {
        try {
            this.servers.clear();
            this.hiddenServers.clear();
            NbtCompound nbtCompound = NbtIo.read(this.client.runDirectory.toPath().resolve("mcsdc_servers.dat"));
            if (nbtCompound == null) {
                return;
            }

            NbtList nbtList = nbtCompound.getList("servers").get();

            for (int i = 0; i < nbtList.size(); ++i) {
                NbtCompound nbtCompound2 = nbtList.getCompound(i).get();
                ServerInfo serverInfo = ServerInfo.fromNbt(nbtCompound2);
                // ensure that any servers we load are properly obfuscated
                serverInfo.name = MLPService.Server.displayForServerAddress(serverInfo.address);
                if (nbtCompound2.getBoolean("hidden").get()) {
                    this.hiddenServers.add(serverInfo);
                }
//                else {
//                    this.servers.add(serverInfo);
//                }
            }
        } catch (Exception var6) {
            LOGGER.error("Couldn't load server list", var6);
        }

    }

    public void saveFile() {
        try {
            NbtList nbtList = new NbtList();
//            Iterator<ServerInfo> var2 = this.servers.iterator();
//
            ServerInfo serverInfo;
            NbtCompound nbtCompound;
//            while (var2.hasNext()) {
//                serverInfo = var2.next();
//                nbtCompound = serverInfo.toNbt();
//                nbtCompound.putBoolean("hidden", false);
//                nbtList.add(nbtCompound);
//            }

            for (ServerInfo hiddenServer : this.hiddenServers) {
                serverInfo = hiddenServer;
                nbtCompound = serverInfo.toNbt();
                nbtCompound.putBoolean("hidden", true);
                nbtList.add(nbtCompound);
            }

            NbtCompound nbtCompound2 = new NbtCompound();
            nbtCompound2.put("servers", nbtList);
            Path path = this.client.runDirectory.toPath();
            Path path2 = Files.createTempFile(path, "mcsdc_servers", ".dat");
            NbtIo.write(nbtCompound2, path2);
            Path path3 = path.resolve("mcsdc_servers.dat_old");
            Path path4 = path.resolve("mcsdc_servers.dat");
            Util.backupAndReplace(path4, path2, path3);
        } catch (Exception var7) {
            LOGGER.error("Couldn't save server list", var7);
        }

    }

    public ServerInfo get(int index) {
        return this.servers.get(index);
    }

    public boolean hasHiddenServer(ServerInfo serverInfo) {
        return this.hiddenServers.stream().anyMatch(x -> x.address.equals(serverInfo.address));
    }

    @Nullable
    public ServerInfo get(String address) {
        Iterator<ServerInfo> var2 = this.servers.iterator();

        ServerInfo serverInfo;
        do {
            if (!var2.hasNext()) {
                var2 = this.hiddenServers.iterator();

                do {
                    if (!var2.hasNext()) {
                        return null;
                    }

                    serverInfo = var2.next();
                } while (!serverInfo.address.equals(address));

                return serverInfo;
            }

            serverInfo = var2.next();
        } while (!serverInfo.address.equals(address));

        return serverInfo;
    }

    @Nullable
    public ServerInfo tryUnhide(String address) {
        for (int i = 0; i < this.hiddenServers.size(); ++i) {
            ServerInfo serverInfo = this.hiddenServers.get(i);
            if (serverInfo.address.equals(address)) {
                this.hiddenServers.remove(i);
                this.servers.add(serverInfo);
                return serverInfo;
            }
        }

        return null;
    }

    public void hide(ServerInfo serverInfo) {
        this.servers.remove(serverInfo);
        this.hiddenServers.add(serverInfo);
    }

    public void remove(ServerInfo serverInfo) {
        if (!this.servers.remove(serverInfo)) {
            this.hiddenServers.remove(serverInfo);
        }
    }

    public void add(ServerInfo serverInfo, boolean hidden) {
        if (hidden) {
            this.hiddenServers.add(0, serverInfo);
        } else {
            this.servers.add(serverInfo);
        }

    }

    public int size() {
        return this.servers.size();
    }

    public void swapEntries(int index1, int index2) {
        ServerInfo serverInfo = this.get(index1);
        this.servers.set(index1, this.get(index2));
        this.servers.set(index2, serverInfo);
        this.saveFile();
    }

    public void set(int index, ServerInfo serverInfo) {
        this.servers.set(index, serverInfo);
    }

    private static boolean replace(ServerInfo serverInfo, List<ServerInfo> serverInfos) {
        for (int i = 0; i < serverInfos.size(); ++i) {
            ServerInfo serverInfo2 = serverInfos.get(i);
            if (serverInfo2.name.equals(serverInfo.name) && serverInfo2.address.equals(serverInfo.address)) {
                serverInfos.set(i, serverInfo);
                return true;
            }
        }

        return false;
    }

    public static void updateServerListEntry(ServerInfo serverInfo) {
        IO_EXECUTOR.send(() -> {
            MLPServerList serverList = new MLPServerList(MinecraftClient.getInstance());
            serverList.loadFile();
            if (!replace(serverInfo, serverList.servers)) {
                replace(serverInfo, serverList.hiddenServers);
            }

            serverList.saveFile();
        });
    }
}
