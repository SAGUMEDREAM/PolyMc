package io.github.theepicblock.polymc.mixins.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

@Mixin(ChunkMap.TrackedEntity.class)
public interface EntityTrackerAccessor {
    @Accessor
    ServerEntity getServerEntity();

    @Accessor
    Set<ServerPlayerConnection> getSeenBy();

    @Accessor
    Entity getEntity();
}
