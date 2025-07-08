// copied and lightly modified from serverseeker (https://github.com/DAMcraft/MeteorServerSeeker/blob/master/src/main/java/de/damcraft/serverseeker/gui/FindNewServersScreen.java)

package puyodead1.mlp.client.ui;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import puyodead1.mlp.MLPMod;
import puyodead1.mlp.MLPService;
import puyodead1.mlp.client.ui.serverlist.MLPMultiplayerScreen;

public class SearchParametersScreen extends WindowScreen {
    public static NbtCompound savedSettings;
    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<Version> versionSetting = sg.add(new EnumSetting.Builder<Version>().name("version").defaultValue(Version.Current).build());
    private final Setting<Integer> protocolVersionSetting = sg.add(new IntSetting.Builder().name("protocol").defaultValue(SharedConstants.getProtocolVersion()).visible(() -> versionSetting.get() == Version.Protocol).min(0).noSlider().build());
    private final Setting<String> versionStringSetting = sg.add(new StringSetting.Builder().name("version-string").defaultValue(SharedConstants.getGameVersion().name()).visible(() -> versionSetting.get() == Version.VersionString).build());
    private final Setting<Flag> visitedSetting = sg.add(new EnumSetting.Builder<Flag>().name("visited").defaultValue(Flag.ANY).build());
    private final Setting<Flag> moddedSetting = sg.add(new EnumSetting.Builder<Flag>().name("modded").defaultValue(Flag.ANY).build());
    private final Setting<Flag> whitelistedSetting = sg.add(new EnumSetting.Builder<Flag>().name("whitelisted").defaultValue(Flag.ANY).build());
    private final Setting<Flag> crackedSetting = sg.add(new EnumSetting.Builder<Flag>().name("cracked").defaultValue(Flag.ANY).build());
    private final Setting<Flag> griefedSetting = sg.add(new EnumSetting.Builder<Flag>().name("griefed").defaultValue(Flag.ANY).build());
    private final Setting<Flag> savedSetting = sg.add(new EnumSetting.Builder<Flag>().name("saved").defaultValue(Flag.ANY).build());
    private final Setting<Flag> activeSetting = sg.add(new EnumSetting.Builder<Flag>().name("active").defaultValue(Flag.ANY).build());
    private final Setting<Flag> hasHistorySetting = sg.add(new EnumSetting.Builder<Flag>().name("history").defaultValue(Flag.ANY).build());
    private final Setting<Flag> hasNotesSetting = sg.add(new EnumSetting.Builder<Flag>().name("notes").defaultValue(Flag.ANY).build());
    private final Setting<Boolean> searchMotdSetting = sg.add(new BoolSetting.Builder().name("MOTD Search").defaultValue(false).build());
    private final Setting<Flag> motdDefaultSetting = sg.add(new EnumSetting.Builder<Flag>().name("Default").defaultValue(Flag.ANY).visible(searchMotdSetting::get).build());
    private final Setting<Flag> motdCommunitySetting = sg.add(new EnumSetting.Builder<Flag>().name("Community").defaultValue(Flag.ANY).visible(searchMotdSetting::get).build());
    private final Setting<Flag> motdCreativeSetting = sg.add(new EnumSetting.Builder<Flag>().name("Creative").defaultValue(Flag.ANY).visible(searchMotdSetting::get).build());
    private final Setting<Flag> motdBigotrySetting = sg.add(new EnumSetting.Builder<Flag>().name("Bigotry").defaultValue(Flag.ANY).visible(searchMotdSetting::get).build());
    private final Setting<Flag> motdFurrySetting = sg.add(new EnumSetting.Builder<Flag>().name("Furry").defaultValue(Flag.ANY).visible(searchMotdSetting::get).build());
    private final Setting<Flag> motdLgbtSetting = sg.add(new EnumSetting.Builder<Flag>().name("LGBT").defaultValue(Flag.ANY).visible(searchMotdSetting::get).build());
    //    private final Setting<Flag> vanillaSetting = sg.add(new EnumSetting.Builder<Flag>().name("vanilla").defaultValue(Flag.ANY).visible(() -> versionSetting.get().equals(Version.Any)).build());

