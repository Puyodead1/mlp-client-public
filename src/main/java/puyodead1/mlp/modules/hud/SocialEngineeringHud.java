package puyodead1.mlp.modules.hud;

import puyodead1.mlp.MLPMod;
import puyodead1.mlp.MLPService;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SocialEngineeringHud extends HudElement {
    public static final HudElementInfo<SocialEngineeringHud> INFO = new HudElementInfo<>(Hud.GROUP, "Social engineering", "Im friends with Chris", SocialEngineeringHud::new);

    private static final Color RED = new Color(255, 0, 0);

    private final List<MLPService.ServerPlayer> playerList = new ArrayList<>();

    private final MLPService mlpService = MLPMod.getMLPService();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private boolean isCracked;

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<SettingColor> textColor = this.sgGeneral.add(new ColorSetting.Builder().name("text-color").description("A.").defaultValue(new SettingColor()).build());

    public SocialEngineeringHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        super.tick(renderer);

        double width = renderer.textWidth("Historical Players:");
        double height = renderer.textHeight();
        if (this.mc.world == null) {
            this.box.setSize(width, height);
            return;
        }

        List<MLPService.ServerPlayer> list = this.playerList;
        synchronized (list) {
            for (MLPService.ServerPlayer entity : this.playerList) {
                String text = entity.name;

                width = Math.max(width, renderer.textWidth(text));
                height += renderer.textHeight() + 2.0;
            }
        }

        this.box.setSize(width, height);

        this.mlpService.findHistoricalPlayers(response -> {
            synchronized (list) {
                this.playerList.clear();
                this.playerList.addAll(this.filterList(response.players));
//                this.isCracked = this.playerList.stream().anyMatch(serverPlayer -> !Input.isValidMinecraftUsername(serverPlayer.name));
                if (response.isCracked != null) this.isCracked = !this.isCracked;
                else this.isCracked = false;
            }
        });
    }

    private List<MLPService.ServerPlayer> filterList(List<MLPService.ServerPlayer> list) {
        Stream<MLPService.ServerPlayer> playerListStream = list.stream();
        return playerListStream.limit(20L).peek(player -> {
            // Convert first_seen to a human-readable format, taken from MeteorServerSeeker (https://github.com/DAMcraft/MeteorServerSeeker/blob/master/src/main/java/de/damcraft/serverseeker/hud/HistoricPlayersHud.java#L128-L150)
            String unit = "s";
            double firstSeen = (int) (System.currentTimeMillis() / 1000 - player.first_seen);
            if (firstSeen >= 60) {
                firstSeen /= 60;
                unit = "min";
            }
            if (firstSeen >= 60 && unit.equals("min")) {
                firstSeen /= 60;
                unit = "h";
            }
            if (firstSeen >= 24 && unit.equals("h")) {
                firstSeen /= 24;
                unit = firstSeen == 1 ? " day" : " days";
            }
            if (firstSeen >= 30 && unit.equals(" days")) {
                firstSeen /= 30;
                unit = firstSeen == 1 ? " month" : " months";
            }
            if (firstSeen >= 12 && (unit.equals(" months"))) {
                firstSeen /= 12;
                unit = firstSeen == 1 ? " year" : " years";
            }
            // Round to 1 decimal place
            firstSeen = Math.round(firstSeen * 10) / 10.0;

            player.name = String.format("%s (%s%s)", player.name, firstSeen, unit);
        }).collect(Collectors.toList());
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;
        renderer.text("Historical Players " + (this.isCracked ? "(cracked)" : ""), x, y, this.isCracked ? RED : this.textColor.get(), true);

        if (this.mc.world == null) return;

        synchronized (this.playerList) {
            for (MLPService.ServerPlayer entity : this.playerList) {
                x = this.x;
                String text = entity.name;
//                Color color = entity.isValid == null ? Color.ORANGE : (entity.isValid ? Color.GREEN : Color.RED);
                renderer.text(text, x, y += renderer.textHeight() + 2.0, this.textColor.get(), true);
            }
        }
    }
}
