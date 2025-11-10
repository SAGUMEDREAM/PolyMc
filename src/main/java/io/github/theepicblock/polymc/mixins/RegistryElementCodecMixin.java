package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(RegistryFileCodec.class)
public class RegistryElementCodecMixin {
    @ModifyVariable(
            method = "encode(Lnet/minecraft/core/Holder;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Holder<?> swapEntry(Holder<?> entry) {
        var ctx = PacketContext.get();
        if (ctx.getClientConnection() != null) {
            try {
                var map = Util.tryGetPolyMap(ctx.getClientConnection());

                if (entry.value() instanceof Item item) {
                    return BuiltInRegistries.ITEM.wrapAsHolder(map.getClientItem(item.getDefaultInstance(), ctx.getPlayer(), null).getItem());
                } else if (entry.value() instanceof Block item && map.getBlockPoly(item) != null) {
                    return BuiltInRegistries.BLOCK.wrapAsHolder(map.getBlockPoly(item).getClientBlock(item.defaultBlockState()).getBlock());
                } else if (entry.value() instanceof SoundEvent event && !Util.isVanillaAndRegistered(entry)) {
                    return Holder.direct(event);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return entry;
    }
}
