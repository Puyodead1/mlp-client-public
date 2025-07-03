package puyodead1.mlp.utils;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import puyodead1.mlp.MLPService;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;

import java.util.Random;

public class BlockPosUtils {
    public static BlockPos from(MLPService.Position vec) {
        return BlockPosUtils.from(vec.x, vec.y, vec.z);
    }

    public static BlockPos from(Position vec) {
        return BlockPosUtils.from(vec.getX(), vec.getY(), vec.getZ());
    }

    public static BlockPos from(double x, double y, double z) {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public static BlockPos from(float x, float y, float z) {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public static Vec3i pickRandomPos() {
        int x = new Random().nextInt(16777215);
        int y = 255;
        int z = new Random().nextInt(16777215);
        return new Vec3i(x, y, z);
    }

    public static Vec3d pickRandomPos3d() {
        int x = new Random().nextInt(16777215);
        int y = 255;
        int z = new Random().nextInt(16777215);
        return new Vec3d(x, y, z);
    }
}
