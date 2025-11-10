package io.github.theepicblock.polymc.mixins.block;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.mixins.TACSAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

@Mixin(ChunkHolder.class)
public abstract class FixLighting extends GenerationChunkHolder {

    @Shadow @Final private ChunkHolder.PlayerProvider playerProvider;

    public FixLighting(ChunkPos pos) {
        super(pos);
    }

    /**
     * Minecraft usually only sends lighting packets when a chunk is on the watch distance edge.
     * This mixin forces lighting packets to be sent regardless, to make sure vanilla clients are kept in sync.
     */
    @Redirect(method = "broadcastChanges", at = @At(value="INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder$PlayerProvider;getPlayers(Lnet/minecraft/world/level/ChunkPos;Z)Ljava/util/List;"))
    private List<ServerPlayer> onGetPlayersWatchingChunk(ChunkHolder.PlayerProvider watchProvider, ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge) {

        // Get all the watchers anyway
        List<ServerPlayer> watchers = watchProvider.getPlayers(this.pos, false);

        if (onlyOnWatchDistanceEdge == false) {
            // This will be sent to everyone regardless. Just use the normal method
            return watchers;
        }

        if (!(watchProvider instanceof TACSAccessor accessor)) {
            // Safety in case someone replaces the provider (TODO: should probably warn)
            return watchers;
        }

        return watchers.stream()
                .filter(watcher -> {
                    var polymap = Util.tryGetPolyMap(watcher);
                    if (polymap.isVanillaLikeMap()) {
                        // Always update vanilla clients
                        return true;
                    }

                    var isOnEdge = watcher.getChunkTrackingView().contains(pos) && !watcher.getChunkTrackingView().isInViewDistance(pos.x, pos.z);

                    return isOnEdge;
                })
                .toList();
    }
}
