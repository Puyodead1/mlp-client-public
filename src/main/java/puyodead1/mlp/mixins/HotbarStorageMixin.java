package puyodead1.mlp.mixins;

import com.mojang.datafixers.DataFixer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import puyodead1.mlp.modules.CreativeHotbarModule;

import java.io.DataInputStream;
import java.io.InputStream;

@Mixin(HotbarStorage.class)
public abstract class HotbarStorageMixin {
    @Unique
    private final Logger LOGGER = LoggerFactory.getLogger(HotbarStorageMixin.class);

    @Final
    @Shadow
    private DataFixer dataFixer;
    @Final
    @Shadow
    private HotbarStorageEntry[] entries;

    @Inject(method = "load", at = @At("HEAD"), cancellable = true)
    private void mlp$loadBuiltInCreativeHotbar(CallbackInfo cb) {
        String HOTBAR_HOTBAR_NBT = "hotbar/hotbar.nbt";

        if(!Modules.get().isActive(CreativeHotbarModule.class)) {
            LOGGER.debug("Module not active");
            return; // to allow the user to use their own hotbar
        } else {
            LOGGER.debug("Module is active");
        }

        try {
            InputStream in = HotbarStorageMixin.class.getClassLoader().getResourceAsStream(HOTBAR_HOTBAR_NBT);
            if (in == null) {
                LOGGER.error("Could not find hotbar/hotbar.nbt");
                return;
            }

            NbtCompound nbtCompound = NbtIo.readCompound(new DataInputStream(in), NbtSizeTracker.ofUnlimitedBytes());
            if (nbtCompound != null) {
                if (!nbtCompound.contains("DataVersion")) {
                    nbtCompound.putInt("DataVersion", 1343);
                }

                int j = NbtHelper.getDataVersion(nbtCompound, 1343);
                nbtCompound = DataFixTypes.HOTBAR.update(this.dataFixer, nbtCompound, j);

                for (int i = 0; i < 9; i++) {
                    this.entries[i] = HotbarStorageEntry.CODEC.parse(NbtOps.INSTANCE, nbtCompound.get(String.valueOf(i))).resultOrPartial(error -> LOGGER.warn("Failed to parse hotbar: {}", error)).orElseGet(HotbarStorageEntry::new);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load creative mode options", e);
        }

        cb.cancel();
    }
}
