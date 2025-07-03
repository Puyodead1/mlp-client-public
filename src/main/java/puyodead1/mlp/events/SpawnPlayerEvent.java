package puyodead1.mlp.events;

import java.util.UUID;
import net.minecraft.util.math.BlockPos;

public record SpawnPlayerEvent(UUID uuid, BlockPos blockPos) { }
