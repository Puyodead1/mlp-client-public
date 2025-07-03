package puyodead1.mlp.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.*;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(LogoDrawer.class)
public class LogoDrawerMixin {
    @Unique
    private static final Identifier MLP_TITLE = Identifier.of("mlp:title.png");
    @Shadow
    @Final
    private boolean ignoreAlpha;

    /**
     * @author Puyodead1
     * @reason Custom Title Image
     */
    @Overwrite
    public void draw(DrawContext context, int screenWidth, float alpha, int y) {
        int texWidth = 1021 / 2;
        int texHeight = 77 / 2;
        int yOffset = 40;

        int j = ColorHelper.getWhite(this.ignoreAlpha ? 1.0f : alpha);
        int i = (screenWidth / 2) - (texWidth / 2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, MLP_TITLE, i, y + yOffset, 0.0f, 0.0f, texWidth, texHeight, texWidth, texHeight, j);
    }
}
