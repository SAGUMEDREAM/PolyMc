package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Minecraft sends sound packets in 2 different ways. Using {@link ClientboundSoundPacket}
 * The former uses a numeric id and the latter an {@link net.minecraft.resources.ResourceLocation}.
 * We should use the latter for non-vanilla sounds. As the client does not have a numeric representation for them.
 */
@Mixin(ClientboundSoundPacket.class)
public class SoundPacketFix {
    @ModifyArg(method = "write", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V"))
    private Object replaceSound(Object entry) {
        var entryT = (Holder<SoundEvent>)entry;
        if (entryT.kind() == Holder.Kind.REFERENCE && Util.isPolyMapVanillaLike(PacketContext.get().getClientConnection()) && !Util.isVanilla(entryT.unwrapKey().get().location())) {
            return Holder.direct(entryT.value());
        }
        return entry;
    }
}
