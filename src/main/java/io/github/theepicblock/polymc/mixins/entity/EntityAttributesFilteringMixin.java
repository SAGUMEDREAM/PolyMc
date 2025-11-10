package io.github.theepicblock.polymc.mixins.entity;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.TransformingPacketCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;

@Mixin(ClientboundUpdateAttributesPacket.class)
public abstract class EntityAttributesFilteringMixin {
    @SuppressWarnings("UnreachableCode")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Ljava/util/function/BiFunction;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateAttributesPacket> removeUnsupportedAttributes(StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateAttributesPacket> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, packet) -> {
            var map = Util.tryGetPolyMap(PacketContext.get().getClientConnection());
            var p = new ClientboundUpdateAttributesPacket(packet.getEntityId(), List.of());
            var list = p.getValues();
            for (ClientboundUpdateAttributesPacket.AttributeSnapshot entry : packet.getValues()) {
                if (map.canReceiveRegistryEntry(BuiltInRegistries.ATTRIBUTE, entry.attribute())) {
                    list.add(entry);
                }
            }

            return p;
        });
    }
}
