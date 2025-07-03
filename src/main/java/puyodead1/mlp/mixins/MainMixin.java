package puyodead1.mlp.mixins;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// Modifies the default width and height arguments to better fit the custom title logo
@Mixin(Main.class)
public class MainMixin {
    @Unique
    private static final double scale = 1.3;

    @ModifyArg(method = "main", at = @At(value = "INVOKE", target = "Ljoptsimple/ArgumentAcceptingOptionSpec;defaultsTo(Ljava/lang/Object;[Ljava/lang/Object;)Ljoptsimple/ArgumentAcceptingOptionSpec;", ordinal = 5, remap = false))
    private static Object modifyDefaultWidth(Object value) {
        return (int) Math.ceil(854 * scale);
    }

    @ModifyArg(method = "main", at = @At(value = "INVOKE", target = "Ljoptsimple/ArgumentAcceptingOptionSpec;defaultsTo(Ljava/lang/Object;[Ljava/lang/Object;)Ljoptsimple/ArgumentAcceptingOptionSpec;", ordinal = 6, remap = false))
    private static Object modifyDefaultHeight(Object value) {
        return (int) Math.ceil(480 * scale);
    }
}
