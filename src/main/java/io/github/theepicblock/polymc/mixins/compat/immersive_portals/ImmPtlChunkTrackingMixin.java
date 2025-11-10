package io.github.theepicblock.polymc.mixins.compat.immersive_portals;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.theepicblock.polymc.impl.misc.WatchListener;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;

import java.util.ArrayList;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

@Mixin(ImmPtlChunkTracking.class)
public class ImmPtlChunkTrackingMixin {
    @Inject(method = "lambda$purge$5", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;sendPacket(Lnet/minecraft/network/protocol/Packet;)V"), require = 0)
    private static void unloadChunkOnClient(Map.Entry<ServerPlayer, ImmPtlChunkTracking.PlayerWatchRecord> e, CallbackInfoReturnable<Boolean> cir) {
        removePlayer(e.getKey(), e.getValue());

    }

    @Inject(method = "lambda$forceRemovePlayer$16", at = @At(value = "INVOKE", target = "Lqouteall/imm_ptl/core/network/PacketRedirection;sendRedirectedMessage(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/network/packet/Packet;)V"), require = 0)
    private static void unloadChunkOnClient2(ServerPlayer player, ResourceKey dim, Long2ObjectMap.Entry e, CallbackInfoReturnable<Boolean> cir, @Local ImmPtlChunkTracking.PlayerWatchRecord rec) {
        removePlayer(player, rec);
    }

    @Unique
    private static void removePlayer(ServerPlayer player, ImmPtlChunkTracking.PlayerWatchRecord record) {
        var pos = new ChunkPos(record.chunkPos);
        ((WatchListener)player.level().getServer().getLevel(record.dimension)
                .getChunk(pos.x, pos.z))
                .polymc$removePlayer(player);
    }
}
