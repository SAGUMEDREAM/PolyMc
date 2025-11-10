package io.github.theepicblock.polymc.mixins;


import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/codec/ByteBufCodecs$29", priority = 500)
public abstract class PacketCodecsRegistryMixin {

    @Shadow @Final private ResourceKey val$registryKey;

    @SuppressWarnings({"rawtypes", "ShadowModifiers"})

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Ljava/lang/Object;)V", at = @At("HEAD"), argsOnly = true)
    private Object polymc$changeData(Object val, RegistryFriendlyByteBuf buf) {
        var player = PacketContext.get();
        var map = Util.tryGetPolyMap(player);

        if (val instanceof Holder<?> registryEntry) {
            var value = registryEntry.value();
            var out = map.tryRemapping(value, player);
            if (value != out) {
                return buf.registryAccess().lookupOrThrow(this.val$registryKey).wrapAsHolder(out);
            }
            return val;
        }

        return map.tryRemapping(val, player);
    }
}