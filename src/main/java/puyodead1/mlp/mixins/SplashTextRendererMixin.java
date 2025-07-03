package puyodead1.mlp.mixins;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SplashTextRenderer.class)
public class SplashTextRendererMixin {
    @Shadow
    @Final
    private String text;

    /**
     * @author Puyodead1
     * @reason Move splash text down to fit new title logo
     */
    @Overwrite
    public void render(DrawContext context, int screenWidth, TextRenderer textRenderer, float alpha) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float)screenWidth / 2.0F + 250.0F, 100.0F);
        context.getMatrices().rotate(-0.34906584F);
        float f = 1.8F - MathHelper.abs(MathHelper.sin((float)(Util.getMeasuringTimeMs() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) * 0.1F);
        f = f * 100.0F / (float)(textRenderer.getWidth(this.text) + 32);
        context.getMatrices().scale(f, f);
        context.drawCenteredTextWithShadow(textRenderer, this.text, 0, -8, ColorHelper.withAlpha(alpha, -256));
        context.getMatrices().popMatrix();
    }
}
