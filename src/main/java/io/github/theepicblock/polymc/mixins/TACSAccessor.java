package io.github.theepicblock.polymc.mixins;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface TACSAccessor {
    @Accessor
    int getServerViewDistance();

    @Accessor
    Int2ObjectMap<ChunkMap.TrackedEntity> getEntityMap();
}
