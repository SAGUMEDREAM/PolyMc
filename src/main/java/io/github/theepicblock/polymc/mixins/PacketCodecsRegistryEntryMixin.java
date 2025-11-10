package io.github.theepicblock.polymc.mixins;


import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/codec/ByteBufCodecs$30", priority = 500)
public abstract class PacketCodecsRegistryEntryMixin {
    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/core/Holder;)V", at = @At("HEAD"), argsOnly = true)
    private Holder<?> polymer$changeData(Holder<?> val, RegistryFriendlyByteBuf buf) {
        var player = PacketContext.get();
        var map = Util.tryGetPolyMap(player);
        if (val.value() instanceof SoundEvent soundEvent && !map.canReceiveEntry(BuiltInRegistries.SOUND_EVENT, soundEvent)) {
            return Holder.direct(val.value());
        }

        return val;
    }

}