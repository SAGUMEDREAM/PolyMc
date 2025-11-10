package io.github.theepicblock.polymc.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.impl.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

@Mixin(Registry.class)
public interface RegistryMixin {
    @Shadow Holder<Object> wrapAsHolder(Object value);

    @Shadow Optional<Holder.Reference<Object>> get(int rawId);

    @Shadow int getId(@Nullable Object value);

    @ModifyReturnValue(method = "referenceHolderWithLifecycle", at = @At(value = "RETURN"))
    private Codec<Holder.Reference<Object>> patchCodec(Codec<Holder.Reference<Object>> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread() && content.isBound()) {
                var ctx = PacketContext.get();
                var map = Util.tryGetPolyMap(ctx);
                //noinspection rawtypes
                if (map.canReceiveRegistryEntry((Registry) this, (Holder) content)) {
                    return content;
                }

                var fallback = this.get(0).orElseThrow();
                var val = content.value();
                if (val instanceof Item item) {
                    var client = map.getClientItem(new ItemStack(item), ctx.getPlayer(), null);
                    return this.get(this.getId(client.getItem())).orElse(fallback);
                } else if (val instanceof Block item) {
                    var client = map.getClientState(item.defaultBlockState(), ctx.getPlayer());
                    return this.get(this.getId(client.getBlock())).orElse(fallback);
                } else if (val instanceof SoundEvent) {
                    return this.get(this.getId(SoundEvents.EMPTY)).orElse(fallback);
                }

                return fallback;
            }
            return content;
        });
    }
}