    private final MLPMultiplayerScreen multiplayerScreen;
    private final MLPService mlpService;
    public WButton searchButton;
    WContainer settingsContainer;

    public SearchParametersScreen(MLPMultiplayerScreen screen, MLPService mlpService) {
        super(GuiThemes.get(), "Search Parameters");
        this.multiplayerScreen = screen;
        this.mlpService = mlpService;
    }

    @Override
    public void initWidgets() {
        loadSettings();
        onClosed(this::saveSettings);
        settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings));
        searchButton = add(theme.button("Search")).expandX().widget();
        add(theme.button("Reset all")).expandX().widget().action = this::resetSettings;
        add(theme.button("Back")).expandX().widget().action = this::close;
        searchButton.action = () -> {
            if (visitedSetting.get().bool == null && griefedSetting.get().bool  == null && moddedSetting.get().bool  == null && savedSetting.get().bool  == null && whitelistedSetting.get().bool  == null && activeSetting.get().bool == null && crackedSetting.get().bool  == null && versionSetting.get().equals(Version.Any) && !searchMotdSetting.get()){
                add(theme.label("Everything searches are not allowed.")).expandX().widget();
                return;
            }

            MLPService.FindServersRequest request = mlpService.currentFindRequest;

            request.search.flags.setVisited(visitedSetting.get());
            request.search.flags.setModded(moddedSetting.get());
            request.search.flags.setCracked(crackedSetting.get());
            request.search.flags.setGriefed(griefedSetting.get());
            request.search.flags.setSaved(savedSetting.get());
            request.search.flags.setActive(activeSetting.get());
            request.search.flags.setWhitelist(whitelistedSetting.get());
            request.search.extra.setHasHistory(hasHistorySetting.get());
            request.search.extra.setHasNotes(hasNotesSetting.get());

            if (searchMotdSetting.get()) {
                request.search.extra.motd.setDefault(motdDefaultSetting.get().bool);
                request.search.extra.motd.setCommunity(motdCommunitySetting.get().bool);
                request.search.extra.motd.setCreative(motdCreativeSetting.get().bool);
                request.search.extra.motd.setBigotry(motdBigotrySetting.get().bool);
                request.search.extra.motd.setFurry(motdFurrySetting.get().bool);
                request.search.extra.motd.setLgbt(motdLgbtSetting.get().bool);
            }

            switch (versionSetting.get()) {
                case Protocol -> request.search.version.setProtocol(protocolVersionSetting.get());
                case VersionString -> request.search.version.setName(versionStringSetting.get());
                case Current -> request.search.version.setProtocol(SharedConstants.getProtocolVersion());
                case Any -> request.search.version.clear();
            }

            this.multiplayerScreen.refreshList();
            if (this.client == null) return;

            if (MLPMod.getMultiplayerScreen() != null) {
                client.setScreen(MLPMod.getMultiplayerScreen());
            } else {
                client.setScreen(this.multiplayerScreen);
            }
        };
    }

    @Override
    public void tick() {
        super.tick();
        settings.tick(settingsContainer, theme);
    }

    public void saveSettings() {
        savedSettings = sg.toTag();
    }

    public void loadSettings() {
        if (savedSettings == null) return;
        sg.fromTag(savedSettings);
    }

    public void resetSettings() {
        for (Setting<?> setting : sg) {
            setting.reset();
        }
        saveSettings();
        reload();
    }

    public enum Flag {
        YES(true),
        NO(false),
        ANY(null);

        public final Boolean bool;
        Flag(Boolean bool){
            this.bool = bool;
        }
    }

    public enum Version {
        Current, Any, Protocol, VersionString;

        @Override
        public String toString() {
            return switch (this) {
                case Current -> "Current";
                case Any -> "Any";
                case Protocol -> "Protocol";
                case VersionString -> "Version String";
            };
        }
    }
}
