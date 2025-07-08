/*
 * This file is taken from McsdcMeteor (https://github.com/Nxyi/McsdcMeteor).
 */

package puyodead1.mlp.client.ui;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import puyodead1.mlp.MLPMod;
import puyodead1.mlp.MLPService;
import puyodead1.mlp.client.ui.serverlist.MLPServerInfo;
import puyodead1.mlp.utils.TicketIDGenerator;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TicketIDScreen extends WindowScreen {
    private static TicketIDScreen instance = null;
    private MultiplayerScreen multiplayerScreen;
    private Screen parent;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<String> TicketIDString = sg.add(new StringSetting.Builder()
        .name("ID")
        .description("")
        .defaultValue("")
        .build()
    );

    public static TicketIDScreen instance(Screen parent) {
        if (instance == null) {
            instance = new TicketIDScreen();
        }
        instance.setParent(parent);
        return instance;
    }

    public void setParent(Screen parent) {
        this.parent = parent;
    }

    public TicketIDScreen() {
        super(GuiThemes.get(), "Ticket ID");
    }
    WContainer settingsContainer;
    @Override
    public void initWidgets() {

        WContainer settingsContainer = add(theme.verticalList()).expandX().widget();
        settingsContainer.minWidth = 300;
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("Search")).expandX().widget().action = () -> {
            reload();
            if (TicketIDString.get().isEmpty()){
                add(theme.label("Enter a Ticket ID")).expandX();
                return;
            }

            try { // some error checking since it likes to crash out
                String address = TicketIDGenerator.decodeTicketID(TicketIDString.get());
                if (!TicketIDGenerator.isValidIPv4WithPort(address)){
                    add(theme.label("Invalid Ticket ID")).expandX();
                    return;
                }
                MLPService.ServerSearchRequest request = new MLPService.ServerSearchRequest();
                request.search.setAddress(address);
                MLPService.Server server = MLPMod.getMLPService().searchServer(request);
                System.out.println(server);
                MLPServerInfo serverInfo = new MLPServerInfo(MLPService.Server.displayForServerAddress(address), server);
                mc.execute(() -> {
                    this.client.setScreen(new ServerInfoScreen(server, serverInfo));
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
                add(theme.label("Error")).expandX();
            }
        };
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
