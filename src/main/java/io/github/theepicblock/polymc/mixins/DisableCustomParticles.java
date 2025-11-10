package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public class DisableCustomParticles {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacketInject(Packet<?> packet, ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        if (this instanceof ServerPlayerConnection player
                && packet instanceof ClientboundLevelParticlesPacket particlePacket && Util.isPolyMapVanillaLike(player.getPlayer())) {
            var effect = particlePacket.getParticle();
            if (!Util.isVanilla(BuiltInRegistries.PARTICLE_TYPE.getKey(effect.getType()))) {
                ci.cancel();
            }
        }
    }
}
