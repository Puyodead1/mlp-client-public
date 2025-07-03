package puyodead1.mlp.modules.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;
import puyodead1.mlp.modules.MLPAddOn;

public class Watermark extends HudElement {
    public static final HudElementInfo<Watermark> INFO = new HudElementInfo<>(MLPAddOn.MainHud, "MLP Logo", "Shows the MLP logo.", Watermark::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the logo.")
        .defaultValue(0.5)
        .min(0.1)
        .sliderRange(0.1, 1)
        .build()
    );

    private final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder()
        .name("Rainbow")
        .description("Yes rainbow")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> chromaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Rainbow speed")
        .description("The speed of the colors switching")
        .defaultValue(0.09)
        .min(0.01)
        .sliderMax(5)
        .decimalPlaces(2)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color of the background.")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(() -> !chroma.get())
        .build()
    );

    private final Setting<Images> image = sgGeneral.add(new EnumSetting.Builder<Images>()
        .name("Image")
        .description("What image to use.")
        .defaultValue(Images.mlp)
        .build()
    );

    private final Identifier IDENTIFIER = Identifier.of("mlp", image.get().name() + ".png");


    public Watermark() {
        super(INFO);
    }

    private static final RainbowColor RAINBOW = new RainbowColor();

    @Override
    public void setSize(double h, double w) {
        super.setSize(512 * scale.get(), 512 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(1, 1);
        if (!Utils.canUpdate()) return;

        if (chroma.get()) {
            RAINBOW.setSpeed(chromaSpeed.get() / 100);
            renderer.texture(IDENTIFIER, this.getX(), this.getY(), this.getWidth(), this.getHeight(), RAINBOW.getNext());
        } else {
            renderer.texture(IDENTIFIER, this.getX(), this.getY(), this.getWidth(), this.getHeight(), color.get());
        }

//        Renderer2D.TEXTURE.render(null);
    }

    // Make sure image name is the same as put here
    public enum Images {
        mlp, icon, fifthcolumn
    }
}
