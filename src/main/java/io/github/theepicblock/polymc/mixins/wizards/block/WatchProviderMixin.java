package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public abstract class WatchProviderMixin {
    @Inject(method = "sendChunk",
            at = @At("HEAD"))
    private static void onSendChunkData(ServerGamePacketListenerImpl handler, ServerLevel world, LevelChunk chunk, CallbackInfo ci) {
        ((WatchListener)chunk).polymc$addPlayer(handler.player);
    }

    @Inject(method = "dropChunk",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void onSendUnloadPacket(ServerPlayer player, ChunkPos pos, CallbackInfo ci) {
        var chunk = player.level().getChunkSource().getChunkForLighting(pos.x, pos.z);
        if (!(chunk instanceof WatchListener)) return;

        ((WatchListener)chunk).polymc$removePlayer(player);
    }
}
