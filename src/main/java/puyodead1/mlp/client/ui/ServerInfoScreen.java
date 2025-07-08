package puyodead1.mlp.client.ui;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import puyodead1.mlp.MLPService;
import puyodead1.mlp.utils.TicketIDGenerator;
import puyodead1.mlp.utils.TimeAgo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ServerInfoScreen extends WindowScreen {
    private final MLPService.Server server;
    private final ServerInfo serverInfo;

    public ServerInfoScreen(MLPService.Server server, ServerInfo serverInfo) {
        super(GuiThemes.get(), "Server Info");
        this.server = server;
        this.serverInfo = serverInfo;
    }

    @Override
    public void initWidgets() {
        Boolean visited = server.status.visited;
        Boolean griefed = server.status.griefed;
        Boolean modded = server.status.modded;
        Boolean whitelisted = server.status.whitelist;
        Boolean cracked = server.status.cracked;

        String version = server.version;
        String notes = server.notes;

        WTable dataTable = add(theme.table()).expandX().widget();
        WTable playersTable = add(theme.table()).expandX().widget();

        dataTable.add(theme.horizontalSeparator("Info")).expandX();
        dataTable.row();

        dataTable.add(
            theme.label("Address:")
        );
        dataTable.add(theme.label(this.server.displayServerAddress()));
        dataTable.add(theme.button("Copy")).widget().action = () -> {
            mc.keyboard.setClipboard(this.server.displayServerAddress());
        };
        dataTable.row();

        String ticketID = TicketIDGenerator.generateTicketID(this.server.address);
        dataTable.add(
            theme.label("ID:")
        );
        dataTable.add(theme.label(ticketID));
        dataTable.add(theme.button("Copy")).widget().action = () -> {
            mc.keyboard.setClipboard(ticketID);
        };
        dataTable.row();

        dataTable.add(theme.label("Version: "));
        dataTable.add(theme.label(version));
        dataTable.row();

        if (serverInfo.label != null) {
            String description = serverInfo.label.getString();
            dataTable.add(theme.label("Description:"));
            if (description.length() > 100) description = description.substring(0, 100) + "...";
            description = description.replace("\n", "\\n");
            description = description.replace("§r", "");
            dataTable.add(theme.label(description));
            dataTable.row();
        }

        if(this.serverInfo.players != null) {
            dataTable.add(theme.label("Online Players: "));
            dataTable.add(theme.label(String.valueOf(serverInfo.players.online())));
            dataTable.row();

            dataTable.add(theme.label("Max Players: "));
            dataTable.add(theme.label(String.valueOf(serverInfo.players.max())));
            dataTable.row();
        }

        if (notes != null) {
            dataTable.add(
                theme.label("Notes")
            );
            dataTable.add(theme.label(notes.strip()));
            dataTable.row();
        }

        dataTable.add(theme.horizontalSeparator("Status")).expandX();
        dataTable.row();

        dataTable.add(theme.label("Visited: "));
        dataTable.add(theme.label(visited == null ? "Unknown" : visited.toString()));
        dataTable.row();

        dataTable.add(theme.label("Griefed: "));
        dataTable.add(theme.label(griefed == null ? "Unknown" : griefed.toString()));
        dataTable.row();

        dataTable.add(theme.label("Modded: "));
        dataTable.add(theme.label(modded == null ? "Unknown" : modded.toString()));
        dataTable.row();

        dataTable.add(theme.label("Whitelisted: "));
        dataTable.add(theme.label(whitelisted == null ? "Unknown" : whitelisted.toString()));
        dataTable.row();

        dataTable.add(theme.label("Cracked: "));
        dataTable.add(theme.label(cracked == null ? "Unknown" : cracked.toString()));
        dataTable.row();

        dataTable.add(theme.label("Last Seen: "));
        dataTable.add(theme.label(TimeAgo.timeAgo(server.last_seen_online)));
        dataTable.row();

        playersTable.add(theme.horizontalSeparator("Historical Players")).expandX();
        playersTable.row();

        if(server.historical != null) {
            playersTable.add(theme.label("Name ")).expandX();
            playersTable.add(theme.label("First Seen ")).expandX();
            playersTable.row();

            for (MLPService.ServerPlayer player : server.historical.reversed()) {
                String name = player.name;
                playersTable.add(theme.label(name + " ")).expandX();
                playersTable.add(theme.label(TimeAgo.playerTimeAgo(player.first_seen) + " ")).expandX();
                playersTable.add(theme.button("Login")).expandX().widget().action = () -> {
                    new CrackedAccount(player.name).login();
                };

                if(mc.world == null) {
                    playersTable.add(theme.button("Login & Join")).expandX().widget().action = () -> {
                        new CrackedAccount(player.name).login();
                        ConnectScreen.connect(this.parent, mc, ServerAddress.parse(serverInfo.address), serverInfo, false, null);
                    };
                }

//                if (server.historical.getLast() != player)
                    playersTable.row();
            }
        } else {
            playersTable.add(theme.label("No historical players found."));
        }
    }
}
