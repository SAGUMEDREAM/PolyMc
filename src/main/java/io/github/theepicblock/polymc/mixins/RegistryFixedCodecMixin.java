package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(RegistryFixedCodec.class)
public class RegistryFixedCodecMixin {
    @SuppressWarnings("unchecked")
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
                } else if (entry.value() instanceof SoundEvent event && !Util.isVanilla(BuiltInRegistries.SOUND_EVENT.getKey(event))) {
                    return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY);
                } else if (entry.value() instanceof EntityType<?> && !map.canReceiveRegistryEntry(BuiltInRegistries.ENTITY_TYPE, (Holder<EntityType<?>>) entry)) {
                    return EntityType.MARKER.builtInRegistryHolder();
                } else if (entry.value() instanceof Attribute && !map.canReceiveRegistryEntry(BuiltInRegistries.ATTRIBUTE, (Holder<Attribute>) entry)) {
                    return Attributes.SPAWN_REINFORCEMENTS_CHANCE;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return entry;
    }
}
